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
package org.xmind.ui.internal.editpolicies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.xmind.core.ILegend;
import org.xmind.core.IPositioned;
import org.xmind.core.ISheet;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.draw2d.geometry.IIntersectionSolver;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.ModifyLegendVisibilityCommand;
import org.xmind.ui.commands.ModifyPositionCommand;
import org.xmind.ui.internal.layouts.SheetIntersectionSolver;
import org.xmind.ui.mindmap.IMindMapViewer;
import org.xmind.ui.mindmap.ISheetPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class MapPolicy extends MindMapPolicyBase {

    public boolean understands(String requestType) {
        return super.understands(requestType)
                || MindMapUI.REQ_SHOW_LEGEND.equals(requestType)
                || MindMapUI.REQ_HIDE_LEGEND.equals(requestType)
                || MindMapUI.REQ_TILE.equals(requestType);
    }

    public void handle(Request request) {
        String type = request.getType();
        if (MindMapUI.REQ_SHOW_LEGEND.equals(type)) {
            modifyLegendVisibility(request, true);
        } else if (MindMapUI.REQ_HIDE_LEGEND.equals(type)) {
            modifyLegendVisibility(request, false);
        } else if (MindMapUI.REQ_TILE.equals(type)) {
            tile(request);
        }
    }

    private void tile(Request request) {
        ISheetPart sheet = getSheetPart(request);
        SheetIntersectionSolver intersectionSolver = new SheetIntersectionSolver();
        Point origin = ((IReferencedFigure) sheet.getFigure()).getOrigin();
        intersectionSolver.setOrigin(origin);
        intersectionSolver.recordInitPositions(sheet, false);
        intersectionSolver.solve();
        Collection<Object> keys = intersectionSolver
                .getKeys(IIntersectionSolver.CATEGORY_FREE);
        List<Command> commands = new ArrayList<Command>(keys.size());
        for (Object key : keys) {
            if (key instanceof IGraphicalPart) {
                IGraphicalPart part = (IGraphicalPart) key;
                Object o = MindMapUtils.getRealModel(part);
                if (o instanceof IPositioned) {
                    IPositioned p = (IPositioned) o;
                    Point pos = intersectionSolver.getSolvedPosition(key);
                    pos = toRelative(pos, origin);
                    commands.add(new ModifyPositionCommand(p, MindMapUtils
                            .toModelPosition(pos)));
                }
            }
        }

        if (commands.isEmpty())
            return;

        CompoundCommand cmd = new CompoundCommand(commands);
        cmd.setLabel(CommandMessages.Command_Tile);
        saveAndRun(cmd, request.getTargetDomain());
    }

    private Point toRelative(Point pos, Point origin) {
        Dimension offset = pos.getDifference(origin);
        return new Point(offset.width, offset.height);
    }

    private ISheetPart getSheetPart(Request request) {
        IPart target = request.getPrimaryTarget();
        if (target instanceof ISheetPart)
            return (ISheetPart) target;
        IViewer viewer = request.getTargetViewer();
        if (viewer instanceof IMindMapViewer) {
            return ((IMindMapViewer) viewer).getSheetPart();
        }
        return null;
    }

    private void modifyLegendVisibility(Request request, boolean visible) {
        ILegend legend = getLegend(request);
        if (legend == null)
            return;

        PropertyCommandBuilder builder = new PropertyCommandBuilder(request);
        if (!builder.canStart())
            return;

        builder.start();
        builder.setLabel(visible ? CommandMessages.Command_ShowLegend
                : CommandMessages.Command_HideLegend);
        builder.add(new ModifyLegendVisibilityCommand(legend, visible), true);
        builder.addSource(legend, false);
        builder.end();
    }

    private ILegend getLegend(Request request) {
        IPart target = request.getPrimaryTarget();
        if (target != null) {
            Object m = MindMapUtils.getRealModel(target);
            if (m instanceof ISheet)
                return ((ISheet) m).getLegend();
            if (m instanceof ILegend)
                return (ILegend) m;
        }
        IViewer viewer = request.getTargetViewer();
        if (viewer instanceof IMindMapViewer) {
            ISheet sheet = ((IMindMapViewer) viewer).getSheet();
            if (sheet != null)
                return sheet.getLegend();
        }
        return null;
    }
}