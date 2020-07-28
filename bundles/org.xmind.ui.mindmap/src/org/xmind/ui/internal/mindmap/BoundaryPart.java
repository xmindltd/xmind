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

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Cursor;
import org.xmind.core.Core;
import org.xmind.core.IBoundary;
import org.xmind.core.IRelationship;
import org.xmind.core.ITitled;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.gef.GEF;
import org.xmind.gef.IViewer;
import org.xmind.gef.draw2d.IAnchor;
import org.xmind.gef.part.IGraphicalEditPart;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IPartListener;
import org.xmind.gef.part.IRequestHandler;
import org.xmind.gef.part.PartEvent;
import org.xmind.gef.policy.NullEditPolicy;
import org.xmind.gef.service.IFeedback;
import org.xmind.gef.util.GEFUtils;
import org.xmind.ui.decorations.IBoundaryDecoration;
import org.xmind.ui.internal.decorators.BoundaryDecorator;
import org.xmind.ui.internal.figures.BoundaryFigure;
import org.xmind.ui.internal.graphicalpolicies.BoundaryGraphicalPolicy;
import org.xmind.ui.mindmap.IBoundaryPart;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IConnectionPart;
import org.xmind.ui.mindmap.IRangeListener;
import org.xmind.ui.mindmap.IRelationshipPart;
import org.xmind.ui.mindmap.ISelectionFeedbackHelper;
import org.xmind.ui.mindmap.ISheetPart;
import org.xmind.ui.mindmap.ITitleTextPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.mindmap.RangeEvent;
import org.xmind.ui.style.Styles;

public class BoundaryPart extends NodePart implements IBoundaryPart {

    private List<IBranchPart> enclosingBranches = null;

    private ITitleTextPart title = null;

    private IAnchor anchor = null;

    private List<IRangeListener> rangeListeners = null;

    private IPartListener parentListener = new IPartListener() {

        public void childRemoving(PartEvent event) {
            if (event.child instanceof IBranchPart)
                clearBranchesCache();
        }

        public void childAdded(PartEvent event) {
            if (event.child instanceof IBranchPart)
                clearBranchesCache();
        }

    };

    public BoundaryPart() {
        setDecorator(BoundaryDecorator.getInstance());
        setGraphicalPolicy(BoundaryGraphicalPolicy.getInstance());
    }

    public IBoundary getBoundary() {
        return (IBoundary) getRealModel();
    }

    public IBranchPart getOwnedBranch() {
        if (getParent() instanceof IBranchPart)
            return (IBranchPart) getParent();
        return null;
    }

    protected void register() {
        registerModel(getBoundary());
        super.register();
    }

    protected void unregister() {
        super.unregister();
        unregisterModel(getBoundary());
    }

    protected Object[] getModelChildren(Object model) {
        if (hasTitle() && !isPolygonShape(getFigure())) {
            return new Object[] { new ViewerModel(BoundaryTitleTextPart.class,
                    getBoundary()) };
        }
        return super.getModelChildren(model);
    }

    public ITitleTextPart getTitle() {
        return title;
    }

    public void setTitle(ITitleTextPart title) {
        this.title = title;
        ((BoundaryFigure) getFigure())
                .setTitle(title == null ? null : title.getTextFigure());
    }

    private boolean hasTitle() {
        ITitled titled = getBoundary();
        return titled.hasTitle() && !"".equals(titled.getTitleText()); //$NON-NLS-1$
    }

    public boolean isPolygonShape(IFigure figure) {
        if (figure == null || !(figure instanceof BoundaryFigure))
            return false;

        IBoundaryDecoration decoration = ((BoundaryFigure) figure)
                .getDecoration();
        if (decoration == null)
            return false;

        return Styles.BOUNDARY_SHAPE_POLYGON.equals(decoration.getId())
                || Styles.BOUNDARY_SHAPE_ROUNDEDPOLYGON
                        .equals(decoration.getId());
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(IBoundary.class))
            return getBoundary();
        if (adapter == TitleTextPart.class || adapter == ITitleTextPart.class)
            return getTitle();
        return super.getAdapter(adapter);
    }

    protected void fillSourceConnections(List<IConnectionPart> list) {
        IViewer viewer = getSite().getViewer();
        if (viewer != null) {
            ISheetPart sheet = (ISheetPart) viewer.getAdapter(ISheetPart.class);
            if (sheet != null) {
                String id = getBoundary().getId();
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

    protected void fillTargetConnections(List<IConnectionPart> list) {
        IViewer viewer = getSite().getViewer();
        if (viewer != null) {
            ISheetPart sheet = (ISheetPart) viewer.getAdapter(ISheetPart.class);
            if (sheet != null) {
                String id = getBoundary().getId();
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
        reqHandler.installEditPolicy(GEF.ROLE_MOVABLE,
                NullEditPolicy.getInstance());

        reqHandler.installEditPolicy(GEF.ROLE_NAVIGABLE,
                MindMapUI.POLICY_TOPIC_NAVIGABLE);
    }

    protected void registerCoreEvents(Object source,
            ICoreEventRegister register) {
        super.registerCoreEvents(source, register);
        register.register(Core.StartIndex);
        register.register(Core.EndIndex);

        register.register(Core.Range);

        register.register(Core.TitleText);
        register.register(Core.Style);
    }

    public void handleCoreEvent(CoreEvent event) {
        String type = event.getType();
        if (Core.StartIndex.equals(type) || Core.EndIndex.equals(type)) {
            refresh();
            getFigure().revalidate();
            IBranchPart branch = getOwnedBranch();
            if (branch != null) {
                branch.treeUpdate(true);
            }
            fireRangeChanged();
        } else if (Core.TitleText.equals(type)) {
            ((BoundaryFigure) getFigure()).setTitleVisible(
                    hasTitle() && !isPolygonShape(getFigure()));
            refresh();
            getFigure().revalidate();
        } else if (Core.Style.equals(type)) {
            update();
            ((BoundaryFigure) getFigure()).setTitleVisible(
                    hasTitle() && !isPolygonShape(getFigure()));
            refresh();
            getFigure().revalidate();
        } else {
            super.handleCoreEvent(event);
        }
    }

    public List<IBranchPart> getEnclosingBranches() {
        if (enclosingBranches == null) {
            enclosingBranches = fillEnclosingBranches(
                    new ArrayList<IBranchPart>());
        }
        return enclosingBranches;
    }

    private List<IBranchPart> fillEnclosingBranches(List<IBranchPart> list) {
        IBoundary b = getBoundary();
        List<ITopic> subtopics = b.getEnclosingTopics();
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

    /*
     * (non-Javadoc)
     * 
     * @seeorg.xmind.ui.mindmap.IBranchRangePart#encloses(org.xmind.ui.mindmap.
     * IBranchPart)
     */
    public boolean encloses(IBranchPart subbranch) {
        return getBoundary().encloses(subbranch.getTopic());
    }

    private void clearBranchesCache() {
        enclosingBranches = null;
    }

    public void setParent(IPart parent) {
        if (getParent() instanceof BranchPart) {
            getParent().removePartListener(parentListener);
            ((BranchPart) getParent()).removeBoundary(this);
        }
        super.setParent(parent);
        if (getParent() instanceof BranchPart) {
            ((BranchPart) getParent()).addBoundary(this);
            getParent().addPartListener(parentListener);
        }
    }

    public void refresh() {
        clearBranchesCache();
        super.refresh();
        ITitleTextPart title = getTitle();
        if (title != null) {
            title.refresh();
        }
    }

    public void update() {
        super.update();
        ITitleTextPart title = getTitle();
        if (title != null) {
            title.update();
        }
    }

    protected IFigure createFigure() {
        BoundaryFigure figure = new BoundaryFigure();
        figure.setTitleVisible(hasTitle() && !isPolygonShape(figure));
        return figure;
    }

    public IPart findAt(Point position) {
        IPart ret;
        ITitleTextPart title = getTitle();
        if (title != null) {
            ret = ((IGraphicalEditPart) title).findAt(position);
            if (ret != null)
                return this;
        }
        ret = super.findAt(position);
        return ret;
    }

    protected ISelectionFeedbackHelper createSelectionFeedbackHelper() {
        return new BoundarySelectionFeedbackHelper();
    }

    protected IFeedback createFeedback() {
        return new BoundaryFeedback(this);
    }

    public IFigure findTooltipAt(Point position) {
        if (hasTitle() && !isPolygonShape(getFigure()) && title != null
                && title.getFigure().containsPoint(position)) {
            return new Label(getBoundary().getTitleText());
        }
        return super.findTooltipAt(position);
    }

    public Cursor getCursor(Point pos) {
        int orientation = ((BoundaryFeedback) getFeedback())
                .getOrientation(pos);
        if (orientation != PositionConstants.NONE)
            return GEFUtils.getPositionCursor(orientation);
        return super.getCursor(pos);
    }

    public boolean containsPoint(Point position) {
        return super.containsPoint(position)
                || ((BoundaryFeedback) getFeedback())
                        .getOrientation(position) != PositionConstants.NONE;
    }

    public IAnchor getSourceAnchor(IGraphicalPart connection) {
        return getAnchor();
    }

    public IAnchor getTargetAnchor(IGraphicalPart connection) {
        return getAnchor();
    }

    protected IAnchor getAnchor() {
        if (anchor == null) {
            anchor = new DecoratedAnchor(getFigure());
        }
        return anchor;
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