package org.xmind.cathy.internal.dashboard;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.IWorkbook;
import org.xmind.core.io.ByteArrayStorage;
import org.xmind.core.style.IStyle;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.util.Properties;
import org.xmind.ui.gallery.GalleryLayout;
import org.xmind.ui.gallery.GallerySelectTool;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.internal.ITemplateDescriptor;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.internal.wizards.TemplateLabelProvider;
import org.xmind.ui.mindmap.MindMapUI;

public class NewFromStructuresDashboardPage extends DashboardPage {

    private GalleryViewer viewer;

    public void setFocus() {
        if (viewer != null && viewer.getControl() != null
                && !viewer.getControl().isDisposed()) {
            viewer.getControl().setFocus();
        }
    }

    public void createControl(Composite parent) {
        viewer = new GalleryViewer();

        EditDomain editDomain = new EditDomain();
        editDomain.installTool(GEF.TOOL_SELECT, new GallerySelectTool());
        viewer.setEditDomain(editDomain);

        Properties properties = viewer.getProperties();
        properties.set(GalleryViewer.Horizontal, Boolean.TRUE);
        properties.set(GalleryViewer.Wrap, Boolean.TRUE);
        properties.set(GalleryViewer.TitlePlacement,
                GalleryViewer.TITLE_BOTTOM);
        properties.set(GalleryViewer.SingleClickToOpen, Boolean.TRUE);
        properties.set(GalleryViewer.SolidFrames, true);
        properties.set(GalleryViewer.FlatFrames, true);
        properties.set(GalleryViewer.ImageConstrained, true);
        properties.set(GalleryViewer.Layout,
                new GalleryLayout(GalleryLayout.ALIGN_CENTER,
                        GalleryLayout.ALIGN_TOPLEFT, 10, 10,
                        new Insets(5, 15, 5, 15)));

        Control control = viewer.createControl(parent);
        control.setBackground(parent.getBackground());
        control.setForeground(parent.getForeground());

        StructureListContentProvider contentProvider = new StructureListContentProvider();
        viewer.setContentProvider(contentProvider);
        viewer.setLabelProvider(new TemplateLabelProvider());

        viewer.setInput(StructureListContentProvider.getDefaultInput());

        viewer.addOpenListener(new IOpenListener() {
            public void open(OpenEvent event) {
                handleStructureSelected(event.getSelection());
            }
        });

        setControl(control);
    }

    private void handleStructureSelected(ISelection selection) {
        Display.getCurrent().asyncExec(new Runnable() {
            public void run() {
                if (viewer == null || viewer.getControl() == null
                        || viewer.getControl().isDisposed())
                    return;

                viewer.setSelection(StructuredSelection.EMPTY);
            }
        });

        if (selection == null || selection.isEmpty()
                || !(selection instanceof IStructuredSelection))
            return;

        Object selectedElement = ((IStructuredSelection) selection)
                .getFirstElement();
        if (selectedElement == null
                || !(selectedElement instanceof ITemplateDescriptor))
            return;

        final ITemplateDescriptor primaryTemplate = (ITemplateDescriptor) selectedElement;
        final IStyle theme = chooseTheme(viewer.getControl().getShell());
        if (theme == null)
            return;

        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                byte[] workbookData = makeTemplateWorkbookData(primaryTemplate,
                        theme);
                openWorkbookEditorForData(workbookData);
            }
        });
    }

    private byte[] makeTemplateWorkbookData(
            final ITemplateDescriptor primaryTemplate, final IStyle theme)
                    throws IOException, CoreException {
        IWorkbook tempWorkbook;
        InputStream primaryStream = primaryTemplate.newStream();
        try {
            tempWorkbook = Core.getWorkbookBuilder()
                    .loadFromStream(primaryStream, new ByteArrayStorage());
        } finally {
            primaryStream.close();
        }

        if (theme == MindMapUI.getResourceManager().getBlankTheme()) {
            tempWorkbook.getPrimarySheet().setThemeId(null);
        } else {
            IStyle importedTheme = tempWorkbook.getStyleSheet()
                    .importStyle(theme);
            if (importedTheme == null) {
                tempWorkbook.getPrimarySheet().setThemeId(null);
            } else {
                tempWorkbook.getPrimarySheet()
                        .setThemeId(importedTheme.getId());
            }
        }

        ByteArrayOutputStream tempStream = new ByteArrayOutputStream();
        try {
            tempWorkbook.save(tempStream);
        } finally {
            tempStream.close();
        }

        return tempStream.toByteArray();
    }

    private void openWorkbookEditorForData(byte[] data)
            throws PartInitException {
        IWorkbenchWindow wbWindow = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        if (wbWindow == null)
            return;

        IWorkbenchPage wbPage = wbWindow.getActivePage();
        if (wbPage == null)
            return;

        IEditorInput editorInput = MME
                .createTemplatedEditorInput(new ByteArrayInputStream(data));
        wbPage.openEditor(editorInput, MindMapUI.MINDMAP_EDITOR_ID);

        hideDashboard();
    }

    private IStyle chooseTheme(Shell shell) {
        ThemeChooserDialog dialog = new ThemeChooserDialog(shell);
        int result = dialog.open();
        if (result == ThemeChooserDialog.CANCEL)
            return null;
        return dialog.getSelectedTheme();
    }

}
