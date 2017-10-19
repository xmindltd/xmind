package org.xmind.ui.internal.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;
import org.xmind.ui.internal.IActionBuilder;
import org.xmind.ui.internal.ImageActionExtensionManager;

public class InsertImageMenu extends ContributionItem implements
        IWorkbenchContribution {

    private IWorkbenchWindow window;
    protected boolean dirty = true;

    private IMenuListener menuListener = new IMenuListener() {
        public void menuAboutToShow(IMenuManager manager) {
            manager.markDirty();
            dirty = true;
        }
    };

    public boolean isDirty() {
        return dirty;
    }

    /**
     * Overridden to always return true and force dynamic menu building.
     */
    public boolean isDynamic() {
        return true;
    }

    public void fill(Menu menu, int index) {
        if (window == null)
            return;

        if (getParent() instanceof MenuManager) {
            ((MenuManager) getParent()).addMenuListener(menuListener);
        }

        if (!dirty) {
            return;
        }

        MenuManager manager = new MenuManager();
        fillMenu(manager);
        IContributionItem items[] = manager.getItems();
        if (items.length > 0) {
            for (int i = 0; i < items.length; i++) {
                items[i].fill(menu, index++);
            }
        }
        dirty = false;
    }

    private void fillMenu(MenuManager manager) {
        List<IActionBuilder> imageActionBuilders = ImageActionExtensionManager
                .getInstance().getActionBuilders();
        List<IWorkbenchAction> imageActionExtensions = new ArrayList<IWorkbenchAction>(
                imageActionBuilders.size());

        IWorkbenchPage page = window.getActivePage();
        if (page != null) {
            for (IActionBuilder builder : imageActionBuilders) {
                IWorkbenchAction imageActionExtension = builder
                        .createAction(page);
                imageActionExtensions.add(imageActionExtension);
            }
            for (IWorkbenchAction action : imageActionExtensions) {
                manager.add(action);
            }
        }
    }

    public void initialize(IServiceLocator serviceLocator) {
        this.window = (IWorkbenchWindow) serviceLocator
                .getService(IWorkbenchWindow.class);
    }

}
