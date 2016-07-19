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

import static org.xmind.core.internal.dom.DOMConstants.ATTR_STYLE_ID;
import static org.xmind.core.internal.dom.DOMConstants.TAG_PROPERTIES;

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
import org.xmind.core.ICloneData;
import org.xmind.core.IFileEntry;
import org.xmind.core.IManifest;
import org.xmind.core.IWorkbook;
import org.xmind.core.IWorkbookComponent;
import org.xmind.core.internal.CloneData;
import org.xmind.core.internal.zip.ArchiveConstants;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerResource;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyled;
import org.xmind.core.util.CloneHandler;
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
                CloneHandler handler = new CloneHandler(result);
                if (source instanceof IWorkbookComponent) {
                    IWorkbook sourceWorkbook = ((IWorkbookComponent) source)
                            .getOwnedWorkbook();
                    handler.withWorkbooks(sourceWorkbook, targetWorkbook);
                } else if (source instanceof IMarker) {
                    handler.withMarkerSheets(((IMarker) source).getOwnedSheet(),
                            targetWorkbook.getMarkerSheet());
                } else {
                    /// unrecognized object, skip it
                    continue;
                }

                try {
                    handler.cloneObject(source);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private static Node clone(Document doc, Node source) {
        if (source.getOwnerDocument() == doc) {
            return source.cloneNode(true);
        }
        return doc.importNode(source, true);
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
                InputStream is = sourceResource.openInputStream();
                if (is != null) {
                    try {
                        String targetPath = ArchiveConstants.PATH_MARKERS
                                + targetMarker.getResourcePath();
                        IFileEntry entry = targetWorkbook.getManifest()
                                .createFileEntry(targetPath);
                        OutputStream out = entry.openOutputStream();
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
            try {
                sourceInputStream = sourceEntry.openInputStream();
                sourcePath = sourceEntry.getPath();
            } catch (IOException e) {
            }
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