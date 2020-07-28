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

import static org.xmind.gef.GEF.CS_POST_EXECUTE;
import static org.xmind.gef.GEF.CS_POST_REDO;
import static org.xmind.gef.GEF.CS_POST_UNDO;
import static org.xmind.gef.GEF.CS_PRE_EXECUTE;
import static org.xmind.gef.GEF.CS_PRE_REDO;
import static org.xmind.gef.GEF.CS_PRE_UNDO;

import java.util.ArrayList;
import java.util.List;

import org.xmind.gef.GEF;

/**
 * @author Brian Sun
 */
public class CommandStack extends CommandStackBase
        implements ICommandStack2, ICommandStack3 {

    private class DelegateListener implements ICommandStackListener {

        public void handleCommandStackEvent(CommandStackEvent event) {
            fireEvent(event);
        }

    }

    protected List<Command> commandList;

    private int currentLocation = -1;

    private int saveLocation = -1;

    private boolean alwaysDirty = false;

    private ICommandStackDelegate delegate = null;

    private DelegateListener delegateListener = null;

    private boolean inCompoundCommand = false;

    private Command compoundCommand = null;

    public CommandStack() {
        this(DEFAULT_UNDO_LIMIT);
    }

    /**
     * @param undoLimit
     */
    public CommandStack(int undoLimit) {
        super(undoLimit);
        commandList = new ArrayList<Command>(undoLimit);
    }

    public void startCompoundCommand() {
        if (delegate instanceof ICommandStack3) {
            ((ICommandStack3) delegate).startCompoundCommand();
            return;
        }
        inCompoundCommand = true;
    }

    public void endCompoundCommand() {
        if (delegate instanceof ICommandStack3) {
            ((ICommandStack3) delegate).endCompoundCommand();
            return;
        }
        inCompoundCommand = false;
        if (compoundCommand != null) {
            Command command = compoundCommand;
            compoundCommand = null;
            postExecute(command);
        }
    }

    public void execute(Command command) {
        if (delegate != null && delegate.canExecute(command)) {
            delegate.execute(command);
            return;
        }

        if (command == null || !command.canExecute())
            return;

        fireEvent(command, CS_PRE_EXECUTE);

        beginTransaction();
        command.execute();

        if (inCompoundCommand) {
            if (compoundCommand == null) {
                compoundCommand = command;
                return;
            } else {
                endCompoundCommand();
            }
        }

        endTransaction(command, CS_PRE_EXECUTE);
        postExecute(command);

    }

    private void postExecute(Command command) {
        fireEvent(command, CS_POST_EXECUTE);
        if (command.canUndo()) {
            pushCommand(command);
        }
        fireEvent(null, GEF.CS_UPDATED);
    }

    private void pushCommand(Command cmd) {
        discardRedoables();
        commandList.add(cmd);
        if (saveLocation > currentLocation)
            saveLocation = -1;
        currentLocation++;
        fireEvent(cmd, GEF.CS_COMMAND_PUSHED);
        if (getUndoLimit() > 0 && currentLocation >= getUndoLimit()) {
            Command discarded = commandList.remove(0);
            if (discarded != null) {
                discarded.dispose();
            }
            currentLocation--;
            if (saveLocation >= 0)
                saveLocation--;
            alwaysDirty = true;
        }
    }

    private void discardRedoables() {
        while (commandList.size() - 1 > currentLocation) {
            Command discarded = commandList.remove(commandList.size() - 1);
            if (discarded != null) {
                discarded.dispose();
            }
        }
    }

    /**
     * @return the commandList
     */
    public List<Command> getCommandList() {
        return commandList;
    }

    public boolean canUndo() {
        if (delegate != null)
            return delegate.canUndo();

        return currentLocation >= 0;
    }

    public void undo() {
        if (delegate != null && delegate.canUndo()) {
            delegate.undo();
            return;
        }

        Command undoCmd = commandList.get(currentLocation);
        if (undoCmd.canUndo()) {
            beginTransaction();
            fireEvent(undoCmd, CS_PRE_UNDO);
            undoCmd.undo();
            endTransaction(undoCmd, CS_PRE_UNDO);
            currentLocation--;
            fireEvent(undoCmd, CS_POST_UNDO);
        } else {
            currentLocation--;
        }
        fireEvent(null, GEF.CS_UPDATED);
    }

    public void undo(boolean discard) {
        undo();
        if (discard) {
            discardRedoables();
        }
    }

    public boolean canRedo() {
        if (delegate != null)
            return delegate.canRedo();

        return currentLocation < commandList.size() - 1;
    }

    public void redo() {
        if (delegate != null && delegate.canRedo()) {
            delegate.redo();
            return;
        }

        Command redoCmd = commandList.get(currentLocation + 1);
        if (redoCmd.canExecute()) {
            fireEvent(redoCmd, CS_PRE_REDO);
            beginTransaction();
            redoCmd.redo();
            endTransaction(redoCmd, CS_PRE_REDO);
            currentLocation++;
            fireEvent(redoCmd, CS_POST_REDO);
        } else {
            currentLocation++;
        }
        fireEvent(null, GEF.CS_UPDATED);
    }

//    /**
//     * @deprecated
//     */
//    public String getRepeatLabel() {
//        if (delegate != null)
//            return delegate.getRepeatLabel();
//        return super.getRepeatLabel();
//    }
//
//    /**
//     * @deprecated
//     */
//    public void repeat() {
//        if (delegate != null) {
//            delegate.repeat();
//            return;
//        }
//        super.repeat();
////        execute( commandList.get( currentLocation ).clone() );
//    }

    public boolean isDirty() {
        if (delegate != null)
            return delegate.isDirty();

        return saveLocation != currentLocation || alwaysDirty;
    }

    public void markSaved() {
//        if (delegate != null) {
//            delegate.markSaved();
//            return;
//        }

        saveLocation = currentLocation;
        alwaysDirty = false;
    }

    public void clear() {
//        if (delegate != null) {
//            delegate.clear();
//            return;
//        }

        currentLocation = -1;
        saveLocation = -1;
        for (Command c : commandList)
            c.dispose();
        commandList.clear();
        fireEvent(null, GEF.CS_UPDATED);
    }

//    /**
//     * @see org.xmind.gef.command.ICommandStack#canRepeat()
//     * @deprecated
//     */
//    public boolean canRepeat() {
//        if (delegate != null)
//            return delegate.canRepeat();
//        return super.canRepeat();
////        return currentLocation >= 0
////                && commandList.get( currentLocation ).canRepeat();
//    }

    /**
     * @see org.xmind.gef.command.ICommandStack#getRedoLabel()
     */
    public String getRedoLabel() {
        if (delegate != null && delegate.canRedo())
            return delegate.getRedoLabel();
        return commandList.get(currentLocation + 1).getLabel();
    }

    /**
     * @see org.xmind.gef.command.ICommandStack#getUndoLabel()
     */
    public String getUndoLabel() {
        if (delegate != null && delegate.canUndo())
            return delegate.getUndoLabel();
        return commandList.get(currentLocation).getLabel();
    }

    public void setUndoLimit(int undoLimit) {
//        if (delegate != null) {
//            delegate.setUndoLimit(undoLimit);
//            return;
//        }
        super.setUndoLimit(undoLimit);
        while (undoLimit > 0 && commandList.size() > undoLimit) {
            deleteFirst();
        }
        fireEvent(null, GEF.CS_UPDATED);
    }

    private void deleteFirst() {
        if (commandList.size() > 0) {
            Command cmd = commandList.remove(0);
            if (cmd != null) {
                cmd.dispose();
                if (saveLocation >= 0)
                    saveLocation--;
                if (currentLocation >= 0)
                    currentLocation--;
                alwaysDirty = true;
            }
        }
    }

//    public int getUndoLimit() {
//        if (delegate != null)
//            return delegate.getUndoLimit();
//        return super.getUndoLimit();
//    }

    public void setDelegate(ICommandStackDelegate delegate) {
        if (delegate == this.delegate)
            return;

        if (this.delegate != null) {
            if (delegateListener != null) {
                this.delegate.removeCSListener(delegateListener);
            }
        }
        this.delegate = delegate;
        if (delegate != null) {
            if (delegateListener == null)
                delegateListener = new DelegateListener();
            delegate.addCSListener(delegateListener);
        } else {
            delegateListener = null;
        }
        fireEvent(null, GEF.CS_UPDATED);
    }

    public ICommandStackDelegate getDelegate() {
        return delegate;
    }

    public void dispose() {
        setDelegate(null);
        super.dispose();
    }
}