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
package org.xmind.gef.ui.actions;

import org.xmind.gef.command.CommandStackEvent;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.command.ICommandStackListener;
import org.xmind.gef.ui.editor.IGraphicalEditor;

public abstract class CommandStackAction extends EditorAction implements
        ICommandStackAction, ICommandStackListener {

    private ICommandStack commandStack;

    protected CommandStackAction(IGraphicalEditor editor) {
        super(editor);
        setCommandStack(editor.getCommandStack());
    }

    protected ICommandStack getCommandStack() {
        return commandStack;
    }

    public void setCommandStack(ICommandStack commandStack) {
        if (commandStack == this.commandStack)
            return;

        if (this.commandStack != null)
            unhook(this.commandStack);
        this.commandStack = commandStack;
        if (commandStack != null)
            hook(commandStack);

        update();
    }

    private void unhook(ICommandStack cs) {
        cs.removeCSListener(this);
    }

    private void hook(ICommandStack cs) {
        cs.addCSListener(this);
    }

    public void handleCommandStackEvent(CommandStackEvent event) {
        update();
    }

    protected abstract void update();

    public void dispose() {
        setCommandStack(null);
        super.dispose();
    }

}