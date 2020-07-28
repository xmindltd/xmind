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
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LayoutListener;
import org.eclipse.draw2d.SWTEventDispatcher;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.xmind.ui.internal.ToolkitImages;

/**
 * A slider viewer displays linear values in a native
 * {@link org.eclipse.swt.widgets.Scale} widget (on Mac OS X) or a custom
 * {@link org.eclipse.draw2d.FigureCanvas} (for other platforms). The custom
 * canvas draws a draggable handle along and above a straight slot to mimic the
 * look and feel of the native Mac scale widget.
 * 
 * <p>
 * A {@link ISliderContentProvider} is used to provide major tick values
 * including the minimum and maximum values and the algorithm to convert between
 * widget selections and selectable values. The default content provider is an
 * implementation that simply converts between double values and their
 * <code>Double</code> object representation.
 * </p>
 * 
 * <p>
 * A {@link ILabelProvider} is used to provide the tool-tip text for the
 * selected value.
 * </p>
 * 
 * <p>
 * A {@link StructionSelection} is used as the viewer's selection, which always
 * contains the selected value as its only element.
 * </p>
 * 
 * <p>
 * The user interaction is guaranteed to behave the same for either native Scale
 * widget or custom canvas:
 * <ul>
 * <li>Press the left mouse button down on anywhere along the slot to trigger a
 * <i>selection</i> event;</li>
 * <li>Drag the mouse while pressing down the left button along the slot to
 * trigger <i>selection</i> events sequentially;</li>
 * <li>Release the mouse button to trigger a <i>post selection</i> event;</li>
 * <li>Double click on the slot to trigger an <i>open</i> event.</li>
 * </ul>
 * </p>
 * 
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>HORIZONTAL, VERTICAL</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles HORIZONTAL and VERTICAL may be specified.
 * </p>
 * 
 * @author Frank Shaka
 * 
 */
public class SliderViewer extends ContentViewer implements
        IPostSelectionProvider {

    protected static int DEFAULT_WIDTH = 200;

    protected static int DEFAULT_HEIGHT = 15;

    private static final int SLOT_HEIGHT = 4;

    private final class SliderFigureCanvas extends FigureCanvas {

        private SliderFigureCanvas(int style, Composite parent) {
            super(style, parent);
        }

        public Point computeSize(int wHint, int hHint, boolean changed) {
            int w = (wHint != SWT.DEFAULT) ? wHint : (vertical ? DEFAULT_HEIGHT
                    : DEFAULT_WIDTH);
            int h = (hHint != SWT.DEFAULT) ? hHint : (vertical ? DEFAULT_WIDTH
                    : DEFAULT_HEIGHT);
            org.eclipse.swt.graphics.Rectangle trim = computeTrim(0, 0, w, h);
            return new Point(trim.width, trim.height);
        }

        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            redraw();
        }
    }

    private final class BlockFigure extends ImageFigure {
        protected void paintFigure(Graphics graphics) {
            graphics.setAntialias(SWT.ON);
            if (control != null && !control.isDisposed() && control.isEnabled()) {
                graphics.setAlpha(0xff);
            } else {
                graphics.setAlpha(0x80);
            }
            super.paintFigure(graphics);
        }
    }

    private class SlotFigure extends Figure {

        public boolean containsPoint(int x, int y) {
            Rectangle r = getBounds();
            return y >= r.y - 5 && //
                    y < r.y + r.height + 5 && //
                    x >= r.x && //
                    x < r.x + r.width;
        }

        protected void paintFigure(Graphics graphics) {
            int alpha = 0x60;
            graphics.setAntialias(SWT.ON);
            if (control != null && !control.isDisposed() && control.isEnabled()) {
                graphics.setAlpha(0xff);
            } else {
                graphics.setAlpha(0x90);
            }
            Rectangle r = getBounds();
            Path shape = new Path(Display.getCurrent());
            float corner = Math.max(2, (vertical ? r.width : r.height) / 2);
            SWTUtils.addRoundedRectangle(shape, r.x, r.y, r.width - 1,
                    r.height - 1, corner);

            Pattern pattern = new Pattern(Display.getCurrent(), //
                    r.x, r.y, //
                    vertical ? r.right() - 1 : r.x, //
                    vertical ? r.y : r.bottom() - 1,// 
                    ColorConstants.gray, alpha,//
                    ColorConstants.lightGray, alpha);
            graphics.setBackgroundPattern(pattern);
            graphics.fillPath(shape);
            graphics.setBackgroundPattern(null);
            pattern.dispose();

            graphics.setAlpha(alpha);
            graphics.setForegroundColor(ColorConstants.gray);
            graphics.drawPath(shape);
            shape.dispose();
        }

    }

    private class SliderEventDispatcher extends SWTEventDispatcher {

        protected void updateFigureUnderCursor(MouseEvent me) {
            super.updateFigureUnderCursor(me);
            if (getCursorTarget() == blockFigure
                    && (me.stateMask & SWT.BUTTON_MASK) != 0) {
                getToolTipHelper().updateToolTip(null, null, 0, 0);
                updateHoverSource(me);
            }
        }
    }

    private static class DefaultSliderContentProvider implements
            ISliderContentProvider {

        public double getRatio(Object input, Object value) {
            return ((Double) value).doubleValue();
        }

        public Object getValue(Object input, double ratio) {
            return Double.valueOf(ratio);
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

    }

    private Control control;

    private IFigure slotFigure;

    private IFigure blockFigure;

    private double selectionRatio = 0;

    private Object selectionValue = null;

    private boolean vertical;

    private List<IOpenListener> openListeners = null;

    private List<ISelectionChangedListener> postSelectionChangedListeners = null;

    /**
     * @see SWT#HORIZONTAL
     * @see SWT#VERTICAL
     * @param parent
     * @param style
     */
    public SliderViewer(Composite parent, int style) {
        this.vertical = (style & SWT.VERTICAL) != 0;
        this.control = createControl(parent, style);
        configureControl(control);
        hookControl(control);
        setContentProvider(new DefaultSliderContentProvider());
    }

    protected Control createControl(Composite parent, int style) {
        int canvasStyle = SWT.NO_REDRAW_RESIZE | SWT.V_SCROLL | SWT.H_SCROLL
                | SWT.DOUBLE_BUFFERED;
        FigureCanvas fc = new SliderFigureCanvas(canvasStyle, parent);
        return fc;
    }

    protected void configureControl(Control control) {
        if (control instanceof Scale) {
            Scale scale = (Scale) control;
            scale.setMinimum(0);
            scale.setMaximum(10000);
            scale.setSelection(0);
            return;
        } else if (control instanceof FigureCanvas) {
            FigureCanvas fc = (FigureCanvas) control;
            fc.setScrollBarVisibility(FigureCanvas.NEVER);
            fc.getLightweightSystem().setEventDispatcher(
                    new SliderEventDispatcher());
            fc.setViewport(createViewport(fc));
            IFigure contents = createContents(fc);
            fc.setContents(contents);
            createSlotFigure(contents);
            createBlockFigure(contents);
        }
    }

    protected Viewport createViewport(FigureCanvas fc) {
        return new Viewport(true);
    }

    protected IFigure createContents(final FigureCanvas fc) {
        FreeformLayer contents = new FreeformLayer();
        contents.addLayoutListener(new LayoutListener.Stub() {
            public boolean layout(IFigure container) {
                layoutFigures(fc);
                return true;
            }
        });
        return contents;
    }

    protected void createBlockFigure(IFigure contents) {
        blockFigure = createBlockFigure();
        contents.add(blockFigure);
    }

    protected void createSlotFigure(IFigure contents) {
        slotFigure = createSlotFigure();
        contents.add(slotFigure);
    }

    protected IFigure createBlockFigure() {
        final BlockFigure figure = new BlockFigure();
        ImageDescriptor descriptor = ToolkitImages
                .get(ToolkitImages.SLIDER_HANDLE);
        final Image image = descriptor == null ? null : descriptor
                .createImage(control.getDisplay());
        figure.setImage(image);
        figure.setSize(figure.getPreferredSize());
        control.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                if (image != null) {
                    image.dispose();
                }
                figure.setImage(null);
            }
        });
        return figure;
    }

    protected IFigure createSlotFigure() {
        return new SlotFigure();
    }

    protected void hookControl(final Control control) {
        super.hookControl(control);
        if (control instanceof Scale) {
            Listener listener = new Listener() {

                private boolean mouseDown = false;
                private boolean mouseDrag = false;
                private boolean selectionChangedDuringDragging = false;

                public void handleEvent(Event event) {
                    switch (event.type) {
                    case SWT.Selection:
                        boolean selectionChanged = handleScaleSelection((Scale) event.widget);
                        if (selectionChanged && !selectionChangedDuringDragging
                                && (mouseDrag || mouseDown)) {
                            selectionChangedDuringDragging = true;
                        }
//                        if (!selectionChangedDuringDragging) {
//                            firePostSelectionChanged();
//                        }
                        break;
                    case SWT.MouseDoubleClick:
                        mouseDown = false;
                        mouseDrag = false;
                        selectionChangedDuringDragging = false;
                        fireOpen(new OpenEvent(SliderViewer.this,
                                getSelection()));
                        break;
                    case SWT.MouseDown:
                        mouseDown = true;
                        mouseDrag = false;
                        selectionChangedDuringDragging = false;
                        break;
                    case SWT.MouseMove:
                        if (!mouseDrag && mouseDown) {
                            mouseDrag = true;
                        }
                        break;
                    case SWT.MouseUp:
                        boolean postSelected = selectionChangedDuringDragging;
                        mouseDown = false;
                        mouseDrag = false;
                        selectionChangedDuringDragging = false;
                        if (postSelected) {
                            firePostSelectionChanged();
                        }
                    }
                }
            };
            control.addListener(SWT.Selection, listener);
            control.addListener(SWT.MouseDoubleClick, listener);
            control.addListener(SWT.MouseDown, listener);
            control.addListener(SWT.MouseMove, listener);
            control.addListener(SWT.MouseUp, listener);
            return;
        } else if (control instanceof FigureCanvas) {
            Listener listener = new Listener() {

                private boolean mouseDown = false;
                private boolean mouseDrag = false;
                private boolean selectionChangedDuringDragging = false;

                public void handleEvent(Event event) {
                    switch (event.type) {
                    case SWT.Resize:
                        handleResize();
                        break;
                    case SWT.MouseMove:
                        if (!mouseDrag && mouseDown) {
                            mouseDrag = true;
                        }
                        if (mouseDrag && (event.stateMask & SWT.BUTTON1) != 0) {
                            if (handleCanvasSelection(event.x, event.y)) {
                                selectionChangedDuringDragging = true;
                            }
                        }
                        break;
                    case SWT.MouseDown:
                        if (receives(event.x, event.y)) {
                            mouseDown = true;
                            mouseDrag = false;
                            if (event.button == 1) {
                                if (handleCanvasSelection(event.x, event.y)) {
                                    selectionChangedDuringDragging = true;
                                }
                            }
                        }
                        break;
                    case SWT.MouseUp:
                        boolean postSelected = selectionChangedDuringDragging;
                        mouseDown = false;
                        mouseDrag = false;
                        selectionChangedDuringDragging = false;
                        if (postSelected) {
                            firePostSelectionChanged();
                        }
                        break;
                    case SWT.MouseDoubleClick:
                        mouseDown = false;
                        mouseDrag = false;
                        fireOpen(new OpenEvent(SliderViewer.this,
                                getSelection()));
                        break;
                    }
                }
            };
            control.addListener(SWT.MouseMove, listener);
            control.addListener(SWT.MouseDown, listener);
            control.addListener(SWT.MouseUp, listener);
            control.addListener(SWT.Resize, listener);
            control.addListener(SWT.MouseDoubleClick, listener);
        }
    }

    protected boolean handleScaleSelection(Scale scale) {
        int min = scale.getMinimum();
        double portion = (scale.getSelection() - min) * 1.0d
                / (scale.getMaximum() - min);
        boolean selectionChanged = internalSetSelection(portion, false);
        refresh();
        return selectionChanged;
    }

    protected boolean handleCanvasSelection(int x, int y) {
        double newPortion = calcNewPortionCanvas(x, y);
        newPortion = Math.max(0, Math.min(1, newPortion));
        return internalSetSelection(newPortion, true);
    }

    protected double calcNewPortionCanvas(int x, int y) {
        Rectangle r = slotFigure.getBounds();
        if (vertical) {
            return ((r.y + r.height - y) * 1.0d) / r.height;
        } else {
            return ((x - r.x) * 1.0d) / r.width;
        }
    }

    protected boolean isVertical() {
        return vertical;
    }

    protected void layoutFigures(FigureCanvas fc) {
        Rectangle r = new Rectangle(fc.getViewport().getClientArea());
        if (vertical)
            transpose(r);
        Dimension size = blockFigure.getPreferredSize();
        Rectangle b = new Rectangle(r.x + size.width / 2, r.y
                + (r.height - SLOT_HEIGHT) / 2, r.width - size.width,
                SLOT_HEIGHT);
        int x = (int) (b.x + b.width * selectionRatio);
        int y = b.y + b.height - b.height / 2;
        Rectangle b2 = new Rectangle(x - size.width / 2, y - size.height / 2,
                size.width, size.height);
        if (vertical) {
            transpose(b);
            transpose(b2);
        }
        slotFigure.setBounds(b);
        blockFigure.setBounds(b2);
    }

    private static void transpose(Rectangle r) {
        int temp = r.x;
        r.x = -r.y - r.height;
        r.y = -temp - r.width;
        temp = r.width;
        r.width = r.height;
        r.height = temp;
    }

    protected boolean receives(int x, int y) {
        return slotFigure.containsPoint(x, y)
                || blockFigure.containsPoint(x, y);
    }

    protected IFigure getSlotFigure() {
        return slotFigure;
    }

    protected IFigure getBlockFigure() {
        return blockFigure;
    }

    protected double getSelectionPortion() {
        return selectionRatio;
    }

    public Control getControl() {
        return control;
    }

    public Object getSelectionValue() {
        if (selectionValue == null) {
            if (getContentProvider() instanceof ISliderContentProvider) {
                selectionValue = ((ISliderContentProvider) getContentProvider())
                        .getValue(getInput(), 0);
            }
            if (selectionValue == null) {
                selectionValue = Double.valueOf(0);
            }
        }
        return selectionValue;
    }

    public ISelection getSelection() {
        return new StructuredSelection(getSelectionValue());
    }

    public void refresh() {
        selectionRatio = calcSelectionRatio();
        if (control instanceof Scale) {
            refreshScale((Scale) control);
        } else if (control instanceof FigureCanvas) {
            refreshCanvas((FigureCanvas) control);
        }
    }

    protected void refreshScale(Scale scale) {
        int min = scale.getMinimum();
        int sel = (int) Math.round((scale.getMaximum() - min) * selectionRatio
                + min);
        scale.setSelection(sel);
        scale.setToolTipText(getSelectionText());
    }

    private String getSelectionText() {
        Object value = getSelectionValue();
        if (value == null)
            return null;
        if (getLabelProvider() instanceof ILabelProvider) {
            return ((ILabelProvider) getLabelProvider()).getText(value);
        }
        return null;
    }

    protected void refreshCanvas(FigureCanvas fc) {
        fc.getContents().revalidate();
        fc.getContents().repaint();
        String text = getSelectionText();
        if (text == null) {
            blockFigure.setToolTip(null);
        } else {
            blockFigure.setToolTip(new Label(text));
        }

    }

    protected double calcSelectionRatio() {
        Object value = getSelectionValue();
        if (value != null) {
            if (getContentProvider() instanceof ISliderContentProvider) {
                return ((ISliderContentProvider) getContentProvider())
                        .getRatio(getInput(), value);
            }
            if (value instanceof Double)
                return ((Double) value).doubleValue();
        }
        return 0;
    }

    public void setSelection(ISelection selection, boolean reveal) {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) selection;
            Object value = ss.getFirstElement();
            internalSetSelection(value, true);
        }
    }

    protected boolean internalSetSelection(double newPortion,
            boolean needRefresh) {
        return internalSetSelection(calcSelectionValue(newPortion), needRefresh);
    }

    protected Object calcSelectionValue(double portion) {
        if (portion >= 0 && portion <= 1) {
            if (getContentProvider() instanceof ISliderContentProvider) {
                return ((ISliderContentProvider) getContentProvider())
                        .getValue(getInput(), portion);
            }
        }
        return Double.valueOf(portion);
    }

    protected boolean internalSetSelection(Object newValue, boolean needRefresh) {
        if (newValue == selectionValue
                || (selectionValue != null && selectionValue.equals(newValue)))
            return false;

        this.selectionValue = newValue;
        if (needRefresh)
            refresh();
        fireSelectionChanged(new SelectionChangedEvent(this, getSelection()));
        return true;
    }

    protected void handleResize() {
        slotFigure.revalidate();
    }

    public void addOpenListener(IOpenListener listener) {
        if (openListeners == null)
            openListeners = new ArrayList<IOpenListener>();
        openListeners.add(listener);
    }

    public void removeOpenListener(IOpenListener listener) {
        if (openListeners == null)
            return;
        openListeners.remove(listener);
    }

    protected void fireOpen(final OpenEvent event) {
        if (openListeners == null)
            return;
        for (final Object l : openListeners.toArray()) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    ((IOpenListener) l).open(event);
                }
            });
        }
    }

    public void addPostSelectionChangedListener(
            ISelectionChangedListener listener) {
        if (postSelectionChangedListeners == null)
            postSelectionChangedListeners = new ArrayList<ISelectionChangedListener>();
        postSelectionChangedListeners.add(listener);
    }

    public void removePostSelectionChangedListener(
            ISelectionChangedListener listener) {
        if (postSelectionChangedListeners == null)
            return;
        postSelectionChangedListeners.remove(listener);
    }

    protected void firePostSelectionChanged(final SelectionChangedEvent event) {
        if (postSelectionChangedListeners == null)
            return;
        for (final Object l : postSelectionChangedListeners.toArray()) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    ((ISelectionChangedListener) l).selectionChanged(event);
                }
            });
        }
    }

    protected void firePostSelectionChanged() {
        if (getControl().isDisposed())
            return;
        getControl().getDisplay().asyncExec(new Runnable() {
            public void run() {
                if (getControl().isDisposed())
                    return;
                firePostSelectionChanged(new SelectionChangedEvent(
                        SliderViewer.this, getSelection()));
            }
        });
    }
}