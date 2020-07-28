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
package org.xmind.ui.forms;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

public class BellowsLayout extends Layout {

    public int alignment = SWT.BEGINNING;

    public int direction = SWT.HORIZONTAL;

    public int marginWidth = 0;

    public int marginHeight = 0;

    public int marginLeft = 0;

    public int marginRight = 0;

    public int marginTop = 0;

    public int marginBottom = 0;

    public int spacing = 0;

    private int cachedWHint = SWT.DEFAULT;
    private int cachedHHint = SWT.DEFAULT;
    private Point cachedSize = null;

    private static interface IAdjust {
        int adjust(int width, int adjustment);
    }

    private static final IAdjust EXPAND = new IAdjust() {
        public int adjust(int width, int adjustment) {
            return width + adjustment;
        }
    };

    private static final IAdjust SHRINK = new IAdjust() {
        public int adjust(int width, int adjustment) {
            return width - adjustment;
        }
    };

    public BellowsLayout() {
    }

    public BellowsLayout(int direction) {
        this.direction = direction;
    }

    protected Point computeSize(Composite composite, int wHint, int hHint,
            boolean flushCache) {
        if (wHint < 0 || hHint < 0) {
            if (flushCache || this.cachedSize == null
                    || wHint != this.cachedWHint || hHint != this.cachedHHint) {
                layout(composite, getWidth(wHint, hHint),
                        getHeight(wHint, hHint), null, flushCache);
            }
            return new Point(this.cachedSize.x, this.cachedSize.y);
        } else {
            return new Point(wHint, hHint);
        }
    }

    protected void layout(Composite composite, boolean flushCache) {
        Rectangle bounds = composite.getClientArea();
        int wHint = getWidth(bounds);
        int hHint = getHeight(bounds);
        layout(composite, wHint, hHint, bounds, flushCache);
    }

    private static class Adjustable implements Comparable<Adjustable> {
        final int controlIndex;
        final int adjustableWidth;

        public Adjustable(int controlIndex, int extraWidth) {
            this.controlIndex = controlIndex;
            this.adjustableWidth = extraWidth;
        }

        public int compareTo(Adjustable that) {
            return this.adjustableWidth - that.adjustableWidth;
        }

        public int hashCode() {
            return controlIndex ^ adjustableWidth;
        }

        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj == null || !(obj instanceof Adjustable))
                return false;
            Adjustable that = (Adjustable) obj;
            return this.controlIndex == that.controlIndex;
        }

        @Override
        public String toString() {
            return "{controlIndex=" + controlIndex //$NON-NLS-1$
                    + ",adjustableWidth=" + adjustableWidth //$NON-NLS-1$
                    + "}"; //$NON-NLS-1$
        }
    }

    private void layout(Composite composite, int wHint, int hHint,
            Rectangle bounds, boolean flushCache) {
        Control[] allControls = composite.getChildren();
        int count = 0;
        Control[] controls = new Control[allControls.length];
        int[] prefWidths = new int[allControls.length];
        int[] prefHeights = new int[allControls.length];
        int[] cellWidths = new int[allControls.length];

        Map<Integer, SortedSet<Adjustable>> softShrinkables = new HashMap<Integer, SortedSet<Adjustable>>(
                allControls.length);
        SortedSet<Adjustable> hardShrinkables = new TreeSet<Adjustable>();
        Map<Integer, SortedSet<Adjustable>> softExpandables = new HashMap<Integer, SortedSet<Adjustable>>(
                allControls.length);
        SortedSet<Adjustable> hardExpandables = new TreeSet<Adjustable>();

        int childHeightHint = hHint < 0 ? hHint : Math.max(
                0,
                hHint - getHeight(marginWidth, marginHeight) * 2
                        - getHeight(marginLeft, marginTop)
                        - getHeight(marginRight, marginBottom));

        int actualHeight = 0;
        int actualWidth = 0;

        Control control;
        BellowsData data;
        Point size;
        int controlIndex;
        Integer adjustPriority;
        SortedSet<Adjustable> adjustableSet;
        int adjustableWidth;
        int total = allControls.length;
        for (int i = 0; i < total; i++) {
            control = allControls[i];
            data = getLayoutData(control);
            if (data.exclude)
                continue;
            controlIndex = count;
            count++;
            controls[controlIndex] = control;
            size = data.computeSize(
                    control,
                    data.minorAlignment == SWT.FILL ? getWidth(SWT.DEFAULT,
                            childHeightHint) : SWT.DEFAULT,
                    data.minorAlignment == SWT.FILL ? getHeight(SWT.DEFAULT,
                            childHeightHint) : SWT.DEFAULT, flushCache);
            prefHeights[controlIndex] = getHeight(size);
            prefWidths[controlIndex] = cellWidths[controlIndex] = getWidth(size);
            actualHeight = Math.max(actualHeight, prefHeights[controlIndex]);
            actualWidth += cellWidths[controlIndex];
            if (data.expandable) {
                adjustableWidth = data.hardMaximum == 0 ? Integer.MAX_VALUE
                        : data.hardMaximum
                                - Math.max(prefWidths[controlIndex],
                                        data.softMaximum);
                if (adjustableWidth > 0) {
                    hardExpandables.add(new Adjustable(controlIndex,
                            adjustableWidth));
                }
                adjustableWidth = data.softMaximum == 0 ? Integer.MAX_VALUE
                        : data.softMaximum - prefWidths[controlIndex];
                if (adjustableWidth > 0) {
                    adjustPriority = Integer.valueOf(data.expandPriority);
                    adjustableSet = softExpandables.get(adjustPriority);
                    if (adjustableSet == null) {
                        adjustableSet = new TreeSet<Adjustable>();
                        softExpandables.put(adjustPriority, adjustableSet);
                    }
                    adjustableSet.add(new Adjustable(controlIndex,
                            adjustableWidth));
                }
            }
            if (data.shrinkable) {
                adjustableWidth = Math.min(prefWidths[controlIndex],
                        data.softMinimum) - Math.max(0, data.hardMinimum);
                if (adjustableWidth > 0) {
                    hardShrinkables.add(new Adjustable(controlIndex,
                            adjustableWidth));
                }
                adjustableWidth = prefWidths[controlIndex]
                        - Math.max(0, data.softMinimum);
                if (adjustableWidth > 0) {
                    adjustPriority = Integer.valueOf(data.shrinkPriority);
                    adjustableSet = softShrinkables.get(adjustPriority);
                    if (adjustableSet == null) {
                        adjustableSet = new TreeSet<Adjustable>();
                        softShrinkables.put(adjustPriority, adjustableSet);
                    }
                    adjustableSet.add(new Adjustable(controlIndex,
                            adjustableWidth));
                }
            }
        }

        actualWidth += getWidth(marginWidth, marginHeight) * 2
                + getWidth(marginLeft, marginTop)
                + getWidth(marginRight, marginBottom) + spacing * (count - 1);
        actualHeight += getHeight(marginWidth, marginHeight) * 2
                + getHeight(marginLeft, marginTop)
                + getHeight(marginRight, marginBottom);

        if (wHint >= 0 && wHint != actualWidth && count > 0) {
            int adjustment;
            int adjustables;
            int singleAdjustment;
            Integer[] adjustPriorities;
            IAdjust adjust;
            Map<Integer, SortedSet<Adjustable>> softAdjustables;
            SortedSet<Adjustable> hardAdjustables;
            if (wHint < actualWidth) {
                adjustment = actualWidth - wHint;
                adjust = SHRINK;
                softAdjustables = softShrinkables;
                hardAdjustables = hardShrinkables;
            } else {
                adjustment = wHint - actualWidth;
                adjust = EXPAND;
                softAdjustables = softExpandables;
                hardAdjustables = hardExpandables;
            }
            adjustPriorities = softAdjustables.keySet().toArray(
                    new Integer[softAdjustables.size()]);
            Arrays.sort(adjustPriorities);
            for (int i = adjustPriorities.length - 1; i >= 0; i--) {
                adjustPriority = adjustPriorities[i];
                adjustableSet = softAdjustables.get(adjustPriority);
                if (!adjustableSet.isEmpty()) {
                    adjustables = adjustableSet.size();
                    for (Adjustable adjustable : adjustableSet) {
                        singleAdjustment = Math.min(adjustment / adjustables,
                                adjustable.adjustableWidth);
                        adjustment -= singleAdjustment;
                        actualWidth = adjust.adjust(actualWidth,
                                singleAdjustment);
                        cellWidths[adjustable.controlIndex] = adjust.adjust(
                                cellWidths[adjustable.controlIndex],
                                singleAdjustment);
                        adjustables--;
                        if (adjustment <= 0)
                            break;
                    }

                    if (adjustment <= 0)
                        break;
                }
            }

            if (adjustment > 0) {
                adjustables = hardAdjustables.size();
                for (Adjustable adjustable : hardAdjustables) {
                    singleAdjustment = Math.min(adjustment / adjustables,
                            adjustable.adjustableWidth);
                    adjustment -= singleAdjustment;
                    actualWidth = adjust.adjust(actualWidth, singleAdjustment);
                    cellWidths[adjustable.controlIndex] = adjust.adjust(
                            cellWidths[adjustable.controlIndex],
                            singleAdjustment);
                    adjustables--;
                    if (adjustment <= 0)
                        break;
                }
            }
        }

        if (bounds == null) {
            // Calculate preferred size:
            if (direction == SWT.HORIZONTAL) {
                this.cachedSize = new Point(actualWidth, actualHeight);
            } else {
                this.cachedSize = new Point(actualHeight, actualWidth);
            }
        } else {
            // Layout children controls within bounds:
            total = count;
            int x = getX(bounds) + getWidth(marginWidth, marginHeight)
                    + getWidth(marginLeft, marginTop);
            int y = getY(bounds) + getHeight(marginWidth, marginHeight)
                    + getHeight(marginLeft, marginTop);
            int gap;
            switch (alignment) {
            case SWT.END:
            case SWT.BOTTOM:
            case SWT.RIGHT:
                x += wHint - actualWidth;
                gap = 0;
                break;
            case SWT.CENTER:
                x += (wHint - actualWidth) / 2;
                gap = 0;
                break;
            case SWT.FILL:
                gap = wHint - actualWidth;
                break;
            default:
                gap = 0;
            }
            int controlX, controlY, controlWidth, controlHeight, singleGap;
            for (int i = 0; i < total; i++) {
                controlIndex = i;
                control = controls[controlIndex];
                data = getLayoutData(control);
                switch (data.majorAlignment) {
                case SWT.BEGINNING:
                case SWT.LEFT:
                case SWT.TOP:
                    controlWidth = prefWidths[controlIndex];
                    controlX = x;
                    break;
                case SWT.END:
                case SWT.RIGHT:
                case SWT.BOTTOM:
                    controlWidth = prefWidths[controlIndex];
                    controlX = x + cellWidths[controlIndex] - controlWidth;
                    break;
                case SWT.CENTER:
                    controlWidth = prefWidths[controlIndex];
                    controlX = x + (cellWidths[controlIndex] - controlWidth)
                            / 2;
                    break;
                default:
                    controlWidth = cellWidths[controlIndex];
                    controlX = x;
                    break;
                }

                switch (data.minorAlignment) {
                case SWT.BEGINNING:
                case SWT.LEFT:
                case SWT.TOP:
                    controlHeight = prefHeights[controlIndex];
                    controlY = y;
                    break;
                case SWT.END:
                case SWT.RIGHT:
                case SWT.BOTTOM:
                    controlHeight = prefHeights[controlIndex];
                    controlY = y + childHeightHint - controlHeight;
                    break;
                case SWT.CENTER:
                    controlHeight = prefHeights[controlIndex];
                    controlY = y + (childHeightHint - controlHeight) / 2;
                    break;
                default:
                    controlHeight = childHeightHint;
                    controlY = y;
                    break;
                }

                if (direction == SWT.HORIZONTAL) {
                    control.setBounds(controlX, controlY, controlWidth,
                            controlHeight);
                } else {
                    control.setBounds(controlY, controlX, controlHeight,
                            controlWidth);
                }
                x += cellWidths[controlIndex] + spacing;
                if (gap != 0) {
                    singleGap = gap / (total - i);
                    gap -= singleGap;
                    x += singleGap;
                }
            }
        }
    }

    private int getWidth(Point size) {
        return direction == SWT.HORIZONTAL ? size.x : size.y;
    }

    private int getWidth(Rectangle rect) {
        return direction == SWT.HORIZONTAL ? rect.width : rect.height;
    }

    private int getWidth(int width, int height) {
        return direction == SWT.HORIZONTAL ? width : height;
    }

    private int getHeight(Point size) {
        return direction == SWT.HORIZONTAL ? size.y : size.x;
    }

    private int getHeight(Rectangle rect) {
        return direction == SWT.HORIZONTAL ? rect.height : rect.width;
    }

    private int getHeight(int width, int height) {
        return direction == SWT.HORIZONTAL ? height : width;
    }

    private int getX(Rectangle rect) {
        return direction == SWT.HORIZONTAL ? rect.x : rect.y;
    }

    private int getY(Rectangle rect) {
        return direction == SWT.HORIZONTAL ? rect.y : rect.x;
    }

    private BellowsData getLayoutData(Control control) {
        Object data = control.getLayoutData();
        if (data == null && !(data instanceof BellowsData)) {
            data = new BellowsData();
            control.setLayoutData(data);
        }
        return (BellowsData) data;
    }

    @Override
    protected boolean flushCache(Control control) {
        this.cachedSize = null;
        BellowsData data = getLayoutData(control);
        data.flushCache();
        return true;
    }

}
