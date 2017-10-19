package org.xmind.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.ui.IEditorPart;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.ui.commands.MindMapCommandConstants;
import org.xmind.ui.internal.MindMapUIPlugin;

public class AlignmentHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        alignment(MindMapHandlerUtil.findContributingEditor(event),
                event.getParameter(MindMapCommandConstants.ALIGNMENT_PARAM));
        return null;
    }

    private void alignment(IEditorPart editor, String param) {
        if (param == null || editor == null)
            return;

        int value = getAlignment(param);
        if (value == PositionConstants.NONE)
            return;

        IViewer viewer = MindMapUIPlugin.getAdapter(editor, IViewer.class);
        if (viewer == null)
            return;

        EditDomain domain = viewer.getEditDomain();
        if (domain == null)
            return;

        domain.handleRequest(new Request(GEF.REQ_ALIGN).setViewer(viewer)
                .setParameter(GEF.PARAM_ALIGNMENT, value));
    }

    private int getAlignment(String alignment) {
        if (MindMapCommandConstants.ALIGNMENT_PARAM_LEFT.equals(alignment)) {
            return PositionConstants.LEFT;
        } else if (MindMapCommandConstants.ALIGNMENT_PARAM_RIGHT
                .equals(alignment)) {
            return PositionConstants.RIGHT;
        } else if (MindMapCommandConstants.ALIGNMENT_PARAM_CENTER
                .equals(alignment)) {
            return PositionConstants.CENTER;
        } else
            if (MindMapCommandConstants.ALIGNMENT_PARAM_TOP.equals(alignment)) {
            return PositionConstants.TOP;
        } else if (MindMapCommandConstants.ALIGNMENT_PARAM_MIDDLE
                .equals(alignment)) {
            return PositionConstants.MIDDLE;
        } else if (MindMapCommandConstants.ALIGNMENT_PARAM_BOTTOM
                .equals(alignment)) {
            return PositionConstants.BOTTOM;
        }
        return PositionConstants.NONE;
    }

}
