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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.internal.DPIUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmind.core.CoreException;
import org.xmind.core.IFileEntry;
import org.xmind.core.IImage;
import org.xmind.core.INotes;
import org.xmind.core.INotesContent;
import org.xmind.core.ISheet;
import org.xmind.core.ISummary;
import org.xmind.core.ITopic;
import org.xmind.core.internal.dom.DOMConstants;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerRef;
import org.xmind.core.marker.IMarkerResource;
import org.xmind.core.marker.IMarkerVariation;
import org.xmind.core.style.IStyle;
import org.xmind.core.util.DOMUtils;
import org.xmind.core.util.FileUtils;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.core.util.Property;
import org.xmind.ui.mindmap.MindMapImageExporter;
import org.xmind.ui.util.ImageFormat;
import org.xmind.ui.util.MindMapUtils;
import org.xmind.ui.wizards.ExportContants;
import org.xmind.ui.wizards.ExportUtils;
import org.xmind.ui.wizards.Exporter;
import org.xmind.ui.wizards.IExportPart;
import org.xmind.ui.wizards.RelationshipDescription;

public class HtmlExporter extends Exporter {

    private static class TitlePart extends HtmlExportPart {

        private final int level;

        private static final String SPACE = "\u00A0"; //$NON-NLS-1$

        public TitlePart(HtmlExporter exporter, ITopic topic, int level) {
            super(exporter, topic);
            this.level = level;
        }

        protected Node createNode() {
            ITopic topic = (ITopic) getElement();
            String tag = HtmlConstants.TAGS_H[Math
                    .min(HtmlConstants.TAGS_H.length - 1, level)];
            Element ele = createDOMElement(tag);

            boolean isRoot = isCentralTopic(topic);
            ele.setAttribute(HtmlConstants.ATT_CLASS,
                    isRoot ? "root" : "topic"); //$NON-NLS-1$ //$NON-NLS-2$
            if (isRoot) {
                ele.setAttribute(HtmlConstants.ATT_ALIGN, HtmlConstants.CENTER);
            }

            writeStyle(ele, topic);

            String num = ExportUtils.getNumberingText(topic,
                    getExporter().getCentralTopic());
            if (num != null) {
                ele.appendChild(createText(num));
                ele.appendChild(createText(" ")); //$NON-NLS-1$
            }

            String title = getLeftIndent() + topic.getTitleText();
            Element titleAnchor = createDOMElement(HtmlConstants.TAG_A);
            titleAnchor.setAttribute(HtmlConstants.ATT_NAME, topic.getId());

            String hyperlink = topic.getHyperlink();
            if (HyperlinkUtils.isAttachmentURL(hyperlink)
                    && !isLinkToWeb(hyperlink)) {
                if (getExporter()
                        .getBoolean(ExportContants.INCLUDE_ATTACHMENT)) {
                    hyperlink = getExporter().createFilePath(hyperlink, title);
                } else {
                    hyperlink = null;
                }
            } else {
                if (!getExporter()
                        .getBoolean(ExportContants.INCLUDE_HYPERLINK)) {
                    hyperlink = null;
                } else if (hyperlink != null
                        && hyperlink.startsWith("xmind:")) { //$NON-NLS-1$
                    hyperlink = hyperlink.substring(6);
                } else if (isLinkToWeb(hyperlink)
                        && !hyperlink.startsWith("http:") //$NON-NLS-1$
                        && !hyperlink.startsWith("https:")) { //$NON-NLS-1$
                    hyperlink = "http://" + hyperlink; //$NON-NLS-1$
                }
            }
            if (hyperlink != null) {
                titleAnchor.setAttribute(HtmlConstants.ATT_HREF, hyperlink);
            }

            titleAnchor.appendChild(createText(title));
            ele.appendChild(titleAnchor);

            return ele;
        }

        private String getLeftIndent() {
            StringBuffer sb = new StringBuffer();
            for (int i = 1; i < level; i++)
                sb.append(SPACE);
            return sb.toString();
        }

        @SuppressWarnings("nls")
        private boolean isLinkToWeb(String hyperlink) {
            if (hyperlink == null)
                return false;

            if (hyperlink.contains("www.") || hyperlink.contains(".com")
                    || hyperlink.contains(".cn") || hyperlink.contains(".org")
                    || hyperlink.contains(".cc")
                    || hyperlink.contains(".net")) {
                return true;
            }
            return false;
        }

    }

    private static class OverviewPart extends HtmlExportPart {

        private static final ImageFormat FORMAT = ImageFormat.JPEG;

        public OverviewPart(HtmlExporter exporter, ITopic topic) {
            super(exporter, topic);
        }

        protected Node createNode() {
            ITopic topic = (ITopic) getElement();
            boolean root = isCentralTopic(topic);
            String path = getExporter().createOverview(topic, FORMAT);
            Element div = createDOMElement(HtmlConstants.TAG_DIV);
            div.setAttribute(HtmlConstants.ATT_CLASS,
                    root ? "globalOverview" : "overview"); //$NON-NLS-1$ //$NON-NLS-2$
            if (root) {
                div.setAttribute(HtmlConstants.ATT_ALIGN, HtmlConstants.CENTER);
            }

            Element img = createDOMElement(HtmlConstants.TAG_IMG);
            img.setAttribute(HtmlConstants.ATT_SRC, path);
            div.appendChild(img);

            return div;
        }

    }

    private static class TagsPart extends HtmlExportPart {

        private Set<IMarkerRef> markers;

        private Set<String> labels;

        public TagsPart(HtmlExporter exporter, ITopic topic,
                Set<IMarkerRef> markers, Set<String> labels) {
            super(exporter, topic);
            this.markers = markers;
            this.labels = labels;
        }

        protected Node createNode() {
            Element ele = createDOMElement(HtmlConstants.TAG_P);
            ele.setAttribute(HtmlConstants.ATT_CLASS, "labelsAndMarkers"); //$NON-NLS-1$
            ITopic topic = (ITopic) getElement();
            boolean isRoot = isCentralTopic(topic);
            if (isRoot) {
                ele.setAttribute(HtmlConstants.ATT_ALIGN, HtmlConstants.CENTER);
            }

            boolean hasMarkers = false;
            boolean hasLabels = labels != null;

            if (markers != null) {
                Iterator<IMarkerRef> markerIt = markers.iterator();
                while (markerIt.hasNext()) {
                    IMarkerRef m = markerIt.next();
                    String markerId = m.getMarkerId();
                    String path = getExporter().createMarkerPath(markerId);
                    if (path != null) {
                        hasMarkers = true;
                        Element img = createDOMElement(HtmlConstants.TAG_IMG);
                        img.setAttribute(HtmlConstants.ATT_CLASS, "marker"); //$NON-NLS-1$
                        img.setAttribute(HtmlConstants.ATT_SRC, path);
                        ele.appendChild(img);

                        if (markerIt.hasNext()) {
                            ele.appendChild(createText(" ")); //$NON-NLS-1$
                        }
                    }
                }
            }

            if (hasMarkers && hasLabels && !isRoot) {
                ele.appendChild(createText(COMMA));
            }

            if (hasLabels) {
                Iterator<String> labelIt = labels.iterator();
                while (labelIt.hasNext()) {
                    String label = labelIt.next();
                    Element span = createDOMElement(HtmlConstants.TAG_SPAN);
                    span.setAttribute(HtmlConstants.ATT_CLASS, "label"); //$NON-NLS-1$
                    span.appendChild(createText(label));
                    ele.appendChild(span);

                    if (labelIt.hasNext()) {
                        ele.appendChild(createText(COMMA));
                    }
                }
            }
            return ele;
        }

    }

    private static class ImagePart extends HtmlExportPart {

        public ImagePart(HtmlExporter exporter, IImage element) {
            super(exporter, element);
        }

        protected Node createNode() {
            IImage image = (IImage) getElement();
            Element ele = createDOMElement(HtmlConstants.TAG_P);
            ele.setAttribute(HtmlConstants.ATT_CLASS, "topicImage"); //$NON-NLS-1$
            if (isCentralTopic(image.getParent())) {
                ele.setAttribute(HtmlConstants.ATT_ALIGN, HtmlConstants.CENTER);
            }

            String url = image.getSource();
            if (HyperlinkUtils.isAttachmentURL(url)) {
                url = getExporter().createFilePath(url, null);
            }
            if (url != null) {
                Element img = createDOMElement(HtmlConstants.TAG_IMG);
                img.setAttribute(HtmlConstants.ATT_SRC, url);

                int width = image.getWidth();
                if (width != IImage.UNSPECIFIED) {
                    img.setAttribute(HtmlConstants.ATT_WIDTH,
                            String.valueOf(width));
                }

                int height = image.getHeight();
                if (height != IImage.UNSPECIFIED) {
                    img.setAttribute(HtmlConstants.ATT_HEIGHT,
                            String.valueOf(height));
                }
                ele.appendChild(img);
            }
            return ele;
        }

    }

    private static class NotesPart extends HtmlExportPart {

        public NotesPart(HtmlExporter exporter, INotesContent element) {
            super(exporter, element);
        }

        protected Node createNode() {
            Element ele = createDOMElement(HtmlConstants.TAG_DIV);
            ele.setAttribute(HtmlConstants.ATT_CLASS, "notesContainer"); //$NON-NLS-1$

            INotesContent content = (INotesContent) getElement();
            Element contentEle = (Element) content.getAdapter(Element.class);
            if (contentEle != null) {
                String format = content.getFormat();
                if (INotes.PLAIN.equals(format)) {
                    appendPlainNotes(ele, content, contentEle);
                } else {
                    appendHtmlNotes(ele, content, contentEle);
                }
            }
            return ele;
        }

        private void appendPlainNotes(Element ele, INotesContent content,
                Element contentEle) {
            String textContent = contentEle.getTextContent();
            String[] lines = textContent.split("\\r\\n|\\r|\\n"); //$NON-NLS-1$
            for (String line : lines) {
                Element p = createDOMElement(HtmlConstants.TAG_P);
                p.setTextContent(line);
                ele.appendChild(p);
            }
        }

        private void appendHtmlNotes(Element ele, INotesContent content,
                Element contentEle) {
            Document doc = ele.getOwnerDocument();
            NodeList children = contentEle.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node n = children.item(i);
                n = importNode(doc, n);
                ele.appendChild(n);
            }
        }

        private Node importNode(Document doc, Node n) {
            if (n instanceof Element) {
                return importElement(doc, (Element) n);
            }
            return doc.importNode(n, true);
        }

        private Element importElement(Document doc, Element e) {
            String tag = DOMUtils.getLocalName(e.getTagName());
            Element e2 = doc.createElement(tag);
            NamedNodeMap attrs = e.getAttributes();
            for (int i = 0; i < attrs.getLength(); i++) {
                Node item = attrs.item(i);
                String name = item.getNodeName();
                String value = item.getNodeValue();
                if (DOMConstants.ATTR_STYLE_ID.equals(name)) {
                    name = HtmlConstants.ATT_CLASS;
                    value = getExporter().addStyle(value);
                } else if (DOMConstants.ATTR_SRC.equals(name)) {
                    if (HyperlinkUtils.isAttachmentURL(value)) {
                        value = getExporter().createFilePath(value, null);
                    }
                }
                e2.setAttribute(DOMUtils.getLocalName(name), value);
            }

            NodeList children = e.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node c = children.item(i);
                Node c2 = importNode(doc, c);
                e2.appendChild(c2);
            }
            return e2;
        }

    }

    private static class RelationshipsPart extends HtmlExportPart {

        private List<RelationshipDescription> relationships;

        public RelationshipsPart(HtmlExporter exporter, ITopic element,
                List<RelationshipDescription> relationships) {
            super(exporter, element);
            this.relationships = relationships;
        }

        protected Node createNode() {
            Element ele = createDOMElement(HtmlConstants.TAG_P);
            ele.setAttribute(HtmlConstants.ATT_CLASS, "relationships"); //$NON-NLS-1$
            ele.appendChild(createText(SEE_ALSO));
            ele.appendChild(createText(" ")); //$NON-NLS-1$

            Iterator<RelationshipDescription> relIt = relationships.iterator();
            while (relIt.hasNext()) {
                RelationshipDescription rel = relIt.next();
                Element anchor = createDOMElement(HtmlConstants.TAG_A);
                anchor.setAttribute(HtmlConstants.ATT_HREF,
                        "#" + rel.target.getId()); //$NON-NLS-1$
                anchor.setTextContent(rel.description);
                ele.appendChild(anchor);
                if (relIt.hasNext()) {
                    ele.appendChild(createText(COMMA));
                }
            }

            return ele;
        }

    }

    private static class SummaryPart extends HtmlExportPart {

        public SummaryPart(HtmlExporter exporter, ISummary summary) {
            super(exporter, summary);
        }

        protected Node createNode() {
            Element ele = createDOMElement(HtmlConstants.TAG_P);
            ele.setAttribute(HtmlConstants.ATT_CLASS, "summary"); //$NON-NLS-1$
            ele.appendChild(createText("(")); //$NON-NLS-1$

            ISummary summary = (ISummary) getElement();
            List<ITopic> topics = summary.getEnclosingTopics();
            Iterator<ITopic> topicIt = topics.iterator();
            while (topicIt.hasNext()) {
                ITopic topic = topicIt.next();
                Element anchor = createDOMElement(HtmlConstants.TAG_A);
                anchor.setAttribute(HtmlConstants.ATT_HREF,
                        "#" + topic.getId()); //$NON-NLS-1$
                anchor.setTextContent(topic.getTitleText());
                ele.appendChild(anchor);
                if (topicIt.hasNext()) {
                    ele.appendChild(createText(COMMA));
                }
            }

            ele.appendChild(createText(")")); //$NON-NLS-1$
            return ele;
        }

    }

//    private static class BoundariesPart extends HtmlExportPart {
//
//        public BoundariesPart(HtmlExporter exporter, ITopic topic) {
//            super(exporter, topic);
//        }
//
//        protected Node createNode() {
//            Element ele = createDOMElement(HtmlConstants.TAG_P);
//            ele.setAttribute(HtmlConstants.ATT_CLASS, "boundaries"); //$NON-NLS-1$
//            ele.appendChild(createText(WizardMessages.Export_Groups));
//            return ele;
//        }
//
//        public Node getContentNode(HtmlExportPart child) {
//            if (child instanceof BoundaryPart) {
//                return DOMUtils.ensureChildElement(getNode(),
//                        HtmlConstants.TAG_UL);
//            }
//            return super.getContentNode(child);
//        }
//
//    }
//
//    private static class BoundaryPart extends HtmlExportPart {
//
//        private List<ITopic> topics;
//
//        public BoundaryPart(HtmlExporter exporter, IBoundary boundary,
//                List<ITopic> topics) {
//            super(exporter, boundary);
//            this.topics = topics;
//        }
//
//        protected Node createNode() {
//            Element ele = createDOMElement(HtmlConstants.TAG_LI);
//            ele.setAttribute(HtmlConstants.ATT_CLASS, "boundary"); //$NON-NLS-1$
//
//            IBoundary boundary = (IBoundary) getElement();
//
//            Element boundaryAnchor = createDOMElement(HtmlConstants.TAG_A);
//            boundaryAnchor.setAttribute(HtmlConstants.ATT_NAME, boundary
//                    .getId());
//            boundaryAnchor.appendChild(createText(boundary.getTitleText()));
//            ele.appendChild(boundaryAnchor);
//
//            ele.appendChild(createText(": ")); //$NON-NLS-1$
//
//            Iterator<ITopic> topicIt = topics.iterator();
//            while (topicIt.hasNext()) {
//                ITopic topic = topicIt.next();
//                Element topicAnchor = createDOMElement(HtmlConstants.TAG_A);
//                topicAnchor.setAttribute(HtmlConstants.ATT_HREF,
//                        "#" + topic.getId()); //$NON-NLS-1$
//                topicAnchor.appendChild(createText(topic.getTitleText()));
//                ele.appendChild(topicAnchor);
//                if (topicIt.hasNext()) {
//                    ele.appendChild(createText(COMMA));
//                }
//            }
//            return ele;
//        }
//
//    }

    private static final Object NULL = new Object();

    private static final String IMAGES = "images"; //$NON-NLS-1$

    private static final String FILES = "_files"; //$NON-NLS-1$

    private String title;

    private String targetPath;

    private String filesPath;

    private String imagesPath;

    private String relativeFilesPath;

    private String relativeImagesPath;

    private Document document;

    private Element headEle;

    private Element bodyEle;

    private Map<String, List<String>> usedStyles = null;

    private Map<String, IStyle> styleMap = null;

    private Map<String, String> styleIdMap = null;

    private Map<String, Object> files = null;

    private Map<String, Object> markerPaths = null;

    public HtmlExporter(ISheet sheet, ITopic centralTopic, String targetPath,
            String title) {
        super(sheet, centralTopic);
        this.targetPath = targetPath;
        this.title = title;
    }

    public void init() {
        appendTopic(getCentralTopic(), 0, null);
    }

    private void appendTopic(ITopic topic, int level, HtmlExportPart parent) {
        TitlePart topicPart = new TitlePart(this, topic, level);
        topicPart.setParent(parent);
        append(topicPart);

        appendTopicContent(topic, level, parent);
    }

    private void appendTopicContent(ITopic topic, int level,
            HtmlExportPart parent) {
        Set<IMarkerRef> markers = topic.getMarkerRefs();
        Set<String> labels = topic.getLabels();
        boolean hasMarker = getBoolean(ExportContants.INCLUDE_MARKERS)
                && !markers.isEmpty();
        boolean hasLabel = getBoolean(ExportContants.INCLUDE_LABELS)
                && !labels.isEmpty();
        if (hasMarker || hasLabel) {
            TagsPart tags = new TagsPart(this, topic,
                    hasMarker ? markers : null, hasLabel ? labels : null);
            tags.setParent(parent);
            append(tags);
        }

        if (hasOverview(topic)) {
            OverviewPart overview = new OverviewPart(this, topic);
            overview.setParent(parent);
            append(overview);
        }

        if (getBoolean(ExportContants.INCLUDE_IMAGE)) {
            IImage image = topic.getImage();
            if (image.getSource() != null) {
                ImagePart imagePart = new ImagePart(this, image);
                imagePart.setParent(parent);
                append(imagePart);
            }
        }

        if (getBoolean(ExportContants.INCLUDE_NOTES)) {
            INotesContent content = topic.getNotes().getContent(INotes.HTML);
            if (content == null)
                content = topic.getNotes().getContent(INotes.PLAIN);
            if (content != null) {
                NotesPart notesPart = new NotesPart(this, content);
                notesPart.setParent(parent);
                append(notesPart);
            }
        }

        if (getBoolean(ExportContants.INCLUDE_RELATIONSHIPS)) {
            List<RelationshipDescription> relationships = ExportUtils
                    .getRelationships(topic, getRelationships());
            if (!relationships.isEmpty()) {
                RelationshipsPart relsPart = new RelationshipsPart(this, topic,
                        relationships);
                relsPart.setParent(parent);
                append(relsPart);
            }
        }

//        List<IBoundary> bs = null;
//        List<List<ITopic>> ts = null;
//        Collection<IBoundary> boundaries = topic.getBoundaries();
//        if (!boundaries.isEmpty()) {
//            for (IBoundary boundary : boundaries) {
//                if (!boundary.isOverall()) {
//                    List<ITopic> topics = boundary.getEnclosingTopics();
//                    if (!topics.isEmpty()) {
//                        if (bs == null)
//                            bs = new ArrayList<IBoundary>();
//                        bs.add(boundary);
//                        if (ts == null)
//                            ts = new ArrayList<List<ITopic>>();
//                        ts.add(topics);
//                    }
//                }
//            }
//        }
//
//        for (ITopic child : topic.getAllChildren()) {
//            for (IBoundary b : child.getBoundaries()) {
//                if (b.isOverall()) {
//                    if (bs == null)
//                        bs = new ArrayList<IBoundary>();
//                    bs.add(b);
//                    if (ts == null)
//                        ts = new ArrayList<List<ITopic>>();
//                    ts.add(Arrays.asList(child));
//                    break;
//                }
//            }
//        }
//
//        if (bs != null && ts != null) {
//            BoundariesPart boundariesPart = new BoundariesPart(this, topic);
//            boundariesPart.setParent(parent);
//            append(boundariesPart);
//
//            Iterator<IBoundary> bsIt = bs.iterator();
//            Iterator<List<ITopic>> tsIt = ts.iterator();
//            while (bsIt.hasNext() && tsIt.hasNext()) {
//                IBoundary boundary = bsIt.next();
//                List<ITopic> topics = tsIt.next();
//                BoundaryPart boundaryPart = new BoundaryPart(this, boundary,
//                        topics);
//                boundaryPart.setParent(boundariesPart);
//                append(boundaryPart);
//            }
//        }

        int nextLevel = level + 1;
        for (ITopic sub : topic.getChildren(ITopic.ATTACHED)) {
            appendTopic(sub, nextLevel, parent);
        }

        if (getBoolean(ExportContants.INCLUDE_SUMMARIES)) {
            for (ISummary summary : topic.getSummaries()) {
                appendSummary(summary, topic, nextLevel, parent);
            }
        }

        if (getBoolean(ExportContants.INCLUDE_FLOATING_TOPICS)) {
            for (ITopic sub : topic.getChildren(ITopic.DETACHED)) {
                appendTopic(sub, nextLevel, parent);
            }
        }
    }

    private void appendSummary(ISummary summary, ITopic topic, int nextLevel,
            HtmlExportPart parent) {
        ITopic summaryTopic = summary.getTopic();
        if (summaryTopic == null)
            return;

        TitlePart topicPart = new TitlePart(this, summaryTopic, nextLevel);
        topicPart.setParent(parent);
        append(topicPart);

        SummaryPart summaryPart = new SummaryPart(this, summary);
        summaryPart.setParent(parent);
        append(summaryPart);

        appendTopicContent(summaryTopic, nextLevel, parent);
    }

    public String getTargetPath() {
        return targetPath;
    }

    public String getFilesPath() {
        if (filesPath == null) {
            filesPath = createFilesPath();
        }
        return filesPath;
    }

    private String createFilesPath() {
        File f = new File(getTargetPath());
        String name = f.getName();
        String parent = f.getParent();
        int index = name.lastIndexOf('.');
        if (index >= 0) {
            name = name.substring(0, index) + FILES;
        } else {
            name += FILES;
        }
        return new File(parent, name).getAbsolutePath();
    }

    public String getImagesPath() {
        if (imagesPath == null) {
            imagesPath = createImagesPath();
        }
        return imagesPath;
    }

    private String createImagesPath() {
        String path = getFilesPath();
        return new File(path, IMAGES).getAbsolutePath();
    }

    public String getRelativeFilesPath() {
        if (relativeFilesPath == null) {
            relativeFilesPath = new File(getFilesPath()).getName();
        }
        return relativeFilesPath;
    }

    public String getRelativeImagesPath() {
        if (relativeImagesPath == null) {
            relativeImagesPath = connectPath(getRelativeFilesPath(), IMAGES);
        }
        return relativeImagesPath;
    }

    public String connectPath(String parent, String child) {
        return parent + "/" + child; //$NON-NLS-1$
    }

    public String newPath(String parent, String name, String ext) {
        File f = new File(parent, name + ext);
        String newName;
        int i = 1;
        while (f.exists()) {
            i++;
            newName = name + " " + i; //$NON-NLS-1$
            f = new File(parent, newName + ext);
        }
        return f.getAbsolutePath();
    }

    public Document getDocument() {
        if (document == null)
            document = createDocument();
        return document;
    }

    protected Element getHeadElement() {
        if (headEle == null) {
            headEle = DOMUtils.ensureChildElement(
                    getDocument().getDocumentElement(), HtmlConstants.TAG_HEAD);
        }
        return headEle;
    }

    private Document createDocument() {
        Document doc = DOMUtils.createDocument();
        Element ele = DOMUtils.createElement(doc, HtmlConstants.TAG_HTML);
        Element head = DOMUtils.createElement(ele, HtmlConstants.TAG_HEAD);
        createMeta(head, "Content-Type", "text/html; charset=utf-8"); //$NON-NLS-1$ //$NON-NLS-2$
        createMeta(head, "Content-Style-Type", "text/css"); //$NON-NLS-1$ //$NON-NLS-2$

        if (title != null) {
            Element titleEle = DOMUtils.createElement(head,
                    HtmlConstants.TAG_TITLE);
            titleEle.setTextContent(title);
        }
        return doc;
    }

    private Element createMeta(Element head, String httpEquiv, String content) {
        Element meta = DOMUtils.createElement(head, HtmlConstants.TAG_META);
        meta.setAttribute("http-equiv", httpEquiv); //$NON-NLS-1$
        meta.setAttribute("content", content); //$NON-NLS-1$
        return meta;
    }

    protected Node getBodyNode() {
        if (bodyEle == null) {
            bodyEle = createBodyNode();
        }
        return bodyEle;
    }

    private Element createBodyNode() {
        return DOMUtils.createElement(getDocument().getDocumentElement(),
                HtmlConstants.TAG_BODY);
    }

    public String addStyle(String styleId) {
        if (styleId == null)
            return null;

        String cachedStyleId = styleIdMap == null ? null
                : styleIdMap.get(styleId);
        if (cachedStyleId != null)
            return cachedStyleId;

        IStyle style = getStyle(styleId);
        if (style == null)
            return null;

        String type = style.getType();
        if (!isStyleInteresting(style, type))
            return null;

        if (usedStyles == null)
            usedStyles = new HashMap<String, List<String>>();
        List<String> list = usedStyles.get(type);
        if (list == null) {
            list = new ArrayList<String>();
            usedStyles.put(type, list);
        }
        String newStyleId = newStyleId(type, list.size() + 1);
        list.add(newStyleId);

        if (styleIdMap == null)
            styleIdMap = new HashMap<String, String>();
        styleIdMap.put(styleId, newStyleId);

        if (styleMap == null)
            styleMap = new HashMap<String, IStyle>();
        styleMap.put(newStyleId, style);
        return newStyleId;
    }

    private String newStyleId(String type, int index) {
        if (IStyle.PARAGRAPH.equals(type))
            return "p" + index; //$NON-NLS-1$
        else
            /* if (IStyle.TEXT.equals(type)) */
            return "s" + index; //$NON-NLS-1$
    }

    private boolean isStyleInteresting(IStyle style, String type) {
        return IStyle.PARAGRAPH.equals(type) || IStyle.TEXT.equals(type);
    }

    protected void write(IProgressMonitor monitor, IExportPart part)
            throws InvocationTargetException, InterruptedException {
        HtmlExportPart child = (HtmlExportPart) part;
        HtmlExportPart parent = child.getParent();
        if (parent != null) {
            child.addToParent(parent);
        } else {
            getBodyNode().appendChild(child.getNode());
        }
    }

    public void end() throws InvocationTargetException {
        createStyles();
        try {
            FileOutputStream out = new FileOutputStream(targetPath);
            try {
                DOMUtils.save(getDocument(), out, true);
            } catch (IOException e) {
                throw new InvocationTargetException(e);
            } catch (CoreException e) {
                throw new InvocationTargetException(e);
            }
        } catch (FileNotFoundException e) {
            throw new InvocationTargetException(e);
        } finally {
            super.end();
        }
    }

    private void createStyles() {
        if (usedStyles == null || usedStyles.isEmpty())
            return;

        StringBuilder sb = new StringBuilder();
        sb.append(HtmlConstants.LINE_SEP);

        List<String> ps = usedStyles.get(IStyle.PARAGRAPH);
        if (ps != null && !ps.isEmpty()) {
            createStyles(HtmlConstants.TAG_P, ps, sb);
        }

        List<String> ts = usedStyles.get(IStyle.TEXT);
        if (ts != null && !ts.isEmpty()) {
            createStyles(HtmlConstants.TAG_SPAN, ts, sb);
        }

        String content = sb.toString();

        Element styleEle = DOMUtils.ensureChildElement(getHeadElement(),
                HtmlConstants.TAG_STYLE);
        styleEle.setAttribute(HtmlConstants.ATT_TYPE, "text/css"); //$NON-NLS-1$
        styleEle.setTextContent(content);
    }

    private void createStyles(String tag, List<String> styleIds,
            StringBuilder sb) {
        for (String styleId : styleIds) {
            IStyle style = styleMap == null ? null : styleMap.get(styleId);
            if (style != null) {
                createStyle(tag, styleId, style, sb);
            }
        }
    }

    private void createStyle(String tag, String styleId, IStyle style,
            StringBuilder sb) {
        sb.append(tag);
        sb.append('.');
        sb.append(styleId);
        sb.append(' ');
        sb.append('{');

        Iterator<Property> properties = style.properties();
        while (properties.hasNext()) {
            Property property = properties.next();
            String name = DOMUtils.getLocalName(property.key);
            String value = property.value;
            sb.append(name);
            sb.append(':');
            sb.append(' ');
            sb.append(value);
            if (properties.hasNext()) {
                sb.append(';');
                sb.append(' ');
            }
        }

        sb.append('}');
        sb.append(HtmlConstants.LINE_SEP);
    }

    public String createOverview(ITopic topic, ImageFormat format) {
        String title = topic.getTitleText();
        String path = newPath(getImagesPath(), MindMapUtils.trimFileName(title),
                format.getExtensions().get(0));
        FileUtils.ensureFileParent(new File(path));
        String relativePath = connectPath(getRelativeImagesPath(),
                new File(path).getName());

        MindMapImageExporter exporter = createOverviewExporter(topic);
        exporter.setTargetFile(new File(path));
        exporter.export();
//        Display display = getDisplay();
//        Shell shell = getShell();
//        MindMapPreviewBuilder overviewBuilder = createOverviewBuilder(topic);
//        if (overviewBuilder != null) {
//            try {
//                if (shell != null) {
//                    overviewBuilder.build(shell, path);
//                } else {
//                    overviewBuilder.build(display, path);
//                }
//            } catch (IOException e) {
//                String message = NLS
//                        .bind(Message_FailedToCreateOverview, title);
//                log(e, message);
//            }
//        }
        return relativePath;
    }

    public String createFilePath(String hyperlink, String suggestedName) {
        Object cache = files == null ? null : files.get(hyperlink);
        if (cache == NULL)
            return null;
        if (cache instanceof String)
            return (String) cache;

        String entryPath = HyperlinkUtils.toAttachmentPath(hyperlink);
        IFileEntry entry = getFileEntry(entryPath);
        if (entry == null) {
            return cacheFile(hyperlink, NULL);
        }
        if (suggestedName == null) {
            suggestedName = FileUtils.getFileName(entryPath);
        }
        String name = FileUtils.getNoExtensionFileName(suggestedName);
        String ext = FileUtils.getExtension(suggestedName);
        String path = newPath(getFilesPath(), name, ext);

        InputStream in = entry.getInputStream();
        if (in != null) {
            FileUtils.ensureFileParent(new File(path));
            try {
                FileUtils.transfer(in, new FileOutputStream(path), true);
            } catch (IOException e) {
                String message = NLS.bind(Message_FailedToCopyAttachment,
                        suggestedName);
                log(e, message);
            }
        }

        String fileName = new File(path).getName();
        String relativePath = connectPath(getRelativeFilesPath(), fileName);
        return cacheFile(hyperlink, relativePath);
    }

    private String cacheFile(String hyperlink, Object path) {
        if (files == null)
            files = new HashMap<String, Object>();
        files.put(hyperlink, path);
        return path == NULL ? null : (String) path;
    }

    public String createMarkerPath(String markerId) {
        if (markerId == null)
            return null;
        Object cache = markerPaths == null ? null : markerPaths.get(markerId);
        if (cache == NULL)
            return null;
        if (cache instanceof String)
            return (String) cache;

        IMarker marker = getMarker(markerId);
        if (marker == null)
            return cacheMarker(markerId, NULL);

        IMarkerResource resource = marker.getResource();
        if (resource == null)
            return cacheMarker(markerId, NULL);

        String name = FileUtils.getFileName(resource.getPath());
        String ext = FileUtils.getExtension(name);
        name = FileUtils.getNoExtensionFileName(name);
        String path = newPath(getImagesPath(), name, ext);
        FileUtils.ensureFileParent(new File(path));

        int zoom = DPIUtil.getDeviceZoom();

        List<IMarkerVariation> variations = resource.getVariations();
        InputStream in = null;
        try {
            if (variations.size() > 0) {
                in = resource.openInputStream(
                        variations.get(variations.size() - 1), zoom);
            } else {
                in = resource.openInputStream(zoom);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (in != null) {
            try {
                FileUtils.transfer(in, new FileOutputStream(path), true);
            } catch (IOException e) {
                log(e, NLS.bind(Message_FailedToCopyMarker, markerId));
            }
        }

        String fileName = new File(path).getName();
        String relativePath = connectPath(getRelativeImagesPath(), fileName);
        return cacheMarker(markerId, relativePath);
    }

    private String cacheMarker(String markerId, Object path) {
        if (markerPaths == null)
            markerPaths = new HashMap<String, Object>();
        markerPaths.put(markerId, path);
        return path == NULL ? null : (String) path;
    }
}
