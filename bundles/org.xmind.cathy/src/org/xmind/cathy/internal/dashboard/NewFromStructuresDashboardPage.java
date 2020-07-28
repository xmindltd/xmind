package org.xmind.cathy.internal.dashboard;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.xmind.cathy.internal.dashboard.StructureListContentProvider.StructureDescriptor;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.core.style.IStyle;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.ui.internal.SpaceCollaborativeEngine;
import org.xmind.gef.util.Properties;
import org.xmind.ui.gallery.GalleryLayout;
import org.xmind.ui.gallery.GallerySelectTool;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.dashboard.pages.DashboardPage;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.mindmap.WorkbookInitializer;
import org.xmind.ui.resources.ColorUtils;

@SuppressWarnings("restriction")
public class NewFromStructuresDashboardPage extends DashboardPage
        implements IAdaptable {

    private static final int FRAME_WIDTH = 210;
    private static final int FRAME_HEIGHT = 130;

    private GalleryViewer viewer;

    private ResourceManager resources;

    public void setFocus() {
        if (viewer != null && viewer.getControl() != null
                && !viewer.getControl().isDisposed()) {
            viewer.getControl().setFocus();
        }
    }

    public void createControl(Composite parent) {
        resources = new LocalResourceManager(JFaceResources.getResources(),
                parent);

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
        properties.set(GalleryViewer.ImageStretched, Boolean.TRUE);
        properties.set(GalleryViewer.ContentPaneBorderWidth, 1);
        properties.set(GalleryViewer.ContentPaneBorderColor,
                (Color) resources.get(ColorUtils.toDescriptor("#cccccc"))); //$NON-NLS-1$

        properties.set(GalleryViewer.FrameContentSize,
                new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        properties.set(GalleryViewer.Layout,
                new GalleryLayout(GalleryLayout.ALIGN_TOPLEFT,
                        GalleryLayout.ALIGN_TOPLEFT, 30, 0,
                        new Insets(10, 65, 20, 65)));

        properties.set(GalleryViewer.ContentPaneSpaceCollaborativeEngine,
                new SpaceCollaborativeEngine());

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
        final IStyle theme = chooseTheme(viewer.getControl().getShell(),
                structure.getValue());
        if (theme == null)
            return;

        MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                UserDataConstants.CATEGORY_WORKBOOK,
                UserDataConstants.CREATE_WORKBOOK);
        MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                UserDataConstants.CATEGORY_SHEET,
                UserDataConstants.CREATE_SHEET);

        String vs = structure.getValue();
        String ID = vs.replaceAll("\\.", "_");  //$NON-NLS-1$//$NON-NLS-2$
        MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                UserDataConstants.CATEGORY_STRUCTURE,
                String.format(UserDataConstants.USE_STRUCTURE_TYPE_S, ID));
        WorkbookInitializer initializer = WorkbookInitializer.getDefault()
                .withStructureClass(structure.getValue()).withTheme(theme);
        IEditorInput editorInput = MindMapUI.getEditorInputFactory()
                .createEditorInputForWorkbookInitializer(initializer, null);
        getContext().openEditor(editorInput, MindMapUI.MINDMAP_EDITOR_ID);
    }

    private IStyle chooseTheme(Shell shell, String structureClass) {
        ThemeChooserDialog dialog = new ThemeChooserDialog(shell,
                structureClass);
        int result = dialog.open();
        if (result == ThemeChooserDialog.CANCEL)
            return null;
        return dialog.getSelectedTheme();
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (viewer != null) {
            if (adapter.isAssignableFrom(viewer.getClass()))
                return adapter.cast(viewer);
            T obj = viewer.getAdapter(adapter);
            if (obj != null)
                return obj;
        }
        return null;
    }

}
