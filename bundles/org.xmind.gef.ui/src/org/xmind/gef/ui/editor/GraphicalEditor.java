/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.gef.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.command.CommandStack;
import org.xmind.gef.command.CommandStackEvent;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.command.ICommandStackListener;
import org.xmind.gef.ui.actions.ActionRegistry;
import org.xmind.gef.ui.actions.IActionRegistry;
import org.xmind.gef.ui.actions.ICommandStackAction;
import org.xmind.gef.ui.internal.GEFPlugin;
import org.xmind.gef.util.EventListenerSupport;
import org.xmind.gef.util.IEventDispatcher;
import org.xmind.ui.tabfolder.DelegatedSelectionProvider;
import org.xmind.ui.tabfolder.IDelegatedSelectionProvider;
import org.xmind.ui.tabfolder.IPageClosedListener;

/**
 * @author Brian Sun
 */
public abstract class GraphicalEditor extends EditorPart
        implements IGraphicalEditor, ICommandStackListener {

    protected class PageInputSelectionProvider implements ISelectionProvider {

        private EventListenerSupport listeners = new EventListenerSupport();

        public void addSelectionChangedListener(
                ISelectionChangedListener listener) {
            listeners.addListener(ISelectionChangedListener.class, listener);
        }

        public ISelection getSelection() {
            IGraphicalEditorPage page = getActivePageInstance();
            if (page != null) {
                Object pageInput = page.getInput();
                if (pageInput != null) {
                    return new StructuredSelection(pageInput);
                }
            }
            return StructuredSelection.EMPTY;
        }

        public void removeSelectionChangedListener(
                ISelectionChangedListener listener) {
            listeners.removeListener(ISelectionChangedListener.class, listener);
        }

        public void setSelection(ISelection selection) {
            if (selection instanceof IStructuredSelection) {
                Object pageInput = ((IStructuredSelection) selection)
                        .getFirstElement();
                if (pageInput != null) {
                    ensurePageVisible(pageInput);
                }
            }
        }

        protected void firePageChanged() {
            fireSelectionChanged(
                    new SelectionChangedEvent(this, getSelection()));
        }

        private void fireSelectionChanged(final SelectionChangedEvent event) {
            listeners.fireEvent(ISelectionChangedListener.class,
                    new IEventDispatcher() {

                        public void dispatch(Object listener) {
                            ((ISelectionChangedListener) listener)
                                    .selectionChanged(event);
                        }
                    });
        }

    }

    protected class MultiPageSelectionProvider
            extends DelegatedSelectionProvider {

        /*
         * (non-Javadoc)
         * @see
         * org.xmind.ui.tabfolder.DelegatedSelectionProvider#setSelection(org
         * .eclipse.jface.viewers.ISelection)
         */
        @Override
        public void setSelection(ISelection selection) {
            Object input = findOwnedInput(selection);
            if (input != null) {
                ensurePageVisible(input);
            }
            super.setSelection(selection);
        }
    }

    private Composite container = null;

    private IPageContainerPresentation containerPresentation = null;

    private IPageChangedListener presentationHooker = new IPageChangedListener() {

        public void pageChanged(PageChangedEvent event) {
            handlePageChange(getActivePage());
        }
    };

    private List<IGraphicalEditorPage> pages = new ArrayList<IGraphicalEditorPage>();

    private EventListenerSupport listeners = new EventListenerSupport();

    private ICommandStack commandStack = null;

    private IMiniBar miniBar = null;

    private IMiniBarContributor miniBarContributor = null;

    private IActionRegistry actionRegistry = null;

    private List<ICommandStackAction> csActions = null;

    private int activePageIndex = -1;

    private PageInputSelectionProvider pageInputSelectionProvider = null;

    private MenuManager pagePopupMenu = null;

    private IGlobalActionHandlerService globalActionHandlerService = null;

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite,
     * org.eclipse.ui.IEditorInput)
     */
    @Override
    public void init(IEditorSite site, IEditorInput input)
            throws PartInitException {
        setSite(site);
        setInput(input);
        site.setSelectionProvider(createSelectionProvider());
        setCommandStack(createCommandStack());
    }

    protected ISelectionProvider createSelectionProvider() {
        return new MultiPageSelectionProvider();
    }

    protected Object findOwnedInput(ISelection selection) {
        return null;
    }

    protected Composite getContainer() {
        return container;
    }

    protected IPageContainerPresentation getContainerPresentation() {
        return containerPresentation;
    }

    protected void hookContainerPresentation() {
        getContainerPresentation().addPageChangedListener(presentationHooker);
    }

    public void createPartControl(Composite parent) {
        if (containerPresentation == null) {
            containerPresentation = createContainerPresentation();
            hookContainerPresentation();
        }
        Composite containerParent = createContainerParent(parent);
        this.container = containerPresentation.createContainer(containerParent);
        createEditorContents();
    }

    protected void createEditorContents() {
        if (getContainer() instanceof CTabFolder) {
            createMiniBarComposite((CTabFolder) getContainer());
            createPageContextMenu((CTabFolder) getContainer());
        }
    }

    private void createMiniBarComposite(CTabFolder tabFolder) {
        final Composite composite = new Composite(getContainer(), SWT.None);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        composite.setLayout(layout);

        createMiniBar(composite);
        final ToolBar control = ((ToolBarManager) miniBar.getToolBarManager())
                .getControl();
        GridData controlData = new GridData(SWT.RIGHT, SWT.FILL, true, true);
        control.setLayoutData(controlData);

        tabFolder.setTopRight(composite, SWT.RIGHT);
    }

    protected IPageContainerPresentation createContainerPresentation() {
        return new TabFolderContainerPresentation();
    }

    /**
     * Creates the parent control for the container returned by
     * {@link #getContainer() }.
     * <p>
     * Subclasses may extend and must call super implementation first.
     * </p>
     * 
     * @param parent
     *            the parent for all of the editors contents.
     * @return the parent for this editor's container. Must not be
     *         <code>null</code>.
     */
    protected Composite createContainerParent(Composite parent) {
        parent.setLayout(new FillLayout());
        return parent;
    }

    public void addPage(IGraphicalEditorPage page) {
        page.setEditDomain(createEditDomain(page));
        createPageControl(page);
        pages.add(page);
    }

    private void createPageControl(IGraphicalEditorPage page) {
        page.createPageControl(getContainer());
        Assert.isNotNull(page.getControl());
        Assert.isNotNull(page.getViewer());
        Assert.isNotNull(page.getViewer().getControl());
        addPageControl(page.getControl());
    }

    private void addPageControl(Control pageControl) {
        int index = containerPresentation.getPageCount(getContainer());
        containerPresentation.addPage(getContainer(), index, pageControl);
    }

    protected EditDomain createEditDomain(IGraphicalEditorPage page) {
        return new EditDomain();
    }

    protected void disposeEditDomain(IGraphicalEditorPage page,
            EditDomain editDomain) {
        editDomain.dispose();
    }

    protected void createPageContextMenu(Composite container) {
        if (pagePopupMenu == null) {
            pagePopupMenu = createPagePopupMenu();
            String menuId = getSite().getId() + ".page"; //$NON-NLS-1$
            if (isPagePopupMenuDynamic()) {
                setupDynamicPopupMenu(container, pagePopupMenu);
            } else {
                contributeToPagePopupMenu(pagePopupMenu);
            }
            registerPagePopupMenu(menuId, pagePopupMenu);
        }
        container.setMenu(pagePopupMenu.createContextMenu(container));

    }

    private void setupDynamicPopupMenu(final Composite container,
            MenuManager popupMenu) {
        final boolean[] showsItems = new boolean[1];
        showsItems[0] = false;
        popupMenu.setRemoveAllWhenShown(true);
        popupMenu.addMenuListener(new IMenuListener() {

            public void menuAboutToShow(IMenuManager manager) {
                if (showsItems[0]) {
                    contributeToPagePopupMenu(manager);
                }
            }
        });
        container.addMenuDetectListener(new MenuDetectListener() {

            public void menuDetected(MenuDetectEvent e) {
                CTabFolder folder = (CTabFolder) container;
                Point p = folder.toControl(e.x, e.y);
                showsItems[0] = !(folder.getClientArea().contains(p)
                        || folder.getTopRight().getBounds().contains(p));
            }
        });
    }

    protected void registerPagePopupMenu(String menuId, MenuManager menu) {
        getSite().registerContextMenu(menuId, menu,
                getPageInputSelectionProvider());
    }

    protected void contributeToPagePopupMenu(IMenuManager menu) {
        IEditorActionBarContributor contributor = getEditorSite()
                .getActionBarContributor();
        if (contributor instanceof GraphicalEditorActionBarContributor) {
            ((GraphicalEditorActionBarContributor) contributor)
                    .contributeToPagePopupMenu(menu);
        }
    }

    protected boolean isPagePopupMenuDynamic() {
        return true;
    }

    protected MenuManager createPagePopupMenu() {
        return new MenuManager();
    }

    public void removePage(IGraphicalEditorPage page) {
        removePage(findPage(page));
    }

    protected void removePage(int pageIndex) {
        Assert.isTrue(pageIndex >= 0 && pageIndex < getPageCount());
        boolean wasActivePage = pageIndex == getActivePage();
        IGraphicalEditorPage page = getPage(pageIndex);
        containerPresentation.disposePage(getContainer(), pageIndex);
        if (page != null) {
            page.dispose();
        }
        pages.remove(page);
        if (wasActivePage) {
            if (pageIndex == getPageCount())
                pageIndex--;
            setActivePage(pageIndex);
        }
        firePageClosed(page);
    }

    public IGraphicalEditorPage getPage(int pageIndex) {
        if (pageIndex < 0 || pageIndex >= getPageCount())
            return null;
        return pages.get(pageIndex);
    }

    public int findPage(IGraphicalEditorPage page) {
        return pages.indexOf(page);
    }

    protected void postSave() {
        if (getCommandStack() != null) {
            getCommandStack().markSaved();
        }
        firePropertyChange(PROP_DIRTY);
    }

    public int getPageCount() {
        return pages.size();
    }

    public boolean isDirty() {
        return getCommandStack() != null && getCommandStack().isDirty();
    }

    /**
     * Creates the mini bar on the part control.
     * <p>
     * <b>IMPORTANT:</b> This mini bar contribution relies on the fact that the
     * page container is a CTabFolder, so it may do nothing if the
     * implementation changes.
     * </p>
     * 
     * @see #getContainer()
     */
    protected final void createMiniBar(Composite parent) {
        if (!(getContainer() instanceof CTabFolder))
            return;

        miniBar = new MiniBar();
        initializeMiniBar(miniBar);
        if (!((MiniBar) miniBar).isEmpty()) {
            createMiniBarControl(miniBar, (CTabFolder) getContainer(), parent);
        }
    }

    /**
     * @param miniBar
     */
    private void initializeMiniBar(IMiniBar miniBar) {
        if (getMiniBarContributor() != null) {
            getMiniBarContributor().init(miniBar, this);
        }
    }

    /**
     * Creates the mini bar's control on the specified tab folder.
     * 
     * @param miniBar
     * @param tabFolder
     */
    private void createMiniBarControl(IMiniBar miniBar, CTabFolder tabFolder,
            Composite parent) {
        ((ToolBarManager) miniBar.getToolBarManager()).createControl(parent);
    }

    public IMiniBarContributor getMiniBarContributor() {
        return miniBarContributor;
    }

    public void setMiniBarContributor(IMiniBarContributor miniBarContributor) {
        this.miniBarContributor = miniBarContributor;
    }

    /**
     * @return commandStack
     */
    public ICommandStack getCommandStack() {
        return commandStack;
    }

    protected ICommandStack createCommandStack() {
        return new CommandStack();
    }

    public void handleCommandStackEvent(CommandStackEvent event) {
        if ((event.getStatus() & GEF.CS_POST_MASK) != 0
                || event.getStatus() == GEF.CS_UPDATED) {
            getSite().getShell().getDisplay().asyncExec(new Runnable() {

                public void run() {
                    firePropertyChange(PROP_DIRTY);
                }
            });
        }
    }

    @Deprecated
    protected void fireDirty() {
        firePropertyChange(PROP_DIRTY);
    }

    /**
     * @param commandStack
     *            the commandStack to set
     */
    public void setCommandStack(ICommandStack commandStack) {
        ICommandStack oldCS = this.commandStack;
        if (commandStack == oldCS)
            return;

        this.commandStack = commandStack;
        commandStackChanged(oldCS, commandStack);
    }

    protected void commandStackChanged(ICommandStack oldCS,
            ICommandStack newCS) {
        if (oldCS != null) {
            unhookCommandStack(oldCS);
        }
        if (newCS != null) {
            hookCommandStack(newCS);
        }
        for (IGraphicalEditorPage page : getPages()) {
            EditDomain domain = page.getEditDomain();
            if (domain != null) {
                domain.setCommandStack(newCS);
            }
        }
        if (csActions != null) {
            for (ICommandStackAction action : csActions) {
                action.setCommandStack(newCS);
            }
        }
        firePropertyChange(PROP_DIRTY);
    }

    protected void hookCommandStack(ICommandStack cs) {
        cs.addCSListener(this);
    }

    protected void unhookCommandStack(ICommandStack cs) {
        cs.removeCSListener(this);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAdapter(Class<T> adapter) {
        T result = getEditorAdapter(adapter);
        if (result != null) {
            return result;
        }

        Object activePage = getSelectedPage();
        if (activePage != null) {
            result = GEFPlugin.getAdapter(activePage, adapter);
            if (result != null)
                return result;
        }

        return super.getAdapter(adapter);
    }

    protected <T> T getEditorAdapter(Class<T> adapter) {
        if (adapter.isInstance(this))
            return adapter.cast(this);
        if (adapter == ICommandStack.class)
            return adapter.cast(getCommandStack());
        if (adapter == IActionRegistry.class)
            return adapter.cast(getActionRegistry());
        if (adapter == IMiniBar.class)
            return adapter.cast(miniBar);
        if (adapter == IMiniBarContributor.class)
            return adapter.cast(getMiniBarContributor());
        if (adapter == IGlobalActionHandlerService.class) {
            if (globalActionHandlerService == null) {
                globalActionHandlerService = new GlobalActionHandlerService(
                        this);
            }
            return adapter.cast(globalActionHandlerService);
        }
        if (adapter == IGlobalActionHandlerUpdater.class) {
            IEditorActionBarContributor contributor = getEditorSite()
                    .getActionBarContributor();
            if (contributor instanceof IGlobalActionHandlerUpdater) {
                return adapter.cast(contributor);
            }
            return null;
        }
        return null;
    }

    public Object getSelectedPage() {
        return getActivePageInstance();
    }

    public IGraphicalEditorPage getActivePageInstance() {
        return getPage(getActivePage());
    }

    public void addPageChangedListener(IPageChangedListener listener) {
        listeners.addListener(IPageChangedListener.class, listener);
    }

    public void removePageChangedListener(IPageChangedListener listener) {
        listeners.removeListener(IPageChangedListener.class, listener);
    }

    protected void firePageChanged(Object page) {
        final PageChangedEvent event = new PageChangedEvent(this, page);
        listeners.fireEvent(IPageChangedListener.class, new IEventDispatcher() {

            public void dispatch(Object listener) {
                ((IPageChangedListener) listener).pageChanged(event);
            }
        });
    }

    protected void firePageClosed(final Object page) {
        listeners.fireEvent(IPageChangedListener.class, new IEventDispatcher() {

            public void dispatch(Object listener) {
                if (listener instanceof IPageClosedListener) {
                    ((IPageClosedListener) listener).pageClosed(page);
                }
            }
        });
    }

    @SuppressWarnings("deprecation")
    protected void handlePageChange(int newPageIndex) {
        boolean wasFocused = false;
        IGraphicalEditorPage oldActivePage = getPage(activePageIndex);
        if (oldActivePage != null && oldActivePage.isActive()) {
            wasFocused = oldActivePage.isFocused();
//            EditDomain editDomain = oldActivePage.getEditDomain();
//            if (editDomain != null) {
//                editDomain.setActiveTool(GEF.TOOL_DEFAULT);
//            }
            oldActivePage.setActive(false);
        }

        this.activePageIndex = newPageIndex;
        IGraphicalEditorPage activePage = getPage(newPageIndex);
        if (activePage != null && !activePage.isActive()) {
            activePage.setActive(true);
        }
        if (wasFocused) {
            activePage.setFocus();
        }

        IWorkbenchPage page = getSite().getPage();
        if (page != null) {
            //Only when this part is current active part, configure editor by use of active editor page.
            IWorkbenchPart currentActivePart = page.getActivePart();
            if (currentActivePart == this) {
                IEditorActionBarContributor contributor = getEditorSite()
                        .getActionBarContributor();
                if (contributor != null
                        && contributor instanceof GraphicalEditorActionBarContributor) {
                    ((GraphicalEditorActionBarContributor) contributor)
                            .setActivePage(activePage);
                }
            }
        }

        ISelectionProvider selectionProvider = getSite().getSelectionProvider();
        if (selectionProvider instanceof IDelegatedSelectionProvider) {
            ((IDelegatedSelectionProvider) selectionProvider)
                    .setDelegate(activePage == null ? null
                            : activePage.getSelectionProvider());
        }
        if (getMiniBarContributor() != null) {
            getMiniBarContributor().setActivePage(activePage);
        }
        if (pageInputSelectionProvider != null) {
            Object pageInput = activePage == null ? null
                    : activePage.getInput();
            pageInputSelectionProvider
                    .setSelection(pageInput == null ? StructuredSelection.EMPTY
                            : new StructuredSelection(pageInput));
        }
        firePageChanged(activePage);
    }

    public void setFocus() {
        setFocus(getActivePage());
    }

    protected void setFocus(int pageIndex) {
        if (pageIndex < 0 || pageIndex >= getPageCount()) {
            container.setFocus();
        } else {
            IGraphicalEditorPage page = getPage(pageIndex);
            if (page != null) {
                page.setFocus();
            } else {
                Control control = containerPresentation
                        .getPageControl(getContainer(), pageIndex);
                if (control != null && !control.isDisposed()) {
                    control.setFocus();
                }
            }
        }
    }

    public void movePageTo(int oldIndex, int newIndex) {
        IGraphicalEditorPage activePage = getActivePageInstance();
        boolean wasActive = oldIndex == getActivePage();
        pages.add(newIndex, pages.remove(oldIndex));
        for (int i = 0; i < pages.size(); i++) {
            IGraphicalEditorPage page = pages.get(i);
            boolean wasFocused = page.isFocused();
            containerPresentation.setPageControl(getContainer(), i,
                    page.getControl());
            if (wasFocused)
                page.setFocus();
            page.updatePageTitle();
        }
        if (wasActive) {
            pages.get(newIndex).getControl().setVisible(true);
            setActivePage(newIndex);
        }
        if (activePage != null) {
            containerPresentation.setActivePage(getContainer(),
                    pages.indexOf(activePage));
        }
    }

    public String getPageText(int pageIndex) {
        return containerPresentation.getPageText(getContainer(), pageIndex);
    }

    public void setPageText(int pageIndex, String text) {
        if (text == null)
            text = ""; //$NON-NLS-1$
        containerPresentation.setPageText(getContainer(), pageIndex, text);
    }

    public int getActivePage() {
        return containerPresentation.getActivePage(getContainer());
    }

    public void setActivePage(int pageIndex) {
//        Assert.isTrue(pageIndex < getPageCount());
        if (pageIndex < 0 || pageIndex >= getPageCount())
            return;

        if (pageIndex >= 0) {
            containerPresentation.setActivePage(getContainer(), pageIndex);
        } else {
            containerPresentation.setActivePage(getContainer(),
                    containerPresentation.getPageCount(getContainer()) - 1);
        }
        handlePageChange(pageIndex);
    }

    public IGraphicalEditorPage[] getPages() {
        return pages.toArray(new IGraphicalEditorPage[pages.size()]);
    }

    protected ISelectionProvider getPageInputSelectionProvider() {
        if (pageInputSelectionProvider == null) {
            pageInputSelectionProvider = new PageInputSelectionProvider();
        }
        return pageInputSelectionProvider;
    }

    protected IActionRegistry getActionRegistry() {
        if (actionRegistry == null)
            actionRegistry = new ActionRegistry();
        return actionRegistry;
    }

    @Override
    public void dispose() {
        if (csActions != null) {
            for (ICommandStackAction action : csActions) {
                action.setCommandStack(null);
            }
            csActions = null;
        }
        if (actionRegistry != null) {
            actionRegistry.dispose();
            actionRegistry = null;
        }
        if (pagePopupMenu != null) {
            pagePopupMenu.dispose();
            pagePopupMenu = null;
        }

        if (miniBar != null) {
            IToolBarManager toolBarManager = miniBar.getToolBarManager();
            if (toolBarManager instanceof ToolBarManager) {
                ((ToolBarManager) toolBarManager).dispose();
            }
            miniBar = null;
        }

        if (miniBarContributor != null) {
            miniBarContributor.dispose();
            miniBarContributor = null;
        }

        if (commandStack != null) {
            if (!commandStack.isDisposed())
                disposeCommandStack(commandStack);
            commandStack = null;
        }
        IWorkbenchPartSite site = getSite();
        if (site != null) {
            site.setSelectionProvider(null);
        }
        disposePages();
        super.dispose();
    }

    protected void disposeCommandStack(ICommandStack commandStack) {
        commandStack.dispose();
    }

    private void disposePages() {
        if (pages.isEmpty())
            return;

        for (final Object o : pages.toArray()) {
            SafeRunner.run(new SafeRunnable() {

                public void run() throws Exception {
                    IGraphicalEditorPage page = (IGraphicalEditorPage) o;
                    page.dispose();
                    EditDomain editDomain = page.getEditDomain();
                    if (editDomain != null) {
                        disposeEditDomain(page, editDomain);
                    }
                }
            });
        }
        pages.clear();
    }

    /**
     * @param sourceEvent
     * @return
     */
    public IGraphicalEditorPage findPage(Object input) {
        for (IGraphicalEditorPage page : getPages()) {
            Object pageInput = page.getInput();
            if (pageInput == input
                    || (input != null && input.equals(pageInput)))
                return page;
        }
        return null;
    }

    public IGraphicalEditorPage ensurePageVisible(Object input) {
        IGraphicalEditorPage page = findPage(input);
        if (page != null) {
            if (page != getActivePageInstance()) {
                setActivePage(page.getIndex());
                page = getActivePageInstance();
            }
        }
        return page;
    }

    public boolean navigateTo(Object input, Object... elements) {
        IGraphicalEditorPage page = ensurePageVisible(input);
        if (page != null) {
            if (elements == null)
                return true;
            ISelectionProvider viewer = page.getSelectionProvider();
            if (viewer != null) {
                viewer.setSelection(new StructuredSelection(elements));
                return true;
            }
        }
        return false;
    }

    protected void addCommandStackAction(ICommandStackAction action) {
        if (csActions == null)
            csActions = new ArrayList<ICommandStackAction>();
        csActions.add(action);
    }
}
