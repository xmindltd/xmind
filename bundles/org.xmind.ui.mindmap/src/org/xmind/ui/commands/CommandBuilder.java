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

import java.util.ArrayList;
import java.util.List;

import org.xmind.gef.ISourceProvider2;
import org.xmind.gef.IViewer;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.command.ICommandStack3;

public class CommandBuilder {

    private IViewer viewer;

    private ICommandStack commandStack;

    private CommandBuilder delegate;

    private String label;

    private CompoundCommand compoundCommand = null;

    private boolean executed = false;

    private List<Command> pendingCommands = null;

    public CommandBuilder(ICommandStack commandStack) {
        this(null, commandStack);
    }

    public CommandBuilder(CommandBuilder delegate) {
        this(null, delegate);
    }

    public CommandBuilder(IViewer viewer, ICommandStack commandStack) {
        this.viewer = viewer;
        this.commandStack = commandStack;
        this.delegate = null;
    }

    public CommandBuilder(IViewer viewer, CommandBuilder delegate) {
        this.viewer = viewer;
        this.commandStack = null;
        this.delegate = delegate;
    }

    public boolean canStart() {
        return commandStack != null || delegate != null;
    }

    public void start() {
        if (commandStack instanceof ICommandStack3) {
            ((ICommandStack3) commandStack).startCompoundCommand();
        }
    }

    public void end() {
        handlePendingCommands();
        if (commandStack instanceof ICommandStack3) {
            ((ICommandStack3) commandStack).endCompoundCommand();
        }
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public IViewer getViewer() {
        return viewer;
    }

    public CompoundCommand getCommand() {
        return compoundCommand;
    }

    public final void add(Command command, boolean sourceCollectable) {
        if (delegate != null) {
            delegate.add(command, sourceCollectable);
            return;
        }

        if (command == null)
            return;

        if (command instanceof ISourceProvider2) {
            ((ISourceProvider2) command)
                    .setSourceCollectable(sourceCollectable);
        }

        if (this.compoundCommand == null) {
            this.compoundCommand = new CompoundCommand(command);
            if (label != null) {
                this.compoundCommand.setLabel(label);
            }
            execute(this.compoundCommand);
        } else {
            this.compoundCommand.append(command);
            execute(command);
        }
    }

    protected void execute(Command command) {
        if (commandStack != null && !executed) {
            if (command.canExecute()) {
                commandStack.execute(command);
                executed = true;
            }
        } else {
            command.execute();
        }
    }

    public void addPendingCommand(Command command, boolean sourceCollectable) {
        if (pendingCommands == null)
            pendingCommands = new ArrayList<Command>();
        pendingCommands.add(command);
        if (command instanceof ISourceProvider2) {
            ((ISourceProvider2) command)
                    .setSourceCollectable(sourceCollectable);
        }
    }

    public void removePendingCommand(Command command) {
        if (pendingCommands != null) {
            pendingCommands.remove(command);
        }
    }

    protected void handlePendingCommands() {
        if (pendingCommands == null || pendingCommands.isEmpty())
            return;

        while (!pendingCommands.isEmpty()) {
            Command command = pendingCommands.remove(0);
            boolean sourceCollectable = (command instanceof ISourceProvider2) ? ((ISourceProvider2) command)
                    .isSourceCollectable()
                    : false;
            add(command, sourceCollectable);
        }
    }

}