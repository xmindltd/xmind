/*
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and above are dual-licensed
 * under the Eclipse Public License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors: XMind Ltd. - initial API and implementation
 */
package org.xmind.ui.internal.imports.freemind;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmind.core.Core;
import org.xmind.core.IBoundary;
import org.xmind.core.IFileEntry;
import org.xmind.core.IHtmlNotesContent;
import org.xmind.core.IImage;
import org.xmind.core.INotes;
import org.xmind.core.IParagraph;
import org.xmind.core.IRelationship;
import org.xmind.core.ISheet;
import org.xmind.core.ISpan;
import org.xmind.core.ITextSpan;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.core.internal.dom.StyleSheetImpl;
import org.xmind.core.io.ResourceMappingManager;
import org.xmind.core.io.freemind.FreeMindConstants;
import org.xmind.core.io.freemind.FreeMindResourceMappingManager;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.core.style.IStyled;
import org.xmind.core.util.DOMUtils;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.imports.ImportMessages;
import org.xmind.ui.internal.imports.ImporterUtils;
import org.xmind.ui.io.MonitoredInputStream;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.style.Styles;
import org.xmind.ui.wizards.MindMapImporter;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class FreeMindImporter extends MindMapImporter
        implements FreeMindConstants, ErrorHandler {

    private class LinkPoint {
        ITopic end1;
        ITopic end2;

        public LinkPoint(Element ele) {
            String id1 = att(ele, "ID"); //$NON-NLS-1$
            end1 = findLinkTopic(id1);
            Element linkNode = child(ele, "arrowlink"); //$NON-NLS-1$
            String id2 = att(linkNode, "DESTINATION"); //$NON-NLS-1$
            end2 = findLinkTopic(id2);
        }

        private ITopic findLinkTopic(String id) {
            if (idMap == null || idMap.isEmpty())
                return null;
            String topicId = idMap.get(id);
            return getTargetWorkbook().findTopic(topicId);
        }

        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj != null && !(obj instanceof LinkPoint))
                return false;
            LinkPoint that = (LinkPoint) obj;
            return this.end1 == that.end1 && this.end2 == that.end2;
        }
    }

    private class NotesImporter {
        IHtmlNotesContent content;
        IParagraph currentParagraph = null;
        Stack<IStyle> styleStack = new Stack<IStyle>();

        public NotesImporter(IHtmlNotesContent content) {
            this.content = content;
        }

        public void importFrom(Element htmlEle) throws InterruptedException {
            checkInterrupted();
            String tagName = DOMUtils.getLocalName(htmlEle.getTagName());
            boolean isParagraph = "p".equalsIgnoreCase(tagName) //$NON-NLS-1$
                    || "li".equalsIgnoreCase(tagName); //$NON-NLS-1$
            IStyle style = pushStyle(htmlEle,
                    isParagraph ? IStyle.PARAGRAPH : IStyle.TEXT);
            if (isParagraph)
                addParagraph();

            NodeList nl = htmlEle.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                short nodeType = node.getNodeType();
                if (nodeType == Node.TEXT_NODE) {
                    addText(node.getTextContent());
                } else if (nodeType == Node.ELEMENT_NODE) {
                    importFrom((Element) node);
                }
            }
            popStyle(style);
        }

        private void addText(String text) {
            if (text == null)
                return;
            text = text.trim();
            if ("".equals(text)) //$NON-NLS-1$
                return;
            ITextSpan textSpan = content.createTextSpan(text);
            addSpan(textSpan);
        }

        private void addSpan(ISpan span) {
            if (currentParagraph == null)
                addParagraph();
            currentParagraph.addSpan(span);
            registerStyle(span, Styles.FontFamily, Styles.FontSize,
                    Styles.TextColor, Styles.FontWeight, Styles.TextDecoration,
                    Styles.FontStyle);
        }

        private void addParagraph() {
            currentParagraph = content.createParagraph();
            content.addParagraph(currentParagraph);
            registerStyle(currentParagraph, Styles.TextAlign);
        }

        private void registerStyle(IStyled host, String... keys) {
            for (IStyle style : styleStack) {
                registerStyle(host, style, keys);
            }
        }

        private void registerStyle(IStyled host, IStyle style, String... keys) {
            for (String key : keys) {
                String value = style.getProperty(key);
                if (value != null)
                    FreeMindImporter.this.registerStyle(host, key, value);
            }
        }

        private IStyle pushStyle(Element ele, String type) {
            IStyle style = getStyleSheet().createStyle(type);
            receiveStyle(ele, style);
            if (style.isEmpty())
                style = null;
            else
                styleStack.push(style);
            return style;
        }

        private void receiveStyle(Element ele, IStyle style) {
            String alignStyle = att(ele, "style"); //$NON-NLS-1$
            if (alignStyle != null) {
                String align = FreeMindImporter.this.parseAlign(alignStyle);
                style.setProperty(Styles.TextAlign, align);
            }
            String name = ele.getTagName();
            if ("b".equalsIgnoreCase(name)) //$NON-NLS-1$
                style.setProperty(Styles.FontWeight, Styles.FONT_WEIGHT_BOLD);
            else if ("i".equalsIgnoreCase(name)) //$NON-NLS-1$
                style.setProperty(Styles.FontStyle, Styles.FONT_STYLE_ITALIC);
            else if ("u".equalsIgnoreCase(name)) //$NON-NLS-1$
                style.setProperty(Styles.TextDecoration,
                        Styles.TEXT_DECORATION_UNDERLINE);
            else if ("font".equalsIgnoreCase(name)) { //$NON-NLS-1$
                String fontFamily = att(ele, "face"); //$NON-NLS-1$
                if (fontFamily != null)
                    style.setProperty(Styles.FontWeight, fontFamily);
                String color = att(ele, "color"); //$NON-NLS-1$
                if (color != null)
                    style.setProperty(Styles.TextColor, color);
                String size = att(ele, "size"); //$NON-NLS-1$
                if (size != null) {
                    String fontSize = FreeMindImporter.this.parseSize(size);
                    style.setProperty(Styles.FontSize, fontSize + "pt"); //$NON-NLS-1$
                }
            }
        }

        private void popStyle(IStyle style) {
            if (style == null)
                return;
            if (!styleSheet.isEmpty() && styleStack.peek() == style)
                styleStack.pop();
        }
    }

    private static ResourceMappingManager mappings = null;

    private List<Element> linkEles = null;

    private Map<String, String> idMap = new HashMap<String, String>();

    private Map<IStyled, IStyle> styleMap = new HashMap<IStyled, IStyle>();

    private Map<ITopic, String> topicHyperMap = null;

    private IStyleSheet styleSheet = null;

    private StringBuilder topicText = null;

    public FreeMindImporter(String sourcePath, IWorkbook targetWorkbook) {
        super(sourcePath, targetWorkbook);
    }

    public FreeMindImporter(String sourcePath) {
        super(sourcePath);
    }

    public void build() throws InvocationTargetException, InterruptedException {
        MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                UserDataConstants.CATEGORY_IMPORT,
                UserDataConstants.IMPORT_FROM_FREE_MIND);
        try {
            DocumentBuilder builder = getDocumentBuilder();
            builder.setErrorHandler(this);
            Document doc;
            InputStream in = new FileInputStream(getSourcePath());
            try {
                in = new MonitoredInputStream(in, getMonitor());
                doc = builder.parse(in);
            } finally {
                builder.setErrorHandler(null);
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            checkInterrupted();
            Element rootElement = doc.getDocumentElement();
            loadSheet(rootElement);

            checkInterrupted();
            arrangeStyles();

            checkInterrupted();
            dealTopicHyperlinks();

        } catch (Throwable e) {
            throw new InvocationTargetException(e);
        } finally {
            idMap = null;
            styleMap = null;
        }
        postBuilded();
    }

    private void dealTopicHyperlinks() {
        if (topicHyperMap == null || topicHyperMap.isEmpty())
            return;
        for (Entry<ITopic, String> entry : topicHyperMap.entrySet()) {
            ITopic topic = entry.getKey();
            String nodeId = entry.getValue();
            if (idMap == null || idMap.isEmpty())
                break;
            String topicId = idMap.get(nodeId);
            if (topicId == null)
                continue;
            topic.setHyperlink("xmind:#" + topicId); //$NON-NLS-1$
        }
    }

    private void arrangeStyles() throws InterruptedException {
        IStyleSheet targetStyleSheet = getTargetWorkbook().getStyleSheet();
        for (Entry<IStyled, IStyle> entry : styleMap.entrySet()) {
            checkInterrupted();
            IStyled styleOwned = entry.getKey();
            IStyle style = entry.getValue();
            IStyle importStyle = targetStyleSheet.importStyle(style);
            if (importStyle != null)
                styleOwned.setStyleId(importStyle.getId());
        }
    }

    private void loadSheet(Element rootEle) throws InterruptedException {
        checkInterrupted();
        ISheet sheet = getTargetWorkbook().createSheet();
        sheet.setTitleText(getSuggestedSheetTitle());
        Element nodeEle = child(rootEle, "node"); //$NON-NLS-1$
        if (nodeEle != null)
            loadTopic(sheet.getRootTopic(), nodeEle);
        else
            sheet.getRootTopic()
                    .setTitleText(ImportMessages.Importer_CentralTopic);

        if (linkEles != null && !linkEles.isEmpty())
            loadRelationship();

        addTargetSheet(sheet);
    }

    private void loadRelationship() {
        for (Element linkEle : linkEles) {
            if (linkEle == null)
                continue;
            LinkPoint link = new LinkPoint(linkEle);
            ITopic end1 = link.end1;
            ITopic end2 = link.end2;
            if (end1 != null && end2 != null) {
                IRelationship relationship = getTargetWorkbook()
                        .createRelationship(end1, end2);
                relationship.setEnd1Id(end1.getId());
                relationship.setEnd2Id(end2.getId());

                Element arrowEle = child(linkEle, "arrowlink"); //$NON-NLS-1$
                loadLinkShape(relationship, arrowEle);
                loadLinkColor(relationship, arrowEle);
            }
        }
    }

    private void loadLinkColor(IRelationship relationship, Element arrowEle) {
        String color = att(arrowEle, "COLOR"); //$NON-NLS-1$
        if (color == null)
            return;
        registerStyle(relationship, Styles.LineColor, color);
    }

    private void loadLinkShape(IRelationship relationship, Element arrowEle) {
        String startArrow = att(arrowEle, "STARTARROW"); //$NON-NLS-1$
        String startShape = parseArrowShape(startArrow);
        registerStyle(relationship, Styles.ArrowBeginClass, startShape);
        String endArrow = att(arrowEle, "ENDARROW"); //$NON-NLS-1$
        String endShape = parseArrowShape(endArrow);
        registerStyle(relationship, Styles.ArrowEndClass, endShape);
    }

    private String parseArrowShape(String shape) {
        if ("Default".equals(shape)) //$NON-NLS-1$
            return "org.xmind.arrowShape.normal"; //$NON-NLS-1$
        return "org.xmind.arrowShape.none"; //$NON-NLS-1$
    }

    private void loadTopic(ITopic topic, Element nodeEle)
            throws InterruptedException {
        checkInterrupted();
        String id = att(nodeEle, "ID"); //$NON-NLS-1$
        if (id != null) {
            idMap.put(id, topic.getId());
        }

        checkInterrupted();
        String text = att(nodeEle, "TEXT"); //$NON-NLS-1$
        if (text == null)
            text = ImporterUtils.getDefaultTopicTitle(topic);
        else if (text.contains("../../../../")) { //$NON-NLS-1$
            int index = text.indexOf("../../../../"); //$NON-NLS-1$
            String path = text.substring(index + "../../../../".length(), text //$NON-NLS-1$
                    .length() - 2);
            loadImage(topic, path);
            text = ImporterUtils.getDefaultTopicTitle(topic);
        }
        topic.setTitleText(text);

        checkInterrupted();
        String folded = att(nodeEle, "FOLDED"); //$NON-NLS-1$
        if (folded != null && "true".equals(folded)) //$NON-NLS-1$
            topic.setFolded(true);

        checkInterrupted();
        if (!topic.isRoot()) {
            Element ele = child(nodeEle, "cloud"); //$NON-NLS-1$
            if (ele != null) {
                IBoundary boundary = getTargetWorkbook().createBoundary();
                int index = topic.getIndex();
                boundary.setStartIndex(index);
                boundary.setEndIndex(index);
                topic.getParent().addBoundary(boundary);
            }
        }

        checkInterrupted();
        String link = att(nodeEle, "LINK"); //$NON-NLS-1$
        if (link != null) {
            if (link.startsWith("../../../../")) { //$NON-NLS-1$
                String hyperlink = link.substring("../../../../".length()); //$NON-NLS-1$
                topic.setHyperlink("file:" + hyperlink); //$NON-NLS-1$
            } else if (link.startsWith("#")) { //$NON-NLS-1$
                String nodeId = link.substring(1);
                if (topicHyperMap == null)
                    topicHyperMap = new HashMap<ITopic, String>();
                topicHyperMap.put(topic, nodeId);
            } else {
                topic.setHyperlink(link);
            }
        }

        checkInterrupted();
        String backgroundColor = att(nodeEle, "BACKGROUND_COLOR"); //$NON-NLS-1$
        registerStyle(topic, Styles.FillColor, backgroundColor);

        checkInterrupted();
        String foregroundColor = att(nodeEle, "COLOR"); //$NON-NLS-1$
        registerStyle(topic, Styles.TextColor, foregroundColor);

        checkInterrupted();
        Element fontEle = child(nodeEle, "font"); //$NON-NLS-1$
        loadFont(fontEle, topic);

        checkInterrupted();
        Element linkNode = child(nodeEle, "arrowlink"); //$NON-NLS-1$
        if (linkNode != null) {
            if (linkEles == null)
                linkEles = new ArrayList<Element>();
            linkEles.add(nodeEle);
        }

        Iterator<Element> iconIter = children(nodeEle, "icon"); //$NON-NLS-1$
        while (iconIter.hasNext()) {
            checkInterrupted();
            Element iconEle = iconIter.next();
            String builtIn = att(iconEle, "BUILTIN"); //$NON-NLS-1$
            if (builtIn != null) {
                String markerId = getTransferred("marker", builtIn, null); //$NON-NLS-1$
                if (markerId == null)
                    markerId = "other-question"; //$NON-NLS-1$
                topic.addMarker(markerId);
            }
        }

        checkInterrupted();
        Element hookEle = child(nodeEle, "hook"); //$NON-NLS-1$
        if (hookEle != null) {
            String name = att(hookEle, "NAME"); //$NON-NLS-1$
            if ("accessories/plugins/NodeNote.properties".equals(name)) { //$NON-NLS-1$
                Element notesEle = child(hookEle, "text"); //$NON-NLS-1$
                if (notesEle != null)
                    loadNotesContent(topic, notesEle);
            }
        }

        Iterator<Element> notesIter = children(nodeEle, "richcontent"); //$NON-NLS-1$
        while (notesIter.hasNext()) {
            checkInterrupted();
            Element richEle = notesIter.next();
            String type = att(richEle, "TYPE"); //$NON-NLS-1$
            if (type != null && "NOTE".equals(type)) { //$NON-NLS-1$
                Element htmlEle = child(richEle, "html"); //$NON-NLS-1$
                if (htmlEle != null) {
                    IHtmlNotesContent notesContent = (IHtmlNotesContent) getTargetWorkbook()
                            .createNotesContent(INotes.HTML);
                    NotesImporter notesImport = new NotesImporter(notesContent);
                    notesImport.importFrom(htmlEle);
                    topic.getNotes().setContent(INotes.HTML, notesContent);
                }
            } else if ("NODE".equals(type)) { //$NON-NLS-1$
                topicText = null;
                Element htmlEle = child(richEle, "html"); //$NON-NLS-1$
                if (htmlEle != null) {
                    loadNode(topic, htmlEle);
                }
                if (topicText != null) {
                    text = topicText.toString().trim();
                } else {
                    text = ImporterUtils.getDefaultTopicTitle(topic);
                }
                topic.setTitleText(text);
            }
        }

        Iterator<Element> nodeIter = children(nodeEle, "node"); //$NON-NLS-1$
        while (nodeIter.hasNext()) {
            checkInterrupted();
            Element subNodeEle = nodeIter.next();
            ITopic subTopic = getTargetWorkbook().createTopic();
            topic.add(subTopic);
            loadTopic(subTopic, subNodeEle);
        }
    }

    private void loadNotesContent(ITopic topic, Element ele) {
        String text = ele.getTextContent().trim();
        if (text == null)
            return;
        IHtmlNotesContent notesContent = (IHtmlNotesContent) getTargetWorkbook()
                .createNotesContent(INotes.HTML);
        ITextSpan span = notesContent.createTextSpan(text);
        IParagraph paragraph = notesContent.createParagraph();
        paragraph.addSpan(span);
        notesContent.addParagraph(paragraph);
        topic.getNotes().setContent(INotes.HTML, notesContent);
    }

    private void loadNode(ITopic topic, Element element)
            throws InterruptedException {
        checkInterrupted();
        String tagName = DOMUtils.getLocalName(element.getTagName());
        if ("img".equalsIgnoreCase(tagName)) { //$NON-NLS-1$
            String src = att(element, "src"); //$NON-NLS-1$
            if (src.startsWith("../../../../")) { //$NON-NLS-1$
                String path = src.substring("../../../../".length()); //$NON-NLS-1$
                loadImage(topic, path);
            } else {
                String path = getSourcePath().substring(0,
                        getSourcePath().lastIndexOf("\\") + 1) + src;//$NON-NLS-1$
                loadImage(topic, path);
            }
        } else if ("p".equalsIgnoreCase(tagName) //$NON-NLS-1$
                || "li".equalsIgnoreCase(tagName)) { //$NON-NLS-1$
            String text = element.getTextContent().trim();
            if (text != null) {
                if (topicText == null)
                    topicText = new StringBuilder();
                topicText.append(text);
                topicText.append('\n');
            }

            String align = att(element, "style"); //$NON-NLS-1$
            if (align != null) {
                String value = parseAlign(align);
                registerStyle(topic, Styles.TextAlign, value);
            }
        } else if ("b".equalsIgnoreCase(tagName)) //$NON-NLS-1$
            registerStyle(topic, Styles.FontWeight, Styles.FONT_WEIGHT_BOLD);
        else if ("i".equalsIgnoreCase(tagName)) //$NON-NLS-1$
            registerStyle(topic, Styles.FontStyle, Styles.FONT_STYLE_ITALIC);
        else if ("font".equalsIgnoreCase(tagName)) { //$NON-NLS-1$
            String fontFamily = att(element, "face"); //$NON-NLS-1$
            if (fontFamily != null)
                registerStyle(topic, Styles.FontWeight, fontFamily);
            String color = att(element, "color"); //$NON-NLS-1$
            if (color != null)
                registerStyle(topic, Styles.TextColor, color);
            String size = att(element, "size"); //$NON-NLS-1$
            if (size != null)
                registerStyle(topic, Styles.FontSize, parseSize(size));
        }

        Element[] children = DOMUtils.getChildElements(element);
        for (Element ele : children) {
            loadNode(topic, ele);
        }
    }

    private void loadImage(ITopic topic, String path)
            throws InterruptedException {
        checkInterrupted();
        IFileEntry imgEntry = loadAttachment(path);
        if (imgEntry != null) {
            IImage image = topic.getImage();
            image.setSource(HyperlinkUtils.toAttachmentURL(imgEntry.getPath()));
        }
    }

    private IFileEntry loadAttachment(String path) throws InterruptedException {
        checkInterrupted();
        try {
            IFileEntry entry = getTargetWorkbook().getManifest()
                    .createAttachmentFromFilePath(path);
            return entry;
        } catch (IOException e) {
            log(e, "failed to create attachment from: " + path); //$NON-NLS-1$
        }
        return null;
    }

    private void loadFont(Element fontEle, ITopic topic) {
        if (fontEle == null)
            return;
        String fontName = att(fontEle, "NAME"); //$NON-NLS-1$
        String availableFontName = FontUtils.getAAvailableFontNameFor(fontName);
        fontName = availableFontName != null ? availableFontName : fontName;
        if (fontName != null) {
            registerStyle(topic, Styles.FontFamily, fontName);
        }
        String size = att(fontEle, "SIZE"); //$NON-NLS-1$
        if (size != null) {
            registerStyle(topic, Styles.FontSize, size);
        }
        String italic = att(fontEle, "ITALIC"); //$NON-NLS-1$
        if ("true".equalsIgnoreCase(italic)) { //$NON-NLS-1$
            registerStyle(topic, Styles.FontStyle, Styles.FONT_STYLE_ITALIC);
        }
        String bold = att(fontEle, "BOLD"); //$NON-NLS-1$
        if ("true".equalsIgnoreCase(bold)) //$NON-NLS-1$
            registerStyle(topic, Styles.FontWeight, Styles.FONT_WEIGHT_BOLD);
    }

    private void registerStyle(IStyled styleOwned, String key, String value) {
        if (value == null)
            return;
        IStyle style = styleMap.get(styleOwned);
        if (style == null) {
            style = getStyleSheet().createStyle(styleOwned.getStyleType());
            getStyleSheet().addStyle(style, IStyleSheet.NORMAL_STYLES);
            styleMap.put(styleOwned, style);
        }
        style.setProperty(key, value);
    }

    private String parseSize(String size) {
        if ("2".equals(size)) //$NON-NLS-1$
            return "10"; //$NON-NLS-1$
        else if ("3".equals(size)) //$NON-NLS-1$
            return "12"; //$NON-NLS-1$
        else if ("4".equals(size)) //$NON-NLS-1$
            return "14"; //$NON-NLS-1$
        else if ("5".equals(size)) //$NON-NLS-1$
            return "18"; //$NON-NLS-1$
        else if ("6".equals(size)) //$NON-NLS-1$
            return "24"; //$NON-NLS-1$
        return "8"; //$NON-NLS-1$
    }

    private String parseAlign(String align) {
        if (align.endsWith(Styles.ALIGN_CENTER))
            return Styles.ALIGN_CENTER;
        else if (align.endsWith(Styles.ALIGN_RIGHT))
            return Styles.ALIGN_RIGHT;
        return Styles.ALIGN_LEFT;
    }

    private IStyleSheet getStyleSheet() {
        if (styleSheet == null) {
            styleSheet = Core.getStyleSheetBuilder().createStyleSheet();
            ((StyleSheetImpl) styleSheet)
                    .setManifest(getTargetWorkbook().getManifest());
        }
        return styleSheet;
    }

    private String getTransferred(String type, String sourceId,
            String defaultId) {
        if (sourceId != null) {
            ResourceMappingManager mappings = getMappings();
            if (mappings != null) {
                String destination = mappings.getDestination(type, sourceId);
                if (destination != null)
                    return destination;
            }
        }
        return defaultId;
    }

    private ResourceMappingManager getMappings() {
        if (mappings == null)
            mappings = createMappings();
        return mappings;
    }

    private ResourceMappingManager createMappings() {
        return FreeMindResourceMappingManager.getInstance();
    }

    private void checkInterrupted() throws InterruptedException {
        if (getMonitor().isCanceled())
            throw new InterruptedException();
    }

    private DocumentBuilder getDocumentBuilder()
            throws ParserConfigurationException {
        return DOMUtils.getDefaultDocumentBuilder();
    }

    private static Element child(Element parentEle, String childTag) {
        return children(parentEle, childTag).next();
    }

    private static Iterator<Element> children(final Element parentEle,
            final String childTag) {
        return new Iterator<Element>() {
            String tag = DOMUtils.getLocalName(childTag);
            Iterator<Element> it = DOMUtils.childElementIter(parentEle);
            Element next = findNext();

            public void remove() {
            }

            private Element findNext() {
                while (it.hasNext()) {
                    Element ele = it.next();
                    if (DOMUtils.getLocalName(ele.getTagName())
                            .equalsIgnoreCase(tag)) {
                        return ele;
                    }
                }
                return null;
            }

            public Element next() {
                Element result = next;
                next = findNext();
                return result;
            }

            public boolean hasNext() {
                return next != null;
            }
        };
    }

    private static String att(Element ele, String attName) {
        if (ele.hasAttribute(attName))
            return ele.getAttribute(attName);

        attName = DOMUtils.getLocalName(attName);
        NamedNodeMap atts = ele.getAttributes();
        for (int i = 0; i < atts.getLength(); i++) {
            Node att = atts.item(i);
            if (attName.equalsIgnoreCase(
                    DOMUtils.getLocalName(att.getNodeName()))) {
                return att.getNodeValue();
            }
        }
        return null;
    }

    public void error(SAXParseException exception) throws SAXException {
        log(exception, null);
    }

    public void fatalError(SAXParseException exception) throws SAXException {
        log(exception, null);
    }

    public void warning(SAXParseException exception) throws SAXException {
        log(exception, null);
    }
}
