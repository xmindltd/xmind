package org.xmind.ui.internal.editor;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.Logger;

/**
 * 
 * @author Ren Siu
 * @since 3.6.50
 */
public class BackgroundSaveWorkbook {

    private static final BackgroundSaveWorkbook INSTANCE = new BackgroundSaveWorkbook();

    private static boolean DEBUGGING = MindMapUIPlugin
            .isDebugging("/debug/autosave"); //$NON-NLS-1$

    private static class DaemonJob extends Job {

        private int intervals;

        /**
         * @param name
         */
        public DaemonJob(int intervals) {
            super("Background Save Workbooks Daemon"); //$NON-NLS-1$
            setSystem(true);
            setPriority(LONG);
            this.intervals = intervals;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.
         * IProgressMonitor)
         */
        @Override
        protected IStatus run(IProgressMonitor monitor) {
            monitor.beginTask(null, 1);
            try {
                do {
                    IStatus slept = sleep(monitor);
                    if (slept != null && !slept.isOK())
                        return slept;

                    if (DEBUGGING)
                        System.out.println("AutoSave starts now..."); //$NON-NLS-1$

                    IWorkbenchWindow[] windows = PlatformUI.getWorkbench()
                            .getWorkbenchWindows();
                    for (IWorkbenchWindow ww : windows) {
                        IWorkbenchPage[] pages = ww.getPages();
                        for (IWorkbenchPage wp : pages) {
                            IEditorReference[] ers = wp.getEditorReferences();
                            for (IEditorReference er : ers) {
                                IEditorInput editorInput = er.getEditorInput();
                                if (editorInput == null)
                                    continue;
                                IWorkbookRef workbookRef = editorInput
                                        .getAdapter(IWorkbookRef.class);
                                save(monitor, workbookRef);
                                if (monitor.isCanceled())
                                    return Status.CANCEL_STATUS;
                            }
                        }
                    }

                    if (DEBUGGING)
                        System.out.println("AutoSave finishes."); //$NON-NLS-1$

                } while (!monitor.isCanceled());
                if (monitor.isCanceled())
                    return Status.CANCEL_STATUS;
                return Status.OK_STATUS;
            } catch (Throwable e) {
                if (e instanceof InterruptedException)
                    return Status.CANCEL_STATUS;

                if (DEBUGGING) {
                    System.err.println("AutoSave error:"); //$NON-NLS-1$
                    e.printStackTrace();
                }

                String msg = "Background workbook saver daemon ended with unknown error"; //$NON-NLS-1$
                Logger.log(e, msg);
                return new Status(IStatus.WARNING, MindMapUI.PLUGIN_ID,
                        IStatus.ERROR, msg, e);
            }
        }

        private IStatus sleep(IProgressMonitor monitor) {
            int total = intervals;
            try {
                if (DEBUGGING && total > 5000) {
                    Thread.sleep(total - 5000);
                    System.out.println("AutoSave will start in 5 seconds..."); //$NON-NLS-1$
                    Thread.sleep(3000);
                    System.out.println("AutoSave will start in 2 seconds..."); //$NON-NLS-1$
                    Thread.sleep(2000);
                } else {
                    if (DEBUGGING)
                        System.out.println("AutoSave will start in " //$NON-NLS-1$
                                + (total / 1000) + " seconds..."); //$NON-NLS-1$
                    Thread.sleep(total);
                }
            } catch (InterruptedException e) {
                return Status.CANCEL_STATUS;
            }
            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;
            return Status.OK_STATUS;
        }

        private void save(IProgressMonitor monitor, IWorkbookRef workbookRef)
                throws InterruptedException, InvocationTargetException {
            if (workbookRef.canSave() && workbookRef.isDirty()) {
                workbookRef.save(monitor);
            }
        }

        protected void canceling() {
            super.canceling();
            Thread t = getThread();
            if (t != null)
                t.interrupt();
        }

    }

    private DaemonJob daemon = null;

    public synchronized void reset(int intervals, boolean enabled) {
        stopAll();
        if (enabled) {
            daemon = new DaemonJob(intervals);
            daemon.schedule();
        }
    }

    public synchronized boolean isRunning() {
        return daemon != null;
    }

    /**
     * 
     */
    public synchronized void stopAll() {
        if (daemon != null) {
            Thread thread = daemon.getThread();
            daemon.cancel();
            if (thread != null) {
                thread.interrupt();
            }
            daemon = null;
        }
    }

    public static BackgroundSaveWorkbook getInstance() {
        return INSTANCE;
    }

}
