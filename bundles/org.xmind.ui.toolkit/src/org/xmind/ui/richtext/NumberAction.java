package org.xmind.ui.richtext;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.xmind.ui.internal.ToolkitImages;

public class NumberAction extends Action implements IRichTextAction {

    private IRichTextEditViewer viewer;

    public NumberAction(IRichTextEditViewer viewer) {
        this(viewer, "&Number", ToolkitImages.get(ToolkitImages.NUMBER), //$NON-NLS-1$
                "Number"); //$NON-NLS-1$
    }

    public NumberAction(IRichTextEditViewer viewer, String text,
            ImageDescriptor image, String toolTip) {
        super(text, IAction.AS_CHECK_BOX);
        this.viewer = viewer;
        setId(TextActionConstants.NUMBER_ID);
        setImageDescriptor(image);
        setToolTipText(toolTip);
    }

    public void run() {
        if (viewer == null || viewer.getControl().isDisposed())
            return;
        viewer.getRenderer().numberSelectionParagraph(isChecked());
    }

    public void selectionChanged(IRichTextEditViewer viewer, ISelection selection) {
        setChecked(viewer.getRenderer().getNumberSelectionParagraph());
    }

    public void dispose() {
        viewer = null;
    }
}
