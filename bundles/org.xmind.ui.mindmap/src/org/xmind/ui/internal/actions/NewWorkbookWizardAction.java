package org.xmind.ui.internal.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.xmind.ui.internal.MindMapMessages;

public class NewWorkbookWizardAction extends Action implements IWorkbenchAction {

    private IWorkbenchWindow window;

    public NewWorkbookWizardAction(IWorkbenchWindow window) {
        super();
        this.window = window;
        setId("org.xmind.ui.newWorkbookWizard"); //$NON-NLS-1$
        setActionDefinitionId("org.xmind.ui.command.newWorkbookWizard"); //$NON-NLS-1$
        ISharedImages images = window.getWorkbench().getSharedImages();
        setImageDescriptor(images
                .getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD));
        setDisabledImageDescriptor(images
                .getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD_DISABLED));
        setText(MindMapMessages.NewWorkbookDialog_text);
        setToolTipText(MindMapMessages.NewWorkbookDialog_toolTip);
    }

    public void run() {
        IWorkbenchWindow window = this.window;
        if (window == null)
            return;

//        NewWorkbookEditor.showIn(window);
//        NewWorkbookWizardDialog.openWizard(window, false);
    }

    public void dispose() {
        this.window = null;
    }
}
