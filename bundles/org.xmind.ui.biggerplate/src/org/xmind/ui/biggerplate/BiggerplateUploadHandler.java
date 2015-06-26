package org.xmind.ui.biggerplate;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.internal.biggerplate.BiggerplateUploader;
import org.xmind.ui.mindmap.IMindMapViewer;

public class BiggerplateUploadHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        upload(event);
        return null;
    }

    private void upload(ExecutionEvent event) {
        IEditorPart editor = HandlerUtil.getActiveEditor(event);
        if (!(editor instanceof IGraphicalEditor))
            return;

        IGraphicalEditorPage page = ((IGraphicalEditor) editor)
                .getActivePageInstance();
        if (page == null)
            return;

        IGraphicalViewer viewer = page.getViewer();
        if (!(viewer instanceof IMindMapViewer))
            return;

        final IMindMapViewer mindMapViewer = (IMindMapViewer) viewer;

        final Control control = mindMapViewer.getControl();
        if (control == null || control.isDisposed())
            return;

        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
//                new BiggerplateUploader(control.getShell(), mindMapViewer)
//                        .upload();
                BiggerplateUploader.getUploader(control.getShell(),
                        mindMapViewer).upload();
            }
        });
    }
}
