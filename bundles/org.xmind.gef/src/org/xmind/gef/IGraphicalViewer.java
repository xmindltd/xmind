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

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.part.IGraphicalPart;

/**
 * @author Brian Sun
 */
public interface IGraphicalViewer extends IViewer {

    /**
     * A viewer property indicating that this viewer uses 'smooth scrolling'
     * behaviour when the canvas is to be moved to a desired view location by
     * calling 'scrollTo', 'scrollToX', or 'scrollToY', etc. By turning on
     * 'smooth scrolling', the viewer always modifies the viewport's range model
     * before updating the canvas, while the default behaviour determines by
     * itself when to update the canvas first for efficient painting and
     * repainting. 'Smooth scrolling' is typically useful when some figure is
     * wanted to stay in a steady position relative to the viewport's client
     * area and not to be moved with the canvas before the range model is
     * modified.
     * <p>
     * Values: Boolean, <code>true</code> to use smooth scroll,
     * <code>false</code> otherwise.
     * </p>
     */
    String VIEWER_SCROLL_SMOOTH = "scrollSmooth"; //$NON-NLS-1$

    /**
     * A viewer property indicating that the viewer ignores scroll events when
     * the cursor is not moved by user while the viewport is being scrolled. The
     * default behaviour is to convert scroll events into new 'mouse move'
     * events to dispatch.
     * <p>
     * Values: Boolean, <code>true</code> to ignore scroll event,
     * <code>false</code> otherwise.
     * </p>
     */
    String VIEWER_IGNORE_SCROLL_EVENT = "ignoreScrollEvent"; //$NON-NLS-1$

    String VIEWER_START_DRAG_THRESHOLD = "startDragThreshold"; //$NON-NLS-1$

    /**
     * A viewer property indicating that all text figures should be rendered as
     * a path.
     * <p>
     * Values: Boolean, <code>true</code> to render text as paths,
     * <code>false</code> to use default renderer.
     * </p>
     */
    String VIEWER_RENDER_TEXT_AS_PATH = "renderTextAsPath"; //$NON-NLS-1$

    FigureCanvas getCanvas();

    Dimension getSize();

    Rectangle getClientArea();

    void scrollToX(int x);

    void scrollToY(int x);

    void scrollTo(Point p);

    void scrollTo(int x, int y);

    void scrollDelta(Dimension d);

    void scrollDelta(int dx, int dy);

    Point getScrollPosition();

    void ensureVisible(Rectangle box);

    void center(int x, int y);

    void center(Point cen);

    void center(Rectangle area);

    Point computeToLayer(Point controlPoint, boolean zoomed);

    Point computeToControl(Point layerPoint, boolean zoomed);

    Point computeToDisplay(Point layerPoint, boolean zoomed);

    Point getCenterPoint();

    Layer getLayer(Object key);

    ILayerManager getLayerManager();

    void setLayerManager(ILayerManager layerManager);

    ZoomManager getZoomManager();

    void setZoomManager(ZoomManager zoomManager);

    IGraphicalPart findGraphicalPart(Object model);

}
