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

import org.eclipse.draw2d.geometry.Point;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.tools.ParentSearchKey;

public interface IInsertableBranchStructureExtension {

    /**
     * 
     * @param branch
     * @param key
     * @return
     */
    int calcChildDistance(IBranchPart branch, ParentSearchKey key);

    /**
     * 
     * @param key
     * @return
     */
    int calcChildIndex(IBranchPart branch, ParentSearchKey key);

    /**
     * 
     * @param branch
     * @param key
     * @return
     */
    IInsertion calcInsertion(IBranchPart branch, ParentSearchKey key);

    /**
     * 
     * @param branch
     * @param child
     * @param key
     * @return
     */
    Point calcInsertionPosition(IBranchPart branch, IBranchPart child,
            ParentSearchKey key);

    /**
     * 
     * @param branch
     * @param child
     * @param key
     * @return
     */
    boolean isBranchMoved(IBranchPart branch, IBranchPart child,
            ParentSearchKey key);

}