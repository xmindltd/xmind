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
package org.xmind.ui.animation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutListener;
import org.eclipse.draw2d.UpdateManager;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.widgets.Control;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.service.GraphicalViewerService;
import org.xmind.gef.service.IAnimationService;
import org.xmind.gef.service.IPlaybackProvider;
import org.xmind.ui.util.UITimer;

public class AnimationService extends GraphicalViewerService implements
        IAnimationService {

    private static boolean DEBUG = false;

    private class LayoutCapturer implements LayoutListener {

        public void invalidate(IFigure container) {
            if (isRecordingInitial()) {
                hookPlayback(container);
            }
        }

        public boolean layout(IFigure container) {
            if (isAnimating()) {
                return playback(container);
            }
            return false;
        }

        public void postLayout(IFigure container) {
            if (isRecordingFinal()) {
                hookCapture(container);
            }
        }

        public void remove(IFigure child) {
        }

        public void setConstraint(IFigure child, Object constraint) {
        }

    }

    private class PlaybackTask extends SafeRunnable {

        public void run() throws Exception {
            if (getViewer().getControl().isDisposed())
                return;

            if (initialStates != null) {
                for (IFigure figure : initialStates.keySet()) {
                    figure.revalidate();
                }
            }

            if (timer == null || timer.isCanceled())
                return;

            progress = timer.getCurrentLoop() * 1.0f / (timer.getLoops() + 1);
        }

    }

    private class PlaybackTimer extends UITimer {

        private Runnable afterEffect;

        public PlaybackTimer(Runnable after) {
            super(0, DEBUG ? 300 : 10, DEBUG ? 10 : 3, task);
            this.afterEffect = after;
        }

        @Override
        protected void onFinished() {
            super.onFinished();
            timer = null;
            stop();
            runAfterEffect();
        }

        private void runAfterEffect() {
            if (afterEffect != null && !getViewer().getControl().isDisposed()) {
                SafeRunner.run(new SafeRunnable(
                        "Error running effect after animation.") { //$NON-NLS-1$
                            public void run() throws Exception {
                                afterEffect.run();
                            }
                        });
            }
        }

        @Override
        protected void onCanceled() {
            super.onCanceled();
            runAfterEffect();
            timer = null;
            stop();
        }

    }

    private static final int IDLE = 0;
    private static final int RECORDING_INITIAL = 1;
    private static final int RECORDING_FINAL = 2;
    private static final int PLAYBACK = 3;

    private static final IPlaybackProvider DEFAULT_PLAYBACK_PROVIDER = new LayoutPlaybackProvider();

    private UpdateManager updateManager;

    private int state = IDLE;

    private final LayoutCapturer layoutCapturer = new LayoutCapturer();

    private Map<IFigure, IGraphicalPart> registry = null;

    private IPlaybackProvider playbackProvider = DEFAULT_PLAYBACK_PROVIDER;

    private Set<IFigure> keyFigures = null;

    private Map<IFigure, Object> initialStates = null;

    private Map<IFigure, Object> finalStates = null;

    private Set<IFigure> toCapture = null;

    private float progress = 0;

    private ISafeRunnable task = new PlaybackTask();

    private UITimer timer = null;

    public AnimationService(IGraphicalViewer viewer) {
        super(viewer);
    }

    protected void hookControl(Control control) {
        super.hookControl(control);
        if (control instanceof FigureCanvas) {
            updateManager = ((FigureCanvas) control).getLightweightSystem()
                    .getUpdateManager();
        } else {
            updateManager = null;
        }
    }

    protected void activate() {
    }

    protected void deactivate() {
        stop();
    }

    public void registerFigure(IFigure figure, IGraphicalPart part) {
        figure.addLayoutListener(layoutCapturer);
        if (registry == null)
            registry = new HashMap<IFigure, IGraphicalPart>();
        registry.put(figure, part);
    }

    public void unregisterFigure(IFigure figure) {
        figure.removeLayoutListener(layoutCapturer);
        if (registry != null) {
            registry.remove(figure);
        }
    }

    public IGraphicalPart getRegisteredPart(IFigure figure) {
        return registry == null ? null : registry.get(figure);
    }

    public boolean isIdle() {
        return state == IDLE;
    }

    public boolean isAnimating() {
        return state == PLAYBACK;
    }

    protected boolean isRecordingInitial() {
        return state == RECORDING_INITIAL;
    }

    protected boolean isRecordingFinal() {
        return state == RECORDING_FINAL;
    }

    public IPlaybackProvider getPlaybackProvider() {
        return playbackProvider;
    }

    public void setPlaybackProvider(IPlaybackProvider playbackProvider) {
        if (playbackProvider == null)
            playbackProvider = DEFAULT_PLAYBACK_PROVIDER;
        this.playbackProvider = playbackProvider;
    }

    public void start(Runnable keyframeMaker, Runnable beforeEffect,
            Runnable afterEffect) {
        if (keyframeMaker == null || !isActive() || updateManager == null
                || getViewer().getControl().isDisposed())
            return;

        stop();

        if (!markBegin())
            return;

        try {
            keyframeMaker.run();
        } catch (Exception e) {
            stop();
            return;
        }

        if (state == IDLE || keyFigures == null || keyFigures.isEmpty()
                || getViewer().getControl().isDisposed()) {
            stop();
            return;
        }

        markEnd();

        runPlayback(beforeEffect, afterEffect);
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        state = IDLE;

        SafeRunner.run(task);

        initialStates = null;
        finalStates = null;
        keyFigures = null;
        toCapture = null;
        state = IDLE;
    }

    protected boolean markBegin() {
        if (state == IDLE) {
            state = RECORDING_INITIAL;
            initialStates = new HashMap<IFigure, Object>();
            finalStates = new HashMap<IFigure, Object>();
            keyFigures = new HashSet<IFigure>();
            toCapture = new HashSet<IFigure>();
            return true;
        }
        return false;
    }

    protected void recordInitialState(IFigure figure) {
        if (initialStates != null) {
            initialStates.put(figure, getCurrentState(figure));
        }
    }

    protected void markEnd() {
        state = RECORDING_FINAL;
        if (updateManager != null) {
            updateManager.performValidation();
        }
        recordFinalStates();
    }

    protected Object getCurrentState(IFigure figure) {
        return playbackProvider.getState(figure, getRegisteredPart(figure));
    }

    private void recordFinalStates() {
        Iterator<IFigure> figureIterator = keyFigures.iterator();
        while (figureIterator.hasNext()) {
            IFigure fig = figureIterator.next();
            if (toCapture.contains(fig)) {
                recordFinalState(fig);
            } else {
                figureIterator.remove();
            }
        }
    }

    protected void recordFinalState(IFigure figure) {
        if (finalStates != null) {
            finalStates.put(figure, getCurrentState(figure));
        }
    }

    private void runPlayback(final Runnable beforeEffect, Runnable afterEffect) {
        if (beforeEffect != null) {
            SafeRunner.run(new SafeRunnable(
                    "Error running effect before animation.") { //$NON-NLS-1$
                        public void run() throws Exception {
                            beforeEffect.run();
                        }
                    });
        }
        if (state == IDLE || getViewer().getControl().isDisposed())
            return;

        state = PLAYBACK;
        progress = 0;
        SafeRunner.run(task);
        timer = new PlaybackTimer(afterEffect);
        timer.run();
    }

    protected boolean playback(IFigure figure) {
        if (toCapture != null && toCapture.contains(figure)) {
            return doPlayback(figure);
        }
        return false;
    }

    protected boolean doPlayback(IFigure figure) {
        Map initial = (Map) getInitialState(figure);
        Map ending = (Map) getFinalState(figure);
        if (initial == null || ending == null)
            return false;
        return playbackProvider.doPlayback(figure, getRegisteredPart(figure),
                initial, ending, progress);
    }

    protected Object getInitialState(IFigure figure) {
        return initialStates == null ? null : initialStates.get(figure);
    }

    protected Object getFinalState(IFigure figure) {
        return finalStates == null ? null : finalStates.get(figure);
    }

    protected void hookPlayback(IFigure figure) {
        if (keyFigures != null) {
            if (keyFigures.add(figure)) {
                recordInitialState(figure);
            }
        }
    }

    protected void hookCapture(IFigure container) {
        if (keyFigures != null && keyFigures.contains(container)
                && toCapture != null) {
            toCapture.add(container);
        }
    }

//    public void addFigureInitial(IFigure figure) {
//        hookPlayback(figure);
//    }
//
//    public void addFigureFinal(IFigure figure) {
//        hookCapture(figure);
//        if (toCapture != null && toCapture.contains(figure))
//            recordFinalState(figure);
//    }

    public boolean isAnimating(IFigure figure) {
        if (isAnimating())
            return getInitialState(figure) != null
                    && getFinalState(figure) != null;
        if (isRecordingFinal())
            return getInitialState(figure) != null;
        return false;
    }

}