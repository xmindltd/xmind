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
package org.xmind.ui.properties;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ColorDescriptor;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.xmind.ui.util.IChained;
import org.xmind.ui.viewers.ILabelDescriptor;

public class PropertyEditingEntry
        implements IPropertyEditingEntry, IChained<PropertyEditingEntry> {

    private static final int MARGIN_V = 5;
    private static final int MARGIN_H = 8;
    private static final int CORNER = 8;
    private static final int HOVER_CORNER = 6;
    private static final int HOVER_SHRINK_V = 4;
    private static final int HOVER_SHRINK_H = 3;

//    private static final float RADIUS = 8;
//    private static final float RADIUS_BEND = RADIUS / 4;

    protected class PropertyEditingCanvas extends Canvas {

        /**
         * @param parent
         * @param style
         */
        public PropertyEditingCanvas(Composite parent, int style) {
            super(parent, style);
        }

        public void copy() {
            handleCopy();
        }

        public void paste() {
            handlePaste();
        }

    }

    private PropertiesEditor parent;

    private IPropertySource source;

    private IPropertyDescriptor descriptor;

    private Canvas canvas;

    private TextLayout nameLayout;

    private TextLayout valueLayout;

    private Image valueImage = null;

    private Color valueColor = null;

    private Font valueFont = null;

    private PropertyEditor editor;

    private Color foreground = null;

    private Font font = null;

    private Color selectedBackground = null;

    private Color selectedForeground = null;

    private Font selectedFont = null;

    private boolean selected = false;

    private boolean showingHover = false;

    private boolean showingEditorHover = false;

    private PropertyEditingEntry prev = null;

    private PropertyEditingEntry next = null;

    public PropertyEditingEntry(PropertiesEditor parent, IPropertySource source,
            IPropertyDescriptor descriptor) {
        this.parent = parent;
        this.source = source;
        this.descriptor = descriptor;
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter == IPropertySource.class)
            return source;
        if (adapter == IPropertyDescriptor.class)
            return descriptor;
        return null;
    }

    public boolean isEditable() {
        return editor != null;
    }

    public boolean isResettable() {
        return source.isPropertyResettable(descriptor.getId());
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.ui.properties.IPropertyEditingEntry#isPropertySet()
     */
    public boolean isPropertySet() {
        return source.isPropertySet(descriptor.getId());
    }

    public void resetPropertyValue() {
        source.resetPropertyValue(descriptor.getId());
        update();
    }

    public void createControl(Composite parent) {
        Assert.isTrue(canvas == null);

        nameLayout = new TextLayout(parent.getDisplay());
        nameLayout.setAlignment(SWT.LEFT);
        valueLayout = new TextLayout(parent.getDisplay());
        valueLayout.setAlignment(SWT.RIGHT);

        canvas = new PropertyEditingCanvas(parent, SWT.NONE);
        canvas.setToolTipText(descriptor.getDescription());
        canvas.setLayout(new Layout() {
            protected void layout(Composite composite, boolean flushCache) {
                if (flushCache) {
                    clearLayoutCache();
                }
                layoutControls();
            }

            protected Point computeSize(Composite composite, int wHint,
                    int hHint, boolean flushCache) {
                if (flushCache) {
                    clearLayoutCache();
                }
                return computeCanvasSize(wHint, hHint);
            }
        });
        Listener listener = new Listener() {

            long lastTime = 0;

            boolean pressedInEditorHover = false;

            public void handleEvent(Event event) {
                switch (event.type) {
                case SWT.Paint:
                    paintCanvas(event.gc, event.x, event.y, event.width,
                            event.height);
                    break;
                case SWT.FocusIn:
                    selectSingle();
                    break;
                case SWT.FocusOut:
//                    asyncCheckFocus();
                    break;
                case SWT.Traverse:
                    event.doit = handleKeyTraverse(event.detail);
                    break;
                case SWT.KeyDown:
                    handleKeyPress(event.keyCode, event.stateMask);
                    break;
                case SWT.MouseDown:
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastTime > Display.getCurrent()
                            .getDoubleClickTime()) {
                        lastTime = currentTime;
                        handleSingleClick();
                        if (isInEditorHover(event.x, event.y)) {
                            pressedInEditorHover = true;
                        }
                    }
                    break;
                case SWT.MouseUp:
                    if (pressedInEditorHover) {
                        open();
                    }
                    pressedInEditorHover = false;
                    break;
                case SWT.MouseMove:
                    handleMouseMove(event.x, event.y);
                    break;
                case SWT.MouseExit:
                    Display.getCurrent().asyncExec(new Runnable() {
                        public void run() {
                            setShowingHover(false);
                            setShowingEditorHover(false);
                        }
                    });
                    break;
                case SWT.MouseEnter:
                    handleMouseMove(event.x, event.y);
                    break;
                case SWT.MouseDoubleClick:
                    handleDoubleClick();
                }
            }

        };
        canvas.addListener(SWT.Paint, listener);
        canvas.addListener(SWT.Traverse, listener);
        canvas.addListener(SWT.FocusIn, listener);
        canvas.addListener(SWT.FocusOut, listener);
        canvas.addListener(SWT.KeyDown, listener);
        canvas.addListener(SWT.MouseDown, listener);
        canvas.addListener(SWT.MouseUp, listener);
        canvas.addListener(SWT.MouseMove, listener);
        canvas.addListener(SWT.MouseExit, listener);
        canvas.addListener(SWT.MouseEnter, listener);
        canvas.addListener(SWT.MouseDoubleClick, listener);

        editor = descriptor.createPropertyEditor(canvas);
        if (editor != null)
            initPropertyEditor(editor);

        updateLabels();
        updateEditor();
    }

    private void initPropertyEditor(final PropertyEditor editor) {
        editor.addEditingListener(new IEditingListener() {
            public void editingCanceled() {
                hideEditor();
                update();
            }

            public void editingFinished() {
                changeProperty(editor.getValue());
                hideEditor();
                update();
            }
        });
        editor.deactivate();
    }

    private void paintCanvas(GC gc, int px, int py, int pw, int ph) {
        if (canvas == null || canvas.isDisposed())
            return;

        Rectangle r = canvas.getBounds();
        Color b1 = canvas.getBackground();

        gc.setAntialias(SWT.ON);
        gc.setTextAntialias(SWT.ON);

        // Draw selection background:
        if (isSelected()) {
            gc.setBackground(selectedBackground);
            gc.fillRoundRectangle(0, 0, r.width, r.height, CORNER, CORNER);
        } else if (showingHover) {
            int oldAlpha = gc.getAlpha();
            gc.setAlpha(96 * oldAlpha / 255);
            gc.setBackground(selectedBackground);
            gc.fillRoundRectangle(0, 0, r.width, r.height, CORNER, CORNER);
            gc.setAlpha(oldAlpha);
        }

        // Draw edit indicator:
        if (showingEditorHover) {
            int oldAlpha = gc.getAlpha();
            gc.setAlpha(192 * oldAlpha / 255);
            gc.setBackground(b1);
            gc.fillRoundRectangle(r.width / 2 + HOVER_SHRINK_H, HOVER_SHRINK_V,
                    r.width / 2 - HOVER_SHRINK_H - HOVER_SHRINK_H,
                    r.height - HOVER_SHRINK_V - HOVER_SHRINK_V, HOVER_CORNER,
                    HOVER_CORNER);
            gc.setAlpha(oldAlpha);
//            
//            Control ec = editor == null ? null : editor.getControl();
//            if (ec != null && !ec.isDisposed()) {
//                Rectangle eb = ec.getBounds();
//                gc.setAlpha(192);
//                gc.setBackground(b1);
//                gc.fillRoundRectangle(eb.x, eb.y, eb.width, eb.height, 8, 8);
//                gc.setAlpha(255);
//            }
        }

        // Draw property name label:
        gc.setForeground(canvas.getForeground());
        gc.setFont(canvas.getFont());
        if (nameLayout != null && !nameLayout.isDisposed()) {
            Rectangle nb = nameLayout.getBounds();
            nameLayout.draw(gc, MARGIN_H, (r.height - nb.height) / 2);
        }

        // Draw property value repsentating image:
        int right = r.width - MARGIN_H;
        if (valueImage != null && !valueImage.isDisposed()) {
            Rectangle ib = valueImage.getBounds();
            int iw, ih;
            if (ib.height > r.height) {
                ih = r.height;
                iw = ib.width * r.height / ib.height;
            } else {
                ih = ib.height;
                iw = ib.width;
            }
            gc.drawImage(valueImage, 0, 0, ib.width, ib.height, right - iw,
                    (r.height - ih) / 2, iw, ih);
            right = right - iw - 3;
        }

        // Draw property value label:
        if (valueLayout != null && !valueLayout.isDisposed()) {
            Rectangle vb = valueLayout.getBounds();
            valueLayout.draw(gc, right - vb.width, (r.height - vb.height) / 2);
        }

    }

    private void clearLayoutCache() {
        //TODO
    }

    private void layoutControls() {
        Rectangle r = canvas.getBounds();
        int hw = Math.max(0, (r.width - MARGIN_H) / 2 - MARGIN_H);
        nameLayout.setWidth(Math.max(hw, 1));
        valueLayout.setWidth(Math.max(hw, 1));
        if (editor != null) {
            Control ec = editor.getControl();
            int eh = r.height - MARGIN_V - MARGIN_V;
            ec.setBounds(r.width - MARGIN_H - hw, (r.height - eh) / 2, hw, eh);
        }

//        if (border != null) {
//            border.dispose();
//            border = null;
//        }
//        border = new Path(Display.getCurrent());
//        float left = 0, top = 0, right = r.width, bottom = r.height;
//        border.moveTo(right - RADIUS, top);
//        border.cubicTo(right - RADIUS_BEND, top, right, top + RADIUS_BEND,
//                right, top + RADIUS);
//        border.lineTo(right, bottom - RADIUS);
//        border.cubicTo(right, bottom - RADIUS_BEND, right - RADIUS_BEND,
//                bottom, right - RADIUS, bottom);
//        border.lineTo(left + RADIUS, bottom);
//        border.cubicTo(left + RADIUS_BEND, bottom, left, bottom - RADIUS_BEND,
//                left, bottom - RADIUS);
//        border.lineTo(left, top + RADIUS);
//        border.cubicTo(left, top + RADIUS_BEND, left + RADIUS_BEND, top, left
//                + RADIUS, top);
//        border.close();
    }

    private Point computeCanvasSize(int wHint, int hHint) {
        if (wHint < 0 || hHint < 0) {
            if (editor != null && editor.getControl() != null
                    && !editor.getControl().isDisposed()) {
                Point editorSize = editor.getControl()
                        .computeSize(
                                wHint < 0 ? SWT.DEFAULT
                                        : Math.max(0,
                                                (wHint - MARGIN_H) / 2
                                                        - MARGIN_H - 1),
                                hHint, true);
                if (wHint < 0)
                    wHint = editorSize.x * 2;
                if (hHint < 0)
                    hHint = editorSize.y;
            } else {
                if (wHint < 0)
                    wHint = 100;
                if (hHint < 0)
                    hHint = 1;
            }
            Rectangle nb = nameLayout.getBounds();
            Rectangle vb = valueLayout.getBounds();
            hHint = Math.max(hHint, Math.max(nb.height, vb.height)) + MARGIN_V
                    + MARGIN_V;
        }
        return new Point(wHint, hHint);
    }

    public Control getControl() {
        return canvas;
    }

    public void setFont(Font font) {
        this.font = font;
        updateWidgets();
    }

    public void setForeground(Color color) {
        this.foreground = color;
        updateWidgets();
    }

    public void setSelectedBackground(Color color) {
        this.selectedBackground = color;
        updateWidgets();
    }

    public void setSelectedForeground(Color color) {
        this.selectedForeground = color;
        updateWidgets();
    }

    public void setSelectedFont(Font font) {
        this.selectedFont = font;
        updateWidgets();
    }

    private void showEditor() {
        if (editor != null) {
            editor.activate();
        }
    }

    private void hideEditor() {
        if (editor != null) {
            editor.deactivate();
        }
    }

    private void changeProperty(Object newValue) {
        source.setPropertyValue(descriptor.getId(), newValue);
    }

    public void setFocus() {
        if (editor != null && editor.isActivated()) {
            editor.setFocus();
        } else if (canvas != null && !canvas.isDisposed()) {
            canvas.setFocus();
        }
    }

    public void dispose() {
        if (editor != null) {
            editor.dispose();
            editor = null;
        }
        if (canvas != null && !canvas.isDisposed()) {
            canvas.dispose();
        }
        canvas = null;
        if (valueImage != null && !valueImage.isDisposed()) {
            valueImage.dispose();
        }
        valueImage = null;
        if (nameLayout != null && !nameLayout.isDisposed()) {
            nameLayout.dispose();
        }
        nameLayout = null;
        if (valueLayout != null && !valueLayout.isDisposed()) {
            valueLayout.dispose();
        }
        valueLayout = null;
//        if (border != null && !border.isDisposed()) {
//            border.dispose();
//        }
//        border = null;
    }

    public void update() {
        updateWidgets();
        updateEditor();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        boolean oldSelected = this.selected;
        this.selected = selected;
        updateWidgets();
        if (oldSelected != selected) {
            if (canvas != null && !canvas.isDisposed()) {
                canvas.redraw();
            }
        }
    }

    public void open() {
        selectSingle();
        showEditor();
        setShowingEditorHover(false);
        setFocus();
    }

    protected void updateWidgets() {
        if (isSelected()) {
            updateColorsFonts(selectedForeground, selectedFont);
        } else {
            updateColorsFonts(foreground, font);
        }
        updateLabels();
    }

    private void updateColorsFonts(Color foreground, Font font) {
        if (canvas != null && !canvas.isDisposed()) {
            canvas.setBackground(canvas.getParent().getBackground());
            canvas.setForeground(foreground);
            canvas.setFont(font);
            if (editor != null) {
                editor.setBackground(isSelected() ? selectedBackground
                        : canvas.getBackground());
                editor.setForeground(foreground);
                editor.setFont(font);
            }
        }
    }

    private void updateLabels() {
        Object value = source.getPropertyValue(descriptor.getId());
        if (nameLayout != null && !nameLayout.isDisposed()) {
            nameLayout.setText(descriptor.getDisplayName());
            nameLayout
                    .setStyle(
                            new TextStyle(canvas.getFont(),
                                    canvas.getForeground(), null),
                            0, nameLayout.getText().length());
        }
        ILabelDescriptor labelDescriptor = descriptor.getLabelDescriptor();
        ImageDescriptor image = labelDescriptor == null ? null
                : labelDescriptor.getImage(value);
        if (valueImage != null) {
            valueImage.dispose();
        }
        valueImage = image == null ? null
                : image.createImage(false, Display.getCurrent());

        ColorDescriptor color = labelDescriptor == null ? null
                : labelDescriptor.getForeground(value);
        if (valueColor != null) {
            valueColor.dispose();
        }
        valueColor = color == null ? null
                : color.createColor(Display.getCurrent());

        FontDescriptor font = labelDescriptor == null ? null
                : labelDescriptor.getFont(value);
        if (valueFont != null) {
            valueFont.dispose();
        }
        valueFont = font == null ? null : font.createFont(Display.getCurrent());

        if (valueLayout != null && !valueLayout.isDisposed()) {
            String valueText = labelDescriptor == null
                    ? (value == null ? "" //$NON-NLS-1$
                            : value.toString())
                    : labelDescriptor.getText(value);
            if (valueText == null)
                valueText = ""; //$NON-NLS-1$
            valueLayout.setText(valueText);
            valueLayout.setStyle(new TextStyle(
                    valueFont == null ? canvas.getFont() : valueFont,
                    valueColor == null ? canvas.getForeground() : valueColor,
                    null), 0, valueText.length());
        }
//        canvas.layout(true);
        canvas.redraw();
    }

    private void updateEditor() {
        if (editor != null) {
            editor.setValue(source.getPropertyValue(descriptor.getId()));
        }
    }

    private void selectSingle() {
        parent.select(this);
    }

    private boolean selectNext() {
        return parent.select(getNext());
    }

    private boolean selectPrev() {
        return parent.select(getPrevious());
    }

    protected void handleDoubleClick() {
        open();
    }

    protected void handleSingleClick() {
        selectSingle();
        setFocus();
    }

    protected boolean handleKeyTraverse(int traversal) {
        if (traversal == SWT.TRAVERSE_RETURN) {
            if (editor != null) {
                if (editor.isActivated()) {
                    changeProperty(editor.getValue());
                    hideEditor();
                    update();
                } else {
                    open();
                }
            }
            return false;
        } else if (traversal == SWT.TRAVERSE_ARROW_PREVIOUS) {
//            selectPrev();
            return true;
        } else if (traversal == SWT.TRAVERSE_ARROW_NEXT) {
//            selectNext();
            return true;
        } else if (traversal == SWT.TRAVERSE_TAB_NEXT) {
//            return selectNext();
            return true;
        } else if (traversal == SWT.TRAVERSE_TAB_PREVIOUS) {
//            return selectPrev();
            return true;
        } else if (traversal == SWT.TRAVERSE_ESCAPE) {
            if (editor.isActivated()) {
                hideEditor();
                update();
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    protected void handleKeyPress(int keyCode, int stateMask) {
        if (stateMask == 0) {
            if (keyCode == SWT.ARROW_DOWN || keyCode == SWT.ARROW_RIGHT) {
                selectNext();
            } else if (keyCode == SWT.ARROW_UP || keyCode == SWT.ARROW_LEFT) {
                selectPrev();
            } else if (keyCode == '\t') {
//                selectNext();
            }
        }
    }

    protected void handleMouseMove(int x, int y) {
        setShowingHover(isInHover(x, y));
        if (editor != null) {
            if (!editor.isActivated()) {
                if (isInEditorHover(x, y)) {
                    setShowingEditorHover(true);
                } else {
                    setShowingEditorHover(false);
                }
            } else {
                setShowingEditorHover(false);
            }
        }
    }

    protected boolean isInHover(int x, int y) {
        Rectangle r = canvas.getBounds();
        return x >= 0 && y >= 0 && x < r.width && y < r.height;
    }

    protected boolean isInEditorHover(int x, int y) {
        Rectangle r = canvas.getBounds();
        return x > r.width / 2 && x < r.width && y >= 0 && y < r.height;
    }

    private void setShowingHover(boolean showing) {
        if (showing == this.showingHover)
            return;

        this.showingHover = showing;
        if (canvas != null && !canvas.isDisposed()) {
            canvas.redraw();
        }
    }

    private void setShowingEditorHover(boolean showing) {
        if (showing == this.showingEditorHover)
            return;

        this.showingEditorHover = showing;
        if (canvas != null && !canvas.isDisposed()) {
            if (showing) {
                canvas.setCursor(
                        canvas.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
            } else {
                canvas.setCursor(null);
            }
            canvas.redraw();
        }
    }

    public PropertyEditingEntry getPrevious() {
        return prev;
    }

    public PropertyEditingEntry getNext() {
        return next;
    }

    public void setPrevious(PropertyEditingEntry element) {
        this.prev = element;
    }

    public void setNext(PropertyEditingEntry element) {
        this.next = element;
    }

    public void setPopupMenu(Menu menu) {
        if (canvas != null && !canvas.isDisposed()) {
            canvas.setMenu(menu);
        }
    }

    private void handleCopy() {
        String propertyId = descriptor.getId();
        Object value = source.getPropertyValue(propertyId);
        IPropertyTransfer transfer = parent.getTransfer();
        if (transfer != null) {
            transfer.setPropertyValueToClipboard(propertyId, value);
        }
    }

    private void handlePaste() {
        String propertyId = descriptor.getId();
        IPropertyTransfer transfer = parent.getTransfer();
        if (transfer != null) {
            Object value = transfer.getPropertyValueFromClipboard(propertyId);
            source.setPropertyValue(propertyId, value);
        }
    }

    @Override
    public boolean isTextOperation() {
        return descriptor.isTextOperation();
    }

}
