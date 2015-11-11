package org.xmind.cathy.internal.dashboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.xmind.cathy.internal.WorkbenchMessages;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.event.KeyEvent;
import org.xmind.gef.util.Properties;
import org.xmind.ui.gallery.GalleryLayout;
import org.xmind.ui.gallery.GallerySelectTool;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.internal.ITemplateDescriptor;
import org.xmind.ui.internal.ITemplateManagerListener;
import org.xmind.ui.internal.MindMapTemplateManager;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.internal.wizards.FileTemplateDescriptor;
import org.xmind.ui.internal.wizards.TemplateLabelProvider;
import org.xmind.ui.mindmap.MindMapUI;

public class NewFromTemplatesDashboardPage extends DashboardPage
        implements ITemplateManagerListener {

    private static final int FRAME_WIDTH = 225;
    private static final int FRAME_HEIGHT = 130;

    private class TemplateGallerySelectTool extends GallerySelectTool {
        @Override
        protected boolean handleKeyUp(KeyEvent ke) {
            int state = ke.getState();
            int key = ke.keyCode;
            if (state == 0 && key == SWT.DEL) {
                ISelection selection = viewer.getSelection();
                if (selection instanceof IStructuredSelection) {
                    Object element = ((IStructuredSelection) selection)
                            .getFirstElement();
                    if (element instanceof ITemplateDescriptor) {
                        ITemplateDescriptor template = (ITemplateDescriptor) element;
                        if (template instanceof FileTemplateDescriptor) {
                            if (MessageDialog.openConfirm(
                                    viewer.getControl().getShell(),
                                    WorkbenchMessages.ConfirmDeleteTemplateDialog_title,
                                    NLS.bind(
                                            WorkbenchMessages.ConfirmDeleteTemplateDialog_message_withTemplateName,
                                            template.getName())))
                                MindMapTemplateManager.getInstance()
                                        .removeTemplate(template);
                        }
                    }
                }
            }
            return super.handleKeyUp(ke);
        }
    }

    private GalleryViewer viewer;
    private boolean normalOrEditMode;

    public void setFocus() {
        if (viewer != null && viewer.getControl() != null
                && !viewer.getControl().isDisposed()) {
            viewer.getControl().setFocus();
        }
    }

    @Override
    public void dispose() {
        MindMapTemplateManager.getInstance()
                .removeTemplateManagerListener(this);
        super.dispose();
    }

    public void setState(boolean normalOrEditMode) {
        this.normalOrEditMode = normalOrEditMode;
    }

    public void createControl(Composite parent) {
        viewer = new GalleryViewer();

        EditDomain editDomain = new EditDomain();
        editDomain.installTool(GEF.TOOL_SELECT,
                new TemplateGallerySelectTool());
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
        properties.set(GalleryViewer.ImageStretched, true);
        properties.set(GalleryViewer.Layout,
                new GalleryLayout(GalleryLayout.ALIGN_CENTER,
                        GalleryLayout.ALIGN_TOPLEFT, 10, 10,
                        new Insets(5, 15, 5, 15)));
        properties.set(GalleryViewer.FrameContentSize,
                new Dimension(FRAME_WIDTH, FRAME_HEIGHT));

        Control control = viewer.createControl(parent);
        control.setBackground(parent.getBackground());
        control.setForeground(parent.getForeground());

        viewer.setLabelProvider(new TemplateLabelProvider());

        viewer.setInput(getViewerInput());

        viewer.addOpenListener(new IOpenListener() {
            public void open(OpenEvent event) {
                if (normalOrEditMode)
                    handleTemplateSelected(event.getSelection());
            }
        });

        MindMapTemplateManager.getInstance().addTemplateManagerListener(this);

        setControl(control);
    }

    public void addSelectionChangedListener(
            ISelectionChangedListener listener) {
        if (viewer != null) {
            viewer.addSelectionChangedListener(listener);
        }
    }

    private Object getViewerInput() {
        List<ITemplateDescriptor> templates = new ArrayList<ITemplateDescriptor>();
        MindMapTemplateManager templateManager = MindMapTemplateManager
                .getInstance();
        templates.addAll(templateManager.loadCustomTemplates());
        templates.addAll(templateManager.loadSystemTemplates());
        // move recently added template ahead
        Collections.reverse(templates);
        return templates;
    }

    public void templateAdded(ITemplateDescriptor template) {
        if (viewer == null || viewer.getControl() == null
                || viewer.getControl().isDisposed())
            return;
        viewer.setInput(getViewerInput());
    }

    public void templateRemoved(ITemplateDescriptor template) {
        if (viewer == null || viewer.getControl() == null
                || viewer.getControl().isDisposed())
            return;
        viewer.setInput(getViewerInput());
    }

    private void handleTemplateSelected(ISelection selection) {
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

        final ITemplateDescriptor template = (ITemplateDescriptor) selectedElement;
        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                IWorkbenchWindow wbWindow = PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow();
                if (wbWindow == null)
                    return;

                IWorkbenchPage wbPage = wbWindow.getActivePage();
                if (wbPage == null)
                    return;

                IEditorInput editorInput = MME
                        .createTemplatedEditorInput(template.newStream());
                wbPage.openEditor(editorInput, MindMapUI.MINDMAP_EDITOR_ID);

                hideDashboard();
            }
        });
    }

}
