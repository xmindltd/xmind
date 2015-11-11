package org.xmind.ui.internal.notes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.Core;
import org.xmind.core.INotes;
import org.xmind.core.INotesContent;
import org.xmind.core.IPlainNotesContent;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.gef.ui.actions.IActionRegistry;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.ui.actions.DelegatingAction;
import org.xmind.ui.actions.MindMapActionFactory;
import org.xmind.ui.commands.DeleteNotesCommand;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.mindmap.BranchPart;
import org.xmind.ui.mindmap.AbstractInfoItemContributor;
import org.xmind.ui.mindmap.IInfoPart;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;

public class NotesInfoItemContributor extends AbstractInfoItemContributor {

    private static class ShowNotesAction extends Action {

        private ITopicPart topicPart;

        public ShowNotesAction(ITopicPart topicPart) {
            super(MindMapMessages.EditNotes_text,
                    MindMapUI.getImages().get(IMindMapImages.NOTES, true));
            setId(MindMapActionFactory.EDIT_NOTES.getId());
            setDisabledImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.NOTES, false));
            this.topicPart = topicPart;
        }

        public void run() {
            if (!topicPart.getStatus().isActive())
                return;

            IWorkbenchWindow window = PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow();
            if (window == null)
                return;

            NotesPopup popup = new NotesPopup(window,
                    ((BranchPart) topicPart.getParent()).getTopicPart(), true,
                    false);
            popup.open();
        }
    }

    private static final int NOTES_LENGTH = 24;

    private static final int MININUM = 4;

    public IAction createAction(ITopicPart topicPart, ITopic topic) {
        INotes notes = topic.getNotes();
        if (notes.isEmpty())
            return null;

        IAction action = null;
        IActionRegistry actionRegistry = (IActionRegistry) topicPart
                .getAdapter(IActionRegistry.class);
        if (actionRegistry != null) {
            action = actionRegistry
                    .getAction(MindMapActionFactory.EDIT_NOTES.getId());
            if (action != null)
                action = new DelegatingAction(action);
        }
        if (action == null)
            action = new ShowNotesAction(topicPart);

        INotesContent content = notes.getContent(INotes.PLAIN);
        if (content instanceof IPlainNotesContent) {
            String text = ((IPlainNotesContent) content).getTextContent();
            if (text.length() > 500)
                text = text.substring(0, 500) + "...\n..."; //$NON-NLS-1$
            action.setToolTipText(text);
        }
        return action;
    }

    public String getContent(ITopic topic) {
        INotes notes = topic.getNotes();
        if (notes.isEmpty())
            return null;

        INotesContent content = notes.getContent(INotes.PLAIN);
        if (content instanceof IPlainNotesContent) {
            String text = ((IPlainNotesContent) content).getTextContent()
                    .replaceAll("\r\n|\r|\n", " "); //$NON-NLS-1$ //$NON-NLS-2$
            if (text.length() < MININUM)
                return text;
            return text
                    .substring(0,
                            (text.length() - MININUM) < NOTES_LENGTH
                                    ? (text.length() - MININUM) : NOTES_LENGTH)
                    + "..."; //$NON-NLS-1$
        }
        return null;
    }

    @Override
    public boolean isCardModeAvailable(ITopic topic, ITopicPart topicPart) {
        return !isIconTipOnly(topicPart);
    }

    @Override
    protected void registerTopicEvent(ITopicPart topicPart, ITopic topic,
            ICoreEventRegister register) {
        register.register(Core.TopicNotes);
    }

    @Override
    protected void handleTopicEvent(IInfoPart infoPart, CoreEvent event) {
        infoPart.refresh();
    }

    @Override
    protected void handleTopicEvent(ITopicPart topicPart, CoreEvent event) {
        topicPart.refresh();
    }

    public void removeNotes(ITopic topic) {
        if (topic == null)
            return;

        IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        if (window == null)
            return;

        IGraphicalEditor editor = (IGraphicalEditor) window.getActivePage()
                .getActiveEditor();
        if (editor == null)
            return;

        DeleteNotesCommand removeCommand = new DeleteNotesCommand(topic);
        if (removeCommand != null)
            editor.getCommandStack().execute(removeCommand);
    }

    @Override
    public List<IAction> getPopupMenuActions(ITopicPart topicPart,
            final ITopic topic) {
        List<IAction> actions = new ArrayList<IAction>();
        IAction editNotesAction = createAction(topicPart, topic);

        editNotesAction.setText(MindMapMessages.ModifyMenu);
        IAction deleteNotesAction = new Action(
                MindMapMessages.InfoItem_Delete_text) {
            @Override
            public void run() {
                removeNotes(topic);
            };
        };
        deleteNotesAction.setId("org.xmind.ui.removeNotes"); //$NON-NLS-1$
        deleteNotesAction.setImageDescriptor(
                MindMapUI.getImages().get(IMindMapImages.DELETE, true));

        actions.add(editNotesAction);
        actions.add(deleteNotesAction);
        return actions;
    }

}
