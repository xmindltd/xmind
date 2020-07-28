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
package org.xmind.gef;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.xmind.gef.command.CommandStackEvent;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.command.ICommandStackListener;

public class SelectionStack implements ISelectionStack, ICommandStackListener {

    private ISelectionProvider selectionProvider = null;

    private ICommandStack commandStack = null;

    private List<ISelection> selections = null;

    private int cursor = 0;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.mindmap.editor.ISelectionStack#setSelectionProvider(org.
     * eclipse.jface.viewers.ISelectionProvider)
     */
    public void setSelectionProvider(ISelectionProvider selectionProvider) {
        if (selectionProvider == this.selectionProvider)
            return;
        this.selectionProvider = selectionProvider;
        this.selections = null;
        this.cursor = 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.mindmap.editor.ISelectionStack#setCommandStack(org.xmind
     * .gef.command.ICommandStack)
     */
    public void setCommandStack(ICommandStack commandStack) {
        ICommandStack oldCS = this.commandStack;
        if (commandStack == oldCS)
            return;
        if (oldCS != null) {
            oldCS.removeCSListener(this);
        }
        this.commandStack = commandStack;
        if (commandStack != null) {
            commandStack.addCSListener(this);
        }
    }

    public void handleCommandStackEvent(CommandStackEvent event) {
        if (selectionProvider == null)
            return;

        if (selections == null)
            selections = new ArrayList<ISelection>(30);

        if ((event.getStatus() & GEF.CS_PRE_EXECUTE) != 0)
            preExecute();
        else if ((event.getStatus() & GEF.CS_PRE_UNDO) != 0)
            preUndo();
        else if ((event.getStatus() & GEF.CS_PRE_REDO) != 0)
            preRedo();
        else if ((event.getStatus() & GEF.CS_POST_EXECUTE) != 0)
            postExecute();
        else if ((event.getStatus() & GEF.CS_POST_UNDO) != 0)
            postUndo();
        else if ((event.getStatus() & GEF.CS_POST_REDO) != 0)
            postRedo();
    }

    /**
     * Snapshots the current selection and push it into stack, and clear all
     * cached selections from the current position on.
     */
    protected void preExecute() {
        clearFromCursor();
        if (cursor >= 0 && cursor <= selections.size()) {
            ISelection selection = selectionProvider.getSelection();
            selections.add(cursor, selection);
        }
    }

    /**
     * Clears all cached selections from the current position on.
     */
    protected void clearFromCursor() {
        if (cursor <= 0) {
            selections.clear();
        } else {
            while (cursor < selections.size()) {
                selections.remove(selections.size() - 1);
            }
        }
    }

    /**
     * Try to preserve the previous selection after a command is executed.
     */
    protected void postExecute() {
        restoreSelection();
        ++cursor;
    }

    /**
     * Snapshots the current selection and push it into the stack if there's no
     * selection on the current position before a command is about to be undone.
     */
    protected void preUndo() {
        ISelection selection = selectionProvider.getSelection();
        if (cursor == selections.size()) {
            selections.add(cursor, selection);
        } else if (cursor >= 0 && cursor < selections.size()) {
//            ISelection cached = selections.get(cursor);
//            if (cached == null)
            selections.set(cursor, selection);
        }
    }

    /**
     * Restores the previous selection that had been cached before this command
     * was executed or redone.
     */
    protected void postUndo() {
        --cursor;
        restoreSelection();
    }

    /**
     * Snapshots the current selection and push it into the stack if there's no
     * selection on the current position before a command is about to be redone.
     */
    protected void preRedo() {
        if (cursor >= 0 && cursor < selections.size()) {
//            ISelection cached = selections.get(cursor);
//            if (cached == null) {
            ISelection selection = selectionProvider.getSelection();
            selections.set(cursor, selection);
//            }
        } else if (cursor == selections.size()) {
            ISelection selection = selectionProvider.getSelection();
            selections.add(cursor, selection);
        }
    }

    /**
     * Restores the previous selection that had been cached before this command
     * was undone.
     */
    protected void postRedo() {
        ++cursor;
        restoreSelection();
    }

    /**
     * Restores the current selection to the selection provider. Does nothing is
     * no selection is accessible.
     */
    protected void restoreSelection() {
        if (cursor >= 0 && cursor < selections.size()) {
            ISelection selection = selections.get(cursor);
            if (selection != null) {
                selectionProvider.setSelection(selection);
            }
        }
    }

}