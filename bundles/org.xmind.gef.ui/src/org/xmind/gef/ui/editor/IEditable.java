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
package org.xmind.gef.ui.editor;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.xmind.gef.command.ICommandStack;

/**
 * This interface is a base interface that manages data of an editable document.
 * 
 * <p>
 * The primary attribute of an editable object is its URI which locates the
 * content. In a typical scenario, a client opens an editable document by
 * calling {@link #open(IProgressMonitor)}, makes changes to its content, which
 * can be saved to the URI by calling {@link #save(IProgressMonitor)}, and
 * finally closes the document by calling {@link #close(IProgressMonitor)}.
 * </p>
 * 
 * @author Frank Shaka
 * @since 3.6.50
 */
public interface IEditable extends IAdaptable {

    /**
     * State bit: The editable is open and has no errors or conflicts.
     */
    int NORMAL = 0;

    /**
     * State bit: The editable has either not been successfully opened, or has
     * been since closed. Its properties might not be valid.
     */
    int CLOSED = 1 << 0;

    /**
     * State bit: Asynchronous jobs are being performed. See
     * {@link #getProgress()} for detailed progress info.
     */
    int IN_PROGRESS = 1 << 1;

    /**
     * State bit: Current version is in conflict with another version.
     */
    int IN_CONFLICT = 1 << 2;

    /**
     * State bit: The editable is being opened.
     */
    int OPENING = 1 << 3;

    /**
     * State bit: The editable is being saved.
     */
    int SAVING = 1 << 4;

    /**
     * State bit: The editable is being closed.
     */
    int CLOSING = 1 << 5;

    /**
     * The maximum progress number.
     */
    int MAX_PROGRESS = 10000;

    /**
     *
     */
    String PROP_NAME = "name"; //$NON-NLS-1$
    String PROP_DESCRIPTION = "description"; //$NON-NLS-1$
    String PROP_MODIFICATION_TIME = "modificationTime"; //$NON-NLS-1$
    String PROP_STATE = "state"; //$NON-NLS-1$
    String PROP_PROGRESS = "progress"; //$NON-NLS-1$
    String PROP_COMMAND_STACK = "commandStack"; //$NON-NLS-1$
    String PROP_ACTIVE_CONTEXT = "activeContext"; //$NON-NLS-1$
    String PROP_EXISTS = "exists"; //$NON-NLS-1$
    String PROP_CAN_SAVE = "canSave"; //$NON-NLS-1$
    String PROP_DIRTY = "dirty"; //$NON-NLS-1$
    String PROP_MESSAGES = "messages"; //$NON-NLS-1$

    /**
     * @return
     */
    URI getURI();

    /**
     * Returns the name of this editable, or <code>null</code> if the name is
     * undetermined.
     *
     * @return name of this editable, or <code>null</code> if the name is
     *         undetermined
     */
    String getName();

    /**
     * Returns a short text describing this editable, or <code>null</code> if
     * the description is undetermined. May be same with name or not.
     *
     * @return a short text describing this editable, or <code>null</code> if
     *         the description is undetermined
     */
    String getDescription();

    /**
     *
     * @return
     */
    long getModificationTime();

    /**
     *
     * @return
     */
    int getState();

    /**
     * Tests whether this editable is in the given state.
     *
     * @param state
     * @return
     */
    boolean isInState(int state);

    /**
     * [0, MAX_PROGRESS]
     *
     * @return
     */
    int getProgress();

    /**
     *
     * @return
     */
    ICommandStack getCommandStack();

    /**
     *
     * @return
     */
    IEditingContext getActiveContext();

    /**
     *
     * @param context
     */
    void setActiveContext(IEditingContext context);

    /**
     *
     * @return
     */
    boolean exists();

    /**
     * Opens this editable and loads its content from the URI, or does nothing
     * if the editable is already open or is being opened.
     *
     * @param monitor
     *            the progress monitor to use for reporting progress to the
     *            user. It is the caller's responsibility to call done() on the
     *            given monitor. Accepts null, indicating that no progress
     *            should be reported and that the operation cannot be cancelled.
     * @exception InvocationTargetException
     *                if this method must propagate a checked exception, it
     *                should wrap it inside an
     *                <code>InvocationTargetException</code>; runtime exceptions
     *                are automatically wrapped in an
     *                <code>InvocationTargetException</code> by the calling
     *                context
     * @exception InterruptedException
     *                if the operation detects a request to cancel, using
     *                <code>IProgressMonitor.isCanceled()</code>, it should exit
     *                by throwing <code>InterruptedException</code>
     */
    void open(IProgressMonitor monitor)
            throws InterruptedException, InvocationTargetException;

    /**
     * Closes this editable and unload its content, after saving any unsaved
     * changes, or does nothing if the editable is already closed or never
     * opened.
     *
     * @param monitor
     *            the progress monitor to use for reporting progress to the
     *            user. It is the caller's responsibility to call done() on the
     *            given monitor. Accepts null, indicating that no progress
     *            should be reported and that the operation cannot be cancelled.
     * @exception InvocationTargetException
     *                if this method must propagate a checked exception, it
     *                should wrap it inside an
     *                <code>InvocationTargetException</code>; runtime exceptions
     *                are automatically wrapped in an
     *                <code>InvocationTargetException</code> by the calling
     *                context
     * @exception InterruptedException
     *                if the operation detects a request to cancel, using
     *                <code>IProgressMonitor.isCanceled()</code>, it should exit
     *                by throwing <code>InterruptedException</code>
     */
    void close(IProgressMonitor monitor)
            throws InterruptedException, InvocationTargetException;

    /**
     * Checks whether this editable can be saved.
     *
     * @return
     */
    boolean canSave();

    /**
     * Saves this editable to the URI.
     *
     * @param monitor
     *            the progress monitor to use for reporting progress to the
     *            user. It is the caller's responsibility to call done() on the
     *            given monitor. Accepts null, indicating that no progress
     *            should be reported and that the operation cannot be cancelled.
     * @exception InvocationTargetException
     *                if this method must propagate a checked exception, it
     *                should wrap it inside an
     *                <code>InvocationTargetException</code>; runtime exceptions
     *                are automatically wrapped in an
     *                <code>InvocationTargetException</code> by the calling
     *                context
     * @exception InterruptedException
     *                if the operation detects a request to cancel, using
     *                <code>IProgressMonitor.isCanceled()</code>, it should exit
     *                by throwing <code>InterruptedException</code>
     */
    void save(IProgressMonitor monitor)
            throws InterruptedException, InvocationTargetException;

    /**
     *
     * @return
     */
    boolean isDirty();

    /**
     *
     */
    void discardChanges();

    /**
     *
     * @param cleaner
     */
    void markDirtyWith(IEditableCleaner cleaner);

    /**
     *
     * @param cleaner
     */
    void unmarkDirtyWith(IEditableCleaner cleaner);

    /**
     * 
     * @return
     */
    List<IInteractiveMessage> getMessages();

    /**
     * 
     * @param message
     */
    void addMessage(IInteractiveMessage message);

    /**
     * 
     * @param message
     */
    void removeMessage(IInteractiveMessage message);

    /**
     *
     * @param listener
     */
    void addPropertyChangeListener(IPropertyChangeListener listener);

    /**
     *
     * @param listener
     */
    void removePropertyChangeListener(IPropertyChangeListener listener);

}
