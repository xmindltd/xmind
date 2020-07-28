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
package org.xmind.gef.event;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.RangeModel;
import org.eclipse.draw2d.ToolTipHelper;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.GraphicalViewer;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.acc.IAccessible;
import org.xmind.gef.dnd.DndData;
import org.xmind.gef.dnd.IDndClient;
import org.xmind.gef.dnd.IDndSupport;
import org.xmind.gef.draw2d.KeepVisibleToolTipHelper;
import org.xmind.gef.part.GraphicalEditPart;
import org.xmind.gef.part.IGraphicalEditPart;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.status.IStatusMachine;
import org.xmind.gef.tool.AbstractTool;
import org.xmind.gef.tool.IDragDropHandler;
import org.xmind.gef.tool.IGraphicalTool;
import org.xmind.gef.tool.ITool;

/**
 * @author Brian Sun
 */
public class PartsEventDispatcher extends ViewerEventDispatcher
        implements DropTargetListener {

    protected class PartAccessibilityDispatcher
            extends AccessibilityDispatcher {

        private IAccessible get(int childID) {
            if (childID == ACC.CHILDID_SELF || childID == ACC.CHILDID_NONE) {
                IPart focused = getFocusedPart();
                if (focused != null) {
                    IAccessible acc = (IAccessible) focused
                            .getAdapter(IAccessible.class);
                    if (acc != null)
                        return acc;
                }
                IPart contents = getViewer().getRootPart().getContents();
                if (contents == null)
                    return null;
                return (IAccessible) contents.getAdapter(IAccessible.class);
            }
            return getViewer().getAccessibleRegistry().getAccessible(childID);
        }

        public void getChildAtPoint(AccessibleControlEvent e) {
            IPart part = findPart(e.x, e.y);
            if (part == null)
                return;
            IAccessible acc = (IAccessible) part.getAdapter(IAccessible.class);
            if (acc != null) {
                e.childID = acc.getAccessibleId();
            }
        }

        public void getChildCount(AccessibleControlEvent e) {
            e.detail = getViewer().getAccessibleRegistry().getNumAccessibles();
        }

        public void getChildren(AccessibleControlEvent e) {
            e.children = getViewer().getAccessibleRegistry()
                    .getAllAccessibleIDs();
        }

        public void getDefaultAction(AccessibleControlEvent e) {
            IAccessible acc = get(e.childID);
            if (acc != null) {
                String defaultAction = acc.getDefaultAction();
                if (defaultAction != null)
                    e.result = defaultAction;
            }
        }

        public void getFocus(AccessibleControlEvent e) {
            if (control.isFocusControl()) {
                IPart focusedPart = getFocusedPart();
                if (focusedPart != null) {
                    IAccessible acc = (IAccessible) focusedPart
                            .getAdapter(IAccessible.class);
                    if (acc != null) {
                        e.childID = acc.getAccessibleId();
                        return;
                    }
                }
                e.childID = ACC.CHILDID_SELF;
            } else {
                e.childID = ACC.CHILDID_NONE;
            }
        }

        private IPart getFocusedPart() {
            Object focused = getViewer().getFocused();
            return focused == null ? null
                    : getViewer().getSelectionSupport()
                            .findSelectablePart(focused);
        }

        public void getLocation(AccessibleControlEvent e) {
            IAccessible acc = get(e.childID);
            if (acc != null) {
                Rectangle r = acc.getLocation();
                if (r != null) {
                    e.x = r.x;
                    e.y = r.y;
                    if (r.width >= 0)
                        e.width = r.width;
                    if (r.height >= 0)
                        e.height = r.height;
                }
            }
        }

        public void getRole(AccessibleControlEvent e) {
            IAccessible acc = get(e.childID);
            if (acc != null) {
                int role = acc.getRole();
                if (role >= 0) {
                    e.detail = role;
                }
            }
        }

        public void getSelection(AccessibleControlEvent e) {
            List<IPart> selectedParts = getViewer().getSelectionSupport()
                    .getPartSelection();
            if (selectedParts.isEmpty()) {
                if (getViewer().getControl().isFocusControl()) {
                    e.childID = ACC.CHILDID_SELF;
                } else {
                    e.childID = ACC.CHILDID_NONE;
                }
                return;
            }

            List<Integer> childIds = new ArrayList<Integer>(
                    selectedParts.size());
            for (IPart p : selectedParts) {
                IAccessible acc = (IAccessible) p.getAdapter(IAccessible.class);
                if (acc != null) {
                    childIds.add(acc.getAccessibleId());
                }
            }
            if (childIds.isEmpty()) {
                e.childID = ACC.CHILDID_NONE;
            } else {
                e.childID = ACC.CHILDID_MULTIPLE;
                e.children = childIds.toArray();
            }
        }

        public void getState(AccessibleControlEvent e) {
            IAccessible acc = get(e.childID);
            if (acc != null) {
                int state = acc.getState();
                if (state >= 0) {
                    e.detail = state;
                }
            }
        }

        public void getValue(AccessibleControlEvent e) {
            IAccessible acc = get(e.childID);
            if (acc != null) {
                String value = acc.getValue();
                if (value != null) {
                    e.result = value;
                }
            }
        }

        public void getDescription(AccessibleEvent e) {
            IAccessible acc = get(e.childID);
            if (acc != null) {
                String description = acc.getDescription();
                if (description != null) {
                    e.result = description;
                }
            }
        }

        public void getHelp(AccessibleEvent e) {
            IAccessible acc = get(e.childID);
            if (acc != null) {
                String help = acc.getHelp();
                if (help != null) {
                    e.result = help;
                }
            }
        }

        public void getKeyboardShortcut(AccessibleEvent e) {
            IAccessible acc = get(e.childID);
            if (acc != null) {
                String keyboardShortcut = acc.getKeyboardShortcut();
                if (keyboardShortcut != null)
                    e.result = keyboardShortcut;
            }
        }

        public void getName(AccessibleEvent e) {
            IAccessible acc = get(e.childID);
            if (acc != null) {
                String name = acc.getName();
                if (name != null) {
                    e.result = name;
                }
            }
        }

    }

    private static int LONG_PRESSING_ACTIVATION_TIME = 500;

    private static int DEFAULT_START_DRAG_THRESHOLD = 3;

    private ToolTipHelper toolTipHelper;

    private Shell shell = null;

    private DropTarget dropTarget = null;

    private boolean ignoreDoubleClicking = false;

    private boolean ignoreLongPressing = false;

    private boolean ignoreDragging = false;

    private boolean ignoreNextMouseUp = false;

    private MouseDragEvent lastDragEvent = null;

    private MouseEvent currentMouseEvent = null;

    private DragDropEvent currentDropEvent = null;

    private IDragDropHandler dropHandler = null;

    private int pressedMouseButton = 0;

    private PartAccessibilityDispatcher accDispatcher = null;

    private PropertyChangeListener viewportScrollListener = null;

    private RangeModel horizontalRangeModel = null;

    private RangeModel verticalRangeModel = null;

    private boolean mouseHovering = false;

    private boolean isDraging = false;

    /**
     * @param domain
     */
    public PartsEventDispatcher(IGraphicalViewer viewer) {
        super(viewer);
    }

    public EditDomain getDomain() {
        return getViewer().getEditDomain();
    }

    protected void onActivated() {
        super.onActivated();
        setIgnoreDoubleClicking(false);
    }

    protected void onDeactivated() {
        hideToolTip();
        cancelLongPressing();

        lastDragEvent = null;
        currentMouseEvent = null;
        currentDropEvent = null;
        dropHandler = null;

        setIgnoreDoubleClicking(true);
    }

    @Override
    public void setControl(Control c) {
        if (c == control)
            return;
        if (control != null && !control.isDisposed())
            throw new RuntimeException(
                    "Can not set control again once it has been set"); //$NON-NLS-1$
        if (c != null)
            c.addDisposeListener(new org.eclipse.swt.events.DisposeListener() {
                public void widgetDisposed(DisposeEvent e) {
                    if (toolTipHelper != null)
                        toolTipHelper.dispose();
                }
            });
        control = c;

        createDropTarget(c);
    }

    protected void createDropTarget(Control c) {
        if (dropTarget != null) {
            dropTarget.dispose();
            dropTarget = null;
        }

        if (getDndSupport() != null) {
            dropTarget = new DropTarget(c, getDndSupport().getStyle());
            dropTarget.setTransfer(getDndSupport().getTransfers());
            dropTarget.addDropListener(this);
            c.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e) {
                    disposeDropTarget();
                }
            });
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.gef.event.ViewerEventDispatcher#dndSupportChanged(org.xmind
     * .gef.dnd.IDndSupport, org.xmind.gef.dnd.IDndSupport)
     */
    @Override
    protected void dndSupportChanged(IDndSupport oldDndSupport,
            IDndSupport newDndSupport) {
        super.dndSupportChanged(oldDndSupport, newDndSupport);
        if (control != null && !control.isDisposed()) {
            createDropTarget(control);
        }
    }

    protected DropTarget getDropTarget() {
        return dropTarget;
    }

    protected void disposeDropTarget() {
        if (dropTarget != null) {
            if (!dropTarget.isDisposed()) {
                dropTarget.removeDropListener(this);
            }
            dropTarget.dispose();
            dropTarget = null;
        }
    }

    /**
     * @param controlX
     * @param controlY
     * @return
     */
    protected Point convertPoint(int controlX, int controlY) {
        Point p = new Point(controlX, controlY);
        if (getViewer().getControl() != null
                && !getViewer().getControl().isDisposed()) {
//            if (isScaleLocation()) {
            p = getViewer().computeToLayer(p, true);
//            } else {
//                p.translate(getViewer().getScrollPosition());
//            }
        }
        return p;
    }

    /**
     * @param me
     * @return
     */
    protected MouseEvent convertMouse(org.eclipse.swt.events.MouseEvent me) {
        Point pos = convertPoint(me.x, me.y);
        return MouseEvent.createEvent(me, findPart(me.x, me.y), pos);
    }

    protected MouseEvent convertMouse(org.eclipse.swt.events.MouseEvent me,
            IPart host) {
        Point pos = convertPoint(me.x, me.y);
        return MouseEvent.createEvent(me, host, pos);
    }

    /**
     * @param me
     * @return
     */
    protected MouseDragEvent convertDrag(org.eclipse.swt.events.MouseEvent me) {
        Point pos = convertPoint(me.x, me.y);
        return MouseDragEvent.createEvent(me, lastDragEvent, pos,
                findPart(me.x, me.y));
    }

    protected MouseDragEvent convertDrag(org.eclipse.swt.events.MouseEvent me,
            IPart host) {
        Point pos = convertPoint(me.x, me.y);
        return MouseDragEvent.createEvent(me, lastDragEvent, pos, host);
    }

    protected MouseDragEvent convertDrag(org.eclipse.swt.events.MouseEvent me,
            MouseEvent current) {
        return MouseDragEvent.createEvent(me, lastDragEvent,
                current.cursorLocation, current.target);
    }

    /**
     * @param ke
     * @return
     */
    protected KeyEvent convertKey(org.eclipse.swt.events.KeyEvent ke) {
        return KeyEvent.createEvent(ke, isImeOpened());
    }

    protected MouseWheelEvent convertWheel(org.eclipse.swt.widgets.Event e) {
        return MouseWheelEvent.createEvent(findPart(e.x, e.y), e);
    }

    /**
     * @param me
     * @return
     */
    protected MouseDragEvent createDragEvent(
            org.eclipse.swt.events.MouseEvent me) {
        Point startPoint = convertPoint(me.x, me.y);
        return MouseDragEvent.createEvent(me, findPart(me.x, me.y), startPoint);
    }

    public PartAccessibilityDispatcher getPartAccessibilityDispatcher() {
        if (accDispatcher == null)
            accDispatcher = new PartAccessibilityDispatcher();
        return accDispatcher;
    }

    @Override
    protected AccessibilityDispatcher getAccessibilityDispatcher() {
        return getPartAccessibilityDispatcher();
    }

    protected IPart findPart(int controlX, int controlY) {
        IPart part = getViewer().findPart(controlX, controlY);
        return part == null ? getViewer().getRootPart() : part;
    }

//    /**
//     * @param position
//     */
//    protected IPart findPart(Point position) {
//        return findPart(getViewer().getRootPart(), position);
//    }
//
//    protected IPart findPart(IPart parent, Point position) {
//        if (parent == null)
//            return null;
//        if (parent instanceof IGraphicalEditPart) {
//            return (((IGraphicalEditPart) parent).findAt(position));
//        }
//        for (IPart p : parent.getChildren()) {
//            IPart ret = findPart(p, position);
//            if (ret != null)
//                return ret;
//        }
//        return null;
//    }

    protected ITool getActiveTool() {
        EditDomain domain = getDomain();
        return domain != null && !domain.isDisposed() ? domain.getActiveTool()
                : null;
    }

    protected boolean isImeOpened() {
        if (shell == null) {
            if (control != null & !control.isDisposed()) {
                shell = control.getShell();
            } else {
                Control viewerControl = getViewer().getControl();
                if (viewerControl != null && !viewerControl.isDisposed())
                    shell = viewerControl.getShell();
            }
        }
        return shell == null ? false : shell.getImeInputMode() != SWT.NONE;
    }

    /**
     * @see org.eclipse.draw2d.SWTEventDispatcher#dispatchFocusGained(org.eclipse.swt.events.FocusEvent)
     */
    @Override
    public void dispatchFocusGained(FocusEvent e) {
        if (!isActive())
            return;
//        super.dispatchFocusGained(e);
        ITool tool = getActiveTool();
        if (tool != null) {
            tool.focusGained(getViewer());
            MouseEvent me = currentMouseEvent;
            if (me != null) {
                updateCursor(me.cursorLocation, me.target);
            }
        } else {
            super.dispatchFocusGained(e);
        }
        updateFocus();
    }

    /**
     * @see org.eclipse.draw2d.SWTEventDispatcher#dispatchFocusLost(org.eclipse.swt.events.FocusEvent)
     */
    @Override
    public void dispatchFocusLost(FocusEvent e) {
        cancelLongPressing();
        if (!isActive())
            return;
//        super.dispatchFocusLost(e);
        ITool tool = getActiveTool();
        if (tool != null) {
            tool.focusLost(getViewer());
            MouseEvent me = currentMouseEvent;
            if (me != null) {
                updateCursor(me.cursorLocation, me.target);
            }
        } else {
            super.dispatchFocusLost(e);
        }
    }

    /**
     * @see org.eclipse.draw2d.SWTEventDispatcher#dispatchKeyPressed(org.eclipse.swt.events.KeyEvent)
     */
    @Override
    public void dispatchKeyPressed(org.eclipse.swt.events.KeyEvent e) {
        if (!isActive())
            return;
//        super.dispatchKeyPressed(e);
        ITool tool = getActiveTool();
        if (tool != null) {
            KeyEvent ke = convertKey(e);
            tool.keyDown(ke, getViewer());
            e.doit = !ke.isConsumed();
            MouseEvent me = currentMouseEvent;
            if (me != null && isValidGraphicalPart(me.target)) {
                updateCursor(me.cursorLocation, me.target);
            }
        } else {
            super.dispatchKeyPressed(e);
        }
        if ((e.keyCode & SWT.ESC) != 0) {
            lastDragEvent = null;
        }
        updateFocus();
    }

    /**
     * @see org.eclipse.draw2d.SWTEventDispatcher#dispatchKeyReleased(org.eclipse.swt.events.KeyEvent)
     */
    @Override
    public void dispatchKeyReleased(org.eclipse.swt.events.KeyEvent e) {
        if (!isActive())
            return;
//        super.dispatchKeyReleased(e);
        ITool tool = getActiveTool();
        if (tool != null) {
            KeyEvent ke = convertKey(e);
            tool.keyUp(ke, getViewer());
            e.doit = !ke.isConsumed();
            MouseEvent me = currentMouseEvent;
            if (me != null && isValidGraphicalPart(me.target)) {
                updateCursor(me.cursorLocation, me.target);
            }
        } else {
            super.dispatchKeyReleased(e);
        }
        updateFocus();
    }

    /**
     * @see org.eclipse.draw2d.SWTEventDispatcher#dispatchKeyTraversed(org.eclipse.swt.events.TraverseEvent)
     */
    @Override
    public void dispatchKeyTraversed(TraverseEvent e) {
        if (!isActive())
            return;
//        super.dispatchKeyTraversed(e);
        ITool tool = getActiveTool();
        if (tool != null) {
            KeyEvent ke = convertKey(e);
            tool.keyTraversed(ke, getViewer());
            e.doit = !ke.isConsumed();
            e.detail = ke.traverse;
            MouseEvent me = currentMouseEvent;
            if (me != null && isValidGraphicalPart(me.target)) {
                updateCursor(me.cursorLocation, me.target);
            }
        } else {
            super.dispatchKeyTraversed(e);
        }
        updateFocus();
    }

    /**
     * @see org.eclipse.draw2d.SWTEventDispatcher#dispatchMouseDoubleClicked(org.eclipse.swt.events.MouseEvent)
     */
    @Override
    public void dispatchMouseDoubleClicked(
            org.eclipse.swt.events.MouseEvent me) {
        if (!isActive())
            return;

//        super.dispatchMouseDoubleClicked(me);

        cancelToolTipShowing();

        if (ignoresDoubleClicking()) {
            setIgnoreDoubleClicking(false);
            return;
        }
        receive(me);
        ITool tool = getActiveTool();
        if (tool != null) {
            MouseEvent e = currentMouseEvent;
            if (tool instanceof IGraphicalTool) {
                ((IGraphicalTool) tool).setCursorPosition(e.cursorLocation);
            }
            tool.mouseDoubleClick(e, getViewer());
            updateCursor(e.cursorLocation, e.target);
            ignoreDragging = e.isConsumed() && pressedMouseButton != 0;
        } else {
            super.dispatchMouseDoubleClicked(me);
        }
        updateFocus();
    }

    /**
     * @see org.eclipse.draw2d.SWTEventDispatcher#dispatchMouseEntered(org.eclipse.swt.events.MouseEvent)
     */
    @Override
    public void dispatchMouseEntered(org.eclipse.swt.events.MouseEvent me) {
        if (!isActive())
            return;
//        super.dispatchMouseEntered(me);
        mouseHovering = false;
        hookScrollBars();
        cancelToolTipShowing();
        receive(me);
        ITool tool = getActiveTool();
        if (tool != null) {
            MouseEvent e = currentMouseEvent;
            if (tool instanceof IGraphicalTool) {
                ((IGraphicalTool) tool).setCursorPosition(e.cursorLocation);
            }
            tool.mouseEntered(e, getViewer());
            updateCursor(e.cursorLocation, e.target);
        } else {
            super.dispatchMouseEntered(me);
        }
        updateFocus();
    }

    /**
     * @see org.eclipse.draw2d.SWTEventDispatcher#dispatchMouseExited(org.eclipse.swt.events.MouseEvent)
     */
    @Override
    public void dispatchMouseExited(org.eclipse.swt.events.MouseEvent me) {
        unhookScrollBars();
        if (!isActive())
            return;
//        super.dispatchMouseExited(me);
        mouseHovering = false;
        cancelToolTipShowing();
        hideToolTip();
        receive(me);
        ITool tool = getActiveTool();
        if (tool != null) {
            MouseEvent e = currentMouseEvent;
            if (tool instanceof IGraphicalTool) {
                ((IGraphicalTool) tool).setCursorPosition(e.cursorLocation);
            }
            tool.mouseExited(e, getViewer());
            updateCursor(e.cursorLocation, e.target);
        } else {
            super.dispatchMouseExited(me);
        }
        updateFocus();
    }

    private void hookScrollBars() {
        Viewport viewport = getViewer().getCanvas().getViewport();
        horizontalRangeModel = viewport.getHorizontalRangeModel();
        horizontalRangeModel
                .addPropertyChangeListener(getViewportScrollListener());
        verticalRangeModel = viewport.getVerticalRangeModel();
        verticalRangeModel
                .addPropertyChangeListener(getViewportScrollListener());
    }

    private void unhookScrollBars() {
        if (horizontalRangeModel != null) {
            horizontalRangeModel
                    .removePropertyChangeListener(getViewportScrollListener());
            horizontalRangeModel = null;
        }
        if (verticalRangeModel != null) {
            verticalRangeModel
                    .removePropertyChangeListener(getViewportScrollListener());
            verticalRangeModel = null;
        }
    }

    private PropertyChangeListener getViewportScrollListener() {
        if (viewportScrollListener == null) {
            viewportScrollListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    dispatchViewerScrolled(evt);
                }
            };
        }
        return viewportScrollListener;
    }

    protected void dispatchViewerScrolled(PropertyChangeEvent evt) {
        if (lastDragEvent != null || currentDropEvent != null
                || Boolean.TRUE.equals(getViewer().getProperties()
                        .get(IGraphicalViewer.VIEWER_IGNORE_SCROLL_EVENT)))
            return;

        Display currentDisplay = Display.getCurrent();
        org.eclipse.swt.graphics.Point loc = currentDisplay.getCursorLocation();
        loc = control.toControl(loc);
        org.eclipse.swt.graphics.Rectangle bounds = control.getBounds();
        if (bounds.contains(loc)) {
            org.eclipse.swt.widgets.Event event = new org.eclipse.swt.widgets.Event();
            org.eclipse.swt.events.MouseEvent last = currentMouseEvent == null
                    ? null : currentMouseEvent.currentSWTEvent;
            if (last != null) {
                event.button = last.button;
                event.count = last.count;
                event.data = last.data;
                event.display = last.display;
                event.stateMask = last.stateMask;
                event.widget = last.widget;
            } else {
                event.display = currentDisplay;
                event.widget = control;
            }
            event.time = (int) System.currentTimeMillis();
            event.x = loc.x;
            event.y = loc.y;
            dispatchMouseMoved(new org.eclipse.swt.events.MouseEvent(event));
        }
    }

    /**
     * @see org.eclipse.draw2d.SWTEventDispatcher#dispatchMouseHover(org.eclipse.swt.events.MouseEvent)
     */
    @Override
    public void dispatchMouseHover(org.eclipse.swt.events.MouseEvent me) {
        if (!isActive())
            return;

//        super.dispatchMouseHover(me);
        mouseHovering = true;
        cancelToolTipShowing();
        receive(me);
        ITool tool = getActiveTool();
        if (tool != null) {
            MouseEvent e = currentMouseEvent;
            if (tool instanceof IGraphicalTool) {
                ((IGraphicalTool) tool).setCursorPosition(e.cursorLocation);
            }
            tool.mouseHover(e, getViewer());
            updateCursor(e.cursorLocation, e.target);
            updateToolTip(e);
        } else {
            super.dispatchMouseHover(me);
        }
        updateFocus();
    }

    public void cancelToolTipShowing() {
        // if ( tooltipTimer != null ) {
        // tooltipTimer.cancel();
        // tooltipTimer = null;
        // }
    }

    public void updateToolTip() {
        if (control == null || control.isDisposed())
            return;
        hideToolTip();
        if (mouseHovering && currentMouseEvent != null) {
            updateToolTip(currentMouseEvent, false);
        }
    }

    protected void updateToolTip(MouseEvent me, boolean useEventTarget) {
        Point cp = null;
        org.eclipse.swt.events.MouseEvent e = me.getCurrentSWTEvent();
        if (e != null) {
            cp = new Point(e.x, e.y);
        } else {
            cp = getViewer().computeToControl(me.cursorLocation, true);
        }

        IPart target = useEventTarget ? me.target : findPart(cp.x, cp.y);
        if (target != null && target instanceof IGraphicalPart) {
            ITool tool = getActiveTool();
            IFigure tooltip = null;
            if (tool instanceof IGraphicalTool) {
                tooltip = ((IGraphicalTool) tool).getToolTip(target,
                        me.cursorLocation);
            }
            if (tooltip == null && target instanceof IGraphicalEditPart) {
                tooltip = ((IGraphicalEditPart) target)
                        .findTooltipAt(me.cursorLocation);
            }
            if (tooltip != null) {
                Point absolute = new Point(control.toDisplay(cp.x, cp.y));
                getToolTipHelper().displayToolTipNear(
                        ((IGraphicalPart) target).getFigure(), tooltip,
                        absolute.x, absolute.y);
                return;
            }
        }
        hideToolTip();
    }

    protected ToolTipHelper getToolTipHelper() {
        if (toolTipHelper == null) {
            if (currentMouseEvent != null) {
                IPart target = currentMouseEvent.target;
                if (target != null && target instanceof GraphicalEditPart) {
                    String actionId = ((GraphicalEditPart) target)
                            .getActionId();
                    if (actionId != null && (actionId
                            .equals("org.xmind.ui.editNotes") //$NON-NLS-1$
                            || actionId.equals("org.xmind.ui.editComments"))) { //$NON-NLS-1$
                        toolTipHelper = new KeepVisibleToolTipHelper(control);
                    } else {
                        toolTipHelper = new ToolTipHelper(control);
                    }
                }
            }
        } else {
            if (currentMouseEvent != null) {
                IPart target = currentMouseEvent.target;
                if (target != null && target instanceof GraphicalEditPart) {
                    String actionId = ((GraphicalEditPart) target)
                            .getActionId();
                    if (actionId != null && (actionId
                            .equals("org.xmind.ui.editNotes") //$NON-NLS-1$
                            || actionId.equals("org.xmind.ui.editComments"))) { //$NON-NLS-1$
                        if (!(toolTipHelper instanceof KeepVisibleToolTipHelper)) {
                            toolTipHelper.dispose();
                            toolTipHelper = new KeepVisibleToolTipHelper(
                                    control);
                        }
                    } else {
                        if (toolTipHelper instanceof KeepVisibleToolTipHelper) {
                            toolTipHelper.dispose();
                            toolTipHelper = new ToolTipHelper(control);
                        }
                    }
                }
            }
        }

        if (toolTipHelper == null) {
            toolTipHelper = new ToolTipHelper(control);
        }

        return toolTipHelper;
    }

    /**
     * @param me
     */
    protected void updateToolTip(MouseEvent me) {
        updateToolTip(me, true);
    }

    protected boolean isValidGraphicalPart(IPart host) {
        return host != null && host instanceof IGraphicalPart
                && host.getStatus().isActive();
    }

    protected void updateFocus() {
        IFigure focusableFigure = null;
        IPart focusedPart = getViewer().getFocusedPart();
        if (focusedPart instanceof IGraphicalPart) {
            IFigure fig = ((IGraphicalPart) focusedPart).getFigure();
            if (fig.isRequestFocusEnabled()) {
                focusableFigure = fig;
            }
        }
        setFocus(focusableFigure);
    }

    /**
     * @see org.eclipse.draw2d.SWTEventDispatcher#dispatchMouseMoved(org.eclipse.swt.events.MouseEvent)
     */
    @Override
    public void dispatchMouseMoved(org.eclipse.swt.events.MouseEvent me) {
        if (!isActive())
            return;

        /*
         * IMPORTANT: IF brainy is running on JDK 1.6beta, the sentence below
         * will fix a huge bug.
         */
        if (me.x > 0x8fff) {
            me.x -= 65536;
        }
        /* IMPORTANT: end IF. */

//        super.dispatchMouseMoved(me);

        mouseHovering = false;
        cancelToolTipShowing();

        receive(me);

        ITool tool = getActiveTool();
        if (tool != null) {
            MouseEvent e = currentMouseEvent;
            if (tool instanceof IGraphicalTool) {
                ((IGraphicalTool) tool).setCursorPosition(e.cursorLocation);
            }
            if ((me.stateMask & SWT.BUTTON_MASK) != 0 && lastDragEvent != null
                    && !ignoreDragging) {
                lastDragEvent = convertDrag(me, e);
                if (!isDraging) {
                    isDraging = isDragDetected(lastDragEvent,
                            getViewer().getProperties().getInteger(
                                    IGraphicalViewer.VIEWER_START_DRAG_THRESHOLD,
                                    DEFAULT_START_DRAG_THRESHOLD));
                }
                if (isDraging) {
                    tool.mouseDrag(lastDragEvent, getViewer());
                }
            } else if ((me.stateMask & SWT.BUTTON_MASK) == 0) {
                tool.mouseMove(e, getViewer());
            }
            updateCursor(e.cursorLocation, e.target);
            if (getToolTipHelper().isShowing())
                updateToolTip(e);
        } else {
            super.dispatchMouseMoved(me);
        }
        updateFocus();
    }

    private boolean isDragDetected(MouseDragEvent me, int threshold) {
        int deltaX = Math.abs(me.cursorLocation.x - me.startingLocation.x);
        int deltaY = Math.abs(me.cursorLocation.y - me.startingLocation.y);
        return deltaX >= threshold || deltaY >= threshold;
    }

    private void receive(org.eclipse.swt.events.MouseEvent me) {
        readStateMask(me);
        MouseEvent current = convertMouse(me);
        ITool tool;
        if (currentMouseEvent == null
                || current.target != currentMouseEvent.target) {
            if (currentMouseEvent != null) {
                tool = getActiveTool();
                if (tool != null)
                    tool.mouseExited(currentMouseEvent, getViewer());
                hideToolTip();
            }
            if (current.target != null) {
                tool = getActiveTool();
                if (tool != null)
                    tool.mouseEntered(current, getViewer());
            }
        }
        currentMouseEvent = current;
    }

    protected void readStateMask(org.eclipse.swt.events.MouseEvent me) {
        ITool tool = getActiveTool();
        if (tool != null && tool instanceof AbstractTool) {
            IStatusMachine statusMachine = ((AbstractTool) tool).getStatus();
            int stateMask = me.stateMask;
            statusMachine.setStatus(GEF.ST_CONTROL_PRESSED,
                    (stateMask & SWT.MOD1) != 0);
            statusMachine.setStatus(GEF.ST_SHIFT_PRESSED,
                    (stateMask & SWT.MOD2) != 0);
            statusMachine.setStatus(GEF.ST_ALT_PRESSED,
                    (stateMask & SWT.MOD3) != 0);
        }
    }

    /**
     * 
     */
    public void hideToolTip() {
        if (control != null && !control.isDisposed()) {
            getToolTipHelper().updateToolTip(null, null, 0, 0);
        }
    }

    /**
     * @param pos
     * @param host
     */
    protected void updateCursor(Point pos, IPart host) {
        if (!isActive())
            return;

        Cursor currentCursor = null;
        ITool tool = getActiveTool();
        if (tool instanceof IGraphicalTool) {
            currentCursor = ((IGraphicalTool) tool).getCurrentCursor(pos, host);
        }
        if (currentCursor == null && host != null
                && host instanceof IGraphicalEditPart
                && host.getStatus().isActive()) {
            currentCursor = ((IGraphicalEditPart) host).getCursor(pos);
        }
        setCursor(currentCursor);
    }

    protected int getLongPressingActivationTime() {
        return LONG_PRESSING_ACTIVATION_TIME;
    }

    /**
     * @see org.eclipse.draw2d.SWTEventDispatcher#dispatchMousePressed(org.eclipse.swt.events.MouseEvent)
     */
    @Override
    public void dispatchMousePressed(org.eclipse.swt.events.MouseEvent me) {
        isDraging = false;
        pressedMouseButton = me.button;
        if (!isActive())
            return;

//        super.dispatchMousePressed(me);
        cancelToolTipShowing();
        lastDragEvent = createDragEvent(me);
        receive(me);
        ITool tool = getActiveTool();
        if (tool != null) {
            final MouseEvent e = currentMouseEvent;
            if (tool instanceof IGraphicalTool) {
                ((IGraphicalTool) tool).setCursorPosition(e.cursorLocation);
            }
            tool.mouseDown(e, getViewer());
            if (e.isConsumed()) {
                ignoreNextMouseUp = true;
            }
            updateCursor(e.cursorLocation, e.target);
            if (getToolTipHelper().isShowing())
                updateToolTip(e);
            ignoreLongPressing = false;
            setIgnoreDoubleClicking(e.isConsumed());

            Display.getCurrent().timerExec(getLongPressingActivationTime(),
                    new Runnable() {
                        public void run() {
                            dispatchMouseLongPressed(e);
                        }
                    });
        } else {
            super.dispatchMousePressed(me);
            setIgnoreDoubleClicking(getCurrentEvent() != null
                    && getCurrentEvent().isConsumed());
        }
        updateFocus();
    }

    /**
     * @return the mousePressed
     */
    public boolean isMousePressed() {
        return pressedMouseButton != 0;
    }

    public int getPressedMouseButton() {
        return pressedMouseButton;
    }

    public boolean ignoresDoubleClicking() {
        return ignoreDoubleClicking;
    }

    public void setIgnoreDoubleClicking(boolean ignore) {
        this.ignoreDoubleClicking = ignore;
    }

    public void cancelLongPressing() {
        this.ignoreLongPressing = true;
    }

    public boolean ignoresLongPressing() {
        return ignoreLongPressing;
    }

    /**
     * @param mouseEvent
     */
    public void dispatchMouseLongPressed(MouseEvent me) {
        if (!isActive())
            return;
        if (!ignoresLongPressing() && me == currentMouseEvent && me != null
                && !me.isConsumed()) {
            ITool tool = getActiveTool();
            if (tool != null) {
                if (tool instanceof IGraphicalTool) {
                    ((IGraphicalTool) tool)
                            .setCursorPosition(me.cursorLocation);
                }
                tool.mouseLongPressed(me, getViewer());
                updateCursor(me.cursorLocation, me.target);
            }
        }
    }

    /**
     * @see org.eclipse.draw2d.SWTEventDispatcher#dispatchMouseReleased(org.eclipse.swt.events.MouseEvent)
     */
    @Override
    public void dispatchMouseReleased(org.eclipse.swt.events.MouseEvent me) {
        if (ignoreNextMouseUp) {
            ignoreNextMouseUp = false;
            return;
        }
        pressedMouseButton = 0;
        ignoreDragging = false;
        isDraging = false;
        if (!isActive())
            return;
//        super.dispatchMouseReleased(me);
        cancelToolTipShowing();
        lastDragEvent = null;
        receive(me);
        ITool tool = getActiveTool();
        if (tool != null) {
            MouseEvent e = currentMouseEvent;
            if (tool instanceof IGraphicalTool) {
                ((IGraphicalTool) tool).setCursorPosition(e.cursorLocation);
            }
            tool.mouseUp(e, getViewer());
            updateCursor(e.cursorLocation, e.target);
        } else {
            super.dispatchMouseReleased(me);
        }
        updateFocus();
    }

    /**
     * @see org.eclipse.draw2d.EventDispatcher#dispatchMouseWheelScrolled(org.eclipse.swt.widgets.Event)
     */
    @Override
    public void dispatchMouseWheelScrolled(
            org.eclipse.swt.widgets.Event event) {
        if (!isActive())
            return;
//        super.dispatchMouseWheelScrolled(event);
        cancelToolTipShowing();
        ITool tool = getActiveTool();
        if (tool != null) {
            MouseEvent e = currentMouseEvent;
            if (e != null && tool instanceof IGraphicalTool) {
                ((IGraphicalTool) tool).setCursorPosition(e.cursorLocation);
            }
            MouseWheelEvent mwe = convertWheel(event);
            tool.mouseWheelScrolled(mwe, getViewer());
            if (e != null) {
                updateCursor(e.cursorLocation, e.target);
            }
            event.doit = mwe.doIt;
        } else {
            super.dispatchMouseWheelScrolled(event);
        }
    }

    protected DragDropEvent createDropEvent(DropTargetEvent e, boolean drop) {
        TransferData[] dataTypes;
        if (drop && e.currentDataType != null) {
            dataTypes = new TransferData[] { e.currentDataType };
        } else {
            dataTypes = e.dataTypes;
        }
        DndData dndData = getDndSupport().parseData(dataTypes, getDropTarget(),
                !drop);
        if (dndData == null)
            return null;

        Point p = new Point(control.toControl(e.x, e.y));
        Point location = convertPoint(p.x, p.y);
        IPart host = findPart(p.x, p.y);
        DragDropEvent event = DragDropEvent.createFrom(e, host, location);
        event.dndData = dndData;

        IDndClient client = getDndSupport().getDndClient(dndData.clientId);
        if (event.detail == DND.DROP_DEFAULT)
            event.detail = event.operations;
        if ((event.detail & DND.DROP_LINK) != 0 && !client
                .canLink(dndData.dataType, getViewer(), location, host)) {
            event.detail &= ~DND.DROP_LINK;
        }
        if ((event.detail & DND.DROP_MOVE) != 0 && !client
                .canMove(dndData.dataType, getViewer(), location, host)) {
            event.detail &= ~DND.DROP_MOVE;
        }
        if ((event.detail & DND.DROP_COPY) != 0 && !client
                .canCopy(dndData.dataType, getViewer(), location, host)) {
            event.detail &= ~DND.DROP_COPY;
        }
        return event;
    }

    protected void feedback(DragDropEvent de, DropTargetEvent swtEvent) {
        swtEvent.detail = de.detail;
        swtEvent.currentDataType = de.dndData.dataType;
        updateControl();
    }

    /**
     * 
     */
    private void updateControl() {
        LightweightSystem lws;
        if (control instanceof FigureCanvas) {
            lws = ((FigureCanvas) control).getLightweightSystem();
        } else if (getViewer() instanceof GraphicalViewer) {
            lws = ((GraphicalViewer) getViewer()).getLightweightSystem();
        } else if (getViewer().getCanvas() != null
                && !getViewer().getCanvas().isDisposed()) {
            lws = getViewer().getCanvas().getLightweightSystem();
        } else {
            return;
        }
        lws.getUpdateManager().performUpdate();
    }

    protected IDragDropHandler getDragDropHandler() {
        IDragDropHandler handler = null;
        ITool tool = getActiveTool();
        if (tool instanceof IDragDropHandler) {
            handler = (IDragDropHandler) tool;
        }
        return handler;
    }

    public void dragEnter(DropTargetEvent event) {
        if (!isActive())
            return;
        dropHandler = null;
        currentDropEvent = createDropEvent(event, false);
        if (currentDropEvent == null) {
            event.detail = DND.DROP_NONE;
            return;
        }

        IDragDropHandler handler = getDragDropHandler();
        if (handler != null) {
            if (handler instanceof IGraphicalTool) {
                ((IGraphicalTool) handler)
                        .setCursorPosition(currentDropEvent.location);
            }
            handler.dragStarted(currentDropEvent, getViewer());
            handler.dragEntered(currentDropEvent, getViewer());
            feedback(currentDropEvent, event);
        }
    }

    public void dragLeave(DropTargetEvent event) {
        if (!isActive())
            return;

        if (currentDropEvent == null) {
            event.detail = DND.DROP_NONE;
            return;
        }

        final IDragDropHandler handler = getDragDropHandler();
        if (handler != null) {
            if (handler instanceof IGraphicalTool) {
                ((IGraphicalTool) handler)
                        .setCursorPosition(currentDropEvent.location);
            }
            // The passed-in DropTargetEvent object's contents
            // may be invalid, such as position and detail, so we use
            // the last DragDropEvent object instead of creating
            // a new one, and thus achieve a correct effect that
            // the drag operation 'LEAVE' off that every last part
            // it ever stayed over.
            handler.dragExited(currentDropEvent, getViewer());
            handler.dragDismissed(currentDropEvent, getViewer());
            feedback(currentDropEvent, event);
            dropHandler = handler;
        }
    }

    public void dragOperationChanged(DropTargetEvent event) {
        if (!isActive())
            return;
        currentDropEvent = createDropEvent(event, false);
        if (currentDropEvent == null) {
            event.detail = DND.DROP_NONE;
            return;
        }

        IDragDropHandler handler = getDragDropHandler();
        if (handler != null) {
            if (handler instanceof IGraphicalTool) {
                ((IGraphicalTool) handler)
                        .setCursorPosition(currentDropEvent.location);
            }
            handler.dragOperationChanged(currentDropEvent, getViewer());
            feedback(currentDropEvent, event);
        }
    }

    public void dragOver(DropTargetEvent event) {
        if (!isActive())
            return;
        DragDropEvent prevDropEvent = currentDropEvent;
        currentDropEvent = createDropEvent(event, false);
        if (currentDropEvent == null) {
            event.detail = DND.DROP_NONE;
            return;
        }

        IDragDropHandler handler = getDragDropHandler();
        if (handler != null) {

            if (handler instanceof IGraphicalTool) {
                ((IGraphicalTool) handler)
                        .setCursorPosition(currentDropEvent.location);
            }

            // test if the cursor leaves a part and enters another
            if (prevDropEvent != null
                    && prevDropEvent.target != currentDropEvent.target) {
                handler.dragExited(prevDropEvent, getViewer());
                handler.dragEntered(currentDropEvent, getViewer());
            }

            handler.dragOver(currentDropEvent, getViewer());
            feedback(currentDropEvent, event);
        }
    }

    public void drop(DropTargetEvent event) {
        if (!isActive())
            return;
        currentDropEvent = createDropEvent(event, true);
        if (currentDropEvent == null) {
            event.detail = DND.DROP_NONE;
            return;
        }

        IDragDropHandler handler = dropHandler;
        if (handler == null)
            handler = getDragDropHandler();
        if (handler != null) {
            if (handler instanceof IGraphicalTool) {
                ((IGraphicalTool) handler)
                        .setCursorPosition(currentDropEvent.location);
            }
            handler.drop(currentDropEvent, getViewer());
            feedback(currentDropEvent, event);
            dropHandler = null;
        }
    }

    public void dropAccept(DropTargetEvent event) {
        if (!isActive())
            return;
        currentDropEvent = createDropEvent(event, false);
        if (currentDropEvent == null) {
            event.detail = DND.DROP_NONE;
            return;
        }

        IDragDropHandler handler = dropHandler;
        if (handler == null)
            handler = getDragDropHandler();
        if (handler != null) {
            if (handler instanceof IGraphicalTool) {
                ((IGraphicalTool) handler)
                        .setCursorPosition(currentDropEvent.location);
            }
            handler.dropAccept(currentDropEvent, getViewer());
            feedback(currentDropEvent, event);
        }
    }

}
