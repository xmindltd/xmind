package org.xmind.cathy.internal.jobs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.xmind.cathy.internal.CathyPlugin;
import org.xmind.cathy.internal.Log;
import org.xmind.cathy.internal.WorkbenchMessages;
import org.xmind.ui.internal.actions.OpenHomeMapAction;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.mindmap.MindMapUI;

//import org.eclipse.core.internal.resources.File;

public class StartupJob extends Job {

    private static final boolean DEBUG_CHECK_OPEN_FILE = CathyPlugin
            .getDefault().isDebugging("/debug/checkopenfile"); //$NON-NLS-1$

    private final IWorkbench workbench;

    public StartupJob(IWorkbench workbench, boolean showProgress) {
        super(WorkbenchMessages.StartupJob_jobName);
        this.workbench = workbench;
        setUser(showProgress);
        setSystem(!showProgress);
    }

    protected IStatus run(IProgressMonitor monitor) {
        monitor.beginTask(null, 3);
        doStartup(monitor);
        Display display = workbench.getDisplay();
        if (display != null && !display.isDisposed()) {
            display.asyncExec(new Runnable() {
                public void run() {
                    System.setProperty("org.xmind.cathy.app.status", //$NON-NLS-1$
                            "workbenchReady"); //$NON-NLS-1$
                }
            });
        }
        monitor.done();
        return Status.OK_STATUS;
    }

    protected void doStartup(IProgressMonitor monitor) {
        checkAndRecoverFiles(monitor);

        if (monitor.isCanceled())
            return;

        if (DEBUG_CHECK_OPEN_FILE) {
            checkAndOpenFiles(monitor);
        } else {
            //delete file paths which need to open from command line
            Log openFile = Log.get(Log.OPENING);
            if (openFile.exists())
                openFile.delete();
        }

        if (monitor.isCanceled())
            return;
        waitForInitialFilesLoaded(monitor);

        if (monitor.isCanceled())
            return;
        openStartupMap(monitor);
    }

    /**
     * Wait for a short while before initial files are loaded. On Mac OS X,
     * files opened by double clicking or 'open' command are passed to Eclipse
     * RCP via <code>SWT.OpenDocument</code> event which comes a slight later
     * than workbench startup.
     * 
     * @param monitor
     */
    protected void waitForInitialFilesLoaded(IProgressMonitor monitor) {
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            monitor.setCanceled(true);
        }
    }

    protected void openStartupMap(IProgressMonitor monitor) {
        if (!hasOpenedEditors()) {
            int action = CathyPlugin.getDefault().getPreferenceStore()
                    .getInt(CathyPlugin.STARTUP_ACTION);
            if (action == CathyPlugin.STARTUP_ACTION_HOME) {
                monitor.subTask(WorkbenchMessages.StartupJob_OpenHomeMap);
                SafeRunner.run(new SafeRunnable() {
                    public void run() throws Exception {
                        workbench.getDisplay().syncExec(new Runnable() {
                            public void run() {
                                IWorkbenchWindow window = workbench
                                        .getActiveWorkbenchWindow();
                                if (window != null) {
                                    final IWorkbenchPage page = window
                                            .getActivePage();
                                    Shell shell = window.getShell();
                                    if (page != null) {
                                        OpenHomeMapAction.openHomeMap(shell,
                                                page);
                                    }
                                }
                            }
                        });
                    }
                });
            } else if (action == CathyPlugin.STARTUP_ACTION_BLANK) {
                monitor.subTask(WorkbenchMessages.StartupJob_OpenBlankMap);
                openBlankMap();
            } else if (action == CathyPlugin.STARTUP_ACTION_LAST) {
                IPath editorStatusPath = WorkbenchPlugin.getDefault()
                        .getDataLocation().append("XMind_Editors.xml"); //$NON-NLS-1$
                //open unclosed editors in the last session.
                final File stateFile = editorStatusPath.toFile();
                if (stateFile.exists())
                    workbench.getDisplay().syncExec(new Runnable() {
                        public void run() {
                            SafeRunner.run(new SafeRunnable() {
                                public void run() throws Exception {
                                    IWorkbenchWindow window = workbench
                                            .getActiveWorkbenchWindow();
                                    if (window != null) {
                                        IWorkbenchPage page = window
                                                .getActivePage();
                                        if (page != null) {
                                            openUnclosedMapLastSession(
                                                    stateFile, page);
                                        }
                                    }
                                }
                            });
                        }
                    });
            }

        }
        monitor.worked(1);
    }

    private void openUnclosedMapLastSession(File statusFile,
            final IWorkbenchPage page)
                    throws FileNotFoundException, UnsupportedEncodingException,
                    WorkbenchException, CoreException, PartInitException {
        FileInputStream input = new FileInputStream(statusFile);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, "utf-8")); //$NON-NLS-1$
        IMemento memento = XMLMemento.createReadRoot(reader);
        IMemento childMem = memento.getChild(IWorkbenchConstants.TAG_EDITORS);
        IMemento[] childrenEditor = childMem.getChildren("editor"); //$NON-NLS-1$
        IEditorPart activeEditorPart = null;
        for (IMemento childEditor : childrenEditor) {
            String path = childEditor.getString("path"); //$NON-NLS-1$
            if (path != null) {
                IEditorInput editorInput = MME.createFileEditorInput(path);
                IEditorPart editorPart = page.openEditor(editorInput,
                        MindMapUI.MINDMAP_EDITOR_ID);
                if ("true".equals(childEditor.getString("activePart"))) { //$NON-NLS-1$ //$NON-NLS-2$
                    activeEditorPart = editorPart;
                }
            }
        }
        if (activeEditorPart != null) {
            page.activate(activeEditorPart);
        }
    }

    private boolean hasOpenedEditors() {
        final boolean[] ret = new boolean[1];
        ret[0] = false;
        workbench.getDisplay().syncExec(new Runnable() {
            public void run() {
                for (IWorkbenchWindow window : workbench
                        .getWorkbenchWindows()) {
                    IWorkbenchPage page = window.getActivePage();
                    if (page != null) {
                        if (page.getEditorReferences().length > 0) {
                            ret[0] = true;
                            return;
                        }
                    }
                }
            }
        });
        return ret[0];
    }

    /**
     * Check and recover files opened in the last workbench session. This method
     * consumes 2 ticks from the given progress monitor.
     * 
     * @param monitor
     */
    protected void checkAndRecoverFiles(IProgressMonitor monitor) {
        Job subJob = new CheckRecoverFilesJob(workbench);
        subJob.setUser(isUser());
        subJob.setSystem(isSystem());
        subJob.setProgressGroup(monitor, 1);
        subJob.schedule();
        try {
            subJob.join();
        } catch (InterruptedException e) {
        }
    }

    protected void checkAndOpenFiles(IProgressMonitor monitor) {
        Job subJob = new CheckOpenFilesJob(workbench);
        subJob.setRule(Log.get(Log.OPENING));
        subJob.setUser(isUser());
        subJob.setSystem(isSystem());
        subJob.setProgressGroup(monitor, 1);
        subJob.schedule();
        try {
            subJob.join();
        } catch (InterruptedException e) {
        }
    }

    private IEditorPart openBlankMap() {
        return openEditor(MME.createNonExistingEditorInput());
    }

    private IEditorPart openEditor(final IEditorInput editorInput) {
        final IEditorPart[] editor = new IEditorPart[1];
        workbench.getDisplay().syncExec(new Runnable() {
            public void run() {
                IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
                if (window != null) {
                    final IWorkbenchPage page = window.getActivePage();
                    if (page != null) {
                        SafeRunner.run(new SafeRunnable() {
                            public void run() throws Exception {
                                editor[0] = page.openEditor(editorInput,
                                        MindMapUI.MINDMAP_EDITOR_ID);
                            }
                        });
                    }
                }
            }
        });
        return editor[0];
    }

}
