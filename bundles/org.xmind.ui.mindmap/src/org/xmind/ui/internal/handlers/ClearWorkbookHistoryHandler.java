package org.xmind.ui.internal.handlers;

import java.net.URI;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.xmind.ui.IEditorHistory.IEditorHistoryListener;
import org.xmind.ui.internal.dialogs.DialogMessages;
import org.xmind.ui.mindmap.MindMapUI;

public class ClearWorkbookHistoryHandler extends AbstractHandler
        implements IEditorHistoryListener {

    public ClearWorkbookHistoryHandler() {
        MindMapUI.getEditorHistory().addEditorHistoryListener(this);
        setBaseEnabled(calcEnabledState());
    }

    public Object execute(ExecutionEvent event) throws ExecutionException {
        doExecute(event);
        return null;
    }

    private void doExecute(ExecutionEvent event) {
        Shell shell = HandlerUtil.getActiveShell(event);
        if (shell == null || shell.isDisposed())
            return;

        if (!MessageDialog.openConfirm(shell,
                DialogMessages.ConfirmClearRecentFileListDialog_title,
                DialogMessages.ConfirmClearRecentFileListDialog_message))
            return;

        MindMapUI.getEditorHistory().clear();
    }

    public void editorHistoryChanged() {
        setBaseEnabled(calcEnabledState());
    }

    private boolean calcEnabledState() {
        int itemsToShow = WorkbenchPlugin.getDefault().getPreferenceStore()
                .getInt(IPreferenceConstants.RECENT_FILES);
        if (itemsToShow <= 0)
            return false;

        URI[] items = MindMapUI.getEditorHistory()
                .getRecentInputURIs(itemsToShow);
        if (items.length == 0)
            return false;

        return true;
    }

}
