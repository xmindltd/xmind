package org.xmind.ui.internal.comments;

import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.util.TopicIterator;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.ui.internal.MindMapMessages;
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

        ITopic nextTopic = findNextTopicWithComments(topic);
        if (nextTopic == null || nextTopic == topic)
            return;

        ITopicPart topicPart = MindMapUtils.findTopicPart(
                getTargetEditor().getAdapter(IGraphicalViewer.class),
                nextTopic);
        if (topicPart == null)
            return;

        dialog.close();

        CommentsUtils.reveal(getTargetEditor(), nextTopic);
        CommentsPopup popup = new CommentsPopup(
                getTargetEditor().getSite().getWorkbenchWindow(), topicPart,
                true);
        popup.open();
    }

    private ITopic findNextTopicWithComments(ITopic sourceTopic) {
        ISheet sheet = sourceTopic.getOwnedSheet();
        if (sheet == null)
            return null;

        TopicIterator it = new TopicIterator(sheet.getRootTopic());
        boolean sourceFound = false;
        while (it.hasNext()) {
            ITopic nextTopic = it.next();
            if (!sourceFound) {
                if (nextTopic == sourceTopic) {
                    sourceFound = true;
                }
            } else {
                if (nextTopic.getOwnedWorkbook().getCommentManager()
                        .hasComments(nextTopic.getId()))
                    return nextTopic;
            }
        }

        if (sourceFound) {
            it = new TopicIterator(sheet.getRootTopic());
            while (it.hasNext()) {
                ITopic nextTopic = it.next();
                if (nextTopic == sourceTopic)
                    break;
                if (nextTopic.getOwnedWorkbook().getCommentManager()
                        .hasComments(nextTopic.getId()))
                    return nextTopic;
            }
        }

        return null;
    }

}