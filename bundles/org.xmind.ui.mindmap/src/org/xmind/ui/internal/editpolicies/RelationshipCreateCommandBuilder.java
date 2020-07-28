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

import org.eclipse.draw2d.geometry.Point;
import org.xmind.core.IControlPoint;
import org.xmind.core.IRelationship;
import org.xmind.core.IRelationshipEnd;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.gef.IViewer;
import org.xmind.gef.command.ICommandStack;
import org.xmind.ui.commands.AddRelationshipCommand;
import org.xmind.ui.commands.AddTopicCommand;
import org.xmind.ui.commands.CommandBuilder;
import org.xmind.ui.commands.CreateRelationshipCommand;
import org.xmind.ui.commands.CreateTopicCommand;
import org.xmind.ui.commands.ModifyPositionCommand;
import org.xmind.ui.commands.ModifyRelationshipEndCommand;
import org.xmind.ui.commands.ModifyTitleTextCommand;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.util.MindMapUtils;

public class RelationshipCreateCommandBuilder extends CommandBuilder {

    private ISheet parent;

    private IRelationshipEnd end1;

    public RelationshipCreateCommandBuilder(IViewer viewer,
            CommandBuilder delegate, ISheet parent, IRelationshipEnd end1) {
        super(viewer, delegate);
        this.parent = parent;
        this.end1 = end1;
    }

    public RelationshipCreateCommandBuilder(IViewer viewer,
            ICommandStack commandStack, ISheet parent, IRelationshipEnd end1) {
        super(viewer, commandStack);
        this.parent = parent;
        this.end1 = end1;
    }

    public void create(IRelationshipEnd end2) {
        createRelationship(end2, true);
    }

    public void create(Point targetPosition) {
        CreateTopicCommand createTopic = new CreateTopicCommand(
                parent.getOwnedWorkbook());
        add(createTopic, true);
        ITopic end2 = (ITopic) createTopic.getSource();
        add(new ModifyTitleTextCommand(end2,
                MindMapMessages.TitleText_FloatingTopic), false);
        add(new ModifyPositionCommand(end2,
                MindMapUtils.toModelPosition(targetPosition)), false);
        ITopic rootTopic = (ITopic) getViewer().getAdapter(ITopic.class);
        if (rootTopic == null)
            rootTopic = parent.getRootTopic();
        add(new AddTopicCommand(end2, rootTopic, -1, ITopic.DETACHED), false);
        createRelationship(end2, false);
    }

    private void createRelationship(IRelationshipEnd end2,
            boolean relationshipCollectalbe) {
        IWorkbook workbook = parent.getOwnedWorkbook();
        CreateRelationshipCommand create = new CreateRelationshipCommand(
                workbook);
        add(create, relationshipCollectalbe);
        IRelationship rel = (IRelationship) create.getSource();

        IControlPoint cp1 = rel.getControlPoint(0);
        IControlPoint cp2 = rel.getControlPoint(1);

        cp1.setPolarAngle(Math.PI / 12);
        cp1.setPolarAmount(0.3);

        cp2.setPolarAngle(Math.PI / 12);
        cp2.setPolarAmount(0.3);

        add(new ModifyRelationshipEndCommand(rel, end1.getId(), true), false);
        add(new ModifyRelationshipEndCommand(rel, end2.getId(), false), false);
        add(new AddRelationshipCommand(rel, parent), false);

    }

}