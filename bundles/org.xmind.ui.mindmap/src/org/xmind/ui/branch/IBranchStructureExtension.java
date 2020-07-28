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

import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IBranchRangePart;
import org.xmind.ui.mindmap.ISummaryPart;

public interface IBranchStructureExtension {

    /**
     * 
     * @param branch
     * @return
     */
    int getSourceOrientation(IBranchPart branch);

    /**
     * 
     * @param branch
     * @param subBranch
     * @return
     */
    int getChildTargetOrientation(IBranchPart branch, IBranchPart subBranch);

    /**
     * 
     * @param branch
     * @param range
     * @return
     */
    int getRangeGrowthDirection(IBranchPart branch, IBranchRangePart range);

    /**
     * 
     * @param branch
     * @param summary
     * @return
     */
    int getSummaryDirection(IBranchPart branch, ISummaryPart summary);

    /**
     * 
     * @param branch
     * @param child
     * @param direction
     * @return
     */
    int getQuickMoveOffset(IBranchPart branch, IBranchPart child, int direction);

}