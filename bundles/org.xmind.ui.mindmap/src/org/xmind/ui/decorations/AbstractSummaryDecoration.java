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
package org.xmind.ui.decorations;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.xmind.gef.draw2d.IAnchor;
import org.xmind.gef.draw2d.decoration.IShadowedDecoration;
import org.xmind.gef.draw2d.decoration.PathConnectionDecoration;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.ui.style.Styles;

public abstract class AbstractSummaryDecoration extends
        PathConnectionDecoration implements ISummaryDecoration,
        IShadowedDecoration {

    private int direction = PositionConstants.EAST;

    private IAnchor conclusionAnchor = null;

    private PrecisionPoint conclusionPoint = null;

    protected AbstractSummaryDecoration() {
        super();
    }

    protected AbstractSummaryDecoration(String id) {
        super(id);
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(IFigure figure, int direction) {
        if (direction == this.direction)
            return;

        this.direction = direction;
        if (figure != null) {
            figure.revalidate();
            repaint(figure);
        }
        invalidate();
    }

    protected boolean isHorizontal() {
        return (getDirection() & PositionConstants.EAST_WEST) != 0;
    }

    public IAnchor getNodeAnchor() {
        return conclusionAnchor;
    }

    public void setNodeAnchor(IFigure figure, IAnchor anchor) {
        if (anchor == this.conclusionAnchor)
            return;

        this.conclusionAnchor = anchor;
        if (figure != null) {
            figure.revalidate();
            repaint(figure);
        }
        invalidate();
    }

    public void invalidate() {
        super.invalidate();
        conclusionPoint = null;
    }

    protected void reroute(IFigure figure, PrecisionPoint sourcePos,
            PrecisionPoint targetPos, boolean validating) {
        PrecisionPoint oldConclusionPoint = this.conclusionPoint;
        PrecisionPoint newConclusionPoint = new PrecisionPoint();
        reroute(figure, sourcePos, targetPos, newConclusionPoint);
        this.conclusionPoint = newConclusionPoint;
        if (!validating && figure != null) {
            if (!newConclusionPoint.equals(oldConclusionPoint)) {
                figure.revalidate();
                repaint(figure);
            }
        }
    }

    protected void reroute(IFigure figure, PrecisionPoint sourcePos,
            PrecisionPoint targetPos, PrecisionPoint conclusionPoint) {
        IAnchor sa = getSourceAnchor();
        IAnchor ta = getTargetAnchor();
        IAnchor ca = getNodeAnchor();
        if (sa != null) {
            sourcePos.setLocation(sa.getLocation(getDirection(), getSpacing()));
        }
        if (ta != null) {
            targetPos.setLocation(ta.getLocation(getDirection(), getSpacing()));
        }
        if (ca != null) {
            conclusionPoint.setLocation(ca.getLocation(
                    invertDirection(getDirection()), getSpacing()));
        } else {
            double cx = (sourcePos.x + targetPos.x) / 2;
            double cy = (sourcePos.y + targetPos.y) / 2;
            switch (getDirection()) {
            case PositionConstants.NORTH:
                conclusionPoint.setLocation(cx, cy - getWidth());
                break;
            case PositionConstants.SOUTH:
                conclusionPoint.setLocation(cx, cy + getWidth());
                break;
            case PositionConstants.EAST:
                conclusionPoint.setLocation(cx + getWidth(), cy);
                break;
            case PositionConstants.WEST:
                conclusionPoint.setLocation(cx - getWidth(), cy);
                break;
            }
        }
    }

    protected static int invertDirection(int dir) {
        switch (dir) {
        case PositionConstants.NORTH:
            return PositionConstants.SOUTH;
        case PositionConstants.SOUTH:
            return PositionConstants.NORTH;
        case PositionConstants.EAST:
            return PositionConstants.WEST;
        case PositionConstants.WEST:
            return PositionConstants.EAST;
        }
        return dir;
    }

    protected int getSpacing() {
        return Styles.DEFAULT_SUMMARY_SPACING;
    }

    protected int getWidth() {
        return Styles.DEFAULT_SUMMARY_WIDTH;
    }

    public int getPreferredWidth(IFigure figure) {
        checkValidation(figure);
        int s = getSpacing();
        return getWidth() + s + s;
    }

    @Override
    protected int getLineWidthForChecking() {
        return super.getLineWidthForChecking() * 2 + 10;
    }

    public PrecisionPoint getConclusionPoint(IFigure figure) {
        checkValidation(figure);
        return conclusionPoint;
    }

    protected boolean isPositionValid() {
        return super.isPositionValid() && conclusionPoint != null;
    }

    public void paintShadow(IFigure figure, Graphics graphics) {
        if (!isVisible())
            return;
        checkValidation(figure);
        graphics.setAlpha(getAlpha());
        graphics.setForegroundColor(ColorConstants.black);
        graphics.setLineWidth(getLineWidth());
        graphics.setLineStyle(getLineStyle());
        drawLine(figure, graphics);
    }
}