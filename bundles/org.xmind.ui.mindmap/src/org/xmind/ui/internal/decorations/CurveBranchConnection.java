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
import org.eclipse.draw2d.PositionConstants;
import org.xmind.gef.draw2d.geometry.PrecisionLine;
import org.xmind.gef.draw2d.geometry.PrecisionLine.LineType;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.AbstractBranchConnection;

public class CurveBranchConnection extends AbstractBranchConnection {

    private static final double CPRatio = 1.0 / 3;

    private PrecisionPoint s1 = new PrecisionPoint();

    private PrecisionPoint s2 = new PrecisionPoint();

    private PrecisionPoint t1 = new PrecisionPoint();

    private PrecisionPoint t2 = new PrecisionPoint();

    private PrecisionPoint c1 = new PrecisionPoint();

    private PrecisionPoint c2 = new PrecisionPoint();

    private PrecisionPoint c3 = new PrecisionPoint();

    private PrecisionPoint c4 = new PrecisionPoint();

    private PrecisionPoint c5 = new PrecisionPoint();

    private PrecisionPoint c6 = new PrecisionPoint();

    private PrecisionPoint control = null;

    public CurveBranchConnection() {
        super();
    }

    public CurveBranchConnection(String id) {
        super(id);
    }

    protected void route(IFigure figure, Path shape) {
        PrecisionPoint sourcePos = getSourcePosition(figure);
        PrecisionPoint targetPos = getTargetPosition(figure);
        if (isTapered()) {
            shape.moveTo(s1);
            shape.quadTo(c1, t1);
            shape.lineTo(t2);
            shape.quadTo(c2, s2);
            shape.cubicTo(c6, c5, s1);
            shape.close();
        } else {
            shape.moveTo(sourcePos);
            shape.quadTo(control, targetPos);
        }
    }

    protected void calculateControlPoints(IFigure figure,
            PrecisionPoint sourcePos, PrecisionPoint targetPos) {
        control = new PrecisionPoint();
        calcControlPoint(sourcePos, targetPos, isTargetHorizontal(), control);
        if (isTapered()) {
            calcTaperedPositions(sourcePos, control, 0, s1, s2);
            calcTaperedPositions(control, sourcePos, 0, 2, c2, c1);
            calcTaperedPositions(control, targetPos, 1, 1, t1, t2);
            calcTaperedPositions(control, targetPos, 0, 2, c3, c4);
            PrecisionLine l1 = new PrecisionLine(s1, c1, LineType.Ray);
            PrecisionLine l2 = new PrecisionLine(t1, c3, LineType.Ray);
            List<PrecisionPoint> ps = l1.getLinesIntersections(l2);
            if (!ps.isEmpty() && ps.size() == 1) {
                c1.setLocation(ps.get(0));
            }
            l1.setOrigin(s2);
            l1.setTerminus(c2);
            l2.setOrigin(t2);
            l2.setTerminus(c4);
            ps = l1.getLinesIntersections(l2);
            if (!ps.isEmpty() && ps.size() == 1) {
                c2.setLocation(ps.get(0));
            }

            calculateSourceControlPoints(c1, s1, s2, c5);
            calculateSourceControlPoints(c2, s2, s1, c6);
        }
    }

    protected boolean isPositionValid() {
        return super.isPositionValid() && control != null;
    }

    public void invalidate() {
        super.invalidate();
        control = null;
    }

    protected boolean isTargetHorizontal() {
        return (getTargetOrientation() & PositionConstants.EAST_WEST) != 0;
    }

    protected double getControlPointRatio() {
        return CPRatio;
    }

    protected PrecisionPoint calcControlPoint(PrecisionPoint source,
            PrecisionPoint target, boolean targetHorizontal,
            PrecisionPoint result) {
        return result
                .setLocation(//
                        targetHorizontal
                                ? target.x * getControlPointRatio() + source.x
                                        * (1 - getControlPointRatio())
                                : target.x, //
                        targetHorizontal ? target.y
                                : target.y * getControlPointRatio() + source.y
                                        * (1 - getControlPointRatio()));
    }

}
