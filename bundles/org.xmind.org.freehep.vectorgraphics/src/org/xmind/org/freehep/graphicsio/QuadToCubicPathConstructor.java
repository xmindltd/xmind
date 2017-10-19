// Copyright 2001 freehep
package org.xmind.org.freehep.graphicsio;

import java.io.IOException;

/**
 * Implements the Quadratic Bezier Curve PathConstructor functionality in terms
 * of Cubic Bezier Curves
 *
 * @author Mark Donszelmann
 * @version $Id: freehep-graphicsio/src/main/java/org/freehep/graphicsio/QuadToCubicPathConstructor.java 5641ca92a537 2005/11/26 00:15:35 duns $
 */
public abstract class QuadToCubicPathConstructor extends
        AbstractPathConstructor {

    protected QuadToCubicPathConstructor() {
        super();
    }

    public void move(double x, double y) throws IOException {
        currentX = x;
        currentY = y;
    }

    public void line(double x, double y) throws IOException {
        currentX = x;
        currentY = y;
    }

    public void quad(double x1, double y1, double x2, double y2)
            throws IOException {
        double xctrl1 = x1 + (currentX - x1) / 3.;
        double yctrl1 = y1 + (currentY - y1) / 3.;
        double xctrl2 = x1 + (x2 - x1) / 3.;
        double yctrl2 = y1 + (y2 - y1) / 3.;

        cubic(xctrl1, yctrl1, xctrl2, yctrl2, x2, y2);

        currentX = x2;
        currentY = y2;
    }

    public void cubic(double x1, double y1, double x2, double y2, double x3,
            double y3) throws IOException {
        currentX = x3;
        currentY = y3;
    }

    public void closePath(double x0, double y0) throws IOException {
        currentX = 0;
        currentY = 0;
    }
}
