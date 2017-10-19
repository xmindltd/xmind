package org.xmind.cathy.internal.css;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.inject.Inject;

import org.eclipse.e4.ui.css.swt.dom.CTabFolderElement;
import org.eclipse.e4.ui.css.swt.dom.CompositeElement;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolderRenderer;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.xmind.cathy.internal.ICathyConstants;

@SuppressWarnings("restriction")
public class CathyCTabFolderRendering extends CTabFolderRenderer
        implements ICTabFolderRendering {
    private static final String CONTAINS_TOOLBAR = "CathyCTabFolderRendering.containsToolbar"; //$NON-NLS-1$

    // Constants for circle drawing
    final static int LEFT_TOP = 0;
    final static int LEFT_BOTTOM = 1;
    final static int RIGHT_TOP = 2;
    final static int RIGHT_BOTTOM = 3;

    // drop shadow constants
    final static int SIDE_DROP_WIDTH = 3;
    final static int BOTTOM_DROP_WIDTH = 4;

    // keylines
    final static int OUTER_KEYLINE = 1;
    final static int INNER_KEYLINE = 0;
    final static int TOP_KEYLINE = 0;

    // Item Constants
    static final int ITEM_TOP_MARGIN = 2;
    static final int ITEM_BOTTOM_MARGIN = 6;
    static final int ITEM_LEFT_MARGIN = 8;
    static final int ITEM_RIGHT_MARGIN = 4;
    static final int INTERNAL_SPACING = 4;

    static final int FLAGS = SWT.DRAW_TRANSPARENT | SWT.DRAW_MNEMONIC;
    static final String ELLIPSIS = "..."; //$NON-NLS-1$
    static final String E4_TOOLBAR_ACTIVE_IMAGE = "org.eclipse.e4.renderer.toolbar_background_active_image"; //$NON-NLS-1$
    static final String E4_TOOLBAR_INACTIVE_IMAGE = "org.eclipse.e4.renderer.toolbar_background_inactive_image"; //$NON-NLS-1$

    static final int BUTTON_BORDER = SWT.COLOR_WIDGET_DARK_SHADOW;
    static final int BUTTON_SIZE = 16;

    static final RGB CLOSE_FILL = new RGB(252, 160, 160);

    private Color closeFillColor;

    int[] shape;

    Image shadowImage, toolbarActiveImage, toolbarInactiveImage;

    int cornerSize = 14;

    //The best value
    boolean shadowEnabled = true;
    Color shadowColor;
    Color outerKeyline, innerKeyline;
    Color[] activeToolbar;
    int[] activePercents;
    Color[] inactiveToolbar;
    int[] inactivePercents;
    boolean active;

    Color[] selectedTabFillColors;
    int[] selectedTabFillPercents;

    Color[] selectedTabAreaColors;
    int[] selectedTabAreaPercents;

    Color[] unselectedTabsColors;
    int[] unselectedTabsPercents;

    Color[] hoverTabColors;
    int[] hoverTabPercents;

    Color tabOutlineColor;

    int paddingLeft = 2, paddingRight = 2, paddingTop = 2, paddingBottom = 2;

    private CTabFolderRendererWrapper rendererWrapper;
    private CTabFolderWrapper parentWrapper;

    private boolean textVisible = true;

    private boolean imageVisible = true;

    private boolean outerBorderVisible = true;
    private boolean innerBorderVisible = true;

    private boolean unselectedTabsBackgroundVisible = true;

    private Image maxImage;

    private Image minImage;

    private Image closeImage;

    private Image closeHoverImage;

    private int[] tabArea;

    //temp
    private boolean hoverBorderVisible = false;

    private boolean nothingToRender = false;

    @Inject
    public CathyCTabFolderRendering(CTabFolder parent) {
        super(parent);
        parentWrapper = new CTabFolderWrapper(parent);
        rendererWrapper = new CTabFolderRendererWrapper(this);
    }

    @Override
    protected Rectangle computeTrim(int part, int state, int x, int y,
            int width, int height) {
        if (!nothingToRender) {
            boolean onBottom = isTabOnBottom();
            int borderTop = onBottom ? INNER_KEYLINE + OUTER_KEYLINE
                    : TOP_KEYLINE + OUTER_KEYLINE;
            int borderBottom = onBottom ? TOP_KEYLINE + OUTER_KEYLINE
                    : INNER_KEYLINE + OUTER_KEYLINE;
            int marginWidth = parent.marginWidth;
            int marginHeight = parent.marginHeight;
            int sideDropWidth = shadowEnabled ? SIDE_DROP_WIDTH : 0;
            int bottomDropWidth = shadowEnabled ? BOTTOM_DROP_WIDTH : 0;
            int headerBorderBottom = outerBorderVisible ? OUTER_KEYLINE : 0;
            switch (part) {
            //body trimmed + body client area
            case PART_BODY:
                if (state == SWT.FILL) {
                    x = x - paddingLeft - sideDropWidth
                            - (INNER_KEYLINE + OUTER_KEYLINE) - marginWidth;
                    int tabHeight = parent.getTabHeight() + 1;
                    y = onBottom
                            ? y - paddingTop - marginHeight - borderTop
                                    - bottomDropWidth
                            : y - paddingTop - marginHeight - tabHeight
                                    - borderTop - headerBorderBottom
                                    - bottomDropWidth;
                    width = 2 * (INNER_KEYLINE + OUTER_KEYLINE) + paddingLeft
                            + paddingRight + 2 * sideDropWidth
                            + 2 * marginWidth;
                    height += paddingTop + paddingBottom + bottomDropWidth;
                    height += tabHeight + headerBorderBottom + borderBottom
                            + borderTop;
                } else {
                    x = x - marginWidth - OUTER_KEYLINE - INNER_KEYLINE
                            - sideDropWidth - (cornerSize / 2) - paddingLeft;
                    width = width + 2 * OUTER_KEYLINE + 2 * INNER_KEYLINE
                            + 2 * marginWidth + 2 * sideDropWidth + cornerSize
                            + paddingRight + paddingLeft;
                    int tabHeight = parent.getTabHeight() + 1;
                    if (parent.getMinimized()) {
                        y = onBottom ? y - borderTop - 5
                                : y - tabHeight - borderTop - 5;
                        height = borderTop + borderBottom + tabHeight;
                    } else {
                        y = onBottom
                                ? y - marginHeight - borderTop - paddingTop
                                        - bottomDropWidth
                                : y - marginHeight - tabHeight - borderTop
                                        - paddingTop - headerBorderBottom
                                        - bottomDropWidth;
                        height = height + borderBottom + borderTop
                                + 2 * marginHeight + tabHeight
                                + headerBorderBottom + bottomDropWidth
                                + paddingTop + paddingBottom;
                    }
                }
                break;
            case PART_HEADER:
                x = x - (INNER_KEYLINE + OUTER_KEYLINE) - marginWidth
                        - sideDropWidth;
                width = width + 2 * (INNER_KEYLINE + OUTER_KEYLINE)
                        + 2 * marginWidth + 2 * sideDropWidth;
                y = y - borderTop - marginHeight;
                break;
            case PART_BORDER:
                x = x - INNER_KEYLINE - OUTER_KEYLINE - sideDropWidth
                        - (cornerSize / 4);
                width = width + 2 * (INNER_KEYLINE + OUTER_KEYLINE)
                        + 2 * sideDropWidth + cornerSize / 2;
                height = height + borderTop + borderBottom;
                y = y - borderTop;
                if (onBottom) {
                    if (shadowEnabled) {
                        height += 3;
                    }
                }
                break;
            default:
                if (0 <= part && part < parent.getItemCount()) {
                    x = x - ITEM_LEFT_MARGIN;// - (CORNER_SIZE/2);
                    width = width + ITEM_LEFT_MARGIN + ITEM_RIGHT_MARGIN + 1;
                    y = y - ITEM_TOP_MARGIN;
                    height = height + ITEM_TOP_MARGIN + ITEM_BOTTOM_MARGIN;
                }
                break;
            }
        }
        return new Rectangle(x, y, width, height);
    }

    @Override
    protected Point computeSize(int part, int state, GC gc, int wHint,
            int hHint) {
        int width = 0, height = 0;
        switch (part) {
        case PART_HEADER:
            int fixedTabHeight = parentWrapper.getFixedTabHeight();
            if (fixedTabHeight != SWT.DEFAULT) {
                //TODO use field variable instead of 1
                height = fixedTabHeight == 0 ? 0 : fixedTabHeight + 1; // +1 for line drawn across top of tab
            } else {
                CTabItem[] items = parent.getItems();
                if (items.length == 0) {
                    height = gc.textExtent("Default", FLAGS).y + ITEM_TOP_MARGIN //$NON-NLS-1$
                            + ITEM_BOTTOM_MARGIN;
                } else {
                    for (int i = 0; i < items.length; i++) {
                        height = Math.max(height,
                                computeSize(i, SWT.NONE, gc, wHint, hHint).y);
                    }
                }
                height = Math.max(height, parent.getTabHeight() + 1);
                gc.dispose();
            }
            break;
        case PART_MAX_BUTTON:
        case PART_MIN_BUTTON:
        case PART_CLOSE_BUTTON:
            width = height = BUTTON_SIZE;
            break;
        case PART_CHEVRON_BUTTON:
            width = 3 * BUTTON_SIZE / 2;
            height = BUTTON_SIZE;
            break;
        default:
            if (0 <= part && part < parent.getItemCount()) {
                gc.setAdvanced(true);

                CTabItem item = parent.getItem(part);
                if (item.isDisposed())
                    return new Point(0, 0);

                if (imageVisible || shouldDrawImage(item)) {
                    Image image = item.getImage();
                    if (image != null && !image.isDisposed()) {
                        Rectangle bounds = image.getBounds();
                        if ((state & SWT.SELECTED) != 0
                                || parent.getUnselectedImageVisible()) {
                            width += bounds.width;
                        }
                        height = bounds.height;
                    }
                }

                if (textVisible) {
                    String text = null;
                    if ((state & MINIMUM_SIZE) != 0) {
                        int minChars = parent.getMinimumCharacters();
                        text = minChars == 0 ? null : item.getText();
                        if (text != null && text.length() > minChars) {
                            if (useEllipse()) {
                                int end = minChars < ELLIPSIS.length() + 1
                                        ? minChars
                                        : minChars - ELLIPSIS.length();
                                text = text.substring(0, end);
                                if (minChars > ELLIPSIS.length() + 1)
                                    text += ELLIPSIS;
                            } else {
                                int end = minChars;
                                text = text.substring(0, end);
                            }
                        }
                    } else {
                        text = item.getText();
                    }
                    if (text != null) {
                        if (width > 0)
                            width += INTERNAL_SPACING;
                        if (item.getFont() == null) {
                            Point size = gc.textExtent(text, FLAGS);
                            width += size.x;
                            height = Math.max(height, size.y);
                        } else {
                            Font gcFont = gc.getFont();
                            gc.setFont(item.getFont());
                            Point size = gc.textExtent(text, FLAGS);
                            width += size.x;
                            height = Math.max(height, size.y);
                            gc.setFont(gcFont);
                        }
                    }
                }

                if (parentWrapper.isShowClose() || item.getShowClose()) {
                    if ((state & SWT.SELECTED) != 0
                            || parent.getUnselectedCloseVisible()) {
                        if (width > 0)
                            width += INTERNAL_SPACING;
                        width += computeSize(PART_CLOSE_BUTTON, SWT.NONE, gc,
                                SWT.DEFAULT, SWT.DEFAULT).x;
                    }
                }
            }
            break;
        }
        Rectangle trim = computeTrim(part, state, 0, 0, width, height);
        width = trim.width;
        height = trim.height;
        return new Point(width, height);
    }

    private boolean useEllipse() {
        return parent.getSimple();
    }

    private Color getCloseFillColor() {
        if (closeFillColor == null) {
            closeFillColor = new Color(parent.getDisplay(), CLOSE_FILL);
        }
        return closeFillColor;
    }

    @Override
    protected void dispose() {
        if (shadowImage != null && !shadowImage.isDisposed()) {
            shadowImage.dispose();
            shadowImage = null;
        }
        if (closeFillColor != null) {
            closeFillColor.dispose();
            closeFillColor = null;
        }
        super.dispose();
    }

    @Override
    protected void draw(int part, int state, Rectangle bounds, GC gc) {
        if (nothingToRender) {
            return;
        }

        switch (part) {
        case PART_BACKGROUND:
            this.drawCustomBackground(gc, bounds, state);
            return;
        case PART_BODY:
            this.drawTabBody(gc, bounds, state);
            return;
        case PART_HEADER:
            this.drawTabHeader(gc, bounds, state);
            return;
        case PART_MAX_BUTTON:
            if (maxImage != null) {
                this.drawMaximizeButton(gc, bounds, state);
                return;
            }
            break;
        case PART_MIN_BUTTON:
            if (minImage != null) {
                this.drawMinimizeButton(gc, bounds, state);
                return;
            }
            break;
        case PART_CLOSE_BUTTON:
            if (closeImage != null) {
                this.drawCloseButton(gc, bounds, state);
                return;
            }
            break;
        default:
            if (0 <= part && part < parent.getItemCount()) {
                gc.setAdvanced(true);
                if (bounds.width == 0 || bounds.height == 0)
                    return;
                if ((state & SWT.SELECTED) != 0) {
                    drawSelectedTab(part, gc, bounds, state);
                    state &= ~SWT.BACKGROUND;
                    if ((state & SWT.SELECTED) != 0)
                        toDrawTab(true, part, gc, bounds, state);
                } else {
                    drawUnselectedTab(part, gc, bounds, state);
                    if ((state & SWT.HOT) == 0 && !active) {
                        gc.setAlpha(0x7f);
                        state &= ~SWT.BACKGROUND;
                        toDrawTab(false, part, gc, bounds, state);
                        gc.setAlpha(0xff);
                    } else {
                        state &= ~SWT.BACKGROUND;
                        toDrawTab(false, part, gc, bounds, state);
                    }
                }
                return;
            }
        }
        super.draw(part, state, bounds, gc);
    }

    private void drawCloseButton(GC gc, Rectangle bounds, int state) {
        Image hoverImage = closeHoverImage == null ? closeImage
                : closeHoverImage;
        switch (state & (SWT.HOT | SWT.SELECTED | SWT.BACKGROUND)) {
        case SWT.NONE:
            gc.drawImage(closeImage, bounds.x, bounds.y);
            break;
        case SWT.HOT:
            gc.drawImage(hoverImage, bounds.x, bounds.y);
            break;
        case SWT.SELECTED:
            gc.drawImage(hoverImage, bounds.x + 1, bounds.y + 1);
            break;
        case SWT.BACKGROUND:
            break;
        }
    }

    private void drawMinimizeButton(GC gc, Rectangle bounds, int state) {
        gc.drawImage(minImage, bounds.x, bounds.y);
    }

    private void drawMaximizeButton(GC gc, Rectangle bounds, int state) {
        gc.drawImage(maxImage, bounds.x, bounds.y);
    }

    void drawTabHeader(GC gc, Rectangle bounds, int state) {
        boolean onBottom = parent.getTabPosition() == SWT.BOTTOM;

        // Fill in background
        Region clipping = new Region();
        gc.getClipping(clipping);
        Region region = new Region();
        region.add(shape);
        region.intersect(clipping);
        gc.setClipping(region);

        int header = shadowEnabled ? onBottom ? 6 : 3 : 1;
        Rectangle trim = computeTrim(PART_HEADER, state, 0, 0, 0, 0);
        trim.width = bounds.width - trim.width;
        trim.height = computeSize(PART_HEADER, state, gc, 0, 0).y;
        trim.x = -trim.x;
        trim.y = onBottom ? bounds.height - parent.getTabHeight() - 1 - header
                : -trim.y;
        draw(PART_BACKGROUND, SWT.NONE, trim, gc);

        gc.setClipping(clipping);
        clipping.dispose();
        region.dispose();

        if (outerKeyline == null)
            outerKeyline = gc.getDevice().getSystemColor(SWT.COLOR_BLACK);
        gc.setForeground(outerKeyline);
        if (outerBorderVisible) {
            gc.drawPolyline(tabArea);
            gc.drawLine(trim.x, trim.y + trim.height, trim.x + trim.width,
                    trim.y + trim.height);
        }
    }

    void generateTabArea(Rectangle bounds) {
        int[] points = new int[1024];
        int index = 0;
        int radius = cornerSize / 2;
        int marginWidth = parent.marginWidth;
        int marginHeight = parent.marginHeight;
        int delta = (INNER_KEYLINE + OUTER_KEYLINE) * 2 + marginWidth * 2;
        int width = bounds.width - delta;
        int height = Math.max(
                parent.getTabHeight() + INNER_KEYLINE + OUTER_KEYLINE,
                bounds.height
                        - (INNER_KEYLINE + OUTER_KEYLINE + marginHeight * 2));

        int circX = bounds.x + radius + delta / 2;
        int circY = bounds.y + radius;

        // Body
        index = 0;
        int[] ltt = { bounds.x + delta / 2,
                bounds.y + parent.getTabHeight() + delta };
        System.arraycopy(ltt, 0, points, index, ltt.length);
        index += ltt.length;

        int[] lbb = drawCircle(circX, circY + height - (radius * 2), radius,
                LEFT_BOTTOM);
        System.arraycopy(lbb, 0, points, index, lbb.length);
        index += lbb.length;

        int[] rb = drawCircle(circX + width - (radius * 2),
                circY + height - (radius * 2), radius, RIGHT_BOTTOM);
        System.arraycopy(rb, 0, points, index, rb.length);
        index += rb.length;

        int[] rt = { bounds.x + delta / 2 + width,
                bounds.y + parent.getTabHeight() + delta };
        System.arraycopy(rt, 0, points, index, rt.length);
        index += rt.length;

//        points[index++] = bounds.x + delta / 2;
//        points[index++] = bounds.y + parent.getTabHeight() + 1;

        int[] tempPoints = new int[index];
        System.arraycopy(points, 0, tempPoints, 0, index);

        tabArea = tempPoints;

    }

    void drawTabBody(GC gc, Rectangle bounds, int state) {
        generateTabArea(bounds);

        int[] points = new int[1024];
        int index = 0;
        int radius = cornerSize / 2;
        int marginWidth = parent.marginWidth;
        int marginHeight = parent.marginHeight;
        int delta = (INNER_KEYLINE + OUTER_KEYLINE) * 2 + marginWidth * 2;
        int width = bounds.width - delta;
        int height = Math.max(
                parent.getTabHeight() + INNER_KEYLINE + OUTER_KEYLINE,
                bounds.height
                        - (INNER_KEYLINE + OUTER_KEYLINE + marginHeight * 2));

        int circX = bounds.x + radius + delta / 2;
        int circY = bounds.y + radius;

        // Body
        index = 0;
        int[] ltt = drawCircle(circX, circY, radius, LEFT_TOP);
        System.arraycopy(ltt, 0, points, index, ltt.length);
        index += ltt.length;

        int[] lbb = drawCircle(circX, circY + height - (radius * 2), radius,
                LEFT_BOTTOM);
        System.arraycopy(lbb, 0, points, index, lbb.length);
        index += lbb.length;

        int[] rb = drawCircle(circX + width - (radius * 2),
                circY + height - (radius * 2), radius, RIGHT_BOTTOM);
        System.arraycopy(rb, 0, points, index, rb.length);
        index += rb.length;

        int[] rt = drawCircle(circX + width - (radius * 2), circY, radius,
                RIGHT_TOP);
        System.arraycopy(rt, 0, points, index, rt.length);
        index += rt.length;
        points[index++] = circX;
        points[index++] = circY - radius;

        int[] tempPoints = new int[index];
        System.arraycopy(points, 0, tempPoints, 0, index);

        // Fill in parent background for non-rectangular shape
        Region r = new Region();
        r.add(bounds);
        r.subtract(tempPoints);
        gc.setBackground(parent.getParent().getBackground());
        Display display = parent.getDisplay();
        Region clipping = new Region();
        gc.getClipping(clipping);
        r.intersect(clipping);
        gc.setClipping(r);
        Rectangle mappedBounds = display.map(parent, parent.getParent(),
                bounds);
        parent.getParent().drawBackground(gc, bounds.x, bounds.y, bounds.width,
                bounds.height, mappedBounds.x, mappedBounds.y);

        // Shadow
        if (shadowEnabled)
            drawShadow(display, bounds, gc);

        gc.setClipping(clipping);
        clipping.dispose();
        r.dispose();

        // Remember for use in header drawing
        shape = tempPoints;
    }

    /*
     * Draw active and unactive selected tab item
     */
    void drawSelectedTab(int itemIndex, GC gc, Rectangle bounds, int state) {
        if (parent.getSingle() && parent.getItem(itemIndex).isShowing())
            return;

        boolean onBottom = parent.getTabPosition() == SWT.BOTTOM;
        int header = shadowEnabled ? 2 : 0;
        int width = bounds.width;
        int[] points = new int[1024];
        int index = 0;
        int radius = cornerSize / 2;
        int circX = bounds.x + radius;
        int circY = onBottom ? bounds.y + bounds.height - header - radius
                : bounds.y + radius;
        int selectionX1, selectionY1, selectionX2, selectionY2;
        int bottomY = onBottom ? bounds.y - header : bounds.y + bounds.height;
        if (itemIndex == 0
                && bounds.x == -computeTrim(CTabFolderRenderer.PART_HEADER,
                        SWT.NONE, 0, 0, 0, 0).x) {
//            circX -= 1;
//            points[index++] = circX - radius;
//            points[index++] = bottomY;

            points[index++] = selectionX1 = circX - radius;
            points[index++] = selectionY1 = bottomY;
        } else {
            if (active) {
                points[index++] = shadowEnabled ? SIDE_DROP_WIDTH
                        : 0 + INNER_KEYLINE + OUTER_KEYLINE;
                points[index++] = bottomY;
            }
            points[index++] = selectionX1 = bounds.x;
            points[index++] = selectionY1 = bottomY;
        }

        int startX = -1, endX = -1;
        if (!onBottom) {
            int[] ltt = drawCircle(circX, circY, radius, LEFT_TOP);
            startX = ltt[6];
            for (int i = 0; i < ltt.length / 2; i += 2) {
                int tmp = ltt[i];
                ltt[i] = ltt[ltt.length - i - 2];
                ltt[ltt.length - i - 2] = tmp;
                tmp = ltt[i + 1];
                ltt[i + 1] = ltt[ltt.length - i - 1];
                ltt[ltt.length - i - 1] = tmp;
            }
            System.arraycopy(ltt, 0, points, index, ltt.length);
            index += ltt.length;

            int[] rt = drawCircle(circX + width - (radius * 2), circY, radius,
                    RIGHT_TOP);
            endX = rt[rt.length - 4];
            for (int i = 0; i < rt.length / 2; i += 2) {
                int tmp = rt[i];
                rt[i] = rt[rt.length - i - 2];
                rt[rt.length - i - 2] = tmp;
                tmp = rt[i + 1];
                rt[i + 1] = rt[rt.length - i - 1];
                rt[rt.length - i - 1] = tmp;
            }
            System.arraycopy(rt, 0, points, index, rt.length);
            index += rt.length;

            points[index++] = selectionX2 = bounds.width + circX - radius;
            points[index++] = selectionY2 = bottomY;
        } else {
            int[] ltt = drawCircle(circX, circY, radius, LEFT_BOTTOM);
            startX = ltt[6];
            System.arraycopy(ltt, 0, points, index, ltt.length);
            index += ltt.length;

            int[] rt = drawCircle(circX + width - (radius * 2), circY, radius,
                    RIGHT_BOTTOM);
            endX = rt[rt.length - 4];
            System.arraycopy(rt, 0, points, index, rt.length);
            index += rt.length;

            points[index++] = selectionX2 = bounds.width + circX - radius;
            points[index++] = selectionY2 = bottomY;
        }

        if (active) {
            points[index++] = parent.getSize().x - (shadowEnabled
                    ? SIDE_DROP_WIDTH : 0 + INNER_KEYLINE + OUTER_KEYLINE);
            points[index++] = bottomY;
        }
        gc.setClipping(bounds.x, onBottom ? bounds.y - header : bounds.y,
                parent.getSize().x - (shadowEnabled ? SIDE_DROP_WIDTH
                        : 0 + INNER_KEYLINE + OUTER_KEYLINE),
                bounds.y + bounds.height);

        Pattern backgroundPattern = null;
        if (selectedTabFillColors == null) {
            setSelectedTabFill(gc.getDevice().getSystemColor(SWT.COLOR_WHITE));
        }
        if (selectedTabFillColors.length == 1) {
            gc.setBackground(selectedTabFillColors[0]);
            gc.setForeground(selectedTabFillColors[0]);
        } else if (!onBottom && selectedTabFillColors.length == 2) {
            // for now we support the 2-colors gradient for selected tab
            backgroundPattern = new Pattern(gc.getDevice(), 0, 0, 0,
                    bounds.height + 1, selectedTabFillColors[0],
                    selectedTabFillColors[1]);
            gc.setBackgroundPattern(backgroundPattern);
            gc.setForeground(selectedTabFillColors[1]);
        }

        int[] tmpPoints = new int[index];
        System.arraycopy(points, 0, tmpPoints, 0, index);
        gc.fillPolygon(tmpPoints);

        //cover item bottom border using background color
        gc.drawLine(selectionX1, selectionY1, selectionX2, selectionY2);

        gc.setClipping(bounds.x - 1,
                onBottom ? bounds.y - header : bounds.y - 1,
                parent.getSize().x - (shadowEnabled ? SIDE_DROP_WIDTH
                        : 0 + INNER_KEYLINE + OUTER_KEYLINE),
                bounds.y + bounds.height);
        if (innerBorderVisible) {
            if (innerKeyline == null)
                innerKeyline = gc.getDevice().getSystemColor(SWT.COLOR_BLACK);
            gc.setForeground(innerKeyline);
            gc.drawPolyline(tmpPoints);
        }
        Rectangle rect = null;
        gc.setClipping(rect);

        if (outerBorderVisible) {
            if (!onBottom) {
                if (outerKeyline == null)
                    outerKeyline = gc.getDevice()
                            .getSystemColor(SWT.COLOR_BLACK);
                gc.setForeground(outerKeyline);
                gc.drawLine(startX + 2, 1, endX - 1, 1);
            }
        }

        if (backgroundPattern != null) {
            backgroundPattern.dispose();
        }
    }

    private boolean isTabOnBottom() {
        return parent.getTabPosition() == SWT.BOTTOM;
    }

    private String getShortenedText(GC gc, String text, int width) {
        return useEllipse() ? getShortenedText(gc, text, width, ELLIPSIS)
                : getShortenedText(gc, text, width, ""); //$NON-NLS-1$
    }

    private String getShortenedText(GC gc, String text, int width,
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

    private void toDrawTab(boolean selected, int itemIndex, GC gc,
            Rectangle bounds, int state) {
        CTabItem item = parent.getItem(itemIndex);
        int x = bounds.x;
        int y = bounds.y;
        int height = bounds.height;
        int width = bounds.width;

        int rightEdge = Math.min(x + width, parentWrapper.getRightItemEdge(gc));

        if ((state & SWT.FOREGROUND) != 0) {
            CTabItemWrapper itemWrapper = new CTabItemWrapper(item);
            Rectangle itemCloseRect = itemWrapper.getCloseRect();
            itemCloseRect = new Rectangle(itemCloseRect.x, itemCloseRect.y,
                    itemCloseRect.width, itemCloseRect.height);

            // draw Image
            Rectangle trim = computeTrim(itemIndex, SWT.NONE, 0, 0, 0, 0);
            int xDraw = x - trim.x;
            if (parent.getSingle()
                    && (parentWrapper.isShowClose() || item.getShowClose()))
                xDraw += itemWrapper.getCloseRect().width;
            if (imageVisible || shouldDrawImage(item)) {
                Image image = item.getImage();
                if (image != null && !image.isDisposed()) {
                    Rectangle imageBounds = image.getBounds();
                    // only draw image if it won't overlap with close button
                    int maxImageWidth = rightEdge - xDraw
                            - (trim.width + trim.x);
                    if (!parent.getSingle() && itemCloseRect.width > 0)
                        maxImageWidth -= itemCloseRect.width + INTERNAL_SPACING;
                    if (imageBounds.width < maxImageWidth) {
                        int imageX = xDraw;
                        int imageY = y + (height - imageBounds.height) / 2;
                        imageY += isTabOnBottom() ? -1 : 1;
                        gc.drawImage(image, imageX, imageY);
                        xDraw += imageBounds.width + INTERNAL_SPACING;
                    }
                }
            }

            if (textVisible) {
                // draw Text
                int textWidth = rightEdge - xDraw - (trim.width + trim.x);
                if (!parent.getSingle() && itemCloseRect.width > 0)
                    textWidth -= itemCloseRect.width + INTERNAL_SPACING;
                if (textWidth > 0) {
                    Font gcFont = gc.getFont();
                    gc.setFont(item.getFont() == null ? parent.getFont()
                            : item.getFont());

                    if (itemWrapper.getShortenedText() == null || itemWrapper
                            .getShortenedTextWidth() != textWidth) {
                        itemWrapper.setShortenedText(getShortenedText(gc,
                                item.getText(), textWidth));
                        itemWrapper.setShortenedTextWidth(textWidth);
                    }
                    Point extent = gc.textExtent(itemWrapper.getShortenedText(),
                            FLAGS);
                    int textY = y + (height - extent.y) / 2;
                    textY += isTabOnBottom() ? -1 : 1;

                    gc.setForeground(selected ? parent.getSelectionForeground()
                            : parent.getForeground());
                    gc.drawText(itemWrapper.getShortenedText(), xDraw, textY,
                            FLAGS);
                    gc.setFont(gcFont);

                    if (selected) {
                        // draw a Focus rectangle
                        if (parent.isFocusControl()) {
                            Display display = parent.getDisplay();
                            if (parent.getSimple() || parent.getSingle()) {
                                gc.setBackground(display
                                        .getSystemColor(SWT.COLOR_BLACK));
                                gc.setForeground(display
                                        .getSystemColor(SWT.COLOR_WHITE));
                                gc.drawFocus(xDraw - 1, textY - 1, extent.x + 2,
                                        extent.y + 2);
                            } else {
                                gc.setForeground(
                                        display.getSystemColor(BUTTON_BORDER));
                                gc.drawLine(xDraw, textY + extent.y + 1,
                                        xDraw + extent.x + 1,
                                        textY + extent.y + 1);
                            }
                        }
                    }

                }
            }

            if (parentWrapper.isShowClose() || item.getShowClose()) {
                if (closeImage != null) {
                    drawCloseButton(gc, itemCloseRect,
                            itemWrapper.getCloseImageState());
                } else {
                    drawClose2(gc, itemCloseRect,
                            itemWrapper.getCloseImageState());
                }
            }
        }
    }

    void drawClose2(GC gc, Rectangle closeRect, int closeImageState) {
        if (closeRect.width == 0 || closeRect.height == 0)
            return;
        Display display = parent.getDisplay();

        // draw X 9x9
        int x = closeRect.x + Math.max(1, (closeRect.width - 9) / 2);
        int y = closeRect.y + Math.max(1, (closeRect.height - 9) / 2);

        Color closeBorder = display.getSystemColor(BUTTON_BORDER);
        int[] fillShape = new int[] { x + 1, y + 1, x + 3, y + 1, x + 5, y + 3,
                x + 6, y + 3, x + 8, y + 1, x + 10, y + 1, x + 10, y + 3, x + 8,
                y + 5, x + 8, y + 6, x + 10, y + 8, x + 10, y + 10, x + 8,
                y + 10, x + 6, y + 8, x + 5, y + 8, x + 3, y + 10, x + 1,
                y + 10, x + 1, y + 8, x + 3, y + 6, x + 3, y + 5, x + 1,
                y + 3 };
        int[] drawShape = new int[] { x, y, x + 2, y, x + 4, y + 2, x + 5,
                y + 2, x + 7, y, x + 9, y, x + 9, y + 2, x + 7, y + 4, x + 7,
                y + 5, x + 9, y + 7, x + 9, y + 9, x + 7, y + 9, x + 5, y + 7,
                x + 4, y + 7, x + 2, y + 9, x, y + 9, x, y + 7, x + 2, y + 5,
                x + 2, y + 4, x, y + 2 };
        switch (closeImageState & (SWT.HOT | SWT.SELECTED | SWT.BACKGROUND)) {
        case SWT.NONE: {
            gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
            gc.fillPolygon(fillShape);
            gc.setForeground(closeBorder);
            gc.drawPolygon(drawShape);
            break;
        }
        case SWT.HOT: {
            gc.setBackground(getCloseFillColor());
            gc.fillPolygon(fillShape);
            gc.setForeground(closeBorder);
            gc.drawPolygon(drawShape);
            break;
        }
        case SWT.SELECTED: {
            gc.setBackground(getCloseFillColor());
            gc.fillPolygon(fillShape);
            gc.setForeground(closeBorder);
            gc.drawPolygon(drawShape);
            break;
        }
        case SWT.BACKGROUND: {
            rendererWrapper.drawClose(gc, closeRect, closeImageState);
            break;
        }
        }
    }

    private boolean shouldDrawImage(CTabItem item) {
        Object model = item.getData(AbstractPartRenderer.OWNING_ME);
        if (model != null && model instanceof MUIElement) {
            MUIElement element = (MUIElement) model;
            if (element.getTags().contains(ICathyConstants.TAG_SHOW_IMAGE)) {
                return true;
            }
        }
        return false;
    }

    void drawUnselectedTab(int itemIndex, GC gc, Rectangle bounds, int state) {
        if ((state & SWT.HOT) != 0) {
            int header = shadowEnabled ? 2 : 0;
            int width = bounds.width;
            boolean onBottom = parent.getTabPosition() == SWT.BOTTOM;
            int[] points = new int[1024];
            int[] inactive = new int[8];
            int index = 0, inactive_index = 0;
            int radius = cornerSize / 2;
            int circX = bounds.x + radius;
            int circY = onBottom
                    ? bounds.y + bounds.height + 1 - header - radius
                    : bounds.y - 1 + radius;
            int bottomY = onBottom ? bounds.y - header
                    : bounds.y + bounds.height;

            int leftIndex = circX;
            if (itemIndex == 0) {
//                if (parent.getSelectionIndex() != 0)
//                    leftIndex -= 1;
                points[index++] = leftIndex - radius;
                points[index++] = bottomY;

            } else {
                points[index++] = bounds.x;
                points[index++] = bottomY;
            }

            if (!active) {
                System.arraycopy(points, 0, inactive, 0, index);
                inactive_index += 2;
            }

            int rightIndex = circX - 1;
            int startX = -1, endX = -1;
            if (!onBottom) {
                int[] ltt = drawCircle(leftIndex, circY, radius, LEFT_TOP);
                startX = ltt[6];
                for (int i = 0; i < ltt.length / 2; i += 2) {
                    int tmp = ltt[i];
                    ltt[i] = ltt[ltt.length - i - 2];
                    ltt[ltt.length - i - 2] = tmp;
                    tmp = ltt[i + 1];
                    ltt[i + 1] = ltt[ltt.length - i - 1];
                    ltt[ltt.length - i - 1] = tmp;
                }
                System.arraycopy(ltt, 0, points, index, ltt.length);
                index += ltt.length;

                if (!active) {
                    System.arraycopy(ltt, 0, inactive, inactive_index, 2);
                    inactive_index += 2;
                }

                int[] rt = drawCircle(rightIndex + width - (radius * 2), circY,
                        radius, RIGHT_TOP);
                endX = rt[rt.length - 4];
                for (int i = 0; i < rt.length / 2; i += 2) {
                    int tmp = rt[i];
                    rt[i] = rt[rt.length - i - 2];
                    rt[rt.length - i - 2] = tmp;
                    tmp = rt[i + 1];
                    rt[i + 1] = rt[rt.length - i - 1];
                    rt[rt.length - i - 1] = tmp;
                }
                System.arraycopy(rt, 0, points, index, rt.length);
                index += rt.length;
                if (!active) {
                    System.arraycopy(rt, rt.length - 4, inactive,
                            inactive_index, 2);
                    inactive[inactive_index] -= 1;
                    inactive_index += 2;
                }
            } else {
                int[] ltt = drawCircle(leftIndex, circY, radius, LEFT_BOTTOM);
                startX = ltt[6];
                System.arraycopy(ltt, 0, points, index, ltt.length);
                index += ltt.length;

                if (!active) {
                    System.arraycopy(ltt, 0, inactive, inactive_index, 2);
                    inactive_index += 2;
                }

                int[] rt = drawCircle(rightIndex + width - (radius * 2), circY,
                        radius, RIGHT_BOTTOM);
                endX = rt[rt.length - 4];
                System.arraycopy(rt, 0, points, index, rt.length);
                index += rt.length;
                if (!active) {
                    System.arraycopy(rt, rt.length - 4, inactive,
                            inactive_index, 2);
                    inactive[inactive_index] -= 1;
                    inactive_index += 2;
                }

            }

            points[index++] = bounds.width + rightIndex - radius;
            points[index++] = bottomY;

            if (!active) {
                System.arraycopy(points, index - 2, inactive, inactive_index,
                        2);
                inactive[inactive_index] -= 1;
                inactive_index += 2;
            }

            gc.setClipping(points[0],
                    onBottom ? bounds.y - header : bounds.y - 1,
                    parent.getSize().x - (shadowEnabled ? SIDE_DROP_WIDTH
                            : 0 + INNER_KEYLINE + OUTER_KEYLINE),
                    bounds.y + bounds.height);

            if (hoverTabColors == null) {
                hoverTabColors = new Color[] {
                        gc.getDevice().getSystemColor(SWT.COLOR_WHITE) };
                hoverTabPercents = new int[] { 100 };
            }

            gc.setBackground(hoverTabColors[0]);
            int[] tmpPoints = new int[index];
            System.arraycopy(points, 0, tmpPoints, 0, index);
            gc.fillPolygon(tmpPoints);
            Color tempBorder = new Color(gc.getDevice(), 182, 188, 204);
            gc.setForeground(tempBorder);
            tempBorder.dispose();

            if (hoverBorderVisible) {
                if (outerBorderVisible) {
                    if (outerKeyline == null)
                        outerKeyline = gc.getDevice()
                                .getSystemColor(SWT.COLOR_BLACK);
                    gc.setForeground(outerKeyline);
                    if (active) {
                        gc.drawLine(startX, 1, endX, 1);
                    } else {
                        gc.drawLine(inactive[0], inactive[1], inactive[2],
                                inactive[3]);
                        gc.drawLine(inactive[4], inactive[5], inactive[6],
                                inactive[7]);
                    }
                }

                if (innerBorderVisible) {
                    if (innerKeyline == null)
                        innerKeyline = gc.getDevice()
                                .getSystemColor(SWT.COLOR_BLACK);
                    gc.setForeground(innerKeyline);
                    gc.drawPolyline(tmpPoints);
                }
            }

            Rectangle rect = null;
            gc.setClipping(rect);

            // gc.setForeground(outerKeyline);
            // gc.drawPolyline(shape);
        }
    }

    static int[] drawCircle(int xC, int yC, int r, int circlePart) {
        int x = 0, y = r, u = 1, v = 2 * r - 1, e = 0;
        int[] points = new int[1024];
        int[] pointsMirror = new int[1024];
        int loop = 0;
        int loopMirror = 0;
        if (r == 0) {
            for (int i = 0; i < 4; i++) {
                points[loop++] = xC;
                points[loop++] = yC;
            }
        }
        while (x < y) {
            if (circlePart == RIGHT_BOTTOM) {
                points[loop++] = xC + x;
                points[loop++] = yC + y;
            }
            if (circlePart == RIGHT_TOP) {
                points[loop++] = xC + y;
                points[loop++] = yC - x;
            }
            if (circlePart == LEFT_TOP) {
                points[loop++] = xC - x;
                points[loop++] = yC - y;
            }
            if (circlePart == LEFT_BOTTOM) {
                points[loop++] = xC - y;
                points[loop++] = yC + x;
            }
            x++;
            e += u;
            u += 2;
            if (v < 2 * e) {
                y--;
                e -= v;
                v -= 2;
            }
            if (x > y)
                break;
            if (circlePart == RIGHT_BOTTOM) {
                pointsMirror[loopMirror++] = xC + y;
                pointsMirror[loopMirror++] = yC + x;
            }
            if (circlePart == RIGHT_TOP) {
                pointsMirror[loopMirror++] = xC + x;
                pointsMirror[loopMirror++] = yC - y;
            }
            if (circlePart == LEFT_TOP) {
                pointsMirror[loopMirror++] = xC - y;
                pointsMirror[loopMirror++] = yC - x;
            }
            if (circlePart == LEFT_BOTTOM) {
                pointsMirror[loopMirror++] = xC - x;
                pointsMirror[loopMirror++] = yC + y;
            }
            // grow?
            if ((loop + 1) > points.length) {
                int length = points.length * 2;
                int[] newPointTable = new int[length];
                int[] newPointTableMirror = new int[length];
                System.arraycopy(points, 0, newPointTable, 0, points.length);
                points = newPointTable;
                System.arraycopy(pointsMirror, 0, newPointTableMirror, 0,
                        pointsMirror.length);
                pointsMirror = newPointTableMirror;
            }
        }
        int[] finalArray = new int[loop + loopMirror];
        System.arraycopy(points, 0, finalArray, 0, loop);
        for (int i = loopMirror - 1, j = loop; i > 0; i = i - 2, j = j + 2) {
            int tempY = pointsMirror[i];
            int tempX = pointsMirror[i - 1];
            finalArray[j] = tempX;
            finalArray[j + 1] = tempY;
        }
        return finalArray;
    }

    static RGB blend(RGB c1, RGB c2, int ratio) {
        int r = blend(c1.red, c2.red, ratio);
        int g = blend(c1.green, c2.green, ratio);
        int b = blend(c1.blue, c2.blue, ratio);
        return new RGB(r, g, b);
    }

    static int blend(int v1, int v2, int ratio) {
        int b = (ratio * v1 + (100 - ratio) * v2) / 100;
        return Math.min(255, b);
    }

    void drawShadow(final Display display, Rectangle bounds, GC gc) {
        if (shadowImage == null) {
            createShadow(display);
        }
        int x = bounds.x;
        int y = bounds.y;
        int SIZE = shadowImage.getBounds().width / 3;

        int height = Math.max(bounds.height, SIZE * 2);
        int width = Math.max(bounds.width, SIZE * 2);
        // top left
        gc.drawImage(shadowImage, 0, 0, SIZE, SIZE, 2, 10, SIZE, 20);
        int fillHeight = height - SIZE * 2;
        int fillWidth = width + 5 - SIZE * 2;

        int xFill = 0;
        for (int i = SIZE; i < fillHeight; i += SIZE) {
            xFill = i;
            gc.drawImage(shadowImage, 0, SIZE, SIZE, SIZE, 2, i, SIZE, SIZE);
        }

        // Pad the rest of the shadow
        gc.drawImage(shadowImage, 0, SIZE, SIZE, fillHeight - xFill, 2,
                xFill + SIZE, SIZE, fillHeight - xFill);

        // bl
        gc.drawImage(shadowImage, 0, 40, 20, 20, 2, y + height - SIZE, 20, 20);

        int yFill = 0;
        for (int i = SIZE; i <= fillWidth; i += SIZE) {
            yFill = i;
            gc.drawImage(shadowImage, SIZE, SIZE * 2, SIZE, SIZE, i,
                    y + height - SIZE, SIZE, SIZE);
        }
        // Pad the rest of the shadow
        gc.drawImage(shadowImage, SIZE, SIZE * 2, fillWidth - yFill, SIZE,
                yFill + SIZE, y + height - SIZE, fillWidth - yFill, SIZE);

        // br
        gc.drawImage(shadowImage, SIZE * 2, SIZE * 2, SIZE, SIZE,
                x + width - SIZE - 1, y + height - SIZE, SIZE, SIZE);

        // tr
        gc.drawImage(shadowImage, (SIZE * 2), 0, SIZE, SIZE,
                x + width - SIZE - 1, 10, SIZE, SIZE);

        xFill = 0;
        for (int i = SIZE; i < fillHeight; i += SIZE) {
            xFill = i;
            gc.drawImage(shadowImage, SIZE * 2, SIZE, SIZE, SIZE,
                    x + width - SIZE - 1, i, SIZE, SIZE);
        }

        // Pad the rest of the shadow
        gc.drawImage(shadowImage, SIZE * 2, SIZE, SIZE, fillHeight - xFill,
                x + width - SIZE - 1, xFill + SIZE, SIZE, fillHeight - xFill);
    }

    void createShadow(final Display display) {
        if (shadowImage != null) {
            shadowImage.dispose();
            shadowImage = null;
        }
        ImageData data = new ImageData(60, 60, 32,
                new PaletteData(0xFF0000, 0xFF00, 0xFF));
        Image tmpImage = shadowImage = new Image(display, data);
        GC gc = new GC(tmpImage);
        if (shadowColor == null)
            shadowColor = gc.getDevice().getSystemColor(SWT.COLOR_GRAY);
        gc.setBackground(shadowColor);
        drawTabBody(gc, new Rectangle(0, 0, 60, 60), SWT.None);
        ImageData blured = blur(tmpImage, 5, 25);
        shadowImage = new Image(display, blured);
        tmpImage.dispose();
        gc.dispose();
    }

    public ImageData blur(Image src, int radius, int sigma) {
        float[] kernel = create1DKernel(radius, sigma);

        ImageData imgPixels = src.getImageData();
        int width = imgPixels.width;
        int height = imgPixels.height;

        int[] inPixels = new int[width * height];
        int[] outPixels = new int[width * height];
        int offset = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                RGB rgb = imgPixels.palette.getRGB(imgPixels.getPixel(x, y));
                if (rgb.red == 255 && rgb.green == 255 && rgb.blue == 255) {
                    inPixels[offset] = (rgb.red << 16) | (rgb.green << 8)
                            | rgb.blue;
                } else {
                    inPixels[offset] = (imgPixels.getAlpha(x, y) << 24)
                            | (rgb.red << 16) | (rgb.green << 8) | rgb.blue;
                }
                offset++;
            }
        }

        convolve(kernel, inPixels, outPixels, width, height, true);
        convolve(kernel, outPixels, inPixels, height, width, true);

        ImageData dst = new ImageData(imgPixels.width, imgPixels.height, 24,
                new PaletteData(0xff0000, 0xff00, 0xff));

        dst.setPixels(0, 0, inPixels.length, inPixels, 0);
        offset = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (inPixels[offset] == -1) {
                    dst.setAlpha(x, y, 0);
                } else {
                    int a = (inPixels[offset] >> 24) & 0xff;
                    // if (a < 150) a = 0;
                    dst.setAlpha(x, y, a);
                }
                offset++;
            }
        }
        return dst;
    }

    private void convolve(float[] kernel, int[] inPixels, int[] outPixels,
            int width, int height, boolean alpha) {
        int kernelWidth = kernel.length;
        int kernelMid = kernelWidth / 2;
        for (int y = 0; y < height; y++) {
            int index = y;
            int currentLine = y * width;
            for (int x = 0; x < width; x++) {
                // do point
                float a = 0, r = 0, g = 0, b = 0;
                for (int k = -kernelMid; k <= kernelMid; k++) {
                    float val = kernel[k + kernelMid];
                    int xcoord = x + k;
                    if (xcoord < 0)
                        xcoord = 0;
                    if (xcoord >= width)
                        xcoord = width - 1;
                    int pixel = inPixels[currentLine + xcoord];
                    // float alp = ((pixel >> 24) & 0xff);
                    a += val * ((pixel >> 24) & 0xff);
                    r += val * (((pixel >> 16) & 0xff));
                    g += val * (((pixel >> 8) & 0xff));
                    b += val * (((pixel) & 0xff));
                }
                int ia = alpha ? clamp((int) (a + 0.5)) : 0xff;
                int ir = clamp((int) (r + 0.5));
                int ig = clamp((int) (g + 0.5));
                int ib = clamp((int) (b + 0.5));
                outPixels[index] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
                index += height;
            }
        }

    }

    private int clamp(int value) {
        if (value > 255)
            return 255;
        if (value < 0)
            return 0;
        return value;
    }

    private float[] create1DKernel(int radius, int sigma) {
        // guideline: 3*sigma should be the radius
        int size = radius * 2 + 1;
        float[] kernel = new float[size];
        int radiusSquare = radius * radius;
        float sigmaSquare = 2 * sigma * sigma;
        float piSigma = 2 * (float) Math.PI * sigma;
        float sqrtSigmaPi2 = (float) Math.sqrt(piSigma);
        int start = size / 2;
        int index = 0;
        float total = 0;
        for (int i = -start; i <= start; i++) {
            float d = i * i;
            if (d > radiusSquare) {
                kernel[index] = 0;
            } else {
                kernel[index] = (float) Math.exp(-(d) / sigmaSquare)
                        / sqrtSigmaPi2;
            }
            total += kernel[index];
            index++;
        }
        for (int i = 0; i < size; i++) {
            kernel[i] /= total;
        }
        return kernel;
    }

    public Rectangle getPadding() {
        return new Rectangle(paddingTop, paddingRight, paddingBottom,
                paddingLeft);
    }

    public void setPadding(int paddingLeft, int paddingRight, int paddingTop,
            int paddingBottom) {
        this.paddingLeft = paddingLeft;
        this.paddingRight = paddingRight;
        this.paddingTop = paddingTop;
        this.paddingBottom = paddingBottom;
        parent.redraw();
    }

    public void setCornerRadius(int radius) {
        cornerSize = radius;
        parent.redraw();
    }

    public void setShadowVisible(boolean visible) {
        this.shadowEnabled = visible;
        parent.redraw();
    }

    public void setShadowColor(Color color) {
        this.shadowColor = color;
        createShadow(parent.getDisplay());
        parent.redraw();
    }

    public void setOuterKeyline(Color color) {
        this.outerKeyline = color;
        // TODO: HACK! Should be set based on pseudo-state.
        if (color != null) {
            setActive(!(color.getRed() == 255 && color.getGreen() == 255
                    && color.getBlue() == 255));
        }
        parent.redraw();
    }

    public void setSelectedTabFill(Color color) {
        setSelectedTabFill(new Color[] { color }, new int[] { 100 });
    }

    public void setSelectedTabFill(Color[] colors, int[] percents) {
        selectedTabFillColors = colors;
        selectedTabFillPercents = percents;
        parent.redraw();
    }

    public void setSelectedTabAreaColor(Color color) {
        setSelectedTabAreaColor(new Color[] { color }, new int[] { 100 });
    }

    public void setSelectedTabAreaColor(Color[] colors, int[] percents) {
        selectedTabAreaColors = colors;
        selectedTabAreaPercents = percents;
        parent.redraw();
    }

    public void setUnselectedTabsColor(Color color) {
        setUnselectedTabsColor(new Color[] { color }, new int[] { 100 });
    }

    public void setUnselectedTabsColor(Color[] colors, int[] percents) {
        unselectedTabsColors = colors;
        unselectedTabsPercents = percents;

        parent.redraw();
    }

    public void setUnselectedTabsBackgroundVisible(boolean visible) {
        unselectedTabsBackgroundVisible = visible;
        parent.redraw();
    }

    public void setUnselectedHotTabsColorBackground(Color color) {
        setHoverTabColor(new Color[] { color }, new int[] { 100 });
    }

    public void setHoverTabColor(Color color) {
        setHoverTabColor(new Color[] { color }, new int[] { 100 });

    }

    public void setHoverTabColor(Color[] colors, int[] percents) {
        hoverTabColors = colors;
        hoverTabPercents = percents;
        parent.redraw();
    }

    public void setTabOutline(Color color) {
        this.tabOutlineColor = color;
        parent.redraw();
    }

    public void setInnerKeyline(Color color) {
        this.innerKeyline = color;
        parent.redraw();
    }

    public void setTextVisible(boolean visible) {
        this.textVisible = visible;
        parent.redraw();
    }

    public void setImageVisible(boolean visible) {
        this.imageVisible = visible;
        parent.redraw();
    }

    public void setOuterBorderVisible(boolean visible) {
        this.outerBorderVisible = visible;
        parent.redraw();
    }

    public void setInnerBorderVisible(boolean visible) {
        this.innerBorderVisible = visible;
        parent.redraw();
    }

    public void setActiveToolbarGradient(Color[] color, int[] percents) {
        activeToolbar = color;
        activePercents = percents;
    }

    public void setInactiveToolbarGradient(Color[] color, int[] percents) {
        inactiveToolbar = color;
        inactivePercents = percents;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setMaximizeImage(Image maxImage) {
        this.maxImage = maxImage;
    }

    public void setMinimizeImage(Image minImage) {
        this.minImage = minImage;
    }

    public void setCloseImage(Image closeImage) {
        this.closeImage = closeImage;
    }

    public void setClsoeHoverImage(Image closeHoverImage) {
        this.closeHoverImage = closeHoverImage;
    }

    public void setNoneRender(boolean nothingToRender) {
        this.nothingToRender = nothingToRender;
    }

    private void drawCustomBackground(GC gc, Rectangle bounds, int state) {
        boolean selected = (state & SWT.SELECTED) != 0;
        Color defaultBackground = selected ? parent.getSelectionBackground()
                : parent.getBackground();
        boolean vertical = selected
                ? parentWrapper.isSelectionGradientVertical()
                : parentWrapper.isGradientVertical();
        Rectangle partHeaderBounds = bounds;

        if (unselectedTabsBackgroundVisible) {
            drawUnselectedTabBackground(gc, partHeaderBounds, state, vertical,
                    defaultBackground);
        }
        drawTabAreaBackground(gc, partHeaderBounds, state, vertical,
                defaultBackground);

        int borderTop = isTabOnBottom() ? INNER_KEYLINE + OUTER_KEYLINE
                : TOP_KEYLINE + OUTER_KEYLINE;
        int borderBottom = isTabOnBottom() ? TOP_KEYLINE + OUTER_KEYLINE
                : INNER_KEYLINE + OUTER_KEYLINE;
        int bottomDropWidth = shadowEnabled ? BOTTOM_DROP_WIDTH : 0;
        int sideDropWidth = shadowEnabled ? SIDE_DROP_WIDTH : 0;
        int headerBorderBottom = outerBorderVisible ? OUTER_KEYLINE : 0;
        Rectangle underTabAreaBounds = new Rectangle(
                partHeaderBounds.x + paddingLeft + sideDropWidth,
                partHeaderBounds.y + partHeaderBounds.height + bottomDropWidth
                        + paddingTop + headerBorderBottom,
                bounds.width - paddingLeft - paddingRight - 2 * sideDropWidth,
                parent.getBounds().height - partHeaderBounds.height - paddingTop
                        - paddingBottom - headerBorderBottom
                        - ((cornerSize / 4) + borderBottom + borderTop)
                        - bottomDropWidth * 2);
        drawUnderTabAreaBackground(gc, underTabAreaBounds, state, vertical,
                defaultBackground);
        drawChildrenBackground(partHeaderBounds);
    }

    private void drawUnderTabAreaBackground(GC gc, Rectangle tabAreaBounds,
            int state, boolean vertical, Color defaultBackground) {
        Color[] underTabAreaColors = new Color[] {
                gc.getDevice().getSystemColor(SWT.COLOR_WHITE) };
        int[] underTabAreaPercents = new int[] { 100 };
        rendererWrapper.drawBackground(gc, tabAreaBounds.x, tabAreaBounds.y,
                tabAreaBounds.width, tabAreaBounds.height, defaultBackground,
                underTabAreaColors, underTabAreaPercents, vertical);
    }

    private void drawUnselectedTabBackground(GC gc, Rectangle partHeaderBounds,
            int state, boolean vertical, Color defaultBackground) {
        if (unselectedTabsColors == null) {
            boolean selected = (state & SWT.SELECTED) != 0;
            unselectedTabsColors = selected
                    ? parentWrapper.getSelectionGradientColors()
                    : parentWrapper.getGradientColors();
            unselectedTabsPercents = selected
                    ? parentWrapper.getSelectionGradientPercents()
                    : parentWrapper.getGradientPercents();
        }
        if (unselectedTabsColors == null) {
            unselectedTabsColors = new Color[] {
                    gc.getDevice().getSystemColor(SWT.COLOR_WHITE) };
            unselectedTabsPercents = new int[] { 100 };
        }

        rendererWrapper.drawBackground(gc, partHeaderBounds.x,
                partHeaderBounds.y, partHeaderBounds.width,
                partHeaderBounds.height, defaultBackground,
                unselectedTabsColors, unselectedTabsPercents, vertical);
    }

    private void drawTabAreaBackground(GC gc, Rectangle partHeaderBounds,
            int state, boolean vertical, Color defaultBackground) {
        Color[] colors = selectedTabAreaColors;
        int[] percents = selectedTabAreaPercents;

        if (colors != null && colors.length == 2) {
            colors = new Color[] { colors[1], colors[1] };
        }
        if (colors == null) {
            boolean selected = (state & SWT.SELECTED) != 0;
            colors = selected ? parentWrapper.getSelectionGradientColors()
                    : parentWrapper.getGradientColors();
            percents = selected ? parentWrapper.getSelectionGradientPercents()
                    : parentWrapper.getGradientPercents();
        }
        if (colors == null) {
            colors = new Color[] {
                    gc.getDevice().getSystemColor(SWT.COLOR_WHITE) };
            percents = new int[] { 100 };
        }

        rendererWrapper.drawBackground(gc, partHeaderBounds.x,
                partHeaderBounds.y + partHeaderBounds.height,
                partHeaderBounds.width, parent.getBounds().height,
                defaultBackground, colors, percents, vertical);
    }

    // Workaround for the bug 433276. Remove it when the bug gets fixed
    private void drawChildrenBackground(Rectangle partHeaderBounds) {
        for (Control control : parent.getChildren()) {
            if (!CompositeElement.hasBackgroundOverriddenByCSS(control)
                    && containsToolbar(control)) {
                drawChildBackground((Composite) control, partHeaderBounds);
            }
        }
    }

    private boolean containsToolbar(Control control) {
        if (control.getData(CONTAINS_TOOLBAR) != null) {
            return true;
        }

        if (control instanceof ToolBar) {
            control.setData(CONTAINS_TOOLBAR, true);
            return true;
        }

        if (control instanceof Composite) {
            for (Control child : ((Composite) control).getChildren()) {
                if (child instanceof ToolBar) {
                    control.setData(CONTAINS_TOOLBAR, true);
                    return true;
                }
            }
        }
        return false;
    }

    private void drawChildBackground(Composite composite,
            Rectangle partHeaderBounds) {
        Rectangle rec = composite.getBounds();
        Color background = null;
        boolean partOfHeader = rec.y >= partHeaderBounds.y
                && rec.y < partHeaderBounds.height;

        if (!partOfHeader) {
            background = composite.getDisplay().getSystemColor(SWT.COLOR_WHITE);
        }
        CTabFolderElement.setBackgroundOverriddenDuringRenderering(composite,
                background);
    }

    private static class CTabFolderRendererWrapper
            extends ReflectionSupport<CTabFolderRenderer> {
        private Method drawBackgroundMethod;

        private Method drawCloseMethod;

        public CTabFolderRendererWrapper(CTabFolderRenderer instance) {
            super(instance);
        }

        public void drawBackground(GC gc, int x, int y, int width, int height,
                Color defaultBackground, Color[] colors, int[] percents,
                boolean vertical) {
            if (drawBackgroundMethod == null) {
                drawBackgroundMethod = getMethod("drawBackground", //$NON-NLS-1$
                        new Class<?>[] { GC.class, int[].class, int.class,
                                int.class, int.class, int.class, Color.class,
                                Image.class, Color[].class, int[].class,
                                boolean.class });
            }
            executeMethod(drawBackgroundMethod,
                    new Object[] { gc, null, x, y, width, height,
                            defaultBackground, null, colors, percents,
                            vertical });
        }

        public void drawClose(GC gc, Rectangle closeRect, int closeImageState) {
            if (drawCloseMethod == null) {
                drawCloseMethod = getMethod("drawClose", new Class<?>[] { //$NON-NLS-1$
                        GC.class, Rectangle.class, int.class });
            }
            executeMethod(drawCloseMethod,
                    new Object[] { gc, closeRect, closeImageState });
        }
    }

    private static class CTabItemWrapper extends ReflectionSupport<CTabItem> {

        private Field shortenedTextField;

        private Field shortenedTextWidthField;

        private Field closeRectField;

        private Field closeImageStateField;

        public CTabItemWrapper(CTabItem instance) {
            super(instance);
        }

        public String getShortenedText() {
            if (shortenedTextField == null) {
                shortenedTextField = getField("shortenedText"); //$NON-NLS-1$
            }
            return (String) getFieldValue(shortenedTextField);
        }

        public void setShortenedText(String value) {
            set("shortenedText", value); //$NON-NLS-1$
        }

        public Integer getShortenedTextWidth() {
            if (shortenedTextWidthField == null) {
                shortenedTextWidthField = getField("shortenedTextWidth"); //$NON-NLS-1$
            }
            return (Integer) getFieldValue(shortenedTextWidthField);
        }

        public void setShortenedTextWidth(int value) {
            set("shortenedTextWidth", value); //$NON-NLS-1$
        }

        public Rectangle getCloseRect() {
            if (closeRectField == null) {
                closeRectField = getField("closeRect"); //$NON-NLS-1$
            }
            return (Rectangle) getFieldValue(closeRectField);
        }

        public int getCloseImageState() {
            if (closeImageStateField == null) {
                closeImageStateField = getField("closeImageState"); //$NON-NLS-1$
            }
            return (Integer) getFieldValue(closeImageStateField);
        }

    }

    private static class CTabFolderWrapper
            extends ReflectionSupport<CTabFolder> {
        private Field selectionGradientVerticalField;

        private Field gradientVerticalField;

        private Field selectionGradientColorsField;

        private Field selectionGradientPercentsField;

        private Field gradientColorsField;

        private Field gradientPercentsField;

        private Field showCloseField;

        private Field fixedTabHeightField;

        private Method getRightItemEdgeMethod;

        public CTabFolderWrapper(CTabFolder instance) {
            super(instance);
        }

        public boolean isShowClose() {
            if (showCloseField == null) {
                showCloseField = getField("showClose"); //$NON-NLS-1$
            }
            Boolean result = (Boolean) getFieldValue(showCloseField);
            return result != null ? result : true;
        }

        public boolean isSelectionGradientVertical() {
            if (selectionGradientVerticalField == null) {
                selectionGradientVerticalField = getField(
                        "selectionGradientVertical"); //$NON-NLS-1$
            }
            Boolean result = (Boolean) getFieldValue(
                    selectionGradientVerticalField);
            return result != null ? result : true;
        }

        public boolean isGradientVertical() {
            if (gradientVerticalField == null) {
                gradientVerticalField = getField("gradientVertical"); //$NON-NLS-1$
            }
            Boolean result = (Boolean) getFieldValue(gradientVerticalField);
            return result != null ? result : true;
        }

        public Color[] getSelectionGradientColors() {
            if (selectionGradientColorsField == null) {
                selectionGradientColorsField = getField(
                        "selectionGradientColorsField"); //$NON-NLS-1$
            }
            return (Color[]) getFieldValue(selectionGradientColorsField);
        }

        public int[] getSelectionGradientPercents() {
            if (selectionGradientPercentsField == null) {
                selectionGradientPercentsField = getField(
                        "selectionGradientPercents"); //$NON-NLS-1$
            }
            return (int[]) getFieldValue(selectionGradientPercentsField);
        }

        public Color[] getGradientColors() {
            if (gradientColorsField == null) {
                gradientColorsField = getField("gradientColors"); //$NON-NLS-1$
            }
            return (Color[]) getFieldValue(gradientColorsField);
        }

        public int[] getGradientPercents() {
            if (gradientPercentsField == null) {
                gradientPercentsField = getField("gradientPercents"); //$NON-NLS-1$
            }
            return (int[]) getFieldValue(gradientPercentsField);
        }

        public int getRightItemEdge(GC gc) {
            if (getRightItemEdgeMethod == null) {
                getRightItemEdgeMethod = getMethod("getRightItemEdge", //$NON-NLS-1$
                        new Class<?>[] { GC.class });
            }
            return (Integer) executeMethod(getRightItemEdgeMethod,
                    new Object[] { gc });
        }

        public int getFixedTabHeight() {
            if (fixedTabHeightField == null) {
                fixedTabHeightField = getField("fixedTabHeight"); //$NON-NLS-1$
            }
            return (Integer) getFieldValue(fixedTabHeightField);
        }
    }

    private static class ReflectionSupport<T> {
        private T instance;

        public ReflectionSupport(T instance) {
            this.instance = instance;
        }

        protected Object getFieldValue(Field field) {
            Object value = null;
            if (field != null) {
                boolean accessible = field.isAccessible();
                try {
                    field.setAccessible(true);
                    value = field.get(instance);
                } catch (Exception exc) {
                    // do nothing
                } finally {
                    field.setAccessible(accessible);
                }
            }
            return value;
        }

        protected Field getField(String name) {
            Class<?> cls = instance.getClass();
            while (!cls.equals(Object.class)) {
                try {
                    return cls.getDeclaredField(name);
                } catch (Exception exc) {
                    cls = cls.getSuperclass();
                }
            }
            return null;
        }

        public Object set(String name, Object value) {
            try {
                Field field = getField(name);
                boolean accessible = field.isAccessible();
                field.setAccessible(true);
                field.set(instance, value);
                field.setAccessible(accessible);
                return value;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        protected Object executeMethod(Method method, Object... params) {
            Object value = null;
            if (method != null) {
                boolean accessible = method.isAccessible();
                try {
                    method.setAccessible(true);
                    value = method.invoke(instance, params);
                } catch (Exception exc) {
                    // do nothing
                } finally {
                    method.setAccessible(accessible);
                }
            }
            return value;
        }

        protected Method getMethod(String name, Class<?>... params) {
            Class<?> cls = instance.getClass();
            while (!cls.equals(Object.class)) {
                try {
                    return cls.getDeclaredMethod(name, params);
                } catch (Exception exc) {
                    cls = cls.getSuperclass();
                }
            }
            return null;
        }
    }

}
