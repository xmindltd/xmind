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

package org.xmind.gef.service;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.FreeformFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;

/**
 * @author Frank Shaka
 * 
 */
public class CenterPreservationService extends GraphicalViewerService implements
        Listener, PropertyChangeListener {

    protected static final Rectangle TEMP_AREA = new Rectangle();

    private boolean centerOnContents;

    private Display display = null;

    protected Viewport viewport = null;

    protected Point centerPoint = new Point();

    private boolean mousePressing = false;

    private boolean keyPressing = false;

    private boolean resizedDuringMousePress = false;

    private boolean resizedDuringKeyPress = false;

    /**
     * 
     */
    public CenterPreservationService(IGraphicalViewer viewer) {
        this(viewer, false);
    }

    /**
     * Constructs a new center preservation service. The parameter
     * <code>centerOnContents</code> enables/disables the ability to take the
     * center of the contents layer as the new center point.
     * 
     * @param viewer
     * @param centerOnContents
     */
    public CenterPreservationService(IGraphicalViewer viewer,
            boolean centerOnContents) {
        super(viewer);
        this.centerOnContents = centerOnContents;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.service.AbstractViewerService#hookControl(org.eclipse.swt
     * .widgets.Control)
     */
    @Override
    protected void hookControl(Control control) {
        super.hookControl(control);
        FigureCanvas canvas = (FigureCanvas) control;
        if (canvas != null && !canvas.isDisposed()) {
            canvas.addListener(SWT.Resize, this);
            canvas.addListener(SWT.FocusIn, this);
            canvas.addListener(SWT.FocusOut, this);
            canvas.addListener(SWT.Paint, this);

            viewport = canvas.getViewport();
            if (viewport != null) {
                viewport.addPropertyChangeListener(
                        Viewport.PROPERTY_VIEW_LOCATION, this);
            }
            display = canvas.getDisplay();
            if (display != null) {
                display.addFilter(SWT.MouseDown, this);
                display.addFilter(SWT.MouseUp, this);
                display.addFilter(SWT.KeyDown, this);
                display.addFilter(SWT.KeyUp, this);
            }
        }
        mousePressing = false;
        keyPressing = false;
        resizedDuringMousePress = false;
        resizedDuringKeyPress = false;
        updateCenterPoint();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.service.AbstractViewerService#unhookControl(org.eclipse
     * .swt.widgets.Control)
     */
    @Override
    protected void unhookControl(Control control) {
        FigureCanvas canvas = (FigureCanvas) control;
        if (canvas != null && !canvas.isDisposed()) {
            canvas.removeListener(SWT.Resize, this);
            canvas.removeListener(SWT.FocusIn, this);
            canvas.removeListener(SWT.Paint, this);
        }

        if (viewport != null) {
            viewport.removePropertyChangeListener(
                    Viewport.PROPERTY_VIEW_LOCATION, this);
        }
        if (display != null && !display.isDisposed()) {
            display.removeFilter(SWT.MouseDown, this);
            display.removeFilter(SWT.MouseUp, this);
            display.removeFilter(SWT.KeyDown, this);
            display.removeFilter(SWT.KeyUp, this);
        }
        mousePressing = false;
        keyPressing = false;
        resizedDuringMousePress = false;
        resizedDuringKeyPress = false;
        super.unhookControl(control);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.service.AbstractViewerService#activate()
     */
    @Override
    protected void activate() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.service.AbstractViewerService#deactivate()
     */
    @Override
    protected void deactivate() {
    }

    /*
     * (non-Javadoc)
     * 
     * @seejava.beans.PropertyChangeListener#propertyChange(java.beans.
     * PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (!isActive())
            return;

        if ((mousePressing && resizedDuringMousePress)
                || (keyPressing && resizedDuringKeyPress))
            return;

        updateCenterPoint();
    }

    private void updateCenterPoint() {
        if (!isActive() || viewport == null)
            return;
        if (centerOnContents) {
            Layer layer = getViewer().getLayer(GEF.LAYER_CONTENTS);
            Rectangle bounds;
            if (layer instanceof FreeformFigure) {
                bounds = ((FreeformFigure) layer).getFreeformExtent();
            } else {
                bounds = layer.getBounds();
            }
            centerPoint.setLocation(bounds.x + bounds.width / 2, bounds.y
                    + bounds.height / 2);
        } else {
            Rectangle r = viewport.getClientArea(TEMP_AREA);
            centerPoint.setLocation(r.x + r.width / 2, r.y + r.height / 2);
        }
    }

    private void centerViewportOnResize() {
        if (!isActive() || viewport == null)
            return;

        Rectangle r = viewport.getClientArea(TEMP_AREA);
        viewport.setViewLocation(centerPoint.x - r.width / 2, centerPoint.y
                - r.height / 2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.
     * Event)
     */
    public void handleEvent(Event event) {
        if (!isActive())
            return;

        if (event.type == SWT.Resize) {
            if (needsCenterWhenResizing()) {
                centerViewportOnResize();
            }
        } else if (event.type == SWT.FocusIn || event.type == SWT.Paint) {
            // Fix bug:
            FigureCanvas fc = (FigureCanvas) event.widget;
            org.eclipse.swt.graphics.Rectangle clientArea = fc.getClientArea();
            if (clientArea.width > 0 || clientArea.height > 0) {
                centerViewportOnResize();
                fc.removeListener(event.type, this);
            }
        } else if (event.type == SWT.MouseDown) {
            mousePressing = true;
        } else if (event.type == SWT.KeyDown) {
            keyPressing = true;
        } else if (event.type == SWT.MouseUp) {
            mousePressing = false;
            if (resizedDuringMousePress) {
                resizedDuringMousePress = false;
                centerViewportOnResize();
            }
        } else if (event.type == SWT.KeyUp) {
            keyPressing = false;
            if (resizedDuringKeyPress) {
                resizedDuringKeyPress = false;
                centerViewportOnResize();
            }
        }

    }

    // If resizing occurs while mouse or key is pressed,
    // don't center until mouse or key is released
    private boolean needsCenterWhenResizing() {
        if (!mousePressing && !keyPressing)
            return true;

        if (mousePressing)
            resizedDuringMousePress = true;
        if (keyPressing)
            resizedDuringKeyPress = true;
        return false;
    }
}
