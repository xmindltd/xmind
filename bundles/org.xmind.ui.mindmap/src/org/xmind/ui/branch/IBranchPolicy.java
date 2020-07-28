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

import org.xmind.gef.graphicalpolicy.IGraphicalPolicy;
import org.xmind.ui.mindmap.IBranchPart;

public interface IBranchPolicy extends IGraphicalPolicy {

    /**
     * Cache key for the current structure algorithm id ({@link String}) on a
     * branch.
     */
    String CACHE_STRUCTURE_ID = "org.xmind.ui.branchCache.structureId"; //$NON-NLS-1$

    void flushStructureCache(IBranchPart branch, boolean includeAncestors,
            boolean includeDescendants);

    /**
     * Tests whether or not the specified property of the given part can be
     * modified by users.
     * <p>
     * Normally this method simply returns <code>true</code> to let users do
     * most of the modifications, but it may return <code>false</code> under
     * some specific circumstances, e.g., every top branch is agreed to be
     * always unfolded, so the <code>folded</code> property is restricted to
     * unmodifiable on a central branch part.
     * </p>
     * 
     * @param part
     *            The part; must not be <code>null</code>.
     * @param propertyKey
     *            The key for the property; must not be <code>null</code>.
     * @return Whether the specified property of the part is modifiable.
     */
    boolean isPropertyModifiable(IBranchPart branch, String propertyKey);

    /**
     * Indicates whether or not the specified property of the branch part can be
     * modified by users.
     * <p>
     * Normally this method simply returns <code>true</code> to let users do
     * most of the modifications, but it may return <code>false</code> under
     * some specific circumstances, e.g., every top branch is agreed to be
     * always unfolded, so the <code>folded</code> property of them is
     * restricted to unmodifiable.
     * </p>
     * 
     * @param branch
     *            The branch part; must not be <code>null</code>.
     * @param propertyKey
     *            The key for the property; must not be <code>null</code>.
     * @param secondaryKey
     *            The secondary key for the property; may be <code>null</code>.
     *            The secondary key is used to represent a more accurate
     *            identification of the desired property. For example, to
     *            specify the <code>shape</code> <code>style</code> property,
     *            use <code>style</code> as a property key and
     *            <code>shape</code> as a secondary key.
     * @return Whether the specified property of the branch is modifiable.
     */
    boolean isPropertyModifiable(IBranchPart branch, String propertyKey,
            String secondaryKey);

//    /**
//     * 
//     * @param parent
//     * @param type
//     * @return
//     */
//    IToolHelper getToolHelper(IBranchPart parent,
//            Class<? extends IToolHelper> type);

    void postDeactivate(IBranchPart branch);

}