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
package org.xmind.ui.commands;

import org.eclipse.core.runtime.Assert;
import org.xmind.core.IWorkbook;
import org.xmind.gef.command.SourceCommand;

public class MoveSheetCommand extends SourceCommand {

    private int oldIndex;

    private int newIndex;

    public MoveSheetCommand(IWorkbook workbook, int oldIndex, int newIndex) {
        super(workbook);
        int size = workbook.getSheets().size();
        Assert.isTrue(oldIndex >= 0 && oldIndex < size);
        this.oldIndex = oldIndex;
        if (newIndex < 0 || newIndex >= size)
            newIndex = size - 1;
        this.newIndex = newIndex;
    }

    public void execute() {
        super.execute();
    }

    public void redo() {
        ((IWorkbook) getSource()).moveSheet(oldIndex, newIndex);
        super.redo();
    }

    public void undo() {
        ((IWorkbook) getSource()).moveSheet(newIndex, oldIndex);
        super.undo();
    }

}