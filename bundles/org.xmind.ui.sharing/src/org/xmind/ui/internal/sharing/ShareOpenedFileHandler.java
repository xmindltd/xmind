/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.sharing;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.sharing.ISharingService;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.internal.handlers.MindMapHandlerUtil;
import org.xmind.ui.mindmap.MindMapUI;

/**
 * 
 * @author Frank Shaka
 * 
 */
public class ShareOpenedFileHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        final IEditorPart editor = MindMapHandlerUtil
                .findContributingEditor(event);
        if (editor == null || !(MindMapUI.MINDMAP_EDITOR_ID
                .equals(editor.getSite().getId())))
            return null;

        shareOpenedFile(editor);

        return null;
    }

    private void shareOpenedFile(final IEditorPart editor) {
        final ISharingService sharingService = LocalNetworkSharingUI
                .getDefault().getSharingService();
        if (sharingService == null) {
            LocalNetworkSharingUI.log(
                    "Failed to share opened file in local network: No sharing service available.", //$NON-NLS-1$
                    null);
            return;
        }

        if (!PlatformUI.isWorkbenchRunning())
            return;

        final IWorkbench workbench = PlatformUI.getWorkbench();
        final Display display = workbench.getDisplay();
        if (display == null || display.isDisposed())
            return;

        display.syncExec(new Runnable() {

            public void run() {
                File file = MME.getFile(editor.getEditorInput());
                if (file == null || !file.exists()) {
                    if (editor instanceof ISaveablePart) {
                        if (!MessageDialog.openConfirm(
                                Display.getCurrent().getActiveShell(),
                                SharingMessages.CommonDialogTitle_LocalNetworkSharing,
                                SharingMessages.ShareOpenedFileHandler_SaveTipMessageDialog_text))
                            return;

                        ((ISaveablePart) editor).doSaveAs();
                        file = MME.getFile(editor.getEditorInput());
                        if (file == null || !file.exists())
                            // We don't show warning here because user must have
                            // canceled the Save As process.
                            return;
                    } else {
                        MessageDialog.openInformation(
                                editor.getSite().getShell(),
                                SharingMessages.CommonDialogTitle_LocalNetworkSharing,
                                SharingMessages.OpenedEditorHasNoXMindFileToShare_dialogMessage);
                        return;
                    }
                }

                SharingUtils.addSharedMaps(display.getActiveShell(),
                        sharingService, new File[] { file });

            }
        });
    }

}
