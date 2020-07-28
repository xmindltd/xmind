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

import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerRef;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.part.IPart;
import org.xmind.ui.commands.AddMarkerCommand;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.DeleteMarkerCommand;
import org.xmind.ui.mindmap.IMarkerPart;
import org.xmind.ui.mindmap.ITopicPart;

public class MarkerMovablePolicy extends MindMapPolicyBase {

    public boolean understands(String requestType) {
        return super.understands(requestType)
                || GEF.REQ_MOVETO.equals(requestType)
                || GEF.REQ_COPYTO.equals(requestType);
    }

    public void handle(Request request) {
        String type = request.getType();
        if (GEF.REQ_MOVETO.equals(type)) {
            moveMarker(request);
        } else if (GEF.REQ_COPYTO.equals(type)) {
            copyMarker(request);
        }
    }

    private void moveMarker(Request request) {
        copyMarker(request, true);
    }

    private void copyMarker(Request request) {
        copyMarker(request, false);
    }

    private void copyMarker(Request request, boolean deleteSource) {
        IPart primaryTarget = request.getPrimaryTarget();
        if (!(primaryTarget instanceof IMarkerPart))
            return;

        IMarkerPart marker = (IMarkerPart) primaryTarget;

        Object param = request.getParameter(GEF.PARAM_PARENT);
        if (param == null || !(param instanceof ITopicPart))
            return;

        ITopicPart targetParent = (ITopicPart) param;
        ITopic targetTopic = targetParent.getTopic();
        IMarkerRef sourceMarker = marker.getMarkerRef();
        if (sourceMarker.getParent() == targetTopic)
            return;

        List<Command> cmds = new ArrayList<Command>();
        createAddMarkerCommand(targetTopic, sourceMarker.getMarkerId(), cmds);
        if (cmds.isEmpty())
            return;

        if (deleteSource) {
            DeleteMarkerCommand delete = new DeleteMarkerCommand(sourceMarker
                    .getParent(), sourceMarker.getMarkerId());
            delete.setSourceCollectable(false);
            cmds.add(delete);
        }

        CompoundCommand cmd = new CompoundCommand(cmds);
        cmd.setLabel(deleteSource ? CommandMessages.Command_MoveMarker
                : CommandMessages.Command_CopyMarker);
        saveAndRun(cmd, request.getTargetDomain());
        select(cmd.getSources(), request.getTargetViewer());
    }

    private void createAddMarkerCommand(ITopic topic, String newMarkerId,
            List<Command> cmds) {
        if (topic.hasMarker(newMarkerId))
            return;

        IMarker marker = findMarker(topic, newMarkerId);
        if (marker != null) {
            IMarkerGroup group = marker.getParent();
            if (group != null && group.isSingleton()) {
                removeSingletonMarkers(topic, newMarkerId, group, cmds);
            }
            cmds.add(new AddMarkerCommand(topic, newMarkerId));
        }
    }

    private IMarker findMarker(ITopic topic, String markerId) {
        IWorkbook workbook = topic.getOwnedWorkbook();
        if (workbook != null) {
            IMarkerSheet markerSheet = workbook.getMarkerSheet();
            if (markerSheet != null) {
                return markerSheet.findMarker(markerId);
            }
        }
        return null;
    }

    private void removeSingletonMarkers(ITopic topic, String newMarkerId,
            IMarkerGroup group, List<Command> cmds) {
        for (IMarker m : group.getMarkers()) {
            String markerId = m.getId();
            if (!newMarkerId.equals(markerId)) {
                if (topic.hasMarker(markerId)) {
                    DeleteMarkerCommand cmd = new DeleteMarkerCommand(topic,
                            markerId);
                    cmd.setSourceCollectable(false);
                    cmds.add(cmd);
                }
            }
        }
    }

}