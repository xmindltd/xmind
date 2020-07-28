/*
 * *****************************************************************************
 * * Copyright (c) 2006-2012 XMind Ltd. and others. This file is a part of XMind
 * 3. XMind releases 3 and above are dual-licensed under the Eclipse Public
 * License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details. Contributors: XMind Ltd. -
 * initial API and implementation
 *******************************************************************************/
package org.xmind.gef.ui.editor;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.xmind.gef.GEF;
import org.xmind.gef.command.CommandStack;
import org.xmind.gef.command.CommandStackEvent;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.command.ICommandStackListener;

/**
 * <p>
 * This class implements basic behaviors of {@link IEditable}. A document-based
 * application should create its own subclass of <code>Editable</code> and
 * provide abilities to access actual content.
 * </p>
 * <h2>Subclassing Notes</h2>
 * <ul>
 * <li>Each subclass <b>MUST</b> override {@link #doOpen(IProgressMonitor)
 * doOpen()} and <b>MUST NOT</b> call <code>super.doOpen()</code>, unless it
 * overrides {@link #open(IProgressMonitor) open()} to change the default
 * behavior.</li>
 * <li>Each subclass <b>MUST</b> override {@link #doSave(IProgressMonitor)
 * doSave()} and <b>MUST NOT</b> call <code>super.doSave()</code> if it may
 * return <code>true</code> by overriding {@link #canSave()}, unless it
 * overrides {@link #save(IProgressMonitor)} to change the default behavior.
 * </li>
 * <li>Each subclass may override {@link #doClose(IProgressMonitor) doClose()}
 * to perform additional actions while closing the document.</li>
 * </ul>
 *
 * @author Frank Shaka
 * @since 3.6.50
 */
public abstract class Editable implements IEditable {

    private URI uri;

    private int state;

    private long modificationTime = 0;

    private int progress = 0;

    private ICommandStack commandStack = null;

    private IEditingContext activeContext = null;

    private final List<IEditableCleaner> cleaners = new ArrayList<IEditableCleaner>();

    private final ListenerList<IPropertyChangeListener> listenerManager = new ListenerList<IPropertyChangeListener>();

    private int contentRefCount = 0;

    private String logMessage = ""; //$NON-NLS-1$

    private ICommandStackListener commandStackHook = new ICommandStackListener() {

        @Override
        public void handleCommandStackEvent(CommandStackEvent event) {
            if ((event.getStatus() & GEF.CS_UPDATED) != 0) {
                boolean isDirty = isDirty();
                firePropertyChanged(PROP_DIRTY, false, isDirty);
            }

            doHandleCommandStackChange(event);
        }
    };

    private final List<IInteractiveMessage> messages = new ArrayList<IInteractiveMessage>();

    protected Editable(URI uri) {
        this.uri = uri;
        this.state = CLOSED;
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (URI.class.equals(adapter))
            return adapter.cast(getURI());
        if (ICommandStack.class.equals(adapter))
            return adapter.cast(getCommandStack());
        return Platform.getAdapterManager().getAdapter(this, adapter);
    }

    @Override
    public URI getURI() {
        return this.uri;
    }

    @Override
    public String getName() {
        if (this.uri != null) {
            String path = this.uri.getPath();
            if (path != null && path.length() > 0) {
                if (path.charAt(path.length() - 1) == '/') {
                    path = path.substring(0, path.length() - 1);
                }
                int sep = path.lastIndexOf('/');
                if (sep >= 0) {
                    return path.substring(sep + 1);
                }
            }
        }
        return null;
    }

    @Override
    public String getDescription() {
        return getName();
    }

    @Override
    public long getModificationTime() {
        return this.modificationTime;
    }

    protected void setModificationTime(long modificationTime) {
        long oldModificationTime = this.modificationTime;
        if (modificationTime == oldModificationTime)
            return;

        this.modificationTime = modificationTime;
        firePropertyChanged(PROP_MODIFICATION_TIME, oldModificationTime,
                modificationTime);
    }

    @Override
    public synchronized int getState() {
        return this.state;
    }

    @Override
    public boolean isInState(int state) {
        return (getState() & state) != 0;
    }

    protected void setState(int newState) {
        int oldState;
        synchronized (this) {
            oldState = this.state;
            if (newState == oldState)
                return;
            this.state = newState;
        }
        firePropertyChanged(PROP_STATE, oldState, newState);
    }

    protected void modifyState(int stateToAdd, int stateToRemove) {
        int newState = getState();
        if (stateToAdd != 0) {
            newState |= stateToAdd;
        }
        if (stateToRemove != 0) {
            newState &= ~stateToRemove;
        }
        setState(newState);
    }

    protected void addState(int state) {
        setState(getState() | state);
    }

    protected void removeState(int state) {
        setState(getState() & (~state));
    }

    @Override
    public synchronized int getProgress() {
        return this.progress;
    }

    protected void setProgress(int newProgress) {
        int oldProgress;
        synchronized (this) {
            oldProgress = this.progress;
            if (newProgress == oldProgress)
                return;
            this.progress = newProgress;
        }
        firePropertyChanged(PROP_PROGRESS, oldProgress, newProgress);
    }

    protected void addProgress(int delta) {
        setProgress(getProgress() + delta);
    }

    protected void removeProgress(int delta) {
        setProgress(getProgress() - delta);
    }

    @Override
    public ICommandStack getCommandStack() {
        if (this.commandStack == null) {
            setCommandStack(createDefaultCommandStack());
        }
        return this.commandStack;
    }

    protected ICommandStack createDefaultCommandStack() {
        return new CommandStack();
    }

    protected void setCommandStack(ICommandStack commandStack) {
        ICommandStack oldCommandStack = this.commandStack;
        if (oldCommandStack == commandStack)
            return;

        if (oldCommandStack != null) {
            oldCommandStack.removeCSListener(commandStackHook);
            oldCommandStack.dispose();
        }
        this.commandStack = commandStack;
        if (commandStack != null) {
            commandStack.addCSListener(commandStackHook);
        }
        firePropertyChanged(PROP_COMMAND_STACK, oldCommandStack, commandStack);
    }

    @Override
    public IEditingContext getActiveContext() {
        IEditingContext context = this.activeContext;
        return context == null ? IEditingContext.NULL : context;
    }

    protected <T> T getService(Class<T> serviceType) {
        return getActiveContext().getAdapter(serviceType);
    }

    @Override
    public void setActiveContext(IEditingContext context) {
        IEditingContext oldContext = this.activeContext;
        if (oldContext == context)
            return;
        this.activeContext = context;
        firePropertyChanged(PROP_ACTIVE_CONTEXT, oldContext, context);
    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public boolean isDirty() {
        return (commandStack != null && commandStack.isDirty())
                || !cleaners.isEmpty();
    }

    @Override
    public void markDirtyWith(IEditableCleaner cleaner) {
        boolean wasDirty = isDirty();
        synchronized (this.cleaners) {
            this.cleaners.add(cleaner);
        }
        boolean isDirty = isDirty();
        if (wasDirty != isDirty) {
            firePropertyChanged(PROP_DIRTY, wasDirty, isDirty);
        }
    }

    @Override
    public void unmarkDirtyWith(IEditableCleaner cleaner) {
        boolean wasDirty = isDirty();
        synchronized (this.cleaners) {
            this.cleaners.remove(cleaner);
        }
        boolean isDirty = isDirty();
        if (wasDirty != isDirty) {
            firePropertyChanged(PROP_DIRTY, wasDirty, isDirty);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.gef.ui.editor.IEditable#discardChanges()
     */
    @Override
    public void discardChanges() {
        boolean wasDirty = isDirty();
        doDiscardChanges();
        boolean isDirty = isDirty();
        if (wasDirty != isDirty) {
            firePropertyChanged(PROP_DIRTY, wasDirty, isDirty);
        }
    }

    protected void doDiscardChanges() {
        if (commandStack != null) {
            while (commandStack.isDirty() && commandStack.canUndo()) {
                commandStack.markSaved();
            }
        }
        synchronized (this.cleaners) {
            this.cleaners.clear();
        }
    }

    protected void clean(IProgressMonitor monitor)
            throws InterruptedException, InvocationTargetException {
        IEditableCleaner[] theCleaners = new IEditableCleaner[0];
        synchronized (this.cleaners) {
            theCleaners = this.cleaners.toArray(theCleaners);
            this.cleaners.clear();
        }

        SubMonitor subMonitor = SubMonitor.convert(monitor, theCleaners.length);
        for (int i = 0; i < theCleaners.length; i++) {
            IEditableCleaner cleaner = theCleaners[i];
            doClean(subMonitor.newChild(1), cleaner);
        }
    }

    protected void doClean(IProgressMonitor monitor, IEditableCleaner cleaner)
            throws InterruptedException, InvocationTargetException {
        cleaner.cleanEditable(monitor, this);
    }

    @Override
    public void addPropertyChangeListener(IPropertyChangeListener listener) {
        listenerManager.add(listener);
    }

    @Override
    public void removePropertyChangeListener(IPropertyChangeListener listener) {
        listenerManager.remove(listener);
    }

    protected void firePropertyChanged(String property, Object oldValue,
            Object newValue) {
        final PropertyChangeEvent event = new PropertyChangeEvent(this,
                property, oldValue, newValue);
        for (final Object o : listenerManager.getListeners()) {
            if (o instanceof IPropertyChangeListener) {
                try {
                    ((IPropertyChangeListener) o).propertyChange(event);
                } catch (Exception e) {
                    handlePropertyChangeNotificationError(e);
                }
            }
        }
    }

    private void handlePropertyChangeNotificationError(Exception e) {
        // TODO handle errors during editable state change notification
    }

    @Override
    public void open(IProgressMonitor monitor)
            throws InterruptedException, InvocationTargetException {
        if (!isInState(CLOSED)) {
            // already opened
            contentRefCount += 1;
            return;
        }

        if (isInState(OPENING | CLOSING | SAVING))
            // already being opened
            throw new IllegalStateException(
                    "Concurrent open/close/save operations are not allowed"); //$NON-NLS-1$

        try {
            SubMonitor subMonitor = SubMonitor.convert(monitor, 100);

            subMonitor.newChild(10);
            addState(OPENING);
            try {
                if (subMonitor.isCanceled())
                    throw new InterruptedException();
                doOpen(subMonitor.newChild(80));

                subMonitor.newChild(5);
                contentRefCount += 1;
                if (isInState(CLOSED)) {
                    removeState(CLOSED);
                }
            } finally {
                subMonitor.setWorkRemaining(5);
                subMonitor.newChild(5);
                removeState(OPENING);
            }
        } catch (OperationCanceledException e) {
            // interpret cancellation
            throw new InterruptedException();
        }
    }

    /**
     * Perform actual <em>open</em> operations.
     * <p>
     * This method is, by default, called by {@link #open(IProgressMonitor)} and
     * its default implementation from {@link Editable} does nothing but throws
     * {@link UnsupportedOperationException}, so subclasses <b>MUST</b> override
     * this method and <b>MUST NOT</b> call <code>super.doOpen()</code>, unless
     * they override {@link #open(IProgressMonitor)} to change the default
     * behavior.
     * </p>
     *
     * @see #open(IProgressMonitor)
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
    protected void doOpen(IProgressMonitor monitor)
            throws InterruptedException, InvocationTargetException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canSave() {
        return false;
    }

    @Override
    public void save(IProgressMonitor monitor)
            throws InterruptedException, InvocationTargetException {
        logMessage = ""; //$NON-NLS-1$
        appendLog("[save] start..."); //$NON-NLS-1$

        if (!canSave()) {
            appendLog("[save] save operation is not allowed...over..."); //$NON-NLS-1$
            throw new IllegalStateException("Save operation is not allowed"); //$NON-NLS-1$
        }

        if (isInState(CLOSED)) {
            // already closed
            appendLog(
                    "[save] can't perform save operation while editable is closed...over..."); //$NON-NLS-1$
            throw new IllegalStateException(
                    "Can't perform save operation while editable is closed"); //$NON-NLS-1$
        }

        if (isInState(OPENING | CLOSING | SAVING)) {
            // already being opened/closing/saving
            appendLog(
                    "[save] concurrent open/close/save operations are not allowed in SynchronizedEditable...over..."); //$NON-NLS-1$
            throw new IllegalStateException(
                    "Concurrent open/close/save operations are not allowed in SynchronizedEditable"); //$NON-NLS-1$
        }

        try {
            SubMonitor subMonitor = SubMonitor.convert(monitor, 100);

            subMonitor.newChild(5);
            appendLog("[save] add state..."); //$NON-NLS-1$
            addState(SAVING);
            try {
                boolean wasDirty = isDirty();
                if (subMonitor.isCanceled()) {
                    appendLog("[save] canceled...over..."); //$NON-NLS-1$
                    throw new InterruptedException();
                }

                appendLog("[save] clean..."); //$NON-NLS-1$
                clean(subMonitor.newChild(5));

                if (subMonitor.isCanceled()) {
                    appendLog("[save] canceled...over..."); //$NON-NLS-1$
                    throw new OperationCanceledException();
                }

                doSave(subMonitor.newChild(80));
                markSaved(subMonitor.newChild(5));

                subMonitor.newChild(4);
                boolean isDirty = isDirty();
                if (wasDirty != isDirty) {
                    firePropertyChanged(PROP_DIRTY, wasDirty, isDirty);
                }

                appendLog("[save] success...over..."); //$NON-NLS-1$
            } finally {
                subMonitor.setWorkRemaining(1);
                subMonitor.newChild(1);
                removeState(SAVING);

                log(null, logMessage);
                logMessage = ""; //$NON-NLS-1$
            }
        } catch (OperationCanceledException e) {
            // interpret cancellation
            throw new InterruptedException();
        }
    }

    /**
     * Perform actual <em>save</em> operations.
     * <p>
     * This method is, by default, called by {@link #save(IProgressMonitor)} and
     * its default implementation from {@link Editable} does nothing but throws
     * {@link UnsupportedOperationException}, so subclasses <b>MUST</b> override
     * this method and <b>MUST NOT</b> call <code>super.doSave()</code> if they
     * return <code>true</code> by overriding {@link #canSave()}, unless they
     * override {@link #save(IProgressMonitor)} to change the default behavior.
     * </p>
     *
     * @see #save(IProgressMonitor)
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
    protected void doSave(IProgressMonitor monitor)
            throws InterruptedException, InvocationTargetException {
        throw new UnsupportedOperationException();
    }

    /**
     * Called after the save operation has successfully completed. Typically
     * used to send notifications.
     * <p>
     * Subclasses may override and call <code>super.markSaved()</code>.
     * </p>
     *
     * @param monitor
     * @throws InterruptedException
     * @throws InvocationTargetException
     */
    protected void markSaved(IProgressMonitor monitor)
            throws InterruptedException, InvocationTargetException {
        ICommandStack commandStack = getCommandStack();
        if (commandStack != null) {
            commandStack.markSaved();
        }
    }

    @Override
    public void close(IProgressMonitor monitor)
            throws InterruptedException, InvocationTargetException {
        if (isInState(CLOSED))
            // already closed
            return;

        if (isInState(OPENING | CLOSING | SAVING))
            // already being opened
            throw new IllegalStateException(
                    "Concurrent open/close/save operations are not allowed in SynchronizedEditable"); //$NON-NLS-1$

        if (contentRefCount > 1) {
            // some other clients requiring content
            // should do nothing
            contentRefCount -= 1;
            return;
        }

        try {
            // no other clients requiring content
            SubMonitor subMonitor = SubMonitor.convert(monitor, 100);

            if (isDirty() && canSave()) {
                // should save first
                doSave(subMonitor.newChild(70));
            }
            if (subMonitor.isCanceled())
                throw new InterruptedException();
            subMonitor.setWorkRemaining(30);

            if (commandStack != null) {
                commandStack.clear();
            }

            subMonitor.newChild(5);
            contentRefCount -= 1;
            addState(CLOSED);
            try {
                if (subMonitor.isCanceled())
                    throw new InterruptedException();
                doClose(subMonitor.newChild(20));
                if (!isInState(CLOSED)) {
                    addState(CLOSED);
                }
            } finally {
                subMonitor.setWorkRemaining(5);
                subMonitor.newChild(5);
                removeState(CLOSING);
            }
        } catch (OperationCanceledException e) {
            // interpret cancellation
            throw new InterruptedException();
        }
    }

    /**
     * Perform actual <em>close</em> operations.
     * <p>
     * This method is, by default, called from {@link #close(IProgressMonitor)}
     * and its default implementation from {@link Editable} does nothing, so
     * subclasses <em>may or may not</em> override this method and need not to
     * call <code>super.doClose()</code>.
     * </p>
     *
     * @see #close(IProgressMonitor)
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
    protected void doClose(IProgressMonitor monitor)
            throws InterruptedException, InvocationTargetException {
        // do nothing, subclasses may override
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.gef.ui.editor.IEditable#getMessages()
     */
    @Override
    public List<IInteractiveMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.gef.ui.editor.IEditable#addMessage(org.eclipse.jface.dialogs.
     * IMessageProvider)
     */
    @Override
    public void addMessage(IInteractiveMessage message) {
        List<IInteractiveMessage> oldMessages = new ArrayList<IInteractiveMessage>(
                messages);
        if (messages.add(message)) {
            firePropertyChanged(PROP_MESSAGES, oldMessages,
                    new ArrayList<IInteractiveMessage>(messages));
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.gef.ui.editor.IEditable#removeMessage(org.eclipse.jface.dialogs
     * .IMessageProvider)
     */
    @Override
    public void removeMessage(IInteractiveMessage message) {
        List<IInteractiveMessage> oldMessages = new ArrayList<IInteractiveMessage>(
                messages);
        if (messages.remove(message)) {
            firePropertyChanged(PROP_MESSAGES, oldMessages,
                    new ArrayList<IInteractiveMessage>(messages));
        }
    }

    protected void doHandleCommandStackChange(CommandStackEvent event) {
        /// do nothing, subclasses may override
    }

    protected void appendLog(String message) {
        logMessage += message;
    }

    protected void log(Throwable e, String message) {
    }

}
