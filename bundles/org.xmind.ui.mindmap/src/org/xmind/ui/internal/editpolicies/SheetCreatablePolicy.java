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
package org.xmind.ui.internal.editpolicies;

import org.eclipse.draw2d.geometry.Point;
import org.xmind.core.IRelationshipEnd;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.gef.GEF;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.part.IPart;
import org.xmind.ui.commands.AddTopicCommand;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.CreateTopicCommand;
import org.xmind.ui.commands.ModifyPositionCommand;
import org.xmind.ui.commands.ModifyTitleTextCommand;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class SheetCreatablePolicy extends MindMapPolicyBase {

    public boolean understands(String requestType) {
        return super.understands(requestType)
                || MindMapUI.REQ_CREATE_FLOAT.equals(requestType)
                || MindMapUI.REQ_CREATE_RELATIONSHIP.equals(requestType);
    }

    public void handle(Request request) {
        String reqType = request.getType();
        if (MindMapUI.REQ_CREATE_FLOAT.equals(reqType)) {
            createFloatingTopic(request);
        } else if (MindMapUI.REQ_CREATE_RELATIONSHIP.equals(reqType)) {
            createRelationship(request);
        }
    }

    private void createRelationship(Request request) {
        IPart sourceNode = (IPart) request
                .getParameter(MindMapUI.PARAM_SOURCE_NODE);
        if (sourceNode == null)
            return;

        Object m1 = MindMapUtils.getRealModel(sourceNode);
        if (!(m1 instanceof IRelationshipEnd))
            return;

        IRelationshipEnd end1 = (IRelationshipEnd) m1;

        IViewer viewer = request.getTargetViewer();
        if (viewer == null)
            return;

        ISheet parent = (ISheet) viewer.getAdapter(ISheet.class);
        if (parent == null)
            return;

        RelationshipCreateCommandBuilder builder = new RelationshipCreateCommandBuilder(
                viewer, request.getTargetCommandStack(), parent, end1);
        if (builder.canStart()) {
            builder.start();

            IPart targetNode = (IPart) request
                    .getParameter(MindMapUI.PARAM_TARGET_NODE);
            if (targetNode != null) {
                Object m2 = MindMapUtils.getRealModel(targetNode);
                if (m2 instanceof IRelationshipEnd)
                    builder.create((IRelationshipEnd) m2);
            } else {
                Point targetPosition = (Point) request
                        .getParameter(GEF.PARAM_POSITION);
                if (targetPosition != null) {
                    builder.create(targetPosition);
                }
            }

            builder.end();

            select(builder.getCommand().getSources(), viewer);
        }

//        IRelationshipEnd end2 = null;
//        IPart targetNode = (IPart) request
//                .getParameter(MindMapUI.PARAM_TARGET_NODE);
//        if (targetNode != null) {
//            Object m2 = MindMapUtils.getRealModel(targetNode);
//            if (m2 instanceof IRelationshipEnd)
//                end2 = (IRelationshipEnd) m2;
//        } else {
//
//        }
//
//        if (end2 != null) {
//            Command cmd = createCreateRelationship(parent, end1, end2);
//            cmd.setLabel(CommandMessages.Command_CreateRelationship);
//            if (cmd != null) {
//                saveAndRun(cmd, request.getTargetDomain());
//                if (cmd instanceof ISourceProvider) {
//                    select(((ISourceProvider) cmd).getSources(), viewer);
//                }
//            }
//        }
    }

//    private Command createCreateRelationship(ISheet parent,
//            IRelationshipEnd end1, IRelationshipEnd end2) {
//        IWorkbook workbook = parent.getOwnedWorkbook();
//        if (workbook == null)
//            return null;
//        CreateRelationshipCommand create = new CreateRelationshipCommand(
//                workbook);
//        ModifyRelationshipEndCommand setEnd1 = new ModifyRelationshipEndCommand(
//                create, end1.getId(), true);
//        ModifyRelationshipEndCommand setEnd2 = new ModifyRelationshipEndCommand(
//                create, end2.getId(), false);
//        AddRelationshipCommand add = new AddRelationshipCommand(create, parent);
//        return new CompoundCommand(create, setEnd1, setEnd2, add);
//    }

    private void createFloatingTopic(Request request) {
        IViewer viewer = request.getTargetViewer();
        if (viewer == null)
            return;

        ITopic rootTopic = (ITopic) viewer.getAdapter(ITopic.class);
        if (rootTopic == null)
            return;

        Point position = (Point) request.getParameter(GEF.PARAM_POSITION);
        if (position == null)
            return;

        PropertyCommandBuilder builder = new PropertyCommandBuilder(request);
        if (!builder.canStart())
            return;

        builder.start();
        builder.setLabel(CommandMessages.Command_CreateFloatingTopic);
        CreateTopicCommand create = new CreateTopicCommand(
                rootTopic.getOwnedWorkbook());
        builder.add(create, true);
        builder.add(
                new AddTopicCommand(create, rootTopic, -1, ITopic.DETACHED),
                false);
        builder.add(new ModifyTitleTextCommand(create,
                MindMapMessages.TitleText_FloatingTopic), false);
        builder.add(
                new ModifyPositionCommand(create, MindMapUtils
                        .toModelPosition(position)), false);
        builder.addSource(create.getSource(), true);
        builder.end();

        select(builder.getCommand().getSources(), viewer);
//        saveAndRun(cmd, request.getTargetDomain());
//
//        if (cmd instanceof ISourceProvider) {
//            List<Object> sources = ((ISourceProvider) cmd).getSources();
//            if (!sources.isEmpty()) {
//                select(sources, viewer);
//                if (isAnimationRequired(request))
//                    animateCommand(cmd, viewer);
//            }
//        }
    }

//    protected void doAnimateCommand(Command cmd, IAnimationService anim,
//            IViewer viewer) {
//        List<Object> creations = ((ISourceProvider) cmd).getSources();
//        final List<IMinimizable> minimizables = getMinimizables(creations,
//                viewer);
//        if (minimizables.isEmpty())
//            return;
//        for (IMinimizable min : minimizables) {
//            min.setMinimized(true);
//        }
//        ((GraphicalViewer) viewer).getLightweightSystem().getUpdateManager()
//                .performValidation();
//        super.doAnimateCommand(cmd, anim, viewer);
//    }
//
//    protected void createAnimation(Command cmd, IViewer viewer) {
//        List<Object> creations = ((ISourceProvider) cmd).getSources();
//        final List<IMinimizable> minimizables = getMinimizables(creations,
//                viewer);
//        for (IMinimizable min : minimizables) {
//            min.setMinimized(false);
//        }
//    }
//
//    private List<IMinimizable> getMinimizables(List<Object> creations,
//            IViewer viewer) {
//        List<IMinimizable> list = new ArrayList<IMinimizable>(creations.size());
//        for (Object o : creations) {
//            IPart part = viewer.findPart(o);
//            if (part instanceof IGraphicalPart) {
//                IFigure figure = ((IGraphicalPart) part).getFigure();
//                if (figure instanceof IMinimizable) {
//                    list.add((IMinimizable) figure);
//                }
//            }
//        }
//        return list;
//    }

}