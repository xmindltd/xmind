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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchWindow;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.part.IPart;
import org.xmind.gef.ui.actions.ISelectionAction;
import org.xmind.gef.ui.actions.PageAction;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.actions.MindMapActionFactory;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.notes.NotesPopup;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.util.MindMapUtils;

public class EditNotesAction extends PageAction implements ISelectionAction {

    public EditNotesAction(IGraphicalEditorPage page) {
        super(MindMapActionFactory.EDIT_NOTES.getId(), page);
    }

    public void setSelection(ISelection selection) {
        setEnabled(MindMapUtils.isSingleTopic(selection));
    }

    public void run() {
        IGraphicalEditor editor = getEditor();
        if (editor == null)
            return;

        IWorkbenchWindow window = editor.getSite().getWorkbenchWindow();
        if (window == null)
            return;

        IGraphicalViewer viewer = getViewer();
        if (viewer == null)
            return;

        Control control = viewer.getControl();
        if (control == null || control.isDisposed())
            return;

        ITopicPart topicPart = getSelectionTopicPart(viewer);
        if (topicPart == null)
            return;

        MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                UserDataConstants.CATEGORY_NOTES,
                UserDataConstants.USE_NOTES);
        NotesPopup popup = new NotesPopup(window, topicPart, true, true);
        popup.open();
    }

    private ITopicPart getSelectionTopicPart(IGraphicalViewer viewer) {
        ISelection selection = viewer.getSelection();
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) selection;
            Object o = ss.getFirstElement();
            IPart part = viewer.findPart(o);
            if (part instanceof ITopicPart)
                return (ITopicPart) part;
        }
        return null;
    }

}
