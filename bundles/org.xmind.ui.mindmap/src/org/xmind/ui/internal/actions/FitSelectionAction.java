package org.xmind.ui.internal.actions;

import org.xmind.gef.GEF;
import org.xmind.gef.ui.actions.RequestAction;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.actions.MindMapActionFactory;

@Deprecated
public class FitSelectionAction extends RequestAction {

    public FitSelectionAction(IGraphicalEditorPage page) {
        super(MindMapActionFactory.FIT_SELECTION.getId(), page,
                GEF.REQ_FITSELECTION);
    }

}
