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

import java.util.List;

import org.xmind.gef.part.IPart;
import org.xmind.ui.mindmap.IBranchPart;

public interface INavigableBranchStructureExtension {

    /**
     * 
     * @param branch
     * @param navReqType
     * @return
     */
    IPart calcNavigation(IBranchPart branch, String navReqType);

    /**
     * 
     * @param branch
     * @param sourceChild
     * @param navReqType
     * @param sequential
     * @return
     */
    IPart calcChildNavigation(IBranchPart branch, IBranchPart sourceChild,
            String navReqType, boolean sequential);

    /**
     * 
     * @param branch
     * @param navReqType
     * @param sourceChild
     * @param startChild
     * @param results
     */
    void calcSequentialNavigation(IBranchPart branch, IBranchPart startChild,
            IBranchPart endChild, List<IBranchPart> results);

    /**
     * 
     * @param branch
     * @param results
     */
    void calcTraversableChildren(IBranchPart branch, List<IBranchPart> results);

    /**
     * 
     * @param branch
     * @param sourceChild
     * @param results
     */
    void calcTraversableBranches(IBranchPart branch, IBranchPart sourceChild,
            List<IBranchPart> results);

}