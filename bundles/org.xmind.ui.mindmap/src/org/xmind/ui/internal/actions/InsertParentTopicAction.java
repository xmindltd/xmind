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
package org.xmind.ui.internal.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.xmind.core.ITopic;
import org.xmind.gef.ui.actions.ISelectionAction;
import org.xmind.gef.ui.actions.RequestAction;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.actions.MindMapActionFactory;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class InsertParentTopicAction extends RequestAction
        implements ISelectionAction {

    public InsertParentTopicAction(IGraphicalEditorPage page) {
        super(MindMapActionFactory.INSERT_PARENT_TOPIC.getId(), page,
                MindMapUI.REQ_CREATE_PARENT);
        setText(MindMapMessages.InsertParentTopic_text);
        setToolTipText(MindMapMessages.InsertParentTopic_toolTip);
        setActionDefinitionId("org.xmind.ui.command.insert.parentTopic"); //$NON-NLS-1$
        setImageDescriptor(
                MindMapUI.getImages().get(IMindMapImages.INSERT_PARENT, true));
        setDisabledImageDescriptor(
                MindMapUI.getImages().get(IMindMapImages.INSERT_PARENT, false));
    }

    public void setSelection(ISelection selection) {
        setEnabled(MindMapUtils.isSingleTopic(selection)
                && !MindMapUtils.hasCentralTopic(selection, getViewer())
                && !MindMapUtils.hasSummary(selection, getViewer()));
        if (MindMapUtils.isSingleTopic(selection)) {
            setEnabled(!MindMapUtils.hasCentralTopic(selection, getViewer())
                    && !MindMapUtils.hasSummary(selection, getViewer()));
        } else if (MindMapUtils.isAllSuchElements(selection,
                MindMapUI.CATEGORY_TOPIC)) {
            List<ITopic> topics = getAllTopics(selection);
            if (topics == null || topics.size() == 0
                    || containsCentralTopic(topics)
                    || containsSummaryTopic(topics)) {
                setEnabled(false);
            } else {
                setEnabled(isAllBrothers(
                        MindMapUtils.filterOutDescendents(topics, null)));
            }
        }
    }

    private List<ITopic> getAllTopics(ISelection selection) {
        List<Object> topics = MindMapUtils.getAllSuchElements(selection,
                MindMapUI.CATEGORY_TOPIC);
        if (topics == null) {
            return null;
        } else {
            List<ITopic> list = new ArrayList<ITopic>();
            Collections.addAll(list, topics.toArray(new ITopic[0]));
            return list;
        }
    }

    private boolean isAllBrothers(List<ITopic> topics) {
        if (topics == null || topics.size() == 0) {
            return false;
        }
        if (topics.size() == 1) {
            return true;
        }
        for (int i = 0; i < topics.size() - 1; i++) {
            if (!isBrothers(topics.get(i), topics.get(i + 1))) {
                return false;
            }
        }

        return true;
    }

    private boolean isBrothers(ITopic t1, ITopic t2) {
        if (t1 == null || t2 == null) {
            return false;
        }

        return t1.getParent() == t2.getParent();
    }

    public boolean containsCentralTopic(List<ITopic> topics) {
        if (topics == null || topics.size() == 0) {
            return false;
        }

        for (ITopic t : topics) {
            if (isCentralTopic(t)) {
                return true;
            }
        }

        return false;
    }

    public boolean isCentralTopic(ITopic topic) {
        if (topic == null) {
            return false;
        }

        return topic.getOwnedSheet().getRootTopic() == topic;
    }

    private boolean containsSummaryTopic(List<ITopic> topics) {
        if (topics == null || topics.isEmpty())
            return false;

        for (ITopic t : topics) {
            if (isSummary(t))
                return true;
        }

        return false;
    }

    private boolean isSummary(ITopic topic) {
        if (topic == null)
            return false;

        return ITopic.SUMMARY.equals(topic.getType());
    }

}
