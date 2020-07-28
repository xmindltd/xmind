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
package org.xmind.ui.richtext;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoContext;

/**
 * @author Frank Shaka
 */
public interface IRichDocumentUndoManager {

    /**
     * Adds the specified listener to the list of document undo listeners that
     * are notified before and after changes are undone or redone in the
     * document. This method has no effect if the instance being added is
     * already in the list.
     * <p>
     * Notifications will not be received if there are no clients connected to
     * the receiver. Registering a document undo listener does not implicitly
     * connect the listener to the receiver.
     * </p>
     * <p>
     * Document undo listeners must be prepared to receive notifications from a
     * background thread. Any UI access occurring inside the implementation must
     * be properly synchronized using the techniques specified by the client's
     * widget library.
     * </p>
     * 
     * @param listener
     *            the document undo listener to be added as a listener
     */
    void addDocumentUndoListener(IRichDocumentUndoListener listener);

    /**
     * Removes the specified listener from the list of document undo listeners.
     * <p>
     * Removing a listener which is not registered has no effect
     * </p>
     * 
     * @param listener
     *            the document undo listener to be removed
     */
    void removeDocumentUndoListener(IRichDocumentUndoListener listener);

    /**
     * Returns the undo context registered for this document
     * 
     * @return the undo context registered for this document
     */
    IUndoContext getUndoContext();

    /**
     * Closes the currently open text edit and open a new one.
     */
    void commit();

    /**
     * Connects to the undo manager. Used to signify that a client is monitoring
     * the history kept by the undo manager. This message has no effect if the
     * client is already connected.
     * 
     * @param client
     *            the object connecting to the undo manager
     */
    void connect(Object client);

    /**
     * Disconnects from the undo manager. Used to signify that a client is no
     * longer monitoring the history kept by the undo manager. If all clients
     * have disconnected from the undo manager, the undo history will be
     * deleted.
     * 
     * @param client
     *            the object disconnecting from the undo manager
     */
    void disconnect(Object client);

    /**
     * Signals the undo manager that all subsequent changes until
     * <code>endCompoundChange</code> is called are to be undone in one piece.
     */
    void beginCompoundChange();

    /**
     * Signals the undo manager that the sequence of changes which started with
     * <code>beginCompoundChange</code> has been finished. All subsequent
     * changes are considered to be individually undo-able.
     */
    void endCompoundChange();

    /**
     * Sets the limit of the undo history to the specified value. The provided
     * limit will supersede any previously set limit.
     * 
     * @param undoLimit
     *            the length of this undo manager's history
     */
    void setMaximalUndoLevel(int undoLimit);

    /**
     * Resets the history of the undo manager. After that call, there aren't any
     * undo-able or redo-able text changes.
     */
    void reset();

    /**
     * Returns whether at least one text change can be rolled back.
     * 
     * @return <code>true</code> if at least one text change can be rolled
     *         back
     */
    boolean undoable();

    /**
     * Returns whether at least one text change can be repeated. A text change
     * can be repeated only if it was executed and rolled back.
     * 
     * @return <code>true</code> if at least on text change can be repeated
     */
    boolean redoable();

    /**
     * Rolls back the most recently executed text change.
     * 
     * @throws ExecutionException
     *             if an exception occurred during undo
     */
    void undo() throws ExecutionException;

    /**
     * Repeats the most recently rolled back text change.
     * 
     * @throws ExecutionException
     *             if an exception occurred during redo
     */
    void redo() throws ExecutionException;

    /**
     * Transfers the undo history from the specified document undo manager to
     * this undo manager. This message should only be used when it is known that
     * the content of the document of the original undo manager when the last
     * undo operation was recorded is the same as this undo manager's current
     * document content, since the undo history is based on document indexes. It
     * is the responsibility of the caller to ensure that this call is used
     * correctly.
     * 
     * @param manager
     *            the document undo manger whose history is to be transferred to
     *            the receiver
     */
    void transferUndoHistory(IRichDocumentUndoManager manager);

}