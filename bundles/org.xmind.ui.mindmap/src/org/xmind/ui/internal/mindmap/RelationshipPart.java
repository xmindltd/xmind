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

import static org.xmind.ui.mindmap.MindMapUI.SOURCE_ANCHOR;
import static org.xmind.ui.mindmap.MindMapUI.SOURCE_CONTROL_POINT;
import static org.xmind.ui.mindmap.MindMapUI.TARGET_ANCHOR;
import static org.xmind.ui.mindmap.MindMapUI.TARGET_CONTROL_POINT;

import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Cursor;
import org.xmind.core.Core;
import org.xmind.core.IControlPoint;
import org.xmind.core.IRelationship;
import org.xmind.core.IRelationshipEnd;
import org.xmind.core.ITitled;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.gef.GEF;
import org.xmind.gef.part.IGraphicalEditPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IRequestHandler;
import org.xmind.gef.policy.NullEditPolicy;
import org.xmind.gef.service.IFeedback;
import org.xmind.ui.internal.decorators.RelationshipDecorator;
import org.xmind.ui.internal.figures.RelationshipFigure;
import org.xmind.ui.internal.graphicalpolicies.RelationshipGraphicalPolicy;
import org.xmind.ui.mindmap.INodePart;
import org.xmind.ui.mindmap.IRelationshipPart;
import org.xmind.ui.mindmap.ISelectionFeedbackHelper;
import org.xmind.ui.mindmap.ISheetPart;
import org.xmind.ui.mindmap.ITitleTextPart;
import org.xmind.ui.mindmap.MindMapUI;

/**
 * @author MANGOSOFT
 * 
 */
public class RelationshipPart extends ConnectionPart implements
        IRelationshipPart {

    private ITitleTextPart title = null;

    public RelationshipPart() {
        setDecorator(RelationshipDecorator.getInstance());
        setGraphicalPolicy(RelationshipGraphicalPolicy.getInstance());
    }

    protected IFeedback createFeedback() {
        RelationshipFeedback feedback = new RelationshipFeedback(this);
        feedback.addFeedback(new DecoratedRelFeedback(this));
        return feedback;
    }

    protected ISelectionFeedbackHelper createSelectionFeedbackHelper() {
        return new RelationshipSelectionHelper();
    }

    public IRelationship getRelationship() {
        return (IRelationship) super.getRealModel();
    }

    public ISheetPart getOwnerSheet() {
        if (getParent() instanceof ISheetPart) {
            return (ISheetPart) getParent();
        }
        return null;
    }

    public void setParent(IPart parent) {
        if (getParent() instanceof SheetPart) {
            ((SheetPart) getParent()).removeRelationship(this);
        }
        super.setParent(parent);
        if (getParent() instanceof SheetPart) {
            ((SheetPart) getParent()).addRelationship(this);
        }
    }

    public ITitleTextPart getTitle() {
        return title;
    }

    public void setTitle(ITitleTextPart title) {
        this.title = title;
        ((RelationshipFigure) getFigure()).setTitle(title == null ? null
                : title.getTextFigure());
    }

    protected void register() {
        registerModel(getRelationship());
        super.register();
    }

    protected void unregister() {
        super.unregister();
        unregisterModel(getRelationship());
    }

    private boolean hasTitle() {
        ITitled titled = getRelationship();
        return titled.hasTitle() && !"".equals(titled.getTitleText()); //$NON-NLS-1$
    }

    protected Object[] getModelChildren(Object model) {
        return new Object[] { new ViewerModel(RelTitleTextPart.class,
                getRelationship()) };
    }

    protected void declareEditPolicies(IRequestHandler reqHandler) {
        super.declareEditPolicies(reqHandler);
        reqHandler.installEditPolicy(GEF.ROLE_SELECTABLE,
                NullEditPolicy.getInstance());
        reqHandler.installEditPolicy(GEF.ROLE_MOVABLE,
                MindMapUI.POLICY_RELATIONSHIP_MOVABLE);
        reqHandler.installEditPolicy(GEF.ROLE_EDITABLE,
                MindMapUI.POLICY_EDITABLE);
        reqHandler.installEditPolicy(GEF.ROLE_MODIFIABLE,
                MindMapUI.POLICY_MODIFIABLE);
        reqHandler.installEditPolicy(GEF.ROLE_DELETABLE,
                MindMapUI.POLICY_DELETABLE);
        reqHandler.installEditPolicy(GEF.ROLE_CREATABLE,
                MindMapUI.POLICY_RELATIONSHIP_CREATABLE);
        reqHandler.installEditPolicy(GEF.ROLE_TRAVERSABLE,
                MindMapUI.POLICY_RELATIONSHIP_TRAVERSABLE);

        reqHandler.installEditPolicy(GEF.ROLE_NAVIGABLE,
                MindMapUI.POLICY_TOPIC_NAVIGABLE);
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(IRelationship.class))
            return getRelationship();
        if (adapter == TitleTextPart.class || adapter == ITitleTextPart.class)
            return getTitle();
        return super.getAdapter(adapter);
    }

    public int getPointId(Point position) {
        return ((RelationshipFeedback) getFeedback()).getPointId(position);
    }

    public boolean containsPoint(Point position) {
        return super.containsPoint(position)
                || ((getStatus().isSelected() || getStatus().isPreSelected()) && getPointId(position) != 0);
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

    public Cursor getCursor(Point pos) {
        int pointId = getPointId(pos);
        if (pointId == SOURCE_ANCHOR || pointId == TARGET_ANCHOR)
            return Cursors.HAND;
        if (pointId == SOURCE_CONTROL_POINT || pointId == TARGET_CONTROL_POINT)
            return Cursors.CROSS;
        return super.getCursor(pos);
    }

    public void refresh() {
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
        RelationshipFigure fig = new RelationshipFigure();
//        fig.setTitle(title.getTextFigure());
        fig.setTitleVisible(hasTitle());
        return fig;
    }

    protected INodePart findSourceNode() {
        return findNode(getRelationship().getEnd1());
    }

    protected INodePart findTargetNode() {
        return findNode(getRelationship().getEnd2());
    }

    private INodePart findNode(IRelationshipEnd end) {
        if (end != null) {
            IPart p = getSite().getViewer().findPart(end);
            if (p instanceof INodePart)
                return (INodePart) p;
        }
        return null;
    }

//    public void addNotify() {
//        register();
//        for (IPart child : getChildren()) {
//            child.addNotify();
//        }
//        refresh();
//    }
//
//    public void removeNotify() {
//        getStatus().dePreSelect();
//        getStatus().deSelect();
//        getStatus().lostFocus();
//        title.removeNotify();
//        for (Object o : getChildren().toArray()) {
//            ((IPart) o).removeNotify();
//        }
//        removeTitleView();
//        unregister();
//    }
//
//    public void figureMoved(IFigure source) {
//        update();
//    }

    protected void registerCoreEvents(Object source, ICoreEventRegister register) {
        super.registerCoreEvents(source, register);
//        register.register(Core.RelationshipControlPoint);
        register.register(Core.RelationshipEnd1);
        register.register(Core.RelationshipEnd2);
        register.register(Core.Style);
//        register.register(Core.TitleText);

        if (source instanceof IRelationship) {
            IControlPoint cp1 = ((IRelationship) source).getControlPoint(0);
            if (cp1 != null) {
                register.setNextSourceFrom(cp1);
                register.register(Core.Position);
            }
            IControlPoint cp2 = ((IRelationship) source).getControlPoint(1);
            if (cp2 != null) {
                register.setNextSourceFrom(cp2);
                register.register(Core.Position);
            }
        }
    }

    @Override
    public void handleCoreEvent(CoreEvent event) {
        String type = event.getType();
        if (Core.Position.equals(type)) {
            update();
        } else if (Core.RelationshipEnd1.equals(type)
                || Core.RelationshipEnd2.equals(type)) {
            refresh();
        } else if (Core.Style.equals(type)) {
            update();
//        } else if (Core.TitleText.equals(type)) {
//            ((RelationshipFigure) getFigure()).setTitleVisible(hasTitle());
//            refresh();
        } else {
            super.handleCoreEvent(event);
        }
    }

//    public void setModel(Object model) {
//        super.setModel(model);
//        title.setModel(new ViewerModel(RelTitleTextPart.class, getRealModel()));
//    }
//
//    protected void onActivated() {
//        super.onActivated();
//        title.getStatus().activate();
//    }
//
//    protected void onDeactivated() {
//        title.getStatus().deactivate();
//        super.onDeactivated();
//    }
//
//    protected void addTitleView() {
//        Layer layer = ((IGraphicalViewer) getSite().getViewer())
//                .getLayer(MindMapUI.LAYER_TITLE);
//        if (layer != null) {
//            layer.add(title.getTextFigure());
//        }
//    }
//
//    protected void removeTitleView() {
//        ITextFigure titleFigure = title.getTextFigure();
//        if (titleFigure.getParent() != null) {
//            titleFigure.getParent().remove(titleFigure);
//        }
//    }

}