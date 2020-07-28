/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
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
/**
 * 
 */
package org.xmind.ui.internal.zen;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmind.core.IBoundary;
import org.xmind.core.IComment;
import org.xmind.core.ICommentManager;
import org.xmind.core.IControlPoint;
import org.xmind.core.IFileEntry;
import org.xmind.core.IHtmlNotesContent;
import org.xmind.core.IHyperlinkSpan;
import org.xmind.core.IIdentifiable;
import org.xmind.core.IImage;
import org.xmind.core.ILegend;
import org.xmind.core.INotes;
import org.xmind.core.INotesContent;
import org.xmind.core.IParagraph;
import org.xmind.core.IPlainNotesContent;
import org.xmind.core.IRelationship;
import org.xmind.core.IResourceRef;
import org.xmind.core.ISettingEntry;
import org.xmind.core.ISheet;
import org.xmind.core.ISheetSettings;
import org.xmind.core.ISpan;
import org.xmind.core.ISpanList;
import org.xmind.core.ISummary;
import org.xmind.core.ITopic;
import org.xmind.core.ITopicExtension;
import org.xmind.core.ITopicExtensionElement;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.dom.BoundaryImpl;
import org.xmind.core.internal.dom.DOMConstants;
import org.xmind.core.internal.dom.RelationshipImpl;
import org.xmind.core.internal.dom.SheetImpl;
import org.xmind.core.internal.dom.SummaryImpl;
import org.xmind.core.internal.dom.TopicImpl;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.core.style.IStyled;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.core.util.IStyleRefCounter;
import org.xmind.ui.internal.MindMapUIPlugin;

public class SheetDeserializer {

    private IWorkbook workbook;

    private Map<Properties, IStyle> styleTable;

    /**
     * 
     */
    public SheetDeserializer(IWorkbook workbook) {
        this.workbook = workbook;
        this.styleTable = new HashMap<Properties, IStyle>();
    }

    /**
     * @param sheet
     * @param sheetObject
     */
    public void deserialize(ISheet sheet, JSONObject sheetObject)
            throws IOException {

        String sheetId = sheetObject.optString(ZenConstants.KEY_ID);
        ((SheetImpl) sheet).getImplementation()
                .setAttribute(DOMConstants.ATTR_ID, sheetId);

        String sheetTitle = sheetObject.optString(ZenConstants.KEY_TITLE);
        sheet.setTitleText(sheetTitle);

        JSONObject rootTopicObject = sheetObject
                .optJSONObject(ZenConstants.KEY_ROOT_TOPIC);
        Assert.isNotNull(rootTopicObject);
        ITopic rootTopic = parseTopic(rootTopicObject, null);
        sheet.replaceRootTopic(rootTopic);

        JSONArray relationshipArray = sheetObject
                .optJSONArray(ZenConstants.KEY_RELATIONSHIPS);
        if (relationshipArray != null) {
            for (Object relationshipArrayElement : relationshipArray) {
                if (relationshipArrayElement instanceof JSONObject) {
                    sheet.addRelationship(parseRelationship(
                            (JSONObject) relationshipArrayElement));
                }
            }
        }

        // legend
        JSONObject legendObject = sheetObject
                .optJSONObject(ZenConstants.KEY_LEGEND);
        if (legendObject != null) {
            ILegend legend = sheet.getLegend();
            legend.setVisible(ZenConstants.VAL_VISIBLE.equals(
                    legendObject.optString(ZenConstants.KEY_VISIBILITY, null)));
            JSONObject positionObject = legendObject
                    .optJSONObject(ZenConstants.KEY_POSITION);
            if (positionObject != null) {
                legend.setPosition(positionObject.optInt(ZenConstants.KEY_X, 0),
                        positionObject.optInt(ZenConstants.KEY_Y, 0));
            }
            JSONObject markerMapObject = legendObject
                    .optJSONObject(ZenConstants.KEY_MARKERS);
            JSONObject groupMapObject = legendObject
                    .optJSONObject(ZenConstants.KEY_GROUPS);
            IMarkerSheet markerSheet = workbook.getMarkerSheet();
            if (groupMapObject != null) {
                Iterator<String> groupIdIt = groupMapObject.keys();
                while (groupIdIt.hasNext()) {
                    String groupId = groupIdIt.next();
                    JSONObject groupObject = groupMapObject
                            .getJSONObject(groupId);
                    IMarkerGroup group = markerSheet.getMarkerGroup(groupId);
                    if (group == null) {
                        group = markerSheet.createMarkerGroupById(groupId);
                    }
                    group.setName(groupObject.optString(ZenConstants.KEY_NAME));

                    JSONArray markerArray = groupObject
                            .optJSONArray(ZenConstants.KEY_MARKERS);
                    if (markerArray != null) {
                        for (Object markerArrayElement : markerArray) {
                            String markerId = (String) markerArrayElement;
                            JSONObject markerObject = markerMapObject
                                    .optJSONObject(markerId);
                            if (markerObject != null) {
                                IMarker marker = markerSheet
                                        .getMarker(markerId);
                                if (marker == null) {
                                    String resourceURL = markerObject.optString(
                                            ZenConstants.KEY_RESOURCE);
                                    String resourcePath = resourceURL == null
                                            ? null
                                            : HyperlinkUtils.toAttachmentPath(
                                                    resourceURL);
                                    String newPath = allocateMarkerResource(
                                            markerSheet, resourcePath);
                                    marker = markerSheet.createMarkerById(
                                            markerId, newPath);
                                }
                                if (marker.getParent() == null)
                                    group.addMarker(marker);
                            }
                        }
                    }

                    if (!group.isEmpty() && group.getParent() == null) {
                        markerSheet.addMarkerGroup(group);
                    }
                }
            }
            if (markerMapObject != null) {
                Iterator<String> markerIdIt = markerMapObject.keys();
                while (markerIdIt.hasNext()) {
                    String markerId = markerIdIt.next();
                    JSONObject markerObject = markerMapObject
                            .optJSONObject(markerId);
                    if (markerObject != null) {
                        legend.setMarkerDescription(markerId, markerObject
                                .optString(ZenConstants.KEY_NAME, null));
                    }
                }
            }
        }

        JSONObject settingsObject = sheetObject
                .optJSONObject(ZenConstants.KEY_SETTINGS);
        if (settingsObject != null) {
            ISheetSettings settings = sheet.getSettings();
            Iterator<String> settingPathIt = settingsObject.keys();
            while (settingPathIt.hasNext()) {
                String settingPath = settingPathIt.next();
                JSONArray settingEntryArray = settingsObject
                        .optJSONArray(settingPath);
                if (settingEntryArray != null) {
                    for (Object settingEntryArrayElement : settingEntryArray) {
                        if (settingEntryArrayElement instanceof JSONObject) {
                            JSONObject settingEntryObject = (JSONObject) settingEntryArrayElement;
                            ISettingEntry settingEntry = settings
                                    .createEntry(settingPath);
                            Iterator<String> keyIt = settingEntryObject.keys();
                            while (keyIt.hasNext()) {
                                String key = keyIt.next();
                                settingEntry.setAttribute(key,
                                        settingEntryObject.optString(key,
                                                null));
                            }
                            settings.addEntry(settingEntry);
                        }
                    }

                }
            }
        }

        deserializeStyle(sheet, sheetObject);
        deserializeTheme(sheet, sheetObject);
    }

    private ITopic parseTopic(JSONObject topicObject, String type) {
        String topicId = topicObject.optString(ZenConstants.KEY_ID, null);
        Assert.isNotNull(topicId);
        ITopic topic = workbook.createTopic();
        ((TopicImpl) topic).getImplementation()
                .setAttribute(DOMConstants.ATTR_ID, topicId);

        topic.setTitleText(topicObject.optString(ZenConstants.KEY_TITLE, null));
        topic.setTitleWidth(topicObject.optInt(ZenConstants.KEY_TITLE_WIDTH,
                ITopic.UNSPECIFIED));
        topic.setFolded(ZenConstants.VAL_FOLDED
                .equals(topicObject.optString(ZenConstants.KEY_BRANCH, null)));

        deserializeTopicStructure(topicObject, topic, type);
        JSONObject positionObject = topicObject
                .optJSONObject(ZenConstants.KEY_POSITION);
        if (positionObject != null) {
            topic.setPosition(positionObject.optInt(ZenConstants.KEY_X, 0),
                    positionObject.optInt(ZenConstants.KEY_Y, 0));
        } else {
            topic.setPosition(null);
        }
        topic.setHyperlink(topicObject.optString(ZenConstants.KEY_HREF, null));

        String zClass = topicObject.optString(ZenConstants.KEY_ZCLASS, null);
        if (zClass != null) {
            topic.setZClass(zClass);
        }

        JSONArray labelArray = topicObject
                .optJSONArray(ZenConstants.KEY_LABELS);
        if (labelArray != null) {
            Set<String> labels = new HashSet<String>();
            for (Object label : labelArray) {
                if (label instanceof String) {
                    labels.add((String) label);
                }
            }
            topic.setLabels(labels);
        }

        JSONArray markerRefArray = topicObject
                .optJSONArray(ZenConstants.KEY_MARKERS);
        if (markerRefArray != null) {
            for (Object markerRefObject : markerRefArray) {
                if (markerRefObject instanceof JSONObject) {
                    String markerId = ((JSONObject) markerRefObject)
                            .optString(ZenConstants.KEY_MARKER_ID, null);
                    if (markerId != null) {
                        topic.addMarker(markerId);
                    }
                }
            }
        }

        deserializeStyle(topic, topicObject);

        JSONObject imageObject = topicObject
                .optJSONObject(ZenConstants.KEY_IMAGE);
        if (imageObject != null) {
            topic.getImage().setSource(
                    imageObject.optString(ZenConstants.KEY_SRC, null));
            topic.getImage().setAlignment(
                    imageObject.optString(ZenConstants.KEY_ALIGN, null));
            topic.getImage().setWidth(imageObject.optInt(ZenConstants.KEY_WIDTH,
                    IImage.UNSPECIFIED));
            topic.getImage().setHeight(imageObject
                    .optInt(ZenConstants.KEY_HEIGHT, IImage.UNSPECIFIED));
        }

        JSONObject numberingObject = topicObject
                .optJSONObject(ZenConstants.KEY_NUMBERING);
        if (numberingObject != null) {
            topic.getNumbering().setFormat(numberingObject
                    .optString(ZenConstants.KEY_NUMBER_FORMAT, null));
            topic.getNumbering().setDepth(numberingObject
                    .optString(ZenConstants.KEY_NUMBER_DEPTH, null));
            topic.getNumbering().setPrefix(
                    numberingObject.optString(ZenConstants.KEY_PREFIX, null));
            topic.getNumbering().setSuffix(
                    numberingObject.optString(ZenConstants.KEY_SUFFIX, null));
            topic.getNumbering().setSeparator(numberingObject
                    .optString(ZenConstants.KEY_NUMBER_SEPARATOR, null));
            String prepend = numberingObject
                    .optString(ZenConstants.KEY_PREPENDING_NUMBERS, null);
            if (ZenConstants.VAL_NONE.equals(prepend)) {
                topic.getNumbering().setPrependsParentNumbers(false);
            }
        }

        JSONObject notesObject = topicObject
                .optJSONObject(ZenConstants.KEY_NOTES);
        if (notesObject != null) {
            JSONObject plainObject = notesObject
                    .optJSONObject(ZenConstants.KEY_PLAIN);
            if (plainObject != null) {
                String textContent = plainObject
                        .optString(ZenConstants.KEY_CONTENT, null);
                if (textContent != null) {
                    INotesContent plainContent = workbook
                            .createNotesContent(INotes.PLAIN);
                    if (plainContent instanceof IPlainNotesContent) {
                        ((IPlainNotesContent) plainContent)
                                .setTextContent(textContent);
                        topic.getNotes().setContent(INotes.PLAIN, plainContent);
                    }
                }
            }

            JSONObject htmlObject = notesObject
                    .optJSONObject(ZenConstants.KEY_HTML);
            if (htmlObject != null) {
                JSONObject contentObject = htmlObject
                        .optJSONObject(ZenConstants.KEY_CONTENT);
                if (contentObject != null) {
                    INotesContent htmlContent = workbook
                            .createNotesContent(INotes.HTML);
                    if (htmlContent instanceof IHtmlNotesContent) {
                        deserializeHtmlNotesContent(
                                (IHtmlNotesContent) htmlContent, contentObject);
                        topic.getNotes().setContent(INotes.HTML, htmlContent);
                    }
                }
            }
        }

        JSONObject childrenObject = topicObject
                .optJSONObject(ZenConstants.KEY_CHILDREN);
        if (childrenObject != null) {
            Iterator<String> types = childrenObject.keys();
            while (types.hasNext()) {
                String childType = types.next();
                JSONArray topicArray = childrenObject.optJSONArray(childType);
                for (Object childObject : topicArray) {
                    ITopic childTopic = parseTopic((JSONObject) childObject,
                            childType);
                    topic.add(childTopic, childType);
                }
            }
        }

        JSONArray boundaryArray = topicObject
                .optJSONArray(ZenConstants.KEY_BOUNDARIES);
        if (boundaryArray != null) {
            for (Object boundaryObject : boundaryArray) {
                if (boundaryObject instanceof JSONObject) {
                    topic.addBoundary(
                            parseBoundary((JSONObject) boundaryObject));
                }
            }
        }

        JSONArray summaryArray = topicObject
                .optJSONArray(ZenConstants.KEY_SUMMARIES);
        if (summaryArray != null) {
            for (Object summaryObject : summaryArray) {
                if (summaryObject instanceof JSONObject) {
                    topic.addSummary(parseSummary((JSONObject) summaryObject));
                }
            }
        }

        JSONArray extArray = topicObject
                .optJSONArray(ZenConstants.KEY_EXTENSIONS);
        if (extArray != null) {
            for (Object extObject : extArray) {
                if (extObject instanceof JSONObject) {
                    deserializeTopicExtension(topic, (JSONObject) extObject);
                }
            }
        }

        JSONArray commentArray = topicObject
                .optJSONArray(ZenConstants.KEY_COMMENTS);
        if (commentArray != null) {
            for (Object commentObject : commentArray) {
                if (commentObject instanceof JSONObject) {
                    deserializeComment(topic, (JSONObject) commentObject);
                }
            }
        }

        return topic;
    }

    private void deserializeTopicStructure(JSONObject topicObject, ITopic topic,
            String type) {
        String structureClass = topicObject
                .optString(ZenConstants.KEY_STRUCTURE_CLASS, null);
        if ("detached".equals(type)) { //$NON-NLS-1$
            if ("org.xmind.ui.map".equals(structureClass) //$NON-NLS-1$
                    || "org.xmind.ui.map.unbalanced".equals(structureClass)) { //$NON-NLS-1$
                structureClass = "org.xmind.ui.map.floating"; //$NON-NLS-1$
            } else if ("org.xmind.ui.map.clockwise".equals(structureClass)) { //$NON-NLS-1$
                structureClass = "org.xmind.ui.map.floating.clockwise"; //$NON-NLS-1$
            } else if ("org.xmind.ui.map.anticlockwise" //$NON-NLS-1$
                    .equals(structureClass)) {
                structureClass = "org.xmind.ui.map.floating.anticlockwise"; //$NON-NLS-1$
            }
        }
        topic.setStructureClass(structureClass);
    }

    /**
     * @param topic
     * @param commentObject
     */
    private void deserializeComment(IIdentifiable object,
            JSONObject commentObject) {
        String author = commentObject.optString(ZenConstants.KEY_AUTHOR, null);
        long creationTime = commentObject
                .optLong(ZenConstants.KEY_CREATION_TIME, 0);
        String objectId = object.getId();
        if (author == null)
            return;

        ICommentManager commentManager = workbook.getCommentManager();
        IComment comment = commentManager.createComment(author, creationTime,
                objectId);
        comment.setContent(
                commentObject.optString(ZenConstants.KEY_CONTENT, null));
        commentManager.addComment(comment);
    }

    private void deserializeHtmlNotesContent(IHtmlNotesContent content,
            JSONObject contentObject) {
        JSONArray paragraphArray = contentObject
                .optJSONArray(ZenConstants.KEY_PARAGRAPHS);
        if (paragraphArray != null) {
            for (Object paragraphArrayElement : paragraphArray) {
                if (paragraphArrayElement instanceof JSONObject) {
                    JSONObject paragraphObject = (JSONObject) paragraphArrayElement;
                    IParagraph paragraph = content.createParagraph();
                    deserializeStyle(paragraph, paragraphObject);
                    deserializeSpanList(paragraph, paragraphObject, content);
                    content.addParagraph(paragraph);
                }
            }
        }
    }

    private void deserializeSpanList(ISpanList spanList,
            JSONObject sourceObject, IHtmlNotesContent spanFactory) {
        JSONArray spanArray = sourceObject.optJSONArray(ZenConstants.KEY_SPANS);
        if (spanArray == null || spanArray.length() == 0)
            return;

        for (Object spanArrayElement : spanArray) {
            if (spanArrayElement instanceof JSONObject) {
                JSONObject spanObject = (JSONObject) spanArrayElement;
                ISpan span;
                String text = spanObject.optString(ZenConstants.KEY_TEXT, null);
                if (text != null) {
                    span = spanFactory.createTextSpan(text);
                } else {
                    String imageSource = spanObject
                            .optString(ZenConstants.KEY_IMAGE, null);
                    if (imageSource != null) {
                        span = spanFactory.createImageSpan(imageSource);
                    } else {
                        String href = spanObject
                                .optString(ZenConstants.KEY_HREF, null);
                        if (href != null) {
                            span = spanFactory.createHyperlinkSpan(href);
                            deserializeSpanList((IHyperlinkSpan) span,
                                    spanObject, spanFactory);
                        } else {
                            continue;
                        }
                    }
                }
                deserializeStyle(span, spanObject);
                spanList.addSpan(span);
            }
        }
    }

    private IBoundary parseBoundary(JSONObject boundaryObject) {
        String id = boundaryObject.optString(ZenConstants.KEY_ID, null);
        Assert.isNotNull(id);
        IBoundary boundary = workbook.createBoundary();
        ((BoundaryImpl) boundary).getImplementation()
                .setAttribute(DOMConstants.ATTR_ID, id);

        boundary.setTitleText(
                boundaryObject.optString(ZenConstants.KEY_TITLE, null));

        String range = boundaryObject.optString(ZenConstants.KEY_RANGE, null);
        if (Ranges.RANGE_MASTER.equals(range)) {
            boundary.setMasterBoundary(true);
        } else {
            boundary.setStartIndex(Ranges.parseStartIndex(range));
            boundary.setEndIndex(Ranges.parseEndIndex(range));
        }

        deserializeStyle(boundary, boundaryObject);
        return boundary;
    }

    private ISummary parseSummary(JSONObject summaryObject) {
        String id = summaryObject.optString(ZenConstants.KEY_ID, null);
        Assert.isNotNull(id);
        ISummary summary = workbook.createSummary();
        ((SummaryImpl) summary).getImplementation()
                .setAttribute(DOMConstants.ATTR_ID, id);
        summary.setTopicId(
                summaryObject.optString(ZenConstants.KEY_TOPIC_ID, null));

        String range = summaryObject.optString(ZenConstants.KEY_RANGE, null);
        summary.setStartIndex(Ranges.parseStartIndex(range));
        summary.setEndIndex(Ranges.parseEndIndex(range));

        deserializeStyle(summary, summaryObject);
        return summary;
    }

    private IRelationship parseRelationship(JSONObject relObject) {
        String relId = relObject.optString(ZenConstants.KEY_ID, null);
        Assert.isNotNull(relId);
        IRelationship rel = workbook.createRelationship();
        ((RelationshipImpl) rel).getImplementation()
                .setAttribute(DOMConstants.ATTR_ID, relId);

        rel.setTitleText(relObject.optString(ZenConstants.KEY_TITLE, null));
        rel.setEnd1Id(relObject.optString(ZenConstants.KEY_END1_ID, null));
        rel.setEnd2Id(relObject.optString(ZenConstants.KEY_END2_ID, null));

        JSONObject controlPointMap = relObject
                .optJSONObject(ZenConstants.KEY_CONTROL_POINTS);
        if (controlPointMap != null) {
            Iterator<String> indexKeys = controlPointMap.keys();
            while (indexKeys.hasNext()) {
                String indexKey = indexKeys.next();
                int index;
                try {
                    index = Integer.parseInt(indexKey, 10);
                } catch (NumberFormatException e) {
                    continue;
                }
                deserializeControlPoint(rel, index,
                        controlPointMap.getJSONObject(indexKey));
            }
        }

        deserializeStyle(rel, relObject);

        return rel;
    }

    private void deserializeControlPoint(IRelationship rel, int index,
            JSONObject controlPointObject) {
        boolean hasPosition = controlPointObject.has(ZenConstants.KEY_X)
                || controlPointObject.has(ZenConstants.KEY_Y);
        boolean hasAngle = controlPointObject.has(ZenConstants.KEY_ANGLE);
        boolean hasAmount = controlPointObject.has(ZenConstants.KEY_AMOUNT);
        if (!hasPosition && !hasAngle && !hasAmount)
            return;

        IControlPoint controlPoint = rel.getControlPoint(index);
        if (hasPosition) {
            controlPoint.setPosition(
                    controlPointObject.optInt(ZenConstants.KEY_X, 0),
                    controlPointObject.optInt(ZenConstants.KEY_Y, 0));
        }
        if (hasAngle) {
            controlPoint.setPolarAngle(
                    controlPointObject.optDouble(ZenConstants.KEY_ANGLE, 0));
        }
        if (hasAmount) {
            controlPoint.setPolarAmount(
                    controlPointObject.optDouble(ZenConstants.KEY_AMOUNT, 0));
        }
    }

    private void deserializeTopicExtension(ITopic topic, JSONObject extObject) {
        String providerName = extObject.optString(ZenConstants.KEY_PROVIDER,
                null);
        Assert.isNotNull(providerName);
        ITopicExtension ext = topic.createExtension(providerName);

        deserializeTopicExtensionElement(ext.getContent(), extObject);

        JSONArray resourceRefArray = extObject
                .optJSONArray(ZenConstants.KEY_RESOURCE_REFS);
        if (resourceRefArray != null) {
            for (Object resourceRefArrayElement : resourceRefArray) {
                if (resourceRefArrayElement instanceof String) {
                    String refURL = (String) resourceRefArrayElement;
                    if (HyperlinkUtils.isAttachmentURL(refURL)) {
                        ext.addResourceRef(
                                ext.getOwnedWorkbook().createResourceRef(
                                        IResourceRef.FILE_ENTRY, HyperlinkUtils
                                                .toAttachmentPath(refURL)));
                    }
                }
            }
        }
    }

    private void deserializeTopicExtensionElement(ITopicExtensionElement ele,
            JSONObject eleObject) {
        JSONObject attrMapObject = eleObject
                .optJSONObject(ZenConstants.KEY_ATTRS);
        if (attrMapObject != null) {
            Iterator<String> attrKeyIt = attrMapObject.keys();
            while (attrKeyIt.hasNext()) {
                String attrKey = attrKeyIt.next();
                ele.setAttribute(attrKey, attrMapObject.getString(attrKey));
            }
        }

        Object content = eleObject.opt(ZenConstants.KEY_CONTENT);
        if (content instanceof String) {
            ele.setTextContent((String) content);
        } else if (content instanceof JSONArray) {
            JSONArray childElementArray = (JSONArray) content;
            for (Object childElementObject : childElementArray) {
                if (childElementObject instanceof JSONObject) {
                    String childName = ((JSONObject) childElementObject)
                            .optString(ZenConstants.KEY_NAME, null);
                    Assert.isNotNull(childName);
                    deserializeTopicExtensionElement(ele.createChild(childName),
                            (JSONObject) childElementObject);
                }
            }
        } else {
            /// TODO bad file format
        }
    }

    private void deserializeStyle(IStyled styled, JSONObject sourceObject) {
        JSONObject styleObject = sourceObject
                .optJSONObject(ZenConstants.KEY_STYLE);
        if (styleObject == null)
            return;

        IStyle style = findOrCreateStyle(styleObject, IStyleSheet.NORMAL_STYLES,
                styled.getStyleType());
        if (style == null)
            return;

        styled.setStyleId(style.getId());
    }

    private IStyle findOrCreateStyle(JSONObject styleObject, String groupName,
            String defaultType) {
        String type = styleObject.optString(ZenConstants.KEY_TYPE, null);
        if (type == null) {
            type = defaultType;
        }
        if (type == null) {
            return null;
        }

        JSONObject propertiesObject = styleObject
                .optJSONObject(ZenConstants.KEY_PROPERTIES);
        if (propertiesObject == null)
            return null;

        IStyle style = workbook.getStyleSheet().createStyle(type);

        Properties properties = new Properties();
        properties.setProperty("$__TYPE__$", type); //$NON-NLS-1$
        properties.setProperty("$__GROUP__$", groupName); //$NON-NLS-1$

        Iterator<String> keyIt = propertiesObject.keys();
        while (keyIt.hasNext()) {
            String key = keyIt.next();

            ///fix: value may be number.
            String value = ""; //$NON-NLS-1$
            try {
                value = propertiesObject.getString(key);
            } catch (JSONException e) {
                Object value_0 = propertiesObject.get(key);
                if (value_0 instanceof Integer || value_0 instanceof Double) {
                    if ("fo:font-size".equals(key) //$NON-NLS-1$
                            || "line-width".equals(key)) { //$NON-NLS-1$
                        value = value_0 + "pt"; //$NON-NLS-1$
                    } else {
                        value = "" + value_0; //$NON-NLS-1$
                    }
                }

                MindMapUIPlugin.log(e, null);
            }

            style.setProperty(key, value);
            properties.setProperty(key, value);
        }

        IStyle createdStyle = styleTable.get(properties);
        if (createdStyle != null) {
            workbook.getAdapter(IStyleRefCounter.class)
                    .decreaseRef(style.getId());
            return createdStyle;
        }

        styleTable.put(properties, style);
        workbook.getStyleSheet().addStyle(style, groupName);

        return style;
    }

    private void deserializeTheme(ISheet sheet, JSONObject sheetObject) {
        JSONObject themeObject = sheetObject
                .optJSONObject(ZenConstants.KEY_THEME);
        if (themeObject == null)
            return;

        IStyle theme = workbook.getStyleSheet().createStyle(IStyle.THEME);

        Properties properties = new Properties();
        properties.setProperty("$__TYPE__$", IStyle.THEME); //$NON-NLS-1$
        properties.setProperty("$__GROUP__$", IStyleSheet.MASTER_STYLES); //$NON-NLS-1$

        Iterator<String> keyIt = themeObject.keys();
        while (keyIt.hasNext()) {
            String key = keyIt.next();
            JSONObject styleObject = themeObject.optJSONObject(key);
            if (styleObject == null)
                continue;

            IStyle style = findOrCreateStyle(styleObject,
                    IStyleSheet.AUTOMATIC_STYLES, null);
            if (style == null)
                continue;

            theme.setDefaultStyleId(key, style.getId());
            properties.setProperty(key, style.getId());
        }

        IStyle createdTheme = styleTable.get(properties);
        if (createdTheme != null) {
            workbook.getAdapter(IStyleRefCounter.class)
                    .decreaseRef(createdTheme.getId());
            theme = createdTheme;
        } else {
            styleTable.put(properties, theme);
            workbook.getStyleSheet().addStyle(theme, IStyleSheet.MASTER_STYLES);
        }

        sheet.setThemeId(theme.getId());
    }

    private String allocateMarkerResource(IMarkerSheet markerSheet,
            String resourcePath) throws IOException {
        IFileEntry fileEntry = workbook.getManifest()
                .getFileEntry(resourcePath);
        String fileName = resourcePath.substring(resourcePath.indexOf("/") + 1); //$NON-NLS-1$
        String newPath = markerSheet
                .allocateMarkerResource(fileEntry.openInputStream(), fileName);
        return newPath;
    }

}
