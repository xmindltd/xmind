package org.xmind.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.ui.commands.MindMapCommandConstants;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.actions.ActionConstants;

public class SortHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        sort(MindMapHandlerUtil.findContributingEditor(event),
                event.getParameter(MindMapCommandConstants.SORT_PARAM));
        return null;
    }

    private void sort(IEditorPart editor, String param) {
        if (param == null || editor == null)
            return;

        String value = getSortId(param);
        if (value == null)
            return;

        IViewer viewer = MindMapUIPlugin.getAdapter(editor, IViewer.class);
        if (viewer == null)
            return;

        EditDomain domain = viewer.getEditDomain();
        if (domain == null)
            return;

        domain.handleRequest(new Request(GEF.REQ_SORT).setViewer(viewer)
                .setParameter(GEF.PARAM_COMPARAND, value));
    }

    private String getSortId(String param) {
        if (MindMapCommandConstants.SORT_PARAM_TITLE.equals(param)) {
            return ActionConstants.SORT_TITLE_ID;
        } else if (MindMapCommandConstants.SORT_PARAM_PRIORITY.equals(param)) {
            return ActionConstants.SORT_PRIORITY_ID;
        } else if (MindMapCommandConstants.SORT_PARAM_MODIFIED.equals(param)) {
            return ActionConstants.SORT_MODIFIED_ID;
        }
        return null;
    }

}
