package org.xmind.cathy.internal.jobs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.xmind.cathy.internal.WorkbenchMessages;
import org.xmind.ui.mindmap.MindMapUI;

public abstract class AbstractCheckFilesJob extends Job {

    private final IWorkbench workbench;

    private List<IEditorInput> editorsToOpen;

    public AbstractCheckFilesJob(IWorkbench workbench, String jobName) {
        super(jobName);
        this.workbench = workbench;
        addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void aboutToRun(IJobChangeEvent event) {
                prepare();
            }

            /*
             * (non-Javadoc)
             * 
             * @see
             * org.eclipse.core.runtime.jobs.JobChangeAdapter#done(org.eclipse
             * .core.runtime.jobs.IJobChangeEvent)
             */
            @Override
            public void done(IJobChangeEvent event) {
                finish();
            }
        });
    }

    protected void prepare() {
        editorsToOpen = null;
    }

    protected void finish() {
        editorsToOpen = null;
    }

    public IWorkbench getWorkbench() {
        return workbench;
    }

    protected void addEditorToOpen(IEditorInput input) {
        if (editorsToOpen == null)
            editorsToOpen = new ArrayList<IEditorInput>();
        editorsToOpen.add(input);
    }

    protected void openEditors(IProgressMonitor monitor, String taskName,
            int ticks, boolean activate) {
        if (editorsToOpen != null && !editorsToOpen.isEmpty()) {
            monitor = new SubProgressMonitor(monitor, ticks);
            monitor.beginTask(taskName, editorsToOpen.size());
            openEditors(monitor, editorsToOpen, activate);
            monitor.done();
        } else {
            monitor.worked(ticks);
        }
    }

    protected void openEditors(IProgressMonitor monitor,
            List<IEditorInput> editorInputs, boolean activate) {
        for (final IEditorInput input : editorInputs) {
            monitor.subTask(input.getName());
            IEditorPart editor = openEditor(input, activate);
            if (editor != null)
                activate = false;
            monitor.worked(1);
        }
    }

    protected IEditorPart openEditor(final IEditorInput input,
            final boolean activate) {
        if (input == null)
            return null;

        Display display = workbench.getDisplay();
        if (display == null)
            return null;

        final IEditorPart[] result = new IEditorPart[1];
        display.syncExec(new Runnable() {
            public void run() {
                IWorkbenchWindow window = getPrimaryWindow();
                if (window == null)
                    return;
                final IWorkbenchPage page = window.getActivePage();
                if (page != null) {
                    SafeRunner.run(new SafeRunnable(
                            NLS.bind(
                                    WorkbenchMessages.CheckOpenFilesJob_FailsToOpen_message,
                                    input.getName())) {
                        public void run() throws Exception {
                            result[0] = page.openEditor(input,
                                    MindMapUI.MINDMAP_EDITOR_ID, activate);
                        }
                    });
                }
            }

        });
        return result[0];
    }

    private IWorkbenchWindow getPrimaryWindow() {
        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        if (window == null) {
            IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
            if (windows != null && windows.length > 0) {
                window = windows[0];
            }
        }
        return window;
    }
}
