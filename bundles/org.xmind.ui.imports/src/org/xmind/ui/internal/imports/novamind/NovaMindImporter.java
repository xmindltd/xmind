package org.xmind.ui.internal.imports.novamind;

import java.io.BufferedInputStream;
import java.io.File;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.osgi.util.NLS;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.IBoundary;
import org.xmind.core.IFileEntry;
import org.xmind.core.IHtmlNotesContent;
import org.xmind.core.IHyperlinkSpan;
import org.xmind.core.IIdentifiable;
import org.xmind.core.IImage;
import org.xmind.core.INotes;
import org.xmind.core.INumbering;
import org.xmind.core.IParagraph;
import org.xmind.core.IRelationship;
import org.xmind.core.ISheet;
import org.xmind.core.ISpan;
import org.xmind.core.ITopic;
import org.xmind.core.ITopicExtension;
import org.xmind.core.ITopicExtensionElement;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.Image;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.core.internal.dom.StyleSheetImpl;
import org.xmind.core.io.DirectoryStorage;
import org.xmind.core.io.IInputSource;
import org.xmind.core.io.IStorage;
import org.xmind.core.io.ResourceMappingManager;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.core.style.IStyled;
import org.xmind.core.util.DOMUtils;
import org.xmind.core.util.FileUtils;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.imports.ImportMessages;
import org.xmind.ui.internal.protocols.FilePathParser;
import org.xmind.ui.io.MonitoredInputStream;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;
import org.xmind.ui.wizards.MindMapImporter;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author lyn
 */

public class NovaMindImporter extends MindMapImporter
        implements NMConstants, ErrorHandler {

    private static final Pattern DATE_PATTERN = Pattern.compile(
            "((\\d+)-(\\d{1,2})-(\\d{1,2}))T((\\d{1,2}):(\\d{1,2}):(\\d{1,2}))"); //$NON-NLS-1$

    private static final String TRANSPARENT_VALUE = "0.00"; //$NON-NLS-1$

    private static final double DPM = 1d;

    private static final String CONTENT_XML = "content.xml"; //$NON-NLS-1$

    private static final String MANIFEST_XML = "manifest.xml"; //$NON-NLS-1$

    private static final String STYLE_SHEET_XML = "style-sheet.xml"; //$NON-NLS-1$

    private static final String SEP = File.separator;

    private static final String RESOURCES_FOLDER = "Resources" + SEP; //$NON-NLS-1$

    private class NotesImporter {

        IParagraph currentParagraph = null;

        IHtmlNotesContent content;

        public NotesImporter(IHtmlNotesContent content) {
            this.content = content;
        }

        public void loadFrom(Element notesEle) throws InterruptedException {
            checkInterrupted();

            Element richTextEle = child(notesEle, "rich-text"); //$NON-NLS-1$
            if (richTextEle == null)
                return;

            NodeList nl = richTextEle.getChildNodes();

            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                String nodeName = node.getNodeName();
                if ("text-run".equals(nodeName)) { //$NON-NLS-1$
                    loadText(node);
                } else if ("hyperlink".equals(nodeName)) { //$NON-NLS-1$
                    /// TODO add hyperlink to notes

                    NamedNodeMap atts = node.getAttributes();
                    Node href = atts.getNamedItem("url"); //$NON-NLS-1$
                    IHyperlinkSpan linkSpan = content
                            .createHyperlinkSpan(href.getTextContent());
                    addSpan(node, linkSpan);

                    NodeList hnl = node.getChildNodes();
                    for (int j = 0; j < hnl.getLength(); j++) {
                        Node hNode = hnl.item(j);
                        if (hNode != null
                                && "text-run".equals(hNode.getNodeName())) { //$NON-NLS-1$
                            loadText(hNode);
                        }
                    }
                }
            }
        }

        private void loadText(Node node) throws InterruptedException {
            NodeList ps = node.getChildNodes();
            for (int pi = 0; pi < ps.getLength(); pi++) {
                Node p = ps.item(pi);
                if (p.getNodeType() == Node.TEXT_NODE) {
                    addText(node, p.getTextContent());
                } else if ("p".equals(p.getNodeName())) { //$NON-NLS-1$
                    addParagraph(node);
                }
            }
        }

        private void addText(Node node, String text)
                throws InterruptedException {
            if (text == null || "".equals(text)) //$NON-NLS-1$
                return;

            addSpan(node, content.createTextSpan(text));
        }

        private void addSpan(Node node, ISpan span)
                throws InterruptedException {
            if (currentParagraph == null)
                addParagraph(node);

            currentParagraph.addSpan(span);
            loadStyle(node, span);
            loadAlign(node, currentParagraph);
        }

        private void addParagraph(Node node) throws InterruptedException {
            currentParagraph = content.createParagraph();
            content.addParagraph(currentParagraph);
            loadAlign(node, currentParagraph);
        }

        @SuppressWarnings({ "nls" })
        private void loadStyle(Node node, IStyled host)
                throws InterruptedException {
            checkInterrupted();

            NamedNodeMap atts = node.getAttributes();

            Node nmColorNode = atts.getNamedItem("font-color");
            if (nmColorNode != null) {
                String nmColor = nmColorNode.getTextContent();
                nmColor = NovaMindImporter.this.realNmColor(nmColor);
                registerStyle(host, Styles.TextColor,
                        parseColor(nmColor.substring(3)));
            }
            Node fontSizeNode = atts.getNamedItem("font-size");
            if (fontSizeNode != null)
                registerStyle(host, Styles.FontSize, NovaMindImporter
                        .parseFontSize(fontSizeNode.getTextContent()));

            Node boldNode = atts.getNamedItem("bold");
            if (boldNode != null)
                registerStyle(host, Styles.FontWeight,
                        Boolean.parseBoolean(boldNode.getTextContent())
                                ? Styles.FONT_WEIGHT_BOLD : null);

            Node italicNode = atts.getNamedItem("italic");
            if (italicNode != null)
                registerStyle(host, Styles.FontStyle,
                        Boolean.parseBoolean(italicNode.getTextContent())
                                ? Styles.FONT_STYLE_ITALIC : null);

            String textDecoration = StyleUtils.toTextDecoration(
                    atts.getNamedItem("underline-style") != null,
                    atts.getNamedItem("strikethrough-style") != null);
            registerStyle(host, Styles.TextDecoration, textDecoration);

            String fontName = null;
            Node macFontNameNode = atts.getNamedItem("mac-font-name");
            if (macFontNameNode != null)
                fontName = macFontNameNode.getTextContent();

            if (fontName == null || "".equals(fontName)) {
                Node windowsFontNameNode = atts
                        .getNamedItem("windows-font-name");
                if (windowsFontNameNode != null)
                    fontName = windowsFontNameNode.getTextContent();
            }

            if (fontName != null) {
                String availableFontName = FontUtils
                        .getAAvailableFontNameFor(fontName);
                fontName = availableFontName != null ? availableFontName
                        : fontName;
                registerStyle(host, Styles.FontFamily, fontName);
            }
        }

        private void loadAlign(Node node, IStyled host)
                throws InterruptedException {
            checkInterrupted();

            NamedNodeMap atts = node.getAttributes();

            Node alignmentNode = atts.getNamedItem("alignment"); //$NON-NLS-1$
            if (alignmentNode != null)
                registerStyle(host, Styles.TextAlign,
                        alignmentNode.getTextContent().toLowerCase());
        }

    }

    private static ResourceMappingManager mappings = null;

    private IStorage tempStorage;

    private IInputSource tempSource;

    private Document content;

    private Document manifest;

    private Document styleSheet;

    private ISheet targetSheet;

    private Map<IStyled, IStyle> styleMap = new HashMap<IStyled, IStyle>(30);

    private IStyle theme = null;

    private IStyleSheet tempStyleSheet = null;

    private Map<String, String> topicIdMap = new HashMap<String, String>(30);

    private Map<String, String> refIdMap = new HashMap<String, String>(30);

    private Map<String, List<ITopic>> topicLinkMap = new HashMap<String, List<ITopic>>(
            10);

    private Map<String, Element> resourceRefMap = new HashMap<String, Element>();

    private Map<String, String> resourceMap = new HashMap<String, String>(30);

    private Map<String, Element> themeRefMap = new HashMap<String, Element>();

    private Map<String, String> assignRefMap = new HashMap<String, String>();

    private NovaMindImporter(String sourcePath) {
        super(sourcePath);
    }

    public NovaMindImporter(String sourcePath, IWorkbook targetWorkbook) {
        super(sourcePath, targetWorkbook);
    }

    public void build() throws InvocationTargetException, InterruptedException {
        MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                UserDataConstants.CATEGORY_IMPORT,
                UserDataConstants.IMPORT_FROM_NOVA);
        getMonitor().beginTask(null, 100);
        try {
            getMonitor()
                    .subTask(ImportMessages.MindManagerImporter_ReadingContent);

            tempStorage = createTemporaryStorage();
            extractSourceFileToTemporaryStorage();
            tempSource = tempStorage.getInputSource();

            content = readFile(CONTENT_XML);
            manifest = readFile(MANIFEST_XML);
            styleSheet = readFile(STYLE_SHEET_XML);

            getMonitor().worked(45);
            checkInterrupted();

            getMonitor().subTask(
                    ImportMessages.MindManagerImporter_ReadingElements);
            loadWorkbook(content.getDocumentElement());
            setTopicLinks();
            getMonitor().worked(45);

            checkInterrupted();
            getMonitor().subTask(
                    ImportMessages.MindManagerImporter_ArrangingStyles);
            arrangeStyles();
            getMonitor().worked(5);

            checkInterrupted();
            getMonitor().subTask(
                    ImportMessages.MindManagerImporter_GeneratingTheme);
            generateTheme();
            getMonitor().worked(5);
            getMonitor().done();
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        } finally {
            clearTempStorage();
        }
        postBuilded();
    }

    private Document readFile(String name) throws Exception {
        InputStream docEntryStream = tempSource.getEntryStream(name);
        if (docEntryStream == null)
            throw new IOException("No content entry"); //$NON-NLS-1$

        DocumentBuilder builder = getDocumentBuilder();
        builder.setErrorHandler(this);
        InputStream in = new MonitoredInputStream(docEntryStream, getMonitor());
        Document doc;
        try {
            doc = builder.parse(in);
        } finally {
            builder.setErrorHandler(null);
            try {
                in.close();
            } catch (Exception e) {
            }
        }
        return doc;
    }

    private IStorage createTemporaryStorage() throws IOException {
        String id = String.format("%1$tY%1$tm%1$td%1$tH%1$tM%1$tS", //$NON-NLS-1$
                System.currentTimeMillis());
        File tempDir = FileUtils.ensureDirectory(new File(
                Core.getWorkspace().getTempDir("import/mindmanager"), id)); //$NON-NLS-1$
        return new DirectoryStorage(tempDir);
    }

    private void extractSourceFileToTemporaryStorage()
            throws IOException, CoreException {
        FileInputStream fin = new FileInputStream(getSourcePath());
        try {
            ZipInputStream zin = new ZipInputStream(new MonitoredInputStream(
                    new BufferedInputStream(fin), getMonitor()));
            try {
                FileUtils.extractZipFile(zin, tempStorage.getOutputTarget());
            } finally {
                zin.close();
            }
        } finally {
            fin.close();
        }

    }

    private void checkInterrupted() throws InterruptedException {
        if (getMonitor().isCanceled())
            throw new InterruptedException();
    }

    private void clearTempStorage() {
        if (tempStorage != null) {
            tempStorage.clear();
            tempStorage = null;
        }
    }

    private void loadWorkbook(Element docEle) throws InterruptedException {
        checkInterrupted();

        Element workbookEle = child(docEle, "maps"); //$NON-NLS-1$
        if (workbookEle == null)
            return;

        Element sheetEle = child(workbookEle, "map"); //$NON-NLS-1$
        if (sheetEle == null)
            return;

        loadSheet(sheetEle);
    }

    private void loadSheet(Element sheetEle) throws InterruptedException {
        checkInterrupted();

        targetSheet = getTargetWorkbook().createSheet();
        targetSheet.setTitleText(getSuggestedSheetTitle());

        Element topicEle = child(sheetEle, "topic-node"); //$NON-NLS-1$
        if (topicEle != null)
            loadRootTopic(topicEle);

        Element relationshipsEle = child(sheetEle, "link-lines"); //$NON-NLS-1$
        if (relationshipsEle != null)
            loadRelationships(relationshipsEle);

        String themeRef = att(sheetEle, "theme-ref"); //$NON-NLS-1$
        Element themeEle = findThemeEle(themeRef);
        if (themeEle != null)
            loadTheme(themeEle);

        addTargetSheet(targetSheet);
    }

    public ISheet getTargetSheet() {
        return targetSheet;
    }

    private void loadRootTopic(Element topicEle) throws InterruptedException {
        checkInterrupted();

        ITopic rootTopic = getTargetSheet().getRootTopic();
        loadTopic(topicEle, rootTopic);

        Element subTopicsEle = child(topicEle, "sub-topics"); //$NON-NLS-1$
        if (subTopicsEle != null)
            loadSubElements(subTopicsEle, rootTopic);
    }

    private void loadSubElements(Element topicsEle, ITopic topic)
            throws InterruptedException {
        checkInterrupted();

        Iterator<Element> subElements = children(topicsEle, "topic-node"); //$NON-NLS-1$

        while (subElements.hasNext()) {
            Element subElement = subElements.next();

            String type = att(subElement, "type"); //$NON-NLS-1$

            if ("Boundary".equals(type)) { //$NON-NLS-1$
                loadBoundary(subElement, topic);
            } else {
                ITopic subTopic = addElement(topic, subElement, type);

                loadTopic(subElement, subTopic);

                Element subTopicsEle = child(subElement, "sub-topics"); //$NON-NLS-1$
                if (subTopicsEle != null)
                    loadSubElements(subTopicsEle, subTopic);
            }

        }
    }

    private void loadTopic(Element topicEle, ITopic topic)
            throws InterruptedException {
        checkInterrupted();

        loadOId(topicEle, topic);
        loadTopicContent(topicEle, topic);

        loadTopicNodeStyle(topicEle, topic);
    }

    private ITopic addElement(ITopic topic, Element topicEle, String type) {
        ITopic subTopic = getTargetWorkbook().createTopic();

        if ("FloatingTopic".equals(type)) //$NON-NLS-1$
            topic.add(subTopic, ITopic.DETACHED);
        else if ("Callout".equals(type)) //$NON-NLS-1$
            topic.add(subTopic, ITopic.CALLOUT);
        else
            topic.add(subTopic);

        return subTopic;
    }

    private void loadOId(Element nmEle, IIdentifiable element) {
        String OId = att(nmEle, "id"); //$NON-NLS-1$
        if (OId != null)
            topicIdMap.put(OId, element.getId());
    }

    private void loadTopicContent(Element topicEle, ITopic topic)
            throws InterruptedException {
        checkInterrupted();

        String id = att(topicEle, "id"); //$NON-NLS-1$
        String ref = att(topicEle, "topic-ref"); //$NON-NLS-1$
        refIdMap.put(ref, id);
        Element contentEle = findTopicContentEle(ref);
        if (contentEle == null)
            return;

        loadTitleText(contentEle, topic);
        loadNotes(contentEle, topic);
        loadHyperlink(contentEle, topic);
        loadAttachments(contentEle, topic);
        loadImages(contentEle, topic);
        loadTask(contentEle, topic);
    }

    private Element findTopicContentEle(String refId)
            throws InterruptedException {
        checkInterrupted();

        if (refId == null)
            return null;

        Element contentEle = content.getDocumentElement();

        Element topicsEle = child(contentEle, "topics"); //$NON-NLS-1$

        if (topicsEle == null)
            return null;

        Iterator<Element> topicEles = children(topicsEle, "topic"); //$NON-NLS-1$

        while (topicEles.hasNext()) {
            Element topic = topicEles.next();
            String id = att(topic, "id"); //$NON-NLS-1$

            if (refId.equals(id))
                return topic;
        }

        return null;
    }

    private void loadTitleText(Element contentEle, ITopic topic)
            throws InterruptedException {
        Element text = child(contentEle, "rich-text"); //$NON-NLS-1$
        if (text == null)
            return;

        Element titleRun = child(text, "text-run"); //$NON-NLS-1$
        if (titleRun == null)
            return;

        topic.setTitleText(titleRun.getTextContent());
        loadTextStyle(titleRun, topic);
    }

    private void loadNotes(Element contentEle, ITopic topic)
            throws InterruptedException {
        checkInterrupted();

        Element notesEle = child(contentEle, "notes"); //$NON-NLS-1$
        if (notesEle == null)
            return;

        IHtmlNotesContent content = (IHtmlNotesContent) getTargetWorkbook()
                .createNotesContent(INotes.HTML);
        NotesImporter notesImporter = new NotesImporter(content);
        notesImporter.loadFrom(notesEle);
        topic.getNotes().setContent(INotes.HTML, content);
    }

    private void loadHyperlink(Element contentEle, ITopic topic)
            throws InterruptedException {
        checkInterrupted();

        Element linksEle = child(contentEle, "links"); //$NON-NLS-1$
        if (linksEle == null)
            return;

        Iterator<Element> it = children(linksEle, "link"); //$NON-NLS-1$

        while (it.hasNext()) {
            Element linkDataEle = it.next();
            String url = att(linkDataEle, "url"); //$NON-NLS-1$
            if (url == null)
                continue;

            String name = att(linkDataEle, "display-name"); //$NON-NLS-1$
            if (name == null || "".equals(name)) //$NON-NLS-1$
                name = url;
            ITopic linkTopic = getTargetWorkbook().createTopic();
            linkTopic.setTitleText(name);
            topic.add(linkTopic);

            if (url.startsWith("novamind://topic/")) { //$NON-NLS-1$
                recordTopicLink(url.replace("novamind://topic/", ""), //$NON-NLS-1$//$NON-NLS-2$
                        linkTopic);
                continue;
            } else if (!HyperlinkUtils.isLinkToWeb(url)) {
                url = FilePathParser.toURI(url, false);
            }
            linkTopic.setHyperlink(url);

        }
    }

    private void loadAttachments(Element contentEle, ITopic topic)
            throws InterruptedException {
        checkInterrupted();

        if (manifest == null)
            return;

        Element attachmentsEle = child(contentEle, "attachments"); //$NON-NLS-1$
        if (attachmentsEle == null)
            return;

        Iterator<Element> attachmentEles = children(attachmentsEle,
                "attachment"); //$NON-NLS-1$

        while (attachmentEles.hasNext()) {
            Element attDataEle = attachmentEles.next();
            IFileEntry entry = getResourceFileEntry(attDataEle);
            if (entry != null) {
                ITopic attTopic = getTargetWorkbook().createTopic();
                String title = getResourceTitle(attDataEle);
                attTopic.setTitleText(title == null
                        ? new File(entry.getPath()).getName() : title);
                attTopic.setHyperlink(
                        HyperlinkUtils.toAttachmentURL(entry.getPath()));
                topic.add(attTopic, ITopic.ATTACHED);
            }
        }
    }

    private void loadImages(Element contentEle, ITopic topic)
            throws InterruptedException {
        checkInterrupted();

        if (manifest == null)
            return;

        loadImage(contentEle, topic, "top-image", IImage.TOP, 0, 64); //$NON-NLS-1$
        loadImage(contentEle, topic, "bottom-image", IImage.BOTTOM, 0, 64); //$NON-NLS-1$
        loadImage(contentEle, topic, "left-image", Image.LEFT, 64, 0); //$NON-NLS-1$
        loadImage(contentEle, topic, "right-image", Image.RIGHT, 64, 0); //$NON-NLS-1$
    }

    private void loadImage(Element contentEle, ITopic topic, String imageAtt,
            String alignment, int w, int h) throws InterruptedException {
        Element imageEle = child(contentEle, imageAtt);
        if (imageEle != null) {
            IFileEntry entry = getResourceFileEntry(imageEle);
            if (entry != null) {
                String title = getResourceTitle(imageEle);
                ITopic attTopic = getTargetWorkbook().createTopic();
                attTopic.setTitleText(title == null
                        ? new File(entry.getPath()).getName() : title);
                IImage image = attTopic.getImage();
                image.setSource(
                        HyperlinkUtils.toAttachmentURL(entry.getPath()));
                image.setAlignment(alignment);
                image.setSize(w == 0 ? IImage.UNSPECIFIED : w,
                        h == 0 ? IImage.UNSPECIFIED : h);
                topic.add(attTopic);
            }
        }
    }

    private void loadTask(Element contentEle, ITopic topic)
            throws InterruptedException {
        checkInterrupted();

        Element taskEle = child(contentEle, "project-task"); //$NON-NLS-1$
        if (taskEle == null)
            return;

        ITopicExtensionElement taskContent = null;
        String priority = att(taskEle, "priority"); //$NON-NLS-1$
        loadMarker(topic, parsePriority(priority));

        String pc = att(taskEle, "percentage-complete"); //$NON-NLS-1$
        loadMarker(topic, parsePercentage(pc));

        if (pc != null) {
            taskContent = ensureTaskContent(topic, taskContent);
            taskContent.deleteChildren("progress"); //$NON-NLS-1$
            ITopicExtensionElement ele = taskContent.createChild("progress"); //$NON-NLS-1$
            ele.setTextContent(pc);
        }

        String start = att(taskEle, "start-time"); //$NON-NLS-1$
        if (start != null && !"".equals(start)) { //$NON-NLS-1$
            Matcher m = DATE_PATTERN.matcher(start);
            if (m.find()) {
                taskContent = ensureTaskContent(topic, taskContent);
                taskContent.deleteChildren("start-date"); //$NON-NLS-1$
                ITopicExtensionElement ele = taskContent
                        .createChild("start-date"); //$NON-NLS-1$
                ele.setTextContent(m.group(1) + " " + m.group(5)); //$NON-NLS-1$
            }
        }

        String end = att(taskEle, "end-time"); //$NON-NLS-1$
        if (end != null) {
            Matcher m = DATE_PATTERN.matcher(end);
            if (m.find()) {
                taskContent = ensureTaskContent(topic, taskContent);
                taskContent.deleteChildren("end-date");//$NON-NLS-1$
                ITopicExtensionElement ele = taskContent
                        .createChild("end-date"); //$NON-NLS-1$
                ele.setTextContent(m.group(1) + " " + m.group(5)); //$NON-NLS-1$
            }
        }

        String duration = att(taskEle, "duration"); //$NON-NLS-1$
        String dUnit = att(taskEle, "duration-unit"); //$NON-NLS-1$
        if (duration != null && !"".equals(duration)) { //$NON-NLS-1$
            String durationLabel = null;
            if ("Years".equals(dUnit)) { //$NON-NLS-1$
                durationLabel = NLS.bind(
                        ImportMessages.NovaMindImporter_Duration_Years_label,
                        duration);
            } else if ("Months".equals(dUnit)) { //$NON-NLS-1$
                durationLabel = NLS.bind(
                        ImportMessages.NovaMindImporter_Duration_Months_label,
                        duration);
            } else if ("Weeks".equals(dUnit)) { //$NON-NLS-1$
                durationLabel = NLS.bind(
                        ImportMessages.NovaMindImporter_Duration_Weeks_label,
                        duration);
            } else if ("Hours".equals(dUnit)) { //$NON-NLS-1$
                durationLabel = NLS.bind(
                        ImportMessages.NovaMindImporter_Duration_Hours_label,
                        duration);
            } else if ("Minutes".equals(dUnit)) { //$NON-NLS-1$
                durationLabel = NLS.bind(
                        ImportMessages.NovaMindImporter_Duration_Minutes_label,
                        duration);
            } else {
                durationLabel = NLS.bind(
                        ImportMessages.NovaMindImporter_Duration_Days_label,
                        duration);
            }

            topic.addLabel(
                    NLS.bind(ImportMessages.NovaMindImporter_Duration_label,
                            durationLabel));
        }

        Element resourcesEle = child(taskEle, "assigned-resources"); //$NON-NLS-1$
        if (resourcesEle != null) {
            Iterator<Element> it = children(resourcesEle,
                    "project-resource-ref"); //$NON-NLS-1$
            String assigns = ""; //$NON-NLS-1$
            while (it.hasNext()) {
                Element next = it.next();
                String ref = next.getTextContent();
                assigns += findAssign(ref) + ","; //$NON-NLS-1$
            }
            if (assigns != null && !"".equals(assigns)) { //$NON-NLS-1$
                assigns = assigns.substring(0, assigns.length() - 2);
                taskContent = ensureTaskContent(topic, taskContent);
                taskContent.deleteChildren("assigned-to");//$NON-NLS-1$
                ITopicExtensionElement ele = taskContent
                        .createChild("assigned-to"); //$NON-NLS-1$
                ele.setTextContent(assigns);
                topic.addLabel(
                        NLS.bind(ImportMessages.NovaMindImporter_Resource_label,
                                assigns.replaceAll(",", ";")));  //$NON-NLS-1$//$NON-NLS-2$
            }
        }
    }

    private void loadMarker(ITopic topic, String markerId)
            throws InterruptedException {
        checkInterrupted();

        if (markerId != null)
            topic.addMarker(markerId);
    }

    private static String parsePriority(String nmPriority) {
        if (nmPriority == null)
            return null;

        try {
            int priority = Integer.parseInt(nmPriority);
            switch (priority) {
            case 1:
                return "priority-1"; //$NON-NLS-1$
            case 2:
                return "priority-2"; //$NON-NLS-1$
            case 3:
                return "priority-3"; //$NON-NLS-1$
            case 4:
                return "priority-4"; //$NON-NLS-1$
            case 5:
                return "priority-5"; //$NON-NLS-1$
            case 6:
                return "priority-6"; //$NON-NLS-1$
            case 7:
                return "priority-7"; //$NON-NLS-1$
            case 8:
                return "priority-8"; //$NON-NLS-1$
            case 9:
                return "priority-9"; //$NON-NLS-1$
            }
        } catch (NumberFormatException e) {
        }

        return null;
    }

    @SuppressWarnings("nls")
    private static String parsePercentage(String nmPercentage) {
        if (nmPercentage == null)
            return null;

        try {
            int pc = Integer.parseInt(nmPercentage);
            if (pc < 12)
                return "task-start";
            if (pc < 25)
                return "task-oct";
            if (pc < 37)
                return "task-quarter";
            if (pc < 50)
                return "task-3oct";
            if (pc < 62)
                return "task-half";
            if (pc < 75)
                return "task-5oct";
            if (pc < 87)
                return "task-3quar";
            if (pc < 100)
                return "task-7oct";
            if (pc == 100)
                return "task-done";
        } catch (NumberFormatException e) {
        }
        return null;
    }

    private void loadTextStyle(Element titleEle, IStyled host)
            throws InterruptedException {
        checkInterrupted();

        String nmColor = att(titleEle, "font-color"); //$NON-NLS-1$
        if (nmColor != null) {
            nmColor = realNmColor(nmColor);
            registerStyle(host, Styles.TextColor,
                    parseColor(nmColor.substring(3)));
        }

        registerStyle(host, Styles.FontSize,
                parseFontSize(att(titleEle, "font-size"))); //$NON-NLS-1$
        registerStyle(host, Styles.FontWeight,
                Boolean.parseBoolean(att(titleEle, "bold")) //$NON-NLS-1$
                        ? Styles.FONT_WEIGHT_BOLD : null);
        registerStyle(host, Styles.FontStyle,
                Boolean.parseBoolean(att(titleEle, "italic")) //$NON-NLS-1$
                        ? Styles.FONT_STYLE_ITALIC : null);
        String textDecoration = StyleUtils.toTextDecoration(
                att(titleEle, "underline-style") != null, //$NON-NLS-1$
                att(titleEle, "strikethrough-style") != null);  //$NON-NLS-1$
        registerStyle(host, Styles.TextDecoration, textDecoration);
        String alignment = att(titleEle, "alignment"); //$NON-NLS-1$
        if (alignment != null)
            registerStyle(host, Styles.TextAlign, alignment.toLowerCase());

        String fontName = att(titleEle, "mac-font-name"); //$NON-NLS-1$
        if (fontName == null)
            fontName = att(titleEle, "windows-font-name"); //$NON-NLS-1$

        if (fontName != null) {
            String availableFontName = FontUtils
                    .getAAvailableFontNameFor(fontName);
            fontName = availableFontName != null ? availableFontName : fontName;
            registerStyle(host, Styles.FontFamily, fontName);
        }

    }

    private void loadTopicNodeStyle(Element topicEle, ITopic topic)
            throws InterruptedException {
        checkInterrupted();

        Element nodeViewEle = child(topicEle, "topic-node-view"); //$NON-NLS-1$
        if (nodeViewEle == null)
            return;

        loadPosition(nodeViewEle, topic);

        Element nodeStyleEle = child(nodeViewEle, "topic-node-style"); //$NON-NLS-1$
        if (nodeStyleEle == null)
            return;

        loadFillStyle(nodeStyleEle, topic, null);
        loadShapeStyle(nodeStyleEle, topic);
        loadLineStyle(nodeStyleEle, topic);
        loadStructure(nodeStyleEle, topic);
        loadConnectionStyle(nodeStyleEle, topic);
        loadNumbering(nodeStyleEle, topic);
    }

    private void loadPosition(Element nodeViewEle, ITopic topic)
            throws InterruptedException {
        checkInterrupted();

        String location = att(nodeViewEle, "manual-location"); //$NON-NLS-1$
        if ((location == null || "".equals(location)) //$NON-NLS-1$
                && (ITopic.DETACHED.equals(topic.getType())
                        || ITopic.CALLOUT.equals(topic.getType())))
            location = att(nodeViewEle, "location"); //$NON-NLS-1$

        if (location == null || "".equals(location)) //$NON-NLS-1$
            return;

        String[] split = location.split(","); //$NON-NLS-1$
        if (split.length != 2)
            return;

        Float x = parseFloat(split[0]);
        Float y = parseFloat(split[1]);
        if (x != null && y != null && x.floatValue() != 0
                && y.floatValue() != 0) {
            topic.setPosition(mm2Dots(x), mm2Dots(y));
        }
    }

    private void loadFillStyle(Element nodeStyleEle, IStyled host,
            String defaultColor) throws InterruptedException {
        checkInterrupted();

        Element fillStyleEle = child(nodeStyleEle, "fill-style"); //$NON-NLS-1$
        if (fillStyleEle == null)
            return;

        String nmColor = getFillValue(fillStyleEle);
        if (nmColor == null) {
            if (defaultColor == null)
                return;
            nmColor = defaultColor;
        }

        nmColor = realNmColor(nmColor);

        String opacity = parseAlpha(nmColor);
        String fillColor = parseColor(nmColor.substring(3));
        if (opacity != null && !opacity.equals(TRANSPARENT_VALUE))
            registerStyle(host, Styles.Opacity, opacity);

        if (fillColor != null)
            registerStyle(host, Styles.FillColor, fillColor);
    }

    private String realNmColor(String nmColor) {
        if (!nmColor.startsWith("#")) //$NON-NLS-1$
            nmColor = getMapping("nmColor", nmColor, "#FFFFF");  //$NON-NLS-1$//$NON-NLS-2$
        return nmColor;
    }

    private String getFillValue(Element fillStyleEle)
            throws InterruptedException {
        checkInterrupted();

        String fillValue = null;

        Element advancedGradientEle = child(fillStyleEle, "advanced-gradient"); //$NON-NLS-1$
        if (advancedGradientEle != null) {
            Element advancedColors = child(advancedGradientEle, "colors"); //$NON-NLS-1$
            if (advancedColors != null) {
                Element colorEle = child(advancedColors, "color-stop"); //$NON-NLS-1$
                if (colorEle != null)
                    fillValue = att(colorEle, "color"); //$NON-NLS-1$
            }
        }

        if (fillValue == null) {
            Element simpleGradientEle = child(fillStyleEle, "simple-gradient"); //$NON-NLS-1$
            if (simpleGradientEle != null) {
                String sColor = att(simpleGradientEle, "start-color"); //$NON-NLS-1$
                String eColor = att(simpleGradientEle, "end-color"); //$NON-NLS-1$
                fillValue = sColor != null ? sColor : eColor;
            }
        }

        if (fillValue == null) {
            Element solidColorEle = child(fillStyleEle, "solid-color"); //$NON-NLS-1$
            if (solidColorEle != null)
                fillValue = att(solidColorEle, "color"); //$NON-NLS-1$
        }

        return fillValue;
    }

    private String parseAlpha(String color) {
        if (color != null && color.startsWith("#")) { //$NON-NLS-1$
            try {
                int alpha = Integer.parseInt(color.substring(1, 2), 16);
                double opacity = ((double) alpha) * 100 / 255;
                return String.format("%.2f", opacity); //$NON-NLS-1$
            } catch (Exception e) {
            }
        }

        return null;
    }

    private static String parseColor(String color) {
        if (color != null) {
            int r;
            int g;
            int b;
            try {
                r = Integer.parseInt(color.substring(0, 2), 16);
                g = Integer.parseInt(color.substring(2, 4), 16);
                b = Integer.parseInt(color.substring(4, 6), 16);
                return ColorUtils.toString(r, g, b);
            } catch (Throwable t) {
            }
        }
        return null;
    }

    private static String parseFontSize(String size) {
        if (size != null) {
            try {
                double value = Double.parseDouble(size);
                size = StyleUtils.addUnitPoint((int) value);
            } catch (Exception e) {
            }
        }
        return size;
    }

    private void loadShapeStyle(Element nodeStyleEle, IStyled host)
            throws InterruptedException {
        checkInterrupted();

        Element shapeStyleEle = child(nodeStyleEle, "shape-style"); //$NON-NLS-1$
        if (shapeStyleEle == null)
            return;

        if (host instanceof ITopic) {
            Element topicShapeEle = child(shapeStyleEle, "topic-shape-style"); //$NON-NLS-1$
            if (topicShapeEle != null) {
                String shape = att(topicShapeEle, "type"); //$NON-NLS-1$
                if (ITopic.CALLOUT.equals(((ITopic) host).getType()))
                    registerStyle(host, Styles.CalloutShapeClass,
                            parseCalloutShape(shape));
                else
                    registerStyle(host, Styles.ShapeClass,
                            parseTopicShape(shape));
            }

        }

        if (host instanceof IBoundary) {
            Element boundaryShapeEle = child(shapeStyleEle,
                    "boundary-shape-style"); //$NON-NLS-1$
            if (boundaryShapeEle != null)
                registerStyle(host, Styles.ShapeClass,
                        parseBoundaryShape(att(boundaryShapeEle, "type"))); //$NON-NLS-1$

        }

    }

    private void loadLineStyle(Element nodeStyleEle, IStyled host)
            throws InterruptedException {
        checkInterrupted();

        Element lineStyleEle = child(nodeStyleEle, "line-style"); //$NON-NLS-1$
        if (lineStyleEle == null)
            return;

        String nmColor = att(lineStyleEle, "color"); //$NON-NLS-1$
        if (nmColor != null) {
            nmColor = realNmColor(nmColor);
            registerStyle(host, Styles.LineColor,
                    parseColor(nmColor.substring(3)));
        }

        String width = parseLineWidth(att(lineStyleEle, "stroke-width")); //$NON-NLS-1$
        registerStyle(host, Styles.LineWidth, width);

        String stroke = att(lineStyleEle, "draws-stroke"); //$NON-NLS-1$
        if (Boolean.parseBoolean(stroke))
            registerStyle(host, Styles.BorderLineWidth, "0"); //$NON-NLS-1$

        String linePattern = att(lineStyleEle, "dash"); //$NON-NLS-1$
        registerStyle(host, Styles.LinePattern, parseLinePattern(linePattern));
    }

    private static String parseLinePattern(String linePattern) {
        if (linePattern == null)
            return Styles.LINE_PATTERN_SOLID;

        char c = linePattern.charAt(0);

        switch (c) {
        case 'b':
        case 'e':
        case 'i':
        case 'r':
            return Styles.LINE_PATTERN_DASH;
        case 'c':
        case 'f':
        case 'l':
        case 's':
        case 'y':
            return Styles.LINE_PATTERN_DOT;
        case 'd':
        case 'g':
        case 'j':
        case 'm':
        case 'o':
        case 'p':
        case 't':
        case 'v':
        case 'w':
            return Styles.LINE_PATTERN_DASH_DOT;
        case 'h':
        case 'k':
        case 'n':
        case 'q':
        case 'u':
        case 'x':
            return Styles.LINE_PATTERN_DASH_DOT_DOT;
        }

        return Styles.LINE_PATTERN_SOLID;
    }

    private static String parseLineWidth(String width) {
        if (width == null)
            return null;

        Float w = parseFloat(width.replace("pt", "")); //$NON-NLS-1$ //$NON-NLS-2$
        if (w != null) {
            if (w <= 2)
                return "1"; //$NON-NLS-1$
            if (w <= 4)
                return "2"; //$NON-NLS-1$
            if (w <= 6)
                return "3"; //$NON-NLS-1$
            if (w <= 8)
                return "4"; //$NON-NLS-1$
            return "5"; //$NON-NLS-1$
        }
        return null;
    }

    private static Float parseFloat(String value) {
        if (value != null) {
            try {
                return Float.valueOf(value);
            } catch (Throwable e) {
            }
        }
        return null;
    }

    private void loadStructure(Element styleEle, ITopic topic)
            throws InterruptedException {
        checkInterrupted();

        Element layoutStyle = child(styleEle, "children-layout-style"); //$NON-NLS-1$
        if (layoutStyle == null)
            return;

        topic.setStructureClass(
                parseStructureType(topic, att(layoutStyle, "angle"), //$NON-NLS-1$
                        att(layoutStyle, "layout-mode"))); //$NON-NLS-1$
    }

    private static String parseStructureType(ITopic topic, String angle,
            String mode) {
        if (mode != null && mode.equalsIgnoreCase("Radial")) { //$NON-NLS-1$
            return "org.xmind.ui.map.unbalanced"; //$NON-NLS-1$
        } else {
            if ("0".equals(angle)) //$NON-NLS-1$
                return "org.xmind.ui.logic.right"; //$NON-NLS-1$
            if ("90".equals(angle)) //$NON-NLS-1$
                return "org.xmind.ui.org-chart.down"; //$NON-NLS-1$
            if ("180".equals(angle)) //$NON-NLS-1$
                return "org.xmind.ui.logic.left"; //$NON-NLS-1$
            if ("270".equals(angle)) //$NON-NLS-1$
                return "org.xmind.ui.org-chart.up"; //$NON-NLS-1$
        }

        if (topic.getParent() != null)
            return topic.getParent().getStructureClass();

        return null;
    }

    private void loadConnectionStyle(Element styleEle, ITopic topic)
            throws InterruptedException {
        checkInterrupted();

        Element connectionStyleEle = child(styleEle,
                "children-connection-style"); //$NON-NLS-1$
        if (connectionStyleEle == null)
            return;

        String type = att(connectionStyleEle, "connection-type"); //$NON-NLS-1$
        registerStyle(topic, Styles.LineClass, parseBranchConnection(type));
    }

    private static String parseBranchConnection(String lineShape) {
        return getMapping("branchConnection", lineShape, //$NON-NLS-1$
                "org.xmind.branchConnection.curve");  //$NON-NLS-1$
    }

    private void loadNumbering(Element styleEle, ITopic topic)
            throws InterruptedException {
        checkInterrupted();

        Element numberingEle = child(styleEle,
                "children-outline-numbering-style"); //$NON-NLS-1$
        if (numberingEle == null)
            return;

        String depthValue = att(numberingEle, "depth"); //$NON-NLS-1$
        if (depthValue == null || "".equals(depthValue)) //$NON-NLS-1$
            return;

        INumbering numbering = topic.getNumbering();
        if ("0".equals(depthValue)) { //$NON-NLS-1$
            numbering.setFormat("org.xmind.numbering.none"); //$NON-NLS-1$
            return;
        }

        int depth = Integer.parseInt(depthValue);
        depth = depth == -1 ? 10 : depth;

        String format = "org.xmind.numbering.arabic"; //$NON-NLS-1$
        String separator = "org.xmind.numbering.separator.dot"; //$NON-NLS-1$
        String prefix = null;
        String suffix = null;

        Element formatArrayEle = child(numberingEle, "format-array"); //$NON-NLS-1$
        if (formatArrayEle != null) {
            Element formatEle = child(formatArrayEle, "outline-format"); //$NON-NLS-1$
            if (formatEle != null) {
                format = parseNumberFormat(att(formatEle, "style")); //$NON-NLS-1$
                separator = parseNumberSeprator(att(formatEle, "separator")); //$NON-NLS-1$
                prefix = att(formatEle, "prefix"); //$NON-NLS-1$
                suffix = att(formatEle, "suffix"); //$NON-NLS-1$
            }
        }

        numbering.setFormat(format);
        numbering.setSeparator(separator);
        numbering.setDepth(String.valueOf(depth));
        if (prefix != null)
            numbering.setPrefix(prefix);
        if (suffix != null)
            numbering.setSuffix(suffix);
    }

    private static String parseNumberFormat(String format) {
        return getMapping("numberFormat", format, "org.xmind.numbering.arabic");  //$NON-NLS-1$//$NON-NLS-2$
    }

    private static String parseNumberSeprator(String seprator) {
        return getMapping("numberSeprator", seprator, //$NON-NLS-1$
                "org.xmind.numbering.separator.dot");  //$NON-NLS-1$
    }

    private void loadBoundary(Element boundaryEle, ITopic topic)
            throws InterruptedException {
        checkInterrupted();

        if (topic.isRoot())
            return;

        IBoundary boundary = getTargetWorkbook().createBoundary();
        if (topic.isAttached()) {
            ITopic parent = topic.getParent();
            int index = topic.getIndex();
            boundary.setStartIndex(index);
            boundary.setEndIndex(index);
            parent.addBoundary(boundary);
        } else {
            boundary.setMasterBoundary(true);
            topic.addBoundary(boundary);
        }

        loadBoundaryStyle(boundaryEle, boundary);
    }

    private void loadBoundaryStyle(Element boundaryEle, IBoundary boundary)
            throws InterruptedException {
        checkInterrupted();

        Element nodeViewEle = child(boundaryEle, "topic-node-view"); //$NON-NLS-1$
        if (nodeViewEle == null)
            return;

        Element nodeStyleEle = child(nodeViewEle, "topic-node-style"); //$NON-NLS-1$
        if (nodeStyleEle == null)
            return;

        loadFillStyle(nodeStyleEle, boundary, "#fffce0bf"); //$NON-NLS-1$
        loadLineStyle(nodeStyleEle, boundary);
        loadShapeStyle(nodeStyleEle, boundary);
    }

    private void loadRelationships(Element relationshipsEle)
            throws InterruptedException {
        checkInterrupted();

        Iterator<Element> relationshipEles = children(relationshipsEle,
                "topic-node"); //$NON-NLS-1$

        while (relationshipEles.hasNext())
            loadRelationship(relationshipEles.next());
    }

    private void loadRelationship(Element relationshipEle)
            throws InterruptedException {
        checkInterrupted();

        Element relDataEle = child(relationshipEle, "link-line-data"); //$NON-NLS-1$
        if (relDataEle == null)
            return;

        IRelationship rel = getTargetWorkbook().createRelationship();
        getTargetSheet().addRelationship(rel);

        loadConnections(relDataEle, rel, true);

        Element nodeViewEle = child(relationshipEle, "topic-node-view"); //$NON-NLS-1$
        if (nodeViewEle == null)
            return;

        Element styleEle = child(nodeViewEle, "topic-node-style"); //$NON-NLS-1$
        if (styleEle == null)
            return;

        loadLineStyle(styleEle, rel);
    }

    private void loadConnections(Element relDataEle, IRelationship rel,
            boolean autoRouting) throws InterruptedException {
        checkInterrupted();

        String startOId = att(relDataEle, "start-topic-node-ref"); //$NON-NLS-1$
        String endOId = att(relDataEle, "end-topic-node-ref"); //$NON-NLS-1$

        String startId = topicIdMap.get(startOId);
        String endId = topicIdMap.get(endOId);

        if (startId != null && endId != null) {
            rel.setEnd1Id(startId);
            rel.setEnd2Id(endId);
        }

        // TODO control point
//        String isAuto = att(relDataEle, "is-automatic-link-line"); //$NON-NLS-1$
//        if (!Boolean.parseBoolean(isAuto)) {
//            Element cpsEle = child(relDataEle, "control-points"); //$NON-NLS-1$
//            if (cpsEle != null) {
//                Iterator<Element> cpEles = children(cpsEle, "control-point"); //$NON-NLS-1$
//                int index = 0;
//                while (cpEles.hasNext()) {
//                    String loc = att(cpEles.next(), "location"); //$NON-NLS-1$
//                    String[] split = loc.split(","); //$NON-NLS-1$
//                    Float x = parseFloat(split[0]);
//                    Float y = parseFloat(split[1]);
//                    if (x != null && y != null && x.floatValue() != 0
//                            && y.floatValue() != 0) {
//                        rel.getControlPoint(index++).setPosition(
//                                mm2Dots(x.floatValue()),
//                                mm2Dots(y.floatValue()));
//                    }
//                }
//
//            }
//        }

        Element startStyleEle = child(relDataEle, "start-terminator"); //$NON-NLS-1$
        if (startStyleEle != null)
            registerStyle(rel, Styles.ArrowBeginClass,
                    parseRelTerminatorType(att(startStyleEle, "type"))); //$NON-NLS-1$

        Element endStyleEle = child(relDataEle, "end-terminator");//$NON-NLS-1$
        if (endStyleEle != null)
            registerStyle(rel, Styles.ArrowEndClass,
                    parseRelTerminatorType(att(endStyleEle, "type"))); //$NON-NLS-1$
    }

    private String parseRelTerminatorType(String type) {
        return getMapping("arrowShape", type, null); //$NON-NLS-1$
    }

    private static String parseTopicShape(String nmShape) {
        return getMapping("topicShape", nmShape, null); //$NON-NLS-1$
    }

    private static String parseCalloutShape(String nmShape) {
        return getMapping("calloutShape", nmShape, null); //$NON-NLS-1$
    }

    private static String parseBoundaryShape(String nmShape) {
        return getMapping("boundaryShape", nmShape, //$NON-NLS-1$
                "org.xmind.boundaryShape.polygon");  //$NON-NLS-1$
    }

    private void registerStyle(IStyled styleOwner, String key, String value) {
        if (value == null)
            return;

        IStyle style = styleMap.get(styleOwner);
        if (style == null) {
            style = getTempStyleSheet().createStyle(styleOwner.getStyleType());
            getTempStyleSheet().addStyle(style, IStyleSheet.NORMAL_STYLES);
            styleMap.put(styleOwner, style);
        }
        if (Styles.TextDecoration.equals(key)) {
            String oldValue = style.getProperty(key);
            if (oldValue != null && !oldValue.contains(value)) {
                boolean underline = oldValue
                        .contains(Styles.TEXT_DECORATION_UNDERLINE)
                        || value.contains(Styles.TEXT_DECORATION_UNDERLINE);
                boolean strikeout = oldValue
                        .contains(Styles.TEXT_DECORATION_LINE_THROUGH)
                        || value.contains(Styles.TEXT_DECORATION_LINE_THROUGH);
                value = StyleUtils.toTextDecoration(underline, strikeout);
            }
        }
        style.setProperty(key, value);
    }

    private IStyleSheet getTempStyleSheet() {
        if (tempStyleSheet == null) {
            tempStyleSheet = Core.getStyleSheetBuilder().createStyleSheet();
            ((StyleSheetImpl) tempStyleSheet)
                    .setManifest(getTargetWorkbook().getManifest());
        }
        return tempStyleSheet;
    }

    private void arrangeStyles() throws InterruptedException {
        IStyleSheet targetStyleSheet = getTargetWorkbook().getStyleSheet();
        for (Entry<IStyled, IStyle> en : styleMap.entrySet()) {
            checkInterrupted();
            IStyled styleOwner = en.getKey();
            IStyle style = en.getValue();
            IStyle importedStyle = targetStyleSheet.importStyle(style);
            if (importedStyle != null) {
                styleOwner.setStyleId(importedStyle.getId());
            }
        }
    }

    private void loadTheme(Element themeEle) throws InterruptedException {
        checkInterrupted();

        loadSheetStyle(themeEle);
        loadTopicTheme(themeEle);
        loadRelTheme(themeEle);
        loadBoundaryTheme(themeEle);
    }

    private void loadSheetStyle(Element themeEle) throws InterruptedException {
        checkInterrupted();

        Element styleEle = child(themeEle, "background-style"); //$NON-NLS-1$
        if (styleEle == null)
            return;

        ISheet sheet = getTargetSheet();

        registerStyle(sheet, Styles.LineTapered, Styles.TAPERED);

        Element bgFillEle = child(styleEle, "solid-color"); //$NON-NLS-1$
        if (bgFillEle != null) {
            String nmColor = att(bgFillEle, "color"); //$NON-NLS-1$
            if (nmColor != null) {
                registerStyle(sheet, Styles.FillColor,
                        parseColor(realNmColor(nmColor).substring(3)));
            }
        }

        Element bgImageEle = child(styleEle, "texture-image"); //$NON-NLS-1$
        if (bgImageEle != null) {
            IFileEntry entry = getResourceFileEntry(bgImageEle);
            if (entry != null && entry.getSize() > 0) {
                registerStyle(sheet, Styles.Background,
                        HyperlinkUtils.toAttachmentURL(entry.getPath()));
                registerStyle(sheet, Styles.Opacity,
                        att(bgImageEle, "opacity"));  //$NON-NLS-1$
            }
        }
    }

    private void loadTopicTheme(Element themeEle) throws InterruptedException {
        checkInterrupted();

        Element rootStyleEle = child(themeEle, "root-topic-style"); //$NON-NLS-1$
        if (rootStyleEle != null)
            loadTopicTheme(rootStyleEle, Styles.FAMILY_CENTRAL_TOPIC, true);

        Element floatStyleEle = child(themeEle, "floating-topic-style"); //$NON-NLS-1$
        if (floatStyleEle != null)
            loadTopicTheme(floatStyleEle, Styles.FAMILY_FLOATING_TOPIC, false);

        Element calloutStyleEle = child(themeEle, "callout-style"); //$NON-NLS-1$
        if (calloutStyleEle != null)
            loadTopicTheme(calloutStyleEle, Styles.FAMILY_CALLOUT_TOPIC, false);
    }

    private void loadTopicTheme(Element parentEle, String styleFamily,
            boolean withChildren) throws InterruptedException {
        checkInterrupted();

        loadThemeFill(parentEle, IStyle.TOPIC, styleFamily);
        loadThemeLineStyle(parentEle, IStyle.TOPIC, styleFamily);
        loadThemeShapeStyle(parentEle, IStyle.TOPIC, styleFamily);
        loadThemeTextStyle(parentEle, IStyle.TOPIC, styleFamily);

        Element childEle = child(parentEle, "children-default-style"); //$NON-NLS-1$
        if (withChildren && childEle != null)
            loadTopicTheme(childEle, Styles.FAMILY_SUB_TOPIC, false);
    }

    private void loadThemeFill(Element parentEle, String type,
            String styleFamily) throws InterruptedException {
        checkInterrupted();

        Element fillStyleEle = child(parentEle, "fill-style"); //$NON-NLS-1$
        if (fillStyleEle != null) {
            String nmColor = getFillValue(fillStyleEle);
            if (nmColor != null) {
                nmColor = realNmColor(nmColor);
                String fillColor = parseColor(nmColor.substring(3));
                registerTheme(type, styleFamily, Styles.FillColor, fillColor);
            }
        }
    }

    private void loadThemeTextStyle(Element parentEle, String type,
            String styleFamily) throws InterruptedException {
        checkInterrupted();
        Element textEle = child(parentEle, "text-style"); //$NON-NLS-1$
        if (textEle == null)
            return;

        String nmColor = att(textEle, "font-color"); //$NON-NLS-1$
        if (nmColor != null) {
            nmColor = realNmColor(nmColor);
            registerTheme(type, styleFamily, Styles.TextColor,
                    parseColor(nmColor.substring(3)));
        }

        registerTheme(type, styleFamily, Styles.FontSize,
                parseFontSize(att(textEle, "font-size"))); //$NON-NLS-1$
        registerTheme(type, styleFamily, Styles.FontWeight,
                Boolean.parseBoolean(att(textEle, "bold")) //$NON-NLS-1$
                        ? Styles.FONT_WEIGHT_BOLD : null);
        registerTheme(type, styleFamily, Styles.FontStyle,
                Boolean.parseBoolean(att(textEle, "italic")) //$NON-NLS-1$
                        ? Styles.FONT_STYLE_ITALIC : null);
        String textDecoration = StyleUtils.toTextDecoration(
                att(textEle, "underline-style") != null, //$NON-NLS-1$
                att(textEle, "strikethrough-style") != null);  //$NON-NLS-1$
        registerTheme(type, styleFamily, Styles.TextDecoration, textDecoration);

        String alignment = att(textEle, "alignment"); //$NON-NLS-1$
        if (alignment != null)
            registerTheme(type, styleFamily, Styles.TextAlign,
                    alignment.toLowerCase());

        String fontName = att(textEle, "mac-font-name"); //$NON-NLS-1$
        if (fontName == null)
            fontName = att(textEle, "windows-font-name"); //$NON-NLS-1$

        if (fontName != null) {
            String availableFontName = FontUtils
                    .getAAvailableFontNameFor(fontName);
            fontName = availableFontName != null ? availableFontName : fontName;
            registerTheme(type, styleFamily, Styles.FontFamily, fontName);
        }

    }

    private void loadThemeLineStyle(Element parentEle, String type,
            String styleFamily) throws InterruptedException {
        checkInterrupted();

        Element lineStyleEle = child(parentEle, "line-style"); //$NON-NLS-1$
        if (lineStyleEle == null)
            return;

        String nmColor = att(lineStyleEle, "color"); //$NON-NLS-1$
        if (nmColor != null) {
            nmColor = realNmColor(nmColor);
            registerTheme(type, styleFamily, Styles.LineColor, nmColor);
        }

        String width = parseLineWidth(att(lineStyleEle, "stroke-width")); //$NON-NLS-1$
        registerTheme(type, styleFamily, Styles.LineWidth, width);

        String stroke = att(lineStyleEle, "draws-stroke"); //$NON-NLS-1$
        if (Boolean.parseBoolean(stroke))
            registerTheme(type, styleFamily, Styles.BorderLineWidth, "0"); //$NON-NLS-1$

        String linePattern = att(lineStyleEle, "dash"); //$NON-NLS-1$
        registerTheme(type, styleFamily, Styles.LinePattern, linePattern);
    }

    private void loadThemeShapeStyle(Element parentEle, String type,
            String styleFamily) throws InterruptedException {
        checkInterrupted();

        Element shapeStyleEle = child(parentEle, "shape-style"); //$NON-NLS-1$
        if (shapeStyleEle == null)
            return;

        if (IStyle.TOPIC.equals(type)) {
            Element topicShapeEle = child(shapeStyleEle, "topic-shape-style"); //$NON-NLS-1$
            if (topicShapeEle != null) {
                String shape = att(topicShapeEle, "type"); //$NON-NLS-1$
                if (Styles.FAMILY_CALLOUT_TOPIC.equals(styleFamily))
                    registerTheme(type, styleFamily, Styles.CalloutShapeClass,
                            parseCalloutShape(shape));
                else
                    registerTheme(type, styleFamily, Styles.ShapeClass,
                            parseTopicShape(shape));
            }

        }

        if (IStyle.BOUNDARY.equals(type)) {
            Element boundaryShapeEle = child(shapeStyleEle,
                    "boundary-shape-style"); //$NON-NLS-1$
            if (boundaryShapeEle != null)
                registerTheme(type, styleFamily, Styles.ShapeClass,
                        parseBoundaryShape(att(boundaryShapeEle, "type"))); //$NON-NLS-1$

        }
    }

    private void loadRelTheme(Element themeEle) throws InterruptedException {
        checkInterrupted();

        Element relEle = child(themeEle, "link-link-style"); //$NON-NLS-1$
        if (relEle == null)
            return;

        loadThemeLineStyle(relEle, IStyle.RELATIONSHIP,
                Styles.FAMILY_RELATIONSHIP);
    }

    private void loadBoundaryTheme(Element themeEle)
            throws InterruptedException {
        checkInterrupted();

        Element boundaryEle = child(themeEle, "boundary-style-1"); //$NON-NLS-1$
        if (boundaryEle == null)
            return;

        loadThemeShapeStyle(boundaryEle, IStyle.BOUNDARY,
                Styles.FAMILY_BOUNDARY);
        loadThemeFill(boundaryEle, IStyle.BOUNDARY, Styles.FAMILY_BOUNDARY);
        loadThemeLineStyle(boundaryEle, IStyle.BOUNDARY,
                Styles.FAMILY_BOUNDARY);
    }

    private void registerTheme(String type, String styleFamily, String styleKey,
            String styleValue) throws InterruptedException {
        checkInterrupted();
        if (styleFamily == null || styleKey == null || styleValue == null)
            return;

        if (theme == null) {
            theme = getTempStyleSheet().createStyle(IStyle.THEME);
            getTempStyleSheet().addStyle(theme, IStyleSheet.MASTER_STYLES);
        }

        IStyle defaultStyle = theme.getDefaultStyle(styleFamily);
        if (defaultStyle == null) {
            defaultStyle = getTempStyleSheet().createStyle(type);
            getTempStyleSheet().addStyle(defaultStyle,
                    IStyleSheet.AUTOMATIC_STYLES);
        }
        defaultStyle.setProperty(styleKey, styleValue);
    }

    private void generateTheme() throws InterruptedException {
        checkInterrupted();
        if (theme != null) {
            IStyle importedTheme = getTargetWorkbook().getStyleSheet()
                    .importStyle(theme);
            if (importedTheme != null) {
                getTargetSheet().setThemeId(importedTheme.getId());
            }
        }
    }

    private void recordTopicLink(String OId, ITopic sourceTopic) {
        List<ITopic> topics = topicLinkMap.get(OId);
        if (topics == null) {
            topics = new ArrayList<ITopic>();
            topicLinkMap.put(OId, topics);
        }
        topics.add(sourceTopic);
    }

    private void setTopicLinks() {
        for (Entry<String, List<ITopic>> en : topicLinkMap.entrySet()) {
            String Oid = refIdMap.get(en.getKey());
            String id = topicIdMap.get(Oid);
            if (id != null) {
                for (ITopic topic : en.getValue()) {
                    topic.setHyperlink(HyperlinkUtils.toInternalURL(id));
                }
            }
        }
    }

    private String getResourceTitle(Element originEle)
            throws InterruptedException {
        checkInterrupted();

        Element resourceEle = findResourceEle(originEle);
        if (resourceEle == null)
            return null;

        return att(resourceEle, "original-filename"); //$NON-NLS-1$
    }

    private IFileEntry getResourceFileEntry(Element originEle)
            throws InterruptedException {
        checkInterrupted();

        Element resourceEle = findResourceEle(originEle);
        if (resourceEle == null)
            return null;

        String url = att(resourceEle, "url"); //$NON-NLS-1$
        String name = att(resourceEle, "original-filename"); //$NON-NLS-1$
        if (url != null && !"".equals(url)) //$NON-NLS-1$
            return loadAttachment(RESOURCES_FOLDER + url, name);

        return null;
    }

    private Element findResourceEle(Element originEle)
            throws InterruptedException {
        checkInterrupted();
        if (originEle == null)
            return null;

        String resourceRef = att(originEle, "resource-ref"); //$NON-NLS-1$
        if (resourceRef == null || "".equals(resourceRef)) //$NON-NLS-1$
            return null;

        Element resourceEle = resourceRefMap.get(resourceRef);
        if (resourceEle != null)
            return resourceEle;

        Element resourcesEle = child(manifest.getDocumentElement(),
                "resources"); //$NON-NLS-1$

        if (resourcesEle == null)
            return null;

        Iterator<Element> it = children(resourcesEle, "resource"); //$NON-NLS-1$

        while (it.hasNext()) {
            Element next = it.next();
            if (resourceRef.equals(att(next, "id"))) { //$NON-NLS-1$
                resourceRefMap.put(resourceRef, next);
                return next;
            }
        }

        return null;
    }

    @SuppressWarnings({ "deprecation", "resource" })
    private IFileEntry loadAttachment(String url, String proposalName)
            throws InterruptedException {
        checkInterrupted();

        if (resourceMap.containsKey(url)) {
            String path = resourceMap.get(url);
            return path == null ? null
                    : getTargetWorkbook().getManifest().getFileEntry(path);
        }

        InputStream nmEntryStream = tempSource.getEntryStream(url);

        if (nmEntryStream == null)
            return null;

        if (proposalName != null) {
            if (proposalName.startsWith("*.")) { //$NON-NLS-1$
                String ext = proposalName.substring(1);
                String oldName = new File(url).getName();
                proposalName = FileUtils.getNoExtensionFileName(oldName) + ext;
            }
        }

        MonitoredInputStream in = new MonitoredInputStream(nmEntryStream,
                getMonitor());

        String path = null;
        try {
            IFileEntry entry = getTargetWorkbook().getManifest()
                    .createAttachmentFromStream(in, proposalName);
            path = entry.getPath();
        } catch (Exception e) {
            log(e, "Failed to create attachment from: " + url); //$NON-NLS-1$
        } finally {
            try {
                in.close();
            } catch (IOException e) {
            }
        }

        resourceMap.put(url, path);
        return getTargetWorkbook().getManifest().getFileEntry(path);
    }

    private static ITopicExtensionElement ensureTaskContent(ITopic topic,
            ITopicExtensionElement taskContent) {
        if (taskContent != null)
            return taskContent;
        ITopicExtension ext = topic.createExtension("org.xmind.ui.taskInfo"); //$NON-NLS-1$
        return ext.getContent();
    }

    private Element findThemeEle(String themeRef) throws InterruptedException {
        checkInterrupted();

        if (themeRef == null || "".equals(themeRef)) //$NON-NLS-1$
            return null;

        Element themeEle = themeRefMap.get(themeRef);
        if (themeEle != null)
            return themeEle;

        if (styleSheet == null)
            return null;

        Element styleSheetEle = styleSheet.getDocumentElement();
        if (styleSheetEle == null)
            return null;

        Element themesEle = child(styleSheetEle, "themes"); //$NON-NLS-1$
        if (themesEle == null)
            return null;

        Iterator<Element> it = children(themesEle, "theme"); //$NON-NLS-1$
        while (it.hasNext()) {
            Element next = it.next();
            if (themeRef.equals(att(next, "id"))) { //$NON-NLS-1$
                themeRefMap.put(themeRef, next);
                return next;
            }
        }

        return null;
    }

    private String findAssign(String ref) throws InterruptedException {
        checkInterrupted();

        if (ref == null || "".equals(ref)) //$NON-NLS-1$
            return null;

        String assign = assignRefMap.get(ref);
        if (assign != null)
            return assign;

        Element contentEle = content.getDocumentElement();

        Element resourcesEle = child(contentEle, "project-resources"); //$NON-NLS-1$
        if (resourcesEle == null)
            return null;

        Iterator<Element> it = children(resourcesEle, "project-resource"); //$NON-NLS-1$

        while (it.hasNext()) {
            Element next = it.next();
            if (ref.equals(att(next, "id"))) { //$NON-NLS-1$
                String name = att(next, "name"); //$NON-NLS-1$
                assignRefMap.put(ref, name);
                return name;
            }
        }

        return null;
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

    private static int mm2Dots(float mm) {
        return (int) (mm * DPM);
    }

    private static String getMapping(String type, String sourceId,
            String defaultId) {
        if (sourceId != null) {
            String destination = getMappings().getDestination(type, sourceId);
            if (destination != null)
                return destination;
        }
        return defaultId;
    }

    private static ResourceMappingManager getMappings() {
        if (mappings == null)
            mappings = createMappings();

        return mappings;
    }

    private static ResourceMappingManager createMappings() {
        return NovaMindResourceMappingManager.getInstance();
    }

    private static DocumentBuilder getDocumentBuilder()
            throws ParserConfigurationException {
        return DOMUtils.getDefaultDocumentBuilder();
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
