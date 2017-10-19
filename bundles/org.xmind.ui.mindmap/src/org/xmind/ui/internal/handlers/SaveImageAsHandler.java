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
import org.xmind.core.IImage;
import org.xmind.core.ITopic;
import org.xmind.core.util.FileUtils;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.ui.internal.dialogs.DialogMessages;

public class SaveImageAsHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
        saveSelectedImageAsFile(selection);
        return null;
    }

    private void saveSelectedImageAsFile(ISelection selection) {
        IImage image = getSourceImage(selection);
        if (image == null)
            return;

        ITopic sourceTopic = image.getParent();
        if (sourceTopic == null)
            return;

        String url = image.getSource();
        if (url == null || !HyperlinkUtils.isAttachmentURL(url))
            return;

        final String entryPath = HyperlinkUtils.toAttachmentPath(url);
        final IFileEntry entry = sourceTopic.getOwnedWorkbook().getManifest()
                .getFileEntry(entryPath);
        if (entry == null)
            return;

        FileDialog dialog = new FileDialog(
                Display.getCurrent().getActiveShell(), SWT.SAVE);
        String ext = FileUtils.getExtension(entryPath);

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
            return;

        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                final InputStream is = entry.openInputStream();
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
    }

    private IImage getSourceImage(ISelection selection) {
        IImage image;
        if (selection instanceof IStructuredSelection) {
            for (Object element : ((IStructuredSelection) selection).toList()) {
                if (element instanceof ITopic) {
                    ITopic topic = (ITopic) element;
                    image = topic.getImage();
                    if (image.getSource() != null)
                        return image;
                } else if (element instanceof IImage) {
                    image = (IImage) element;
                    if (image.getSource() != null)
                        return image;
                }
            }
        }
        return null;
    }

}
