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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;
import org.xmind.core.Core;
import org.xmind.core.IBoundary;
import org.xmind.core.ISheet;
import org.xmind.core.ISummary;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.gef.GEF;
import org.xmind.gef.draw2d.IAnchor;
import org.xmind.gef.draw2d.IDecoratedFigure;
import org.xmind.gef.draw2d.decoration.IConnectionDecoration;
import org.xmind.gef.draw2d.decoration.ICorneredDecoration;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.draw2d.decoration.ILineDecoration;
import org.xmind.gef.draw2d.decoration.IShapeDecoration;
import org.xmind.gef.graphicalpolicy.IGraphicalPolicy;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.part.IGraphicalEditPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IRequestHandler;
import org.xmind.ui.branch.IBranchPolicy;
import org.xmind.ui.branch.IBranchStructureExtension;
import org.xmind.ui.branch.IBranchStyleSelector;
import org.xmind.ui.decorations.IBranchConnectionDecoration;
import org.xmind.ui.decorations.IBranchConnections;
import org.xmind.ui.decorations.IBranchConnections2;
import org.xmind.ui.decorations.ICalloutTopicDecoration;
import org.xmind.ui.internal.InfoItemContributorManager;
import org.xmind.ui.internal.ModelCacheManager;
import org.xmind.ui.internal.decorations.BranchConnections;
import org.xmind.ui.internal.decorations.CalloutBranchConnections;
import org.xmind.ui.internal.figures.BranchFigure;
import org.xmind.ui.internal.layouts.BranchLayout;
import org.xmind.ui.mindmap.IBoundaryPart;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IInfoPart;
import org.xmind.ui.mindmap.ILabelPart;
import org.xmind.ui.mindmap.IMindMapViewer;
import org.xmind.ui.mindmap.INodePart;
import org.xmind.ui.mindmap.IPlusMinusPart;
import org.xmind.ui.mindmap.ISheetPart;
import org.xmind.ui.mindmap.ISummaryPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;
import org.xmind.ui.util.MindMapUtils;

/**
 * @author MANGOSOFT
 */
public class BranchPart extends MindMapPartBase implements IBranchPart {

    private ITopicPart topicPart = null;

    private List<IBranchPart> subBranches = null;

    private IPlusMinusPart plusMinus = null;

    private List<IBoundaryPart> boundaries = null;

    private List<ISummaryPart> summaries = null;

    private List<IBranchPart> summaryBranches = null;

    private List<IBranchPart> calloutBranches = null;

    private ILabelPart label = null;

    private IInfoPart infoPart = null;

    private boolean central = false;

    private ChildSorter sorter = new ChildSorter(this);

    private BranchConnections connections = null;

    private String branchPolicyId = null;

    private int level = -1;

    private CalloutBranchConnections calloutConnections;

    private String overrideBranchType = null;

    public BranchPart() {
//        setDecorator(BranchDecorator.getInstanceo());
    }

    public ITopic getTopic() {
        return (ITopic) getRealModel();
    }

    public IBranchPart getParentBranch() {
        if (getParent() instanceof IBranchPart)
            return (IBranchPart) getParent();
        return null;
    }

    public ITopicPart getTopicPart() {
        return topicPart;
    }

    public void setTopicPart(ITopicPart topicPart) {
        this.topicPart = topicPart;
    }

    public IPlusMinusPart getPlusMinus() {
        return plusMinus;
    }

    public void setPlusMinus(IPlusMinusPart plusMinus) {
        this.plusMinus = plusMinus;
    }

    public List<IBranchPart> getSubBranches() {
        if (subBranches == null) {
            subBranches = new ArrayList<IBranchPart>();
        }
        return subBranches;
    }

    public void addSubBranch(IBranchPart subBranch) {
        getSubBranches().add(subBranch);
        sorter.sort(getSubBranches());
        int index = getSubBranches().indexOf(subBranch);
        getConnections().add(getFigure(), index, null);
    }

    public void removeSubBranch(IBranchPart subBranch) {
        int index = getSubBranches().indexOf(subBranch);
        getConnections().remove(getFigure(), index);
        getSubBranches().remove(subBranch);
    }

    public List<IBranchPart> getCalloutBranches() {
        if (calloutBranches == null)
            calloutBranches = new ArrayList<IBranchPart>();
        return calloutBranches;
    }

    public void addCalloutBranch(IBranchPart calloutBranch) {
        getCalloutBranches().add(calloutBranch);
        sorter.sort(getCalloutBranches());
        int index = getCalloutBranches().indexOf(calloutBranch);
        getCalloutConnections().add(getFigure(), index, null);
    }

    public void removeCalloutBranch(IBranchPart calloutBranch) {
        int index = getCalloutBranches().indexOf(calloutBranch);
        getCalloutConnections().remove(getFigure(), index);
        getCalloutBranches().remove(calloutBranch);
    }

    public List<ISummaryPart> getSummaries() {
        if (summaries == null) {
            summaries = new ArrayList<ISummaryPart>();
        }
        return summaries;
    }

    public void addSummary(ISummaryPart summary) {
        getSummaries().add(summary);
        sorter.sort(getSummaries());
    }

    public void removeSummary(ISummaryPart summary) {
        getSummaries().remove(summary);
    }

    public List<IBranchPart> getSummaryBranches() {
        if (summaryBranches == null)
            summaryBranches = new ArrayList<IBranchPart>();
        return summaryBranches;
    }

    public void addSummaryBranch(IBranchPart summaryBranch) {
        getSummaryBranches().add(summaryBranch);
        sorter.sort(getSummaryBranches());
    }

    public void removeSummaryBranch(IBranchPart summaryBranch) {
        getSummaryBranches().remove(summaryBranch);
    }

    public ILabelPart getLabel() {
        return label;
    }

    public void setLabel(ILabelPart label) {
        this.label = label;
    }

    public IInfoPart getInfoPart() {
        return infoPart;
    }

    public void setinfoPart(IInfoPart information) {
        this.infoPart = information;
    }

    protected IFigure createFigure() {
        BranchFigure branchFigure = new BranchFigure();
        branchFigure.setConnections(getConnections());
        branchFigure.setCalloutConnections(getCalloutConnections());
        return branchFigure;
    }

    public void setParent(IPart parent) {
        if (getParent() instanceof BranchPart) {
            BranchPart parentBranch = (BranchPart) getParent();
            parentBranch.removeSubBranch(this);
            parentBranch.removeCalloutBranch(this);
            parentBranch.removeSummaryBranch(this);
        } else if (getParent() instanceof SheetPart) {
            SheetPart sheet = (SheetPart) getParent();
            sheet.removeFloatingBranch(this);
            if (sheet.getCentralBranch() == this)
                sheet.setCentralBranch(null);
        }
        super.setParent(parent);
        central = getTopic()
                .equals(getSite().getViewer().getAdapter(ITopic.class));
        if (getParent() instanceof BranchPart) {
            BranchPart parentBranch = (BranchPart) getParent();
            level = parentBranch.getLevel() + 1;
            if (ITopic.ATTACHED.equals(getTopic().getType())) {
                parentBranch.addSubBranch(this);
            } else if (ITopic.SUMMARY.equals(getTopic().getType())) {
                parentBranch.addSummaryBranch(this);
            } else if (ITopic.CALLOUT.equals(getTopic().getType())) {
                parentBranch.addCalloutBranch(this);
            }
        } else if (getParent() instanceof SheetPart) {
            SheetPart sheet = (SheetPart) getParent();
            if (getTopic().equals(sheet.getCentralTopic())) {
                level = 0;
                sheet.setCentralBranch(this);
            } else if (ITopic.DETACHED.equals(getTopic().getType())) {
                level = 1;
                sheet.addFloatingBranch(this);
            }
        }
        setBranchPolicyId(findNewBranchPolicyId());
    }

    public int getBranchIndex() {
        IBranchPart parentBranch = getParentBranch();
        if (parentBranch != null) {
            if (MindMapUI.BRANCH_CALLOUT.equals(getBranchType())) {
                return parentBranch.getCalloutBranches().indexOf(this);
            } else
                return parentBranch.getSubBranches().indexOf(this);
        }
        return 0;
    }

    public int getLevel() {
        return level;
    }

    public List<IBoundaryPart> getBoundaries() {
        if (boundaries == null) {
            boundaries = new ArrayList<IBoundaryPart>();
        }
        return boundaries;
    }

    public void addBoundary(IBoundaryPart boundary) {
        getBoundaries().add(boundary);
        sorter.sort(getBoundaries());
    }

    public void removeBoundary(IBoundaryPart boundary) {
        getBoundaries().remove(boundary);
    }

    public boolean isCentral() {
        return central;
    }

    public boolean isFolded() {
        return ((BranchFigure) getFigure()).isFolded();
    }

    public String getBranchType() {
        if (overrideBranchType != null)
            return overrideBranchType;
        if (isCentral())
            return MindMapUI.BRANCH_CENTRAL;
        IPart p = getParent();
        if (p instanceof IBranchPart) {
            IBranchPart parentBranch = (IBranchPart) p;
            if (parentBranch.isCentral()
                    && parentBranch.getSubBranches().contains(this))
                return MindMapUI.BRANCH_MAIN;
            if (parentBranch.getSummaryBranches().contains(this))
                return MindMapUI.BRANCH_SUMMARY;
            if (parentBranch.getCalloutBranches().contains(this))
                return MindMapUI.BRANCH_CALLOUT;
        }
        if (p instanceof ISheetPart
                && ((ISheetPart) p).getFloatingBranches().contains(this))
            return MindMapUI.BRANCH_FLOATING;
        return MindMapUI.BRANCH_SUB;
    }

    public void setOverrideBranchType(String overrideBranchType) {
        this.overrideBranchType = overrideBranchType;
    }

    protected LayoutManager createLayoutManager() {
        return new BranchLayout(this);
    }

    protected Object[] getModelChildren(Object model) {
        List<Object> list = new ArrayList<Object>();
        ITopic topic = getTopic();
        boolean showsSubTopics = showsSubTopics();
        if (showsSubTopics) {
            for (IBoundary b : MindMapUtils.getSortedBoundaries(topic)) {
                list.add(new ViewerModel(BoundaryPart.class, b));
            }
        } else {
            for (IBoundary b : topic.getBoundaries()) {
                if (b.isMasterBoundary()) {
                    list.add(new ViewerModel(BoundaryPart.class, b));
                    break;
                }
            }
        }

        list.add(new ViewerModel(TopicPart.class, topic));

        if (!(InfoItemContributorManager.getInstance().getContributors()
                .isEmpty()))
            list.add(new ViewerModel(InfoPart.class, topic));

        if (showsSubTopics) {
            List<ITopic> children = topic.getChildren(ITopic.ATTACHED);
            if (!children.isEmpty()) {
                list.add(new ViewerModel(PlusMinusPart.class, topic));
            }
            for (ITopic subtopic : children) {
                list.add(new ViewerModel(BranchPart.class, subtopic));
            }

            List<ITopic> calloutTopics = topic.getChildren(ITopic.CALLOUT);
            for (ITopic calloutTopic : calloutTopics) {
                list.add(new ViewerModel(BranchPart.class, calloutTopic));
            }

            for (ISummary summary : topic.getSummaries()) {
                ITopic summaryTopic = summary.getTopic();
                if (summaryTopic != null) {
                    list.add(new SummaryViewerModel(SummaryPart.class, summary,
                            summaryTopic));
                }
            }
            for (ITopic summaryTopic : topic.getChildren(ITopic.SUMMARY)) {
                list.add(new ViewerModel(BranchPart.class, summaryTopic));
            }
        }
        return list.toArray();
    }

    private boolean showsSubTopics() {
        int maxLevel = getSite().getViewer().getProperties()
                .getInteger(IMindMapViewer.VIEWER_MAX_TOPIC_LEVEL, -1);
        return maxLevel < 0 || level < maxLevel;
    }

    protected void addChild(IPart child, int index) {
        super.addChild(child, index);
        if (getStatus().isActive()) {
            update();
            if (child instanceof IBranchPart && child.getStatus().isActive()) {
                child.update();
            }
        }
    }

    protected void removeChild(IPart child) {
        super.removeChild(child);
        if (getStatus().isActive()) {
            update();
        }
    }

    protected void reorderChild(IPart child, int index) {
        int oldBranchIndex = getSubBranches().indexOf(child);
        int oldCallloutBranchIndex = getCalloutBranches().indexOf(child);
        super.reorderChild(child, index);
        if (oldCallloutBranchIndex >= 0) {
            sorter.sort(getCalloutBranches());
            int newCalloutBranchIndex = getCalloutBranches().indexOf(child);
            getCalloutConnections().move(getFigure(), oldCallloutBranchIndex,
                    newCalloutBranchIndex);
        }
        if (oldBranchIndex >= 0) {
            sorter.sort(getSubBranches());
            int newBranchIndex = getSubBranches().indexOf(child);
            getConnections().move(getFigure(), oldBranchIndex, newBranchIndex);
        } else if (getBoundaries().contains(child)) {
            sorter.sort(getBoundaries());
        } else if (getSummaries().contains(child)) {
            sorter.sort(getSummaries());
        } else if (getSummaryBranches().contains(child)) {
            sorter.sort(getSummaryBranches());
        }
        update();
    }

    protected void registerCoreEvents(Object source,
            ICoreEventRegister register) {
        super.registerCoreEvents(source, register);
        register.register(Core.TopicFolded);
        register.register(Core.TopicAdd);
        register.register(Core.TopicRemove);
        register.register(Core.Position);
        register.register(Core.BoundaryAdd);
        register.register(Core.BoundaryRemove);
        register.register(Core.SummaryAdd);
        register.register(Core.SummaryRemove);
        register.register(Core.Style);
        register.register(Core.StructureClass);
        register.register(Core.Labels);
        register.register(Core.TopicNotes);
//        register.register(Core.TopicHyperlink);
        ISheet sheet = getTopic().getOwnedSheet();
        register.setNextSourceFrom(sheet);
        register.register(Core.Visibility);
        register.register(Core.SheetSettings);
    }

    public void handleCoreEvent(final CoreEvent event) {
        Object modelcacheForLayout = ModelCacheManager.getInstance().getCache(
                getTopic(), ModelCacheManager.MODEL_CACHE_DELAYLAYOUT);
        if (Boolean.TRUE.equals(modelcacheForLayout)) {
            return;
        }
        String type = event.getType();
        if (Core.TopicFolded.equals(type)) {
            treeUpdate(true);
        } else if (Core.TopicAdd.equals(type)
                || Core.TopicRemove.equals(type)) {
            Object topicType = event.getData();
            if (ITopic.ATTACHED.equals(topicType)
                    || ITopic.CALLOUT.equals(topicType)) {
                refresh();
            } else if (ITopic.SUMMARY.equals(topicType)) {
                refresh();
            }
        } else if (Core.Position.equals(type)) {
            org.xmind.core.util.Point newPosition = (org.xmind.core.util.Point) event
                    .getNewValue();
            Point p = MindMapUtils.toGraphicalPosition(newPosition);
            getCacheManager().setCache(CACHE_PREF_POSITION, p);
            //update();
            getFigure().revalidate();
        } else if (Core.BoundaryAdd.equals(type)
                || Core.BoundaryRemove.equals(type)
                || Core.SummaryAdd.equals(type)
                || Core.SummaryRemove.equals(type)) {
            refresh();
        } else if (Core.Style.equals(type)) {
            refreshStyles();
        } else if (Core.StructureClass.equals(type)) {
            treeUpdateBranchPolicy();
            sendFakeStyleEvent();
        } else if (Core.Labels.equals(type) || Core.TopicNotes.equals(type)
                || Core.TopicHyperlink.equals(type)
                || Core.Visibility.equals(type)
                || Core.SheetSettings.equals(type)) {
            refresh();
            if (getInfoPart() != null)
                getInfoPart().refresh();
            if (getTopicPart() != null)
                getTopicPart().refresh();
        } else {
            super.handleCoreEvent(event);
        }
    }

    // Force an 'unchanged' style change be notified, so that
    // property sections or other style listeners can update themselves.
    // TODO: replace this with a better way of event dispatching, 
    // e.g. Properties and PropertyChangeListener
    private void sendFakeStyleEvent() {
        ICoreEventSupport coreEventSupport = (ICoreEventSupport) getTopic()
                .getOwnedWorkbook().getAdapter(ICoreEventSupport.class);
        if (coreEventSupport != null) {
            ICoreEventSource source = (ICoreEventSource) getTopic();
            String styleId = getTopic().getStyleId();
            CoreEvent event = new CoreEvent(source, Core.Style, styleId,
                    styleId);
            coreEventSupport.dispatch(source, event);
        }
    }

    public void refreshStyles() {
        treeFlushStyleCaches();
        treeUpdate(true);
        treeRefresh();
        floatingRefresh();
    }

    private void treeFlushStyleCaches() {
        IStyleSelector ss = getGraphicalPolicy().getStyleSelector(this);
        if (ss instanceof IBranchStyleSelector) {
            ((IBranchStyleSelector) ss).flushStyleCaches(this);
        }
        for (IBranchPart subBranch : getSubBranches()) {
            ((BranchPart) subBranch).treeFlushStyleCaches();
        }
        for (IBranchPart calloutBranch : getCalloutBranches()) {
            ((BranchPart) calloutBranch).treeFlushStyleCaches();
        }
        for (IBranchPart summaryBranch : getSummaryBranches()) {
            ((BranchPart) summaryBranch).treeFlushStyleCaches();
        }
    }

    protected void treeUpdateBranchPolicy() {
        updateBranchPolicy();
        updateView();
        refreshChildren();
        for (IBranchPart subBranch : getSubBranches()) {
            ((BranchPart) subBranch).treeUpdateBranchPolicy();
        }
        for (IBranchPart calloutBranch : getCalloutBranches()) {
            ((BranchPart) calloutBranch).treeUpdateBranchPolicy();
        }
        for (IBranchPart summaryBranch : getSummaryBranches()) {
            ((BranchPart) summaryBranch).treeUpdateBranchPolicy();
        }
        updateChildren();
    }

    private void updateBranchPolicy() {
        String newId = findNewBranchPolicyId();
        setBranchPolicyId(newId);
    }

    private void setBranchPolicyId(String newId) {
        if ((branchPolicyId == null && newId == null)
                || (branchPolicyId != null && branchPolicyId.equals(newId))) {
            return;
        }
        IBranchPolicy newPolicy = MindMapUI.getBranchPolicyManager()
                .getBranchPolicy(newId);
        branchPolicyId = newId;
        setGraphicalPolicy(newPolicy);
        getFigure().revalidate();
    }

    public void setGraphicalPolicy(IGraphicalPolicy graphicalPolicy) {
        IGraphicalPolicy oldPolicy = getGraphicalPolicy();
        super.setGraphicalPolicy(graphicalPolicy);
        if (oldPolicy instanceof IBranchPolicy) {
            ((IBranchPolicy) oldPolicy).postDeactivate(this);
        }
    }

    public String getBranchPolicyId() {
        if (branchPolicyId == null) {
            branchPolicyId = findNewBranchPolicyId();
        }
        return branchPolicyId;
    }

    private String findNewBranchPolicyId() {
        return MindMapUI.getBranchPolicyManager().calculateBranchPolicyId(this,
                getTopic().getStructureClass());
    }

    public IBranchPolicy getBranchPolicy() {
        IGraphicalPolicy gp = getGraphicalPolicy();
        if (gp instanceof IBranchPolicy)
            return (IBranchPolicy) gp;
        return MindMapUI.getBranchPolicyManager().getDefaultBranchPolicy();
    }

    public void setModel(Object model) {
        super.setModel(model);
        Point p = MindMapUtils.toGraphicalPosition(getTopic().getPosition());
        getCacheManager().setCache(CACHE_PREF_POSITION, p);
    }

    protected void onActivated() {
        super.onActivated();
        //getBranchPolicy().activate(this);
    }

    protected void onDeactivated() {
//        getBranchPolicy().deactivate(this);
        super.onDeactivated();
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.ui.internal.mindmap.MindMapPartBase#updateView()
     */
    @Override
    protected void updateView() {
        super.updateView();
        BranchFigure branchFigure = (BranchFigure) getFigure();
        branchFigure.setFolded(shouldBeFolded());
        boolean animating = isFigureAnimating();
        IBranchPart parent = getParentBranch();
        if (parent != null) {
            branchFigure.setMinimized(isUnusedSummaryBranch(parent));
            IFigure parentFigure = parent.getFigure();
            if (parentFigure instanceof BranchFigure) {
                BranchFigure parentBranchFigure = (BranchFigure) parentFigure;
                if (!animating) {
                    branchFigure.setVisible(!parentBranchFigure.isFolded()
                            && parentBranchFigure.isVisible());
                }
            }
        }
        IStyleSelector ss = getStyleSelector(this);
        IDecoration decoration = branchFigure.getDecoration();
        String newDescId = getString(this, ss, Styles.BranchDecorationClass,
                Styles.DEF_BRANCH_DECORATION);
        if (!isSameDecoration(decoration, newDescId)) {
            decoration = createBranchDecoration(this, newDescId);
            branchFigure.setDecoration(decoration);
        }
        if (decoration != null) {
            decoration.setAlpha(branchFigure, 0xff);
            if (!animating) {
                decoration.setVisible(branchFigure, branchFigure.isVisible());
            }
        }

    }

    private boolean isUnusedSummaryBranch(IBranchPart parent) {
        if (parent.getSummaryBranches().contains(this)) {
            String topicId = getTopic().getId();
            for (ISummary s : parent.getTopic().getSummaries()) {
                if (topicId.equals(s.getTopicId()))
                    return false;
            }
            return true;
        }
        return false;
    }

    private boolean shouldBeFolded() {
        if (!isPropertyModifiable(Core.TopicFolded))
            return false;
        Object cache = MindMapUtils.getCache(this, IBranchPart.CACHE_FOLDED);
        if (cache != null)
            return Boolean.TRUE.equals(cache);
        return getTopic().isFolded();
    }

    protected void updateChildren() {
        super.updateChildren();
        BranchFigure branchFigure = (BranchFigure) getFigure();
        branchFigure.setFolded(shouldBeFolded());
        boolean animating = isFigureAnimating();
        IBranchPart parent = getParentBranch();
        if (parent != null) {
            IFigure parentFigure = parent.getFigure();
            if (parentFigure instanceof BranchFigure) {
                BranchFigure parentBranchFigure = (BranchFigure) parentFigure;
                if (!animating) {
                    branchFigure.setVisible(!parentBranchFigure.isFolded()
                            && parentBranchFigure.isVisible());
                }
                if (MindMapUI.BRANCH_CALLOUT.equals(getBranchType())) {
                    decorateCalloutConnections(parent, parentBranchFigure,
                            parent.getCalloutConnections(), animating);
                }
            }
        }
        if (getTopicPart() != null) {
            getTopicPart().update();
            getTopicPart().refresh();
        }
        if (getPlusMinus() != null)
            getPlusMinus().update();
        if (getLabel() != null)
            getLabel().update();
        if (getInfoPart() != null) {
            getInfoPart().update();
            getInfoPart().refresh();
        }
//        for (IBranchPart calloutPart : getCalloutBranches()) {
//            calloutPart.update();
//        }
        for (IBoundaryPart b : getBoundaries()) {
            b.update();
        }
        for (ISummaryPart s : getSummaries()) {
            s.update();
        }
        IStyleSelector ss = getStyleSelector(this);
        decorateConnections(ss, branchFigure, getConnections(), animating);

        decorateCalloutConnections(this, (BranchFigure) getFigure(),
                getCalloutConnections(), animating);
//        for (IBranchPart s : getSubBranches()) {
//            ((BranchPart) s).updateChildren();
//        }
    }

    private void decorateCalloutConnections(IBranchPart parent,
            BranchFigure parentFigure, IBranchConnections2 connections,
            boolean ignoreVisibility) {
        connections.setId("org.xmind.ui.calloutbranchconnections"); //$NON-NLS-1$
        List<IBranchPart> calloutBranches = parent.getCalloutBranches();
        for (int i = 0; i < calloutBranches.size(); i++) {
            decorateCalloutConnection(parent, parentFigure, connections, i,
                    ignoreVisibility);
        }
    }

    private void decorateCalloutConnection(IBranchPart parent,
            BranchFigure parentFigure, IBranchConnections2 connections,
            int index, boolean ignoreVisibility) {
        IBranchPart calloutBranch = parent.getCalloutBranches().get(index);
        IDecoration connection = connections.getDecoration(index);
        BranchFigure calloutBranchFigure = (BranchFigure) calloutBranch
                .getFigure();

        IDecoratedFigure topicFigure = (IDecoratedFigure) calloutBranch
                .getTopicPart().getFigure();
        IDecoration topicDecoration = topicFigure.getDecoration();
        if (topicDecoration instanceof ICalloutTopicDecoration) {
            ICalloutTopicDecoration ctd = (ICalloutTopicDecoration) topicDecoration;
            String connectionId = ctd.getFromLineClass();
            if (!isSameDecoration(connection, connectionId)) {
                connection = createBranchConnection(calloutBranch,
                        connectionId);
                ((CalloutBranchConnections) connections)
                        .putFigureToDecoration(calloutBranchFigure, connection);
                connections.setDecoration(parentFigure, index, connection);
            }
            if (connection instanceof IShapeDecoration) {
                IShapeDecoration shapedConnection = (IShapeDecoration) connection;
                shapedConnection.setFillColor(parentFigure,
                        ctd.getFromFillColor());
                shapedConnection.setAlpha(parentFigure, 0xff);
            }

            if (connection instanceof ICorneredDecoration) {
                ((ICorneredDecoration) connection).setCornerSize(parentFigure,
                        ctd.getFromLineCorner());
            }

            if (connection instanceof IConnectionDecoration) {
                IConnectionDecoration connectedConnection = (IConnectionDecoration) connection;
                IAnchor sourceAnchor = null;
                ITopicPart topic = parent.getTopicPart();
                if (topic instanceof INodePart) {
                    sourceAnchor = ((INodePart) topic).getSourceAnchor(parent);
                }
                connectedConnection.setSourceAnchor(parentFigure, sourceAnchor);

                IAnchor targetAnchor = null;
                ITopicPart subTopic = calloutBranch.getTopicPart();
                if (subTopic instanceof INodePart) {
                    targetAnchor = ((INodePart) subTopic)
                            .getTargetAnchor(parent);
                }
                connectedConnection.setTargetAnchor(calloutBranchFigure,
                        targetAnchor);
            }

            if (connection instanceof ILineDecoration) {
                ILineDecoration linedConnection = (ILineDecoration) connection;
                linedConnection.setLineColor(parentFigure,
                        ctd.getFromLineColor());
                linedConnection.setLineStyle(parentFigure,
                        ctd.getFromLineStyle());
                linedConnection.setLineWidth(parentFigure,
                        ctd.getFromLineWidth());
            }

            if (connection instanceof IBranchConnectionDecoration) {
                IBranchConnectionDecoration branchConnection = (IBranchConnectionDecoration) connection;
                branchConnection.setSourceExpansion(parentFigure, 0);
                branchConnection.setSourceOrientation(parentFigure,
                        PositionConstants.NONE);

                branchConnection.setTargetOrientation(calloutBranchFigure,
                        PositionConstants.NONE);
                branchConnection.setTargetExpansion(calloutBranchFigure, 0);

                if (!ignoreVisibility) {
                    branchConnection.setVisible(calloutBranchFigure,
                            branchConnection.getSourceAnchor() != null
                                    && branchConnection
                                            .getTargetAnchor() != null
                                    && calloutBranchFigure.isVisible());
                }
            }
        }
    }

    private void decorateConnections(IStyleSelector ss, BranchFigure figure,
            IBranchConnections connections, boolean ignoreVisibility) {
        String newConnectionId = getString(this, ss, Styles.LineClass,
                Styles.BRANCH_CONN_STRAIGHT);
        connections.setId(newConnectionId);

        IAnchor sourceAnchor = null;
        ITopicPart topic = getTopicPart();
        if (topic instanceof INodePart) {
            sourceAnchor = ((INodePart) topic).getSourceAnchor(this);
        }
        connections.setSourceAnchor(figure, sourceAnchor);

        connections.setAlpha(figure, 0xff);
        connections.setLineColor(figure, getColor(this, ss, Styles.LineColor,
                newConnectionId, Styles.DEF_TOPIC_LINE_COLOR));
        connections.setLineStyle(figure,
                getLineStyle(this, ss, newConnectionId, SWT.LINE_SOLID));
        connections.setLineWidth(figure,
                getInteger(this, ss, Styles.LineWidth, newConnectionId, 1));

        int sourceOrientation = PositionConstants.NONE;
        IStructure structure = getBranchPolicy().getStructure(this);
        if (structure instanceof IBranchStructureExtension) {
            sourceOrientation = ((IBranchStructureExtension) structure)
                    .getSourceOrientation(this);
        }
        connections.setSourceOrientation(figure, sourceOrientation);

        int sourceExpansion = getSourceExpansion(this, ss, sourceOrientation,
                connections.getLineWidth(), connections.getId());
        connections.setSourceExpansion(figure, sourceExpansion);

        connections.setTapered(figure, isBranchLineTapered(this, ss));
        connections.setCornerSize(figure,
                getInteger(this, ss, Styles.LineCorner, newConnectionId, 5));

        List<IBranchPart> subBranches = getSubBranches();
        for (int i = 0; i < subBranches.size(); i++) {
            IBranchPart subBranch = subBranches.get(i);
            IDecoration connection = connections.getDecoration(i);
            if (!isSameDecoration(connection, newConnectionId)) {
                connection = createBranchConnection(this, newConnectionId);
                connections.setDecoration(figure, i, connection);
            }
            if (connection != null
                    && connection instanceof IBranchConnectionDecoration) {
                decorateConnection(this, ss, figure, subBranch,
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
        connection.setLineColor(figure, getBranchConnectionColor(branch, ss,
                subBranch, subBranchIndex, connections.getLineColor()));

        if (!ignoreVisibility) {
            connection.setVisible(figure,
                    connection.getSourceAnchor() != null
                            && connection.getTargetAnchor() != null
                            && figure.isVisible() && !figure.isFolded());
        }
    }

    public void treeUpdate(boolean updateParent) {
        if (updateParent) {
            IBranchPart parentBranch = getParentBranch();
            if (parentBranch != null) {
                ((BranchPart) parentBranch).updateChildren();
            }
        }
        updateView();
        for (IBranchPart subBranch : getSubBranches()) {
            subBranch.treeUpdate(false);
        }
        for (IBranchPart calloutBranch : getCalloutBranches()) {
            calloutBranch.treeUpdate(false);
        }
        for (IBranchPart summaryBranch : getSummaryBranches()) {
            summaryBranch.treeUpdate(false);
        }
        updateChildren();
    }

    public void treeRefresh() {
        if (getInfoPart() != null)
            getInfoPart().refresh();
        if (getTopicPart() != null)
            getTopicPart().refresh();
        for (IBranchPart subBranch : getSubBranches()) {
            subBranch.treeRefresh();
        }
        for (IBranchPart calloutBranch : getCalloutBranches()) {
            calloutBranch.treeRefresh();
        }
        for (IBranchPart summaryBranch : getSummaryBranches()) {
            summaryBranch.treeRefresh();
        }
        updateChildren();
    }

    private void floatingRefresh() {
        IPart parent = getParent();
        if (parent != null && parent instanceof SheetPart) {
            List<IBranchPart> floatings = ((SheetPart) parent)
                    .getFloatingBranches();
            for (IBranchPart floating : floatings) {
                IInfoPart infoPart = floating.getInfoPart();
                if (infoPart != null)
                    infoPart.refresh();
                floating.treeRefresh();
            }
        }
    }

    protected void declareEditPolicies(IRequestHandler reqHandler) {
        super.declareEditPolicies(reqHandler);
        reqHandler.installEditPolicy(GEF.ROLE_CREATABLE,
                MindMapUI.POLICY_TOPIC_CREATABLE);
        reqHandler.installEditPolicy(GEF.ROLE_EXTENDABLE,
                MindMapUI.POLICY_EXTENDABLE);
        reqHandler.installEditPolicy(GEF.ROLE_DELETABLE,
                MindMapUI.POLICY_DELETABLE);
    }

    public boolean isPropertyModifiable(String propertyName) {
        return isPropertyModifiable(propertyName, null);
    }

    public boolean isPropertyModifiable(String propertyName,
            String secondaryKey) {
        return getBranchPolicy().isPropertyModifiable(this, propertyName,
                secondaryKey);
    }

    public IPart findAt(Point position) {
        IPart ret;
        IPart focusedPart = getSite().getViewer().getFocusedPart();
        if (focusedPart instanceof IGraphicalEditPart
                && getBoundaries().contains(focusedPart)) {
            ret = ((IGraphicalEditPart) focusedPart).findAt(position);
            if (ret != null)
                return ret;
        }
        ret = super.findAt(position);
        if (ret == this)
            return null;
        return ret;
    }

    public boolean canSearchChild() {
        BranchFigure figure = (BranchFigure) getFigure();
        return !figure.isFolded() && !figure.isMinimized() && figure.isVisible()
                && figure.isEnabled()
                && (!getSubBranches().isEmpty()
                        || !getSummaryBranches().isEmpty()
                        || !getCalloutBranches().isEmpty());
    }

    protected boolean isFigureAnimatable() {
        return true;
    }

    public IBranchConnections getConnections() {
        if (connections == null) {
            connections = new BranchConnections();
        }
        return connections;
    }

    public IBranchConnections2 getCalloutConnections() {
        if (calloutConnections == null)
            calloutConnections = new CalloutBranchConnections();
        return calloutConnections;
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(ITopic.class))
            return getTopic();
        if (adapter == ITopicPart.class)
            return getTopicPart();
        if (adapter == IPlusMinusPart.class)
            return getPlusMinus();
        if (adapter == ILabelPart.class)
            return getLabel();
        if (adapter == IInfoPart.class)
            return getInfoPart();
        return super.getAdapter(adapter);
    }

}
