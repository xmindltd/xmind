package org.xmind.ui.internal.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.dialogs.DialogMessages;
import org.xmind.ui.internal.editor.WorkbookHistory;
import org.xmind.ui.internal.editor.WorkbookHistoryItem;
import org.xmind.ui.util.Logger;

/**
 * 
 * @author Frank Shaka
 * @deprecated Use {@link RecentFileListContributionItem} instead
 */
public class ReopenWorkbookMenu extends Action implements IWorkbenchAction {

    private static final String MODIFIED_TIME_KEY = "org.xmind.ui.workbookHistory.lastModifiedTime"; //$NON-NLS-1$

    private class WorkbookHistoryMenuCreator
            implements IMenuCreator, MenuListener {

        private Menu menu;

        public void dispose() {
            if (menu != null) {
                menu.dispose();
                menu = null;
            }
        }

        public Menu getMenu(Control parent) {
            if (menu != null && !menu.isDisposed())
                return menu;
            menu = new Menu(parent);
            menu.addMenuListener(this);
            return menu;
        }

        public Menu getMenu(Menu parent) {
            if (menu != null && !menu.isDisposed())
                return menu;
            menu = new Menu(parent);
            menu.addMenuListener(this);
            return menu;
        }

        public void menuHidden(MenuEvent e) {
        }

        public void menuShown(MenuEvent e) {
            refreshMenu((Menu) e.widget);
        }

    }

    private IWorkbenchWindow window;

    public ReopenWorkbookMenu(IWorkbenchWindow window) {
        this(window, ActionConstants.REOPEN_WORKBOOK_MENU_ID);
    }

    public ReopenWorkbookMenu(IWorkbenchWindow window, String id) {
        super(MindMapMessages.ReopenWorkbookMenu_text);
        this.window = window;
        setId(id);
        setToolTipText(MindMapMessages.ReopenWorkbookMenu_toolTip);
        setMenuCreator(new WorkbookHistoryMenuCreator());
    }

    public void dispose() {
        this.window = null;
    }

    @Override
    public void runWithEvent(Event event) {
        if (event.widget instanceof ToolItem) {
            showDropDownMenu((ToolItem) event.widget, event);
        }
    }

    private void showDropDownMenu(ToolItem ti, Event e) {
        IMenuCreator mc = getMenuCreator();
        if (mc == null)
            return;

        Menu m = mc.getMenu(ti.getParent());
        if (m != null) {
            // position the menu below the drop down item
            Rectangle r = ti.getBounds();
            Point point = ti.getParent()
                    .toDisplay(new Point(r.x, r.y + r.height));
            m.setLocation(point.x, point.y); // waiting
                                             // for SWT
                                             // 0.42
            m.setVisible(true);
            return; // we don't fire the action
        }
    }

    private void refreshMenu(Menu menu) {
        long lastModifiedTime = WorkbookHistory.getInstance()
                .getLastModifiedTime();
        Object cachedLastModifiedTime = menu.getData(MODIFIED_TIME_KEY);
        if (cachedLastModifiedTime instanceof Long
                && ((Long) cachedLastModifiedTime)
                        .longValue() >= lastModifiedTime)
            return;

        rebuildMenu(menu);
        menu.setData(MODIFIED_TIME_KEY, Long.valueOf(lastModifiedTime));
    }

    private void rebuildMenu(Menu menu) {
        removeAll(menu);
        fillMenu(menu);
        if (menu.getItemCount() == 0) {
            MenuItem placeholder = new MenuItem(menu, SWT.PUSH, 0);
            placeholder.setText(
                    MindMapMessages.ReopenWorkbookMenu_NoItemsPlaceholder_text);
            placeholder.setEnabled(false);
        }
    }

    private void removeAll(Menu menu) {
        int total = menu.getItemCount();
        for (int i = total; i > 0; i--) {
            menu.getItem(i - 1).dispose();
        }
    }

    private void fillMenu(Menu menu) {
        if (window == null || window.getActivePage() == null)
            return;

        int itemsToShow = WorkbenchPlugin.getDefault().getPreferenceStore()
                .getInt(IPreferenceConstants.RECENT_FILES);
        if (itemsToShow <= 0)
            return;

        WorkbookHistoryItem[] items = WorkbookHistory.getInstance()
                .getTopItems(itemsToShow);
        if (items.length == 0)
            return;

        int index = fillWithItems(menu, 0, items);
        if (menu.getItemCount() > 0) {
            new MenuItem(menu, SWT.SEPARATOR, index);
            ++index;
            fillClearItem(menu, index);
        }
    }

    private int fillWithItems(Menu menu, int index,
            WorkbookHistoryItem[] items) {
        Object[] paths = getDisambiguatedPaths(items);
        for (int i = 0; i < items.length; i++) {
            WorkbookHistoryItem item = items[i];
            fillWithItem(menu, index, item, i, paths[i].toString());
            ++index;
        }
        return index;
    }

    private void fillWithItem(Menu menu, int menuItemIndex,
            final WorkbookHistoryItem item, int itemIndex, String path) {
        MenuItem mi = new MenuItem(menu, SWT.PUSH, menuItemIndex);
        mi.setText(path);
        mi.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                reopen(item);
            }
        });
    }

    private int fillClearItem(Menu menu, int index) {
        MenuItem mi = new MenuItem(menu, SWT.PUSH, index);
        mi.setText(MindMapMessages.ReopenWorkbookMenu_ClearListAction_text);
        mi.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                clear();
            }
        });
        ++index;

        return index;
    }

    private static class FileDissection {
        String head;
        String neck;
        File body;

        public FileDissection(File file) {
            this.head = file.getName();
            this.neck = null;
            this.body = file.getParentFile();
        }

        public void increaseNeck() {
            if (body == null)
                return;
            String name = body.getName();
            body = body.getParentFile();
            if (neck == null) {
                neck = name;
            } else {
                neck = name + File.separator + neck;
            }
        }

        public boolean canDisambiguateWith(FileDissection that) {
            return that != null && this.head.equals(that.head)
                    && (this.neck == null || this.neck == that.neck
                            || (this.neck != null
                                    && this.neck.equals(that.neck)))
                    && (this.body != null || that.body != null);
        }

        @Override
        public String toString() {
            return neck == null ? head
                    : String.format("%2$s - %3$s", File.separator, head, neck); //$NON-NLS-1$
        }
    }

    private Object[] getDisambiguatedPaths(WorkbookHistoryItem[] items) {
        Object[] paths = new Object[items.length];
        List<FileDissection> files = new ArrayList<FileDissection>(
                items.length);

        for (int i = 0; i < items.length; i++) {
            WorkbookHistoryItem item = items[i];
            String uri = item.getURI();
            String path = item.getPath();
            if (uri.equals(path)) {
                // Workbook loaded from a URL:
                paths[i] = uri;
            } else {
                // Workbook loaded form a local file:
                FileDissection file = new FileDissection(new File(path));
                paths[i] = file;
                files.add(file);
            }
        }

        for (int i = 1; i < files.size(); i++) {
            FileDissection file1 = files.get(i);
            for (int j = 0; j < i; j++) {
                FileDissection file2 = files.get(j);
                while (file1.canDisambiguateWith(file2)) {
                    file1.increaseNeck();
                    file2.increaseNeck();
                }
            }
        }

        return paths;
    }

    private void reopen(final WorkbookHistoryItem item) {
        if (window == null)
            return;

        IWorkbenchPage page = window.getActivePage();
        if (page == null)
            return;

        if (item.getExistingEditorInput() != null) {
            IEditorPart editor = page.findEditor(item.getExistingEditorInput());
            if (editor != null) {
                page.activate(editor);
                return;
            }
        }

        try {
            item.reopen(page);
        } catch (CoreException e) {
            Logger.log(e);
        }
    }

    private void clear() {
        if (window == null)
            return;

        if (!MessageDialog.openConfirm(window.getShell(),
                DialogMessages.ConfirmClearRecentFileListDialog_title,
                DialogMessages.ConfirmClearRecentFileListDialog_message))
            return;

        WorkbookHistory.getInstance().clear();
    }

}
