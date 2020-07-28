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

public class RoundSummaryDecoration extends AbstractSummaryDecoration {

    private static PrecisionPoint ctrl = new PrecisionPoint();
    private static PrecisionPoint c = new PrecisionPoint();
    private static double ratio = 0.3;

    public RoundSummaryDecoration() {
    }

    public RoundSummaryDecoration(String id) {
        super(id);
    }

    protected void route(IFigure figure, Path shape) {
        PrecisionPoint sp = getSourcePosition(figure);
        PrecisionPoint tp = getTargetPosition(figure);
        PrecisionPoint cp = getConclusionPoint(figure);

        shape.moveTo(sp);
        boolean horizontal = isHorizontal();
        if (horizontal) {
            double x = (Math.abs(sp.x - cp.x) < Math.abs(tp.x - cp.x)) ? sp.x
                    : tp.x;
            c.setLocation(x * ratio + cp.x * (1 - ratio), cp.y);
            shape.quadTo(ctrl.setLocation(c.x, sp.y), c);
            shape.quadTo(ctrl.setLocation(c.x, tp.y), tp);
        } else {
            double y = (Math.abs(sp.y - cp.y) < Math.abs(tp.y - cp.y)) ? sp.y
                    : tp.y;
            c.setLocation(cp.x, y * ratio + cp.y * (1 - ratio));
            shape.quadTo(ctrl.setLocation(sp.x, c.y), c);
            shape.quadTo(ctrl.setLocation(tp.x, c.y), tp);
        }

        shape.moveTo(c);
        shape.lineTo(cp);
    }

}