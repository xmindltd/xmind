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
package org.xmind.gef.draw2d.graphics;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.PathData;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.geometry.PrecisionRectangle;

public class Path extends org.eclipse.swt.graphics.Path {

    /**
     * @param device
     */
    public Path(Device device) {
        super(device);
    }

    public Path(Device device, PathData data) {
        super(device, data);
    }

    /**
     * @see org.eclipse.swt.graphics.Path#moveTo(float, float)
     */
    public void moveTo(PrecisionPoint p) {
        super.moveTo((float) p.x, (float) p.y);
    }

    public void moveTo(Point p) {
        super.moveTo(p.x, p.y);
    }

    /**
     * @see org.eclipse.swt.graphics.Path#lineTo(float, float)
     */
    public void lineTo(PrecisionPoint p) {
        super.lineTo((float) p.x, (float) p.y);
    }

    public void lineTo(Point p) {
        super.lineTo(p.x, p.y);
    }

    /**
     * @see org.eclipse.swt.graphics.Path#cubicTo(float, float, float, float,
     *      float, float)
     */
    public void cubicTo(PrecisionPoint control1, PrecisionPoint control2,
            PrecisionPoint dest) {
        super.cubicTo((float) control1.x, (float) control1.y,
                (float) control2.x, (float) control2.y, (float) dest.x,
                (float) dest.y);
    }

    public void cubicTo(Point control1, Point control2, Point dest) {
        super.cubicTo(control1.x, control1.y, control2.x, control2.y, dest.x,
                dest.y);
    }

    /**
     * @see org.eclipse.swt.graphics.Path#quadTo(float, float, float, float)
     */
    public void quadTo(PrecisionPoint control, PrecisionPoint dest) {
        super.quadTo((float) control.x, (float) control.y, (float) dest.x,
                (float) dest.y);
    }

    public void quadTo(Point control, Point dest) {
        super.quadTo(control.x, control.y, dest.x, dest.y);
    }

    public void addArc(PrecisionRectangle bounds, float startAngle,
            float arcAngle) {
        super.addArc((float) bounds.x, (float) bounds.y, (float) bounds.width,
                (float) bounds.height, startAngle, arcAngle);
    }

    public void addArc(Rectangle bounds, float startAngle, float arcAngle) {
        super.addArc(bounds.x, bounds.y, bounds.width, bounds.height,
                startAngle, arcAngle);
    }

    public void addRectangle(PrecisionRectangle bounds) {
        super.addRectangle((float) bounds.x, (float) bounds.y,
                (float) bounds.width, (float) bounds.height);
    }

    public void addRectangle(Rectangle bounds) {
        super.addRectangle(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public void addRoundedRectangle(Rectangle bounds, float corner) {
        addRoundedRectangle(bounds.x, bounds.y, bounds.width, bounds.height,
                corner);
    }

    public void addRoundedRectangleByRatio(Rectangle bounds,
            float cornerRatio) {
        addRoundedRectangle(bounds.x, bounds.y, bounds.width, bounds.height,
                Math.min(bounds.width, bounds.height) * cornerRatio);
    }

    public void addRoundedRectangle(PrecisionRectangle bounds, float corner) {
        addRoundedRectangle((float) bounds.x, (float) bounds.y,
                (float) bounds.width, (float) bounds.height, corner);
    }

    private static final float CORNER_CONTROL_RATIO = 0.447715f;

    public void addRoundedRectangle(float x, float y, float width, float height,
            float corner) {
        float r = x + width;
        float b = y + height;
        float x0 = x + width / 2;
        float y0 = y + height / 2;

        float x1 = Math.min(x + corner, x0);
        moveTo(x1, y);

        float y1 = Math.min(y + corner, y0);
        float cx1 = x + (x1 - x) * CORNER_CONTROL_RATIO;
        float cy1 = y + (y1 - y) * CORNER_CONTROL_RATIO;
        cubicTo(cx1, y, x, cy1, x, y1);

        float y2 = Math.max(b - corner, y0);
        lineTo(x, y2);

        float cy2 = b - (b - y2) * CORNER_CONTROL_RATIO;
        cubicTo(x, cy2, cx1, b, x1, b);

        float x2 = Math.max(r - corner, x0);
        lineTo(x2, b);

        float cx2 = r - (r - x2) * CORNER_CONTROL_RATIO;
        cubicTo(cx2, b, r, cy2, r, y2);

        lineTo(r, y1);

        cubicTo(r, cy1, cx2, y, x2, y);

//        float y1 = Math.min(y + corner, y0);
//        moveTo(x, y1);

//        float x1 = Math.min(x + corner, x0);
//        float cx1 = x + (x1 - x) * CORNER_CONTROL_RATIO;
//        float cy1 = y + (y1 - y) * CORNER_CONTROL_RATIO;
//        cubicTo(x, cy1, cx1, y, x1, y);
//
//        float x2 = Math.max(r - corner, x0);
//        lineTo(x2, y);
//
//        float cx2 = r - (r - x2) * CORNER_CONTROL_RATIO;
//        cubicTo(cx2, y, r, cy1, r, y1);
//
//        float y2 = Math.max(b - corner, y0);
//        lineTo(r, y2);
//
//        float cy2 = b - (b - y2) * CORNER_CONTROL_RATIO;
//        cubicTo(r, cy2, cx2, b, x2, b);
//
//        lineTo(x1, b);
//
//        cubicTo(cx1, b, x, cy2, x, y2);

        close();
    }

    public void addRoundedPolygon(float corner, PrecisionPoint... points) {
        if (points == null || points.length < 3)
            return;
        float[] locs = new float[points.length * 2];
        for (int i = 0; i < points.length; i++) {
            PrecisionPoint p = points[i];
            locs[i * 2] = (float) p.x;
            locs[i * 2 + 1] = (float) p.y;
        }
        addRoundedPolygon(corner, locs);
    }

    public void addRoundedPolygon(float corner, float... locs) {
        if (locs == null || locs.length < 6)
            return;
        float qc = corner / 4;
        int len = locs.length;
        float[] last = null;
        for (int i = 0; i < len - 1; i += 2) {
            float x1 = locs[i];
            float y1 = locs[i + 1];
            float x2 = i < len - 2 ? locs[i + 2] : locs[i - len + 2];
            float y2 = i < len - 2 ? locs[i + 3] : locs[i - len + 3];
            float x3 = i < len - 4 ? locs[i + 4] : locs[i - len + 4];
            float y3 = i < len - 4 ? locs[i + 5] : locs[i - len + 5];
            if (last == null) {
                last = calcPoint(x1, y1, x2, y2, corner);
                moveTo(last[0], last[1]);
            }
            float[] p1 = calcPoint(x2, y2, x1, y1, corner);
            lineTo(p1[0], p1[1]);
            float[] c1 = calcPoint(x2, y2, x1, y1, qc);
            float[] c2 = calcPoint(x2, y2, x3, y3, qc);
            float[] p2 = calcPoint(x2, y2, x3, y3, corner);
            cubicTo(c1[0], c1[1], c2[0], c2[1], p2[0], p2[1]);
            last = p2;
        }
        close();
    }

    private float[] calcPoint(float x1, float y1, float x2, float y2,
            float dist) {
        float x;
        float y;
        if (x1 == x2) {
            x = x1;
            if (y1 == y2) {
                y = y1;
            } else {
                y = y1 + dist;
            }
        } else {
            if (y1 == y2) {
                y = y1;
                x = x1 + dist;
            } else {
                float d = (x2 - x1) / (y2 - y1);
                float r = (float) (dist / Math.sqrt(d * d + 1));
                if (y2 < y1)
                    r = -r;
                y = r + y1;
                x = (y - y1) * d + x1;
            }
        }
        return new float[] { x, y };
    }

    public void addString(String text, PrecisionPoint loc, Font font) {
        super.addString(text, (float) loc.x, (float) loc.y, font);
    }

    public void addString(String text, Point loc, Font font) {
        super.addString(text, loc.x, loc.y, font);
    }

    public PrecisionPoint getCurrentPoint() {
        float[] current = new float[2];
        getCurrentPoint(current);
        return new PrecisionPoint(current[0], current[1]);
    }

}