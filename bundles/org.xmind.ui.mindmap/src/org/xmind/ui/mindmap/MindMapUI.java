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
package org.xmind.ui.mindmap;

import java.util.Map;

import org.xmind.gef.GEF;
import org.xmind.gef.dnd.IDndSupport;
import org.xmind.gef.part.IPartFactory;
import org.xmind.gef.service.IPlaybackProvider;
import org.xmind.ui.branch.IBranchPolicyManager;
import org.xmind.ui.decorations.IDecorationFactory;
import org.xmind.ui.decorations.IDecorationManager;
import org.xmind.ui.internal.InternalMindMapUI;

public class MindMapUI {

    public static final String PLUGIN_ID = "org.xmind.ui"; //$NON-NLS-1$

    public static final String MINDMAP_EDITOR_ID = "org.xmind.ui.MindMapEditor"; //$NON-NLS-1$

    public static final String PERSPECTIVE_ID = "org.xmind.ui.perspective.mindmapping"; //$NON-NLS-1$

    public static final String VIEW_MARKER = "org.xmind.ui.MarkerView"; //$NON-NLS-1$

    public static final String VIEW_NOTES = "org.xmind.ui.NotesView"; //$NON-NLS-1$

    public static final String VIEW_COMMENTS = "org.xmind.ui.CommentsView"; //$NON-NLS-1$

    public static final String VIEW_THEMES = "org.xmind.ui.ThemesView"; //$NON-NLS-1$

    public static final String VIEW_OVERVIEW = "org.xmind.ui.OverviewView"; //$NON-NLS-1$

    public static final String VIEW_REVISIONS = "org.xmind.ui.RevisionsView"; //$NON-NLS-1$

    public static final String VIEW_INSPECTOR = "org.xmind.ui.InspectorView"; //$NON-NLS-1$

    public static final String VIEW_BLACKBOX = "org.xmind.ui.BlackBoxView"; //$NON-NLS-1$

    public static final String VIEW_BROSWER = "org.xmind.ui.BrowserView"; //$NON-NLS-1$

    public static final String VIEW_SPELLING = "org.xmind.ui.SpellingCheckView"; //$NON-NLS-1$

    /**
     * @deprecated Styles view has been removed since 3.5.
     */
    @Deprecated
    public static final String VIEW_STYLES = "org.xmind.ui.StylesView"; //$NON-NLS-1$

    /**
     * @deprecated LocalNetworkSharing view is no longer included in
     *             org.xmind.ui plugin.
     */
    @Deprecated
    public static final String VIEW_LOCAL_NETWORK_SHARING = "org.xmind.ui.LocalNetworkSharingView"; //$NON-NLS-1$

    public static final String POPUP_DIALOG_SETTINGS_ID = "org.xmind.ui.popupDialog"; //$NON-NLS-1$

    public static final String FILE_EXT_XMIND = ".xmind"; //$NON-NLS-1$

    public static final String FILE_EXT_XMIND_TEMP = FILE_EXT_XMIND + ".temp"; //$NON-NLS-1$

    public static final String FILE_EXT_TEMPLATE = ".xmt"; //$NON-NLS-1$

    public static final String FILE_EXT_MARKER_PACKAGE = ".xmp"; //$NON-NLS-1$

    public static final String CONTEXT_MINDMAP = "org.xmind.ui.context.mindmap"; //$NON-NLS-1$

    public static final String CONTEXT_MINDMAP_EDIT = "org.xmind.ui.context.mindmap.edit"; //$NON-NLS-1$

    public static final String CONTEXT_MINDMAP_TEXTEDIT = "org.xmind.ui.context.mindmap.textEdit"; //$NON-NLS-1$

    public static final String CONTEXT_MINDMAP_TRAVERSE = "org.xmind.ui.context.mindmap.traverse"; //$NON-NLS-1$

    public static final String CONTEXT_PAGETITLE_EDIT = "org.xmind.ui.context.mindmap.pagetitle"; //$NON-NLS-1$

    /*
     * Roles:
     */
    public static final String ROLE_MAP = "map"; //$NON-NLS-1$

    /*
     * Request Types:
     */
    public static final String REQ_CREATE_CHILD = "create_child"; //$NON-NLS-1$
    public static final String REQ_CREATE_BEFORE = "create_before"; //$NON-NLS-1$
    public static final String REQ_CREATE_PARENT = "create_parent"; //$NON-NLS-1$
    public static final String REQ_CREATE_SHEET = "create_sheet"; //$NON-NLS-1$
    public static final String REQ_CREATE_FLOAT = "create_floating"; //$NON-NLS-1$
    public static final String REQ_CREATE_RELATIONSHIP = "create_relationship"; //$NON-NLS-1$
    public static final String REQ_CREATE_BOUNDARY = "create_boundary"; //$NON-NLS-1$
    public static final String REQ_CREATE_SUMMARY = "create_summary"; //$NON-NLS-1$
    public static final String REQ_CREATE_CALLOUT = "create_callout"; //$NON-NLS-1$

    public static final String REQ_DUPLICATE_TOPIC = "duplicate"; //$NON-NLS-1$

    public static final String REQ_SELECT_CENTRAL = "select_central"; //$NON-NLS-1$
    public static final String REQ_SELECT_BROTHERS = "select_brothers"; //$NON-NLS-1$
    public static final String REQ_SELECT_CHILDREN = "select_children"; //$NON-NLS-1$
    public static final String REQ_SELECT_BY_MARKER = "select_by_marker"; //$NON-NLS-1$

    public static final String REQ_NAV_SIBLING = "navigate_sibling"; //$NON-NLS-1$
    public static final String REQ_NAV_CHILD = "navigate_child"; //$NON-NLS-1$

    public static final String REQ_PASTE_ALL = GEF.REQ_PASTE;
    public static final String REQ_PASTE_CONTENT = "paste_content"; //$NON-NLS-1$
    public static final String REQ_PASTE_FORMAT = "paste_format"; //$NON-NLS-1$
    public static final String REQ_REPLACE_ALL = "replace_all"; //$NON-NLS-1$

    public static final String REQ_ADD_IMAGE = "add_image"; //$NON-NLS-1$
    public static final String REQ_ADD_MARKER = "add_marker"; //$NON-NLS-1$
    public static final String REQ_ADD_ATTACHMENT = "add_attachment"; //$NON-NLS-1$

    public static final String REQ_RETARGET_REL = "retarget_relationship"; //$NON-NLS-1$
    public static final String REQ_MOVE_CONTROL_POINT = "move_control_point"; //$NON-NLS-1$

    public static final String REQ_SHOW_LEGEND = "show_legend"; //$NON-NLS-1$
    public static final String REQ_HIDE_LEGEND = "hide_legend"; //$NON-NLS-1$

    public static final String REQ_SHOW_NOTES = "show_notes"; //$NON-NLS-1$
    public static final String REQ_EDIT_LABEL = "edit_label"; //$NON-NLS-1$
    public static final String REQ_EDIT_LEGEND_ITEM = "edit_legend_item"; //$NON-NLS-1$

    public static final String REQ_RESET_POSITION = "reset_position"; //$NON-NLS-1$
    public static final String REQ_TILE = "tile"; //$NON-NLS-1$

    public static final String REQ_CANCEL = "cancel_operation"; //$NON-NLS-1$
    public static final String REQ_OPEN = "open"; //$NON-NLS-1$
    public static final String REQ_SAVE_ATT_AS = "save_attachment_as"; //$NON-NLS-1$
    public static final String REQ_CANCEL_HYPERLINK = "cancel_hyperlink"; //$NON-NLS-1$

    public static final String REQ_DRILLDOWN = "drill_down"; //$NON-NLS-1$
    public static final String REQ_DRILLUP = "drill_up"; //$NON-NLS-1$

    public static final String REQ_MODIFY_STYLE = "modify_style"; //$NON-NLS-1$
    public static final String REQ_MODIFY_TITLE = "modify_title"; //$NON-NLS-1$
    public static final String REQ_MODIFY_NOTES = "modify_notes"; //$NON-NLS-1$
    public static final String REQ_MODIFY_HYPERLINK = "modify_hyperlink"; //$NON-NLS-1$
    public static final String REQ_MODIFY_THEME = "modify_theme"; //$NON-NLS-1$
    public static final String REQ_MODIFY_LABEL = "modify_label"; //$NON-NLS-1$
    public static final String REQ_MODIFY_NUMBERING = "modify_numbering"; //$NON-NLS-1$
    public static final String REQ_MODIFY_RANGE = "modify_range"; //$NON-NLS-1$
    public static final String REQ_REMOVE_ALLSTYLES = "remove_all_styles"; //$NON-NLS-1$

    /*
     * Tool Types:
     */
    public static final String TOOL_CREATE_RELATIONSHIP = "org.xmind.ui.tool.createRelationship"; //$NON-NLS-1$
    public static final String TOOL_CREATE_BOUNDARY = "org.xmind.ui.tool.createBoundary"; //$NON-NLS-1$
    public static final String TOOL_CREATE_SUMMARY = "org.xmind.ui.tool.createSummary"; //$NON-NLS-1$
    public static final String TOOL_CREATE_FLOAT = "org.xmind.ui.tool.createFloatingTopic"; //$NON-NLS-1$
    public static final String TOOL_CREATE_LEGEND = "org.xmind.ui.tool.createLegend"; //$NON-NLS-1$

    public static final String TOOL_MOVE_TOPIC = "org.xmind.ui.tool.moveTopic"; //$NON-NLS-1$
    public static final String TOOL_MOVE_RELATIONSHIP = "org.xmind.ui.tool.moveRelationship"; //$NON-NLS-1$
    public static final String TOOL_MOVE_MARKER = "org.xmind.ui.tool.moveMarker"; //$NON-NLS-1$
    public static final String TOOL_MOVE_IMAGE = "org.xmind.ui.tool.moveImage"; //$NON-NLS-1$
    public static final String TOOL_MOVE_LEGEND = "org.xmind.ui.tool.moveLegend"; //$NON-NLS-1$

    public static final String TOOL_RESIZE_RANGE = "org.xmind.ui.tool.resizeRange"; //$NON-NLS-1$
    public static final String TOOL_RESIZE_IMAGE = "org.xmind.ui.tool.resizeImage"; //$NON-NLS-1$
    public static final String TOOL_PASTE_FLOAT = "org.xmind.ui.tool.pasteFloatingTopic"; //$NON-NLS-1$

    public static final String TOOL_EDIT_TOPIC_TITLE = "org.xmind.ui.tool.editTopicTitle"; //$NON-NLS-1$
    public static final String TOOL_EDIT_LABEL = "org.xmind.ui.tool.editLabel"; //$NON-NLS-1$
    public static final String TOOL_EDIT_LEGEND_ITEM = "org.xmind.ui.tool.editLegendItem"; //$NON-NLS-1$
    public static final String TOOL_EDIT_SHEET_TITLE = "org.xmind.ui.tool.editSheetTitle"; //$NON-NLS-1$

    public static final String POLICY_DELETABLE = "org.xmind.ui.editPolicy.deletable"; //$NON-NLS-1$
    public static final String POLICY_EDITABLE = "org.xmind.ui.editPolicy.editable"; //$NON-NLS-1$
    public static final String POLICY_MODIFIABLE = "org.xmind.ui.editPolicy.modifiable"; //$NON-NLS-1$
    public static final String POLICY_EXTENDABLE = "org.xmind.ui.editPolicy.extendable"; //$NON-NLS-1$
    public static final String POLICY_SHEET_SCALABLE = "org.xmind.ui.editPolicy.sheetScalable"; //$NON-NLS-1$
    public static final String POLICY_TOPIC_CREATABLE = "org.xmind.ui.editPolicy.topicCreatable"; //$NON-NLS-1$
    public static final String POLICY_TOPIC_MOVABLE = "org.xmind.ui.editPolicy.topicMovable"; //$NON-NLS-1$
    public static final String POLICY_RELATIONSHIP_MOVABLE = "org.xmind.ui.editPolicy.relationshipMovable"; //$NON-NLS-1$
    public static final String POLICY_RELATIONSHIP_CREATABLE = "org.xmind.ui.editPolicy.relationshipCreatable"; //$NON-NLS-1$
    public static final String POLICY_SHEET_CREATABLE = "org.xmind.ui.editPolicy.sheetCreatable"; //$NON-NLS-1$
    public static final String POLICY_SUMMARY_CREATABLE = "org.xmind.ui.editPolicy.summaryCreatable"; //$NON-NLS-1$
    public static final String POLICY_TOPIC_NAVIGABLE = "org.xmind.ui.editPolicy.topicNavigable"; //$NON-NLS-1$
    public static final String POLICY_TOPIC_TRAVERSABLE = "org.xmind.ui.editPolicy.topicTraversable"; //$NON-NLS-1$
    public static final String POLICY_RELATIONSHIP_TRAVERSABLE = "org.xmind.ui.editPolicy.relationshipTraversable"; //$NON-NLS-1$
    public static final String POLICY_IMAGE_MOVABLE = "org.xmind.ui.editPolicy.imageMovable"; //$NON-NLS-1$
    public static final String POLICY_MARKER_MOVABLE = "org.xmind.ui.editPolicy.markerMovable"; //$NON-NLS-1$
    public static final String POLICY_LEGEND_MOVABLE = "org.xmind.ui.editPolicy.legendMovable"; //$NON-NLS-1$
    public static final String POLICY_MAP = "org.xmind.ui.editPolicy.map"; //$NON-NLS-1$
    public static final String POLICY_LEGEND_ITEM_MODIFIABLE = "org.xmind.ui.editPolicy.legendItemModifiable"; //$NON-NLS-1$
    public static final String POLICY_DROP_TARGET = "org.xmind.ui.editPolicy.dropTarget"; //$NON-NLS-1$

    public static final String POLICY_SORTABLE = "org.xmind.ui.editPolicy.topicSortable"; //$NON-NLS-1$

    public static final String POLICY_INFO = "org.xmind.ui.editPolicy.info"; //$NON-NLS-1$

    public static final String POLICY_MAP_MOVABLE = "org.xmind.ui.editPolicy.mapMovable"; //$NON-NLS-1$

    public static final Object LAYER_TITLE = "org.xmind.ui.layer.title"; //$NON-NLS-1$
    public static final Object LAYER_UNDO = "org.xmind.ui.layer.undo"; //$NON-NLS-1$
    public static final Object LAYER_COVER = "org.xmind.ui.layer.cover"; //$NON-NLS-1$
    public static final Object LAYER_SKYLIGHT = "org.xmind.ui.layer.skylight"; //$NON-NLS-1$

    /**
     * Request parameter prefix used to identify a style property parameter in a
     * 'modify style' request, e.g., <code>'styleProperty.line-width'</code>.
     * <dl>
     * <dt>Values:</dt>
     * <dd><code>String</code></dd>
     * </dl>
     */
    public static final String PARAM_STYLE_PREFIX = "styleProperty."; //$NON-NLS-1$

    /**
     * Request parameter prefix used to identify a property parameter in a
     * 'move'/'create' request, e.g., <code>'property.title'</code>.
     * <dl>
     * <dt>Values:</dt>
     * <dd><code>Object</code></dd>
     * </dl>
     */
    public static final String PARAM_PROPERTY_PREFIX = "property."; //$NON-NLS-1$

    /**
     * Request parameter: the preferred command label of a 'modify style'
     * request.
     * <dl>
     * <dt>Values:</dt>
     * <dd><code>String</code></dd>
     * </dl>
     */
    public static final String PARAM_COMMAND_LABEL = "commandLabel"; //$NON-NLS-1$

    /**
     * Request parameter: whether or not the reqeust is performed with
     * animation.
     * <dl>
     * <dt>Values:</dt>
     * <dd><code>Boolean</code></dd>
     * </dl>
     */
    public static final String PARAM_WITH_ANIMATION = "withAnimation"; //$NON-NLS-1$

    /**
     * Request parameter: the source node of a connection request.
     * <dl>
     * <dt>Values:</dt>
     * <dd>an {@link org.xmind.gef.part.IPart}</dd>
     * </dl>
     */
    public static final String PARAM_SOURCE_NODE = "sourceNode"; //$NON-NLS-1$

    /**
     * Request parameter: the taraget node of a connection request.
     * <dl>
     * <dt>Values:</dt>
     * <dd>an {@link org.xmind.gef.part.IPart}</dd>
     * </dl>
     */
    public static final String PARAM_TARGET_NODE = "targetNode"; //$NON-NLS-1$

    /**
     * Request parameter: the marker id(s) of an 'add marker' request.
     * <dl>
     * <dt>Values:</dt>
     * <dd>the marker id (<code>String</code>), or an array of marker ids (
     * <code>String[]</code>)</dd>
     * </dl>
     */
    public static final String PARAM_MARKER_ID = "markerId"; //$NON-NLS-1$

    /**
     * Request parameter: objects to be put in the range of a 'modify range'
     * request.
     * <dl>
     * <dt>Values:</dt>
     * <dd>a array of <code>Object</code> (<code>Object[]</code>)</dd>
     * </dl>
     */
    public static final String PARAM_RANGE = "range"; //$NON-NLS-1$

    /**
     * Request parameter: whether or not to make a copy of the source.
     * <dl>
     * <dt>Values:</dt>
     * <dd><code>Boolean</code></dd>
     * </dl>
     */
    public static final String PARAM_COPY = "copy"; //$NON-NLS-1$

    /**
     * Request parameter: whether or not to make a free move.
     * <dl>
     * <dt>Values:</dt>
     * <dd><code>Boolean</code></dd>
     * </dl>
     */
    public static final String PARAM_FREE = "free"; //$NON-NLS-1$

    /**
     * Request parameter: the numbering format of the 'modify numbering'
     * request.
     * <dl>
     * <dt>Values:</dt>
     * <dd><code>String</code></dd>
     * </dl>
     */
    public static final String PARAM_NUMBERING_FORMAT = "numbering.format"; //$NON-NLS-1$

    /**
     * Request parameter: the numbering separator of the 'modify numbering'
     * request.
     * <dl>
     * <dt>Values:</dt>
     * <dd><code>String</code></dd>
     * </dl>
     */
    public static final String PARAM_NUMBERING_SEPARATOR = "numbering.separator"; //$NON-NLS-1$

    /**
     * Request parameter: the numbering prefix of the 'modify numbering'
     * request.
     * <dl>
     * <dt>Values:</dt>
     * <dd><code>String</code></dd>
     * </dl>
     */
    public static final String PARAM_NUMBERING_PREFIX = "numbering.prefix"; //$NON-NLS-1$

    /**
     * Request parameter: the numbering suffix of the 'modify numbering'
     * request.
     * <dl>
     * <dt>Values:</dt>
     * <dd><code>String</code></dd>
     * </dl>
     */
    public static final String PARAM_NUMBERING_SUFFIX = "numbering.suffix"; //$NON-NLS-1$

    /**
     * Request parameter: the parent numbering prepending value of the 'modify
     * numbering' request.
     * <dl>
     * <dt>Values:</dt>
     * <dd><code>Boolean</code></dd>
     * </dl>
     */
    public static final String PARAM_NUMBERING_PREPENDING = "numbering.prepending"; //$NON-NLS-1$

    /**
     * Request parameter: the numbering depth of the 'modify numbering' request.
     *
     * <dl>
     * <dt>Values:</dt>
     * <dd><code>String</code></dd>
     * </dl>
     */
    public static final String PARAM_NUMBERING_DEPTH = "numbering.depth"; //$NON-NLS-1$

    /**
     * Request parameter: the dragging point's id of a 'move relationship'
     * request.
     * <dl>
     * <dt>Values:</dt>
     * <dd>one of {@link #SOURCE_ANCHOR}, {@link #TARGET_ANCHOR},
     * {@link #SOURCE_CONTROL_POINT}, {@link #TARGET_CONTROL_POINT}</dd>
     * </dl>
     */
    public static final String PARAM_MOVE_REL_POINT_ID = "moveRel.pointId"; //$NON-NLS-1$

    /**
     * Request parameter: the new node part of a 'move relationship' request.
     * <dl>
     * <dt>Values:</dt>
     * <dd>an {@link org.xmind.gef.part.IPart}</dd>
     * </dl>
     */
    public static final String PARAM_MOVE_REL_NEW_NODE = "moveRel.newNode"; //$NON-NLS-1$

//    /**
//     * Request parameter: the new angle of a 'move relationship' request.
//     *
//     * <dl>
//     * <dt>Values:</dt>
//     * <dd><code>Double</code></dd>
//     * </dl>
//     */
//    public static final String PARAM_MOVE_REL_NEW_ANGLE = "moveRel.newAngle"; //$NON-NLS-1$
//
//    /**
//     * Request parameter: the new amount of a 'move relationship' request.
//     *
//     * <dl>
//     * <dt>Values:</dt>
//     * <dd><code>Double</code></dd>
//     * </dl>
//     */
//    public static final String PARAM_MOVE_REL_NEW_AMOUNT = "moveRel.newAmount"; //$NON-NLS-1$

    /**
     * Request parameter: a specified resource (marker/style/theme).
     */
    public static final String PARAM_RESOURCE = "resource"; //$NON-NLS-1$

    public static final String PARAM_OVERRIDE = "override"; //$NON-NLS-1$

    /**
     * Request parameter: data for drag-and-drop request.
     * <dl>
     * <dt>Values:</dt>
     * <dd>a {@link Map}<code>&lt;String, Object&gt</code> mapping dnd client
     * identifier to transfer data</dd>
     * </dl>
     */
    public static final String PARAM_DND_DATA = "dndData"; //$NON-NLS-1$

    /**
     * Request parameter for 'replace' or 'replace all' request to provide the
     * text to be replaced with.
     */
    public static final String PARAM_REPLACEMENT = "replacement"; //$NON-NLS-1$

    /**
     * Request parameter for 'replace' or 'replace all' request to ignore case
     * when searching text to replace.
     */
    public static final String PARAM_IGNORE_CASE = "ignoreCase"; //$NON-NLS-1$

    /**
     * The maximum zoom scale (value=<code>500(%)</code>).
     */
    public static final int ZOOM_MAX = 500;
    /**
     * The minimum zoom scale (value=<code>10(%)</code>).
     */
    public static final int ZOOM_MIN = 10;

    /**
     * Point id for the source anchor of a relationship (value=<code>1</code>).
     *
     * @see org.xmind.ui.internal.mindmap.RelationshipPart#getPointId(org.eclipse.draw2d.geometry.Point)
     */
    public static final int SOURCE_ANCHOR = 1;

    /**
     * Point id for the target anchor of a relationship (value=<code>2</code>).
     *
     * @see org.xmind.ui.internal.mindmap.RelationshipPart#getPointId(org.eclipse.draw2d.geometry.Point)
     */
    public static final int TARGET_ANCHOR = 2;

    /**
     * Point id for the source control point of a relationship (value=
     * <code>3</code>).
     *
     * @see org.xmind.ui.internal.mindmap.RelationshipPart#getPointId(org.eclipse.draw2d.geometry.Point)
     */
    public static final int SOURCE_CONTROL_POINT = 3;

    /**
     * Point id for the target control point of a relationship (value=
     * <code>4</code>).
     *
     * @see org.xmind.ui.internal.mindmap.RelationshipPart#getPointId(org.eclipse.draw2d.geometry.Point)
     */
    public static final int TARGET_CONTROL_POINT = 4;

    // ========================
    //   Branch Policy IDs
    // ------------------------

//    public static final String STRUCTURE_MAP = "or.xmind.branchPolicy.map";
//    public static final String STRUCTURE_ORG_UP = "or.xmind.branchPolicy.org-chart.up";
//    public static final String STRUCTURE_ORG_DOWN = "or.xmind.branchPolicy.org-chart.down";
//    public static final String STRUCTURE_LOGIC_LEFT = "or.xmind.branchPolicy.logic-chart.left";
//    public static final String STRUCTURE_LOGIC_RIGHT = "or.xmind.branchPolicy.logic-chart.right";
//    public static final String STRUCTURE_TREE_LEFT = "or.xmind.branchPolicy.tree.left";
//    public static final String STRUCTURE_TREE_RIGHT = "or.xmind.branchPolicy.tree.right";

    // ==========================
    //   Species
    // --------------------------
    public static final String CATEGORY_TOPIC = "org.xmind.ui.topic"; //$NON-NLS-1$
    public static final String CATEGORY_SHEET = "org.xmind.ui.sheet"; //$NON-NLS-1$
    public static final String CATEGORY_BOUNDARY = "org.xmind.ui.boundary"; //$NON-NLS-1$
    public static final String CATEGORY_RELATIONSHIP = "org.xmind.ui.relationship"; //$NON-NLS-1$
    public static final String CATEGORY_SUMMARY = "org.xmind.ui.summary"; //$NON-NLS-1$
    public static final String CATEGORY_MARKER = "org.xmind.ui.marker"; //$NON-NLS-1$
    public static final String CATEGORY_IMAGE = "org.xmind.ui.image"; //$NON-NLS-1$
    public static final String CATEGORY_CALLOUT = "org.xmind.ui.callout"; //$NON-NLS-1$

    // ==========================
    //   Branch Types
    // ==========================
    public static final String BRANCH_CENTRAL = "centralBranch"; //$NON-NLS-1$
    public static final String BRANCH_MAIN = "mainBranch"; //$NON-NLS-1$
    public static final String BRANCH_SUB = "subBranch"; //$NON-NLS-1$
    public static final String BRANCH_FLOATING = "floatingBranch"; //$NON-NLS-1$
    public static final String BRANCH_SUMMARY = "summaryBranch"; //$NON-NLS-1$
    public static final String BRANCH_ALL = "allBranches"; //$NON-NLS-1$
    public static final String BRANCH_CALLOUT = "calloutBranch"; //$NON-NLS-1$

    // ==========================
    //   DND Types
    // --------------------------
    public static final String DND_TEXT = "org.xmind.ui.dnd.text"; //$NON-NLS-1$
    public static final String DND_FILE = "org.xmind.ui.dnd.file"; //$NON-NLS-1$
    public static final String DND_URL = "org.xmind.ui.dnd.url"; //$NON-NLS-1$
    public static final String DND_MINDMAP_ELEMENT = "org.xmind.ui.dnd.mindMapElement"; //$NON-NLS-1$

    /**
     * This range is used by {@link org.xmind.ui.tools.ParentSearcher} and all
     * {@link org.xmind.ui.branch.IBranchPolicy BranchPolicies} to determine
     * which topic is about to have the dragging topic attached on it (value=
     * <code>200</code>).
     */
    public static final int SEARCH_RANGE = 200;

    /**
     * The alpha value used to indicate the disabled branches (value=
     * <code>0x50</code>).
     */
    public static final int ALPHA_DISABLED_BRANCH = 0x50;

    /**
     * The default color value for warning some topics (value=
     * <code>"#f00000"</code>).
     */
    public static final String COLOR_WARNING = "#f00000"; //$NON-NLS-1$

    /**
     * The line width of dummy topic/connection when dragging a topic (value=
     * <code>3</code>).
     */
    public static final int LINE_WIDTH_DUMMY = 3;

    public static final String LABEL_SEPARATOR = ","; //$NON-NLS-1$

    public static final int IMAGE_INIT_WIDTH = 400;

    public static final int IMAGE_INIT_HEIGHT = 400;

    /**
     * The default alpha value of fog around skylight (value=0x80).
     */
    public static final int ALPHA_FOG_AROUND_SKYLIGHT = 0x80;

    /**
     * The default line width of selection figure (value=4).
     */
    public static final int SELECTION_LINE_WIDTH = 4;

    /**
     * The default corner size of selection figure (value=5).
     */
    public static final int SELECTION_ROUNDED_CORNER = 5;

    /**
     * The default height of a diamond figure.
     */
    public static final int HEIGHT_DIAMOND = 11;

    /**
     * The default height of a dot figure.
     */
    public static final int HEIGHT_DOT = 9;

    /**
     * The default height of a square figure.
     */
    public static final int HEIGHT_SQUARE = 7;

    /**
     * The default square figure's fill color value ("#30a0f0").
     */
    public static final String FILL_COLOR_IMAGE_POINTS = "#30a0f0"; //$NON-NLS-1$

    /**
     * The middle bend points will be hidden if the length of the bounding box
     * is shorter than this value (value=28).
     */
    public static final int HIDE_BEND_POINT_LENGTH = 28;

    /**
     * The default margin between the contents and the sheet border (value=
     * <code>500</code>).
     */
    public static final int SHEET_MARGIN = 500;

    public static final int DEF_MARKER_WIDTH = 16;

    public static final int DEF_MARKER_HEIGHT = 16;

    public static final int MAX_MARKER_WIDTH = 48;

    public static final int MAX_MARKER_HEIGHT = 48;

    public static final String DEFAULT_NUMBER_FORMAT = "org.xmind.numbering.none"; //$NON-NLS-1$

    public static final String PREVIEW_NUMBER_FORMAT = "org.xmind.numbering.arabic"; //$NON-NLS-1$

    public static final String DEFAULT_NUMBER_SEPARATOR = "org.xmind.numbering.separator.dot"; //$NON-NLS-1$

    public static final String FILL_COLOR_PRESELECTION = "#80c0d0"; //$NON-NLS-1$

    public static final String LINE_COLOR_SELECTION_DISABLED = "#808080"; //$NON-NLS-1$

    public static final String LINE_COLOR_FOCUS_DISABLED = "#606060"; //$NON-NLS-1$

    public static final String LINE_COLOR_PRESELECTION = "#a8c0d8"; //$NON-NLS-1$

    public static final String LINE_COLOR_SELECTION = "#1040a8"; //$NON-NLS-1$

    public static final String LINE_COLOR_FOCUS = "#1020f0"; //$NON-NLS-1$

    public static final String CACHE_TEXT_STYLE = "org.xmind.ui.cache.textStyle"; //$NON-NLS-1$

    /**
     * Core event type for notifying a workbook being closed (value is
     * 'workbookClose'). In XMind, a workbook is closed when all editors
     * associated with it are closed. This event is dispatched after the last
     * such editor is closed.
     * <dl>
     * <dt>Source:</dt>
     * <dd>{@link org.xmind.core.IWorkbook}</dd>
     * </dl>
     */
    public static final String WorkbookClose = "workbookClose"; //$NON-NLS-1$

    public static final int DEFAULT_EXPORT_MARGIN = 15;

    public static final int NAV_SCROLL_STEP = 50;

    public MindMapUI() {
    }

    public static IMindMapImages getImages() {
        return InternalMindMapUI.getDefault().getImages();
    }

    public static IPartFactory getMindMapPartFactory() {
        return InternalMindMapUI.getDefault().getMindMapPartFactory();
    }

    public static IPartFactory getMindMapTreePartFactory() {
        return InternalMindMapUI.getDefault().getMindMapTreePartFactory();
    }

    public static IDndSupport getMindMapDndSupport() {
        return InternalMindMapUI.getDefault().getMindMapDndSupport();
    }

    public static IProtocolManager getProtocolManager() {
        return InternalMindMapUI.getDefault().getProtocolManager();
    }

    public static IBranchPolicyManager getBranchPolicyManager() {
        return InternalMindMapUI.getDefault().getBranchPolicyManager();
    }

    public static IPlaybackProvider getPlaybackProvider() {
        return InternalMindMapUI.getDefault().getPlaybackProvider();
    }

    public static boolean isAnimationEnabled() {
        return InternalMindMapUI.getDefault().isAnimationEnabled();
    }

    public static boolean isOverlapsAllowed() {
        return InternalMindMapUI.getDefault().isOverlapsAllowed();
    }

    public static boolean isGradientColor() {
        return InternalMindMapUI.getDefault().isGradientColorEnabled();
    }

    public static boolean isFreePositionMoveAllowed() {
        return InternalMindMapUI.getDefault().isFreePositionMoveAllowed();
    }

    public static IResourceManager getResourceManager() {
        return InternalMindMapUI.getDefault().getResourceManager();
    }

    public static IDecorationManager getDecorationManager() {
        return InternalMindMapUI.getDefault().getDecorationManager();
    }

    public static IDecorationFactory getMindMapDecorationFactory() {
        return InternalMindMapUI.getDefault().getMindMapDecorationFactory();
    }

    public static ICategoryManager getCategoryManager() {
        return InternalMindMapUI.getDefault().getCategoryManager();
    }

    public static IEditPolicyManager getEditPolicyManager() {
        return InternalMindMapUI.getDefault().getEditPolicyManager();
    }

    @Deprecated
    public static IWorkbookRefManager getWorkbookRefManager() {
        return InternalMindMapUI.getDefault().getWorkbookRefManager();
    }

    public static INumberFormatManager getNumberFormatManager() {
        return InternalMindMapUI.getDefault().getNumberFormatManager();
    }

    public static INumberSeparatorManager getNumberSeparatorManager() {
        return InternalMindMapUI.getDefault().getNumberSeparatorManager();
    }

    /**
     * @return
     * @deprecated Use
     *             <code>serviceLocator.getService(org.xmind.ui.editor.IEditorHistory.class)</code>
     */
    @Deprecated
    public static org.xmind.ui.IEditorHistory getEditorHistory() {
        return InternalMindMapUI.getDefault().getEditorHistory();
    }

    public static IEditorInputFactory getEditorInputFactory() {
        return InternalMindMapUI.getDefault().getEditorInputFactory();
    }

}
