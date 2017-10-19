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
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.draw2d.ReferencedLayoutData;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.geometry.IIntersectionSolver;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ISheetPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class SheetLayout extends MindMapLayoutBase {

    //private static final Point REFERENCE = new Point();

    private SheetIntersectionSolver intersectionSolver = new SheetIntersectionSolver();

    public SheetLayout(ISheetPart sheet) {
        super(sheet);
    }

    protected ISheetPart getSheet() {
        return (ISheetPart) super.getPart();
    }

    private boolean needsSolveIntersections() {
        return !MindMapUI.isOverlapsAllowed();
    }

    protected void fillLayoutData(IFigure container, ReferencedLayoutData data) {
        Point ref = data.getReference();
        ISheetPart sheet = getSheet();

        intersectionSolver.setOrigin(ref);
        intersectionSolver.recordInitPositions(sheet, true);

        if (needsSolveIntersections()) {
            intersectionSolver.solve();
        }

        Rectangle freeBranchesBounds = null;

        for (Object key : intersectionSolver
                .getKeys(IIntersectionSolver.CATEGORY_FREE)) {
            if (key instanceof IGraphicalPart) {
                IFigure figure = ((IGraphicalPart) key).getFigure();
                Rectangle rect = intersectionSolver.getSolvedBounds(key);
                if (key instanceof IBranchPart
                        && ((IBranchPart) key).getParentBranch() != null) {
                    // free main & sub branches
                    IBranchPart branch = (IBranchPart) key;
                    Point pos = intersectionSolver.getSolvedPosition(key);
                    MindMapUtils.setCache(branch,
                            IBranchPart.CACHE_PREF_POSITION, pos);
                    freeBranchesBounds = Geometry.union(freeBranchesBounds,
                            rect);
                } else {
                    // floating branches & legend
                    data.put(figure, rect);
                }
            }
        }

        IBranchPart centralBranch = sheet.getCentralBranch();
        if (centralBranch != null) {
            IFigure centralBranchFigure = centralBranch.getFigure();
            Rectangle centralBranchBounds = ((IReferencedFigure) centralBranchFigure)
                    .getPreferredBounds(ref);
            centralBranchBounds = Geometry.union(centralBranchBounds,
                    freeBranchesBounds);
            data.put(centralBranchFigure, centralBranchBounds);
        }

        intersectionSolver.clear();

//        for (IBranchPart floatingBranch : getSheet().getFloatingBranches()) {
//            REFERENCE.setLocation(ref);
//            Point position = (Point) MindMapUtils.getCache(floatingBranch,
//                    IBranchPart.CACHE_PREF_POSITION);
//            if (position != null) {
//                REFERENCE.translate(position);
//            }
//            IFigure floatingBranchFigure = floatingBranch.getFigure();
//            Rectangle floatingBranchBounds = ((IReferencedFigure) floatingBranchFigure)
//                    .getPreferredBounds(REFERENCE);
//            data.put(floatingBranchFigure, floatingBranchBounds);
//        }
//
//        ILegendPart legend = sheet.getLegend();
//        if (legend != null) {
//            IFigure legendFigure = legend.getFigure();
//            if (legendFigure.isEnabled()) {
//                REFERENCE.setLocation(ref);
//                Point position = legend.getPreferredPosition();
//                if (position == null) {
//                    REFERENCE.translate(0, 200);
//                } else {
//                    REFERENCE.translate(position);
//                }
//                Rectangle r = ((IReferencedFigure) legendFigure)
//                        .getPreferredBounds(REFERENCE);
//                data.put(legendFigure, r);
//            }
//        }
    }

//    public void layout(IFigure container) {
//        super.layout(container);
////        for (RelationshipPart relationship : getSheet().getRelationships()) {
////            IFigure relFigure = relationship.getFigure();
////            if (relFigure instanceof RelationshipFigure) {
////                Rectangle bounds = ((RelationshipFigure) relFigure)
////                        .getPreferredBounds();
////                relFigure.setBounds(bounds);
////            }
////        }
//    }
}