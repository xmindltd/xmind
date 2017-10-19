package org.xmind.ui.internal.actions;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchWindow;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.part.IPart;
import org.xmind.gef.ui.actions.ISelectionAction;
import org.xmind.gef.ui.actions.PageAction;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.comments.CommentsPopup;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class EditCommentsAction extends PageAction implements ISelectionAction {

    public EditCommentsAction(IGraphicalEditorPage page) {
        super("org.xmind.ui.editComments", page); //$NON-NLS-1$
        setText(MindMapMessages.EditComments_text);
        setImageDescriptor(
                MindMapUI.getImages().get("menu_modify_comment.png", true)); //$NON-NLS-1$
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

        CommentsPopup popup = new CommentsPopup(window, topicPart, true);
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

    public void setSelection(ISelection selection) {
        setEnabled(MindMapUtils.isSingleTopic(selection));
    }

}
