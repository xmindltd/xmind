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
package org.xmind.ui.internal.editpolicies;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.draw2d.geometry.AlignmentSolver;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.geometry.IIntersectionSolver;
import org.xmind.gef.draw2d.geometry.IPositionSolver;
import org.xmind.gef.draw2d.geometry.SplitIntersectionSolver;
import org.xmind.gef.policy.GraphicalPartBoundsProvider;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class TopicAlignmentSolver extends AlignmentSolver {

    private IPositionSolver intersectionSolver = null;

    private Collection<Object> refKeys = null;

    public TopicAlignmentSolver(int alignmentHint) {
        super(alignmentHint);
        setDefaultBoundsProvider(GraphicalPartBoundsProvider.getDefault());
    }

    public void recordInitPositions(List<ITopicPart> topics) {
        Collection<Object> steadyTopics = null;
        Collection<Object> freeTopics = null;
        Collection<Object> floatingTopics = null;
        for (ITopicPart topic : topics) {
            IBranchPart branch = topic.getOwnerBranch();
            boolean floating = branch.getParentBranch() == null
                    && !branch.isCentral();
            String category;
            if (floating || MindMapUtils.isBranchFree(branch)) {
                category = IIntersectionSolver.CATEGORY_FREE;
                if (floating) {
                    if (steadyTopics == null && freeTopics == null) {
                        if (floatingTopics == null)
                            floatingTopics = new HashSet<Object>();
                        floatingTopics.add(topic);
                    }
                } else {
                    if (steadyTopics == null) {
                        if (freeTopics == null) {
                            freeTopics = new HashSet<Object>();
                            floatingTopics = null;
                        }
                        freeTopics.add(topic);
                    }
                }
            } else {
                category = IIntersectionSolver.CATEGORY_STEADY;
                if (steadyTopics == null) {
                    steadyTopics = new HashSet<Object>();
                    floatingTopics = null;
                    freeTopics = null;
                }
                steadyTopics.add(topic);
            }
            if (steadyTopics != null) {
                refKeys = steadyTopics;
            } else if (freeTopics != null) {
                refKeys = freeTopics;
            } else if (floatingTopics != null) {
                refKeys = floatingTopics;
            } else {
                refKeys = null;
            }
            recordInitPosition(topic, ((IReferencedFigure) topic.getFigure())
                    .getReference(), category, false);
        }
    }

    public void setAlignmentHint(int alignmentHint) {
        super.setAlignmentHint(alignmentHint);
        if (intersectionSolver instanceof SplitIntersectionSolver) {
            ((SplitIntersectionSolver) intersectionSolver)
                    .setHorizontal(!isHorizontal());
        }
    }

    public void clear() {
        super.clear();
        refKeys = null;
    }

    public void solve() {
        super.solve();
        if (!MindMapUI.isOverlapsAllowed()) {
            if (intersectionSolver == null) {
                intersectionSolver = new SplitIntersectionSolver(
                        !isHorizontal());
                intersectionSolver
                        .setDefaultBoundsProvider(GraphicalPartBoundsProvider
                                .getDefault());
            }
            intersectionSolver.setOrigin(calcIntersectionOrigin());
            for (String category : getCategories()) {
                for (Object key : getKeys(category)) {
                    ITopicPart topicPart = (ITopicPart) key;
                    IBranchPart branch = topicPart.getOwnerBranch();
                    intersectionSolver.recordInitPosition(branch,
                            getSolvedPosition(key), category, true);
                }
            }
            intersectionSolver.solve();
            intersectionSolver.clear();
        }
    }

    private Point calcIntersectionOrigin() {
        Rectangle r = null;
        for (Object key : getKeys()) {
            r = Geometry.union(r, getSolvedBounds(key));
        }
        if (r != null)
            return r.getCenter();
        return getOrigin();
    }

    protected Collection<Object> getReferenceKeys() {
        if (refKeys != null)
            return refKeys;
        return super.getReferenceKeys();
    }

}