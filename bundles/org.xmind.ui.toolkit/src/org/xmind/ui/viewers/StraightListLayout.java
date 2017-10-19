package org.xmind.ui.viewers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class StraightListLayout extends HintedListLayout {

//    private static class StraightListLayoutData {
//
//        int computedWidth = -1, computedHeight = -1;
//        int computedWHint = -1, computedHHint = -1;
//        int appliedWidth = -1, appliedHeight = -1;
//        int appliedWHint = -1, appliedHHint = -1;
//
//        Point computeSize(Control control, int wHint, int hHint,
//                boolean flushCache, boolean computing) {
//            if (computing) {
//                if (!flushCache && wHint == computedWHint
//                        && hHint == computedHHint && computedWidth >= 0
//                        && computedHeight >= 0) {
//                    return new Point(computedWidth, computedHeight);
//                }
//                computedWHint = wHint;
//                computedHHint = hHint;
//                Point size = control.computeSize(wHint, hHint, flushCache);
//                computedWidth = size.x;
//                computedHeight = size.y;
//                return size;
//            } else {
//                if (!flushCache && wHint == appliedWHint
//                        && hHint == appliedHHint && appliedWidth >= 0
//                        && appliedHeight >= 0) {
//                    return new Point(appliedWidth, appliedHeight);
//                }
//                appliedWHint = wHint;
//                appliedHHint = hHint;
//                Point size = control.computeSize(wHint, hHint, flushCache);
//                appliedWidth = size.x;
//                appliedHeight = size.y;
//                return size;
//            }
//        }
//
//    }

    private boolean horizontal;

    /**
     * Constructs a new instance of this class with the specified style.
     * 
     * @param style
     *            one of SWT.VERTICAL or SWT.HORIZONTAL
     */
    public StraightListLayout(int style) {
        this.horizontal = (style & SWT.HORIZONTAL) != 0;
    }

    public Point computeSize(MListViewer viewer, Composite composite, int wHint,
            int hHint, boolean flushCache) {
        if (wHint >= 0 && hHint >= 0)
            return new Point(wHint, hHint);

        Control[] items = composite.getChildren();

        int hListAlignment = getHint(ALIGNMENT_LIST_HORIZONTAL,
                horizontal ? SWT.LEAD : SWT.FILL);
        int vListAlignment = getHint(ALIGNMENT_LIST_VERTICAL,
                horizontal ? SWT.FILL : SWT.LEAD);
        int itemWHint = getHint(ITEM_WIDTH,
                horizontal || hListAlignment != SWT.FILL ? SWT.DEFAULT : wHint);
        int itemHHint = getHint(ITEM_HEIGHT,
                horizontal && vListAlignment == SWT.FILL ? hHint : SWT.DEFAULT);
        int spacing = getHint(
                horizontal ? SPACING_HORIZONTAL : SPACING_VERTICAL, 0);

        Point listSize = new Point(0, 0);

        for (int i = 0; i < items.length; i++) {
            Control item = items[i];
            Point itemSize = item.computeSize(itemWHint, itemHHint, flushCache);
            if (horizontal) {
                listSize.x += itemSize.x;
                listSize.y = Math.max(listSize.y, itemSize.y);
            } else {
                listSize.x = Math.max(listSize.x, itemSize.x);
                listSize.y += itemSize.y;
            }
        }

        if (items.length > 0) {
            if (horizontal) {
                listSize.x += spacing * (items.length - 1);
            } else {
                listSize.y += spacing * (items.length - 1);
            }
        }

        listSize.x += getHint(MARGIN_LEFT, 0) + getHint(MARGIN_RIGHT, 0);
        listSize.y += getHint(MARGIN_TOP, 0) + getHint(MARGIN_BOTTOM, 0);

        return listSize;
    }

    public void layout(MListViewer viewer, Composite composite,
            boolean flushCache) {
        Rectangle area = composite.getClientArea();
        Control[] items = composite.getChildren();
        if (items.length <= 0)
            return;

        int marginTop = getHint(MARGIN_TOP, 0);
        int marginBottom = getHint(MARGIN_BOTTOM, 0);
        int marginLeft = getHint(MARGIN_LEFT, 0);
        int marginRight = getHint(MARGIN_RIGHT, 0);
        int spacing = getHint(
                horizontal ? SPACING_HORIZONTAL : SPACING_VERTICAL, 0);
        int hListAlignment = getHint(ALIGNMENT_LIST_HORIZONTAL,
                horizontal ? SWT.LEAD : SWT.FILL);
        int vListAlignment = getHint(ALIGNMENT_LIST_VERTICAL,
                horizontal ? SWT.FILL : SWT.LEAD);
        int hItemAlignment = getHint(ALIGNMENT_ITEM_HORIZONTAL, SWT.FILL);
        int vItemAlignment = getHint(ALIGNMENT_ITEM_VERTICAL, SWT.FILL);

        if (marginLeft + marginRight > area.width) {
            area.x += (marginLeft + marginRight - area.width) / 2;
            area.width = marginLeft + marginRight;
        }
        if (marginTop + marginBottom > area.height) {
            area.y += (marginTop + marginBottom - area.height) / 2;
            area.height = marginTop + marginBottom;
        }

        int left = area.x + marginLeft;
        int top = area.y + marginTop;
        int wHint = area.width - marginLeft - marginRight;
        int hHint = area.height - marginTop - marginBottom;

        int itemWHint = getHint(ITEM_WIDTH,
                horizontal || hListAlignment != SWT.FILL ? SWT.DEFAULT : wHint);
        int itemHHint = getHint(ITEM_HEIGHT,
                horizontal && vListAlignment == SWT.FILL ? hHint : SWT.DEFAULT);
        Point listSize = new Point(0, 0);

        Point[] itemSizes = new Point[items.length];

        Control item;
        Point itemSize;

        for (int i = 0; i < items.length; i++) {
            item = items[i];
            itemSize = item.computeSize(itemWHint, itemHHint, flushCache);
            itemSizes[i] = new Point(itemSize.x, itemSize.y);
            if (horizontal) {
                listSize.x += itemSize.x;
                listSize.y = Math.max(listSize.y, itemSize.y);
            } else {
                listSize.x = Math.max(listSize.x, itemSize.x);
                listSize.y += itemSize.y;
            }
        }

        int allSpacing = spacing * (items.length - 1);

        int allAdjustment = 0;
        if (hListAlignment == SWT.FILL) {
            if (horizontal) {
                allAdjustment = wHint - allSpacing - listSize.x;
            } else {
                listSize.x = wHint;
            }
        } else if (hListAlignment == SWT.CENTER) {
            left += (wHint - allSpacing - listSize.x) / 2;
        } else if (hListAlignment == SWT.TRAIL) {
            left += wHint - allSpacing - listSize.x;
        }

        if (vListAlignment == SWT.FILL) {
            if (horizontal) {
                listSize.y = hHint;
            } else {
                allAdjustment = hHint - allSpacing - listSize.y;
            }
        } else if (vListAlignment == SWT.CENTER) {
            top += (hHint - allSpacing - listSize.y) / 2;
        } else if (vListAlignment == SWT.TRAIL) {
            top += hHint - allSpacing - listSize.y;
        }

        int adjustment;
        Rectangle itemBounds;
        Point cellSize;
        for (int i = 0; i < items.length; i++) {
            if (i > 0) {
                if (horizontal) {
                    left += spacing;
                } else {
                    top += spacing;
                }
            }

            item = items[i];
            itemSize = itemSizes[i];
            adjustment = allAdjustment / (items.length - i);
            allAdjustment -= adjustment;
            if (horizontal) {
                cellSize = new Point(itemSize.x + adjustment, listSize.y);
            } else {
                cellSize = new Point(listSize.x, itemSize.y + adjustment);
            }

            itemBounds = new Rectangle(left, top, itemSize.x, itemSize.y);
            if (hItemAlignment == SWT.FILL) {
                itemBounds.width = cellSize.x;
            } else if (hItemAlignment == SWT.CENTER) {
                itemBounds.x += (cellSize.x - itemBounds.width) / 2;
            } else if (hItemAlignment == SWT.TRAIL) {
                itemBounds.x += cellSize.x - itemBounds.width;
            }
            if (vItemAlignment == SWT.FILL) {
                itemBounds.height = cellSize.y;
            } else if (vItemAlignment == SWT.CENTER) {
                itemBounds.y += (cellSize.y - itemBounds.height) / 2;
            } else if (vItemAlignment == SWT.TRAIL) {
                itemBounds.y += cellSize.y - itemBounds.height;
            }

            item.setBounds(itemBounds);

            if (horizontal) {
                left += cellSize.x;
            } else {
                top += cellSize.y;
            }
        }

    }

    public void itemAdded(MListViewer viewer, Composite composite,
            Control item) {
    }

    public void itemRemoved(MListViewer viewer, Composite composite,
            Control item) {
    }

}
