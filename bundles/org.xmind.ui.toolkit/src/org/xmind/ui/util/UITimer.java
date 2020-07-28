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
package org.xmind.ui.util;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.swt.widgets.Display;

/**
 * This class should only be used in UI thread.
 * 
 * @author Frank Shaka
 */
public class UITimer extends Cancelable {

    private class InnerTask implements Runnable {
        public void run() {
            if (isCanceled())
                return;
            if (!isValid()) {
                cancel();
                return;
            }
            if (loops > 0) {
                if (currentLoop >= loops) {
                    currentLoop = 0;
                    innerTask = null;
                    onFinished();
                    return;
                } else {
                    currentLoop++;
                }
            }
            long start = System.currentTimeMillis();
            SafeRunner.run(task);
            long end = System.currentTimeMillis();

            if (isCanceled()) {
                innerTask = null;
                postCanceled();
            } else {
                Display display = Display.getCurrent();
                if (display == null || display.isDisposed()) {
                    postCanceled();
                } else if (innerTask != null) {
                    int taskDuration = (int) (end - start);
                    if (taskDuration >= interval) {
                        display.asyncExec(innerTask);
                    } else {
                        final int nextInterval = interval - taskDuration;
                        display.timerExec(nextInterval, innerTask);
                    }
                }
            }
        }
    }

    private ISafeRunnable task;

    private int delay;

    private int interval;

    private int loops = -1;

    private int currentLoop = 0;

    private Runnable innerTask = null;

    private ITimerValidator validator = null;

    /**
     * @param task
     * @param delay
     * @param interval
     */
    public UITimer(int delay, int interval, ISafeRunnable task) {
        super();
        this.task = task;
        this.delay = delay;
        this.interval = interval;
    }

    public UITimer(int delay, int interval, int loops, ISafeRunnable task) {
        this(delay, interval, task);
        this.loops = loops < 0 ? loops : Math.max(1, loops);
    }

    public void start() {
        reinitialize();
        run();
    }

    @Override
    protected void doJob() {
        if (!isValid()) {
            cancel();
            return;
        }
        innerTask = new InnerTask();
        Display display = Display.getCurrent();
        if (display != null && !display.isDisposed())
            display.timerExec(delay, innerTask);
    }

    /**
     * 
     */
    public void cancel() {
        super.cancel();
        innerTask = null;
        onCanceled();
    }

    protected void onFinished() {
    }

    protected void onCanceled() {
    }

    protected void postCanceled() {
    }

    /**
     * @return the delay
     */
    public int getDelay() {
        return delay;
    }

    /**
     * @param delay
     *            the delay to set
     */
    public void setDelay(int delay) {
        this.delay = delay;
    }

    /**
     * @return the interval
     */
    public int getInterval() {
        return interval;
    }

    /**
     * @param interval
     *            the interval to set
     */
    public void setInterval(int interval) {
        this.interval = interval;
    }

    /**
     * @return the task
     */
    public ISafeRunnable getTask() {
        return task;
    }

    /**
     * @param task
     *            the task to set
     */
    public void setTask(ISafeRunnable task) {
        this.task = task;
    }

    /**
     * @return the times
     */
    public int getLoops() {
        return loops;
    }

    /**
     * @param times
     *            the times to set
     */
    public void setLoops(int times) {
        this.loops = times;
    }

    /**
     * @return the currentTime
     */
    public int getCurrentLoop() {
        return currentLoop;
    }

    /**
     * @param validator
     *            the validator to set
     */
    public void setValidator(ITimerValidator validator) {
        this.validator = validator;
    }

    /**
     * @return the validator
     */
    public ITimerValidator getValidator() {
        return validator;
    }

    protected boolean isValid() {
        return validator == null || validator.isRunnable();
    }

}