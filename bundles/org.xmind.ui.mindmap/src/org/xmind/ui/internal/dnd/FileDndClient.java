/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.dnd;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.DPIUtil;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.xmind.core.IFileEntry;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.util.FileUtils;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.gef.GEF;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.part.IPart;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.dialogs.DialogMessages;
import org.xmind.ui.internal.protocols.FilePathParser;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.prefs.PrefConstants;
import org.xmind.ui.util.ImageFormat;
import org.xmind.ui.util.Logger;

@SuppressWarnings("restriction")
public class FileDndClient extends MindMapDNDClientBase {

    private static final String CREATE_IMAGE = "CREATE_IMAGE"; //$NON-NLS-1$

    private static final String ADD_EXTERNAL_FILE = "dndConfirm.ExternalFile"; //$NON-NLS-1$

    private class FileDropHandler {

        private String path;
        private String active;

        public FileDropHandler(String path, String action) {
            this.path = path;
            this.active = action;
        }

        public void createViewerElements(IWorkbook workbook,
                ITopic targetParent, List<Object> elements) {
            File file = new File(path);
            if (PrefConstants.CREATE_HYPERLINK.equals(active)) {
                elements.add(createFileHyperlinkTopic(workbook, file));
            } else if (PrefConstants.CREATE_ATTACHMENT.equals(active)) {
                elements.add(createAttachmentTopic(workbook, file, null));
            } else if (CREATE_IMAGE.equals(active)) {
                if (targetParent != null) {
                    elements.add(
                            createImageOnTopic(workbook, targetParent, file));
                }
            }
        }

        private ITopic createFileHyperlinkTopic(IWorkbook workbook, File file) {
            ITopic topic = workbook.createTopic();
            topic.setTitleText(file.getName());
            topic.setHyperlink(
                    FilePathParser.toURI(file.getAbsolutePath(), false));
            return topic;
        }

        private ITopic createAttachmentTopic(IWorkbook workbook, File file,
                ITopic parent) {
            ITopic topic = workbook.createTopic();
            topic.setTitleText(file.getName());
            if (file.isDirectory()) {
                String[] subfiles = file.list();
                for (int i = 0; i < subfiles.length; i++) {
                    File subfile = new File(file, subfiles[i]);
                    ITopic subtopic = createAttachmentTopic(workbook, subfile,
                            topic);
                    topic.add(subtopic, ITopic.ATTACHED);
                }
            } else {
                try {
                    IFileEntry entry = workbook.getManifest()
                            .createAttachmentFromFilePath(
                                    file.getAbsolutePath());
                    entry.decreaseReference();
                    entry.increaseReference();
                    if (isImagePath(file.getAbsolutePath())) {
                        Dimension size = getImageSize(file.getAbsolutePath());
                        if (size != null) {
                            topic.getImage().setSource(HyperlinkUtils
                                    .toAttachmentURL(entry.getPath()));
                            topic.getImage().setSize(size.width, size.height);
                            topic.setTitleText(""); //$NON-NLS-1$
                        } else {
                            topic.setHyperlink(HyperlinkUtils
                                    .toAttachmentURL(entry.getPath()));
                        }
                    } else {
                        topic.setHyperlink(HyperlinkUtils
                                .toAttachmentURL(entry.getPath()));
                    }
                } catch (IOException e) {
                    Logger.log(e, "Error occurred when transfering file: " //$NON-NLS-1$
                            + file.getAbsolutePath());
                }
            }
            return topic;
        }

        private Object createImageOnTopic(IWorkbook workbook, ITopic topic,
                File file) {
            try {
                IFileEntry entry = workbook.getManifest()
                        .createAttachmentFromFilePath(file.getAbsolutePath());
                entry.decreaseReference();
                entry.increaseReference();

                Dimension size = getImageSize(file.getAbsolutePath());
                if (size != null) {
                    return createModifyImageCommand(topic,
                            HyperlinkUtils.toAttachmentURL(entry.getPath()),
                            size.width, size.height, null);
                } else {
                    Logger.log(
                            "[FileDndClient] Failed to open invalid image file: " //$NON-NLS-1$
                                    + file.getAbsolutePath());
                }
            } catch (IOException e) {
                Logger.log(e, "Error occurred when transfering file: " //$NON-NLS-1$
                        + file.getAbsolutePath());
            }
            return null;
        }
    }

    private FileTransfer transfer = FileTransfer.getInstance();

    private IPreferenceStore pref = null;

    public Object getData(Transfer transfer, TransferData data) {
        if (transfer == this.transfer) {
            return this.transfer.nativeToJava(data);
        }
        return null;
    }

    public Transfer getTransfer() {
        return transfer;
    }

    public Object toTransferData(Object[] viewerElements, IViewer viewer) {
        return null;
    }

    @Override
    protected Object[] toViewerElements(Object transferData, Request request,
            IWorkbook workbook, ITopic targetParent, boolean dropInParent) {
        if (transferData instanceof String[]) {
            String[] paths = (String[]) transferData;
            if (workbook != null) {
                List<FileDropHandler> handlers = createFileDropHandlers(paths,
                        request.getIntParameter(GEF.PARAM_DROP_OPERATION,
                                DND.DROP_DEFAULT),
                        dropInParent,
                        request.getTargetViewer().getControl().getShell());
                if (handlers != null) {
                    List<Object> elements = new ArrayList<Object>(
                            handlers.size());
                    for (FileDropHandler handler : handlers) {
                        handler.createViewerElements(workbook, targetParent,
                                elements);
                    }
                    return elements.toArray();
                }
            }
        }
        return null;
    }

    private List<FileDropHandler> createFileDropHandlers(String[] paths,
            int operation, boolean dropInParent, Shell shell) {
        List<FileDropHandler> handlers = new ArrayList<FileDropHandler>(
                paths.length);
        if (isSingleImage(paths) && dropInParent) {
            createImageFileDropHandler(paths[0], operation, handlers);
        } else if (isSingleFolder(paths)) {
            createSingleFolderDropHandler(paths[0], operation, handlers, shell);
        } else if (isSingleFile(paths)) {
            createSingleFileDropHandler(paths[0], operation, handlers, shell);
        } else {
            createMultipleFilesDropHandler(paths, operation, handlers, shell);
        }
        return handlers;
    }

    private void createImageFileDropHandler(String imagePath, int operation,
            List<FileDropHandler> handlers) {
        if (operation == DND.DROP_LINK) {
            createFileDropHandlers(handlers, PrefConstants.CREATE_HYPERLINK,
                    imagePath);
        } else {
            createFileDropHandlers(handlers, CREATE_IMAGE, imagePath);
        }
    }

    private void createSingleFolderDropHandler(String path, int operation,
            List<FileDropHandler> handlers, Shell shell) {
        if (operation == DND.DROP_LINK) {
            createFileDropHandlers(handlers, PrefConstants.CREATE_HYPERLINK,
                    path);
        } else if (operation == DND.DROP_COPY) {
            createFileDropHandlers(handlers, PrefConstants.CREATE_ATTACHMENT,
                    path);
        } else {
            askForConfirmation(shell, DialogMessages.DND_ExternalFolder,
                    ADD_EXTERNAL_FILE,
                    NLS.bind(
                            DialogMessages.DND_ExternalFolder_confirmation_with_path,
                            path),
                    handlers, path);
        }
    }

    private void createSingleFileDropHandler(String path, int operation,
            List<FileDropHandler> handlers, Shell shell) {
        if (operation == DND.DROP_LINK) {
            createFileDropHandlers(handlers, PrefConstants.CREATE_HYPERLINK,
                    path);
        } else if (operation == DND.DROP_COPY) {
            createFileDropHandlers(handlers, PrefConstants.CREATE_ATTACHMENT,
                    path);
        } else {
            askForConfirmation(shell, DialogMessages.DND_ExternalFile,
                    ADD_EXTERNAL_FILE,
                    NLS.bind(
                            DialogMessages.DND_ExternalFile_confirmation_with_path_size,
                            path,
                            org.xmind.ui.viewers.FileUtils.fileLengthToString(
                                    new File(path).length())),
                    handlers, path);
        }
    }

    private void createMultipleFilesDropHandler(String[] paths, int operation,
            List<FileDropHandler> handlers, Shell shell) {
        if (operation == DND.DROP_LINK) {
            createFileDropHandlers(handlers, PrefConstants.CREATE_HYPERLINK,
                    paths);
        } else if (operation == DND.DROP_COPY) {
            createFileDropHandlers(handlers, PrefConstants.CREATE_ATTACHMENT,
                    paths);
        } else {
            StringBuffer sb = new StringBuffer(paths.length * 30);
            for (int i = 0; i < paths.length; i++) {
                File file = new File(paths[i]);
                if (i < 3) {
                    if (i > 0) {
                        sb.append('\r');
                        sb.append('\n');
                    }
                    sb.append(paths[i]);
                    if (!file.isDirectory()) {
                        sb.append(' ');
                        sb.append('(');
                        long size = file.length();
                        sb.append(org.xmind.ui.viewers.FileUtils
                                .fileLengthToString(size));
                        sb.append(')');
                    }
                } else {
                    sb.append('\r');
                    sb.append('\n');
                    sb.append(NLS.bind(
                            DialogMessages.DND_MultipleExternalFiles_moreFiles_with_number,
                            paths.length - 3));
                    break;
                }
            }
            askForConfirmation(shell, DialogMessages.DND_MultipleExternalFiles,
                    ADD_EXTERNAL_FILE,
                    NLS.bind(
                            DialogMessages.DND_MultipleExternalFiles_confirmation_with_fileList,
                            sb.toString()),
                    handlers, paths);
        }
    }

    private void askForConfirmation(Shell shell, String itemName,
            String prefKey, String dialogMessage,
            List<FileDropHandler> handlers, String... paths) {
        String active = getPref().getString(prefKey);
        if ("".equals(active) || PrefConstants.ASK_USER.equals(active)) { //$NON-NLS-1$
            active = null;
        }
        if (active == null) {
            shell.forceActive();
            MessageDialogWithToggle dialog = new MessageDialogWithToggle(shell,
                    NLS.bind(
                            DialogMessages.DND_ConfirmDroppingFileDialog_title_with_type,
                            itemName),
                    null, //
                    dialogMessage, //
                    SWT.ICON_QUESTION, //
                    new String[] { //
                            DialogMessages.DND_ConfirmDroppingFileDialog_LinkButton_text, //
                            DialogMessages.DND_ConfirmDroppingFileDialog_CopyButton_text, //
                            IDialogConstants.CANCEL_LABEL //
                    }, 0, //
                    NLS.bind(
                            DialogMessages.DND_ConfirmDroppingFileDialog_RememberCheck_text_with_type,
                            itemName),
                    false);
            int ret = dialog.open();
            if (ret == IDialogConstants.INTERNAL_ID) {
                active = PrefConstants.CREATE_HYPERLINK;
            } else if (ret == IDialogConstants.INTERNAL_ID + 1) {
                active = PrefConstants.CREATE_ATTACHMENT;
            }
            if (dialog.getToggleState() && active != null) {
                getPref().setValue(prefKey, active);
            }
        }
        if (active != null) {
            createFileDropHandlers(handlers, active, paths);
        }
    }

    private void createFileDropHandlers(List<FileDropHandler> handlers,
            String action, String... paths) {
        for (int i = 0; i < paths.length; i++) {
            handlers.add(new FileDropHandler(paths[i], action));
        }
    }

    private IPreferenceStore getPref() {
        if (pref == null) {
            pref = MindMapUIPlugin.getDefault().getPreferenceStore();
        }
        return pref;
    }

    private static boolean isSingleImage(String[] paths) {
        return paths.length == 1 && isImagePath(paths[0]);
    }

    private static boolean isImagePath(String path) {
        String ext = FileUtils.getExtension(path);
        return ImageFormat.findByExtension(ext, null) != null;
    }

    private static boolean isSingleFolder(String[] paths) {
        return paths.length == 1 && new File(paths[0]).isDirectory();
    }

    private static boolean isSingleFile(String[] paths) {
        return paths.length == 1;
    }

    private static Dimension getImageSize(String path) {
        try {
            Image tempImage = new Image(Display.getCurrent(), path);
            Rectangle imageBounds = tempImage.getBounds();
            tempImage.dispose();
            boolean needZoom = DPIUtil.getDeviceZoom() > 100;
            int width = needZoom ? imageBounds.width / 2 : imageBounds.width;
            int height = needZoom ? imageBounds.height / 2 : imageBounds.height;
            return Geometry.getScaledConstrainedSize(width, height,
                    MindMapUI.IMAGE_INIT_WIDTH, MindMapUI.IMAGE_INIT_HEIGHT);
        } catch (Throwable e) {
        }
        return null;
    }

    public boolean canLink(TransferData data, IViewer viewer, Point location,
            IPart target) {
        return true;
    }

}
