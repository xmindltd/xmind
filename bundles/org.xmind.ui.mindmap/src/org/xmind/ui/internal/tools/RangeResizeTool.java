/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.Request;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.gef.part.IGraphicalEditPart;
import org.xmind.gef.service.IBendPointsFeedback;
import org.xmind.gef.service.IFeedback;
import org.xmind.gef.tool.ISourceTool;
import org.xmind.ui.branch.IBranchStructureExtension;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IBranchRangePart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;

public class RangeResizeTool extends FeedbackResizeTool implements ISourceTool {

    private Set<IBranchPart> newRange;

    private IBranchPart parentBranch;

    private int minLength;

    private int maxLength;

    private IBranchRangePart source;

    public void setSource(IGraphicalEditPart source) {
        Assert.isTrue(source instanceof IBranchRangePart);
        this.source = (IBranchRangePart) source;
        parentBranch = this.source.getOwnedBranch();
        newRange = new HashSet<IBranchPart>(this.source.getEnclosingBranches());
    }

    public IGraphicalEditPart getSource() {
        return (IGraphicalEditPart) source;
    }

    public IBranchRangePart getRangeSourcePart() {
        return source;
    }

    protected Rectangle getSourceArea() {
        return getSource().getFigure().getBounds();
    }

    protected IFeedback getSourceFeedback() {
        return (IFeedback) getSource().getAdapter(IFeedback.class);
    }

    protected void initFeedback(IBendPointsFeedback feedback) {
        super.initFeedback(feedback);
        minLength = calcMinLength(getOrientation());
        maxLength = calcMaxLength(getOrientation());
        updateRange(getResultArea());
    }

    protected int calcMaxLength(int orientation) {
        Rectangle parentBounds = parentBranch.getFigure().getBounds();
        Insets insets = getRangeSourcePart().getFigure().getInsets();
        if (orientation == PositionConstants.SOUTH) {
            return parentBounds.bottom() - getInitArea().y + insets.bottom;
        } else if (orientation == PositionConstants.NORTH) {
            return getInitArea().bottom() - parentBounds.y + insets.top;
        } else if (orientation == PositionConstants.WEST) {
            return getInitArea().right() - parentBounds.x + insets.left;
        } else {
            return parentBounds.right() - getInitArea().x + insets.right;
        }
    }

    protected int calcMinLength(int orientation) {
        int length = 3;
        IBranchPart last = calcLastBranch(orientation);
        if (last != null) {
            ITopicPart topicPart = last.getTopicPart();
            if (topicPart != null) {
                if (orientation == PositionConstants.SOUTH
                        || orientation == PositionConstants.NORTH) {
                    length += topicPart.getFigure().getBounds().height;
                } else {
                    length += topicPart.getFigure().getBounds().width;
                }
            }
        }
        if (orientation == PositionConstants.SOUTH) {
            length += getRangeSourcePart().getFigure().getInsets().top;
        } else if (orientation == PositionConstants.NORTH) {
            length += getRangeSourcePart().getFigure().getInsets().bottom;
        } else if (orientation == PositionConstants.WEST) {
            length += getRangeSourcePart().getFigure().getInsets().right;
        } else {
            length += getRangeSourcePart().getFigure().getInsets().left;
        }
        return length;
    }

    private IBranchPart calcLastBranch(int orientation) {
        List<IBranchPart> enclosingBranches = getRangeSourcePart()
                .getEnclosingBranches();
        if (enclosingBranches.isEmpty())
            return null;
        IStructure structure = parentBranch.getBranchPolicy().getStructure(
                parentBranch);
        if (structure instanceof IBranchStructureExtension) {
            int direction = ((IBranchStructureExtension) structure)
                    .getRangeGrowthDirection(parentBranch, getRangeSourcePart());
            if (direction == orientation)
                return first(enclosingBranches);
            if (direction == Geometry.getOppositePosition(orientation))
                return last(enclosingBranches);
        }
        return null;
    }

    private IBranchPart first(List<IBranchPart> branches) {
        return branches.get(0);
    }

    private IBranchPart last(List<IBranchPart> branches) {
        return branches.get(branches.size() - 1);
    }

    protected void updateAreaBounds(Rectangle area, Point cursorPosition) {
        super.updateAreaBounds(area, cursorPosition);
        Rectangle resultArea = getResultArea();

        updateRange(resultArea);
        addMissingBranches();

    }

    private void updateRange(Rectangle area) {
        for (IBranchPart branch : parentBranch.getSubBranches()) {
            ITopicPart topicPart = branch.getTopicPart();
            if (topicPart != null) {
                Point pt = topicPart.getFigure().getBounds().getCenter();
                if (area.contains(pt)) {
                    newRange.add(branch);
                    topicPart.getStatus().preSelect();
                } else {
                    newRange.remove(branch);
                    topicPart.getStatus().dePreSelect();
                }
            }
        }
    }

    private void addMissingBranches() {
        List<IBranchPart> subBranches = parentBranch.getSubBranches();
        List<Integer> optedBranches = null;
        for (int i = 0; i < subBranches.size(); i++) {
            IBranchPart subBranch = subBranches.get(i);
            ITopicPart topicPart = subBranch.getTopicPart();
            if (topicPart != null) {
                boolean preSelected = topicPart.getStatus().isPreSelected();
                if (preSelected) {
                    if (optedBranches == null)
                        optedBranches = new ArrayList<Integer>();
                    optedBranches.add(i);
                }
            }
        }
        for (int j = optedBranches.get(0); j <= optedBranches.get(optedBranches
                .size() - 1); j++) {
            ITopicPart topicPart = subBranches.get(j).getTopicPart();
            if (topicPart != null) {
                boolean preSelected = topicPart.getStatus().isPreSelected();
                if (!preSelected)
                    topicPart.getStatus().preSelect();
            }
        }

    }

    protected void removeFeedback(IBendPointsFeedback feedback) {
        super.removeFeedback(feedback);
        clearRange();
    }

    public void finish() {
        ArrayList<Object> range = new ArrayList<Object>(newRange);
        super.finish();
        sendRequest(range);
    }

    private void sendRequest(List<Object> newRange) {
        IBranchRangePart rangeSource = getRangeSourcePart();
        Request request = new Request(MindMapUI.REQ_MODIFY_RANGE);
        request.setViewer(getTargetViewer());
        request.setPrimaryTarget(rangeSource);
        request.setParameter(MindMapUI.PARAM_RANGE, newRange.toArray());
        getDomain().handleRequest(request);
    }

    private void clearRange() {
        for (IBranchPart branch : parentBranch.getSubBranches()) {
            ITopicPart topicPart = branch.getTopicPart();
            if (topicPart != null) {
                topicPart.getStatus().dePreSelect();
            }
        }
        newRange = null;
    }

    protected int constrainWidth(int w) {
        if (getOrientation() == PositionConstants.EAST
                || getOrientation() == PositionConstants.WEST) {
            return Math.max(Math.min(w, maxLength), minLength);
        }
        return super.constrainWidth(w);
    }

    protected int constrainHeight(int h) {
        if (getOrientation() == PositionConstants.NORTH
                || getOrientation() == PositionConstants.SOUTH) {
            return Math.max(Math.min(h, maxLength), minLength);
        }
        return super.constrainHeight(h);
    }

}