package org.xmind.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.gef.EditDomain;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.Request;
import org.xmind.ui.commands.MindMapCommandConstants;
import org.xmind.ui.mindmap.MindMapUI;

public class AddMarkerHandler extends AbstractHandler {

    private static final String DEFAULT_MARKER_ID = "priority-1"; //$NON-NLS-1$

    public Object execute(ExecutionEvent event) throws ExecutionException {
        addMarker(event);
        return null;
    }

    private void addMarker(ExecutionEvent event) {
        IEditorPart editor = HandlerUtil.getActiveEditor(event);
        if (editor == null)
            return;

        IGraphicalViewer viewer = editor.getAdapter(IGraphicalViewer.class);
        if (viewer == null)
            return;

        EditDomain domain = viewer.getEditDomain();
        if (domain == null)
            return;

        Request request = new Request(MindMapUI.REQ_ADD_MARKER);
        request.setViewer(viewer);
        request.setDomain(domain);

        String markerId = event.getParameter(
                MindMapCommandConstants.ADD_MARKER_PARAM_MARKER_ID);
        if (markerId == null)
            markerId = DEFAULT_MARKER_ID;
        request.setParameter(MindMapUI.PARAM_MARKER_ID, markerId);
        domain.handleRequest(request);
    }

}
