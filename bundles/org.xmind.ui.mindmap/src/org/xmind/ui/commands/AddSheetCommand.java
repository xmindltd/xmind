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

import org.xmind.core.ISheet;
import org.xmind.core.IWorkbook;
import org.xmind.gef.command.SourceCommand;

public class AddSheetCommand extends SourceCommand {

    private ISheet sourceSheet;

    private int index;

    /**
     * 
     */
    public AddSheetCommand(ISheet sourceSheet, IWorkbook targetParent) {
        this(sourceSheet, targetParent, -1);
    }

    public AddSheetCommand(ISheet sourceSheet, IWorkbook targetParent,
            int index) {
        super(targetParent);
        this.sourceSheet = sourceSheet;
        this.index = index;
    }

    public void redo() {
        if (index < 0) {
            ((IWorkbook) getSource()).addSheet(sourceSheet);
        } else {
            ((IWorkbook) getSource()).addSheet(sourceSheet, index);
        }
        super.redo();
    }

    public void undo() {
        ((IWorkbook) getSource()).removeSheet(sourceSheet);
        super.undo();
    }

}
