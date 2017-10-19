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
package org.xmind.ui.internal.layouts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.core.IPositioned;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.draw2d.geometry.DelegatingIntersectionSolver;
import org.xmind.gef.draw2d.geometry.IIntersectionSolver;
import org.xmind.gef.draw2d.geometry.IPositionSolver;
import org.xmind.gef.draw2d.geometry.SplitIntersectionSolver;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.policy.GraphicalPartBoundsProvider;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ILegendPart;
import org.xmind.ui.mindmap.ISheetPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.util.MindMapUtils;

public class SheetIntersectionSolver extends DelegatingIntersectionSolver {

    private static class SteadyBoundsProvider extends
            GraphicalPartBoundsProvider {

        public Rectangle getPrefBounds(Object host, Point reference) {
            if (host instanceof IBranchPart) {
                IBranchPart branch = (IBranchPart) host;
                if (branch.getBoundaries().isEmpty()
                        && branch.getSummaries().isEmpty()) {
                    ITopicPart topicPart = branch.getTopicPart();
                    if (topicPart != null)
                        host = topicPart.getFigure();
                }
            }
            return super.getPrefBounds(host, reference);
        }
    }

    private static final SteadyBoundsProvider STEADY_BOUNDS_PROVIDER_INSTANCE = new SteadyBoundsProvider();

    public void recordInitPositions(ISheetPart sheet,
            boolean useModelOrGraphicalPosition) {
        IBranchPart centralBranch = sheet.getCentralBranch();
        if (centralBranch != null) {
            recordBranchPosition(centralBranch, sheet, false,
                    useModelOrGraphicalPosition);
        }

        for (IBranchPart floatingBranch : sheet.getFloatingBranches()) {
            recordFreePosition(floatingBranch, useModelOrGraphicalPosition,
                    sheet);
        }

        ILegendPart legend = sheet.getLegend();
        if (legend != null) {
            recordFreePosition(legend, useModelOrGraphicalPosition, sheet);
        }
    }

    private void recordBranchPosition(IBranchPart branch, ISheetPart sheet,
            boolean freeable, boolean useModelOrGraphicalPosition) {
        IFigure figure = branch.getFigure();
        if (!figure.isVisible() || !figure.isEnabled())
            return;

//        if (freeable && MindMapUtils.isBranchFreeable(branch)) {
//            recordFreePosition(branch, useModelOrGraphicalPosition);
//        } else {
        recordInitPosition(branch, getPosition(branch, sheet), CATEGORY_STEADY,
                false);
        if (!branch.isFolded() && !takesEntireBranch(branch)) {
            boolean childrenFreeable = MindMapUtils
                    .isSubBranchesFreeable(branch);
            for (IBranchPart sub : branch.getSubBranches()) {
                recordBranchPosition(sub, sheet, childrenFreeable,
                        useModelOrGraphicalPosition);
            }
            for (IBranchPart sum : branch.getSummaryBranches()) {
                recordBranchPosition(sum, sheet, childrenFreeable,
                        useModelOrGraphicalPosition);
            }
        }
//        }
    }

    private boolean takesEntireBranch(IBranchPart branch) {
        //TODO check whether it takes the entire branch area to avoid intersection
        return false;
    }

    private void recordFreePosition(IGraphicalPart part,
            boolean useModelOrGraphicalPosition, ISheetPart sheet) {
        IFigure figure = part.getFigure();
        if (!figure.isVisible() || !figure.isEnabled())
            return;

        Point position = null;
        if (useModelOrGraphicalPosition) {
            Object model = MindMapUtils.getRealModel(part);
            if (model instanceof IPositioned) {
                Point offset = MindMapUtils
                        .toGraphicalPosition(((IPositioned) model)
                                .getPosition());
                if (offset != null) {
                    position = getOrigin().getTranslated(offset);
                }
            }
        }
        if (position == null) {
            position = getPosition(part, sheet);
        }
        recordInitPosition(part, position, CATEGORY_FREE, false);
    }

    private Point getPosition(IGraphicalPart part, ISheetPart sheet) {
        Point position = getActualPosition(part);
        Point lastRef = getLastSheetReference(sheet);
        return position.getTranslated(-lastRef.x, -lastRef.y);
    }

    private Point getLastSheetReference(ISheetPart sheet) {
        if (sheet.getFigure() instanceof IReferencedFigure)
            return ((IReferencedFigure) sheet.getFigure()).getLastReference();
        return sheet.getFigure().getBounds().getLocation();
    }

    private Point getActualPosition(IGraphicalPart part) {
        IFigure figure = part.getFigure();
        if (figure instanceof IReferencedFigure)
            return ((IReferencedFigure) figure).getReference();
        return figure.getBounds().getCenter();
    }

    protected IPositionSolver createDelegate() {
        IIntersectionSolver solver = new SplitIntersectionSolver(false);
        solver.setDefaultBoundsProvider(GraphicalPartBoundsProvider
                .getDefault());
        solver.setGeneralBoundsProvider(CATEGORY_STEADY,
                STEADY_BOUNDS_PROVIDER_INSTANCE);
        solver.setSpacing(15);
        return solver;
    }

}