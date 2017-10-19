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
package org.xmind.core.internal.dom;

import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmind.core.IAdaptable;
import org.xmind.core.IFileEntry;
import org.xmind.core.IIdentifiable;
import org.xmind.core.IManifest;
import org.xmind.core.IWorkbook;
import org.xmind.core.IWorkbookComponentRefManager;
import org.xmind.core.util.DOMUtils;
import org.xmind.core.util.HyperlinkUtils;

public class InternalHyperlinkUtils {

    private static boolean isInWorkingRevision(IAdaptable object) {
        Node node = (Node) object.getAdapter(Node.class);
        if (node != null) {
            Document doc = DOMUtils.getOwnerDocument(node);
            if (doc != null) {
                Element docEle = doc.getDocumentElement();
                if (docEle != null) {
                    return !DOMConstants.TAG_REVISION_CONTENT.equals(docEle
                            .getNodeName());
                }
            }
        }
        return true;
    }

    public static void activateHyperlink(IWorkbook workbook, String url,
            IAdaptable source) {
        if (workbook != null) {
            if (HyperlinkUtils.isAttachmentURL(url)) {
                if (isInWorkingRevision(source)) {
                    String attPath = HyperlinkUtils.toAttachmentPath(url);
                    increaseFileEntryRef(workbook, attPath);
                }
            } else if (HyperlinkUtils.isInternalURL(url)) {
                if (source instanceof IIdentifiable) {
                    String sourceId = ((IIdentifiable) source).getId();
                    String id = HyperlinkUtils.toElementID(url);
                    increateElementRef(workbook, id, sourceId);
                }
            }
        }
    }

    public static void increaseFileEntryRef(IWorkbook workbook, String entryPath) {
        if (workbook != null && entryPath != null) {
            IFileEntry e = workbook.getManifest().getFileEntry(entryPath);
            if (e != null) {
                e.increaseReference();
                if (e.isDirectory()) {
                    for (IFileEntry sub : e.getSubEntries()) {
                        sub.increaseReference();
                    }
                }
            }
        }
    }

    public static void increateElementRef(IWorkbook workbook, String elementId,
            String sourceId) {
        if (workbook != null && elementId != null) {
            IWorkbookComponentRefManager counter = (IWorkbookComponentRefManager) workbook
                    .getAdapter(IWorkbookComponentRefManager.class);
            if (counter != null) {
                counter.increaseRef(sourceId, elementId);
            }
        }
    }

    public static void deactivateHyperlink(IWorkbook workbook, String url,
            IAdaptable source) {
        if (workbook != null) {
            if (HyperlinkUtils.isAttachmentURL(url)) {
                if (isInWorkingRevision(source)) {
                    String attPath = HyperlinkUtils.toAttachmentPath(url);
                    decreaseFileEntryRef(workbook, attPath);
                }
            } else if (HyperlinkUtils.isInternalURL(url)) {
                if (source instanceof IIdentifiable) {
                    String sourceId = ((IIdentifiable) source).getId();
                    String elementId = HyperlinkUtils.toElementID(url);
                    decreaseElementRef(workbook, elementId, sourceId);
                }
            }
        }
    }

    public static void decreaseFileEntryRef(IWorkbook workbook, String entryPath) {
        if (workbook != null && entryPath != null) {
            IFileEntry e = workbook.getManifest().getFileEntry(entryPath);
            if (e != null) {
                e.decreaseReference();
                if (e.isDirectory()) {
                    for (IFileEntry sub : e.getSubEntries()) {
                        sub.decreaseReference();
                    }
                }
            }
        }
    }

    public static void decreaseElementRef(IWorkbook workbook, String elementId,
            String sourceId) {
        if (workbook != null && elementId != null) {
            IWorkbookComponentRefManager counter = (IWorkbookComponentRefManager) workbook
                    .getAdapter(IWorkbookComponentRefManager.class);
            if (counter != null) {
                counter.decreaseRef(sourceId, elementId);
            }
        }
    }

    public static String importAttachmentURL(String sourceHyperlink,
            IWorkbook sourceWorkbook, IWorkbook targetWorkbook)
            throws IOException {
        String sourcePath = HyperlinkUtils.toAttachmentPath(sourceHyperlink);
        String targetPath = importAttachment(sourcePath, sourceWorkbook,
                targetWorkbook);
        return HyperlinkUtils.toAttachmentURL(targetPath);
    }

    public static String importAttachment(String sourcePath,
            IWorkbook sourceWorkbook, IWorkbook targetWorkbook)
            throws IOException {
        IFileEntry sourceEntry = sourceWorkbook.getManifest().getFileEntry(
                sourcePath);
        if (sourceEntry != null) {
            IManifest manifest = targetWorkbook.getManifest();
            IFileEntry targetEntry = manifest
                    .cloneEntryAsAttachment(sourceEntry);
            if (targetEntry != null) {
                return targetEntry.getPath();
            }
        }
        return sourcePath;
    }
}