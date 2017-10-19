/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.fishbone.structures;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.draw2d.geometry.PrecisionInsets;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.ui.mindmap.IBranchPart;

public class FishboneData {

    public PrecisionInsets topicRefIns;

    public PrecisionInsets rTopicRefIns;

    public PrecisionInsets branchRefIns;

    public PrecisionInsets rBranchRefIns;

    private Map<IBranchPart, PrecisionPoint> childOffsets = null;

    public void addChildOffset(IBranchPart subBranch, PrecisionPoint offset) {
        if (childOffsets == null)
            childOffsets = new HashMap<IBranchPart, PrecisionPoint>();
        childOffsets.put(subBranch, offset);
    }

    private PrecisionPoint getChildOffset(IBranchPart subBranch) {
        return childOffsets == null ? null : childOffsets.get(subBranch);
    }

    private PrecisionPoint getChildPrefRef(IBranchPart subBranch,
            PrecisionPoint ref) {

        PrecisionPoint offset = getChildOffset(subBranch);
        return offset == null ? null : ref.getTranslated(offset);
    }

    public Rectangle getChildPrefBounds(IBranchPart subBranch,
            PrecisionPoint ref) {

        PrecisionPoint childRef = getChildPrefRef(subBranch, ref);
        if (childRef != null) {
            Point draw2DPoint = childRef.toDraw2DPoint();
            IReferencedFigure figure = (IReferencedFigure) subBranch
                    .getFigure();

            Rectangle bounds = figure.getPreferredBounds(draw2DPoint);

            return bounds;
        }
        return null;
    }

}