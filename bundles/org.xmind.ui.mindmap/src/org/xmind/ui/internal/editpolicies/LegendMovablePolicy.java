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

import org.eclipse.draw2d.geometry.Point;
import org.xmind.core.ILegend;
import org.xmind.core.ISheet;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.part.IPart;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.ModifyPositionCommand;
import org.xmind.ui.util.MindMapUtils;

public class LegendMovablePolicy extends MindMapPolicyBase {

    public boolean understands(String requestType) {
        return super.understands(requestType)
                || (GEF.REQ_MOVETO.equals(requestType));
    }

    public void handle(Request request) {
        String type = request.getType();
        if (GEF.REQ_MOVETO.equals(type)) {
            moveLegend(request);
        }
    }

    private void moveLegend(Request request) {
        ILegend legend = getLegend(request);
        if (legend == null)
            return;

        if (!request.hasParameter(GEF.PARAM_POSITION))
            return;

        Object param = request.getParameter(GEF.PARAM_POSITION);
        if (param != null && !(param instanceof Point))
            return;

        Point position = (Point) param;
        ModifyPositionCommand cmd = new ModifyPositionCommand(legend,
                MindMapUtils.toModelPosition(position));
        cmd.setLabel(CommandMessages.Command_MoveLegend);
        saveAndRun(cmd, request.getTargetDomain());
    }

    private ILegend getLegend(Request request) {
        IPart target = request.getPrimaryTarget();
        if (target != null) {
            Object m = MindMapUtils.getRealModel(target);
            if (m instanceof ILegend)
                return (ILegend) m;
            if (m instanceof ISheet)
                return ((ISheet) m).getLegend();
        }
        return null;
    }
}