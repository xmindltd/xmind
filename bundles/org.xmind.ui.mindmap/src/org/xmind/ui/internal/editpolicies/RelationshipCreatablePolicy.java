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

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.xmind.core.IRelationship;
import org.xmind.core.ITopic;
import org.xmind.gef.GEF;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.draw2d.IDecoratedFigure;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.part.IPart;
import org.xmind.ui.commands.AddRelationshipCommand;
import org.xmind.ui.commands.AddTopicCommand;
import org.xmind.ui.commands.CreateRelationshipCommand;
import org.xmind.ui.commands.CreateTopicCommand;
import org.xmind.ui.commands.ModifyPositionCommand;
import org.xmind.ui.commands.ModifyRelationshipEndCommand;
import org.xmind.ui.commands.ModifyStyleCommand;
import org.xmind.ui.commands.ModifyTitleTextCommand;
import org.xmind.ui.decorations.IRelationshipDecoration;
import org.xmind.ui.mindmap.IRelationshipPart;
import org.xmind.ui.util.MindMapUtils;

public class RelationshipCreatablePolicy extends MindMapPolicyBase {

    public boolean understands(String requestType) {
        return super.understands(requestType)
                || GEF.REQ_CREATE.equals(requestType);
    }

    public void handle(Request request) {
        String type = request.getType();
        if (GEF.REQ_CREATE.equals(type)) {
            performCreateTopic(request);
        }
    }

    private void performCreateTopic(Request request) {
        IPart source = request.getPrimaryTarget();
        if (!(source instanceof IRelationshipPart))
            return;

        IViewer viewer = request.getTargetViewer();
        if (viewer == null)
            return;

        IRelationshipPart rel = (IRelationshipPart) source;
        IRelationship r = rel.getRelationship();
        IFigure figure = rel.getFigure();
        Point newPosition = null;
        if (figure instanceof IDecoratedFigure) {
            IDecoration decoration = ((IDecoratedFigure) figure)
                    .getDecoration();
            if (decoration instanceof IRelationshipDecoration) {
                PrecisionPoint titlePosition = ((IRelationshipDecoration) decoration)
                        .getTitlePosition(figure);
                newPosition = titlePosition.toDraw2DPoint();
            }
        }
        if (newPosition == null) {
            newPosition = figure.getBounds().getCenter();
        }

        ITopic rootTopic = (ITopic) viewer.getAdapter(ITopic.class);
        CreateTopicCommand createTopic = new CreateTopicCommand(rootTopic
                .getOwnedWorkbook());
        String newTitle = r.getTitleText();
        ModifyTitleTextCommand modifyTitle = new ModifyTitleTextCommand(
                createTopic, newTitle);
        ModifyTitleTextCommand clearRelTitle = new ModifyTitleTextCommand(r,
                null);
        ModifyPositionCommand modifyPosition = new ModifyPositionCommand(
                createTopic, MindMapUtils.toModelPosition(newPosition));
        ModifyRelationshipEndCommand modifyRelEnd = new ModifyRelationshipEndCommand(
                r, createTopic, false);
//        ResetRelationshipControlPointCommand resetCP1 = new ResetRelationshipControlPointCommand(
//                r, 0);
//        ResetRelationshipControlPointCommand resetCP2 = new ResetRelationshipControlPointCommand(
//                r, 1);
        ModifyPositionCommand resetCP1 = new ModifyPositionCommand(r
                .getControlPoint(0), null);
        ModifyPositionCommand resetCP2 = new ModifyPositionCommand(r
                .getControlPoint(1), null);
        CreateRelationshipCommand createRel = new CreateRelationshipCommand(
                rootTopic.getOwnedWorkbook());
        ModifyRelationshipEndCommand modifyRel2End1 = new ModifyRelationshipEndCommand(
                createRel, createTopic, true);
        ModifyRelationshipEndCommand modifyRel2End2 = new ModifyRelationshipEndCommand(
                createRel, r.getEnd2Id(), false);
        ModifyStyleCommand modifyRelStyle = new ModifyStyleCommand(createRel, r
                .getStyleId());
        AddTopicCommand addTopic = new AddTopicCommand(createTopic, rootTopic,
                -1, ITopic.DETACHED);
        AddRelationshipCommand addRel = new AddRelationshipCommand(createRel, r
                .getOwnedSheet());
        clearRelTitle.setSourceCollectable(false);
        modifyRelEnd.setSourceCollectable(false);
        resetCP1.setSourceCollectable(false);
        resetCP2.setSourceCollectable(false);
        createRel.setSourceCollectable(false);
        modifyRel2End1.setSourceCollectable(false);
        modifyRel2End2.setSourceCollectable(false);
        modifyRelStyle.setSourceCollectable(false);
        addRel.setSourceCollectable(false);
        CompoundCommand cmd = new CompoundCommand(createTopic, modifyTitle,
                clearRelTitle, modifyPosition, modifyRelEnd, resetCP1,
                resetCP2, createRel, modifyRel2End1, modifyRel2End2,
                modifyRelStyle, addTopic, addRel);
        saveAndRun(cmd, request.getTargetDomain());
        if (cmd instanceof ISourceProvider) {
            select(((ISourceProvider) cmd).getSources(), viewer);
        }
    }

}