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
import org.eclipse.swt.graphics.Color;
import org.xmind.gef.draw2d.PointFigure;

public class PointFeedback extends AbstractFeedback {

    public static final int DOT = PointFigure.SHAPE_DOT;

    public static final int DIAMOND = PointFigure.SHAPE_DIAMOND;

    public static final int SQUARE = PointFigure.SHAPE_SQUARE;

    private int pointShape;

    private int height;

    private Color borderColor;

    private Color fillColor;

    private int alpha;

    private PointFigure figure;

    private Point position;

    private IPointProvider positionProvider;

    public PointFeedback(int pointShape) {
        this.pointShape = pointShape;
    }

    public void addToLayer(IFigure layer) {
        if (figure == null)
            figure = createFigure();
        layer.add(figure);
    }

    private PointFigure createFigure() {
        PointFigure figure = new PointFigure(pointShape);
        figure.setSize(height, height);
        figure.setForegroundColor(borderColor);
        figure.setBackgroundColor(fillColor);
        figure.setMainAlpha(alpha);
        figure.setSubAlpha(alpha);
        return figure;
    }

    public boolean containsPoint(Point point) {
        if (figure != null && position != null) {
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

        if (positionProvider != null) {
            Point p = positionProvider.getPoint();
            if (p != null)
                setPosition(p);
        }
        if (position != null) {
            figure.setReference(getScaledPosition(position));
        }

        figure.setSize(height, height);
        figure.setForegroundColor(borderColor);
        figure.setBackgroundColor(fillColor);
        figure.setMainAlpha(alpha);
        figure.setSubAlpha(alpha);
    }

    private Point getScaledPosition(Point p) {
        if (getZoomManager() != null) {
            p = getZoomManager().getScaled(p);
        }
        return p;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public IPointProvider getPositionProvider() {
        return positionProvider;
    }

    public void setPositionProvider(IPointProvider positionProvider) {
        this.positionProvider = positionProvider;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public void setHeight(int height) {
        this.height = height;
    }

}