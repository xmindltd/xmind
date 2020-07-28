package org.xmind.ui.internal.protocols;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.gef.command.ICommandStack;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.ModifyTopicHyperlinkCommand;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.dialogs.DialogMessages;
import org.xmind.ui.mindmap.IHyperlinked;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.IProtocol;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class TopicProtocol implements IProtocol {

    private class GoTopicAction extends Action implements IHyperlinked {

        private String uri;

        private IWorkbook workbook;

        private Object context;

        public GoTopicAction(String text, ImageDescriptor image, String uri,
                IWorkbook workbook, Object context) {
            super(text);
            this.uri = uri;
            this.workbook = workbook;
            this.context = context;
            setImageDescriptor(image);
        }

        public void run() {
            Object element = HyperlinkUtils.findElement(uri, workbook);
            if (element != null) {
                navigateTo(context, element, workbook);
            } else {
                // Element may have been deleted, ask whether to delete 
                // this link as well.
                ITopic topic = findSourceTopic(context);
                if (topic == null)
                    return;

                if (confirmDelete(context, uri)) {
                    deleteHyperlink(topic, context, uri);
                }
            }
        }

        @Override
        public String getHyperlink() {
            return uri;
        }
    }

    public TopicProtocol() {
    }

    public IAction createOpenHyperlinkAction(final Object context,
            final String uri) {
        final IWorkbook workbook = MindMapUtils.findWorkbook(context);
        if (workbook == null)
            return null;
        final Object element = HyperlinkUtils.findElement(uri, workbook);
        ImageDescriptor icon = getIcon(element);
        if (icon == null) {
            icon = MindMapUI.getImages().get(IMindMapImages.UNKNOWN_FILE, true);
        }
        String title = getTitle(element);
        String sheetTitle = getSheetTitle(element);
        String name;
        if (sheetTitle != null) {
            name = String.format("%s (%s)", title, sheetTitle); //$NON-NLS-1$
        } else {
            name = title;
        }

        Action action = new GoTopicAction(
                MindMapMessages.TopicProtocol_GoToTopic_text, icon, uri,
                workbook, context);
        action.setToolTipText(name);
        return action;
    }

    /**
     * @param element
     * @return
     */
    private ImageDescriptor getIcon(Object element) {
        if (element instanceof ITopic) {
            ITopic topic = (ITopic) element;
            return MindMapUI.getImages().getTopicIcon(topic, true);
        }
        return null;
    }

    /**
     * @param element
     * @return
     */
    private String getSheetTitle(Object element) {
        ISheet sheet = getSheet(element);
        return sheet == null ? null : sheet.getTitleText();
    }

    /**
     * @param element
     * @return
     */
    private ISheet getSheet(Object element) {
        return MindMapUtils.findSheet(element);
    }

    /**
     * @param element
     * @return
     */
    private String getTitle(Object element) {
        return MindMapUtils.getText(element);
    }

    private static void navigateTo(Object context, Object element,
            IWorkbook workbook) {
        if (context instanceof IAdaptable) {
            ISelectionProvider selectionProvider = (ISelectionProvider) ((IAdaptable) context)
                    .getAdapter(ISelectionProvider.class);
            if (selectionProvider != null) {
                selectionProvider
                        .setSelection(new StructuredSelection(element));
                return;
            }
        }
    }

    private Shell findShell(Object context) {
        if (context instanceof IAdaptable)
            return (Shell) ((IAdaptable) context).getAdapter(Shell.class);
        return null;
    }

    public boolean isHyperlinkModifiable(Object source, String uri) {
        return true;
    }

    private boolean confirmDelete(Object context, String uri) {
        return MessageDialog.openQuestion(findShell(context),
                DialogMessages.TopicProtocol_ConfirmDeleteInvalidTopicHyperlink_windowTitle,
                DialogMessages.TopicProtocol_ConfirmDeleteInvalidTopicHyperlink_message);
    }

    private void deleteHyperlink(ITopic topic, Object context, String uri) {
        ModifyTopicHyperlinkCommand command = new ModifyTopicHyperlinkCommand(
                topic, null);
        command.setLabel(CommandMessages.Command_ModifyTopicHyperlink);
        ICommandStack commandStack = findCommandStack(context);
        if (commandStack != null) {
            commandStack.execute(command);
        } else {
            command.execute();
        }
    }

    private ITopic findSourceTopic(Object context) {
        if (context instanceof ITopic)
            return (ITopic) context;
        if (context instanceof IAdaptable)
            return (ITopic) ((IAdaptable) context).getAdapter(ITopic.class);
        if (context instanceof org.xmind.core.IAdaptable)
            return (ITopic) ((org.xmind.core.IAdaptable) context)
                    .getAdapter(ITopic.class);
        return null;
    }

    private ICommandStack findCommandStack(Object context) {
        if (context instanceof IAdaptable) {
            return (ICommandStack) ((IAdaptable) context)
                    .getAdapter(ICommandStack.class);
        }
        return null;
    }

}
