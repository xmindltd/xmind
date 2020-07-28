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
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.IAnchor;
import org.xmind.gef.draw2d.ITextFigure;
import org.xmind.gef.draw2d.ITitledFigure;
import org.xmind.gef.draw2d.decoration.IShadowedDecoration;
import org.xmind.gef.draw2d.decoration.PathConnectionDecoration;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.style.Styles;

public abstract class AbstractRelationshipDecoration
        extends PathConnectionDecoration
        implements IRelationshipDecoration, IShadowedDecoration {

    private static final Rectangle CLIP = new Rectangle();

    protected Double sourceCPAngle = null;

    protected Double sourceCPAmount = null;

    protected Double targetCPAngle = null;

    protected Double targetCPAmount = null;

    protected Point relativeSourceCP = null;

    protected Point relativeTargetCP = null;

    private PrecisionPoint sourceCP = null;

    private PrecisionPoint targetCP = null;

    private IArrowDecoration arrow1 = null;

    private IArrowDecoration arrow2 = null;

    private PrecisionPoint titlePos = null;

    protected AbstractRelationshipDecoration() {
    }

    protected AbstractRelationshipDecoration(String id) {
        super(id);
    }

    protected int getLineWidthForChecking() {
        return super.getLineWidthForChecking() * 3 + 10;
    }

    public void reroute(IFigure figure) {
        super.reroute(figure);
        updateArrows(figure);
    }

    protected void reroute(IFigure figure, PrecisionPoint sourcePos,
            PrecisionPoint targetPos, boolean validating) {
        PrecisionPoint oldSourceCP = this.sourceCP;
        PrecisionPoint oldTargetCP = this.targetCP;
        PrecisionPoint oldTitlePos = this.titlePos;
        PrecisionPoint newSourceCP = new PrecisionPoint();
        PrecisionPoint newTargetCP = new PrecisionPoint();
        PrecisionPoint newTitlePos = new PrecisionPoint();
        reroute(figure, sourcePos, targetPos, newSourceCP, newTargetCP);
        calcTitlePosition(figure, newTitlePos, sourcePos, targetPos,
                newSourceCP, newTargetCP);
        this.sourceCP = newSourceCP;
        this.targetCP = newTargetCP;
        this.titlePos = newTitlePos;
        if (!validating && figure != null) {
            if (!newSourceCP.equals(oldSourceCP)
                    || !newTargetCP.equals(oldTargetCP)
                    || !newTitlePos.equals(oldTitlePos)) {
                figure.revalidate();
                repaint(figure);
            }
        }
    }

    protected void reroute(IFigure figure, PrecisionPoint sourcePos,
            PrecisionPoint targetPos, PrecisionPoint sourceCP,
            PrecisionPoint targetCP) {
        IAnchor sa = getSourceAnchor();
        IAnchor ta = getTargetAnchor();
//        if (sa != null)
//            sourcePos.setLocation(sa.getReferencePoint());
//        if (ta != null)
//            targetPos.setLocation(ta.getReferencePoint());

        PrecisionPoint a1 = null;
        PrecisionPoint a2 = null;
        if (sa != null) {
            if (relativeSourceCP != null) {
                sourceCP.setLocation(sa.getReferencePoint())
                        .translate(relativeSourceCP.x, relativeSourceCP.y);
                sourcePos.setLocation(sa.getLocation(sourceCP, 0));
            } else if (ta != null) {
                if (a1 == null)
                    a1 = sa.getLocation(ta.getReferencePoint(), 0);
                sourcePos.setLocation(a1);
                if (a2 == null)
                    a2 = ta.getLocation(sa.getReferencePoint(), 0);
                sourceCP.setLocation(a1).move(a2,
                        Styles.DEF_CONTROL_POINT_AMOUNT);
            }
        }

        if (ta != null) {
            if (relativeTargetCP != null) {
                targetCP.setLocation(ta.getReferencePoint())
                        .translate(relativeTargetCP.x, relativeTargetCP.y);
                targetPos.setLocation(ta.getLocation(targetCP, 0));
            } else if (sa != null) {
                if (a2 == null)
                    a2 = ta.getLocation(sa.getReferencePoint(), 0);
                targetPos.setLocation(a2);
                if (a1 == null)
                    a1 = sa.getLocation(ta.getReferencePoint(), 0);
                targetCP.setLocation(a2).move(a1,
                        Styles.DEF_CONTROL_POINT_AMOUNT);
            }
        }

//        double dx = targetPos.x - sourcePos.x;
//        double dy = targetPos.y - sourcePos.y;
//        double angle1 = getAngleValue(sourceCPAngle);
//        double theta1 = Geometry.getAngle(dx, dy) + angle1;
//        sourceCP.setLocation(sourcePos).move(theta1, 100);
//
//        double angle2 = getAngleValue(targetCPAngle);
//        double theta2 = Geometry.getAngle(-dx, -dy) + angle2;
//        targetCP.setLocation(targetPos).move(theta2, 100);
//
//        if (sa != null)
//            sourcePos.setLocation(sa.getLocation(sourceCP, 0));
//        if (ta != null)
//            targetPos.setLocation(ta.getLocation(targetCP, 0));
//
//        dx = targetPos.x - sourcePos.x;
//        dy = targetPos.y - sourcePos.y;
//        double d = Math.max(Math.hypot(dx, dy), Geometry.MIN_DISTANCE);
//
//        double amount1 = getAmountValue(sourceCPAmount);
//        sourceCP.setLocation(sourcePos).move(theta1, d * amount1);
//
//        double amount2 = getAmountValue(targetCPAmount);
//        targetCP.setLocation(targetPos).move(theta2, d * amount2);
    }

    protected void updatePosAndCp(IAnchor anchor, PrecisionPoint pos,
            PrecisionPoint cp, double angle) {
        Point center = anchor.getReferencePoint().toDraw2DPoint();
        Point rotatedCp = Geometry.getRotatedPoint(
                new Point(cp.toDraw2DPoint()).translate(-center.x, -center.y),
                angle);
        cp.setLocation(rotatedCp.translate(center.x, center.y));
        pos.setLocation(anchor.getLocation(cp, 0));
    }

    protected void calcTitlePosition(IFigure figure, PrecisionPoint titlePos,
            PrecisionPoint sourcePos, PrecisionPoint targetPos,
            PrecisionPoint sourceCP, PrecisionPoint targetCP) {
        titlePos.setLocation((sourcePos.x + targetPos.x) / 2,
                (sourcePos.y + targetPos.y) / 2);
    }

//    private double getAngleValue(Double angle) {
//        return angle == null ? Styles.DEF_CONTROL_POINT_ANGLE : angle
//                .doubleValue();
//    }
//
//    private double getAmountValue(Double amount) {
//        return amount == null ? Styles.DEF_CONTROL_POINT_AMOUNT : amount
//                .doubleValue();
//    }

    protected boolean isPositionValid() {
        return super.isPositionValid() && sourceCP != null && targetCP != null
                && titlePos != null;
    }

    public PrecisionPoint getSourceControlPoint(IFigure figure) {
        checkValidation(figure);
        return sourceCP;
    }

    public PrecisionPoint getTargetControlPoint(IFigure figure) {
        checkValidation(figure);
        return targetCP;
    }

    public void setRelativeSourceControlPoint(IFigure figure, Point point) {
        if (point == this.relativeSourceCP
                || (point != null && point.equals(this.relativeSourceCP)))
            return;

        this.relativeSourceCP = point;
        if (figure != null) {
            figure.revalidate();
            figure.repaint();
        }
        invalidate();
    }

    public void setRelativeTargetControlPoint(IFigure figure, Point point) {
        if (point == this.relativeTargetCP
                || (point != null && point.equals(this.relativeTargetCP)))
            return;

        this.relativeTargetCP = point;
        if (figure != null) {
            figure.revalidate();
            figure.repaint();
        }
        invalidate();
    }

    public void setSourceControlPointHint(IFigure figure, Double angle,
            Double amount) {
        boolean changed = false;
        if (angle != this.sourceCPAngle
                && (angle == null || !angle.equals(this.sourceCPAngle))) {
            changed = true;
            this.sourceCPAngle = angle;
        }
        if (amount != this.sourceCPAmount
                && (amount == null || !amount.equals(this.sourceCPAmount))) {
            changed = true;
            this.sourceCPAmount = amount;
        }
        if (changed && figure != null) {
            figure.revalidate();
            repaint(figure);
        }
    }

    public void setTargetControlPointHint(IFigure figure, Double angle,
            Double amount) {
        boolean changed = false;
        if (angle != this.targetCPAngle
                && (angle == null || !angle.equals(this.targetCPAngle))) {
            changed = true;
            this.targetCPAngle = angle;
        }
        if (amount != this.targetCPAmount
                && (amount == null || !amount.equals(this.targetCPAmount))) {
            changed = true;
            this.targetCPAmount = amount;
        }
        if (changed && figure != null) {
            figure.revalidate();
            repaint(figure);
        }
    }

//    public void setOrthogonalSourceControlPointHint(IFigure figure,
//            PrecisionPoint pos) {
//    }
//
//    public void setOrthogonalTargetControlPointHint(IFigure figure,
//            PrecisionPoint pos) {
//    }
//
//    public Double getPolarSourceControlPointAmountHint() {
//        return sourceCPAmount;
//    }
//
//    public Double getPolarSourceControlPointAngleHint() {
//        return sourceCPAngle;
//    }

    protected double getSourceAnchorAngle(IFigure figure) {
        PrecisionPoint p1 = getSourcePosition(figure);
        PrecisionPoint p2 = getSourceControlPoint(figure);
        return Geometry.getAngle(p2, p1);
    }

    protected double getTargetAnchorAngle(IFigure figure) {
        PrecisionPoint p1 = getTargetPosition(figure);
        PrecisionPoint p2 = getTargetControlPoint(figure);
        return Geometry.getAngle(p2, p1);
    }

    public IArrowDecoration getArrow1() {
        return arrow1;
    }

    public IArrowDecoration getArrow2() {
        return arrow2;
    }

    public void setArrow1(IFigure figure, IArrowDecoration arrow) {
        if (arrow == this.arrow1)
            return;

        this.arrow1 = arrow;
        if (figure != null) {
            figure.revalidate();
            repaint(figure);
        }
    }

    public void setArrow2(IFigure figure, IArrowDecoration arrow) {
        if (arrow == this.arrow2)
            return;

        this.arrow2 = arrow;
        if (figure != null) {
            figure.revalidate();
            repaint(figure);
        }
    }

    @Override
    public void invalidate() {
        if (arrow1 != null)
            arrow1.invalidate();
        if (arrow2 != null)
            arrow2.invalidate();
        super.invalidate();
    }

    public void validate(IFigure figure) {
        super.validate(figure);
        if (arrow1 != null)
            arrow1.validate(figure);
        if (arrow2 != null)
            arrow2.validate(figure);
    }

    public void paint(IFigure figure, Graphics graphics) {
        super.paint(figure, graphics);
        if (arrow1 != null)
            arrow1.paint(figure, graphics);
        if (arrow2 != null)
            arrow2.paint(figure, graphics);
    }

    public Rectangle getPreferredBounds(IFigure figure) {
        Rectangle r = super.getPreferredBounds(figure);
        if (arrow1 != null)
            r = r.getUnion(arrow1.getPreferredBounds(figure));
        if (arrow2 != null)
            r = r.getUnion(arrow2.getPreferredBounds(figure));
        return r;
    }

    public PrecisionPoint getTitlePosition(IFigure figure) {
        checkValidation(figure);
        return titlePos;
    }

    private void updateArrows(IFigure figure) {
        if (arrow1 != null) {
            arrow1.setPosition(figure, getSourcePosition(figure));
            arrow1.setAngle(figure, getSourceAnchorAngle(figure));
            arrow1.reshape(figure);
        }
        if (arrow2 != null) {
            arrow2.setPosition(figure, getTargetPosition(figure));
            arrow2.setAngle(figure, getTargetAnchorAngle(figure));
            arrow2.reshape(figure);
        }
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

    protected void paintPath(IFigure figure, Graphics graphics, Path path,
            boolean fill) {
        ITextFigure tf = getTitleFigure(figure);
        if (tf != null && tf.isShowing()) {
            Rectangle bounds = figure.getBounds();
            Rectangle titleArea = tf.getBounds();
            if (titleArea.intersects(bounds)) {
                graphics.pushState();
                try {
                    paintPathAroundTitle(figure, graphics, path, fill, bounds,
                            titleArea);
                } finally {
                    graphics.popState();
                }
                return;
            }
        }
        super.paintPath(figure, graphics, path, fill);
    }

    private void paintPathAroundTitle(IFigure figure, Graphics graphics,
            Path path, boolean fill, Rectangle bounds, Rectangle titleArea) {
        // clip the top part
        int w = bounds.width;
        int h = titleArea.y - bounds.y;
        if (w > 0 && h > 0) {
            CLIP.setSize(w, h);
            CLIP.setLocation(bounds.x, bounds.y);
            paintPathWithClip(figure, graphics, path, fill, CLIP);
        }

        // clip the bottom part
        w = bounds.width;
        h = bounds.y + bounds.height - titleArea.y - titleArea.height;
        if (w > 0 && h > 0) {
            CLIP.setSize(w, h);
            CLIP.setLocation(bounds.x, titleArea.y + titleArea.height);
            paintPathWithClip(figure, graphics, path, fill, CLIP);
        }

        // clip the left part
        w = titleArea.x - bounds.x;
        h = titleArea.height;
        if (w > 0 && h > 0) {
            CLIP.setSize(w, h);
            CLIP.setLocation(bounds.x, titleArea.y);
            paintPathWithClip(figure, graphics, path, fill, CLIP);
        }

        // clip the right part
        w = bounds.x + bounds.width - titleArea.x - titleArea.width;
        if (w > 0 && h > 0) {
            CLIP.setSize(w, h);
            CLIP.setLocation(titleArea.x + titleArea.width, titleArea.y);
            paintPathWithClip(figure, graphics, path, fill, CLIP);
        }

        CLIP.setBounds(titleArea);
        int alpha = graphics.getAlpha();
        graphics.setAlpha((int) (alpha * 0.1));
        paintPathWithClip(figure, graphics, path, fill, CLIP);
    }

    protected void paintPathWithClip(IFigure figure, Graphics graphics,
            Path path, boolean fill, Rectangle clip) {
        graphics.setClip(clip);
        super.paintPath(figure, graphics, path, fill);
        graphics.restoreState();
    }

    protected ITextFigure getTitleFigure(IFigure figure) {
        if (figure instanceof ITitledFigure) {
            return ((ITitledFigure) figure).getTitle();
        }
        return null;
    }

}