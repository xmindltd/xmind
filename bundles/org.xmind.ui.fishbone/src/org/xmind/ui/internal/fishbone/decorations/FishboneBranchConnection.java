/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.fishbone.decorations;

import org.eclipse.draw2d.IFigure;
import org.xmind.gef.draw2d.IAnchor;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.geometry.PrecisionLine;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.AbstractBranchConnection;
import org.xmind.ui.internal.fishbone.structures.ISubDirection;
import org.xmind.ui.mindmap.IBranchPart;

public class FishboneBranchConnection extends AbstractBranchConnection {

    private IBranchPart branch;

    private PrecisionPoint joint = null;

    public FishboneBranchConnection(IBranchPart branch, String id) {
        super(id);
        this.branch = branch;
    }

    protected boolean usesFill() {
        return false;
    }

    protected void route(IFigure figure, Path shape) {
        PrecisionPoint sp = getSourcePosition(figure);
        PrecisionPoint tp = getTargetPosition(figure);
        shape.moveTo(sp);
        if (joint != null) {
            shape.lineTo(joint);
        }
        shape.lineTo(tp);
    }

    protected void reroute(IFigure figure, PrecisionPoint sourcePos,
            PrecisionPoint targetPos, boolean validating) {
        IAnchor sa = getSourceAnchor();
        IAnchor ta = getTargetAnchor();
        if (sa != null && ta != null) {
            PrecisionPoint p1 = sa.getLocation(Geometry
                    .getOppositePosition(getSourceOrientation()), 0);
            PrecisionPoint p2 = sa.getLocation(getSourceOrientation(),
                    getSourceExpansion());
            PrecisionPoint p3 = ta.getLocation(getTargetOrientation(),
                    getTargetExpansion());
            PrecisionPoint p4 = ta.getLocation(Geometry
                    .getOppositePosition(getTargetOrientation()), 0);
            PrecisionPoint j = calcJoint(p1, p2, p3, p4);
            if (j == null) {
                if (p3.getDistance2(p2) > p4.getDistance2(p2)) {
                    PrecisionPoint temp = p3;
                    p3 = p4;
                    p4 = temp;
                }
                j = calcJoint2(ta.getOwner(), p1, p2, p3, p4);
            }
            if (needsSourceLine(ta.getOwner())) {
                joint = j;
                sourcePos.setLocation(p2);
                targetPos.setLocation(calcTarget(ta.getOwner(), p3, p4));
            } else {
                joint = null;
                sourcePos.setLocation(j);
                targetPos.setLocation(calcTarget(ta.getOwner(), p3, p4));
            }
        }
    }

    private PrecisionPoint calcJoint2(IFigure child, PrecisionPoint p1,
            PrecisionPoint p2, PrecisionPoint p3, PrecisionPoint p4) {
        double angle = calcAngle(p1, p2, p3, p4);
        PrecisionPoint p6 = p3.getMoved(Math.toRadians(angle), 100);
        PrecisionPoint j = calcJoint(p1, p2, p3, p6);
        if (j == null)
            return p3;
        return j;
    }

    private double calcAngle(PrecisionPoint p1, PrecisionPoint p2,
            PrecisionPoint p3, PrecisionPoint p4) {
        if (Math.abs(p1.y - p2.y) < 2) {
            if (p1.x > p2.x) {
                if (p3.y < p1.y)
                    return ISubDirection.NWR.getRotateAngle();
                return ISubDirection.SWR.getRotateAngle();
            }
            if (p3.y < p1.y)
                return ISubDirection.NER.getRotateAngle();
            return ISubDirection.SER.getRotateAngle();
        }
        return 0;
    }

    private PrecisionPoint calcJoint(PrecisionPoint p1, PrecisionPoint p2,
            PrecisionPoint p3, PrecisionPoint p4) {
        if (isParalell(p1, p2, p3, p4))
            return null;
        PrecisionLine line1 = new PrecisionLine(p1, p2,
                PrecisionLine.LineType.Line);
        PrecisionLine line2 = new PrecisionLine(p4, p3,
                PrecisionLine.LineType.Line);
        return line1.intersect(line2);
    }

    private boolean isParalell(PrecisionPoint p1, PrecisionPoint p2,
            PrecisionPoint p3, PrecisionPoint p4) {
        double dx1 = p1.x - p2.x;
        double dy1 = p1.y - p2.y;
        double dx2 = p3.x - p4.x;
        double dy2 = p3.y - p4.y;
        if (Math.abs(dx1) < 2)
            return Math.abs(dx2) < 2;
        if (Math.abs(dy1) < 2)
            return Math.abs(dy2) < 2;
        double s1 = dy1 / dx1;
        double s2 = dy2 / dx2;
        return Math.abs(s1 / s2 - 1) < 0.01;
    }

    private PrecisionPoint calcTarget(IFigure child, PrecisionPoint p3,
            PrecisionPoint p4) {
//        if (child != null && child instanceof IDecoratedFigure) {
//            IDecoration decoration = ((IDecoratedFigure) child).getDecoration();
//            if (decoration instanceof FishboneTopicDecoration)
//                return p4;
//        }
        return p3;
    }

    private boolean needsSourceLine(IFigure child) {
        return child == null || !isChild(child, branch.getFigure());
    }

    private boolean isChild(IFigure figure, IFigure branchFigure) {
        IFigure parent = figure.getParent();
        if (parent == branchFigure)
            return true;
        if (parent == null || parent == branchFigure.getParent())
            return false;
        return isChild(parent, branchFigure);
    }

    public void invalidate() {
        joint = null;
        super.invalidate();
    }

}