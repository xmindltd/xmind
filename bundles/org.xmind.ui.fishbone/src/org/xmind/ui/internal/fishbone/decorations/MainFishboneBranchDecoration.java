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
package org.xmind.ui.internal.fishbone.decorations;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.GEF;
import org.xmind.gef.draw2d.IDecoratedFigure;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.draw2d.decoration.AbstractDecoration;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.draw2d.decoration.IShapeDecoration;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.geometry.IPrecisionTransformer;
import org.xmind.gef.draw2d.geometry.PrecisionHorizontalFlipper;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.geometry.PrecisionRectangle;
import org.xmind.gef.draw2d.graphics.GradientPattern;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.ITopicDecoration;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ITopicPart;

public class MainFishboneBranchDecoration extends AbstractDecoration {

    private IBranchPart branch;

    private IPrecisionTransformer hf = new PrecisionHorizontalFlipper();

    private PrecisionPoint lineStart = null;

    private PrecisionPoint lineEnd = null;

    private PrecisionPoint tailTop = null;

    private PrecisionPoint tailBottom = null;

    public static final int TailLength = 30;

    public MainFishboneBranchDecoration(IBranchPart branch, String id) {
        super(id);
        this.branch = branch;
    }

    public void validate(IFigure figure) {
        super.validate(figure);
        if (branch.isFolded() || branch.getSubBranches().isEmpty()) {
            setVisible(figure, false);
            return;
        }

        setVisible(figure, true);

        PrecisionPoint ref = getReference(figure);
        hf.setOrigin(ref);
        int orientation = branch.getConnections().getSourceOrientation();
        hf.setEnabled(orientation == PositionConstants.WEST);

        PrecisionRectangle r = getChildrenBounds(figure);
        PrecisionRectangle b = hf.tr(getTopicBounds(ref));
        PrecisionPoint source = hf.rp(new PrecisionPoint(b.right() - 1, ref.y));
        PrecisionPoint target = hf
                .rp(new PrecisionPoint(hf.tr(r).right(), ref.y));
        double tailX = target.x
                + (source.x < target.x ? MainFishboneBranchDecoration.TailLength
                        : -MainFishboneBranchDecoration.TailLength);
        double tw = Math.max(0, Math.min(b.height / 2,
                Math.min(target.y - r.y, r.bottom() - target.y)));

        this.lineStart = source;
        this.lineEnd = target;
        this.tailTop = new PrecisionPoint(tailX, target.y - tw);
        this.tailBottom = new PrecisionPoint(tailX, target.y + tw);
    }

    private PrecisionRectangle getTopicBounds(PrecisionPoint ref) {
        ITopicPart topicPart = branch.getTopicPart();
        if (topicPart != null) {
            return new PrecisionRectangle(topicPart.getFigure().getBounds());
        }
        return new PrecisionRectangle(ref.x, ref.y, 0, 0);
    }

    private PrecisionRectangle getChildrenBounds(IFigure figure) {
        PrecisionRectangle r = null;
        ITopicPart topicPart = branch.getTopicPart();
        if (topicPart != null) {
            r = Geometry.union(r,
                    new PrecisionRectangle(topicPart.getFigure().getBounds()));
        }
        for (IBranchPart subBranch : branch.getSubBranches()) {
            r = Geometry.union(r,
                    new PrecisionRectangle(subBranch.getFigure().getBounds()));
        }
        if (r != null)
            return r;
        return new PrecisionRectangle(figure.getBounds());
    }

    private PrecisionPoint getReference(IFigure figure) {
        ITopicPart topicPart = branch.getTopicPart();
        if (topicPart != null) {
            return new PrecisionPoint(
                    ((IReferencedFigure) topicPart.getFigure()).getReference());
        }
        return new PrecisionPoint(figure.getBounds().getCenter());
    }

    public void invalidate() {
        lineStart = null;
        lineEnd = null;
        tailTop = null;
        tailBottom = null;
        super.invalidate();
    }

    protected void performPaint(IFigure figure, Graphics graphics) {
        if (lineStart == null || lineEnd == null || tailTop == null
                || tailBottom == null)
            return;

        ITopicDecoration topicDecoration = getTopicDecoration();
        if (topicDecoration == null)
            return;

        Color lineColor = topicDecoration.getLineColor();
        Color fillColor = topicDecoration.getFillColor();

        if (lineColor == null)
            return;

        int lineWidth = Math.max(1, topicDecoration.getLineWidth());
        int lineStyle = topicDecoration.getLineStyle();

        graphics.setAlpha(getAlpha());
        graphics.setLineStyle(lineStyle);
        graphics.setAntialias(SWT.ON);

        Path shape = new Path(Display.getCurrent());
        if (branch.getConnections().isTapered()) {
            graphics.setLineWidth(lineWidth);
            shape.moveTo(
                    (float) lineStart.x + (lineStart.x < lineEnd.x ? -1 : 1),
                    (float) (lineStart.y - lineWidth * 5 - 0));
            shape.lineTo(
                    (float) lineStart.x + (lineStart.x < lineEnd.x ? -1 : 1),
                    (float) (lineStart.y + lineWidth * 5 + 0));
            shape.lineTo((float) lineEnd.x,
                    (float) (lineEnd.y + lineWidth * 0.5));
            shape.lineTo((float) lineEnd.x,
                    (float) (lineEnd.y - lineWidth * 0.5));
            shape.close();
            graphics.setBackgroundColor(lineColor);
            graphics.fillPath(shape);

//            shape.moveTo(lineStart);
//            shape.lineTo(lineEnd);
//            graphics.setForegroundColor(lineColor);
//            graphics.setLineWidth(lineWidth * 5);
//            graphics.drawPath(shape);
        } else {
            shape.moveTo(lineStart);
            shape.lineTo(lineEnd);
            graphics.setForegroundColor(lineColor);
            graphics.setLineWidth(lineWidth);
            graphics.drawPath(shape);
        }
        shape.dispose();

        shape = new Path(Display.getCurrent());
        shape.moveTo(lineEnd);
        shape.lineTo(tailTop);
        shape.lineTo(tailBottom);
        shape.lineTo(lineEnd);
        shape.close();

        if (fillColor != null) {
            Pattern pattern = null;
            if (isGradient()) {
                PrecisionRectangle r = new PrecisionRectangle(tailTop,
                        tailBottom).union(lineEnd);
                pattern = createPattern(figure, getAlpha(), fillColor, r);
                if (pattern != null) {
                    graphics.pushState();
                    graphics.setBackgroundPattern(pattern);
                }
            } else {
                graphics.setAlpha(getAlpha());
                graphics.setBackgroundColor(fillColor);
            }
            graphics.fillPath(shape);
            if (pattern != null) {
                graphics.popState();
                pattern.dispose();
            }
        }

        if (lineColor != null) {
            graphics.setForegroundColor(lineColor);
            graphics.setLineWidth(lineWidth);
            graphics.setLineStyle(lineStyle);
            graphics.setAlpha(getAlpha());
            graphics.drawPath(shape);
        }

        shape.dispose();
    }

    private ITopicDecoration getTopicDecoration() {
        ITopicPart topicPart = branch.getTopicPart();
        if (topicPart != null) {
            IFigure topicFigure = topicPart.getFigure();
            if (topicFigure instanceof IDecoratedFigure) {
                IDecoration decoration = ((IDecoratedFigure) topicFigure)
                        .getDecoration();
                if (decoration instanceof ITopicDecoration)
                    return (ITopicDecoration) decoration;
            }
        }
        return null;
    }

    private Pattern createPattern(IFigure figure, int alpha, Color color,
            PrecisionRectangle r) {
        int delta = (int) (r.height * 0.4);
        Pattern p = new GradientPattern(Display.getCurrent(), //
                (float) r.x, (float) (r.y - delta), //
                (float) r.x, (float) (r.y + r.height), //
                ColorConstants.white, alpha, color, alpha);
        return p;
    }

    private boolean isGradient() {
        ITopicPart topicPart = branch.getTopicPart();
        if (topicPart != null) {
            IFigure figure = topicPart.getFigure();
            if (figure instanceof IDecoratedFigure) {
                IDecoration decoration = ((IDecoratedFigure) figure)
                        .getDecoration();
                if (decoration instanceof IShapeDecoration) {
                    return ((IShapeDecoration) decoration).isGradient();
                }
            }
        }
        return GEF.IS_PLATFORM_SUPPORT_GRADIENT;
    }

}
