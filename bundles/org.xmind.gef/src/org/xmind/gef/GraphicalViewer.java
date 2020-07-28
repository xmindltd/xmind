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
package org.xmind.gef;

import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.RangeModel;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.GestureEvent;
import org.eclipse.swt.events.GestureListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.xmind.gef.dnd.IDndSupport;
import org.xmind.gef.event.PartsEventDispatcher;
import org.xmind.gef.event.ViewerEventDispatcher;
import org.xmind.gef.part.IGraphicalEditPart;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IGraphicalRootPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IRootPart;
import org.xmind.gef.tool.ITool;
import org.xmind.gef.tool.SelectTool;

/**
 * @author Brian Sun
 */
public class GraphicalViewer extends AbstractViewer
        implements IGraphicalViewer {

    private static final Point POINT = new Point();

    protected class GraphicalSelectionSupport extends SelectionSupport {

        public boolean isSelectable(IPart p) {
            boolean selectable = super.isSelectable(p);
            if (selectable) {
                if (p instanceof IGraphicalPart) {
                    IFigure fig = ((IGraphicalPart) p).getFigure();
                    if (fig == null || !fig.isShowing())
                        return false;
                }
            }
            return selectable;
        }

        protected void partSelectionChanged(List<? extends IPart> parts,
                boolean reveal) {
            super.partSelectionChanged(parts, reveal);
            if (getEditDomain() != null) {
                ITool tool = getEditDomain().getTool(GEF.TOOL_SELECT);
                if (tool != null && tool instanceof SelectTool) {
                    ((SelectTool) tool).resetSeqSelectStart();
                }
                IPart focused = findSelectablePart(getFocused());
                if (focused == null || !focused.getStatus().isActive()
                        || !focused.getStatus().isSelected()) {
                    setFocused(findSelectedPart(parts));
                }
            }
        }

        protected IPart findSelectedPart(List<? extends IPart> parts) {
            for (IPart p : parts) {
                if (p.getStatus().isActive() && p.getStatus().isSelected())
                    return p;
            }
            return null;
        }
    }

    private LightweightSystem lws = createLightweightSystem();

    private ILayerManager layerManager = null;

    private ViewerEventDispatcher eventDispatcher = createEventDispatcher();

    private ZoomManager zoomManager = null;

    private Viewport viewport = new Viewport(true);

    public GraphicalViewer() {
        super();
        lws.setEventDispatcher(eventDispatcher);
        lws.setContents(viewport);
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (LightweightSystem.class.equals(adapter))
            return adapter.cast(getLightweightSystem());
        if (ILayerManager.class.equals(adapter))
            return adapter.cast(getLayerManager());
        if (FigureCanvas.class.equals(adapter))
            return adapter.cast(getCanvas());
        if (ZoomManager.class.equals(adapter))
            return adapter.cast(getZoomManager());
        if (IGraphicalPart.class.equals(adapter))
            return adapter.cast(getRootPart() instanceof IGraphicalPart
                    ? getRootPart() : null);
        if (IFigure.class.equals(adapter))
            return adapter.cast(getRootFigure());
        if (Viewport.class.equals(adapter))
            return adapter.cast(getViewport());
        return super.getAdapter(adapter);
    }

    public IGraphicalPart findGraphicalPart(Object model) {
        IPart part = findPart(model);
        if (part instanceof IGraphicalPart)
            return (IGraphicalPart) part;
        return null;
    }

    public IPart findPart(int x, int y) {
        return findPart(convertPoint(x, y));
    }

    protected Point convertPoint(int controlX, int controlY) {
        return computeToLayer(POINT.setLocation(controlX, controlY), true);
    }

    protected IPart findPart(Point position) {
        return findPart(getRootPart(), position);
    }

    protected IPart findPart(IPart parent, Point position) {
        if (parent == null || !(parent instanceof IGraphicalEditPart))
            return null;
        return (((IGraphicalEditPart) parent).findAt(position,
                getPartSearchCondition()));
    }

    protected LightweightSystem createLightweightSystem() {
        return new LightweightSystem();
    }

    public LightweightSystem getLightweightSystem() {
        return lws;
    }

    protected Viewport getViewport() {
        return viewport;
    }

    public Control createControl(Composite parent) {
        return createControl(parent, SWT.DOUBLE_BUFFERED);
    }

    protected Control internalCreateControl(Composite parent, int style) {
        //FIXME
        FigureCanvas canvas = new FigureCanvas(parent, style,
                getLightweightSystem()) {

            @Override
            public org.eclipse.swt.graphics.Rectangle computeTrim(int x, int y,
                    int width, int height) {
                org.eclipse.swt.graphics.Rectangle trim = super.computeTrim(x,
                        y, width, height);
                if (!getVerticalBar().isVisible()) {
                    trim.width = 0;
                }
                if (!getHorizontalBar().isVisible()) {
                    trim.height = 0;
                }

                return trim;
            }
        };

        addGestureZoomSupport(canvas);
        addHorizontalScrollSupport(canvas);
        canvas.setViewport(viewport);
        return canvas;
    }

    private void addGestureZoomSupport(FigureCanvas canvas) {
        final int FREQUENCY = 10;
        final int[] eventCounts = new int[] { 8, 8 };
        canvas.addGestureListener(new GestureListener() {

            public void gesture(GestureEvent e) {
                if (e.magnification > 1) {
                    if (eventCounts[0] >= FREQUENCY) {
                        eventCounts[0] = 0;
                        sendRequest(GEF.REQ_ZOOMIN);
                    } else {
                        eventCounts[0]++;
                    }
                    eventCounts[1] = 8;
                } else if (e.magnification > 0 && e.magnification < 1) {
                    if (eventCounts[1] >= FREQUENCY) {
                        eventCounts[1] = 0;
                        sendRequest(GEF.REQ_ZOOMOUT);
                    } else {
                        eventCounts[1]++;
                    }
                    eventCounts[0] = 8;
                }
            }
        });
    }

    private void sendRequest(final String reqType) {
        if (getEditDomain() != null) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    getEditDomain().handleRequest(reqType,
                            GraphicalViewer.this);
                }
            });
        }
    }

    // add horizontal scroll support for windows
    private void addHorizontalScrollSupport(final FigureCanvas canvas) {
        if (Util.isWindows()) {
            canvas.addListener(SWT.MouseHorizontalWheel, new Listener() {

                public void handleEvent(Event event) {
                    if (!canvas.isDisposed()) {
                        int offset = -event.count;
                        ScrollBar horizontalBar = getCanvas()
                                .getHorizontalBar();

                        if (horizontalBar != null) {
                            horizontalBar.setSelection(
                                    horizontalBar.getSelection() + offset
                                            * horizontalBar.getIncrement());

                            RangeModel model = getViewport()
                                    .getHorizontalRangeModel();
                            int hBarOffset = Math.max(0, -model.getMinimum());
                            getCanvas().scrollToX(
                                    horizontalBar.getSelection() - hBarOffset);
                        }
                    }
                }
            });
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.gef.AbstractViewer#hookControl(org.eclipse.swt.widgets.Control)
     */
    @Override
    protected void hookControl(Control control) {
        if (getEditDomain() != null) {
            eventDispatcher.activate();
        }
        super.hookControl(control);
    }

    public void setRootPart(IRootPart rootPart) {
        super.setRootPart(rootPart);
        IFigure rootFigure = getRootFigure();
        if (rootFigure instanceof Viewport) {
            viewport = (Viewport) rootFigure;
        } else {
            viewport = new Viewport(true);
            viewport.setContents(rootFigure);
        }
        getLightweightSystem().setContents(viewport);
        if (getCanvas() != null && !getCanvas().isDisposed()) {
            getCanvas().setViewport(viewport);
        }
    }

    protected IFigure getRootFigure() {
        if (getRootPart() instanceof IGraphicalPart) {
            return ((IGraphicalPart) getRootPart()).getFigure();
        }
        return null;
    }

    protected void revalidateContents() {
        getCanvas().layout(true);
    }

    protected ViewerEventDispatcher createEventDispatcher() {
        return new PartsEventDispatcher(this);
    }

//    public ViewerEventDispatcher getEventDispatcher() {
//        return eventDispatcher;
//    }

    public void setEditDomain(EditDomain editDomain) {
        if (getControl() != null && !getControl().isDisposed()) {
            eventDispatcher.deactivate();
        }
        super.setEditDomain(editDomain);
        if (getEditDomain() != null && getControl() != null
                && !getControl().isDisposed()) {
            eventDispatcher.activate();
        }
    }

    public Layer getLayer(Object key) {
        return layerManager == null ? null : layerManager.getLayer(key);
    }

    public ILayerManager getLayerManager() {
        return layerManager;
    }

    public void setLayerManager(ILayerManager layerManager) {
        this.layerManager = layerManager;
    }

    public void updateToolTip() {
        if (getControl() != null && !getControl().isDisposed()) {
            eventDispatcher.updateToolTip();
        }
    }

    public void hideToolTip() {
        if (getControl() != null)
            getControl().setToolTipText(null);
    }

    public FigureCanvas getCanvas() {
        return (FigureCanvas) super.getControl();
    }

    public Dimension getSize() {
        return new Dimension(getCanvas().getSize());
    }

    public Rectangle getClientArea() {
        Rectangle area = new Rectangle(getCanvas().getClientArea());
        area.setLocation(getScrollPosition());
        return area;
    }

    public Point getCenterPoint() {
        return getViewport().getClientArea().getCenter();
    }

    public void center(Rectangle area) {
        center(area.getCenter());
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.gef.IGraphicalViewer#center(int, int)
     */
    public void center(int x, int y) {
        Rectangle clientArea = getViewport().getClientArea();
        scrollTo(x - clientArea.width >> 1, y - clientArea.height >> 1);
    }

    public void center(Point center) {
        center(center.x, center.y);
    }

    public void scrollToX(int x) {
        if (usesSmoothScroll()) {
            getViewport().setHorizontalLocation(x);
        } else {
            getCanvas().scrollToX(x);
        }
    }

    public void scrollToY(int y) {
        if (usesSmoothScroll()) {
            getViewport().setVerticalLocation(y);
        } else {
            getCanvas().scrollToY(y);
        }
    }

    public void scrollTo(Point p) {
        scrollTo(p.x, p.y);
    }

    public void scrollTo(int x, int y) {
        if (usesSmoothScroll()) {
            getViewport().setViewLocation(x, y);
        } else {
            getCanvas().scrollTo(x, y);
        }
    }

    protected boolean usesSmoothScroll() {
        return Boolean.TRUE.equals(getProperties().get(VIEWER_SCROLL_SMOOTH));
    }

    /**
     * @see org.xmind.gef.IViewer#scrollDelta(org.eclipse.draw2d.geometry.Dimension)
     */
    public void scrollDelta(Dimension d) {
        scrollTo(getScrollPosition().translate(d));
    }

    /**
     * @see org.xmind.gef.IGraphicalViewer#scrollDelta(int, int)
     */
    public void scrollDelta(int dx, int dy) {
        scrollTo(getScrollPosition().translate(dx, dy));
    }

    /**
     * @see org.xmind.gef.IViewer#getScrollPosition()
     */
    public Point getScrollPosition() {
        return getViewport().getViewLocation();
    }

    /**
     * @see org.xmind.gef.IGraphicalViewer#computeToLayer(org.eclipse.draw2d.geometry.Point,
     *      boolean)
     */
    public Point computeToLayer(Point controlPoint, boolean zoomed) {
        Point p = getScrollPosition();
        p.translate(controlPoint);
        if (zoomed) {
            return p.scale(1 / getZoomManager().getScale());
        }
        return p;
    }

    /**
     * @see org.xmind.gef.IGraphicalViewer#computeToControl(org.eclipse.draw2d.geometry.Point,
     *      boolean)
     */
    public Point computeToControl(Point layerPoint, boolean zoomed) {
        POINT.setLocation(layerPoint);
        if (zoomed) {
            POINT.scale(getZoomManager().getScale());
        }
        return getScrollPosition().negate().translate(POINT);
    }

    public Point computeToDisplay(Point layerPoint, boolean zoomed) {
        Point p = computeToControl(layerPoint, zoomed);
        org.eclipse.swt.graphics.Point loc = getControl().toDisplay(p.x, p.y);
        return p.setLocation(loc.x, loc.y);
    }

    public void ensureVisible(Rectangle box) {
        box = getZoomManager().getScaled(box);
        Rectangle clientArea = getClientArea();
        if (clientArea.contains(box) || box.contains(clientArea))
            return;
        ensureVisible(box, clientArea, 0);
    }

    public void ensureControlVisible(Rectangle box) {
        Rectangle clientArea = new Rectangle(getCanvas().getClientArea());
        if (clientArea.contains(box) || box.contains(clientArea))
            return;
        ensureVisible(box, clientArea, 0);
    }

    /**
     * @param box
     * @param clientArea
     * @param margin
     */
    protected void ensureVisible(Rectangle box, Rectangle clientArea,
            int margin) {
        int dx = 0;
        int dy = 0;
        if (box.width > clientArea.width)
            dx = box.getCenter().x - clientArea.getCenter().x;
        else if (box.x < clientArea.x)
            dx = box.x - clientArea.x - margin;
        else if (box.right() > clientArea.right())
            dx = box.right() - clientArea.right() + margin;
        if (box.height > clientArea.height)
            dy = box.getCenter().y - clientArea.getCenter().y;
        else if (box.y < clientArea.y)
            dy = box.y - clientArea.y - margin;
        else if (box.bottom() > clientArea.bottom())
            dy = box.bottom() - clientArea.bottom() + margin;
        smoothScrollDelta(dx, dy);
    }

    /**
     * @param dx
     * @param dy
     */
    protected void smoothScrollDelta(int dx, int dy) {
        scrollDelta(dx, dy);
    }

    protected IGraphicalRootPart getGraphicalRootEditPart() {
        IRootPart rootPart = getRootPart();
        if (rootPart instanceof IGraphicalRootPart) {
            return (IGraphicalRootPart) rootPart;
        }
        return null;
    }

    protected ISelectionSupport createSelectionSupport() {
        return new GraphicalSelectionSupport();
    }

    protected void inputChanged(Object input, Object oldInput) {
        boolean controlAvailable = getControl() != null
                && !getControl().isDisposed();
        if (controlAvailable) {
            getControl().setRedraw(false);
        }

        ISelection oldSelection = getSelection();
        setSelectionOnInputChanged(StructuredSelection.EMPTY);
        double oldScale = getZoomManager().getScale();
        if (controlAvailable && getEditDomain() != null) {
            eventDispatcher.deactivate();
        }
        internalInputChanged(input, oldInput);
        if (controlAvailable && getEditDomain() != null) {
            eventDispatcher.activate();
        }
        setSelectionOnInputChanged(oldSelection);
        if (oldScale >= 0) {
            getZoomManager().setScale(oldScale);
        }
        if (controlAvailable) {
            getControl().setRedraw(true);
        }
    }

    protected void setSelectionOnInputChanged(ISelection selection) {
        setSelection(selection);
    }

    protected void internalInputChanged(Object input, Object oldInput) {
        super.inputChanged(input, oldInput);
    }

//    /**
//     * @param x
//     * @param y
//     */
//    protected void alternativeScrollTo(int x, int y) {
//        int hOffset = verifyScrollBarOffset(getViewport()
//                .getHorizontalRangeModel(), x);
//        int vOffset = verifyScrollBarOffset(getViewport()
//                .getVerticalRangeModel(), y);
//
//        int hOffsetOld = getViewport().getViewLocation().x;
//        if (hOffset == hOffsetOld) {
//            scrollToY(y);
//            return;
//        }
//        int dx = -hOffset + hOffsetOld;
//
//        int vOffsetOld = getViewport().getViewLocation().y;
//        if (vOffset == vOffsetOld) {
//            scrollToX(x);
//            return;
//        }
//        int dy = -vOffset + vOffsetOld;
//
//        Rectangle clientArea = getViewport().getBounds()
//                .getCropped(getViewport().getInsets());
//        Rectangle blit = clientArea.getResized(-Math.abs(dx), -Math.abs(dy));
//        Rectangle expose = clientArea.getCopy();
//        Rectangle expose2 = clientArea.getCopy();
//        Point dest = clientArea.getTopLeft();
//        expose.width = Math.abs(dx);
//        if (dx < 0) { //Moving left?
//            blit.translate(-dx, 0); //Move blit area to the right
//            expose.x = dest.x + blit.width;
//        } else
//            //Moving right
//            dest.x += dx; //Move expose area to the right
//
//        expose2.height = Math.abs(dy);
//        if (dy < 0) { //Moving up?
//            blit.translate(0, -dy); //Move blit area down
//            expose2.y = dest.y + blit.height; //Move expose area down
//        } else
//            //Moving down
//            dest.y += dy;
//
//        // fix for bug 41111
//        Control[] children = getCanvas().getChildren();
//        boolean[] manualMove = new boolean[children.length];
//        for (int i = 0; i < children.length; i++) {
//            org.eclipse.swt.graphics.Rectangle bounds = children[i].getBounds();
//            manualMove[i] = blit.width <= 0 || blit.height < 0
//                    || bounds.x > blit.x + blit.width
//                    || bounds.y > blit.y + blit.height
//                    || bounds.x + bounds.width < blit.x
//                    || bounds.y + bounds.height < blit.y;
//        }
//        getCanvas().scroll(dest.x, dest.y, blit.x, blit.y, blit.width,
//                blit.height, true);
//
//        for (int i = 0; i < children.length; i++) {
//            org.eclipse.swt.graphics.Rectangle bounds = children[i].getBounds();
//            if (manualMove[i])
//                children[i].setBounds(bounds.x + dx, bounds.y, bounds.width,
//                        bounds.height);
//        }
//
//        getViewport().setIgnoreScroll(true);
//        getViewport().setHorizontalLocation(hOffset);
//        getViewport().setVerticalLocation(vOffset);
//        getViewport().setIgnoreScroll(false);
//        getCanvas().redraw(expose.x, expose.y, expose.width, expose.height,
//                true);
//        getCanvas().redraw(expose2.x, expose2.y, expose2.width, expose2.height,
//                true);
//    }
//
//    private int verifyScrollBarOffset(RangeModel model, int value) {
//        value = Math.max(model.getMinimum(), value);
//        return Math.min(model.getMaximum() - model.getExtent(), value);
//    }

    public ZoomManager getZoomManager() {
        if (zoomManager == null)
            zoomManager = new ZoomManager();
        return zoomManager;
    }

    public void setZoomManager(ZoomManager zoomManager) {
        this.zoomManager = zoomManager;
    }

    public void setDndSupport(IDndSupport dndSupport) {
        super.setDndSupport(dndSupport);
        eventDispatcher.setDndSupport(dndSupport);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.gef.AbstractViewer#setCursor(org.eclipse.swt.graphics.Cursor)
     */
    @Override
    public void setCursor(Cursor cursor) {
        eventDispatcher.setOverridingCursor(cursor);
    }

}
