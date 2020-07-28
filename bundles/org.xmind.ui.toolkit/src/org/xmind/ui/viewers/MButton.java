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
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class MButton extends Viewer {

    /**
     * Style bit: Create control with default behaviours, i.e. showing text,
     * showing image.
     */
    public static final int NORMAL = 0;

    /**
     * Style bit: Don't show text.
     */
    public static final int NO_TEXT = 1;

    /**
     * Style bit: Don't show image.
     */
    public static final int NO_IMAGE = 1 << 1;

    /**
     * Style bit: Don't show spinner arrows.
     */
    public static final int NO_ARROWS = 1 << 2;

    private static final boolean DRAWS_FOCUS = Util.isMac();

    protected static final int MARGIN = DRAWS_FOCUS ? 4 : 1;
    protected static final int CORNER_SIZE = 5;
    protected static final int BORDER = (CORNER_SIZE + 1) / 2;
    protected static final int FOCUS_CORNER_SIZE = CORNER_SIZE - 2;
    protected static final int FOCUS_BORDER = (FOCUS_CORNER_SIZE + 1) / 2;
    protected static final int IMAGE_TEXT_SPACING = 3;
    protected static final int CONTENT_ARROW_SPACING = 4;
    protected static final int ARROW_WIDTH = 7;
    protected static final int ARROW_HEIGHT = 4;
    protected static final int ARROWS_SPACING = 2;
    protected static final String ELLIPSIS = "..."; //$NON-NLS-1$

    private Composite control;

    private int style;

    private String text = null;

    private Image image = null;

    private Color textForeground = null;

    private Color textBackground = null;

    private boolean hovered = false;

    private boolean pressed = false;

    private boolean forceFocus = false;

    private Point textSize = null;

    private Point imageSize = null;

    private int leftPadding = BORDER;

    private int rightPadding = BORDER;

    private int topPadding = BORDER;

    private int bottomPadding = BORDER;

    private List<IOpenListener> openListeners = null;

    /*
     * Caches:
     */
    private Point cachedTextSize = null;
    private Point cachedImageSize = null;
    private String appliedText = null;
    private Rectangle bounds = null;
    private Rectangle contentArea = null;
    private Point arrowLoc = null;
    private Rectangle imgArea = null;
    private Rectangle textArea = null;

    /**
     * Constructs a new instance of this class given its parent and a style
     * value describing its behavior and appearance.
     * 
     * @param parent
     *            a composite control which will be the parent of the new
     *            instance (cannot be null)
     * @param style
     *            the style of control to construct
     * 
     * @see #NORMAL
     * @see #NO_TEXT
     * @see #NO_IMAGE
     * @see #NO_ARROWS
     */
    public MButton(Composite parent, int style) {
        this.style = checkStyle(style, NORMAL, NORMAL, NO_TEXT, NO_IMAGE)
                | checkStyle(style, SWT.NONE, NO_ARROWS);
        this.control = new Canvas(parent, SWT.DOUBLE_BUFFERED) {
            public Point computeSize(int wHint, int hHint, boolean changed) {
                checkWidget();
                if (changed)
                    clearCaches();
                Point imageSize = getImageSize();
                Point textSize = getTextSize();
                boolean hasArrows = hasArrows();

                int width;
                if (wHint != SWT.DEFAULT) {
                    width = Math.max(wHint, MARGIN * 2);
                } else {
                    width = MARGIN * 2 + imageSize.x + textSize.x + leftPadding
                            + rightPadding;
                    if (hasArrows) {
                        width += ARROW_WIDTH + CONTENT_ARROW_SPACING;
                    }
                    if (imageSize.x != 0 && textSize.x != 0) {
                        width += IMAGE_TEXT_SPACING;
                    }
//                    if (hasArrows && (imageSize.x != 0 || textSize.x != 0)) {
//                        width += CONTENT_ARROW_SPACING;
//                    }
                }

                int minHeight = MARGIN * 2 + Math.max(imageSize.y, textSize.y)
                        + topPadding + bottomPadding;
                if (hasArrows) {
                    minHeight = Math.max(minHeight, ARROW_HEIGHT * 2
                            + ARROWS_SPACING);
                }
                int height = minHeight;
                Rectangle trim = computeTrim(0, 0, width, height);
                return new Point(trim.width, trim.height);
            }
        };
        hookControl(control);
    }

    protected void hookControl(Control control) {
        Listener listener = new Listener() {
            public void handleEvent(Event event) {
                switch (event.type) {
                case SWT.Paint:
                    paint(event.gc, event.display);
                    break;
                case SWT.Resize:
                    clearCaches();
                    break;
                case SWT.MouseDown:
                    if (event.button == 1)
                        handleMousePress();
                    break;
                case SWT.MouseUp:
                    if (event.button == 1)
                        handleMouseRelease();
                    break;
                case SWT.MouseEnter:
                    handleMouseEnter();
                    break;
                case SWT.MouseExit:
                    handleMouseExit();
                    break;
                case SWT.KeyDown:
                    handleKeyPress(event);
                    break;
                case SWT.FocusIn:
                    handleFocusIn();
                    break;
                case SWT.FocusOut:
                    handleFocusOut();
                }
            }
        };
        control.addListener(SWT.Paint, listener);
        control.addListener(SWT.Resize, listener);
        control.addListener(SWT.MouseDown, listener);
        control.addListener(SWT.MouseUp, listener);
        control.addListener(SWT.MouseEnter, listener);
        control.addListener(SWT.MouseExit, listener);
        control.addListener(SWT.KeyDown, listener);
        control.addListener(SWT.FocusIn, listener);
        control.addListener(SWT.FocusOut, listener);
    }

    protected void handleMousePress() {
        if (!getControl().isEnabled())
            return;
        setHovered(true);
        setPressed(true);
        getControl().setFocus();
        fireOpen();
    }

    protected void handleMouseRelease() {
        if (!getControl().isEnabled())
            return;
        setPressed(false);
    }

    protected void handleMouseEnter() {
        if (!getControl().isEnabled())
            return;
        setHovered(true);
    }

    protected void handleMouseExit() {
        if (!getControl().isEnabled())
            return;
        setHovered(false);
    }

    protected void handleFocusIn() {
        if (!getControl().isEnabled())
            return;
        refreshControl();
    }

    protected void handleFocusOut() {
        if (!getControl().isEnabled())
            return;
        setPressed(false);
        refreshControl();
    }

    protected void handleKeyPress(Event e) {
        if (!getControl().isEnabled())
            return;

        int keyCode = e.keyCode;
        int stateMask = e.stateMask;
        if (SWTUtils.matchKeyCode(keyCode, SWT.TAB)) {
            if (SWTUtils.matchState(stateMask, SWT.SHIFT)) {
                getControl().traverse(SWT.TRAVERSE_TAB_PREVIOUS);
            } else if (SWTUtils.matchState(stateMask, 0)) {
                getControl().traverse(SWT.TRAVERSE_TAB_NEXT);
            }
        } else if (SWTUtils.matchKey(stateMask, keyCode, 0, SWT.CR)) {
            fireOpen();
        }
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

    protected void fireOpen() {
        fireOpen(new OpenEvent(this, getSelection()));
    }

    public Control getControl() {
        return control;
    }

    protected int getStyle() {
        return style;
    }

    public String getText() {
        return text;
    }

    public Image getImage() {
        return image;
    }

    public boolean hasText() {
        return text != null && (style & NO_TEXT) == 0;
    }

    public boolean hasImage() {
        return image != null && (style & NO_IMAGE) == 0;
    }

    protected boolean hasArrows() {
        return (style & NO_ARROWS) == 0;
    }

    public Color getTextForeground() {
        return textForeground;
    }

    public Color getTextBackground() {
        return textBackground;
    }

    public void setTextForeground(Color c) {
        if (c == this.textForeground
                || (c != null && c.equals(this.textForeground)))
            return;
        this.textForeground = c;
        refreshControl();
    }

    public void setTextBackground(Color c) {
        if (c == this.textBackground
                || (c != null && c.equals(this.textBackground)))
            return;
        this.textBackground = c;
        refreshControl();
    }

    public boolean isHovered() {
        return hovered;
    }

    public boolean isPressed() {
        return pressed;
    }

    public boolean isForceFocus() {
        return forceFocus;
    }

    public void setHovered(boolean hovered) {
        if (hovered == this.hovered)
            return;
        this.hovered = hovered;
        refreshControl();
    }

    public void setPressed(boolean pressed) {
        if (pressed == this.pressed)
            return;
        this.pressed = pressed;
        refreshControl();
    }

    public void setForceFocus(boolean focused) {
        if (focused == this.forceFocus)
            return;
        this.forceFocus = focused;
        refreshControl();
    }

    public void setText(String text) {
        if (text == this.text || (text != null && text.equals(this.text)))
            return;
        this.text = text;
        cachedTextSize = null;
        clearCaches();
        refreshControl();
    }

    public void setImage(Image image) {
        if (image == this.image)
            return;
        this.image = image;
        cachedImageSize = null;
        clearCaches();
        refreshControl();
    }

    public Point getTextSize() {
        if (textSize != null)
            return textSize;
        if (cachedTextSize == null) {
            cachedTextSize = calcTextSize();
        }
        return cachedTextSize;
    }

    public Point getImageSize() {
        if (imageSize != null)
            return imageSize;
        if (cachedImageSize == null) {
            cachedImageSize = calcImageSize();
        }
        return cachedImageSize;
    }

    public void setTextSize(Point size) {
        if (size == this.textSize
                || (size != null && size.equals(this.textSize)))
            return;
        this.textSize = size;
        cachedTextSize = null;
        refreshControl();
    }

    public void setImageSize(Point size) {
        if (size == this.imageSize
                || (size != null && size.equals(this.imageSize)))
            return;
        this.imageSize = size;
        cachedImageSize = null;
        refreshControl();
    }

    protected Point calcTextSize() {
        String string = getText();
        if (!hasText()) {
            if ((style & NO_TEXT) != 0)
                return new Point(0, 0);
            string = "X"; //$NON-NLS-1$
        }
        Point size;
        GC gc = new GC(getControl().getDisplay());
        try {
            gc.setFont(getControl().getFont());
            size = gc.stringExtent(string);
        } finally {
            gc.dispose();
        }
        if (size.x == 0 && hasText())
            size.x = 5;
        return size;
    }

    protected Point calcImageSize() {
        Point size = new Point(0, 0);
        if (hasImage()) {
            Rectangle bounds = image.getBounds();
            size.x = Math.max(size.x, bounds.width);
            size.y = Math.max(size.y, bounds.height);
        }
        return size;
    }

    public void setEnabled(boolean enabled) {
        getControl().setEnabled(enabled);
        refreshControl();
    }

    public boolean isEnabled() {
        return getControl().isEnabled();
    }

    public String getAppliedText() {
        if (appliedText == null) {
            buildCaches();
        }
        return appliedText;
    }

    protected void clearCaches() {
        appliedText = null;
        bounds = null;
        contentArea = null;
        arrowLoc = null;
        imgArea = null;
        textArea = null;
    }

    protected void buildCaches() {
        bounds = control.getClientArea();
        bounds.x += MARGIN;
        bounds.y += MARGIN;
        bounds.width -= MARGIN * 2;
        bounds.height -= MARGIN * 2;
        int x1 = bounds.x + leftPadding;
        int y1 = bounds.y + topPadding;
        int w1 = bounds.width - (leftPadding + rightPadding);
        int h1 = bounds.height - (topPadding + bottomPadding);
        boolean hasArrows = hasArrows();

        if (hasArrows) {
            arrowLoc = new Point(x1 + w1 + rightPadding / 2 - ARROW_WIDTH, y1
                    + (h1 - ARROW_HEIGHT * 2 - ARROWS_SPACING) / 2 - 1);
        }
        contentArea = new Rectangle(x1, y1, w1
                - (hasArrows ? ARROW_WIDTH + CONTENT_ARROW_SPACING : 0), h1);

        boolean hasImage = hasImage();
        boolean hasText = hasText();
        if (hasImage) {
            if (hasText) {
                Point imgSize = getImageSize();
                imgArea = new Rectangle(x1, y1, imgSize.x, h1);
            } else {
                imgArea = contentArea;
            }
        }

        if (hasText) {
            if (hasImage) {
                int w = imgArea.width + IMAGE_TEXT_SPACING;
                textArea = new Rectangle(imgArea.x + w, y1, contentArea.width
                        - w, h1);
            } else {
                textArea = contentArea;
            }
            int maxTextWidth = textArea.width;
            Point textSize = getTextSize();
            if (textSize.x > maxTextWidth) {
                GC gc = new GC(getControl().getDisplay());
                try {
                    gc.setFont(getControl().getFont());
                    appliedText = getSubString(gc, text,
                            maxTextWidth - gc.stringExtent(ELLIPSIS).x)
                            + ELLIPSIS;
                } finally {
                    gc.dispose();
                }
            } else {
                appliedText = text;
            }
        }

    }

    protected void paint(GC gc, Display display) {
        if (bounds == null)
            buildCaches();

        gc.setAntialias(SWT.ON);
        gc.setTextAntialias(SWT.ON);

        int x, y, w, h;
        boolean focused = getControl().isFocusControl() || isForceFocus();
        boolean hasBackgroundAndBorder = pressed || hovered || focused;
        if (hasBackgroundAndBorder) {
            // draw control background
            gc.setBackground(getBorderBackground(display));
            gc.fillRoundRectangle(bounds.x, bounds.y, bounds.width,
                    bounds.height, CORNER_SIZE, CORNER_SIZE);
        }

        if (focused) {
            // draw focused content background
            x = contentArea.x - FOCUS_BORDER;
            y = contentArea.y - FOCUS_BORDER;
            w = contentArea.width + FOCUS_BORDER * 2;
            h = contentArea.height + FOCUS_BORDER * 2;
            gc.setBackground(getRealTextBackground(display));
            gc.fillRoundRectangle(x, y, w, h, FOCUS_CORNER_SIZE,
                    FOCUS_CORNER_SIZE);
        }

        boolean hasImage = hasImage();
        boolean hasText = hasText();
        if (hasImage) {
            Rectangle clipping = gc.getClipping();
            if (clipping == null || clipping.intersects(imgArea)) {
                // draw image
                Point imgSize = getImageSize();
                x = imgArea.x + (imgArea.width - imgSize.x) / 2;
                y = imgArea.y + (imgArea.height - imgSize.y) / 2;
                gc.setClipping(imgArea);
                gc.drawImage(image, x, y);
                gc.setClipping(clipping);
            }
        }
        if (hasText) {
            Rectangle clipping = gc.getClipping();
            if (clipping == null || clipping.intersects(textArea)) {
                // draw text
                String text = getAppliedText();
                gc.setFont(getControl().getFont());
                Point ext = gc.stringExtent(text);
//                    if (hasImage) {
                x = textArea.x;
//                    } else {
//                        x = textArea.x + (textArea.width - ext.x) / 2;
//                    }
                y = textArea.y + (textArea.height - ext.y) / 2;
                gc.setClipping(textArea);
                gc.setForeground(getRealTextForeground(display));
                gc.drawString(text, x, y, true);
                gc.setClipping(clipping);
            }
        }

        // draw arrows
        if (hasArrows() && arrowLoc != null) {
            gc.setBackground(Display.getCurrent().getSystemColor(
                    SWT.COLOR_WIDGET_NORMAL_SHADOW));
            x = arrowLoc.x + ARROW_WIDTH / 2;
            y = arrowLoc.y;
            int x1 = arrowLoc.x - 1;
            int y1 = arrowLoc.y + ARROW_HEIGHT + 1;
            int x2 = arrowLoc.x + ARROW_WIDTH;
            gc.fillPolygon(new int[] { x, y, x1, y1, x2, y1 });

            y += ARROW_HEIGHT * 2 + ARROWS_SPACING + 1;
            x1 = arrowLoc.x;
            y1 += ARROWS_SPACING;
            gc.fillPolygon(new int[] { x, y, x2, y1, x1 - 1, y1 });
        }

        // draw border
        if (focused) {
            x = bounds.x;
            y = bounds.y;
            w = bounds.width;
            h = bounds.height;
            if (DRAWS_FOCUS) {
                gc.drawFocus(x - MARGIN + 1, y - MARGIN + 1,
                        w + MARGIN * 2 - 2, h + MARGIN * 2 - 2);
            } else {
                gc.setForeground(getBorderForeground(display, focused));
                gc.drawRoundRectangle(x, y, w, h, CORNER_SIZE, CORNER_SIZE);
            }
        }
    }

    private Color getRealTextForeground(Display display) {
        if (!getControl().isEnabled())
            return display.getSystemColor(SWT.COLOR_GRAY);
        if (textForeground != null)
            return textForeground;
        return display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
    }

    private Color getRealTextBackground(Display display) {
        if (textBackground != null)
            return textBackground;
        return display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
    }

    private Color getBorderBackground(Display display) {
        if (pressed)
            return display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
        return display.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
    }

    private Color getBorderForeground(Display display, boolean focused) {
        if (focused)
            return display.getSystemColor(SWT.COLOR_WIDGET_BORDER);
        return display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
    }

    protected static int checkStyle(int style, int defaultValue, int... bits) {
        for (int bit : bits) {
            int s = style & bit;
            if (s != 0)
                return s;
        }
        return defaultValue;
    }

    protected static String getSubString(GC gc, String string, int maxWidth) {
        Point ext = gc.stringExtent(string);
        if (ext.x <= maxWidth || string.length() == 0)
            return string;
        return getSubString(gc, string.substring(0, string.length() - 1),
                maxWidth);
    }

    public Object getInput() {
        return null;
    }

    public ISelection getSelection() {
        return new StructuredSelection(this);
    }

    public void refresh() {
    }

    public void refreshControl() {
        getControl().redraw();
    }

    public void setInput(Object input) {
    }

    public void setSelection(ISelection selection, boolean reveal) {
    }

    public void setPaddings(int leftPadding, int topPadding, int rightPadding,
            int bottomPadding) {
        this.leftPadding = leftPadding;
        this.topPadding = topPadding;
        this.rightPadding = rightPadding;
        this.bottomPadding = bottomPadding;
        refreshControl();
    }

    public void setHorizontalPaddings(int leftPadding, int rightPadding) {
        this.leftPadding = leftPadding;
        this.rightPadding = rightPadding;
        refreshControl();
    }

    public void setVerticalPaddings(int topPadding, int bottomPadding) {
        this.topPadding = topPadding;
        this.bottomPadding = bottomPadding;
        refreshControl();
    }

    public int getLeftPadding() {
        return leftPadding;
    }

    public int getTopPadding() {
        return topPadding;
    }

    public int getRightPadding() {
        return rightPadding;
    }

    public int getBottomPadding() {
        return bottomPadding;
    }

}