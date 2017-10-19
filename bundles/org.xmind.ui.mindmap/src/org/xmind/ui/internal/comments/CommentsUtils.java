package org.xmind.ui.internal.comments;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.ui.util.MindMapUtils;

public class CommentsUtils {

    private CommentsUtils() {
    }

    public static void collectTopicsWithComments(ITopic topic,
            List<ITopic> result) {
        if (topic.getOwnedWorkbook().getCommentManager()
                .hasComments(topic.getId())) {
            result.add(topic);
        }

        Iterator<ITopic> childIt = topic.getAllChildrenIterator();
        while (childIt.hasNext()) {
            collectTopicsWithComments(childIt.next(), result);
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

    public static void reveal(IGraphicalEditor editor, Object target) {
        if (editor == null)
            return;

        editor.getSite().getPage().activate(editor);

        if (target instanceof ITopic || target instanceof ISheet) {
            ISelectionProvider selectionProvider = editor.getSite()
                    .getSelectionProvider();
            if (selectionProvider != null) {
                selectionProvider.setSelection(new StructuredSelection(target));
            }
        }
    }

}
