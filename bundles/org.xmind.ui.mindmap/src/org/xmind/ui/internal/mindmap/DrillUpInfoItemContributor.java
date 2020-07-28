package org.xmind.ui.internal.mindmap;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.gef.EditDomain;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.IViewer;
import org.xmind.ui.mindmap.AbstractInfoItemContributor;
import org.xmind.ui.mindmap.IDrillDownTraceService;
import org.xmind.ui.mindmap.IInfoPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;

public class DrillUpInfoItemContributor extends AbstractInfoItemContributor {

    private static class IconTipDrillUpAction extends Action {

        private IViewer viewer;

        public IconTipDrillUpAction(IViewer viewer) {
            this.viewer = viewer;
        }

        public void run() {
            EditDomain domain = viewer.getEditDomain();
            if (domain == null)
                return;

            domain.handleRequest(MindMapUI.REQ_DRILLUP, viewer);
        }
    }

    public IAction createAction(ITopicPart topicPart, ITopic topic) {
        IViewer viewer = topicPart.getSite().getViewer();
        if (viewer != null && topic.equals(viewer.getAdapter(ITopic.class))
                && topic.getParent() != null && hasTraceService(viewer)) {
            return new IconTipDrillUpAction(viewer);
        }
        return null;
    }

    public String getSVGFilePath(ITopic topic, IAction action) {
        return "platform:/plugin/org.xmind.ui.mindmap/icons/drill_up.svg"; //$NON-NLS-1$
    }

    private boolean hasTraceService(IViewer viewer) {
        if (viewer instanceof IGraphicalViewer) {
            return ((IGraphicalViewer) viewer)
                    .getService(IDrillDownTraceService.class) != null;
        }
        return false;
    }

    @Override
    protected void registerTopicEvent(ITopicPart topicPart, ITopic topic,
            ICoreEventRegister register) {
    }

    @Override
    protected void handleTopicEvent(IInfoPart infoPart, CoreEvent event) {
    }

    @Override
    protected void handleTopicEvent(ITopicPart topicPart, CoreEvent event) {
    }

}
