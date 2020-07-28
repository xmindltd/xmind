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

import org.xmind.gef.GEF;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.command.Command;
import org.xmind.gef.dnd.DndData;
import org.xmind.gef.dnd.IDndClient;
import org.xmind.gef.dnd.IDndSupport;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.mindmap.MindMapUI;

public class DropTargetPolicy extends MindMapPolicyBase {

    public boolean understands(String requestType) {
        return super.understands(requestType)
                || GEF.REQ_DROP.equals(requestType);
    }

    public void handle(Request request) {
        String type = request.getType();
        if (GEF.REQ_DROP.equals(type)) {
            drop(request);
        }
    }

    private void drop(Request request) {
        DndData dndData = (DndData) request
                .getParameter(MindMapUI.PARAM_DND_DATA);
        if (dndData == null)
            return;

        IViewer viewer = request.getTargetViewer();
        if (viewer == null)
            return;

        IDndSupport dndSupport = viewer.getDndSupport();
        if (dndSupport == null)
            return;

        IDndClient client = dndSupport.getDndClient(dndData.clientId);
        if (client == null)
            return;

        Command command = client.makeDNDCommand(dndData.parsedData, request);
        if (command == null)
            return;

        command.setLabel(CommandMessages.Command_AddResources);
        saveAndRun(command, request.getTargetDomain());

        if (command instanceof ISourceProvider) {
            select(((ISourceProvider) command).getSources(), viewer);
        }
    }

}