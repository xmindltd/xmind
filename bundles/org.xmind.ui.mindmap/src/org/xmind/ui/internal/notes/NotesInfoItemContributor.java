package org.xmind.ui.internal.notes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Display;
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
import org.xmind.ui.internal.e4models.IModelConstants;
import org.xmind.ui.internal.utils.E4Utils;
import org.xmind.ui.mindmap.AbstractInfoItemContributor;
import org.xmind.ui.mindmap.IInfoPart;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;

public class NotesInfoItemContributor extends AbstractInfoItemContributor {

    private static final String PRESENTATION_VIERWER_CLASS_NAME = "PresentationViewer"; //$NON-NLS-1$

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
            if (topicPart == null || topicPart.getSite() == null
                    || topicPart.getSite().getViewer() == null
                    || topicPart.getSite().getViewer().getClass()
                            .getSimpleName()
                            .equals(PRESENTATION_VIERWER_CLASS_NAME))
                return;

            if (!topicPart.getStatus().isActive())
                return;

//            MindMapUIPlugin.getDefault().getUsageDataCollector()
//                    .increase("UseNotesCount"); //$NON-NLS-1$

            final IWorkbenchWindow window = PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow();
            if (window == null)
                return;

            Display.getCurrent().asyncExec(new Runnable() {
                public void run() {
                    E4Utils.showPart(IModelConstants.COMMAND_SHOW_MODEL_PART,
                            window, IModelConstants.PART_ID_NOTES, null,
                            IModelConstants.PART_STACK_ID_RIGHT);
                }
            });
        }
    }

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
        if (action == null || action.getImageDescriptor() == null)
            action = new ShowNotesAction(topicPart);

        INotesContent content = notes.getContent(INotes.PLAIN);
        if (content instanceof IPlainNotesContent) {
            String text = ((IPlainNotesContent) content).getTextContent();
            if (text.length() > 500)
                text = text.substring(0, 500) + "...\n..."; //$NON-NLS-1$
            action.setToolTipText(text);
        }

        action.setEnabled(true);

        return action;
    }

    public String getContent(ITopic topic) {
        INotes notes = topic.getNotes();
        if (notes.isEmpty())
            return null;

        INotesContent content = notes.getContent(INotes.PLAIN);
        if (content instanceof IPlainNotesContent)
            return ((IPlainNotesContent) content).getTextContent()
                    .replaceAll("\r\n|\r|\n", " "); //$NON-NLS-1$ //$NON-NLS-2$

        return null;
    }

    public String getSVGFilePath(ITopic topic, IAction action) {
        return "platform:/plugin/org.xmind.ui.mindmap/icons/notes.svg"; //$NON-NLS-1$
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
        editNotesAction.setImageDescriptor(null);
        IAction deleteNotesAction = new Action(
                MindMapMessages.InfoItem_Delete_text) {
            @Override
            public void run() {
                removeNotes(topic);
            };
        };
        deleteNotesAction.setId("org.xmind.ui.removeNotes"); //$NON-NLS-1$
        deleteNotesAction.setImageDescriptor(null);

        actions.add(editNotesAction);
        actions.add(deleteNotesAction);
        return actions;
    }

}
