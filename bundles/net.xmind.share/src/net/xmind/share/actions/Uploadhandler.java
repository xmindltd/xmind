package net.xmind.share.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.internal.handlers.MindMapHandlerUtil;
import org.xmind.ui.mindmap.IMindMapViewer;

import net.xmind.share.Uploader;

public class UploadHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        upload(event);
        return null;
    }

    private void upload(ExecutionEvent event) {
        IEditorPart editor = MindMapHandlerUtil.findContributingEditor(event);
        if (!(editor instanceof IGraphicalEditor))
            return;

        IGraphicalEditorPage page = ((IGraphicalEditor) editor)
                .getActivePageInstance();
        if (page == null)
            return;

        IGraphicalViewer viewer = page.getViewer();
        if (!(viewer instanceof IMindMapViewer))
            return;

        final IMindMapViewer currentViewer = (IMindMapViewer) viewer;

        final Control control = currentViewer.getControl();
        if (control == null || control.isDisposed())
            return;

        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                new Uploader(control.getShell(), currentViewer).upload();
            }
        });
    }

}
