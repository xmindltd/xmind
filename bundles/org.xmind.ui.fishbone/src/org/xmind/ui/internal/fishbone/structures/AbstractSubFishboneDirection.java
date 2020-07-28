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
package org.xmind.ui.internal.fishbone.structures;

import org.xmind.gef.draw2d.geometry.IPrecisionTransformer;
import org.xmind.gef.draw2d.geometry.PrecisionRotator;
import org.xmind.ui.mindmap.IBranchPart;

public abstract class AbstractSubFishboneDirection implements ISubDirection {

    private boolean rotated;

    private boolean downwards;

    private boolean rightHeaded;

    private boolean childrenTraverseReversed;

    private double rotateAngle;

    private int sourceOrientation;

    private int childTargetOrientation;

//    private String childNavType;

    protected AbstractSubFishboneDirection(double rotateAngle,
            boolean downwards, boolean rightHeaded,
            boolean childrenTraverseReversed, int sourceOrientation,
            int childTargetOrientation) {
        this.rotated = true;
        this.rotateAngle = rotateAngle;
        this.downwards = downwards;
        this.rightHeaded = rightHeaded;
        this.childrenTraverseReversed = childrenTraverseReversed;
        this.sourceOrientation = sourceOrientation;
        this.childTargetOrientation = childTargetOrientation;
//        this.childNavType = childNavType;
    }

    protected AbstractSubFishboneDirection(boolean downwards,
            boolean rightHeaded, boolean childrenTraverseReversed,
            int sourceOrientation, int childTargetOrientation) {
        this.rotated = false;
        this.rotateAngle = 0;
        this.downwards = downwards;
        this.rightHeaded = rightHeaded;
        this.childrenTraverseReversed = childrenTraverseReversed;
        this.sourceOrientation = sourceOrientation;
        this.childTargetOrientation = childTargetOrientation;
//        this.childNavType = childNavType;
    }

    public int getSourceOrientation() {
        return sourceOrientation;
    }

    public int getChildTargetOrientation() {
        return childTargetOrientation;
    }

    public double getRotateAngle() {
        return rotateAngle;
    }

    public boolean isRotated() {
        return rotated;
    }

    public boolean isChildrenTraverseReversed() {
        return childrenTraverseReversed;
    }

    public boolean isDownwards() {
        return downwards;
    }

    public boolean isRightHeaded() {
        return rightHeaded;
    }

//    public String getChildNavigationType() {
//        return childNavType;
//    }
    public void fillFishboneExtraData(IBranchPart branch, FishboneData data,
            IPrecisionTransformer h, PrecisionRotator r, double spacing,
            boolean extraBranch) {
    }

}