package org.xmind.ui.internal.e4models;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolderRenderer;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.widgets.Control;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.resources.FontUtils;

public class ViewModelFolderRenderer extends CTabFolderRenderer {

    private static final int FLAGS = SWT.DRAW_TRANSPARENT | SWT.DRAW_MNEMONIC;

    private static final String ELLIPSIS = "..."; //$NON-NLS-1$

    private static final int BORDER1_COLOR = SWT.COLOR_WIDGET_NORMAL_SHADOW;

    private ResourceManager resources;

    public ViewModelFolderRenderer(CTabFolder parent) {
        super(parent);
        resources = new LocalResourceManager(JFaceResources.getResources(),
                parent);
    }

    @Override
    protected Point computeSize(int part, int state, GC gc, int wHint,
            int hHint) {
        Point size = super.computeSize(part, state, gc, wHint, hHint);
        if (0 <= part && part < parent.getItemCount()) {

            Control topRight = parent.getTopRight();
            if (parent.getItemCount() == 1) {
                size.x = parent.getSize().x;
            } else {
                size.x = (parent.getSize().x - (topRight.getSize().x + 8))
                        / parent.getItemCount();
            }
            size.y += 5;
        }

        return size;
    }

    @Override
    protected void draw(final int part, int state, Rectangle bounds, GC gc) {
        if (part == PART_HEADER) {
            drawTabArea2(gc, bounds, state);
        } else if (0 <= part && part < parent.getItemCount()) {
            if (bounds.width == 0 || bounds.height == 0) {
                return;
            }

            CTabItem item = parent.getItem(part);
            if (parent.getItemCount() == 1) {
                drawSingleItem(bounds, gc, item);
            } else {
                drawMultiItems(part, state, bounds, gc, item);
            }
        } else {
            super.draw(part, state, bounds, gc);
        }
    }

    private void drawSingleItem(Rectangle bounds, GC gc, CTabItem item) {
        //draw background
        int fullWidth = parent.getSize().x;

        gc.setBackground(
                (Color) resources.get(ColorUtils.toDescriptor("#f4f4f4"))); //$NON-NLS-1$
        gc.fillRectangle(bounds.x + 1, bounds.y + 1, bounds.x + fullWidth - 2,
                bounds.y + bounds.height - 1);

        //draw border
        gc.setLineWidth(1);
        gc.setForeground(
                (Color) resources.get(ColorUtils.toDescriptor("#cecece"))); //$NON-NLS-1$
        gc.drawLine(bounds.x + 1, bounds.y + bounds.height,
                bounds.x + fullWidth - 2, bounds.y + bounds.height);

        Rectangle topBounds = new Rectangle(bounds.x, bounds.y,
                parent.getSize().x, bounds.height);
        drawText(bounds, gc, item, "#1c1c1c", topBounds); //$NON-NLS-1$
    }

    private void drawMultiItems(final int part, int state, Rectangle bounds,
            GC gc, CTabItem item) {
        int fullWidth = parent.getSize().x;

        //draw background
        gc.setBackground(
                (Color) resources.get(ColorUtils.toDescriptor("#f4f4f4"))); //$NON-NLS-1$
        gc.fillRectangle(bounds.x + 1, bounds.y + 1, bounds.width,
                bounds.height - 1);

        //draw border
        gc.setLineWidth(1);
        gc.setForeground(
                (Color) resources.get(ColorUtils.toDescriptor("#a0a0a0"))); //$NON-NLS-1$
        gc.drawLine(bounds.x + 1, bounds.y + bounds.height,
                bounds.x + bounds.width, bounds.y + bounds.height);

        if ((state & SWT.SELECTED) != 0) {
            drawSelectedItem(part, gc, bounds, state);
        } else {
            drawUnselectedItem(part, gc, bounds, state);
        }

        //draw top right control area.
        if (part == parent.getItemCount() - 1) {
            //draw background
            gc.setBackground(
                    (Color) resources.get(ColorUtils.toDescriptor("#f4f4f4"))); //$NON-NLS-1$
            gc.fillRectangle(bounds.x + bounds.width + 1, bounds.y + 1,
                    fullWidth - (bounds.x + bounds.width + 1) - 1,
                    bounds.height - 1);

            //draw border
            gc.setLineWidth(1);
            gc.setForeground(
                    (Color) resources.get(ColorUtils.toDescriptor("#a0a0a0"))); //$NON-NLS-1$
            gc.drawLine(bounds.x + bounds.width + 1, bounds.y + bounds.height,
                    fullWidth - 1, bounds.y + bounds.height);
        }
    }

    private void drawSelectedItem(int part, GC gc, Rectangle bounds,
            int state) {
        //draw background
        gc.setBackground(
                (Color) resources.get(ColorUtils.toDescriptor("#28abe6"))); //$NON-NLS-1$
        gc.fillRectangle(bounds.x + 1, bounds.y + 1, bounds.width - 1,
                bounds.height - 1);

        //draw separator
        gc.setLineWidth(1);
        gc.setForeground(
                (Color) resources.get(ColorUtils.toDescriptor("#a0a0a0"))); //$NON-NLS-1$
        gc.drawLine(bounds.x + bounds.width, bounds.y, bounds.x + bounds.width,
                bounds.y + bounds.height - 1);

        if (part != 0) {
            gc.drawLine(bounds.x, bounds.y, bounds.x,
                    bounds.y + bounds.height - 1);
        }

        CTabItem item = parent.getItem(part);
        drawText(bounds, gc, item, "#ffffff", null); //$NON-NLS-1$
    }

    private void drawUnselectedItem(int part, GC gc, Rectangle bounds,
            int state) {
        //draw background
        gc.setBackground(
                (Color) resources.get(ColorUtils.toDescriptor("#f4f4f4"))); //$NON-NLS-1$
        gc.fillRectangle(bounds.x + 1, bounds.y + 1, bounds.width - 3,
                bounds.height - 1);

        //draw separator
        gc.setLineWidth(1);
        gc.setForeground(
                (Color) resources.get(ColorUtils.toDescriptor("#a0a0a0"))); //$NON-NLS-1$
        gc.drawLine(bounds.x + bounds.width, bounds.y, bounds.x + bounds.width,
                bounds.y + bounds.height - 1);

        if (part != 0) {
            gc.drawLine(bounds.x, bounds.y, bounds.x,
                    bounds.y + bounds.height - 1);
        }

        CTabItem item = parent.getItem(part);
        drawText(bounds, gc, item, "#1c1c1c", null); //$NON-NLS-1$
    }

    private void drawText(Rectangle bounds, GC gc, CTabItem item,
            String foreground, Rectangle topBounds) {
        Color gcForeground = gc.getForeground();
        gc.setForeground(
                (Color) resources.get(ColorUtils.toDescriptor(foreground)));

        Font gcFont = gc.getFont();
        gc.setFont((Font) resources.get(FontDescriptor.createFrom(
                FontUtils.relativeHeight(gcFont.getFontData(), 2))));

        item.setText(shortenText2(true, gc, item.getText(), bounds.width));

        Point extent = gc.textExtent(item.getText(), FLAGS);

        int textX = bounds.x + (bounds.width - extent.x) / 2;
        //centered text
        if (topBounds != null) {
            textX = topBounds.x + (topBounds.width - extent.x) / 2;
            if (textX + extent.x > bounds.x + bounds.width) {
                textX = bounds.x + bounds.width - extent.x;
            }
        }
        int textY = bounds.y + (bounds.height - extent.y) / 2;

        gc.drawText(item.getText(), textX, textY, FLAGS);

        gc.setFont(gcFont);
        gc.setForeground(gcForeground);
    }

    private String shortenText2(boolean useEllipses, GC gc, String text,
            int width) {
        return useEllipses ? shortenText2(gc, text, width, ELLIPSIS)
                : shortenText2(gc, text, width, ""); //$NON-NLS-1$
    }

    private String shortenText2(GC gc, String text, int width,
            String ellipses) {
        if (gc.textExtent(text, FLAGS).x <= width)
            return text;
        int ellipseWidth = gc.textExtent(ellipses, FLAGS).x;
        int length = text.length();
        TextLayout layout = new TextLayout(parent.getDisplay());
        layout.setText(text);
        int end = layout.getPreviousOffset(length, SWT.MOVEMENT_CLUSTER);
        while (end > 0) {
            text = text.substring(0, end);
            int l = gc.textExtent(text, FLAGS).x;
            if (l + ellipseWidth <= width) {
                break;
            }
            end = layout.getPreviousOffset(end, SWT.MOVEMENT_CLUSTER);
        }
        layout.dispose();
        return end == 0 ? text.substring(0, 1) : text + ellipses;
    }

    private void drawTabArea2(GC gc, Rectangle bounds, int state) {
        Color borderColor = parent.getDisplay().getSystemColor(BORDER1_COLOR);
        int tabHeight = parent.getTabHeight();

        gc.setLineWidth(1);
        gc.setForeground(borderColor);
        gc.drawRectangle(bounds.x, bounds.y, bounds.width - 1, tabHeight);
    }

}
