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

import static org.xmind.ui.style.StyleUtils.createBranchConnection;
import static org.xmind.ui.style.StyleUtils.createBranchDecoration;
import static org.xmind.ui.style.StyleUtils.getBranchConnectionColor;
import static org.xmind.ui.style.StyleUtils.getColor;
import static org.xmind.ui.style.StyleUtils.getInteger;
import static org.xmind.ui.style.StyleUtils.getLineStyle;
import static org.xmind.ui.style.StyleUtils.getString;
import static org.xmind.ui.style.StyleUtils.getStyleSelector;
import static org.xmind.ui.style.StyleUtils.isBranchLineTapered;
import static org.xmind.ui.style.StyleUtils.isSameDecoration;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.SWT;
import org.xmind.core.Core;
import org.xmind.core.ISummary;
import org.xmind.gef.draw2d.IAnchor;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.part.Decorator;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.branch.IBranchStructureExtension;
import org.xmind.ui.decorations.IBranchConnectionDecoration;
import org.xmind.ui.decorations.IBranchConnections;
import org.xmind.ui.internal.figures.BranchFigure;
import org.xmind.ui.mindmap.IAnimatablePart;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.INodePart;
import org.xmind.ui.mindmap.IPlusMinusPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;
import org.xmind.ui.util.MindMapUtils;

public class BranchDecorator extends Decorator {

    private static final BranchDecorator instanceo = new BranchDecorator();

    public void activate(IGraphicalPart part, IFigure figure) {
        super.activate(part, figure);
        if (part instanceof IBranchPart && figure instanceof BranchFigure) {
            IBranchPart branch = (IBranchPart) part;
            BranchFigure branchFigure = (BranchFigure) figure;
            branchFigure.setConnections(branch.getConnections());
        }
    }

    public void decorate(IGraphicalPart part, IFigure figure) {
        if (figure instanceof BranchFigure) {
            BranchFigure branchFigure = (BranchFigure) figure;
            if (part instanceof IBranchPart) {
                IBranchPart branch = (IBranchPart) part;
                branchFigure.setFolded(isBranchFolded(branch));
                boolean animating = branch instanceof IAnimatablePart
                        && ((IAnimatablePart) branch).isFigureAnimating();
                IBranchPart parent = branch.getParentBranch();
                if (parent != null) {
                    IFigure parentFigure = parent.getFigure();
                    if (parentFigure instanceof BranchFigure) {
                        BranchFigure parentBranchFigure = (BranchFigure) parentFigure;
                        branchFigure.setMinimized(parentBranchFigure.isFolded()
                                || parentBranchFigure.isMinimized()
                                || isUnusedSummaryBranch(branch));
                        if (!animating) {
                            branchFigure.setVisible(!parentBranchFigure
                                    .isFolded()
                                    && parentBranchFigure.isVisible());
                        }
                    }
                }
                decorateBranchDecoration(part, getStyleSelector(part),
                        branchFigure, animating);
            }
        }
    }

    private boolean isUnusedSummaryBranch(IBranchPart branch) {
        IBranchPart parent = branch.getParentBranch();
        if (parent != null && parent.getSummaryBranches().contains(branch)) {
            String topicId = branch.getTopic().getId();
            for (ISummary s : parent.getTopic().getSummaries()) {
                if (topicId.equals(s.getTopicId()))
                    return false;
            }
            return true;
        }
        return false;
    }

    protected void decorateBranchDecoration(IGraphicalPart part,
            IStyleSelector ss, BranchFigure branchFigure, boolean animating) {
        IDecoration decoration = branchFigure.getDecoration();
        String newDescId = getString(part, ss, Styles.BranchDecorationClass,
                Styles.DEF_BRANCH_DECORATION);
        if (!isSameDecoration(decoration, newDescId)) {
            decoration = createBranchDecoration(part, newDescId);
            branchFigure.setDecoration(decoration);
        }
        if (decoration != null) {
            decoration.setAlpha(branchFigure, 0xff);
            if (!animating) {
                decoration.setVisible(branchFigure, branchFigure.isVisible());
            }
        }
    }

    private boolean isBranchFolded(IBranchPart branch) {
        if (!branch.isPropertyModifiable(Core.TopicFolded))
            return false;
        Object cache = MindMapUtils.getCache(branch, IBranchPart.CACHE_FOLDED);
        if (cache != null)
            return Boolean.TRUE.equals(cache);
        return branch.getTopic().isFolded();
    }

    public void decorateChildren(IGraphicalPart part, IFigure figure) {
        super.decorateChildren(part, figure);
        if (figure instanceof BranchFigure) {
            BranchFigure branchFigure = (BranchFigure) figure;
            if (part instanceof IBranchPart) {
                IBranchPart branch = (IBranchPart) part;
                branchFigure.setFolded(isBranchFolded(branch));
                boolean animating = branch instanceof IAnimatablePart
                        && ((IAnimatablePart) branch).isFigureAnimating();
                IBranchPart parent = branch.getParentBranch();
                if (parent != null) {
                    IFigure parentFigure = parent.getFigure();
                    if (parentFigure instanceof BranchFigure) {
                        BranchFigure parentBranchFigure = (BranchFigure) parentFigure;
                        branchFigure.setMinimized(parentBranchFigure.isFolded()
                                || parentBranchFigure.isMinimized()
                                || isUnusedSummaryBranch(branch));
                        if (!animating) {
                            branchFigure.setVisible(!parentBranchFigure
                                    .isFolded()
                                    && parentBranchFigure.isVisible());
                        }
                    }
                }
                IStyleSelector ss = getStyleSelector(part);
                decorateConnections(branch, ss, branchFigure,
                        branch.getConnections(), animating);
            }
        }
    }

    private void decorateConnections(IBranchPart branch, IStyleSelector ss,
            BranchFigure figure, IBranchConnections connections,
            boolean ignoreVisibility) {
        String newConnectionId = getString(branch, ss, Styles.LineClass,
                Styles.BRANCH_CONN_STRAIGHT);
        connections.setId(newConnectionId);

        IAnchor sourceAnchor = null;
        ITopicPart topic = branch.getTopicPart();
        if (topic instanceof INodePart) {
            sourceAnchor = ((INodePart) topic).getSourceAnchor(branch);
        }
        connections.setSourceAnchor(figure, sourceAnchor);

        connections.setAlpha(figure, 0xff);
        connections.setLineColor(
                figure,
                getColor(branch, ss, Styles.LineColor, newConnectionId,
                        Styles.DEF_TOPIC_LINE_COLOR));
        connections.setLineStyle(figure,
                getLineStyle(branch, ss, newConnectionId, SWT.LINE_SOLID));
        connections.setLineWidth(figure,
                getInteger(branch, ss, Styles.LineWidth, newConnectionId, 1));

        int sourceOrientation = PositionConstants.NONE;
        IStructure structure = branch.getBranchPolicy().getStructure(branch);
        if (structure instanceof IBranchStructureExtension) {
            sourceOrientation = ((IBranchStructureExtension) structure)
                    .getSourceOrientation(branch);
        }
        connections.setSourceOrientation(figure, sourceOrientation);

        int sourceExpansion = getSourceExpansion(branch, ss, sourceOrientation,
                connections.getLineWidth(), connections.getId());
        connections.setSourceExpansion(figure, sourceExpansion);

        connections.setTapered(figure, isBranchLineTapered(branch, ss));
        connections.setCornerSize(figure,
                getInteger(branch, ss, Styles.LineCorner, newConnectionId, 5));

        List<IBranchPart> subBranches = branch.getSubBranches();
        for (int i = 0; i < subBranches.size(); i++) {
            IBranchPart subBranch = subBranches.get(i);
            IDecoration connection = connections.getDecoration(i);
            if (!isSameDecoration(connection, newConnectionId)) {
                connection = createBranchConnection(branch, newConnectionId);
                connections.setDecoration(figure, i, connection);
            }
            if (connection != null
                    && connection instanceof IBranchConnectionDecoration) {
                decorateConnection(branch, ss, figure, subBranch,
                        (IBranchConnectionDecoration) connection, i,
                        connections, ignoreVisibility);
            }
        }
    }

    private int getSourceExpansion(IBranchPart branch, IStyleSelector ss,
            int sourceOrientation, int lineWidth, String decorationId) {
        if (sourceOrientation == PositionConstants.NONE)
            return 0;

        IPlusMinusPart plusMinus = branch.getPlusMinus();
        if (plusMinus != null && plusMinus.getFigure().isVisible()) {
            Dimension size = plusMinus.getFigure().getPreferredSize();
            return Math.max(size.width, size.height) + lineWidth + 1;
        }

        int spacing = StyleUtils.getInteger(branch, ss, Styles.MajorSpacing,
                decorationId, -1);
        if (spacing >= 0)
            return spacing / 2;

        return Styles.DEFAULT_EXPANSION + lineWidth;
    }

    private void decorateConnection(IBranchPart branch, IStyleSelector ss,
            BranchFigure figure, IBranchPart subBranch,
            IBranchConnectionDecoration connection, int subBranchIndex,
            IBranchConnections connections, boolean ignoreVisibility) {
        IAnchor targetAnchor = null;
        ITopicPart subTopic = subBranch.getTopicPart();
        if (subTopic instanceof INodePart) {
            targetAnchor = ((INodePart) subTopic).getTargetAnchor(branch);
        }
        connection.setTargetAnchor(figure, targetAnchor);

        int targetOrientation = PositionConstants.NONE;
        IStructure structure = branch.getBranchPolicy().getStructure(branch);
        if (structure instanceof IBranchStructureExtension) {
            targetOrientation = ((IBranchStructureExtension) structure)
                    .getChildTargetOrientation(branch, subBranch);
        }
        connection.setTargetOrientation(figure, targetOrientation);
        connection.setTargetExpansion(figure, 0);
        connection.setLineColor(
                figure,
                getBranchConnectionColor(branch, ss, subBranch, subBranchIndex,
                        connections.getLineColor()));

        if (!ignoreVisibility) {
            connection.setVisible(
                    figure,
                    connection.getSourceAnchor() != null
                            && connection.getTargetAnchor() != null
                            && figure.isVisible() && !figure.isFolded());
        }
    }

//    private IBranchStructure getStructureAlgorithm(IBranchPart branch) {
//        IBranchPolicy policy = branch.getBranchPolicy();
//        if (policy == null)
//            return null;
//        IStructure sa = policy.getStructure(branch);
//        if (sa instanceof IBranchStructure)
//            return (IBranchStructure) sa;
//        return null;
//    }

    public static BranchDecorator getInstanceo() {
        return instanceo;
    }
}