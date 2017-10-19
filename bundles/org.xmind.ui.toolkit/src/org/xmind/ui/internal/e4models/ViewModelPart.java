package org.xmind.ui.internal.e4models;

import org.eclipse.e4.ui.internal.workbench.OpaqueElementUtil;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.renderers.swt.MenuManagerRenderer;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.Accessible;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.xmind.ui.dialogs.Messages;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.resources.FontUtils;

@SuppressWarnings("restriction")
public class ViewModelPart extends ModelPart {

    private Composite rightBar;

    private Composite menuBar;

    private boolean adjusting;

    private ToolBar viewMenuTB;

    private Label tipLabel;

    protected void createContent(Composite parent) {
        CTabFolder ctf = new CTabFolder(parent, SWT.BORDER);
        ctf.setRenderer(new ViewModelFolderRenderer(ctf));
        ctf.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        CTabItem ti = new CTabItem(ctf, SWT.NONE);
        MPart partModel = getAdapter(MPart.class);
        ti.setText(getLabel(partModel, partModel.getLocalizedLabel()));
        ti.setToolTipText(getToolTip(partModel.getLocalizedTooltip()));
        ctf.setSelection(ti);

        Composite contentContainer = new Composite(ctf, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        contentContainer.setLayout(layout);

        ti.setControl(contentContainer);

        Control content = doCreateContent(contentContainer);
        content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        addTopRight(ctf, true);
        adjustViewMenuBar(true);
    }

    protected Control doCreateContent(Composite parent) {
        return null;
    }

    protected String getLabel(MUILabel itemPart, String newName) {
        if (newName == null) {
            newName = ""; //$NON-NLS-1$
        } else {
            newName = LegacyActionTools.escapeMnemonics(newName);
        }

        if (itemPart instanceof MDirtyable
                && ((MDirtyable) itemPart).isDirty()) {
            newName = '*' + newName;
        }
        return newName;
    }

    protected String getToolTip(String newToolTip) {
        return newToolTip == null || newToolTip.length() == 0 ? null
                : LegacyActionTools.escapeMnemonics(newToolTip);
    }

    /**
     * @param visibleOrEnable
     *            true is to setVisible, otherwise to setEnable.
     */
    protected void addTopRight(CTabFolder ctf, boolean visibleOrEnable) {
        rightBar = new Composite(ctf, SWT.NONE);
        rightBar.setBackground(
                (Color) resources.get(ColorUtils.toDescriptor("#f4f4f4"))); //$NON-NLS-1$

        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 0;
        rightBar.setLayout(layout);

        createTipLabel(rightBar);
        createMenuBar(rightBar, visibleOrEnable);

        ctf.setTopRight(rightBar, SWT.RIGHT);
    }

    private void createTipLabel(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.marginRight = 5;
        composite.setLayout(layout);

        tipLabel = new Label(composite, SWT.NONE);
        tipLabel.setBackground(composite.getBackground());
        GridData layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, true);
        layoutData.verticalIndent = 3;
        tipLabel.setLayoutData(layoutData);
        tipLabel.setForeground(
                (Color) resources.get(ColorUtils.toDescriptor("#a1a1a1"))); //$NON-NLS-1$
        tipLabel.setFont((Font) resources
                .get(FontDescriptor.createFrom(FontUtils.relativeHeight(
                        tipLabel.getFont().getFontData(), -1))));

        //set initial state
        composite.setVisible(false);
        ((GridData) composite.getLayoutData()).exclude = true;
    }

    private void createMenuBar(Composite parent, boolean visibleOrEnable) {
        menuBar = new Composite(parent, SWT.NONE);
        menuBar.setBackground(parent.getBackground());
        menuBar.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        menuBar.setLayout(gridLayout);

        viewMenuTB = new ToolBar(menuBar, SWT.FLAT | SWT.RIGHT);
        viewMenuTB.setBackground(menuBar.getBackground());
        viewMenuTB.setLayoutData(
                new GridData(SWT.CENTER, SWT.BOTTOM, false, true));
        viewMenuTB.setData(TAG_VIEW_MENU);
        ToolItem ti = new ToolItem(viewMenuTB, SWT.PUSH);
        ti.setToolTipText(Messages.ViewModelPart_MenuBar_toolTip);

        if (visibleOrEnable) {
            // Initially it's not visible
            menuBar.setVisible(false);
        } else {
            viewMenuTB.setEnabled(false);
        }

        ti.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                showViewMenu((ToolItem) e.widget);
            }

            public void widgetDefaultSelected(SelectionEvent e) {
                showViewMenu((ToolItem) e.widget);
            }
        });
        viewMenuTB.getAccessible()
                .addAccessibleListener(new AccessibleAdapter() {
                    @Override
                    public void getName(AccessibleEvent e) {
                        if (e.childID != ACC.CHILDID_SELF) {
                            Accessible accessible = (Accessible) e.getSource();
                            ToolBar toolBar = (ToolBar) accessible.getControl();
                            if (0 <= e.childID
                                    && e.childID < toolBar.getItemCount()) {
                                ToolItem item = toolBar.getItem(e.childID);
                                if (item != null) {
                                    e.result = item.getToolTipText();
                                }
                            }
                        }
                    }
                });
    }

    protected void setTip(String tip) {
        tipLabel.setText(tip == null ? "" : tip); //$NON-NLS-1$

        Composite tipComposite = tipLabel.getParent();
        tipComposite.setVisible(tip != null && !tip.equals("")); //$NON-NLS-1$
        ((GridData) tipComposite.getLayoutData()).exclude = !tipComposite
                .getVisible();

        adjustRightBar();
    }

    private void adjustRightBar() {
        rightBar.setVisible(
                tipLabel.getParent().getVisible() || menuBar.getVisible());
        rightBar.pack(true);
    }

    protected void showViewMenu(ToolItem item) {
        MPart part = getAdapter(MPart.class);
        if (part == null)
            return;

        Control ctrl = (Control) part.getWidget();
        MMenu menuModel = getViewMenu(part);
        if (menuModel == null || !menuModel.isToBeRendered())
            return;

        final Menu swtMenu = (Menu) part.getContext()
                .get(IPresentationEngine.class)
                .createGui(menuModel, ctrl.getShell(), part.getContext());
        if (swtMenu == null)
            return;

        ctrl.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                if (!swtMenu.isDisposed()) {
                    swtMenu.dispose();
                }
            }
        });

        // ...and Show it...
        Rectangle ib = item.getBounds();
        Point displayAt = item.getParent().toDisplay(ib.x, ib.y + ib.height);
        swtMenu.setLocation(displayAt);
        swtMenu.setVisible(true);

        Display display = swtMenu.getDisplay();
        while (!swtMenu.isDisposed() && swtMenu.isVisible()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        if (!swtMenu.isDisposed()
                && !(swtMenu.getData() instanceof MenuManager)) {
            swtMenu.dispose();
        }

    }

    /**
     * @param visibleOrEnable
     *            true is to setVisible, otherwise to setEnable.
     */
    protected void adjustViewMenuBar(boolean visibleOrEnable) {
        if (adjusting)
            return;

        if (menuBar == null)
            return;

        adjusting = true;

        MPart part = getAdapter(MPart.class);
        MMenu viewMenu = getViewMenu(part);
        boolean needsMenu = viewMenu != null
                && hasVisibleMenuItems(viewMenu, part);

        if (visibleOrEnable) {
            menuBar.setVisible(needsMenu);
            ((GridData) menuBar.getLayoutData()).exclude = !menuBar
                    .getVisible();
        } else {
            viewMenuTB.setEnabled(needsMenu);
        }

        adjustRightBar();

        adjusting = false;
    }

    @SuppressWarnings("restriction")
    private boolean hasVisibleMenuItems(MMenu viewMenu, MPart part) {
        if (!viewMenu.isToBeRendered() || !viewMenu.isVisible()) {
            return false;
        }

        for (MMenuElement menuElement : viewMenu.getChildren()) {
            if (menuElement.isToBeRendered() && menuElement.isVisible()) {
                if (OpaqueElementUtil.isOpaqueMenuItem(menuElement)
                        || OpaqueElementUtil
                                .isOpaqueMenuSeparator(menuElement)) {
                    IContributionItem item = (IContributionItem) OpaqueElementUtil
                            .getOpaqueItem(menuElement);
                    if (item != null && item.isVisible()) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }

        Object menuRenderer = viewMenu.getRenderer();
        if (menuRenderer instanceof MenuManagerRenderer) {
            MenuManager manager = ((MenuManagerRenderer) menuRenderer)
                    .getManager(viewMenu);
            if (manager != null && manager.isVisible()) {
                return true;
            }
        }

        Control control = (Control) part.getWidget();
        if (control != null) {
            Menu menu = (Menu) part.getContext().get(IPresentationEngine.class)
                    .createGui(viewMenu, control.getShell(), part.getContext());
            if (menu != null) {
                menuRenderer = viewMenu.getRenderer();
                if (menuRenderer instanceof MenuManagerRenderer) {
                    MenuManagerRenderer menuManagerRenderer = (MenuManagerRenderer) menuRenderer;
                    MenuManager manager = menuManagerRenderer
                            .getManager(viewMenu);
                    if (manager != null) {
                        // remark ourselves as dirty so that the menu will be
                        // reconstructed
                        manager.markDirty();
                    }
                }
                return menu.getItemCount() != 0;
            }
        }
        return false;
    }

    protected MMenu getViewMenu(MPart part) {
        if (part == null || part.getMenus() == null) {
            return null;
        }
        for (MMenu menu : part.getMenus()) {
            boolean viewMenu = menu.getTags().contains(TAG_VIEW_MENU);
            if (viewMenu) {
                return menu;
            }
        }
        return null;
    }

}
