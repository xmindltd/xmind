package org.xmind.ui.internal.comments;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.comment.IComment;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.ui.util.MindMapUtils;

public class CommentsUtils {

    private CommentsUtils() {
    }

    public static List<ITopic> getAllTopicsWithComments(ISheet sheet) {
        List<ITopic> topicsWithComments = new ArrayList<ITopic>();
        List<ITopic> allTopics = MindMapUtils.getAllTopics(sheet, true, true);
        for (ITopic topic : allTopics) {
            if (CommentsUtils.hasComments(topic)) {
                topicsWithComments.add(topic);
            }
        }
        return topicsWithComments;
    }

    public static boolean hasComments(ITopic topic) {
        if (topic == null) {
            return false;
        }
        List<IComment> comments = topic.getOwnedWorkbook().getCommentManager()
                .getComments(topic);
        if (comments.size() != 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * if topics contain topic, return; else insert topic into List<topics> at
     * the proper position.
     * 
     * @param topics
     * @param topic
     */
    public static void insertTopic(List<ITopic> topics, ITopic topic) {
        if (topics == null || topics.size() == 0 || topics.contains(topic)) {
            return;
        }

        List<ITopic> allTopics = MindMapUtils
                .getAllTopics(topic.getOwnedSheet(), true, true);
        int index = allTopics.indexOf(topic);
        for (int i = index + 1; i < allTopics.size(); i++) {
            ITopic t = allTopics.get(i);
            if (topics.contains(t)) {
                topics.add(topics.indexOf(t), topic);
                return;
            }
        }
        topics.add(topic);
    }

    public static void addRecursiveMouseListener(Control c, MouseListener ml,
            Control excludeControl) {
        if (c == null || c.isDisposed() || ml == null || c == excludeControl) {
            return;
        }
        c.addMouseListener(ml);
        if (c instanceof Composite) {
            for (final Control cc : ((Composite) c).getChildren()) {
                addRecursiveMouseListener(cc, ml, excludeControl);
            }
        }
    }

    public static void removeRecursiveMouseListener(Control c, MouseListener ml,
            Control excludeControl) {
        if (c == null || c.isDisposed() || ml == null || c == excludeControl) {
            return;
        }
        c.removeMouseListener(ml);
        if (c instanceof Composite) {
            for (final Control cc : ((Composite) c).getChildren()) {
                removeRecursiveMouseListener(cc, ml, excludeControl);
            }
        }
    }

    public static List<IComment> getAllCommentsOfSheetAndChildren(
            ISheet sheet) {
        List<IComment> comments = new ArrayList<IComment>();
        if (sheet == null) {
            return comments;
        }
        comments.addAll(sheet.getOwnedWorkbook().getCommentManager()
                .getComments(sheet));
        List<ITopic> topics = MindMapUtils.getAllTopics(sheet, true, true);
        for (ITopic topic : topics) {
            comments.addAll(topic.getOwnedWorkbook().getCommentManager()
                    .getComments(topic));
        }
        return comments;
    }

    public static void reveal(IGraphicalEditor sourceEditor, Object target) {
        if (sourceEditor == null) {
            return;
        }

        if (target instanceof ITopic || target instanceof ISheet) {
            ISelectionProvider selectionProvider = sourceEditor.getSite()
                    .getSelectionProvider();
            if (selectionProvider != null) {
                selectionProvider.setSelection(new StructuredSelection(target));
            }
        }
    }

}
