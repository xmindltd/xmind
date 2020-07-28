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
package org.xmind.ui.tabfolder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.xmind.ui.resources.ColorUtils;

/**
 * @author Brian Sun
 */
public class PageMoveHelper implements Listener {

    private static final int INSERTION_MARK_WIDTH = 5;

    private static final int W = 15;

    private static final int H = 14;

    private static final int[] SHAPE = new int[] { 4, 0, //
            W - 4, 0, //
            W - 4, H - 8, //
            W - 1, H - 8, //
            W / 2, H, //
            0, H - 8,//
            4, H - 8 };

    private static final int[] BORDER = new int[] { 4, 0, //
            W - 5, 0, //
            W - 5, H - 8, //
            W - 2, H - 8, //
            W / 2, H - 2, //
            1, H - 8,//
            4, H - 8 };

    private CTabFolder tabFolder;

    private CTabItem sourceItem = null;

    private int insertionMarkIndex = -1;

    private List<IPageMoveListener> listeners = null;

    private Shell arrow = null;

    private Region arrowShape = null;

    /**
     * @param parent
     */
    public PageMoveHelper(CTabFolder tabFolder) {
        this.tabFolder = tabFolder;
        hookTabFolder();
    }

    private void hookTabFolder() {
        tabFolder.addListener(SWT.Paint, this);
        tabFolder.addListener(SWT.MouseDown, this);
        tabFolder.addListener(SWT.MouseUp, this);
        tabFolder.addListener(SWT.MouseMove, this);
    }

    private int findInsertionMarkIndex(int x, int y) {
        int resultIndex = -1;
        CTabItem item = tabFolder.getItem(new Point(x, y));
        if (item != null) {
            resultIndex = tabFolder.indexOf(item);
            int division = calculateDivision(item, y);
            if (x >= division)
                resultIndex++;
        } else if (tabFolder.getItemCount() > 0) {
            int division = calculateDivision(0, y);
            if (division >= 0 && x < division) {
                resultIndex = 0;
            } else {
                int lastIndex = tabFolder.getItemCount() - 1;
                division = calculateDivision(lastIndex, y);
                if (division >= 0 && x > division)
                    resultIndex = lastIndex + 1;
            }
        }
        return resultIndex;
    }

    private int calculateDivision(int index, int y) {
        return calculateDivision(tabFolder.getItem(index), y);
    }

    private int calculateDivision(CTabItem item, int y) {
        Rectangle r = item.getBounds();
        if (y >= r.y && y < r.y + r.width)
            return r.x + r.width * 2 / 3;
        return -1;
    }

    private void cancel() {
        disposeArrow();
        tabFolder.setCursor(null);
        sourceItem = null;
        insertionMarkIndex = -1;
    }

    public void addListener(IPageMoveListener l) {
        if (listeners == null)
            listeners = new ArrayList<IPageMoveListener>();
        listeners.add(l);
    }

    public void removeListener(IPageMoveListener l) {
        listeners.remove(l);
    }

    protected void firePageMoved(final int fromIndex, final int toIndex) {
        if (listeners == null)
            return;
        for (final Object l : listeners.toArray()) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    ((IPageMoveListener) l).pageMoved(fromIndex, toIndex);
                }
            });
        }
    }

    public void handleEvent(Event event) {
        switch (event.type) {
        case SWT.Paint:
            handlePaint(event);
            break;
        case SWT.MouseDown:
            handleMouseDown(event);
            break;
        case SWT.MouseUp:
            handleMouseUp(event);
            break;
        case SWT.MouseMove:
            handleMouseMove(event);
            break;
        case SWT.Dispose:
            handleDispose(event);
            break;
        }
    }

    private void handlePaint(Event e) {
        if (insertionMarkIndex >= 0) {
            paintInsertionMarkAtIndex(insertionMarkIndex, e.gc, e.display);
        }
    }

    private Point calcInsertionLocation(int insertIndex, boolean toDisplay) {
        int lastIndex = tabFolder.getItemCount() - 1;
        if (insertIndex >= 0 && insertIndex <= lastIndex) {
            CTabItem item = tabFolder.getItem(insertIndex);
            Rectangle r = item.getBounds();
            if (toDisplay)
                return tabFolder.toDisplay(r.x, r.y);
            return new Point(r.x, r.y);
        } else if (insertIndex > lastIndex) {
            CTabItem item = tabFolder.getItem(lastIndex);
            Rectangle r = item.getBounds();
            if (toDisplay)
                return tabFolder.toDisplay(r.x + r.width - 1, r.y);
            return new Point(r.x + r.width - 1, r.y);
        }
        return null;
    }

    private void paintInsertionMarkAtIndex(int insertIndex, GC gc,
            Display display) {
        Point loc = calcInsertionLocation(insertIndex, false);
        if (loc != null) {
            paintInsertionMarkAt(loc.x, loc.y, tabFolder.getTabHeight(), gc,
                    display);
        }
    }

    private void paintInsertionMarkAt(int x, int y, int height, GC gc,
            Display display) {
        gc.setAlpha(0xd0);
        gc.setBackground(ColorUtils.getColor("#0060d0")); //$NON-NLS-1$
        gc.fillRectangle(x - INSERTION_MARK_WIDTH / 2, y, INSERTION_MARK_WIDTH,
                height);
    }

    private void handleMouseDown(Event e) {
        sourceItem = tabFolder.getItem(new Point(e.x, e.y));
    }

    private void handleMouseUp(Event e) {
        if (sourceItem != null && insertionMarkIndex >= 0) {
            int from = tabFolder.indexOf(sourceItem);
            int to = insertionMarkIndex;//tabFolder.indexOf( over );
            if (insertionMarkIndex > tabFolder.getSelectionIndex())
                to--;
            if (from != to) {
                firePageMoved(from, to);
            }
            tabFolder.setSelection(to);
            tabFolder.redraw();
        }
        cancel();
    }

    private void handleMouseMove(Event e) {
        if ((e.stateMask & SWT.BUTTON_MASK) != 0 && sourceItem != null) {
            tabFolder.setCursor(Display.getCurrent().getSystemCursor(
                    SWT.CURSOR_HAND));
            int oldInsertIndex = insertionMarkIndex;
            int newInsertIndex = findInsertionMarkIndex(e.x, e.y);
            if (newInsertIndex != oldInsertIndex) {
                insertionMarkIndex = newInsertIndex;
                updateArrow(insertionMarkIndex);
                tabFolder.redraw();
            }
        } else {
            cancel();
        }
    }

    private void handleDispose(Event e) {
        disposeArrow();
        disposeArrowShape();
    }

    private void updateArrow(int insertIndex) {
        createArrow();
        Point loc = calcInsertionLocation(insertIndex, true);
        if (loc != null) {
            arrow.setVisible(true);
            arrow.setLocation(loc.x - W / 2, loc.y - H - 3);
        } else {
            arrow.setVisible(false);
        }
    }

    private void createArrow() {
        if (arrow != null && !arrow.isDisposed())
            return;

        arrow = new Shell(tabFolder.getShell(), SWT.ON_TOP | SWT.NO_TRIM);
        arrow.setRegion(getArrowShape());
        arrow.setBackground(ColorUtils.getColor("#4088ff")); //$NON-NLS-1$
        arrow.setSize(W + 1, H + 1);
        arrow.addListener(SWT.Paint, new Listener() {
            public void handleEvent(Event event) {
                GC gc = event.gc;
                gc.setForeground(ColorUtils.getColor("#000080")); //$NON-NLS-1$
                gc.setLineStyle(SWT.LINE_SOLID);
                gc.setLineWidth(1);
                gc.drawPolygon(BORDER);
            }
        });
    }

    private Region getArrowShape() {
        if (arrowShape == null || arrowShape.isDisposed()) {
            arrowShape = new Region(tabFolder.getDisplay());
            arrowShape.add(SHAPE);
        }
        return arrowShape;
    }

    private void disposeArrow() {
        if (arrow != null) {
            arrow.dispose();
            arrow = null;
        }
    }

    private void disposeArrowShape() {
        if (arrowShape != null) {
            arrowShape.dispose();
            arrowShape = null;
        }
    }
}