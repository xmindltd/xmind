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
package org.xmind.ui.color;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.ExternalActionManager;
import org.eclipse.jface.action.ExternalActionManager.IBindingManagerCallback;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManagerOverrides;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.bindings.Trigger;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyLookupFactory;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

/**
 * @author Frank Shaka
 */
public class ColorPicker extends ContributionItem
        implements ISelectionProvider {

    private final class ColorChooserPopupDialog extends PopupDialog {

        private Rectangle aroundBounds = null;

        private PaletteViewer viewer;

        private boolean showingNativeColorDialog = false;

        public ColorChooserPopupDialog(Shell parent) {
            super(parent, SWT.NO_TRIM, true, false, false, false, false, null,
                    null);
        }

        protected Control createDialogArea(Composite parent) {
            Composite composite = (Composite) super.createDialogArea(parent);
            GridLayoutFactory layout = GridLayoutFactory.fillDefaults()
                    .spacing(0, 0);
            GridDataFactory layoutData = GridDataFactory.fillDefaults()
                    .grab(true, false);
            layout.copy().margins(4, 4).applyTo(composite);

            if (viewer == null) {
                viewer = new PaletteViewer() {
                    protected RGB openNativeColorDialog(Shell shell,
                            RGB oldColor) {
                        showingNativeColorDialog = true;
                        try {
                            return super.openNativeColorDialog(shell, oldColor);
                        } finally {
                            showingNativeColorDialog = false;
                        }
                    }
                };
                viewer.setShowAutoItem(hasPopupStyle(AUTO));
                viewer.setShowNoneItem(hasPopupStyle(NONE));
                viewer.setShowCustomItem(hasPopupStyle(CUSTOM));
                viewer.addSelectionChangedListener(
                        viewerSelectionChangedListener);
                viewer.addOpenListener(viewerOpenListener);
            }
            viewer.createControl(composite);
            layoutData.applyTo(viewer.getControl());
            viewer.setAutoColor(autoColor);
            viewer.setInput(palette);
            selectionChangedDuringUpdate = true;
            viewer.setSelection(selection);
            selectionChangedDuringUpdate = false;

            return composite;
        }

        public int open(Rectangle aroundBounds) {
            this.aroundBounds = aroundBounds;
            return super.open();
        }

        public int open(Point location) {
            if (location != null) {
                this.aroundBounds = new Rectangle(location.x, location.y, 1, 1);
            }
            return super.open();
        }

        private Point calcLocation(Rectangle aroundBounds, Point shellSize) {
            Point loc = new Point(aroundBounds.x, aroundBounds.y);
            Rectangle area = getShell().getMonitor().getClientArea();
            if (aroundBounds.x + shellSize.x > area.x + area.width) {
                loc.x = aroundBounds.x + aroundBounds.width - shellSize.x;
            }
            if (aroundBounds.y + aroundBounds.height + shellSize.y > area.y
                    + area.height) {
                loc.y = aroundBounds.y - shellSize.y;
            } else {
                loc.y = aroundBounds.y + aroundBounds.height;
            }
            return loc;
        }

        protected Point getInitialLocation(Point initialSize) {
            if (aroundBounds != null) {
                return calcLocation(aroundBounds, initialSize);
            }
            return super.getInitialLocation(initialSize);
        }

        public boolean close() {
            Shell parentShell = null;
            if (getShell() != null && !getShell().isDisposed()
                    && getShell().getParent() instanceof Shell) {
                parentShell = (Shell) getShell().getParent();
            }

            if (showingNativeColorDialog)
                return false;
            boolean closed = super.close();
            this.aroundBounds = null;
            if (action != null) {
                action.setChecked(false);
            }
//            if (viewer != null) {
//                viewer
//                        .removeSelectionChangedListener(viewerSelectionChangedListener);
//                viewer.removeOpenListener(viewerOpenListener);
//            }
            if (parentShell != null && !parentShell.isDisposed()) {
                Shell activeShell = Display.getCurrent().getActiveShell();
                if (parentShell.getData() instanceof PopupDialog
                        && parentShell != activeShell) {
                    ((PopupDialog) parentShell.getData()).close();
                }
            }
            return closed;
        }

        @Override
        protected Color getBackground() {
            return Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
        }

    }

    private final class DropDownAction extends ColorAction {
        private DropDownAction(String text) {
            super(null, IAction.AS_CHECK_BOX);
            setText(text);
        }

        public void run() {
            super.run();
            open();
        }
    }

    /**
     * Popup style for normal palette contents.
     */
    public static final int NORMAL = 0;

    /**
     * Popup style for palette contents with 'Automatic' selection.
     */
    public static final int AUTO = 1;

    /**
     * Popup style for palette contents with 'None' selection.
     */
    public static final int NONE = 1 << 1;

    /**
     * Popup style for palette contents with 'Custom' selection.
     */
    public static final int CUSTOM = 1 << 2;

    /**
     * Mode bit: Show text on tool items, even if an image is present. If this
     * mode bit is not set, text is only shown on tool items if there is no
     * image present.
     * 
     * @since 3.0
     */
    public static int MODE_FORCE_TEXT = 1;

    private static boolean USE_COLOR_ICONS = true;

    /**
     * Returns whether color icons should be used in toolbars.
     * 
     * @return <code>true</code> if color icons should be used in toolbars,
     *         <code>false</code> otherwise
     */
    public static boolean getUseColorIconsInToolbars() {
        return USE_COLOR_ICONS;
    }

    /**
     * Sets whether color icons should be used in toolbars.
     * 
     * @param useColorIcons
     *            <code>true</code> if color icons should be used in toolbars,
     *            <code>false</code> otherwise
     */
    public static void setUseColorIconsInToolbars(boolean useColorIcons) {
        USE_COLOR_ICONS = useColorIcons;
    }

    /**
     * The presentation mode.
     */
    private int mode = 0;

    private Widget widget;

    private IAction action;

    private int popupStyle;

    private PaletteContents palette;

    private ColorChooserPopupDialog popup;

    private IColorSelection selection;

    private RGB autoColor;

    /**
     * Listener for SWT tool item widget events.
     */
    private Listener toolItemListener;

    /**
     * Listener for SWT menu item widget events.
     */
    private Listener menuItemListener;

    /**
     * Remembers all images in use by this contribution item
     */
    private LocalResourceManager imageManager;

    /**
     * Listener for action property change notifications.
     */
    private final IPropertyChangeListener propertyListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
            actionPropertyChange(event);
        }
    };

    /**
     * The listener for changes to the text of the action contributed by an
     * external source.
     */
    private final IPropertyChangeListener actionTextListener = new IPropertyChangeListener() {

        /**
         * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
         */
        public void propertyChange(PropertyChangeEvent event) {
            update(event.getProperty());
        }
    };

    private List<ISelectionChangedListener> selectionChangedListeners = null;

    private List<IOpenListener> openListeners = null;

    private final ISelectionChangedListener viewerSelectionChangedListener = new ISelectionChangedListener() {

        public void selectionChanged(SelectionChangedEvent event) {
            if (selectionChangedDuringUpdate)
                return;
            if (popup != null)
                popup.close();
            setSelection(event.getSelection());
        }

    };

    private final IOpenListener viewerOpenListener = new IOpenListener() {

        public void open(OpenEvent event) {
            if (popup != null)
                popup.close();
            fireOpenEvent(event);
        }

    };

    private boolean selectionChangedDuringUpdate = false;

    public ColorPicker(PaletteContents palette) {
        this(NORMAL, palette, null, null, null);
    }

    public ColorPicker(int popupStyle, PaletteContents palette) {
        this(popupStyle, palette, null, null, null);
    }

    public ColorPicker(int popupStyle, PaletteContents palette, String text) {
        this(popupStyle, palette, text, null, null);
    }

    public ColorPicker(int popupStyle, PaletteContents palette, String text,
            ImageDescriptor image) {
        this(popupStyle, palette, text, image, null);
    }

    public ColorPicker(int popupStyle, PaletteContents palette, String text,
            ImageDescriptor image, String id) {
        super(id);
        if (image != null) {
            this.action = new Action(text, image) {
                public void run() {
                    super.run();
                    open();
                }
            };
        } else {
            this.action = new DropDownAction(text);
        }
        this.popupStyle = popupStyle;
        this.palette = palette;
    }

    public ISelection getSelection() {
        return selection == null ? ColorSelection.EMPTY : selection;
    }

    public void setAutoColor(RGB color) {
        this.autoColor = color;
    }

    public void setSelection(ISelection selection) {
        if (selection == null || !(selection instanceof IColorSelection))
            selection = ColorSelection.EMPTY;
        if (selection == this.selection
                || (selection != null && selection.equals(this.selection)))
            return;
        this.selection = (IColorSelection) selection;
        update(null);
        fireSelectionChanged(new SelectionChangedEvent(this, getSelection()));
    }

    public void addSelectionChangedListener(
            ISelectionChangedListener listener) {
        if (selectionChangedListeners == null)
            selectionChangedListeners = new ArrayList<ISelectionChangedListener>();
        selectionChangedListeners.add(listener);
    }

    public void removeSelectionChangedListener(
            ISelectionChangedListener listener) {
        if (selectionChangedListeners == null)
            return;
        selectionChangedListeners.remove(listener);
    }

    protected void fireSelectionChanged(final SelectionChangedEvent event) {
        if (selectionChangedListeners == null)
            return;
        for (final Object l : selectionChangedListeners.toArray()) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    ((ISelectionChangedListener) l).selectionChanged(event);
                }
            });
        }
    }

    public void addOpenListener(IOpenListener listener) {
        if (openListeners == null)
            openListeners = new ArrayList<IOpenListener>();
        openListeners.add(listener);
    }

    public void removeOpenListener(IOpenListener listener) {
        if (openListeners == null)
            return;
        openListeners.remove(listener);
    }

    protected void fireOpenEvent(final OpenEvent event) {
        if (openListeners == null)
            return;
        for (final Object l : openListeners.toArray()) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    ((IOpenListener) l).open(event);
                }
            });
        }
    }

    private boolean hasPopupStyle(int style) {
        return (popupStyle & style) != 0;
    }

    public void open() {
        if (widget instanceof ToolItem) {
            ToolItem item = (ToolItem) widget;
            ColorChooserPopupDialog dialog = getPopupDialog();
            if (dialog != null) {
                dialog.open(getItemBoundsToDisplay(item));
                popup.getShell().addDisposeListener(new DisposeListener() {

                    @Override
                    public void widgetDisposed(DisposeEvent e) {
                        popup = null;
                    }
                });
            }
        } else {
            Point curLoc = Display.getCurrent().getCursorLocation();
            if (curLoc != null) {
                ColorChooserPopupDialog dialog = getPopupDialog();
                if (dialog != null) {
                    dialog.open(curLoc);
                    popup.getShell().addDisposeListener(new DisposeListener() {

                        @Override
                        public void widgetDisposed(DisposeEvent e) {
                            popup = null;
                        }
                    });
                }
            }
        }
    }

    protected ColorChooserPopupDialog getPopupDialog() {
        if (popup == null) {
            Shell shell = getShell();
            if (shell != null) {
                popup = new ColorChooserPopupDialog(shell);
            }
        }
        return popup;
    }

    private Shell getShell() {
        if (widget instanceof ToolItem) {
            return ((ToolItem) widget).getParent().getShell();
        } else if (widget instanceof MenuItem) {
            return ((MenuItem) widget).getParent().getShell();
        } else if (widget instanceof Control) {
            return ((Control) widget).getShell();
        } else if (Display.getCurrent() != null) {
            return Display.getCurrent().getActiveShell();
        }
        return null;
    }

    private Rectangle getItemBoundsToDisplay(ToolItem item) {
        Rectangle bounds = item.getBounds();
        Point loc = item.getParent().toDisplay(bounds.x, bounds.y);
        bounds.x = loc.x;
        bounds.y = loc.y;
        return bounds;
    }

//    private void close() {
//        if (popup != null) {
//            popup.close();
//        }
//    }

    /**
     * The <code>ActionContributionItem</code> implementation of this ,
     * <code>IContributionItem</code> method creates an SWT
     * <code>ToolItem</code> for the action using the action's style. If the
     * action's checked property has been set, a button is created and primed to
     * the value of the checked property. If the action's menu creator property
     * has been set, a drop-down tool item is created. ATTN: Brian Sun has
     * modified this method!
     */
    public void fill(ToolBar parent, int index) {
        if (widget == null && parent != null) {
            int flags = SWT.PUSH | SWT.RIGHT;
            if (action != null) {
                int style = action.getStyle();
                if (style == IAction.AS_CHECK_BOX) {
                    flags = SWT.CHECK;
                } else if (style == IAction.AS_RADIO_BUTTON) {
                    flags = SWT.RADIO;
                } else if (style == IAction.AS_DROP_DOWN_MENU) {
                    flags = SWT.DROP_DOWN;
                }
            }

            ToolItem ti = null;
            if (index >= 0) {
                ti = new ToolItem(parent, flags, index);
            } else {
                ti = new ToolItem(parent, flags);
            }
            ti.setData(this);
            ti.addListener(SWT.Selection, getToolItemListener());
            ti.addListener(SWT.Dispose, getToolItemListener());

            widget = ti;

            update(null);

            // Attach some extra listeners.
            action.addPropertyChangeListener(propertyListener);
            if (action != null) {
                String commandId = action.getActionDefinitionId();
                ExternalActionManager.ICallback callback = ExternalActionManager
                        .getInstance().getCallback();

                if ((callback != null) && (commandId != null)) {
                    callback.addPropertyChangeListener(commandId,
                            actionTextListener);
                }
            }
        }
    }

    @Override
    public void fill(Menu parent, int index) {
        if (widget == null && parent != null) {
            Menu subMenu = null;
            int flags = SWT.PUSH;
            if (action != null) {
                int style = action.getStyle();
                if (style == IAction.AS_CHECK_BOX) {
                    flags = SWT.CHECK;
                } else if (style == IAction.AS_RADIO_BUTTON) {
                    flags = SWT.RADIO;
                } else if (style == IAction.AS_DROP_DOWN_MENU) {
                    IMenuCreator mc = action.getMenuCreator();
                    if (mc != null) {
                        subMenu = mc.getMenu(parent);
                        flags = SWT.CASCADE;
                    }
                }
            }

            MenuItem mi = null;
            if (index >= 0) {
                mi = new MenuItem(parent, flags, index);
            } else {
                mi = new MenuItem(parent, flags);
            }
            widget = mi;

            mi.setData(this);
            mi.addListener(SWT.Dispose, getMenuItemListener());
            mi.addListener(SWT.Selection, getMenuItemListener());
            if (action.getHelpListener() != null) {
                mi.addHelpListener(action.getHelpListener());
            }

            if (subMenu != null) {
                mi.setMenu(subMenu);
            }

            update(null);

            // Attach some extra listeners.
            action.addPropertyChangeListener(propertyListener);
            if (action != null) {
                String commandId = action.getActionDefinitionId();
                ExternalActionManager.ICallback callback = ExternalActionManager
                        .getInstance().getCallback();

                if ((callback != null) && (commandId != null)) {
                    callback.addPropertyChangeListener(commandId,
                            actionTextListener);
                }
            }
        }
    }

    /**
     * Returns the listener for SWT menu item widget events.
     * 
     * @return a listener for menu item events
     */
    private Listener getMenuItemListener() {
        if (menuItemListener == null) {
            menuItemListener = new Listener() {
                public void handleEvent(Event event) {
                    switch (event.type) {
                    case SWT.Dispose:
                        handleWidgetDispose(event);
                        break;
                    case SWT.Selection:
                        Widget ew = event.widget;
                        if (ew != null) {
                            handleWidgetSelection(event,
                                    ((MenuItem) ew).getSelection());
                        }
                        break;
                    }
                }
            };
        }
        return menuItemListener;
    }

    /**
     * Returns the listener for SWT tool item widget events.
     * 
     * @return a listener for tool item events
     */
    private Listener getToolItemListener() {
        if (toolItemListener == null) {
            toolItemListener = new Listener() {
                public void handleEvent(Event event) {
                    switch (event.type) {
                    case SWT.Dispose:
                        handleWidgetDispose(event);
                        break;
                    case SWT.Selection:
                        Widget ew = event.widget;
                        if (ew != null) {
                            handleWidgetSelection(event,
                                    ((ToolItem) ew).getSelection());
                        }
                        break;
                    }
                }
            };
        }
        return toolItemListener;
    }

    /**
     * Handles a property change event on the action (forwarded by nested
     * listener).
     */
    private void actionPropertyChange(final PropertyChangeEvent e) {
        // This code should be removed. Avoid using free asyncExec

        if (isVisible() && widget != null) {
            Display display = widget.getDisplay();
            if (display.getThread() == Thread.currentThread()) {
                update(e.getProperty());
            } else {
                display.asyncExec(new Runnable() {
                    public void run() {
                        update(e.getProperty());
                    }
                });
            }

        }
    }

    /**
     * Returns the action associated with this contribution item.
     * 
     * @return the action
     */
    public IAction getAction() {
        return action;
    }

    /**
     * Returns the presentation mode, which is the bitwise-or of the
     * <code>MODE_*</code> constants. The default mode setting is 0, meaning
     * that for menu items, both text and image are shown (if present), but for
     * tool items, the text is shown only if there is no image.
     * 
     * @return the presentation mode settings
     * @since 3.0
     */
    public int getMode() {
        return mode;
    }

    /*
     * (non-Javadoc) Method declared on Object.
     */
    public int hashCode() {
        return action.hashCode();
    }

    /**
     * Returns whether the given action has any images.
     * 
     * @param actionToCheck
     *            the action
     * @return <code>true</code> if the action has any images,
     *         <code>false</code> if not
     */
    private boolean hasImages(IAction actionToCheck) {
        return actionToCheck.getImageDescriptor() != null
                || actionToCheck.getHoverImageDescriptor() != null
                || actionToCheck.getDisabledImageDescriptor() != null;
    }

//    /**
//     * Returns whether the command corresponding to this action
//     * is active.
//     */
//    private boolean isCommandActive() {
//        IAction actionToCheck = getAction();
//
//        if (actionToCheck != null) {
//            String commandId = actionToCheck.getActionDefinitionId();
//            ExternalActionManager.ICallback callback = ExternalActionManager
//                    .getInstance().getCallback();
//
//            if (callback != null) {
//                return callback.isActive(commandId);
//            }
//        }
//        return true;
//    }

    /**
     * The action item implementation of this <code>IContributionItem</code>
     * method returns <code>true</code> for menu items and <code>false</code>
     * for everything else.
     */
    public boolean isDynamic() {
        if (widget instanceof MenuItem) {
            //Optimization. Only recreate the item is the check or radio style has changed. 
            boolean itemIsCheck = (widget.getStyle() & SWT.CHECK) != 0;
            boolean actionIsCheck = getAction() != null
                    && getAction().getStyle() == IAction.AS_CHECK_BOX;
            boolean itemIsRadio = (widget.getStyle() & SWT.RADIO) != 0;
            boolean actionIsRadio = getAction() != null
                    && getAction().getStyle() == IAction.AS_RADIO_BUTTON;
            return (itemIsCheck != actionIsCheck)
                    || (itemIsRadio != actionIsRadio);
        }
        return false;
    }

    /*
     * (non-Javadoc) Method declared on IContributionItem.
     */
    public boolean isEnabled() {
        return action != null && action.isEnabled();
    }

//    /**
//     * Returns <code>true</code> if this item is allowed to enable,
//     * <code>false</code> otherwise.
//     * 
//     * @return if this item is allowed to be enabled
//     * @since 2.0
//     */
//    protected boolean isEnabledAllowed() {
//        if (getParent() == null) {
//            return true;
//        }
//        Boolean value = getParent().getOverrides().getEnabled(this);
//        return (value == null) ? true : value.booleanValue();
//    }
//
//    /**
//     * The <code>ActionContributionItem</code> implementation of this 
//     * <code>ContributionItem</code> method extends the super implementation
//     * by also checking whether the command corresponding to this action is active.
//     */
//    public boolean isVisible() {
//        return super.isVisible() && isCommandActive();
//    }

    /**
     * Sets the presentation mode, which is the bitwise-or of the
     * <code>MODE_*</code> constants.
     * 
     * @param mode
     *            the presentation mode settings
     * @since 3.0
     */
    public void setMode(int mode) {
        this.mode = mode;
        update();
    }

    /**
     * Handles a widget dispose event for the widget corresponding to this item.
     */
    private void handleWidgetDispose(Event e) {
        // Check if our widget is the one being disposed.
        if (e.widget == widget) {
            // Dispose of the menu creator.
            if (action.getStyle() == IAction.AS_DROP_DOWN_MENU) {
                IMenuCreator mc = action.getMenuCreator();
                if (mc != null) {
                    mc.dispose();
                }
            }

            // Unhook all of the listeners.
            action.removePropertyChangeListener(propertyListener);
            if (action != null) {
                String commandId = action.getActionDefinitionId();
                ExternalActionManager.ICallback callback = ExternalActionManager
                        .getInstance().getCallback();

                if ((callback != null) && (commandId != null)) {
                    callback.removePropertyChangeListener(commandId,
                            actionTextListener);
                }
            }

            // Clear the widget field.
            widget = null;

            disposeOldImages();
        }
    }

    /**
     * Handles a widget selection event.
     */
    private void handleWidgetSelection(Event e, boolean selection) {

        Widget item = e.widget;
        if (item != null) {
            int style = item.getStyle();

            if ((style & (SWT.TOGGLE | SWT.CHECK)) != 0) {
                if (action.getStyle() == IAction.AS_CHECK_BOX) {
                    action.setChecked(selection);
                }
            } else if ((style & SWT.RADIO) != 0) {
                if (action.getStyle() == IAction.AS_RADIO_BUTTON) {
                    action.setChecked(selection);
                }
            } else if ((style & SWT.DROP_DOWN) != 0) {
                if (e.detail == 4) { // on drop-down button
                    if (action.getStyle() == IAction.AS_DROP_DOWN_MENU) {
//                        IMenuCreator mc = action.getMenuCreator();
//                        ToolItem ti = (ToolItem) item;
//                        // we create the menu as a sub-menu of "dummy" so that we can use
//                        // it in a cascading menu too.
//                        // If created on a SWT control we would get an SWT error...
//                        //Menu dummy= new Menu(ti.getParent());
//                        //Menu m= mc.getMenu(dummy);
//                        //dummy.dispose();
//                        if (mc != null) {
//                            Menu m = mc.getMenu(ti.getParent());
//                            if (m != null) {
//                                // position the menu below the drop down item
//                                Rectangle b = ti.getBounds();
//                                Point p = ti.getParent().toDisplay(
//                                        new Point(b.x, b.y + b.height));
//                                m.setLocation(p.x, p.y); // waiting for SWT 0.42
//                                m.setVisible(true);
//                                return; // we don't fire the action
//                            }
//                        }
                    }
                }
            }

            // Ensure action is enabled first.
            // See 1GAN3M6: ITPUI:WINNT - Any IAction in the workbench can be executed while disabled.
            if (action.isEnabled()) {
                boolean trace = Policy.TRACE_ACTIONS;

                long ms = System.currentTimeMillis();
                if (trace) {
                    System.out.println("Running action: " + action.getText()); //$NON-NLS-1$
                }

                action.runWithEvent(e);

                if (trace) {
                    System.out.println((System.currentTimeMillis() - ms)
                            + " ms to run action: " + action.getText()); //$NON-NLS-1$
                }
            }
        }
    }

    /**
     * The action item implementation of this <code>IContributionItem</code>
     * method calls <code>update(null)</code>.
     */
    public final void update() {
        update(null);
    }

    /**
     * Synchronizes the UI with the given property. ATTN: Brian Sun �޸�������
     * 
     * @param propertyName
     *            the name of the property, or <code>null</code> meaning all
     *            applicable properties
     */
    public void update(String propertyName) {
        if (widget != null) {
            // determine what to do         
            boolean textChanged = propertyName == null
                    || propertyName.equals(IAction.TEXT);
            boolean imageChanged = propertyName == null
                    || propertyName.equals(IAction.IMAGE);
            boolean tooltipTextChanged = propertyName == null
                    || propertyName.equals(IAction.TOOL_TIP_TEXT);
            boolean enableStateChanged = propertyName == null
                    || propertyName.equals(IAction.ENABLED) || propertyName
                            .equals(IContributionManagerOverrides.P_ENABLED);
            boolean checkChanged = (action.getStyle() == IAction.AS_CHECK_BOX
                    || action.getStyle() == IAction.AS_RADIO_BUTTON)
                    && (propertyName == null
                            || propertyName.equals(IAction.CHECKED));
            boolean colorChanged = propertyName == null
                    || propertyName.equals(IColorAction.COLOR);

            if (widget instanceof ToolItem) {
                //int toolbarStyle = SWT.NONE;

                ToolItem ti = (ToolItem) widget;
                String text = action.getText();
                // the set text is shown only if there is no image or if forced by MODE_FORCE_TEXT
                boolean showText = text != null
                        && ((getMode() & MODE_FORCE_TEXT) != 0
                                || !hasImages(action));
//                        && ((toolbarStyle & BFaceConstants.TOOLBAR_TEXT)!=0 || 
//                                ((toolbarStyle & BFaceConstants.TOOLBAR_TEXT_RIGHT)!=0 && hasRightText));

                // only do the trimming if the text will be used
                if (showText && text != null) {
                    text = Action.removeAcceleratorText(text);
                    text = Action.removeMnemonics(text);
                }

                if (textChanged) {
                    String textToSet = showText ? text : ""; //$NON-NLS-1$
                    boolean rightStyle = (ti.getParent().getStyle()
                            & SWT.RIGHT) != 0;
                    if (rightStyle || !ti.getText().equals(textToSet)) {
                        // In addition to being required to update the text if it
                        // gets nulled out in the action, this is also a workaround 
                        // for bug 50151: Using SWT.RIGHT on a ToolBar leaves blank space
                        ti.setText(textToSet);
                    }
                }

                if (imageChanged) {
                    // only substitute a missing image if it has no text
                    updateImages(!showText);
                }

                if (tooltipTextChanged || textChanged) {
                    String toolTip = action.getToolTipText();
                    if ((toolTip == null) || (toolTip.length() == 0)) {
                        toolTip = text;
                    }
                    // if the text is showing, then only set the tooltip if
                    // different
                    if (!showText || toolTip != null) {
//                            && !toolTip.equals(text)) {
                        ti.setToolTipText(toolTip);
                    } else {
                        ti.setToolTipText(null);
                    }
                }

                if (enableStateChanged) {
                    boolean shouldBeEnabled = action.isEnabled();
//                            && isEnabledAllowed();

                    if (ti.getEnabled() != shouldBeEnabled) {
                        ti.setEnabled(shouldBeEnabled);
                    }
                }

                if (checkChanged) {
                    boolean bv = action.isChecked();

                    if (ti.getSelection() != bv) {
                        ti.setSelection(bv);
                    }
                }

                if (colorChanged) {
                    updateColors();
                }
                return;
            }

            if (widget instanceof MenuItem) {
                MenuItem mi = (MenuItem) widget;

                if (textChanged) {
                    int accelerator = 0;
                    String acceleratorText = null;
                    IAction updatedAction = getAction();
                    String text = null;
                    accelerator = updatedAction.getAccelerator();
                    ExternalActionManager.ICallback callback = ExternalActionManager
                            .getInstance().getCallback();

                    // Block accelerators that are already in use.
                    if ((accelerator != 0) && (callback != null)
                            && (callback.isAcceleratorInUse(accelerator))) {
                        accelerator = 0;
                    }

                    /*
                     * Process accelerators on GTK in a special way to avoid Bug
                     * 42009. We will override the native input method by
                     * allowing these reserved accelerators to be placed on the
                     * menu. We will only do this for "Ctrl+Shift+[0-9A-FU]".
                     */
                    final String commandId = updatedAction
                            .getActionDefinitionId();
                    if (("gtk".equals(SWT.getPlatform())) //$NON-NLS-1$
                            && (callback instanceof IBindingManagerCallback)
                            && (commandId != null)) {
                        final IBindingManagerCallback bindingManagerCallback = (IBindingManagerCallback) callback;
                        final IKeyLookup lookup = KeyLookupFactory.getDefault();
                        final TriggerSequence[] triggerSequences = bindingManagerCallback
                                .getActiveBindingsFor(commandId);
                        for (int i = 0; i < triggerSequences.length; i++) {
                            final TriggerSequence triggerSequence = triggerSequences[i];
                            final Trigger[] triggers = triggerSequence
                                    .getTriggers();
                            if (triggers.length == 1) {
                                final Trigger trigger = triggers[0];
                                if (trigger instanceof KeyStroke) {
                                    final KeyStroke currentKeyStroke = (KeyStroke) trigger;
                                    final int currentNaturalKey = currentKeyStroke
                                            .getNaturalKey();
                                    if ((currentKeyStroke
                                            .getModifierKeys() == (lookup
                                                    .getCtrl()
                                                    | lookup.getShift()))
                                            && ((currentNaturalKey >= '0'
                                                    && currentNaturalKey <= '9')
                                                    || (currentNaturalKey >= 'A'
                                                            && currentNaturalKey <= 'F')
                                                    || (currentNaturalKey == 'U'))) {
                                        accelerator = currentKeyStroke
                                                .getModifierKeys()
                                                | currentNaturalKey;
                                        acceleratorText = triggerSequence
                                                .format();
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if (accelerator == 0) {
                        if ((callback != null) && (commandId != null)) {
                            acceleratorText = callback
                                    .getAcceleratorText(commandId);
                        }
                    } else {
                        acceleratorText = Action
                                .convertAccelerator(accelerator);
                    }

                    IContributionManagerOverrides overrides = null;

                    if (getParent() != null) {
                        overrides = getParent().getOverrides();
                    }

                    if (overrides != null) {
                        text = getParent().getOverrides().getText(this);
                    }

                    mi.setAccelerator(accelerator);

                    if (text == null) {
                        text = updatedAction.getText();
                    }

                    if (text == null) {
                        text = ""; //$NON-NLS-1$
                    } else {
                        text = Action.removeAcceleratorText(text);
                    }

                    if (acceleratorText == null) {
                        mi.setText(text);
                    } else {
                        mi.setText(text + '\t' + acceleratorText);
                    }
                }

                if (imageChanged) {
                    updateImages(false);
                }

                if (enableStateChanged) {
                    boolean shouldBeEnabled = action.isEnabled();
//                            && isEnabledAllowed();

                    if (mi.getEnabled() != shouldBeEnabled) {
                        mi.setEnabled(shouldBeEnabled);
                    }
                }

                if (checkChanged) {
                    boolean bv = action.isChecked();

                    if (mi.getSelection() != bv) {
                        mi.setSelection(bv);
                    }
                }

                if (colorChanged) {
                    updateColors();
                }
                return;
            }

            if (widget instanceof Button) {
                Button button = (Button) widget;

                if (imageChanged && updateImages(false)) {
                    textChanged = false; // don't update text if it has an image
                }

                if (textChanged) {
                    String text = action.getText();
                    if (text == null) {
                        text = ""; //$NON-NLS-1$
                    } else {
                        text = Action.removeAcceleratorText(text);
                    }
                    button.setText(text);
                }

                if (tooltipTextChanged) {
                    button.setToolTipText(action.getToolTipText());
                }

                if (enableStateChanged) {
                    boolean shouldBeEnabled = action.isEnabled();
//                            && isEnabledAllowed();

                    if (button.getEnabled() != shouldBeEnabled) {
                        button.setEnabled(shouldBeEnabled);
                    }
                }

                if (checkChanged) {
                    boolean bv = action.isChecked();

                    if (button.getSelection() != bv) {
                        button.setSelection(bv);
                    }
                }

                if (colorChanged) {
                    updateColors();
                }
                return;
            }
        }

    }

    private void updateColors() {
        if (selection != null) {
            RGB c = selection.getColor();
            if (action instanceof IColorAction) {
                ((IColorAction) action).setColor(c);
            }
        }
    }

    /**
     * Updates the images for this action.
     * 
     * @param forceImage
     *            <code>true</code> if some form of image is compulsory, and
     *            <code>false</code> if it is acceptable for this item to have
     *            no image
     * @return <code>true</code> if there are images for this action,
     *         <code>false</code> if not
     */
    private boolean updateImages(boolean forceImage) {

        ResourceManager parentResourceManager = JFaceResources.getResources();

        if (widget instanceof ToolItem) {
            if (USE_COLOR_ICONS) {
                ImageDescriptor image = action.getHoverImageDescriptor();
                if (image == null) {
                    image = action.getImageDescriptor();
                }
                ImageDescriptor disabledImage = action
                        .getDisabledImageDescriptor();

                // Make sure there is a valid image.
                if (image == null && forceImage) {
                    image = ImageDescriptor.getMissingImageDescriptor();
                }

                LocalResourceManager localManager = new LocalResourceManager(
                        parentResourceManager);

                // performance: more efficient in SWT to set disabled and hot image before regular image
                ((ToolItem) widget).setDisabledImage(disabledImage == null
                        ? null
                        : localManager.createImageWithDefault(disabledImage));
                ((ToolItem) widget).setImage(image == null ? null
                        : localManager.createImageWithDefault(image));

                disposeOldImages();
                imageManager = localManager;

                return image != null;
            }
            ImageDescriptor image = action.getImageDescriptor();
            ImageDescriptor hoverImage = action.getHoverImageDescriptor();
            ImageDescriptor disabledImage = action.getDisabledImageDescriptor();

            // If there is no regular image, but there is a hover image,
            // convert the hover image to gray and use it as the regular image.
            if (image == null && hoverImage != null) {
                image = ImageDescriptor.createWithFlags(
                        action.getHoverImageDescriptor(), SWT.IMAGE_GRAY);
            } else {
                // If there is no hover image, use the regular image as the hover image,
                // and convert the regular image to gray
                if (hoverImage == null && image != null) {
                    hoverImage = image;
                    image = ImageDescriptor.createWithFlags(
                            action.getImageDescriptor(), SWT.IMAGE_GRAY);
                }
            }

            // Make sure there is a valid image.
            if (hoverImage == null && image == null && forceImage) {
                image = ImageDescriptor.getMissingImageDescriptor();
            }

            // Create a local resource manager to remember the images we've allocated for this tool item
            LocalResourceManager localManager = new LocalResourceManager(
                    parentResourceManager);

            // performance: more efficient in SWT to set disabled and hot image before regular image
            ((ToolItem) widget).setDisabledImage(disabledImage == null ? null
                    : localManager.createImageWithDefault(disabledImage));
            ((ToolItem) widget).setHotImage(hoverImage == null ? null
                    : localManager.createImageWithDefault(hoverImage));
            ((ToolItem) widget).setImage(image == null ? null
                    : localManager.createImageWithDefault(image));

            // Now that we're no longer referencing the old images, clear them out.
            disposeOldImages();
            imageManager = localManager;

            return image != null;
        } else if (widget instanceof Item || widget instanceof Button) {

            // Use hover image if there is one, otherwise use regular image.
            ImageDescriptor image = action.getHoverImageDescriptor();
            if (image == null) {
                image = action.getImageDescriptor();
            }
            // Make sure there is a valid image.
            if (image == null && forceImage) {
                image = ImageDescriptor.getMissingImageDescriptor();
            }

            // Create a local resource manager to remember the images we've allocated for this widget
            LocalResourceManager localManager = new LocalResourceManager(
                    parentResourceManager);

            if (widget instanceof Item) {
                ((Item) widget).setImage(image == null ? null
                        : localManager.createImageWithDefault(image));
            } else if (widget instanceof Button) {
                ((Button) widget).setImage(image == null ? null
                        : localManager.createImageWithDefault(image));
            }

            // Now that we're no longer referencing the old images, clear them out.
            disposeOldImages();
            imageManager = localManager;

            return image != null;
        }
        return false;
    }

    /**
     * Dispose any images allocated for this contribution item
     */
    private void disposeOldImages() {
        if (imageManager != null) {
            imageManager.dispose();
            imageManager = null;
        }
    }

}
