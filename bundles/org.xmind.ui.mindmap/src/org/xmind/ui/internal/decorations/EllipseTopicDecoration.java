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

import static java.lang.Math.max;
import static java.lang.Math.round;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.AbstractTopicDecoration;
import org.xmind.ui.util.NumberUtils;

public class EllipseTopicDecoration extends AbstractTopicDecoration {

    private static final double PROPORTION = 1; //1.618d;

    private static final int CORNER_GAP = 2;

    private static final int SHRINKAGE = 10;

    public EllipseTopicDecoration() {
        super();
    }

    public EllipseTopicDecoration(String id) {
        super(id);
    }

    protected void sketch(IFigure figure, Path shape, Rectangle box,
            int purpose) {
        if (purpose == CHECK) {
            int halfLineWidth = getLineWidth() / 2;
            shape.addArc(box.getExpanded(halfLineWidth, halfLineWidth), 0, 360);
        } else {
            shape.addArc(box, 0, 360);
        }
        shape.close();
    }

    public PrecisionPoint getAnchorLocation(IFigure figure, double refX,
            double refY, double expansion) {
        return Geometry.getChopOvalLocation(refX, refY, figure.getBounds(),
                expansion);
    }

    public Insets getPreferredInsets(IFigure figure, int width, int height) {
        double w = width / 2.0 + CORNER_GAP;
        double h = height / 2.0 + CORNER_GAP;
        //
        //     w ^ 2             h ^ 2 
        // --------------- + --------------- = 1 
        // ( w + l ) ^ 2      ( h + t ) ^ 2
        // 
        // t / l = k
        // ( deprecated: ( h + t ) / ( w + l ) = PROPORTION ) 
        //
        // double t = sqrt( w * w / ( PROPORTION * PROPORTION ) + h * h ) - h;
        // double l = ( h + t ) * PROPORTION - w;
        double k = PROPORTION;
        int a = 1;
        double b = 2 * (k * w + h) / k;
        double c = 4 * w * h / k;
        int d = 0;
        double e = -w * w * h * h / (k * k);
        double l = NumberUtils.newton(new double[] { a, b, c, d, e }, w / 2);
        double t = k * l;

        int prefHeight = (int) round(t);
        int prefWidth = (int) round(l);
        int minHeight = max(a, prefHeight - SHRINKAGE / 2);
        int minWidth = max(a, prefWidth - SHRINKAGE);
        return Geometry.add(super.getPreferredInsets(figure, width, height),
                minHeight, minWidth, minHeight, minWidth);
    }

}