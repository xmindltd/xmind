package org.xmind.cathy.internal.dashboard;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.xmind.cathy.internal.ICathyConstants;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.dashboard.pages.DashboardPage;
import org.xmind.ui.internal.dashboard.pages.IDashboardContext;
import org.xmind.ui.mindmap.IResourceManagerListener;
import org.xmind.ui.mindmap.ITemplate;
import org.xmind.ui.mindmap.MindMapUI;

public class NewFromTemplatesDashboardPage extends DashboardPage
        implements IResourceManagerListener, IAdaptable {

    private CategorizedTemplateViewer viewer;

    private boolean templateOpening;

    public void setFocus() {
        if (viewer != null && viewer.getControl() != null
                && !viewer.getControl().isDisposed()) {
            viewer.getControl().setFocus();
        }
    }

    @Override
    public void dispose() {
        MindMapUI.getResourceManager().removeResourceManagerListener(this);
        super.dispose();
    }

    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginLeft = 60;
        layout.marginRight = 0;
        layout.marginHeight = 7;
        container.setLayout(layout);

        MindMapUIPlugin.getDefault().getUsageDataCollector()
                .trackView(UserDataConstants.VIEW_TEMPLATES);
        viewer = new CategorizedTemplateViewer(container);
        Control control = viewer.getControl();
        control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        viewer.addOpenListener(new IOpenListener() {
            public void open(OpenEvent event) {
                if (!templateOpening) {
                    handleTemplateSelected(event.getSelection());
                    MindMapUIPlugin.getDefault().getUsageDataCollector()
                            .trackEvent(UserDataConstants.CATEGORY_WORKBOOK,
                                    UserDataConstants.CREATE_WORKBOOK);
                    MindMapUIPlugin.getDefault().getUsageDataCollector()
                            .trackEvent(UserDataConstants.CATEGORY_TEMPLATE,
                                    UserDataConstants.USE_TEMPLATES);
                }
            }
        });

        MindMapUI.getResourceManager().addResourceManagerListener(this);

        registerContextMenu(control);
        setControl(container);
    }

    public void registerAvailableCommands() {
        IDashboardContext context = getContext();

        context.registerAvailableCommandId(
                ICathyConstants.COMMAND_TEMPLATE_DUPLICATE);
        context.registerAvailableCommandId(
                ICathyConstants.COMMAND_TEMPLATE_RENAME);
        context.registerAvailableCommandId(
                ICathyConstants.COMMAND_TEMPLATE_DELETE);
    }

    private void registerContextMenu(Control control) {
        getContext().registerContextMenu(control,
                ICathyConstants.POPUP_TEMPLATE);

        //add context menu for nested viewers' control.
        Object input = viewer.getInput();
        if (input instanceof Object[]) {
            Object[] groups = (Object[]) viewer.getInput();
            for (Object group : groups) {
                GalleryViewer nestedViewer = viewer.getNestedViewer(group);
                if (nestedViewer != null) {
                    nestedViewer.getControl().setMenu(control.getMenu());
                }
            }
        }
    }

    public void addSelectionChangedListener(
            ISelectionChangedListener listener) {
        if (viewer != null) {
            viewer.addSelectionChangedListener(listener);
        }
    }

    public void userTemplateAdded(ITemplate template) {
        if (viewer != null) {
            viewer.userTemplateAdded(template);
        }
    }

    public void userTemplateRemoved(ITemplate template) {
        if (viewer != null) {
            viewer.userTemplateRemoved(template);
        }
    }

    private void handleTemplateSelected(ISelection selection) {
        templateOpening = true;
        Display.getCurrent().asyncExec(new Runnable() {
            public void run() {
                if (viewer == null || viewer.getControl() == null
                        || viewer.getControl().isDisposed())
                    return;

                viewer.setSelection(StructuredSelection.EMPTY);
            }
        });

        if (selection == null || selection.isEmpty()
                || !(selection instanceof IStructuredSelection)) {
            templateOpening = false;
            return;
        }

        Object selectedElement = ((IStructuredSelection) selection)
                .getFirstElement();
        if (selectedElement == null || !(selectedElement instanceof ITemplate))
            return;

        ITemplate template = (ITemplate) selectedElement;
        if (template != null && null != template.getName())
            MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                    UserDataConstants.CATEGORY_TEMPLATE,
                    String.format(UserDataConstants.USE_TEMPLATE_S,
                            template.getName().replaceAll(" ", "_"))); //$NON-NLS-1$ //$NON-NLS-2$

        IEditorInput editorInput = MindMapUI.getEditorInputFactory()
                .createEditorInput(template.createWorkbookRef());
        getContext().openEditor(editorInput, MindMapUI.MINDMAP_EDITOR_ID);

        templateOpening = false;
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
