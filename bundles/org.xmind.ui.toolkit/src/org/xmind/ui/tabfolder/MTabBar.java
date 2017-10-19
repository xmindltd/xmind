package org.xmind.ui.tabfolder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.util.IStyleProvider;
import org.xmind.ui.util.StyleProvider;

/**
 * <dl>
 * <dt>Styles</dt>
 * <dd>(none)</dd>
 * <dt>Style Keys:</dt>
 * <dd>MARGIN, BORDER, PADDING, SEPARATOR, CORNER</dd>
 * </dl>
 * 
 * @author Frank Shaka
 * @since 3.6.0
 */
public class MTabBar extends Composite {

    public static final String MARGIN = IStyleProvider.MARGIN;
    public static final String BORDER = IStyleProvider.BORDER;
    public static final String PADDING = IStyleProvider.PADDING;
    public static final String SEPARATOR = IStyleProvider.SEPARATOR;
    public static final String CORNER = IStyleProvider.CORNER;

    private class MTabBarLayout extends Layout {

        @Override
        protected Point computeSize(Composite composite, int wHint, int hHint,
                boolean flushCache) {
            return computeTabBarSize(wHint, hHint);
        }

        @Override
        protected void layout(Composite composite, boolean flushCache) {
            layoutTabBar();
        }

    }

    private List<MTabBarItem> items;
    private int selectedIndex = -1;

    private boolean vertical = false;

    private IStyleProvider styleProvider = new StyleProvider();
    private boolean usingDefaultStyles = true;

    private MTabBarItem trackedItem = null;

    // Layout caches, validated by layout, used by paint
    private int marginWidth = 0;
    private int marginHeight = 0;
    private int borderWidth = 0;
    private int paddingWidth = 0;
    private int paddingHeight = 0;
    private int hSpacing = 0;
    private int vSpacing = 0;
    private int cornerWidth = 0;
    private int cornerHeight = 0;
    private Rectangle[] separators = null;

    private Listener listener;
    private boolean inDispose;

    private ResourceManager resources;

    public MTabBar(Composite parent, int style) {
        super(parent, style | SWT.DOUBLE_BUFFERED);

        this.resources = new LocalResourceManager(JFaceResources.getResources(),
                parent);

        this.items = new ArrayList<MTabBarItem>();

        super.setLayout(new MTabBarLayout());

        listener = new Listener() {

            public void handleEvent(Event event) {
                switch (event.type) {
                case SWT.Dispose:
                    onDispose(event);
                    break;
                case SWT.Resize:
                    onResize(event);
                    break;

                case SWT.Paint:
                    onPaint(event);
                    break;

                case SWT.MouseEnter:
                    onMouseEnter(event);
                    break;

                case SWT.MouseExit:
                    onMouseExit(event);
                    break;

                case SWT.MouseMove:
                    onMouseMove(event);
                    break;

                case SWT.MouseDown:
                    onMouseDown(event);
                    break;

                case SWT.MouseUp:
                    onMouseUp(event);
                    break;

                }
            }

        };
        addListener(SWT.Dispose, listener);
        addListener(SWT.Resize, listener);
        addListener(SWT.Paint, listener);
        addListener(SWT.MouseEnter, listener);
        addListener(SWT.MouseExit, listener);
        addListener(SWT.MouseMove, listener);
        addListener(SWT.MouseDown, listener);
        addListener(SWT.MouseUp, listener);

    }

    @Override
    public void setLayout(Layout layout) {
        checkWidget();
        // prevents layout from being changed by clients
    }

    public MTabBarItem[] getItems() {
        checkWidget();
        return items.toArray(new MTabBarItem[items.size()]);
    }

    public int getItemCount() {
        checkWidget();
        return items.size();
    }

    public MTabBarItem getSelection() {
        checkWidget();
        return selectedIndex < 0 ? null : items.get(selectedIndex);
    }

    public int getSelectionIndex() {
        checkWidget();
        return selectedIndex;
    }

    public int indexOf(MTabBarItem item) {
        checkWidget();
        if (item == null)
            return -1;
        return items.indexOf(item);
    }

    public MTabBarItem getItem(int index) {
        checkWidget();
        return index < 0 || index >= items.size() ? null : items.get(index);
    }

    public MTabBarItem getItem(Point pt) {
        checkWidget();
        return getItem(pt.x, pt.y);
    }

    private MTabBarItem getItem(int x, int y) {
        for (MTabBarItem item : items) {
            if (item.getVisible() && item.getBounds().contains(x, y))
                return item;
        }
        return null;
    }

    public void setSelection(MTabBarItem item) {
        checkWidget();
        if (item != null && !item.isRadioButton())
            return;

        MTabBarItem oldItem = getSelection();
        selectedIndex = indexOf(item);
        if (oldItem != null) {
            oldItem.setSelected(false);
        }
        if (item != null) {
            item.setSelected(true);
        }
    }

    public boolean isVertical() {
        checkWidget();
        return vertical;
    }

    public void setVertical(boolean vertical) {
        checkWidget();

        if (vertical == this.vertical)
            return;

        this.vertical = vertical;
        updateTabBar();
    }

    public IStyleProvider getStyleProvider() {
        checkWidget();
        return styleProvider;
    }

    public void setStyleProvider(IStyleProvider styleProvider) {
        checkWidget();
        IStyleProvider oldStyleProvider = usingDefaultStyles ? null
                : this.styleProvider;
        if (styleProvider == oldStyleProvider)
            return;
        if (styleProvider != null) {
            this.styleProvider = styleProvider;
            usingDefaultStyles = false;
        } else {
            this.styleProvider = new StyleProvider();
            usingDefaultStyles = true;
        }

        reskin(SWT.NONE);
        pack(false);

        updateTabBar();
    }

    protected void createItem(MTabBarItem item, int index) {
        items.add(index, item);

        if (item.isRadioButton()) {
            boolean isPrimary = true;
            for (MTabBarItem it : items) {
                if (it != item && it.isRadioButton()) {
                    isPrimary = false;
                    break;
                }
            }
            if (isPrimary) {
                selectedIndex = items.indexOf(item);
                item.setSelected(true);
            }
        }

        layout(true);
        redraw();
    }

    protected void destroyItem(MTabBarItem item) {
        if (inDispose)
            return;

        int index = indexOf(item);
        if (index < 0) {
            return;
        }

        if (index == selectedIndex) {
            int nextIndex = -1;
            MTabBarItem next = null;
            for (int j = index + 1; j < items.size(); j++) {
                next = items.get(j);
                if (next.isRadioButton()) {
                    nextIndex = j;
                    break;
                }
            }
            if (nextIndex < 0) {
                for (int j = index - 1; j >= 0; j--) {
                    next = items.get(j);
                    if (next.isRadioButton()) {
                        nextIndex = j;
                        break;
                    }
                }
            }
            if (nextIndex >= 0) {
                next.setSelected(true);
            }
            selectedIndex = nextIndex;
        }
        items.remove(index);

        layout(true);
        redraw();
    }

    protected void updateItem(MTabBarItem item) {
        layout(true);
        redraw();
    }

    protected Point computeTabBarSize(int wHint, int hHint) {
        Point size;
        if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT) {
            size = new Point(wHint, hHint);
        } else {
            boolean tabBarVertical = isVertical();
            IStyleProvider styles = getStyleProvider();
            int marginWidth = styles.getWidth(this, MARGIN, 0);
            int marginHeight = styles.getHeight(this, MARGIN, 0);
            int borderWidth = Math.max(styles.getWidth(this, BORDER, 0),
                    styles.getHeight(this, BORDER, 0));
            int paddingWidth = styles.getWidth(this, PADDING, 0);
            int paddingHeight = styles.getHeight(this, PADDING, 0);

            int trimWidth = marginWidth + borderWidth + paddingWidth
                    + marginWidth + borderWidth + paddingWidth;
            int trimHeight = marginHeight + borderWidth + paddingHeight
                    + marginHeight + borderWidth + paddingHeight;

            if (wHint != SWT.DEFAULT)
                wHint = Math.max(0, wHint - trimWidth);
            if (hHint != SWT.DEFAULT)
                hHint = Math.max(0, hHint - trimHeight);

            int hSpacing = styles.getWidth(this, SEPARATOR, 0);
            int vSpacing = styles.getHeight(this, SEPARATOR, 0);

            int itemCount = getItemCount();
            int width = 0, height = 0;
            MTabBarItem item;
            Point itemSize;
            for (int i = 0; i < itemCount; i++) {
                item = getItem(i);
                if (!item.getVisible())
                    continue;
                itemSize = computeItemSize(item, styles, tabBarVertical,
                        tabBarVertical ? wHint : SWT.DEFAULT,
                        tabBarVertical ? SWT.DEFAULT : hHint, false);
                if (tabBarVertical) {
                    if (i > 0)
                        height += vSpacing;
                    width = Math.max(width, itemSize.x);
                    height += itemSize.y;
                } else {
                    if (i > 0)
                        width += hSpacing;
                    width += itemSize.x;
                    height = Math.max(height, itemSize.y);
                }
            }
            size = new Point(width + trimWidth, height + trimHeight);
        }
        Rectangle trimmed = computeTrim(0, 0, size.x, size.y);

        return new Point(trimmed.width, trimmed.height);
    }

    private Point computeItemSize(MTabBarItem item, IStyleProvider styles,
            boolean tabBarVertical, int wHint, int hHint, boolean cache) {
        boolean separator = item.isSeparator();
        Image image = item.getImage();
        String text = item.getText();

        boolean imageVisible = !separator && image != null
                && styles.getVisibility(item, MTabBarItem.IMAGE, true);
        boolean textVisible = !separator && text != null && !"".equals(text) //$NON-NLS-1$
                && styles.getVisibility(item, MTabBarItem.TEXT, true);
        int textPosition = styles.getPosition(item, MTabBarItem.TEXT,
                SWT.BOTTOM);
        boolean itemVertical = (textPosition & (SWT.TOP | SWT.BOTTOM)) != 0;

        int marginWidth = styles.getWidth(item, MTabBarItem.MARGIN, 0);
        int marginHeight = styles.getHeight(item, MTabBarItem.MARGIN, 0);
        int hSpacing = styles.getWidth(item, MTabBarItem.SEPARATOR, 0);
        int vSpacing = styles.getHeight(item, MTabBarItem.SEPARATOR, 0);

        Font font = styles.getFont(item, MTabBarItem.TEXT);
        if (font == null)
            font = getFont();

        if (cache) {
            item.marginWidth = marginWidth;
            item.marginHeight = marginHeight;
            item.hSpacing = hSpacing;
            item.vSpacing = vSpacing;
            item.textPosition = textPosition;
            item.imageVisible = imageVisible;
            item.textVisible = textVisible;
            item.font = font;
        }

        wHint = styles.getWidth(item, null, itemVertical ? wHint : SWT.DEFAULT);
        hHint = styles.getHeight(item, null,
                itemVertical ? SWT.DEFAULT : hHint);

        if (separator) {
            int separatorWidth = item.getWidth();
            if (separatorWidth == SWT.SEPARATOR_FILL)
                return new Point(0, 0);
            return tabBarVertical ? new Point(wHint, separatorWidth)
                    : new Point(separatorWidth, hHint);
        }

        /// Set width or height with the given value
        int width = item.getWidth();
        if (width > 0 && width != MTabBarItem.DEFAULT_SEPARATOR_WIDTH) {
            if (itemVertical) {
                hHint = width;
            }
        }
        if (wHint >= 0)
            wHint = Math.max(0, wHint - marginWidth - marginWidth);
        if (hHint >= 0)
            hHint = Math.max(0, hHint - marginHeight - marginHeight);

        int imageWHint = styles.getWidth(item, MTabBarItem.IMAGE,
                itemVertical ? wHint : SWT.DEFAULT);
        int imageHHint = styles.getHeight(item, MTabBarItem.IMAGE,
                itemVertical ? SWT.DEFAULT : hHint);
        int textWHint = styles.getWidth(item, MTabBarItem.TEXT,
                itemVertical ? wHint : SWT.DEFAULT);
        int textHHint = styles.getHeight(item, MTabBarItem.TEXT,
                itemVertical ? SWT.DEFAULT : hHint);

        Point imageSize = imageVisible ? computeImageSize(item.getImage())
                : new Point(0, 0);
        Point textSize = textVisible ? computeTextSize(item.getText(), font)
                : new Point(0, 0);

        Point imageAreaSize = imageVisible ? computePreferredSize(imageSize.x,
                imageSize.y, imageWHint, imageHHint) : new Point(0, 0);
        Point textAreaSize = textVisible ? computePreferredSize(textSize.x,
                textSize.y, textWHint, textHHint) : new Point(0, 0);

        int itemWidth, itemHeight;
        if (itemVertical) {
            itemWidth = wHint < 0 ? Math.max(imageAreaSize.x, textAreaSize.x)
                    : wHint;
            if (hHint < 0) {
                if (imageVisible && textVisible) {
                    itemHeight = imageAreaSize.y + vSpacing + textAreaSize.y;
                } else if (imageVisible) {
                    itemHeight = imageAreaSize.y;
                } else if (textVisible) {
                    itemHeight = textAreaSize.y;
                } else {
                    itemHeight = 0;
                }
            } else {
                itemHeight = hHint;
            }
        } else {
            if (wHint < 0) {
                if (imageVisible && textVisible) {
                    itemWidth = imageAreaSize.x + hSpacing + textAreaSize.x;
                } else if (imageVisible) {
                    itemWidth = imageAreaSize.x;
                } else if (textVisible) {
                    itemWidth = textAreaSize.x;
                } else {
                    itemWidth = 0;
                }
            } else {
                itemWidth = wHint;
            }
            itemHeight = hHint < 0 ? Math.max(imageAreaSize.y, textAreaSize.y)
                    : hHint;
        }

        return new Point(itemWidth + marginWidth + marginWidth,
                itemHeight + marginHeight + marginHeight);
    }

    private Point computeImageSize(Image image) {
        if (image == null)
            return new Point(0, 0);
        Rectangle b = image.getBounds();
        return new Point(b.width, b.height);
    }

    private Point computeTextSize(String text, Font font) {
        if (text == null || "".equals(text)) //$NON-NLS-1$
            return new Point(0, 0);
        Point textSize;
        GC gc = new GC(this);
        try {
            gc.setFont(font);
            textSize = gc.textExtent(text);
        } finally {
            gc.dispose();
        }
        return textSize;
    }

    private static Point computePreferredSize(int srcWidth, int srcHeight,
            int wHint, int hHint) {
        if (wHint == 0 || hHint == 0)
            return new Point(0, 0);
        if (wHint > 0 && hHint > 0)
            return new Point(wHint, hHint);
        if (srcWidth == 0 || srcHeight == 0)
            return new Point(0, 0);
        if (wHint < 0 && hHint < 0)
            return new Point(srcWidth, srcHeight);
        if (wHint < 0) {
            if (srcHeight <= hHint)
                return new Point(srcWidth, hHint);
            int width = srcWidth * hHint / srcHeight;
            if (width <= 0)
                return new Point(0, 0);
            return new Point(width, hHint);
        }
        // if (hHint < 0)
        if (srcWidth <= wHint)
            return new Point(wHint, srcHeight);
        int height = srcHeight * wHint / srcWidth;
        if (height <= 0)
            return new Point(0, 0);
        return new Point(wHint, height);
    }

    private static Point computeConstrainedSize(int srcWidth, int srcHeight,
            int wHint, int hHint) {
        float scaleX = (wHint < 0 || srcWidth <= wHint) ? 1
                : ((float) wHint) / srcWidth;
        float scaleY = (hHint < 0 || srcHeight <= hHint) ? 1
                : ((float) hHint) / srcHeight;
        float scale = Math.min(scaleX, scaleY);
        int destWidth = (int) Math.ceil(srcWidth * scale);
        int destHeight = (int) Math.ceil(srcHeight * scale);
        return new Point(destWidth, destHeight);
    }

    protected void layoutTabBar() {
        IStyleProvider styles = getStyleProvider();

        marginWidth = styles.getWidth(this, MARGIN, 0);
        marginHeight = styles.getHeight(this, MARGIN, 0);
        borderWidth = Math.max(styles.getWidth(this, BORDER, 0),
                styles.getHeight(this, BORDER, 0));
        paddingWidth = styles.getWidth(this, PADDING, 0);
        paddingHeight = styles.getHeight(this, PADDING, 0);
        hSpacing = styles.getWidth(this, SEPARATOR, 0);
        vSpacing = styles.getHeight(this, SEPARATOR, 0);
        cornerWidth = styles.getWidth(this, CORNER, 0);
        cornerHeight = styles.getHeight(this, CORNER, 0);

        layoutItems(styles);
        layoutChildren(styles);
    }

    protected void layoutChildren(IStyleProvider styles) {
        // to be implemented by subclasses
    }

    protected void layoutItems(IStyleProvider styles) {
        int itemCount = getItemCount();
        if (itemCount == 0) {
            separators = null;
            return;
        }

        Rectangle area = getClientArea();

        boolean tabBarVertical = isVertical();
        int contentWidth = Math.max(0, area.width - marginWidth - marginWidth
                - borderWidth - borderWidth - paddingWidth - paddingWidth);
        int contentHeight = Math.max(0,
                area.height - marginHeight - marginHeight - borderWidth
                        - borderWidth - paddingHeight - paddingHeight);

        int contentX = area.x + marginWidth + borderWidth + paddingWidth;
        int contentY = area.y + marginHeight + borderWidth + paddingHeight;
        Point contentSize;
        int itemX, itemY, itemWidth, itemHeight;
        boolean itemBoundsChanged = false;
        MTabBarItem item;

        Point[] contentSizes = new Point[itemCount];
        int expansion = 0;
        int expandingItemCount = 0;
        for (int i = 0; i < itemCount; i++) {
            item = getItem(i);
            if (!item.getVisible())
                continue;
            contentSize = computeItemSize(item, styles, tabBarVertical,
                    contentWidth, contentHeight, true);
            contentSizes[i] = contentSize;
            if (expansion > 0) {
                expansion += tabBarVertical ? vSpacing : hSpacing;
            }
            expansion += tabBarVertical ? contentSize.y : contentSize.x;
            if (item.isSeparator() && item.getWidth() == SWT.SEPARATOR_FILL) {
                expandingItemCount++;
            }
        }

        if (expansion > 0) {
            expansion = Math.max(0, tabBarVertical ? (contentHeight - expansion)
                    : (contentWidth - expansion));
        }

        int itemExpansion;
        separators = new Rectangle[itemCount - 1];
        for (int i = 0; i < itemCount; i++) {
            item = getItem(i);
            if (!item.getVisible())
                continue;

            contentSize = contentSizes[i];
            if (i > 0) {
                if (tabBarVertical)
                    contentY += vSpacing;
                else
                    contentX += hSpacing;
            }

            if (expansion > 0 && item.isSeparator()
                    && item.getWidth() == SWT.SEPARATOR_FILL) {
                itemExpansion = expansion / expandingItemCount;
                expansion -= itemExpansion;
                expandingItemCount--;
                if (tabBarVertical)
                    contentSize.y += itemExpansion;
                else
                    contentSize.x += itemExpansion;
            }

            itemX = contentX;
            itemY = contentY;
            itemWidth = contentSize.x;
            itemHeight = contentSize.y;

            item.contentBounds.x = contentX;
            item.contentBounds.y = contentY;
            item.contentBounds.width = contentSize.x;
            item.contentBounds.height = contentSize.y;

            if (!item.isSeparator())
                layoutItem(item, styles);

            if (tabBarVertical) {
                contentY += contentSize.y;
                itemX -= borderWidth + paddingWidth;
                itemWidth += borderWidth + borderWidth + paddingWidth
                        + paddingWidth;
                if (i == 0) {
                    itemY -= borderWidth + paddingHeight;
                    itemHeight += borderWidth + paddingHeight;
                }
                if (i == itemCount - 1) {
                    itemHeight += borderWidth + paddingHeight;
                }
            } else {
                contentX += contentSize.x;
                itemY -= borderWidth + paddingHeight;
                itemHeight += borderWidth + borderWidth + paddingHeight
                        + paddingHeight;
                if (i == 0) {
                    itemX -= borderWidth + paddingWidth;
                    itemWidth += borderWidth + paddingWidth;
                }
                if (i == itemCount - 1) {
                    itemWidth += borderWidth + paddingWidth;
                }
            }

            itemBoundsChanged |= item.setBounds(itemX, itemY, itemWidth,
                    itemHeight);

            if (i < itemCount - 1) {
                if (tabBarVertical) {
                    separators[i] = new Rectangle(
                            contentX - borderWidth - paddingWidth, contentY,
                            contentWidth + borderWidth + borderWidth
                                    + paddingWidth + paddingWidth,
                            vSpacing);
                } else {
                    separators[i] = new Rectangle(contentX,
                            contentY - borderWidth - paddingWidth, hSpacing,
                            contentHeight + borderWidth + borderWidth
                                    + paddingHeight + paddingHeight);
                }
            }
        }
        if (itemBoundsChanged) {
            redraw();
        }
    }

    /**
     * Validates bounds of item image and text.
     * 
     * @param item
     * @param styles
     */
    private void layoutItem(MTabBarItem item, IStyleProvider styles) {
        int contentX = item.contentBounds.x + item.marginWidth;
        int contentY = item.contentBounds.y + item.marginHeight;
        int contentWidth = item.contentBounds.width - item.marginWidth
                - item.marginWidth;
        int contentHeight = item.contentBounds.height - item.marginHeight
                - item.marginHeight;

        if (item.imageVisible && item.textVisible) {

            boolean itemVertical = (item.textPosition
                    & (SWT.TOP | SWT.BOTTOM)) != 0;
            boolean imageLeading = (item.textPosition
                    & (SWT.RIGHT | SWT.BOTTOM)) != 0;

            Point imageSize = item.imageVisible
                    ? computeImageSize(item.getImage()) : new Point(0, 0);
            Point textSize = item.textVisible
                    ? computeTextSize(item.getText(), item.font)
                    : new Point(0, 0);

            int imageWHint = styles.getWidth(item, MTabBarItem.IMAGE,
                    itemVertical ? contentWidth : SWT.DEFAULT);
            int imageHHint = styles.getHeight(item, MTabBarItem.IMAGE,
                    itemVertical ? SWT.DEFAULT : contentHeight);
            int textWHint = styles.getWidth(item, MTabBarItem.TEXT,
                    itemVertical ? contentWidth : SWT.DEFAULT);
            int textHHint = styles.getHeight(item, MTabBarItem.TEXT,
                    itemVertical ? SWT.DEFAULT : contentHeight);

            Point imageAreaSize = item.imageVisible
                    ? computePreferredSize(imageSize.x, imageSize.y, imageWHint,
                            imageHHint)
                    : new Point(0, 0);
            Point textAreaSize = item.textVisible
                    ? computePreferredSize(textSize.x, textSize.y, textWHint,
                            textHHint)
                    : new Point(0, 0);

            if (itemVertical) {
                if (imageHHint > 0 && textHHint < 0) {
                    textAreaSize.y = Math.max(0,
                            contentHeight - imageAreaSize.y - item.vSpacing);
                } else if (imageHHint < 0 && textHHint > 0) {
                    imageAreaSize.y = Math.max(0,
                            contentHeight - textAreaSize.y - item.vSpacing);
                }
            } else {
                if (imageWHint > 0 && textWHint < 0) {
                    textAreaSize.x = Math.max(0,
                            contentWidth - imageAreaSize.x - item.hSpacing);
                } else if (imageWHint < 0 && textWHint > 0) {
                    imageAreaSize.x = Math.max(0,
                            contentWidth - textAreaSize.x - item.hSpacing);
                }
            }

            imageSize = computeConstrainedSize(imageSize.x, imageSize.y,
                    imageAreaSize.x, imageAreaSize.y);
            textSize = computeConstrainedSize(textSize.x, textSize.y,
                    textAreaSize.x, textAreaSize.y);

            if (itemVertical) {
                item.imageBounds.x = contentX
                        + (contentWidth - imageSize.x) / 2;
                item.imageBounds.y = contentY
                        + (imageAreaSize.y - imageSize.y) / 2;
                item.textBounds.x = contentX + (contentWidth - textSize.x) / 2;
                item.textBounds.y = contentY
                        + (textAreaSize.y - textSize.y) / 2;
                if (imageLeading) {
                    item.textBounds.y += imageAreaSize.y + item.vSpacing;
                } else {
                    item.imageBounds.y += textAreaSize.y + item.vSpacing;
                }
            } else {
//                item.imageBounds.x = contentX
//                        + (imageAreaSize.x - imageSize.x) / 2;
//                item.imageBounds.y = contentY
//                        + (contentHeight - imageSize.y) / 2;
//                item.textBounds.x = contentX
//                        + (textAreaSize.x - textSize.x) / 2;
//                item.textBounds.y = contentY
//                        + (textAreaSize.y - textSize.y) / 2;
//                if (imageLeading) {
//                    item.textBounds.x += imageAreaSize.x + item.hSpacing;
//                } else {
//                    item.imageBounds.x += textAreaSize.x + item.hSpacing;
//                }
                if (imageLeading) {
                    item.imageBounds.x = contentX;
                    item.imageBounds.y = contentY
                            + (contentHeight - imageSize.y) / 2;
                    item.imageBounds.width = imageAreaSize.x;
                    item.imageBounds.height = imageSize.y;
                    item.textBounds.x = contentX + item.imageBounds.width
                            + item.hSpacing;
                    item.textBounds.y = item.imageBounds.y;
                    item.textBounds.width = item.contentBounds.width
                            - item.imageBounds.width - item.hSpacing;
                    item.textBounds.height = item.imageBounds.height;
//                    System.out.println("imageBouns:" + item.imageBounds + "-->"
//                            + "textBounds:" + item.textBounds
//                            + " contentBounds:" + item.contentBounds);
                } else {
                    item.textBounds.x = contentX;
                    item.textBounds.y = contentY;
                    item.textBounds.width = imageAreaSize.x;
                    item.textBounds.height = item.contentBounds.height;
                    item.imageBounds.x = contentX + item.textBounds.width
                            + item.hSpacing;
                    item.imageBounds.y = contentY;
                    item.imageBounds.width = item.contentBounds.width
                            - item.imageBounds.width - item.hSpacing;
                    item.imageBounds.height = item.contentBounds.height;
                }
                return;
            }

            item.imageBounds.width = imageSize.x;
            item.imageBounds.height = imageSize.y;
            item.textBounds.width = textSize.x;
            item.textBounds.height = textSize.y;

        } else if (item.imageVisible) {
            Point imageSize = computeImageSize(item.getImage());
            Point constrainedSize = computeConstrainedSize(imageSize.x,
                    imageSize.y, contentWidth, contentHeight);
            item.imageBounds.x = contentX
                    + (contentWidth - constrainedSize.x) / 2;
            item.imageBounds.y = contentY
                    + (contentHeight - constrainedSize.y) / 2;
            item.imageBounds.width = constrainedSize.x;
            item.imageBounds.height = constrainedSize.y;

            item.textBounds.x = 0;
            item.textBounds.y = 0;
            item.textBounds.width = 0;
            item.textBounds.height = 0;
        } else if (item.textVisible) {
            Point textSize = computeTextSize(item.getText(), item.font);
            Point constrainedSize = computeConstrainedSize(textSize.x,
                    textSize.y, contentWidth, contentHeight);
            item.imageBounds.x = 0;
            item.imageBounds.y = 0;
            item.imageBounds.width = 0;
            item.imageBounds.height = 0;
            item.textBounds.x = contentX
                    + (contentWidth - constrainedSize.x) / 2;
            item.textBounds.y = contentY
                    + (contentHeight - constrainedSize.y) / 2;
            item.textBounds.width = constrainedSize.x;
            item.textBounds.height = constrainedSize.y;
        } else {
            item.imageBounds.x = 0;
            item.imageBounds.y = 0;
            item.imageBounds.width = 0;
            item.imageBounds.height = 0;
            item.textBounds.x = 0;
            item.textBounds.y = 0;
            item.textBounds.width = 0;
            item.textBounds.height = 0;
        }
    }

    protected void updateTabBar() {
        layout(true);
        redraw();
    }

    private void paintTabBar(GC gc) {
        int itemCount = getItemCount();
        if (itemCount == 0)
            return;

        Rectangle borderArea = getClientArea();
        borderArea.x += marginWidth;
        borderArea.y += marginHeight;
        borderArea.width -= marginWidth + marginWidth;
        borderArea.height -= marginHeight + marginHeight;

        Rectangle borderFillArea = new Rectangle(borderArea.x + borderWidth,
                borderArea.y + borderWidth,
                borderArea.width - borderWidth - borderWidth,
                borderArea.height - borderWidth - borderWidth);

        MTabBarItem item;
        for (int i = 0; i < itemCount; i++) {
            item = getItem(i);
            if (!item.getVisible())
                continue;
            paintItemFill(gc, item, borderFillArea, i == 0, i == itemCount - 1);
        }

        paintBorderAndSeparators(gc, borderArea);

        for (int i = 0; i < itemCount; i++) {
            item = getItem(i);
            if (!item.getVisible())
                continue;
            paintItem(gc, item);
        }
    }

    private void paintBorderAndSeparators(GC gc, Rectangle borderArea) {
        IStyleProvider styles = getStyleProvider();
        Color borderColor = styles.getColor(this, BORDER);
        if (borderColor == null || borderColor.getAlpha() <= 0)
            return;

        int borderAlpha = styles.getAlpha(this, BORDER, 0xff);
        if (borderAlpha <= 0)
            return;

        Color oldForeground = gc.getForeground();
        Color oldBackground = gc.getBackground();
        int oldAlpha = gc.getAlpha();
        int oldLineWidth = gc.getLineWidth();
        int oldLineStyle = gc.getLineStyle();
        int oldLineCap = gc.getLineCap();

        gc.setForeground(borderColor);
        gc.setBackground(borderColor);
        gc.setAlpha(borderAlpha);
        gc.setLineWidth(borderWidth);
        gc.setLineStyle(SWT.LINE_SOLID);
        gc.setLineCap(SWT.CAP_SQUARE);

        if (cornerWidth > 0 && cornerHeight > 0) {
            gc.drawRoundRectangle(borderArea.x + borderWidth / 2,
                    borderArea.y + borderWidth / 2,
                    borderArea.width - borderWidth,
                    borderArea.height - borderWidth, cornerWidth + cornerWidth,
                    cornerHeight + cornerHeight);
        } else {
            gc.drawRectangle(borderArea.x + borderWidth / 2,
                    borderArea.y + borderWidth / 2,
                    borderArea.width - borderWidth,
                    borderArea.height - borderWidth);
        }

        boolean tabBarVertical = isVertical();
        if (separators != null && separators.length > 0) {
            for (int i = 0; i < separators.length; i++) {
                Rectangle sep = separators[i];
                if (tabBarVertical) {
                    gc.fillRectangle(sep.x + borderWidth, sep.y,
                            sep.width - borderWidth - borderWidth, sep.height);
                } else {
                    gc.fillRectangle(sep.x, sep.y + borderWidth, sep.width,
                            sep.height - borderWidth - borderWidth);
                }
            }
        }

        gc.setLineCap(oldLineCap);
        gc.setLineStyle(oldLineStyle);
        gc.setLineWidth(oldLineWidth);
        gc.setAlpha(oldAlpha);
        gc.setBackground(oldBackground);
        gc.setForeground(oldForeground);
    }

    private void paintItem(GC gc, MTabBarItem item) {
        if (item.contentBounds.width <= 0 || item.contentBounds.height <= 0)
            // empty client area, paint nothing
            return;

        if (!item.imageVisible && !item.textVisible)
            // neither image nor text is available for painting
            return;

        Rectangle oldClipping = gc.getClipping();

        gc.setClipping(item.bounds);
        gc.setAntialias(SWT.ON);
        gc.setTextAntialias(SWT.ON);

        if (item.imageVisible) {
            paintItemImage(gc, item);
        }
        if (item.textVisible) {
            paintItemText(gc, item);
        }

        gc.setClipping(oldClipping);
    }

    private void paintItemImage(GC gc, MTabBarItem item) {
        Image image = item.getImage();
        if (image == null)
            return;

        Rectangle dest = item.imageBounds;
        if (dest.width <= 0 || dest.height <= 0)
            return;

        Rectangle src = image.getBounds();
        gc.drawImage(image, src.x, src.y, src.width, src.height, dest.x, dest.y,
                dest.width, dest.height);
    }

    private void paintItemText(GC gc, MTabBarItem item) {
        String text = item.getText();
        if (text == null || "".equals(text)) //$NON-NLS-1$
            return;

        Rectangle dest = item.textBounds;
        if (dest.width <= 0 || dest.height <= 0)
            return;

        Color textColor = getStyleProvider().getColor(item, MTabBarItem.TEXT);
        if (textColor == null)
            textColor = getForeground();

        Color oldForeground = gc.getForeground();
        gc.setForeground(textColor);
        gc.setFont(item.font);
        Point textSize = gc.textExtent(text);

        IStyleProvider styles = getStyleProvider();
        int textAlign = styles.getTextAlign(item, MTabBarItem.TEXT_ALIGN,
                SWT.CENTER);

        Transform t = new Transform(gc.getDevice());
        float scaleX = Math.min(1, ((float) dest.width) / textSize.x);
        float scaleY = Math.min(1, ((float) dest.height) / textSize.y);
        float scale = Math.min(scaleX, scaleY);
        float offsetX = dest.x + ((float) dest.width) / 2;
        float offsetY = dest.y + ((float) dest.height) / 2;
        float offsetX2 = 0;
        float offsetY2 = -((float) textSize.y) / 2;

        switch (textAlign) {
        case SWT.LEFT:
            offsetX2 = -dest.width / 2 + item.hSpacing * 5;
            break;
        case SWT.CENTER:
            offsetX2 = -((float) textSize.x) / 2;
            break;
        case SWT.RIGHT:
            offsetX2 = -dest.width / 2 + (dest.width - textSize.x) / 2;
            break;
        }

        t.translate(offsetX, offsetY);
        t.scale(scale, scale);
        t.translate(offsetX2, offsetY2 / scale);

        Transform oldTransform = new Transform(gc.getDevice());
        try {
            gc.getTransform(oldTransform);

            try {
                gc.setTransform(t);
                gc.drawText(text, 0, 0, true);
            } finally {
                t.dispose();
            }

            gc.setTransform(oldTransform);
        } finally {
            oldTransform.dispose();
        }

        gc.setForeground(oldForeground);
    }

    private void paintItemFill(GC gc, MTabBarItem item, Rectangle borderArea,
            boolean first, boolean last) {
        IStyleProvider styles = getStyleProvider();

        /// Set fill color with given color
        Color fillColor = null;

        String color = item.getColor();
        if (color != null && !"".equals(color)) { //$NON-NLS-1$
            RGB rgb = ColorUtils.toRGB(color);
            fillColor = resources.createColor(rgb);
        }

        if (fillColor == null)
            fillColor = styles.getColor(item, MTabBarItem.FILL);

        if (fillColor == null || fillColor.getAlpha() <= 0)
            return;

        int alpha = styles.getAlpha(item, MTabBarItem.FILL, 0xFF);
        if (alpha <= 0)
            return;

        Rectangle oldClipping = gc.getClipping();
        int oldAlpha = gc.getAlpha();
        Color oldBackground = gc.getBackground();

        gc.setAntialias(SWT.ON);
        gc.setTextAntialias(SWT.ON);
        gc.setClipping(item.bounds);
        gc.setAlpha(alpha);
        gc.setBackground(fillColor);

        if (cornerWidth > 0 && cornerHeight > 0) {
            gc.fillRoundRectangle(borderArea.x, borderArea.y, borderArea.width,
                    borderArea.height, cornerWidth + cornerWidth - borderWidth,
                    cornerWidth + cornerWidth - borderWidth);
        } else {
            gc.fillRectangle(borderArea);
        }

        gc.setBackground(oldBackground);
        gc.setAlpha(oldAlpha);
        gc.setClipping(oldClipping);
    }

    private void onDispose(Event event) {
        removeListener(SWT.Dispose, listener);
        notifyListeners(SWT.Dispose, event);
        event.type = SWT.None;

        inDispose = true;

        for (MTabBarItem item : items) {
            if (!item.isDisposed())
                item.dispose();
        }
    }

    private void onResize(Event event) {
        if (inDispose)
            return;
        layout(true);
    }

    private void onPaint(Event event) {
        if (inDispose)
            return;
        paintTabBar(event.gc);
    }

    private void onMouseEnter(Event event) {
        MTabBarItem item = getItem(event.x, event.y);
        if (item != null) {
            item.setPreselected(true);
        }
    }

    private void onMouseExit(Event event) {
        for (MTabBarItem item : items) {
            item.setPreselected(false);
        }
    }

    private void onMouseMove(Event event) {
        MTabBarItem newTarget = null;
        MTabBarItem oldTarget = null;
        for (MTabBarItem item : items) {
            if (item.getBounds().contains(event.x, event.y))
                newTarget = item;
            if (item.isPreselected())
                oldTarget = item;
        }

        if (newTarget == oldTarget)
            return;

        if (oldTarget != null) {
            oldTarget.setPreselected(false);
            if (oldTarget == trackedItem) {
                oldTarget.setSelected(false);
            }
        }
        if (newTarget != null) {
            newTarget.setPreselected(true);
            if (newTarget == trackedItem) {
                newTarget.setSelected(true);
            }
        }
    }

    private void onMouseDown(Event event) {
        trackedItem = null;

        MTabBarItem item = getItem(event.x, event.y);
        if (item != null && (item.isPushButton() || item.isRadioButton())) {
            item.setSelected(true);
            if (item.isRadioButton()) {
                mouseSelect(item, event);
            } else {
                trackedItem = item;
            }
        }
    }

    private void onMouseUp(Event event) {
        MTabBarItem item = getItem(event.x, event.y);
        if (item != null && item.isPushButton()) {
            item.setSelected(false);
            if (item == trackedItem) {
                update();
                mouseSelect(item, event);
            }
        }
        trackedItem = null;
    }

    private void mouseSelect(MTabBarItem item, Event event) {
        if (item.isSeparator())
            return;

        int index = indexOf(item);
        Widget target;
        if (item.isPushButton()) {
            target = item;
        } else {
            setSelection(item);
            target = this;
        }
        Event e = new Event();
        e.item = item;
        e.index = index;
        e.detail = event.detail;
        e.x = event.x;
        e.y = event.y;
        target.notifyListeners(SWT.Selection, e);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        // TODO Auto-generated method stub
        super.setBounds(x, y, width, height);
    }

    @Override
    public void setBounds(Rectangle rect) {
        // TODO Auto-generated method stub
        super.setBounds(rect);
    }

}
