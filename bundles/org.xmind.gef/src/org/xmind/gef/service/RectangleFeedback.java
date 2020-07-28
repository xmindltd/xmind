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

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.xmind.gef.draw2d.SimpleRectangleFigure;
import org.xmind.gef.draw2d.geometry.Geometry;

public class RectangleFeedback extends AbstractFeedback {

    private SimpleRectangleFigure figure;

    private Rectangle bounds;

    private Color borderColor;

    private Color fillColor;

    private int borderAlpha;

    private int fillAlpha;

    private IRectangleProvider boundsProvider;

    private SimpleRectangleFigure createFigure() {
        SimpleRectangleFigure figure = new SimpleRectangleFigure();
        figure.setForegroundColor(borderColor);
        figure.setBackgroundColor(fillColor);
        figure.setMainAlpha(borderAlpha);
        figure.setSubAlpha(fillAlpha);
        return figure;
    }

    public void addToLayer(IFigure layer) {
        if (figure == null)
            figure = createFigure();
        layer.add(figure);
    }

    public boolean containsPoint(Point point) {
        if (figure != null && bounds != null) {
            if (getZoomManager() != null)
                point = getZoomManager().getScaled(point);
            return figure.containsPoint(point) && figure.isShowing();
        }
        return false;
    }

    public void removeFromLayer(IFigure layer) {
        if (figure != null)
            layer.remove(figure);
    }

    public void update() {
        if (figure == null)
            return;

        if (boundsProvider != null) {
            Rectangle rect = boundsProvider.getRectangle();
            if (rect != null) {
                setBounds(rect);
            }
        }
        if (bounds != null) {
            figure.setClient(getScaledBounds(bounds));
        }

        figure.setForegroundColor(borderColor);
        figure.setBackgroundColor(fillColor);
        figure.setMainAlpha(borderAlpha);
        figure.setSubAlpha(fillAlpha);
    }

    private Rectangle getScaledBounds(Rectangle r) {
        if (getZoomManager() != null) {
            r = getZoomManager().getScaled(r);
        }
        return Geometry.getBounds(r, true, 1, 0);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }

    public IRectangleProvider getBoundsProvider() {
        return boundsProvider;
    }

    public void setBoundsProvider(IRectangleProvider boundsProvider) {
        this.boundsProvider = boundsProvider;
    }

    public void setBorderAlpha(int borderAlpha) {
        this.borderAlpha = borderAlpha;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    public void setFillAlpha(int fillAlpha) {
        this.fillAlpha = fillAlpha;
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

}