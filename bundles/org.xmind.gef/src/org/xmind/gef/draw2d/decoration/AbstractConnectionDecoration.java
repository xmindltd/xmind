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
package org.xmind.gef.draw2d.decoration;

import org.eclipse.draw2d.IFigure;
import org.xmind.gef.draw2d.IAnchor;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;

/**
 * @author Frank Shaka
 */
public abstract class AbstractConnectionDecoration
        extends AbstractLineDecoration implements IConnectionDecoration {

    private IAnchor sourceAnchor = null;

    private IAnchor targetAnchor = null;

    private PrecisionPoint sourcePos = null;

    private PrecisionPoint targetPos = null;

    protected AbstractConnectionDecoration() {
        super();
    }

    protected AbstractConnectionDecoration(String id) {
        super(id);
    }

    public IAnchor getSourceAnchor() {
        return sourceAnchor;
    }

    public IAnchor getTargetAnchor() {
        return targetAnchor;
    }

    public void setSourceAnchor(IFigure figure, IAnchor anchor) {
        if (anchor == this.sourceAnchor)
            return;

        this.sourceAnchor = anchor;
        if (figure != null) {
            figure.revalidate();
            repaint(figure);
        }
        invalidate();
    }

    public void setTargetAnchor(IFigure figure, IAnchor anchor) {
        if (anchor == this.targetAnchor)
            return;

        this.targetAnchor = anchor;
        if (figure != null) {
            figure.revalidate();
            repaint(figure);
        }
        invalidate();
    }

    public PrecisionPoint getSourcePosition(IFigure figure) {
        checkValidation(figure);
        return sourcePos;
    }

    public PrecisionPoint getTargetPosition(IFigure figure) {
        checkValidation(figure);
        return targetPos;
    }

    public void reroute(IFigure figure) {
        reroute(figure, false);
    }

    protected void reroute(IFigure figure, boolean validating) {
        PrecisionPoint oldSourcePos = this.sourcePos;
        PrecisionPoint oldTargetPos = this.targetPos;
        PrecisionPoint newSourcePos = new PrecisionPoint();
        PrecisionPoint newTargetPos = new PrecisionPoint();
        reroute(figure, newSourcePos, newTargetPos, validating);
        this.sourcePos = checkPoint(newSourcePos);
        this.targetPos = checkPoint(newTargetPos);

        if (!validating && figure != null) {
            boolean noMove = newSourcePos.equals(oldSourcePos)
                    && newTargetPos.equals(oldTargetPos);
            boolean translational = (oldSourcePos != null
                    && newSourcePos != null && oldTargetPos != null
                    && newTargetPos != null)
                    && Math.abs((newTargetPos.x - oldTargetPos.x)
                            - (newSourcePos.x - oldSourcePos.x)) < 1
                    && Math.abs((newTargetPos.y - oldTargetPos.y)
                            - (newSourcePos.y - oldSourcePos.y)) < 1;

            if (!noMove && !translational) {
                figure.revalidate();
                repaint(figure);
            }
        }
    }

    protected void reroute(IFigure figure, PrecisionPoint sourcePos,
            PrecisionPoint targetPos, boolean validating) {
        IAnchor sa = getSourceAnchor();
        IAnchor ta = getTargetAnchor();
        if (sa == null || ta == null) {
            if (sa != null) {
                sourcePos.setLocation(sa.getReferencePoint());
            }
            if (ta != null) {
                targetPos.setLocation(ta.getReferencePoint());
            }
            return;
        }
        targetPos.setLocation(ta.getLocation(sa.getReferencePoint(), 0));
        sourcePos.setLocation(sa.getLocation(ta.getReferencePoint(), 0));
    }

    public void validate(IFigure figure) {
        super.validate(figure);
        if (!isPositionValid()) {
            reroute(figure, true);
        }
    }

    protected boolean isPositionValid() {
        return sourcePos != null && targetPos != null;
    }

    protected PrecisionPoint checkPoint(PrecisionPoint source) {
        if (Double.isNaN(source.x) || Double.isInfinite(source.x)) {
            source.x = 0.0;
        }
        if (Double.isNaN(source.y) || Double.isInfinite(source.y)) {
            source.y = 0.0;
        }
        return source;
    }
}
