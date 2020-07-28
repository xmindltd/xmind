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
package org.xmind.ui.internal.wizards;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmind.core.IControlPoint;
import org.xmind.core.IFileEntry;
import org.xmind.core.IHtmlNotesContent;
import org.xmind.core.IHyperlinkSpan;
import org.xmind.core.IImage;
import org.xmind.core.INotes;
import org.xmind.core.INotesContent;
import org.xmind.core.IParagraph;
import org.xmind.core.IRelationship;
import org.xmind.core.ISheet;
import org.xmind.core.ISpan;
import org.xmind.core.ITextSpan;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.marker.IMarkerRef;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.core.style.IStyled;
import org.xmind.core.util.DOMUtils;
import org.xmind.core.util.FileUtils;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.core.util.Point;
import org.xmind.ui.internal.protocols.FilePathParser;
import org.xmind.ui.internal.protocols.FileProtocol;
import org.xmind.ui.style.Styles;

/**
 * 
 * @author Karelun huang
 */
public class FreeMindExporter {

    private static class EdgeData {
        String color;
        String style;
        String width;

        public EdgeData(String color, String style, String width) {
            this.color = color;
            this.style = style;
            this.width = width;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (!(obj instanceof EdgeData))
                return false;
            EdgeData ed = (EdgeData) obj;
            return ed.color.equals(this.color) && ed.style.equals(this.style)
                    && ed.width.equals(this.width);
        }
    }

    private static final String IMAGE_FILE = "images"; //$NON-NLS-1$

    private ISheet sheet;

    private String targetPath;

    private IProgressMonitor monitor;

    private Document document;

    private File imageDir = null;

    private Map<ITopic, EdgeData> edgeDataMap = null;

    private Map<String, String> markers = null;

    public FreeMindExporter(ISheet sheet, String targetPath) {
        this.sheet = sheet;
        this.targetPath = targetPath;
    }

    public ISheet getSheet() {
        return sheet;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public IProgressMonitor getMonitor() {
        if (monitor == null)
            monitor = new NullProgressMonitor();
        return monitor;
    }

    public void setMonitor(IProgressMonitor monitor) {
        this.monitor = monitor;
    }

    public void build() throws InvocationTargetException, InterruptedException {
        getMonitor().beginTask(null, 100);
        try {
            document = getDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new InvocationTargetException(e);
        }
        checkInterrupter();
        try {
            writeContent();
        } catch (Throwable e) {
            throw new InvocationTargetException(e);
        }

        try {
            OutputStream os = new FileOutputStream(targetPath);
            // put the document's content to write in the tempPath
            DOMUtils.save(document, os, true);
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
    }

    private void writeContent() throws Exception {
        Element mapEle = DOMUtils.createElement(document, "map"); //$NON-NLS-1$
        writeMap(mapEle);
        if (edgeDataMap != null)
            edgeDataMap.clear();
        if (markers != null)
            markers.clear();
    }

    private void writeMap(Element mapEle) throws Exception {
        mapEle.setAttribute("version", "0.8.1"); //$NON-NLS-1$ //$NON-NLS-2$
        ITopic rootTopic = getSheet().getRootTopic();
        cacheMarkers();
        writeTopic(mapEle, rootTopic);
    }

    private void writeTopic(Element element, ITopic topic) throws Exception {
        Element nodeEle = DOMUtils.createElement(element, "node"); //$NON-NLS-1$
        IStyle style = getStyle(topic);
        writeColor(nodeEle, topic, style);
        writeCreateFolderAndId(nodeEle, topic);
        writeLink(nodeEle, topic);
        writeModify(nodeEle);
        writePosition(nodeEle, topic);
        writeTopicShapeStyle(nodeEle, style);
        writeImageOrText(nodeEle, topic);
        WriteVShift(nodeEle, topic);

        cacheEdge(topic, style);
        writeEdge(nodeEle, topic);

        writeArrowLink(nodeEle, topic);
        writeFont(nodeEle, style);
        writeIcon(nodeEle, topic);
        writeHookNotes(nodeEle, topic);
        writSubTopics(nodeEle, topic);
    }

    private void writeHookNotes(Element nodeEle, ITopic topic) {
        INotes notes = topic.getNotes();
        if (notes == null)
            return;
        INotesContent content = notes.getContent(INotes.HTML);
        if (content != null) {
            Element hookEle = DOMUtils.createElement(nodeEle, "hook"); //$NON-NLS-1$
            hookEle.setAttribute("NAME", //$NON-NLS-1$
                    "accessories/plugins/NodeNote.properties"); //$NON-NLS-1$
            Element notesEle = DOMUtils.createElement(hookEle, "text"); //$NON-NLS-1$
            IHtmlNotesContent html = (IHtmlNotesContent) content;
            List<IParagraph> paragraphs = html.getParagraphs();
            StringBuilder paraBuffer = new StringBuilder();
            for (IParagraph paragraph : paragraphs) {
                List<ISpan> spans = paragraph.getSpans();
                StringBuilder spanBuffer = new StringBuilder();
                for (ISpan span : spans) {
                    writeSpan(spanBuffer, span);
                }
                if (paraBuffer.length() > 0) {
                    //paraBuffer.append("&#xa;"); //$NON-NLS-1$
                    paraBuffer.append((char) 10);
                }
                paraBuffer.append(spanBuffer.toString());
            }
            notesEle.setTextContent(paraBuffer.toString());
        }
    }

    private void writeSpan(StringBuilder buffer, ISpan span) {
        if (span instanceof ITextSpan) {
            String text = ((ITextSpan) span).getTextContent();
            buffer.append(text);
        } else if (span instanceof IHyperlinkSpan) {
            for (ISpan subSpan : ((IHyperlinkSpan) span).getSpans()) {
                writeSpan(buffer, subSpan);
            }
        }
    }

    private void writeIcon(Element nodeEle, ITopic topic) {
        Set<IMarkerRef> markers = topic.getMarkerRefs();
        if (markers == null)
            return;
        Iterator<IMarkerRef> iter = markers.iterator();
        while (iter.hasNext()) {
            IMarkerRef next = iter.next();
            String icon = findTransferIcon(next);
            if (icon != null) {
                Element iconEle = DOMUtils.createElement(nodeEle, "icon"); //$NON-NLS-1$
                iconEle.setAttribute("BUILTIN", icon); //$NON-NLS-1$
            }
        }
    }

    private String findTransferIcon(IMarkerRef marker) {
        String markerId = marker.getMarkerId();
        if (markerId == null)
            return null;
        if (markerId.startsWith("flag")) //$NON-NLS-1$
            markerId = "flag"; //$NON-NLS-1$
        else if (markerId.contains("star")) //$NON-NLS-1$
            markerId = "star"; //$NON-NLS-1$
        String icon = markers.get(markerId);
        if (icon != null)
            return icon;
        return null;
    }

    private IStyle getStyle(IStyled styleOwner) {
        if (styleOwner != null) {
            String styleId = styleOwner.getStyleId();
            if (styleId == null)
                return null;
            IWorkbook workbook = getSheet().getOwnedWorkbook();
            IStyleSheet styleSheet = workbook.getStyleSheet();
            return styleSheet.findStyle(styleId);
        }
        return null;
    }

    private void cacheEdge(ITopic topic, IStyle style) {
        if (style == null)
            return;
        String lineColor = style.getProperty(Styles.LineColor);
        String lineClass = style.getProperty(Styles.LineClass);
        String lineWidth = style.getProperty(Styles.LineWidth);
        if (lineColor == null && lineClass == null && lineWidth == null)
            return;
        String edgeStyle = null;
        if (lineClass != null) {
            if (lineClass.endsWith("curve")) //$NON-NLS-1$
                edgeStyle = "bezier"; //$NON-NLS-1$
            else if (lineClass.endsWith("straight")) //$NON-NLS-1$
                edgeStyle = "linear"; //$NON-NLS-1$
        }

        String edgeWidth = null;
        if ("1pt".equals(lineWidth)) //$NON-NLS-1$
            edgeWidth = "thin"; //$NON-NLS-1$
        else if ("2pt".equals(lineWidth)) //$NON-NLS-1$
            edgeWidth = "1"; //$NON-NLS-1$
        else if ("3pt".equals(lineWidth)) //$NON-NLS-1$
            edgeWidth = "2"; //$NON-NLS-1$
        else if ("4pt".equals(lineWidth)) //$NON-NLS-1$
            edgeWidth = "4"; //$NON-NLS-1$
        else if ("5pt".equals(lineWidth)) //$NON-NLS-1$
            edgeWidth = "8"; //$NON-NLS-1$

        if (edgeDataMap == null)
            edgeDataMap = new HashMap<ITopic, EdgeData>();
        EdgeData edgeData = new EdgeData(lineColor, edgeStyle, edgeWidth);
        edgeDataMap.put(topic, edgeData);

    }

    private void writeEdge(Element nodeEle, ITopic topic) {
        if (edgeDataMap == null)
            return;
        ITopic parent = topic.getParent();
        if (parent == null)
            return;
        EdgeData edgeData = edgeDataMap.get(parent);
        if (edgeData == null)
            return;
        Element edgeEle = DOMUtils.createElement(nodeEle, "edge"); //$NON-NLS-1$
        String edgeColor = edgeData.color;
        if (edgeColor != null)
            edgeEle.setAttribute("COLOR", edgeColor); //$NON-NLS-1$
        String edgeStyle = edgeData.style;
        if (edgeStyle != null)
            edgeEle.setAttribute("STYLE", edgeStyle); //$NON-NLS-1$
        String edgeWidth = edgeData.width;
        if (edgeWidth != null)
            edgeEle.setAttribute("WIDTH", edgeWidth); //$NON-NLS-1$
    }

    private void writeColor(Element nodeEle, ITopic topic, IStyle style) {
        if (style == null)
            return;
        String backgroundColor = style.getProperty(Styles.FillColor);
        if (backgroundColor != null)
            nodeEle.setAttribute("BACKGROUND_COLOR", backgroundColor); //$NON-NLS-1$

        String color = style.getProperty(Styles.TextColor);
        if (color != null)
            nodeEle.setAttribute("COLOR", color); //$NON-NLS-1$
    }

    private void writeArrowLink(Element nodeEle, ITopic topic) {
        List<IRelationship> relationships = findRelationship(topic);
        if (relationships == null)
            return;
        for (IRelationship relationship : relationships) {
            Element arrowlinkEle = DOMUtils.createElement(nodeEle, "arrowlink"); //$NON-NLS-1$
            IStyle style = getRelationStyle(relationship);
            writeArrowLinkColor(arrowlinkEle, style);
            boolean canWriteCP = canWriteControlpoint(relationship);
            writeEndArrow(arrowlinkEle, relationship, style, canWriteCP);
            String arrowLineId = relationship.getId();
            arrowlinkEle.setAttribute("ID", arrowLineId); //$NON-NLS-1$
            writeStartArrow(arrowlinkEle, relationship, style, canWriteCP);
        }
    }

    private boolean canWriteControlpoint(IRelationship relationship) {
        String end1Id = relationship.getEnd1Id();
        String end2Id = relationship.getEnd2Id();
        IWorkbook workbook = getSheet().getOwnedWorkbook();
        ITopic topic1 = workbook.findTopic(end1Id);
        ITopic topic2 = workbook.findTopic(end2Id);
        if (topic1.isAttached() && topic2.isAttached())
            return true;
        return false;
    }

    private void writeStartArrow(Element arrowlinkEle,
            IRelationship relationship, IStyle style, boolean canWritePoint) {
        String arrowStart = null;
        String startArrow = null;
        if (style != null)
            arrowStart = style.getProperty(Styles.ArrowBeginClass);
        if (arrowStart == null || arrowStart.endsWith("none")) //$NON-NLS-1$
            startArrow = "None"; //$NON-NLS-1$
        else
            startArrow = "Default"; //$NON-NLS-1$
        arrowlinkEle.setAttribute("STARTARROW", startArrow); //$NON-NLS-1$

        if (!canWritePoint)
            return;
        IControlPoint controlPoint = relationship.getControlPoint(0);
        Point p = controlPoint.getPosition();
        if (p != null) {
            String endInc = String.valueOf(p.x) + ";" + String.valueOf(p.y); //$NON-NLS-1$
            arrowlinkEle.setAttribute("STARTINCLINATION", endInc); //$NON-NLS-1$
        }
    }

    private void writeEndArrow(Element arrowlinkEle, IRelationship relationship,
            IStyle style, boolean canWritePoint) {

        String endArrowId = relationship.getEnd2Id();
        if (endArrowId != null)
            arrowlinkEle.setAttribute("DESTINATION", endArrowId); //$NON-NLS-1$
        String arrowEnd = null;
        String endArrow = null;
        if (style != null)
            arrowEnd = style.getProperty(Styles.ArrowEndClass);
        if (arrowEnd != null && arrowEnd.endsWith("none")) //$NON-NLS-1$
            endArrow = "None"; //$NON-NLS-1$
        else
            endArrow = "Default"; //$NON-NLS-1$
        arrowlinkEle.setAttribute("ENDARROW", endArrow); //$NON-NLS-1$

        if (!canWritePoint)
            return;
        IControlPoint controlPoint = relationship.getControlPoint(1);
        Point p = controlPoint.getPosition();
        if (p != null) {
            String endInc = String.valueOf(p.x) + ";" + String.valueOf(p.y); //$NON-NLS-1$
            arrowlinkEle.setAttribute("ENDINCLINATION", endInc); //$NON-NLS-1$
        }
    }

    private void writeArrowLinkColor(Element arrowlinkEle, IStyle style) {
        if (style == null)
            return;
        String lineColor = style.getProperty(Styles.LineColor);
        if (lineColor == null)
            return;
        arrowlinkEle.setAttribute("COLOR", lineColor); //$NON-NLS-1$
    }

    private IStyle getRelationStyle(IRelationship relationship) {
        String styleId = relationship.getStyleId();
        IWorkbook workbook = getSheet().getOwnedWorkbook();
        IStyleSheet styleSheet = workbook.getStyleSheet();
        return styleSheet.findStyle(styleId);

    }

    private void WriteVShift(Element nodeEle, ITopic topic) {
        if (topic.hasPosition() && topic.isAttached()) {
            Point position = topic.getPosition();
            String value = String.valueOf(position.y);
            nodeEle.setAttribute("VSHIFT", value); //$NON-NLS-1$
        }
    }

    private void writeImageOrText(Element nodeEle, ITopic topic)
            throws Exception {
        IImage image = topic.getImage();
        String source = image.getSource();
        if (source != null) {
            File imageDir = getImageDir();
            String entryPath = HyperlinkUtils.toAttachmentPath(source);
            IFileEntry fileEntry = getSheet().getOwnedWorkbook().getManifest()
                    .getFileEntry(entryPath);
            if (fileEntry != null) {
                String path = fileEntry.getPath();
                int lastIndex = path.lastIndexOf('/');
                String fileName = path.substring(lastIndex + 1);
                InputStream is = fileEntry.getInputStream();
                if (is != null) {
                    FileOutputStream os = new FileOutputStream(
                            new File(imageDir, fileName));
                    FileUtils.transfer(is, os, true);

                    String sourcePath = "images" + "/" + fileName; //$NON-NLS-1$ //$NON-NLS-2$
                    // " ASCII is 34
                    String value = "<html><img src=" + (char) 34 + sourcePath //$NON-NLS-1$
                            + (char) 34 + ">"; //$NON-NLS-1$
                    nodeEle.setAttribute("TEXT", value); //$NON-NLS-1$
                    return;
                }
            }
        }
        String text = topic.getTitleText();
        nodeEle.setAttribute("TEXT", text); //$NON-NLS-1$
    }

    private File getImageDir() {
        if (imageDir == null) {
            String imageSource = new File(targetPath).getParent();
            imageDir = FileUtils
                    .ensureDirectory(new File(imageSource, IMAGE_FILE));
        }
        return imageDir;
    }

    private void writSubTopics(Element topicEle, ITopic topic)
            throws Exception {
        List<ITopic> children = topic.getAllChildren();
        if (children != null) {
            for (ITopic subTopic : children)
                writeTopic(topicEle, subTopic);
        }
    }

    private void writeFont(Element nodeEle, IStyle style) {
        if (style == null)
            return;
        String bold = style.getProperty(Styles.FontWeight);
        String italic = style.getProperty(Styles.FontStyle);
        String fontSize = style.getProperty(Styles.FontSize);
        if (bold == null && italic == null && fontSize == null)
            return;
        Element fontEle = DOMUtils.createElement(nodeEle, "font"); //$NON-NLS-1$
        if (bold != null) {
            String isBold = "bold".equals(bold) ? "true" : null; //$NON-NLS-1$ //$NON-NLS-2$
            if (isBold != null)
                fontEle.setAttribute("BOLD", isBold); //$NON-NLS-1$
        }
        if (italic != null) {
            String isItalic = "italic".equals(italic) ? "true" : null; //$NON-NLS-1$ //$NON-NLS-2$
            if (isItalic != null)
                fontEle.setAttribute("ITALIC", isItalic); //$NON-NLS-1$
        }
        fontEle.setAttribute("NAME", "SansSerif"); //$NON-NLS-1$ //$NON-NLS-2$
        if (fontSize != null) {
            int index = fontSize.indexOf("pt"); //$NON-NLS-1$
            if (index >= 0) {
                String size = fontSize.substring(0, index);
                fontEle.setAttribute("SIZE", size); //$NON-NLS-1$
            } else {
                fontEle.setAttribute("SIZE", fontSize); //$NON-NLS-1$
            }
        } else
            fontEle.setAttribute("SIZE", "12"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private void writePosition(Element nodeEle, ITopic topic) {
        ITopic parent = topic.getParent();
        if (parent != null && parent.isRoot()) {
            List<ITopic> topics = parent.getAllChildren();
            int index = topics.indexOf(topic);
            String value = index <= topics.size() / 2 ? "right" : "left"; //$NON-NLS-1$ //$NON-NLS-2$
            nodeEle.setAttribute("POSITION", value); //$NON-NLS-1$
        }
    }

    private void writeTopicShapeStyle(Element nodeEle, IStyle style) {
        if (style == null)
            return;
        String shapeStyle = null;
        String shape = style.getProperty(Styles.ShapeClass);
        if (shape == null) {
            shapeStyle = "bubble"; //$NON-NLS-1$
            return;
        }
        if (shape.endsWith("noBorder") || shape.endsWith("undeline")) { //$NON-NLS-1$ //$NON-NLS-2$
            shapeStyle = "fork"; //$NON-NLS-1$
        } else
            shapeStyle = "bubble"; //$NON-NLS-1$
        nodeEle.setAttribute("STYLE", shapeStyle); //$NON-NLS-1$
    }

    private void writeModify(Element nodeEle) {
        String value = String.valueOf(System.currentTimeMillis());
        nodeEle.setAttribute("MODIFIED", value); //$NON-NLS-1$
    }

    private void writeLink(Element nodeEle, ITopic topic) throws Exception {
        String hyperlink = topic.getHyperlink();
        if (hyperlink == null)
            return;
        String link = toLink(topic);
        if (link != null) {
            nodeEle.setAttribute("LINK", link); //$NON-NLS-1$
        }
    }

    private String toLink(ITopic topic) throws Exception {
        String hyperlink = topic.getHyperlink();
        if (hyperlink == null)
            return null;
        if (hyperlink.startsWith("file:")) { //$NON-NLS-1$
            String path = FilePathParser.toPath(hyperlink);
            return FileProtocol.getAbsolutePath(topic, path);
//            if (FilePathParser.isPathRelative(path)) {
//                IWorkbook workbook = topic.getOwnedWorkbook();
//                String base = workbook.getFile();
//                if (base != null) {
//                    base = new File(base).getParent();
//                    if (base != null) {
//                        return FilePathParser.toAbsolutePath(base, path);
//                    }
//                }
//                return FilePathParser
//                        .toAbsolutePath(System.getProperty("user.home"), path); //$NON-NLS-1$
//            }
//            return path;
        } else if (HyperlinkUtils.isInternalURL(hyperlink)) {
            return HyperlinkUtils.toElementID(hyperlink);
        } else if (HyperlinkUtils.isAttachmentURL(hyperlink)) {
            File imageDir = getImageDir();
            String entryPath = HyperlinkUtils.toAttachmentPath(hyperlink);
            IFileEntry fileEntry = topic.getOwnedWorkbook().getManifest()
                    .getFileEntry(entryPath);
            if (fileEntry == null)
                return null;

            String path = fileEntry.getPath();
            int lastIndex = path.lastIndexOf('/');
            String fileName = path.substring(lastIndex + 1);
            InputStream is = fileEntry.getInputStream();
            if (is != null) {
                try {
                    FileOutputStream os = new FileOutputStream(
                            new File(imageDir, fileName));
                    try {
                        FileUtils.transfer(is, os, false);
                        return IMAGE_FILE + "/" + fileName; //$NON-NLS-1$
                    } finally {
                        os.close();
                    }
                } finally {
                    is.close();
                }
            }
        } else if (HyperlinkUtils.isLinkToWeb(hyperlink)
                && !hyperlink.contains("http://") //$NON-NLS-1$
                && !hyperlink.contains("https://")) //$NON-NLS-1$
            return "http://" + hyperlink; //$NON-NLS-1$
        return hyperlink;
    }

    private void writeCreateFolderAndId(Element nodeEle, ITopic topic) {
        long times = System.currentTimeMillis();
        String value = String.valueOf(times);
        nodeEle.setAttribute("CREATED", value); //$NON-NLS-1$
        if (topic.isFolded())
            nodeEle.setAttribute("FOLDER", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        String id = topic.getId();
        nodeEle.setAttribute("ID", id); //$NON-NLS-1$
    }

    private void cacheMarkers() {
        if (markers == null)
            markers = new HashMap<String, String>();
        markers.put("priority-1", "full-1"); //$NON-NLS-1$ //$NON-NLS-2$
        markers.put("priority-2", "full-2"); //$NON-NLS-1$ //$NON-NLS-2$
        markers.put("priority-3", "full-3"); //$NON-NLS-1$//$NON-NLS-2$
        markers.put("priority-4", "full-4"); //$NON-NLS-1$ //$NON-NLS-2$
        markers.put("priority-5", "full-5"); //$NON-NLS-1$//$NON-NLS-2$
        markers.put("priority-6", "full-6"); //$NON-NLS-1$ //$NON-NLS-2$
        markers.put("smiley-smile", "ksmiletris"); //$NON-NLS-1$ //$NON-NLS-2$
        markers.put("flag", "flag"); //$NON-NLS-1$//$NON-NLS-2$
        markers.put("star", "bookmark"); //$NON-NLS-1$ //$NON-NLS-2$
        markers.put("other-email", "Mail"); //$NON-NLS-1$ //$NON-NLS-2$
        markers.put("other-phone", "kaddressbook"); //$NON-NLS-1$//$NON-NLS-2$
        markers.put("other-question", "help"); //$NON-NLS-1$ //$NON-NLS-2$
        markers.put("other-lightbulb", "idea"); //$NON-NLS-1$ //$NON-NLS-2$
        markers.put("other-unlock", "password"); //$NON-NLS-1$//$NON-NLS-2$
        markers.put("other-yes", "button_ok"); //$NON-NLS-1$ //$NON-NLS-2$
        markers.put("other-no", "button_cancel"); //$NON-NLS-1$//$NON-NLS-2$
        markers.put("other-bomb", "clanbomber"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private List<IRelationship> findRelationship(ITopic topic) {
        Set<IRelationship> relationships = getSheet().getRelationships();
        if (relationships == null)
            return null;
        List<IRelationship> result = null;
        Iterator<IRelationship> iter = relationships.iterator();
        while (iter.hasNext()) {
            IRelationship next = iter.next();
            if (next != null) {
                String end1Id = next.getEnd1Id();
                if (topic.getId().equals(end1Id)) {
                    if (result == null)
                        result = new ArrayList<IRelationship>();
                    result.add(next);
                }
            }
        }
        return result;
    }

    private void checkInterrupter() throws InterruptedException {
        if (getMonitor().isCanceled())
            throw new InterruptedException();
    }

    private static DocumentBuilder getDocumentBuilder()
            throws ParserConfigurationException {
        return DOMUtils.getDefaultDocumentBuilder();
    }
}
