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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.xmind.gef.draw2d.ReferencedLayoutData;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.ui.internal.figures.BoundaryFigure;
import org.xmind.ui.mindmap.IBoundaryPart;
import org.xmind.ui.mindmap.IBranchPart;

public class BoundaryLayoutHelper {

    public static interface ISubBranchInsetsCalculator {

        void calculateSubBranchInsets(BoundaryData boundary,
                SubBranchData subBranch, Insets insets);
    }

    public static class BoundaryData implements Comparable<BoundaryData> {

        public IBoundaryPart boundary;

        public IFigure boundaryFigure;

        private Insets prefInsets;

        private List<IBranchPart> subBranches;

        private Map<IBranchPart, Insets> branchBorders = new HashMap<IBranchPart, Insets>();

        private int startIndex = -1;

        private int endIndex = -1;

        private int direction;

        private boolean overall;

        public BoundaryData(IBoundaryPart boundary, int direction,
                Rectangle prefBoundaryBounds) {
            this.boundary = boundary;
            this.boundaryFigure = boundary.getFigure();
            this.prefInsets = boundaryFigure.getInsets();

            if (prefBoundaryBounds != null
                    && ((BoundaryFigure) boundary.getFigure()).isTitleVisible()
                    && boundary.getTitle() != null
                    && boundary.getTitle().getFigure() != null) {
                Dimension s = boundary.getTitle().getFigure().getPreferredSize(
                        prefBoundaryBounds.width, SWT.DEFAULT);
                this.prefInsets = new Insets(this.prefInsets);
                this.prefInsets.top = Math.max(s.height, this.prefInsets.top);

                this.prefInsets.left += 5;
            }
//            System.out.println("PrefInsets: " + this.prefInsets);

//            if (boundaryFigure instanceof ITitledFigure) {
//                ITextFigure title = ((ITitledFigure) boundaryFigure).getTitle();
//                if (boundary.getBoundary().hasTitle()) {
//                    Dimension s = title.getPreferredSize();
//                    prefInsets.top = Math.max(s.height, prefInsets.top);
//                }
//            }
            this.subBranches = boundary.getEnclosingBranches();
            this.direction = direction;
            this.overall = boundary.getBoundary().isMasterBoundary();
        }

        public boolean isOverall() {
            return overall;
        }

        public int getDirection() {
            return direction;
        }

        public Insets createInsets() {
            return new Insets(prefInsets);
        }

        public Rectangle expanded(Rectangle rect) {
            return rect.expand(prefInsets);
        }

        public List<IBranchPart> getSubBranches() {
            return subBranches;
        }

        public void setSubBranchInsets(IBranchPart subBranch, Insets ins) {
            branchBorders.put(subBranch, ins);
        }

        public Insets getSubBranchInsets(IBranchPart subBranch) {
            return branchBorders.get(subBranch);
        }

        public boolean isEmpty() {
            return subBranches.isEmpty();
        }

        public IBranchPart getFirst() {
            return isEmpty() ? null : subBranches.get(0);
        }

        public IBranchPart getLast() {
            return isEmpty() ? null : subBranches.get(subBranches.size() - 1);
        }

        public boolean isFirst(SubBranchData subBranch) {
            return isFirst(subBranch.subBranch);
        }

        public boolean isLast(SubBranchData subBranch) {
            return isLast(subBranch.subBranch);
        }

        public boolean isFirst(IBranchPart subBranch) {
            return subBranch == getFirst();
        }

        public boolean isLast(IBranchPart subBranch) {
            return subBranch == getLast();
        }

        public boolean contains(BoundaryData another) {
            return subBranches.containsAll(another.subBranches);
        }

        public boolean contains(IBranchPart subBranch) {
            return subBranches.contains(subBranch);
        }

        public boolean contains(SubBranchData subBranch) {
            return contains(subBranch.subBranch);
        }

        public int getStartIndex() {
            if (startIndex < 0 && !isEmpty()) {
                startIndex = getFirst().getBranchIndex();
            }
            return startIndex;
        }

        public int getEndIndex() {
            if (endIndex < 0 && !isEmpty()) {
                endIndex = getLast().getBranchIndex();
            }
            return endIndex;
        }

        public int compareTo(BoundaryData that) {
            if (this.isOverall())
                return 100;
            if (that.isOverall())
                return -100;
            if (this.contains(that))
                return 10;
            if (that.contains(this))
                return -10;
            if (this.isEmpty())
                return -100;
            if (that.isEmpty())
                return 100;
            return this.getStartIndex() - that.getStartIndex();
        }
    }

    public static class SubBranchData {

        public IBranchPart subBranch;

        public List<BoundaryData> boundaries = new ArrayList<BoundaryData>();

        private Insets insets = null;

        public SubBranchData(IBranchPart subBranch,
                BoundaryData[] allBoundaries) {
            this.subBranch = subBranch;
            for (BoundaryData b : allBoundaries) {
                if (b.contains(subBranch)) {
                    boundaries.add(b);
                }
            }
        }

        public boolean isEmpty() {
            return boundaries.isEmpty();
        }

        public Insets getInsets() {
            if (insets == null) {
                insets = calcInnerInsets(null);
            }
            return insets;
        }

        public Insets calcInnerInsets(BoundaryData boundary) {
            Insets insets = null;
            BoundaryData last = null;
            Insets lastLevelIns = null;
            for (BoundaryData b : boundaries) {
                if (last != null && boundary != null
                        && !boundary.contains(last)) {
                    lastLevelIns = null;
                    break;
                }

                if (b == boundary)
                    break;

                if (last != null && b.contains(last)) {
                    insets = Geometry.add(insets, lastLevelIns);
                    lastLevelIns = null;
                }

                last = b;
                Insets ins = b.getSubBranchInsets(subBranch);
                lastLevelIns = Geometry.union(lastLevelIns, ins);
            }
            if (lastLevelIns != null)
                insets = Geometry.add(insets, lastLevelIns);
            return insets == null ? IFigure.NO_INSETS : insets;
        }

    }

    private Map<IBoundaryPart, BoundaryData> boundaries = new HashMap<IBoundaryPart, BoundaryData>();

    private Map<IBranchPart, SubBranchData> subBranches = new HashMap<IBranchPart, SubBranchData>();

    private BoundaryData overallBoundary = null;

    public BoundaryLayoutHelper() {
    }

    public void reset(IBranchPart branch, IBranchStructureExtension algorithm,
            Map<IFigure, Rectangle> prefBounds) {
        boundaries.clear();
        subBranches.clear();

        for (IBoundaryPart boundary : branch.getBoundaries()) {
            BoundaryData boundaryData = new BoundaryData(boundary,
                    algorithm.getRangeGrowthDirection(branch, boundary),
                    prefBounds == null ? null
                            : prefBounds.get(boundary.getFigure()));
            boundaries.put(boundary, boundaryData);
//            if (boundaryData.isOverall()
//                    && (overallBoundary == null || MindMapUI.BRANCH_FLOATING
//                            .equals(branch.getBranchType()))) {
//                overallBoundary = boundaryData;
//            }
            if (boundaryData.isOverall() && getOverallBoundary() == null) {
                setOverallBoundary(boundaryData);
            }
        }
        if (!isEmpty()) {
            BoundaryData[] allBoundaries = boundaries.values()
                    .toArray(new BoundaryData[0]);
            Arrays.sort(allBoundaries);
            for (IBranchPart subBranch : branch.getSubBranches()) {
                SubBranchData data = new SubBranchData(subBranch,
                        allBoundaries);
                if (!data.isEmpty()) {
                    subBranches.put(subBranch, data);
                }
            }
        }
        for (IBoundaryPart b : branch.getBoundaries()) {
            BoundaryData boundary = getBoundaryData(b);
            for (IBranchPart s : boundary.subBranches) {
                SubBranchData subBranch = getSubBranchData(s);
                boundary.setSubBranchInsets(subBranch.subBranch,
                        createSubBranchInsets(boundary, subBranch));
            }
        }
    }

    public BoundaryData getOverallBoundary() {
        return overallBoundary;
    }

    protected Insets createSubBranchInsets(BoundaryData boundary,
            SubBranchData subBranch) {
        Insets ins = boundary.createInsets();
        switch (boundary.getDirection()) {
        case PositionConstants.EAST:
            if (!boundary.isFirst(subBranch)) {
                ins.left = 0;
            }
            if (!boundary.isLast(subBranch)) {
                ins.right = 0;
            }
            break;
        case PositionConstants.WEST:
            if (!boundary.isFirst(subBranch)) {
                ins.right = 0;
            }
            if (!boundary.isLast(subBranch)) {
                ins.left = 0;
            }
            break;
        case PositionConstants.SOUTH:
            if (!boundary.isFirst(subBranch)) {
                ins.top = 0;
            }
            if (!boundary.isLast(subBranch)) {
                ins.bottom = 0;
            }
            break;
        case PositionConstants.NORTH:
            if (!boundary.isFirst(subBranch)) {
                ins.bottom = 0;
            }
            if (!boundary.isLast(subBranch)) {
                ins.top = 0;
            }
            break;
        }

        return ins;
    }

    public boolean isEmpty() {
        return boundaries.isEmpty();
    }

    public boolean hasSubBranch(IBranchPart subBranch) {
        return subBranches.containsKey(subBranch);
    }

    public SubBranchData getSubBranchData(IBranchPart subBranch) {
        return subBranches.get(subBranch);
    }

    public BoundaryData getBoundaryData(IBoundaryPart boundary) {
        return boundaries.get(boundary);
    }

    public Insets getInsets(IBranchPart subBranch) {
        SubBranchData data = getSubBranchData(subBranch);
        return data != null ? data.getInsets() : IFigure.NO_INSETS;
    }

    public Rectangle getBorderedBounds(IBranchPart subBranch,
            ReferencedLayoutData data) {
        Rectangle r = data.get(subBranch.getFigure());
        if (r != null) {
            r = r.getExpanded(getInsets(subBranch));
        }
        return r;
    }

    public Dimension getBorderedSize(IBranchPart subBranch) {
        return getBorderedSize(subBranch, -1, -1);
    }

    public Dimension getBorderedSize(IBranchPart subBranch, int wHint,
            int hHint) {
        Dimension s = subBranch.getFigure().getPreferredSize(wHint, hHint);
        Insets ins = getInsets(subBranch);
        s = new Dimension(s.width + ins.getWidth(), s.height + ins.getHeight());
        return s;
    }

    public Insets getInnerInsets(SubBranchData subBranch,
            BoundaryData boundary) {
        return subBranch.calcInnerInsets(boundary);
    }

    public void setOverallBoundary(BoundaryData overallBoundary) {
        this.overallBoundary = overallBoundary;
    }

}