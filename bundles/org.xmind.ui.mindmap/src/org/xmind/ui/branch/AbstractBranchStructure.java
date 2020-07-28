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

import static org.xmind.ui.style.StyleUtils.getInteger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.GEF;
import org.xmind.gef.draw2d.IDecoratedFigure;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.draw2d.IRotatableFigure;
import org.xmind.gef.draw2d.ReferencedLayoutData;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.geometry.PrecisionDimension;
import org.xmind.gef.draw2d.geometry.PrecisionRectangle;
import org.xmind.gef.draw2d.geometry.PrecisionRotator;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.ui.branch.BoundaryLayoutHelper.BoundaryData;
import org.xmind.ui.decorations.IBranchConnectionDecoration;
import org.xmind.ui.decorations.IBranchConnections2;
import org.xmind.ui.decorations.ISummaryDecoration;
import org.xmind.ui.decorations.ITopicDecoration;
import org.xmind.ui.internal.figures.BoundaryFigure;
import org.xmind.ui.internal.figures.BranchFigure;
import org.xmind.ui.internal.figures.InformationFigure;
import org.xmind.ui.internal.figures.TopicFigure;
import org.xmind.ui.mindmap.IBoundaryPart;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IBranchRangePart;
import org.xmind.ui.mindmap.IInfoPart;
import org.xmind.ui.mindmap.ILabelPart;
import org.xmind.ui.mindmap.IPlusMinusPart;
import org.xmind.ui.mindmap.ISummaryPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;
import org.xmind.ui.tools.ParentSearchKey;
import org.xmind.ui.util.MindMapUtils;

public abstract class AbstractBranchStructure implements IBranchStructure,
        IBranchStructureExtension, INavigableBranchStructureExtension,
        IInsertableBranchStructureExtension {

    protected static final String CACHE_STRUCTURE_DATA = "org.xmind.ui.branchCache.structureData"; //$NON-NLS-1$

    protected static final String CACHE_BOUNDARY_LAYOUT_HELPER = "org.xmind.ui.branchCache.boundaryLayoutHelper"; //$NON-NLS-1$

    protected static class LayoutInfo extends ReferencedLayoutData {

        private ReferencedLayoutData delegate;

        private boolean folded;

        private boolean minimized;

        private Rectangle minArea;

        private boolean hasBoundaryTitles;

        public LayoutInfo(ReferencedLayoutData delegate, boolean folded,
                boolean minimized) {
            this.delegate = delegate;
            this.folded = folded;
            this.minimized = minimized;
            this.minArea = null;
            this.hasBoundaryTitles = false;
        }

        public boolean isFolded() {
            return folded;
        }

        public boolean isMinimized() {
            return minimized;
        }

        public Rectangle getMinArea() {
            return minArea;
        }

        public void setMinArea(Rectangle minArea) {
            this.minArea = minArea;
        }

        public void putMinArea(IFigure figure) {
            if (minArea == null) {
                delegate.put(figure, delegate.createInitBounds());
            } else {
                delegate.put(figure, minArea.getCopy());
            }
        }

        public void add(Rectangle blankArea) {
            delegate.add(blankArea);
        }

        public void addMargins(Insets margin) {
            delegate.addMargins(margin);
        }

        public void addMargins(int top, int left, int bottom, int right) {
            delegate.addMargins(top, left, bottom, right);
        }

        public Rectangle createInitBounds() {
            return delegate.createInitBounds();
        }

        public Rectangle createInitBounds(Point ref) {
            return delegate.createInitBounds(ref);
        }

        public Rectangle get(Object figure) {
            return delegate.get(figure);
        }

        public Rectangle getCheckedClientArea() {
            return delegate.getCheckedClientArea();
        }

        public Rectangle getClientArea() {
            return delegate.getClientArea();
        }

        public Point getReference() {
            return delegate.getReference();
        }

        public void put(IFigure figure, Rectangle preferredBounds) {
            delegate.put(figure, preferredBounds);
        }

        public void translate(int dx, int dy) {
            delegate.translate(dx, dy);
        }

    }

    public void fillLayoutData(IBranchPart branch, ReferencedLayoutData data) {
        BranchFigure figure = (BranchFigure) branch.getFigure();
        boolean folded = figure.isFolded();
        boolean minimized = figure.isMinimized();
        LayoutInfo info = new LayoutInfo(data, folded, minimized);
        fillLayoutInfo(branch, info);
    }

    protected void fillLayoutInfo(IBranchPart branch, LayoutInfo info) {
        IStyleSelector styleSelector = branch.getBranchPolicy()
                .getStyleSelector(branch);
        if (styleSelector != null) {
            String hideCallout = styleSelector.getStyleValue(branch,
                    Styles.HideCallout);
            if (Boolean.parseBoolean(hideCallout) && MindMapUI.BRANCH_CALLOUT
                    .equals(branch.getBranchType())) {
                branch.getFigure().setVisible(false);
                return;
            }
        }
//        if ("org.xmind.ui.spreadsheet".equals(branch.getBranchPolicyId()) //$NON-NLS-1$
//                && MindMapUI.BRANCH_CALLOUT.equals(branch.getBranchType())) {
//            branch.getFigure().setVisible(false);
//            return;
//        }

        fillTopic(branch, info);
        fillPlusMinus(branch, info);
        fillLabel(branch, info);
        fillInformation(branch, info);

        List<IBranchPart> subBranches = branch.getSubBranches();
        List<IBoundaryPart> boundaries = branch.getBoundaries();

        BoundaryLayoutHelper boundaryLayoutHelper = getBoundaryLayoutHelper(
                branch);

//        boundaryLayoutHelper.reset(branch, this, null);
        info.hasBoundaryTitles = false;

        ReferencedLayoutData fakeDelegate = info.delegate.copy();
        ReferencedLayoutData realDelegate = info.delegate;
        info.delegate = fakeDelegate;

        fillSubBranches(branch, subBranches, info);
        fillBoundaries(branch, boundaries, subBranches, info);
        info.delegate = realDelegate;

        if (info.hasBoundaryTitles) {
            Map<IFigure, Rectangle> cachedBounds = new HashMap<IFigure, Rectangle>();
            for (IBoundaryPart boundary : branch.getBoundaries()) {
                cachedBounds.put(boundary.getFigure(),
                        fakeDelegate.get(boundary.getFigure()));
            }
            boundaryLayoutHelper.reset(branch, this, cachedBounds);
            fillSubBranches(branch, subBranches, info);
            fillBoundaries(branch, boundaries, subBranches, info);
        } else {
            for (IBranchPart subBranch : branch.getSubBranches()) {
                info.delegate.put(subBranch.getFigure(),
                        fakeDelegate.get(subBranch.getFigure()));
            }
            for (IBoundaryPart boundary : branch.getBoundaries()) {
                info.delegate.put(boundary.getFigure(),
                        fakeDelegate.get(boundary.getFigure()));
            }
        }

        List<IBranchPart> calloutBranches = branch.getCalloutBranches();
        fillCalloutBranches(branch, calloutBranches, info);

        List<ISummaryPart> summaries = branch.getSummaries();
        List<IBranchPart> summaryBranches = new ArrayList<IBranchPart>(
                branch.getSummaryBranches());
        fillSummaries(branch, summaries, summaryBranches, subBranches, info);
        fillUnhandledSummaryBranches(branch, summaryBranches, info);

        addExtraSpaces(branch, info);

        boundaryLayoutHelper.reset(branch, this, null);
        info.hasBoundaryTitles = false;

        fakeDelegate = info.delegate.copy();
        realDelegate = info.delegate;
        info.delegate = fakeDelegate;
        info.hasBoundaryTitles = false;
        fillOverallBoundary(branch, boundaries, info);
        info.delegate = realDelegate;
        if (info.hasBoundaryTitles) {
            Map<IFigure, Rectangle> cachedBounds = new HashMap<IFigure, Rectangle>();
            for (IBoundaryPart boundary : branch.getBoundaries()) {
                cachedBounds.put(boundary.getFigure(),
                        fakeDelegate.get(boundary.getFigure()));
            }
            boundaryLayoutHelper.setOverallBoundary(null);
            boundaryLayoutHelper.reset(branch, this, cachedBounds);
            fillOverallBoundary(branch, boundaries, info);
        } else {
            for (IBranchPart subBranch : branch.getSubBranches()) {
                info.delegate.put(subBranch.getFigure(),
                        fakeDelegate.get(subBranch.getFigure()));
            }
            for (IBoundaryPart boundary : branch.getBoundaries()) {
                info.delegate.put(boundary.getFigure(),
                        fakeDelegate.get(boundary.getFigure()));
            }
        }

    }

    protected void fillCalloutBranches(IBranchPart branch,
            List<IBranchPart> calloutBranches, LayoutInfo info) {
        if (calloutBranches.isEmpty())
            return;

        IStyleSelector styleSelector = branch.getBranchPolicy()
                .getStyleSelector(branch);
        if (styleSelector != null) {
            String hideCallout = styleSelector.getStyleValue(branch,
                    Styles.HideCallout);
            if (Boolean.parseBoolean(hideCallout)) {
                for (IBranchPart calloutBranch : calloutBranches) {
                    calloutBranch.getFigure().setVisible(false);
                }
                IBranchConnections2 calloutConnections = branch
                        .getCalloutConnections();
                for (int index = 0; index < calloutBranches.size(); index++) {
                    IDecoration decoration = calloutConnections
                            .getDecoration(index);
                    if (decoration instanceof IBranchConnectionDecoration) {
                        ((IBranchConnectionDecoration) decoration)
                                .setVisible(branch.getFigure(), false);
                    }
                }
                return;
            }
        }

        if ((info.isFolded() || info.isMinimized())
                && minimizesSubBranchesToOnePoint()) {
            if (info.isFolded() && !info.isMinimized()) {
                info.setMinArea(info.createInitBounds(calcSubBranchesMinPoint(
                        branch, calloutBranches, info)));
            }
            for (IBranchPart calloutBranch : calloutBranches) {
                info.putMinArea(calloutBranch.getFigure());
            }
        } else {
            doFillCalloutBranches(branch, calloutBranches, info);
            for (IBranchPart calloutBranch : calloutBranches) {
                IFigure calloutBranchFigure = calloutBranch.getFigure();
                if (info.get(calloutBranchFigure) == null) {
                    info.putMinArea(calloutBranchFigure);
                }
            }
        }
    }

    protected void doFillCalloutBranches(IBranchPart branch,
            List<IBranchPart> calloutBranches, LayoutInfo info) {
        if (branch.isCentral()) {
            for (IBranchPart calloutBranch : calloutBranches) {
                calloutBranch.getFigure().setVisible(false);
            }
            IBranchConnections2 calloutConnections = branch
                    .getCalloutConnections();
            for (int index = 0; index < calloutBranches.size(); index++) {
                IDecoration decoration = calloutConnections
                        .getDecoration(index);
                if (decoration instanceof IBranchConnectionDecoration) {
                    ((IBranchConnectionDecoration) decoration)
                            .setVisible(branch.getFigure(), false);
                }
            }
            return;
        }

        IBranchPart parentBranch = branch.getParentBranch();
        int orientation = getChildTargetOrientation(parentBranch, branch);
        boolean left = PositionConstants.EAST == orientation;
        boolean right = PositionConstants.WEST == orientation;
        boolean south = PositionConstants.NORTH == orientation;
        boolean north = PositionConstants.SOUTH == orientation;

        for (IBranchPart calloutBranch : calloutBranches) {
            //changed
            Rectangle parentTopicArea = info
                    .get(branch.getTopicPart().getFigure());
            Rectangle parentBranchArea = info.getCheckedClientArea();

            IReferencedFigure calloutBranchFigure = (IReferencedFigure) calloutBranch
                    .getFigure();

            ITopicPart calloutTopicPart = calloutBranch.getTopicPart();
            if (calloutTopicPart == null)
                continue;
            IFigure calloutFigure = calloutTopicPart.getFigure();
            Dimension calloutSize = calloutFigure.getPreferredSize();

            //over parent topic center
            org.xmind.core.util.Point position = calloutBranch.getTopic()
                    .getPosition();
            if (position == null)
                position = new org.xmind.core.util.Point();
            boolean originPosition = position.x == 0 && position.y == 0;

            int offsetX = originPosition ? 0 : position.x;
            int offsetY = originPosition
                    ? -calloutSize.height / 2 - parentTopicArea.height / 2 - 10
                    : position.y;

            boolean upDown = offsetY < 0;

            int dummyCalloutX = parentTopicArea.getCenter().x + offsetX
                    - calloutSize.width / 2;

            if (left) {
                //limit left movable boundary
                int dx = (dummyCalloutX + calloutSize.width)
                        - (parentTopicArea.x + parentTopicArea.width);
                offsetX = dx > 0 ? offsetX - dx : offsetX;
            } else if (right) {
                //limit right movable boundary
                int dx = dummyCalloutX - parentTopicArea.x;
                offsetX = dx < 0 ? offsetX - dx : offsetX;
            }

            Point reference = info.getReference();
            Point translated = reference.getTranslated(offsetX, offsetY);
            Rectangle bounds = calloutBranchFigure
                    .getPreferredBounds(translated);

            int subRectX = left || south || north ? parentBranchArea.x
                    : parentBranchArea.x + parentTopicArea.width;
            int subRectY = left || right || north ? parentBranchArea.y
                    : parentBranchArea.y + parentTopicArea.height;
            int subRectWidth = left || right
                    ? parentBranchArea.width - parentTopicArea.width
                    : parentBranchArea.width;
            int subRectHeight = left || right ? parentBranchArea.height
                    : parentBranchArea.height - parentTopicArea.height;
            Rectangle subRect = new Rectangle(subRectX, subRectY, subRectWidth,
                    subRectHeight);
            boolean touchSub = subRect.touches(bounds);
            boolean touchParentTopic = bounds.touches(parentTopicArea);
            if (touchSub) {
                int y = upDown ? subRect.y - bounds.height - 10
                        : subRect.bottom() + 10;
                bounds.setY(y);
            } else if (touchParentTopic) {
                int y = upDown ? parentTopicArea.y - bounds.height - 10
                        : parentTopicArea.bottom() + 10;
                bounds.setY(y);
            }
            info.put(calloutBranchFigure, bounds);
        }
    }

    protected void fillTopic(IBranchPart branch, LayoutInfo info) {
        ITopicPart topic = branch.getTopicPart();
        if (topic != null) {
            if (info.isMinimized()) {
                info.putMinArea(topic.getFigure());
            } else {
                doFillTopic(branch, topic, info);
            }
        }
    }

    protected void doFillTopic(IBranchPart branch, ITopicPart topicPart,
            LayoutInfo info) {
        IFigure fig = topicPart.getFigure();
        if (fig instanceof IReferencedFigure) {
            IReferencedFigure refFig = (IReferencedFigure) fig;
            Rectangle bounds = refFig.getPreferredBounds(info.getReference());
            info.put(refFig, bounds);
        } else {
            Dimension size = fig.getPreferredSize();
            Point ref = info.getReference();
            Rectangle r = new Rectangle(ref.x - size.width / 2,
                    ref.y - size.height / 2, size.width, size.height);
            info.put(fig, r);
        }
    }

    protected void fillPlusMinus(IBranchPart branch, LayoutInfo info) {
        IPlusMinusPart plusMinus = branch.getPlusMinus();
        if (plusMinus != null) {
            IFigure pmFigure = plusMinus.getFigure();
            if (info.isMinimized()) {
                info.putMinArea(pmFigure);
            } else {
                doFillPlusMinus(branch, plusMinus, info);
                if (info.get(pmFigure) == null) {
                    info.putMinArea(pmFigure);
                }
            }
        }
    }

    protected abstract void doFillPlusMinus(IBranchPart branch,
            IPlusMinusPart plusMinus, LayoutInfo info);

    protected void fillLabel(IBranchPart branch, LayoutInfo info) {
        ILabelPart label = branch.getLabel();
        if (label != null) {
            if (info.isMinimized() || !label.getFigure().isVisible()) {
                info.putMinArea(label.getFigure());
            } else {
                doFillLabel(branch, label, info);
            }
        }
    }

    protected void doFillLabel(IBranchPart branch, ILabelPart label,
            LayoutInfo info) {
        IFigure figure = label.getFigure();
        ITopicPart topicPart = branch.getTopicPart();
        Rectangle area;
        if (topicPart != null) {
            area = info.get(topicPart.getFigure());
        } else {
            area = info.createInitBounds();
        }
        if (figure instanceof IRotatableFigure) {
            IRotatableFigure f = (IRotatableFigure) figure;
            double angle = f.getRotationDegrees();
            if (!Geometry.isSameAngleDegree(angle, 0, 0.00001)) {
                Point ref = info.getReference();
                PrecisionRotator r = new PrecisionRotator();
                r.setOrigin(ref.x, ref.y);
                r.setAngle(angle);
                PrecisionRectangle rect = r.r(new PrecisionRectangle(area));
                PrecisionDimension size = f.getNormalPreferredSize(-1, -1);
                rect.x += (rect.width - size.width) / 2;
                rect.y = rect.bottom() - 2;
                rect.width = size.width;
                rect.height = size.height;
                r.t(rect);
                info.put(figure, rect.toDraw2DRectangle());
                return;
            }
        }
        Dimension size = figure.getPreferredSize();
        Rectangle r = new Rectangle(area.x + (area.width - size.width) / 2,
                area.bottom() - 2, size.width, size.height);
        info.put(figure, r);
    }

    protected void fillInformation(IBranchPart branch, LayoutInfo info) {
        IInfoPart information = branch.getInfoPart();
        if (information != null) {
            if (info.isMinimized() || !information.getFigure().isVisible()) {
                info.putMinArea(information.getFigure());
            } else {
                doFillInfomation(branch, information, info);
            }
        }
    }

    protected void doFillInfomation(IBranchPart branch, IInfoPart information,
            LayoutInfo info) {
        IFigure figure = information.getFigure();
        ITopicPart topicPart = branch.getTopicPart();
        Rectangle area;
        if (topicPart != null) {
            area = info.get(topicPart.getFigure());
        } else {
            area = info.createInitBounds();
        }

        if (figure instanceof InformationFigure) {
            ((InformationFigure) figure).setConstraint(area.getSize().width);
        }

        Dimension size = figure.getPreferredSize();

        TopicFigure tf = (TopicFigure) topicPart.getFigure();
        ITopicDecoration decoration = tf.getDecoration();
        String shapeId = decoration.getId();
        int x;
        if (Styles.TOPIC_SHAPE_ROUNDEDRECT.equals(shapeId)
                || Styles.TOPIC_SHAPE_RECT.equals(shapeId)
                || Styles.TOPIC_SHAPE_UNDERLINE.equals(shapeId))
            x = area.x;
        else
            x = area.x + (area.width - size.width) / 2;

        int y = area.bottom() - 1;
        if (!Styles.TOPIC_SHAPE_UNDERLINE.equals(shapeId))
            y -= 1;

        Rectangle r = new Rectangle(x, y, size.width, size.height);
        info.put(figure, r);
    }

    protected void fillSubBranches(IBranchPart branch,
            List<IBranchPart> subBranches, LayoutInfo info) {
        if (subBranches.isEmpty())
            return;
        if ((info.isFolded() || info.isMinimized())
                && minimizesSubBranchesToOnePoint()) {
            if (info.isFolded() && !info.isMinimized()) {
                info.setMinArea(info.createInitBounds(
                        calcSubBranchesMinPoint(branch, subBranches, info)));
            }
            for (IBranchPart subBranch : subBranches) {
                info.putMinArea(subBranch.getFigure());
            }
        } else {
            doFillSubBranches(branch, subBranches, info);
            for (IBranchPart subBranch : subBranches) {
                IFigure subBranchFigure = subBranch.getFigure();
                if (info.get(subBranchFigure) == null) {
                    info.putMinArea(subBranchFigure);
                }
            }
        }
    }

    protected abstract void doFillSubBranches(IBranchPart branch,
            List<IBranchPart> subBranches, LayoutInfo info);

    protected boolean minimizesSubBranchesToOnePoint() {
        return true;
    }

    protected Point calcSubBranchesMinPoint(IBranchPart branch,
            List<IBranchPart> subBranches, LayoutInfo info) {
        IPlusMinusPart plusMinus = branch.getPlusMinus();
        if (plusMinus != null) {
            Rectangle pmBounds = info.get(plusMinus.getFigure());
            if (pmBounds != null) {
                return pmBounds.getCenter();
            }
        }
        return info.getReference();
    }

    protected void fillBoundaries(IBranchPart branch,
            List<IBoundaryPart> boundaries, List<IBranchPart> subBranches,
            LayoutInfo info) {

        if (boundaries.isEmpty())
            return;
        if (subBranches.isEmpty() || ((info.isFolded() || info.isMinimized())
                && minimizesSubBranchesToOnePoint())) {
            for (IBoundaryPart b : boundaries) {
                info.putMinArea(b.getFigure());
            }
        } else {
            doFillBoundaries(branch, boundaries, info);
        }
    }

    protected void doFillBoundaries(IBranchPart branch,
            List<IBoundaryPart> boundaries, LayoutInfo info) {

        for (IBoundaryPart boundary : boundaries) {
            doFillBoundary(branch, boundary, info);
        }
    }

    protected void doFillBoundary(IBranchPart branch, IBoundaryPart boundary,
            LayoutInfo info) {
        BoundaryLayoutHelper helper = getBoundaryLayoutHelper(branch);
        BoundaryData boundaryData = helper.getBoundaryData(boundary);

        if (boundary.getFigure() != null
                && ((BoundaryFigure) boundary.getFigure()).isTitleVisible()
                && boundary.getTitle() != null) {
            info.hasBoundaryTitles = true;
        }

        if (boundaryData.isOverall()) {
            if (boundaryData != helper.getOverallBoundary()) {
                info.putMinArea(boundary.getFigure());
            }
            return;
        }
        Rectangle area = null;
        for (IBranchPart subBranch : boundaryData.getSubBranches()) {
            Insets ins = helper.getInnerInsets(
                    helper.getSubBranchData(subBranch), boundaryData);
            Rectangle r2 = info.get(subBranch.getFigure());
            area = Geometry.union(area, r2.getExpanded(ins));
        }
        if (area == null) {
            info.putMinArea(boundary.getFigure());
        } else {
            area = boundaryData.expanded(area);
            info.put(boundary.getFigure(), area);
        }
    }

    protected void fillSummaries(IBranchPart branch,
            List<ISummaryPart> summaries, List<IBranchPart> summaryBranches,
            List<IBranchPart> subBranches, LayoutInfo info) {
        if (!summaries.isEmpty()) {
            if (subBranches.isEmpty()
                    || ((info.isFolded() || info.isMinimized())
                            && minimizesSubBranchesToOnePoint())) {
                for (ISummaryPart s : summaries) {
                    info.putMinArea(s.getFigure());
                }
            } else {
                doFillSummaries(branch, summaries, summaryBranches, info);
            }
        }
    }

    private void doFillSummaries(IBranchPart branch,
            List<ISummaryPart> summaries, List<IBranchPart> summaryBranches,
            LayoutInfo info) {
        for (ISummaryPart summary : summaries) {
            doFillSummary(branch, summary, summaryBranches, info);
        }
    }

    private void doFillSummary(IBranchPart branch, ISummaryPart summary,
            List<IBranchPart> summaryBranches, LayoutInfo info) {
        int direction = getSummaryDirection(branch, summary);
        Rectangle area = getSummaryArea(branch, summary, direction, info);
        if (area != null) {
            info.put(summary.getFigure(), area);
        } else {
            info.putMinArea(summary.getFigure());
        }
        IBranchPart conclusionBranch = getConclusionBranch(branch, summary,
                summaryBranches);
        if (conclusionBranch != null) {
            if (area == null) {
                info.putMinArea(conclusionBranch.getFigure());
            } else {
                Insets ins = getConclusionReferenceDescription(branch, summary,
                        conclusionBranch);
                int x, y;
                switch (direction) {
                case PositionConstants.NORTH:
                    x = area.x + area.width / 2;
                    y = area.y - ins.bottom;
                    break;
                case PositionConstants.SOUTH:
                    x = area.x + area.width / 2;
                    y = area.bottom() + ins.top;
                    break;
                case PositionConstants.WEST:
                    x = area.x - ins.right;
                    y = area.y + area.height / 2;
                    break;
                default:
                    x = area.right() + ins.left;
                    y = area.y + area.height / 2;
                }
                info.put(conclusionBranch.getFigure(),
                        Geometry.getExpanded(x, y, ins));
            }
        }
    }

    private IBranchPart getConclusionBranch(IBranchPart branch,
            ISummaryPart summary, List<IBranchPart> summaryBranches) {
        IGraphicalPart part = summary.getNode();
        if (part instanceof ITopicPart) {
            IBranchPart conclusionBranch = ((ITopicPart) part).getOwnerBranch();
            if (conclusionBranch != null
                    && summaryBranches.contains(conclusionBranch)) {
                summaryBranches.remove(conclusionBranch);
                return conclusionBranch;
            }
        }
        return null;
    }

    private Insets getConclusionReferenceDescription(IBranchPart branch,
            ISummaryPart summary, IGraphicalPart conclusion) {
        IFigure fig = conclusion.getFigure();
        if (fig instanceof IReferencedFigure)
            return ((IReferencedFigure) fig).getReferenceDescription();
        Dimension size = fig.getPreferredSize();
        int w = size.width / 2;
        int h = size.height / 2;
        return new Insets(h, w, size.height - h, size.width - w);
    }

    protected Rectangle getSummaryArea(IBranchPart branch, ISummaryPart summary,
            int direction, ReferencedLayoutData data) {
        Rectangle r = null;
        for (IBranchPart subBranch : summary.getEnclosingBranches()) {
            r = Geometry.union(r, data.get(subBranch.getFigure()));
        }
        if (r == null)
            return null;

        Rectangle area = data.createInitBounds();
        int width = getPreferredSummaryWidth(summary);
        switch (direction) {
        case PositionConstants.NORTH:
            area.x = r.x;
            area.width = r.width;
            area.y = r.y - width;
            area.height = width;
            break;
        case PositionConstants.SOUTH:
            area.x = r.x;
            area.width = r.width;
            area.y = r.bottom();
            area.height = width;
            break;
        case PositionConstants.WEST:
            area.x = r.x - width;
            area.width = width;
            area.y = r.y;
            area.height = r.height;
            break;
        default:
            area.x = r.right();
            area.width = width;
            area.y = r.y;
            area.height = r.height;
        }
        IStyleSelector ss = StyleUtils.getStyleSelector(summary);
        String shape = StyleUtils.getString(summary, ss, Styles.ShapeClass,
                null);
        int lineWidth = StyleUtils.getInteger(summary, ss, Styles.LineWidth,
                shape, 1);
        return area.expand(lineWidth, lineWidth);
    }

    private int getPreferredSummaryWidth(ISummaryPart summary) {
        IFigure figure = summary.getFigure();
        if (figure instanceof IDecoratedFigure) {
            IDecoration decoration = ((IDecoratedFigure) figure)
                    .getDecoration();
            if (decoration instanceof ISummaryDecoration) {
                return ((ISummaryDecoration) decoration)
                        .getPreferredWidth(figure);
            }
        }
        return Styles.DEFAULT_SUMMARY_WIDTH
                + Styles.DEFAULT_SUMMARY_SPACING * 2;
    }

    protected void fillUnhandledSummaryBranches(IBranchPart branch,
            List<IBranchPart> summaryBranches, LayoutInfo info) {
        if (!summaryBranches.isEmpty()) {
            for (IBranchPart summaryBranch : summaryBranches) {
                info.putMinArea(summaryBranch.getFigure());
            }
        }
    }

    protected void addExtraSpaces(IBranchPart branch,
            ReferencedLayoutData data) {
        // may be subclassed
    }

    public int getSummaryDirection(IBranchPart branch, ISummaryPart summary) {
        return PositionConstants.EAST;
    }

    protected void fillOverallBoundary(IBranchPart branch,
            List<IBoundaryPart> boundaries, LayoutInfo info) {
        if (boundaries.isEmpty())
            return;
        IBoundaryPart boundary = branch.getBoundaries()
                .get(boundaries.size() - 1);

        if (boundary.getFigure() != null
                && ((BoundaryFigure) boundary.getFigure()).isTitleVisible()
                && boundary.getTitle() != null) {
            info.hasBoundaryTitles = true;
        }

        BoundaryLayoutHelper helper = getBoundaryLayoutHelper(branch);
        BoundaryData overallBoundary = helper.getOverallBoundary();
        if (overallBoundary == null)
            return;

        if (info.isMinimized()) {
            info.putMinArea(overallBoundary.boundaryFigure);
        } else {
            Rectangle area = info.getCheckedClientArea();
            area = overallBoundary.expanded(area.getCopy());
            info.put(overallBoundary.boundaryFigure, area);
        }
    }

    public int getRangeGrowthDirection(IBranchPart branch,
            IBranchRangePart range) {
        return PositionConstants.SOUTH;
    }

    public void invalidate(IGraphicalPart part) {
        if (part instanceof IBranchPart) {
            invalidateBranch((IBranchPart) part);
        }
    }

    protected void invalidateBranch(IBranchPart branch) {
        MindMapUtils.flushCache(branch, CACHE_STRUCTURE_DATA);
        MindMapUtils.flushCache(branch, CACHE_BOUNDARY_LAYOUT_HELPER);
        ITopicPart topic = branch.getTopicPart();
        if (topic != null) {
            IFigure topicFigure = topic.getFigure();
            if (topicFigure != null) {
                topicFigure.invalidate();
            }
        }
    }

    protected BoundaryLayoutHelper getBoundaryLayoutHelper(IBranchPart branch) {
        BoundaryLayoutHelper helper = (BoundaryLayoutHelper) MindMapUtils
                .getCache(branch, CACHE_BOUNDARY_LAYOUT_HELPER);
        if (helper == null) {
            helper = new BoundaryLayoutHelper();
            helper.reset(branch, this, null);
            MindMapUtils.setCache(branch, CACHE_BOUNDARY_LAYOUT_HELPER, helper);
        }
        return helper;
    }

    protected Dimension getBorderedSize(IBranchPart branch,
            IBranchPart subBranch) {
        return getBoundaryLayoutHelper(branch).getBorderedSize(subBranch);
    }

    protected int getMinorSpacing(IBranchPart branch) {
        return getInteger(branch,
                branch.getBranchPolicy().getStyleSelector(branch),
                Styles.MinorSpacing, 5);
    }

    protected int getMajorSpacing(IBranchPart branch) {
        return StyleUtils.getMajorSpacing(branch, 5);
    }

    protected Object getStructureData(IBranchPart branch) {
        Object data = MindMapUtils.getCache(branch, CACHE_STRUCTURE_DATA);
        if (!isValidStructureData(branch, data)) {
            data = createStructureData(branch);
            if (data != null) {
                MindMapUtils.setCache(branch, CACHE_STRUCTURE_DATA, data);
            }
        }
        return data;
    }

    protected Object createStructureData(IBranchPart branch) {
        return null;
    }

    protected boolean isValidStructureData(IBranchPart branch, Object data) {
        return data != null;
    }

//    public void calculateSubBranchInsets(BoundaryData boundary,
//            SubBranchData subBranch, Insets insets) {
//    }

    /*
     * Subclass may extend this method.
     * @seeorg.xmind.ui.mindmap.graphicalpolicies.IBranchStructure#
     * calcSourceOrientation(org.xmind.ui.parts.IBranchPart)
     */
    public int getSourceOrientation(IBranchPart branch) {
        return PositionConstants.NONE;
    }

    /*
     * Subclass may extend this method.
     * @seeorg.xmind.ui.mindmap.graphicalpolicies.IBranchStructure#
     * calcChildTargetOrientation(org.xmind.ui.parts.IBranchPart,
     * org.xmind.ui.parts.IBranchPart)
     */
    public int getChildTargetOrientation(IBranchPart branch,
            IBranchPart subBranch) {
        return PositionConstants.NONE;
    }

//    /*
//     * Subclass may extend this method.
//     * 
//     * @see org.xmind.ui.graphicalpolicies.IBranchStructure#calcChildTargetOrientation(org.xmind.ui.parts.IBranchPart,
//     *      org.xmind.gef.draw2d.IReferencedFigure)
//     */
//    public int calcChildTargetOrientation(IBranchPart branch,
//            IReferencedFigure childFigure) {
//        return PositionConstants.NONE;
//    }

    /*
     * Subclass may extend this method.
     * @seeorg.xmind.ui.mindmap.graphicalpolicies.IBranchStructure#
     * calcChildIndex(org.xmind.ui.tools.ParentSearchKey)
     */
    public int calcChildIndex(IBranchPart branch, ParentSearchKey key) {
        return calcInsIndex(branch, key, false);
    }

    /*
     * Subclass may extend this method.
     * @seeorg.xmind.ui.mindmap.graphicalpolicies.IBranchStructure#
     * calcChildDistance(org.xmind.ui.parts.IBranchPart,
     * org.xmind.ui.tools.ParentSearchKey)
     */
    public int calcChildDistance(IBranchPart branch, ParentSearchKey key) {
        return -1;
    }

    public IInsertion calcInsertion(IBranchPart branch, ParentSearchKey key) {
        return null;
    }

    public IPart calcNavigation(IBranchPart branch, String navReqType) {
        return null;
    }

    public IPart calcChildNavigation(IBranchPart branch,
            IBranchPart sourceChild, String navReqType, boolean sequential) {
        if (GEF.REQ_NAV_BEGINNING.equals(navReqType)) {
            return getSubTopicPart(branch, 0);
        } else if (GEF.REQ_NAV_END.equals(navReqType)) {
            return getSubTopicPart(branch, branch.getSubBranches().size() - 1);
        }
        return null;
    }

    public void calcSequentialNavigation(IBranchPart branch,
            IBranchPart startChild, IBranchPart endChild,
            List<IBranchPart> results) {
        addSubBranches(branch, startChild.getBranchIndex(),
                endChild.getBranchIndex(), results);
    }

    public void calcTraversableBranches(IBranchPart branch,
            IBranchPart sourceChild, List<IBranchPart> results) {
        addSubBranch(branch, sourceChild.getBranchIndex() + 1, results);
        results.add(branch);
        addSubBranch(branch, sourceChild.getBranchIndex() - 1, results);
    }

    public void calcTraversableChildren(IBranchPart branch,
            List<IBranchPart> results) {
        addSubBranches(branch, 0, branch.getSubBranches().size() - 1, results);
    }

    protected void addSubBranches(IBranchPart branch, IBranchPart fromChild,
            IBranchPart toChild, List<IBranchPart> results) {
        addSubBranches(branch, branch.getSubBranches().indexOf(fromChild),
                branch.getSubBranches().indexOf(toChild), results);
    }

    protected void addSubBranches(IBranchPart branch, int fromIndex,
            int toIndex, List<IBranchPart> results) {
        boolean decreasing = toIndex < fromIndex;
        for (int i = fromIndex; decreasing ? i >= toIndex : i <= toIndex;) {
            addSubBranch(branch, i, results);
            if (decreasing) {
                i--;
            } else {
                i++;
            }
        }
    }

    protected void addSubBranch(IBranchPart branch, int index,
            List<IBranchPart> results) {
        if (index < 0 || index >= branch.getSubBranches().size())
            return;
        results.add(branch.getSubBranches().get(index));
    }

    protected IBranchPart getSubBranch(IBranchPart branch, int index) {
        if (index >= 0 && index < branch.getSubBranches().size()) {
            return branch.getSubBranches().get(index);
        }
        return null;
    }

    protected ITopicPart getSubTopicPart(IBranchPart branch, int index) {
        IBranchPart subBranch = getSubBranch(branch, index);
        if (subBranch != null)
            return subBranch.getTopicPart();
        return null;
    }

    protected IInsertion getCurrentInsertion(IBranchPart branch) {
        return (IInsertion) MindMapUtils.getCache(branch,
                IInsertion.CACHE_INSERTION);
    }

    public int getQuickMoveOffset(IBranchPart branch, IBranchPart child,
            int direction) {
        return 0;
    }

    protected Point getChildRef(IBranchPart branch, Point branchRef,
            ParentSearchKey key) {
        return key.getCursorPos();
    }

    public Point calcInsertionPosition(IBranchPart branch, IBranchPart child,
            ParentSearchKey key) {
        if (child == null)
            return calcInsertPosition(branch, child, key);

        if (getOldIndex(branch, child) == -1) {
            return calcInsertPosition(branch, child, key);
        } else {
            return calcMovePosition(branch, child, key);
        }
    }

    public boolean isBranchMoved(IBranchPart branch, IBranchPart child,
            ParentSearchKey key) {
        int index = calcInsIndex(branch, key, true);
        List<Integer> disables = getDisableBranches(branch);

        return !(disables != null
                && (disables.contains(index) || disables.contains(index - 1)));
    }

    protected int calcInsIndex(IBranchPart branch, ParentSearchKey key,
            boolean withDisabled) {
        return -1;
    }

    protected Point calcInsertPosition(IBranchPart branch, IBranchPart child,
            ParentSearchKey key) {
        List<IBranchPart> subBranches = branch.getSubBranches();

        if (subBranches.isEmpty())
            return calcFirstChildPosition(branch, key);

        int index = calcInsIndex(branch, key, true);

        int minorSpacing = getMinorSpacing(branch);
        Dimension insSize = key.getFigure().getSize();
        Dimension inventSize = key.getInvent().getSize();

        List<IBoundaryPart> boundaries = branch.getBoundaries();

        if (index == 0) {
            IBranchPart sub = subBranches.get(0);
            Rectangle bounds = sub.getFigure().getBounds();
            if (!boundaries.isEmpty()) {
                for (IBoundaryPart boundary : boundaries) {
                    Rectangle bBounds = boundary.getFigure().getBounds();
                    List<IBranchPart> enclosingBranches = boundary
                            .getEnclosingBranches();
                    if ((!enclosingBranches.isEmpty())
                            && sub.equals(enclosingBranches.get(0)))
                        bounds = bBounds.contains(bounds) ? bBounds : bounds;
                }
            }

            int x;
            if (key.getCursorPos().x > 0)
                x = bounds.x + inventSize.width / 2;
            else
                x = bounds.right() - inventSize.width / 2;
            int y = bounds.y - minorSpacing - insSize.height / 2;

            return new Point(x, y);
        }

        if (index == subBranches.size()) {
            IBranchPart sub = subBranches.get(subBranches.size() - 1);
            Rectangle bounds = sub.getFigure().getBounds();
            if (!boundaries.isEmpty()) {
                for (IBoundaryPart boundary : boundaries) {
                    Rectangle bBounds = boundary.getFigure().getBounds();
                    List<IBranchPart> enclosingBranches = boundary
                            .getEnclosingBranches();
                    if ((!enclosingBranches.isEmpty())
                            && sub.equals(enclosingBranches
                                    .get(enclosingBranches.size() - 1)))
                        bounds = bBounds.contains(bounds) ? bBounds : bounds;
                }
            }

            int x;
            if (key.getCursorPos().x > 0)
                x = bounds.x + inventSize.width / 2;
            else
                x = bounds.right() - inventSize.width / 2;
            int y = bounds.bottom() + minorSpacing + insSize.height / 2;

            return new Point(x, y);
        }
        return calcInventPosition(subBranches.get(index - 1),
                subBranches.get(index), key, key.getCursorPos().x > 0);

    }

    protected Point calcMovePosition(IBranchPart branch, IBranchPart child,
            ParentSearchKey key) {
        List<IBranchPart> subBranches = branch.getSubBranches();
        List<Integer> disables = getDisableBranches(branch);

        int index = calcInsIndex(branch, key, true);
        int oldIndex = getOldIndex(branch, child);
        if (disables != null) {
            if (disables.contains(index - 1)) {
                index--;
                oldIndex = index;
            } else if (disables.contains(index)) {
                oldIndex = index;
            }
        }
        Dimension inventSize = key.getInvent().getSize();

        if (index == oldIndex) {
            IBranchPart sub = subBranches.get(index);
            int delta = getTopicSize(sub).width / 2 - inventSize.width / 2;
            return getFigureLocation(sub.getFigure()).getTranslated(
                    key.getCursorPos().x < 0 ? delta : -delta, 0);
        }

        return calcInsertPosition(branch, child, key);
    }

    protected Point calcFirstChildPosition(IBranchPart branch,
            ParentSearchKey key) {
        int x = getTopicSize(branch).width / 2 + getMajorSpacing(branch)
                + key.getInvent().getSize().width / 2;
        return getFigureLocation(branch.getFigure())
                .getTranslated(key.getCursorPos().x < 0 ? -x : x, 0);
    }

    protected Point calcInventPosition(IBranchPart orientation,
            IBranchPart assist, ParentSearchKey key, boolean isRightOrUp) {
        int minorSpacing = getMinorSpacing(orientation.getParentBranch());
        Dimension insSize = key.getFigure().getSize();
        Dimension inventSize = key.getInvent().getSize();

        Rectangle oriBounds = orientation.getFigure().getBounds();
        Rectangle assBounds = assist.getFigure().getBounds();

        Rectangle uBounds = oriBounds;
        Rectangle dBounds = assBounds;

        List<IBoundaryPart> boundaries = orientation.getParentBranch()
                .getBoundaries();
        if (!boundaries.isEmpty()) {
            for (IBoundaryPart boundary : boundaries) {
                List<IBranchPart> enclosingBranches = boundary
                        .getEnclosingBranches();
                Rectangle bBounds = boundary.getFigure().getBounds();

                if ((!enclosingBranches.isEmpty()) && orientation.equals(
                        enclosingBranches.get(enclosingBranches.size() - 1)))
                    uBounds = bBounds.contains(uBounds) ? bBounds : uBounds;

                if ((!enclosingBranches.isEmpty())
                        && assist.equals(enclosingBranches.get(0)))
                    dBounds = bBounds.contains(dBounds) ? bBounds : dBounds;
            }
        }

        boolean isBefourBounds;
        Rectangle bounds;
        if (uBounds.equals(oriBounds)) {
            bounds = uBounds;
            isBefourBounds = false;
        } else if (dBounds.equals(assBounds)) {
            bounds = dBounds;
            isBefourBounds = true;
        } else {
            if (isRightOrUp) {
                if (uBounds.x > dBounds.x) {
                    bounds = uBounds;
                    isBefourBounds = false;
                } else {
                    bounds = dBounds;
                    isBefourBounds = true;
                }
            } else {
                if (uBounds.right() < dBounds.right()) {
                    bounds = uBounds;
                    isBefourBounds = false;
                } else {
                    bounds = dBounds;
                    isBefourBounds = true;
                }
            }
        }

        int x;
        if (isRightOrUp)
            x = bounds.x + inventSize.width / 2;
        else
            x = bounds.right() - inventSize.width / 2;

        int y;
        if (isBefourBounds)
            y = bounds.y - minorSpacing - insSize.height / 2;
        else
            y = bounds.bottom() + minorSpacing + insSize.height / 2;

        return new Point(x, y);
    }

    protected int getOldIndex(IBranchPart branch, IBranchPart child) {
        List<IBranchPart> subBranches = branch.getSubBranches();

        if (branch.equals(child.getParentBranch()))
            return child.getBranchIndex();

        for (IBranchPart sub : subBranches) {
            if (!sub.getFigure().isEnabled())
                return sub.getBranchIndex();
        }

        return -1;
    }

    protected Point getFigureLocation(IFigure figure) {
        if (figure instanceof IReferencedFigure)
            return ((IReferencedFigure) figure).getReference();
        else
            return figure.getBounds().getLocation();
    }

    protected Dimension getTopicSize(IBranchPart branch) {
        if (branch == null)
            return new Dimension();
        return branch.getTopicPart().getFigure().getSize();
    }

    protected List<Integer> getDisableBranches(IBranchPart branch) {
        List<IBranchPart> subBranches = branch.getSubBranches();
        List<Integer> disables = null;

        for (int i = 0; i < subBranches.size(); i++) {
            IBranchPart sub = subBranches.get(i);
            if (!sub.getFigure().isEnabled()) {
                if (disables == null)
                    disables = new ArrayList<Integer>();
                disables.add(i);
            }
        }
        return disables;
    }

}
