package org.xmind.ui.editor;

import java.util.List;

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
import org.xmind.core.ISettingEntry;
import org.xmind.core.ISheet;
import org.xmind.core.ISheetSettings;
import org.xmind.ui.internal.editor.MindMapEditor;
import org.xmind.ui.resources.ColorUtils;

public class PageTitleTabFolderRenderer extends CTabFolderRenderer {

    private static final int FLAGS = 9;

    private static final String ELLIPSIS = "..."; //$NON-NLS-1$

    private MindMapEditor editor;

    private ResourceManager resources;

    public PageTitleTabFolderRenderer(CTabFolder parent, MindMapEditor editor) {
        super(parent);
        this.editor = editor;
        resources = new LocalResourceManager(JFaceResources.getResources(),
                parent);
    }

    @Override
    protected void draw(final int part, int state, Rectangle bounds, GC gc) {
        super.draw(part, state, bounds, gc);

        if (0 <= part && part < parent.getItemCount()) {
            if (bounds.width == 0 || bounds.height == 0)
                return;
            if ((state & SWT.SELECTED) != 0) {
                drawSelectedItem(part, gc, bounds, state);
            } else {
                drawUnselectedItem(part, gc, bounds, state);
            }
        }
    }

    private void drawSelectedItem(int index, GC gc, Rectangle bounds,
            int state) {
        if ((state & SWT.BACKGROUND) != 0) {
            drawItemBackground(index, gc, true);
        }
        if ((state & SWT.FOREGROUND) != 0) {
            drawItemText(index, gc);
        }
    }

    private void drawUnselectedItem(int index, GC gc, Rectangle bounds,
            int state) {
        CTabItem item = parent.getItem(index);
        // Do not draw partial items
        if (!item.isShowing())
            return;

        Rectangle clipping = gc.getClipping();
        if (!clipping.intersects(bounds))
            return;

        if ((state & SWT.BACKGROUND) != 0) {
            drawItemBackground(index, gc, false);
        }
    }

    private void drawItemBackground(int index, GC gc, boolean selected) {
        Rectangle bounds = parent.getItem(index).getBounds();
        Color gcBackground = gc.getBackground();

        Color background = getColor(index);
        if (background != null && !gcBackground.equals(background)) {
            gc.setBackground(background);

            Rectangle paintedArea = selected
                    ? new Rectangle(bounds.x, bounds.y + bounds.height * 6 / 7,
                            bounds.width, bounds.height / 7)
                    : new Rectangle(bounds.x, bounds.y, bounds.width,
                            bounds.height / 7);
            gc.fillRectangle(paintedArea);

            //recovery gc context
            gc.setBackground(gcBackground);
        }
    }

    private void drawItemText(int index, GC gc) {
        Color foreground = getColor(index);
        if (foreground == null || foreground.equals(gc.getForeground())) {
            return;
        }

        CTabItem item = parent.getItem(index);
        Rectangle bounds = item.getBounds();
        int x = bounds.x;
        int y = bounds.y;
        int height = bounds.height;
        int width = bounds.width;

        Rectangle trim = computeTrim(index, SWT.NONE, 0, 0, 0, 0);
        int xDraw = x - trim.x;

        int textWidth = x + width - xDraw - (trim.width + trim.x);
        if (textWidth > 0) {
            Font gcFont = gc.getFont();
            Color gcForeground = gc.getForeground();
            Color gcBackground = gc.getBackground();
            gc.setFont(item.getFont());
            gc.setBackground(
                    parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));

            String shortenedText = shortenText2(gc, item.getText(), textWidth);
            Point extent = gc.textExtent(shortenedText, FLAGS);
            int textY = y + (height - extent.y) / 2;
            boolean onBottom = (parent.getStyle() & SWT.BOTTOM) != 0;
            textY += onBottom ? -1 : 1;

            gc.setForeground(foreground);

//            gc.setBackground();
            gc.drawText(shortenedText, xDraw, textY, false);

            //recovery gc context
            gc.setFont(gcFont);
            gc.setForeground(gcForeground);
            gc.setBackground(gcBackground);
        }
    }

    private String shortenText2(GC gc, String text, int width) {
        return useEllipses2() ? shortenText2(gc, text, width, ELLIPSIS)
                : shortenText2(gc, text, width, ""); //$NON-NLS-1$
    }

    private boolean useEllipses2() {
        return parent.getSimple();
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

    private Color getColor(int index) {
        if (index < 0 || index >= editor.getPages().length) {
            return null;
        }

        ISheet sheet = editor.getPage(index).getAdapter(ISheet.class);
        String rgb = getRgb(sheet);

        if (rgb == null || rgb.equals("")) { //$NON-NLS-1$
            return null;
        }
        return (Color) resources.get(ColorUtils.toDescriptor(rgb));
    }

    private String getRgb(ISheet sheet) {
        ISettingEntry entry = findEntry(sheet);
        return entry == null ? null
                : entry.getAttribute(ISheetSettings.ATTR_RGB);
    }

    private ISettingEntry findEntry(ISheet sheet) {
        List<ISettingEntry> entries = sheet.getSettings()
                .getEntries(ISheetSettings.TAB_COLOR);
        return entries.size() == 0 ? null : entries.get(0);
    }

}
