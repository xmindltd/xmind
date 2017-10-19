package org.xmind.ui.internal.dnd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.ImageTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.xmind.core.Core;
import org.xmind.core.IFileEntry;
import org.xmind.core.IImage;
import org.xmind.core.IManifest;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.ui.internal.AttachmentImageDescriptor;
import org.xmind.ui.util.Logger;

public class ImageDndClient extends MindMapDNDClientBase {

    private ImageTransfer transfer = ImageTransfer.getInstance();

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
        if (viewerElements != null && viewerElements.length > 0) {
            for (Object element : viewerElements) {
                if (element instanceof IImage) {
                    IImage image = (IImage) element;
                    String source = image.getSource();
                    if (source != null) {
                        if (HyperlinkUtils.isAttachmentURL(source)) {
                            String path = HyperlinkUtils
                                    .toAttachmentPath(source);
                            ImageDescriptor imageDescriptor = AttachmentImageDescriptor
                                    .createFromEntryPath(
                                            image.getOwnedWorkbook(), path);
                            ImageData imageData = imageDescriptor
                                    .getImageData();
                            return imageData;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected Object[] toViewerElements(Object transferData, Request request,
            IWorkbook workbook, ITopic targetParent, boolean dropInParent) {
        if (transferData instanceof ImageData) {
            if (workbook != null) {
                ImageData imageData = (ImageData) transferData;
                ImageLoader saver = new ImageLoader();
                saver.data = new ImageData[] { imageData };
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                try {
                    saver.save(os, SWT.IMAGE_PNG);
                } finally {
                    try {
                        os.close();
                    } catch (IOException e) {
                    }
                }
                byte[] imageDataInBytes = os.toByteArray();
                IManifest manifest = workbook.getManifest();
                try {
                    ByteArrayInputStream is = new ByteArrayInputStream(
                            imageDataInBytes);
                    IFileEntry entry;
                    try {
                        entry = manifest.createAttachmentFromStream(is,
                                "temp.png", Core.MEDIA_TYPE_IMAGE_PNG); //$NON-NLS-1$
                    } finally {
                        is.close();
                    }
                    String imageSource = HyperlinkUtils.toAttachmentURL(entry
                            .getPath());
                    if (targetParent != null && dropInParent) {
                        return new Object[] { createModifyImageCommand(
                                targetParent, imageSource, IImage.UNSPECIFIED,
                                IImage.UNSPECIFIED, null) };
                    }
                    ITopic topic = workbook.createTopic();
                    topic.getImage().setSource(imageSource);
                    return new Object[] { topic };
                } catch (IOException e) {
                    Logger.log(e,
                            "[ImageDndClient] Failed to create image entry."); //$NON-NLS-1$
                }
            }
        }
        return null;
    }

}
