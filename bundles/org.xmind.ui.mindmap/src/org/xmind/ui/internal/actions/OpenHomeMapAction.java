package org.xmind.ui.internal.actions;

import java.io.File;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.dialogs.DialogMessages;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.prefs.PrefConstants;
import org.xmind.ui.util.PrefUtils;

@Deprecated
public class OpenHomeMapAction extends Action implements IWorkbenchAction {

    private IWorkbenchWindow window;

    public OpenHomeMapAction(IWorkbenchWindow window) {
        super(MindMapMessages.OpenHomeMap_text);
        if (window == null)
            throw new IllegalArgumentException();
        this.window = window;
        setId("org.xmind.ui.openHomeMap"); //$NON-NLS-1$
        setToolTipText(MindMapMessages.OpenHomeMap_toolTip);
        setActionDefinitionId("org.xmind.ui.command.openHomeMap"); //$NON-NLS-1$
    }

    @Override
    public void run() {
        if (window == null)
            return;

        Shell shell = window.getShell();
        final IWorkbenchPage page = window.getActivePage();
        openHomeMap(shell, page);
    }

    public void dispose() {
        window = null;
    }

    public static boolean openHomeMap(final Shell shell,
            final IWorkbenchPage page) {
        final String path = MindMapUIPlugin.getDefault().getPreferenceStore()
                .getString(PrefConstants.HOME_MAP_LOCATION);
        if (path == null || "".equals(path)) {//$NON-NLS-1$
            PrefUtils.openPrefDialog(shell, PrefUtils.GENERAL_PREF_PAGE_ID);
            return false;
        }
        if (!new File(path).exists()) {
            Display.getCurrent().asyncExec(new Runnable() {
                public void run() {
                    String dialogMessage = MindMapMessages.OpenHomeMapAction_HomeMapMissingMessage;
                    String[] dialogButtonLabels = { IDialogConstants.OK_LABEL,
                            MindMapMessages.OpenHomeMapAction_LaterOperationButton };
                    int index = new MessageDialog(shell,
                            org.xmind.ui.dialogs.IDialogConstants.COMMON_TITLE,
                            null, dialogMessage, MessageDialog.WARNING,
                            dialogButtonLabels, 0).open();
                    if (index == 0) {
                        PrefUtils.openPrefDialog(shell,
                                PrefUtils.GENERAL_PREF_PAGE_ID);
                    }
                }
            });
            return false;
        }

        final boolean opened[] = new boolean[1];
        opened[0] = false;
        String errMessage = NLS
                .bind(DialogMessages.FailedToLoadWorkbook_message, path);
        SafeRunner.run(new SafeRunnable(errMessage) {
            public void run() throws Exception {
                IEditorInput input = MindMapUI.getEditorInputFactory()
                        .createEditorInputForFile(new File(path));
                IEditorPart editor = page.openEditor(input,
                        MindMapUI.MINDMAP_EDITOR_ID);
                opened[0] = editor != null;
            }
        });
        return opened[0];
    }

}
