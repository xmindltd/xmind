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

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.xmind.gef.IViewer;
import org.xmind.gef.part.IPart;

/**
 * @author Brian Sun
 */
public class TreeRootPart extends TreePart implements ITreeRootPart {

    private IViewer viewer = null;

    private ITreePart contents = null;

    public void setViewer(IViewer viewer) {
        this.viewer = viewer;
    }

    public IViewer getViewer() {
        return viewer;
    }

    public IPart getContents() {
        return contents;
    }

    public void setContents(IPart part) {
        IViewer viewer = getSite().getViewer();
        if (viewer instanceof ITreeViewer) {
            ((ITreeViewer) viewer).scheduleRedraw();
        }
        if (contents != null) {
            if (getWidget() != null)
                ((Tree) getWidget()).removeAll();
            removeChild(contents);
        }
        contents = (ITreePart) part;

        if (contents != null)
            addChild(contents, -1);
    }

    protected void refreshChildren() {
        // do nothing
    }

    protected void preSetWidget(Widget widget) {
        if (widget != null) {
            for (IPart child : getChildren()) {
                ITreePart treeChild = (ITreePart) child;
                treeChild.setWidget(new TreeItem((Tree) widget, 0));

                //We have just assigned a new TreeItem to the EditPart
                treeChild.refresh();
            }
        } else {
            for (IPart child : getChildren()) {
                ((ITreePart) child).setWidget(null);
            }
        }
    }

}