package org.xmind.ui.internal.biggerplate.jobs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.jobs.Job;

public abstract class CancelableJob extends Job {

    public CancelableJob(String name) {
        super(name);
    }

    private boolean isCanceled;

    private List<IJobClosedListener> listeners = new ArrayList<IJobClosedListener>();

    @Override
    protected void canceling() {
        isCanceled = true;
        fireJobClosed();
        super.canceling();
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public void addJobClosedListener(IJobClosedListener listener) {
        listeners.add(listener);
    }

    public void removeJobClosedListener(IJobClosedListener listener) {
        listeners.remove(listener);
    }

    private void fireJobClosed() {
        IJobClosedListener[] arrListeners = listeners
                .toArray(new IJobClosedListener[0]);
        for (IJobClosedListener listener : arrListeners) {
            listener.jobClosed();
        }
    }

}
