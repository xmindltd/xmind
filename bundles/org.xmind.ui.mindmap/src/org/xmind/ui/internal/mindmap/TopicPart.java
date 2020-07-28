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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.geometry.Point;
import org.xmind.core.Core;
import org.xmind.core.IImage;
import org.xmind.core.INumbering;
import org.xmind.core.IRelationship;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerRef;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.gef.GEF;
import org.xmind.gef.IViewer;
import org.xmind.gef.draw2d.IAnchor;
import org.xmind.gef.draw2d.IAnchorListener;
import org.xmind.gef.draw2d.ITitledFigure;
import org.xmind.gef.draw2d.decoration.IConnectionDecoration;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IRequestHandler;
import org.xmind.gef.policy.NullEditPolicy;
import org.xmind.gef.ui.actions.ActionRegistry;
import org.xmind.gef.ui.actions.IActionRegistry;
import org.xmind.ui.internal.IconTipContributorManager;
import org.xmind.ui.internal.InfoItemContributorManager;
import org.xmind.ui.internal.TopicInfoItemManager;
import org.xmind.ui.internal.decorators.TopicDecorator;
import org.xmind.ui.internal.figures.TopicFigure;
import org.xmind.ui.internal.graphicalpolicies.TopicGraphicalPolicy;
import org.xmind.ui.internal.layouts.TopicLayout;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IConnectionPart;
import org.xmind.ui.mindmap.IIconTipContributor;
import org.xmind.ui.mindmap.IIconTipPart;
import org.xmind.ui.mindmap.IImagePart;
import org.xmind.ui.mindmap.IInfoItemContributor;
import org.xmind.ui.mindmap.IMarkerPart;
import org.xmind.ui.mindmap.IMindMapViewer;
import org.xmind.ui.mindmap.INumberingPart;
import org.xmind.ui.mindmap.IRelationshipPart;
import org.xmind.ui.mindmap.ISelectionFeedbackHelper;
import org.xmind.ui.mindmap.ISheetPart;
import org.xmind.ui.mindmap.ITitleTextPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

/**
 * @author MANGOSOFT
 */
public class TopicPart extends NodePart implements ITopicPart {

    private static Comparator<IMarkerRef> MARKER_REF_COMPARATOR = new Comparator<IMarkerRef>() {

        public int compare(IMarkerRef p, IMarkerRef q) {
            IMarker pMarker = p.getMarker();
            IMarker qMarker = q.getMarker();
            if (pMarker == null && qMarker == null)
                return 0;

            IMarkerSheet pSheet;
            List<IMarkerGroup> grouplist;
            IMarkerGroup pMarkerGroup;

            if (qMarker == null && pMarker != null) {
                pSheet = pMarker.getOwnedSheet();
                grouplist = pSheet.getMarkerGroups();
                pMarkerGroup = pMarker.getParent();
                return pMarkerGroup == null ? 0
                        : -10000 - 2000 + grouplist.indexOf(pMarkerGroup) * 100
                                + pMarkerGroup.getMarkers().indexOf(pMarker);
            }

            IMarkerSheet qSheet;
            IMarkerGroup qMarkerGroup;

            if (pMarker == null && qMarker != null) {
                qSheet = qMarker.getOwnedSheet();
                grouplist = qSheet.getMarkerGroups();
                qMarkerGroup = qMarker.getParent();
                return qMarkerGroup == null ? 0
                        : 10000 + 2000 - grouplist.indexOf(qMarkerGroup) * 100
                                - qMarkerGroup.getMarkers().indexOf(qMarker);
            }
            pSheet = pMarker.getOwnedSheet();
            qSheet = qMarker.getOwnedSheet();

            grouplist = pSheet.getMarkerGroups();
            pMarkerGroup = pMarker.getParent();
            qMarkerGroup = qMarker.getParent();
            if (pMarkerGroup == null || qMarkerGroup == null) {
                return 0;
            }

            if (pSheet.equals(qSheet)) {
                if (pMarkerGroup.equals(qMarkerGroup)) {
                    List<IMarker> nosingle = pMarkerGroup.getMarkers();
                    return nosingle.indexOf(pMarker)
                            - nosingle.indexOf(qMarker);
                }
                return (grouplist.indexOf(pMarkerGroup)
                        - grouplist.indexOf(qMarkerGroup)) * 100
                        + pMarkerGroup.getMarkers().indexOf(pMarker)
                        - qMarkerGroup.getMarkers().indexOf(qMarker);
            } else {
                if (MindMapUI.getResourceManager().getSystemMarkerSheet()
                        .equals(pSheet))
                    return -2000
                            + (grouplist.indexOf(pMarkerGroup) - qSheet
                                    .getMarkerGroups().indexOf(qMarkerGroup))
                                    * 100
                            + pMarkerGroup.getMarkers().indexOf(pMarker)
                            - qMarkerGroup.getMarkers().indexOf(qMarker);
                return 2000
                        + (grouplist.indexOf(pMarkerGroup) - qSheet
                                .getMarkerGroups().indexOf(qMarkerGroup)) * 100
                        + pMarkerGroup.getMarkers().indexOf(pMarker)
                        - qMarkerGroup.getMarkers().indexOf(qMarker);

            }

        }
    };

    private ITitleTextPart title = null;

    private List<IMarkerPart> markers = null;

    private List<IIconTipPart> iconTips = null;

    private IImagePart image = null;

    private INumberingPart numbering = null;

    private final ChildSorter sorter = new ChildSorter(this);

    private IActionRegistry actionRegistry = null;

    private IAnchor anchor = null;

    private IAnchorListener anchorListener = null;

    private TopicInfoItemManager topicInfoItemManager = new TopicInfoItemManager(
            this);;

    public TopicPart() {
        setDecorator(TopicDecorator.getInstance());
        setGraphicalPolicy(TopicGraphicalPolicy.getInstance());
    }

    public ITopic getTopic() {
        return (ITopic) getRealModel();
    }

    public IBranchPart getOwnerBranch() {
        if (getParent() instanceof IBranchPart)
            return (IBranchPart) getParent();
        return null;
    }

    public void setParent(IPart parent) {
        if (getParent() instanceof BranchPart) {
            BranchPart branch = (BranchPart) getParent();
            if (branch.getTopicPart() == this) {
                branch.setTopicPart(null);
            }
        }
        super.setParent(parent);
        if (getParent() instanceof BranchPart) {
            BranchPart branch = (BranchPart) getParent();
            branch.setTopicPart(this);
        }
    }

    protected LayoutManager createLayoutManager() {
        return new TopicLayout(this);
    }

    protected void declareEditPolicies(IRequestHandler reqHandler) {
        super.declareEditPolicies(reqHandler);
        reqHandler.installEditPolicy(GEF.ROLE_SELECTABLE,
                NullEditPolicy.getInstance());
        reqHandler.installEditPolicy(GEF.ROLE_NAVIGABLE,
                MindMapUI.POLICY_TOPIC_NAVIGABLE);
        reqHandler.installEditPolicy(GEF.ROLE_EDITABLE,
                MindMapUI.POLICY_EDITABLE);
        reqHandler.installEditPolicy(GEF.ROLE_MODIFIABLE,
                MindMapUI.POLICY_MODIFIABLE);
        reqHandler.installEditPolicy(GEF.ROLE_TRAVERSABLE,
                MindMapUI.POLICY_TOPIC_TRAVERSABLE);
        reqHandler.installEditPolicy(GEF.ROLE_DROP_TARGET,
                MindMapUI.POLICY_DROP_TARGET);
        reqHandler.installEditPolicy(GEF.ROLE_SORTABLE,
                MindMapUI.POLICY_SORTABLE);
        if (!isCentral() && !ITopic.SUMMARY.equals(getTopic().getType())) {
            reqHandler.installEditPolicy(GEF.ROLE_MOVABLE,
                    MindMapUI.POLICY_TOPIC_MOVABLE);
        }
        if (isCentral()) {
            reqHandler.installEditPolicy(GEF.ROLE_MAP_MOVABLE,
                    MindMapUI.POLICY_MAP_MOVABLE);
        }
    }

    private boolean isCentral() {
        IBranchPart branch = getOwnerBranch();
        return branch != null && branch.isCentral();
    }

//    protected void uninstallPolicies(IRequestProcessor reqProc) {
//        if (reqProc.hasRole(GEF.ROLE_MOVABLE))
//            reqProc.uninstallPolicy(GEF.ROLE_MOVABLE);
//        reqProc.uninstallPolicy(GEF.ROLE_MODIFIABLE);
//        reqProc.uninstallPolicy(GEF.ROLE_EDITABLE);
//        reqProc.uninstallPolicy(GEF.ROLE_NAVIGABLE);
//        reqProc.uninstallPolicy(GEF.ROLE_SELECTABLE);
//        super.uninstallPolicies(reqProc);
//    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.gef.part.GraphicalEditPart#containsPoint(org.eclipse.draw2d
     * .geometry.Point)
     */
    @Override
    public boolean containsPoint(Point position) {
        // TODO Auto-generated method stub
        return super.containsPoint(position);
    }

    public ITitleTextPart getTitle() {
        return title;
    }

    public void setTitle(ITitleTextPart title) {
        this.title = title;
    }

    public List<IMarkerPart> getMarkers() {
        if (markers == null) {
            markers = new ArrayList<IMarkerPart>();
        }
        return markers;
    }

    public void addMarker(IMarkerPart marker) {
        getMarkers().add(marker);
        sorter.sort(getMarkers());
    }

    public void removeMarker(IMarkerPart marker) {
        getMarkers().remove(marker);
    }

    public List<IIconTipPart> getIconTips() {
        if (iconTips == null) {
            iconTips = new ArrayList<IIconTipPart>();
        }
        return iconTips;
    }

    public void addIconTip(IIconTipPart iconTip) {
        getIconTips().add(iconTip);
        sorter.sort(getIconTips());
    }

    public void removeIconTip(IIconTipPart iconTip) {
        getIconTips().remove(iconTip);
    }

    public IImagePart getImagePart() {
        return image;
    }

    public void setImagePart(IImagePart image) {
        this.image = image;
    }

    public INumberingPart getNumberingPart() {
        return numbering;
    }

    public void setNumberingPart(INumberingPart numbering) {
        this.numbering = numbering;
    }

    protected IFigure createFigure() {
        return new TopicFigure();
    }

    protected ISelectionFeedbackHelper createSelectionFeedbackHelper() {
        return new DefaultSelectionFeedbackHelper();
    }

    protected void register() {
        registerModel(getTopic());
        super.register();
    }

    protected void unregister() {
        super.unregister();
        unregisterModel(getTopic());
    }

    protected Object[] getModelChildren(Object model) {
        List<Object> list = new ArrayList<Object>();
        ITopic topic = getTopic();

        list.add(new ViewerModel(TopicTitleTextPart.class, topic));

        if (getNumberingText() != null) {
            list.add(new ViewerModel(NumberingPart.class, topic));
        }

        addIconTips(topic, list);

        addMarkers(topic, list);

        if (topic.getImage().getSource() != null) {
            list.add(new ViewerModel(ImagePart.class, topic.getImage()));
        }
        return list.toArray();
    }

    private void addMarkers(ITopic topic, List<Object> list) {
        Set<IMarkerRef> markerRefs = topic.getMarkerRefs();
        if (markerRefs.isEmpty())
            return;
        List<IMarkerRef> markerRefsToSort = new ArrayList<IMarkerRef>(
                markerRefs);
        Collections.sort(markerRefsToSort, MARKER_REF_COMPARATOR);
        for (IMarkerRef ref : markerRefsToSort) {
            list.add(new ViewerModel(MarkerPart.class, ref));
        }
    }

    private void addIconTips(ITopic topic, List<Object> list) {
        list.addAll(topicInfoItemManager.getIconTips());
    }

    public IPart findAt(Point position) {
        IPart ret = super.findAt(position);
        if (ret != null) {
            if (ret == title || ret == numbering) {
                if (containsPoint(position)) {
                    // Prevent title exceeding topic bounds
                    return this;
                } else {
                    ret = null;
                }
            }
        }
        return ret;
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (adapter.isAssignableFrom(ITopic.class))
            return adapter.cast(getTopic());
        if (adapter == TitleTextPart.class || adapter == ITitleTextPart.class)
            return adapter.cast(getTitle());
        if (adapter == IBranchPart.class)
            return adapter.cast(getOwnerBranch());
        if (adapter == IActionRegistry.class)
            return adapter.cast(getActionRegistry());
        return super.getAdapter(adapter);
    }

    public IActionRegistry getActionRegistry() {
        if (actionRegistry == null) {
            actionRegistry = new ActionRegistry();
            IViewer viewer = getSite().getViewer();
            if (viewer != null) {
                actionRegistry.setParent((IActionRegistry) viewer
                        .getProperties().get(IMindMapViewer.VIEWER_ACTIONS));
            }
        }
        return actionRegistry;
    }

    protected void registerCoreEvents(Object source,
            ICoreEventRegister register) {
        super.registerCoreEvents(source, register);
        register.register(Core.MarkerRefAdd);
        register.register(Core.MarkerRefRemove);

        if (source instanceof ITopic) {
            ITopic parent = ((ITopic) source).getParent();
            if (parent != null) {
                register.setNextSourceFrom(parent);
                register.register(Core.TopicAdd);
                register.register(Core.TopicRemove);
                INumbering parentNumbering = parent.getNumbering();
                register.setNextSourceFrom(parentNumbering);
                register.register(Core.NumberFormat);
                register.register(Core.NumberingPrefix);
                register.register(Core.NumberingSuffix);
                register.register(Core.NumberPrepending);
                register.register(Core.NumberingSeparator);
                register.register(Core.NumberingDepth);
            }
        }

        IImage imageModel = getTopic().getImage();
        register.setNextSourceFrom(imageModel);
        register.register(Core.ImageSource);
    }

    public void handleCoreEvent(CoreEvent event) {
        String type = event.getType();
        if (Core.MarkerRefAdd.equals(type)
                || Core.MarkerRefRemove.equals(type)) {
            refresh();
        } else if (Core.ImageSource.equals(type)) {
            boolean hasNoImage = event.getNewValue() == null;
            boolean hadNoImage = event.getOldValue() == null;
            if ((hasNoImage && !hadNoImage) || (hadNoImage && !hasNoImage)) {
                refresh();
            }
        } else if (Core.TopicAdd.equals(type)
                || Core.TopicRemove.equals(type)) {
            if (ITopic.ATTACHED.equals(event.getData())) {
                treeRefresh();
            }
        } else if (Core.NumberFormat.equals(type)
                || Core.NumberingPrefix.equals(type)
                || Core.NumberingSuffix.equals(type)
                || Core.NumberPrepending.equals(type)
                || Core.NumberingSeparator.equals(type)
                || Core.NumberingDepth.equals(type)) {
            treeRefresh();
        } else {
            super.handleCoreEvent(event);
        }
    }

    protected void treeRefresh() {
        refresh();
        IBranchPart branch = getOwnerBranch();
        if (branch != null) {
            for (IBranchPart sub : branch.getSubBranches()) {
                ITopicPart child = sub.getTopicPart();
                if (child instanceof TopicPart) {
                    ((TopicPart) child).treeRefresh();
                }
            }
            for (IBranchPart sub : branch.getCalloutBranches()) {
                ITopicPart child = sub.getTopicPart();
                if (child instanceof TopicPart) {
                    ((TopicPart) child).treeRefresh();
                }
            }
        }
    }

    protected void onActivated() {
        super.onActivated();
        for (IIconTipContributor iconTipCont : IconTipContributorManager
                .getInstance().getContributors()) {
            iconTipCont.topicActivated(this);
        }

        for (IInfoItemContributor infoItemCont : InfoItemContributorManager
                .getInstance().getContributors())
            infoItemCont.topicActivated(this);

        for (IInfoItemContributor infoItemCont : InfoItemContributorManager
                .getInstance().getBothContributors())
            infoItemCont.topicActivated(this);
    }

    protected void onDeactivated() {
        for (IInfoItemContributor infoItemCont : InfoItemContributorManager
                .getInstance().getContributors())
            infoItemCont.topicDeactivated(this);

        for (IInfoItemContributor infoItemCont : InfoItemContributorManager
                .getInstance().getBothContributors())
            infoItemCont.topicDeactivated(this);

        for (IIconTipContributor iconTipCont : IconTipContributorManager
                .getInstance().getContributors()) {
            iconTipCont.topicDeactivated(this);
        }
        topicInfoItemManager.topicDeactivated();
        super.onDeactivated();
    }

    public void refresh() {
        super.refresh();
        for (IIconTipPart iconTip : getIconTips()) {
            iconTip.refresh();
        }
        if (getParent() instanceof BranchPart) {
            BranchPart branch = (BranchPart) getParent();
            if (branch.getInfoPart() != null)
                branch.getInfoPart().refresh();
        }
    }

    public void update() {
        super.update();
        for (IIconTipPart iconTip : getIconTips()) {
            iconTip.update();
        }
        if (getParent() instanceof BranchPart) {
            BranchPart branch = (BranchPart) getParent();
            if (branch.getInfoPart() != null)
                branch.getInfoPart().update();
        }
    }

    protected void updateChildren() {
        super.updateChildren();
        if (title != null) {
            title.update();
        }
        if (numbering != null) {
            numbering.update();
        }
        if (image != null) {
            image.update();
        }
    }

    protected void fillSourceConnections(List<IConnectionPart> list) {
        IViewer viewer = getSite().getViewer();
        if (viewer != null) {
            ISheetPart sheet = (ISheetPart) viewer.getAdapter(ISheetPart.class);
            if (sheet != null) {
                String id = getTopic().getId();
                for (IRelationshipPart rel : sheet.getRelationships()) {
                    IRelationship r = rel.getRelationship();
                    if (r != null) {
                        if (id.equals(r.getEnd1Id()))
                            list.add(rel);
                    }
                }
            }
        }
    }

    protected void fillTargetConnections(List<IConnectionPart> list) {
        IViewer viewer = getSite().getViewer();
        if (viewer != null) {
            ISheetPart sheet = (ISheetPart) viewer.getAdapter(ISheetPart.class);
            if (sheet != null) {
                String id = getTopic().getId();
                for (IRelationshipPart rel : sheet.getRelationships()) {
                    IRelationship r = rel.getRelationship();
                    if (r != null) {
                        if (id.equals(r.getEnd2Id()))
                            list.add(rel);
                    }
                }
            }
        }
    }

    @Override
    public void setModel(Object model) {
        super.setModel(model);
        setAccessible(new TopicAccessible(this));
    }

    @Override
    protected void addChild(IPart child, int index) {
        super.addChild(child, index);
        update();
    }

    @Override
    protected void removeChild(IPart child) {
        super.removeChild(child);
        update();
    }

    protected void reorderChild(IPart child, int index) {
        super.reorderChild(child, index);
        if (getMarkers().contains(child)) {
            sorter.sort(getMarkers());
        } else if (getIconTips().contains(child)) {
            sorter.sort(getIconTips());
        }
        update();
    }

    protected void addChildView(IPart child, int index) {
        super.addChildView(child, index);
        if (getFigure() instanceof ITitledFigure) {
            if (child instanceof ITitleTextPart) {
                ((ITitledFigure) getFigure())
                        .setTitle(((ITitleTextPart) child).getTextFigure());
            }
        }
    }

    protected void removeChildView(IPart child) {
        super.removeChildView(child);
        if (getFigure() instanceof ITitledFigure) {
            if (child instanceof ITitleTextPart) {
                ((ITitledFigure) getFigure()).setTitle(null);
            }
        }
    }

    public IAnchor getSourceAnchor(IGraphicalPart connection) {
        return getTopicAnchor();
    }

    public IAnchor getTargetAnchor(IGraphicalPart connection) {
        return getTopicAnchor();
    }

    protected IAnchor getTopicAnchor() {
        if (anchor == null) {
            anchor = new DecoratedAnchor(getFigure());
            if (anchorListener == null) {
                anchorListener = new IAnchorListener() {
                    public void anchorMoved(IAnchor anchor) {
                        topicAnchorMoved();
                    }
                };
            }
            anchor.addAnchorListener(anchorListener);
        }
        return anchor;
    }

    private void topicAnchorMoved() {
        IBranchPart branch = getOwnerBranch();
        if (branch != null) {
            branch.getConnections().rerouteAll(branch.getFigure());
            branch.getCalloutConnections().rerouteAll(branch.getFigure());

            IBranchPart parentBranch = branch.getParentBranch();
            if (parentBranch != null) {
                IDecoration decoration = null;
                if (MindMapUI.BRANCH_CALLOUT.equals(branch.getBranchType())) {
                    decoration = parentBranch.getCalloutConnections()
                            .getDecoration(branch.getBranchIndex());
                } else
                    decoration = parentBranch.getConnections()
                            .getDecoration(branch.getBranchIndex());
                if (decoration instanceof IConnectionDecoration) {
                    ((IConnectionDecoration) decoration)
                            .reroute(parentBranch.getFigure());
                }
            }
        }
    }

    @Override
    protected IFigure getShadowSource() {
        return getFigure();
    }

    public String getNumberingText() {
        return MindMapUtils.getNumberingText(getTopic(), null, null);
    }

    public String getFullNumberingText() {
        return MindMapUtils.getFullNumberingText(getTopic(), null, null);
    }

}
