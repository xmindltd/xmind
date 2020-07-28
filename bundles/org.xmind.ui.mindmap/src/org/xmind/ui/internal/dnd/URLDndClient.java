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

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.dnd.URLTransfer;
import org.xmind.core.IImage;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.util.FileUtils;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.part.IPart;
import org.xmind.ui.util.ImageFormat;
import org.xmind.ui.util.Logger;

public class URLDndClient extends MindMapDNDClientBase {

    private URLTransfer transfer = URLTransfer.getInstance();

    public Object getData(Transfer transfer, TransferData data) {
        if (transfer == this.transfer)
            return this.transfer.nativeToJava(data);
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
        String url = (String) transferData;
        if (workbook != null) {
            try {
                URI uri = new URI(url);
                if (targetParent != null && dropInParent) {
                    if (isImageURL(uri)) {
                        return new Object[] { createModifyImageCommand(
                                targetParent, uri.toString(),
                                IImage.UNSPECIFIED, IImage.UNSPECIFIED, null) };
                    } else {
                        return new Object[] { uri };
                    }
                } else {
                    ITopic topic = workbook.createTopic();
                    if (isImageURL(uri)) {
                        topic.getImage().setSource(uri.toString());
                    } else {
                        topic.setTitleText(url);
                        topic.setHyperlink(url);
                    }
                    return new Object[] { topic };
                }
            } catch (URISyntaxException e) {
                Logger.log("[URLDndClient] Failed to parse invalid URL: " + url); //$NON-NLS-1$
            }
        }
        return null;
    }

    private boolean isImageURL(URI uri) {
        String path = uri.getPath();
        if (path == null)
            return false;
        return ImageFormat.findByExtension(FileUtils.getExtension(path), null) != null;
    }

    @Override
    public boolean canLink(TransferData data, IViewer viewer, Point location,
            IPart target) {
        return true;
    }
}