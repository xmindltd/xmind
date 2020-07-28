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
package org.xmind.gef.tree;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.xmind.gef.AbstractViewer;
import org.xmind.gef.part.IPart;

/**
 * @author Brian Sun
 */
public class TreeViewer extends AbstractViewer implements ITreeViewer {

    private boolean schedulingRedraw = false;

    /**
     * @param
     */
    public TreeViewer() {
    }

    public Tree getTree() {
        return (Tree) getControl();
    }

    protected void revealParts(List<? extends IPart> parts) {
        for (IPart part : parts) {
            if (part instanceof ITreePart && part.getStatus().isActive()) {
                ITreePart tp = (ITreePart) part;
                if (tp.getWidget() instanceof TreeItem) {
                    TreeItem item = (TreeItem) tp.getWidget();
                    ensureVisible(item);
                    getTree().showItem(item);
                }
            }
        }
    }

    protected void ensureVisible(TreeItem item) {
        TreeItem parentItem = item.getParentItem();
        while (parentItem != null) {
            parentItem.setExpanded(true);
            parentItem = parentItem.getParentItem();
        }
    }

    protected Control internalCreateControl(Composite parent, int style) {
        Tree tree = new Tree(parent, style);
        tree.setHeaderVisible(getProperties().getBoolean(PROP_HEADER_VISIBLE,
                true));
        tree.setLinesVisible(getProperties().getBoolean(PROP_LINES_VISIBLE,
                true));
        return tree;
    }

    protected void hookControl(final Control control) {
        super.hookControl(control);
        control.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                updateSelection();
            }
        });
        ((ITreeRootPart) getRootPart()).setWidget(control);
    }

    protected void updateSelection() {
        TreeItem[] selectedItems = getTree().getSelection();
        List<IPart> selectedParts = new ArrayList<IPart>(selectedItems.length);
        for (TreeItem item : selectedItems) {
            Object data = item.getData();
            if (data instanceof IPart) {
                selectedParts.add((IPart) data);
            }
        }
        getSelectionSupport().setSelection(
                new StructuredSelection(selectedParts), true);
    }

    @Override
    public void inputChanged(Object oldInput, Object newInput) {
        Control c = getControl();
        if (c != null && !c.isDisposed()) {
            c.setRedraw(false);
        }
        super.inputChanged(oldInput, newInput);
        if (c != null && !c.isDisposed()) {
            c.setRedraw(true);
        }
    }

    public void scheduleRedraw() {
        if (Util.isMac())
            // Redraw scheduling performs awefully on OS X.
            return;

        Control c = getControl();
        if (c == null || c.isDisposed())
            return;

        if (schedulingRedraw)
            return;

        schedulingRedraw = true;
        c.setRedraw(false);
        Display.getCurrent().asyncExec(new Runnable() {

            public void run() {
                Control c = getControl();
                if (c != null && !c.isDisposed()) {
                    c.setRedraw(true);
                }
                schedulingRedraw = false;
            }

        });
    }

}