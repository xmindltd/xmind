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

import static org.xmind.core.internal.dom.DOMConstants.ATTR_END1;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_END2;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_HREF;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_ID;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_MARKER_ID;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_RESOURCE_ID;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_SRC;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_STYLE_ID;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_THEME;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_TOPIC_ID;
import static org.xmind.core.internal.dom.DOMConstants.TAG_IMG;
import static org.xmind.core.internal.dom.DOMConstants.TAG_MARKER_REF;
import static org.xmind.core.internal.dom.DOMConstants.TAG_PROPERTIES;
import static org.xmind.core.internal.dom.DOMConstants.TAG_RELATIONSHIP;
import static org.xmind.core.internal.dom.DOMConstants.TAG_RESOURCE_REF;
import static org.xmind.core.internal.dom.DOMConstants.TAG_SUMMARY;
import static org.xmind.core.internal.dom.DOMConstants.TAG_TOPIC;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xmind.core.Core;
import org.xmind.core.IAdaptable;
import org.xmind.core.ICloneData;
import org.xmind.core.IFileEntry;
import org.xmind.core.IImage;
import org.xmind.core.IManifest;
import org.xmind.core.IResourceRef;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.IWorkbookComponent;
import org.xmind.core.internal.CloneData;
import org.xmind.core.internal.ICloneDataListener;
import org.xmind.core.internal.zip.ArchiveConstants;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerResource;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyled;
import org.xmind.core.util.DOMUtils;
import org.xmind.core.util.FileUtils;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.core.util.Property;

public class WorkbookUtilsImpl {

    private WorkbookUtilsImpl() {
    }

    public static ICloneData clone(IWorkbook targetWorkbook,
            Collection<? extends Object> sources, ICloneData prevResult) {
        CloneData result = new CloneData(sources, prevResult);
        for (Object source : sources) {
            if (result.get(source) == null) {
                Object cloned;
                if (source instanceof IImage) {
                    cloned = doClone((WorkbookImpl) targetWorkbook,
                            ((IImage) source).getParent(), result);
                    if (cloned instanceof ITopic) {
                        cloned = ((ITopic) cloned).getImage();
                    }
                } else {
                    cloned = doClone((WorkbookImpl) targetWorkbook, source,
                            result);
                }
                if (cloned != null) {
                    result.put(source, cloned);
                }
            }
        }
        return result;
    }

    private static Object doClone(WorkbookImpl targetWorkbook, Object source,
            CloneData data) {
        if (source instanceof IAdaptable) {
            if (source instanceof IWorkbookComponent) {
                Node sourceNode = (Node) ((IAdaptable) source)
                        .getAdapter(Node.class);
                if (sourceNode != null) {
                    return cloneWorkbookComponent(targetWorkbook, sourceNode,
                            (IWorkbookComponent) source, data);
                }
            } else if (source instanceof IMarker) {
                IMarker sourceMarker = (IMarker) source;
                return cloneMarker(targetWorkbook,
                        (MarkerSheetImpl) targetWorkbook.getMarkerSheet(),
                        sourceMarker.getId(), sourceMarker,
                        sourceMarker.getOwnedSheet(), data);
            }
            return null;
        } else {
            return source;
        }
    }

    private static Object cloneWorkbookComponent(WorkbookImpl targetWorkbook,
            Node sourceEle, IWorkbookComponent source, CloneData data) {
        Document doc = targetWorkbook.getImplementation();
        Node clonedEle = clone(doc, sourceEle);
        if (clonedEle instanceof Element) {
            replaceAttributes(targetWorkbook, (Element) clonedEle,
                    source.getOwnedWorkbook(), data);
        }
        return targetWorkbook.getAdaptableRegistry().getAdaptable(clonedEle);
    }

    private static Node clone(Document doc, Node source) {
        if (source.getOwnerDocument() == doc) {
            return source.cloneNode(true);
        }
        return doc.importNode(source, true);
    }

    private static void replaceAttributes(WorkbookImpl targetWorkbook,
            Element ele, IWorkbook sourceWorkbook, CloneData data) {
        if (ele.hasAttribute(ATTR_ID)) {
            replaceId(ele, data, ICloneData.WORKBOOK_COMPONENTS);
        }

        for (Iterator<Element> it = DOMUtils.childElementIter(ele); it
                .hasNext();) {
            replaceAttributes(targetWorkbook, it.next(), sourceWorkbook, data);
        }

        String tag = ele.getTagName();
        if (TAG_SUMMARY.equals(tag)) {
            replaceTopicRef(ele, ATTR_TOPIC_ID, data);
        } else if (TAG_RELATIONSHIP.equals(tag)) {
            replaceTopicRef(ele, ATTR_END1, data);
            replaceTopicRef(ele, ATTR_END2, data);
        } else if (TAG_TOPIC.equals(tag)) {
            replaceTopicHyperlink(targetWorkbook, ele, sourceWorkbook, data);
            //replaceTopicImageUrl(targetWorkbook, ele, sourceWorkbook, data);
            //replaceNotes(targetWorkbook, ele, sourceWorkbook, data);
        } else if (TAG_MARKER_REF.equals(tag)) {
            replaceMarkerRef(targetWorkbook, ele, sourceWorkbook, data);
        } else if (TAG_IMG.equals(tag)) {
            replaceImageUrl(targetWorkbook, ele, sourceWorkbook, data);
        } else if (TAG_RESOURCE_REF.equals(tag)) {
            replaceResourceId(targetWorkbook, ele, sourceWorkbook, data);
        }

        if (ele.hasAttribute(ATTR_STYLE_ID)) {
            replaceStyle(targetWorkbook, ele, ATTR_STYLE_ID, sourceWorkbook,
                    data);
        }
        if (ele.hasAttribute(ATTR_THEME)) {
            replaceStyle(targetWorkbook, ele, ATTR_THEME, sourceWorkbook, data);
        }

        NamedNodeMap attributes = ele.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node item = attributes.item(i);
            if (item.getNodeName().endsWith("-id")) { //$NON-NLS-1$
                replaceExtensionWithIdAttr(targetWorkbook, ele,
                        item.getNodeName(), sourceWorkbook, data);
            }
        }
    }

    private static void replaceResourceId(WorkbookImpl targetWorkbook,
            Element ele, IWorkbook sourceWorkbook, CloneData data) {
        String type = DOMUtils.getAttribute(ele, DOMConstants.ATTR_TYPE);
        if (IResourceRef.FILE_ENTRY.equals(type)) {
            String sourceEntryPath = DOMUtils.getAttribute(ele,
                    ATTR_RESOURCE_ID);
            if (sourceEntryPath != null) {
                try {
                    String targetEntryPath = InternalHyperlinkUtils
                            .importAttachment(sourceEntryPath, sourceWorkbook,
                                    targetWorkbook);
                    DOMUtils.setAttribute(ele, ATTR_RESOURCE_ID,
                            targetEntryPath);
                } catch (IOException e) {
                }
            }
        }
    }

    private static void replaceId(Element ele, CloneData data,
            String category) {
        String oldId = DOMUtils.getAttribute(ele, ATTR_ID);
        String newId = Core.getIdFactory().createId();
        DOMUtils.replaceId(ele, newId);
        if (oldId != null && data.getString(category, oldId) == null) {
            data.putString(category, oldId, newId);
        }
    }

    private static void replaceTopicRef(final Element e, final String attrName,
            final CloneData data) {
        String oldRefId = DOMUtils.getAttribute(e, attrName);
        if (oldRefId == null)
            return;

        String newRefId = data.getString(ICloneData.WORKBOOK_COMPONENTS,
                oldRefId);
        if (newRefId == null) {
            data.addCloneDataListener(ICloneData.WORKBOOK_COMPONENTS, oldRefId,
                    new ICloneDataListener() {
                        public void objectCloned(Object source, Object cloned) {
                        }

                        public void stringCloned(String category, String source,
                                String cloned) {
                            DOMUtils.setAttribute(e, attrName, cloned);
                            data.removeCloneDataListener(source, this);
                        }
                    });
        } else {
            DOMUtils.setAttribute(e, attrName, newRefId);
        }
    }

//    private static void replaceStyle(WorkbookImpl targetWorkbook,
//            final Element styledEle, IWorkbook sourceWorkbook,
//            final CloneData data) {
//        replaceStyle(targetWorkbook, styledEle, ATTR_STYLE_ID, sourceWorkbook,
//                data);
//    }
//
    private static void replaceStyle(WorkbookImpl targetWorkbook,
            final Element styledEle, String styleTag, IWorkbook sourceWorkbook,
            final CloneData data) {
        String styleId = styledEle.getAttribute(styleTag);
        IStyle sourceStyle = sourceWorkbook.getStyleSheet().findStyle(styleId);
        String sourceStyleId = sourceStyle == null ? null : sourceStyle.getId();
        String targetStyleId;
        if (sourceStyle != null) {
            IStyle targetStyle = importStyle(
                    (StyleSheetImpl) targetWorkbook.getStyleSheet(),
                    (StyleImpl) sourceStyle,
                    (StyleSheetImpl) sourceWorkbook.getStyleSheet(), data);
            targetStyleId = targetStyle == null ? null : targetStyle.getId();
        } else {
            IStyle targetStyle = (IStyle) data.get(sourceStyle);
            targetStyleId = targetStyle == null ? null : targetStyle.getId();
        }
        if (targetStyleId == null) {
            styledEle.removeAttribute(styleTag);
        } else {
            styledEle.setAttribute(styleTag, targetStyleId);
        }
        if (sourceStyleId != null && targetStyleId != null) {
            data.putString(ICloneData.STYLESHEET_COMPONENTS, sourceStyleId,
                    targetStyleId);
        }

//        String oldStyleId = DOMUtils.getAttribute(styledEle, styleTag);
//        if (oldStyleId == null)
//            return;
//
//        String newStyleId = data.getString(oldStyleId);
//        if (newStyleId != null || data.isCloned(oldStyleId)) {
//            DOMUtils.setAttribute(styledEle, styleTag, newStyleId);
//            return;
//        }
//
//        IStyleSheet sourceStyleSheet = sourceWorkbook.getStyleSheet();
//        IStyle sourceStyle = sourceStyleSheet.findStyle(oldStyleId);
//        if (sourceStyle != null) {
//            Properties sourceContents = getCachedSourceStyleContents(
//                    sourceStyle, targetWorkbook, sourceWorkbook, data);
//            if (!sourceContents.isEmpty()) {
//                IStyleSheet targetStyleSheet = targetWorkbook.getStyleSheet();
//                IStyle targetStyle = findSimilarStyle(sourceContents,
//                        targetStyleSheet, data);
//                if (targetStyle == null) {
//                    targetStyle = createStyle(sourceContents, sourceStyle
//                            .getType(), targetStyleSheet, targetWorkbook,
//                            sourceWorkbook, data);
//                }
//                newStyleId = targetStyle.getId();
//            }
//        }
//        DOMUtils.setAttribute(styledEle, styleTag, newStyleId);
//        data.put(oldStyleId, newStyleId);
    }

//    private static IStyle createStyle(Properties contents, String styleType,
//            IStyleSheet targetStyleSheet, WorkbookImpl targetWorkbook,
//            IWorkbook sourceWorkbook, CloneData data) {
//        IStyle newStyle = targetStyleSheet.createStyle(styleType);
//        Enumeration<?> keys = contents.propertyNames();
//        while (keys.hasMoreElements()) {
//            String key = (String) keys.nextElement();
//            String value = contents.getProperty(key);
//            if (HyperlinkUtils.isAttachmentURL(value)) {
//                value = cloneUrl(targetWorkbook, value, sourceWorkbook, data);
//            }
//            newStyle.setProperty(key, value);
//        }
//        targetStyleSheet.addStyle(newStyle, IStyleSheet.NORMAL_STYLES);
//        return newStyle;
//    }
//
//    private static IStyle findSimilarStyle(Properties sourceContents,
//            IStyleSheet targetStyleSheet, CloneData data) {
//        for (IStyle style : targetStyleSheet.getAllStyles()) {
//            Properties targetContents = getCachedStyleContents(style, data);
//            if (targetContents.equals(sourceContents))
//                return style;
//        }
//        return null;
//    }
//
//    private static Properties getCachedSourceStyleContents(IStyle style,
//            WorkbookImpl targetWorkbook, IWorkbook sourceWorkbook,
//            CloneData data) {
//        Properties map = (Properties) data.getCache(style);
//        if (map == null) {
//            map = new Properties();
//            Iterator<Property> it = style.properties();
//            while (it.hasNext()) {
//                Property p = it.next();
//                map.setProperty(p.key, p.value);
//            }
//            data.cache(style, map);
//        }
//        return map;
//    }
//
//    private static Properties getCachedStyleContents(IStyle style,
//            CloneData data) {
//        Properties map = (Properties) data.getCache(style);
//        if (map == null) {
//            map = new Properties();
//            Iterator<Property> it = style.properties();
//            while (it.hasNext()) {
//                Property p = it.next();
//                map.setProperty(p.key, p.value);
//            }
//            data.cache(style, map);
//        }
//        return map;
//    }

//    private static String cloneUrl(WorkbookImpl targetWorkbook,
//            String sourceUrl, IWorkbook sourceWorkbook, CloneData data) {
//        String clonedUrl = data.getString(sourceUrl);
//        if (clonedUrl != null || data.isCloned(sourceUrl))
//            return clonedUrl;
//
//        if (HyperlinkUtils.isAttachmentURL(sourceUrl)) {
//            try {
//                clonedUrl = InternalHyperlinkUtils.importAttachmentURL(
//                        sourceUrl, sourceWorkbook, targetWorkbook);
//            } catch (IOException e) {
//            }
//        } else if (HyperlinkUtils.isInternalURL(sourceUrl)) {
//            String sourceId = HyperlinkUtils.toElementID(sourceUrl);
//
//        } else {
//            clonedUrl = sourceUrl;
//        }
//
//        data.put(sourceUrl, clonedUrl);
//        return clonedUrl;
//    }

    private static void replaceHyperlink(WorkbookImpl targetWorkbook,
            final Element ele, final String attr, IWorkbook sourceWorkbook,
            final CloneData data) {
        final String sourceUrl = DOMUtils.getAttribute(ele, attr);
        if (sourceUrl != null) {
            String clonedUrl = data.getString(ICloneData.URLS, sourceUrl);
            boolean async = false;
            if (clonedUrl == null
                    && !data.isCloned(ICloneData.URLS, sourceUrl)) {
                if (HyperlinkUtils.isAttachmentURL(sourceUrl)) {
                    try {
                        clonedUrl = InternalHyperlinkUtils.importAttachmentURL(
                                sourceUrl, sourceWorkbook, targetWorkbook);
                    } catch (IOException e) {
                    }
                } else if (HyperlinkUtils.isInternalURL(sourceUrl)) {
                    String sourceId = HyperlinkUtils.toElementID(sourceUrl);
                    if (!data.isCloned(ICloneData.WORKBOOK_COMPONENTS,
                            sourceId)) {
                        async = true;
                        data.addCloneDataListener(
                                ICloneData.WORKBOOK_COMPONENTS, sourceId,
                                new ICloneDataListener() {
                                    public void objectCloned(Object source,
                                            Object cloned) {
                                    }

                                    public void stringCloned(String category,
                                            String source, String cloned) {
                                        String targetUrl = cloned == null ? null
                                                : HyperlinkUtils
                                                        .toInternalURL(cloned);
                                        data.putString(ICloneData.URLS,
                                                sourceUrl, targetUrl);
                                        DOMUtils.setAttribute(ele, attr,
                                                targetUrl);
                                        data.removeCloneDataListener(source,
                                                this);
                                    }
                                });
                    } else {
                        String targetId = data.getString(
                                ICloneData.WORKBOOK_COMPONENTS, sourceId);
                        clonedUrl = HyperlinkUtils.toInternalURL(targetId);
                    }
                } else {
                    clonedUrl = sourceUrl;
                }
                if (!async)
                    data.putString(ICloneData.URLS, sourceUrl, clonedUrl);
            }
            if (!async)
                DOMUtils.setAttribute(ele, attr, clonedUrl);
        }
    }

    private static void replaceTopicHyperlink(WorkbookImpl targetWorkbook,
            Element topicEle, IWorkbook sourceWorkbook, CloneData data) {
        replaceHyperlink(targetWorkbook, topicEle, ATTR_HREF, sourceWorkbook,
                data);
//        String oldUrl = DOMUtils.getAttribute(topicEle, ATTR_HREF);
//        if (oldUrl != null) {
//            String newUrl = cloneUrl(targetWorkbook, oldUrl, sourceWorkbook,
//                    data);
//            DOMUtils.setAttribute(topicEle, ATTR_HREF, newUrl);
//        }
    }

    private static void replaceExtensionWithIdAttr(WorkbookImpl targetWorkbook,
            final Element ele, final String attr, IWorkbook sourceWorkbook,
            CloneData data) {
        String sourceId = DOMUtils.getAttribute(ele, attr);

        if (sourceId != null) {
            if (!data.isCloned(ICloneData.WORKBOOK_COMPONENTS, sourceId)) {
                data.addCloneDataListener(ICloneData.WORKBOOK_COMPONENTS,
                        sourceId, new ICloneDataListener() {
                            public void stringCloned(String category,
                                    String source, String cloned) {
                                DOMUtils.setAttribute(ele, attr, cloned);
                            }

                            public void objectCloned(Object source,
                                    Object cloned) {
                            }
                        });
            } else {
                String targetId = null;
                targetId = data.getString(ICloneData.WORKBOOK_COMPONENTS,
                        sourceId);
                if (targetId == null) {
                    ele.removeAttribute(attr);
                } else {
                    ele.setAttribute(attr, targetId);
                }
            }
        }

    }

    private static void replaceImageUrl(WorkbookImpl targetWorkbook,
            Element imgEle, IWorkbook sourceWorkbook, CloneData data) {
        replaceHyperlink(targetWorkbook, imgEle, ATTR_SRC, sourceWorkbook,
                data);
//        String oldUrl = DOMUtils.getAttribute(imgEle, ATTR_SRC);
//        if (oldUrl != null) {
//            String newUrl = cloneUrl(targetWorkbook, oldUrl, sourceWorkbook,
//                    data);
//            DOMUtils.setAttribute(imgEle, ATTR_SRC, newUrl);
//        }
    }

    private static void replaceMarkerRef(WorkbookImpl targetWorkbook,
            Element ele, IWorkbook sourceWorkbook, CloneData data) {
        String oldMarkerId = DOMUtils.getAttribute(ele, ATTR_MARKER_ID);
        if (oldMarkerId != null) {
            String newMarkerId = cloneMarkerId(targetWorkbook, oldMarkerId,
                    sourceWorkbook, data);
            DOMUtils.setAttribute(ele, ATTR_MARKER_ID, newMarkerId);
        }
    }

    private static String cloneMarkerId(WorkbookImpl targetWorkbook,
            String sourceMarkerId, IWorkbook sourceWorkbook, CloneData data) {
        String clonedMarkerId = data
                .getString(ICloneData.MARKERSHEET_COMPONENTS, sourceMarkerId);
        if (clonedMarkerId != null || data
                .isCloned(ICloneData.MARKERSHEET_COMPONENTS, sourceMarkerId))
            return clonedMarkerId;

        //When id equals,adopt override strategy.

        IMarkerSheet sourceMarkerSheet = sourceWorkbook.getMarkerSheet();
        MarkerSheetImpl targetMarkerSheet = (MarkerSheetImpl) targetWorkbook
                .getMarkerSheet();
        targetMarkerSheet.importFrom(sourceMarkerSheet);

//        IMarkerSheet sourceMarkerSheet = sourceWorkbook.getMarkerSheet();
//        IMarker sourceMarker = sourceMarkerSheet.getMarker(sourceMarkerId);
//        if (sourceMarker != null) {
//            MarkerSheetImpl targetMarkerSheet = (MarkerSheetImpl) targetWorkbook
//                    .getMarkerSheet();
//            IMarker existingMarker = targetMarkerSheet
//                    .getMarker(sourceMarkerId);
//            if (existingMarker == null) {
//                IMarker clonedMarker = cloneMarker(targetWorkbook,
//                        targetMarkerSheet, sourceMarkerId, sourceMarker,
//                        sourceMarkerSheet, data);
//                if (clonedMarker != null) {
//                    clonedMarkerId = clonedMarker.getId();
//                }
//            }
//        }
        if (clonedMarkerId == null) {
            clonedMarkerId = sourceMarkerId;
        }
        data.putString(ICloneData.MARKERSHEET_COMPONENTS, sourceMarkerId,
                clonedMarkerId);
        return clonedMarkerId;
    }

    private static IMarker cloneMarker(WorkbookImpl targetWorkbook,
            MarkerSheetImpl targetMarkerSheet, String sourceMarkerId,
            IMarker sourceMarker, IMarkerSheet sourceMarkerSheet,
            CloneData data) {
        if (!sourceMarkerSheet.isPermanent()) {
            IMarkerGroup sourceGroup = sourceMarker.getParent();
            if (sourceGroup != null) {
                String sourceGroupId = sourceGroup.getId();
                String clonedGroupId = data.getString(
                        ICloneData.MARKERSHEET_COMPONENTS, sourceGroupId);
                if (clonedGroupId == null
                        && !data.isCloned(ICloneData.MARKERSHEET_COMPONENTS,
                                sourceGroupId)) {
                    IMarkerGroup targetGroup = targetMarkerSheet
                            .getMarkerGroup(sourceGroupId);
                    if (targetGroup != null && targetMarkerSheet
                            .equals(targetGroup.getOwnedSheet())) {
                        data.putString(ICloneData.MARKERSHEET_COMPONENTS,
                                sourceGroupId, sourceGroupId);
                    } else {
                        cloneMarkerGroup(targetWorkbook, targetMarkerSheet,
                                sourceMarker, sourceGroup, sourceMarkerSheet,
                                data);
                    }
                    String clonedMarkerId = data.getString(
                            ICloneData.MARKERSHEET_COMPONENTS, sourceMarkerId);
                    if (clonedMarkerId != null) {
                        return targetMarkerSheet.getMarker(clonedMarkerId);
                    }
                    IMarker targetMarker = targetMarkerSheet
                            .getMarker(sourceMarkerId);
                    if (targetMarker == null) {
                        //TODO clone missing marker
                    }
                }
            }
        }
        return sourceMarker;
    }

    private static void cloneMarkerGroup(WorkbookImpl targetWorkbook,
            MarkerSheetImpl targetMarkerSheet, IMarker sourceMarker,
            IMarkerGroup sourceGroup, IMarkerSheet sourceMarkerSheet,
            CloneData data) {
        MarkerGroupImpl targetGroup;

        IMarkerGroup existingGroup = targetMarkerSheet
                .getMarkerGroup(sourceGroup.getId());
        if (existingGroup != null
                && targetMarkerSheet.equals(existingGroup.getOwnedSheet())) {
            targetGroup = (MarkerGroupImpl) existingGroup;
            //TODO clone ungrouped markers
        } else {
            Node sourceGroupNode = (Node) sourceGroup.getAdapter(Node.class);
            if (sourceGroupNode != null) {
                Node clonedGroupNode = targetMarkerSheet.getImplementation()
                        .importNode(sourceGroupNode, true);
                replaceMarkerPath(targetWorkbook, targetMarkerSheet,
                        clonedGroupNode, data);
                MarkerGroupImpl clonedGroup = (MarkerGroupImpl) targetMarkerSheet
                        .getElementAdapter(clonedGroupNode);
                transferMarkerResources(targetWorkbook, targetMarkerSheet,
                        clonedGroup, sourceGroup, sourceMarkerSheet, data);
                targetGroup = clonedGroup;
            } else {
                targetGroup = (MarkerGroupImpl) targetMarkerSheet
                        .createMarkerGroup(sourceGroup.isSingleton());
                cloneMarkerGroup(targetWorkbook, targetMarkerSheet, targetGroup,
                        sourceGroup, sourceMarkerSheet, data);
            }
        }
        data.putString(ICloneData.MARKERSHEET_COMPONENTS, sourceGroup.getId(),
                targetGroup.getId());
    }

    private static void transferMarkerResources(WorkbookImpl targetWorkbook,
            MarkerSheetImpl targetMarkerSheet, MarkerGroupImpl targetGroup,
            IMarkerGroup sourceGroup, IMarkerSheet sourceMarkerSheet,
            CloneData data) {
        for (IMarker targetMarker : targetGroup.getMarkers()) {
            String markerId = targetMarker.getId();
            IMarker sourceMarker = sourceMarkerSheet.getMarker(markerId);
            if (sourceMarker != null) {
                transferMarkerResource(targetWorkbook, sourceMarker,
                        targetMarker);
            }
            data.putString(ICloneData.MARKERSHEET_COMPONENTS, markerId,
                    markerId);
        }
    }

    private static void cloneMarkerGroup(WorkbookImpl targetWorkbook,
            MarkerSheetImpl targetMarkerSheet, MarkerGroupImpl targetGroup,
            IMarkerGroup sourceGroup, IMarkerSheet sourceMarkerSheet,
            CloneData data) {
        for (IMarker sourceMarker : sourceGroup.getMarkers()) {
            IMarker clonedMarker;
            String sourceMarkerId = sourceMarker.getId();
            IMarker existingMarker = targetMarkerSheet
                    .getMarker(sourceMarkerId);
            if (existingMarker != null && targetMarkerSheet
                    .equals(existingMarker.getOwnedSheet())) {
                clonedMarker = existingMarker;
            } else {
                clonedMarker = cloneMarker(targetWorkbook, targetMarkerSheet,
                        sourceMarkerSheet, sourceMarker, data);
                targetGroup.addMarker(clonedMarker);
            }
            data.putString(ICloneData.MARKERSHEET_COMPONENTS, sourceMarkerId,
                    clonedMarker.getId());
        }
    }

    private static IMarker cloneMarker(WorkbookImpl targetWorkbook,
            MarkerSheetImpl targetMarkerSheet, IMarkerSheet sourceMarkerSheet,
            IMarker sourceMarker, CloneData data) {
        IMarker clonedMarker;
        Node sourceMarkerNode = (Node) sourceMarker.getAdapter(Node.class);
        if (sourceMarkerNode != null) {
            Node clonedMarkerNode = targetMarkerSheet.getImplementation()
                    .importNode(sourceMarkerNode, true);
            replaceMarkerPath(targetWorkbook, targetMarkerSheet,
                    clonedMarkerNode, data);
            clonedMarker = (IMarker) targetMarkerSheet
                    .getElementAdapter(clonedMarkerNode);
        } else {
            clonedMarker = createSimilarMarker(targetWorkbook,
                    targetMarkerSheet, sourceMarker, sourceMarkerSheet, data);
        }
        transferMarkerResource(targetWorkbook, sourceMarker, clonedMarker);
        return clonedMarker;
    }

    private static void transferMarkerResource(WorkbookImpl targetWorkbook,
            IMarker sourceMarker, IMarker targetMarker) {
        IMarkerResource sourceResource = sourceMarker.getResource();
        if (sourceResource != null) {
            //IMarkerResource targetResource = targetMarker.getResource();
            try {
                InputStream is = sourceResource.getInputStream();
                if (is != null) {
                    try {
                        String targetPath = ArchiveConstants.PATH_MARKERS
                                + targetMarker.getResourcePath();
                        IFileEntry entry = targetWorkbook.getManifest()
                                .createFileEntry(targetPath);
                        OutputStream out = entry.getOutputStream();
                        if (out != null) {
                            FileUtils.transfer(is, out, true);
                        }
                    } finally {
                        is.close();
                    }
                }
            } catch (IOException e) {
                Core.getLogger().log(e,
                        "Failed to transfer marker resource from " //$NON-NLS-1$
                                + sourceMarker.getResourcePath() + " to " //$NON-NLS-1$
                                + targetMarker.getResourcePath());
            }
        }
    }

    private static void replaceMarkerPath(WorkbookImpl targetWorkbook,
            MarkerSheetImpl targetMarkerSheet, Node node, CloneData data) {
        if (node instanceof Element) {
            Element ele = (Element) node;
            if (DOMConstants.TAG_MARKER.equals(ele.getTagName())) {
                String clonedPath = createNewMarkerPath(
                        ele.getAttribute(DOMConstants.ATTR_RESOURCE));
                ele.setAttribute(DOMConstants.ATTR_RESOURCE, clonedPath);
            }
            Iterator<Element> it = DOMUtils.childElementIter(ele);
            while (it.hasNext()) {
                replaceMarkerPath(targetWorkbook, targetMarkerSheet, it.next(),
                        data);
            }
        }
    }

    private static String createNewMarkerPath(String sourcePath) {
        String clonedPath = Core.getIdFactory().createId()
                + FileUtils.getExtension(sourcePath);
        return clonedPath;
    }

    private static MarkerImpl createSimilarMarker(WorkbookImpl targetWorkbook,
            MarkerSheetImpl targetMarkerSheet, IMarker sourceMarker,
            IMarkerSheet sourceMarkerSheet, CloneData data) {
        MarkerImpl newMarker = (MarkerImpl) targetMarkerSheet.createMarker(
                createNewMarkerPath(sourceMarker.getResourcePath()));
        newMarker.setName(sourceMarker.getName());
        return newMarker;
    }

    public static void increaseStyleRef(WorkbookImpl workbook, IStyled styled) {
        if (workbook == null || styled == null)
            return;

        String styleId = styled.getStyleId();
        if (styleId == null)
            return;

        workbook.getStyleRefCounter().increaseRef(styleId);
    }

    public static void decreaseStyleRef(WorkbookImpl workbook, IStyled styled) {
        if (workbook == null || styled == null)
            return;

        String styleId = styled.getStyleId();
        if (styleId == null)
            return;

        workbook.getStyleRefCounter().decreaseRef(styleId);
    }

    public static IStyle importStyle(StyleSheetImpl targetSheet,
            StyleImpl sourceStyle, StyleSheetImpl sourceSheet) {
        return importStyle(targetSheet, sourceStyle, sourceSheet,
                new CloneData(Arrays.asList(sourceStyle), null));
    }

    public static IStyle importStyle(StyleSheetImpl targetSheet,
            StyleImpl sourceStyle, StyleSheetImpl sourceSheet, CloneData data) {
        if (sourceSheet != null && sourceSheet.equals(targetSheet))
            return sourceStyle;

        if (sourceSheet == null)
            return importNoParentStyle(targetSheet, sourceStyle);

        if (data == null)
            data = new CloneData(Arrays.asList(sourceStyle), null);
        StyleProperties sourceProp = getStyleProperties(sourceStyle, data);
        if (sourceProp.isEmpty())
            return null;

        String sourceGroup = sourceSheet.findOwnedGroup(sourceStyle);
        IStyle targetStyle = findSimilarStyle(targetSheet, sourceGroup,
                sourceProp, data);
        if (targetStyle != null)
            return targetStyle;

        cloneStyle(targetSheet, sourceStyle, sourceSheet, data);
        targetStyle = (IStyle) data.get(sourceStyle);

        if (targetStyle != null && sourceGroup != null) {
            targetSheet.addStyle(targetStyle, sourceGroup);
        }

        return targetStyle;
    }

    private static IStyle findSimilarStyle(StyleSheetImpl targetSheet,
            String group, StyleProperties sourceProp, CloneData data) {
        Set<IStyle> styles;
        if (group == null)
            styles = targetSheet.getAllStyles();
        else
            styles = targetSheet.getStyles(group);
        for (IStyle style : styles) {
            if (sourceProp.equals(getStyleProperties(style, data)))
                return style;
        }
        return null;
    }

    private static class StyleProperties {

        Map<String, String> properties = new HashMap<String, String>();

        Map<String, StyleProperties> defaultStyles = new HashMap<String, StyleProperties>();

        public StyleProperties(IStyle style, CloneData data) {
            if (style == null)
                return;
            Iterator<Property> propIt = style.properties();
            while (propIt.hasNext()) {
                Property next = propIt.next();
                properties.put(next.key, next.value);
            }
            Iterator<Property> dsIt = style.defaultStyles();
            while (dsIt.hasNext()) {
                Property next = dsIt.next();
                String family = next.key;
                IStyle ds = style.getDefaultStyleById(next.value);
                StyleProperties dsProp = (StyleProperties) data.getCache(ds);
                if (dsProp == null) {
                    dsProp = new StyleProperties(ds, data);
                }
                defaultStyles.put(family, dsProp);
            }
            data.cache(style, this);
        }

        public boolean isEmpty() {
            return properties.isEmpty() && defaultStyles.isEmpty();
        }

        public int hashCode() {
            return properties.hashCode() ^ defaultStyles.hashCode();
        }

        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj == null || !(obj instanceof StyleProperties))
                return false;
            StyleProperties that = (StyleProperties) obj;
            return this.properties.equals(that.properties)
                    && this.defaultStyles.equals(that.defaultStyles);
        }
    }

    private static StyleProperties getStyleProperties(IStyle style,
            CloneData data) {
        StyleProperties prop = (StyleProperties) data.getCache(style);
        if (prop == null) {
            prop = new StyleProperties(style, data);
        }
        return prop;
    }

    private static void cloneStyle(StyleSheetImpl targetSheet,
            StyleImpl sourceStyle, StyleSheetImpl sourceSheet, CloneData data) {
        Element sourceEle = sourceStyle.getImplementation();
        Node targetEle = clone(targetSheet.getImplementation(), sourceEle);
        if (targetEle instanceof Element) {
            IStyle sameIdStyle = targetSheet.findStyle(sourceStyle.getId());
            if (sameIdStyle != null) {
                DOMUtils.replaceId((Element) targetEle);
            }
            replaceStyleProperties(targetSheet, (Element) targetEle, sourceEle,
                    sourceStyle, sourceSheet, data);
        }
        IStyle targetStyle = (IStyle) targetSheet.getNodeAdaptable(targetEle);
        data.put(sourceStyle, targetStyle);
    }

    private static void replaceStyleProperties(StyleSheetImpl targetSheet,
            Element targetEle, Element sourceEle, StyleImpl sourceStyle,
            StyleSheetImpl sourceSheet, CloneData data) {
        String type = targetEle.getAttribute(DOMConstants.ATTR_TYPE)
                .toLowerCase();
        String propTagName = type + "-" + TAG_PROPERTIES; //$NON-NLS-1$
        Iterator<Element> targetPropIt = DOMUtils
                .childElementIterByTag(targetEle, propTagName);
        while (targetPropIt.hasNext()) {
            Element targetPropEle = targetPropIt.next();

            NamedNodeMap attrs = targetPropEle.getAttributes();
            for (int i = 0; i < attrs.getLength(); i++) {
                Node attr = attrs.item(i);
//                String key = attr.getNodeName();
                String value = attr.getNodeValue();
                if (HyperlinkUtils.isAttachmentURL(value)
                        || HyperlinkUtils.isInternalAttachmentURL(value)) {
                    String newValue = cloneAttachment(value,
                            sourceSheet.getManifest(),
                            targetSheet.getManifest(), data);
                    attr.setNodeValue(newValue);
                }
            }

            Iterator<Element> targetDSIt = DOMUtils.childElementIterByTag(
                    targetPropEle, DOMConstants.TAG_DEFAULT_STYLE);
            while (targetDSIt.hasNext()) {
                Element targetDSEle = targetDSIt.next();
                String family = DOMUtils.getAttribute(targetDSEle,
                        DOMConstants.ATTR_STYLE_FAMILY);
                if (family != null) {
                    String dsId = DOMUtils.getAttribute(targetDSEle,
                            DOMConstants.ATTR_STYLE_ID);
                    IStyle sourceDS = sourceStyle.getDefaultStyleById(dsId);
                    if (sourceDS != null) {
                        IStyle targetDS = importStyle(targetSheet,
                                (StyleImpl) sourceDS, sourceSheet, data);
                        if (targetDS != null) {
                            DOMUtils.setAttribute(targetDSEle, ATTR_STYLE_ID,
                                    targetDS.getId());
                        }
                    }
                }
            }
        }
    }

    private static String cloneAttachment(String sourceURL,
            IManifest sourceManifest, IManifest targetManifest,
            CloneData data) {
        String targetURL = data.getString(ICloneData.URLS, sourceURL);
        if (targetURL != null)
            return targetURL;

        boolean isSystemAttachment = sourceManifest == null
                && HyperlinkUtils.isInternalAttachmentURL(sourceURL);
        if (targetManifest == null
                || (sourceManifest == null && !isSystemAttachment)) {
            return (String) cache(data, sourceURL, sourceURL);
        }

        InputStream sourceInputStream = null;

        String sourcePath = null;
        String sourceMadiaType = null;

        if (isSystemAttachment) {
            try {
                URL url = new URL(sourceURL);
                sourceInputStream = url.openStream();
                sourcePath = url.getPath();
                String ext = FileUtils.getExtension(sourcePath);
                sourceMadiaType = generateMediaType(ext);
            } catch (MalformedURLException e1) {
            } catch (IOException e1) {
            }
        } else {
            IFileEntry sourceEntry = sourceManifest
                    .getFileEntry(HyperlinkUtils.toAttachmentPath(sourceURL));
            if (sourceEntry == null)
                return (String) cache(data, sourceURL, sourceURL);
            sourceInputStream = sourceEntry.getInputStream();
            sourcePath = sourceEntry.getPath();
        }

        String newPath = Core.getIdFactory().createId()
                + FileUtils.getExtension(sourcePath);
        String attachmentPath = targetManifest.makeAttachmentPath(newPath);
        String mediaType = sourceMadiaType;
        IFileEntry targetEntry = targetManifest.createFileEntry(attachmentPath,
                mediaType);
        targetEntry.increaseReference();

        if (sourceInputStream != null) {
            try {
                OutputStream os = targetEntry.openOutputStream();
                try {
                    FileUtils.transfer(sourceInputStream, os, true);
                } finally {
                    os.close();
                }
            } catch (IOException e) {
                Core.getLogger().log(e,
                        "Failed to clone attachment " + sourceURL); //$NON-NLS-1$
            } finally {
                try {
                    sourceInputStream.close();
                } catch (IOException e) {
                }
            }
        }
        targetURL = HyperlinkUtils.toAttachmentURL(targetEntry.getPath());
        return (String) cache(data, sourceURL, targetURL);
    }

    private static String generateMediaType(String ext) {
        String mediaType = "image/png"; //$NON-NLS-1$
        if ("gif".equalsIgnoreCase(ext)) { //$NON-NLS-1$
            mediaType = "image/gif"; //$NON-NLS-1$
        } else if ("jpeg".equalsIgnoreCase(ext)) { //$NON-NLS-1$
            mediaType = "image/jpeg"; //$NON-NLS-1$
        } else if ("bmp".equalsIgnoreCase(ext)) { //$NON-NLS-1$
            mediaType = "image/bmp"; //$NON-NLS-1$
        }
        return mediaType;
    }

    private static Object cache(CloneData data, Object source, Object target) {
        data.cache(source, target);
        return target;
    }

    private static IStyle importNoParentStyle(StyleSheetImpl targetSheet,
            StyleImpl sourceStyle) {
        Element sourceEle = sourceStyle.getImplementation();
        Node targetEle = clone(targetSheet.getImplementation(), sourceEle);
        return (IStyle) targetSheet.getNodeAdaptable(targetEle);
    }

}