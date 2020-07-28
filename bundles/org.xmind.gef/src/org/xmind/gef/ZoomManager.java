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

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * @author Brian Sun
 */
public class ZoomManager extends ZoomObject {

    private double max;

    private double min;

    private double scale = 1;

    public ZoomManager() {
        this(0.5d, 2.0d);
    }

    public ZoomManager(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public void setConstraints(double min, double max) {
        this.min = min;
        this.max = max;
        setScale(getScale());
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        scale = Math.min(getMax(), Math.max(getMin(), scale));
        internalSetScale(scale);
    }

    private void internalSetScale(double scale) {
        double oldScale = getScale();
        if (scale == oldScale)
            return;
        this.scale = scale;
        fireScaleChanged(oldScale, getScale());
    }

    public void zoomIn() {
        double scale = getScale();
        if (scale >= 1 && scale < 1.2) {
            scale = forceMultiple(scale + 0.2, 0.2);
        } else if (scale < 1) {
            scale = Math.min(1.0, forceMultiple(scale + 0.1, 0.1));
        } else {
            scale = forceMultiple(scale + 0.5, 0.5);
        }
        setScale(scale);
    }

    public void zoomOut() {
        double scale = getScale();
        if (scale <= 1) {
            scale = forceMultiple(scale + 0.09, 0.1) - 0.1;
        } else if (scale > 1 && scale <= 1.2) {
            scale = forceMultiple(scale + 0.19, 0.2) - 0.2;
        } else {
            scale = Math.max(1.2, forceMultiple(scale + 0.49, 0.5) - 0.5);
        }
        setScale(scale);
    }

    private static double forceMultiple(double a, double m) {
        int x = (int) Math.round(a * 100);
        int y = (int) Math.round(m * 100);
        int d = x / y;

        double tmp = d * m;
        tmp = ((int) (tmp * 100)) / 100.0;
        return tmp;
    }

    public void actualSize() {
        setScale(1.0);
    }

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }

    public void fitScale(Dimension box, Dimension client) {
        fitScale(box, client, -1, -1);
    }

    private double calculateFitScale(Dimension box, Dimension client) {
        return Math.min(box.width * 1.0 / client.width, box.height * 1.0
                / client.height);
    }

    public void fitScale(Dimension box, Dimension client, double min, double max) {
        double scale = calculateFitScale(box, client);
        if (min > max) {
            double t = max;
            max = min;
            min = t;
        }
        if (min > 0 && scale < min) {
            if (getScale() > min)
                scale = getScale();
            else
                scale = min;
        }
        if (max > 0 && scale > max) {
            if (getScale() < max)
                scale = getScale();
            else
                scale = max;
        }
        setScale(scale);
    }

    public Dimension getScaled(Dimension d) {
        return d.getScaled(getScale());
    }

    public Point getScaled(Point p) {
        return p.getScaled(getScale());
    }

    public Rectangle getScaled(Rectangle r) {
        return r.getCopy().scale(getScale());
    }

    public Dimension getAntiScaled(Dimension d) {
        return d.getScaled(1 / getScale());
    }

    public Point getAntiScaled(Point p) {
        return p.getScaled(1 / getScale());
    }

    public Rectangle getAntiScaled(Rectangle r) {
        return r.getCopy().scale(1 / getScale());
    }

}