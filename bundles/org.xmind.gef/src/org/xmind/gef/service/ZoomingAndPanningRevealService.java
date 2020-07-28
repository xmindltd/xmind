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
package org.xmind.gef.service;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.part.IGraphicalPart;

public abstract class ZoomingAndPanningRevealService extends BaseRevealService {

    private class RevealJob implements Runnable {

        private Display display;

        private List<IGraphicalPart> toReveal;

        private long startTime = -1;

        private boolean canceled = false;

        private int elapsedSteps = -1;

        public RevealJob(Display display, List<IGraphicalPart> toReveal) {
            this.display = display;
            this.toReveal = toReveal;
        }

        public void cancel() {
            boolean oldCanceled = this.canceled;
            setCanceled();
            if (!oldCanceled) {
                revealingCanceled(new RevealEvent(
                        ZoomingAndPanningRevealService.this, toReveal));
            }
        }

        private void setCanceled() {
            canceled = true;
        }

        public void run() {
            if (canceled)
                return;

            Control control = getViewer().getControl();
            if (control == null || control.isDisposed()) {
                setCanceled();
                return;
            }

            if (!isAnimationEnabled()) {
                finish();
                return;
            }

            long currentTime = System.currentTimeMillis();
            if (startTime < 0)
                startTime = currentTime;

            int elapsedTime = (int) (currentTime - startTime);
            int remainingTime = getDuration() - elapsedTime;
            if (remainingTime <= 0) {
                finish();
                return;
            }

            double intervals = INTERVALS;
            if (elapsedSteps < 0) {
                elapsedSteps = 0;
                intervals += 10;
            } else {
                intervals += (((double) (currentTime - startTime
                        - INTERVALS * elapsedSteps)) / elapsedSteps);
            }

            int remainingSteps = (int) ((remainingTime + intervals - 1)
                    / intervals);
            if (remainingSteps <= 0) {
                finish();
                return;
            }

            Rectangle revealBounds = getRevealBounds(toReveal);
            if (revealBounds == null) {
                cancel();
                return;
            }

            doStep(toReveal, revealBounds, remainingSteps);

            display.timerExec(INTERVALS, this);
            elapsedSteps++;
        }

        public void finish() {
            setCanceled();
            revealJobFinished(toReveal);
        }

    }

    private static final int INTERVALS = 20;

    private int duration = 200;

    private int delay = 100;

    private RevealJob job = null;

    private double cachedScale = -1;

    private boolean centered = false;

    private int spacing = 20;

    private boolean zoomed = false;

    private boolean animationEnabled = true;

    private boolean shouldRevealOnIntersection = true;

    protected ZoomingAndPanningRevealService(IGraphicalViewer viewer) {
        super(viewer);
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public void setCentered(boolean centered) {
        this.centered = centered;
    }

    public boolean isCentered() {
        return this.centered;
    }

    public void setSpacing(int spacing) {
        this.spacing = spacing;
    }

    public int getSpacing() {
        return this.spacing;
    }

    /**
     * @param zoomed
     *            the zoomed to set
     */
    public void setZoomed(boolean zoomed) {
        this.zoomed = zoomed;
    }

    public void setShouldRevealOnIntersection(boolean should) {
        this.shouldRevealOnIntersection = should;
    }

    public boolean isShouldRevealOnIntersection() {
        return shouldRevealOnIntersection;
    }

    /**
     * @return the zoomed
     */
    public boolean isZoomed() {
        return this.zoomed;
    }

    public void setAnimationEnabled(boolean animationEnabled) {
        this.animationEnabled = animationEnabled;
    }

    protected boolean isAnimationEnabled() {
        return animationEnabled;
    }

    protected void activate() {
    }

    protected void deactivate() {
    }

    public void reveal(ISelection selection) {
        if (!isActive())
            return;

        startReveal(selection);
    }

    protected void startReveal(ISelection selection) {
        if (job != null) {
            job.cancel();
            job = null;
        }
        cachedScale = -1;

        List<IGraphicalPart> toReveal = collectPartsToReveal(selection);
        if (toReveal != null && shouldReveal(toReveal)) {
            Display display = Display.getCurrent();
            job = new RevealJob(display, toReveal);
            display.timerExec(delay, job);
            revealingStarted(new RevealEvent(this, toReveal));
        }
    }

    protected boolean shouldReveal(List<IGraphicalPart> toReveal) {
        return !toReveal.isEmpty();
    }

    protected double calcTargetScale(List<IGraphicalPart> toReveal,
            Rectangle revealBounds) {
        if (!isZoomed())
            return -1;

        if (cachedScale > 0)
            return cachedScale;

        Rectangle clientArea = getViewer().getClientArea();
        int width = revealBounds.width;
        int height = revealBounds.height;

        double scale = 2.3d;

        double w = width * scale;
        double h = height * scale;

        double minWidth = clientArea.width * 0.08d;
        double minHeight = clientArea.height * 0.08d;

        if (w < minWidth || h < minHeight) {
            double s1 = w < minWidth ? minWidth / width : scale;
            double s2 = h < minHeight ? minHeight / height : scale;
            scale = Math.max(s1, s2);
            w = width * scale;
            h = height * scale;
        }

        double maxWidth = clientArea.width * 0.6d;
        double maxHeight = clientArea.height * 0.6d;
        if (w > maxWidth || h > maxHeight) {
            double s1 = w > maxWidth ? maxWidth / width : scale;
            double s2 = h > maxHeight ? maxHeight / height : scale;
            scale = Math.min(s1, s2);
        }

        cachedScale = scale;
        return scale;
    }

    protected PrecisionPoint calcTargetCenter(List<IGraphicalPart> toReveal,
            Rectangle revealBounds, double targetScale) {
        if (isCentered()) {
            return new PrecisionPoint(revealBounds.getCenter());
        } else {
            return calcLeastTargetCenter(toReveal, revealBounds, targetScale);
        }
    }

    /**
     * @param toReveal
     * @param revealBounds
     * @param targetScale
     * @return
     */
    protected PrecisionPoint calcLeastTargetCenter(
            List<IGraphicalPart> toReveal, Rectangle revealBounds,
            double targetScale) {

        Rectangle clientArea = getViewerClientArea();
        revealBounds.expand(getSpacing(), getSpacing());
        if (shouldReveal(revealBounds, clientArea)) {
            int dx = 0;
            int dy = 0;
            int margin = 50;
            int offsetH = clientArea.width >> 1;
            int offsetV = clientArea.height >> 1;

            if (revealBounds.x < clientArea.x) {
                dx = revealBounds.x + offsetH - margin - getSpacing();
            } else if (revealBounds.right() > clientArea.right()) {
                dx = revealBounds.right() - offsetH + margin + getSpacing();
            }

            if (revealBounds.y < clientArea.y) {
                dy = revealBounds.y + offsetV - margin - getSpacing();
            } else if (revealBounds.bottom() > clientArea.bottom()) {
                dy = revealBounds.bottom() - offsetV + margin + getSpacing();
            }

            dx = (int) (dx == 0 ? getViewerCenterPoint(getViewerScale()).x
                    : dx);
            dy = (int) (dy == 0 ? getViewerCenterPoint(getViewerScale()).y
                    : dy);

            return getViewerCenterPoint(getViewerScale()).translate(dx, dy);
        }
        return null;
    }

    protected boolean shouldReveal(Rectangle revealBounds,
            Rectangle clientArea) {
        if (isShouldRevealOnIntersection()) {
            return !clientArea.contains(revealBounds)
                    && !revealBounds.contains(clientArea);
        }
        return clientArea.bottom() < revealBounds.bottom()
                || clientArea.getTop().y > revealBounds.y
                || clientArea.right() < revealBounds.right()
                || clientArea.getLeft().x > revealBounds.x;
    }

    protected Rectangle getViewerClientArea() {
        Rectangle clientArea = getViewer().getClientArea();
        return getViewer().getZoomManager().getAntiScaled(clientArea);
    }

    protected Rectangle getRevealBounds(List<IGraphicalPart> parts) {
        Rectangle r = null;
        for (IGraphicalPart p : parts) {
            r = Geometry.union(r, getRevealBounds(p));
        }
        return r;
    }

    protected Rectangle getRevealBounds(IGraphicalPart p) {
        return p.getFigure().getBounds();
    }

    protected double getViewerScale() {
        return getViewer().getZoomManager().getScale();
    }

    protected PrecisionPoint getViewerCenterPoint(double scale) {
        return new PrecisionPoint(getViewer().getCenterPoint())
                .scale(1 / scale);
    }

    protected List<IGraphicalPart> collectPartsToReveal(ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) selection;
            List<IGraphicalPart> list = new ArrayList<IGraphicalPart>(
                    ss.size());
            for (Object o : ss.toList()) {
                IGraphicalPart p = getViewer().findGraphicalPart(o);
                if (p != null && !exclude(p)) {
                    list.add(p);
                }
            }
            return list;
        }
        return null;
    }

    protected boolean exclude(IGraphicalPart part) {
        return false;
    }

    protected void finishCurrentJob() {
        if (job != null) {
            job.finish();
            job = null;
        }
    }

    protected void cancelCurrentJob() {
        if (job != null) {
            job.cancel();
            job = null;
        }
    }

    protected void revealJobFinished(List<IGraphicalPart> toReveal) {

        Rectangle revealBounds = getRevealBounds(toReveal);
        if (revealBounds != null) {
            double targetScale = calcTargetScale(toReveal, revealBounds);
            PrecisionPoint targetCenter = calcTargetCenter(toReveal,
                    revealBounds, targetScale);
            if (targetScale > 0) {
                getViewer().getZoomManager().setScale(targetScale);
            }
            if (targetCenter != null) {
                getViewer().center(targetCenter.getScaled(getViewerScale())
                        .toRoundedDraw2DPoint());
            }
        }
        revealingFinished(new RevealEvent(this, toReveal));
    }

    protected void doStep(List<IGraphicalPart> toReveal, Rectangle revealBounds,
            int remainingSteps) {
        double scale = getViewerScale();
        PrecisionPoint center = getViewerCenterPoint(scale);
        double targetScale = calcTargetScale(toReveal, revealBounds);
        PrecisionPoint targetCenter = calcTargetCenter(toReveal, revealBounds,
                targetScale);

        if (targetScale > 0) {
            double remainingScale = targetScale - scale;
            double stepScale = remainingScale / remainingSteps;
            scale += stepScale;
            getViewer().getZoomManager().setScale(scale);
        }
        if (targetCenter != null) {
            double horizontalOffset = targetCenter.x - center.x;
            double verticalOffset = targetCenter.y - center.y;
            double stepX = horizontalOffset / remainingSteps;
            double stepY = verticalOffset / remainingSteps;
            center.x += stepX;
            center.y += stepY;
            getViewer().center(targetCenter.getScaled(getViewerScale())
                    .toRoundedDraw2DPoint());
        }
    }
}
