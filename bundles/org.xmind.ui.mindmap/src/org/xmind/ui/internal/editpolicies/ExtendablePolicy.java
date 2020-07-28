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
package org.xmind.ui.internal.editpolicies;

import static org.xmind.gef.GEF.REQ_COLLAPSE;
import static org.xmind.gef.GEF.REQ_COLLAPSE_ALL;
import static org.xmind.gef.GEF.REQ_EXTEND;
import static org.xmind.gef.GEF.REQ_EXTEND_ALL;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.xmind.core.Core;
import org.xmind.core.ITopic;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.command.Command;
import org.xmind.gef.part.IPart;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.ModifyFoldedCommand;
import org.xmind.ui.decorations.IBranchConnections;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.util.MindMapUtils;

public class ExtendablePolicy extends MindMapPolicyBase {

    public boolean understands(String requestType) {
        return super.understands(requestType) || REQ_EXTEND.equals(requestType)
                || REQ_EXTEND_ALL.equals(requestType)
                || REQ_COLLAPSE.equals(requestType)
                || REQ_COLLAPSE_ALL.equals(requestType);
    }

    public void handle(Request request) {
        String type = request.getType();
        if (REQ_EXTEND.equals(type) || REQ_EXTEND_ALL.equals(type)
                || REQ_COLLAPSE.equals(type) || REQ_COLLAPSE_ALL.equals(type)) {
            performExtendOrCollapse(request);
        }
    }

//    public void performMultiPartRequest(SourceRequest request) {
//        String type = request.getType();
//        if (REQ_EXTEND.equals(type) || REQ_EXTEND_ALL.equals(type)
//                || REQ_COLLAPSE.equals(type) || REQ_COLLAPSE_ALL.equals(type)) {
//            performExtendOrCollapse(request);
//        } else {
//            super.performMultiPartRequest(request);
//        }
//    }

    private void performExtendOrCollapse(Request req) {
        String type = req.getType();
        Command cmd = null;
        if (REQ_EXTEND.equals(type)) {
            cmd = createModifyFoldedCommand(req, false, false);
        } else if (REQ_COLLAPSE.equals(type)) {
            cmd = createModifyFoldedCommand(req, true, false);
        } else if (REQ_EXTEND_ALL.equals(type)) {
            cmd = createModifyFoldedCommand(req, false, true);
        } else if (REQ_COLLAPSE_ALL.equals(type)) {
            cmd = createModifyFoldedCommand(req, true, true);
        }
        if (cmd != null) {
            boolean animated = isAnimationRequired(req)
                    && animateCommand(cmd, req.getTargetViewer());
            if (!animated) {
                saveAndRun(cmd, req.getTargetDomain());
            }
        }
    }

    private Command createModifyFoldedCommand(Request req, boolean newFolded,
            boolean deeply) {
        List<IPart> parts = getAllFoldableParts(req.getTargetViewer(), req
                .getTargets(), deeply);
        List<ITopic> topics = MindMapUtils.getTopics(parts);
        ModifyFoldedCommand command = new ModifyFoldedCommand(topics, newFolded);
        command.setLabel(getFoldCommandLabel(newFolded, deeply));
        return command;
    }

    private String getFoldCommandLabel(boolean newFolded, boolean deeply) {
        if (newFolded) {
            return deeply ? CommandMessages.Command_CollapseAll
                    : CommandMessages.Command_Collapse;
        }
        return deeply ? CommandMessages.Command_ExtendAll
                : CommandMessages.Command_Extend;
    }

    protected List<IPart> getAllFoldableParts(IViewer viewer,
            List<? extends IPart> parts, boolean deeply) {
        List<IPart> results = new ArrayList<IPart>(parts.size()
                * (deeply ? 2 : 1));
        for (IPart p : parts) {
            IBranchPart branch = MindMapUtils.findBranch(p);
            if (branch != null) {
                if (branch.isPropertyModifiable(Core.TopicFolded)) {
                    results.add(branch);
                }
                if (deeply) {
                    results.addAll(getAllFoldableParts(viewer, branch
                            .getSubBranches(), deeply));
                    results.addAll(getAllFoldableParts(viewer, branch
                            .getSummaryBranches(), deeply));
                }
            }
        }
        return results;
    }

    protected void createAnimation(Command cmd, IViewer viewer) {
        super.createAnimation(cmd, viewer);
        if (cmd instanceof ISourceProvider) {
            List<Object> sources = ((ISourceProvider) cmd).getSources();
            List<IPart> parts = MindMapUtils.getParts(sources, viewer);
            for (IPart p : parts) {
                IBranchPart branch = MindMapUtils.findBranch(p);
                if (branch != null) {
                    showSubBranches(branch);
                }
            }
        }
    }

    private void showSubBranches(IBranchPart branch) {
        IBranchConnections connections = branch.getConnections();
        if (connections != null) {
            connections.setVisible(branch.getFigure(), true);
        }
        for (IBranchPart subBranch : branch.getSubBranches()) {
            showSubBranch(subBranch);
        }
        for (IBranchPart summaryBranch : branch.getSummaryBranches()) {
            showSubBranch(summaryBranch);
        }
    }

    private void showSubBranch(IBranchPart subBranch) {
        IFigure figure = subBranch.getFigure();
        figure.setVisible(true);
        if (!subBranch.isFolded()) {
            showSubBranches(subBranch);
        }
    }

    protected Runnable createAfterEffect(final Command cmd, final IViewer viewer) {
        return new Runnable() {
            public void run() {
                List<Object> sources = ((ISourceProvider) cmd).getSources();
                List<IPart> parts = MindMapUtils.getParts(sources, viewer);
                for (IPart p : parts) {
                    IBranchPart branch = MindMapUtils.findBranch(p);
                    if (branch != null) {
                        branch.treeUpdate(true);
                    }
                }
            }
        };
    }
}