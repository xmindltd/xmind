/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.io;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.widgets.Display;
import org.xmind.ui.internal.ToolkitPlugin;

public class UIJobChangeListener implements IJobChangeListener {

    private Display display;

    private boolean syncExecute;

    public UIJobChangeListener() {
        this(false);
    }

    public UIJobChangeListener(boolean syncExecute) {
        this(Display.getCurrent(), syncExecute);
    }

    public UIJobChangeListener(Display display) {
        this(display, false);
    }

    public UIJobChangeListener(Display display, boolean syncExecute) {
        this.display = display;
        this.syncExecute = syncExecute;
    }

    protected boolean isSyncExecute() {
        return syncExecute;
    }

    protected Display getDisplay() {
        return display;
    }

    private void runInUIThread(final Runnable runnable) {
        if (display != null && !display.isDisposed()) {
            Runnable runnable2 = new Runnable() {
                public void run() {
                    SafeRunner.run(new SafeRunnable() {
                        public void run() throws Exception {
                            runnable.run();
                        }
                    });
                }
            };
            if (isSyncExecute()) {
                display.syncExec(runnable2);
            } else {
                display.asyncExec(runnable2);
            }
        } else {
            try {
                runnable.run();
            } catch (Throwable e) {
                ToolkitPlugin.getDefault().getLog().log(
                        new Status(IStatus.ERROR, ToolkitPlugin.PLUGIN_ID,
                                "Error occurred when running in UI thread.", //$NON-NLS-1$
                                e));
            }
        }
    }

    public final void aboutToRun(final IJobChangeEvent event) {
        runInUIThread(new Runnable() {
            public void run() {
                doAboutToRun(event);
            }
        });
    }

    public final void awake(final IJobChangeEvent event) {
        runInUIThread(new Runnable() {
            public void run() {
                doAwake(event);
            }
        });
    }

    public final void done(final IJobChangeEvent event) {
        runInUIThread(new Runnable() {
            public void run() {
                doDone(event);
            }
        });
    }

    public final void running(final IJobChangeEvent event) {
        runInUIThread(new Runnable() {
            public void run() {
                doRunning(event);
            }
        });
    }

    public final void scheduled(final IJobChangeEvent event) {
        runInUIThread(new Runnable() {
            public void run() {
                doScheduled(event);
            }
        });
    }

    public final void sleeping(final IJobChangeEvent event) {
        runInUIThread(new Runnable() {
            public void run() {
                doSleeping(event);
            }
        });
    }

    /**
     * Called safely in UI thread
     * 
     * @param event
     */
    protected void doAboutToRun(IJobChangeEvent event) {
        //subclasses may override
    }

    /**
     * Called safely in UI thread
     * 
     * @param event
     */
    protected void doAwake(IJobChangeEvent event) {
        //subclasses may override
    }

    /**
     * Called safely in UI thread
     * 
     * @param event
     */
    protected void doDone(IJobChangeEvent event) {
        //subclasses may override
    }

    /**
     * Called safely in UI thread
     * 
     * @param event
     */
    protected void doRunning(IJobChangeEvent event) {
        //subclasses may override
    }

    /**
     * Called safely in UI thread
     * 
     * @param event
     */
    protected void doScheduled(IJobChangeEvent event) {
        //subclasses may override
    }

    /**
     * Called safely in UI thread
     * 
     * @param event
     */
    protected void doSleeping(IJobChangeEvent event) {
        //subclasses may override
    }

}