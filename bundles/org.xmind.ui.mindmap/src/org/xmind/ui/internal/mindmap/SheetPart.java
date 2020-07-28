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
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.xmind.core.Core;
import org.xmind.core.ILegend;
import org.xmind.core.IRelationship;
import org.xmind.core.IRelationshipEnd;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.gef.GEF;
import org.xmind.gef.part.IGraphicalEditPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IRequestHandler;
import org.xmind.ui.internal.decorators.SheetDecorator;
import org.xmind.ui.internal.figures.SheetFigure;
import org.xmind.ui.internal.graphicalpolicies.SheetGraphicalPolicy;
import org.xmind.ui.internal.layouts.SheetLayout;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IConnectionPart;
import org.xmind.ui.mindmap.ILegendPart;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.IMindMapViewer;
import org.xmind.ui.mindmap.IRelationshipPart;
import org.xmind.ui.mindmap.ISheetPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

/**
 * @author MANGOSOFT
 */
public class SheetPart extends MindMapPartBase
        implements ISheetPart, ControlListener {

    private IBranchPart centralBranch = null;

    private List<IBranchPart> floatingBranches = null;

    private List<IRelationshipPart> relationships = null;

    private ILegendPart legend = null;

    private final ChildSorter sorter = new ChildSorter(this);

    public SheetPart() {
        setDecorator(SheetDecorator.getInstance());
        setGraphicalPolicy(SheetGraphicalPolicy.getInstance());
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(ISheet.class))
            return getSheet();
        return super.getAdapter(adapter);
    }

    public ISheet getSheet() {
        return ((IMindMap) super.getModel()).getSheet();
    }

    public ITopic getRootTopic() {
        return getSheet().getRootTopic();
    }

    public ITopic getCentralTopic() {
        return ((IMindMap) super.getModel()).getCentralTopic();
    }

    protected void register() {
        registerModel(getSheet());
        super.register();
    }

    protected void unregister() {
        super.unregister();
        unregisterModel(getSheet());
    }

    public IBranchPart getCentralBranch() {
        return centralBranch;
    }

    public void setCentralBranch(IBranchPart branch) {
        this.centralBranch = branch;
    }

    public List<IBranchPart> getFloatingBranches() {
        if (floatingBranches == null) {
            floatingBranches = new ArrayList<IBranchPart>();
        }
        return floatingBranches;
    }

    public void addFloatingBranch(IBranchPart branch) {
        getFloatingBranches().add(branch);
        sorter.sort(getFloatingBranches());
    }

    public void removeFloatingBranch(IBranchPart branch) {
        getFloatingBranches().remove(branch);
    }

    public List<IRelationshipPart> getRelationships() {
        if (relationships == null) {
            relationships = new ArrayList<IRelationshipPart>();
        }
        return relationships;
    }

    public void addRelationship(IRelationshipPart relationship) {
        getRelationships().add(relationship);
        sorter.sort(getRelationships());
    }

    public void removeRelationship(IConnectionPart relationship) {
        getRelationships().remove(relationship);
    }

    public ILegendPart getLegend() {
        return legend;
    }

    public void setLegend(ILegendPart legend) {
        this.legend = legend;
    }

    protected IFigure createFigure() {
        return new SheetFigure();
    }

    protected LayoutManager createLayoutManager() {
        return new SheetLayout(this);
    }

    protected Object[] getModelChildren(Object model) {
        List<Object> list = new ArrayList<Object>();
        ILegend legend = getSheet().getLegend();
        if (legend.isVisible()) {
            list.add(new ViewerModel(LegendPart.class, legend));
        }

        ITopic rootTopic = getCentralTopic();
        list.add(new ViewerModel(BranchPart.class, rootTopic));
        for (ITopic floatingTopic : rootTopic.getChildren(ITopic.DETACHED)) {
            list.add(new ViewerModel(BranchPart.class, floatingTopic));
        }
        for (IRelationship rel : getSheet().getRelationships()) {
            if (isRelationshipVisible(rel, rootTopic)) {
                ViewerModel m = new ViewerModel(RelationshipPart.class, rel);
                list.add(m);
            }
        }
        return list.toArray();
    }

    private boolean isRelationshipVisible(IRelationship r, ITopic rootTopic) {
        IRelationshipEnd end1 = r.getEnd1();
        if (end1 instanceof ITopic) {
            if (!isTopicVisible((ITopic) end1, rootTopic))
                return false;
        }
        IRelationshipEnd end2 = r.getEnd2();
        if (end2 instanceof ITopic) {
            if (!isTopicVisible((ITopic) end2, rootTopic))
                return false;
        }
        return true;
    }

    private boolean isTopicVisible(ITopic t, ITopic rootTopic) {
        if (t.equals(rootTopic))
            return true;

        if (isOverMaxLevel(t, rootTopic))
            return false;

        ITopic parent = t.getParent();
        if (parent == null || parent.equals(rootTopic))
            return true;
        String topicType = t.getType();
        if (!ITopic.ATTACHED.equals(topicType)
                && !ITopic.SUMMARY.equals(topicType)
                && !ITopic.CALLOUT.equals(topicType))
            return false;
        return isTopicVisible(parent, rootTopic);
    }

    private boolean isOverMaxLevel(ITopic t, ITopic rootTopic) {
        int maxLevel = getSite().getViewer().getProperties()
                .getInteger(IMindMapViewer.VIEWER_MAX_TOPIC_LEVEL, -1);
        if (maxLevel < 0)
            return false;
        int level = MindMapUtils.getLevel(t, rootTopic);
        return level < 0 || level > maxLevel;
    }

    protected void reorderChild(IPart child, int index) {
        super.reorderChild(child, index);
        if (getFloatingBranches().contains(child)) {
            sorter.sort(getFloatingBranches());
        } else if (getRelationships().contains(child)) {
            sorter.sort(getRelationships());
        }
    }

    protected void declareEditPolicies(IRequestHandler reqHandler) {
        super.declareEditPolicies(reqHandler);
        reqHandler.installEditPolicy(GEF.ROLE_CREATABLE,
                MindMapUI.POLICY_SHEET_CREATABLE);
        reqHandler.installEditPolicy(GEF.ROLE_SCALABLE,
                MindMapUI.POLICY_SHEET_SCALABLE);
        reqHandler.installEditPolicy(GEF.ROLE_EDITABLE,
                MindMapUI.POLICY_EDITABLE);
        reqHandler.installEditPolicy(GEF.ROLE_MODIFIABLE,
                MindMapUI.POLICY_MODIFIABLE);
        reqHandler.installEditPolicy(GEF.ROLE_DROP_TARGET,
                MindMapUI.POLICY_DROP_TARGET);
        reqHandler.installEditPolicy(MindMapUI.ROLE_MAP, MindMapUI.POLICY_MAP);
    }

    protected void registerCoreEvents(Object source,
            ICoreEventRegister register) {
        super.registerCoreEvents(source, register);
        register.register(Core.RelationshipAdd);
        register.register(Core.RelationshipRemove);
        register.register(Core.Style);
        register.register(Core.ThemeId);
        register.register(Core.SheetSettings);

        ITopic rootTopic = getCentralTopic();
        register.setNextSourceFrom(rootTopic);
        register.register(Core.TopicAdd);
        register.register(Core.TopicRemove);

        ILegend legend = getSheet().getLegend();
        register.setNextSourceFrom(legend);
        register.register(Core.Visibility);
    }

    public void handleCoreEvent(CoreEvent event) {
        String type = event.getType();
        if (Core.TopicAdd.equals(type) || Core.TopicRemove.equals(type)) {
            if (ITopic.DETACHED.equals(event.getData())) {
                refresh();
            }
        } else if (Core.Style.equals(type)) {
            update();
            updateCentralBranchStyle();
            refreshLegend();
        } else if (Core.RelationshipAdd.equals(type)
                || Core.RelationshipRemove.equals(type)) {
            refresh();
        } else if (Core.Visibility.equals(type)) {
            refresh();
        } else if (Core.SheetSettings.equals(type)) {
            refresh();
        } else if (Core.ThemeId.equals(type)) {
            refreshTheme();
        } else {
            super.handleCoreEvent(event);
        }
    }

    private void updateCentralBranchStyle() {
        IBranchPart b = getCentralBranch();
        if (b instanceof BranchPart) {
            ((BranchPart) b).refreshStyles();
        }
    }

    private void refreshLegend() {
        if (getLegend() != null)
            getLegend().refresh();
    }

    public void refreshTheme() {
        refresh();
        if (centralBranch != null && centralBranch instanceof BranchPart) {
            ((BranchPart) centralBranch).refreshStyles();
        }
        for (IBranchPart floatingBranch : getFloatingBranches()) {
            if (floatingBranch instanceof BranchPart) {
                ((BranchPart) floatingBranch).refreshStyles();
            }
        }
        for (IRelationshipPart r : getRelationships()) {
            r.refresh();
        }
        if (legend != null) {
            legend.refresh();
        }

    }

    protected boolean isFigureAnimatable() {
        return true;
    }

    public IPart findAt(Point position) {
        IPart ret;
        IPart focused = getSite().getViewer().getFocusedPart();
        if (focused instanceof IGraphicalEditPart
                && focused.getParent() == this) {
            ret = ((IGraphicalEditPart) focused).findAt(position);
            if (ret != null)
                return ret;
        }
        ret = super.findAt(position);
        if (ret == this)
            return null;
        return ret;
    }

    @Override
    public void setModel(Object model) {
        super.setModel(model);
        setAccessible(new SheetAccessible(this));
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.ui.internal.mindmap.MindMapPartBase#onActivated()
     */
    @Override
    protected void onActivated() {
        super.onActivated();
        getSite().getViewer().getControl().addControlListener(this);
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.ui.internal.mindmap.MindMapPartBase#onDeactivated()
     */
    @Override
    protected void onDeactivated() {
        getSite().getViewer().getControl().removeControlListener(this);
        super.onDeactivated();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.swt.events.ControlListener#controlMoved(org.eclipse.swt.
     * events .ControlEvent)
     */
    public void controlMoved(ControlEvent e) {
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.swt.events.ControlListener#controlResized(org.eclipse.swt
     * .events.ControlEvent)
     */
    public void controlResized(ControlEvent e) {
        getFigure().revalidate();
    }

}
