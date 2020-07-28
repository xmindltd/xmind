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
import org.xmind.gef.draw2d.SimpleLineFigure;

public class LineFeedback extends AbstractFeedback {

    private Color color;

    private int alpha;

    private SimpleLineFigure figure;

    private Point sourceProsition;

    private Point targetPosition;

    private IPositionProvider positionProvider;

    public void addToLayer(IFigure layer) {
        if (figure == null)
            figure = createFigure();
        layer.add(figure);
    }

    private SimpleLineFigure createFigure() {
        SimpleLineFigure figure = new SimpleLineFigure();
        figure.setForegroundColor(color);
        figure.setAlpha(alpha);
        return figure;
    }

    public boolean containsPoint(Point point) {
        if (figure != null) {
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
            Point p1 = positionProvider.getSourcePosition();
            Point p2 = positionProvider.getTargetPosition();
            if (p1 != null && p2 != null) {
                setLocations(p1, p2);
            }
        }
        if (sourceProsition != null && targetPosition != null) {
            Point p1 = sourceProsition;
            Point p2 = targetPosition;
            if (getZoomManager() != null) {
                p1 = getZoomManager().getScaled(p1);
                p2 = getZoomManager().getScaled(p2);
            }
            figure.setLocations(p1, p2);
        }

        figure.setForegroundColor(color);
        figure.setAlpha(alpha);
    }

    public void setLocations(Point sourcePosition, Point targetPosition) {
        this.sourceProsition = sourcePosition;
        this.targetPosition = targetPosition;
    }

    public Point getSourceProsition() {
        return sourceProsition;
    }

    public Point getTargetPosition() {
        return targetPosition;
    }

    public IPositionProvider getPositionProvider() {
        return positionProvider;
    }

    public void setPositionProvider(IPositionProvider positionProvider) {
        this.positionProvider = positionProvider;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public void setColor(Color color) {
        this.color = color;
    }

}