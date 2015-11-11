package org.xmind.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.core.IWorkbook;
import org.xmind.ui.internal.MindMapTemplateManager;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.dialogs.DialogMessages;
import org.xmind.ui.internal.editor.MindMapEditor;
import org.xmind.ui.mindmap.MindMapUI;

public class SaveAsTemplateHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        saveAsTemplate(HandlerUtil.getActivePartChecked(event));
        return null;
    }

    private void saveAsTemplate(IWorkbenchPart part) {
        if (part == null || !(part instanceof MindMapEditor))
            return;

        final MindMapEditor editor = (MindMapEditor) part;

        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                editor.doSaveAs(new NullProgressMonitor(),
                        MindMapUI.FILE_EXT_TEMPLATE,
                        DialogMessages.TemplateFilterName);

                IWorkbook workbook = MindMapUIPlugin.getAdapter(editor,
                        IWorkbook.class);
                MindMapTemplateManager.getInstance().importCustomTemplate(
                        workbook, editor.getEditorInput().getName());
            }
        });

    }

}
