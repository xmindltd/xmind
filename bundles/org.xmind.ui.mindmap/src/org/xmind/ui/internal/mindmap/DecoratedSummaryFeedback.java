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

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.SWT;
import org.xmind.gef.draw2d.IAnchor;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.branch.IBranchStructureExtension;
import org.xmind.ui.decorations.ISummaryDecoration;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ISummaryPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;

public class DecoratedSummaryFeedback extends DecoratedLineFeedback {

    private IAnchor nodeAnchor;

    public DecoratedSummaryFeedback(IGraphicalPart part) {
        super(part);
        setLineColor(ColorUtils.getColor(MindMapUI.LINE_COLOR_SELECTION));
        setLineStyle(SWT.LINE_SOLID);
        setLineWidthExpansion(MindMapUI.SELECTION_LINE_WIDTH / 2);
    }

    protected IDecoration createNewDecoration(IFigure figure,
            String decorationId) {
        return StyleUtils.createSummaryDecoration(getHost(), decorationId);
    }

    protected String getNewDecorationId() {
        return StyleUtils.getString(getHost(), StyleUtils
                .getStyleSelector(getHost()), Styles.ShapeClass,
                Styles.SUMMARY_SHAPE_ANGLE);
    }

    protected IAnchor getSourceAnchor(IGraphicalPart part) {
        if (part instanceof ISummaryPart)
            return ((ISummaryPart) part).getSourceAnchor();
        return super.getSourceAnchor(part);
    }

    protected IAnchor getTargetAnchor(IGraphicalPart part) {
        if (part instanceof ISummaryPart)
            return ((ISummaryPart) part).getTargetAnchor();
        return super.getTargetAnchor(part);
    }

    protected void updateDecoration(IFigure figure, IDecoration decoration,
            String decorationId, IStyleSelector ss) {
        super.updateDecoration(figure, decoration, decorationId, ss);
        setNodeAnchor(getNodeAnchor(), figure, decoration);
        if (getHost() instanceof ISummaryPart
                && decoration instanceof ISummaryDecoration) {
            ISummaryPart summary = (ISummaryPart) getHost();
            IBranchPart branch = summary.getOwnedBranch();
            if (branch != null) {
                IStructure sa = branch.getBranchPolicy().getStructure(branch);
                if (sa instanceof IBranchStructureExtension) {
                    int direction = ((IBranchStructureExtension) sa)
                            .getSummaryDirection(branch, summary);
                    ((ISummaryDecoration) decoration).setDirection(figure,
                            direction);
                }
            }
        }
    }

    protected void disposeOldDecoration(IFigure figure, IDecoration decoration) {
        super.disposeOldDecoration(figure, decoration);
        setNodeAnchor(null, figure, decoration);
    }

    protected IAnchor getNodeAnchor() {
        if (getHost() instanceof ISummaryPart)
            return ((ISummaryPart) getHost()).getNodeAnchor();
        return null;
    }

    protected void setNodeAnchor(IAnchor anchor, IFigure figure,
            IDecoration decoration) {
        if (anchor != this.nodeAnchor) {
            if (this.nodeAnchor != null) {
                unhookAnchor(this.nodeAnchor);
            }
            this.nodeAnchor = anchor;
            if (anchor != null) {
                hookAnchor(anchor);
            }
            figure.revalidate();
            figure.repaint();
        }
        if (decoration instanceof ISummaryDecoration) {
            ((ISummaryDecoration) decoration).setNodeAnchor(figure, anchor);
        }
    }
}