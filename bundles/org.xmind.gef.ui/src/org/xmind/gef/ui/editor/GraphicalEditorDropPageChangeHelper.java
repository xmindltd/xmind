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
package org.xmind.gef.ui.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * @author Frank Shaka
 */
public class GraphicalEditorDropPageChangeHelper {

    private class PageChangeTask implements Runnable {
        private boolean canceled = false;

        public void run() {
            if (canceled)
                return;
            changePage();
            currentTask = null;
        }

        public void cancel() {
            this.canceled = true;
        }
    }

    private static final int PAGE_CHANGE_DELAY = 1000;

    private IGraphicalEditor editor;

    private CTabFolder tabFolder;

    private DropTarget dropTarget;

    private DropTargetListener handler;

    private CTabItem currentItem = null;

    private PageChangeTask currentTask = null;

    public GraphicalEditorDropPageChangeHelper(IGraphicalEditor editor,
            CTabFolder tabFolder, int style, Transfer[] acceptableTransfers) {
        this.editor = editor;
        this.tabFolder = tabFolder;
        createDropTarget(style, acceptableTransfers);
    }

    private void createDropTarget(int style, Transfer[] acceptableTransfers) {
        dropTarget = new DropTarget(tabFolder, style);
        if (acceptableTransfers != null) {
            dropTarget.setTransfer(acceptableTransfers);
        }
        handler = createHandler();
        dropTarget.addDropListener(handler);
        hookTabFolder();
    }

    private void hookTabFolder() {
        tabFolder.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
                if (currentItem != null) {
                    GC gc = e.gc;
                    if (gc != null) {
                        gc.setAlpha(0x30);
                        gc.setBackground(e.display
                                .getSystemColor(SWT.COLOR_LIST_SELECTION));
                        //gc.setForeground( tabFolder.getDisplay().getSystemColor( SWT.COLOR_RED ) );
                        Rectangle r = currentItem.getBounds();
                        //gc.drawRectangle( r.x, r.y, r.width - 1, r.height - 1 );
                        gc.fillRectangle(r);
                    }
                }
            }
        });
        tabFolder.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                dispose();
            }
        });
    }

    private DropTargetListener createHandler() {
        return new DropTargetAdapter() {
            @Override
            public void dragOver(DropTargetEvent event) {
                CTabItem oldItem = currentItem;
                CTabItem newItem = findItem(event.x, event.y);
                if (newItem != oldItem) {
                    cancelUndoneTask();
                    if (newItem != tabFolder.getSelection()) {
                        currentItem = newItem;
                        if (currentItem != null) {
                            startNewTask();
                        }
                        redrawTabFolder();
                    } else {
                        currentItem = null;
                        if (oldItem != null)
                            redrawTabFolder();
                    }
                }
                event.detail = DND.DROP_COPY;
            }

            @Override
            public void dragLeave(DropTargetEvent event) {
                cancelUndoneTask();
                boolean needRedraw = currentItem != null;
                currentItem = null;
                if (needRedraw)
                    redrawTabFolder();
            }

        };
    }

    private void startNewTask() {
        currentTask = new PageChangeTask();
        tabFolder.getDisplay().timerExec(PAGE_CHANGE_DELAY, currentTask);
    }

    private void redrawTabFolder() {
        tabFolder.redraw();
    }

    private void cancelUndoneTask() {
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
        }
    }

    protected void changePage() {
        if (currentItem == null)
            return;
        int pageIndex = tabFolder.indexOf(currentItem);
        editor.setActivePage(pageIndex);
        currentItem = null;
        redrawTabFolder();
    }

    private CTabItem findItem(int x, int y) {
        Point location = tabFolder.toControl(x, y);
        return tabFolder.getItem(location);
    }

    protected void dispose() {
        cancelUndoneTask();
        if (dropTarget != null) {
            if (handler != null && !dropTarget.isDisposed()) {
                dropTarget.removeDropListener(handler);
            }
            dropTarget.dispose();
        }
        dropTarget = null;
        handler = null;
        tabFolder = null;
        editor = null;
    }

}