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

import static org.xmind.ui.util.MindMapUtils.findBranch;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.graphics.Cursor;
import org.xmind.core.Core;
import org.xmind.core.ISheet;
import org.xmind.core.ISummary;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.gef.GEF;
import org.xmind.gef.IViewer;
import org.xmind.gef.draw2d.AbstractAnchor;
import org.xmind.gef.draw2d.IAnchor;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IPartListener;
import org.xmind.gef.part.IPartListener2;
import org.xmind.gef.part.IRequestHandler;
import org.xmind.gef.part.PartEvent;
import org.xmind.gef.policy.NullEditPolicy;
import org.xmind.gef.service.IBendPointsFeedback;
import org.xmind.gef.service.IFeedback;
import org.xmind.gef.util.GEFUtils;
import org.xmind.ui.internal.decorators.SummaryDecorator;
import org.xmind.ui.internal.figures.SummaryFigure;
import org.xmind.ui.internal.graphicalpolicies.SummaryGraphicalPolicy;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.INodePart;
import org.xmind.ui.mindmap.IRangeListener;
import org.xmind.ui.mindmap.ISelectionFeedbackHelper;
import org.xmind.ui.mindmap.ISummaryPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.mindmap.RangeEvent;
import org.xmind.ui.util.MindMapUtils;

public class SummaryPart extends MindMapPartBase
        implements FigureListener, ISummaryPart {

    private static class SummarySourceAnchor extends AbstractAnchor {

        private SummarySourceAnchor(IFigure owner) {
            super(owner);
        }

        public PrecisionPoint getLocation(double x, double y,
                double expansion) {
            return Geometry.getChopBoxLocation(x, y, getOwner().getBounds(),
                    expansion);
        }

        @Override
        public PrecisionPoint getLocation(int orientation, double expansion) {
            IFigure owner = getOwner();
            Rectangle r = owner.getBounds();

            if (orientation == PositionConstants.NORTH) {
                return new PrecisionPoint(r.getTopLeft()).translate(0,
                        -expansion);
            } else if (orientation == PositionConstants.SOUTH) {
                return new PrecisionPoint(r.getBottomRight()).translate(0,
                        expansion);
            } else if (orientation == PositionConstants.WEST) {
                return new PrecisionPoint(r.getBottomLeft())
                        .translate(-expansion, 0);
            }
            return new PrecisionPoint(r.getTopRight()).translate(expansion, 0);
        }

    }

    private static class SummaryTargetAnchor extends AbstractAnchor {
        private SummaryTargetAnchor(IFigure owner) {
            super(owner);
        }

        public PrecisionPoint getLocation(double x, double y,
                double expansion) {
            return Geometry.getChopBoxLocation(x, y, getOwner().getBounds(),
                    expansion);
        }

        @Override
        public PrecisionPoint getLocation(int orientation, double expansion) {
            Rectangle r = getOwner().getBounds();
            if (orientation == PositionConstants.NORTH) {
                return new PrecisionPoint(r.getTopRight()).translate(0,
                        -expansion);
            } else if (orientation == PositionConstants.SOUTH) {
                return new PrecisionPoint(r.getBottomLeft()).translate(0,
                        expansion);
            } else if (orientation == PositionConstants.WEST) {
                return new PrecisionPoint(r.getTopLeft()).translate(-expansion,
                        0);
            }
            return new PrecisionPoint(r.getBottomRight()).translate(expansion,
                    0);
        }
    }

    private IAnchor sourceAnchor = null;

    private IAnchor targetAnchor = null;

    private IFigure enclosure = null;

    private INodePart node = null;

    private List<IBranchPart> enclosingBranches = null;

    private List<IRangeListener> rangeListeners = null;

    private IPartListener parentListener = new IPartListener2() {

        public void childAdding(PartEvent event) {
        }

        public void childRemoving(PartEvent event) {
        }

        public void childRemoved(PartEvent event) {
            if (event.child instanceof IBranchPart) {
                IBranchPart branch = (IBranchPart) event.child;
                refreshEnclosure();
                if (branch.getTopicPart() == getNode())
                    setNode(null);
            }
        }

        public void childAdded(PartEvent event) {
            if (event.child instanceof IBranchPart) {
                IBranchPart branch = (IBranchPart) event.child;
                String topicType = branch.getTopic().getType();
                if (ITopic.ATTACHED.equals(topicType)) {
                    refreshEnclosure();
                } else if (ITopic.SUMMARY.equals(topicType)) {
                    if (MindMapUtils.equals(getSummary().getTopicId(),
                            branch.getTopic().getId())) {
                        setNode((INodePart) branch.getTopicPart());
                    }
                }
            }
        }

    };

//    private IStatusListener nodeStatusListener = new IStatusListener() {
//        public void statusChanged(StatusEvent event) {
//            if (event.key == GEF.PART_SELECTED
//                    || event.key == GEF.PART_PRESELECTED) {
//                getStatus().setStatus(event.key, event.newValue);
//            }
//        }
//    };

    private ISelectionChangedListener viewerListener = new ISelectionChangedListener() {
        public void selectionChanged(SelectionChangedEvent event) {
            syncStatus(isNodeSingleSelected());
        }

        private boolean isNodeSingleSelected() {
            IViewer viewer = getSite().getViewer();
            if (viewer == null)
                return false;

            ISelection selection = viewer.getSelection();
            if (!selection.isEmpty()
                    && !(selection instanceof IStructuredSelection))
                return false;

            IStructuredSelection ss = (IStructuredSelection) selection;
            if (ss.size() == 1
                    && getSummaryTopic().equals(ss.getFirstElement()))
                return true;

            if (!ss.isEmpty() && !(ss.size() == 1
                    && ss.getFirstElement() instanceof ISheet))
                return false;

            return getSummaryTopic().equals(viewer.getPreselected());
        }
    };

    private boolean cursorOverHandle = false;

    public SummaryPart() {
        setDecorator(SummaryDecorator.getInstance());
        setGraphicalPolicy(SummaryGraphicalPolicy.getInstance());
    }

    protected IFigure createFigure() {
        return new SummaryFigure();
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.ui.internal.parts.ISummaryPart#getSummary()
     */
    public ISummary getSummary() {
        if (getModel() instanceof SummaryViewerModel)
            return ((SummaryViewerModel) getModel()).getSummary();
        return null;
//        return (ISummary) getRealModel();
    }

    public ITopic getSummaryTopic() {
        return (ITopic) getRealModel();
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.ui.internal.parts.ISummaryPart#getOwnedBranch()
     */
    public IBranchPart getOwnedBranch() {
        if (getParent() instanceof IBranchPart)
            return (IBranchPart) getParent();
        return null;
    }

    public void setParent(IPart parent) {
        if (getParent() instanceof BranchPart) {
            getParent().removePartListener(parentListener);
            ((BranchPart) getParent()).removeSummary(this);
        }
        super.setParent(parent);
        if (getParent() instanceof BranchPart) {
            ((BranchPart) getParent()).addSummary(this);
            getParent().addPartListener(parentListener);
        }
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(ISummary.class))
            return getSummary();
        if (adapter.isAssignableFrom(ITopic.class))
            return getSummaryTopic();
        return super.getAdapter(adapter);
    }

    protected void declareEditPolicies(IRequestHandler reqHandler) {
        super.declareEditPolicies(reqHandler);
        reqHandler.installEditPolicy(GEF.ROLE_SELECTABLE,
                NullEditPolicy.getInstance());
        reqHandler.installEditPolicy(GEF.ROLE_DELETABLE,
                MindMapUI.POLICY_DELETABLE);
        reqHandler.installEditPolicy(GEF.ROLE_EDITABLE,
                MindMapUI.POLICY_EDITABLE);
        reqHandler.installEditPolicy(GEF.ROLE_MODIFIABLE,
                MindMapUI.POLICY_MODIFIABLE);
//        reqHandler.installEditPolicy(GEF.ROLE_CREATABLE,
//                MindMapUI.POLICY_SUMMARY_CREATABLE);
        reqHandler.installEditPolicy(GEF.ROLE_MOVABLE,
                NullEditPolicy.getInstance());
    }

    protected void registerCoreEvents(Object source,
            ICoreEventRegister register) {
        super.registerCoreEvents(source, register);

        ISummary summary = getSummary();
        register.setNextSourceFrom(summary);
        register.register(Core.StartIndex);
        register.register(Core.EndIndex);
        register.register(Core.TopicRefId);
        register.register(Core.Style);
    }

    public void handleCoreEvent(CoreEvent event) {
        String type = event.getType();
        if (Core.StartIndex.equals(type) || Core.EndIndex.equals(type)) {
            refresh();
            fireRangeChanged();
//            getOwnedBranch().getFigure().invalidate();
//            getOwnedBranch().treeUpdate(true);
        } else if (Core.TopicRefId.equals(type)) {
            setNode(findConclusionNode());
            refresh();
        } else if (Core.Style.equals(type)) {
            update();
        } else {
            super.handleCoreEvent(event);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.ui.internal.parts.ISummaryPart#getEnclosingBranches()
     */
    public List<IBranchPart> getEnclosingBranches() {
        if (enclosingBranches == null) {
            enclosingBranches = fillEnclosingBranches(
                    new ArrayList<IBranchPart>());
            for (IBranchPart subBranch : enclosingBranches) {
                subBranch.getFigure().addFigureListener(this);
            }
        }
        return enclosingBranches;
    }

    private List<IBranchPart> fillEnclosingBranches(List<IBranchPart> list) {
        ISummary s = getSummary();
        List<ITopic> subtopics = s.getEnclosingTopics();
        if (!subtopics.isEmpty()) {
            IViewer viewer = getSite().getViewer();
            for (ITopic t : subtopics) {
                IBranchPart branch = findBranch(viewer.findPart(t));
                if (branch != null)
                    list.add(branch);
            }
        }
        return list;
    }

    private void flushEnclosingBranches() {
        if (enclosingBranches != null) {
            for (IBranchPart subBranch : enclosingBranches) {
                subBranch.getFigure().removeFigureListener(this);
            }
            enclosingBranches = null;
        }
    }

    /*
     * (non-Javadoc)
     * @seeorg.xmind.ui.mindmap.IBranchRangePart#encloses(org.xmind.ui.mindmap.
     * IBranchPart)
     */
    public boolean encloses(IBranchPart subbranch) {
        return getSummary().encloses(subbranch.getTopic());
    }

    public void refresh() {
        refreshEnclosure();
        super.refresh();
    }

    @Override
    public void addNotify() {
        setNode(findConclusionNode());
        super.addNotify();
        for (IBranchPart enclosingBranch : getEnclosingBranches()) {
            enclosingBranch.getFigure().revalidate();
            enclosingBranch.update();
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        setNode(null);
    }

    protected void onActivated() {
        super.onActivated();
        IViewer viewer = getSite().getViewer();
        if (viewer != null) {
            viewer.addSelectionChangedListener(viewerListener);
            viewer.addPreSelectionChangedListener(viewerListener);
        }
    }

    protected void onDeactivated() {
        IViewer viewer = getSite().getViewer();
        if (viewer != null) {
            viewer.removeSelectionChangedListener(viewerListener);
            viewer.removePreSelectionChangedListener(viewerListener);
        }
        super.onDeactivated();
    }

    private INodePart findConclusionNode() {
        ISummary s = getSummary();
        ITopic topic = s.getTopic();
        if (topic != null) {
            IPart topicPart = getSite().getViewer().findPart(topic);
            if (topicPart instanceof INodePart) {
                return (INodePart) topicPart;
            }
        }
        return null;
    }

    private void refreshEnclosure() {
        flushEnclosingBranches();
        updateEnclosure();
    }

    public void figureMoved(IFigure source) {
        updateEnclosure();
        update();
        getFigure().revalidate();
    }

    protected IFigure getEnclosure() {
        if (enclosure == null) {
            enclosure = new Figure();
        }
        return enclosure;
    }

    private void updateEnclosure() {
        Rectangle r = null;
        for (IBranchPart enclosingBranch : getEnclosingBranches()) {
            r = Geometry.union(r, enclosingBranch.getFigure().getBounds());
        }
        if (r == null)
            r = new Rectangle();
        getEnclosure().setBounds(r);
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.ui.internal.parts.ISummaryPart#getSourceAnchor()
     */
    public IAnchor getSourceAnchor() {
        if (sourceAnchor == null) {
            IFigure enclosure2 = getEnclosure();
            sourceAnchor = new SummarySourceAnchor(enclosure2);
        }
        return sourceAnchor;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.ui.internal.parts.ISummaryPart#getTargetAnchor()
     */
    public IAnchor getTargetAnchor() {
        if (targetAnchor == null) {
            IFigure enclosure2 = getEnclosure();
            targetAnchor = new SummaryTargetAnchor(enclosure2);
        }
        return targetAnchor;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.ui.internal.parts.ISummaryPart#getConclusionAnchor()
     */
    public IAnchor getNodeAnchor() {
        if (node != null) {
            return node.getTargetAnchor(this);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.ui.internal.parts.ISummaryPart#getConclusionPart()
     */
    public INodePart getNode() {
        return node;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.ui.internal.parts.ISummaryPart#setConclusionPart(org
     * .xmind.gef.part.IGraphicalPart)
     */
    public void setNode(INodePart part) {
        if (part == this.node)
            return;
//        if (this.conclusionNode != null) {
//            unhookNode(this.conclusionNode);
//        }
        this.node = part;
//        if (this.conclusionNode != null) {
//            hookNode(this.conclusionNode);
//        }
        update();
        syncStatus(false);
        if (part != null) {
            IBranchPart branch = MindMapUtils.findBranch(part);
            if (branch != null) {
                branch.treeUpdate(false);
            } else {
                part.update();
            }
        }
    }

//    private void hookNode(INodePart node) {
//        node.getStatus().addStatusListener(nodeStatusListener);
//        getStatus().setStatus(GEF.PART_SELECTED,
//                node.getStatus().isStatus(GEF.PART_SELECTED));
//        getStatus().setStatus(GEF.PART_PRESELECTED,
//                node.getStatus().isStatus(GEF.PART_PRESELECTED));
//    }
//
//    private void unhookNode(INodePart node) {
//        getStatus().dePreSelect();
//        getStatus().deSelect();
//        node.getStatus().removeStatusListener(nodeStatusListener);
//    }

    private void syncStatus(boolean nodeSingleSelected) {
        boolean oldPreselected = getStatus().isPreSelected();
        boolean preSelected = getNode() != null
                && (getNode().getStatus().isSelected() ? cursorOverHandle
                        : nodeSingleSelected
                                && getNode().getStatus().isPreSelected());
//                || (nodeSingleSelected && getNode() != null && getNode()
//                        .getStatus().isPreSelected());
        boolean selected = nodeSingleSelected && getNode() != null
                && getNode().getStatus().isSelected();
        getStatus().setStatus(GEF.PART_PRESELECTED, preSelected);
        getStatus().setStatus(GEF.PART_SELECTED, selected);
        if (oldPreselected == getStatus().isPreSelected()) {
            updateFeedback();
        }
    }

    @Override
    protected ISelectionFeedbackHelper createSelectionFeedbackHelper() {
        return new SelectionFeedbackHelper();
    }

    protected IFeedback createFeedback() {
        return new SummaryFeedback(this);
    }

    public boolean containsPoint(Point position) {
        return super.containsPoint(position) || (getFeedback() != null
                && getFeedback().containsPoint(position));
    }

    public IPart findAt(Point position) {
        IPart ret = super.findAt(position);
        if (ret == this) {
            if (getFeedback() == null
                    || !getFeedback().containsPoint(position)) {
                cursorOverHandle = false;
                return getNode();
            } else {
                cursorOverHandle = true;
            }
        } else {
            cursorOverHandle = false;
        }
        return ret;
    }

//    protected void updateFeedback() {
//        if (getFeedbackPart() instanceof SummaryFeedback) {
//            ((SummaryFeedback) getFeedbackPart())
//                    .setGrowthDirection(calcGrowthDirection());
//        }
//        super.updateFeedback();
//    }
//
//    private int calcGrowthDirection() {
//        IBranchPart branch = getOwnedBranch();
//        IStructureAlgorithm sa = branch.getBranchPolicy()
//                .getStructureAlgorithm(branch);
//        if (sa instanceof IBranchStructureAlgorithm) {
//            return ((IBranchStructureAlgorithm) sa).calcChildGrowthDirection(
//                    branch, this);
//        }
//        return PositionConstants.NONE;
//    }

    public Cursor getCursor(Point pos) {
        if (getFeedback() instanceof IBendPointsFeedback) {
            int orientation = ((IBendPointsFeedback) getFeedback())
                    .getOrientation(pos);
            if (orientation != PositionConstants.NONE)
                return GEFUtils.getPositionCursor(orientation);
        }
        return super.getCursor(pos);
    }

    public void addRangeListener(IRangeListener listener) {
        if (rangeListeners == null)
            rangeListeners = new ArrayList<IRangeListener>();
        rangeListeners.add(listener);
    }

    public void removeRangeListener(IRangeListener listener) {
        if (rangeListeners != null)
            rangeListeners.remove(listener);
    }

    protected void fireRangeChanged() {
        if (rangeListeners == null)
            return;
        RangeEvent event = new RangeEvent(this);
        for (Object o : rangeListeners.toArray()) {
            ((IRangeListener) o).rangeChanged(event);
        }
    }

}
