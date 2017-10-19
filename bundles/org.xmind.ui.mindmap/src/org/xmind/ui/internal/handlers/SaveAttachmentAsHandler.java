package org.xmind.ui.internal.handlers;

import java.io.FileOutputStream;
import java.io.InputStream;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.core.IFileEntry;
import org.xmind.core.ITopic;
import org.xmind.core.util.FileUtils;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.ui.internal.dialogs.DialogMessages;

public class SaveAttachmentAsHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        ITopic sourceTopic = getSourceTopic(event);

        if (sourceTopic == null)
            return null;

        String url = sourceTopic.getHyperlink();
        if (url == null || !HyperlinkUtils.isAttachmentURL(url))
            return null;

        final String entryPath = HyperlinkUtils.toAttachmentPath(url);
        final IFileEntry entry = sourceTopic.getOwnedWorkbook().getManifest()
                .getFileEntry(entryPath);
        if (entry == null)
            return null;

        String ext = FileUtils.getExtension(entryPath);
        FileDialog dialog = new FileDialog(
                Display.getCurrent().getActiveShell(), SWT.SAVE);

        dialog.setFilterExtensions(new String[] { "*" + ext, "*.*" }); //$NON-NLS-1$//$NON-NLS-2$    
        String extension = ext;
        if (ext != null) {
            Program p = Program.findProgram(ext);
            if (p != null) {
                extension = p.getName();
            }
        }
        dialog.setFilterNames(new String[] { extension, NLS.bind("{0} (*.*)", //$NON-NLS-1$
                DialogMessages.AllFilesFilterName) });
        String name = sourceTopic.getTitleText();
        if (name != null && !name.endsWith(ext)) {
            name += ext;
        }
        if (name != null) {
            dialog.setFileName(name);
        }
        dialog.setOverwrite(true);
        final String targetPath = dialog.open();
        if (targetPath == null)
            return null;

        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                InputStream is = entry.openInputStream();
                try {
                    FileOutputStream os = new FileOutputStream(targetPath);
                    try {
                        FileUtils.transfer(is, os, true);
                    } finally {
                        os.close();
                    }
                } finally {
                    is.close();
                }
            }
        });

        return null;
    }

    private ITopic getSourceTopic(ExecutionEvent event) {
        ISelection selection = HandlerUtil.getCurrentSelection(event);

        if (selection instanceof IStructuredSelection) {
            for (Object element : ((IStructuredSelection) selection).toList()) {
                if (element instanceof ITopic) {
                    return (ITopic) element;

                }
            }
        }

        return null;
    }
}
