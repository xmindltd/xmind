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
package org.xmind.ui.internal.branch;

import static java.lang.Math.log;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;
import static org.xmind.ui.internal.branch.BaseRadialStructure.CACHE_NUMBER_RIGHT_BRANCHES;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.xmind.ui.branch.BranchStructureData;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.util.MindMapUtils;

public class RadialData extends BranchStructureData {

    private static final int Threshold = 200;

    private int defaultSumSpacing = -1;
    private int defaultMinSumSpacing = -1;

    private int numRight = -1;
    private Dimension ovalSize = null;
    private int sumSpacing = -1;
    private int rightSumSpacing = -1;
    private int rightSpacingAddIn = -1;
    private int leftSumSpacing = -1;
    private int leftSpacingAddIn = -1;

    private int[] childrenSpacing = null;

    private Map<IBranchPart, Integer> weights = null;

    public RadialData(IBranchPart branch) {
        super(branch);
    }

    public int getDefaultMinSumSpacing() {
        if (defaultMinSumSpacing < 0) {
            defaultMinSumSpacing = calculateDefaultMinSumSpacing();
        }
        return defaultMinSumSpacing;
    }

    private int calculateDefaultMinSumSpacing() {
        return getMinorSpacing2() * 8;
    }

    public int getDefaultSumSpacing() {
        if (defaultSumSpacing < 0) {
            defaultSumSpacing = calculateDefaultSumSpacing();
        }
        return defaultSumSpacing;
    }

    private int calculateDefaultSumSpacing() {
        return getMinorSpacing2() * 13;
    }

    public int getNumRight() {
        Integer num = getCachedNumRight();
        if (num != null)
            return num.intValue();
        if (numRight < 0) {
            numRight = calculateNumRight();
        }
        return numRight;
    }

    private int calculateNumRight() {
        int num = getSubBranches().size();
        if (num > 1) {
            int totalWeight = getTotalWeight();
            num = calculateNumRight(getSplitWeight(totalWeight), totalWeight);
//            if (totalWeight < getThreshold(num)) {
//                num = max(2, num);
//            }
        }
        return num;
    }

    private Integer getCachedNumRight() {
        return (Integer) MindMapUtils.getCache(getBranch(),
                CACHE_NUMBER_RIGHT_BRANCHES);
    }

    private int getThreshold(int num) {
        return (int) (Threshold * (log(num) + 1));
    }

    private boolean isWithinThreshold(int index, int size) {
        return isWithinThreshold(getSubBranches().get(index), size);
    }

    private boolean isWithinThreshold(IBranchPart subBranch, int size) {
        return getWeight(subBranch) < getThreshold(size);
    }

    private int calculateNumRight(int halfWeight, int totalWeight) {
        int index = 0;
        int lastIndex = -1;
        int rightWeight = 0;
        int blockWeight = 0;
        List<IBranchPart> subBranches = getSubBranches();
        int size = subBranches.size();
        for (IBranchPart subBranch : subBranches) {
            blockWeight += getWeight(subBranch);
            int num = index + 1;
            if (!isInSameRangeWithLast(subBranches, index + 1)) {
                int newRightWeight = rightWeight + blockWeight;
                if (newRightWeight >= halfWeight) {
                    if (lastIndex >= 0 && newRightWeight
                            - halfWeight > halfWeight - rightWeight) {
                        int lastNum = lastIndex + 1;
                        if (index == 1 && lastIndex == 0 //
                                && (isInSameRangeWithLast(subBranches, index) //
                                        || (isWithinThreshold(0, size) //
                                                && isWithinThreshold(subBranch,
                                                        size))))
                            return 2;
                        return lastNum;
                    }
                    if (index == 0 && isWithinThreshold(subBranch, size)
                            && ((size == 2 //
                                    || (size > 2 //
                                            && !isInSameRangeWithLast(
                                                    subBranches, 2))) //
                                    && isWithinThreshold(1, size))) {
                        return 2;
                    }
                    return num;
                }
                rightWeight = newRightWeight;
                blockWeight = 0;
                lastIndex = index;
            }
            index++;
        }
        return index;
    }

    private int getTotalWeight() {
        int weight = 0;
        for (IBranchPart subbranch : getSubBranches()) {
            weight += getWeight(subbranch);
        }
        return weight;
    }

    private int getWeight(IBranchPart branch) {
        if (weights == null) {
            weights = new HashMap<IBranchPart, Integer>();
        }
        Integer weight = weights.get(branch);
        if (weight == null) {
            weight = branch.getFigure().getPreferredSize().height
                    + getMinorSpacing2() * 2;
            weights.put(branch, weight);
        }
        return weight.intValue();
    }

    private int getSplitWeight(int weight) {
        return weight - (weight / 2);
    }

    public int getNumLeft() {
        return getSubBranches().size() - getNumRight();
    }

    public Dimension getOvalSize() {
        if (ovalSize == null) {
            ovalSize = calculateOvalSize();
        }
        return ovalSize;
    }

    private Dimension calculateOvalSize() {
        int a;
        ITopicPart topic = getBranch().getTopicPart();
        if (topic != null) {
            a = topic.getFigure().getPreferredSize().width / 2;
        } else {
            a = 0;
        }
        int b = getSumSpacing() / 2;
        int width = a + getMajorSpacing2();
        int height = (int) (b / sqrt(1 - (a * a * 1.0) / (width * width)));
        return new Dimension(width, height);
    }

    public int getSumSpacing() {
        if (sumSpacing < 0) {
            sumSpacing = calculateSumSpacing();
        }
        return sumSpacing;
    }

    private int calculateSumSpacing() {
        return max(max(getRightSumSpacing(), getLeftSumSpacing()),
                getDefaultSumSpacing());
    }

    public int getRightSumSpacing() {
        if (rightSumSpacing < 0) {
            rightSumSpacing = calculateRightSumSpacing();
        }
        return rightSumSpacing;
    }

    private int calculateRightSumSpacing() {
        return calculateHalfSumSpacing(getNumRight(), true,
                getRightSpacingAddIn());
    }

    private int calculateHalfSumSpacing(int numHalf, boolean firstOrSecond,
            int addIn) {
        if (numHalf <= 0)
            return 0;
        if (numHalf == 1)
            return getDefaultMinSumSpacing();
        return calculateSumSpacing(firstOrSecond, addIn);
    }

    private int calculateSumSpacing(boolean firstOrSecond, int addIn) {
        int start = firstOrSecond ? 0 : getNumRight();
        int num = firstOrSecond ? getNumRight() : getNumLeft();
        int sum = 0;
        for (int i = start; i < start + num - 1; i++) {
            sum += calculateChildSpacing(i, firstOrSecond, addIn);
        }
        return sum;
    }

    private int calculateChildSpacing(int index, boolean firstOrSecond,
            int addIn) {

        if (index + 1 < getSubBranches().size()) {
            Insets ins1 = RadialUtils.getRefInsets(
                    getSubBranches().get(index).getFigure(), firstOrSecond);
            Insets ins2 = RadialUtils.getRefInsets(
                    getSubBranches().get(index + 1).getFigure(), firstOrSecond);
            return ins1.bottom + ins2.top + getMinorSpacing2() * 2 + addIn;
        }
        return addIn;

    }

    private int getMajorSpacing2() {
        return getMajorSpacing() * 3;
    }

    private int getMinorSpacing2() {
        return getMinorSpacing() * 3 / 4 + 8;
    }

    public int getLeftSumSpacing() {
        if (leftSumSpacing < 0) {
            leftSumSpacing = calculateLeftSumSpacing();
        }
        return leftSumSpacing;
    }

    private int calculateLeftSumSpacing() {
        return calculateHalfSumSpacing(getNumLeft(), false,
                getLeftSpacingAddIn());
    }

    public int getRightSpacingAddIn() {
        if (rightSpacingAddIn < 0) {
            rightSpacingAddIn = calculateRightSpacingAddIn();
        }
        return rightSpacingAddIn;
    }

    private int calculateRightSpacingAddIn() {
        return calculateHalfSpacingAddIn(getNumRight(), true);
    }

    private int calculateHalfSpacingAddIn(int numHalf, boolean firstOrSecond) {
        if (numHalf <= 1)
            return 0;
        int minSum = (numHalf <= 2) ? getDefaultMinSumSpacing()
                : (int) (getDefaultMinSumSpacing() * f(numHalf));
        int sum = calculateSumSpacing(firstOrSecond, 0);
        return max(minSum - sum, 0) / (numHalf - 1);
    }

    private double f(int n) {
        return log(n) / log(20) + 1;
    }

    public int getLeftSpacingAddIn() {
        if (leftSpacingAddIn < 0) {
            leftSpacingAddIn = calculateLeftSpacingAddIn();
        }
        return leftSpacingAddIn;
    }

    private int calculateLeftSpacingAddIn() {
        return calculateHalfSpacingAddIn(getNumLeft(), false);
    }

    public int[] getChildrenSpacings() {
        if (childrenSpacing == null) {
            childrenSpacing = calculateChildrenSpacing();
        }
        return childrenSpacing;
    }

    private int[] calculateChildrenSpacing() {
        int[] list = new int[getSubBranches().size()];
        boolean firstOrSecond = true;
        int addIn = getRightSpacingAddIn();
        int numFirst = getNumRight();
        for (int i = 0; i < list.length; i++) {
            if (i == numFirst - 1 || i == list.length - 1) {
                list[i] = 0;
            } else {
                if (i == numFirst) {
                    firstOrSecond = false;
                    addIn = getLeftSpacingAddIn();
                }
                list[i] = calculateChildSpacing(i, firstOrSecond, addIn);
            }
        }
        return list;
    }

    public int getX(int y, boolean firstOrSecond) {
        return firstOrSecond ? getAbsX(y) : -getAbsX(y);
    }

    private int getAbsX(int y) {
        Dimension oval = getOvalSize();
        return (int) (oval.width
                * sqrt(1 - (y * y * 1.0) / (oval.height * oval.height)));
    }

//    public int getHalfSumSpacing(boolean firstOrSecond) {
//        return firstOrSecond ? getRightSumSpacing() : getLeftSumSpacing();
//    }

}