/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.xmind.gef.draw2d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.DeferredUpdateManager;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GraphicsSource;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.UpdateManager;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;

/**
 * An UpdateManager that asynchronously updates the affected figures.
 */
public class DeferredUpdateManager2 extends UpdateManager {

    /**
     * Calls {@link DeferredUpdateManager#performUpdate()}.
     */
    protected class UpdateRequest implements Runnable {

        public UpdateRequest() {
            super();
        }

        /**
         * Calls {@link DeferredUpdateManager#performUpdate()}.
         */
        public void run() {
            performUpdate();
        }
    }

    private Rectangle damage;
    private Map dirtyRegions = new HashMap();

    private GraphicsSource graphicsSource;
    private List invalidFigures = new ArrayList();
    private IFigure root;
    private boolean updateQueued;

    private boolean updating;
    private boolean validating;
    private RunnableChain afterUpdate;

    private List<Runnable> runnables = new ArrayList<Runnable>();

    private static class RunnableChain {
        RunnableChain next;
        Runnable run;

        RunnableChain(Runnable run, RunnableChain next) {
            this.run = run;
            this.next = next;
        }

        void run() {
            if (next != null)
                next.run();
            run.run();
        }
    }

    /**
     * Empty constructor.
     */
    public DeferredUpdateManager2() {
    }

    /**
     * Constructs a new DererredUpdateManager with the given GraphicsSource.
     * 
     * @param gs
     *            the graphics source
     */
    public DeferredUpdateManager2(GraphicsSource gs) {
        setGraphicsSource(gs);
    }

    /**
     * Adds a dirty region (defined by the rectangle <i>x, y, w, h</i>) to the
     * update queue. If the figure isn't visible or either the width or height
     * are 0, the method returns without queueing the dirty region.
     * 
     * @param figure
     *            the figure that contains the dirty region
     * @param x
     *            the x coordinate of the dirty region
     * @param y
     *            the y coordinate of the dirty region
     * @param w
     *            the width of the dirty region
     * @param h
     *            the height of the dirty region
     */
    public synchronized void addDirtyRegion(IFigure figure, int x, int y, int w,
            int h) {
        if (w == 0 || h == 0 || !figure.isShowing())
            return;

        Rectangle rect = (Rectangle) dirtyRegions.get(figure);
        if (rect == null) {
            rect = new Rectangle(x, y, w, h);
            dirtyRegions.put(figure, rect);
        } else
            rect.union(x, y, w, h);

        queueWork();
    }

    /**
     * Adds the given figure to the update queue. Invalid figures will be
     * validated before the damaged regions are repainted.
     * 
     * @param f
     *            the invalid figure
     */
    public synchronized void addInvalidFigure(IFigure f) {
        if (invalidFigures.contains(f))
            return;
        queueWork();
        invalidFigures.add(f);
    }

    /**
     * Returns a Graphics object for the given region.
     * 
     * @param region
     *            the region to be repainted
     * @return the Graphics object
     */
    protected Graphics getGraphics(Rectangle region) {
        if (graphicsSource == null)
            return null;
        return graphicsSource.getGraphics(region);
    }

    /**
     * @since 3.10
     */
    protected void paint(GC gc) {
        if (!validating) {
            SWTGraphics graphics = new SWTGraphics(gc);
            if (!updating) {
                /**
                 * If a paint occurs not as part of an update, we should notify
                 * that the region is being painted. Otherwise, notification
                 * already occurs in repairDamage().
                 */
                Rectangle rect = graphics.getClip(new Rectangle());
                HashMap map = new HashMap();
                map.put(root, rect);
                firePainting(rect, map);
            }
            performValidation();
            root.paint(graphics);
            graphics.dispose();
        } else {
            /*
             * If figures are being validated then we can simply add a dirty
             * region here and update will repaint this region with other dirty
             * regions when it gets to painting. We can't paint if we're not
             * sure that all figures are valid.
             */
            addDirtyRegion(root, new Rectangle(gc.getClipping()));
        }
    }

    /**
     * Performs the update. Validates the invalid figures and then repaints the
     * dirty regions.
     * 
     * @see #validateFigures()
     * @see #repairDamage()
     */
    public synchronized void performUpdate() {
        if (isDisposed() || updating)
            return;
        updating = true;
        try {
            performValidation();
            updateQueued = false;
            repairDamage();
            if (afterUpdate != null) {
                RunnableChain chain = afterUpdate;
                afterUpdate = null;
                chain.run(); // chain may queue additional Runnable.
                if (afterUpdate != null)
                    queueWork();
            }
        } finally {
            updating = false;
        }
    }

    /**
     * @see UpdateManager#performValidation()
     */
    public synchronized void performValidation() {
        while (!invalidFigures.isEmpty() && !validating) {
            doPerformValidation();
            executeRunables();
        }
    }

    protected synchronized void doPerformValidation() {
        if (invalidFigures.isEmpty() || validating)
            return;
        try {
            IFigure fig;
            validating = true;
            fireValidating();
            for (int i = 0; i < invalidFigures.size(); i++) {
                fig = (IFigure) invalidFigures.get(i);
                invalidFigures.set(i, null);
                fig.validate();
            }
        } finally {
            invalidFigures.clear();
            validating = false;
        }
    }

    /**
     * Adds the given exposed region to the update queue and then performs the
     * update.
     * 
     * @param exposed
     *            the exposed region
     */
    public synchronized void performUpdate(Rectangle exposed) {
        addDirtyRegion(root, exposed);
        performUpdate();
    }

    /**
     * Posts an {@link UpdateRequest} using {@link Display#asyncExec(Runnable)}.
     * If work has already been queued, a new request is not needed.
     */
    protected void queueWork() {
        if (!updateQueued) {
            sendUpdateRequest();
            updateQueued = true;
        }
    }

    /**
     * Fires the <code>UpdateRequest</code> to the current display
     * asynchronously.
     * 
     * @since 3.2
     */
    protected void sendUpdateRequest() {
        Display display = Display.getCurrent();
        if (display == null) {
            throw new SWTException(SWT.ERROR_THREAD_INVALID_ACCESS);
        }
        display.asyncExec(new UpdateRequest());
    }

    /**
     * Releases the graphics object, which causes the GraphicsSource to flush.
     * 
     * @param graphics
     *            the graphics object
     */
    protected void releaseGraphics(Graphics graphics) {
        graphics.dispose();
        graphicsSource.flushGraphics(damage);
    }

    /**
     * Repaints the dirty regions on the update queue and calls
     * {@link UpdateManager#firePainting(Rectangle, Map)}, unless there are no
     * dirty regions.
     */
    protected void repairDamage() {
        Iterator keys = dirtyRegions.keySet().iterator();
        Rectangle contribution;
        IFigure figure;
        IFigure walker;

        while (keys.hasNext()) {
            figure = (IFigure) keys.next();
            walker = figure.getParent();
            contribution = (Rectangle) dirtyRegions.get(figure);
            // A figure can't paint beyond its own bounds
            contribution.intersect(figure.getBounds());
            while (!contribution.isEmpty() && walker != null) {
                walker.translateToParent(contribution);
                contribution.intersect(walker.getBounds());
                walker = walker.getParent();
            }
            if (damage == null)
                damage = new Rectangle(contribution);
            else
                damage.union(contribution);
        }

        if (!dirtyRegions.isEmpty()) {
            Map oldRegions = dirtyRegions;
            dirtyRegions = new HashMap();
            firePainting(damage, oldRegions);
        }

        if (damage != null && !damage.isEmpty()) {
            // ystem.out.println(damage);
            Graphics graphics = getGraphics(damage);
            if (graphics != null) {
                root.paint(graphics);
                releaseGraphics(graphics);
            }
        }
        damage = null;
    }

    /**
     * Adds the given runnable and queues an update if an update is not under
     * progress.
     * 
     * @param runnable
     *            the runnable
     */
    public synchronized void runWithUpdate(Runnable runnable) {
        afterUpdate = new RunnableChain(runnable, afterUpdate);
        if (!updating)
            queueWork();
    }

    /**
     * Sets the graphics source.
     * 
     * @param gs
     *            the graphics source
     */
    public void setGraphicsSource(GraphicsSource gs) {
        graphicsSource = gs;
    }

    /**
     * Sets the root figure.
     * 
     * @param figure
     *            the root figure
     */
    public void setRoot(IFigure figure) {
        root = figure;
    }

    /**
     * Validates all invalid figures on the update queue and calls
     * {@link UpdateManager#fireValidating()} unless there are no invalid
     * figures.
     */
    protected void validateFigures() {
        performValidation();
    }

    public void addRunnable(Runnable runnable) {
        if (!runnables.contains(runnable)) {
            runnables.add(runnable);
        }
    }

    public void removeRunnable(Runnable runnable) {
        if (runnables.contains(runnable)) {
            runnables.remove(runnable);
        }
    }

    private synchronized void executeRunables() {
        for (Runnable runnable : runnables) {
            runnable.run();
        }
        runnables.clear();
    }

    public void clearRunnables() {
        runnables.clear();
    }

}
