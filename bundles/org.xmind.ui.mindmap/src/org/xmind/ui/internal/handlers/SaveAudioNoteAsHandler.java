package org.xmind.ui.internal.handlers;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

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
import org.xmind.core.IResourceRef;
import org.xmind.core.ITopic;
import org.xmind.core.ITopicExtension;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.internal.dialogs.DialogMessages;

public class SaveAudioNoteAsHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        ITopic topic = getSourceTopic(event);

        final IFileEntry entry = getAudioFileEntry(topic);
        if (entry == null) {
            return null;
        }

        FileDialog dialog = new FileDialog(
                Display.getCurrent().getActiveShell(), SWT.SAVE);
        String ext = FileUtils.getExtension(entry.getPath());
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
        String fileName = topic.getTitleText();
        if (fileName != null && !fileName.endsWith(ext)) {
            fileName += ext;
        }
        if (fileName != null) {
            dialog.setFileName(fileName);
        }
        dialog.setOverwrite(true);
        final String filePath = dialog.open();
        if (filePath == null) {
            return null;
        }

        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                InputStream is = entry.openInputStream();
                try {
                    FileOutputStream os = new FileOutputStream(filePath);
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

    private IFileEntry getAudioFileEntry(ITopic topic) {
        if (topic == null) {
            return null;
        }
        String entryPath = getAudioEntryPath(topic);
        if (entryPath == null) {
            return null;
        }
        return topic.getOwnedWorkbook().getManifest().getFileEntry(entryPath);
    }

    private String getAudioEntryPath(ITopic topic) {
        ITopicExtension extension = topic
                .getExtension("org.xmind.ui.audionotes"); //$NON-NLS-1$
        if (extension != null) {
            List<IResourceRef> resourceRefs = extension.getResourceRefs();
            if (!resourceRefs.isEmpty()) {
                for (IResourceRef ref : resourceRefs) {
                    if (IResourceRef.FILE_ENTRY.equals(ref.getType())) {
                        String entryPath = ref.getResourceId();
                        if (entryPath != null)
                            return entryPath;
                    }
                }
            }
        }
        return null;
    }
}
