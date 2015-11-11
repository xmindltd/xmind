package org.xmind.ui.internal.actions;

import java.util.List;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.ITopic;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.comments.CommentAction;
import org.xmind.ui.internal.comments.CommentsPopup;
import org.xmind.ui.internal.comments.CommentsUtils;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class ShowNextTopicCommentsAction extends CommentAction {

    private ITopic topic;

    private CommentsPopup dialog;

    public ShowNextTopicCommentsAction(IGraphicalEditor editor,
            CommentsPopup dialog) {
        super(editor);
        this.topic = dialog.getTopic();
        this.dialog = dialog;

        setId("org.xmind.ui.action.showNextTopicComments2"); //$NON-NLS-1$
        setText(MindMapMessages.ShowNextTopicComments_text);
        setImageDescriptor(MindMapUI.getImages().get("next-topic.png", true)); //$NON-NLS-1$
        setToolTipText(MindMapMessages.ShowNextTopicComments_tooltip);
    }

    public void run() {
        if (topic == null) {
            return;
        }
        List<ITopic> topics = CommentsUtils
                .getAllTopicsWithComments(topic.getOwnedSheet());
        if (topics.size() == 0
                || (topics.size() == 1 && topics.contains(topic))) {
            return;
        }

        dialog.close();

        ITopic nextTopic = revealNextTopicWithComments(topics, topic);
        IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        ITopicPart topicPart = MindMapUtils.getTopicPart(nextTopic);
        if (topicPart != null) {
            CommentsPopup popup = new CommentsPopup(window, topicPart, true);
            popup.open();
        }
    }

    private ITopic revealNextTopicWithComments(List<ITopic> topics,
            ITopic topic) {
        CommentsUtils.insertTopic(topics, topic);
        int oldIndex = topics.indexOf(topic);
        int newIndex = (oldIndex == topics.size() - 1 ? 0 : oldIndex + 1);
        ITopic nextTopic = topics.get(newIndex);
        reveal(getTargetEditor(), nextTopic);

        return nextTopic;
    }

    private void reveal(IGraphicalEditor sourceEditor, ITopic topic) {
        if (sourceEditor == null) {
            return;
        }
        sourceEditor.getSite().getPage().activate(sourceEditor);

        if (topic != null) {
            ISelectionProvider selectionProvider = sourceEditor.getSite()
                    .getSelectionProvider();
            if (selectionProvider != null) {
                selectionProvider.setSelection(new StructuredSelection(topic));
            }
        }
    }

}