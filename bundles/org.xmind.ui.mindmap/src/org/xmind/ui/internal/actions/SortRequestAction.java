package org.xmind.ui.internal.actions;

import org.eclipse.jface.viewers.ISelection;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.ui.actions.ISelectionAction;
import org.xmind.gef.ui.actions.RequestAction;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.util.MindMapUtils;

public class SortRequestAction extends RequestAction implements
        ISelectionAction {

    public SortRequestAction(IGraphicalEditorPage page, String id) {
        super(page, GEF.REQ_SORT);
        setId(id);
    }

    public void run() {
        if (isDisposed())
            return;
        Request request = new Request(getRequestType());
        request.setDomain(getEditDomain());
        request.setViewer(getViewer());
        request.setParameter(GEF.PARAM_COMPARAND, getId());
        sendRequest(request);
    }

    public void setSelection(ISelection selection) {
        setEnabled(MindMapUtils.isSingleTopic(selection));
    }

}
