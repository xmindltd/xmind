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
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.draw2d.IAnchor;
import org.xmind.gef.draw2d.decoration.IShadowedDecoration;
import org.xmind.gef.draw2d.decoration.PathConnectionDecoration;
import org.xmind.gef.draw2d.geometry.PrecisionLine;
import org.xmind.gef.draw2d.geometry.PrecisionLine.LineType;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.internal.figures.TopicFigure;

public abstract class AbstractBranchConnection extends PathConnectionDecoration
        implements IBranchConnectionDecoration, IShadowedDecoration {

    private int sourceOrientation = PositionConstants.NONE;

    private int targetOrientation = PositionConstants.NONE;

    private int sourceExpansion = 0;

    private int targetExpansion = 0;

    private boolean tapered = false;

    private boolean taperedStateChanged = false;

    protected AbstractBranchConnection() {
        super();
    }

    protected AbstractBranchConnection(String id) {
        super(id);
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

    protected void reroute(IFigure figure, PrecisionPoint sourcePos,
            PrecisionPoint targetPos, boolean validating) {
        calculateTerminalPoints(figure, sourcePos, targetPos);
        calculateControlPoints(figure, sourcePos, targetPos);
    }

    protected void calculateControlPoints(IFigure figure,
            PrecisionPoint sourcePos, PrecisionPoint targetPos) {
    }

    protected void calculateTerminalPoints(IFigure figure,
            PrecisionPoint sourcePos, PrecisionPoint targetPos) {
        IAnchor sa = getSourceAnchor();
        IAnchor ta = getTargetAnchor();
        int so = getSourceOrientation();
        int to = getTargetOrientation();
        if (so == PositionConstants.NONE && to == PositionConstants.NONE) {
            if (sa != null) {
                if (ta != null) {
                    sourcePos.setLocation(sa.getLocation(ta.getReferencePoint(),
                            getSourceExpansion()));
                } else {
                    sourcePos.setLocation(
                            sa.getLocation(0, 0, getSourceExpansion()));
                }
            }
            if (ta != null) {
                if (sa != null) {
                    targetPos.setLocation(ta.getLocation(sa.getReferencePoint(),
                            getTargetExpansion()));
                } else {
                    targetPos.setLocation(
                            ta.getLocation(0, 0, getTargetExpansion()));
                }
            }
        } else if (so == PositionConstants.NONE) {
            if (ta != null) {
                targetPos.setLocation(ta.getLocation(to, getTargetExpansion()));
            }
            if (sa != null) {
                sourcePos.setLocation(
                        sa.getLocation(targetPos, getSourceExpansion()));
//                sourcePos.setLocation(sa.getOwner().getBounds().getCenter());
            }
        } else if (to == PositionConstants.NONE) {
            if (sa != null) {
                sourcePos.setLocation(sa.getLocation(so, getSourceExpansion()));
            }
            if (ta != null) {
                targetPos.setLocation(
                        ta.getLocation(sourcePos, getTargetExpansion()));
            }
        } else {
            if (sa != null) {
                sourcePos.setLocation(sa.getLocation(so, getSourceExpansion()));
            }
            if (ta != null) {
                targetPos.setLocation(ta.getLocation(to, getTargetExpansion()));
            }
        }
    }

    protected void calculateSourceControlPoints(PrecisionPoint c,
            PrecisionPoint s1, PrecisionPoint s2, PrecisionPoint c2) {
        PrecisionLine line = new PrecisionLine(c, s1, LineType.Ray);
        double length = s1.getDistance(s2);
        double angle = line.getAngle();
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
        c2.setLocation(
                s1.getTranslated(length * cos * 0.5f, length * sin * 0.5f));
    }

    public int getSourceOrientation() {
        return sourceOrientation;
    }

    public int getTargetOrientation() {
        return targetOrientation;
    }

    public void setSourceOrientation(IFigure figure, int orientation) {
        if (orientation == this.sourceOrientation)
            return;

        this.sourceOrientation = orientation;
        if (figure != null) {
            figure.revalidate();
            repaint(figure);
        }
        invalidate();
    }

    public void setTargetOrientation(IFigure figure, int orientation) {
        if (orientation == this.targetOrientation)
            return;

        this.targetOrientation = orientation;
        if (figure != null) {
            figure.revalidate();
            repaint(figure);
        }
        invalidate();
    }

    public int getSourceExpansion() {
        return sourceExpansion;
    }

    public int getTargetExpansion() {
        return targetExpansion;
    }

    public void setSourceExpansion(IFigure figure, int expansion) {
        if (expansion == this.sourceExpansion)
            return;

        this.sourceExpansion = expansion;
        if (figure != null) {
            figure.revalidate();
            repaint(figure);
        }
        invalidate();
    }

    public void setTargetExpansion(IFigure figure, int expansion) {
        if (expansion == this.targetExpansion)
            return;

        this.targetExpansion = expansion;
        if (figure != null) {
            figure.revalidate();
            repaint(figure);
        }
        invalidate();
    }

    public boolean isTapered() {
        return tapered;
    }

    public void setTapered(IFigure figure, boolean tapered) {
        if (tapered == this.tapered)
            return;

        this.tapered = tapered;
        if (figure != null) {
            repaint(figure);
        }
        taperedStateChanged = true;
        invalidate();
    }

    protected boolean isPositionValid() {
        return super.isPositionValid() && !taperedStateChanged;
    }

    public void validate(IFigure figure) {
        taperedStateChanged = false;
        super.validate(figure);
    }

    protected boolean usesFill() {
        return isTapered();
    }

    protected double getThickLineWidth() {
        return getLineWidth() * 5;
    }

    protected void calcTaperedPositions(PrecisionPoint p1, PrecisionPoint p2,
            double amountFromP1ToP2, PrecisionPoint result1,
            PrecisionPoint result2) {
        calcTaperedPositions(p1, p2, amountFromP1ToP2, 5, result1, result2);
    }

    protected void calcTaperedPositions(PrecisionPoint p1, PrecisionPoint p2,
            double amountFromP1ToP2, double thickness, PrecisionPoint result1,
            PrecisionPoint result2) {
        // Initialize results
        result1.setLocation(p1);
        result2.setLocation(p2);

        // No more action needed if p1 and p2 are the same location
        if (p1.equals(p2))
            return;

        // Move results to the point by the amount between p1 and p2
        result1.move(result2, amountFromP1ToP2);
        result2.setLocation(result1);

        // Calculate the line width on that point
        double width = getLineWidth() * thickness * (1 - amountFromP1ToP2)
                + getLineWidth() * amountFromP1ToP2;

        // Calculate the horizontal and vertical distance from that 
        // point to actual positions
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        double d = Math.hypot(dx, dy);
        double e = (width / 2) / d;
        dx *= e;
        dy *= e;

        // Translate results to actual position
        result1.translate(-dy, dx);
        result2.translate(dy, -dx);
    }

//    public int getMinimumMajorSpacing(IFigure figure) {
//        return 0;
//    }

    protected void paintPath(IFigure figure, Graphics graphics, Path path,
            boolean fill) {
        TopicFigure tf = getTopicFigure(figure);
        if (tf != null && (tf.getDecoration().getLineWidth() > 0
                || tf.getDecoration().getFillColor() != null)) {
            Rectangle bounds = figure.getBounds();
            Path shape = new Path(Display.getCurrent());
            ITopicDecoration decoration = tf.getDecoration();

            Rectangle rectangleClip = graphics.getClip(new Rectangle());
            //When get clip from the graphics, there will be a conversion from float to int, with lossing of accuracy.
            //So expand 1 pixel first.
            rectangleClip.expand(new Insets(1));

            shape.addRectangle(new Rectangle(bounds).intersect(rectangleClip));
            shape.addPath(decoration.createClippingPath(tf));

            graphics.pushState();
            try {
                graphics.setClip(shape);
                super.paintPath(figure, graphics, path, fill);
                graphics.restoreState();
            } finally {
                graphics.popState();
                shape.close();
                shape.dispose();
            }
            return;
        }
        super.paintPath(figure, graphics, path, fill);
    };

    private TopicFigure getTopicFigure(IFigure parent) {
        for (Object obj : parent.getChildren()) {
            if (obj instanceof TopicFigure)
                return (TopicFigure) obj;
        }
        return null;
    }

}
