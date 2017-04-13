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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.commonmark.node.Document;
import org.commonmark.node.Heading;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.renderer.markdown.MarkDownContentRender;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.internal.DPIUtil;
import org.xmind.core.IFileEntry;
import org.xmind.core.IImage;
import org.xmind.core.INotes;
import org.xmind.core.INotesContent;
import org.xmind.core.ISheet;
import org.xmind.core.ISummary;
import org.xmind.core.ITopic;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerRef;
import org.xmind.core.marker.IMarkerResource;
import org.xmind.core.marker.IMarkerVariation;
import org.xmind.core.style.IStyle;
import org.xmind.core.util.FileUtils;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.ui.mindmap.MindMapImageExporter;
import org.xmind.ui.util.ImageFormat;
import org.xmind.ui.util.MindMapUtils;
import org.xmind.ui.wizards.ExportContants;
import org.xmind.ui.wizards.ExportUtils;
import org.xmind.ui.wizards.Exporter;
import org.xmind.ui.wizards.IExportPart;
import org.xmind.ui.wizards.RelationshipDescription;

public class MarkDownExporter extends Exporter {

    private static class TitlePart extends MarkDownExportPart {

        private final int level;

        private static final String SPACE = "\u00A0"; //$NON-NLS-1$

        public TitlePart(MarkDownExporter exporter, ITopic topic, int level) {
            super(exporter, topic);
            this.level = level;
        }

        protected Node createNode() {
            ITopic topic = (ITopic) getElement();

            Heading head = new Heading();
            head.setLevel(level + 1);
            Text text = new Text();
            head.appendChild(text);

            text.setLiteral(topic.getTitleText());

            return head;
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

    private static class OverviewPart extends MarkDownExportPart {

        private static final ImageFormat FORMAT = ImageFormat.JPEG;

        public OverviewPart(MarkDownExporter exporter, ITopic topic) {
            super(exporter, topic);
        }

        protected Node createNode() {

            ITopic topic = (ITopic) getElement();

            System.out.println("test=");
            //Heading head = new Heading();
            // head.setLevel(level);
            Text text = new Text();
            //head.appendChild(text);

            text.setLiteral(topic.getTitleText());

            return text;
        }

    }

    private static class TagsPart extends MarkDownExportPart {

        private Set<IMarkerRef> markers;

        private Set<String> labels;

        public TagsPart(MarkDownExporter exporter, ITopic topic,
                Set<IMarkerRef> markers, Set<String> labels) {
            super(exporter, topic);
            this.markers = markers;
            this.labels = labels;
        }

        protected Node createNode() {

            return null;
        }

    }

    private static class ImagePart extends MarkDownExportPart {

        public ImagePart(MarkDownExporter exporter, IImage element) {
            super(exporter, element);
        }

        protected Node createNode() {

            return null;
        }

    }

    private static class NotesPart extends MarkDownExportPart {

        public NotesPart(MarkDownExporter exporter, INotesContent element) {
            super(exporter, element);
        }

        protected Node createNode() {

            return null;
        }

    }

    private static class RelationshipsPart extends MarkDownExportPart {

        private List<RelationshipDescription> relationships;

        public RelationshipsPart(MarkDownExporter exporter, ITopic element,
                List<RelationshipDescription> relationships) {
            super(exporter, element);
            this.relationships = relationships;
        }

        protected Node createNode() {

            return null;
        }

    }

    private static class SummaryPart extends MarkDownExportPart {

        public SummaryPart(MarkDownExporter exporter, ISummary summary) {
            super(exporter, summary);
        }

        protected Node createNode() {

            return null;
        }

    }

//    private static class BoundariesPart extends MarkDownExportPart {
//
//        public BoundariesPart(HtmlExporter exporter, ITopic topic) {
//            super(exporter, topic);
//        }
//
//        protected Node createNode() {
//            Element ele = createDOMElement(MarkDownConstants.TAG_P);
//            ele.setAttribute(MarkDownConstants.ATT_CLASS, "boundaries"); //$NON-NLS-1$
//            ele.appendChild(createText(WizardMessages.Export_Groups));
//            return ele;
//        }
//
//        public Node getContentNode(MarkDownExportPart child) {
//            if (child instanceof BoundaryPart) {
//                return DOMUtils.ensureChildElement(getNode(),
//                        MarkDownConstants.TAG_UL);
//            }
//            return super.getContentNode(child);
//        }
//
//    }
//
//    private static class BoundaryPart extends MarkDownExportPart {
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
//            Element ele = createDOMElement(MarkDownConstants.TAG_LI);
//            ele.setAttribute(MarkDownConstants.ATT_CLASS, "boundary"); //$NON-NLS-1$
//
//            IBoundary boundary = (IBoundary) getElement();
//
//            Element boundaryAnchor = createDOMElement(MarkDownConstants.TAG_A);
//            boundaryAnchor.setAttribute(MarkDownConstants.ATT_NAME, boundary
//                    .getId());
//            boundaryAnchor.appendChild(createText(boundary.getTitleText()));
//            ele.appendChild(boundaryAnchor);
//
//            ele.appendChild(createText(": ")); //$NON-NLS-1$
//
//            Iterator<ITopic> topicIt = topics.iterator();
//            while (topicIt.hasNext()) {
//                ITopic topic = topicIt.next();
//                Element topicAnchor = createDOMElement(MarkDownConstants.TAG_A);
//                topicAnchor.setAttribute(MarkDownConstants.ATT_HREF,
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

    private Node node;

    private Map<String, Object> files = null;

    private Map<String, Object> markerPaths = null;

    public MarkDownExporter(ISheet sheet, ITopic centralTopic,
            String targetPath, String title) {
        super(sheet, centralTopic);
        this.targetPath = targetPath;
        this.title = title;
    }

    public Node getNode() {
        if (node == null)
            node = createNode();
        return node;
    }

    public Node createNode() {

        return new Document();

    }

    public void init() {
        appendTopic(getCentralTopic(), 0, null);
    }

    private void appendTopic(ITopic topic, int level,
            MarkDownExportPart parent) {
        TitlePart topicPart = new TitlePart(this, topic, level);
        topicPart.setParent(parent);
        append(topicPart);

        appendTopicContent(topic, level, parent);
    }

    private void appendTopicContent(ITopic topic, int level,
            MarkDownExportPart parent) {
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
            MarkDownExportPart parent) {
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

    /*
     * public String addStyle(String styleId) { if (styleId == null) return
     * null; String cachedStyleId = styleIdMap == null ? null :
     * styleIdMap.get(styleId); if (cachedStyleId != null) return cachedStyleId;
     * IStyle style = getStyle(styleId); if (style == null) return null; String
     * type = style.getType(); if (!isStyleInteresting(style, type)) return
     * null; if (usedStyles == null) usedStyles = new HashMap<String,
     * List<String>>(); List<String> list = usedStyles.get(type); if (list ==
     * null) { list = new ArrayList<String>(); usedStyles.put(type, list); }
     * String newStyleId = newStyleId(type, list.size() + 1);
     * list.add(newStyleId); if (styleIdMap == null) styleIdMap = new
     * HashMap<String, String>(); styleIdMap.put(styleId, newStyleId); if
     * (styleMap == null) styleMap = new HashMap<String, IStyle>();
     * styleMap.put(newStyleId, style); return newStyleId; }
     */

    /*
     * private String newStyleId(String type, int index) { if
     * (IStyle.PARAGRAPH.equals(type)) return "p" + index; //$NON-NLS-1$ else if
     * (IStyle.TEXT.equals(type)) return "s" + index; //$NON-NLS-1$ }
     */

    private boolean isStyleInteresting(IStyle style, String type) {
        return IStyle.PARAGRAPH.equals(type) || IStyle.TEXT.equals(type);
    }

    protected void write(IProgressMonitor monitor, IExportPart part)
            throws InvocationTargetException, InterruptedException {
        MarkDownExportPart child = (MarkDownExportPart) part;
        MarkDownExportPart parent = child.getParent();

        if (parent != null) {
            child.addToParent(parent);
        } else {
            getNode().appendChild(child.getNode());
        }

        //MarkDownExportPart parent = child.getParent();
        /*
         * if (parent != null) { child.addToParent(parent); } else {
         * //getBodyNode().appendChild(child.getNode()); }
         */
    }

    public void end() throws InvocationTargetException {
        // createStyles();
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(targetPath);
            try {
                //DOMUtils.save(getDocument(), out, true);

                MarkDownContentRender mdRender = MarkDownContentRender.builder()
                        .build();
                String content = mdRender.render(this.getNode());

                out.write(content.getBytes());

            } catch (IOException e) {
                throw new InvocationTargetException(e);
            }
        } catch (FileNotFoundException e) {
            throw new InvocationTargetException(e);
        } finally {

            if (null != out) {
                try {
                    out.close();
                } catch (IOException e) {

                }
            }
            super.end();
        }
    }

    /*
     * private void createStyles() { if (usedStyles == null ||
     * usedStyles.isEmpty()) return; StringBuilder sb = new StringBuilder();
     * sb.append(MarkDownConstants.LINE_SEP); List<String> ps =
     * usedStyles.get(IStyle.PARAGRAPH); if (ps != null && !ps.isEmpty()) {
     * createStyles(MarkDownConstants.TAG_P, ps, sb); } List<String> ts =
     * usedStyles.get(IStyle.TEXT); if (ts != null && !ts.isEmpty()) {
     * createStyles(MarkDownConstants.TAG_SPAN, ts, sb); } String content =
     * sb.toString(); Element styleEle =
     * DOMUtils.ensureChildElement(getHeadElement(),
     * MarkDownConstants.TAG_STYLE);
     * styleEle.setAttribute(MarkDownConstants.ATT_TYPE, "text/css");
     * //$NON-NLS-1$ styleEle.setTextContent(content); }
     */

    /*
     * private void createStyles(String tag, List<String> styleIds,
     * StringBuilder sb) { for (String styleId : styleIds) { IStyle style =
     * styleMap == null ? null : styleMap.get(styleId); if (style != null) {
     * createStyle(tag, styleId, style, sb); } } }
     */

    /*
     * private void createStyle(String tag, String styleId, IStyle style,
     * StringBuilder sb) { sb.append(tag); sb.append('.'); sb.append(styleId);
     * sb.append(' '); sb.append('{'); Iterator<Property> properties =
     * style.properties(); while (properties.hasNext()) { Property property =
     * properties.next(); String name = DOMUtils.getLocalName(property.key);
     * String value = property.value; sb.append(name); sb.append(':');
     * sb.append(' '); sb.append(value); if (properties.hasNext()) {
     * sb.append(';'); sb.append(' '); } } sb.append('}');
     * sb.append(MarkDownConstants.LINE_SEP); }
     */
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
