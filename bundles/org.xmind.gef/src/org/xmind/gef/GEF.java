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
package org.xmind.gef;

import org.eclipse.jface.util.Util;

/**
 * @author Brian Sun
 * @version 2005
 */
public class GEF {

    /*
     * Request Types:
     */
    public static final String REQ_DEBUG = "debug"; //$NON-NLS-1$

    public static final String REQ_SELECT = "select"; //$NON-NLS-1$
    public static final String REQ_SELECT_NONE = "select_none"; //$NON-NLS-1$
    public static final String REQ_SELECT_SINGLE = "select_single"; //$NON-NLS-1$
    public static final String REQ_SELECT_MULTI = "select_multi"; //$NON-NLS-1$
    public static final String REQ_SELECT_ALL = "select_all"; //$NON-NLS-1$

    public static final String REQ_ZOOM = "zoom"; //$NON-NLS-1$
    public static final String REQ_ZOOMIN = "zoom_in"; //$NON-NLS-1$
    public static final String REQ_ZOOMOUT = "zoom_out"; //$NON-NLS-1$
    public static final String REQ_ACTUALSIZE = "actual_size"; //$NON-NLS-1$
    public static final String REQ_FITSIZE = "fit_size"; //$NON-NLS-1$
    public static final String REQ_FITSELECTION = "fit_selection"; //$NON-NLS-1$

    public static final String REQ_COPY = "copy"; //$NON-NLS-1$
    public static final String REQ_CUT = "cut"; //$NON-NLS-1$
    public static final String REQ_PASTE = "paste"; //$NON-NLS-1$
    public static final String REQ_REDO = "redo"; //$NON-NLS-1$
    public static final String REQ_UNDO = "undo"; //$NON-NLS-1$
    public static final String REQ_REPEAT = "repeat"; //$NON-NLS-1$

    public static final String REQ_MODIFY = "modify"; //$NON-NLS-1$
    public static final String REQ_RESIZE = "resize"; //$NON-NLS-1$
    public static final String REQ_EDIT = "edit"; //$NON-NLS-1$
    public static final String REQ_OPEN = "open"; //$NON-NLS-1$

    public static final String REQ_CANCEL = "cancel"; //$NON-NLS-1$
    public static final String REQ_FINISH = "finish"; //$NON-NLS-1$

    public static final String REQ_CREATE = "create"; //$NON-NLS-1$
    public static final String REQ_DELETE = "delete"; //$NON-NLS-1$
    public static final String REQ_MOVETO = "move to"; //$NON-NLS-1$
    public static final String REQ_COPYTO = "copy to"; //$NON-NLS-1$

    public static final String REQ_MOVE_UP = "move_up"; //$NON-NLS-1$
    public static final String REQ_MOVE_DOWN = "move_down"; //$NON-NLS-1$
    public static final String REQ_MOVE_LEFT = "move_left"; //$NON-NLS-1$
    public static final String REQ_MOVE_RIGHT = "move_right"; //$NON-NLS-1$

    public static final String REQ_NAV_UP = "navigate_up"; //$NON-NLS-1$
    public static final String REQ_NAV_DOWN = "navigate_down"; //$NON-NLS-1$
    public static final String REQ_NAV_LEFT = "navigate_left"; //$NON-NLS-1$
    public static final String REQ_NAV_RIGHT = "navigate_right"; //$NON-NLS-1$
    public static final String REQ_NAV_BEGINNING = "navigate_beginning"; //$NON-NLS-1$
    public static final String REQ_NAV_END = "navigate_end"; //$NON-NLS-1$
    public static final String REQ_NAV_BACK = "navigate_backward"; //$NON-NLS-1$
    public static final String REQ_NAV_FORWARD = "navigate_forward"; //$NON-NLS-1$
    public static final String REQ_NAV_NEXT = "navigate_next"; //$NON-NLS-1$
    public static final String REQ_NAV_PREV = "navigate_previous"; //$NON-NLS-1$
    public static final String REQ_MOVE_PREV = "move_previous"; //$NON-NLS-1$
    public static final String REQ_MOVE_NEXT = "move_next"; //$NON-NLS-1$

    public static final String REQ_TRAVERSE = "traverse"; //$NON-NLS-1$
    public static final String REQ_GET_TRAVERSABLES = "get_traversables"; //$NON-NLS-1$

    public static final String REQ_EXTEND = "extend"; //$NON-NLS-1$
    public static final String REQ_COLLAPSE = "collapse"; //$NON-NLS-1$
    public static final String REQ_EXTEND_ALL = "extend_all"; //$NON-NLS-1$
    public static final String REQ_COLLAPSE_ALL = "collapse_all"; //$NON-NLS-1$

    public static final String REQ_SHOW = "show"; //$NON-NLS-1$
    public static final String REQ_HIDE = "hide"; //$NON-NLS-1$
    public static final String REQ_SHOW_ALL = "show_all"; //$NON-NLS-1$
    public static final String REQ_HIDE_ALL = "hide_all"; //$NON-NLS-1$
    public static final String REQ_SHOW_OTHER = "show_other"; //$NON-NLS-1$
    public static final String REQ_SHOW_ONLY = "show_only"; //$NON-NLS-1$

    public static final String REQ_ALIGN = "align"; //$NON-NLS-1$
    public static final String REQ_SORT = "sort"; //$NON-NLS-1$

    public static final String REQ_DROP = "drop"; //$NON-NLS-1$

    public static final String REQ_CONNECT = "connect"; //$NON-NLS-1$

    /*
     * Tool Status:
     */
    public static final int ST_ACTIVE = 1;
    public static final int ST_ALT_PRESSED = 1 << 1;
    public static final int ST_CONTROL_PRESSED = 1 << 2;
    public static final int ST_SHIFT_PRESSED = 1 << 3;
    public static final int ST_MOUSE_HOVER = 1 << 4;
    public static final int ST_MOUSE_DOUBLECLICKING = 1 << 5;
    public static final int ST_MOUSE_PRESSED = 1 << 6;
    public static final int ST_MOUSE_DRAGGING = 1 << 7;
    public static final int ST_MOUSE_RIGHT = 1 << 8;
    public static final int ST_HIDE_CMENU = 1 << 9;
    public static final int ST_FORCE_CMENU = 1 << 10;
    public static final int ST_NO_DRAGGING = 1 << 11;
    public static final int ST_FREE_MOVE_MODE = 1 << 12;

    public static final int ST_MODIFIER_MASK = ST_ALT_PRESSED
            | ST_CONTROL_PRESSED | ST_SHIFT_PRESSED;

    /*
     * Parts Status:
     */
    public static final int PART_ACTIVE = 1;
    public static final int PART_SELECTED = 1 << 1;
    public static final int PART_PRESELECTED = 1 << 2;
    public static final int PART_FOCUSED = 1 << 3;
    public static final int PART_SEL_MASK = PART_PRESELECTED | PART_SELECTED
            | PART_FOCUSED;
    public static final int PART_PRIM_SEL_MASK = PART_SELECTED | PART_FOCUSED;

    /*
     * Part Roles:
     */
    public static final String ROLE_CANVAS = "canvas role"; //$NON-NLS-1$
    public static final String ROLE_MOVABLE = "movable role"; //$NON-NLS-1$
    public static final String ROLE_MODIFIABLE = "modifiable role"; //$NON-NLS-1$
    public static final String ROLE_SELECTABLE = "selectable role"; //$NON-NLS-1$
    public static final String ROLE_NAVIGABLE = "navigable role"; //$NON-NLS-1$
    public static final String ROLE_TRAVERSABLE = "traversable role"; //$NON-NLS-1$
    public static final String ROLE_CONTAINER = "container role"; //$NON-NLS-1$
    public static final String ROLE_CREATABLE = "creatable role"; //$NON-NLS-1$
    public static final String ROLE_DELETABLE = "deletable role"; //$NON-NLS-1$
    public static final String ROLE_EXTENDABLE = "extendable role"; //$NON-NLS-1$
    public static final String ROLE_SCALABLE = "scalable role"; //$NON-NLS-1$
    public static final String ROLE_EDITABLE = "editable role"; //$NON-NLS-1$
    public static final String ROLE_FILTERABLE = "filterable role"; //$NON-NLS-1$
    public static final String ROLE_CONNECTABLE = "connectable role"; //$NON-NLS-1$
    public static final String ROLE_DROP_TARGET = "drop target role"; //$NON-NLS-1$
    public static final String ROLE_MAP_MOVABLE = "map movable role"; //$NON-NLS-1$

    public static final String ROLE_SORTABLE = "sortable role"; //$NON-NLS-1$

    /*
     * Command Types:
     */
    public static final int CMD_NORMAL = 0;
    public static final int CMD_CREATE = 1;
    public static final int CMD_DELETE = 2;
    public static final int CMD_MODIFY = 3;

    /*
     * CommandStack Events:
     */
    public static final int CS_PRE_EXECUTE = 1;
    public static final int CS_PRE_REDO = 1 << 1;
    public static final int CS_PRE_UNDO = 1 << 2;
    public static final int CS_POST_EXECUTE = 1 << 3;
    public static final int CS_POST_REDO = 1 << 4;
    public static final int CS_POST_UNDO = 1 << 5;
    public static final int CS_COMMAND_PUSHED = 1 << 6;
    public static final int CS_UPDATED = 1 << 7;

    public static final int CS_PRE_MASK = CS_PRE_EXECUTE | CS_PRE_UNDO
            | CS_PRE_REDO;
    public static final int CS_POST_MASK = CS_POST_EXECUTE | CS_POST_UNDO
            | CS_POST_REDO;

    /*
     * Tools:
     */
    public static final String TOOL_DEFAULT = "default tool"; //$NON-NLS-1$
    public static final String TOOL_SELECT = "select tool"; //$NON-NLS-1$
    public static final String TOOL_AREASELECT = "area select tool"; //$NON-NLS-1$
    public static final String TOOL_TRAVERSE = "traverse tool"; //$NON-NLS-1$
    public static final String TOOL_CREATE = "create tool"; //$NON-NLS-1$
    public static final String TOOL_MOVE = "move tool"; //$NON-NLS-1$
    public static final String TOOL_BROWSE = "browse tool"; //$NON-NLS-1$
    public static final String TOOL_EDIT = "edit tool"; //$NON-NLS-1$
    public static final String TOOL_RESIZE = "resize tool"; //$NON-NLS-1$
    public static final String TOOL_AREACREATE = "area create tool"; //$NON-NLS-1$
    public static final String TOOL_DND = "dnd tool"; //$NON-NLS-1$
    public static final String TOOL_PREVIEW = "preview tool"; //$NON-NLS-1$

    /*
     * Layers:
     */
    public static final Object LAYERS_SCALABLE = "scalable layers"; //$NON-NLS-1$
    public static final Object LAYER_BACKGROUND = "background layer"; //$NON-NLS-1$
    public static final Object LAYER_CONTENTS = "contents layer"; //$NON-NLS-1$
    public static final Object LAYER_PRESENTATION = "presentation layer"; //$NON-NLS-1$
    public static final Object LAYER_FEEDBACK = "feedback layer"; //$NON-NLS-1$
    public static final Object LAYER_SHADOW = "shadow layer"; //$NON-NLS-1$
    public static final Object LAYER_HANDLE = "handle layer"; //$NON-NLS-1$

    /*
     * Viewer property
     */
    public static final String SelectionConstraint = "selection constraint"; //$NON-NLS-1$
    public static final int SEL_EMPTY = 1;
    public static final int SEL_SINGLE = 1 << 1;
    public static final int SEL_MULTI = 1 << 2;
    public static final int SEL_DEFAULT = SEL_EMPTY | SEL_SINGLE | SEL_MULTI;

    /**
     * Font case
     */
    public static final int CASE_EMPTY = 0;
    public static final int MANUAL = 1;
    public static final int UPPERCASE = 1 << 1;
    public static final int LOWERCASE = 1 << 2;
    public static final int CAPITALIZE = 1 << 3;

    /**
     * Request parameter: the source part from which the request is sent,
     * typically used by a dragging tool to request the target part to show
     * feedback or handle connection command.
     * <dl>
     * <dt>Values:</dt>
     * <dd>a {@link org.xmind.gef.part.IPart}</dd>
     * </dl>
     */
    public static final String PARAM_SOURCE = "source"; //$NON-NLS-1$

    /**
     * Request parameter: the size of a size request.
     * <dl>
     * <dt>Values:</dt>
     * <dd>a {@link org.eclipse.draw2d.geometry.Dimension}</dd>
     * </dl>
     */
    public static final String PARAM_SIZE = "size"; //$NON-NLS-1$

    /**
     * Request parameter: the text of a text request.
     * <dl>
     * <dt>Values:</dt>
     * <dd><code>String</code></dd>
     * </dl>
     */
    public static final String PARAM_TEXT = "text"; //$NON-NLS-1$

    /**
     * Request parameter: the position of a position request.
     * <dl>
     * <dt>Values:</dt>
     * <dd>a {@link org.eclipse.draw2d.geometry.Point}</dd>
     * </dl>
     */
    public static final String PARAM_POSITION = "position"; //$NON-NLS-1$

    /**
     * Request parameter: whether the position is relative.
     * <dl>
     * <dt>Values:</dt>
     * <dd><code>Boolean</code></dd>
     * </dl>
     */
    public static final String PARAM_POSITION_RELATIVE = "positionRelative"; //$NON-NLS-1$

    /**
     * Request parameter: the absolute position of a position request.
     * <dl>
     * <dt>Values:</dt>
     * <dd>a {@link org.eclipse.draw2d.geometry.Point}</dd>
     * </dl>
     */
    public static final String PARAM_POSITION_ABSOLUTE = "positionAbsolute"; //$NON-NLS-1$

    /**
     * Request parameter: the text selection.
     * <dl>
     * <dt>Values:</dt>
     * <dd>an {@link org.eclipse.jface.text.ITextSelection}</dd>
     * </dl>
     */
    public static final String PARAM_TEXT_SELECTION = "textSelection"; //$NON-NLS-1$

    /**
     * Request parameter: the scale of a zoom request.
     * <dl>
     * <dt>Values:</dt>
     * <dd><code>Double</code></dd>
     * </dl>
     */
    public static final String PARAM_ZOOM_SCALE = "zoomScale"; //$NON-NLS-1$

    /**
     * Request parameter: the target parent of a 'move to' or 'copy to' request.
     * <dl>
     * <dt>Values:</dt>
     * <dd>a {@link org.xmind.gef.part.IPart}</dd>
     * </dl>
     */
    public static final String PARAM_PARENT = "parent"; //$NON-NLS-1$

    /**
     * Request parameter: the new index of a 'move to' or 'copy to' request.
     * <dl>
     * <dt>Values:</dt>
     * <dd>an <code>Integer</code></dd>
     * </dl>
     */
    public static final String PARAM_INDEX = "index"; //$NON-NLS-1$

    /**
     * Request parameter: the alignment hint of a 'align' request.
     * <dl>
     * <dt>Values:</dt>
     * <dd>LEFT, CENTER, RIGHT, TOP, MIDDLE, BOTTOM</dd>
     * </dl>
     *
     * @see org.eclipse.draw2d.PositionConstants#LEFT
     * @see org.eclipse.draw2d.PositionConstants#CENTER
     * @see org.eclipse.draw2d.PositionConstants#RIGHT
     * @see org.eclipse.draw2d.PositionConstants#TOP
     * @see org.eclipse.draw2d.PositionConstants#MIDDLE
     * @see org.eclipse.draw2d.PositionConstants#BOTTOM
     */
    public static final String PARAM_ALIGNMENT = "alignment"; //$NON-NLS-1$

    /**
     * Request parameter: the method to compare two elements when handling a
     * 'sort' request.
     */
    public static final String PARAM_COMPARAND = "comparand"; //$NON-NLS-1$

    /**
     * Request parameter: whether the navigation is sequential.
     * <dl>
     * <dt>Values:</dt>
     * <dd><code>Boolean</code></dd>
     * </dl>
     */
    public static final String PARAM_NAV_SEQUENTIAL = "navigationSequential"; //$NON-NLS-1$

    /**
     * Request parameter: the starting part of the sequential navigation.
     * <dl>
     * <dt>Values:</dt>
     * <dd>{@link org.xmind.gef.part.IPart}</dd>
     * </dl>
     */
    public static final String PARAM_NAV_SEQUENCE_START = "navigationSequenceStart"; //$NON-NLS-1$

    /**
     * Request parameter: file path(s) of a path request.
     * <dl>
     * <dt>Values:</dt>
     * <dd>a single file path (<code>String</code>), or an array of file paths (
     * <code>String[]</code>)</dd>
     * </dl>
     */
    public static final String PARAM_PATH = "paths"; //$NON-NLS-1$

    /**
     * Request parameter: whether to take focus on start.
     * <dl>
     * <dt>Values:</dt>
     * <dd><code>Boolean</code></dd>
     * </dl>
     */
    public static final String PARAM_FOCUS = "focus"; //$NON-NLS-1$

    /**
     * Request parameter: drop operation identifier
     * <dl>
     * <dt>Values:</dt>
     * <dd>{@link org.eclipse.swt.dnd.DND#DROP_NONE}</dd>
     * <dd>{@link org.eclipse.swt.dnd.DND#DROP_COPY}</dd>
     * <dd>{@link org.eclipse.swt.dnd.DND#DROP_MOVE}</dd>
     * <dd>{@link org.eclipse.swt.dnd.DND#DROP_LINK}</dd>
     * <dd>{@link org.eclipse.swt.dnd.DND#DROP_DEFAULT}</dd>
     * </dl>
     */
    public static final String PARAM_DROP_OPERATION = "dropOperation"; //$NON-NLS-1$

    /**
     * Result key of the traverse request to retrieve the traversable parts
     * selected by a <code>TraversablePolicy</code>.
     * <dl>
     * <dt>Values:</dt>
     * <dd>an array of parts (<code>{@link org.xmind.gef.part.IPart}[]</code>)
     * </dd>
     * </dl>
     */
    public static final String RESULT_TRAVERSE = "traverseResult"; //$NON-NLS-1$

    /**
     * Result key of the navigation request to retrieve the target parts
     * selected by a <code>NavigablePolicy</code>.
     * <dl>
     * <dt>Values:</dt>
     * <dd>an array of parts (<code>{@link org.xmind.gef.part.IPart}[]</code>)
     * </dd>
     * </dl>
     */
    public static final String RESULT_NAVIGATION = "navigationResult"; //$NON-NLS-1$

    /**
     * Result key of the navigation request to retrieve the part to be focused.
     * <dl>
     * <dt>Values:</dt>
     * <dd>an <code>{@link org.xmind.gef.part.IPart}</code></dd>
     * </dl>
     */
    public static final String RESULT_NEW_FOCUS = "newFocus"; //$NON-NLS-1$

    /*
     * Graphics Hints:
     */
    public static final boolean IS_PLATFORM_SUPPORT_GRADIENT = true;

    private static Boolean textPathSupported = null;

    public static boolean isTextPathSupported() {
        if (textPathSupported == null) {
            textPathSupported = Boolean.valueOf(Util.isWindows());
        }
        return textPathSupported.booleanValue();
    }

    private GEF() {
    }

}
