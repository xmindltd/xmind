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
package org.xmind.ui.internal.actions;

import java.util.ArrayList;
import java.util.List;

import org.xmind.core.ISheet;
import org.xmind.core.IWorkbook;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.ui.actions.EditorAction;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.actions.MindMapActionFactory;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.DeleteSheetCommand;

/**
 * 
 * @author Karelun huang
 */
public class DeleteOtherSheetsAction extends EditorAction {

    public DeleteOtherSheetsAction(IGraphicalEditor editor) {
        super(MindMapActionFactory.DELETE_OTHER_SHEET.getId(), editor);

    }

    public void run() {
        if (isDisposed())
            return;
        IGraphicalEditorPage page = getActivePage();
        if (page == null)
            return;
        ISheet activeSheet = (ISheet) page.getAdapter(ISheet.class);
        if (activeSheet == null) {
            Object input = page.getInput();
            if (input instanceof ISheet)
                activeSheet = (ISheet) input;
        }
        if (activeSheet != null) {
            IWorkbook workbook = activeSheet.getOwnedWorkbook();
            List<ISheet> sheets = workbook.getSheets();
            List<Command> commands = new ArrayList<Command>(sheets.size() - 1);
            for (ISheet sheet : sheets) {
                if (activeSheet.equals(sheet))
                    continue;
                commands.add(new DeleteSheetCommand(sheet));
            }

            saveAndRun(new CompoundCommand(CommandMessages.Command_DeleteSheet,
                    commands));
        }
    }

}
