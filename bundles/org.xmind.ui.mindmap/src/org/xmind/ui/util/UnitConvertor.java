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
package org.xmind.ui.util;

import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.draw2d.geometry.PrecisionInsets;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;

public class UnitConvertor {

    /**
     * Millimeters per inch: <br>
     * <blockquote>a (inch) * INCH_TO_MM = b (mm)</blockquote>
     */
    public static final double INCH_TO_MM = 25.4;

    /**
     * Inches per millimeter:<br>
     * <blockquote>a (mm) * MM_TO_INCH = b (inch)</blockquote>
     */
    public static final double MM_TO_INCH = 1 / INCH_TO_MM;

    private static Point dpi = null;

    private static Point defaultDpi = null;

    public static Point getScreenDpi() {
        if (dpi == null) {
            Display display = Display.getCurrent();
            if (display == null) {
                if (defaultDpi == null)
                    defaultDpi = new Point(72, 72);
                return defaultDpi;
            } else {
                dpi = new Point(display.getDPI());
            }
        }
        return dpi;
    }

    public static double inch2mm(double value) {
        return value * INCH_TO_MM;
    }

    public static double mm2inch(double value) {
        return value / INCH_TO_MM;
    }

    public static PrecisionPoint scrDots2inch(Point p) {
        double x = p.x * 1.0 / getScreenDpi().x;
        double y = p.y * 1.0 / getScreenDpi().y;
        return new PrecisionPoint(x, y);
    }

    public static PrecisionInsets scrDots2inch(Insets ins) {
        double top = ins.top * 1.0 / getScreenDpi().y;
        double left = ins.left * 1.0 / getScreenDpi().x;
        double bottom = ins.bottom * 1.0 / getScreenDpi().y;
        double right = ins.right * 1.0 / getScreenDpi().x;
        return new PrecisionInsets(top, left, bottom, right);
    }

    public static PrecisionPoint scrDots2mm(Point dots) {
        PrecisionPoint p = scrDots2inch(dots);
        p.x = inch2mm(p.x);
        p.y = inch2mm(p.y);
        return p;
    }

    public static PrecisionInsets scrDots2mm(Insets dots) {
        PrecisionInsets ins = scrDots2inch(dots);
        ins.top = inch2mm(ins.top);
        ins.left = inch2mm(ins.left);
        ins.bottom = inch2mm(ins.bottom);
        ins.right = inch2mm(ins.right);
        return ins;
    }

    public static Point inch2scrDots(PrecisionPoint p) {
        return inch2scrDots(p.x, p.y);
    }

    public static Point inch2scrDots(double x, double y) {
        int x2 = (int) (x * getScreenDpi().x);
        int y2 = (int) (y * getScreenDpi().y);
        return new Point(x2, y2);
    }

    public static Point mm2scrDots(PrecisionPoint p) {
        double x = mm2inch(p.x);
        double y = mm2inch(p.y);
        return inch2scrDots(new PrecisionPoint(x, y));
    }

    public static Point mm2scrDots(double x, double y) {
        double x2 = mm2inch(x);
        double y2 = mm2inch(y);
        return inch2scrDots(x2, y2);
    }

    public static PrecisionInsets inch2mm(PrecisionInsets ins) {
        double left = inch2mm(ins.left);
        double right = inch2mm(ins.right);
        double top = inch2mm(ins.top);
        double bottom = inch2mm(ins.bottom);
        return new PrecisionInsets(top, left, bottom, right);
    }

    public static PrecisionInsets mm2inch(PrecisionInsets ins) {
        double left = mm2inch(ins.left);
        double right = mm2inch(ins.right);
        double top = mm2inch(ins.top);
        double bottom = mm2inch(ins.bottom);
        return new PrecisionInsets(top, left, bottom, right);
    }

    public static PrecisionInsets inch2dots(PrecisionInsets ins, Point dpi) {
        double left = ins.left * dpi.x;
        double right = ins.right * dpi.x;
        double top = ins.top * dpi.y;
        double bottom = ins.bottom * dpi.y;
        return new PrecisionInsets(top, left, bottom, right);
    }

    public static PrecisionInsets dots2inch(Insets ins, Point dpi) {
        double left = ins.left * 1.0 / dpi.x;
        double right = ins.right * 1.0 / dpi.x;
        double top = ins.top * 1.0 / dpi.y;
        double bottom = ins.bottom * 1.0 / dpi.y;
        return new PrecisionInsets(top, left, bottom, right);
    }

    public static PrecisionInsets dots2mm(Insets ins, Point dpi) {
        return inch2mm(dots2inch(ins, dpi));
    }

    public static PrecisionInsets mm2dots(PrecisionInsets ins, Point dpi) {
        return inch2dots(mm2inch(ins), dpi);
    }

    public static PrecisionPoint dots2mm(Point p, Point dpi) {
        double x = inch2mm(p.x * 1.0 / dpi.x);
        double y = inch2mm(p.y * 1.0 / dpi.y);
        return new PrecisionPoint(x, y);
    }

    public static PrecisionPoint dots2inch(Point p, Point dpi) {
        double x = p.x * 1.0 / dpi.x;
        double y = p.y * 1.0 / dpi.y;
        return new PrecisionPoint(x, y);
    }

}