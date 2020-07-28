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
package org.xmind.gef.policy;

import java.util.List;

import org.eclipse.draw2d.FreeformFigure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.ZoomManager;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;

/**
 * @author Brian Sun
 */
public class ScalableEditPolicy extends AbstractEditPolicy {

    public boolean understands(String requestType) {
        return super.understands(requestType)
                || GEF.REQ_ZOOM.equals(requestType)
                || GEF.REQ_ZOOMIN.equals(requestType)
                || GEF.REQ_ZOOMOUT.equals(requestType)
                || GEF.REQ_ACTUALSIZE.equals(requestType)
                || GEF.REQ_FITSIZE.equals(requestType)
                || GEF.REQ_FITSELECTION.equals(requestType);
    }

    /**
     * @see org.xmind.gef.policy.Policy#handle(org.xmind.gef.Request)
     */
    public void handle(Request req) {
        String type = req.getType();
        if (GEF.REQ_ZOOM.equals(type)) {
            performZoomRequest(req);
        } else if (GEF.REQ_ZOOMIN.equals(type)) {
            performZoomIn(getGraphicalViewer(req));
        } else if (GEF.REQ_ZOOMOUT.equals(type)) {
            performZoomOut(getGraphicalViewer(req));
        } else if (GEF.REQ_ACTUALSIZE.equals(type)) {
            performActualSize(req);
        } else if (GEF.REQ_FITSIZE.equals(type)) {
            performFitSize(getGraphicalViewer(req));
        } else if (GEF.REQ_FITSELECTION.equals(type)) {
            performFitSelection(req);
        }
    }

    protected IGraphicalViewer getGraphicalViewer(Request req) {
        IViewer viewer = req.getTargetViewer();
        if (viewer != null && viewer instanceof IGraphicalViewer) {
            return (IGraphicalViewer) viewer;
        }
        return null;
    }

    protected void preserveCenter(final Runnable action,
            IGraphicalViewer viewer) {
        PrecisionPoint center = viewer == null ? null
                : new PrecisionPoint(viewer.getCenterPoint());
        double oldScale = viewer.getZoomManager().getScale();

        try {
            action.run();
        } finally {
            if (viewer != null && center != null) {
                if (viewer.getZoomManager() != null)
                    center.scale(
                            2 * viewer.getZoomManager().getScale() / oldScale);
                if (viewer.getZoomManager().getScale() != oldScale)
                    viewer.center(center.toDraw2DPoint());
            }
        }
    }

    /**
     * 
     */
    protected void performZoomOut(IGraphicalViewer viewer) {
        if (viewer == null)
            return;

        final ZoomManager zoomManager = viewer.getZoomManager();
        preserveCenter(new Runnable() {
            public void run() {
                zoomManager.zoomOut();
            }
        }, viewer);
    }

    /**
     * 
     */
    protected void performZoomIn(final IGraphicalViewer viewer) {
        if (viewer == null)
            return;

        preserveCenter(new Runnable() {
            public void run() {
                viewer.getZoomManager().zoomIn();
            }
        }, viewer);
    }

    /**
     * 
     */
    protected void performActualSize(Request req) {
        final IGraphicalViewer viewer = getGraphicalViewer(req);
        if (viewer == null)
            return;

        List<IPart> selectedParts = req.getTargets();
        if (selectedParts.isEmpty()) {
            selectedParts = viewer.getSelectionSupport().getPartSelection();
        }
        if (selectedParts.isEmpty())
            return;

        final Rectangle bounds = getSelectionBounds(viewer, selectedParts);
        if (bounds != null) {
            IFigure viewport = ((IGraphicalViewer) viewer).getCanvas()
                    .getViewport();
            viewer.getZoomManager().actualSize();
            bounds.getCenter().scale(viewer.getZoomManager().getScale());
            viewport.getUpdateManager().runWithUpdate(new Runnable() {
                public void run() {
                    viewer.center(bounds.getCopy()
                            .scale(viewer.getZoomManager().getScale() * 2));
                }
            });
        }
    }

    /**
     * @param request
     */
    protected void performZoomRequest(Request request) {
        Object param = request.getParameter(GEF.PARAM_ZOOM_SCALE);
        if (param == null || !(param instanceof Double))
            return;

        final double scale = ((Double) param).doubleValue();
        final IGraphicalViewer viewer = getGraphicalViewer(request);
        if (viewer == null)
            return;

        preserveCenter(new Runnable() {
            public void run() {
                viewer.getZoomManager().setScale(scale);
            }
        }, viewer);
    }

    protected void performFitSize(final IGraphicalViewer viewer) {
        if (viewer == null)
            return;

        final Rectangle bounds = getContentsBounds(viewer);
        if (bounds != null) {
            fitBounds(viewer, bounds, viewer.getZoomManager());
        }
    }

    protected void fitBounds(final IGraphicalViewer viewer,
            final Rectangle bounds, final ZoomManager zoomManager) {
        IFigure viewport = ((IGraphicalViewer) viewer).getCanvas()
                .getViewport();
        Dimension viewportSize = getViewportSize(viewer, viewport, zoomManager);
        zoomManager.fitScale(viewportSize, bounds.getSize());
        bounds.getCenter().scale(zoomManager.getScale());
        viewport.getUpdateManager().runWithUpdate(new Runnable() {
            public void run() {
                viewer.center(
                        bounds.getCopy().scale(zoomManager.getScale() * 2));
            }
        });
    }

    protected Dimension getViewportSize(IGraphicalViewer viewer,
            IFigure viewport, ZoomManager zoomManager) {
        return viewport.getSize();
    }

    protected void ensureVisible(IGraphicalViewer viewer, Rectangle bounds) {
        viewer.ensureVisible(bounds);
    }

    protected Rectangle getContentsBounds(IGraphicalViewer viewer) {
        return getBounds(viewer.getRootPart().getContents());
    }

    protected Rectangle getBounds(IPart p) {
        if (p != null && p.getStatus().isActive()
                && p instanceof IGraphicalPart) {
            IFigure figure = ((IGraphicalPart) p).getFigure();
            if (figure != null) {
                if (figure instanceof FreeformFigure)
                    return ((FreeformFigure) figure).getFreeformExtent();
                return figure.getBounds();
            }
        }
        return null;
    }

    protected void performFitSelection(Request req) {
        final IGraphicalViewer viewer = getGraphicalViewer(req);
        if (viewer == null)
            return;

        List<IPart> selectedParts = req.getTargets();
        if (selectedParts.isEmpty()) {
            selectedParts = viewer.getSelectionSupport().getPartSelection();
        }
        if (selectedParts.isEmpty())
            return;

        final Rectangle bounds = getSelectionBounds(viewer, selectedParts);
        if (bounds != null) {
            fitBounds(viewer, bounds, viewer.getZoomManager());
        }
    }

    protected Rectangle getSelectionBounds(IGraphicalViewer viewer,
            List<? extends IPart> parts) {
        Rectangle r = null;
        for (IPart p : parts) {
            r = Geometry.union(r, getBounds(p));
        }
        return r;
    }

}
