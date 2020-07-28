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
package org.xmind.gef.command;

public class NullCommandStack extends CommandStackBase {

    public boolean canRedo() {
        return false;
    }

    public boolean canUndo() {
        return false;
    }

    public void clear() {
    }

    public void execute(Command command) {
        if (command != null && command.canExecute())
            command.execute();
    }

    public String getRedoLabel() {
        return Command.EMPTY;
    }

    public String getUndoLabel() {
        return Command.EMPTY;
    }

    public void redo() {
    }

    public void undo() {
    }

}