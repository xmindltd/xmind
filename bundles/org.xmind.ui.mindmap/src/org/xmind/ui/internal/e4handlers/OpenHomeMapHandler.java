package org.xmind.ui.internal.e4handlers;

import java.io.File;

import javax.inject.Inject;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.xmind.ui.dialogs.IDialogConstants;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.protocols.FilePathParser;
import org.xmind.ui.prefs.PrefConstants;
import org.xmind.ui.util.PrefUtils;

public class OpenHomeMapHandler {

    @Inject
    public void execute(IWorkbenchWindow window) {
        if (window == null)
            return;

        String filePath = MindMapUIPlugin.getDefault().getPreferenceStore()
                .getString(PrefConstants.HOME_MAP_LOCATION);
        String errorMessage = null;
        if (filePath == null || "".equals(filePath)) { //$NON-NLS-1$
            errorMessage = MindMapMessages.OpenHomeMap_Error_message;
        } else if (!new File(filePath).exists()) {
            errorMessage = MindMapMessages.OpenHomeMapAction_HomeMapMissingMessage;
        }

        if (errorMessage != null) {
            boolean ok = MessageDialog.openConfirm(window.getShell(),
                    IDialogConstants.COMMON_TITLE, errorMessage);
            if (!ok)
                return;

            PrefUtils.openPrefDialog(window.getShell(),
                    PrefUtils.GENERAL_PREF_PAGE_ID);
            filePath = MindMapUIPlugin.getDefault().getPreferenceStore()
                    .getString(PrefConstants.HOME_MAP_LOCATION);
        }

        if (filePath == null || "".equals(filePath) //$NON-NLS-1$
                || !new File(filePath).exists())
            return;

        OpenWorkbooksHandler.execute(window,
                FilePathParser.toURI(filePath, false));
    }

}
