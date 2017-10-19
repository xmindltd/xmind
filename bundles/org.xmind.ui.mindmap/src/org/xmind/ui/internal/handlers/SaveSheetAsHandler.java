package org.xmind.ui.internal.handlers;

import java.util.Arrays;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.core.Core;
import org.xmind.core.ICloneData;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventSource2;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.MindMapUI;

public class SaveSheetAsHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
        IMindMap mindmap = MindMapUIPlugin.getAdapter(part, IMindMap.class);
        saveSheetAs(part, mindmap);

        return null;
    }

    private void saveSheetAs(final IWorkbenchPart part,
            final IMindMap mindmap) {
        if (mindmap == null)
            return;

        ISheet sheet = mindmap.getSheet();
        final IWorkbook newWorkbook = Core.getWorkbookBuilder()
                .createWorkbook(MME.createTempStorage());
        try {
            newWorkbook.saveTemp();
        } catch (Exception ignore) {
        }
        ICloneData clone = newWorkbook.clone(Arrays.asList(sheet));
        ISheet newSheet = (ISheet) clone.get(sheet);
        initSheet(newSheet);
        ITopic newCentralTopic = (ITopic) clone.get(mindmap.getCentralTopic());
        if (newCentralTopic == null)
            //TODO should we log this?
            return;

        newSheet.replaceRootTopic(newCentralTopic);
        newWorkbook.addSheet(newSheet);
        newWorkbook.removeSheet(newWorkbook.getPrimarySheet());

        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                final IEditorPart newEditor = part.getSite().getPage()
                        .openEditor(
                                MindMapUI.getEditorInputFactory()
                                        .createEditorInputForPreLoadedWorkbook(
                                                newWorkbook, null),
                                //new WorkbookEditorInput(newWorkbook, null, true),
                                MindMapUI.MINDMAP_EDITOR_ID, true);
                // Forcely make editor saveable:
                if (newWorkbook instanceof ICoreEventSource2) {
                    ((ICoreEventSource2) newWorkbook)
                            .registerOnceCoreEventListener(
                                    Core.WorkbookPreSaveOnce,
                                    ICoreEventListener.NULL);
                }
                if (newEditor != null && newEditor instanceof ISaveablePart) {
                    Display.getCurrent().timerExec(500, new Runnable() {
                        public void run() {
                            ((ISaveablePart) newEditor).doSaveAs();
                        }
                    });
                }
            }
        });

    }

    private void initSheet(ISheet sheet) {
        initTopic(sheet.getRootTopic());
    }

    private void initTopic(ITopic topic) {
        for (ITopic child : topic.getAllChildren()) {
            initTopic(child);
        }
    }

}
