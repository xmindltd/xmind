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

import static org.xmind.ui.mindmap.MindMapUI.REQ_ADD_ATTACHMENT;
import static org.xmind.ui.mindmap.MindMapUI.REQ_CREATE_BOUNDARY;
import static org.xmind.ui.mindmap.MindMapUI.REQ_CREATE_RELATIONSHIP;
import static org.xmind.ui.mindmap.MindMapUI.REQ_CREATE_SUMMARY;
import static org.xmind.ui.mindmap.MindMapUI.REQ_DRILLDOWN;
import static org.xmind.ui.mindmap.MindMapUI.REQ_DRILLUP;
import static org.xmind.ui.mindmap.MindMapUI.REQ_EDIT_LABEL;
import static org.xmind.ui.mindmap.MindMapUI.REQ_MODIFY_HYPERLINK;
import static org.xmind.ui.mindmap.MindMapUI.REQ_SELECT_CENTRAL;
import static org.xmind.ui.mindmap.MindMapUI.TOOL_CREATE_BOUNDARY;
import static org.xmind.ui.mindmap.MindMapUI.TOOL_CREATE_RELATIONSHIP;
import static org.xmind.ui.mindmap.MindMapUI.TOOL_CREATE_SUMMARY;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.draw2d.FreeformFigure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.xmind.core.Core;
import org.xmind.core.IFileEntry;
import org.xmind.core.IImage;
import org.xmind.core.IManifest;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.ITopicExtension;
import org.xmind.core.ITopicExtensionElement;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerRef;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.core.util.FileUtils;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.draw2d.IOriginBased;
import org.xmind.gef.event.DragDropEvent;
import org.xmind.gef.event.KeyEvent;
import org.xmind.gef.event.MouseDragEvent;
import org.xmind.gef.event.MouseEvent;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.gef.part.IGraphicalEditPart;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.service.IBendPointsFeedback;
import org.xmind.gef.service.IFeedback;
import org.xmind.gef.service.IRevealService;
import org.xmind.gef.service.IRevealServiceListener;
import org.xmind.gef.service.RevealEvent;
import org.xmind.gef.service.ZoomingAndPanningRevealService;
import org.xmind.gef.tool.AbstractTool;
import org.xmind.gef.tool.IDragDropHandler;
import org.xmind.gef.tool.ISourceTool;
import org.xmind.gef.tool.ITool;
import org.xmind.gef.tool.SelectTool;
import org.xmind.ui.branch.IBranchDoubleClickSupport;
import org.xmind.ui.branch.IBranchMoveSupport;
import org.xmind.ui.commands.DeleteMarkerCommand;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.actions.ReplaceMarkerAction;
import org.xmind.ui.internal.actions.ViewerAction;
import org.xmind.ui.internal.editor.IMESupport;
import org.xmind.ui.internal.mindmap.BoundaryPart;
import org.xmind.ui.internal.mindmap.IconTipPart;
import org.xmind.ui.internal.mindmap.InfoItemIconPart;
import org.xmind.ui.internal.protocols.FilePathParser;
import org.xmind.ui.internal.protocols.FileProtocol;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IBranchRangePart;
import org.xmind.ui.mindmap.IDrillDownTraceService;
import org.xmind.ui.mindmap.IIconTipPart;
import org.xmind.ui.mindmap.IImagePart;
import org.xmind.ui.mindmap.IInfoItemPart;
import org.xmind.ui.mindmap.ILabelPart;
import org.xmind.ui.mindmap.ILegendItemPart;
import org.xmind.ui.mindmap.ILegendPart;
import org.xmind.ui.mindmap.IMarkerPart;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.INumberingPart;
import org.xmind.ui.mindmap.IPlusMinusPart;
import org.xmind.ui.mindmap.IRelationshipPart;
import org.xmind.ui.mindmap.ISheetPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMap;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.Logger;
import org.xmind.ui.util.MindMapUtils;
import org.xmind.ui.viewers.SWTUtils;

public class MindMapSelectTool extends SelectTool {

    private IBranchPart movingSourceBranch = null;

    public MindMapSelectTool() {
        setContextId(MindMapUI.CONTEXT_MINDMAP_EDIT);
    }

    public String getType() {
        return GEF.TOOL_SELECT;
    }

    public boolean handleMouseDown(MouseEvent me) {
        if (me.target instanceof IPlusMinusPart) {
            if (me.target.getParent() instanceof IBranchPart) {
                IBranchPart branch = (IBranchPart) me.target.getParent();
                handleMouseDownOnPlusMinus(me, branch);
                return true;
            }
        } else if (me.target instanceof IconTipPart) {
            if (!me.leftOrRight) {
                if (handleMouseDownOnIconTip(me)) {
                    return true;
                }
            }
        }
        return super.handleMouseDown(me);
    }

    private boolean handleMouseDownOnIconTip(MouseEvent me) {
        IconTipPart iconTip = (IconTipPart) me.target;
        if (iconTip.getPopupMenu() == null) {
            getStatus().setStatus(GEF.ST_MOUSE_PRESSED, true);
            getStatus().setStatus(GEF.ST_MOUSE_RIGHT, !me.leftOrRight);
            selectSingle(iconTip.getTopicPart());
            return true;
        }
        return false;
    }

    protected void handleMouseDownOnPlusMinus(MouseEvent me,
            IBranchPart branch) {
        getStatus().setStatus(GEF.ST_MOUSE_PRESSED, true);
        getStatus().setStatus(GEF.ST_MOUSE_RIGHT, !me.leftOrRight);

        ITopicPart topic = branch.getTopicPart();
        if (topic != null)
            selectSingle(topic);

        sendFoldedRequest(branch);
        me.consume();
    }

    protected void sendFoldedRequest(IBranchPart branch) {
        String reqType = branch.getTopic().isFolded() ? GEF.REQ_EXTEND
                : GEF.REQ_COLLAPSE;
        Request request = new Request(reqType);
        request.setViewer(getTargetViewer());
        request.setPrimaryTarget(branch);
        request.setParameter(MindMapUI.PARAM_WITH_ANIMATION, Boolean.TRUE);
        getDomain().handleRequest(request);
    }

    public boolean handleMouseUp(final MouseEvent me) {
        if (me.target instanceof IconTipPart) {
            if (me.leftOrRight) {
                handleMouseUpOnIconTip(me);
                return true;
            }
        } else if (me.target instanceof IMarkerPart) {
            if (me.leftOrRight) {
                showMarkerMenu((IMarkerPart) me.target);
                return true;
            }
        } else if (me.target instanceof InfoItemIconPart) {
            if (me.leftOrRight) {
                handleMouseUpOnInforItem(me);
                return true;
            }
        }
        return super.handleMouseUp(me);
    }

    private void handleMouseUpOnInforItem(MouseEvent me) {
        getStatus().setStatus(GEF.ST_NO_DRAGGING, false);
        getStatus().setStatus(GEF.ST_MOUSE_PRESSED, false);
        InfoItemIconPart inforItem = (InfoItemIconPart) me.target;
        selectSingle(inforItem.getTopicPart());
        final IAction action = inforItem.getAction();
        if (action != null) {
            setToSelectOnMouseUp(null);
            Display.getCurrent().asyncExec(new Runnable() {
                public void run() {
                    SafeRunner.run(new SafeRunnable() {
                        public void run() throws Exception {
                            action.run();
                        }
                    });
                }
            });
        }
    }

    protected void handleMouseUpOnIconTip(final MouseEvent me) {
        getStatus().setStatus(GEF.ST_NO_DRAGGING, false);
        getStatus().setStatus(GEF.ST_MOUSE_PRESSED, false);
        IconTipPart iconTip = (IconTipPart) me.target;
        selectSingle(iconTip.getTopicPart());
        final IAction action = iconTip.getAction();
        if (action != null) {
            setToSelectOnMouseUp(null);
            Display.getCurrent().asyncExec(new Runnable() {
                public void run() {
                    SafeRunner.run(new SafeRunnable() {
                        public void run() throws Exception {
                            action.run();
                        }
                    });
                }
            });
        }
    }

    protected boolean handleMouseDoubleClick(MouseEvent me) {
        if (me.leftOrRight) {
            if (me.target == null
                    || me.target == getTargetViewer().getRootPart()
                    || me.target instanceof ISheetPart) {
                IBranchPart branch = findBranch(getTargetViewer().getRootPart(),
                        me.cursorLocation);
                if (branch == null || !handleDoubleClickOnBranch(branch,
                        me.cursorLocation)) {
                    handleCreateFloatingTopicRequest(me.cursorLocation);
                }
                me.consume();
                return true;
            }
        }
        return super.handleMouseDoubleClick(me);
    }

    private IBranchPart findBranch(IPart part, Point pos) {
        if (!part.getStatus().isActive())
            return null;

        if (part instanceof IBranchPart) {
            IBranchPart branch = (IBranchPart) part;
            if (branch.getFigure().isVisible()
                    && branch.getFigure().containsPoint(pos)) {
                if (branch.canSearchChild()) {
                    for (IBranchPart sub : branch.getSubBranches()) {
                        IBranchPart b = findBranch(sub, pos);
                        if (b != null)
                            return b;
                    }
                    for (IBranchPart sub : branch.getCalloutBranches()) {
                        IBranchPart b = findBranch(sub, pos);
                        if (b != null)
                            return b;
                    }
                    for (IBranchPart sub : branch.getSummaryBranches()) {
                        IBranchPart b = findBranch(sub, pos);
                        if (b != null)
                            return b;
                    }
                }
                return branch;
            }
        } else {
            if (!(part instanceof IGraphicalPart)
                    || ((IGraphicalPart) part).getFigure().isVisible()) {
                for (IPart child : part.getChildren()) {
                    IBranchPart branch = findBranch(child, pos);
                    if (branch != null)
                        return branch;
                }
            }
        }
        return null;
    }

    private boolean handleDoubleClickOnBranch(IBranchPart branch, Point pos) {
        do {
            IStructure structure = branch.getBranchPolicy()
                    .getStructure(branch);
            if (structure instanceof IBranchDoubleClickSupport) {
                if (((IBranchDoubleClickSupport) structure)
                        .handleDoubleClick(branch, pos))
                    return true;
            }
            branch = branch.getParentBranch();
        } while (branch != null);
        return false;
    }

    protected boolean handleKeyDown(final KeyEvent ke) {
        boolean handled = super.handleKeyDown(ke);
        if (!handled) {
            int keyCode = ke.keyCode;
            int stateMask = ke.getState();
            Request navScrollRequest = createNavScrollRequest(stateMask,
                    keyCode);
            if (navScrollRequest != null) {
                return handleNavScroll(navScrollRequest);
            }

            IPart p = getTargetViewer().getFocusedPart();
            if (p != null && SWTUtils.matchKey(stateMask, keyCode, 0, ' ')
                    && handleQuickOpen(p))
                return true;
            if (SWTUtils.matchKey(stateMask, keyCode, 0, SWT.ESC)
                    && hideQuickOpen())
                return true;
            if (p != null && p.hasRole(GEF.ROLE_EDITABLE)) {
                if (MindMapUtils.isTopicTextChar(ke.character)) {
                    int ignoreCount = getTargetViewer().getProperties()
                            .getInteger(IMESupport.PROP_IGNORE_KEY_DOWN, 0);
                    if (ignoreCount > 0) {
                        getTargetViewer().getProperties().set(
                                IMESupport.PROP_IGNORE_KEY_DOWN,
                                ignoreCount - 1);
                    } else {
                        Request req = new Request(GEF.REQ_EDIT);
                        req.setDomain(getDomain());
                        req.setViewer(getTargetViewer());
                        fillTargets(req, getTargetViewer(), false);
                        if (req.hasTargets()) {
                            startEditing(p, req);
                            if (ke.character != ' ') {
                                final ITool activeTool = getDomain()
                                        .getActiveTool();
                                if (activeTool != this) {
//                                    Display.getCurrent().asyncExec(
//                                            new Runnable() {
//                                                public void run() {
                                    activeTool.keyDown(ke, getTargetViewer());
//                                                }
//                                            });
                                }
                            }
                            return true;
                        }
                    }
                }
            }
        }
        return handled;
    }

    protected boolean hideQuickOpen() {
        if (QuickOpenHelper.getInstance().isOpen()) {
            QuickOpenHelper.getInstance().hide();
            return true;
        }
        return false;
    }

    protected boolean handleQuickOpen(IPart part) {
        if (QuickOpenHelper.getInstance().canShow()) {
            if (QuickOpenHelper.getInstance().isOpen()) {
                QuickOpenHelper.getInstance().hide();
                return true;
            } else {
                String filepath = getQuickOpenFilePaths(part);
                if (filepath != null) {
                    QuickOpenHelper.getInstance().show(filepath);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param p
     * @return
     */
    private String getQuickOpenFilePaths(IPart p) {
        if (p instanceof ITopicPart) {
            ITopic topic = ((ITopicPart) p).getTopic();
            String uri = topic.getHyperlink();
            if (uri != null) {
                String path = uriToFilePath(topic, uri);
                if (path != null)
                    return path;
            }
        } else if (p instanceof IImagePart) {
            ITopic topic = ((IImagePart) p).getTopic();
            String imagePath = getImagePath(topic);
            if (imagePath != null)
                return imagePath;
        }
        return null;
    }

    protected String uriToFilePath(ITopic topic, String uri) {
        if (HyperlinkUtils.isInternalURL(uri))
            return null;

        if (HyperlinkUtils.isAttachmentURL(uri))
            return getAttachmentAbsolutePath(topic, uri);

        String path = FilePathParser.toPath(uri);
        if (path == null)
            return null;
        return FileProtocol.getAbsolutePath(topic, path);
    }

    /**
     * @param topic
     * @return
     */
    private String getImagePath(ITopic topic) {
        IImage image = topic.getImage();
        if (image == null)
            return null;

        String uri = image.getSource();
        if (uri == null)
            return null;
        return uriToFilePath(topic, uri);
    }

    protected String getAttachmentAbsolutePath(ITopic topic, String uri) {
        String path = HyperlinkUtils.toAttachmentPath(uri);
        if (path == null)
            return null;
        IWorkbook workbook = topic.getOwnedWorkbook();
        if (workbook == null)
            return null;

        String hiberLoc = Core.getWorkspace()
                .getAbsolutePath(".temp-attachments/quickOpen"); //$NON-NLS-1$
        if (hiberLoc == null)
            return null;

        File hiberDir = new File(hiberLoc);
        //clear old cache.
        if (hiberDir.exists()) {
            hiberDir.delete();
        }

        File attFile = new File(hiberLoc, path);
        FileUtils.ensureFileParent(attFile);

        IManifest manifest = workbook.getManifest();
        IFileEntry fileEntry = manifest.getFileEntry(path);
        try {
            InputStream is = fileEntry.openInputStream();
            OutputStream os = new FileOutputStream(attFile.getAbsolutePath());
            FileUtils.transfer(is, os);
        } catch (IOException e) {
            Logger.log(e,
                    "Failed to transfer attachment to temp-attachments dir."); //$NON-NLS-1$
            return null;
        }

        if (!attFile.exists())
            return null;

        return attFile.getAbsolutePath();
    }

    protected boolean handleMouseDrag(MouseDragEvent me) {
        movingSourceBranch = null;
        return super.handleMouseDrag(me);
    }

    protected boolean canMove(IPart host, MouseDragEvent me) {
        boolean canMove = super.canMove(host, me);
        if (!canMove) {
            IBranchPart branch = findBranch(host, me.startingLocation);
            if (branch != null) {
                return canMoveInBranch(branch, me);
            }
        }
        return canMove;
    }

    private boolean canMoveInBranch(IBranchPart branch, MouseDragEvent me) {
        do {
            IStructure structure = branch.getBranchPolicy()
                    .getStructure(branch);
            if (structure instanceof IBranchMoveSupport) {
                boolean canMove = ((IBranchMoveSupport) structure)
                        .canMove(branch, me);
                if (canMove) {
                    movingSourceBranch = branch;
                }
                return canMove;
            }
            branch = branch.getParentBranch();
        } while (branch != null);
        return false;
    }

    protected String getMoveTool(IPart source, MouseDragEvent me) {
        if (movingSourceBranch != null) {
            return ((IBranchMoveSupport) movingSourceBranch.getBranchPolicy()
                    .getStructure(movingSourceBranch))
                            .getMoveTool(movingSourceBranch, me);
        }
        if (source instanceof ITopicPart) {
            return MindMapUI.TOOL_MOVE_TOPIC;
        } else if (source instanceof IRelationshipPart) {
            return MindMapUI.TOOL_MOVE_RELATIONSHIP;
        } else if (source instanceof IBranchRangePart) {
            return MindMapUI.TOOL_RESIZE_RANGE;
        } else if (source instanceof IMarkerPart) {
            return MindMapUI.TOOL_MOVE_MARKER;
        } else if (source instanceof IImagePart) {
            IImagePart imagePart = (IImagePart) source;
            Object feedback = imagePart.getAdapter(IFeedback.class);
            if (feedback instanceof IBendPointsFeedback) {
                int orientation = ((IBendPointsFeedback) feedback)
                        .getOrientation(me.startingLocation);
                if (orientation == PositionConstants.NONE) {
                    return MindMapUI.TOOL_MOVE_IMAGE;
                }
            }
            return MindMapUI.TOOL_RESIZE_IMAGE;
        } else if (source instanceof ILegendPart
                || source instanceof ILegendItemPart) {
            return MindMapUI.TOOL_MOVE_LEGEND;
        }
        return super.getMoveTool(source, me);
    }

    protected void changeToMoveTool(String moveToolType, ITool moveTool,
            IPart dragSource, MouseDragEvent me) {
        if (GEF.TOOL_MOVE.equals(moveToolType)) {
            if (!canMove(dragSource, me))
                return;
            if (!dragSource.getStatus().isSelected())
                selectSingle(dragSource);
            getTargetViewer().setFocused(dragSource);
        }
        if (movingSourceBranch != null) {
            super.changeToMoveTool(moveToolType, moveTool, movingSourceBranch,
                    me);
        } else {
            super.changeToMoveTool(moveToolType, moveTool, dragSource, me);
        }
        movingSourceBranch = null;
    }

//    protected IPart findSequenceEnd(IPart current, int direction) {
//        return MindMapUtils.findNaviPart(current, direction);
//    }

    protected List<IPart> getSequenceParts(IPart start, IPart end) {
        return MindMapUtils.getSequenceTopics(start, end);
    }

    protected Request fillTargets(Request request, IViewer viewer,
            boolean includeRootPartIfEmpty) {
        return super.fillTargets(request, viewer, includeRootPartIfEmpty)
                .setParameter(MindMapUI.PARAM_WITH_ANIMATION, Boolean.TRUE);
    }

    protected void handleNonTargetedRequest(Request request) {
        IViewer viewer = request.getTargetViewer();
        if (viewer != null) {
            String requestType = request.getType();
            if (MindMapUI.REQ_CREATE_FLOAT.equals(requestType)) {
                handleCreateFloatingTopic(request, viewer);
                return;
            } else if (MindMapUI.REQ_SELECT_BROTHERS.equals(requestType)) {
                handleSelectBrothers(request);
                return;
            } else if (MindMapUI.REQ_SELECT_CHILDREN.equals(requestType)) {
                handleSelectChildren(request);
                return;
            } else if (MindMapUI.REQ_SELECT_CENTRAL.equals(requestType)) {
                handleSelectCentral(request);
                return;
            } else if (GEF.REQ_EXTEND_ALL.equals(requestType)//
                    || GEF.REQ_COLLAPSE_ALL.equals(requestType)) {
                request = createExtendOrCollapseAllRequest(request, viewer);
            } else if (REQ_SELECT_CENTRAL.equals(requestType)) {
                handleSelectCentral(viewer);
                return;
            } else if (REQ_MODIFY_HYPERLINK.equals(requestType)) {
                request = createModifyHyperlinkRequest(request, viewer);
            } else if (REQ_ADD_ATTACHMENT.equals(requestType)) {
                request = createAddAttachmentRequest(request, viewer);
            } else if (REQ_CREATE_RELATIONSHIP.equals(requestType)) {
                handleCreateRelationship(requestType, viewer);
                return;
            } else if (REQ_CREATE_BOUNDARY.equals(requestType)) {
                request = createCreateBoundaryRequest(request, viewer);
            } else if (REQ_CREATE_SUMMARY.equals(requestType)) {
                request = createCreateSummaryRequest(request, viewer);
            } else if (REQ_EDIT_LABEL.equals(requestType)) {
                handleEditLabelRequest(request, viewer);
                return;
            } else if (REQ_DRILLDOWN.equals(requestType)) {
                handleDrillDown(viewer);
                return;
            } else if (REQ_DRILLUP.equals(requestType)) {
                handleDrillUp(viewer);
                return;
            } else if (GEF.REQ_TRAVERSE.equals(requestType)) {
                handleTraverse(viewer);
                return;
            }
            if (request == null)
                return;
            if (request.hasTargets()) {
                handleTargetedRequest(request);
                return;
            }

        }
        super.handleNonTargetedRequest(request);
    }

    /**
     * @param request
     * @param viewer
     */
    private void handleCreateFloatingTopic(Request request, IViewer viewer) {
        changeActiveTool(MindMapUI.TOOL_CREATE_FLOAT);
        getDomain().handleRequest(request);
    }

    private void handleSelectCentral(Request request) {
        IViewer viewer = request.getTargetViewer();
        if (viewer == null)
            return;

        ITopicPart centralTopic = (ITopicPart) viewer
                .getAdapter(ITopicPart.class);
        if (centralTopic == null)
            return;

        select(Arrays.asList(centralTopic), centralTopic);
        new CenteredRevealHelper(viewer).start(centralTopic);
    }

    private class CenteredRevealHelper implements IRevealServiceListener {

        private ZoomingAndPanningRevealService service;

        private boolean oldCentered;

        /**
         * 
         */
        public CenteredRevealHelper(IViewer viewer) {
            Object service = viewer.getService(IRevealService.class);
            if (service != null
                    && service instanceof ZoomingAndPanningRevealService) {
                this.service = (ZoomingAndPanningRevealService) service;
                this.oldCentered = this.service.isCentered();
            } else {
                this.service = null;
                this.oldCentered = false;
            }
        }

        public void start(IGraphicalPart part) {
            if (this.service != null) {
                this.service.setCentered(true);
                this.service.reveal(new StructuredSelection(part));
                this.service.addRevealServiceListener(this);
            }
        }

        /*
         * (non-Javadoc)
         * @see
         * org.xmind.gef.service.IRevealServiceListener#revealingStarted(org
         * .xmind.gef.service.RevealEvent)
         */
        public void revealingStarted(RevealEvent event) {
        }

        /*
         * (non-Javadoc)
         * @see
         * org.xmind.gef.service.IRevealServiceListener#revealingCanceled(org
         * .xmind.gef.service.RevealEvent)
         */
        public void revealingCanceled(RevealEvent event) {
            restore();
        }

        /*
         * (non-Javadoc)
         * @see
         * org.xmind.gef.service.IRevealServiceListener#revealingFinished(org
         * .xmind.gef.service.RevealEvent)
         */
        public void revealingFinished(RevealEvent event) {
            restore();
        }

        void restore() {
            this.service.removeRevealServiceListener(this);
            this.service.setCentered(this.oldCentered);
        }

    }

    private void handleSelectBrothers(Request request) {
        List<IPart> parts = getSelectedParts(request.getTargetViewer());
        List<ITopicPart> toSelect = new ArrayList<ITopicPart>();
        for (IPart p : parts) {
            IBranchPart branch = MindMapUtils.findBranch(p);
            if (branch != null) {
                collectBrothers(branch, toSelect);
            }
        }
        if (!toSelect.isEmpty()) {
            select(toSelect, toSelect.get(0));
        }
    }

    private void collectBrothers(IBranchPart branch,
            List<ITopicPart> toSelect) {
        IBranchPart parent = branch.getParentBranch();
        if (parent != null) {
            for (IBranchPart child : parent.getSubBranches()) {
                toSelect.add(child.getTopicPart());
            }
            for (IBranchPart child : parent.getCalloutBranches()) {
                toSelect.add(child.getTopicPart());
            }
        }
    }

    private void handleSelectChildren(Request request) {
        List<IPart> parts = getSelectedParts(request.getTargetViewer());
        List<ITopicPart> toSelect = new ArrayList<ITopicPart>();
        for (IPart p : parts) {
            IBranchPart branch = MindMapUtils.findBranch(p);
            if (branch != null) {
                collectChildren(branch, toSelect);
            }
        }
        if (!toSelect.isEmpty()) {
            select(toSelect, toSelect.get(0));
        }
    }

    private void collectChildren(IBranchPart branch,
            List<ITopicPart> toSelect) {
        for (IBranchPart child : branch.getSubBranches()) {
            toSelect.add(child.getTopicPart());
        }
        for (IBranchPart child : branch.getCalloutBranches()) {
            toSelect.add(child.getTopicPart());
        }
    }

    private void handleTraverse(IViewer viewer) {
        IPart focusedPart = viewer.getFocusedPart();
        if (focusedPart != null && focusedPart instanceof IGraphicalEditPart
                && focusedPart.getStatus().isActive()
                && focusedPart.hasRole(GEF.ROLE_TRAVERSABLE)) {
            IGraphicalEditPart source = (IGraphicalEditPart) focusedPart;
            ITool tool = getTool(GEF.TOOL_TRAVERSE);
            if (tool != null) {
                if (tool instanceof ISourceTool) {
                    ((ISourceTool) tool).setSource(source);
                }
                changeActiveTool(GEF.TOOL_TRAVERSE);
            }
        }
    }

    protected void handleTargetedRequest(Request request) {
        String requestType = request.getType();
        if (MindMapUI.REQ_EDIT_LABEL.equals(requestType)) {
            handleEditRequest(request);
            return;
        }
        if (REQ_DRILLDOWN.equals(requestType)) {
            handleDrillDown(request.getTargetViewer(), request.getTargets());
            return;
        } else if (MindMapUI.REQ_SHOW_LEGEND.equals(requestType)) {
            if (request.getPrimaryTarget() instanceof ISheetPart) {
                ISheetPart sheet = (ISheetPart) request.getPrimaryTarget();
                if (!sheet.getSheet().getLegend().hasPosition()) {
                    createLegend(request);
                    return;
                }
            }
        }
        super.handleTargetedRequest(request);
    }

    protected void navigateTo(List<IPart> toSelect, IPart toFocus,
            boolean sequential) {
        List<IBranchPart> branchesToExpand = findBranchesNeedsExpand(toSelect);
        if (branchesToExpand != null && !branchesToExpand.isEmpty()) {
            expand(branchesToExpand);
        }

        if (!canNavigateTo(toSelect))
            return;

        super.navigateTo(toSelect, toFocus, sequential);
    }

    protected boolean canNavigateTo(List<IPart> toSelect) {
        for (IPart p : toSelect) {
            if (p instanceof IGraphicalPart) {
                IFigure figure = ((IGraphicalPart) p).getFigure();
                if (!figure.isVisible())
                    return false;
            }
        }
        return true;
    }

    protected List<IBranchPart> findBranchesNeedsExpand(List<IPart> parts) {
        List<IBranchPart> branches = null;
        for (IPart p : parts) {
            IBranchPart branch = MindMapUtils.findBranch(p);
            if (branch != null) {
                IBranchPart toExpand = findBranchNeedsExpand(branch);
                if (toExpand != null) {
                    if (branches == null)
                        branches = new ArrayList<IBranchPart>();
                    branches.add(toExpand);
                }
            }
        }
        return branches;
    }

    private IBranchPart findBranchNeedsExpand(IBranchPart branch) {
        IBranchPart parent = branch.getParentBranch();
        if (parent != null) {
            if (parent.isFolded())
                return parent;
            return findBranchNeedsExpand(parent);
        }
        return null;
    }

    protected void expand(List<IBranchPart> branches) {
        getDomain().handleRequest(new Request(GEF.REQ_EXTEND)
                .setViewer(getTargetViewer()).setTargets(branches));
    }

    protected Request createAddAttachmentRequest(Request request,
            IViewer viewer) {
        List<ITopicPart> topics = MindMapUtils
                .getTopicParts(getSelectedParts(viewer));
        if (topics.isEmpty())
            return request;

        FileDialog dialog = new FileDialog(viewer.getControl().getShell(),
                SWT.OPEN | SWT.MULTI);
        String ret = dialog.open();
        if (ret == null)
            return request;

        String parentPath = dialog.getFilterPath();
        String[] fileNames = dialog.getFileNames();
        List<String> paths = new ArrayList<String>(fileNames.length);
        for (String fileName : fileNames) {
            if (fileName.contains(".")) { //$NON-NLS-1$
                MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                        UserDataConstants.CATEGORY_ATTACHMENT,
                        String.format(UserDataConstants.ATTACHMENT_FORMAT_S,
                                fileName.toLowerCase().substring(
                                        fileName.lastIndexOf('.') + 1)));
            } else {
                MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                        UserDataConstants.CATEGORY_ATTACHMENT,
                        UserDataConstants.ATTACHMENT_FORMAT_BLANK);
            }
            String path = new File(parentPath, fileName).getAbsolutePath();
            paths.add(path);
        }
        request.setTargets(topics);
        request.setParameter(GEF.PARAM_PATH, paths.toArray(new String[0]));
        return request;
    }

    protected Request createModifyHyperlinkRequest(Request request,
            IViewer viewer) {
        List<ITopicPart> topics = MindMapUtils
                .getTopicParts(getSelectedParts(viewer));
        if (topics.isEmpty())
            return null;

        if (request.hasParameter(GEF.PARAM_TEXT))
            return request.setTargets(topics);

//        Shell parentShell = viewer.getControl().getShell();
//        HyperlinkDialog dialog = new HyperlinkDialog(parentShell,
//                new StructuredSelection(topics));
//        int retCode = dialog.open();
//
//        if (retCode == HyperlinkDialog.OK) {
//            return request.setTargets(topics).setParameter(GEF.PARAM_TEXT,
//                    dialog.getValue());
//        } else if (retCode == HyperlinkDialog.REMOVE) {
//            return request.setTargets(topics)
//                    .setParameter(GEF.PARAM_TEXT, null);
//        }
        return null;
    }

    protected void handleSelectCentral(IViewer viewer) {
        ITopic centralTopic = (ITopic) viewer.getAdapter(ITopic.class);
        if (centralTopic != null) {
            viewer.setSelection(new StructuredSelection(centralTopic));
        }
    }

    protected Request createExtendOrCollapseAllRequest(Request request,
            IViewer viewer) {
        List<IPart> selectedParts = getSelectedParts(viewer);
        if (isSheetSelected(selectedParts, viewer)) {
            ISheetPart sheet = (ISheetPart) viewer.getAdapter(ISheetPart.class);
            if (sheet == null)
                return request;
            IBranchPart centralBranch = sheet.getCentralBranch();
            if (centralBranch == null)
                return request;
            selectedParts = new ArrayList<IPart>();
            selectedParts.add(centralBranch);
            selectedParts.addAll(sheet.getFloatingBranches());
        }
        request.setParameter(MindMapUI.PARAM_WITH_ANIMATION, Boolean.TRUE);
        request.setTargets(selectedParts);
        return request;
    }

    protected boolean isSheetSelected(List<IPart> parts, IViewer viewer) {
        if (parts.isEmpty())
            return true;
        for (IPart p : parts) {
            if (p instanceof ISheetPart || p == viewer.getRootPart())
                return true;
        }
        return false;
    }

    protected void handleCreateFloatingTopicRequest(Point location) {
        if (location == null)
            return;

        ISheetPart sheet = (ISheetPart) getTargetViewer()
                .getAdapter(ISheetPart.class);
        if (sheet == null)
            return;

        Point position = new Point();
        IFigure figure = sheet.getFigure();
        if (figure instanceof IOriginBased) {
            Point origin = ((IOriginBased) figure).getOrigin();
            position.setLocation(location.x - origin.x, location.y - origin.y);
        }
        Request request = new Request(MindMapUI.REQ_CREATE_FLOAT);
        request.setDomain(getDomain());
        request.setViewer(getTargetViewer());
        request.setPrimaryTarget(sheet);
        request.setParameter(GEF.PARAM_POSITION, position);
        request.setParameter(MindMapUI.PARAM_WITH_ANIMATION, Boolean.TRUE);
        getDomain().handleRequest(request);
    }

    protected boolean isSelectableOnSelectAll(IPart child) {
        if (!(child instanceof ITopicPart))
            return false;
        return super.isSelectableOnSelectAll(child);
    }

    protected void handleCreateRelationship(String reqType, IViewer viewer) {
        ITool next = getTool(TOOL_CREATE_RELATIONSHIP);
        if (next instanceof AbstractTool) {
            ((AbstractTool) next).setTargetViewer(viewer);
        }
        changeActiveTool(TOOL_CREATE_RELATIONSHIP);
    }

    protected Request createCreateBoundaryRequest(Request request,
            IViewer viewer) {
        List<IPart> selectedParts = getSelectedParts(viewer);
        List<ITopic> topics = MindMapUtils.getTopics(selectedParts);
        if (!topics.isEmpty()) {
            List<IPart> parts = MindMapUtils.getParts(topics, viewer);
            request.setTargets(parts);
            request.setParameter(MindMapUI.PARAM_WITH_ANIMATION, Boolean.TRUE);
            return request;
        }

        ITool next = getTool(TOOL_CREATE_BOUNDARY);
        if (next instanceof AbstractTool) {
            ((AbstractTool) next).setTargetViewer(viewer);
        }
        changeActiveTool(TOOL_CREATE_BOUNDARY);
        return null;
    }

    private Request createCreateSummaryRequest(Request request,
            IViewer viewer) {
        List<IPart> selectedParts = getSelectedParts(getTargetViewer());
        List<ITopic> topics = MindMapUtils.getTopics(selectedParts);
        if (!topics.isEmpty()) {
            List<IPart> parts = MindMapUtils.getParts(topics,
                    getTargetViewer());
            request.setTargets(parts);
            request.setParameter(MindMapUI.PARAM_WITH_ANIMATION, Boolean.TRUE);
            return request;
        }
        ITool next = getTool(TOOL_CREATE_SUMMARY);
        if (next instanceof AbstractTool) {
            ((AbstractTool) next).setTargetViewer(viewer);
        }
        changeActiveTool(TOOL_CREATE_SUMMARY);
        return null;
    }

    protected void handleDrillDown(IViewer viewer) {
        ISheet sheet = (ISheet) viewer.getAdapter(ISheet.class);
        if (sheet == null)
            return;
        ISelection selection = viewer.getSelection();
        if (selection instanceof IStructuredSelection) {
            Object obj = ((IStructuredSelection) selection).getFirstElement();
            if (obj instanceof ITopic) {
                ITopic rootTopic = (ITopic) obj;
                drillDown(viewer, sheet, rootTopic);
            }
        }
    }

    private void handleDrillDown(IViewer viewer, List<IPart> sources) {
        if (sources.isEmpty())
            return;

        ISheet sheet = (ISheet) viewer.getAdapter(ISheet.class);
        if (sheet == null)
            return;

        IPart p = sources.get(0);
        Object m = MindMapUtils.getRealModel(p);
        if (m instanceof ITopic) {
            drillDown(viewer, sheet, (ITopic) m);
            if (viewer.getEditDomain() != null) {
                viewer.getEditDomain()
                        .handleRequest(MindMapUI.REQ_SELECT_CENTRAL, viewer);
            }
        }
    }

    private void drillDown(IViewer viewer, ISheet sheet, ITopic newRoot) {
        IMindMap newInput = new MindMap(sheet, newRoot);
        initTopicRightNumber(newRoot);
        viewer.setInput(newInput);
    }

    private void initTopicRightNumber(ITopic newRoot) {
        if (newRoot == null)
            return;

        String structureClass = newRoot.getStructureClass();
        if ("org.xmind.ui.map.unbalanced".equals(structureClass) //$NON-NLS-1$
                || structureClass == null) {
            ITopicExtension extension = newRoot
                    .createExtension("org.xmind.ui.map.unbalanced"); //$NON-NLS-1$
            ITopicExtensionElement element = extension.getContent()
                    .getCreatedChild("right-number"); //$NON-NLS-1$
            String rightNum = element.getTextContent();
            if (rightNum == null) {
                int size = newRoot.getChildren(ITopic.ATTACHED).size();
                if (size > 3)
                    size = size / 2;
                element.setTextContent(String.valueOf(size));
            }
        }
    }

    protected void handleDrillUp(IViewer viewer) {
        ISheet sheet = (ISheet) viewer.getAdapter(ISheet.class);
        if (sheet != null) {
            IDrillDownTraceService traceService = (IDrillDownTraceService) viewer
                    .getService(IDrillDownTraceService.class);
            if (traceService != null) {
                ITopic newRoot = traceService.getPreviousCentralTopic();
                drillDown(viewer, sheet, newRoot);
            }
        }
    }

    private void createLegend(Request request) {
        changeActiveTool(MindMapUI.TOOL_CREATE_LEGEND);
    }

    protected void handleEditLabelRequest(Request request, IViewer viewer) {
        request = fillTargets(request, viewer, false);
        List<IPart> targets = new ArrayList<IPart>(request.getTargets());
        for (int i = 0; i < targets.size(); i++) {
            IPart p = targets.get(i);
            if (p instanceof IIconTipPart) {
                targets.set(i, ((IIconTipPart) p).getTopicPart());
            } else if (p instanceof IMarkerPart) {
                targets.set(i, ((IMarkerPart) p).getTopicPart());
            } else if (p instanceof IImagePart) {
                targets.set(i, ((IImagePart) p).getTopicPart());
            } else if (p instanceof INumberingPart) {
                targets.set(i, ((INumberingPart) p).getTopicPart());
            } else if (p instanceof IInfoItemPart) {
                targets.set(i, ((IInfoItemPart) p).getTopicPart());
            }
        }
        request.setTargets(targets);
        handleEditRequest(request);
    }

    protected String getEditTool(IPart source, Request request) {
        String requestType = request == null ? null : request.getType();
        if (source instanceof ITopicPart) {
            if (MindMapUI.REQ_EDIT_LABEL.equals(requestType)) {
                return MindMapUI.TOOL_EDIT_LABEL;
            } else {
                return MindMapUI.TOOL_EDIT_TOPIC_TITLE;
            }
        } else if (source instanceof ILegendItemPart) {
            return MindMapUI.TOOL_EDIT_LEGEND_ITEM;
        }
        return super.getEditTool(source, request);
    }

    protected Request createEditRequestOnDoubleClick(IPart source,
            MouseEvent me) {
        if (source instanceof ILabelPart) {
            ILabelPart label = (ILabelPart) source;
            IBranchPart branch = label.getOwnedBranch();
            if (branch != null) {
                ITopicPart topic = branch.getTopicPart();
                if (topic != null) {
                    return new Request(MindMapUI.REQ_EDIT_LABEL)
                            .setPrimaryTarget(topic).setDomain(getDomain())
                            .setViewer(getTargetViewer());
                }
            }
        } else if (source instanceof ILegendItemPart) {
            ILegendItemPart item = (ILegendItemPart) source;
            return new Request(MindMapUI.REQ_EDIT_LEGEND_ITEM)
                    .setPrimaryTarget(item).setDomain(getDomain())
                    .setViewer(getTargetViewer());
        }
        return super.createEditRequestOnDoubleClick(source, me);
    }

    protected boolean handleDragStarted(DragDropEvent de) {
        ITool dndTool = getTool(GEF.TOOL_DND);
        if (dndTool != null) {
            changeActiveTool(GEF.TOOL_DND);
            if (getDomain().getActiveTool() == dndTool
                    && dndTool instanceof IDragDropHandler) {
                ((IDragDropHandler) dndTool).dragStarted(de, getTargetViewer());
            }
            return true;
        }
        return super.handleDragStarted(de);
    }

    protected void showMarkerMenu(IMarkerPart target) {
        if (getTargetViewer() == null
                || getTargetViewer().getEditDomain() == null
                || getTargetViewer().getEditDomain().getCommandStack() == null
                || "PresentationViewer" //$NON-NLS-1$
                        .equals(getTargetViewer().getClass().getSimpleName()))
            return;

        MenuManager menuManager = new MenuManager();
        fillMarkerGroup(menuManager, target.getMarkerRef(), getTargetViewer());
        final Menu menu = menuManager
                .createContextMenu(getTargetViewer().getControl());
        Point p = getTargetViewer().computeToDisplay(
                target.getFigure().getBounds().getBottomLeft(), true);
        menu.setLocation(p.x, p.y);
        menu.addMenuListener(new MenuListener() {
            public void menuShown(MenuEvent e) {
            }

            public void menuHidden(MenuEvent e) {
                e.display.asyncExec(new Runnable() {
                    public void run() {
                        menu.dispose();
                    }
                });
            }
        });
        menu.getDisplay().asyncExec(new Runnable() {
            public void run() {
                menu.setVisible(true);
            }
        });
    }

    /**
     * @param menuManager
     * @param markerRef
     * @param targetViewer
     */
    private static void fillMarkerGroup(MenuManager menuManager,
            final IMarkerRef markerRef, IGraphicalViewer targetViewer) {
        final Set<String> addedMarkerIds = new HashSet<String>();

        fillMarkerGroup(menuManager, markerRef, targetViewer,
                markerRef.getOwnedWorkbook().getMarkerSheet(), addedMarkerIds);

        fillMarkerGroup(menuManager, markerRef, targetViewer,
                MindMapUI.getResourceManager().getUserMarkerSheet(),
                addedMarkerIds);

        menuManager.add(new Separator());

        ViewerAction deleteAction = new ViewerAction(targetViewer) {
            @Override
            public void run() {
                executeCommand(new DeleteMarkerCommand(markerRef));
            }
        };
        deleteAction.setText(WorkbenchMessages.Workbench_delete);
        deleteAction.setToolTipText(WorkbenchMessages.Workbench_deleteToolTip);
        deleteAction.setImageDescriptor(
                MindMapUI.getImages().get(IMindMapImages.DELETE, true));
        deleteAction.setDisabledImageDescriptor(
                MindMapUI.getImages().get(IMindMapImages.DELETE, false));
        menuManager.add(deleteAction);
    }

    private static void fillMarkerGroup(MenuManager menuManager,
            IMarkerRef sourceMarkerRef, IGraphicalViewer targetViewer,
            IMarkerSheet markerSheet, Set<String> addedMarkerIds) {
        IMarker sourceMarker = markerSheet
                .findMarker(sourceMarkerRef.getMarkerId());
        if (sourceMarker == null)
            return;

        IMarkerGroup group = sourceMarker.getParent();
        if (group == null)
            return;

        List<IMarker> visibleMarkers = new ArrayList<IMarker>();
        for (IMarker marker : group.getMarkers())
            if (!marker.isHidden())
                visibleMarkers.add(marker);

        for (IMarker targetMarker : visibleMarkers) {
            if (addedMarkerIds.contains(targetMarker.getId()))
                continue;

            menuManager.add(new ReplaceMarkerAction(targetViewer,
                    sourceMarkerRef, targetMarker));
            addedMarkerIds.add(targetMarker.getId());
        }
    }

    protected Request createNavScrollRequest(int state, int key) {
        if (state != (SWT.MOD1 | SWT.MOD3))
            return null;
        Request request;
        if (key == SWT.ARROW_UP) {
            request = new Request(GEF.REQ_NAV_UP);
        } else if (key == SWT.ARROW_DOWN) {
            request = new Request(GEF.REQ_NAV_DOWN);
        } else if (key == SWT.ARROW_LEFT) {
            request = new Request(GEF.REQ_NAV_LEFT);
        } else if (key == SWT.ARROW_RIGHT) {
            request = new Request(GEF.REQ_NAV_RIGHT);
        } else {
            return null;
        }
        request.setViewer(getTargetViewer());
        request.setDomain(getDomain());
        return request;
    }

    @Override
    protected boolean handleNavRequest(Request request, boolean sequential) {
        IViewer viewer = request.getTargetViewer();
        ISelection oldSelection = viewer == null ? null : viewer.getSelection();
        boolean handled = super.handleNavRequest(request, sequential);
        ISelection newSelection = viewer == null ? null : viewer.getSelection();
        if (!sequential
                && (!handled || isSelectionEqual(oldSelection, newSelection))) {
            return handleNavScroll(request);
        }
        return handled;
    }

    @SuppressWarnings("unchecked")
    private boolean isSelectionEqual(ISelection oldSelection,
            ISelection newSelection) {
        if (oldSelection instanceof IStructuredSelection
                && newSelection instanceof IStructuredSelection) {
            Set<Object> oldElements = new HashSet<Object>(
                    ((IStructuredSelection) oldSelection).toList());
            Set<Object> newElements = new HashSet<Object>(
                    ((IStructuredSelection) newSelection).toList());
            return oldElements.equals(newElements);
        }
        return oldSelection == newSelection
                || (oldSelection != null && oldSelection.equals(newSelection));
    }

    private boolean handleNavScroll(Request request) {
        String type = request.getType();
        IViewer viewer = request.getTargetViewer();
        if (viewer == null)
            return false;

        Viewport viewport = (Viewport) viewer.getAdapter(Viewport.class);
        if (viewport == null)
            return false;

        IFigure contents = viewport.getContents();
        Rectangle contentsBounds = contents instanceof FreeformFigure
                ? ((FreeformFigure) contents).getFreeformExtent()
                : contents.getBounds();
        contentsBounds = contentsBounds.getExpanded(60, 60)
                .intersect(contents.getBounds());
        Rectangle clientArea = viewport.getClientArea();
        Point center = ((IGraphicalViewer) viewer).getCenterPoint().getCopy();
        int d = 0;
        if (GEF.REQ_NAV_LEFT.equals(type)) {
            d = Math.min(MindMapUI.NAV_SCROLL_STEP,
                    Math.abs(contentsBounds.x - clientArea.x));
            center.translate(-d, 0);
        } else if (GEF.REQ_NAV_UP.equals(type)) {
            d = Math.min(MindMapUI.NAV_SCROLL_STEP,
                    Math.abs(contentsBounds.y - clientArea.y));
            center.translate(0, -d);
        } else if (GEF.REQ_NAV_RIGHT.equals(type)) {
            d = Math.min(MindMapUI.NAV_SCROLL_STEP,
                    Math.abs(contentsBounds.right() - clientArea.right()));
            center.translate(-d, 0);
        } else if (GEF.REQ_NAV_DOWN.equals(type)) {
            d = Math.min(MindMapUI.NAV_SCROLL_STEP,
                    Math.abs(contentsBounds.bottom() - clientArea.bottom()));
            center.translate(0, -d);
        } else {
            return false;
        }
        if (d == 0)
            return false;
        ((IGraphicalViewer) viewer).center(center);
        return true;
    }

    @Override
    protected void handleEditRequest(Request request) {
        IPart source = request.getPrimaryTarget();
        if (source instanceof BoundaryPart) {
            BoundaryPart boundary = (BoundaryPart) source;
            if (boundary.isPolygonShape(boundary.getFigure())) {
                return;
            }
        }

        super.handleEditRequest(request);
    }

}
