package org.xmind.ui.internal.mindmap;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.xmind.core.Core;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.gef.EditDomain;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.part.IPart;
import org.xmind.ui.mindmap.AbstractInfoItemContributor;
import org.xmind.ui.mindmap.IDrillDownTraceService;
import org.xmind.ui.mindmap.IInfoPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;

public class DrillDownInfoItemContributor extends AbstractInfoItemContributor {

    private static class IconTipDrillDownAction extends Action {

        private IViewer viewer;

        private ITopic topic;

        public IconTipDrillDownAction(IViewer viewer, ITopic topic) {
            this.viewer = viewer;
            this.topic = topic;
        }

        public void run() {
            EditDomain domain = viewer.getEditDomain();
            if (domain == null)
                return;

            IPart part = viewer.findPart(topic);
            if (part == null)
                return;

            Request request = new Request(MindMapUI.REQ_DRILLDOWN);
            request.setViewer(viewer);
            request.setPrimaryTarget(part);
            domain.handleRequest(request);
        }

    }

    public IAction createAction(ITopicPart topicPart, ITopic topic) {
        IViewer viewer = topicPart.getSite().getViewer();
        if (viewer != null && hasTraceService(viewer)
                && !topic.equals(viewer.getAdapter(ITopic.class))
                && !topic.getChildren(ITopic.DETACHED).isEmpty()) {
            return new IconTipDrillDownAction(viewer, topic);
        }
        return null;
    }

    public String getSVGFilePath(ITopic topic, IAction action) {
        return "platform:/plugin/org.xmind.ui.mindmap/icons/drill_down.svg"; //$NON-NLS-1$
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
        register.register(Core.TopicAdd);
        register.register(Core.TopicRemove);
    }

    @Override
    protected void handleTopicEvent(IInfoPart infoPart, CoreEvent event) {
    }

    @Override
    protected void handleTopicEvent(ITopicPart topicPart, CoreEvent event) {
        if (ITopic.DETACHED.equals(event.getData())) {
            topicPart.refresh();
        }
    }

}
