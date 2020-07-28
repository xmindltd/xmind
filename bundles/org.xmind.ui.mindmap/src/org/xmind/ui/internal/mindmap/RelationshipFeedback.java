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
package org.xmind.ui.internal.mindmap;

import static org.xmind.ui.mindmap.MindMapUI.SOURCE_ANCHOR;
import static org.xmind.ui.mindmap.MindMapUI.SOURCE_CONTROL_POINT;
import static org.xmind.ui.mindmap.MindMapUI.TARGET_ANCHOR;
import static org.xmind.ui.mindmap.MindMapUI.TARGET_CONTROL_POINT;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.xmind.gef.draw2d.IDecoratedFigure;
import org.xmind.gef.draw2d.decoration.IConnectionDecoration;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.service.CompositeFeedback;
import org.xmind.gef.service.IPointProvider;
import org.xmind.gef.service.IPositionProvider;
import org.xmind.gef.service.LineFeedback;
import org.xmind.gef.service.PointFeedback;
import org.xmind.ui.decorations.IRelationshipDecoration;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;

public class RelationshipFeedback extends CompositeFeedback {

    private IFigure figure;

    private IGraphicalPart part;

    private PointFeedback sourceAnchor;

    private PointFeedback targetAnchor;

    private PointFeedback sourceControlPoint;

    private PointFeedback targetControlPoint;

    private LineFeedback sourceLine;

    private LineFeedback targetLine;

    public RelationshipFeedback(IFigure figure) {
        this.figure = figure;
        init();
    }

    public RelationshipFeedback(IGraphicalPart part) {
        this.part = part;
        init();
    }

    private void init() {
        sourceAnchor = createAnchorPointFeedback();
        targetAnchor = createAnchorPointFeedback();
        sourceControlPoint = createControlPointFeedback();
        targetControlPoint = createControlPointFeedback();
        sourceLine = createLineFeedback();
        targetLine = createLineFeedback();

        sourceAnchor.setPositionProvider(new IPointProvider() {
            public Point getPoint() {
                return getAnchorPoint(true);
            }
        });
        targetAnchor.setPositionProvider(new IPointProvider() {
            public Point getPoint() {
                return getAnchorPoint(false);
            }
        });
        sourceControlPoint.setPositionProvider(new IPointProvider() {
            public Point getPoint() {
                return getControlPoint(true);
            }
        });
        targetControlPoint.setPositionProvider(new IPointProvider() {
            public Point getPoint() {
                return getControlPoint(false);
            }
        });
        sourceLine.setPositionProvider(new IPositionProvider() {
            public Point getTargetPosition() {
                return getAnchorPoint(true);
            }

            public Point getSourcePosition() {
                return getControlPoint(true);
            }
        });
        targetLine.setPositionProvider(new IPositionProvider() {
            public Point getTargetPosition() {
                return getAnchorPoint(false);
            }

            public Point getSourcePosition() {
                return getControlPoint(false);
            }
        });

        addFeedback(sourceLine);
        addFeedback(targetLine);
        addFeedback(sourceAnchor);
        addFeedback(targetAnchor);
        addFeedback(sourceControlPoint);
        addFeedback(targetControlPoint);
    }

    private Point getAnchorPoint(boolean sourceOrTarget) {
        IFigure fig = getFigure();
        if (fig != null)
            return getAnchorPosition(sourceOrTarget, fig);
        return null;
    }

    private Point getControlPoint(boolean sourceOrTarget) {
        IFigure fig = getFigure();
        if (fig != null)
            return getControlPointPosition(sourceOrTarget, fig);
        return null;
    }

    private static PointFeedback createAnchorPointFeedback() {
        PointFeedback feedback = new PointFeedback(PointFeedback.DOT);
        feedback.setHeight(MindMapUI.HEIGHT_DOT);
        feedback.setFillColor(ColorUtils.getColor("#e08000")); //$NON-NLS-1$
        feedback.setBorderColor(ColorUtils.getColor("#c04000")); //$NON-NLS-1$
        return feedback;
    }

    private static PointFeedback createControlPointFeedback() {
        PointFeedback feedback = new PointFeedback(PointFeedback.DIAMOND);
        feedback.setHeight(MindMapUI.HEIGHT_DIAMOND);
        feedback.setFillColor(ColorUtils.getColor("#f0f000")); //$NON-NLS-1$
        feedback.setBorderColor(ColorUtils.getColor("#b0b0b0")); //$NON-NLS-1$
        return feedback;
    }

    private static LineFeedback createLineFeedback() {
        LineFeedback feedback = new LineFeedback();
        feedback.setColor(ColorUtils.getColor("#b0b0b0")); //$NON-NLS-1$
        return feedback;
    }

    public int getPointId(Point p) {
        if (sourceAnchor.containsPoint(p))
            return SOURCE_ANCHOR;
        if (targetAnchor.containsPoint(p))
            return TARGET_ANCHOR;
        if (sourceControlPoint.containsPoint(p))
            return SOURCE_CONTROL_POINT;
        if (targetControlPoint.containsPoint(p))
            return TARGET_CONTROL_POINT;
        return 0;
    }

    private IFigure getFigure() {
        if (figure == null) {
            if (part != null) {
                figure = part.getFigure();
            }
        }
        return figure;
    }

    public void setAlpha(int alpha) {
        sourceAnchor.setAlpha(alpha);
        targetAnchor.setAlpha(alpha);
        sourceControlPoint.setAlpha(alpha);
        targetControlPoint.setAlpha(alpha);
        sourceLine.setAlpha(alpha);
        targetLine.setAlpha(alpha);
    }

    public static Point getAnchorPosition(boolean sourceOrTarget, IFigure figure) {
        if (figure instanceof IDecoratedFigure) {
            IDecoration decoration = ((IDecoratedFigure) figure)
                    .getDecoration();
            if (decoration instanceof IConnectionDecoration) {
                IConnectionDecoration connection = (IConnectionDecoration) decoration;
                if (sourceOrTarget) {
                    PrecisionPoint p = connection.getSourcePosition(figure);
                    if (p != null)
                        return p.toRoundedDraw2DPoint();
                } else {
                    PrecisionPoint p = connection.getTargetPosition(figure);
                    if (p != null)
                        return p.toRoundedDraw2DPoint();
                }
            }
        }
        return null;
    }

    public static Point getControlPointPosition(boolean sourceOrTarget,
            IFigure figure) {
        if (figure instanceof IDecoratedFigure) {
            IDecoration decoration = ((IDecoratedFigure) figure)
                    .getDecoration();
            if (decoration instanceof IRelationshipDecoration) {
                if (sourceOrTarget) {
                    PrecisionPoint p = ((IRelationshipDecoration) decoration)
                            .getSourceControlPoint(figure);
                    if (p != null)
                        return p.toRoundedDraw2DPoint();
                } else {
                    PrecisionPoint p = ((IRelationshipDecoration) decoration)
                            .getTargetControlPoint(figure);
                    if (p != null)
                        return p.toRoundedDraw2DPoint();
                }
            }
        }
        return null;
    }

}