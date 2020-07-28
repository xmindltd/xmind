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
package org.xmind.ui.internal.spreadsheet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.xmind.core.ILabeled;
import org.xmind.core.ITopic;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.part.IPart;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.ModifyLabelCommand;
import org.xmind.ui.internal.editpolicies.MindMapPolicyBase;
import org.xmind.ui.internal.spreadsheet.structures.ColumnHead;
import org.xmind.ui.internal.spreadsheet.structures.ColumnOrder;
import org.xmind.ui.internal.spreadsheet.structures.RowHead;
import org.xmind.ui.internal.spreadsheet.structures.RowOrder;
import org.xmind.ui.util.MindMapUtils;

public class SheetHeadModifiablePolicy extends MindMapPolicyBase {

    public boolean understands(String requestType) {
        return super.understands(requestType)
                || Spreadsheet.REQ_MODIFY_SHEET_HEAD.equals(requestType);
    }

    public void handle(Request request) {
        String reqType = request.getType();
        if (Spreadsheet.REQ_MODIFY_SHEET_HEAD.equals(reqType)) {
            modifySheetHead(request);
        }
    }

    private void modifySheetHead(Request request) {
        if (!request.hasParameter(GEF.PARAM_TEXT))
            return;

        // 1.modify topics' label
        String text = (String) request.getParameter(GEF.PARAM_TEXT);
        if (text == null)
            text = EMPTY;

        Collection<String> labels = MindMapUtils.getLabels(text);
        List<IPart> sources = request.getTargets();
        List<Command> cmds = new ArrayList<Command>(sources.size());
        for (IPart p : sources) {
            Object o = MindMapUtils.getRealModel(p);
            if (o instanceof ILabeled) {
                cmds.add(new ModifyLabelCommand((ILabeled) o, labels));
            }
        }
        if (cmds.isEmpty())
            return;

        CompoundCommand cmd = new CompoundCommand(cmds);

        // 2.modify row/column head order
        ITopic topic = ((ITopic) MindMapUtils.getRealModel(sources.get(0)))
                .getParent().getParent();
        Set<String> oldLabels = ((ITopic) MindMapUtils
                .getRealModel(sources.get(0))).getLabels();

        // modify column head order
        ColumnOrder columnOrder = ColumnOrder.createFromTopic(topic);
        List<ColumnHead> columnHeads = columnOrder.getHeads();
        Iterator<ColumnHead> ite = columnHeads.iterator();
        while (ite.hasNext()) {
            ColumnHead columnHead = ite.next();
            // select the modified columnHead
            if (columnHead.getLabels().equals(oldLabels)) {
                columnHead.setLabels(new TreeSet<String>(labels));

                // If the modified columnHead repeat with other, then remove it
                for (ColumnHead ch : new ArrayList<ColumnHead>(columnHeads)) {
                    if (ch != columnHead
                            && ch.getLabels().equals(columnHead.getLabels())) {
                        ite.remove();
                    }
                }
            }
        }

        ModifyColumnOrderCommand modifyColumnOrderCommand = new ModifyColumnOrderCommand(
                topic, columnOrder);
        cmd.append(modifyColumnOrderCommand);

        // modify row head order
        RowOrder rowOrder = RowOrder.createFromTopic(topic);
        List<RowHead> rowHeads = rowOrder.getHeads();
        Iterator<RowHead> ite2 = rowHeads.iterator();
        while (ite2.hasNext()) {
            RowHead rowHead = ite2.next();
            // select the modified rowHead
            if (rowHead.getLabels().equals(oldLabels)) {
                rowHead.setLabels(new TreeSet<String>(labels));

                // If the modified rowHead repeat with other, then remove it
                for (RowHead rh : new ArrayList<RowHead>(rowHeads)) {
                    if (rh != rowHead
                            && rh.getLabels().equals(rowHead.getLabels())) {
                        ite2.remove();
                    }
                }
            }
        }

        ModifyRowOrderCommand modifyRowOrderCommand = new ModifyRowOrderCommand(
                topic, rowOrder);
        cmd.append(modifyRowOrderCommand);

        cmd.setLabel(CommandMessages.Command_ModifyLabels);
        saveAndRun(cmd, request.getTargetDomain());
        select(cmd.getSources(), request.getTargetViewer());
    }

}