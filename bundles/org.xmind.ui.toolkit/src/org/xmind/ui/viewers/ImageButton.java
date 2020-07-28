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
package org.xmind.ui.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;

/**
 * @author Frank Shaka
 */
public class ImageButton extends Viewer {

    private final class ImageLayout extends Layout {

        protected Point computeSize(Composite composite, int wHint, int hHint,
                boolean flushCache) {
            Image img = getNormalImage();
            if (img == null)
                return new Point(1, 1);
            Rectangle bounds = img.getBounds();
            return new Point(bounds.width, bounds.height);
        }

        protected void layout(Composite composite, boolean flushCache) {
        }

    }

    private final class EventHandler implements Listener {
        public void handleEvent(Event event) {
            switch (event.type) {
            case SWT.Paint:
                handlePaint(event);
                break;
            case SWT.Dispose:
                handleDispose(event);
                break;
            case SWT.MouseDown:
                handleMouseDown(event);
                break;
            case SWT.MouseUp:
                handleMouseUp(event);
                break;
            case SWT.MouseEnter:
                handleMouseEnter(event);
                break;
            case SWT.MouseExit:
                handleMouseExit(event);
                break;
            case SWT.MouseMove:
                handleMouseMove(event);
                break;
            }
        }
    }

    private Object input = null;

    private ImageDescriptor normalImageDescriptor = null;
    private ImageDescriptor pressedImageDescriptor = null;
    private ImageDescriptor disabledImageDescriptor = null;
    private ImageDescriptor hoveredImageDescriptor = null;

    private Image normalImage = null;
    private Image pressedImage = null;
    private Image disabledImage = null;
    private Image hoveredImage = null;

    private Canvas canvas;

    private boolean pressed = false;
    private boolean hovered = false;
//    private boolean enabled = true;

    private Point sourcePos = null;

    private List<IOpenListener> openListeners = null;

    /**
     * TODO complete 'check' style feature later
     */
    private boolean check = false;

    private boolean filled = false;

    public ImageButton(Composite parent, int style) {
        canvas = new Canvas(parent, checkStyle(style) | SWT.DOUBLE_BUFFERED) {
            @Override
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                refresh();
            }
        };
        configureControl(parent, canvas);
        hookControl(canvas);
    }

    private int checkStyle(int style) {
        check = (style & SWT.CHECK) != 0;
        filled = (style & SWT.FILL) != 0;
        int mask = SWT.PUSH | SWT.RADIO | SWT.CHECK | SWT.CASCADE | SWT.FILL;
        style &= ~mask;
        return style;
    }

    protected void configureControl(Composite parent, Control control) {
        control.setBackground(parent.getBackground());
        ((Composite) control).setLayout(new ImageLayout());
    }

    protected void hookControl(Control control) {
        Listener eventHandler = new EventHandler();
        control.addListener(SWT.Paint, eventHandler);
        control.addListener(SWT.Dispose, eventHandler);
        control.addListener(SWT.MouseDown, eventHandler);
        control.addListener(SWT.MouseUp, eventHandler);
        control.addListener(SWT.MouseEnter, eventHandler);
        control.addListener(SWT.MouseExit, eventHandler);
        control.addListener(SWT.MouseMove, eventHandler);
    }

    protected void handlePaint(Event event) {
        Image img = null;
        if (!isEnabled())
            img = getDisabledImage();
        if (img == null) {
            if (hovered)
                img = getHoveredImage();
            if (img == null) {
                if (pressed)
                    img = getPressedImage();
                if (img == null)
                    img = getNormalImage();
                if (img == null)
                    img = getDisabledImage();
            }
        }
        if (img != null) {
            if (filled) {
                Rectangle r1 = img.getBounds();
                Rectangle r2 = canvas.getBounds();
                event.gc.drawImage(img, r1.x, r1.y, r1.width, r1.height, 0, 0,
                        r2.width, r2.height);
            } else {
                event.gc.drawImage(img, 0, 0);
            }
        }
    }

    protected void handleDispose(Event event) {
        dispose();
    }

    protected void handleMouseDown(Event event) {
        if (isEnabled()) {
            if (event.button == 1) {
                pressed = true;
                sourcePos = new Point(event.x, event.y);
                refresh();
            }
        }
    }

    protected void handleMouseUp(Event event) {
        if (isEnabled()) {
            OpenEvent oe = null;
            if (pressed) {
                oe = new OpenEvent(this, getSelection());
            }
            pressed = false;
            sourcePos = null;
            refresh();
            if (oe != null) {
                asyncFireOpen(oe);
            }
        }
    }

    protected void handleMouseEnter(Event event) {
        if (isEnabled()) {
            if (event.button == 1) {
                pressed = true;
                refresh();
            } else {
                hovered = true;
                refresh();
            }
        }
    }

    protected void handleMouseExit(Event event) {
        if (isEnabled()) {
            if (event.button == 1) {
                pressed = false;
                refresh();
            } else {
                hovered = false;
                refresh();
            }
        }
    }

    protected void handleMouseMove(Event event) {
        if (isEnabled() && sourcePos != null) {
            int x = event.x;
            int y = event.y;
            boolean prevInside = containsPoint(sourcePos);
            boolean currInside = containsPoint(x, y);
            if (prevInside && !currInside) {
                handleMouseExit(event);
            } else if (!prevInside && currInside) {
                handleMouseEnter(event);
            }
            sourcePos.x = x;
            sourcePos.y = y;
        }
    }

    protected boolean isEnabled() {
        return canvas != null && !canvas.isDisposed() && canvas.isEnabled();
    }

    private boolean containsPoint(Point p) {
        return getBounds().contains(p);
    }

    private boolean containsPoint(int x, int y) {
        return getBounds().contains(x, y);
    }

    private Rectangle getBounds() {
        Rectangle bounds = canvas.getBounds();
        bounds.x = 0;
        bounds.y = 0;
        return bounds;
    }

    public Control getControl() {
        return canvas;
    }

    public ImageDescriptor getNormalImageDescriptor() {
        return normalImageDescriptor;
    }

    public ImageDescriptor getPressedImageDescriptor() {
        return pressedImageDescriptor;
    }

    public ImageDescriptor getDisabledImageDescriptor() {
        return disabledImageDescriptor;
    }

    public ImageDescriptor getHoveredImageDescriptor() {
        return hoveredImageDescriptor;
    }

    public void setNormalImageDescriptor(ImageDescriptor normalImageDescriptor) {
        if (normalImageDescriptor == this.normalImageDescriptor)
            return;
        if (normalImage != null) {
            normalImage.dispose();
            normalImage = null;
        }
        this.normalImageDescriptor = normalImageDescriptor;
        refresh();
    }

    public void setPressedImageDescriptor(ImageDescriptor pressedImageDescriptor) {
        if (pressedImageDescriptor == this.pressedImageDescriptor)
            return;
        if (pressedImage != null) {
            pressedImage.dispose();
            pressedImage = null;
        }
        this.pressedImageDescriptor = pressedImageDescriptor;
        refresh();
    }

    public void setDisabledImageDescriptor(
            ImageDescriptor disabledImageDescriptor) {
        if (disabledImageDescriptor == this.disabledImageDescriptor)
            return;
        if (disabledImage != null) {
            disabledImage.dispose();
            disabledImage = null;
        }
        this.disabledImageDescriptor = disabledImageDescriptor;
        refresh();
    }

    public void setHoveredImageDescriptor(ImageDescriptor hoveredImageDescriptor) {
        if (hoveredImageDescriptor == this.hoveredImageDescriptor)
            return;
        if (hoveredImage != null) {
            hoveredImage.dispose();
            hoveredImage = null;
        }
        this.hoveredImageDescriptor = hoveredImageDescriptor;
        refresh();
    }

    public void refresh() {
        if (!canvas.isDisposed())
            canvas.redraw();
    }

    protected Image getNormalImage() {
        if (normalImage == null) {
            if (normalImageDescriptor != null) {
                normalImage = normalImageDescriptor.createImage(false);
            }
        }
        return normalImage;
    }

    protected Image getPressedImage() {
        if (pressedImage == null) {
            if (pressedImageDescriptor != null) {
                pressedImage = pressedImageDescriptor.createImage(false);
            }
        }
        return pressedImage;
    }

    protected Image getDisabledImage() {
        if (disabledImage == null) {
            if (disabledImageDescriptor != null) {
                disabledImage = disabledImageDescriptor.createImage(false);
            }
        }
        return disabledImage;
    }

    protected Image getHoveredImage() {
        if (hoveredImage == null) {
            if (hoveredImageDescriptor != null) {
                hoveredImage = hoveredImageDescriptor.createImage(false);
            }
        }
        return hoveredImage;
    }

    public void addOpenListener(IOpenListener listener) {
        if (openListeners == null) {
            openListeners = new ArrayList<IOpenListener>();
        }
        openListeners.add(listener);
    }

    public void removeOpenListener(IOpenListener listener) {
        if (openListeners == null)
            return;
        openListeners.remove(listener);
    }

    private void asyncFireOpen(final OpenEvent event) {
        if (canvas.isDisposed())
            return;
        final Display display = canvas.getDisplay();
        if (display == null || display.isDisposed())
            return;
        display.asyncExec(new Runnable() {
            public void run() {
                if (canvas.isDisposed() || display.isDisposed())
                    return;
                fireOpen(event);
            }
        });
    }

    protected void fireOpen() {
        fireOpen(new OpenEvent(this, getSelection()));
    }

    protected void fireOpen(final OpenEvent event) {
        if (openListeners == null || event == null)
            return;
        Object[] listeners = openListeners.toArray();
        for (int i = 0; i < listeners.length; i++) {
            final IOpenListener listener = (IOpenListener) listeners[i];
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    listener.open(event);
                }
            });
        }
    }

    protected void dispose() {
        if (normalImage != null) {
            normalImage.dispose();
            normalImage = null;
        }
        if (pressedImage != null) {
            pressedImage.dispose();
            pressedImage = null;
        }
        if (disabledImage != null) {
            disabledImage.dispose();
            disabledImage = null;
        }
        if (hoveredImage != null) {
            hoveredImage.dispose();
            hoveredImage = null;
        }
    }

    public boolean isCheck() {
        return check;
    }

    public Object getInput() {
        return input;
    }

    public ISelection getSelection() {
        if (pressed) {
            if (getInput() != null)
                return new StructuredSelection(getInput());
            return new StructuredSelection(this);
        }
        return StructuredSelection.EMPTY;
    }

    public void setInput(Object input) {
        this.input = input;
    }

    public void setSelection(ISelection selection, boolean reveal) {
        if (selection == null)
            return;
        boolean newState = !selection.isEmpty();
        if (newState == this.pressed)
            return;
        this.pressed = newState;
        refresh();
    }

}