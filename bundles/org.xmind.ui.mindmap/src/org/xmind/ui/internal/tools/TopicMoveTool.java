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
package org.xmind.ui.internal.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.UpdateManager;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.graphics.Cursor;
import org.xmind.core.ISummary;
import org.xmind.core.ITopic;
import org.xmind.core.ITopicRange;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.draw2d.DecoratedShapeFigure;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.draw2d.decoration.PathShapeDecoration;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.gef.event.MouseDragEvent;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.gef.part.IGraphicalEditPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.status.IStatusListener;
import org.xmind.gef.status.StatusEvent;
import org.xmind.gef.tool.ITool;
import org.xmind.ui.branch.IInsertableBranchStructureExtension;
import org.xmind.ui.branch.ILockableBranchStructureExtension;
import org.xmind.ui.branch.IMovableBranchStructureExtension;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.prefs.PrefConstants;
import org.xmind.ui.tools.DummyMoveTool;
import org.xmind.ui.tools.ITopicMoveToolHelper;
import org.xmind.ui.tools.ParentSearchKey;
import org.xmind.ui.tools.ParentSearcher;
import org.xmind.ui.util.MindMapUtils;

public class TopicMoveTool extends DummyMoveTool implements IStatusListener {

    private class RoundedRectDecoration extends PathShapeDecoration {

        public Insets getPreferredInsets(IFigure figure, int width,
                int height) {
            int lineWidth = getLineWidth();
            return new Insets(lineWidth, lineWidth, lineWidth, lineWidth);
        }

        @Override
        protected void sketch(IFigure figure, Path shape, Rectangle box,
                int purpose) {
            shape.addRoundedRectangle(box, 5);
        }

    }

    private static final int INVENT_WIDTH = 60;

    private static final int INVENT_HEIGHT = 16;

    private static ITopicMoveToolHelper defaultHelper = null;

    private IFigure invent = null;

    private Point inventStartLoc = null;

    private ParentSearcher parentSearcher = null;

    private boolean slightMove = false;

    private boolean specialMove = false;

    private ITopicMoveToolHelper helper = null;

    private ParentSearchKey key = null;

    private BranchDummy branchDummy = null;

    private IBranchPart targetParent = null;

    private List<IFigure> disabledFigures = null;

    private IPart specialTargetPart = null;

    private int specialIndex = -1;

    public TopicMoveTool() {
        initMoveTopicMoveTool();
        getStatus().addStatusListener(this);
    }

    private void initMoveTopicMoveTool() {
        IPreferenceStore prefStore = MindMapUIPlugin.getDefault()
                .getPreferenceStore();
        boolean status = prefStore
                .getBoolean(PrefConstants.MANUAL_LAYOUT_ALLOWED);
        getStatus().setStatus(GEF.ST_FREE_MOVE_MODE, status);
    }

    public void setSource(IGraphicalEditPart source) {
        Assert.isTrue(source instanceof ITopicPart);
        super.setSource(source);
    }

    protected ITopicPart getSourceTopic() {
        return (ITopicPart) super.getSource();
    }

    protected IBranchPart getSourceBranch() {
        return (IBranchPart) getSourceTopic().getParent();
    }

    public IFigure getInvent() {
        return invent;
    }

    private void doCreateInvent() {
        if (invent != null)
            return;

        if (!getStatus().isStatus(GEF.ST_ACTIVE))
            return;

        invent = createInvent();
    }

    private Point getInventStartLoc() {
        if (inventStartLoc == null) {
            IFigure fig = getInvent();
            if (fig != null) {
                if (fig instanceof IReferencedFigure) {
                    inventStartLoc = ((IReferencedFigure) fig).getReference();
                } else {
                    inventStartLoc = fig.getBounds().getLocation();
                }
            }
        }
        return inventStartLoc;
    }

    protected void onActivated(ITool prevTool) {
        super.onActivated(prevTool);
        lockBranchStructures(getTargetViewer().getRootPart());
        collectDisabledBranches();
        if (!isCopyMove()) {
            disableFigures();
        }
    }

    private void lockBranchStructures(IPart part) {
        if (part instanceof IBranchPart) {
            IBranchPart branch = (IBranchPart) part;
            IStructure sa = branch.getBranchPolicy().getStructure(branch);
            if (sa instanceof ILockableBranchStructureExtension) {
                ((ILockableBranchStructureExtension) sa).lock(branch);
            }
        }
        for (IPart child : part.getChildren()) {
            lockBranchStructures(child);
        }
    }

    private void unlockBranchStructures(IPart part) {
        if (part instanceof IBranchPart) {
            IBranchPart branch = (IBranchPart) part;
            IStructure sa = branch.getBranchPolicy().getStructure(branch);
            if (sa instanceof ILockableBranchStructureExtension) {
                ((ILockableBranchStructureExtension) sa).unlock(branch);
            }
        }
        for (IPart child : part.getChildren()) {
            unlockBranchStructures(child);
        }
    }

    protected IFigure createDummy() {
        slightMove = true;
        if (branchDummy == null) {
            branchDummy = new BranchDummy(getTargetViewer(), getSourceBranch());
        }
        return branchDummy.getBranch().getFigure();
    }

    protected void destroyDummy(IFigure dummy) {
        if (branchDummy != null) {
            branchDummy.dispose();
            branchDummy = null;
        }
        super.destroyDummy(dummy);
    }

    private void destroyInvent() {
        if (invent != null) {
            destroyInvent(invent);
            invent = null;
        }
    }

    private IFigure createInvent() {
        DecoratedShapeFigure figure = new DecoratedShapeFigure();
        Point loc = getDummyStartLoc();
        if (loc != null)
            figure.setLocation(
                    loc.getTranslated(-INVENT_WIDTH / 2, -INVENT_HEIGHT / 2));
        figure.setSize(INVENT_WIDTH, INVENT_HEIGHT);
        figure.setDecoration(new RoundedRectDecoration());

        Layer layer = getTargetViewer().getLayer(GEF.LAYER_PRESENTATION);
        if (layer != null)
            layer.add(figure);

        return figure;
    }

    private void collectDisabledBranches() {
        List<IPart> selectedParts = getSelectedParts(getTargetViewer());
        for (IPart part : selectedParts) {
            addDisabledPart(part);
        }
        List<ITopic> topics = MindMapUtils.getTopics(selectedParts);
        Set<ITopicRange> ranges = MindMapUtils.findContainedRanges(topics, true,
                false);
        if (!ranges.isEmpty()) {
            for (ITopicRange r : ranges) {
                ITopic st = ((ISummary) r).getTopic();
                if (st != null) {
                    addDisabledPart(getTargetViewer().findPart(st));
                }
            }
        }
    }

    private void addDisabledPart(IPart part) {
        if (part instanceof ITopicPart) {
            ITopicPart topic = (ITopicPart) part;
            addDisabledFigure(topic.getFigure());
            IBranchPart branch = topic.getOwnerBranch();
            if (branch != null) {
                addDisabledFigure(branch.getFigure());
            }
        }
    }

    private void addDisabledFigure(IFigure figure) {
        if (disabledFigures == null)
            disabledFigures = new ArrayList<IFigure>();
        disabledFigures.add(figure);
    }

    private void clearDisabledFigures() {
        disabledFigures = null;
    }

    private void disableFigures() {
        if (disabledFigures != null) {
            for (IFigure figure : disabledFigures) {
                figure.setEnabled(false);
            }
        }
    }

    private void enableFigures() {
        if (disabledFigures != null) {
            for (IFigure figure : disabledFigures) {
                figure.setEnabled(true);
            }
        }
    }

    protected void onMoving(Point currentPos, MouseDragEvent me) {
        if (slightMove) {
            if (!((IGraphicalEditPart) getSourceTopic())
                    .containsPoint(currentPos)) {
                slightMove = false;
            }
        }
        super.onMoving(currentPos, me);
        if (branchDummy != null) {
            key = new ParentSearchKey(
                    getSourceTopic(), (IReferencedFigure) branchDummy
                            .getBranch().getTopicPart().getFigure(),
                    currentPos);
            key.setFeedback(branchDummy.getBranch());
            targetParent = updateTargetParent();
            if (specialIndex < 0) {
                if (invent == null)
                    invent = createInvent();
                key.setInvent(invent);
                updateInventPosition(currentPos);
            }
            updateWithParent(targetParent);
        }
    }

    private void updateInventPosition(Point pos) {
        IFigure fig = getInvent();
        int x = 0;
        int y = 0;
        if (fig != null) {
            Point cursorStart = getStartingPosition();
            Point inventStart = getInventStartLoc();
            if (usesRelativeLocation() && cursorStart != null
                    && inventStart != null) {
                if (targetParent != null && key != null) {
                    Point insertionPosition = calcInsertionPosition(
                            targetParent, getSourceBranch(), key);
                    x = insertionPosition.x;
                    y = insertionPosition.y;
                } else {
                    x = pos.x - cursorStart.x + inventStart.x;
                    y = pos.y - cursorStart.y + inventStart.y;
                }
                if (fig instanceof IReferencedFigure)
                    ((IReferencedFigure) fig).setReference(x, y);
                else
                    fig.setLocation(new Point(x, y));
            } else {
                if (fig instanceof IReferencedFigure)
                    ((IReferencedFigure) fig).setReference(pos.x, pos.y);
                else
                    fig.setLocation(pos);
            }
        }
    }

    private Point calcInsertionPosition(IBranchPart parent, IBranchPart child,
            ParentSearchKey key) {
        UpdateManager um = key.getFigure().getUpdateManager();
        if (um != null)
            um.performValidation();

        if (parent != null) {
            IStructure structure = parent.getBranchPolicy()
                    .getStructure(parent);
            if (structure instanceof IInsertableBranchStructureExtension) {
                return ((IInsertableBranchStructureExtension) structure)
                        .calcInsertionPosition(parent, child, key);
            }
        }
        return new Point(0, 0);
    }

    private boolean isBranchMoved(IBranchPart parent, IBranchPart child,
            ParentSearchKey key) {
        UpdateManager um = key.getFigure().getUpdateManager();
        if (um != null)
            um.performValidation();

        if (parent != null) {
            IStructure structure = parent.getBranchPolicy()
                    .getStructure(parent);
            if (structure instanceof IInsertableBranchStructureExtension) {
                return ((IInsertableBranchStructureExtension) structure)
                        .isBranchMoved(parent, child, key);
            }
        }

        return true;
    }

    private IBranchPart updateTargetParent() {
        ITopicPart topicPart = getSourceParentTopic();
        ITopic topic = null;
        if (topicPart != null)
            topic = topicPart.getTopic();
        if (isFloatMove() && topic != null)
            return null;

        if (isSpecialFreeMove() && specialTargetPart instanceof IBranchPart) {
            return (IBranchPart) specialTargetPart;
        }
        if (isFreeMove() || isSlightMove()) {
            IPart parent = getSourceBranch().getParent();
            return parent instanceof IBranchPart ? (IBranchPart) parent : null;
        }
        return getParentSearcher()
                .searchTargetParent(getTargetViewer().getRootPart(), key);
    }

    private void updateWithParent(IBranchPart parent) {
        updateDummyWithParent(parent);
        updateHelperWithParent(parent);
        if (specialIndex < 0)
            updateInventVisible(parent);
    }

    private void updateDummyWithParent(IBranchPart parent) {
    }

    private void updateInventVisible(IBranchPart parent) {
        if (invent != null)
            invent.setVisible(parent != null);
    }

    protected void updateDummyPosition(Point pos) {
        super.updateDummyPosition(pos);
    }

    private void updateHelperWithParent(IBranchPart parent) {
        ITopicMoveToolHelper oldHelper = this.helper;
        ITopicMoveToolHelper newHelper = getHelper(parent);
        if (newHelper != oldHelper) {
            if (oldHelper != null)
                oldHelper.deactivate(getDomain(), getTargetViewer());
            if (newHelper != null)
                newHelper.activate(getDomain(), getTargetViewer());
            this.helper = newHelper;
        }
        if (helper != null) {
            helper.update(parent, isBranchMoved(parent, getSourceBranch(), key),
                    key, specialIndex);
        }
    }

    private ITopicMoveToolHelper getHelper(IBranchPart parent) {
//        if (parent != null) {
//            ITopicMoveToolHelper helper = (ITopicMoveToolHelper) parent
//                    .getBranchPolicy().getToolHelper(parent,
//                            ITopicMoveToolHelper.class);
//            if (helper != null)
//                return helper;
//        }
        return getDefaultHelper();
    }

    protected static ITopicMoveToolHelper getDefaultHelper() {
        if (defaultHelper == null) {
            defaultHelper = new TopicMoveToolHelper();
        }
        return defaultHelper;
    }

    private boolean isSlightMove() {
        if (isFloatMove() || isAlreadyFloat())
            return false;
        if (isSpecialFreeMove())
            return false;
        if (isFreeable()) {
            if (isFreeMove() || isAlreadyFree())
                return false;
        }
        return slightMove;
    }

    private boolean isFloatMove() {
        return getStatus().isStatus(GEF.ST_SHIFT_PRESSED);
    }

    private boolean isFreeMove() {
        if (isFreeMovePattern()) {
            ITopicPart topicPart = getSourceParentTopic();
            ITopic topic = null;
            if (topicPart != null)
                topic = topicPart.getTopic();
            if (isFloatMove() && topic == null)
                return false;
            return true;
        }
        if (isSpecialFreeMove())
            return true;
        if (Util.isMac())
            return getStatus().isStatus(GEF.ST_CONTROL_PRESSED);
        return getStatus().isStatus(GEF.ST_ALT_PRESSED);
    }

    private boolean isFreeMovePattern() {
        ITopicPart topicPart = getSourceParentTopic();
        ITopic topic = null;
        if (topicPart != null)
            topic = topicPart.getTopic();

        return topic == null ? (getStatus().isStatus(GEF.ST_FREE_MOVE_MODE))
                : getStatus().isStatus(GEF.ST_FREE_MOVE_MODE) && topic.isRoot();
    }

    private boolean isCopyMove() {
        if (Util.isMac())
            return getStatus().isStatus(GEF.ST_ALT_PRESSED);
        return getStatus().isStatus(GEF.ST_CONTROL_PRESSED);
    }

    private boolean isSpecialFreeMove() {
        return specialMove;
    }

    private boolean hasSpecicalPart() {
        List<IPart> selectedParts = getSelectedParts(getTargetViewer());
        for (IPart movedPart : selectedParts) {
            if (movedPart instanceof ITopicPart) {
                String type = ((ITopicPart) movedPart).getTopic().getType();
                if (ITopic.CALLOUT.equals(type)) {
                    IPart movedBranch = ((ITopicPart) movedPart).getParent();
                    specialTargetPart = movedBranch.getParent();
                    if (movedBranch != null
                            && specialTargetPart instanceof IBranchPart) {
                        specialIndex = ((IBranchPart) specialTargetPart)
                                .getCalloutBranches().indexOf(movedBranch);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    protected ParentSearcher getParentSearcher() {
        if (parentSearcher == null) {
            parentSearcher = new ParentSearcher();
        }
        return parentSearcher;
    }

    @Override
    protected void start() {
        super.start();
        if (createsDummyOnActivated())
            doCreateInvent();
        specialMove = hasSpecicalPart();
    }

    protected void end() {
        if (helper != null) {
            helper.deactivate(getDomain(), getTargetViewer());
            helper = null;
        }
        super.end();
        destroyInvent();
        inventStartLoc = null;
        targetParent = null;
        parentSearcher = null;
        key = null;
        specialMove = false;
        specialTargetPart = null;
        specialIndex = -1;
        enableFigures();
        clearDisabledFigures();
        unlockBranchStructures(getTargetViewer().getRootPart());
    }

    protected void suspend() {
        if (helper != null) {
            helper.deactivate(getDomain(), getTargetViewer());
            helper = null;
        }
        super.end();
        destroyInvent();
    }

    private void destroyInvent(IFigure invent) {
        if (invent.getParent() != null)
            invent.getParent().remove(invent);
    }

    protected Request createRequest() {
        if (isSlightMove())
            return null;

        IBranchPart targetParentBranch = this.targetParent;
        ITopicPart targetParent = targetParentBranch == null ? null
                : targetParentBranch.getTopicPart();
        boolean relative = true;//isRelative();
        Point position = relative ? getRelativePosition()
                : getAbsolutePosition();
        boolean copy = isCopyMove();
        int index = -1;
        boolean free = isFreeMove();

        if (!isFloatMove()) {
            index = getParentSearcher().getIndex(targetParentBranch, key);
            if (free) {
                if (!isFreeable() && !isAlreadyFloat()
                        && !isSpecialFreeMove()) {
                    free = false;
                }
            } else {
                if (targetParent != null) {
                    if (targetParent == getSourceParentTopic()) {
                        if (isFreeable()) {
                            free = isAlreadyFree();
                        }
                    } else {//if ((!isAlreadyFloat() && !isAlreadyFree())) {
                        position = null;
                    }
                }
            }
            if (!free && targetParent != null) {
                position = null;
            }
        }

        String reqType = copy ? GEF.REQ_COPYTO : GEF.REQ_MOVETO;
        Request request = new Request(reqType);
        request.setDomain(getDomain());
        request.setViewer(getTargetViewer());
        List<IPart> parts = new ArrayList<IPart>();
        for (IPart p : getSelectedParts(getTargetViewer())) {
            if (p.hasRole(GEF.ROLE_MOVABLE)) {
                parts.add(p);
            }
        }
        request.setTargets(parts);
//        fillTargets(request, getTargetViewer(), false);
        request.setPrimaryTarget(getSourceTopic());
        request.setParameter(GEF.PARAM_POSITION, position);
        request.setParameter(GEF.PARAM_POSITION_ABSOLUTE,
                getAbsolutePosition());
        request.setParameter(GEF.PARAM_POSITION_RELATIVE,
                Boolean.valueOf(relative));
        request.setParameter(GEF.PARAM_PARENT, targetParent);
        request.setParameter(GEF.PARAM_INDEX, Integer.valueOf(index));
        request.setParameter(MindMapUI.PARAM_COPY, Boolean.valueOf(copy));
        request.setParameter(MindMapUI.PARAM_FREE, Boolean.valueOf(free));

        IBranchPart sourceParent;
        IBranchPart sourceBranch = getSourceBranch();
        if (sourceBranch != null) {
            sourceParent = sourceBranch.getParentBranch();
            if (sourceParent != null) {
                IStructure structure = sourceParent.getBranchPolicy()
                        .getStructure(sourceParent);
                if (structure instanceof IMovableBranchStructureExtension) {
                    ((IMovableBranchStructureExtension) structure)
                            .decorateMoveOutRequest(sourceParent, key,
                                    targetParentBranch, request);
                }
            }
        } else {
            sourceParent = null;
        }

        if (targetParentBranch != null) {
            IStructure structure = targetParentBranch.getBranchPolicy()
                    .getStructure(targetParentBranch);
            if (structure instanceof IMovableBranchStructureExtension) {
                ((IMovableBranchStructureExtension) structure)
                        .decorateMoveInRequest(targetParentBranch, key,
                                sourceParent, request);
            }
        }
        return request;
    }

    private boolean isAlreadyFree() {
        return getSourceTopic().getTopic().getPosition() != null;
    }

    private boolean isAlreadyFloat() {
        return !getSourceTopic().getTopic().isAttached();
    }

    private boolean isFreeable() {
        if (!MindMapUI.isFreePositionMoveAllowed())
            return false;

        IBranchPart branch = getSourceBranch();
        return branch != null
                && MindMapUtils.isSubBranchesFreeable(branch.getParentBranch());
    }

    private ITopicPart getSourceParentTopic() {
        IBranchPart sourceBranch = getSourceBranch();
        if (sourceBranch != null) {
            IPart p = sourceBranch.getParent();
            if (p instanceof IBranchPart) {
                return ((IBranchPart) p).getTopicPart();
            }
        }
        return null;
    }

//    private boolean isRelative() {
//        return targetParent != null;
//    }

    private Point getRelativePosition() {
        Dimension off = getCursorPosition()
                .getDifference(getStartingPosition());
        return new Point(off.width, off.height);
    }

    private Point getAbsolutePosition() {
        return getCursorPosition();
    }

    public void statusChanged(StatusEvent event) {
        int k = event.key;
        if (getStatus().isStatus(GEF.ST_ACTIVE)) {
            if (k == GEF.ST_SHIFT_PRESSED || k == GEF.ST_CONTROL_PRESSED
                    || k == GEF.ST_ALT_PRESSED) {
                updateDisabilities();
                updateDummyPosition(getCursorPosition());
                targetParent = updateTargetParent();
                updateWithParent(targetParent);
            }
        }
    }

    private void updateDisabilities() {
        if (isCopyMove()) {
            enableFigures();
        } else {
            disableFigures();
        }
    }

    public Cursor getCurrentCursor(Point pos, IPart host) {
        if (isCopyMove())
            return MindMapUI.getImages().getCursor(IMindMapImages.CURSOR_ADD);
        return super.getCurrentCursor(pos, host);
    }

}
