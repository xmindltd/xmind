package org.xmind.ui.internal.actions;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;
import org.xmind.core.ISheet;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.editor.MindMapEditor;

public class AllowManualLayoutMenu extends ContributionItem
        implements IWorkbenchContribution {

    private boolean dirty = true;
//    IGraphicalEditorPage page;
    IWorkbenchWindow window;
    private IMenuListener menuListener = new IMenuListener() {

        public void menuAboutToShow(IMenuManager manager) {
            manager.markDirty();
            dirty = true;
        }
    };

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public void fill(Menu menu, int index) {
        if (getParent() instanceof MenuManager)
            ((MenuManager) getParent()).addMenuListener(menuListener);
        if (!dirty)
            return;

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

    public void fillMenu(MenuManager menuManager) {
        IPreferenceStore prefStore = MindMapUIPlugin.getDefault()
                .getPreferenceStore();
        AllowManualLayoutAction allowManualLayoutAction = new AllowManualLayoutAction(
                prefStore);
        IWorkbenchPart part = window.getActivePage().getActivePart();
        if (null == part || !(part instanceof MindMapEditor)) {
            allowManualLayoutAction.setEnabled(false);
        } else if (part instanceof MindMapEditor) {
            IGraphicalEditorPage page = ((MindMapEditor) part)
                    .getActivePageInstance();
            if (page != null) {
                ISheet sheet = page.getAdapter(ISheet.class);
                String structureClass = sheet.getRootTopic()
                        .getStructureClass();
                allowManualLayoutAction.setEnabled(structureClass == null
                        || structureClass.contains("org.xmind.ui.map")); //$NON-NLS-1$
            }

        }
        menuManager.add(allowManualLayoutAction);
    }

    @Override
    public void initialize(IServiceLocator serviceLocator) {
        window = serviceLocator.getService(IWorkbenchWindow.class);
    }
}
