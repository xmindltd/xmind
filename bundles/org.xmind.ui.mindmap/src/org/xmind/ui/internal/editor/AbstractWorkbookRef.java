package org.xmind.ui.internal.editor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistable;
import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.IDeserializer;
import org.xmind.core.IEntryStreamNormalizer;
import org.xmind.core.IFileEntry;
import org.xmind.core.ISerializer;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSource2;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.internal.zip.ArchiveConstants;
import org.xmind.core.io.ByteArrayStorage;
import org.xmind.core.io.DirectoryStorage;
import org.xmind.core.io.IStorage;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.core.util.ProgressReporter;
import org.xmind.gef.GEF;
import org.xmind.gef.command.CommandStack;
import org.xmind.gef.command.CommandStackEvent;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.ui.editor.Editable;
import org.xmind.gef.ui.editor.IEditingContext;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.IWorkbookRefListener;
import org.xmind.ui.mindmap.MindMapImageExporter;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.prefs.PrefConstants;
import org.xmind.ui.util.ImageFormat;

/**
 * This class implements basic behaviors of {@link IWorkbookRef} by extending
 * the {@link Editable} class.
 * <h2>Subclassing Notes</h2>
 * <ul>
 * <li>Each subclass <b>MUST</b> override
 * {@link #doLoadWorkbookFromURI(IProgressMonitor, URI) doLoadWorkbookFromURI()}
 * , unless it overrides one of {@link #doLoadWorkbook(IProgressMonitor)
 * doLoadWorkbook()}, {@link #doOpen(IProgressMonitor) doOpen()} and
 * {@link #open(IProgressMonitor) open()} to change the default behavior.</li>
 * <li>Each subclass <b>MUST</b> override
 * {@link #doSaveWorkbookToURI(IProgressMonitor, IWorkbook, URI)
 * doSaveWorkbookToURI()} if it may return <code>true</code> from
 * {@link #canSave()}, unless it overrides one of {@link #save(IProgressMonitor)
 * save()} and {@link #doSave(IProgressMonitor) doSave()} to change the default
 * behavior.</li>
 * <li>Each subclass <b>MUST</b> override
 * {@link #doImportFrom(IProgressMonitor, IWorkbookRef) doImportFrom()} if it
 * may return <code>true</code> from {@link #canImportFrom(IWorkbookRef)
 * canImportFrom()}, unless it overrides
 * {@link #importFrom(IProgressMonitor, IWorkbookRef) importFrom()} to change
 * the default behavior.</li>
 * </ul>
 *
 * @author Frank Shaka
 * @since 3.6.50
 */
public abstract class AbstractWorkbookRef extends Editable
        implements IWorkbookRef, ISchedulingRule, IPropertyChangeListener {

    protected static final int TEMP_SAVING_DELAY = 500;

    protected static final String ATT_TEMP_LOCATION = "tempLocation"; //$NON-NLS-1$

    private static IEditingContext defaultEditingContext = IEditingContext.NULL;

    private IWorkbook workbook = null;

    private CoreEventRegister globalEventRegister = null;

    private IStorage tempStorage = null;

    private boolean shouldLoadFromTempStorage = false;

    private ICoreEventListener globalEventListener = new ICoreEventListener() {
        public void handleCoreEvent(CoreEvent event) {
            handleGlobalEvent(event);
        }
    };

    private WorkbookRefEncryptable encryptable = null;

    private Job tempSavingJob = null;
    private Object tempSavingLock = new Object();

    private List<IWorkbookRefListener> workbookRefListeners = new ArrayList<IWorkbookRefListener>();

    protected AbstractWorkbookRef(URI uri, IMemento state) {
        super(uri);

        setEncryptable(createEncryptable());

        setCommandStack(new CommandStack(Math.max(MindMapUIPlugin.getDefault()
                .getPreferenceStore().getInt(PrefConstants.UNDO_LIMIT), 1)));
        MindMapUIPlugin.getDefault().getPreferenceStore()
                .addPropertyChangeListener(this);

        if (state != null) {
            IStorage savedTempStorage = restoreStateForTempStorage(state);
            this.shouldLoadFromTempStorage = savedTempStorage != null;
            setTempStorage(savedTempStorage);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.ui.mindmap.IWorkbookRef#getSaveWizardId()
     */
    @Override
    public String getSaveWizardId() {
        return null;
    }

    public IWorkbook getWorkbook() {
        return this.workbook;
    }

    protected void setWorkbook(IWorkbook workbook) {
        IWorkbook oldWorkbook = this.workbook;
        if (workbook == oldWorkbook)
            return;

        if (globalEventRegister != null) {
            globalEventRegister.unregisterAll();
            globalEventRegister = null;
        }

        if (oldWorkbook != null) {
            IMarkerSheet markerSheet = oldWorkbook.getMarkerSheet();
            if (markerSheet != null) {
                markerSheet.setParentSheet(null);
            }
        }

        this.workbook = workbook;

        if (workbook != null) {
            IMarkerSheet markerSheet = workbook.getMarkerSheet();
            if (markerSheet != null) {
                markerSheet.setParentSheet(
                        MindMapUI.getResourceManager().getSystemMarkerSheet());
            }

            if (globalEventRegister == null) {
                globalEventRegister = new CoreEventRegister(
                        globalEventListener);
            }
            registerGlobalEvents(globalEventRegister, workbook);
        }
    }

    private void registerGlobalEvents(CoreEventRegister register,
            IWorkbook workbook) {
        ICoreEventSupport support = (ICoreEventSupport) workbook
                .getAdapter(ICoreEventSupport.class);
        if (support != null) {
            register.setNextSupport(support);

            register.register(Core.MarkerRefAdd);
            register.register(Core.PasswordChange);
        }
    }

    private void handleGlobalEvent(CoreEvent event) {
        String type = event.getType();
        if (Core.MarkerRefAdd.equals(type)) {
            handleMarkerAdded((String) event.getTarget());
        } else if (Core.PasswordChange.equals(type)) {
            scheduleTempSaving();
        }
    }

    private void handleMarkerAdded(String markerId) {
        IMarkerGroup recentMarkerGroup = MindMapUI.getResourceManager()
                .getRecentMarkerGroup();

        IMarkerSheet systemMarkerSheet = MindMapUI.getResourceManager()
                .getSystemMarkerSheet();
        IMarker systemMarker = systemMarkerSheet.findMarker(markerId);
        if (systemMarker != null) {
            IMarkerGroup group = systemMarker.getParent();
            if (group != null) {
                if (group.getParent() != null
                        && group.getParent().equals(systemMarkerSheet)) {
                    recentMarkerGroup.addMarker(systemMarker);
                }
            }
        }
        IMarkerSheet userMarkerSheet = MindMapUI.getResourceManager()
                .getUserMarkerSheet();
        IMarker userMarker = userMarkerSheet.findMarker(markerId);
        if (userMarker != null) {
            IMarkerGroup group = userMarker.getParent();
            if (group != null) {
                if (group.getParent() != null
                        && group.getParent().equals(userMarkerSheet)) {
                    recentMarkerGroup.addMarker(userMarker);
                }
            }
        }

    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (IWorkbook.class.equals(adapter))
            return adapter.cast(getWorkbook());
        if (IPersistable.class.equals(adapter))
            return adapter.cast(getPersistable());
        if (IEncryptable.class.equals(adapter))
            return adapter.cast(encryptable);
        return super.getAdapter(adapter);
    }

    protected void setEncryptable(WorkbookRefEncryptable encryptable) {
        this.encryptable = encryptable;
    }

    protected WorkbookRefEncryptable getEncryptable() {
        return encryptable;
    }

    protected IEntryStreamNormalizer getEncryptionHandler() {
        return encryptable != null ? encryptable.getEncryptor()
                : IEntryStreamNormalizer.NULL;
    }

    @Override
    protected void doOpen(IProgressMonitor monitor)
            throws InterruptedException, InvocationTargetException {
        SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
        IWorkbook workbook = doLoadWorkbook(subMonitor.newChild(90));
        Assert.isTrue(workbook != null);
        doSaveWorkbookToTempStorage(subMonitor.newChild(10), workbook);
        setWorkbook(workbook);

        MindMapUIPlugin.getDefault().getPreferenceStore()
                .removePropertyChangeListener(this);
        MindMapUIPlugin.getDefault().getPreferenceStore()
                .addPropertyChangeListener(this);
    }

    protected IWorkbook doLoadWorkbook(IProgressMonitor monitor)
            throws InterruptedException, InvocationTargetException {
        if (shouldLoadFromTempStorage) {
            try {
                return doLoadWorkbookFromTempStorage(monitor, getTempStorage());
            } finally {
                shouldLoadFromTempStorage = false;
            }
        }
        return doLoadWorkbookFromURI(monitor, getURI());
    }

    protected IWorkbook doLoadWorkbookFromTempStorage(IProgressMonitor monitor,
            IStorage tempStorage)
            throws InterruptedException, InvocationTargetException {
        try {
            IDeserializer deserializer = Core.getWorkbookBuilder()
                    .newDeserializer();
            deserializer.setEntryStreamNormalizer(getEncryptionHandler());
            deserializer.setWorkbookStorage(tempStorage);
            deserializer.setWorkbookStorageAsInputSource();
            deserializer.deserialize(new ProgressReporter(monitor));
            return deserializer.getWorkbook();
        } catch (IOException e) {
            throw new InvocationTargetException(e);
        } catch (CoreException e) {
            if (e.getType() == Core.ERROR_CANCELLATION)
                throw new InterruptedException();
            throw new InvocationTargetException(e);
        }
    }

    /**
     * Subclasses MUST override this method.
     * 
     * @param monitor
     * @param uri
     * @return
     * @throws InterruptedException
     * @throws InvocationTargetException
     */
    protected IWorkbook doLoadWorkbookFromURI(IProgressMonitor monitor, URI uri)
            throws InterruptedException, InvocationTargetException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doSave(IProgressMonitor monitor)
            throws InterruptedException, InvocationTargetException {
        appendLog("[save] doSave start..."); //$NON-NLS-1$

        IWorkbook workbook = getWorkbook();
        Assert.isTrue(workbook != null);
        URI targetURI = getURI();
        Assert.isTrue(targetURI != null);

        ICoreEventSource workbookAsEventSource;
        if (workbook instanceof ICoreEventSource) {
            workbookAsEventSource = (ICoreEventSource) workbook;
        } else {
            workbookAsEventSource = null;
        }

        if (workbookAsEventSource != null) {
            workbookAsEventSource.getCoreEventSupport().dispatch(
                    workbookAsEventSource, new CoreEvent(workbookAsEventSource,
                            Core.WorkbookPreSaveOnce, null));
            workbookAsEventSource.getCoreEventSupport().dispatch(
                    workbookAsEventSource, new CoreEvent(workbookAsEventSource,
                            Core.WorkbookPreSave, null));
        }

        SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
        doSaveWorkbookToTempStorage(subMonitor.newChild(20), workbook);
        doSaveWorkbookToURI(subMonitor.newChild(80), workbook, targetURI);

        if (workbookAsEventSource != null) {
            workbookAsEventSource.getCoreEventSupport().dispatch(
                    workbookAsEventSource, new CoreEvent(workbookAsEventSource,
                            Core.WorkbookSave, null));
        }

        appendLog("[save] doSave over..."); //$NON-NLS-1$
    }

    /// subclasses may override to prevent default behavior or add custom behaviors
    protected void doSaveWorkbookToTempStorage(IProgressMonitor monitor,
            IWorkbook workbook)
            throws InterruptedException, InvocationTargetException {
        appendLog("[save] doSaveWorkbookToTempStorage start..."); //$NON-NLS-1$

        try {
            ISerializer serializer = Core.getWorkbookBuilder().newSerializer();
            serializer.setWorkbook(workbook);
            serializer.setWorkbookStorageAsOutputTarget();
            serializer.setEntryStreamNormalizer(getEncryptionHandler());
            serializer.serialize(new ProgressReporter(monitor));

            appendLog("[save] doSaveWorkbookToTempStorage over..."); //$NON-NLS-1$

        } catch (IOException e) {
            appendLog("[save] doSaveWorkbookToTempStorage IOException..."); //$NON-NLS-1$
            throw new InvocationTargetException(e);

        } catch (CoreException e) {
            appendLog("[save] doSaveWorkbookToTempStorage CoreException..."); //$NON-NLS-1$
            if (e.getType() == Core.ERROR_CANCELLATION)
                throw new InterruptedException();
            if (e.getType() == Core.ERROR_WRONG_PASSWORD) {
                if (getEncryptable() != null) {
                    getEncryptable().reset();
                }
            }
            throw new InvocationTargetException(e);
        }
    }

    /**
     * The default implementation does nothing but throws
     * <code>UnsupportedOperationException</code>. Subclasses <b>MUST</b>
     * override and <b>MUST NOT</b> call
     * <code>super.doSaveWorkbookToURI()</code> if <code>true</code> may be
     * returned from {@link #canSave()}.
     *
     * @see #doSave(IProgressMonitor)
     * @param monitor
     *            the progress monitor to use for reporting progress to the
     *            user. It is the caller's responsibility to call done() on the
     *            given monitor. Accepts null, indicating that no progress
     *            should be reported and that the operation cannot be cancelled.
     * @param workbook
     *            the workbook to save (never <code>null</code>)
     * @param uri
     *            the location to save workbook to (never <code>null</code>)
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
    protected void doSaveWorkbookToURI(IProgressMonitor monitor,
            IWorkbook workbook, URI uri)
            throws InterruptedException, InvocationTargetException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doClose(IProgressMonitor monitor)
            throws InterruptedException, InvocationTargetException {
        SubMonitor subMonitor = SubMonitor.convert(monitor, 100);

        stopTempSaving();

        IWorkbook workbook = getWorkbook();
        if (workbook != null) {
            doUnloadWorkbook(subMonitor.newChild(30), workbook);
            doClearTempStorageOnClose(subMonitor.newChild(60), workbook);
        }

        if (encryptable != null) {
            encryptable.reset();
        }

        subMonitor.setWorkRemaining(10);
        subMonitor.newChild(10);
        setWorkbook(null);

        setCommandStack(new CommandStack(Math.max(MindMapUIPlugin.getDefault()
                .getPreferenceStore().getInt(PrefConstants.UNDO_LIMIT), 1)));
        MindMapUIPlugin.getDefault().getPreferenceStore()
                .removePropertyChangeListener(this);
    }

    /// subclasses may override to prevent default behavior or add custom behaviors
    protected void doUnloadWorkbook(IProgressMonitor monitor,
            IWorkbook workbook)
            throws InterruptedException, InvocationTargetException {
        // do nothing, subclasses may override
    }

    /// subclasses may override to prevent default behavior or add custom behaviors
    protected void doClearTempStorageOnClose(IProgressMonitor monitor,
            IWorkbook workbook)
            throws InterruptedException, InvocationTargetException {
        IStorage storage = (IStorage) workbook.getAdapter(IStorage.class);
        if (storage != null) {
            storage.clear();
        }
    }

    protected IStorage getTempStorage() {
        if (tempStorage == null) {
            IWorkbook workbook = getWorkbook();
            if (workbook != null) {
                tempStorage = (IStorage) workbook.getAdapter(IStorage.class);
            }
            if (tempStorage == null) {
                tempStorage = createDefaultTempStorage();
            }
        }
        return this.tempStorage;
    }

    protected IStorage createDefaultTempStorage() {
        return MME.createTempStorage();
    }

    protected void setTempStorage(IStorage storage) {
        this.tempStorage = storage;
    }

    @Override
    public boolean canImportFrom(IWorkbookRef source) {
        return false;
    }

    public void importFrom(IProgressMonitor monitor, final IWorkbookRef source)
            throws InterruptedException, InvocationTargetException {
        if (!canImportFrom(source))
            throw new IllegalArgumentException(
                    "Can't import from the given workbook ref"); //$NON-NLS-1$
        if (source.isInState(CLOSED))
            throw new IllegalArgumentException(
                    "The given workbook ref is closed"); //$NON-NLS-1$
        if (!isInState(CLOSED))
            throw new IllegalStateException(
                    "Import operation is not allowed when workbook ref is already open"); //$NON-NLS-1$
        if (isInState(OPENING | CLOSING | SAVING))
            throw new IllegalStateException(
                    "Concurrent open/close/save/import operations are not allowed in a workbook ref"); //$NON-NLS-1$

        try {
            SubMonitor subMonitor = SubMonitor.convert(monitor, 100);

            subMonitor.newChild(5);
            addState(SAVING);
            try {
                doImportFrom(subMonitor.newChild(90), source);
            } finally {
                subMonitor.newChild(5);
                removeState(SAVING);
            }
        } catch (OperationCanceledException e) {
            // interpret cancellation
            throw new InterruptedException();
        }
    }

    protected void doImportFrom(IProgressMonitor monitor, IWorkbookRef source)
            throws InterruptedException, InvocationTargetException {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.ui.mindmap.IWorkbookRef#getPreviewImageData(java.lang.String,
     * org.xmind.ui.internal.editor.MindMapPreviewOptions)
     */
    @Override
    public InputStream getPreviewImageData(String sheetId,
            MindMapPreviewOptions options) throws IOException {
        if (sheetId == null)
            throw new IllegalArgumentException();

        IWorkbook workbook = getWorkbook();

        /// check if workbook is encrypted,
        /// then no preview image should be available
        IFileEntry contentEntry = workbook.getManifest()
                .getFileEntry(ArchiveConstants.CONTENT_XML);
        if (contentEntry != null && contentEntry.getEncryptionData() != null)
            return null;

        if (workbook != null
                && sheetId.equals(workbook.getPrimarySheet().getId())) {
            IFileEntry previewEntry = workbook.getManifest()
                    .getFileEntry(MindMapImageExporter
                            .toThumbnailArchivePath(ImageFormat.PNG));
            if (previewEntry == null) {
                previewEntry = workbook.getManifest()
                        .getFileEntry(MindMapImageExporter
                                .toThumbnailArchivePath(ImageFormat.JPEG));
            }

            if (previewEntry != null) {
                return previewEntry.openInputStream();
            }
        }
        return null;
    }

    protected void saveStateForTempStorage(IMemento state, IWorkbook workbook) {
        IStorage storage = (IStorage) workbook.getAdapter(IStorage.class);
        if (storage == null)
            return;

        if (storage instanceof DirectoryStorage) {
            String path = ((DirectoryStorage) storage).getFullPath();
            if (path != null) {
                state.putString(ATT_TEMP_LOCATION, path);
            }
        } else if (storage instanceof ByteArrayStorage) {
            // TODO save byte array storage state
        }
    }

    protected IStorage restoreStateForTempStorage(IMemento state) {
        String tempLocation = state.getString(ATT_TEMP_LOCATION);
        if (tempLocation != null)
            return new DirectoryStorage(new File(tempLocation));
        // TODO restore byte array storage state
        return null;
    }

    protected IPersistable getPersistable() {
        return new IPersistable() {
            @Override
            public void saveState(IMemento memento) {
                AbstractWorkbookRef.this.saveState(memento);
            }
        };
    }

    protected void saveState(IMemento memento) {
        if (isInState(CLOSED | CLOSING))
            return;

        IWorkbook workbook = getWorkbook();
        if (workbook != null) {
            saveStateForTempStorage(memento, workbook);
        }
    }

    @Override
    public boolean isDirty() {
        return super.isDirty() || (workbook instanceof ICoreEventSource2
                && ((ICoreEventSource2) workbook)
                        .hasOnceListeners(Core.WorkbookPreSaveOnce));
    }

    protected IMindMapPreviewGenerator findPreviewGenerator() {
        return getService(IMindMapPreviewGenerator.class);
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.gef.ui.editor.Editable#getService(java.lang.Class)
     */
    @Override
    protected <T> T getService(Class<T> serviceType) {
        T service = super.getService(serviceType);
        if (service == null) {
            service = defaultEditingContext.getAdapter(serviceType);
        }
        return service;
    }

    protected WorkbookRefEncryptable createEncryptable() {
        return new WorkbookRefEncryptable(this);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.core.runtime.jobs.ISchedulingRule#contains(org.eclipse.core.
     * runtime.jobs.ISchedulingRule)
     */
    @Override
    public boolean contains(ISchedulingRule rule) {
        return this.equals(rule);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.core.runtime.jobs.ISchedulingRule#isConflicting(org.eclipse.
     * core.runtime.jobs.ISchedulingRule)
     */
    @Override
    public boolean isConflicting(ISchedulingRule rule) {
        return this.equals(rule);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.gef.ui.editor.Editable#doHandleCommandStackChange(org.xmind.gef
     * .command.CommandStackEvent)
     */
    @Override
    protected void doHandleCommandStackChange(CommandStackEvent event) {
        super.doHandleCommandStackChange(event);
        if ((event.getStatus() & GEF.CS_POST_MASK) != 0) {
            scheduleTempSaving();
        }
    }

    protected void scheduleTempSaving() {
        final IWorkbook theWorkbook = getWorkbook();
        if (theWorkbook == null)
            return;

        final Object subFamily = this;
        Job job = new Job("Saving Workbook To Temporary Storage") { //$NON-NLS-1$
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                if (monitor != null && monitor.isCanceled())
                    return Status.CANCEL_STATUS;

                try {
                    /// use null progress to indicate non-stoppable task
                    doSaveWorkbookToTempStorage(null, theWorkbook);
                } catch (InterruptedException e) {
                    /// canceled, ignore exception
                } catch (InvocationTargetException e) {
                    return new Status(IStatus.WARNING,
                            MindMapUIPlugin.PLUGIN_ID,
                            "Failed to save workbook to temp location", e); //$NON-NLS-1$
                }
                return Status.OK_STATUS;
            }

            @Override
            public boolean belongsTo(Object family) {
                if (subFamily.equals(family))
                    return true;

                Object jobFamily = getJobFamily();
                return jobFamily != null && jobFamily.equals(family);
            }
        };
        job.setSystem(true);
        job.setRule(this);

        synchronized (tempSavingLock) {
            if (tempSavingJob != null) {
                tempSavingJob.cancel();
                tempSavingJob = null;
            }
            tempSavingJob = job;
        }

        scheduleTempSavingJob(job);
    }

    /**
     * @param job
     */
    protected void scheduleTempSavingJob(Job job) {
        job.schedule(TEMP_SAVING_DELAY);
    }

    protected Object getJobFamily() {
        return AbstractWorkbookRef.class;
    }

    protected void stopTempSaving() {
        synchronized (tempSavingLock) {
            if (tempSavingJob != null) {
                tempSavingJob.cancel();
                tempSavingJob = null;
            }
        }
        Job.getJobManager().cancel(this);
    }

    public static void setDefaultEditingContext(IEditingContext context) {
        defaultEditingContext = (context == null) ? IEditingContext.NULL
                : context;
    }

    public void addWorkbookRefListener(
            IWorkbookRefListener workbookRefListener) {
        workbookRefListeners.add(workbookRefListener);
    }

    public void removeWorkbookRefListener(
            IWorkbookRefListener workbookRefListener) {
        workbookRefListeners.remove(workbookRefListener);
    }

    protected void fileChanged(String title, String message, String[] buttons) {
        for (IWorkbookRefListener listener : new ArrayList<IWorkbookRefListener>(
                workbookRefListeners)) {
            listener.fileChanged(title, message, buttons);
        }
    }

    protected void fileRemoved(String title, String message, String[] buttons,
            boolean forceQuit) {
        for (IWorkbookRefListener listener : new ArrayList<IWorkbookRefListener>(
                workbookRefListeners)) {
            listener.fileRemoved(title, message, buttons, forceQuit);
        }
    }

    @Override
    public boolean activateNotifier() {
        return false;
    }

    public void propertyChange(PropertyChangeEvent event) {
        ICommandStack commandStack = getCommandStack();
        if (commandStack != null) {
            if (PrefConstants.UNDO_LIMIT.equals(event.getProperty())) {
                int num = 0;
                try {
                    num = Integer.parseInt(event.getNewValue().toString());
                } catch (Exception e) {
                }
                commandStack.setUndoLimit(Math.max(num, 1));
            }
        }
    }
}
