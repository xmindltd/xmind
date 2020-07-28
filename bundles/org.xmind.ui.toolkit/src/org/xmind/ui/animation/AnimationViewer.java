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

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;

public class AnimationViewer extends StructuredViewer {

    private static final int DEFAULT_DURATION = 125;

    protected static class Frame {

        Object element;

        Image image;

        long duration;

        Frame next;

        public Frame(Object element, Image image, long duration) {
            this.element = element;
            this.image = image;
            this.duration = duration;
        }
    }

    private class AnimationCanvas extends Canvas {

        public AnimationCanvas(Composite parent, int style) {
            super(parent, style);
        }

        public Point computeSize(int wHint, int hHint, boolean changed) {
            int w = wHint >= 0 ? wHint : 0;
            int h = hHint >= 0 ? hHint : 0;
            if (hasAnimatableFrames()) {
                Frame frame = startFrame;
                do {
                    Rectangle imgBounds = frame.image.getBounds();
                    if (wHint < 0)
                        w = Math.max(w, imgBounds.width);
                    if (hHint < 0)
                        h = Math.max(h, imgBounds.height);
                    frame = frame.next;
                } while (frame != startFrame && frame != null);
            }
            if (getStaticFrame() != null) {
                Frame frame = getStaticFrame();
                Rectangle imgBounds = frame.image.getBounds();
                if (wHint < 0)
                    w = Math.max(w, imgBounds.width);
                if (hHint < 0)
                    h = Math.max(h, imgBounds.height);
            }
            return new Point(w, h);
        }

    }

    private Canvas canvas;

    private Frame startFrame;

    private Frame staticFrame;

    private Frame currentFrame;

    private Thread animationThread;

    public AnimationViewer(Composite parent, int style) {
        this.canvas = new AnimationCanvas(parent, style);
        hookControl(this.canvas);
    }

    protected void hookControl(Control control) {
        super.hookControl(control);
        control.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
                paintCanvas(e);
            }
        });
    }

    protected void inputChanged(Object input, Object oldInput) {
        startFrame = null;
        staticFrame = null;
        buildFrames(input);
        if (canvas != null && !canvas.isDisposed()) {
            setCurrent(getStaticFrame());
            canvas.getParent().layout();
        }
    }

    private void buildFrames(Object input) {
        IStructuredContentProvider provider = (IStructuredContentProvider) getContentProvider();
        Object[] elements = provider.getElements(input);
        Object staticElement;
        if (getContentProvider() instanceof IAnimationContentProvider) {
            staticElement = ((IAnimationContentProvider) getContentProvider())
                    .getStaticElement(input, elements);
            staticFrame = createFrame(staticElement);
        } else {
            staticElement = null;
        }
        Frame last = null;
        for (int i = 0; i < elements.length; i++) {
            Object element = elements[i];
            if (staticElement == null || !staticElement.equals(element)) {
                Frame frame = createFrame(element);
                if (frame != null) {
                    if (startFrame == null)
                        startFrame = frame;
                    if (last != null)
                        last.next = frame;
                    last = frame;
                }
            }
        }
        if (last != null) {
            last.next = startFrame;
        }
    }

    private Frame createFrame(Object element) {
        if (getLabelProvider() instanceof ILabelProvider) {
            Image image = ((ILabelProvider) getLabelProvider())
                    .getImage(element);
            if (image != null) {
                long duration;
                if (getContentProvider() instanceof IAnimationContentProvider) {
                    duration = ((IAnimationContentProvider) getContentProvider())
                            .getDuration(element);
                    if (duration < 0)
                        duration = DEFAULT_DURATION;
                    else if (duration == 0)
                        duration = 1;
                } else {
                    duration = DEFAULT_DURATION;
                }
                return new Frame(element, image, duration);
            }
        }
        return null;
    }

    protected void paintCanvas(PaintEvent e) {
        Frame frame = currentFrame;
        if (frame == null)
            return;

        Rectangle area = canvas.getClientArea();
        if (area.width == 0 || area.height == 0)
            return;

        GC gc = e.gc;
        Rectangle r = frame.image.getBounds();
        int x = area.x + (area.width - r.width) / 2;
        int y = area.y + (area.height - r.height) / 2;
        gc.drawImage(frame.image, x, y);
    }

    protected void handleDispose(DisposeEvent event) {
        super.handleDispose(event);
        startFrame = null;
        staticFrame = null;
        currentFrame = null;
        animationThread = null;
    }

    public void start() {
        if (animationThread == null) {
            createAnimationThread();
        }
    }

    public void stop() {
        if (animationThread != null) {
            animationThread = null;
        }
    }

    private synchronized void createAnimationThread() {
        if (animationThread != null || !hasAnimatableFrames())
            return;

        setCurrent(getStartFrame());
        animationThread = new Thread(new Runnable() {
            public void run() {
                try {
                    loop();
                } catch (Throwable e) {
                }
                if (animationThread == null && !canvas.isDisposed()) {
                    canvas.getDisplay().syncExec(new Runnable() {
                        public void run() {
                            setCurrent(getStaticFrame());
                        }
                    });
                }
            }
        });
        animationThread.setPriority(Thread.NORM_PRIORITY + 2);
        animationThread.setDaemon(true);
        animationThread.start();
    }

    private void loop() {
        while (animationThread != null && !canvas.isDisposed()
                && hasAnimatableFrames()) {
            Display display = canvas.getDisplay();
            if (display.isDisposed())
                return;

            display.syncExec(new Runnable() {
                public void run() {
                    if (animationThread != null && hasAnimatableFrames()) {
                        Frame next;
                        if (currentFrame == null) {
                            next = getStartFrame();
                        } else {
                            next = currentFrame.next;
                        }
                        setCurrent(next);
                    }
                }
            });

            if (currentFrame == null)
                return;

            try {
                Thread.sleep(currentFrame.duration);
            } catch (Exception e) {
            }
        }
    }

    protected boolean hasAnimatableFrames() {
        return startFrame != null;
    }

    protected boolean hasFrames() {
        return staticFrame != null || startFrame != null;
    }

    protected Frame getStartFrame() {
        return startFrame;
    }

    protected Frame getStaticFrame() {
        return staticFrame;
    }

    protected Frame getCurrentFrame() {
        return currentFrame;
    }

    public Image getCurrentImage() {
        return currentFrame == null ? null : currentFrame.image;
    }

    public Object getCurrentElement() {
        return currentFrame == null ? null : currentFrame.element;
    }

    private void setCurrent(Frame frame) {
        if (currentFrame != frame && !canvas.isDisposed()) {
            this.currentFrame = frame;
            canvas.redraw();
            setSelection(new StructuredSelection(frame.element));
        }
    }

    public boolean isAnimating() {
        return animationThread != null;
    }

    protected Widget doFindInputItem(Object element) {
        return canvas;
    }

    protected Widget doFindItem(Object element) {
        return canvas;
    }

    protected void doUpdateItem(Widget item, Object element, boolean fullMap) {
    }

    protected List getSelectionFromWidget() {
        return Collections.EMPTY_LIST;
    }

    protected void internalRefresh(Object element) {
    }

    public void reveal(Object element) {
    }

    protected void setSelectionToWidget(List l, boolean reveal) {
    }

    public Control getControl() {
        return canvas;
    }

}