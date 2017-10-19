package org.xmind.ui.tabfolder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.xmind.ui.util.IStyleProvider;
import org.xmind.ui.util.StyleProvider;

public class MTabFolder extends Composite {

    /**
     * Used as style key for retrieving properties related to a certain part of
     * this control.
     */
    public static final String TAB_BAR = "tabBar"; //$NON-NLS-1$
    public static final String BODY = "body"; //$NON-NLS-1$

    private MTabBar tabBar;

    private Composite body;

    private MTabItem tooltipItem;

    private IStyleProvider styleProvider = new StyleProvider();
    private boolean usingDefaultStyles = true;

    private Listener itemEventHandler = new Listener() {
        public void handleEvent(Event event) {
            switch (event.type) {
            case SWT.Selection:
                handleItemSelection(event);
                break;
            }
        }
    };

    private Listener mouseMoveEventHandler = new Listener() {
        public void handleEvent(Event event) {
            switch (event.type) {
            case SWT.MouseMove:
                handleItemMouseEntered(new Point(event.x, event.y));
                break;
            }
        }
    };

    public MTabFolder(Composite parent) {
        this(parent, SWT.NONE);
    }

    public MTabFolder(Composite parent, int tabBarStyle) {
        super(parent, SWT.NONE);

        GridLayout layout = new GridLayout();
        layout.marginTop = 0;
        layout.marginBottom = 0;
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        layout.makeColumnsEqualWidth = false;
        super.setLayout(layout);

        this.tabBar = new MTabBar(this, tabBarStyle);
        this.tabBar
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        this.tabBar.addListener(SWT.Selection, itemEventHandler);
        this.tabBar.addListener(SWT.MouseMove, mouseMoveEventHandler);
        initialItemTooltip();

        this.tabBar.setStyleProvider(getStyleProvider());

        this.body = new Composite(this, SWT.NONE);

        StackLayout stackLayout = new StackLayout();
        stackLayout.marginWidth = 0;
        stackLayout.marginHeight = 0;
        this.body.setLayout(stackLayout);
        this.body.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        updateTabFolder();
    }

    @Override
    public void setLayout(Layout layout) {
        checkWidget();
        // prevents layout from being changed by clients
    }

    protected MTabBar getTabBar() {
        return this.tabBar;
    }

    public Composite getBody() {
        return this.body;
    }

    public MTabItem[] getItems() {
        checkWidget();
        MTabBarItem[] barItems = tabBar.getItems();
        MTabItem[] items = new MTabItem[barItems.length];
        System.arraycopy(barItems, 0, items, 0, barItems.length);
        return items;
    }

    public int getItemCount() {
        checkWidget();
        return tabBar.getItemCount();
    }

    public MTabItem getItem(int index) {
        checkWidget();
        return (MTabItem) tabBar.getItem(index);
    }

    public MTabItem getItem(Point pt) {
        checkWidget();
        return (MTabItem) tabBar.getItem(pt);
    }

    protected void updateTabFolder() {
        IStyleProvider styles = getStyleProvider();
        Color background = styles.getColor(this, TAB_BAR);
        if (background == null) {
            this.tabBar.setBackground(
                    getDisplay().getSystemColor(SWT.COLOR_TRANSPARENT));
        } else {
            this.tabBar.setBackground(background);
        }

        Color bodyBackground = styles.getColor(this, BODY);
        if (bodyBackground == null) {
            this.body.setBackground(
                    getDisplay().getSystemColor(SWT.COLOR_TRANSPARENT));
        } else {
            this.body.setBackground(bodyBackground);
        }

        int tabBarPosition = styles.getPosition(this, TAB_BAR, SWT.TOP);
        GridLayout layout = (GridLayout) getLayout();
        GridData tabBarLayoutData = (GridData) this.tabBar.getLayoutData();
        if ((tabBarPosition & (SWT.TOP | SWT.BOTTOM)) != 0) {
            // vertical layout
            layout.numColumns = 1;
            tabBarLayoutData.grabExcessHorizontalSpace = true;
            tabBarLayoutData.grabExcessVerticalSpace = false;
        } else {
            // horizontal layout
            layout.numColumns = 2;
            tabBarLayoutData.grabExcessHorizontalSpace = false;
            tabBarLayoutData.grabExcessVerticalSpace = true;
        }

        if ((tabBarPosition & (SWT.TOP | SWT.LEFT)) != 0) {
            this.tabBar.moveAbove(this.body);
        } else {
            this.tabBar.moveBelow(this.body);
        }
        this.tabBar.setVertical((tabBarPosition & (SWT.TOP | SWT.BOTTOM)) == 0);
        this.tabBar.updateTabBar();

        layout(true, true);
        this.tabBar.redraw();
        this.body.redraw();
    }

    public MTabItem getSelection() {
        checkWidget();
        return (MTabItem) this.tabBar.getSelection();
    }

    public void setSelection(MTabItem item) {
        checkWidget();
        MTabItem selectedItem = getSelection();
        if (item == selectedItem || (item != null
                && (item.isSeparator() || item.isPushButton())))
            return;

        this.tabBar.setSelection(item);
        if (item != null) {
            showItemControl(item);
        }
    }

    private void handleItemSelection(Event event) {
        MTabItem item = (MTabItem) event.item;
        if (!item.isSeparator() && !item.isPushButton()) {
            setSelection(item);
            showItemControl(item);
        }
        Event e = new Event();
        e.x = event.x;
        e.y = event.y;
        e.item = event.item;
        e.index = event.index;
        e.detail = event.detail;
        notifyListeners(SWT.Selection, e);
    }

    private void showItemControl(MTabItem item) {
        if (item.getControl() != null) {
            StackLayout stackLayout = (StackLayout) this.body.getLayout();
            stackLayout.topControl = item.getControl();
            this.body.layout(true);
            item.getControl().moveAbove(null);
        }
    }

    private void handleItemMouseEntered(Point location) {
        MTabItem item = (MTabItem) getItem(location);
        if (item != tooltipItem) {
            if (tabBar.getToolTipText() != null) {
                tabBar.setToolTipText(null);
            }
            if (item != null) {
                String itemTooltipText = item.getTooltipText();
                if (itemTooltipText != null && !"".equals(itemTooltipText)) { //$NON-NLS-1$
                    tabBar.setToolTipText(itemTooltipText);
                }
            }
            tooltipItem = item;
        }
    }

    private void initialItemTooltip() {
        final Display display = tabBar.getDisplay();
        display.asyncExec(new Runnable() {

            @Override
            public void run() {
                if (tabBar != null && !tabBar.isDisposed()) {
                    Point location = tabBar
                            .toControl(display.getCursorLocation());
                    handleItemMouseEntered(location);
                }
            }
        });
    }

    public IStyleProvider getStyleProvider() {
        checkWidget();
        return styleProvider;
    }

    public void setStyleProvider(IStyleProvider styleProvider) {
        checkWidget();
        IStyleProvider oldStyleProvider = usingDefaultStyles ? null
                : this.styleProvider;
        if (styleProvider == oldStyleProvider)
            return;
        if (styleProvider != null) {
            this.styleProvider = styleProvider;
            usingDefaultStyles = false;
        } else {
            this.styleProvider = new StyleProvider();
            usingDefaultStyles = true;
        }
        reskin(SWT.NONE);
        //force to trigger skin event
        pack(false);

        updateTabFolder();
        tabBar.setStyleProvider(getStyleProvider());
    }

    protected void createItem(MTabItem item) {
        if (item.isRadioButton() && item.isSelected()) {
            showItemControl(item);
        }
    }

    protected void destroyItem(MTabItem item) {
        if (item.isRadioButton()) {
            MTabItem selectedItem = getSelection();
            if (selectedItem != null) {
                showItemControl(selectedItem);
            } else {
                StackLayout stackLayout = (StackLayout) this.body.getLayout();
                stackLayout.topControl = null;
                this.body.layout(true);
            }
        }
    }

    protected void updateItem(MTabItem item) {
        if (item.isRadioButton() && item.isSelected()) {
            showItemControl(item);
        }
    }

}
