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
package org.xmind.ui.internal.decorators;

import static org.xmind.ui.style.StyleUtils.createArrowDecoration;
import static org.xmind.ui.style.StyleUtils.createRelationshipDecoration;
import static org.xmind.ui.style.StyleUtils.getColor;
import static org.xmind.ui.style.StyleUtils.getInteger;
import static org.xmind.ui.style.StyleUtils.getLineStyle;
import static org.xmind.ui.style.StyleUtils.getString;
import static org.xmind.ui.style.StyleUtils.getStyleSelector;
import static org.xmind.ui.style.StyleUtils.isSameDecoration;

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.SWT;
import org.xmind.core.IControlPoint;
import org.xmind.core.IRelationship;
import org.xmind.gef.draw2d.IAnchor;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.part.Decorator;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.decorations.IArrowDecoration;
import org.xmind.ui.decorations.IRelationshipDecoration;
import org.xmind.ui.internal.figures.RelationshipFigure;
import org.xmind.ui.mindmap.INodePart;
import org.xmind.ui.mindmap.IRelationshipPart;
import org.xmind.ui.style.Styles;
import org.xmind.ui.util.MindMapUtils;

public class RelationshipDecorator extends Decorator {

    private static final RelationshipDecorator instance = new RelationshipDecorator();

    public void decorate(IGraphicalPart part, IFigure figure) {
        super.decorate(part, figure);
        if (figure instanceof RelationshipFigure) {
            decorateRelationship(part, (RelationshipFigure) figure);
        }
    }

    @Override
    public void deactivate(IGraphicalPart part, IFigure figure) {
        if (figure instanceof RelationshipFigure) {
            RelationshipFigure rf = (RelationshipFigure) figure;
            rf.setSourceAnchor(null);
            rf.setTargetAnchor(null);
        }
        super.deactivate(part, figure);
    }

    private void decorateRelationship(IGraphicalPart part,
            RelationshipFigure figure) {
        decorateRelationship(part, getStyleSelector(part), figure);
    }

    private void decorateRelationship(IGraphicalPart part, IStyleSelector ss,
            RelationshipFigure figure) {
        IRelationshipDecoration decoration = figure.getDecoration();
        String newId = getString(part, ss, Styles.ShapeClass,
                Styles.REL_SHAPE_STRAIGHT);
        if (!isSameDecoration(decoration, newId)) {
            decoration = createRelationshipDecoration(part, newId);
            figure.setDecoration(decoration);
        }
        if (decoration != null) {
            String decorationId = decoration.getId();
            decoration.setAlpha(figure, 0xff);
            decoration.setLineColor(figure, getColor(part, ss, Styles.LineColor,
                    decorationId, Styles.DEF_REL_LINE_COLOR));
            decoration.setLineStyle(figure,
                    getLineStyle(part, ss, decorationId, SWT.LINE_DASH));
            decoration.setLineWidth(figure,
                    getInteger(part, ss, Styles.LineWidth, decorationId, 3));

            decorateAnchors(part, figure, decoration);

            Object m = MindMapUtils.getRealModel(part);
            if (m instanceof IRelationship) {
                IRelationship r = (IRelationship) m;
                decorateControlPoints(r, figure, decoration);
            }

            decorateArrows(part, ss, figure, decoration);

            decoration.setVisible(figure,
                    decoration.getSourceAnchor() != null
                            && decoration.getTargetAnchor() != null
                            && figure.isVisible());

            decoration.reroute(figure);
            figure.setBounds(decoration.getPreferredBounds(figure));
        }
    }

    private void decorateAnchors(IGraphicalPart part, RelationshipFigure figure,
            IRelationshipDecoration decoration) {
        if (part instanceof IRelationshipPart) {
            IRelationshipPart rel = (IRelationshipPart) part;
            INodePart sourceNode = rel.getSourceNode();
            IAnchor anchor = sourceNode == null ? null
                    : sourceNode.getSourceAnchor(rel);
            figure.setSourceAnchor(anchor);
            INodePart targetNode = rel.getTargetNode();
            anchor = targetNode == null ? null
                    : targetNode.getTargetAnchor(rel);
            figure.setTargetAnchor(anchor);
            figure.setVisible(sourceNode != null
                    && sourceNode.getFigure().isShowing() && targetNode != null
                    && targetNode.getFigure().isShowing());
        }
    }

    private void decorateArrows(IGraphicalPart part, IStyleSelector ss,
            RelationshipFigure figure, IRelationshipDecoration decoration) {
        IArrowDecoration arrow1 = decoration.getArrow1();
        String newArrow1Id = getString(part, ss, Styles.ArrowBeginClass,
                Styles.ARROW_SHAPE_DOT);
        if (!isSameDecoration(arrow1, newArrow1Id)) {
            arrow1 = createArrowDecoration(part, newArrow1Id);
            decoration.setArrow1(figure, arrow1);
        }
        if (arrow1 != null) {
            decorateArrow(part, figure, decoration, arrow1);
        }

        IArrowDecoration arrow2 = decoration.getArrow2();
        String newArrow2Id = getString(part, ss, Styles.ArrowEndClass,
                Styles.ARROW_SHAPE_NORMAL);
        if (!isSameDecoration(arrow2, newArrow2Id)) {
            arrow2 = createArrowDecoration(part, newArrow2Id);
            decoration.setArrow2(figure, arrow2);
        }
        if (arrow2 != null) {
            decorateArrow(part, figure, decoration, arrow2);
        }
    }

    private void decorateArrow(IGraphicalPart part, RelationshipFigure figure,
            IRelationshipDecoration decoration, IArrowDecoration arrow) {
        arrow.setColor(figure, decoration.getLineColor());
        arrow.setWidth(figure, decoration.getLineWidth());
    }

    private void decorateControlPoints(IRelationship r,
            RelationshipFigure figure, IRelationshipDecoration decoration) {
        if (r != null) {
            IControlPoint cp0 = r.getControlPoint(0);
            decoration.setRelativeSourceControlPoint(figure,
                    MindMapUtils.toGraphicalPosition(cp0.getPosition()));
            Double angle = cp0 == null ? null : cp0.getPolarAngle();
            Double amount = cp0 == null ? null : cp0.getPolarAmount();
            decoration.setSourceControlPointHint(figure, angle, amount);

            IControlPoint cp1 = r.getControlPoint(1);
            decoration.setRelativeTargetControlPoint(figure,
                    MindMapUtils.toGraphicalPosition(cp1.getPosition()));
            angle = cp1 == null ? null : cp1.getPolarAngle();
            amount = cp1 == null ? null : cp1.getPolarAmount();
            decoration.setTargetControlPointHint(figure, angle, amount);
        }
    }

    public static RelationshipDecorator getInstance() {
        return instance;
    }
}