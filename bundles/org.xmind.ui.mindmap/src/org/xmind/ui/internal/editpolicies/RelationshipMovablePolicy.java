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
package org.xmind.ui.internal.editpolicies;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.xmind.core.IControlPoint;
import org.xmind.core.IRelationship;
import org.xmind.core.IRelationshipEnd;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.part.IPart;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.ModifyPositionCommand;
import org.xmind.ui.commands.ModifyRelationshipEndCommand;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class RelationshipMovablePolicy extends MindMapPolicyBase {

    public boolean understands(String requestType) {
        return super.understands(requestType)
                || MindMapUI.REQ_MOVE_CONTROL_POINT.equals(requestType)
                || MindMapUI.REQ_RETARGET_REL.equals(requestType);
    }

    public void handle(Request request) {
        String type = request.getType();
        if (MindMapUI.REQ_MOVE_CONTROL_POINT.equals(type)) {
            performMoveControlPoint(request);
        } else if (MindMapUI.REQ_RETARGET_REL.equals(type)) {
            performRetargetRelationship(request);
        }
    }

    private void performRetargetRelationship(Request request) {
        Object param = request.getParameter(MindMapUI.PARAM_MOVE_REL_POINT_ID);
        if (param == null || !(param instanceof Integer))
            return;

        int pointId = ((Integer) param).intValue();
        boolean sourceOrTarget;
        if (pointId == MindMapUI.SOURCE_ANCHOR) {
            sourceOrTarget = true;
        } else if (pointId == MindMapUI.TARGET_ANCHOR) {
            sourceOrTarget = false;
        } else {
            return;
        }

        IPart newNode = (IPart) request
                .getParameter(MindMapUI.PARAM_MOVE_REL_NEW_NODE);
        if (newNode == null)
            return;

        Object newEnd = MindMapUtils.getRealModel(newNode);
        String newEndId;
        if (newEnd instanceof IRelationshipEnd) {
            newEndId = ((IRelationshipEnd) newEnd).getId();
        } else {
            return;
        }

        List<IPart> sources = request.getTargets();
        List<IRelationship> rels = new ArrayList<IRelationship>(sources.size());
        for (IPart p : sources) {
            Object o = MindMapUtils.getRealModel(p);
            if (o instanceof IRelationship) {
                rels.add((IRelationship) o);
            }
        }
        if (rels.isEmpty())
            return;

        ModifyRelationshipEndCommand cmd = new ModifyRelationshipEndCommand(
                rels, newEndId, sourceOrTarget);
        cmd.setLabel(CommandMessages.Command_RetargetRelationship);
        saveAndRun(cmd, request.getTargetDomain());
    }

    private void performMoveControlPoint(Request request) {
        if (!request.hasParameter(GEF.PARAM_POSITION))
            return;

        Point position = (Point) request.getParameter(GEF.PARAM_POSITION);

        Object param = request.getParameter(MindMapUI.PARAM_MOVE_REL_POINT_ID);
        if (param == null || !(param instanceof Integer))
            return;

        int pointId = ((Integer) param).intValue();
        int index;
        if (pointId == MindMapUI.SOURCE_CONTROL_POINT) {
            index = 0;
        } else if (pointId == MindMapUI.TARGET_CONTROL_POINT) {
            index = 1;
        } else {
            return;
        }

//        Double newAngle = (Double) request
//                .getParameter(MindMapUI.PARAM_MOVE_REL_NEW_ANGLE);
//        Double newAmount = (Double) request
//                .getParameter(MindMapUI.PARAM_MOVE_REL_NEW_AMOUNT);
//        if (newAngle == null || newAmount == null)
//            return;

        List<IPart> sources = request.getTargets();
        List<IControlPoint> controlPoints = new ArrayList<IControlPoint>(
                sources.size());
        for (IPart p : sources) {
            Object o = MindMapUtils.getRealModel(p);
            if (o instanceof IRelationship) {
                controlPoints.add(((IRelationship) o).getControlPoint(index));
            }
        }
        if (controlPoints.isEmpty())
            return;

        ModifyPositionCommand cmd = new ModifyPositionCommand(controlPoints,
                MindMapUtils.toModelPosition(position));
//        ModifyRelationshipControlPointCommand cmd = new ModifyRelationshipControlPointCommand(
//                rels, index, newAngle.doubleValue(), newAmount.doubleValue());
        cmd.setLabel(CommandMessages.Command_MoveRelationshipControlPoint);
        saveAndRun(cmd, request.getTargetDomain());
    }
}