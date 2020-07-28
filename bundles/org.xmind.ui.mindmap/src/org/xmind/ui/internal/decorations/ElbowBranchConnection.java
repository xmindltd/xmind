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
import org.eclipse.draw2d.PositionConstants;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.AbstractBranchConnection;

public class ElbowBranchConnection extends AbstractBranchConnection {

    protected PrecisionPoint elbow = null;
    protected PrecisionPoint s1 = new PrecisionPoint();
    protected PrecisionPoint s2 = new PrecisionPoint();
    protected PrecisionPoint t1 = new PrecisionPoint();
    protected PrecisionPoint t2 = new PrecisionPoint();
    protected PrecisionPoint e1 = new PrecisionPoint();
    protected PrecisionPoint e2 = new PrecisionPoint();
    protected boolean horizontal = false;
    protected boolean vertical = false;

    public ElbowBranchConnection() {
        super();
    }

    public ElbowBranchConnection(String id) {
        super(id);
    }

    protected void route(IFigure figure, Path shape) {
        PrecisionPoint sp = getSourcePosition(figure);
        PrecisionPoint tp = getTargetPosition(figure);
        if (isTapered()) {
            if (horizontal || vertical) {
                if (horizontal && vertical) {
                    shape.moveTo(sp);
                    shape.lineTo((float) sp.x, (float) tp.y);
                    shape.lineTo(tp);
                    shape.lineTo((float) tp.x, (float) sp.y);
                    shape.close();
                } else {
                    shape.moveTo(s1);
                    shape.lineTo(t1);
                    shape.lineTo(t2);
                    shape.lineTo(s2);
                    shape.close();
                }
            } else {
                shape.moveTo(s1);
                shape.lineTo(e1);
                shape.lineTo(t1);
                shape.lineTo(t2);
                shape.lineTo(e2);
                shape.lineTo(s2);
                shape.close();
            }
        } else {
            shape.moveTo(sp);
            shape.lineTo(elbow);
            shape.lineTo(tp);
        }
    }

    protected void calculateControlPoints(IFigure figure,
            PrecisionPoint sourcePos, PrecisionPoint targetPos) {
        elbow = new PrecisionPoint();
        boolean targetHorizontal = isTargetHorizontal();
        calcElbow(sourcePos, targetPos, targetHorizontal, elbow);
        if (isTapered()) {
            double lineWidth = getLineWidth();
            horizontal = Math.abs(sourcePos.y - targetPos.y) < lineWidth;
            vertical = Math.abs(sourcePos.x - targetPos.x) < lineWidth;
            if (horizontal || vertical) {
                if (!(horizontal && vertical)) {
                    if (horizontal) {
                        e1.setLocation(sourcePos.x,
                                (sourcePos.y + targetPos.y) / 2);
                        e2.setLocation(targetPos.x, e1.y);
                    } else if (vertical) {
                        e1.setLocation((sourcePos.x + targetPos.x) / 2,
                                sourcePos.x);
                        e2.setLocation(e1.x, targetPos.y);
                    }
                    calcTaperedPositions(e1, e2, 0, 1, s1, s2);
                    calcTaperedPositions(e1, e2, 1, 1, t1, t2);
                }
            } else {
                calcTaperedPositions(sourcePos, elbow, 0, s1, s2);
                calcTaperedPositions(elbow, targetPos, 1, 1, t1, t2);

                calcElbow(s1, t1, targetHorizontal, e1);
                calcElbow(s2, t2, targetHorizontal, e2);
            }
        }
    }

    protected boolean isPositionValid() {
        return super.isPositionValid() && elbow != null;
    }

    public void invalidate() {
        super.invalidate();
        elbow = null;
    }

    protected boolean isTargetHorizontal() {
        return (getTargetOrientation() & PositionConstants.EAST_WEST) != 0;
    }

    protected PrecisionPoint calcElbow(PrecisionPoint sourcePos,
            PrecisionPoint targetPos, boolean targetHorizontal,
            PrecisionPoint result) {
        return result.setLocation(targetHorizontal ? sourcePos.x : targetPos.x,
                targetHorizontal ? targetPos.y : sourcePos.y);
    }

}