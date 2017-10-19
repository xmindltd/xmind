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

public class ShowPreTopicCommentsAction extends CommentAction {

    private ITopic topic;

    private CommentsPopup dialog;

    public ShowPreTopicCommentsAction(IGraphicalEditor editor,
            CommentsPopup dialog) {
        super(editor);
        this.topic = dialog.getTopic();
        this.dialog = dialog;

        setId("org.xmind.ui.action.showPreTopicComments2"); //$NON-NLS-1$
        setText(MindMapMessages.ShowPreTopicComments_text);
        setImageDescriptor(
                MindMapUI.getImages().get("previous-topic.png", true)); //$NON-NLS-1$
        setToolTipText(MindMapMessages.ShowPreTopicComments_tooltip);
    }

    public void run() {
        if (topic == null) {
            return;
        }

        ITopic previousTopic = findPreviousTopicWithComments(topic);
        if (previousTopic == null || previousTopic == topic)
            return;

        ITopicPart topicPart = MindMapUtils.findTopicPart(
                getTargetEditor().getAdapter(IGraphicalViewer.class),
                previousTopic);
        if (topicPart == null)
            return;

        dialog.close();

        CommentsUtils.reveal(getTargetEditor(), previousTopic);
        CommentsPopup popup = new CommentsPopup(
                getTargetEditor().getSite().getWorkbenchWindow(), topicPart,
                true);
        popup.open();
    }

    private ITopic findPreviousTopicWithComments(ITopic sourceTopic) {
        ISheet sheet = sourceTopic.getOwnedSheet();
        if (sheet == null)
            return null;

        TopicIterator it = new TopicIterator(sheet.getRootTopic(),
                TopicIterator.REVERSED);
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
            it = new TopicIterator(sheet.getRootTopic(),
                    TopicIterator.REVERSED);
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