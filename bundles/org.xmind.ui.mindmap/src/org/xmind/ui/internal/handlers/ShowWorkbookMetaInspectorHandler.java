package org.xmind.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.xmind.gef.IViewer;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.dialogs.WorkbookMetaInspectorDialog;
import org.xmind.ui.mindmap.IMindMapViewer;

public class ShowWorkbookMetaInspectorHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        final IEditorPart editor = MindMapHandlerUtil
                .findContributingEditor(event);
        if (editor == null)
            return null;

        IViewer viewer = MindMapUIPlugin.getAdapter(editor, IViewer.class);
        if (viewer == null || !(viewer instanceof IMindMapViewer))
            return null;

        Control control = viewer.getControl();
        if (control == null)
            return null;
        Shell shell = control.getShell();

        WorkbookMetaInspectorDialog dialog = WorkbookMetaInspectorDialog
                .getInstance(shell);
        dialog.setSourceEditor(editor);

        dialog.open();
        return null;
    }

}
