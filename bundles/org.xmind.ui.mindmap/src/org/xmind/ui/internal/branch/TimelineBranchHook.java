package org.xmind.ui.internal.branch;

import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.IFigure;
import org.xmind.gef.draw2d.IDecoratedFigure;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.part.IPartListener2;
import org.xmind.gef.part.PartEvent;
import org.xmind.ui.branch.IBranchHook;
import org.xmind.ui.mindmap.IBoundaryPart;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IBranchRangePart;
import org.xmind.ui.mindmap.IRangeListener;
import org.xmind.ui.mindmap.ISummaryPart;
import org.xmind.ui.mindmap.RangeEvent;

public class TimelineBranchHook
        implements IBranchHook, FigureListener, IPartListener2, IRangeListener {

    private IBranchPart branch;

    public void hook(IBranchPart branch) {
        this.branch = branch;

        branch.getFigure().addFigureListener(this);
        branch.addPartListener(this);

        for (IBoundaryPart b : branch.getBoundaries())
            b.addRangeListener(this);

        for (ISummaryPart s : branch.getSummaries())
            s.addRangeListener(this);
    }

    public void unhook(IBranchPart branch) {
        for (IBoundaryPart b : branch.getBoundaries()) {
            b.removeRangeListener(this);
        }
        for (ISummaryPart s : branch.getSummaries()) {
            s.removeRangeListener(this);
        }
        branch.removePartListener(this);
        branch.getFigure().removeFigureListener(this);
        updateSubBranches(branch);
    }

    private void updateSubBranches(IBranchPart branch) {
        for (IBranchPart subBranch : branch.getSubBranches()) {
            flushChildStructureType(subBranch);
            subBranch.treeUpdate(false);
        }
    }

    private void flushChildStructureType(IBranchPart subBranch) {
        subBranch.getBranchPolicy().flushStructureCache(subBranch, false, true);
    }

    public void rangeChanged(RangeEvent event) {
        updateSubBranches(branch);
    }

    public void childAdding(PartEvent event) {
    }

    public void childAdded(PartEvent event) {
        if (event.child instanceof IBranchPart) {
            updateSubBranches(branch);
        } else if (event.child instanceof ISummaryPart
                || event.child instanceof IBoundaryPart) {
            updateSubBranches(branch);
            ((IBranchRangePart) event.child).addRangeListener(this);
        }
    }

    public void childRemoving(PartEvent event) {
    }

    public void childRemoved(PartEvent event) {
        if (event.child instanceof IBranchPart) {
            updateSubBranches(branch);
        } else if (event.child instanceof ISummaryPart
                || event.child instanceof IBoundaryPart) {
            updateSubBranches(branch);
            ((IBranchRangePart) event.child).removeRangeListener(this);
        }
    }

    public void figureMoved(IFigure source) {
        IDecoration decoration = ((IDecoratedFigure) branch.getFigure())
                .getDecoration();
        if (decoration != null) {
            decoration.invalidate();
        }
    }

}
