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
package org.xmind.ui.mindmap;

import java.util.List;

import org.xmind.core.ITopic;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.branch.IBranchPolicy;
import org.xmind.ui.decorations.IBranchConnections;
import org.xmind.ui.decorations.IBranchConnections2;

/**
 * 
 * 
 * 
 * @author Frank Shaka
 * 
 */
public interface IBranchPart extends IGraphicalPart {

    /**
     * Cache key for the preferred position (
     * {@link org.eclipse.draw2d.geometry.Point}) of this branch.
     * <p>
     * This value is mainly cached by the branch itself as the topic model's
     * <i>position</i> changed. The branch converts the position model into a
     * graphical point that's relative to the viewer's original point (i.e. the
     * central topic figure's reference point). The parent branch's structure
     * algorithm may use this value to determine this branch's actual position
     * and size in a <i>layout</i> process.
     * </p>
     * <p>
     * NOTE: The value may be <code>null</code> to indicate an unspecified
     * preferred position.
     * </p>
     * <p>
     * Example:
     * 
     * <pre>
     * IBranchPart branch = ....
     * Point position = (Point) branch.getCacheManager()
     *                  .getCache(CACHE_PREF_POSITION);
     * ..... // do things with this position
     * </pre>
     * 
     * </p>
     */
    String CACHE_PREF_POSITION = "org.xmind.ui.cache.preferredPosition"; //$NON-NLS-1$

    /**
     * Cache key for folded state (<code>true</code>, <code>false</code>) that
     * overrides the topic model's folded state and only take effects in viewer,
     * i.e., chaning this state won't affect the topic model. Setting this value
     * to <code>null</code> will clear the overrided state.
     * <p>
     * NOTE: Clients that sets this cache should call
     * {@link #treeUpdate(boolean)} on their own to get correct visual results.
     * </p>
     */
    String CACHE_FOLDED = "org.xmind.ui.cache.folded"; //$NON-NLS-1$

    ITopic getTopic();

    ITopicPart getTopicPart();

    List<IBranchPart> getSubBranches();

    IPlusMinusPart getPlusMinus();

    List<IBoundaryPart> getBoundaries();

    List<ISummaryPart> getSummaries();

    List<IBranchPart> getSummaryBranches();

    ILabelPart getLabel();

    IInfoPart getInfoPart();

    String getBranchType();

    boolean isFolded();

    boolean canSearchChild();

    int getBranchIndex();

    IBranchPolicy getBranchPolicy();

    String getBranchPolicyId();

//    ICacheManager getCacheManager();

    IBranchConnections getConnections();

    IBranchPart getParentBranch();

    boolean isCentral();

    boolean isPropertyModifiable(String propertyName);

    boolean isPropertyModifiable(String propertyName, String secondaryKey);

    void treeUpdate(boolean updateParent);

    void treeRefresh();

    List<IBranchPart> getCalloutBranches();

    IBranchConnections2 getCalloutConnections();

}