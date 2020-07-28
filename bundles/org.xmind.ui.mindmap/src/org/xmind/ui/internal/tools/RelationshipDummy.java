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
package org.xmind.ui.internal.tools;

import java.util.List;

import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.xmind.core.IControlPoint;
import org.xmind.core.IRelationship;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.draw2d.AbstractAnchor;
import org.xmind.gef.draw2d.IAnchor;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.draw2d.ReferencedFigure;
import org.xmind.gef.draw2d.SelectionFigure;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.service.IFeedbackService;
import org.xmind.ui.internal.mindmap.NodePart;
import org.xmind.ui.internal.mindmap.RelationshipFeedback;
import org.xmind.ui.internal.mindmap.RelationshipPart;
import org.xmind.ui.mindmap.IConnectionPart;
import org.xmind.ui.mindmap.INodePart;
import org.xmind.ui.mindmap.IRelationshipPart;
import org.xmind.ui.mindmap.MindMapUI;

public class RelationshipDummy {

    private static class DummyNodePart extends NodePart
            implements FigureListener {

        private IGraphicalPart source;

        private IAnchor anchor = null;

        public DummyNodePart(IGraphicalPart source) {
            this.source = source;
        }

        public DummyNodePart() {
            this(null);
        }

        protected void fillSourceConnections(List<IConnectionPart> list) {
        }

        protected void fillTargetConnections(List<IConnectionPart> list) {
        }

        @Override
        protected IFigure createFigure() {
            ReferencedFigure fig = new ReferencedFigure();
            if (source != null) {
                fig.setBounds(source.getFigure().getBounds());
            } else {
                fig.setSize(5, 5);
            }
            return fig;
        }

        @Override
        public void addNotify() {
            super.addNotify();
            if (source != null) {
                source.getFigure().addFigureListener(this);
            }
        }

        @Override
        public void removeNotify() {
            if (source != null) {
                source.getFigure().removeFigureListener(this);
            }
            super.removeNotify();
        }

        public IAnchor getSourceAnchor(IGraphicalPart connection) {
            return getAnchor();
        }

        public IAnchor getTargetAnchor(IGraphicalPart connection) {
            return getAnchor();
        }

        private IAnchor getAnchor() {
            if (anchor == null) {
                if (source != null && source instanceof INodePart) {
                    anchor = ((INodePart) source).getSourceAnchor(null);
                } else {
                    anchor = new ReferenceAnchor(getFigure());
                }
            }
            return anchor;
        }

        public void figureMoved(IFigure source) {
            getFigure().setBounds(source.getBounds());
        }

    }

    private class DummyRelPart extends RelationshipPart {

        @Override
        protected INodePart findSourceNode() {
            return sn;
        }

        @Override
        protected INodePart findTargetNode() {
            return tn;
        }
    }

    private static class ReferenceAnchor extends AbstractAnchor {

        public ReferenceAnchor(IFigure owner) {
            super(owner);
        }

        public PrecisionPoint getLocation(double x, double y,
                double expansion) {
            return getReferencePoint();
        }
    }

    private IGraphicalViewer viewer;

    private INodePart sn;

    private INodePart tn;

    private IRelationshipPart rel;

    private ITopic t1;

    private ITopic t2;

    private IRelationship r;

    private IFeedbackService feedbackService;

    private RelationshipFeedback feedback;

    public RelationshipDummy(IFigure layer, IGraphicalPart sourceNode,
            Point cursorPosition, IGraphicalViewer viewer) {
        this.viewer = viewer;
        createDummyModels();

        IControlPoint cp1 = r.getControlPoint(0);
        IControlPoint cp2 = r.getControlPoint(1);

        cp1.setPolarAngle(Math.PI / 12);
        cp1.setPolarAmount(0.3);

        cp2.setPolarAngle(Math.PI / 12);
        cp2.setPolarAmount(0.3);

        sn = new DummyNodePart(sourceNode);
        tn = new DummyNodePart();
        rel = new DummyRelPart();

        activateParts(layer);

        ((IReferencedFigure) tn.getFigure()).setReference(cursorPosition);

        feedbackService = (IFeedbackService) viewer
                .getService(IFeedbackService.class);

        if (feedbackService != null) {
            SelectionFigure selectionDummy = feedbackService
                    .setSelected(sn.getFigure());
            //selectionDummy.setSelectionColor(ColorUtils.getColor(MindMapUI.COLOR_WARNING));
            selectionDummy.setSelectionAlpha(0x90);
        }
    }

    public RelationshipDummy(IFigure layer, IRelationshipPart sourceRel,
            int pointId, IGraphicalViewer viewer) {
        this.viewer = viewer;
        createDummyModels();

        IRelationship sr = sourceRel.getRelationship();
        r.setStyleId(sr.getStyleId());
        r.getControlPoint(0).setPosition(sr.getControlPoint(0).getPosition());
        r.getControlPoint(1).setPosition(sr.getControlPoint(1).getPosition());

        r.getControlPoint(0)
                .setPolarAngle(sr.getControlPoint(0).getPolarAngle());
        r.getControlPoint(0)
                .setPolarAmount(sr.getControlPoint(0).getPolarAmount());
        r.getControlPoint(1)
                .setPolarAngle(sr.getControlPoint(1).getPolarAngle());
        r.getControlPoint(1)
                .setPolarAmount(sr.getControlPoint(1).getPolarAmount());

//        if (cp != null) {
//            r.setControlPoint(0, cp.getAngle(), cp.getAmount());
//        }
//        cp = sr.getControlPoint(1);
//        if (cp != null) {
//            r.setControlPoint(1, cp.getAngle(), cp.getAmount());
//        }

        sn = tn = null;
        if (pointId == MindMapUI.SOURCE_ANCHOR) {
            sn = new DummyNodePart();
        } else if (pointId == MindMapUI.TARGET_ANCHOR) {
            tn = new DummyNodePart();
        }
        if (sn == null)
            sn = new DummyNodePart(sourceRel.getSourceNode());
        if (tn == null)
            tn = new DummyNodePart(sourceRel.getTargetNode());
        rel = new DummyRelPart();

        activateParts(layer);

        feedbackService = (IFeedbackService) viewer
                .getService(IFeedbackService.class);

        if (feedbackService != null) {
            feedback = new RelationshipFeedback(rel);
            feedback.setAlpha(0xe0);
            feedbackService.addFeedback(feedback);
        }
    }

    private void createDummyModels() {
        ISheet sheet = (ISheet) viewer.getAdapter(ISheet.class);
        IWorkbook wb = sheet.getOwnedWorkbook();
        t1 = wb.createTopic();
        t2 = wb.createTopic();
        r = wb.createRelationship();
        r.setEnd1Id(t1.getId());
        r.setEnd2Id(t2.getId());
    }

    private void activateParts(IFigure layer) {
        IPart root = viewer.getRootPart();
        sn.setModel(t1);
        sn.setParent(root);
        layer.add(sn.getFigure());
        sn.addNotify();
        sn.getStatus().activate();

        tn.setModel(t2);
        tn.setParent(root);
        layer.add(tn.getFigure());
        tn.addNotify();
        tn.getStatus().activate();

        rel.setModel(r);
        rel.setParent(root);
        layer.add(rel.getFigure());
        rel.addNotify();
        rel.getStatus().activate();
    }

    public INodePart getSourceNodeDummy() {
        return sn;
    }

    public INodePart getTargetNodeDummy() {
        return tn;
    }

    public IRelationshipPart getRelDummy() {
        return rel;
    }

    public void refreshFeedback() {
        if (feedbackService == null)
            return;
        if (feedback != null)
            feedback.update();
    }

    public void dispose() {
        if (feedbackService != null) {
            if (sn != null)
                feedbackService.removeSelection(sn.getFigure());
            feedbackService.removeFeedback(feedback);
            feedbackService = null;
        }

        if (rel != null) {
            rel.getStatus().deactivate();
            rel.removeNotify();
            rel.getFigure().getParent().remove(rel.getFigure());
            rel.setParent(null);
            rel = null;
        }
        if (tn != null) {
            tn.getStatus().deactivate();
            tn.removeNotify();
            tn.getFigure().getParent().remove(tn.getFigure());
            tn.setParent(null);
            tn = null;
        }
        if (sn != null) {
            sn.getStatus().deactivate();
            sn.removeNotify();
            sn.getFigure().getParent().remove(sn.getFigure());
            sn.setParent(null);
            sn = null;
        }
    }

}