package org.xmind.ui.util;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.osgi.util.NLS;
import org.xmind.ui.internal.ToolkitPlugin;

public class JobPool {

    private static boolean DEBUG = false;

    private static final int DEFAULT_RETRY_NUM = 0;

    private static final int DEFAULT_RUNNING_MAX_NUM = 5;

    private static class JobTask {

        final Job job;

        final Runnable onFinish;

        public JobTask(Job job, Runnable onFinish) {
            this.job = job;
            this.onFinish = onFinish;
        }
    }

    private Queue<JobTask> waitingJobs = new ConcurrentLinkedQueue<JobTask>();

    private int retryNum = DEFAULT_RETRY_NUM;

    private int runningMaxNum = DEFAULT_RUNNING_MAX_NUM;

    private int runningJobCount = 0;

    public JobPool() {
    }

    public int getRetryNum() {
        return retryNum;
    }

    public void setRetryNum(int retryNum) {
        this.retryNum = retryNum;
    }

    public int getRunningMaxNum() {
        return runningMaxNum;
    }

    public void setRunningMaxNum(int runningMaxNum) {
        this.runningMaxNum = runningMaxNum;
    }

    public void scheduleJob(final Job job, final Runnable onFinish) {
        waitingJobs.offer(new JobTask(job, onFinish));
        scheduleNext();
    }

    public void cancelRemaining() {
        waitingJobs.clear();
    }

    private synchronized void scheduleNext() {
        if (runningJobCount < runningMaxNum) {
            JobTask task = waitingJobs.poll();
            if (task != null) {
                try {
                    startJob(task.job, task.onFinish);
                } catch (Throwable e) {
                    log(e, "Error occurred while job running..."); //$NON-NLS-1$
                }
                runningJobCount++;
            }
        }
    }

    private void startJob(final Job job, final Runnable onFinish) {
        final int[] retryCount = new int[] { 0 };
        job.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(IJobChangeEvent event) {
                super.done(event);
                int status = event.getJob().getResult().getSeverity();
                if (status != IStatus.OK && status != IStatus.CANCEL) {
                    if (++retryCount[0] < retryNum) {
                        job.schedule();
                        if (DEBUG)
                            System.out.println("Retry failed [try=" //$NON-NLS-1$
                                    + (retryCount[0] + 1) + "]."); //$NON-NLS-1$
                        return;
                    } else {
                        if (DEBUG)
                            System.out.println(NLS.bind(
                                    "Job failed after {0} retries.", retryNum)); //$NON-NLS-1$
                    }
                } else {
                    if (DEBUG) {
                        if (status == IStatus.OK)
                            System.out.println("Job succeeded."); //$NON-NLS-1$
                        else
                            System.out.println("Job canceled."); //$NON-NLS-1$
                    }
                }

                try {
                    if (onFinish != null) {
                        try {
                            onFinish.run();
                        } catch (Throwable e) {
                            log(e, "Error occurred while calling back on job finished."); //$NON-NLS-1$
                        }
                    }
                } finally {
                    runningJobCount--;
                    scheduleNext();
                }
            }
        });
        job.schedule();
        if (DEBUG)
            System.out.println("New job start [try=" + (retryCount[0] + 1) //$NON-NLS-1$
                    + "]."); //$NON-NLS-1$
    }

    public static void log(Throwable exception, String message) {
        ToolkitPlugin.getDefault().getLog()
                .log(new Status(
                        exception == null ? IStatus.INFO : IStatus.ERROR,
                        ToolkitPlugin.PLUGIN_ID, message, exception));
    }

}
