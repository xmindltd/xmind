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
package org.xmind.ui.dialogs;

import java.util.Arrays;

public class Polygon {

    private int[] points;

    private int size = 0;

    public Polygon() {
        this(100);
    }

    public Polygon(int initialSize) {
        this.points = new int[initialSize];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void lineTo(int x, int y) {
        if (size >= 2 && points[size - 2] == x && points[size - 1] == y)
            return;
        if (points.length < size + 2) {
            points = Arrays.copyOf(points, size + 20);
        }
        points[size++] = x;
        points[size++] = y;
    }

    public void cubicTo(int cx1, int cy1, int cx2, int cy2, int x2, int y2) {
        if (size >= 2) {
            cubicTo(points[size - 2], points[size - 1], cx1, cy1, cx2, cy2, x2,
                    y2);
        }
        lineTo(x2, y2);
    }

    private void cubicTo(int x1, int y1, int cx1, int cy1, int cx2, int cy2,
            int x2, int y2) {
        int total = Math.abs(x1 - cx1) + Math.abs(cx1 - cx2)
                + Math.abs(x2 - cx2) + Math.abs(y1 - cy1) + Math.abs(cy2 - cy1)
                + Math.abs(y2 - cy2);
        if (total > 0) {
            for (int i = 0; i < total; i++) {
                double t = ((double) i) / total;
                double tt = 1 - t;
                int x = (int) Math.round(tt * tt * tt * x1 + tt * tt * t * cx1
                        * 3 + tt * t * t * cx2 * 3 + t * t * t * x2);
                int y = (int) Math.round(tt * tt * tt * y1 + tt * tt * t * cy1
                        * 3 + tt * t * t * cy2 * 3 + t * t * t * y2);
                lineTo(x, y);
            }
        }
    }

    public void quadTo(int cx, int cy, int x2, int y2) {
        if (size >= 2) {
            int x1 = points[size - 2];
            int y1 = points[size - 1];
            int total = Math.abs(x1 - cx) + Math.abs(x2 - cx)
                    + Math.abs(y1 - cy) + Math.abs(y2 - cy);
            if (total > 0) {
                for (int i = 0; i < total; i++) {
                    double t = ((double) i) / total;
                    double tt = 1 - t;
                    int x = (int) Math.round(tt * tt * x1 + tt * t * cx * 2 + t
                            * t * x2);
                    int y = (int) Math.round(tt * tt * y1 + tt * t * cy * 2 + t
                            * t * y2);
                    lineTo(x, y);
                }
            }
        }
        lineTo(x2, y2);
    }

    public void roundCornerTo(int px, int py, int x2, int y2) {
        if (size >= 2) {
            int x1 = points[size - 2];
            int y1 = points[size - 1];
            cubicTo(x1, y1, (int) Math.round(x1 * .25 + px * .75),
                    (int) Math.round(y1 * .25 + py * .75),
                    (int) Math.round(x2 * .25 + px * .75),
                    (int) Math.round(y2 * .25 + py * .75), x2, y2);
        }
        lineTo(x2, y2);
    }

    public int getSize() {
        return size;
    }

    public int[] toPointList() {
        return Arrays.copyOf(points, size);
    }

}
