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

import static org.xmind.ui.style.StyleUtils.createBoundaryDecoration;
import static org.xmind.ui.style.StyleUtils.getAlpha;
import static org.xmind.ui.style.StyleUtils.getColor;
import static org.xmind.ui.style.StyleUtils.getInteger;
import static org.xmind.ui.style.StyleUtils.getLineStyle;
import static org.xmind.ui.style.StyleUtils.getString;
import static org.xmind.ui.style.StyleUtils.getStyleSelector;
import static org.xmind.ui.style.StyleUtils.isSameDecoration;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.SWT;
import org.xmind.gef.draw2d.decoration.ICorneredDecoration;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.part.Decorator;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.decorations.IBoundaryDecoration;
import org.xmind.ui.internal.figures.BoundaryFigure;
import org.xmind.ui.mindmap.IBoundaryPart;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.style.Styles;

public class BoundaryDecorator extends Decorator {

    private static final BoundaryDecorator instance = new BoundaryDecorator();

    public void decorate(IGraphicalPart part, IFigure figure) {
        super.decorate(part, figure);
        if (figure instanceof BoundaryFigure) {
            decorateBoundary(part, (BoundaryFigure) figure);
        }
    }

    private void decorateBoundary(IGraphicalPart part, BoundaryFigure figure) {
        IBoundaryDecoration shape = figure.getDecoration();
        IStyleSelector ss = getStyleSelector(part);
        String newShapeId = getString(part, ss, Styles.ShapeClass,
                Styles.BOUNDARY_SHAPE_ROUNDEDRECT);
        if (!isSameDecoration(shape, newShapeId)) {
            shape = createBoundaryDecoration(part, newShapeId);
            figure.setDecoration(shape);
        }
        if (shape != null) {
            String decorationId = shape.getId();
            shape.setAlpha(figure, 0xff);
            int fillAlpha = getAlpha(part, ss, Styles.DEF_BOUNARY_ALPHA);
            shape.setFillAlpha(figure, fillAlpha);
            shape.setFillColor(figure, getColor(part, ss, Styles.FillColor,
                    decorationId, Styles.DEF_BOUNDARY_FILL_COLOR));
            shape.setGradient(figure, false);
            shape.setLineAlpha(figure, 0xff);
            shape.setLineColor(figure, getColor(part, ss, Styles.LineColor,
                    decorationId, Styles.DEF_BOUNDARY_LINE_COLOR));
            shape.setLineStyle(figure, getLineStyle(part, ss, decorationId,
                    SWT.LINE_DASH));
            shape.setLineWidth(figure, getInteger(part, ss, Styles.LineWidth,
                    decorationId, Styles.DEF_BOUNDARY_LINE_WIDTH));
            shape.setVisible(figure, true);
            if (shape instanceof ICorneredDecoration) {
                ((ICorneredDecoration) shape).setCornerSize(figure, getInteger(
                        part, ss, Styles.ShapeCorner, decorationId, 10));
            }
        }

        decorateVisible(part, figure);
    }

    private void decorateVisible(IGraphicalPart part, BoundaryFigure figure) {
        figure.setVisible(isBoundaryFigureVisible(part, figure));
    }

    private boolean isBoundaryFigureVisible(IGraphicalPart part,
            BoundaryFigure figure) {
        if (part instanceof IBoundaryPart) {
            IBoundaryPart boundary = (IBoundaryPart) part;
            if (boundary.getBoundary().isMasterBoundary())
                return true;
            List<IBranchPart> branches = boundary.getEnclosingBranches();
            for (IBranchPart branch : branches) {
                if (branch.getFigure().isShowing()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static BoundaryDecorator getInstance() {
        return instance;
    }
}