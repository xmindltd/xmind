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
package org.xmind.ui.internal.wizards;

import static org.xmind.core.internal.dom.DOMConstants.ATTR_LINE_TAPERED;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_MULTI_LINE_COLORS;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_STYLE_FAMILY;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_TYPE;
import static org.xmind.core.internal.dom.DOMConstants.VAL_NONE;
import static org.xmind.core.internal.dom.DOMConstants.VAL_TAPERED;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Text;
import org.xmind.core.CoreException;
import org.xmind.core.IBoundary;
import org.xmind.core.IControlPoint;
import org.xmind.core.IFileEntry;
import org.xmind.core.IHtmlNotesContent;
import org.xmind.core.IImage;
import org.xmind.core.IImageSpan;
import org.xmind.core.ILegend;
import org.xmind.core.IManifest;
import org.xmind.core.INotes;
import org.xmind.core.INumbering;
import org.xmind.core.IParagraph;
import org.xmind.core.IPlainNotesContent;
import org.xmind.core.IPositioned;
import org.xmind.core.IRelationship;
import org.xmind.core.ISheet;
import org.xmind.core.ISpan;
import org.xmind.core.ITextSpan;
import org.xmind.core.ITitled;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.zip.ArchiveConstants;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerRef;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.core.style.IStyleSheet;
import org.xmind.core.style.IStyled;
import org.xmind.core.util.DOMUtils;
import org.xmind.core.util.FileUtils;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.core.util.Point;

/**
 * @author frankshaka
 * 
 */
public class XMind2008Exporter {

    private IWorkbook sourceWorkbook;

    private String targetPath;

    private IProgressMonitor monitor;

    private Document contentDocument;

    private Document styleDocument;

    private Document metaDocument;

    private Document manifestDocument;

//    /**
//     * Marker path -> marker id
//     */
//    private Map<String, String> attachmentMarkerPaths;

    /**
     * @param sheet
     * @param centralTopic
     */
    public XMind2008Exporter(IWorkbook sourceWorkbook, String targetPath) {
        this.sourceWorkbook = sourceWorkbook;
        this.targetPath = targetPath;
    }

    /**
     * @return the monitor
     */
    public IProgressMonitor getMonitor() {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        return monitor;
    }

    /**
     * @param monitor
     *            the monitor to set
     */
    public void setMonitor(IProgressMonitor monitor) {
        this.monitor = monitor;
    }

    /**
     * @return the sourceWorkbook
     */
    public IWorkbook getSourceWorkbook() {
        return sourceWorkbook;
    }

    /**
     * @return the targetPath
     */
    public String getTargetPath() {
        return targetPath;
    }

    public void export() throws InvocationTargetException, InterruptedException {
        saveContent();

        saveStyles();
        saveMeta();
        saveManifest();

        try {
            ZipOutputStream os = new ZipOutputStream(new FileOutputStream(
                    getTargetPath()));
            try {

                os.putNextEntry(new ZipEntry(ArchiveConstants.CONTENT_XML));
                save(os, contentDocument);

                os.putNextEntry(new ZipEntry(ArchiveConstants.STYLES_XML));
                save(os, styleDocument);

                os.putNextEntry(new ZipEntry(ArchiveConstants.META_XML));
                save(os, metaDocument);

                os.putNextEntry(new ZipEntry(ArchiveConstants.MANIFEST_XML));
                save(os, manifestDocument);

                IManifest manifest = sourceWorkbook.getManifest();
                for (IFileEntry entry : manifest.getFileEntries()) {
                    if (!entry.isDirectory()) {
                        String path = entry.getPath();
                        if (shouldCopyEntry(path)) {
                            copyEntry(os, entry, path);
                        }

                    }
                }

            } finally {
                try {
                    os.close();
                } catch (IOException e) {
                }
            }
        } catch (IOException e) {
            throw new InvocationTargetException(e);
        }
    }

    /**
     * @param os
     * @param entry
     * @param path
     */
    private void copyEntry(ZipOutputStream os, IFileEntry entry, String path)
            throws InvocationTargetException {
        InputStream is = entry.getInputStream();
        if (is == null)
            return;

//        String attachmentMarkerId = getAttachmentMarkerId(path);
//        if (attachmentMarkerId != null) {
//            path = "Pictures/" + attachmentMarkerId + FileUtils.getExtension(path); //$NON-NLS-1$
//        } else 
        if (path.startsWith(ArchiveConstants.PATH_ATTACHMENTS)) {
            path = upperFirst(path);
        }
        try {
            os.putNextEntry(new ZipEntry(path));
            FileUtils.transfer(is, os, false);
        } catch (IOException e) {
            throw new InvocationTargetException(e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            }
        }
    }

//    private String getAttachmentMarkerId(String path) {
//        if (attachmentMarkerPaths != null) {
//            for (Entry<String, String> en : attachmentMarkerPaths.entrySet()) {
//                String p = en.getKey();
//                if (path.contains(p) || p.contains(path)) {
//                    return en.getValue();
//                }
//            }
//        }
//        return null;
//    }

    /**
     * @param path
     * @return
     */
    private boolean shouldCopyEntry(String path) {
        return !ArchiveConstants.CONTENT_XML.equals(path)
                && !ArchiveConstants.STYLES_XML.equals(path)
                && !ArchiveConstants.META_XML.equals(path)
                && !ArchiveConstants.MANIFEST_XML.equals(path);
    }

    /**
     * 
     */
    private void saveManifest() throws InvocationTargetException {
        manifestDocument = createDocument();

        Element docEle;
        Document sourceDoc = (Document) sourceWorkbook.getManifest()
                .getAdapter(Document.class);
        if (sourceDoc != null) {
            docEle = (Element) manifestDocument.importNode(sourceDoc
                    .getDocumentElement(), true);
            downgradeManifest(docEle);
            manifestDocument.appendChild(docEle);
        } else {
            docEle = DOMUtils.createElement(manifestDocument, "manifest"); //$NON-NLS-1$
        }
        docEle.setAttribute("xmlns", "urn:xmind:xmap:xmlns:manifest:1.0"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * @param docEle
     */
    private void downgradeManifest(Element element) {
        if ("file-entry".equals(element.getTagName())) { //$NON-NLS-1$
            String path = element.getAttribute("full-path"); //$NON-NLS-1$
//            String attachmentMarkerId = getAttachmentMarkerId(path);
//            if (attachmentMarkerId != null) {
//                path = "Pictures/" + attachmentMarkerId + FileUtils.getExtension(path); //$NON-NLS-1$
//            } else 
            if (path.startsWith(ArchiveConstants.PATH_ATTACHMENTS)) {
                path = upperFirst(path);
            }
            element.setAttribute("full-path", path); //$NON-NLS-1$
        }
        Iterator<Element> it = DOMUtils.childElementIter(element);
        while (it.hasNext()) {
            downgradeManifest(it.next());
        }
    }

    /**
     * 
     * @throws InvocationTargetException
     */
    private void saveMeta() throws InvocationTargetException {
        metaDocument = createDocument();
        Element docEle = DOMUtils.createElement(metaDocument, "xmap-meta"); //$NON-NLS-1$
        docEle.setAttribute("xmlns", "urn:xmind:xmap:xmlns:meta:1.0"); //$NON-NLS-1$ //$NON-NLS-2$
        docEle.setAttribute("version", "1.0"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * @throws InvocationTargetException
     * 
     */
    private void saveStyles() throws InvocationTargetException {
        styleDocument = createDocument();
        Element docEle = DOMUtils.createElement(styleDocument, "xmap-styles"); //$NON-NLS-1$
        docEle.setAttribute("xmlns", "urn:xmind:xmap:xmlns:style:1.0"); //$NON-NLS-1$ //$NON-NLS-2$
        docEle.setAttribute("xmlns:fo", "http://www.w3.org/1999/XSL/Format"); //$NON-NLS-1$ //$NON-NLS-2$
        docEle.setAttribute("xmlns:svg", "http://www.w3.org/2000/svg"); //$NON-NLS-1$ //$NON-NLS-2$
        docEle.setAttribute("version", "1.0"); //$NON-NLS-1$ //$NON-NLS-2$
        docEle.setAttribute("version", "1.0"); //$NON-NLS-1$ //$NON-NLS-2$

        IStyleSheet styleSheet = sourceWorkbook.getStyleSheet();
        if (styleSheet.isEmpty())
            return;

        saveStyleContent(docEle, styleSheet);
    }

    /**
     * @param docEle
     * @param styleSheet
     */
    private void saveStyleContent(Element docEle, IStyleSheet styleSheet) {
        Document sourceDoc = (Document) styleSheet.getAdapter(Document.class);
        if (sourceDoc == null)
            return;
        Element sourceDocEle = sourceDoc.getDocumentElement();
        Iterator<Element> it = DOMUtils.childElementIter(sourceDocEle);
        Document doc = docEle.getOwnerDocument();
        while (it.hasNext()) {
            Element sourceEle = it.next();
            Element targetEle = (Element) doc.importNode(sourceEle, true);
            downgradeStyles(targetEle);
            docEle.appendChild(targetEle);
        }
    }

    /**
     * @param element
     */
    private void downgradeStyles(Element element) {
        downgradeStyle(element);
        Iterator<Element> it = DOMUtils.childElementIter(element);
        while (it.hasNext()) {
            downgradeStyles(it.next());
        }
    }

    /**
     * @param element
     */
    private void downgradeStyle(Element element) {
        NamedNodeMap attributes = element.getAttributes();
        Map<String, String> added = null;
        for (int i = 0; i < attributes.getLength(); i++) {
            Attr attr = (Attr) attributes.item(i);
            String name = attr.getName();
            String value = attr.getValue();
            if (ATTR_TYPE.equals(name)) {
                attr.setValue(upperFirst(value));
            } else if (ATTR_STYLE_FAMILY.equals(name)) {
                attr.setValue(upperFirst(value));
            } else if (ATTR_MULTI_LINE_COLORS.equals(name)) {
                if (value != null && !VAL_NONE.equals(value)) {
                    added = add(added, "rainbowcolor", "true"); //$NON-NLS-1$ //$NON-NLS-2$
                }
            } else if (ATTR_LINE_TAPERED.equals(name)) {
                if (value.contains(VAL_TAPERED)) {
                    added = add(added, "spinylines", "true"); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
//            if ("floatingMainTopic".equals(value)) { //$NON-NLS-1$
//                attr.setValue("floatingTopic"); //$NON-NLS-1$
//            } else 
            if (VAL_NONE.equals(value)) {
                attr.setValue("$none$"); //$NON-NLS-1$
            } else if (HyperlinkUtils.isAttachmentURL(value)) {
                value = toAttachmentURL(value);
                attr.setValue(value);
            }
        }
        if (added != null) {
            for (Entry<String, String> e : added.entrySet()) {
                element.setAttribute(e.getKey(), e.getValue());
            }
        }
    }

    private Map<String, String> add(Map<String, String> map, String key,
            String value) {
        if (map == null)
            map = new HashMap<String, String>();
        map.put(key, value);
        return map;
    }

    /**
     * @param os
     * @param document
     * @throws CoreException
     */
    private void save(OutputStream os, Document document)
            throws InvocationTargetException {
        try {
            DOMUtils.save(document, os, false);
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
    }

    /**
     * 
     */
    private void saveContent() throws InvocationTargetException {
        contentDocument = createDocument();
        Element docEle = DOMUtils
                .createElement(contentDocument, "xmap-content"); //$NON-NLS-1$
        docEle.setAttribute("xmlns", "urn:xmind:xmap:xmlns:content:1.0"); //$NON-NLS-1$ //$NON-NLS-2$
        docEle.setAttribute("xmlns:xhtml", "http://www.w3.org/1999/xhtml"); //$NON-NLS-1$ //$NON-NLS-2$
        docEle.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink"); //$NON-NLS-1$ //$NON-NLS-2$
        docEle.setAttribute("xmlns:fo", "http://www.w3.org/1999/XSL/Format"); //$NON-NLS-1$ //$NON-NLS-2$
        docEle.setAttribute("xmlns:svg", "http://www.w3.org/2000/svg"); //$NON-NLS-1$ //$NON-NLS-2$
        docEle.setAttribute("version", "1.0"); //$NON-NLS-1$ //$NON-NLS-2$
        saveMaps(docEle);
    }

    /**
     * @param parentEle
     */
    private void saveMaps(Element parentEle) {
        for (ISheet sheet : sourceWorkbook.getSheets()) {
            saveMap(parentEle, sheet);
        }
    }

    /**
     * @param parentEle
     * @param sheet
     */
    private void saveMap(Element parentEle, ISheet sheet) {
        Element mapEle = DOMUtils.createElement(parentEle, "map"); //$NON-NLS-1$
        DOMUtils.replaceId(mapEle, sheet.getId());
        saveTheme(mapEle, sheet);
        saveTitle(mapEle, sheet);
        saveStyle(mapEle, sheet);
        saveRootTopic(mapEle, sheet);
        saveRelationships(mapEle, sheet.getRelationships());
        saveLegend(mapEle, sheet.getLegend());
    }

    /**
     * @param mapEle
     * @param sheet
     */
    private void saveTheme(Element mapEle, ISheet sheet) {
        DOMUtils.setAttribute(mapEle, "theme", sheet.getThemeId()); //$NON-NLS-1$
    }

    /**
     * @param mapEle
     * @param sheet
     */
    private void saveRootTopic(Element mapEle, ISheet sheet) {
        Element rootTopicEle = DOMUtils.createElement(mapEle, "root-topic"); //$NON-NLS-1$
        saveTopic(rootTopicEle, sheet.getRootTopic());
    }

    /**
     * @param rootTopicEle
     * @param rootTopic
     */
    private void saveTopic(Element topicEle, ITopic topic) {
        DOMUtils.replaceId(topicEle, topic.getId());
        saveFolded(topicEle, topic);
        saveHyperlink(topicEle, topic);
        saveStructureClass(topicEle, topic);
        saveStyle(topicEle, topic);

        saveTitle(topicEle, topic);
        savePosition(topicEle, topic);
        saveImage(topicEle, topic);
        saveLabels(topicEle, topic);
        saveMarkers(topicEle, topic);
        saveNotes(topicEle, topic);
        saveNumbering(topicEle, topic);
        saveAttachedSubtopics(topicEle, topic.getChildren(ITopic.ATTACHED));
        saveDetachedSubtopics(topicEle, topic.getChildren(ITopic.DETACHED));
        saveDetachedSubtopics(topicEle, topic.getChildren(ITopic.SUMMARY));
        saveBoundaries(topicEle, topic);
    }

    /**
     * @param topicEle
     * @param topic
     */
    private void saveBoundaries(Element topicEle, ITopic topic) {
        Set<IBoundary> boundaries = topic.getBoundaries();
        if (boundaries.isEmpty())
            return;
        Element boundariesEle = DOMUtils.createElement(topicEle, "boundaries"); //$NON-NLS-1$
        for (IBoundary boundary : boundaries) {
            saveBoundary(boundariesEle, boundary);
        }
    }

    /**
     * @param boundariesEle
     * @param boundary
     */
    private void saveBoundary(Element boundariesEle, IBoundary boundary) {
        Element boundaryEle = DOMUtils.createElement(boundariesEle, "boundary"); //$NON-NLS-1$
        DOMUtils.setAttribute(boundaryEle,
                "start-index", String.valueOf(boundary.getStartIndex())); //$NON-NLS-1$
        DOMUtils.setAttribute(boundaryEle,
                "end-index", String.valueOf(boundary.getEndIndex())); //$NON-NLS-1$
        saveStyle(boundaryEle, boundary);
    }

    /**
     * @param topicEle
     * @param children
     */
    private void saveDetachedSubtopics(Element topicEle, List<ITopic> subtopics) {
        if (subtopics.isEmpty())
            return;
        Element subtopicsEle = DOMUtils.createElement(topicEle,
                "detached-topics"); //$NON-NLS-1$
        saveSubtopics(subtopicsEle, subtopics);
    }

    /**
     * @param topicEle
     * @param children
     */
    private void saveAttachedSubtopics(Element topicEle, List<ITopic> subtopics) {
        if (subtopics.isEmpty())
            return;
        Element subtopicsEle = DOMUtils.createElement(topicEle,
                "attached-topics"); //$NON-NLS-1$
        saveSubtopics(subtopicsEle, subtopics);
    }

    /**
     * @param subtopicsEle
     * @param subtopics
     */
    private void saveSubtopics(Element subtopicsEle, List<ITopic> subtopics) {
        for (ITopic subtopic : subtopics) {
            Element subtopicEle = DOMUtils.createElement(subtopicsEle, "topic"); //$NON-NLS-1$
            saveTopic(subtopicEle, subtopic);
        }
    }

    /**
     * @param topicEle
     * @param topic
     */
    private void saveNumbering(Element topicEle, ITopic topic) {
        INumbering numbering = topic.getNumbering();
        String format = numbering.getNumberFormat();
        String prefix = numbering.getPrefix();
        String suffix = numbering.getSuffix();
        boolean prepend = numbering.prependsParentNumbers();
        if (prepend && format == null && prefix == null && suffix == null)
            return;
        Element numEle = DOMUtils.createElement(topicEle, "numbering"); //$NON-NLS-1$
        if (!prepend) {
            DOMUtils.setAttribute(numEle, "inherited", "false"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (format != null) {
            DOMUtils.setAttribute(numEle, "number-format", format); //$NON-NLS-1$
        }
        if (prefix != null) {
            DOMUtils.setAttribute(numEle, "prefix", prefix); //$NON-NLS-1$
        }
        if (suffix != null) {
            DOMUtils.setAttribute(numEle, "suffix", suffix); //$NON-NLS-1$
        }
    }

    /**
     * @param topicEle
     * @param topic
     */
    private void saveNotes(Element topicEle, ITopic topic) {
        INotes notes = topic.getNotes();
        if (notes.isEmpty())
            return;

        Element notesEle = DOMUtils.createElement(topicEle, "notes"); //$NON-NLS-1$

        IPlainNotesContent plain = (IPlainNotesContent) notes
                .getContent(INotes.PLAIN);
        if (plain != null) {
            String textContent = plain.getTextContent();
            DOMUtils.createText(notesEle, "plain", textContent); //$NON-NLS-1$
        }

        IHtmlNotesContent html = (IHtmlNotesContent) notes
                .getContent(INotes.HTML);
        if (html != null) {
            saveHtmlNotesContent(notesEle, html, topic.getOwnedWorkbook());
        }
    }

    /**
     * @param notesEle
     * @param html
     * @param workbook
     */
    private void saveHtmlNotesContent(Element notesEle, IHtmlNotesContent html,
            IWorkbook workbook) {
        Element richEle = DOMUtils.createElement(notesEle, "rich"); //$NON-NLS-1$
        for (IParagraph p : html.getParagraphs()) {
//        for (IBaseParagraph p : html.getParagraphs()) {

            Element pEle = DOMUtils.createElement(richEle, "xhtml:p"); //$NON-NLS-1$
            saveStyle(pEle, p);
            for (ISpan span : p.getSpans()) {
                if (span instanceof IImageSpan) {
                    saveImageSpan(pEle, (IImageSpan) span);
                } else {
                    saveTextSpan(pEle, (ITextSpan) span);
                }
            }
        }
    }

    /**
     * @param ele
     * @param span
     */
    private void saveImageSpan(Element pEle, IImageSpan span) {
        String source = span.getSource();
        if (source == null)
            return;
        if (HyperlinkUtils.isAttachmentURL(source)) {
            source = toAttachmentURL(source);
        }
        Element imgEle = DOMUtils.createElement(pEle, "xhtml:img"); //$NON-NLS-1$
        DOMUtils.setAttribute(imgEle, "xhtml:src", source); //$NON-NLS-1$
    }

    /**
     * @param ele
     * @param span
     */
    private void saveTextSpan(Element pEle, ITextSpan span) {
        String styleId = span.getStyleId();
        if (styleId == null) {
            Text textNode = pEle.getOwnerDocument().createTextNode(
                    span.getTextContent());
            pEle.appendChild(textNode);
        } else {
            Element spanEle = DOMUtils.createElement(pEle, "xhtml:span"); //$NON-NLS-1$
            DOMUtils.setAttribute(spanEle, "style-id", styleId); //$NON-NLS-1$
            spanEle.setTextContent(span.getTextContent());
        }
    }

    /**
     * @param topicEle
     * @param topic
     */
    private void saveMarkers(Element topicEle, ITopic topic) {
        Set<IMarkerRef> markerRefs = topic.getMarkerRefs();
        if (markerRefs.isEmpty())
            return;
        Element markersEle = DOMUtils.createElement(topicEle, "markers"); //$NON-NLS-1$
        IMarkerSheet markerSheet = topic.getOwnedWorkbook().getMarkerSheet();
//        boolean isSystemSheet = markerSheet.findMarkerGroup("priorityMarkers") != null;
        for (IMarkerRef mr : markerRefs) {
            Element markerEle = DOMUtils.createElement(markersEle, "marker"); //$NON-NLS-1$
            IMarker marker = mr.getMarker();
            if (marker != null) {
                String type;
                String id;
                IMarkerSheet ownedSheet = marker.getOwnedSheet();
                if (ownedSheet != null && !ownedSheet.equals(markerSheet)) {
                    type = "Brainy"; //$NON-NLS-1$
                } else {
                    type = "User"; //$NON-NLS-1$
                }
                IMarkerGroup group = marker.getParent();
                if (group != null) {
                    id = "#" + group.getId() + "/" + marker.getId(); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    id = marker.getId();
                }
//                if (ownedSheet != null && !ownedSheet.equals(markerSheet)) {
//                    type = isSystemSheet ? "Brainy" : "User"; //$NON-NLS-1$ //$NON-NLS-2$
//                    IMarkerGroup group = marker.getParent();
//                    if (group != null) {
//                        id = "#" + group.getId() + "/" + marker.getId(); //$NON-NLS-1$ //$NON-NLS-2$
//                    } else {
//                        id = marker.getId();
//                    }
//                } else {
//                    type = "Attachment"; //$NON-NLS-1$
//                    id = "#" + marker.getId(); //$NON-NLS-1$
//                    if (attachmentMarkerPaths == null)
//                        attachmentMarkerPaths = new HashMap<String, String>();
//                    attachmentMarkerPaths.put(marker.getResourcePath(), marker
//                            .getId());
//                }
                DOMUtils.setAttribute(markerEle, "id", id); //$NON-NLS-1$
                DOMUtils.setAttribute(markerEle, "type", type); //$NON-NLS-1$
            }
        }
    }

    /**
     * @param topicEle
     * @param topic
     */
    private void saveLabels(Element topicEle, ITopic topic) {
        Set<String> labels = topic.getLabels();
        if (labels.isEmpty())
            return;
        Element labelsEle = DOMUtils.createElement(topicEle, "labels"); //$NON-NLS-1$
        for (String label : labels) {
            Element labelEle = DOMUtils.createElement(labelsEle, "label"); //$NON-NLS-1$ 
            labelEle.setTextContent(label);
        }
    }

    /**
     * @param topicEle
     * @param topic
     */
    private void saveImage(Element topicEle, ITopic topic) {
        IImage image = topic.getImage();
        String source = image.getSource();
        if (source == null)
            return;

        Element imgEle = DOMUtils.createElement(topicEle, "xhtml:img"); //$NON-NLS-1$
        if (HyperlinkUtils.isAttachmentURL(source)) {
            source = toAttachmentURL(source);
        }
        DOMUtils.setAttribute(imgEle, "xhtml:src", source); //$NON-NLS-1$
        int width = image.getWidth();
        if (width != IImage.UNSPECIFIED) {
            DOMUtils.setAttribute(imgEle, "svg:width", String.valueOf(width)); //$NON-NLS-1$
        }
        int height = image.getHeight();
        if (height != IImage.UNSPECIFIED) {
            DOMUtils.setAttribute(imgEle, "svg:height", String.valueOf(height)); //$NON-NLS-1$
        }
        DOMUtils.setAttribute(imgEle, "align", image.getAlignment()); //$NON-NLS-1$
    }

    /**
     * @param topicEle
     * @param topic
     */
    private void saveHyperlink(Element topicEle, ITopic topic) {
        String url = topic.getHyperlink();
        if (url == null)
            return;
        if (HyperlinkUtils.isAttachmentURL(url)) {
            url = toAttachmentURL(url);
        }
        DOMUtils.setAttribute(topicEle, "xlink:href", url); //$NON-NLS-1$
    }

    private String toAttachmentURL(String url) {
        String path = HyperlinkUtils.toAttachmentPath(url);
        String attId = FileUtils.getNoExtensionFileName(path);
        return HyperlinkUtils.toAttachmentURL("#" + attId); //$NON-NLS-1$
    }

    /**
     * @param element
     * @param positionOwner
     */
    private void savePosition(Element element, IPositioned positionOwner) {
        if (positionOwner.hasPosition()) {
            savePosition(element, positionOwner.getPosition());
        }
    }

    /**
     * @param topicEle
     * @param topic
     */
    private void saveFolded(Element topicEle, ITopic topic) {
        if (topic.isFolded()) {
            DOMUtils.setAttribute(topicEle, "extended", "false"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * @param topicEle
     * @param topic
     */
    private void saveStructureClass(Element topicEle, ITopic topic) {
        String structureClass = topic.getStructureClass();
        structureClass = downgradeStructureClass(structureClass);
        DOMUtils.setAttribute(topicEle, "structure-class", structureClass); //$NON-NLS-1$
    }

    private String downgradeStructureClass(String structureClass) {
        if (structureClass == null)
            return null;
        if ("org.xmind.ui.logic.left".equals(structureClass)) { //$NON-NLS-1$
            structureClass = "org.xmind.branchPolicy.org-chart.left"; //$NON-NLS-1$
        } else if ("org.xmind.ui.logic.right".equals(structureClass)) { //$NON-NLS-1$
            structureClass = "org.xmind.branchPolicy.org-chart.right"; //$NON-NLS-1$
        } else if ("org.xmind.ui.spreadsheet".equals(structureClass)) { //$NON-NLS-1$
            structureClass = "org.xmind.branchPolicy.chart2d"; //$NON-NLS-1$
        } else if (structureClass.startsWith("org.xmind.ui.")) { //$NON-NLS-1$
            structureClass = "org.xmind.branchPolicy." + structureClass.substring(13); //$NON-NLS-1$
        }
        return structureClass;
    }

    private void savePosition(Element element, Point p) {
        if (p == null)
            return;
        Element posEle = DOMUtils.createElement(element, "position"); //$NON-NLS-1$
        DOMUtils.setAttribute(posEle, "svg:x", String.valueOf(p.x)); //$NON-NLS-1$
        DOMUtils.setAttribute(posEle, "svg:y", String.valueOf(p.y)); //$NON-NLS-1$
    }

    /**
     * @param mapEle
     * @param legend
     */
    private void saveLegend(Element parentEle, ILegend legend) {
        Element legendEle = DOMUtils.createElement(parentEle, "legend"); //$NON-NLS-1$
        if (legend.isVisible()) {
            DOMUtils.setAttribute(legendEle, "svg:visibility", "visible"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        savePosition(legendEle, legend);
        for (String markerId : legend.getMarkerIds()) {
            Element markerDescriptionEle = DOMUtils.createElement(legendEle,
                    "marker"); //$NON-NLS-1$
            DOMUtils.replaceId(markerDescriptionEle, markerId);
            DOMUtils.setAttribute(markerDescriptionEle,
                    "description", legend.getMarkerDescription(markerId)); //$NON-NLS-1$
        }
    }

    /**
     * @param mapEle
     * @param relationships
     */
    private void saveRelationships(Element parentEle,
            Set<IRelationship> relationships) {
        if (relationships.isEmpty())
            return;
        Element relationshipsEle = DOMUtils.createElement(parentEle,
                "relationships"); //$NON-NLS-1$
        for (IRelationship relationship : relationships) {
            saveRelationship(relationshipsEle, relationship);
        }
    }

    /**
     * @param relationshipsEle
     * @param relationship
     */
    private void saveRelationship(Element relationshipsEle,
            IRelationship relationship) {
        Element relationshipEle = DOMUtils.createElement(relationshipsEle,
                "relationship"); //$NON-NLS-1$
        DOMUtils.replaceId(relationshipEle, relationship.getId());
        DOMUtils.setAttribute(relationshipEle,
                "from-id", relationship.getEnd1Id()); //$NON-NLS-1$
        DOMUtils.setAttribute(relationshipEle,
                "to-id", relationship.getEnd2Id()); //$NON-NLS-1$
        saveStyle(relationshipEle, relationship);
        saveTitle(relationshipEle, relationship);
        saveControlPoint(relationshipEle, relationship.getControlPoint(0), 0);
    }

    /**
     * @param relationshipEle
     * @param controlPoint
     * @param i
     */
    private void saveControlPoint(Element relationshipEle,
            IControlPoint controlPoint, int index) {
        Point position = controlPoint.getPosition();
        if (position == null)
            return;
        String attrName = "control-point" + String.valueOf(index + 1); //$NON-NLS-1$
        String value = String.format("%d, %d", position.x, position.y); //$NON-NLS-1$
        DOMUtils.setAttribute(relationshipEle, attrName, value);
    }

    private void saveStyle(Element element, IStyled styleOwner) {
        if (styleOwner == null)
            return;
        String styleId = styleOwner.getStyleId();
        if (styleId == null)
            return;
        DOMUtils.setAttribute(element, "style-id", styleId); //$NON-NLS-1$
    }

    private void saveTitle(Element element, ITitled titleOwner) {
        if (titleOwner == null || !titleOwner.hasTitle())
            return;
        DOMUtils.setText(element, "title", titleOwner.getTitleText()); //$NON-NLS-1$
    }

    private Document createDocument() throws InvocationTargetException {
        try {
            return DOMUtils.doCreateDocument();
        } catch (ParserConfigurationException e) {
            throw new InvocationTargetException(e);
        }
    }

    private static String upperFirst(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

}
