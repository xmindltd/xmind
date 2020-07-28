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

import static org.xmind.ui.style.StyleUtils.createSummaryDecoration;
import static org.xmind.ui.style.StyleUtils.getColor;
import static org.xmind.ui.style.StyleUtils.getInteger;
import static org.xmind.ui.style.StyleUtils.getLineStyle;
import static org.xmind.ui.style.StyleUtils.getString;
import static org.xmind.ui.style.StyleUtils.isSameDecoration;

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.SWT;
import org.xmind.gef.draw2d.decoration.ICorneredDecoration;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.part.Decorator;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.branch.IBranchStructureExtension;
import org.xmind.ui.decorations.ISummaryDecoration;
import org.xmind.ui.internal.figures.SummaryFigure;
import org.xmind.ui.internal.mindmap.SummaryPart;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ISummaryPart;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;

public class SummaryDecorator extends Decorator {

    private static final SummaryDecorator instance = new SummaryDecorator();

    @Override
    public void deactivate(IGraphicalPart part, IFigure figure) {
        if (figure instanceof SummaryFigure) {
            SummaryFigure sf = (SummaryFigure) figure;
            sf.setSourceAnchor(null);
            sf.setTargetAnchor(null);
            sf.setConclusionAnchor(null);
        }
        super.deactivate(part, figure);
    }

    public void decorate(IGraphicalPart part, IFigure figure) {
        super.decorate(part, figure);
        IStyleSelector ss = StyleUtils.getStyleSelector(part);
        if (figure instanceof SummaryFigure) {
            decorateSummary(part, ss, (SummaryFigure) figure);
        }
    }

    private void decorateSummary(IGraphicalPart part, IStyleSelector ss,
            SummaryFigure figure) {
        ISummaryDecoration decoration = figure.getDecoration();
        String newDecId = getString(part, ss, Styles.ShapeClass,
                Styles.SUMMARY_SHAPE_ANGLE);
        if (!isSameDecoration(decoration, newDecId)) {
            decoration = createSummaryDecoration(part, newDecId);
            figure.setDecoration(decoration);
        }
        if (decoration != null) {
            String decorationId = decoration.getId();
            decoration.setAlpha(figure, 0xff);
            decoration.setLineColor(figure, getColor(part, ss,
                    Styles.LineColor, decorationId,
                    Styles.DEF_SUMMARY_LINE_COLOR));
            decoration.setLineStyle(figure, getLineStyle(part, ss,
                    decorationId, SWT.LINE_SOLID));
            decoration.setLineWidth(figure, getInteger(part, ss,
                    Styles.LineWidth, decorationId, 1));
            if (decoration instanceof ICorneredDecoration) {
                ((ICorneredDecoration) decoration).setCornerSize(figure,
                        getInteger(part, ss, Styles.ShapeCorner, decorationId,
                                10));
            }
            if (part instanceof SummaryPart) {
                ISummaryPart summary = (ISummaryPart) part;
                decorateAnchors(figure, decoration, summary);
            }
            decoration.setVisible(figure, figure.isVisible());
        }

        figure.setVisible(isSummaryVisible(part));
    }

    private void decorateAnchors(SummaryFigure figure, ISummaryDecoration dec,
            ISummaryPart summary) {
        figure.setSourceAnchor(summary.getSourceAnchor());
        figure.setTargetAnchor(summary.getTargetAnchor());
        figure.setConclusionAnchor(summary.getNodeAnchor());
        IBranchPart branch = summary.getOwnedBranch();
        if (branch != null) {
            IStructure sa = branch.getBranchPolicy().getStructure(branch);
            if (sa instanceof IBranchStructureExtension) {
                int direction = ((IBranchStructureExtension) sa)
                        .getSummaryDirection(branch, summary);
                dec.setDirection(figure, direction);
            }
        }
    }

    private boolean isSummaryVisible(IGraphicalPart part) {
        if (part instanceof SummaryPart) {
            for (IBranchPart subBranch : ((ISummaryPart) part)
                    .getEnclosingBranches()) {
                if (subBranch.getFigure().isShowing())
                    return true;
            }
        }
        return false;
    }

    public static SummaryDecorator getInstance() {
        return instance;
    }
}