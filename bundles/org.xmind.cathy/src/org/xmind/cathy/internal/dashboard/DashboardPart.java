package org.xmind.cathy.internal.dashboard;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.internal.handlers.WizardHandler;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.service.event.Event;
import org.xmind.cathy.internal.CathyPlugin;
import org.xmind.cathy.internal.ICathyConstants;
import org.xmind.ui.internal.dashboard.pages.IDashboardContext;
import org.xmind.ui.internal.dashboard.pages.IDashboardPage;
import org.xmind.ui.internal.e4handlers.DisabledHandler;
import org.xmind.ui.tabfolder.MTabFolder;
import org.xmind.ui.tabfolder.MTabItem;

/**
 * A view that shows the Dashboard for the Cathy application.
 * 
 * @author Frank Shaka
 * @since 3.6.0
 */
public class DashboardPart implements IDashboardContext {

    private static final String PERSISTED_STATE_KEY_PREFIX = "org.xmind.cathy.dashboard/"; //$NON-NLS-1$

    /**
     * A set of identifiers of commands that the Dashboard allows to be enabled.
     * All other commands registered in the application will be disabled when
     * the Dashboard is showing.
     * 
     * @see #disableUnwantedCommands()
     */
    private static final String[] AVAILABLE_COMMAND_IDS = {
            "org.xmind.ui.command.showDashboard", //$NON-NLS-1$
            "org.xmind.ui.command.toggleDashboard", //$NON-NLS-1$
            "org.xmind.ui.command.welcome", //$NON-NLS-1$
            "org.xmind.ui.command.openWorkbook", //$NON-NLS-1$
            "org.xmind.ui.command.openHomeMap", //$NON-NLS-1$
            "org.xmind.ui.command.newWorkbook", //$NON-NLS-1$
            "org.xmind.ui.command.newFromTemplate", //$NON-NLS-1$
            "org.xmind.ui.command.clearWorkbookHistory", //$NON-NLS-1$
            "net.xmind.ui.command.checkForUpdates", //$NON-NLS-1$
            "net.xmind.ui.command.subscribeNewsletter", //$NON-NLS-1$
            "net.xmind.ui.command.feedback", //$NON-NLS-1$
            "net.xmind.ui.command.signIn", //$NON-NLS-1$
            "net.xmind.ui.command.signOut", //$NON-NLS-1$
            "net.xmind.ui.command.activatePro", //$NON-NLS-1$
            "net.xmind.ui.command.goFeaturedMaps", //$NON-NLS-1$
            "org.xmind.ui.command.showModelPart", //$NON-NLS-1$
            IWorkbenchCommandConstants.FILE_NEW,
            IWorkbenchCommandConstants.FILE_IMPORT,
            IWorkbenchCommandConstants.FILE_RESTART,
            IWorkbenchCommandConstants.FILE_EXIT,
            IWorkbenchCommandConstants.WINDOW_CLOSE_PART,
            IWorkbenchCommandConstants.WINDOW_PREFERENCES,
            IWorkbenchCommandConstants.VIEWS_SHOW_VIEW,
            IWorkbenchCommandConstants.WINDOW_RESET_PERSPECTIVE,
            IWorkbenchCommandConstants.WINDOW_ACTIVATE_EDITOR,
            IWorkbenchCommandConstants.WINDOW_NEXT_EDITOR,
            IWorkbenchCommandConstants.WINDOW_NEXT_VIEW,
            IWorkbenchCommandConstants.WINDOW_PREVIOUS_EDITOR,
            IWorkbenchCommandConstants.WINDOW_PREVIOUS_VIEW,
            IWorkbenchCommandConstants.HELP_HELP_CONTENTS,
            IWorkbenchCommandConstants.HELP_ABOUT,
            IWorkbenchCommandConstants.WINDOW_SHOW_KEY_ASSIST //
    };

    @Inject
    private IEclipseContext context;

    @Inject
    private MPart partModel;

    @Inject
    private EHandlerService handlerService;

    @Inject
    private ECommandService commandService;

    @Inject
    private ESelectionService selectionService;

    @Inject
    private EMenuService menuService;

    @Inject
    private EModelService modelService;

    @Inject
    private MApplication application;

    private MTabFolder tabFolder;

    private Set<String> availableCommandIds;

    private DashboardContent content;

    private ISelectionProvider selectionProvider = null;

    private ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {
        public void selectionChanged(SelectionChangedEvent event) {
            handleSelectionChanged(event);
        }
    };

    protected IEclipseContext getContext() {
        return context;
    }

    @PostConstruct
    public void postConstruct(Composite parent) {

        //
        context.set(IDashboardContext.class, this);

        tabFolder = new MTabFolder(parent);
        ResourceManager resourceManager = new LocalResourceManager(
                JFaceResources.getResources(), tabFolder);
        tabFolder.setStyleProvider(new DashboardStyleProvider(resourceManager));
        tabFolder.setBackground(
                parent.getDisplay().getSystemColor(SWT.COLOR_TRANSPARENT));
        tabFolder.addListener(SWT.Selection, new Listener() {
            public void handleEvent(org.eclipse.swt.widgets.Event event) {
                handlePageSelected();
            }
        });

        availableCommandIds = new HashSet<String>();
        availableCommandIds.addAll(Arrays.asList(AVAILABLE_COMMAND_IDS));

        content = new DashboardContent(this, tabFolder);

        disableUnwantedCommands();
    }

    private void handlePageSelected() {
        if (tabFolder == null || content == null) {
            return;
        }
        MTabItem item = tabFolder.getSelection();
        String pageId = item == null ? null : content.getItemId(item);
        partModel.getTransientData()
                .put(ICathyConstants.DATA_DASHBOARD_SELECTED_PAGE_ID, pageId);
    }

    protected void executeCommand(String commandId,
            Map<String, Object> parameters) {
        if (commandService == null || handlerService == null)
            return;

        ParameterizedCommand command = commandService.createCommand(commandId,
                parameters);
        if (command == null)
            return;

        handlerService.executeHandler(command);
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

    /**
     * Disables almost all commands unless explicitly kept enabled, to make sure
     * that the user can't trigger commands that may not be appropriate during
     * the appearance of the Dashboard.
     * <p>
     * We activate a {@link DisabledHandler} handler object for each command to
     * be disabled. These handlers are registered within the context of this
     * view only, so that they do not override real handlers when the Dashboard
     * is hidden.
     * </p>
     */
    private void disableUnwantedCommands() {
        if (handlerService == null || application == null)
            return;

        for (MCommand command : application.getCommands()) {
            String commandId = command.getElementId();
            if (!availableCommandIds.contains(commandId)) {
                handlerService.activateHandler(commandId,
                        new DisabledHandler(findElementUpdater(commandId)));
            }
        }
    }

    /**
     * Finds an element updater that knows how to decorate UI elements like the
     * real handler of the command.
     * 
     * @param commandId
     *            the identifier of the command to match
     * @return
     */
    private IElementUpdater findElementUpdater(String commandId) {
        if (IWorkbenchCommandConstants.FILE_EXPORT.equals(commandId)) {
            return new WizardHandler.Export();
        } else if (IWorkbenchCommandConstants.FILE_IMPORT.equals(commandId)) {
            return new WizardHandler.Import();
        }
        return null;
    }

    @PreDestroy
    public void preDestroy() {
        this.content = null;
        if (this.tabFolder != null) {
            this.tabFolder.dispose();
            this.tabFolder = null;
        }
    }

    public void hideDashboard() {
        MWindow window = findWindowFor(partModel);
        if (window == null) {
            CathyPlugin.log("Failed to find window for Dashboard part."); //$NON-NLS-1$
            return;
        }

        if (window.getTags().contains(ICathyConstants.TAG_SHOW_DASHBOARD)) {
            window.getTags().remove(ICathyConstants.TAG_SHOW_DASHBOARD);
        }
    }

    private static MWindow findWindowFor(MUIElement element) {
        if (element == null)
            return null;
        if (element instanceof MWindow)
            return (MWindow) element;
        MPlaceholder placeholder = element.getCurSharedRef();
        if (placeholder != null)
            return findWindowFor(placeholder);
        return findWindowFor(element.getParent());
    }

    @Inject
    @Optional
    public void subscribeDashboardTransientData(
            @EventTopic(UIEvents.ApplicationElement.TOPIC_TRANSIENTDATA) Event event) {
        if (partModel == null
                || partModel != event.getProperty(UIEvents.EventTags.ELEMENT))
            return;

        Object selectedPageId = partModel.getTransientData()
                .get(ICathyConstants.DATA_DASHBOARD_SELECTED_PAGE_ID);
        if (selectedPageId == null || !(selectedPageId instanceof String))
            return;

        if (this.tabFolder == null || content == null)
            return;

        MTabItem item = content.getItemById((String) selectedPageId);
        IDashboardPage dashboardPage = content.getDashboardPage(item);
        if (dashboardPage != null) {
            Control control = dashboardPage.getControl();
            if (control == null || control.isDisposed()) {
                dashboardPage.createControl(this.tabFolder.getBody());
                item.setControl(dashboardPage.getControl());
            }
            dashboardPage.setFocus();
            this.tabFolder.setSelection(item);
        }
    }

    @Focus
    public void setFocus() {
        if (tabFolder != null && !tabFolder.isDisposed() && content != null) {
            MTabItem item = tabFolder.getSelection();
            if (item != null) {
                IDashboardPage page = content.getDashboardPage(item);
                if (page != null) {
                    page.setFocus();
                    return;
                } else {
                    Control control = item.getControl();
                    if (control != null) {
                        control.setFocus();
                        return;
                    }
                }
            }
            tabFolder.setFocus();
        }
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (adapter == MPart.class)
            return adapter.cast(partModel);
        if (context != null)
            return context.get(adapter);
        return null;
    }

    public void registerAvailableCommandId(String commandId) {
        availableCommandIds.add(commandId);
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

    public boolean openEditor(final IEditorInput input, final String editorId) {
        final IWorkbenchPage page = context.get(IWorkbenchPage.class);
        if (page == null)
            return false;
        try {
            page.openEditor(input, editorId);
            return true;
        } catch (PartInitException e) {
            StatusManager.getManager().handle(new Status(IStatus.ERROR,
                    CathyPlugin.PLUGIN_ID, e.getMessage(), e),
                    StatusManager.SHOW);
            return false;
        }
    }

    public boolean showView(final String viewId) {
        final IWorkbenchPage page = context.get(IWorkbenchPage.class);
        if (page == null)
            return false;

        try {
            page.showView(viewId);
            return true;
        } catch (PartInitException e) {
            StatusManager.getManager().handle(new Status(IStatus.ERROR,
                    CathyPlugin.PLUGIN_ID, e.getMessage(), e),
                    StatusManager.SHOW);
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.ui.internal.dashboard.pages.IDashboardContext#getPersistedState
     * (java.lang.String)
     */
    public String getPersistedState(String key) {
        Assert.isLegal(key != null);
        return partModel.getPersistedState()
                .get(PERSISTED_STATE_KEY_PREFIX + key);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.ui.internal.dashboard.pages.IDashboardContext#setPersistedState
     * (java.lang.String, java.lang.String)
     */
    public void setPersistedState(String key, String value) {
        Assert.isLegal(key != null);
        if (value == null) {
            partModel.getPersistedState()
                    .remove(PERSISTED_STATE_KEY_PREFIX + key);
        } else {
            partModel.getPersistedState().put(PERSISTED_STATE_KEY_PREFIX + key,
                    value);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.ui.internal.dashboard.pages.IDashboardContext#
     * getContextVariable(java.lang.Class)
     */
    public <T> T getContextVariable(Class<T> key) {
        return context.get(key);
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.ui.internal.dashboard.pages.IDashboardContext#
     * getContextVariable(java.lang.String)
     */
    public Object getContextVariable(String key) {
        return context.get(key);
    }

}
