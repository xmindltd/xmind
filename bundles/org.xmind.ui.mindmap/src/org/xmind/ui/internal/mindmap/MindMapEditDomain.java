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
package org.xmind.ui.internal.mindmap;

import static org.xmind.gef.GEF.TOOL_AREASELECT;
import static org.xmind.gef.GEF.TOOL_BROWSE;
import static org.xmind.gef.GEF.TOOL_DND;
import static org.xmind.gef.GEF.TOOL_EDIT;
import static org.xmind.gef.GEF.TOOL_SELECT;
import static org.xmind.gef.GEF.TOOL_TRAVERSE;
import static org.xmind.ui.mindmap.MindMapUI.REQ_ADD_ATTACHMENT;
import static org.xmind.ui.mindmap.MindMapUI.REQ_ADD_IMAGE;
import static org.xmind.ui.mindmap.MindMapUI.REQ_ADD_MARKER;
import static org.xmind.ui.mindmap.MindMapUI.REQ_CREATE_BEFORE;
import static org.xmind.ui.mindmap.MindMapUI.REQ_CREATE_BOUNDARY;
import static org.xmind.ui.mindmap.MindMapUI.REQ_CREATE_CALLOUT;
import static org.xmind.ui.mindmap.MindMapUI.REQ_CREATE_CHILD;
import static org.xmind.ui.mindmap.MindMapUI.REQ_CREATE_FLOAT;
import static org.xmind.ui.mindmap.MindMapUI.REQ_CREATE_PARENT;
import static org.xmind.ui.mindmap.MindMapUI.REQ_CREATE_RELATIONSHIP;
import static org.xmind.ui.mindmap.MindMapUI.REQ_CREATE_SHEET;
import static org.xmind.ui.mindmap.MindMapUI.REQ_CREATE_SUMMARY;
import static org.xmind.ui.mindmap.MindMapUI.REQ_DUPLICATE_TOPIC;
import static org.xmind.ui.mindmap.MindMapUI.REQ_EDIT_LABEL;
import static org.xmind.ui.mindmap.MindMapUI.REQ_HIDE_LEGEND;
import static org.xmind.ui.mindmap.MindMapUI.REQ_MODIFY_HYPERLINK;
import static org.xmind.ui.mindmap.MindMapUI.REQ_MODIFY_LABEL;
import static org.xmind.ui.mindmap.MindMapUI.REQ_MODIFY_NUMBERING;
import static org.xmind.ui.mindmap.MindMapUI.REQ_MODIFY_RANGE;
import static org.xmind.ui.mindmap.MindMapUI.REQ_MODIFY_STYLE;
import static org.xmind.ui.mindmap.MindMapUI.REQ_MODIFY_THEME;
import static org.xmind.ui.mindmap.MindMapUI.REQ_MOVE_CONTROL_POINT;
import static org.xmind.ui.mindmap.MindMapUI.REQ_NAV_CHILD;
import static org.xmind.ui.mindmap.MindMapUI.REQ_NAV_SIBLING;
import static org.xmind.ui.mindmap.MindMapUI.REQ_REMOVE_ALLSTYLES;
import static org.xmind.ui.mindmap.MindMapUI.REQ_REPLACE_ALL;
import static org.xmind.ui.mindmap.MindMapUI.REQ_RESET_POSITION;
import static org.xmind.ui.mindmap.MindMapUI.REQ_RETARGET_REL;
import static org.xmind.ui.mindmap.MindMapUI.REQ_SHOW_LEGEND;
import static org.xmind.ui.mindmap.MindMapUI.REQ_TILE;
import static org.xmind.ui.mindmap.MindMapUI.TOOL_CREATE_BOUNDARY;
import static org.xmind.ui.mindmap.MindMapUI.TOOL_CREATE_FLOAT;
import static org.xmind.ui.mindmap.MindMapUI.TOOL_CREATE_LEGEND;
import static org.xmind.ui.mindmap.MindMapUI.TOOL_CREATE_RELATIONSHIP;
import static org.xmind.ui.mindmap.MindMapUI.TOOL_CREATE_SUMMARY;
import static org.xmind.ui.mindmap.MindMapUI.TOOL_EDIT_LABEL;
import static org.xmind.ui.mindmap.MindMapUI.TOOL_EDIT_LEGEND_ITEM;
import static org.xmind.ui.mindmap.MindMapUI.TOOL_EDIT_TOPIC_TITLE;
import static org.xmind.ui.mindmap.MindMapUI.TOOL_MOVE_IMAGE;
import static org.xmind.ui.mindmap.MindMapUI.TOOL_MOVE_LEGEND;
import static org.xmind.ui.mindmap.MindMapUI.TOOL_MOVE_MARKER;
import static org.xmind.ui.mindmap.MindMapUI.TOOL_MOVE_RELATIONSHIP;
import static org.xmind.ui.mindmap.MindMapUI.TOOL_MOVE_TOPIC;
import static org.xmind.ui.mindmap.MindMapUI.TOOL_RESIZE_IMAGE;
import static org.xmind.ui.mindmap.MindMapUI.TOOL_RESIZE_RANGE;

import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.policy.IEditPolicy;
import org.xmind.gef.policy.NullEditPolicy;
import org.xmind.gef.tool.BrowsingTool;
import org.xmind.gef.tool.ITool;
import org.xmind.ui.internal.tools.BoundaryCreateTool;
import org.xmind.ui.internal.tools.FloatingTopicCreateTool;
import org.xmind.ui.internal.tools.ImageMoveTool;
import org.xmind.ui.internal.tools.ImageResizeTool;
import org.xmind.ui.internal.tools.LabelEditTool;
import org.xmind.ui.internal.tools.LegendCreateTool;
import org.xmind.ui.internal.tools.LegendItemEditTool;
import org.xmind.ui.internal.tools.LegendMoveTool;
import org.xmind.ui.internal.tools.MarkerMoveTool;
import org.xmind.ui.internal.tools.MindMapDndTool;
import org.xmind.ui.internal.tools.MindMapSelectTool;
import org.xmind.ui.internal.tools.MindMapTraverseTool;
import org.xmind.ui.internal.tools.RangeResizeTool;
import org.xmind.ui.internal.tools.RelationshipCreateTool;
import org.xmind.ui.internal.tools.RelationshipMoveTool;
import org.xmind.ui.internal.tools.SheetTitleEditTool;
import org.xmind.ui.internal.tools.SummaryCreateTool;
import org.xmind.ui.internal.tools.TopicAreaSelectTool;
import org.xmind.ui.internal.tools.TopicMoveTool;
import org.xmind.ui.internal.tools.TopicTitleEditTool;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.tools.TitleEditTool;

public class MindMapEditDomain extends EditDomain {

    public MindMapEditDomain() {
        getPartRoles().setRole(REQ_CREATE_CALLOUT, GEF.ROLE_CREATABLE);

        getPartRoles().setRole(REQ_CREATE_CHILD, GEF.ROLE_CREATABLE);
        getPartRoles().setRole(REQ_CREATE_BEFORE, GEF.ROLE_CREATABLE);
        getPartRoles().setRole(REQ_CREATE_PARENT, GEF.ROLE_CREATABLE);
        getPartRoles().setRole(REQ_CREATE_SHEET, GEF.ROLE_CREATABLE);
        getPartRoles().setRole(REQ_DUPLICATE_TOPIC, GEF.ROLE_CREATABLE);

        getPartRoles().setRole(REQ_CREATE_FLOAT, GEF.ROLE_CREATABLE);
        getPartRoles().setRole(REQ_CREATE_BOUNDARY, GEF.ROLE_CREATABLE);
        getPartRoles().setRole(REQ_CREATE_SUMMARY, GEF.ROLE_CREATABLE);
        getPartRoles().setRole(REQ_ADD_MARKER, GEF.ROLE_CREATABLE);
        getPartRoles().setRole(REQ_ADD_ATTACHMENT, GEF.ROLE_CREATABLE);
        getPartRoles().setRole(REQ_ADD_IMAGE, GEF.ROLE_CREATABLE);
        getPartRoles().setRole(REQ_NAV_SIBLING, GEF.ROLE_NAVIGABLE);
        getPartRoles().setRole(REQ_NAV_CHILD, GEF.ROLE_NAVIGABLE);

        getPartRoles().setRole(REQ_EDIT_LABEL, GEF.ROLE_EDITABLE);
        getPartRoles().setRole(REQ_REPLACE_ALL, GEF.ROLE_EDITABLE);

        getPartRoles().setRole(REQ_MODIFY_HYPERLINK, GEF.ROLE_MODIFIABLE);
        getPartRoles().setRole(REQ_CREATE_RELATIONSHIP, GEF.ROLE_CREATABLE);
        getPartRoles().setRole(REQ_MOVE_CONTROL_POINT, GEF.ROLE_MOVABLE);
        getPartRoles().setRole(REQ_RETARGET_REL, GEF.ROLE_MOVABLE);
        getPartRoles().setRole(REQ_MODIFY_STYLE, GEF.ROLE_MODIFIABLE);
        getPartRoles().setRole(REQ_MODIFY_RANGE, GEF.ROLE_MODIFIABLE);
        getPartRoles().setRole(REQ_MODIFY_LABEL, GEF.ROLE_MODIFIABLE);
        getPartRoles().setRole(REQ_MODIFY_NUMBERING, GEF.ROLE_MODIFIABLE);
        getPartRoles().setRole(REQ_RESET_POSITION, GEF.ROLE_MODIFIABLE);
        getPartRoles().setRole(REQ_MODIFY_THEME, GEF.ROLE_MODIFIABLE);
        getPartRoles().setRole(REQ_REMOVE_ALLSTYLES, GEF.ROLE_MODIFIABLE);

        getPartRoles().setRole(REQ_SHOW_LEGEND, MindMapUI.ROLE_MAP);
        getPartRoles().setRole(REQ_HIDE_LEGEND, MindMapUI.ROLE_MAP);
        getPartRoles().setRole(REQ_TILE, MindMapUI.ROLE_MAP);

        installTool(TOOL_SELECT, new MindMapSelectTool());
        installTool(TOOL_BROWSE, new BrowsingTool());
        installTool(TOOL_EDIT, new TitleEditTool());
        installTool(TOOL_AREASELECT, new TopicAreaSelectTool());
        installTool(TOOL_TRAVERSE, new MindMapTraverseTool());
        installTool(TOOL_DND, new MindMapDndTool());
        installTool(TOOL_CREATE_FLOAT, new FloatingTopicCreateTool());
        installTool(TOOL_EDIT_TOPIC_TITLE, new TopicTitleEditTool());
        installTool(TOOL_CREATE_RELATIONSHIP, new RelationshipCreateTool());
        installTool(TOOL_MOVE_RELATIONSHIP, new RelationshipMoveTool());
        installTool(TOOL_CREATE_BOUNDARY, new BoundaryCreateTool());
        installTool(TOOL_CREATE_SUMMARY, new SummaryCreateTool());
        installTool(TOOL_RESIZE_RANGE, new RangeResizeTool());
        installTool(TOOL_EDIT_LABEL, new LabelEditTool());
        installTool(TOOL_RESIZE_IMAGE, new ImageResizeTool());
        installTool(TOOL_MOVE_TOPIC, new TopicMoveTool());
        installTool(TOOL_MOVE_IMAGE, new ImageMoveTool());
        installTool(TOOL_MOVE_MARKER, new MarkerMoveTool());
        installTool(TOOL_MOVE_LEGEND, new LegendMoveTool());
        installTool(TOOL_CREATE_LEGEND, new LegendCreateTool());
        installTool(TOOL_EDIT_LEGEND_ITEM, new LegendItemEditTool());
        installTool(MindMapUI.TOOL_EDIT_SHEET_TITLE, new SheetTitleEditTool());

        setDefaultTool(GEF.TOOL_SELECT);

    }

    public IEditPolicy getEditPolicy(String role, String id) {
        IEditPolicy editPolicy = super.getEditPolicy(role, id);
        if (editPolicy == null || editPolicy == NullEditPolicy.getInstance())
            return MindMapUI.getEditPolicyManager().getEditPolicy(id);
        return editPolicy;
    }

    public ITool getTool(String id) {
        ITool tool = super.getTool(id);
        if (tool == null) {
            tool = createToolExtension(id);
            if (tool != null) {
                installTool(id, tool);
            }
        }
        return tool;
    }

    private ITool createToolExtension(String id) {
        return ToolExtensionRegistry.getInstance().createTool(id);
    }
}
