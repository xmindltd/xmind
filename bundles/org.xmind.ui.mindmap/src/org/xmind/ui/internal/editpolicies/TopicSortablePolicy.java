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

import java.util.List;

import org.eclipse.jface.viewers.StructuredSelection;
import org.xmind.core.ITopic;
import org.xmind.gef.GEF;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.command.ICommandStack;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.internal.actions.ActionConstants;
import org.xmind.ui.util.MindMapUtils;

/**
 * @author Karelun huang
 */
public class TopicSortablePolicy extends MindMapPolicyBase {

    @Override
    public boolean understands(String requestType) {
        return super.understands(requestType)
                || GEF.REQ_SORT.equals(requestType);
    }

    public void handle(Request request) {
        String requestType = request.getType();
        if (GEF.REQ_SORT.equals(requestType)) {
            sortTopics(request);
        }
    }

    private void sortTopics(Request request) {
        List<ITopic> topics = MindMapUtils.getTopics(request.getTargets());
        if (topics.isEmpty())
            return;

        ITopic parent = topics.get(0);
        IViewer viewer = request.getTargetViewer();
        ICommandStack commandStack = request.getTargetCommandStack();
        SortTopicCommandBuilder builder = new SortTopicCommandBuilder(viewer,
                commandStack);
        if (!builder.canStart())
            return;

        String sortType = (String) request.getParameter(GEF.PARAM_COMPARAND);
        String label = getSortCommandLabel(sortType);
        builder.setLabel(label);
        builder.setSortType(sortType);
        builder.sort(parent);
        builder.end();

        CompoundCommand command = builder.getCommand();
        if (command != null) {
            List<Object> sources = command.getSources();
            viewer.setSelection(new StructuredSelection(sources));
        }
    }

    private String getSortCommandLabel(String sortType) {
        if (ActionConstants.SORT_TITLE_ID.equals(sortType))
            return CommandMessages.Command_SortByTitle;
        else if (ActionConstants.SORT_PRIORITY_ID.equals(sortType))
            return CommandMessages.Command_SortByPriority;
        else if (ActionConstants.SORT_MODIFIED_ID.equals(sortType))
            return CommandMessages.Command_SortByModifiedTime;
        return CommandMessages.Command_Sort;
    }
}
