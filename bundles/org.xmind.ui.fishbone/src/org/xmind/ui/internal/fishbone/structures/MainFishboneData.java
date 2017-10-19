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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.draw2d.geometry.Point;
import org.xmind.gef.draw2d.geometry.HorizontalFlipper;
import org.xmind.gef.draw2d.geometry.IPrecisionTransformer;
import org.xmind.gef.draw2d.geometry.ITransformer;
import org.xmind.gef.draw2d.geometry.PrecisionHorizontalFlipper;
import org.xmind.gef.draw2d.geometry.PrecisionVerticalFlipper;
import org.xmind.ui.branch.BranchStructureData;
import org.xmind.ui.mindmap.IBranchPart;

public class MainFishboneData extends BranchStructureData {

    private Set<Integer> upwardBranches = null;

    public final ITransformer hf = new HorizontalFlipper();

    public final IPrecisionTransformer phf = new PrecisionHorizontalFlipper();

    public final IPrecisionTransformer pvf = new PrecisionVerticalFlipper();

    public final Side upSide = new Side();

    public final Side downSide = new Side();

    public MainFishboneData(IBranchPart branch, boolean transformerEnabled) {
        super(branch);
        this.hf.setEnabled(transformerEnabled);
        this.phf.setEnabled(transformerEnabled);
    }

    public void setOrigin(Point origin) {
        hf.setOrigin(origin);
        phf.setOrigin(origin.x, origin.y);
        pvf.setOrigin(phf.getOrigin());
    }

    public boolean isUpwardBranch(int index) {
        return getUpwardBranches().contains(index);
    }

    private Set<Integer> getUpwardBranches() {
        if (upwardBranches == null) {
            upwardBranches = calcUpwardBranches();
        }
        return upwardBranches;
    }

    private Set<Integer> calcUpwardBranches() {
        HashSet<Integer> set = new HashSet<Integer>();
        int i = 0;
        IBranchPart lastChild = null;
        boolean upwards = true;
        for (IBranchPart subBranch : getBranch().getSubBranches()) {
            if (lastChild == null) {
                set.add(i);
            } else {
                if (!isInSameRange(lastChild, subBranch)) {
                    upwards = !upwards;
                }
                if (upwards)
                    set.add(i);
            }
            lastChild = subBranch;
            i++;
        }
        return set;
    }
}