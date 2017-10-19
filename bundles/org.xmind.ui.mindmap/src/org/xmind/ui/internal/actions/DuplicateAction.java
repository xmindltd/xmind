package org.xmind.ui.internal.actions;

import org.eclipse.jface.viewers.ISelection;
import org.xmind.gef.ui.actions.ISelectionAction;
import org.xmind.gef.ui.actions.RequestAction;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.actions.MindMapActionFactory;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class DuplicateAction extends RequestAction implements ISelectionAction {

    public DuplicateAction(IGraphicalEditorPage page) {
        super(MindMapActionFactory.DUPLICATE.getId(), page,
                MindMapUI.REQ_DUPLICATE_TOPIC);
        setActionDefinitionId(MindMapActionFactory.DUPLICATE.getCommandId());
    }

    public void setSelection(ISelection selection) {
        setEnabled(MindMapUtils.isSingleTopic(selection)
                && !MindMapUtils.hasCentralTopic(selection, getViewer()));
    }

}