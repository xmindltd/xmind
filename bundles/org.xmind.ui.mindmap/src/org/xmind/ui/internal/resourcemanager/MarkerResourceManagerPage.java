package org.xmind.ui.internal.resourcemanager;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.xmind.core.Core;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.e4models.IContextRunnable;
import org.xmind.ui.internal.e4models.IModelConstants;
import org.xmind.ui.internal.utils.ResourceUtils;
import org.xmind.ui.mindmap.MindMapUI;

public class MarkerResourceManagerPage extends ResourceManagerDialogPage
        implements ICoreEventListener {

    private final static int ADD_GROUP_BUTTON_ID = IDialogConstants.CLIENT_ID
            + 1;
    private CoreEventRegister coreEventRegister;
    private MarkerResourceManagerViewer viewer;

    static final String USER_MARKER_PATH = "markers/markerSheet.xml"; //$NON-NLS-1$

    @Override
    protected ResourceManagerViewer createViewer() {
        viewer = new MarkerResourceManagerViewer();
        registerCoreEvent();
        return viewer;
    }

    private void registerCoreEvent() {
        ICoreEventSupport ces = (ICoreEventSupport) MindMapUI
                .getResourceManager().getUserMarkerSheet()
                .getAdapter(ICoreEventSupport.class);
        if (ces != null) {
            coreEventRegister = new CoreEventRegister(this);
            coreEventRegister.setNextSupport(ces);
            coreEventRegister.register(Core.MarkerAdd);
            coreEventRegister.register(Core.MarkerGroupAdd);
            coreEventRegister.register(Core.MarkerRefAdd);
            coreEventRegister.register(Core.TitleText);
            coreEventRegister.register(Core.MarkerRemove);
            coreEventRegister.register(Core.MarkerGroupRemove);
            coreEventRegister.register(Core.MarkerRefRemove);
            coreEventRegister.register(Core.Name);
        }
    }

    @Override
    protected void createButtonsForButtonBar(Composite buttonBar) {
        Button button = createButton(buttonBar, ADD_GROUP_BUTTON_ID,
                MindMapMessages.MarkerResourceManagerPage_AddGroup, false);
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                        UserDataConstants.CATEGORY_MARKER,
                        UserDataConstants.ADD_USER_GROUP);
                IMarkerSheet markerSheet = MindMapUI.getResourceManager()
                        .getUserMarkerSheet();
                IMarkerGroup group = MindMapUI.getResourceManager()
                        .getUserMarkerSheet().createMarkerGroup(true);
                group.setName(
                        MindMapMessages.MarkerResourceManagerPage_UserGroup);
                markerSheet.addMarkerGroup(group);
                try {
                    MindMapUI.getResourceManager().saveUserMarkerSheet();
                } catch (Exception ex) {
                    // TODO: handle exception
                    ;
                }
                viewer.updateInput();
                viewer.activateGroup(group);
                viewer.reveal(group);
                viewer.renameMarkerGroup();
            }
        });

    }

    @Override
    public void handleCoreEvent(final CoreEvent event) {
        if (viewer == null)
            return;

        Control c = viewer.getControl();
        if (c == null || c.isDisposed())
            return;

        c.getDisplay().syncExec(new Runnable() {
            public void run() {
                viewer.refresh();
                viewer.setSelection(new StructuredSelection(event.getSource()),
                        true);
            }
        });
    }

    @Override
    public void dispose() {
        if (coreEventRegister != null)
            coreEventRegister.unregisterAll();
        super.dispose();
    }

    @Override
    protected void registerRunnable(IEclipseContext eclipseContext) {
        super.registerRunnable(eclipseContext);
        eclipseContext.set(
                getId() + "/" + IModelConstants.KEY_MODEL_PART_DELETE, //$NON-NLS-1$
                new IContextRunnable() {
                    @Override
                    public void run() {
                        if (viewer != null && viewer.getControl() != null
                                && !viewer.getControl().isDisposed()) {
                            if (viewer.canEditMarkerGroup()) {
                                viewer.deleteMarkerGroup();
                            }

                            List<IMarker> markers = getSelectedMarker();
                            if (!markers.isEmpty()) {
                                ResourceUtils.deleteMarkers(markers);
                            }
                        }
                    }

                    @Override
                    public boolean canExecute(IEclipseContext context,
                            String contextKey) {
                        if (viewer != null && viewer.getControl() != null
                                && !viewer.getControl().isDisposed()) {
                            if (viewer.canEditMarkerGroup())
                                return true;
                        }
                        List<IMarker> markers = getSelectedMarker();
                        boolean canExecute = !markers.isEmpty();
                        IMarkerSheet userMarkerSheet = MindMapUI
                                .getResourceManager().getUserMarkerSheet();
                        for (IMarker marker : markers) {
                            canExecute = canExecute && userMarkerSheet
                                    .getMarker(marker.getId()) != null;
                        }
                        return canExecute;
                    }
                });
        eclipseContext.set(
                getId() + "/" + IModelConstants.KEY_MODEL_PART_RENAME, //$NON-NLS-1$
                new IContextRunnable() {
                    @Override
                    public void run() {
                        if (viewer != null && viewer.getControl() != null
                                && !viewer.getControl().isDisposed()) {
                            viewer.renameMarkerGroup();
                        }
                    }

                    @Override
                    public boolean canExecute(IEclipseContext context,
                            String contextKey) {
                        if (viewer != null && viewer.getControl() != null
                                && !viewer.getControl().isDisposed()) {
                            return viewer.canEditMarkerGroup();
                        }
                        return false;
                    }
                });
    }

    private List<IMarker> getSelectedMarker() {
        List<IMarker> markers = new ArrayList<IMarker>();
        if (viewer != null && viewer.getControl() != null
                && !viewer.getControl().isDisposed()) {
            ISelection selection = viewer.getStructuredSelection();
            if (selection instanceof IStructuredSelection) {
                for (Object element : ((IStructuredSelection) selection)
                        .toList()) {
                    markers.add((IMarker) element);
                }
            }
        }
        return markers;
    }

    @Override
    protected String getContextMenuId() {
        return IModelConstants.POPUPMENU_ID_RESOURCEMANAGER_MARKER;
    }

    @Override
    public String getModelPageId() {
        return IModelConstants.PAGE_ID_RESOURCE_MANAGER_MARKER;
    }

    @Override
    public String getModelPageTitle() {
        return null;
    }

}
