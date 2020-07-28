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

import org.eclipse.draw2d.IFigure;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.AbstractSummaryDecoration;

public class SquareSummaryDecoration extends AbstractSummaryDecoration {

    private static PrecisionPoint se = new PrecisionPoint();
    private static PrecisionPoint te = new PrecisionPoint();
    private static PrecisionPoint ce = new PrecisionPoint();

    public SquareSummaryDecoration() {
    }

    public SquareSummaryDecoration(String id) {
        super(id);
    }

    protected void route(IFigure figure, Path shape) {
        PrecisionPoint sp = getSourcePosition(figure);
        PrecisionPoint tp = getTargetPosition(figure);
        PrecisionPoint cp = getConclusionPoint(figure);

        boolean horizontal = isHorizontal();
        if (horizontal) {
            double x = calculateCenterX(sp, tp, cp);
            se.setLocation(x, sp.y);
            te.setLocation(x, tp.y);
            ce.setLocation(x, cp.y);
        } else {
            double y = calculateCenterY(sp, tp, cp);
            se.setLocation(sp.x, y);
            te.setLocation(tp.x, y);
            ce.setLocation(cp.x, y);
        }

        shape.moveTo(sp);
        shape.lineTo(se);
        shape.lineTo(te);
        shape.lineTo(tp);
        shape.moveTo(ce);
        shape.lineTo(cp);
    }

    private double calculateCenterX(PrecisionPoint sp, PrecisionPoint tp,
            PrecisionPoint cp) {
        double x1 = (sp.x + cp.x) / 2;
        double x2 = (tp.x + cp.x) / 2;
        return (Math.abs(x1 - cp.x) < Math.abs(x2 - cp.x)) ? x1 : x2;
    }

    private double calculateCenterY(PrecisionPoint sp, PrecisionPoint tp,
            PrecisionPoint cp) {
        double y1 = (sp.y + cp.y) / 2;
        double y2 = (tp.y + cp.y) / 2;
        return (Math.abs(y1 - cp.y) < Math.abs(y2 - cp.y)) ? y1 : y2;
    }

}