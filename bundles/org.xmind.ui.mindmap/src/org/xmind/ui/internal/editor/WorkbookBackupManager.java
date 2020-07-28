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

import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * @deprecated
 */
@Deprecated
public class WorkbookBackupManager implements ISchedulingRule {

//    private class WorkbookBackupWorker extends Job {
//
//        private WorkbookRef ref;
//
//        private IWorkbookBackup backup = null;
//
//        public WorkbookBackupWorker(WorkbookRef ref) {
//            super("WorkbookBackupWorker-" + ref.getKey().toString()); //$NON-NLS-1$
//            this.ref = ref;
//        }
//
//        public WorkbookRef getRef() {
//            return ref;
//        }
//
//        public IWorkbookBackup getBackup() {
//            return backup;
//        }
//
//        protected IStatus run(IProgressMonitor monitor) {
//            try {
//                IWorkbookBackupFactory backupper = ref
//                        .getWorkbookBackupFactory();
//                if (backupper != null) {
//                    backup = backupper.createWorkbookBackup(monitor,
//                            backups.get(ref));
//                }
//            } catch (Throwable e) {
//                return new Status(IStatus.WARNING, MindMapUIPlugin.PLUGIN_ID,
//                        "Failed to make backup for workbook: " //$NON-NLS-1$
//                                + ref.getKey().toString(),
//                        e);
//            }
//            if (monitor.isCanceled())
//                return Status.CANCEL_STATUS;
//            return Status.OK_STATUS;
//        }
//
//    }
//
//    private static final WorkbookBackupManager instance = new WorkbookBackupManager();
//
//    private Map<WorkbookRef, IWorkbookBackup> backups = new HashMap<WorkbookRef, IWorkbookBackup>();
//
//    private Map<WorkbookRef, WorkbookBackupWorker> workers = new HashMap<WorkbookRef, WorkbookBackupWorker>();
//
//    private IJobChangeListener workerListener = new JobChangeAdapter() {
//        public void done(IJobChangeEvent event) {
//            handleWorkerDone((WorkbookBackupWorker) event.getJob(),
//                    event.getResult());
//        }
//    };
//
//    private WorkbookBackupManager() {
//    }
//
//    private WorkbookBackupWorker createWorker(WorkbookRef ref) {
//        WorkbookBackupWorker worker = new WorkbookBackupWorker(ref);
//        worker.setRule(this);
//        worker.setSystem(true);
//        worker.setPriority(Job.SHORT);
//        worker.addJobChangeListener(workerListener);
//        workers.put(ref, worker);
//        worker.schedule();
//        return worker;
//    }
//
//    private void handleWorkerDone(WorkbookBackupWorker worker, IStatus result) {
//        if (result.isOK()) {
//            IWorkbookBackup newBackup = worker.getBackup();
//            IWorkbookBackup oldBackup = backups.put(worker.getRef(), newBackup);
//            if (oldBackup != null && !oldBackup.equals(newBackup)) {
//                oldBackup.dispose();
//            }
//        }
//        workers.remove(worker.getRef());
//    }
//
//    public synchronized void addWorkbook(WorkbookRef ref) {
//        if (backups.containsKey(ref))
//            return;
//
//        IWorkbookBackup oldBackup = backups.put(ref, null);
//        if (oldBackup != null) {
//            oldBackup.dispose();
//        }
//        createWorker(ref);
//    }
//
//    public synchronized void removeWorkbook(WorkbookRef ref) {
//        if (!backups.containsKey(ref))
//            return;
//
//        IWorkbookBackup backup = backups.remove(ref);
//        if (backup != null) {
//            backup.dispose();
//        }
//        WorkbookBackupWorker worker = workers.remove(ref);
//        if (worker != null) {
//            worker.cancel();
//        }
//    }
//
//    public synchronized IWorkbookBackup ensureBackedUp(WorkbookRef ref,
//            IProgressMonitor monitor) {
//        if (!workers.containsKey(ref)) {
//            createWorker(ref);
//        }
//        try {
//            while (workers.containsKey(ref)) {
//                if (monitor.isCanceled())
//                    break;
//                Thread.sleep(1);
//            }
//        } catch (InterruptedException e) {
//        }
//        return backups.get(ref);
//    }
//
//    public static WorkbookBackupManager getInstance() {
//        return instance;
//    }

    public boolean contains(ISchedulingRule rule) {
        return rule == this;
    }

    public boolean isConflicting(ISchedulingRule rule) {
        return rule == this;
    }

}
