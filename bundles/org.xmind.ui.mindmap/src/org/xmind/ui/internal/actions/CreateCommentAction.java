package org.xmind.ui.internal.actions;

import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.comments.CommentAction;
import org.xmind.ui.internal.comments.ICommentTextViewerContainer;
import org.xmind.ui.mindmap.MindMapUI;

/**
 * @deprecated
 * @author Frank Shaka
 *
 */
@Deprecated
public class CreateCommentAction extends CommentAction {

    private Object target;

    private ICommentTextViewerContainer container;

    public CreateCommentAction(IGraphicalEditor editor, Object target,
            ICommentTextViewerContainer container) {
        super(editor);
        this.target = target;
        this.container = container;

        setId("org.xmind.ui.action.addComment"); //$NON-NLS-1$
        setText(MindMapMessages.AddComment_text);
        setImageDescriptor(MindMapUI.getImages().get("new-comment.png", true)); //$NON-NLS-1$
        setToolTipText(MindMapMessages.AddComment_tooltip);
    }

    public void run() {
//        setEnabled(false);
//        control = container.getContentComposite();
//        super.run();
//        final CreateCommentCommand cmd = new CreateCommentCommand(target);
//
//        Display.getCurrent().timerExec(50, new Runnable() {
//            public void run() {
//
//                Display.getCurrent().asyncExec(new Runnable() {
//                    public void run() {
//                        cmd.execute();
//                        setEnabled(true);
//                    }
//                });
//            }
//        });
    }

    @Override
    public void selectionChanged(Object selection) {
        if (selection instanceof ITopic || selection instanceof ISheet) {
            this.target = selection;
            setEnabled(true);
        } else {
            setEnabled(false);
        }
    }

}
