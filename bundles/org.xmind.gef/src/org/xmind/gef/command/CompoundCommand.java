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
package org.xmind.gef.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.xmind.gef.GEF;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.ISourceProvider2;

/**
 * @author Brian Sun
 */
public class CompoundCommand extends Command implements ISourceProvider2 {

    private List<Command> commands;

    private boolean executing = false;

    private boolean sourceCollectable = true;

    public CompoundCommand(List<? extends Command> commands) {
        this(EMPTY, commands);
    }

    public CompoundCommand(Command... commands) {
        this(EMPTY, commands);
    }

    public CompoundCommand(String label, List<? extends Command> commands) {
        super(label);
        Assert.isNotNull(commands);
        for (Command c : commands)
            Assert.isNotNull(c);
        this.commands = new ArrayList<Command>(commands);
    }

    public CompoundCommand(String label, Command... commands) {
        super(label);
        Assert.isNotNull(commands);
        for (Command c : commands)
            Assert.isNotNull(c);
        List<Command> list = Arrays.asList(commands);
        this.commands = new ArrayList<Command>(list);
    }

    public List<Command> getCommands() {
        return commands;
    }

    public int getType() {
        boolean hasModifyCommand = false;
        for (Command cmd : commands) {
            int type = cmd.getType();
            if (type == GEF.CMD_MODIFY && !hasModifyCommand) {
                hasModifyCommand = true;
            }
            if (type != GEF.CMD_NORMAL && type != GEF.CMD_MODIFY)
                return type;
        }
        if (hasModifyCommand)
            return GEF.CMD_MODIFY;
        return super.getType();
    }

    public boolean hasSource() {
        for (Command c : commands) {
            if (c instanceof ISourceProvider) {
                if (((ISourceProvider) c).hasSource())
                    return true;
            }
        }
        return false;
    }

    public Object getSource() {
        for (Command c : commands) {
            if (c instanceof ISourceProvider) {
                if (!(c instanceof ISourceProvider2)
                        || ((ISourceProvider2) c).isSourceCollectable()) {
                    Object source = ((ISourceProvider) c).getSource();
                    if (source != null)
                        return source;
                }
            }
        }
        return null;
    }

    public List<Object> getSources() {
        List<Object> sources = new ArrayList<Object>();
        for (Command c : commands) {
            if (c instanceof ISourceProvider) {
                if (!(c instanceof ISourceProvider2)
                        || ((ISourceProvider2) c).isSourceCollectable()) {
                    List<Object> ss = ((ISourceProvider) c).getSources();
                    for (Object s : ss) {
                        if (s != null && !sources.contains(s)) {
                            sources.add(s);
                        }
                    }
                }
            }
        }
        return sources;
    }

    public void append(Command command) {
        Assert.isNotNull(command);
        commands.add(command);
//        if (canUndo() && command.canExecute()) {
//            command.execute();
//        }
    }

    public boolean canExecute() {
        for (Command cmd : commands) {
            if (cmd.canExecute())
                return true;
        }
        return false;
    }

    public boolean canRedo() {
        for (Command cmd : commands) {
            if (cmd.canRedo())
                return true;
        }
        return false;
    }

    public boolean canUndo() {
        for (Command cmd : commands) {
            if (cmd.canUndo())
                return true;
        }
        return false;
    }

    public void execute() {
        for (Command cmd : commands) {
            if (cmd.canExecute())
                cmd.execute();
        }
        executing = true;
        super.execute();
        executing = false;
    }

    public void redo() {
        if (!executing) {
            for (Command cmd : commands) {
                if (cmd.canRedo())
                    cmd.redo();
            }
        }
        super.redo();
    }

    public void undo() {
        for (int i = commands.size() - 1; i >= 0; i--) {
            Command cmd = commands.get(i);
            if (cmd.canUndo())
                cmd.undo();
        }
        super.undo();
    }

    public void dispose() {
        if (commands != null) {
            for (Command cmd : commands) {
                cmd.dispose();
            }
            commands = null;
        }
        super.dispose();
    }

    public boolean isSourceCollectable() {
        return sourceCollectable;
    }

    public void setSourceCollectable(boolean collectable) {
        this.sourceCollectable = collectable;
    }

}