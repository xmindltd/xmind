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
package org.xmind.core;

import java.util.Comparator;
import java.util.UUID;

import org.xmind.core.internal.InternalCore;
import org.xmind.core.marker.IMarkerSheetBuilder;
import org.xmind.core.style.IStyleSheetBuilder;
import org.xmind.core.util.ILogger;

/**
 * 
 * @author Frank Shaka
 */
@SuppressWarnings("deprecation")
public class Core {

    /**
     * Core event type for modifying the title text (value is 'titleText').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.ITitled}</dd>
     * <dt>OldValue:</dt>
     * <dd>the old title ({@link String}), or <code>null</code> indicating that
     * the title was unspecified</dd>
     * <dt>NewValue:</dt>
     * <dd>the new title ({@link String}), or <code>null</code> indicating that
     * the title is unspecified</dd>
     * </dl>
     * 
     * @see org.xmind.core.ITitled#getTitleText()
     * @see org.xmind.core.ITitled#setTitleText(String)
     */
    public static final String TitleText = "titleText"; //$NON-NLS-1$

    /**
     * Core event type for modifying a topic's title width (value is
     * 'titleWidth').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.ITopic}</dd>
     * <dt>OldValue:</dt>
     * <dd>the old width ({@link Integer}), or <code>null</code> indicating that
     * the width was unspecified</dd>
     * <dt>NewValue:</dt>
     * <dd>the new width ({@link Integer}), or <code>null</code> indicating that
     * the with is unspecified</dd>
     * </dl>
     * 
     * @see org.xmind.core.ITopic#getTitleWidth()
     * @see org.xmind.core.ITopic#setTitleWidth(int)
     */
    public static final String TitleWidth = "titleWidth"; //$NON-NLS-1$

    /**
     * Core event type for modifying the style id (value is 'style').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.style.IStyled}</dd>
     * <dt>OldValue:</dt>
     * <dd>the old style's id ({@link String}), or <code>null</code> indicating
     * that the style was unspecified</dd>
     * <dt>NewValue:</dt>
     * <dd>the new style's id ({@link String}), or <code>null</code> indicating
     * that the style is unspecified</dd>
     * </dl>
     * 
     * @see org.xmind.core.style.IStyled#getStyleId()
     * @see org.xmind.core.style.IStyled#setStyleId(String)
     */
    public static final String Style = "style"; //$NON-NLS-1$

    /**
     * Core event type for folding/unfolding a topic (value is 'topicFolded').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.ITopic}</dd>
     * <dt>OldValue:</dt>
     * <dd>the old folding state ({@link java.lang.Boolean})</dd>
     * <dt>NewValue:</dt>
     * <dd>the new folding state ({@link java.lang.Boolean})</dd>
     * </dl>
     * 
     * @see org.xmind.core.ITopic#isFolded()
     * @see org.xmind.core.ITopic#setFolded(boolean)
     */
    public static final String TopicFolded = "topicFolded"; //$NON-NLS-1$

    /**
     * Core event type for modifying an object's position (value is 'position').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.IPositioned}</dd>
     * <dt>OldValue:</dt>
     * <dd>the old position ({@link org.xmind.core.util.Point}), or
     * <code>null</code> indicating that the position was unspecified</dd>
     * <dt>NewValue:</dt>
     * <dd>the new position ({@link org.xmind.core.util.Point}), or
     * <code>null</code> indicating that the position is unspecified</dd>
     * </dl>
     * 
     * @see org.xmind.core.IPositioned#getPosition()
     * @see org.xmind.core.IPositioned#setPosition(org.xmind.core.util.Point)
     * @see org.xmind.core.IPositioned#setPosition(int, int)
     */
    public static final String Position = "position"; //$NON-NLS-1$

    /**
     * Core event type for modifying a topic's hyperlink reference (value is
     * 'topicHyperlink').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.ITopic}</dd>
     * <dt>OldValue:</dt>
     * <dd>the old hyperlink reference ({@link String}), or <code>null</code>
     * indicating that the hyperlink was unspecified</dd>
     * <dt>NewValue:</dt>
     * <dd>the new hyperlink reference ({@link String}), or <code>null</code>
     * indicating that the hyperlink is unspecified</dd>
     * </dl>
     * 
     * @see org.xmind.core.ITopic#getHyperlink()
     * @see org.xmind.core.ITopic#setHyperlink(String)
     */
    public static final String TopicHyperlink = "topicHyperlink"; //$NON-NLS-1$

    /**
     * Core event type for adding a subtopic to a topic (value is 'topicAdd').
     * <dl>
     * <dt>Source:</dt>
     * <dd>the parent {@link org.xmind.core.ITopic}</dd>
     * <dt>Target:</dt>
     * <dd>the child {@link org.xmind.core.ITopic}</dd>
     * <dt>Index:</dt>
     * <dd>the index of the added subtopic</dd>
     * <dt>Data:</dt>
     * <dd>the child topic's type ({@link String})</dd>
     * </dl>
     * 
     * @see org.xmind.core.ITopic#getType()
     * @see org.xmind.core.ITopic#add(org.xmind.core.ITopic)
     * @see org.xmind.core.ITopic#add(org.xmind.core.ITopic, String)
     * @see org.xmind.core.ITopic#add(org.xmind.core.ITopic, int, String)
     */
    public static final String TopicAdd = "topicAdd"; //$NON-NLS-1$

    /**
     * Core event type for removing a subtopic from a topic (value is
     * 'topicRemove').
     * <dl>
     * <dt>Source:</dt>
     * <dd>the parent {@link org.xmind.core.ITopic}</dd>
     * <dt>Target:</dt>
     * <dd>the child {@link org.xmind.core.ITopic}</dd>
     * <dt>Index:</dt>
     * <dd>the index of the removed subtopic</dd>
     * <dt>Data:</dt>
     * <dd>the child topic's type ({@link String})</dd>
     * </dl>
     * 
     * @see org.xmind.core.ITopic#remove(org.xmind.core.ITopic)
     */
    public static final String TopicRemove = "topicRemove"; //$NON-NLS-1$

    /**
     * Core event type for adding a sheet to a workbook (value is 'sheetAdd').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.IWorkbook}</dd>
     * <dt>Target:</dt>
     * <dd>the added {@link org.xmind.core.ISheet}</dd>
     * <dt>Index:</dt>
     * <dd>the index of the added sheet</dd>
     * </dl>
     * 
     * @see org.xmind.core.IWorkbook#addSheet(org.xmind.core.ISheet)
     * @see org.xmind.core.IWorkbook#addSheet(org.xmind.core.ISheet, int)
     */
    public static final String SheetAdd = "sheetAdd"; //$NON-NLS-1$

    /**
     * Core event type for removing a sheet from a workbook (value is
     * 'sheetRemove').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.IWorkbook}</dd>
     * <dt>Target:</dt>
     * <dd>the removed {@link org.xmind.core.ISheet}</dd>
     * <dt>Index:</dt>
     * <dd>the index of the removed sheet</dd>
     * </dl>
     * 
     * @see org.xmind.core.IWorkbook#removeSheet(org.xmind.core.ISheet)
     */
    public static final String SheetRemove = "sheetRemove"; //$NON-NLS-1$

    /**
     * Core event type for moving a sheet to a different index within the owned
     * workbook (value is 'sheetMove').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.IWorkbook}</dd>
     * <dt>Target:</dt>
     * <dd>the moved {@link org.xmind.core.ISheet}</dd>
     * <dt>Index:</dt>
     * <dd>the old index of the moved sheet</dd>
     * </dl>
     * 
     * @see org.xmind.core.IWorkbook#moveSheet(int, int)
     */
    public static final String SheetMove = "sheetMove"; //$NON-NLS-1$

    /**
     * Core event type for modifying one attribute of a sheet's settings (value
     * is 'sheetSettings').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.ISheet}</dd>
     * <dt>Target:</dt>
     * <dd>the attribute key ({@link String})</dd>
     * <dt>Data:</dt>
     * <dd>the entry path ({@link String})</dd>
     * <dt>OldValue:</dt>
     * <dd>the old attribute value ({@link String})</dd>
     * <dt>NewValue:</dt>
     * <dd>the new attribute value ({@link String})</dd>
     * </dl>
     * 
     * @see org.xmind.core.ISheetSettings
     * @see org.xmind.core.ISheet#getSettings()
     */
    public static final String SheetSettings = "sheetSettings"; //$NON-NLS-1$

    /**
     * Core event type for adding a relationship to a sheet (value is
     * 'relationshipAdd').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.ISheet}</dd>
     * <dt>Target:</dt>
     * <dd>the added {@link org.xmind.core.IRelationship}</dd>
     * </dl>
     * 
     * @see org.xmind.core.ISheet#addRelationship(org.xmind.core.IRelationship)
     */
    public static final String RelationshipAdd = "relationshipAdd"; //$NON-NLS-1$

    /**
     * Core event type for removing a relationship from a sheet (value is
     * 'relationshipRemove').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.ISheet}</dd>
     * <dt>Target:</dt>
     * <dd>the removed {@link org.xmind.core.IRelationship}</dd>
     * </dl>
     * 
     * @see org.xmind.core.ISheet#removeRelationship(org.xmind.core.IRelationship)
     */
    public static final String RelationshipRemove = "relationshipRemove"; //$NON-NLS-1$

    /**
     * Core event type for replacing the root topic of a sheet (value is
     * 'rootTopic').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.ISheet}</dd>
     * <dt>OldValue:</dt>
     * <dd>the old root {@link org.xmind.core.ITopic}</dd>
     * <dt>NewValue:</dt>
     * <dd>the new root {@link org.xmind.core.ITopic}</dd>
     * </dl>
     * 
     * @see org.xmind.core.ISheet#getRootTopic()
     * @see org.xmind.core.ISheet#replaceRootTopic(ITopic)
     */
    public static final String RootTopic = "rootTopic"; //$NON-NLS-1$

//    /**
//     * Core event type for modifying one control point on a relationship (value
//     * is 'relationshipControlPoint').
//     * <dl>
//     * <dt>Source:</dt>
//     * <dd>{@link org.xmind.core.IRelationship}</dd>
//     * <dt>Target:</dt>
//     * <dd>the modified {@link org.xmind.core.IControlPoint}, or
//     * <code>null</code> indicating that the control point is unspecified</dd>
//     * <dt>Index:</dt>
//     * <dd>the index of the modified control point</dd>
//     * </dl>
//     * 
//     * @see org.xmind.core.IRelationship#getControlPoint(int);
//     * @see org.xmind.core.IRelationship#setControlPoint(int, double, double);
//     * @see org.xmind.core.IRelationship#resetControlPoint(int);
//     */
//    public static final String RelationshipControlPoint = "relationshipControlPoint"; //$NON-NLS-1$

    /**
     * Core event type for modifying the first end of a relationship (value is
     * 'relationshipEnd1').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.IRelationship}</dd>
     * <dt>OldValue:</dt>
     * <dd>the old first end's id ({@link String}), or <code>null</code>
     * indicating that the first end was unspecified</dd>
     * <dt>NewValue:</dt>
     * <dd>the new first end's id ({@link String}), or <code>null</code>
     * indicating that the first end is unspecified</dd>
     * </dl>
     * 
     * @see org.xmind.core.IRelationship#getEnd1()
     * @see org.xmind.core.IRelationship#getEnd1Id()
     * @see org.xmind.core.IRelationship#setEnd1Id(String)
     */
    public static final String RelationshipEnd1 = "relationshipEnd1"; //$NON-NLS-1$

    /**
     * Core event type for modifying the second end of a relationship (value is
     * 'relationshipEnd2').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.IRelationship}</dd>
     * <dt>OldValue:</dt>
     * <dd>the old second end's id ({@link String}), or <code>null</code>
     * indicating that the second end was unspecified</dd>
     * <dt>NewValue:</dt>
     * <dd>the new second end's id ({@link String}), or <code>null</code>
     * indicating that the second end is unspecified</dd>
     * </dl>
     * 
     * @see org.xmind.core.IRelationship#getEnd2()
     * @see org.xmind.core.IRelationship#getEnd2Id()
     * @see org.xmind.core.IRelationship#setEnd2Id(String)
     */
    public static final String RelationshipEnd2 = "relationshipEnd2"; //$NON-NLS-1$

    /**
     * Core event type for adding a marker to a marker group (value is
     * 'markerAdd').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.marker.IMarkerGroup}</dd>
     * <dt>Target:</dt>
     * <dd>the added {@link org.xmind.core.marker.IMarker}</dd>
     * <dt>Index:</dt>
     * <dd>the index of the added marker</dd>
     * </dl>
     * 
     * @see org.xmind.core.marker.IMarkerGroup#addMarker(org.xmind.core.marker.IMarker)
     */
    public static final String MarkerAdd = "markerAdd"; //$NON-NLS-1$

    /**
     * Core event type for removing a marker from a marker group (value is
     * 'markerRemove').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.marker.IMarkerGroup}</dd>
     * <dt>Target:</dt>
     * <dd>the removed {@link org.xmind.core.marker.IMarker}</dd>
     * <dt>Index:</dt>
     * <dd>the index of the removed marker</dd>
     * </dl>
     * 
     * @see org.xmind.core.marker.IMarkerGroup#removeMarker(org.xmind.core.marker.IMarker)
     */
    public static final String MarkerRemove = "markerRemove"; //$NON-NLS-1$

    /**
     * Core event type for adding a marker group to a marker sheet (value is
     * 'markerGroupAdd').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.marker.IMarkerSheet}</dd>
     * <dt>Target:</dt>
     * <dd>the added {@link org.xmind.core.marker.IMarkerGroup}</dd>
     * <dt>Index:</dt>
     * <dd>the index of the added marker group</dd>
     * </dl>
     * 
     * @see org.xmind.core.marker.IMarkerSheet#addMarkerGroup(org.xmind.core.marker.IMarkerGroup)
     */
    public static final String MarkerGroupAdd = "markerGroupAdd"; //$NON-NLS-1$

    /**
     * Core event type for removing a marker group from a marker sheet (value is
     * 'markerGroupRemove').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.marker.IMarkerSheet}</dd>
     * <dt>Target:</dt>
     * <dd>the removed {@link org.xmind.core.marker.IMarkerGroup}</dd>
     * <dt>Index:</dt>
     * <dd>the index of the removed marker group</dd>
     * </dl>
     * 
     * @see org.xmind.core.marker.IMarkerSheet#removeMarkerGroup(org.xmind.core.marker.IMarkerGroup)
     */
    public static final String MarkerGroupRemove = "markerGroupRemove"; //$NON-NLS-1$

    /**
     * Core event type for adding a marker to a topic (value is 'markerRefAdd').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.ITopic}</dd>
     * <dt>Target:</dt>
     * <dd>the added marker's id ({@link String})</dd>
     * </dl>
     * 
     * @see org.xmind.core.ITopic#addMarker(String)
     */
    public static final String MarkerRefAdd = "markerRefAdd"; //$NON-NLS-1$

    /**
     * Core event type for removong a marker from a topic (value is
     * 'markerRefRemove').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.ITopic}</dd>
     * <dt>Target:</dt>
     * <dd>the removed marker's id ({@link String})</dd>
     * </dl>
     * 
     * @see org.xmind.core.ITopic#removeMarker(String)
     */
    public static final String MarkerRefRemove = "markerRefRemove"; //$NON-NLS-1$

    /**
     * Core event type for modifying the range of a Range object (value is
     * 'range').
     * <p>
     * An event of this type usually follows a 'startIndex' or 'endIndex' event.
     * </p>
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.IRange}</dd>
     * <dt>OldValue:</dt>
     * <dd>unspecified</dd>
     * <dt>NewValue:</dt>
     * <dd>unspecified</dd>
     * <dl>
     */
    public static final String Range = "range"; //$NON-NLS-1$;

    /**
     * Core event type for modifying the starting index of a Range object (value
     * is 'startIndex').
     * <p>
     * <b>NOTE</b>: This event is always followed by a 'range' event as the
     * range is modified.
     * </p>
     * 
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.IRange}</dd>
     * <dt>OldValue:</dt>
     * <dd>the old starting index ({@link Integer}) of the range, or
     * <code>null</code> indicating that the index was unspecified</dd>
     * <dt>NewValue:</dt>
     * <dd>the new starting index ({@link Integer}) of the range, or
     * <code>null</code> indicating that the index is unspecified</dd>
     * </dl>
     */
    public static final String StartIndex = "startIndex"; //$NON-NLS-1$

    /**
     * Core event type for modifying the ending index of a Range object (value
     * is 'endIndex').
     * <p>
     * <b>NOTE</b>: This event is always followed by a 'range' event as the
     * range is modified.
     * </p>
     * 
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.IRange}</dd>
     * <dt>OldValue:</dt>
     * <dd>the old ending index ({@link Integer}) of the range, or
     * <code>null</code> indicating that the index was unspecified</dd>
     * <dt>NewValue:</dt>
     * <dd>the new ending index ({@link Integer}) of the range, or
     * <code>null</code> indicating that the index is unspecified</dd>
     * </dl>
     */
    public static final String EndIndex = "endIndex"; //$NON-NLS-1$

    /**
     * Core event type for adding a boundary to a topic (value is
     * 'boundaryAdd').
     * 
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.ITopic}</dd>
     * <dt>Target:</dt>
     * <dd>the added {@link org.xmind.core.IBoundary}</dd>
     * </dl>
     */
    public static final String BoundaryAdd = "boundaryAdd"; //$NON-NLS-1$

    /**
     * Core event type for removing a boundary to a topic (value is
     * 'boundaryRemove').
     * 
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.ITopic}</dd>
     * <dt>Target:</dt>
     * <dd>the removed {@link org.xmind.core.IBoundary}</dd>
     * </dl>
     */
    public static final String BoundaryRemove = "boundaryRemove"; //$NON-NLS-1$

    /**
     * Core event type for modifying the attached topic of a summary (value is
     * 'topicRefId').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.ISummary}</dd>
     * <dt>OldValue:</dt>
     * <dd>the old topic's id ({@link String})</dd>
     * <dt>NewValue:</dt>
     * <dd>the new topic's id ({@link String})</dd>
     * </dl>
     */
    public static final String TopicRefId = "topicRefId"; //$NON-NLS-1$

    /**
     * Core event type for adding a summary to a topic (value is 'summaryAdd').
     * 
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.ITopic}</dd>
     * <dt>Target:</dt>
     * <dd>the added {@link org.xmind.core.IBoundary}</dd>
     * </dl>
     */
    public static final String SummaryAdd = "summaryAdd"; //$NON-NLS-1$

    /**
     * Core event type for removing a summary to a topic (value is
     * 'summaryRemove').
     * 
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.ITopic}</dd>
     * <dt>Target:</dt>
     * <dd>the removed {@link org.xmind.core.IBoundary}</dd>
     * </dl>
     */
    public static final String SummaryRemove = "summaryRemove"; //$NON-NLS-1$

    /**
     * Core event type for adding a style to a style sheet (value is
     * 'styleAdd').
     * 
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.style.IStyleSheet}</dd>
     * <dt>Target:</dt>
     * <dd>the added {@link org.xmind.core.style.IStyle}</dd>
     * </dl>
     */
    public static final String StyleAdd = "styleAdd"; //$NON-NLS-1$

    /**
     * Core event type for removing a style from a style sheet (value is
     * 'styleRemove').
     * 
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.style.IStyleSheet}</dd>
     * <dt>Target:</dt>
     * <dd>the removed {@link org.xmind.core.style.IStyle}</dd>
     * </dl>
     */
    public static final String StyleRemove = "styleRemove"; //$NON-NLS-1$

    /**
     * Core event type for modifying a the name (value is 'name).
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.INamed}</dd>
     * <dt>OldValue:</dt>
     * <dd>the old name ({@link String}), or <code>null</code> indicating that
     * the name was unspecified</dd>
     * <dt>NewValue:</dt>
     * <dd>the new name ({@link String}), or <code>null</code> indicating that
     * the name is unspecified</dd>
     * </dl>
     */
    public static final String Name = "name"; //$NON-NLS-1$

    /**
     * Core event type for modifying a property (value is 'property').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.IProperties}</dd>
     * <dt>Target:</dt>
     * <dd>the property name ({@link String})</dd>
     * <dt>OldValue:</dt>
     * <dd>the old property value ({@link String}), or <code>null</code>
     * indicating that the property was unspecified</dd>
     * <dt>NewValue:</dt>
     * <dd>the new property value ({@link String}), or <code>null</code>
     * indicating that the property is unspecified</dd>
     * </dl>
     */
    public static final String Property = "property"; //$NON-NLS-1$

    /**
     * Core event type for modifying a sheet's theme id (value is 'themeId').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.ISheet}</dd>
     * <dt>OldValue:</dt>
     * <dd>the old theme id ({@link String}), or <code>null</code> indicating
     * that the theme id was unspecified</dd>
     * <dt>NewValue:</dt>
     * <dd>the new theme id ({@link String}), or <code>null</code> indicating
     * that the theme id is unspecified</dd>
     * </dl>
     */
    public static final String ThemeId = "themeId"; //$NON-NLS-1$

    /**
     * Core event type for modifying the structure class of a topic (value is
     * 'structureClass').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.ITopic}</dd>
     * <dt>OldValue:</dt>
     * <dd>the old structure class ({@link String}), or <code>null</code>
     * indicating that the structure class was unspecified</dd>
     * <dt>NewValue:</dt>
     * <dd>the new structure class ({@link String}), or <code>null</code>
     * indicating that the structure class is unspecified</dd>
     * </dl>
     */
    public static final String StructureClass = "structureClass"; //$NON-NLS-1$

    /**
     * Core event type for modifying the note content of a topic (value is
     * 'topicNotes').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.ITopic}</dd>
     * <dt>Target:</dt>
     * <dd>the note format (either {@link org.xmind.core.INotes#PLAIN} or
     * {@link org.xmind.core.INotes#HTML})</dd>
     * <dt>OldValue:</dt>
     * <dd>the old notes content ({@link org.xmind.core.INotesContent}), or
     * <code>null</code> indicating that the note content was unspecified</dd>
     * <dt>NewValue:</dt>
     * <dd>the new notes content ({@link org.xmind.core.INotesContent}), or
     * <code>null</code> indicating that the note content is unspecified</dd>
     * </dl>
     * 
     * @see org.xmind.core.INotes#PLAIN
     * @see org.xmind.core.INotes#HTML
     * @see org.xmind.core.INotes#getContent(String)
     * @see org.xmind.core.INotes#setContent(String, INotesContent)
     */
    public static final String TopicNotes = "topicNotes"; //$NON-NLS-1$

    /**
     * Core event type for modifying the labels (value is 'labels').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.ILabeled}</dd>
     * <dt>OldValue:</dt>
     * <dd>a {@link java.util.Set} of <code>String</code> containing all the old
     * labels (may be empty, but is never null)</dd>
     * <dt>NewValue:</dt>
     * <dd>a {@link java.util.Set} of <code>String</code> containing all the new
     * labels (may be empty, but is never null)</dd>
     * </dl>
     */
    public static final String Labels = "labels"; //$NON-NLS-1$

    /**
     * Core event type for changes of the resource references (value is
     * 'resourceRefs').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.util.IRefCounter}</dd>
     * <dt>OldValue:</dt>
     * <dd>a {@link java.util.Collection} of <code>String</code> containing all
     * the old resource references (may be empty, but is never null)</dd>
     * <dt>NewValue:</dt>
     * <dd>a {@link java.util.Collection} of <code>String</code> containing all
     * the new resource references (may be empty, but is never null)</dd>
     * </dl>
     */
    public static final String ResourceRefs = "resourceRefs"; //$NON-NLS-1$

    /**
     * Core event type for modifying the source url of an image (value is
     * 'imageSource').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.IImage}</dd>
     * <dt>OldValue:</dt>
     * <dd>the old source url of the image ({@link String}), or
     * <code>null</code> indicating that the source url was unspecified</dd>
     * <dt>NewValue:</dt>
     * <dd>the new source url of the image ({@link String}), or
     * <code>null</code> indicating that the source url is unspecified</dd>
     * </dl>
     */
    public static final String ImageSource = "imageSource"; //$NON-NLS-1$

    /**
     * Core event type for modifying the width of an image (value is
     * 'imageWidth').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.IImage}</dd>
     * <dt>OldValue:</dt>
     * <dd>the old width of the image ({@link Integer}), or <code>null</code>
     * indicating that the width was unspecified</dd>
     * <dt>NewValue:</dt>
     * <dd>the new width of the image ({@link String}), or <code>null</code>
     * indicating that the width is unspecified</dd>
     * </dl>
     */
    public static final String ImageWidth = "imageWidth"; //$NON-NLS-1$

    /**
     * Core event type for modifying the height of an image (value is
     * 'imageHeight').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.IImage}</dd>
     * <dt>OldValue:</dt>
     * <dd>the old height of the image ({@link Integer}), or <code>null</code>
     * indicating that the height was unspecified</dd>
     * <dt>NewValue:</dt>
     * <dd>the new height of the image ({@link String}), or <code>null</code>
     * indicating that the height is unspecified</dd>
     * </dl>
     */
    public static final String ImageHeight = "imageHeight"; //$NON-NLS-1$

    /**
     * Core event type for modifying the alignment of an image (value is
     * 'imageAlignment').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.IImage}</dd>
     * <dt>OldValue:</dt>
     * <dd>the old alignment of the image ({@link Integer}), or
     * <code>null</code> indicating that the alignment was unspecified</dd>
     * <dt>NewValue:</dt>
     * <dd>the new alignment of the image ({@link String}), or <code>null</code>
     * indicating that the alignment is unspecified</dd>
     * </dl>
     */
    public static final String ImageAlignment = "imageAlignment"; //$NON-NLS-1$

    /**
     * Core event type for modifying the visiblity of a legend (value is
     * 'visibility').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.ILegend}</dd>
     * <dt>OldValue:</dt>
     * <dd>the old visibility ({@link Boolean})</dd>
     * <dt>NewValue:</dt>
     * <dd>the new visibility ({@link Boolean})</dd>
     * </dl>
     */
    public static final String Visibility = "visibility"; //$NON-NLS-1$

    /**
     * Core event type for modifying the description of a marker on a sheet
     * (value is 'markerDescription').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.ILegend}</dd>
     * <dt>Target:</dt>
     * <dd>the marker's id ({@link String})</dd>
     * <dt>OldValue:</dt>
     * <dd>the old description ({@link String}) of the target marker, or
     * <code>null</code> indicating that the description was unspecified</dd>
     * <dt>NewValue:</dt>
     * <dd>the new description ({@link String}) of the target marker, or
     * <code>null</code> indicating that the description is unspecified</dd>
     * </dl>
     */
    public static final String MarkerDescription = "markerDescription"; //$NON-NLS-1$

    public static final String NumberFormat = "numberingFormat"; //$NON-NLS-1$

    public static final String NumberingPrefix = "numberingPrefix"; //$NON-NLS-1$

    public static final String NumberingSuffix = "numberingSuffix"; //$NON-NLS-1$

    public static final String NumberPrepending = "parentNumberingPrepending"; //$NON-NLS-1$

    public static final String NumberingSeparator = "numberingSeparator"; //$NON-NLS-1$

    public static final String NumberingDepth = "numberingDepth"; //$NON-NLS-1$

    /**
     * Core event type for changing the password of a workbook (value is
     * 'passwordChange').
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.IWorkboook}</dd>
     * </dl>
     */
    public static final String PasswordChange = "passwordChange"; //$NON-NLS-1$

    /**
     * Core event type for going to save a workbook (value is
     * 'workbookPreSave'). This type of events is dispatched before a workbook
     * is saved. Listening to this type of events allows modification to the
     * workbook content before saving it to the destination. The most common
     * usage is to save preview pictures by the UI, for we can't generate a
     * preview picture without a graphical environment.
     * 
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.IWorkboook}</dd>
     * </dl>
     * 
     * @see org.xmind.core.IWorkbook#save()
     * @see org.xmind.core.IWorkbook#save(org.xmind.core.io.IOutputTarget)
     * @see org.xmind.core.IWorkbook#save(java.io.OutputStream)
     * @see org.xmind.core.IWorkbook#save(String)
     */
    public static final String WorkbookPreSave = "workbookPreSave"; //$NON-NLS-1$

    /**
     * Core event type for going to save a workbook (value is
     * 'workbookPreSaveOnce'). Similar to {@link #WorkbookPreSave}, but
     * listeners to this type events are notified only once and removed from the
     * event list thereafter. This type of events are commonly used when some
     * pending work that may modify the content of a workbook should be counted
     * before saving the workbook.
     * 
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.IWorkboook}</dd>
     * </dl>
     * 
     * @see org.xmind.core.IWorkbook#save()
     * @see org.xmind.core.IWorkbook#save(org.xmind.core.io.IOutputTarget)
     * @see org.xmind.core.IWorkbook#save(java.io.OutputStream)
     * @see org.xmind.core.IWorkbook#save(String)
     */
    public static final String WorkbookPreSaveOnce = "workbookPreSaveOnce"; //$NON-NLS-1$

    /**
     * Core event type for having saved a workbook (value is 'workbookSave').
     * This type of events is dispatched after a workbook is saved.
     * 
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.IWorkboook}</dd>
     * <dd></dd>
     * </dl>
     * 
     * @see org.xmind.core.IWorkbook#save()
     * @see org.xmind.core.IWorkbook#save(org.xmind.core.io.IOutputTarget)
     * @see org.xmind.core.IWorkbook#save(java.io.OutputStream)
     * @see org.xmind.core.IWorkbook#save(String)
     */
    public static final String WorkbookSave = "workbookSave"; //$NON-NLS-1$

    /**
     * Core event for updating the modified time of an element (value is
     * 'modifyTime'). When this event is dispatched, all other modification info
     * has been updated such as
     * {@link org.xmind.core.IModifiable#getModifiedBy()}.
     * 
     * <dl>
     * <dt>Source:</dt>
     * <dd>a {@link org.xmind.core.IModifiable} object</dd>
     * <dt>OldValue:</dt>
     * <dd>the old modified time (long)</dd>
     * <dt>NewValue:</dt>
     * <dd>the new modified time (long)</dd>
     * </dl>
     * 
     * @see org.xmind.core.IModifiable
     */
    public static final String ModifyTime = "modifyTime"; //$NON-NLS-1$

    /**
     * Core event type for adding a revision to the revision manager (value is
     * 'revisionAdd').
     * 
     * <dl>
     * <dt>Source:</dt>
     * <dd>the parent {@link org.xmind.core.IRevisionManager}</dd>
     * <dt>Target:</dt>
     * <dd>the child {@link org.xmind.core.IRevision}</dd>
     * <dt>Data:</dt>
     * <dd>Corresponding resource ID (String)</dd>
     * </dl>
     * 
     * @see org.xmind.core.IRevisionManager#addRevision(IAdaptable)
     */
    public static final String RevisionAdd = "revisionAdd"; //$NON-NLS-1$

    /**
     * Core event type for removing a revision from the revision manager (value
     * is 'revisionRemove').
     * 
     * <dl>
     * <dt>Source:</dt>
     * <dd>the parent {@link org.xmind.core.IRevisionManager}</dd>
     * <dt>Target:</dt>
     * <dd>the child {@link org.xmind.core.IRevision}</dd>
     * <dt>Data:</dt>
     * <dd>Corresponding resource ID (String)</dd>
     * </dl>
     * 
     * @see org.xmind.core.IRevisionManager#removeRevision(IRevision)
     */
    public static final String RevisionRemove = "revisionRemove"; //$NON-NLS-1$

    /**
     * Core event type for value change in {@link org.xmind.core.IMeta} (value
     * is 'metadata').
     * 
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.IMeta}</dd>
     * <dt>Target:</dt>
     * <dd>a {@link String} representing the key path of the modified metadata
     * </dd>
     * <dt>OldValue:</dt>
     * <dd>the old {@link String} value of the metadata, or <code>null</code> to
     * indicate this metadata was created</dd>
     * <dt>NewValue:</dt>
     * <dd>the new {@link String} value of the metadata, or <code>null</code> to
     * indicate this metadata was deleted</dd>
     * </dl>
     * 
     * @see org.xmind.core.IMeta#setValue(String, String)
     * @see org.xmind.core.IMetaData#setValue(String)
     */
    public static final String Metadata = "metadata"; //$NON-NLS-1$

    /**
     * Core event type for attribute change in {@link org.xmind.core.IMetaData}
     * (value is 'metadataAttribute').
     * 
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.IMeta}</dd>
     * <dt>Target:</dt>
     * <dd>a {@link String} representing the key of the modified metadata</dd>
     * <dt>Data:</dt>
     * <dd>a {@link String} representing the name of the modified attribute</dd>
     * <dt>OldValue:</dt>
     * <dd>the old {@link String} value of the attribute, or <code>null</code>
     * to indicate this attribute was added</dd>
     * <dt>NewValue:</dt>
     * <dd>the new {@link String} value of the attribute, or <code>null</code>
     * to indicate this attribute was removed</dd>
     * 
     * @deprecated Not used any more. {@link #Metadata} is enough.
     * 
     * @see org.xmind.core.IMetaData#setAttribute(String, String)
     */
    public static final String MetadataAttribute = "metadataAttribute"; //$NON-NLS-1$

    /**
     * Core event type for adding a file entry to the owned manifest (value is
     * 'fileEntryAdd'). A file entry is automatically added to the owned
     * manifest once its reference count is increased from 0 to 1.
     * <dl>
     * <dt>Source:</dt>
     * <dd>the {@link org.xmind.core.IManifest}</dd>
     * <dt>Target:</dt>
     * <dd>the {@link org.xmind.core.IFileEntry}</dd>
     * </dl>
     * 
     * @see org.xmind.core.IFileEntry#increaseReference()
     */
    public static final String FileEntryAdd = "fileEntryAdd"; //$NON-NLS-1$

    /**
     * Core event type for removing a file entry from the owned manifest (value
     * is 'fileEntryRemove'). A file entry is automatically removed from the
     * owned manifest once its reference count is decreased from 1 to 0.
     * <dl>
     * <dt>Source:</dt>
     * <dd>the {@link org.xmind.core.IManifest}</dd>
     * <dt>Target:</dt>
     * <dd>the {@link org.xmind.core.IFileEntry}</dd>
     * </dl>
     * 
     * @see org.xmind.core.IFileEntry#decreaseReference()
     */
    public static final String FileEntryRemove = "fileEntryRemove"; //$NON-NLS-1$

    /**
     * Core event type for adding a comment to its associated object (value is
     * 'commentAdd').
     * 
     * <dl>
     * <dt>Source:</dt>
     * <dd>the associated object that implements {@link org.xmind.core.ITitled},
     * e.g. {@link org.xmind.core.ITopic} or {@link org.xmind.core.ISheet}</dd>
     * <dt>Target:</dt>
     * <dd>the {@link org.xmind.core.IComment}</dd>
     * </dl>
     * 
     * @see org.xmind.core.ICommentManager#addComment(IComment)
     */
    public static final String CommentAdd = "commentAdd"; //$NON-NLS-1$

    /**
     * Core event type for removing a comment from its associated object (value
     * is 'commentRemove').
     * 
     * <dl>
     * <dt>Source:</dt>
     * <dd>the associated object that implements {@link org.xmind.core.ITitled},
     * e.g. {@link org.xmind.core.ITopic} or {@link org.xmind.core.ISheet}</dd>
     * <dt>Target:</dt>
     * <dd>the {@link org.xmind.core.IComment}</dd>
     * </dl>
     * 
     * @see org.xmind.core.ICommentManager#removeComment(IComment)
     */
    public static final String CommentRemove = "commentRemove"; //$NON-NLS-1$

    /**
     * Core event type for modifying the content of a comment (value is
     * 'commentContent').
     * 
     * <dl>
     * <dt>Source:</dt>
     * <dd>the {@link org.xmind.core.IComment}</dd>
     * <dt>OldValue:</dt>
     * <dd>the old text content ({@link String}), or <code>null</code> if there
     * was no content before modifying</dd>
     * <dt>NewValue:</dt>
     * <dd>the new text content ({@link String}), or <code>null</code> if there
     * is no content after modifying</dd>
     * </dl>
     */
    public static final String CommentContent = "commentContent"; //$NON-NLS-1$

    /**
     * Error constants indicating that an unknown error occurs (value=1).
     */
    public static final int ERROR_UNKNOWN = 1;

    /**
     * Error constants indicating that a null argument is passed in (value=2).
     */
    public static final int ERROR_NULL_ARGUMENT = 2;

    /**
     * Error constants indicating that an invalid argument is passed in
     * (value=3).
     */
    public static final int ERROR_INVALID_ARGUMENT = 3;

    public static final int ERROR_INVALID_FILE = 10;

    public static final int ERROR_NO_SUCH_ENTRY = 11;

    public static final int ERROR_FAIL_ACCESS_XML_PARSER = 12;

    public static final int ERROR_SYNTAX = 13;
    public static final int ERROR_FAIL_PARSING_XML = ERROR_SYNTAX;

    public static final int ERROR_NO_WORKBOOK_CONTENT = 14;

    public static final int ERROR_FAIL_ACCESS_XML_TRANSFORMER = 15;

    public static final int ERROR_FAIL_INIT_CRYPTOGRAM = 16;

    public static final int ERROR_WRONG_PASSWORD = 17;

    public static final int ERROR_FAIL_SERIALIZING_XML = 18;

    public static final int ERROR_CANCELLATION = 100;

    /**
     * Media type for a textual file (value='text/xml').
     * 
     * @see org.xmind.core.IFileEntry#getMediaType()
     */
    public static final String MEDIA_TYPE_TEXT_XML = "text/xml"; //$NON-NLS-1$

    /**
     * Media type for an image file (value='image/png').
     * 
     * @see org.xmind.core.IFileEntry#getMediaType()
     */
    public static final String MEDIA_TYPE_IMAGE_PNG = "image/png"; //$NON-NLS-1$

    @Deprecated
    public static final String TopicComments = "topicComments"; //$NON-NLS-1$

    private Core() {
        throw new AssertionError("Instantiation of this class is not allowed!"); //$NON-NLS-1$
    }

    /**
     * 
     * <p>
     * <b>WARNING:</b> This is a legacy mechanism to generate unique
     * identifiers. Clients should use {@link UUID} instead for higher
     * uniqueness.
     * </p>
     * 
     * @return an {@link IIdFactory} that generates unique identifiers
     */
    public static IIdFactory getIdFactory() {
        return getInternal().getIdFactory();
    }

    /**
     * 
     * <p>
     * <b>WARNING:</b> The workbook builder no longer supports loading
     * workbooks. Use
     * <code>Core.getSerializationProvider().newDeserializer()</code> for this
     * kind of tasks.
     * </p>
     * 
     * @return an {@link IWorkbookBuilder} that is capable for creating workbook
     *         instances
     */
    public static IWorkbookBuilder getWorkbookBuilder() {
        return getInternal().getWorkbookBuilder();
    }

    public static IWorkspace getWorkspace() {
        return getInternal().getWorkspace();
    }

    public static Comparator<ITopic> getTopicComparator() {
        return getInternal().getTopicComparator();
    }

    public static IMarkerSheetBuilder getMarkerSheetBuilder() {
        return getInternal().getMarkerSheetBuilder();
    }

    public static IStyleSheetBuilder getStyleSheetBuilder() {
        return getInternal().getStyleSheetBuilder();
    }

    public static final String getCurrentVersion() {
        return getInternal().getCurrentVersion();
    }

    public static final boolean isPartiallyCompatible(String version) {
        return getInternal().isPartiallyCompatible(version);
    }

    /**
     * <b>NOTE</b>: For internal use only. Not a public API.
     * 
     * @return the default error logger
     */
    public static final ILogger getLogger() {
        return getInternal().getLogger();
    }

    private static InternalCore getInternal() {
        return InternalCore.getInstance();
    }

}