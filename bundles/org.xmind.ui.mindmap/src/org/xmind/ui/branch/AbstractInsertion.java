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
package org.xmind.ui.branch;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.ui.mindmap.IBranchPart;

public abstract class AbstractInsertion implements IInsertion {

    private IBranchPart parent;

    private int index;

    private Dimension size;

    public AbstractInsertion(IBranchPart parent, int index, Dimension size) {
        this.parent = parent;
        this.index = index;
        this.size = new Dimension(size);
    }

    public int getIndex() {
        return index;
    }

    public IBranchPart getParent() {
        return parent;
    }

    public Dimension getSize() {
        return size;
    }

    public Rectangle createRectangle(int x, int y) {
        return new Rectangle(x, y, size.width, size.height);
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof AbstractInsertion))
            return false;
        AbstractInsertion that = (AbstractInsertion) obj;
        return this.parent == that.parent && this.index == that.index
                && this.size.equals(that.size);
    }

}