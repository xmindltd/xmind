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
package org.xmind.ui.internal.imports.mm;

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
import java.util.Stack;
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
import org.xmind.core.IIdentifiable;
import org.xmind.core.IImage;
import org.xmind.core.IImageSpan;
import org.xmind.core.ILegend;
import org.xmind.core.INotes;
import org.xmind.core.INumbering;
import org.xmind.core.IParagraph;
import org.xmind.core.IPlainNotesContent;
import org.xmind.core.IRelationship;
import org.xmind.core.ISheet;
import org.xmind.core.ISpan;
import org.xmind.core.ISummary;
import org.xmind.core.ITitled;
import org.xmind.core.ITopic;
import org.xmind.core.ITopicExtension;
import org.xmind.core.ITopicExtensionElement;
import org.xmind.core.ITopicRange;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.core.internal.dom.NumberUtils;
import org.xmind.core.internal.dom.StyleSheetImpl;
import org.xmind.core.io.DirectoryStorage;
import org.xmind.core.io.IInputSource;
import org.xmind.core.io.IStorage;
import org.xmind.core.io.ResourceMappingManager;
import org.xmind.core.io.mindmanager.MMConstants;
import org.xmind.core.io.mindmanager.MMResourceMappingManager;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.core.style.IStyled;
import org.xmind.core.util.DOMUtils;
import org.xmind.core.util.FileUtils;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.imports.ImportMessages;
import org.xmind.ui.internal.imports.ImporterUtils;
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

public class MindManagerImporter extends MindMapImporter
        implements MMConstants, ErrorHandler {

    private static final Pattern DATE_PATTERN = Pattern.compile(
            "((\\d+)-(\\d{1,2})-(\\d{1,2}))T((\\d{1,2}):(\\d{1,2}):(\\d{1,2}))"); //$NON-NLS-1$

    private static final String TRANSPARENT_VALUE = "0.00"; //$NON-NLS-1$

    private static Pattern OID_PATTERN = null;

    private class NotesImporter {

        IParagraph currentParagraph = null;
//        IBaseParagraph currentParagraph = null;

        Stack<IStyle> styleStack = new Stack<IStyle>();

        IHtmlNotesContent content;

        public NotesImporter(IHtmlNotesContent content) {
            this.content = content;
        }

        public void loadFrom(Element notesGroupEle, Element element)
                throws InterruptedException {
            checkInterrupted();
            String tagName = DOMUtils.getLocalName(element.getTagName());

            if ("br".equalsIgnoreCase(tagName)) { //$NON-NLS-1$
                addParagraph();
                return;
            }

            boolean isParagraph = "p".equalsIgnoreCase(tagName) //$NON-NLS-1$
                    || "li".equalsIgnoreCase(tagName); //$NON-NLS-1$
            IStyle style = pushStyle(element,
                    isParagraph ? IStyle.PARAGRAPH : IStyle.TEXT);

            if (isParagraph) {
                addParagraph();
            } else if ("img".equalsIgnoreCase(tagName)) { //$NON-NLS-1$

                addImage(notesGroupEle, element);

            }

            NodeList nl = element.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                short nodeType = node.getNodeType();
                if (nodeType == Node.TEXT_NODE) {
                    addText(node.getTextContent());
                } else if (nodeType == Node.ELEMENT_NODE) {
                    loadFrom(notesGroupEle, (Element) node);
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

            addSpan(content.createTextSpan(text));
        }

        private void addImage(Element notesGroupEle, Element imgEle)
                throws InterruptedException {
            Iterator<Element> it = children(notesGroupEle, "ap:NotesData"); //$NON-NLS-1$
            while (it.hasNext()) {
                Element notesDataEle = it.next();
                String imgUri = att(notesDataEle, "ImageUri"); //$NON-NLS-1$
                String sourceImg = att(imgEle, "src"); //$NON-NLS-1$
                if (imgUri != null && sourceImg != null
                        && imgUri.equals(sourceImg)) {
                    loadImageAtt(imgUri, loadUri(notesDataEle));
                    String srcUrl = loadUri(notesDataEle);
                    if (srcUrl != null) {
                        String entryPath = idMap.get(srcUrl);
                        if (entryPath != null) {
                            addSpan(content.createImageSpan(HyperlinkUtils
                                    .toAttachmentPath(entryPath)));
                        }
                    }
                }
            }

        }

        private void addSpan(ISpan span) {
            if (currentParagraph == null)
                addParagraph();

            currentParagraph.addSpan(span);

            if (!(span instanceof IImageSpan)) {
                registerStyle(span, Styles.FontFamily, Styles.FontSize,
                        Styles.TextColor, Styles.FontWeight,
                        Styles.TextDecoration, Styles.FontStyle);
            }
        }

        private void addParagraph() {
            currentParagraph = content.createParagraph();
//            currentParagraph = content
//                    .createParagraph(IBaseParagraph.GENERAL_PARAGRAPH);
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
                if (value != null) {
                    MindManagerImporter.this.registerStyle(host, key, value);
                }
            }
        }

        private IStyle pushStyle(Element ele, String type) {
            IStyle style = getTempStyleSheet().createStyle(type);
            receiveStyle(ele, style);
            if (style.isEmpty()) {
                style = null;
            } else {
                styleStack.push(style);
            }
            return style;
        }

        private void receiveStyle(Element ele, IStyle style) {
            String align = parseAlign(att(ele, "align")); //$NON-NLS-1$
            if (align != null) {
                style.setProperty(Styles.TextAlign, align);
            }

            String name = ele.getTagName();
            if ("b".equalsIgnoreCase(name)) { //$NON-NLS-1$
                style.setProperty(Styles.FontWeight, Styles.FONT_WEIGHT_BOLD);
            } else if ("i".equalsIgnoreCase(name)) { //$NON-NLS-1$
                style.setProperty(Styles.FontStyle, Styles.FONT_STYLE_ITALIC);
            } else if ("u".equalsIgnoreCase(name)) { //$NON-NLS-1$
                style.setProperty(Styles.TextDecoration,
                        Styles.TEXT_DECORATION_UNDERLINE);
            } else if ("s".equalsIgnoreCase(name)) { //$NON-NLS-1$
                style.setProperty(Styles.TextDecoration,
                        Styles.TEXT_DECORATION_LINE_THROUGH);
            } else if ("font".equalsIgnoreCase(name)) { //$NON-NLS-1$
                String fontName = att(ele, "face"); //$NON-NLS-1$
                String availableFontName = FontUtils
                        .getAAvailableFontNameFor(fontName);
                fontName = availableFontName != null ? availableFontName
                        : fontName;
                if (fontName != null) {
                    style.setProperty(Styles.FontFamily, fontName);
                }
            } else if ("span".equalsIgnoreCase(name)) { //$NON-NLS-1$
                if ("font".equalsIgnoreCase( //$NON-NLS-1$
                        ((Element) ele.getParentNode()).getTagName())) {
                    String color = att((Element) ele.getParentNode(), "color"); //$NON-NLS-1$
                    style.setProperty(Styles.TextColor, color);
                }
            }

            String styleContent = att(ele, "style"); //$NON-NLS-1$
            if (styleContent != null) {
                receiveStyleContent(style, styleContent);
            }

        }

        private String parseAlign(String align) {
            if (align != null) {
                if (Styles.ALIGN_CENTER.equalsIgnoreCase(align) //
                        || Styles.ALIGN_LEFT.equalsIgnoreCase(align) //
                        || Styles.ALIGN_RIGHT.equalsIgnoreCase(align))
                    return align;
            }
            return null;
        }

        private void receiveStyleContent(IStyle style, String styleContent) {
            String[] items = styleContent.trim().split(";"); //$NON-NLS-1$
            for (String item : items) {
                item = item.trim();
                int colonIndex = item.indexOf(':');
                if (colonIndex > 0) {
                    String key = item.substring(0, colonIndex).trim();
                    String value = item.substring(colonIndex + 1).trim();
                    receiveStyleItem(style, key, value);
                }
            }
        }

        private void receiveStyleItem(IStyle style, String key, String value) {
            if ("color".equalsIgnoreCase(key)) { //$NON-NLS-1$
                String color = parseColor(value);
                if (color != null) {
                    style.setProperty(Styles.TextColor, color);
                }
            } else if ("font-size".equalsIgnoreCase(key)) { //$NON-NLS-1$
                int fontSize = NumberUtils.safeParseInt(value, -1);
                if (fontSize > 0) {
                    style.setProperty(Styles.FontSize,
                            StyleUtils.addUnitPoint(fontSize));
                }
            }
        }

        private void popStyle(IStyle style) {
            if (style == null)
                return;

            if (styleStack != null && styleStack.peek() == style) {
                styleStack.pop();
            }
        }

        private String parseColor(String color) {
            return color;
//            if (color != null) {
//                return ColorUtils.toString(ColorUtils.toRGB(color));
//            }
//            return null;
        }

    }

    private static final String DOCUMENT_XML = "Document.xml"; //$NON-NLS-1$

    private static final double DPM = 72 / 25.4;

    private static ResourceMappingManager mappings = null;

//    private ZipFile sourceFile;
    private IStorage tempStorage;

    private IInputSource tempSource;

    private ISheet targetSheet;

    private Map<String, String> idMap = new HashMap<String, String>(30);

    private IStyleSheet tempStyleSheet = null;

    private Map<IStyled, IStyle> styleMap = new HashMap<IStyled, IStyle>(30);

    private IStyle theme = null;

    private Map<String, List<ITopic>> topicLinkMap = new HashMap<String, List<ITopic>>(
            10);

    public MindManagerImporter(String sourcePath) {
        super(sourcePath);
    }

    public MindManagerImporter(String sourcePath, IWorkbook targetWorkbook) {
        super(sourcePath, targetWorkbook);
    }

    public void build() throws InvocationTargetException, InterruptedException {
        MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                UserDataConstants.CATEGORY_IMPORT,
                UserDataConstants.IMPORT_FROM_MIND_MANAGER);
        getMonitor().beginTask(null, 100);
        try {
            getMonitor()
                    .subTask(ImportMessages.MindManagerImporter_ReadingContent);

            tempStorage = createTemporaryStorage();
            extractSourceFileToTemporaryStorage();
            tempSource = tempStorage.getInputSource();
//            sourceFile = new ZipFile(getSourcePath());
//            ZipEntry docEntry = sourceFile.getEntry(DOCUMENT_XML);

            Document doc = readContents();

            getMonitor().worked(45);
            checkInterrupted();

            getMonitor().subTask(
                    ImportMessages.MindManagerImporter_ReadingElements);
            loadSheet(doc.getDocumentElement());
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

    private Document readContents() throws Exception {
        InputStream docEntryStream = tempSource.getEntryStream(DOCUMENT_XML);
        if (docEntryStream == null)
            throw new IOException("No content entry"); //$NON-NLS-1$

        DocumentBuilder builder = getDocumentBuilder();
        builder.setErrorHandler(this);
//            InputStream in = sourceFile.getInputStream(docEntryStream);
//            in = new MonitoredInputStream(in, getMonitor());
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

    /**
     * @return
     */
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
//        if (sourceFile != null) {
//            try {
//                sourceFile.close();
//            } catch (IOException e) {
//            }
//        }
//        sourceFile = null;
    }

    private void loadSheet(Element docEle) throws InterruptedException {
        checkInterrupted();

        targetSheet = getTargetWorkbook().createSheet();
        targetSheet.setTitleText(getSuggestedSheetTitle());

        Element oneTopicEle = child(docEle, "OneTopic"); //$NON-NLS-1$
        if (oneTopicEle != null) {
            loadRootTopic(oneTopicEle);
        }

        Element relsEle = child(docEle, "Relationships"); //$NON-NLS-1$
        if (relsEle != null) {
            loadRelationships(relsEle);
        }

        Element styleGroupEle = child(docEle, "StyleGroup"); //$NON-NLS-1$
        if (styleGroupEle != null) {
            loadStyleGroup(styleGroupEle);
        }

        Element markersGroupEle = child(docEle, "MarkersSetGroup"); //$NON-NLS-1$
        if (markersGroupEle != null) {
            loadMarkersGroup(markersGroupEle);
        }

        addTargetSheet(targetSheet);
    }

    public ISheet getTargetSheet() {
        return targetSheet;
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

    private void loadMarkersGroup(Element markersGroupEle)
            throws InterruptedException {
        checkInterrupted();
        ILegend legend = getTargetSheet().getLegend();
        Element markersSetsEle = child(markersGroupEle, "ap:IconMarkersSets"); //$NON-NLS-1$
        if (markersSetsEle != null) {
            Iterator<Element> it = children(markersSetsEle,
                    "ap:IconMarkersSet"); //$NON-NLS-1$
            while (it.hasNext()) {
                checkInterrupted();
                Element markersSetEle = it.next();
                loadLegendMarkers(markersSetEle, legend, "ap:IconMarkers", //$NON-NLS-1$
                        "ap:IconMarker", //$NON-NLS-1$ 
                        "ap:OneStockIcon", //$NON-NLS-1$ 
                        "IconType"); //$NON-NLS-1$ 
            }
        }

        loadLegendMarkers(markersGroupEle, legend, "ap:IconMarkers", //$NON-NLS-1$
                "ap:IconMarker", //$NON-NLS-1$
                "ap:OneStockIcon", //$NON-NLS-1$
                "IconType"); //$NON-NLS-1$
        loadLegendMarkers(markersGroupEle, legend, "ap:TaskPercentageMarkers", //$NON-NLS-1$
                "ap:TaskPercentageMarker", //$NON-NLS-1$
                "ap:TaskPercentage", //$NON-NLS-1$
                "TaskPercentage"); //$NON-NLS-1$
        loadLegendMarkers(markersGroupEle, legend, "ap:TaskPriorityMarkers", //$NON-NLS-1$
                "ap:TaskPriorityMarker", //$NON-NLS-1$
                "ap:TaskPriority", //$NON-NLS-1$
                "TaskPriority" //$NON-NLS-1$
        );
    }

    private void loadLegendMarkers(Element markersSetEle, ILegend legend,
            String markersEleName, String markerEleName, String iconEleName,
            String iconAttrName) throws InterruptedException {
        checkInterrupted();
        Element markersEle = child(markersSetEle, markersEleName);
        if (markersEle != null)
            loadLegendMarkers(markersEle, legend, markerEleName, iconEleName,
                    iconAttrName);
    }

    private void loadLegendMarkers(Element markersEle, ILegend legend,
            String markerEleName, String iconEleName, String iconAttrName)
            throws InterruptedException {
        checkInterrupted();
        Iterator<Element> it = children(markersEle, markerEleName);
        while (it.hasNext()) {
            checkInterrupted();
            Element markerEle = it.next();
            Element iconEle = child(markerEle, iconEleName);
            if (iconEle != null) {
                String type = att(iconEle, iconAttrName);
                if (type != null) {
                    String markerId = getMapping("marker", type, null); //$NON-NLS-1$
                    if (markerId != null) {
                        String name = loadName(markerEle);
                        legend.setMarkerDescription(markerId, name);
                    }
                }
            }
        }
    }

    private static String loadName(Element parentEle) {
        return loadName(parentEle, null);
    }

    private static String loadName(Element parentEle, String eleName) {
        if (eleName == null)
            eleName = "ap:Name"; //$NON-NLS-1$
        Element ele = child(parentEle, eleName);
        if (ele != null) {
            return att(ele, "Name"); //$NON-NLS-1$
        }
        return null;
    }

    private void loadStyleGroup(Element styleGroupEle)
            throws InterruptedException {
        checkInterrupted();
        loadTopicTheme(styleGroupEle);
        loadRelTheme(styleGroupEle);
        loadBoundaryTheme(styleGroupEle);
        loadSheetStyle(styleGroupEle);
    }

    private void loadSheetStyle(Element styleGroupEle)
            throws InterruptedException {
        checkInterrupted();
        ISheet sheet = getTargetSheet();
        Element structureEle = child(styleGroupEle, "ap:Structure"); //$NON-NLS-1$
        if (structureEle != null) {
            Float lineWidth = parseFloat(
                    att(structureEle, "MainTopicLineWidth")); //$NON-NLS-1$
            if (lineWidth != null && lineWidth.floatValue() > 3.0f) {
                registerStyle(sheet, Styles.LineTapered, Styles.TAPERED);
            }
        }

        Element bgFillEle = child(styleGroupEle, "ap:BackgroundFill"); //$NON-NLS-1$
        if (bgFillEle != null) {
            String fillColor = parseColor(att(bgFillEle, "FillColor")); //$NON-NLS-1$
            if (fillColor != null) {
                registerStyle(sheet, Styles.FillColor, fillColor);
            }
        }

        Element bgImgEle = child(styleGroupEle, "ap:BackgroundImageData"); //$NON-NLS-1$
        if (bgImgEle != null) {
            String uri = loadUri(bgImgEle);
            if (uri != null) {
                IFileEntry entry = loadAttachment(null, uri, "*.png"); //$NON-NLS-1$
                if (entry != null && entry.getSize() > 0) {
                    registerStyle(sheet, Styles.Background,
                            HyperlinkUtils.toAttachmentURL(entry.getPath()));
                    int transparency = NumberUtils
                            .safeParseInt(att(bgImgEle, "Transparency"), -1); //$NON-NLS-1$
                    if (transparency >= 0) {
                        double opacity = (100 - transparency) * 1.0 / 100;
                        registerStyle(sheet, Styles.Opacity,
                                String.format("%.2f", opacity)); //$NON-NLS-1$
                    }
                }
            }
        }
    }

    private void loadBoundaryTheme(Element styleGroupEle)
            throws InterruptedException {
        checkInterrupted();
        Element parentEle = child(styleGroupEle, "ap:BoundaryDefaultsGroup"); //$NON-NLS-1$
        if (parentEle == null)
            return;

        loadThemeColor(parentEle, true, true, true, IStyle.BOUNDARY,
                Styles.FAMILY_BOUNDARY);
        loadThemeLineStyle(parentEle, IStyle.BOUNDARY, Styles.FAMILY_BOUNDARY);
        loadThemeBoundaryShape(parentEle, Styles.FAMILY_BOUNDARY);
    }

    private void loadThemeBoundaryShape(Element parentEle, String styleFamily)
            throws InterruptedException {
        checkInterrupted();
        Element ele = child(parentEle, "ap:DefaultBoundaryShape"); //$NON-NLS-1$
        if (ele == null)
            return;

        String shape = parseBoundaryShape(att(ele, "BoundaryShape")); //$NON-NLS-1$
        registerTheme(IStyle.BOUNDARY, styleFamily, Styles.ShapeClass, shape);
    }

    private void loadThemeLineStyle(Element parentEle, String type,
            String styleFamily) throws InterruptedException {
        checkInterrupted();
        Element ele = child(parentEle, "ap:DefaultLineStyle"); //$NON-NLS-1$
        if (ele == null)
            return;

        String linePattern = parseLinePattern(att(ele, "LineDashStyle")); //$NON-NLS-1$
        registerTheme(type, styleFamily, Styles.LinePattern, linePattern);

        String lineWidth = parseLineWidth(att(ele, "LineWidth")); //$NON-NLS-1$
        registerTheme(type, styleFamily, Styles.LineWidth, lineWidth);
    }

    private void loadRelTheme(Element styleGroupEle)
            throws InterruptedException {
        checkInterrupted();
        Element parentEle = child(styleGroupEle,
                "ap:RelationshipDefaultsGroup"); //$NON-NLS-1$
        if (parentEle == null)
            return;

        loadThemeColor(parentEle, false, false, true, IStyle.RELATIONSHIP,
                Styles.FAMILY_RELATIONSHIP);
        loadThemeLineStyle(parentEle, IStyle.RELATIONSHIP,
                Styles.FAMILY_RELATIONSHIP);
        loadThemeArrowStyle(parentEle, Styles.FAMILY_RELATIONSHIP);
        loadThemeRelShape(parentEle, Styles.FAMILY_RELATIONSHIP);
    }

    private void loadThemeRelShape(Element parentEle, String styleFamily)
            throws InterruptedException {
        checkInterrupted();
        Element relShapeEle = child(parentEle,
                "ap:DefaultRelationshipLineShape"); //$NON-NLS-1$
        if (relShapeEle == null)
            return;

        String shape = parseRelationshipShape(att(relShapeEle, "LineShape")); //$NON-NLS-1$
        registerTheme(IStyle.RELATIONSHIP, styleFamily, Styles.ShapeClass,
                shape);
    }

    private void loadThemeArrowStyle(Element parentEle, String styleFamily)
            throws InterruptedException {
        checkInterrupted();
        Iterator<Element> it = children(parentEle, "ap:DefaultConnectionStyle"); //$NON-NLS-1$
        while (it.hasNext()) {
            checkInterrupted();
            Element ele = it.next();
            int index = NumberUtils.safeParseInt(att(ele, "Index"), -1); //$NON-NLS-1$
            if (index == 0 || index == 1) {
                String shape = parseConnShape(att(ele, "ConnectionShape")); //$NON-NLS-1$
                if (shape != null) {
                    String styleKey = index == 0 ? Styles.ArrowBeginClass
                            : Styles.ArrowEndClass;
                    registerTheme(IStyle.RELATIONSHIP, styleFamily, styleKey,
                            shape);
                }
            }
        }
    }

    private void loadThemeColor(Element parentEle, boolean fill,
            boolean fillAlpha, boolean line, String type, String styleFamily)
            throws InterruptedException {
        checkInterrupted();
        Element colorEle = child(parentEle, "ap:DefaultColor"); //$NON-NLS-1$
        if (colorEle == null)
            return;

        if (fill || fillAlpha) {
            String fillColor = att(colorEle, "FillColor"); //$NON-NLS-1$
            if (fill) {
                registerTheme(type, styleFamily, Styles.FillColor,
                        parseColor(fillColor));
            }
            if (fillAlpha) {
                registerTheme(type, styleFamily, Styles.Opacity,
                        parseAlpha(fillColor));
            }
        }

        if (line) {
            String lineColor = att(colorEle, "LineColor"); //$NON-NLS-1$
            registerTheme(type, styleFamily, Styles.LineColor,
                    parseColor(lineColor));
        }
    }

    private void loadTopicTheme(Element styleGroupEle)
            throws InterruptedException {
        checkInterrupted();
        Element rootEle = child(styleGroupEle, "ap:RootTopicDefaultsGroup"); //$NON-NLS-1$
        if (rootEle != null) {
            loadTopicTheme(rootEle, null, null, Styles.FAMILY_CENTRAL_TOPIC);
        }

        int deepestLevel = 0;
        Element realSubEle = null;
        Iterator<Element> it = children(rootEle,
                "ap:RootSubTopicDefaultsGroup"); //$NON-NLS-1$
        while (it.hasNext()) {
            Element subEle = it.next();
            int level = NumberUtils.safeParseInt(att(subEle, "Level"), -1); //$NON-NLS-1$
            if (level == 0) {
                loadTopicTheme(subEle, null, null, Styles.FAMILY_MAIN_TOPIC);
            } else if (level > 0) {
                if (level > deepestLevel) {
                    deepestLevel = level;
                    realSubEle = subEle;
                }
            }
        }

        if (realSubEle != null) {
            loadTopicTheme(realSubEle, null, null, Styles.FAMILY_SUB_TOPIC);
        }

        Element floatingEle = child(styleGroupEle,
                "ap:LabelTopicDefaultsGroup"); //$NON-NLS-1$
        if (floatingEle != null) {
            loadTopicTheme(floatingEle, "ap:DefaultLabelFloatingTopicShape", //$NON-NLS-1$
                    "LabelFloatingTopicShape", Styles.FAMILY_FLOATING_TOPIC); //$NON-NLS-1$
        }
    }

    private void loadTopicTheme(Element parentEle, String shapeEleName,
            String shapeAttrName, String styleFamily)
            throws InterruptedException {
        checkInterrupted();
        loadThemeColor(parentEle, true, false, true, IStyle.TOPIC, styleFamily);
        loadThemeTextStyle(parentEle, styleFamily);
        loadThemeTopicShape(parentEle, shapeEleName, shapeAttrName,
                styleFamily);
        loadThemeBranchStyle(parentEle, styleFamily);
    }

    private void loadThemeBranchStyle(Element parentEle, String styleFamily)
            throws InterruptedException {
        checkInterrupted();
        Element ele = child(parentEle, "ap:DefaultSubTopicsShape"); //$NON-NLS-1$
        if (ele == null)
            return;

        String branchConn = parseBranchConn(
                att(ele, "SubTopicsConnectionStyle")); //$NON-NLS-1$
        registerTheme(IStyle.TOPIC, styleFamily, Styles.LineClass, branchConn);
    }

    private static String parseBranchConn(String connStyle) {
        return getMapping("branchConnection", connStyle, null); //$NON-NLS-1$
    }

    private void loadThemeTopicShape(Element parentEle, String shapeEleName,
            String shapeAttrName, String styleFamily)
            throws InterruptedException {
        checkInterrupted();
        if (shapeEleName == null)
            shapeEleName = "ap:DefaultSubTopicShape"; //$NON-NLS-1$
        Element shapeEle = child(parentEle, shapeEleName);
        if (shapeEle == null)
            return;

        if (shapeAttrName == null)
            shapeAttrName = "SubTopicShape"; //$NON-NLS-1$
        String shape = parseTopicShape(att(shapeEle, shapeAttrName));
        registerTheme(IStyle.TOPIC, styleFamily, Styles.ShapeClass, shape);
    }

    private void loadThemeTextStyle(Element parentEle, String themeKey)
            throws InterruptedException {
        checkInterrupted();
        Element textEle = child(parentEle, "ap:Text"); //$NON-NLS-1$
        if (textEle == null)
            return;

        loadThemeFont(textEle, IStyle.TOPIC, themeKey);
    }

    private void loadThemeFont(Element parentEle, String type,
            String styleFamily) throws InterruptedException {
        checkInterrupted();
        Element fontEle = child(parentEle, "ap:Font"); //$NON-NLS-1$
        if (fontEle == null)
            return;

        String color = parseColor(att(fontEle, "Color")); //$NON-NLS-1$
        registerTheme(type, styleFamily, Styles.TextColor, color);

        String fontName = att(fontEle, "Name"); //$NON-NLS-1$
        String availableFontName = FontUtils.getAAvailableFontNameFor(fontName);
        fontName = availableFontName != null ? availableFontName : fontName;
        registerTheme(type, styleFamily, Styles.FontFamily, fontName);

        String size = att(fontEle, "Size"); //$NON-NLS-1$
        registerTheme(type, styleFamily, Styles.FontSize, size);

        String bold = att(fontEle, "Bold"); //$NON-NLS-1$
        registerTheme(type, styleFamily, Styles.FontWeight,
                Boolean.parseBoolean(bold) ? Styles.FONT_WEIGHT_BOLD : null);

        String italic = att(fontEle, "Italic"); //$NON-NLS-1$
        registerTheme(type, styleFamily, Styles.FontStyle,
                Boolean.parseBoolean(italic) ? Styles.FONT_STYLE_ITALIC : null);

        boolean underline = Boolean.parseBoolean(att(fontEle, "Underline")); //$NON-NLS-1$
        boolean strikeout = Boolean.parseBoolean(att(fontEle, "Strikethrough")); //$NON-NLS-1$
        registerTheme(type, styleFamily, Styles.TextDecoration,
                StyleUtils.toTextDecoration(underline, strikeout));
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

    private void loadRelationships(Element relsEle)
            throws InterruptedException {
        checkInterrupted();
        Iterator<Element> it = children(relsEle, "ap:Relationship"); //$NON-NLS-1$
        while (it.hasNext()) {
            Element relEle = it.next();
            loadRelationship(relEle);
        }
    }

    private void loadRelationship(Element relEle) throws InterruptedException {
        checkInterrupted();
        IRelationship rel = getTargetWorkbook().createRelationship();
        getTargetSheet().addRelationship(rel);
        loadOId(relEle, rel);
        boolean autoRouting = loadAutoRouting(relEle);
        loadConnections(relEle, rel, autoRouting);
        loadLineStyle(relEle, rel);
        loadRelLineShape(relEle, rel);
    }

    private void loadRelLineShape(Element relEle, IRelationship rel)
            throws InterruptedException {
        checkInterrupted();
        Element relShapeEle = child(relEle, "ap:RelationshipLineShape"); //$NON-NLS-1$
        if (relShapeEle == null)
            return;
        String shape = parseRelationshipShape(att(relShapeEle, "LineShape")); //$NON-NLS-1$
        registerStyle(rel, Styles.ShapeClass, shape);
    }

    private static String parseRelationshipShape(String shape) {
        return getMapping("relationshipShape", shape, null); //$NON-NLS-1$
    }

    private void loadConnections(Element relEle, IRelationship rel,
            boolean autoRouting) throws InterruptedException {
        checkInterrupted();
        Iterator<Element> it = children(relEle, "ap:ConnectionGroup"); //$NON-NLS-1$
        while (it.hasNext()) {
            Element connGroupEle = it.next();
            loadRelConnection(connGroupEle, rel, autoRouting);
        }
    }

    private void loadRelConnection(Element connGroupEle, IRelationship rel,
            boolean autoRouting) throws InterruptedException {
        checkInterrupted();
        int index = NumberUtils.safeParseInt(att(connGroupEle, "Index"), -1); //$NON-NLS-1$
        if (index != 0 && index != 1)
            return;

        Element connEle = child(connGroupEle, "ap:Connection"); //$NON-NLS-1$
        if (connEle == null)
            return;

        Element refEle = child(connEle, "ap:ObjectReference"); //$NON-NLS-1$
        if (refEle == null)
            return;

        String oIdRef = att(refEle, "OIdRef"); //$NON-NLS-1$
        String endId = idMap.get(oIdRef);
        if (endId == null)
            return;

        if (index == 0) {
            rel.setEnd1Id(endId);
        } else {
            rel.setEnd2Id(endId);
        }

        if (!autoRouting || connEle != null) {
            String cx = att(connEle, "CX"); //$NON-NLS-1$
            String cy = att(connEle, "CY"); //$NON-NLS-1$
            if (cx != null && cy != null) {
                Float x = parseFloat(cx);
                Float y = parseFloat(cy);
                if (x != null && y != null) {
                    rel.getControlPoint(index).setPosition(
                            mm2Dots(x.floatValue()), mm2Dots(y.floatValue()));
                }
            }
        }

        Element connStyleEle = child(connGroupEle, "ap:ConnectionStyle"); //$NON-NLS-1$
        if (connStyleEle != null) {
            String arrowShape = parseConnShape(
                    att(connStyleEle, "ConnectionShape")); //$NON-NLS-1$
            String styleKey = index == 0 ? Styles.ArrowBeginClass
                    : Styles.ArrowEndClass;
            registerStyle(rel, styleKey, arrowShape);
        }
    }

    private static String parseConnShape(String shape) {
        return getMapping("arrowShape", shape, null); //$NON-NLS-1$
    }

    private boolean loadAutoRouting(Element relEle)
            throws InterruptedException {
        checkInterrupted();
        Element autoRouteEle = child(relEle, "ap:AutoRoute"); //$NON-NLS-1$
        if (autoRouteEle != null) {
            return Boolean.parseBoolean(att(autoRouteEle, "AutoRouting")); //$NON-NLS-1$
        }
        return false;
    }

    private void loadRootTopic(Element oneTopicEle)
            throws InterruptedException {
        checkInterrupted();
        Element topicEle = child(oneTopicEle, "Topic"); //$NON-NLS-1$
        if (topicEle != null) {
            loadTopicContent(topicEle, getTargetSheet().getRootTopic());
        }
    }

    private void loadTopicContent(Element topicEle, ITopic topic)
            throws InterruptedException {
        checkInterrupted();
        loadOId(topicEle, topic);
        loadTitle(topicEle, topic);
        loadColor(topicEle, topic, false);
        loadPosition(topicEle, topic);
        loadTopicViewGroup(topicEle, topic);
        loadImage(topicEle, topic);
        loadTopicShape(topicEle, topic);
        loadHyperlink(topicEle, topic);
        loadMarkers(topicEle, topic);
        loadLabels(topicEle, topic);
        loadNotes(topicEle, topic);
        loadTask(topicEle, topic);
        loadNumbering(topicEle, topic);

        loadSubTopics(topicEle, topic);
        loadDetachedSubTopics(topicEle, topic);
        loadBoundary(topicEle, topic);
        loadStructure(topicEle, topic);
        loadAttachments(topicEle, topic);
    }

    private void loadAttachments(Element topicEle, ITopic topic)
            throws InterruptedException {
        checkInterrupted();
        Element attGroupEle = child(topicEle, "ap:AttachmentGroup"); //$NON-NLS-1$
        if (attGroupEle == null)
            return;

        Iterator<Element> it = children(attGroupEle, "ap:AttachmentData"); //$NON-NLS-1$
        while (it.hasNext()) {
            Element attDataEle = it.next();
            String uri = loadUri(attDataEle);
            if (uri != null) {
                String oId = att(attDataEle, "AttachmentId"); //$NON-NLS-1$
                String name = att(attDataEle, "FileName"); //$NON-NLS-1$
                IFileEntry entry = loadAttachment(oId, uri, name);
                if (entry != null) {
                    if (name == null)
                        name = new File(entry.getPath()).getName();
                    ITopic attTopic = getTargetWorkbook().createTopic();
                    attTopic.setTitleText(name);
                    attTopic.setHyperlink(
                            HyperlinkUtils.toAttachmentURL(entry.getPath()));
                    topic.add(attTopic, ITopic.ATTACHED);
                }
            }
        }
    }

    private void loadStructure(Element topicEle, ITopic topic)
            throws InterruptedException {
        checkInterrupted();
        Element subTopicsShapeEle = child(topicEle, "ap:SubTopicsShape"); //$NON-NLS-1$
        if (subTopicsShapeEle == null)
            return;

        String line = parseBranchConnection(
                att(subTopicsShapeEle, "SubTopicsConnectionStyle")); //$NON-NLS-1$
        registerStyle(topic, Styles.LineClass, line);

        String structureClass = parseStructureType(subTopicsShapeEle);
        topic.setStructureClass(structureClass);
    }

    private static String parseStructureType(Element ele) {
        String align = att(ele, "SubTopicsAlignment"); //$NON-NLS-1$
        String growth = att(ele, "SubTopicsGrowth"); //$NON-NLS-1$
        String growthDir = att(ele, "SubTopicsGrowthDirection"); //$NON-NLS-1$
        if (umCenter.equals(align) && umHorizontal.equals(growth)) {
            if (umLeftAndRight.equals(growthDir))
                return "org.xmind.ui.map.clockwise"; //$NON-NLS-1$
            if (umRight.equals(growthDir))
                return "org.xmind.ui.logic.right"; //$NON-NLS-1$
            if (umLeft.equals(growthDir))
                return "org.xmind.ui.logic.left"; //$NON-NLS-1$
        } else if (umVertical.equals(growth)
                && umMiddle.equals(att(ele, "SubTopicsVerticalAlignment"))) { //$NON-NLS-1$
            String vgd = att(ele, "SubTopicsVerticalGrowthDirection"); //$NON-NLS-1$
            if (umDown.equals(vgd) || umUpAndDown.equals(vgd))
                return "org.xmind.ui.org-chart.down"; //$NON-NLS-1$
            if (umUp.equals(vgd))
                return "org.xmind.ui.org-chart.up"; //$NON-NLS-1$
        } else if (umBottom.equals(align) && umHorizontal.equals(growth)) {
            if (umRight.equals(growthDir) || umLeftAndRight.equals(growthDir))
                return "org.xmind.ui.tree.right"; //$NON-NLS-1$
            if (umLeft.equals(growthDir))
                return "org.xmind.ui.tree.left"; //$NON-NLS-1$
        }
        return null;
    }

    private static String parseBranchConnection(String lineShape) {
        return getMapping("branchConnection", lineShape, null); //$NON-NLS-1$
    }

    private void loadBoundary(Element topicEle, ITopic topic)
            throws InterruptedException {
        checkInterrupted();
        if (topic.isRoot())
            return;

        Element oneBoundaryEle = child(topicEle, "ap:OneBoundary"); //$NON-NLS-1$
        if (oneBoundaryEle == null)
            return;

        Element boundaryEle = child(oneBoundaryEle, "ap:Boundary"); //$NON-NLS-1$
        if (boundaryEle == null)
            return;

        checkInterrupted();
        String boundaryShape = getBoundaryShape(boundaryEle);
        String shape = parseSummaryShape(boundaryShape);
        ITopicRange range;
        ITopic parent;
        if (shape != null && topic.isAttached()) {
            ISummary summary = getTargetWorkbook().createSummary();
            parent = topic.getParent();
            int index = topic.getIndex();
            summary.setStartIndex(index);
            summary.setEndIndex(index);
            parent.addSummary(summary);
            range = summary;
        } else {
            shape = parseBoundaryShape(boundaryShape);
            IBoundary boundary = getTargetWorkbook().createBoundary();
            if (topic.isAttached()) {
                parent = topic.getParent();
                int index = topic.getIndex();
                boundary.setStartIndex(index);
                boundary.setEndIndex(index);
                parent.addBoundary(boundary);
            } else {
                parent = null;
                boundary.setMasterBoundary(true);
                topic.addBoundary(boundary);
            }
            range = boundary;
        }
        loadOId(boundaryEle, (IIdentifiable) range);
        loadColor(boundaryEle, (IStyled) range, true);
        loadLineStyle(boundaryEle, (IStyled) range);
        registerStyle((IStyled) range, Styles.ShapeClass, shape);

        if (range instanceof ISummary && parent != null) {
            ISummary summary = (ISummary) range;
            ITopic summaryTopic = getTargetWorkbook().createTopic();
            parent.add(summaryTopic, ITopic.SUMMARY);
            summary.setTopicId(summaryTopic.getId());

            Element summaryTopicEle = getSummaryTopicEle(boundaryEle);
            if (summaryTopicEle != null) {
                loadTopicContent(summaryTopicEle, summaryTopic);
            } else {
                summaryTopic.setTitleText(" "); //$NON-NLS-1$
                registerStyle(summaryTopic, Styles.ShapeClass,
                        "org.xmind.topicShape.noBorder"); //$NON-NLS-1$
            }
        } else if (range instanceof IBoundary) {
            IBoundary boundary = (IBoundary) range;
            Element boundaryTopicEle = getSummaryTopicEle(boundaryEle);
            if (boundaryTopicEle != null) {
                loadTitle(boundaryTopicEle, boundary);
            }
        }
    }

    private Element getSummaryTopicEle(Element boundaryEle) {
        Element oneSummaryTopicEle = child(boundaryEle, "ap:OneSummaryTopic"); //$NON-NLS-1$
        if (oneSummaryTopicEle != null) {
            return child(oneSummaryTopicEle, "ap:Topic"); //$NON-NLS-1$
        }
        return null;
    }

    private String getBoundaryShape(Element boundaryEle) {
        Element shapeEle = child(boundaryEle, "ap:BoundaryShape"); //$NON-NLS-1$
        if (shapeEle != null) {
            return att(shapeEle, "BoundaryShape"); //$NON-NLS-1$
        }
        return null;
    }

    private static String parseBoundaryShape(String shape) {
        return getMapping("boundaryShape", shape, null); //$NON-NLS-1$
    }

    private static String parseSummaryShape(String shape) {
        return getMapping("summaryShape", shape, null); //$NON-NLS-1$
    }

    private void loadLineStyle(Element parentEle, IStyled host)
            throws InterruptedException {
        checkInterrupted();
        Element lineColor = child(parentEle, "ap:Color"); //$NON-NLS-1$
        if (lineColor != null) {
            String color = att(lineColor, "LineColor");//$NON-NLS-1$
            if (color != null) {
                registerStyle(host, Styles.LineColor, "#" + color.substring(2)); //$NON-NLS-1$
            }
        }

        Element lineStyleEle = child(parentEle, "ap:LineStyle"); //$NON-NLS-1$
        if (lineStyleEle == null)
            return;

        String linePattern = parseLinePattern(
                att(lineStyleEle, "LineDashStyle")); //$NON-NLS-1$
        registerStyle(host, Styles.LinePattern, linePattern);

        String width = parseLineWidth(att(lineStyleEle, "LineWidth")); //$NON-NLS-1$
        registerStyle(host, Styles.LineWidth, width);
    }

    private static String parseLineWidth(String width) {
        Float w = parseFloat(width);
        if (w != null) {
            if (w <= 1.0)
                return "1"; //$NON-NLS-1$
            if (w <= 2.25)
                return "2"; //$NON-NLS-1$
            if (w <= 3.0)
                return "3"; //$NON-NLS-1$
            if (w <= 4.5)
                return "4"; //$NON-NLS-1$
            return "5"; //$NON-NLS-1$
        }
        return null;
    }

    private static String parseLinePattern(String dashStyle) {
        return getMapping("lineStyle", dashStyle, null); //$NON-NLS-1$
    }

    private void loadDetachedSubTopics(Element topicEle, ITopic topic)
            throws InterruptedException {
        checkInterrupted();
        Element subTopicsEle = child(topicEle, "ap:FloatingTopics"); //$NON-NLS-1$
        if (subTopicsEle != null) {
            Iterator<Element> it = children(subTopicsEle, "ap:Topic"); //$NON-NLS-1$
            while (it.hasNext()) {
                Element subTopicEle = it.next();
                ITopic subTopic = getTargetWorkbook().createTopic();
                Element item = child(subTopicEle, "ap:Offset"); //$NON-NLS-1$
                if (item != null && item.hasAttribute("OffsetPriority")) //$NON-NLS-1$
                    topic.add(subTopic, ITopic.DETACHED);
                else
                    topic.add(subTopic, ITopic.CALLOUT);
                loadTopicContent(subTopicEle, subTopic);
            }
        }
    }

    private void loadSubTopics(Element topicEle, ITopic topic)
            throws InterruptedException {
        checkInterrupted();
        Element subTopicsEle = child(topicEle, "ap:SubTopics"); //$NON-NLS-1$
        if (subTopicsEle != null) {
            Iterator<Element> it = children(subTopicsEle, "ap:Topic"); //$NON-NLS-1$
            while (it.hasNext()) {
                Element subTopicEle = it.next();
                ITopic subTopic = getTargetWorkbook().createTopic();
                topic.add(subTopic, ITopic.ATTACHED);
                loadTopicContent(subTopicEle, subTopic);
            }
        }
    }

    private final static String NUMBER_FORMAT_NONE = "org.xmind.numbering.none"; //$NON-NLS-1$

    private final static String NUMBER_FORMAT_ARABIC = "org.xmind.numbering.arabic"; //$NON-NLS-1$

    private final static String NUMBER_FORMAT_ROMAN = "org.xmind.numbering.roman"; //$NON-NLS-1$

    private final static String NUMBER_FORMAT_LOWERCASE = "org.xmind.numbering.lowercase"; //$NON-NLS-1$

    private final static String NUMBER_FORMAT_UPPERCASE = "org.xmind.numbering.uppercase"; //$NON-NLS-1$

    private Map<ITopic, Element> numberingEles;

    private void loadNumbering(Element topicEle, ITopic topic) {
        Element numberingEle = findNumberingEle(topicEle, topic);
        if (numberingEle == null)
            return;

        int maxDepth = getMaxDepth(numberingEle);
        int topicDepth = getTopicDepth(topic);
        String numberFormat = getNumberFormat(numberingEle, topicDepth);
        if (numberFormat == null)
            return;

        String prefix = getNumberingPrefix(numberingEle, topicDepth);
        INumbering numbering = topic.getNumbering();

        if ((maxDepth - topicDepth) >= 0)
            numbering.setFormat(numberFormat);
        else
            numbering.setFormat(NUMBER_FORMAT_NONE);

        if (topicDepth == 1)
            numbering.setPrependsParentNumbers(false);
        numbering.setPrefix(prefix);
    }

    private String getNumberFormat(Element numberingEle, int depth) {
        String numbering = att(numberingEle, "cst1:Numbering"); //$NON-NLS-1$
        if (numbering == null)
            return null;

        int formatIndex = (depth - 1) * 2;
        if (formatIndex < 0 || (formatIndex + 1) > numbering.length())
            return NUMBER_FORMAT_NONE;

        String numberFormat = numbering.substring(formatIndex, formatIndex + 1);
        if ("1".equals(numberFormat)) //$NON-NLS-1$
            return NUMBER_FORMAT_ARABIC;
        if ("I".equals(numberFormat) || "i".equals(numberFormat)) //$NON-NLS-1$ //$NON-NLS-2$
            return NUMBER_FORMAT_ROMAN;
        if ("A".equals(numberFormat)) //$NON-NLS-1$
            return NUMBER_FORMAT_UPPERCASE;
        if ("a".equals(numberFormat)) //$NON-NLS-1$
            return NUMBER_FORMAT_LOWERCASE;
        return NUMBER_FORMAT_NONE;
    }

    private String getNumberingPrefix(Element numberingEle, int topicDepth) {
        String preFix = att(numberingEle, "cst0:Level" + topicDepth + "Text"); //$NON-NLS-1$ //$NON-NLS-2$
        return preFix == null ? "" : preFix; //$NON-NLS-1$
    }

    private int getMaxDepth(Element numberingEle) {
        String depth = att(numberingEle, "cst1:Depth"); //$NON-NLS-1$
        return Integer.parseInt(depth == null ? "1" //$NON-NLS-1$
                : depth);
    }

    private int getTopicDepth(ITopic topic) {
        if (numberingEles == null)
            return 0;

        int topicDepth = 1;
        while (topic != null) {
            Element numberingEle = numberingEles.get(topic);
            if (numberingEle != null)
                return topicDepth;
            topic = topic.getParent();
            topicDepth++;
        }
        return 0;
    }

    private Element findNumberingEle(Element topicEle, ITopic topic) {
        if (topicEle == null)
            return null;

        Element numberingEle = child(topicEle, "cor:Custom"); //$NON-NLS-1$
        if (numberingEle != null) {
            if (numberingEles == null)
                numberingEles = new HashMap<ITopic, Element>();
            numberingEles.put(topic, numberingEle);
            return numberingEle;
        }
        return findParentNumbering(topic);
    }

    private Element findParentNumbering(ITopic topic) {
        if (numberingEles == null)
            return null;

        int topicDepth = 1;
        while (topic.getParent() != null) {
            topic = topic.getParent();
            Element numberingEle = numberingEles.get(topic);
            if (numberingEle != null
                    && (getMaxDepth(numberingEle) >= topicDepth))
                return numberingEle;
            topicDepth++;
        }
        return null;
    }

    private void loadNotes(Element topicEle, ITopic topic)
            throws InterruptedException {
        checkInterrupted();

        Element notesGroupEle = child(topicEle, "ap:NotesGroup"); //$NON-NLS-1$
        if (notesGroupEle == null)
            return;

//        Iterator<Element> it = children(notesGroupEle, "ap:NotesData"); //$NON-NLS-1$
//        while (it.hasNext()) {
//            Element notesDataEle = it.next();
//            String imgUri = att(notesDataEle, "ImageUri"); //$NON-NLS-1$
//            String binaryUri = loadUri(notesDataEle);
//            uriMap.put(imgUri, binaryUri);
//            loadImageAtt(imgUri, binaryUri);
//        }

        Element notesXhtmlDataEle = child(notesGroupEle, "ap:NotesXhtmlData"); //$NON-NLS-1$
        if (notesXhtmlDataEle != null) {
            String bookmarks = getBookmarks(topicEle, topic);

            String plain = att(notesXhtmlDataEle, "PreviewPlainText"); //$NON-NLS-1$
            if (plain != null) {
                IPlainNotesContent content = (IPlainNotesContent) getTargetWorkbook()
                        .createNotesContent(INotes.PLAIN);
                content.setTextContent(bookmarks + plain);
                topic.getNotes().setContent(INotes.PLAIN, content);
            }

            Element htmlEle = child(notesXhtmlDataEle, "xhtml:html"); //$NON-NLS-1$
            if (htmlEle != null) {
                IHtmlNotesContent content = (IHtmlNotesContent) getTargetWorkbook()
                        .createNotesContent(INotes.HTML);
                loadHtmlNotes(notesGroupEle, htmlEle, content, bookmarks);
                topic.getNotes().setContent(INotes.HTML, content);
            }
        }
    }

    private void loadHtmlNotes(Element notesGroupEle, Element htmlEle,
            IHtmlNotesContent content, String bookmarks)
            throws InterruptedException {
        NotesImporter notesImporter = new NotesImporter(content);
        notesImporter.addText(bookmarks);
        notesImporter.loadFrom(notesGroupEle, htmlEle);
    }

    private void loadImageAtt(String imgUri, String uri)
            throws InterruptedException {
        checkInterrupted();
        loadAttachment(null, uri, "*.png"); //$NON-NLS-1$
    }

    private String getBookmarks(Element topicEle, ITopic topic) {
        Element bookmarkEle = child(topicEle, "ap:Bookmark"); //$NON-NLS-1$
        if (bookmarkEle == null)
            return ""; //$NON-NLS-1$

        String bookmarkName = att(bookmarkEle, "Name"); //$NON-NLS-1$
        if (bookmarkName != null) {
            return bookmarkName + '\r';
        }

        return ""; //$NON-NLS-1$
    }

    private void loadLabels(Element topicEle, ITopic topic)
            throws InterruptedException {
        checkInterrupted();
        Element labelsEle = child(topicEle, "ap:TextLabels"); //$NON-NLS-1$
        if (labelsEle == null)
            return;

        Iterator<Element> it = children(labelsEle, "ap:TextLabel"); //$NON-NLS-1$
        while (it.hasNext()) {
            checkInterrupted();
            String label = att(it.next(), "TextLabelName"); //$NON-NLS-1$
            if (label != null) {
                topic.addLabel(label);
            }
        }
    }

    private void loadMarkers(Element topicEle, ITopic topic)
            throws InterruptedException {
        checkInterrupted();
        Element taskEle = child(topicEle, "ap:Task"); //$NON-NLS-1$
        if (taskEle != null) {
            addMarker(topic, att(taskEle, "TaskPriority")); //$NON-NLS-1$
            addMarker(topic, att(taskEle, "TaskPercentage")); //$NON-NLS-1$
        }

        Element iconsGroupEle = child(topicEle, "ap:IconsGroup"); //$NON-NLS-1$
        if (iconsGroupEle != null) {
            Element iconsEle = child(iconsGroupEle, "ap:Icons"); //$NON-NLS-1$
            if (iconsEle != null) {
                Iterator<Element> it = children(iconsEle, "ap:Icon"); //$NON-NLS-1$
                while (it.hasNext()) {
                    addMarker(topic, att(it.next(), "IconType")); //$NON-NLS-1$
                }
            }
        }
    }

    private void addMarker(ITopic topic, String mmIconId)
            throws InterruptedException {
        checkInterrupted();
        if (mmIconId != null) {
            String markerId = parseMarkerId(mmIconId);
            if (markerId != null) {
                topic.addMarker(markerId);
            }
        }
    }

    @SuppressWarnings("nls")
    private void addCheckPoint(ITopicExtensionElement content,
            String hasCheckPoint) {
        if (hasCheckPoint != null && hasCheckPoint.equals("true")) {
            content.setAttribute("check-point", "true");
        }
    }

    private static String parseMarkerId(String mmIconId) {
        return getMapping("marker", mmIconId, "other-question"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private void loadHyperlink(Element topicEle, ITopic topic)
            throws InterruptedException {
        checkInterrupted();
        Element hyperlinkEle = child(topicEle, "ap:Hyperlink"); //$NON-NLS-1$
        if (hyperlinkEle == null)
            return;

        String url = att(hyperlinkEle, "Url"); //$NON-NLS-1$
        if (url != null) {
            if (url.startsWith("#xpointer(")) { //$NON-NLS-1$
                Matcher m = getOIdPattern().matcher(url);
                if (m.find()) {
                    String OId = m.group(1);
                    recordTopicLink(OId, topic);
                }
                return;
            } else if (!url.startsWith("http://") //$NON-NLS-1$
                    && !url.startsWith("https://")) { //$NON-NLS-1$
                String path;
                if (url.startsWith("\"") && url.endsWith("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
                    path = url.substring(1, url.length() - 1);
                } else {
                    path = url;
                }
                String absolute = att(hyperlinkEle, "Absolute"); //$NON-NLS-1$
                url = FilePathParser.toURI(path,
                        absolute != null && !Boolean.parseBoolean(absolute));
            }
            topic.setHyperlink(url);
        }
    }

    private static Pattern getOIdPattern() {
        if (OID_PATTERN == null) {
            OID_PATTERN = Pattern.compile("@OId='([^']*)'"); //$NON-NLS-1$
        }
        return OID_PATTERN;
    }

    private void loadTopicShape(Element topicEle, ITopic topic)
            throws InterruptedException {
        checkInterrupted();
        Element shapeEle = child(topicEle, "ap:SubTopicShape"); //$NON-NLS-1$
        if (shapeEle == null)
            return;

        String shape = parseTopicShape(att(shapeEle, "SubTopicShape")); //$NON-NLS-1$
        registerStyle(topic, Styles.ShapeClass, shape);
    }

    private static String parseTopicShape(String shape) {
        return getMapping("topicShape", shape, null); //$NON-NLS-1$
    }

    private void loadImage(Element topicEle, ITopic topic)
            throws InterruptedException {
        checkInterrupted();
        Element oneImageEle = child(topicEle, "ap:OneImage"); //$NON-NLS-1$
        if (oneImageEle == null)
            return;

        Element imageEle = child(oneImageEle, "ap:Image"); //$NON-NLS-1$
        if (imageEle == null)
            return;

        String oId = att(imageEle, "OId"); //$NON-NLS-1$

        Element imageDataEle = child(imageEle, "ap:ImageData"); //$NON-NLS-1$
        if (imageDataEle == null)
            return;

        String uri = loadUri(imageDataEle);
        IFileEntry imgEntry = loadAttachment(oId, uri, "*.png"); //$NON-NLS-1$
        if (imgEntry == null)
            return;

        IImage image = topic.getImage();
        image.setSource(HyperlinkUtils.toAttachmentURL(imgEntry.getPath()));

        Element sizeEle = child(imageEle, "ap:ImageSize"); //$NON-NLS-1$
        if (sizeEle != null) {
            String width = att(sizeEle, "Width"); //$NON-NLS-1$
            String height = att(sizeEle, "Height"); //$NON-NLS-1$
            Float w = parseFloat(width);
            Float h = parseFloat(height);
            image.setSize(
                    w == null ? IImage.UNSPECIFIED : mm2Dots(w.floatValue()),
                    h == null ? IImage.UNSPECIFIED : mm2Dots(h.floatValue()));
        }

        String position = null;
        Element topicLayoutEle = child(topicEle, "ap:TopicLayout"); //$NON-NLS-1$
        if (topicLayoutEle != null) {
            position = att(topicLayoutEle, "TopicTextAndImagePosition"); //$NON-NLS-1$
        }
        image.setAlignment(parseImageAlign(position));
    }

    private IFileEntry loadAttachment(String oId, String uri,
            String proposalName) throws InterruptedException {
        checkInterrupted();
        if (uri == null)
            return null;

        if (idMap.containsKey(uri)) {
            String path = idMap.get(uri);
            if (path != null) {
                return getTargetWorkbook().getManifest().getFileEntry(path);
            }
            return null;
        }

        String path = null;
        if (uri.startsWith(MMARCH)) {
            String mmEntryPath = uri.substring(MMARCH.length());
//            ZipEntry mmEntry = sourceFile.getEntry(mmEntryPath);
//            if (mmEntry != null) {
            InputStream mmEntryStream = tempSource.getEntryStream(mmEntryPath);
            if (mmEntryStream == null) {
//                log(new FileNotFoundException(),
//                        "No such entry: " + mmEntryPath); //$NON-NLS-1$
                return null;
            }
//                try {
//                    InputStream in = sourceFile.getInputStream(mmEntry);
//                    in = new MonitoredInputStream(in, getMonitor());
            if (proposalName != null) {
                if (proposalName.startsWith("*.")) { //$NON-NLS-1$
                    String ext = proposalName.substring(1);
                    String oldName = new File(mmEntryPath).getName();
                    proposalName = FileUtils.getNoExtensionFileName(oldName)
                            + ext;
                }
            }
            InputStream in = new MonitoredInputStream(mmEntryStream,
                    getMonitor());
            try {
                IFileEntry entry = getTargetWorkbook().getManifest()
                        .createAttachmentFromStream(in, proposalName);
                path = entry.getPath();
            } catch (IOException e) {
                log(e, "Failed to create attachment from: " //$NON-NLS-1$
                        + mmEntryPath);
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        idMap.put(uri, path);
        return getTargetWorkbook().getManifest().getFileEntry(path);
    }

    private static String parseImageAlign(String position) {
        return getMapping("imageAlignment", position, IImage.RIGHT); //$NON-NLS-1$
    }

    private static String loadUri(Element ele) {
        if (ele != null) {
            Element uriEle = child(ele, "cor:Uri"); //$NON-NLS-1$
            if (uriEle != null) {
                return uriEle.getTextContent();
            }
        }
        return null;
    }

    private void loadTopicViewGroup(Element topicEle, ITopic topic)
            throws InterruptedException {
        checkInterrupted();
        Element viewGroupEle = child(topicEle, "ap:TopicViewGroup"); //$NON-NLS-1$
        if (viewGroupEle != null) {
            Element collapsedEle = child(viewGroupEle, "ap:Collapsed"); //$NON-NLS-1$
            if (collapsedEle != null) {
                String c = att(collapsedEle, "Collapsed"); //$NON-NLS-1$
                if (Boolean.parseBoolean(c)) {
                    topic.setFolded(true);
                }
            }
        }
    }

    private void loadPosition(Element topicEle, ITopic topic)
            throws InterruptedException {
        checkInterrupted();
        Element offsetEle = child(topicEle, "ap:Offset"); //$NON-NLS-1$
        if (offsetEle != null) {
            String cx = att(offsetEle, "CX"); //$NON-NLS-1$
            String cy = att(offsetEle, "CY"); //$NON-NLS-1$
            Float x = parseFloat(cx);
            Float y = parseFloat(cy);
            if (x != null && y != null && x.floatValue() != 0
                    && y.floatValue() != 0) {
                topic.setPosition(mm2Dots(x), mm2Dots(y));
            }
        }
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

    private void loadTask(Element ownerEle, ITopic topic)
            throws InterruptedException {
        checkInterrupted();
        Element taskEle = child(ownerEle, "ap:Task"); //$NON-NLS-1$
        if (taskEle == null)
            return;

        ITopicExtensionElement taskContent = null;
        String startDate = att(taskEle, "StartDate"); //$NON-NLS-1$
        if (startDate != null) {
            Matcher m = DATE_PATTERN.matcher(startDate);
            if (m.find()) {
                taskContent = ensureTaskContent(topic, taskContent);
                taskContent.deleteChildren("start-date"); //$NON-NLS-1$
                ITopicExtensionElement ele = taskContent
                        .createChild("start-date"); //$NON-NLS-1$
                ele.setTextContent(m.group(1) + " " + m.group(5)); //$NON-NLS-1$
            }
        }

        String endDate = att(taskEle, "DeadlineDate"); //$NON-NLS-1$;
        if (endDate != null) {
            Matcher m = DATE_PATTERN.matcher(endDate);
            if (m.find()) {
                taskContent = ensureTaskContent(topic, taskContent);
                taskContent.deleteChildren("end-date");//$NON-NLS-1$
                ITopicExtensionElement ele = taskContent
                        .createChild("end-date"); //$NON-NLS-1$
                ele.setTextContent(m.group(1) + " " + m.group(5)); //$NON-NLS-1$

            }
        }

        String resources = att(taskEle, "Resources"); //$NON-NLS-1$
        if (resources != null) {
            String[] resourceArray = resources.split("; "); //$NON-NLS-1$
            taskContent = ensureTaskContent(topic, taskContent);
            taskContent.deleteChildren("assigned-to");//$NON-NLS-1$
            ITopicExtensionElement ele = taskContent.createChild("assigned-to"); //$NON-NLS-1$
            ele.setTextContent(resources);
            for (String resource : resourceArray) {
                topic.addLabel(NLS.bind(
                        ImportMessages.MindManagerImporter_ResourceLabel,
                        resource.replaceAll(",", ";"))); //$NON-NLS-1$//$NON-NLS-2$
            }
        }

        String durationHours = att(taskEle, "DurationHours"); //$NON-NLS-1$
        if (durationHours != null) {
            String durationLabel = null;
            String durationUnit = att(taskEle, "DurationUnit"); //$NON-NLS-1$
            if (durationUnit != null) {
                try {
                    int hours = Integer.parseInt(durationHours);
                    if ("urn:mindjet:Month".equals(durationUnit)) { //$NON-NLS-1$
                        durationLabel = NLS.bind(
                                ImportMessages.MindManagerImporter_Months,
                                hours / 160);
                    } else if ("urn:mindjet:Week".equals(durationUnit)) { //$NON-NLS-1$
                        durationLabel = NLS.bind(
                                ImportMessages.MindManagerImporter_Weeks,
                                hours / 40);
                    } else if ("urn:mindjet:Day".equals(durationUnit)) { //$NON-NLS-1$
                        durationLabel = NLS.bind(
                                ImportMessages.MindManagerImporter_Days,
                                hours / 8);
                    }
                } catch (NumberFormatException e) {
                }
            }
            if (durationLabel == null) {
                durationLabel = NLS.bind(
                        ImportMessages.MindManagerImporter_Hours,
                        durationHours);
            }
            topic.addLabel(
                    NLS.bind(ImportMessages.MindManagerImporter_DurationLabel,
                            durationLabel));
        }

        addCheckPoint(ensureTaskContent(topic, taskContent),
                att(taskEle, "Milestone")); //$NON-NLS-1$
    }

    private static ITopicExtensionElement ensureTaskContent(ITopic topic,
            ITopicExtensionElement taskContent) {
        if (taskContent != null)
            return taskContent;
        ITopicExtension ext = topic.createExtension("org.xmind.ui.taskInfo"); //$NON-NLS-1$
        return ext.getContent();
    }

    private void loadTitle(Element ownerEle, ITitled titleOwner)
            throws InterruptedException {
        checkInterrupted();
        Element textEle = child(ownerEle, "ap:Text"); //$NON-NLS-1$
        String title = textEle == null ? null : att(textEle, "PlainText"); //$NON-NLS-1$
        if (title == null && titleOwner instanceof ITopic) {
            title = ImporterUtils.getDefaultTopicTitle((ITopic) titleOwner);
        }
        if (title != null) {
            titleOwner.setTitleText(title);
        }

        if (textEle != null && titleOwner instanceof IStyled) {
            Element fontEle = child(textEle, "ap:Font"); //$NON-NLS-1$
            if (fontEle != null) {
                loadFont(fontEle, (IStyled) titleOwner);
            }
            String align = textEle == null ? null
                    : att(textEle, "TextAlignment"); //$NON-NLS-1$
            if (align != null) {
                loadTextAlignment(align, (IStyled) titleOwner);
            }
        }
    }

    private void loadTextAlignment(String align, IStyled styleOwner) {
        if (align.startsWith("urn:mindjet:")) { //$NON-NLS-1$
            int len = "urn:mindjet:".length(); //$NON-NLS-1$
            String textAlign = align.substring(len).toLowerCase();
            registerStyle(styleOwner, Styles.TextAlign, textAlign);
        }
    }

    private void loadFont(Element fontEle, IStyled styleOwner)
            throws InterruptedException {
        checkInterrupted();
        registerStyle(styleOwner, Styles.TextColor,
                parseColor(att(fontEle, "Color"))); //$NON-NLS-1$

        String fontName = att(fontEle, "Name"); //$NON-NLS-1$
        String availableFontName = FontUtils.getAAvailableFontNameFor(fontName);
        fontName = availableFontName != null ? availableFontName : fontName;
        registerStyle(styleOwner, Styles.FontFamily, fontName);

        registerStyle(styleOwner, Styles.FontSize,
                parseFontSize(att(fontEle, "Size"))); //$NON-NLS-1$
        registerStyle(styleOwner, Styles.FontWeight,
                Boolean.parseBoolean(att(fontEle, "Bold")) //$NON-NLS-1$
                        ? Styles.FONT_WEIGHT_BOLD : null);
        registerStyle(styleOwner, Styles.FontStyle,
                Boolean.parseBoolean(att(fontEle, "Italic")) //$NON-NLS-1$
                        ? Styles.FONT_STYLE_ITALIC : null);
        String textDecoration = StyleUtils.toTextDecoration(
                Boolean.parseBoolean(att(fontEle, "Underline")), Boolean //$NON-NLS-1$
                        .parseBoolean(att(fontEle, "Strikethrough"))); //$NON-NLS-1$
        registerStyle(styleOwner, Styles.TextDecoration, textDecoration);
    }

    private static String parseFontSize(String size) {
        if (size != null) {
            try {
                double value = Double.parseDouble(size);
                size = StyleUtils.addUnitPoint((int) value);
            } catch (Throwable e) {
            }
        }
        return size;
    }

    private void loadColor(Element parentEle, IStyled host, boolean transparent)
            throws InterruptedException {
        checkInterrupted();
        Element colorEle = child(parentEle, "ap:Color"); //$NON-NLS-1$
        if (colorEle != null) {
            String fillColor = att(colorEle, "FillColor"); //$NON-NLS-1$
            String opacity = parseAlpha(fillColor);
            if (opacity != null && !opacity.equals(TRANSPARENT_VALUE))
                registerStyle(host, Styles.FillColor, parseColor(fillColor));
            if (transparent) {
                registerStyle(host, Styles.Opacity, opacity);
            }

            String lineColor = att(colorEle, "LineColor"); //$NON-NLS-1$
            registerStyle(host, Styles.LineColor, parseColor(lineColor));
        }
    }

    private IStyleSheet getTempStyleSheet() {
        if (tempStyleSheet == null) {
            tempStyleSheet = Core.getStyleSheetBuilder().createStyleSheet();
            ((StyleSheetImpl) tempStyleSheet)
                    .setManifest(getTargetWorkbook().getManifest());
        }
        return tempStyleSheet;
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

    private static String parseAlpha(String mmColor) {
        if (mmColor != null) {
            try {
                int alpha = Integer.parseInt(mmColor.substring(0, 1), 16);
                double opacity = ((double) alpha) * 100 / 255;
                return String.format("%.2f", opacity); //$NON-NLS-1$
            } catch (Throwable t) {
            }
        }
        return null;
    }

    private static String parseColor(String mmColor) {
        if (mmColor != null) {
            int r;
            int g;
            int b;
            try {
                r = Integer.parseInt(mmColor.substring(2, 4), 16);
                g = Integer.parseInt(mmColor.substring(4, 6), 16);
                b = Integer.parseInt(mmColor.substring(6, 8), 16);
                return ColorUtils.toString(r, g, b);
            } catch (Throwable t) {
            }
        }
        return null;
    }

    private void loadOId(Element mmEle, IIdentifiable element) {
        String OId = att(mmEle, "OId"); //$NON-NLS-1$
        if (OId != null) {
            idMap.put(OId, element.getId());
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
            String id = idMap.get(en.getKey());
            if (id != null) {
                for (ITopic topic : en.getValue()) {
                    topic.setHyperlink(HyperlinkUtils.toInternalURL(id));
                }
            }
        }
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
        if (mappings == null) {
            mappings = createMappings();
        }
        return mappings;
    }

    private static ResourceMappingManager createMappings() {
        return MMResourceMappingManager.getInstance();
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
