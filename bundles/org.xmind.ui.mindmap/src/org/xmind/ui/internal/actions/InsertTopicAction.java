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
package org.xmind.ui.internal.actions;

import org.eclipse.jface.viewers.ISelection;
import org.xmind.gef.GEF;
import org.xmind.gef.ui.actions.ISelectionAction;
import org.xmind.gef.ui.actions.RequestAction;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.actions.MindMapActionFactory;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class InsertTopicAction extends RequestAction implements
        ISelectionAction {

    public InsertTopicAction(IGraphicalEditorPage page) {
        super(MindMapActionFactory.INSERT_TOPIC.getId(), page, GEF.REQ_CREATE);
        setText(MindMapMessages.InsertTopic_text);
        setToolTipText(MindMapMessages.InsertTopic_toolTip);
        setImageDescriptor(MindMapUI.getImages().get(
                IMindMapImages.INSERT_AFTER, true));
        setDisabledImageDescriptor(MindMapUI.getImages().get(
                IMindMapImages.INSERT_AFTER, false));
    }

    public void setSelection(ISelection selection) {
        setEnabled(isCreatable(selection));
    }

    private boolean isCreatable(ISelection selection) {
        return MindMapUtils.isSingleTopic(selection)
                || MindMapUtils.matchesSelection(selection,
                        MindMapUI.CATEGORY_RELATIONSHIP, true);
//            return true;
//        if (MindMapUtils.matchesSelection(selection,
//                MindMapUI.CATEGORY_SUMMARY, true)) {
//            ISummary s = (ISummary) ((IStructuredSelection) selection)
//                    .getFirstElement();
//            return s.getTopicId() == null;
//        }
//        return false;
    }

}