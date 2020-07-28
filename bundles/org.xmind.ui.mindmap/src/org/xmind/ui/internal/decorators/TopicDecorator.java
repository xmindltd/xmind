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

import static org.xmind.ui.style.StyleUtils.createTopicDecoration;
import static org.xmind.ui.style.StyleUtils.getColor;
import static org.xmind.ui.style.StyleUtils.getInteger;
import static org.xmind.ui.style.StyleUtils.getLineStyle;
import static org.xmind.ui.style.StyleUtils.getString;
import static org.xmind.ui.style.StyleUtils.getStyleSelector;
import static org.xmind.ui.style.StyleUtils.isSameDecoration;

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.xmind.gef.draw2d.decoration.ICorneredDecoration;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.part.Decorator;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.ui.decorations.ICalloutTopicDecoration;
import org.xmind.ui.decorations.ITopicDecoration;
import org.xmind.ui.internal.figures.TopicFigure;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ISheetPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;
import org.xmind.ui.util.MindMapUtils;

public class TopicDecorator extends Decorator {

    private static final TopicDecorator instance = new TopicDecorator();

    public void decorate(IGraphicalPart part, IFigure figure) {
        super.decorate(part, figure);
        if (figure instanceof TopicFigure) {
            IGraphicalPart branch = MindMapUtils.findBranch(part);
            if (branch != null)
                part = branch;
            decorateTopic(part, getStyleSelector(part), (TopicFigure) figure);
        }
    }

    private void decorateTopic(IGraphicalPart part, IStyleSelector ss,
            TopicFigure figure) {
        ITopicDecoration shape = figure.getDecoration();

        String defaultShapeId = Styles.TOPIC_SHAPE_ROUNDEDRECT;
        String shapeKey = Styles.ShapeClass;

        if (part instanceof IBranchPart) {
            String branchType = ((IBranchPart) part).getBranchType();
            if (MindMapUI.BRANCH_CALLOUT.equals(branchType)) {
                defaultShapeId = Styles.CALLOUT_TOPIC_SHAPE_BALLOON_ELLIPSE;
                shapeKey = Styles.CalloutShapeClass;
            }
        }
        String newShapeId = getString(part, ss, shapeKey, defaultShapeId);

        if (!isSameDecoration(shape, newShapeId)) {
            shape = createTopicDecoration(part, newShapeId);
            figure.setDecoration(shape);
        }
        if (shape != null) {
            String decorationId = shape.getId();
            shape.setAlpha(figure, 0xff);
            shape.setFillAlpha(figure, 0xff);
            shape.setLineAlpha(figure, 0xff);
            Color fillColor = getColor(part, ss, Styles.FillColor, decorationId,
                    Styles.DEF_TOPIC_FILL_COLOR);
            shape.setFillColor(figure, fillColor);

            shape.setGradient(figure, isGradientColor(part, figure, shape));

            int fontSize = 0;
            if (figure.getTitle() != null) {
                fontSize = getInteger(part, ss, Styles.FontSize, 0);
            }

            fontSize = fontSize < 56 ? fontSize : 56;

            shape.setLeftMargin(figure,
                    getInteger(part, ss, Styles.LeftMargin, decorationId, 10)
                            + (int) (fontSize * 0.5));
            shape.setRightMargin(figure,
                    getInteger(part, ss, Styles.RightMargin, decorationId, 10)
                            + (int) (fontSize * 0.5));
            shape.setTopMargin(figure,
                    getInteger(part, ss, Styles.TopMargin, decorationId, 5)
                            + (int) (fontSize * 0.1));
            shape.setBottomMargin(figure,
                    getInteger(part, ss, Styles.BottomMargin, decorationId, 5)
                            + (int) (fontSize * 0.1));

            String defaultColor = ColorUtils
                    .toString(getColor(part, ss, Styles.LineColor, decorationId,
                            Styles.DEF_TOPIC_LINE_COLOR));
            Color borderLineColor = getColor(part, ss, Styles.BorderLineColor,
                    decorationId, defaultColor);
            shape.setLineColor(figure, borderLineColor);

            int defaultWidth = getInteger(part, ss, Styles.LineWidth,
                    decorationId, 1);
            int borderLineWidth = getInteger(part, ss, Styles.BorderLineWidth,
                    decorationId, defaultWidth);
            shape.setLineWidth(figure, borderLineWidth);

            int lineStyle = getLineStyle(part, ss, decorationId,
                    SWT.LINE_SOLID);
            shape.setLineStyle(figure, lineStyle);
            shape.setVisible(figure, true);

            int cornerSize = getInteger(part, ss, Styles.ShapeCorner,
                    decorationId, 10);
            if (shape instanceof ICorneredDecoration) {
                ((ICorneredDecoration) shape).setCornerSize(figure, cornerSize);
            }

            if (shape instanceof ICalloutTopicDecoration) {
                ICalloutTopicDecoration calloutDecoration = (ICalloutTopicDecoration) shape;
                String fromLineClass = MindMapUI.getDecorationManager()
                        .getDecorationDescriptor(decorationId)
                        .getDefaultValueProvider(Styles.CalloutLineClass)
                        .getValue(part, Styles.CalloutLineClass);
                calloutDecoration.setFromLineClass(figure, getString(part, ss,
                        Styles.CalloutLineClass, fromLineClass));
                calloutDecoration.setFromLineColor(figure,
                        getColor(part, ss, Styles.CalloutLineColor,
                                decorationId,
                                ColorUtils.toString(borderLineColor)));
                String defaultFromFillColor = fillColor == null ? Styles.NONE
                        : ColorUtils.toString(fillColor);
                calloutDecoration.setFromFillColor(figure,
                        getColor(part, ss, Styles.CalloutFillColor,
                                decorationId, defaultFromFillColor));
                calloutDecoration.setFromLineStyle(figure, getInteger(part, ss,
                        Styles.CalloutLinePattern, decorationId, lineStyle));
                calloutDecoration.setFromLineWidth(figure,
                        getInteger(part, ss, Styles.CalloutLineWidth,
                                decorationId, borderLineWidth));
                calloutDecoration.setFromLineCorner(figure, getInteger(part, ss,
                        Styles.CalloutLineCorner, decorationId, cornerSize));
            }
        }

        double angle = StyleUtils.getDouble(part, ss, Styles.RotateAngle, 0);
        figure.setRotationDegrees(angle);
    }

    private boolean isGradientColor(IGraphicalPart part, TopicFigure figure,
            ITopicDecoration shape) {
        boolean isGraidentColor = false;

        IPart parentPart = part.getParent();
        while (parentPart != null && !(parentPart instanceof ISheetPart)) {
            parentPart = parentPart.getParent();
        }

        if (parentPart != null && parentPart instanceof ISheetPart) {
            String gradient = getStyleSelector((ISheetPart) parentPart)
                    .getStyleValue((ISheetPart) parentPart,
                            Styles.GradientColor);
            if (Styles.NONE.equals(gradient))
                isGraidentColor = false;
            else if (Styles.GRADIENT.equals(gradient))
                isGraidentColor = true;
        }

        return isGraidentColor;
    }

    public static TopicDecorator getInstance() {
        return instance;
    }
}