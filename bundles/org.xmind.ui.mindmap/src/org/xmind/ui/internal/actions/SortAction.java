package org.xmind.ui.internal.actions;

import org.eclipse.ui.actions.RetargetAction;
import org.xmind.ui.internal.MindMapMessages;

public class SortAction extends RetargetAction {

    public SortAction(String id) {
        super(id, null);
        if (ActionConstants.SORT_TITLE_ID.equals(id)) {
            setText(MindMapMessages.SortByTitle_text);
            setToolTipText(MindMapMessages.SortByTitle_toolTip);
        } else if (ActionConstants.SORT_PRIORITY_ID.equals(id)) {
            setText(MindMapMessages.SortByPriority_text);
            setToolTipText(MindMapMessages.SortByPriority_toolTip);
        } else if (ActionConstants.SORT_MODIFIED_ID.equals(id)) {
            setText(MindMapMessages.SortByModifiedTime_text);
            setToolTipText(MindMapMessages.SortByModifiedTime_toolTip);
        }
    }

}
