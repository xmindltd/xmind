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
import org.xmind.core.ISheet;
import org.xmind.core.IWorkbook;
import org.xmind.gef.GEF;
import org.xmind.gef.command.SourceCommand;

public class DeleteSheetCommand extends SourceCommand {

    private IWorkbook parent;

    private int index;

    public DeleteSheetCommand(ISheet sheet) {
        super(sheet);
        IWorkbook parent = sheet.getParent();
        Assert.isNotNull(parent);
        this.parent = parent;
    }

    public DeleteSheetCommand(ISheet sheet, IWorkbook parent) {
        super(sheet);
        Assert.isNotNull(parent);
        this.parent = parent;
    }

    public int getType() {
        return GEF.CMD_DELETE;
    }

    public boolean canExecute() {
        return super.canExecute();
    }

    public void execute() {
        this.index = ((ISheet) getSource()).getIndex();
        super.execute();
    }

    public void redo() {
        parent.removeSheet((ISheet) getSource());
        super.redo();
    }

    public void undo() {
        parent.addSheet((ISheet) getSource(), index);
        super.undo();
    }
}