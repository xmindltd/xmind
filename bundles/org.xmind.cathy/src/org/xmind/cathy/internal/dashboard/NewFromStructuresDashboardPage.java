package org.xmind.cathy.internal.dashboard;

import org.eclipse.draw2d.geometry.Insets;
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
import org.xmind.cathy.internal.dashboard.StructureListContentProvider.StructureDescriptor;
import org.xmind.core.style.IStyle;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.util.Properties;
import org.xmind.ui.gallery.GalleryLayout;
import org.xmind.ui.gallery.GallerySelectTool;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.internal.dashboard.pages.DashboardPage;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.mindmap.WorkbookInitializer;
import org.xmind.ui.resources.ColorUtils;

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
        properties.set(GalleryViewer.ContentPaneBorderWidth, 1);
        properties.set(GalleryViewer.ContentPaneBorderColor,
                ColorUtils.getColor("#cccccc"));

        Control control = viewer.createControl(parent);
        control.setBackground(parent.getBackground());
        control.setForeground(parent.getForeground());

        StructureListContentProvider contentAndLabelProvider = new StructureListContentProvider();
        viewer.setContentProvider(contentAndLabelProvider);
        viewer.setLabelProvider(
                new StructureListContentProvider.StructureListLabelProvider());

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
                || !(selectedElement instanceof StructureDescriptor))
            return;

        final StructureDescriptor structure = (StructureDescriptor) selectedElement;
        final IStyle theme = chooseTheme(viewer.getControl().getShell());
        if (theme == null)
            return;

        WorkbookInitializer initializer = WorkbookInitializer.getDefault()
                .withStructureClass(structure.getValue()).withTheme(theme);
        IEditorInput editorInput = MindMapUI.getEditorInputFactory()
                .createEditorInputForWorkbookInitializer(initializer, null);
        getContext().openEditor(editorInput, MindMapUI.MINDMAP_EDITOR_ID);
    }

    private IStyle chooseTheme(Shell shell) {
        ThemeChooserDialog dialog = new ThemeChooserDialog(shell);
        int result = dialog.open();
        if (result == ThemeChooserDialog.CANCEL)
            return null;
        return dialog.getSelectedTheme();
    }

}
