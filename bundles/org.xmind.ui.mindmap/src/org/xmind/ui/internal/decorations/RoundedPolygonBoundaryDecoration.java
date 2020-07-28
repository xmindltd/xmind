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
package org.xmind.ui.internal.decorations;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.decoration.ICorneredDecoration;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.mindmap.IBoundaryPart;

public class RoundedPolygonBoundaryDecoration extends PolygonBoundaryDecoration
        implements ICorneredDecoration {

    private static final float CORNER_CONTROL_RATIO = 0.447715f;

    private IBoundaryPart boundary;

    private int cornerSize = 0;

    public RoundedPolygonBoundaryDecoration() {
        super();
    }

    public RoundedPolygonBoundaryDecoration(IBoundaryPart boundary, String id) {
        super(id);
        this.boundary = boundary;
    }

    protected void sketch(IFigure figure, Path shape, Rectangle box,
            int purpose) {
        List<Point> points = calcPathPoints(box, boundary);

        int c = getAppliedCornerSize();

        if (points.size() > 4) {
            if (c == 0) {
                shape.moveTo(points.get(0));
                for (int i = 1; i < points.size(); i++)
                    shape.lineTo(points.get(i));
                shape.lineTo(points.get(0));
                shape.close();
            } else {
                Point p0 = calcRoundedPoint(points.get(0),
                        points.get(points.size() - 1), c);
                Point p1 = null;
                Point c1, c2;
                shape.moveTo(p0);
                for (int i = 0; i < points.size() - 1; i++) {
                    p1 = calcRoundedPoint(points.get(i), points.get(i + 1), c);
                    c1 = calcControlPoint(p0, points.get(i));
                    c2 = calcControlPoint(p1, points.get(i));
//                    shape.lineTo(p1);
                    shape.cubicTo(c1, c2, p1);
                    p0 = calcRoundedPoint(points.get(i + 1), points.get(i), c);
                    shape.lineTo(p0);
                }
                p1 = calcRoundedPoint(points.get(points.size() - 1),
                        points.get(0), c);
//                shape.lineTo(p1);
                c1 = calcControlPoint(p0, points.get(points.size() - 1));
                c2 = calcControlPoint(p1, points.get(points.size() - 1));
                shape.cubicTo(c1, c2, p1);
                p0 = calcRoundedPoint(points.get(0),
                        points.get(points.size() - 1), c);
                shape.lineTo(p0);
                shape.close();
            }
        } else {
            if (c == 0) {
                shape.addRectangle(box);
            } else {
                shape.addRoundedRectangle(box, c);
            }
        }
    }

    private Point calcRoundedPoint(Point p1, Point p2, int corner) {
        int dx = p2.x - p1.x;
        int dy = p2.y - p1.y;

        if (dx == 0) {
            if (dy > 0)
                return p1.getTranslated(0, corner);
            return p1.getTranslated(0, -corner);
        } else if (dy == 0) {
            if (dx > 0)
                return p1.getTranslated(corner, 0);
            return p1.getTranslated(-corner, 0);
        } else {
            double l = p1.getDistance(p2);
            double x = dx / l * corner;
            double y = dy / l * corner;
            return p1.getTranslated(x, y);
        }
    }

    private Point calcControlPoint(Point p1, Point p2) {
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;

        return p1.getTranslated(dx * CORNER_CONTROL_RATIO,
                dy * CORNER_CONTROL_RATIO);
    }

    public int getCornerSize() {
        return cornerSize;
    }

    private int getAppliedCornerSize() {
        return getCornerSize();
    }

    public void setCornerSize(IFigure figure, int cornerSize) {
        if (cornerSize == this.cornerSize)
            return;

        this.cornerSize = cornerSize;
        invalidate();
        if (figure != null) {
            figure.revalidate();
            figure.repaint();
        }
    }

}