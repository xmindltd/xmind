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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.xmind.gef.Disposable;

/**
 * @author Brian Sun
 */
public abstract class CommandStackBase extends Disposable
        implements ICommandStack {

    public static int DEFAULT_UNDO_LIMIT = 20;

    private List<ICommandStackListener> commandStackListeners = null;

    private List<ICommandStackListener> pendingCommandStackListeners = null;

    private List<ICommandStackListener> toBeRemovedCommandStackListeners = null;

    private boolean inTransaction = false;

    private int undoLimit;

    /**
     * @param undoLimit
     */
    public CommandStackBase(int undoLimit) {
        this.undoLimit = undoLimit;
    }

    /**
     * 
     */
    public CommandStackBase() {
        this(DEFAULT_UNDO_LIMIT);
    }

    /**
     * @see org.xmind.gef.command.ICommandStack#addCSListener(org.xmind.gef.command.ICommandStackListener)
     */
    public void addCSListener(ICommandStackListener listener) {
        if (inTransaction) {
            if (pendingCommandStackListeners == null) {
                pendingCommandStackListeners = new ArrayList<ICommandStackListener>();
            }
            pendingCommandStackListeners.add(listener);
        } else {
            if (commandStackListeners == null)
                commandStackListeners = new ArrayList<ICommandStackListener>();
            commandStackListeners.add(listener);
        }
    }

    /**
     * @see org.xmind.gef.command.ICommandStack#removeCSListener(org.xmind.gef.command.ICommandStackListener)
     */
    public void removeCSListener(ICommandStackListener listener) {
        if (inTransaction) {
            if (toBeRemovedCommandStackListeners == null)
                toBeRemovedCommandStackListeners = new ArrayList<ICommandStackListener>();
            toBeRemovedCommandStackListeners.add(listener);
        } else {
            if (commandStackListeners != null) {
                commandStackListeners.remove(listener);
            }
        }
    }

    protected void beginTransaction() {
        inTransaction = true;
    }

    protected void endTransaction(Command command, int preStatus) {
        inTransaction = false;
        if (pendingCommandStackListeners != null
                && commandStackListeners != null) {
            commandStackListeners.addAll(pendingCommandStackListeners);
        }

        if (toBeRemovedCommandStackListeners != null) {
            for (ICommandStackListener toBeRemovedListener : toBeRemovedCommandStackListeners) {
                if (commandStackListeners != null)
                    commandStackListeners.remove(toBeRemovedListener);
                if (pendingCommandStackListeners != null)
                    pendingCommandStackListeners.remove(toBeRemovedListener);
            }
            toBeRemovedCommandStackListeners.clear();
            toBeRemovedCommandStackListeners = null;
        }

        if (pendingCommandStackListeners != null) {
            fireEvent(command, preStatus, pendingCommandStackListeners);
            pendingCommandStackListeners.clear();
            pendingCommandStackListeners = null;
        }
    }

    private void fireEvent(Command command, int preStatus,
            List<ICommandStackListener> pendingCommandStackListeners) {
        for (Object listener : pendingCommandStackListeners.toArray()) {
            ((ICommandStackListener) listener).handleCommandStackEvent(
                    new CommandStackEvent(this, command, preStatus));
        }
    }

    protected void fireEvent(int status) {
        if (commandStackListeners == null)
            return;
        fireEvent(new CommandStackEvent(this, status));
    }

    protected void fireEvent(Command cmd, int status) {
        if (commandStackListeners == null)
            return;
        fireEvent(new CommandStackEvent(this, cmd, status));
    }

    protected void fireEvent(CommandStackEvent event) {
        if (commandStackListeners == null)
            return;
        for (Object listener : commandStackListeners.toArray()) {
            ((ICommandStackListener) listener).handleCommandStackEvent(event);
        }
    }

    /**
     * @see org.xmind.framework.Disposable#removeFromLayer(IFigure)
     */
    @Override
    public void dispose() {
        if (commandStackListeners != null) {
            commandStackListeners.clear();
            commandStackListeners = null;
        }
        clear();
        super.dispose();
    }

    /**
     * @see org.xmind.gef.command.ICommandStack#getUndoLimit()
     */
    public int getUndoLimit() {
        return undoLimit;
    }

    /**
     * @see org.xmind.gef.command.ICommandStack#setUndoLimit(int)
     */
    public void setUndoLimit(int undoLimit) {
        this.undoLimit = undoLimit;
    }

    /**
     * @see org.xmind.gef.command.ICommandStack#getRepeatLabel()
     * @deprecated
     */
    public String getRepeatLabel() {
        return getUndoLabel();
    }

    /**
     * @deprecated
     */
    public boolean canRepeat() {
        return false;
    }

    /**
     * @deprecated
     */
    public void repeat() {
    }

    public void markSaved() {
    }

    public boolean isDirty() {
        return canUndo();
    }
}