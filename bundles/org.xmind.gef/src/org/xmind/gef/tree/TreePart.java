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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.xmind.gef.IViewer;
import org.xmind.gef.part.EditPart;
import org.xmind.gef.part.IPart;

/**
 * @author Brian Sun
 */
public class TreePart extends EditPart implements ITreePart {

    private Widget widget = null;

    private boolean expanded = false;

    public Widget getWidget() {
        return widget;
    }

    public void setWidget(Widget widget) {
        preSetWidget(widget);
        this.widget = widget;
    }

    protected void preSetWidget(Widget widget) {
        if (widget != null) {
            widget.setData(this);
            if (widget instanceof TreeItem) {
                final TreeItem item = (TreeItem) widget;
                item.addDisposeListener(new DisposeListener() {
                    public void widgetDisposed(DisposeEvent e) {
                        expanded = item.getExpanded();
                    }
                });
                if (needsVisibleOnCreating()) {
                    ensureVisible(item);
                }
            }
            for (IPart child : getChildren()) {
                ITreePart treeChild = (ITreePart) child;
                if (widget instanceof TreeItem)
                    treeChild.setWidget(new TreeItem((TreeItem) widget, 0));
                else
                    treeChild.setWidget(new TreeItem((Tree) widget, 0));

                //We have just assigned a new TreeItem to the EditPart
                treeChild.refresh();
            }
            if (widget instanceof TreeItem)
                ((TreeItem) widget).setExpanded(expanded);
        } else {
            for (IPart child : getChildren()) {
                ((ITreePart) child).setWidget(null);
            }
        }
    }

    protected boolean needsVisibleOnCreating() {
        return true;
    }

    protected void ensureVisible(TreeItem item) {
        TreeItem p = item.getParentItem();
        if (p != null) {
            if (!p.getExpanded())
                p.setExpanded(true);
            ensureVisible(p);
        } else
            return;
    }

    protected void addChildView(IPart child, int index) {
        IViewer viewer = getSite().getViewer();
        if (viewer instanceof ITreeViewer) {
            ((ITreeViewer) viewer).scheduleRedraw();
        }
        Widget widget = getWidget();
        TreeItem item;
        if (widget instanceof Tree) {
            item = new TreeItem((Tree) widget, SWT.NONE, index);
        } else {
            item = new TreeItem((TreeItem) widget, SWT.NONE, index);
        }
        ((ITreePart) child).setWidget(item);
    }

    protected void removeChildView(IPart child) {
        IViewer viewer = getSite().getViewer();
        if (viewer instanceof ITreeViewer) {
            ((ITreeViewer) viewer).scheduleRedraw();
        }
        ITreePart treeChild = (ITreePart) child;
        treeChild.getWidget().dispose();
        treeChild.setWidget(null);
    }

    protected void updateView() {
        super.updateView();
        setWidgetImage(getImage());
        setWidgetText(getText());
    }

    protected void reorderChild(IPart child, int index) {
        super.reorderChild(child, index);
        child.refresh();
    }

    protected boolean isValidTreeItem() {
        return widget != null && !widget.isDisposed()
                && widget instanceof TreeItem;
    }

    protected void setWidgetImage(Image image) {
        if (isValidTreeItem()) {
            ((TreeItem) widget).setImage(image);
        }
    }

    protected void setWidgetText(String text) {
        if (isValidTreeItem()) {
            ((TreeItem) widget).setText(text);
        }
    }

    protected Image getImage() {
        return null;
    }

    protected String getText() {
        return ""; //$NON-NLS-1$
    }

}