package org.xmind.ui.internal.e4models;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.impl.StringToObjectMapImpl;
import org.eclipse.e4.ui.model.application.impl.StringToStringMapImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.E4PartWrapper;
import org.osgi.service.event.Event;

@SuppressWarnings("restriction")
public class ModelPart implements IAdaptable, IModelPartContext {

    public static final String TAG_VIEW_MENU = "ViewMenu"; //$NON-NLS-1$

    @Inject
    EMenuService menuService;

    @Inject
    private EModelService modelService;

    @Inject
    private ESelectionService selectionService;

    @Inject
    private MPart partModel;

    @Inject
    private IWorkbenchWindow workbenchWindow;

    @Inject
    private MApplication application;

    private ISelectionProvider selectionProvider = null;

    private ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {
        public void selectionChanged(SelectionChangedEvent event) {
            handleSelectionChanged(event);
        }
    };

    protected LocalResourceManager resources;

    private boolean postConfigurationSuccess;

    private Composite control;

    @PostConstruct
    private void postConstruct(final Composite parent) {
        init();
        Composite composite = new Composite(parent, SWT.NONE);
        resources = new LocalResourceManager(JFaceResources.getResources(),
                composite);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        composite.setLayout(layout);

        createContent(composite);
        this.control = composite;
    }

    protected void init() {
        partModel.getContext().set(IModelPartContext.class, this);
    }

    protected void createContent(Composite parent) {
    }

    @PreDestroy
    private void preDestroy() {
        dispose();
    }

    protected void dispose() {
    }

    @SuppressWarnings("restriction")
    @Focus
    private void delegateSetFocus() {
        setFocus();
        if (workbenchWindow != null) {
            IPartService partService = workbenchWindow.getPartService();
            if (partService instanceof IPartListener) {
                if (partModel.getTransientData().get(
                        E4PartWrapper.E4_WRAPPER_KEY) instanceof E4PartWrapper) {
                    IWorkbenchPart wp = (IWorkbenchPart) partModel
                            .getTransientData()
                            .get(E4PartWrapper.E4_WRAPPER_KEY);
                    ((IPartListener) partService).partActivated(wp);
                }
            }
        }
    }

    protected void setFocus() {
        control.setFocus();
    }

    public void setSelectionProvider(ISelectionProvider selectionProvider) {
        ISelectionProvider oldSelectionProvider = this.selectionProvider;
        if (selectionProvider == oldSelectionProvider)
            return;
        if (oldSelectionProvider != null) {
            oldSelectionProvider
                    .removeSelectionChangedListener(selectionChangedListener);
        }
        this.selectionProvider = selectionProvider;
        if (selectionProvider != null) {
            selectionProvider
                    .addSelectionChangedListener(selectionChangedListener);
        }
        if (selectionService != null) {
            selectionService.setSelection(selectionProvider == null ? null
                    : selectionProvider.getSelection());
        }
    }

    private void handleSelectionChanged(SelectionChangedEvent event) {
        if (selectionService != null) {
            selectionService.setSelection(event.getSelection());
        }
    }

    @Inject
    @Optional
    private void subscribeTopicTransientDataChanged(
            @UIEventTopic(UIEvents.ApplicationElement.TOPIC_TRANSIENTDATA) Event event) {
        Object changedElement = event.getProperty(UIEvents.EventTags.ELEMENT);
        if (!(changedElement instanceof MPart))
            return;

        Object newValue = event.getProperty(UIEvents.EventTags.NEW_VALUE);
        Object oldValue = event.getProperty(UIEvents.EventTags.OLD_VALUE);
        if (((MPart) changedElement).getObject() == this) {
            String newKey = null;
            Object newValueOfKey = null;
            String oldKey = null;
            Object oldValueOfKey = null;

            if (newValue instanceof StringToObjectMapImpl) {
                newKey = ((StringToObjectMapImpl) newValue).getKey();
                newValueOfKey = ((StringToObjectMapImpl) newValue).getValue();
            } else if (newValue instanceof StringToStringMapImpl) {
                newKey = ((StringToStringMapImpl) newValue).getKey();
                newValueOfKey = ((StringToStringMapImpl) newValue).getValue();
            }

            if (oldValue instanceof StringToObjectMapImpl) {
                oldKey = ((StringToObjectMapImpl) oldValue).getKey();
                oldValueOfKey = ((StringToObjectMapImpl) oldValue).getValue();
            } else if (newValue instanceof StringToStringMapImpl) {
                oldKey = ((StringToStringMapImpl) oldValue).getKey();
                oldValueOfKey = ((StringToStringMapImpl) oldValue).getValue();
            }

            if (UIEvents.isADD(event)) {
                handleTransientDataAdded(newKey, newValueOfKey, oldKey,
                        oldValueOfKey);
            } else if (UIEvents.isREMOVE(event)) {
                handleTransientDataRemoved(newKey, newValueOfKey, oldKey,
                        oldValueOfKey);
            }
        }
    }

    protected void handleTransientDataAdded(String newKey, Object newValue,
            String oldKey, Object oldValue) {
    }

    protected void handleTransientDataRemoved(String newKey, Object newValue,
            String oldKey, Object oldValue) {
    }

    @Inject
    @Optional
    public void activePartChanged(
            @UIEventTopic(UIEvents.UILifeCycle.ACTIVATE) Event event) {
        Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
        if (!(element instanceof MPart))
            return;

        MPart part = (MPart) element;

        handlePartActivated(part);

        if (part.getObject() == this) {
            if (!postConfigurationSuccess) {
                Object wp = partModel.getTransientData()
                        .get(E4PartWrapper.E4_WRAPPER_KEY);
                if (wp instanceof E4PartWrapper) {
                    postConfigurationSuccess = postConfiguration(
                            (IWorkbenchPart) wp, partModel);
                }
            }
        }

    }

    protected void handlePartActivated(MPart part) {
    }

    @Inject
    @Optional
    private void subscribeTopicBringToTop(
            @UIEventTopic(UIEvents.UILifeCycle.BRINGTOTOP) Event event) {
        Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
        if (element == partModel) {
            handleBringToTop();
        }
    }

    protected void handleBringToTop() {
    }

    protected boolean postConfiguration(IWorkbenchPart workbenchPart,
            MPart part) {
        return true;
    }

    public boolean registerViewMenu(String viewMenuId) {
        if (viewMenuId == null || partModel == null || modelService == null) {
            return false;
        }

        MMenu viewMenu = null;
        for (MMenu menu : partModel.getMenus()) {
            boolean isViewMenu = menu.getTags().contains(TAG_VIEW_MENU);
            if (isViewMenu && viewMenuId.equals(menu.getElementId())) {
                viewMenu = menu;
                break;
            }
        }

        if (viewMenu == null) {
            viewMenu = modelService.createModelElement(MMenu.class);
            viewMenu.setElementId(viewMenuId);
            viewMenu.getTags().add(TAG_VIEW_MENU);
            partModel.getMenus().add(viewMenu);
        }

        return true;

    }

    public boolean registerContextMenu(Object menuParent, final String menuId) {
        if (!(menuParent instanceof Control) || menuService == null
                || partModel == null) {
            return false;
        }
        Control parentControl = (Control) menuParent;
        MPopupMenu menuModel = null;
        for (MMenu item : partModel.getMenus()) {
            if (menuId.equals(item.getElementId())
                    && item instanceof MPopupMenu) {
                menuModel = (MPopupMenu) item;
                break;
            }
        }
        if (menuModel == null) {
            menuModel = modelService.createModelElement(MPopupMenu.class);
            menuModel.setElementId(menuId);
            menuModel.getTags().add("menuContribution:popup"); //$NON-NLS-1$
            partModel.getMenus().add(menuModel);
        }

        if (menuModel.getWidget() instanceof Menu) {
            Menu menu = (Menu) menuModel.getWidget();
            parentControl.setMenu(menu);
            return true;
        }

        return menuService.registerContextMenu(parentControl, menuId);
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (EModelService.class.equals(adapter)) {
            return adapter.cast(modelService);
        } else if (MPart.class.equals(adapter)) {
            return adapter.cast(partModel);
        } else if (EMenuService.class.equals(adapter)) {
            return adapter.cast(menuService);
        } else if (IWorkbenchWindow.class.equals(adapter)) {
            return adapter.cast(workbenchWindow);
        } else if (ESelectionService.class.equals(adapter)) {
            return adapter.cast(selectionService);
        } else if (IModelPartContext.class.equals(adapter)) {
            return adapter.cast(this);
        } else if (ISelectionProvider.class.equals(adapter)) {
            return adapter.cast(selectionProvider);
        } else if (MApplication.class.equals(adapter)) {
            return adapter.cast(application);
        } else {
            return partModel.getContext().get(adapter);
        }
    }

}
