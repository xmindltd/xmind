package org.xmind.ui.internal.actions;

import org.eclipse.jface.viewers.ISelection;
import org.xmind.gef.ui.actions.ISelectionAction;
import org.xmind.gef.ui.actions.RequestAction;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.actions.MindMapActionFactory;
import org.xmind.ui.mindmap.MindMapUI;

public class RemoveAllStylesAction extends RequestAction implements
        ISelectionAction {

    public RemoveAllStylesAction(IGraphicalEditorPage page) {
        super(MindMapActionFactory.REMOVE_ALL_STYLES.getId(), page,
                MindMapUI.REQ_REMOVE_ALLSTYLES);
    }

    public void setSelection(ISelection selection) {
        setEnabled(true);
    }

}
