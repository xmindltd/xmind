package org.xmind.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.part.IPart;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.internal.comments.CommentsPopup;
import org.xmind.ui.mindmap.ITopicPart;

public class EditCommentsHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {

        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
        if (window == null)
            return null;

        IGraphicalViewer viewer = getViewer(event);
        if (viewer == null)
            return null;

        Control control = viewer.getControl();
        if (control == null || control.isDisposed())
            return null;

        ITopicPart topicPart = getSelectionTopicPart(viewer);
        if (topicPart == null)
            return null;

        final CommentsPopup popup = new CommentsPopup(window, topicPart, true);
        Display.getCurrent().timerExec(50, new Runnable() {
            public void run() {

                Display.getCurrent().asyncExec(new Runnable() {
                    public void run() {
                        popup.open();
                    }
                });
            }
        });

        return null;
    }

    private IGraphicalViewer getViewer(ExecutionEvent event) {
        IEditorPart editor = HandlerUtil.getActiveEditor(event);
        if (editor == null || !(editor instanceof IGraphicalEditor)) {
            return null;
        }

        IGraphicalEditorPage editorPage = ((IGraphicalEditor) editor)
                .getActivePageInstance();
        if (editorPage == null) {
            return null;
        }

        return editorPage.getViewer();
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
