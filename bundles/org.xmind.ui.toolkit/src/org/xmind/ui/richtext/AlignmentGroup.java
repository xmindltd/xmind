/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.richtext;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.ExternalActionManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManagerOverrides;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

public class AlignmentGroup extends ContributionItem {

    private List<IAction> actions = new ArrayList<IAction>();

    private IAction currentAction = null;

    private IPropertyChangeListener actionListener = null;

    /**
     * Listener for SWT tool item widget events.
     */
    private Listener toolItemListener;

    /**
     * The widget created for this item; <code>null</code> before creation and
     * after disposal.
     */
    private ToolItem widget = null;

    /**
     * Listener for action property change notifications.
     */
    private IPropertyChangeListener currentActionListener = null;

    private MenuManager dropDownMenuManager = null;

    /**
     * Remembers all images in use by this contribution item
     */
    private LocalResourceManager imageManager;

    public void add(IAction action) {
        actions.add(action);
        action.addPropertyChangeListener(getActionListener());
        if (currentAction == null) {
            setCurrentAction(action);
        }
    }

    public void remove(IAction action) {
        action.removePropertyChangeListener(getActionListener());
        actions.remove(action);
        if (currentAction == action) {
            setCurrentAction(actions.isEmpty() ? null : actions.get(0));
        }
    }

    public IAction getCurrentAction() {
        return currentAction;
    }

    public void setCurrentAction(IAction action) {
        if (this.currentAction != null && currentActionListener != null) {
            this.currentAction
                    .removePropertyChangeListener(currentActionListener);
        }
        this.currentAction = action;
        if (action != null) {
            action.setChecked(true);
        }
        for (IAction a : actions) {
            if (a != action) {
                a.setChecked(false);
            }
        }
        if (action != null) {
            if (widget != null && !widget.isDisposed()) {
                action.addPropertyChangeListener(getCurrentActionListener());
            }
        }
        update(null);
    }

    private IPropertyChangeListener getActionListener() {
        if (actionListener == null) {
            actionListener = new IPropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent event) {
                    actionPropertyChange(event);
                }
            };
        }
        return actionListener;
    }

    protected void actionPropertyChange(PropertyChangeEvent event) {
        IAction triggerAction = (IAction) event.getSource();
        String property = event.getProperty();
        if (IAction.CHECKED.equals(property)
                && Boolean.TRUE.equals(event.getNewValue())) {
            setCurrentAction(triggerAction);
        }
    }

//    public void fill(Menu menu, int index) {
//        for (IAction action : actions) {
//            new ActionContributionItem(action).fill(menu, index++);
//        }
//    }
//
    public void fill(ToolBar parent, int index) {
        if (widget == null && parent != null) {
            int flags = SWT.DROP_DOWN;
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
            if (currentAction != null)
                currentAction
                        .addPropertyChangeListener(getCurrentActionListener());
//            }
        }
    }

    private IPropertyChangeListener getCurrentActionListener() {
        if (currentActionListener == null) {
            currentActionListener = new IPropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent event) {
                    currentActionPropertyChange(event);
                }

            };
        }
        return currentActionListener;
    }

    private void currentActionPropertyChange(final PropertyChangeEvent e) {
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

    public void update() {
        update(null);
    }

    public void update(String propertyName) {
        if (widget == null || currentAction == null)
            return;

        boolean textChanged = propertyName == null
                || propertyName.equals(IAction.TEXT);
        boolean imageChanged = propertyName == null
                || propertyName.equals(IAction.IMAGE);
        boolean tooltipTextChanged = propertyName == null
                || propertyName.equals(IAction.TOOL_TIP_TEXT);
        boolean enableStateChanged = propertyName == null
                || propertyName.equals(IAction.ENABLED)
                || propertyName.equals(IContributionManagerOverrides.P_ENABLED);
        boolean checkChanged = (currentAction.getStyle() == IAction.AS_CHECK_BOX || currentAction
                .getStyle() == IAction.AS_RADIO_BUTTON)
                && (propertyName == null || propertyName
                        .equals(IAction.CHECKED));
        ToolItem ti = (ToolItem) widget;
        String text = currentAction.getText();
        // the set text is shown only if there is no image or if forced
        // by MODE_FORCE_TEXT
        boolean showText = text != null && !hasImages(currentAction);

        // only do the trimming if the text will be used
        if (showText && text != null) {
            text = Action.removeAcceleratorText(text);
            text = Action.removeMnemonics(text);
        }

        if (textChanged) {
            String textToSet = showText ? text : ""; //$NON-NLS-1$
            boolean rightStyle = (ti.getParent().getStyle() & SWT.RIGHT) != 0;
            if (rightStyle || !ti.getText().equals(textToSet)) {
                // In addition to being required to update the text if
                // it
                // gets nulled out in the action, this is also a
                // workaround
                // for bug 50151: Using SWT.RIGHT on a ToolBar leaves
                // blank space
                ti.setText(textToSet);
            }
        }

        if (imageChanged) {
            // only substitute a missing image if it has no text
            updateImages(!showText);
        }

        if (tooltipTextChanged || textChanged) {
            String toolTip = currentAction.getToolTipText();
            if ((toolTip == null) || (toolTip.length() == 0)) {
                toolTip = text;
            }

            ExternalActionManager.ICallback callback = ExternalActionManager
                    .getInstance().getCallback();
            String commandId = currentAction.getActionDefinitionId();
            if ((callback != null) && (commandId != null) && (toolTip != null)) {
                String acceleratorText = callback.getAcceleratorText(commandId);
                if (acceleratorText != null && acceleratorText.length() != 0) {
                    toolTip = JFaceResources.format(
                            "Toolbar_Tooltip_Accelerator", //$NON-NLS-1$
                            new Object[] { toolTip, acceleratorText });
                }
            }

            // if the text is showing, then only set the tooltip if
            // different
            if (!showText || toolTip != null && !toolTip.equals(text)) {
                ti.setToolTipText(toolTip);
            } else {
                ti.setToolTipText(null);
            }
        }

        if (enableStateChanged) {
            boolean shouldBeEnabled = currentAction.isEnabled()
                    && isEnabledAllowed();

            if (ti.getEnabled() != shouldBeEnabled) {
                ti.setEnabled(shouldBeEnabled);
            }
        }

        if (checkChanged) {
            boolean bv = currentAction.isChecked();

            if (ti.getSelection() != bv) {
                ti.setSelection(bv);
            }
        }

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

        ImageDescriptor image = currentAction.getHoverImageDescriptor();
        if (image == null) {
            image = currentAction.getImageDescriptor();
        }
        ImageDescriptor disabledImage = currentAction
                .getDisabledImageDescriptor();

        // Make sure there is a valid image.
        if (image == null && forceImage) {
            image = ImageDescriptor.getMissingImageDescriptor();
        }

        LocalResourceManager localManager = new LocalResourceManager(
                parentResourceManager);

        // performance: more efficient in SWT to set disabled and hot
        // image before regular image
        widget.setDisabledImage(disabledImage == null ? null : localManager
                .createImageWithDefault(disabledImage));
        widget.setImage(image == null ? null : localManager
                .createImageWithDefault(image));

        disposeOldImages();
        imageManager = localManager;

        return image != null;
    }

    /**
     * Returns <code>true</code> if this item is allowed to enable,
     * <code>false</code> otherwise.
     * 
     * @return if this item is allowed to be enabled
     * @since 2.0
     */
    protected boolean isEnabledAllowed() {
        if (getParent() == null) {
            return true;
        }
        Boolean value = getParent().getOverrides().getEnabled(this);
        return (value == null) ? true : value.booleanValue();
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
                            handleWidgetSelection(event, ((ToolItem) ew)
                                    .getSelection());
                        }
                        break;
                    }
                }
            };
        }
        return toolItemListener;
    }

    /**
     * Handles a widget dispose event for the widget corresponding to this item.
     */
    private void handleWidgetDispose(Event e) {
        // Check if our widget is the one being disposed.
        if (e.widget == widget) {
            if (dropDownMenuManager != null) {
                dropDownMenuManager.dispose();
                dropDownMenuManager = null;
            }

            // Unhook all of the listeners.
            if (currentAction != null && currentActionListener != null) {
                currentAction
                        .removePropertyChangeListener(currentActionListener);
            }
            // Clear the widget field.
            widget = null;

            disposeOldImages();
        }
    }

    private MenuManager getDropDownMenuManager() {
        if (dropDownMenuManager == null) {
            dropDownMenuManager = new MenuManager();
            dropDownMenuManager.setRemoveAllWhenShown(true);
            dropDownMenuManager.addMenuListener(new IMenuListener() {
                public void menuAboutToShow(IMenuManager manager) {
                    for (IAction action : actions) {
                        manager.add(action);
                    }
                }
            });
        }
        return dropDownMenuManager;
    }

    /**
     * Handles a widget selection event.
     */
    private void handleWidgetSelection(Event e, boolean selection) {
        if (e.widget == widget) {
            MenuManager menuMan = getDropDownMenuManager();
            Menu menu = menuMan.createContextMenu(widget.getParent());
            if (menu != null) {
                Rectangle b = widget.getBounds();
                Point p = widget.getParent().toDisplay(b.x, b.y + b.height);
                menu.setLocation(p.x, p.y);
                menu.setVisible(true);
            }
        }
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