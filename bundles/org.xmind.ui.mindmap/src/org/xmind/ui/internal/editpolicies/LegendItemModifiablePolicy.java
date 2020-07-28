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
import org.xmind.gef.Request;
import org.xmind.gef.part.IPart;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.ModifyLegendMarkerDescriptionCommand;
import org.xmind.ui.mindmap.ILegendItemPart;

public class LegendItemModifiablePolicy extends MindMapPolicyBase {

    public boolean understands(String requestType) {
        return super.understands(requestType)
                || GEF.REQ_MODIFY.equals(requestType);
    }

    public void handle(Request request) {
        String type = request.getType();
        if (GEF.REQ_MODIFY.equals(type)) {
            modifyMarkerDescription(request);
        }
    }

    private void modifyMarkerDescription(Request request) {
        ILegendItemPart item = getItem(request);
        if (item == null)
            return;

        if (!request.hasParameter(GEF.PARAM_TEXT))
            return;

        String text = (String) request.getParameter(GEF.PARAM_TEXT);
        PropertyCommandBuilder builder = new PropertyCommandBuilder(request);
        if (!builder.canStart())
            return;

        builder.setLabel(CommandMessages.Command_ModifyLegendMarkerDescription);
        builder.start();
        builder.add(new ModifyLegendMarkerDescriptionCommand(item
                .getOwnedLegend().getLegend(), item.getMarkerId(), text), true);
        builder.addSourcesFromRequest(false);
        builder.end();

//        ModifyLegendMarkerDescriptionCommand modify = new ModifyLegendMarkerDescriptionCommand(
//                item.getOwnedLegend().getLegend(), item.getMarkerId(), text);
//        PropertyCommandBuilder builder = new PropertyCommandBuilder(request
//                .getTargetViewer(), modify);
//        builder.addCommand(modify, true);
//        builder.addFromRequest(request, false);
//        Command command = builder.createCommand();
//        command.setLabel(CommandMessages.Command_ModifyLegendMarkerDescription);
//        saveAndRun(command, request.getTargetDomain());
    }

    private ILegendItemPart getItem(Request request) {
        IPart target = request.getPrimaryTarget();
        if (target instanceof ILegendItemPart)
            return (ILegendItemPart) target;
        return null;
    }

}