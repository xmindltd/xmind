package org.xmind.ui.tabfolder;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.SubActionBars;
import org.eclipse.ui.part.IPageSite;

public class NestedPageSite implements IPageSite {

    private IPageSite parentSite;

    private ISelectionProvider selectionProvider;

    private SubActionBars actionBars;

    public NestedPageSite(IPageSite parentSite) {
        this.parentSite = parentSite;
        this.actionBars = new SubActionBars(parentSite.getActionBars(), this);
    }

    public IWorkbenchPage getPage() {
        return parentSite.getPage();
    }

    public ISelectionProvider getSelectionProvider() {
        return selectionProvider;
    }

    public Shell getShell() {
        return parentSite.getShell();
    }

    public IWorkbenchWindow getWorkbenchWindow() {
        return parentSite.getWorkbenchWindow();
    }

    public void setSelectionProvider(ISelectionProvider provider) {
        this.selectionProvider = provider;
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter == ISelectionProvider.class)
            return getSelectionProvider();
        return parentSite.getAdapter(adapter);
    }

    @SuppressWarnings("unchecked")
    public Object getService(Class api) {
        return parentSite.getService(api);
    }

    public boolean hasService(Class api) {
        return parentSite.hasService(api);
    }

    public void registerContextMenu(String menuId, MenuManager menuManager,
            ISelectionProvider selectionProvider) {
        parentSite.registerContextMenu(menuId, menuManager, selectionProvider);
    }

    public IActionBars getActionBars() {
        return actionBars;
    }

    /**
     * 
     */
    protected void dispose() {
        actionBars.dispose();
    }

}
