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
package org.xmind.ui.internal.editor;

import java.net.URI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.xmind.core.Core;
import org.xmind.core.IEncryptionHandler;
import org.xmind.core.IMeta;
import org.xmind.core.IRevision;
import org.xmind.core.IRevisionManager;
import org.xmind.core.IRevisionRepository;
import org.xmind.core.ISheet;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSource2;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.internal.dom.WorkbookImpl;
import org.xmind.core.io.IStorage;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.gef.GEF;
import org.xmind.gef.command.CommandStack;
import org.xmind.gef.command.CommandStackEvent;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.command.ICommandStackListener;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.prefs.PrefConstants;

/**
 * @author Frank Shaka
 * @since 3.0
 * @deprecated
 */
@Deprecated
public class WorkbookRef
// implements IWorkbookRef, IPropertyChangeListener {
{

//    @Deprecated
//    private final WorkbookRefManager manager;
    @Deprecated
    private URI uri;
    @Deprecated
    private String name;
    @Deprecated
    private IWorkbook workbook;
    @Deprecated
    private ICommandStack commandStack;
    @Deprecated
    private IAdaptable activeContext;
//    @Deprecated
//    private IWorkbookRefStatus status;

    @Deprecated
    private boolean undoing = false;

//    @Deprecated
//    private ListenerList listeners = new ListenerList();

    @Deprecated
    private CoreEventRegister globalEventRegister = null;

    @Deprecated
    private ICoreEventListener globalEventListener = new ICoreEventListener() {
        public void handleCoreEvent(CoreEvent event) {
            handleGlobalEvent(event);
        }
    };

    @Deprecated
    public WorkbookRef(WorkbookRefManager manager, URI uri, IWorkbook workbook,
            String name) {
//        this.manager = manager;
        this.uri = uri;
        this.workbook = workbook;
        this.name = name;
        this.commandStack = new CommandStack(
                Math.max(MindMapUIPlugin.getDefault().getPreferenceStore()
                        .getInt(PrefConstants.UNDO_LIMIT), 1));
        this.commandStack.addCSListener(new ICommandStackListener() {
            public void handleCommandStackEvent(CommandStackEvent event) {
                handleCommandStackChange(event);
            }
        });
//        MindMapUIPlugin.getDefault().getPreferenceStore()
//                .addPropertyChangeListener(this);
//        int statusCode = workbook == null ? IWorkbookRefStatus.LOADING
//                : IWorkbookRefStatus.CLEAN;
//        this.status = new WorkbookRefStatus(statusCode,
//                IWorkbookRefStatus.INITIAL,
//                new Status(IStatus.INFO, MindMapUIPlugin.PLUGIN_ID, null));
    }

    @Deprecated
    public URI getURI() {
        return this.uri;
    }

    @Deprecated
    public String getName() {
        return this.name;
    }

    @Deprecated
    public ICommandStack getCommandStack() {
        return this.commandStack;
    }

    @Deprecated
    public IWorkbook getWorkbook() {
        return this.workbook;
    }

    @Deprecated
    public IAdaptable getActiveContext() {
        return this.activeContext;
    }

//    @Deprecated
//    public void addStatusListener(IWorkbookRefStatusListener listener) {
//        listeners.add(listener);
//    }
//
//    @Deprecated
//    public void removeListener(IWorkbookRefStatusListener listener) {
//        listeners.remove(listener);
//    }
//
//    public IWorkbookRefStatus getStatus() {
//        return status;
//    }

    @Deprecated
    public void markDirty() {

    }

    @Deprecated
    protected void setURI(URI uri) {
        this.uri = uri;
    }

    @Deprecated
    protected void setWorkbook(IWorkbook workbook) {
        if (workbook == this.workbook)
            return;

        if (globalEventRegister != null) {
            globalEventRegister.unregisterAll();
        }

        this.workbook = workbook;
        if (workbook != null) {
            IMarkerSheet markerSheet = workbook.getMarkerSheet();
            if (markerSheet != null) {
                markerSheet.setParentSheet(
                        MindMapUI.getResourceManager().getUserMarkerSheet());
            }

            if (globalEventRegister == null) {
                globalEventRegister = new CoreEventRegister(
                        globalEventListener);
            }
            registerGlobalEvents(globalEventRegister, workbook);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Deprecated
    public <T> T getAdapter(Class<T> adapter) {
        if (adapter == URI.class)
            return adapter.cast(getURI());
        if (adapter == IWorkbook.class)
            return adapter.cast(getWorkbook());
        if (adapter == ICommandStack.class)
            return adapter.cast(getCommandStack());
        if (activeContext != null)
            return activeContext.getAdapter(adapter);
        return null;
    }

    @Deprecated
    public void close() {
//        MindMapUIPlugin.getDefault().getPreferenceStore()
//                .removePropertyChangeListener(this);
        if (commandStack != null) {
            commandStack.dispose();
            commandStack = null;
        }
        if (globalEventRegister != null) {
            globalEventRegister.unregisterAll();
            globalEventRegister = null;
        }
        if (workbook != null) {
            closeWorkbook(workbook);
        }
        workbook = null;
    }

    @Deprecated
    private void closeWorkbook(IWorkbook workbook) {
        ICoreEventSupport support = (ICoreEventSupport) workbook
                .getAdapter(ICoreEventSupport.class);
        if (support != null) {
            support.dispatchTargetChange((ICoreEventSource) workbook,
                    MindMapUI.WorkbookClose, this);
        }
    }

    @Deprecated
    public boolean isContentDirty() {
        if (workbook == null)
            return false;
        if (getCommandStack() != null && getCommandStack().isDirty())
            return true;
        return workbook instanceof ICoreEventSource2
                && ((ICoreEventSource2) workbook)
                        .hasOnceListeners(Core.WorkbookPreSaveOnce);
    }

    @Deprecated
    public boolean isDirty() {
        return isContentDirty();
    }

    @Deprecated
    public void loadWorkbook(IEncryptionHandler encryptionHandler,
            IProgressMonitor monitor) throws CoreException {
//        synchronized (ioLock) {
//        loadWorkbook(createStorage(), encryptionHandler, monitor);
//        }
    }

    @Deprecated
    public void loadWorkbook(IStorage storage,
            IEncryptionHandler encryptionHandler, IProgressMonitor monitor)
                    throws CoreException {
//        synchronized (ioLock) {
        if (workbook != null)
            return;

//            if (workbookLoader == null)
//                throw new CoreException(
//                        new Status(IStatus.ERROR, MindMapUIPlugin.PLUGIN_ID,
//                                "No workbook loader is set.")); //$NON-NLS-1$
//
//            setWorkbook(workbookLoader.loadWorkbook(storage, encryptionHandler,
//                    monitor));
//        }
    }

    @Deprecated
    public void saveWorkbook(IProgressMonitor monitor,
            IWorkbookReferrer previewSaver, boolean skipNewRevisions)
                    throws CoreException {
//        synchronized (ioLock) {
        monitor.beginTask(null, 100);
        if (workbook == null)
            throw new CoreException(new Status(IStatus.ERROR,
                    MindMapUIPlugin.PLUGIN_ID, "No workbook to save.")); //$NON-NLS-1$
//        if (workbookSaver == null)
//            throw new CoreException(
//                    new Status(IStatus.ERROR, MindMapUIPlugin.PLUGIN_ID,
//                            "No workbook saver has been set.")); //$NON-NLS-1$

        // Leave 1 tick for finalizing work:
        int mainWorkTicks = 99;

        if (!skipNewRevisions) {
            monitor.subTask(
                    MindMapMessages.WorkbookSaver_CreateRevisions_taskName);
            saveRevisions(monitor);
        }
        monitor.worked(10);
        mainWorkTicks -= 10;

        // Delete old preview:
        workbook.getManifest().deleteFileEntry("Thumbnails/thumbnail.jpg"); //$NON-NLS-1$
        workbook.getManifest().deleteFileEntry("Thumbnails/thumbnail.png"); //$NON-NLS-1$
        if (previewSaver != null) {
            monitor.subTask(
                    MindMapMessages.WorkbookSaver_SavePreviewImage_taskName);
//            savePreview(monitor);
        } else {
//            setPreviewOutdated(true);
        }
        monitor.worked(10);
        mainWorkTicks -= 10;

        monitor.subTask(
                MindMapMessages.WorkbookSaver_SaveWorkbookContent_taskName);
//        WorkbookBackupManager wbm = WorkbookBackupManager.getInstance();
//        IWorkbookBackup backup = wbm.ensureBackedUp(this, monitor);
        try {
//            workbookSaver.save(monitor, workbook);
        } catch (Throwable e) {
//            if (backup != null) {
//                backup.restore(monitor);
//            }
            if (e instanceof CoreException)
                throw (CoreException) e;
            throw new CoreException(new Status(IStatus.ERROR,
                    MindMapUI.PLUGIN_ID, e.getLocalizedMessage(), e));
        }
//        wbm.removeWorkbook(this);
//        wbm.addWorkbook(this);
        monitor.worked(mainWorkTicks);

        monitor.subTask(MindMapMessages.WorkbookSaver_Finalize_taskName);
//        for (IWorkbookReferrer referrer : getReferrers()) {
//            referrer.postSave(monitor);
//        }

        monitor.done();
//        }
    }

    @Deprecated
    public void saveWorkbookAs(Object newKey, IProgressMonitor monitor,
            IWorkbookReferrer previewSaver, boolean skipNewRevisions)
                    throws CoreException {
//        synchronized (ioLock) {
        monitor.beginTask(null, 100);
        if (workbook == null)
            throw new CoreException(new Status(IStatus.ERROR,
                    MindMapUIPlugin.PLUGIN_ID, "No workbook to save.")); //$NON-NLS-1$

        monitor.subTask(
                MindMapMessages.WorkbookSaver_PrepareNewSaveTarget_taskName);
//        Object oldKey = getKey();
//        setKey(newKey);
//        setWorkbookLoader(null);
//        setWorkbookSaver(null);
//        WorkbookRefInitializer.getInstance().initialize(this, newKey,
//                getPrimaryReferrer());
//        if (workbookSaver == null)
//            throw new CoreException(
//                    new Status(IStatus.ERROR, MindMapUIPlugin.PLUGIN_ID,
//                            "No workbook saver has been set.")); //$NON-NLS-1$

        // Leave 1 tick for finalizing work:
        int mainWorkTicks = 99;

//        WorkbookRefManager.getInstance().changeKey(this, oldKey, newKey);
        monitor.worked(10);
        mainWorkTicks -= 10;

        if (!skipNewRevisions) {
            monitor.subTask(
                    MindMapMessages.WorkbookSaver_CreateRevisions_taskName);
            saveRevisions(monitor);
        }
        monitor.worked(10);
        mainWorkTicks -= 10;

        // Delete old preview:
        workbook.getManifest().deleteFileEntry("Thumbnails/thumbnail.jpg"); //$NON-NLS-1$
        workbook.getManifest().deleteFileEntry("Thumbnails/thumbnail.png"); //$NON-NLS-1$
        if (previewSaver != null) {
            monitor.subTask(
                    MindMapMessages.WorkbookSaver_SavePreviewImage_taskName);
//            savePreview(monitor);
        } else {
//            setPreviewOutdated(true);
        }
        monitor.worked(10);
        mainWorkTicks -= 10;

        monitor.subTask(
                MindMapMessages.WorkbookSaver_SaveWorkbookContent_taskName);
//        workbookSaver.save(monitor, workbook);
        monitor.worked(mainWorkTicks);

//        WorkbookBackupManager.getInstance().removeWorkbook(this);
//        WorkbookBackupManager.getInstance().addWorkbook(this);

        monitor.subTask(MindMapMessages.WorkbookSaver_Finalize_taskName);
//        for (IWorkbookReferrer referrer : getReferrers()) {
//            referrer.postSaveAs(newKey, monitor);
//        }
        monitor.done();
//        }
    }

    @Deprecated
    private void saveRevisions(IProgressMonitor monitor) throws CoreException {
        if (!isContentDirty()
                || ((WorkbookImpl) workbook).isSkipRevisionsWhenSaving()
                || !shouldSaveNewRevisions())
            return;

        IRevisionRepository repo = workbook.getRevisionRepository();
        for (ISheet sheet : workbook.getSheets()) {
            IRevisionManager manager = repo.getRevisionManager(sheet.getId(),
                    IRevision.SHEET);
            IRevision latestRevision = manager.getLatestRevision();
            if (latestRevision == null || sheet.getModifiedTime() == 0 || sheet
                    .getModifiedTime() > latestRevision.getTimestamp()) {
                try {
                    manager.addRevision(sheet);
                } catch (Throwable e) {
                    throw new CoreException(new Status(IStatus.ERROR,
                            MindMapUIPlugin.PLUGIN_ID, null, e));
                }
            }
        }
    }

    @Deprecated
    private boolean shouldSaveNewRevisions() {
        String value = workbook.getMeta()
                .getValue(IMeta.CONFIG_AUTO_REVISION_GENERATION);
        return value == null || IMeta.V_YES.equalsIgnoreCase(value);
    }

    @Deprecated
    public void propertyChange(PropertyChangeEvent event) {
        if (commandStack != null) {
            if (PrefConstants.UNDO_LIMIT.equals(event.getProperty())) {
                commandStack.setUndoLimit(
                        Math.max((Integer) event.getNewValue(), 1));
            }
        }
    }

    @Deprecated
    public String toString() {
        return uri == null ? (workbook == null ? "UnrecognizedWorkbookRef" //$NON-NLS-1$
                : workbook.toString()) : uri.toString();
    }

    @Deprecated
    private void registerGlobalEvents(CoreEventRegister register,
            IWorkbook workbook) {
        ICoreEventSupport support = (ICoreEventSupport) workbook
                .getAdapter(ICoreEventSupport.class);
        if (support != null) {
            register.setNextSupport(support);

            register.register(Core.MarkerRefAdd);
        }
    }

    @Deprecated
    private void handleGlobalEvent(CoreEvent event) {
        String type = event.getType();
        if (Core.MarkerRefAdd.equals(type)) {
            handleMarkerAdded((String) event.getTarget());
        }
    }

    @Deprecated
    private void handleMarkerAdded(String markerId) {
        if (undoing)
            return;

        IMarker systemMarker = MindMapUI.getResourceManager()
                .getSystemMarkerSheet().findMarker(markerId);
        if (systemMarker != null) {
            IMarkerGroup group = systemMarker.getParent();
            if (group != null) {
                if (group.getParent() != null && group.getParent().equals(
                        MindMapUI.getResourceManager().getSystemMarkerSheet()))
                    MindMapUI.getResourceManager().getRecentMarkerGroup()
                            .addMarker(systemMarker);
            }
        }
        IMarker userMarker = MindMapUI.getResourceManager().getUserMarkerSheet()
                .findMarker(markerId);
        if (userMarker != null) {
            IMarkerGroup group = userMarker.getParent();
            if (group != null) {
                if (group.getParent() != null && group.getParent().equals(
                        MindMapUI.getResourceManager().getUserMarkerSheet())) {

                    MindMapUI.getResourceManager().getRecentMarkerGroup()
                            .addMarker(userMarker);
                }
            }
        }
    }

    @Deprecated
    private void handleCommandStackChange(CommandStackEvent event) {
        int status = event.getStatus();
        if ((status & GEF.CS_PRE_UNDO) != 0) {
            undoing = true;
        } else if ((status & GEF.CS_POST_UNDO) != 0) {
            undoing = false;
        }
    }

}
