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
package org.xmind.ui.animation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.xmind.ui.util.UITimer;

/**
 * @author Frank Shaka
 */
public class AnimationTimer extends UITimer {

    private List<IAnimationTask> tasks;

    private Runnable afterEffects = null;

    private boolean finishOnCanceled = true;

    public AnimationTimer(int delay, int interval, int loops,
            IAnimationTask task) {
        this(delay, interval, loops, Collections.singleton(task));
    }

    public AnimationTimer(int delay, int interval, int loops,
            Collection<IAnimationTask> tasks) {
        super(delay, interval, loops, null);
        this.tasks = new ArrayList<IAnimationTask>(tasks);
        setTask(createMainTask());
    }

    private SafeRunnable createMainTask() {
        return new SafeRunnable() {
            public void run() throws Exception {
                performTasks();
            }
        };
    }

    public Runnable getAfterEffects() {
        return afterEffects;
    }

    public void setAfterEffects(Runnable afterEffects) {
        this.afterEffects = afterEffects;
    }

    private void doTask(IAnimationTask task) {
        task.setValue(task.getCurrentValue(getCurrentLoop(), getLoops()));
    }

    @Override
    protected void doJob() {
        onStart();
        super.doJob();
    }

    /**
     * @see org.xmind.framework.Timer#cancel()
     */
    @Override
    public void cancel() {
        super.cancel();
        if (isValid()) {
            for (IAnimationTask task : tasks) {
                task.cancel();
            }
            if (isFinishOnCanceled())
                onFinished();
        }
    }

    protected void onStart() {
        if (isValid()) {
            for (IAnimationTask task : tasks) {
                task.start();
            }
        }
    }

    protected void onFinished() {
        if (isValid()) {
            for (IAnimationTask task : tasks) {
                task.finish();
            }
            if (afterEffects != null) {
                SafeRunner.run(new SafeRunnable() {
                    public void run() throws Exception {
                        afterEffects.run();
                    }
                });
            }
        }
    }

    protected void performTasks() {
        for (IAnimationTask task : tasks) {
            doTask(task);
        }
    }

    public List<IAnimationTask> getTasks() {
        return tasks;
    }

    /**
     * @return the finishOnCanceled
     */
    public boolean isFinishOnCanceled() {
        return finishOnCanceled;
    }

    /**
     * @param finishOnCanceled
     *            the finishOnCanceled to set
     */
    public void setFinishOnCanceled(boolean finishOnCanceled) {
        this.finishOnCanceled = finishOnCanceled;
    }

}