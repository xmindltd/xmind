package org.xmind.ui.internal.handlers;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.xmind.core.Core;
import org.xmind.core.IWorkbook;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.editor.PreLoadedWorkbookRef;
import org.xmind.ui.mindmap.ITemplate;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.MindMapUI;

public class SaveAsTemplateHandler extends AbstractHandler {

    private static final int MAX_TITLE_LENGTH = 50;

    private File tempFolder;

    public Object execute(ExecutionEvent event) throws ExecutionException {
        saveAsTemplate(HandlerUtil.getActiveEditorChecked(event));
        return null;
    }

    private void saveAsTemplate(IEditorPart editor) throws ExecutionException {
        if (editor == null)
            return;

        Display display = Display.getCurrent();
        if (display == null || display.isDisposed())
            return;

        IWorkbookRef workbookRef = editor.getAdapter(IWorkbookRef.class);
        if (workbookRef == null)
            return;

        final IWorkbook workbook = workbookRef.getWorkbook();
        if (workbook == null)
            return;

        String initialName = workbookRef.getName();
        if (initialName == null)
            initialName = ""; //$NON-NLS-1$

        InputDialog dialog = new InputDialog(editor.getSite().getShell(),
                MindMapMessages.SaveAsTemplateHandler_inputDialog_title,
                MindMapMessages.SaveAsTemplateHandler_inputDialog_message,
                initialName, null);
        if (dialog.open() != InputDialog.OK)
            return;

        final String name = dialog.getValue();
        importCustomTemplate(display, editor, workbook,
                name.length() > MAX_TITLE_LENGTH
                        ? name.substring(0, MAX_TITLE_LENGTH) : name);
    }

    private void importCustomTemplate(final Display display,
            final IEditorPart editor, final IWorkbook workbook, String name)
            throws ExecutionException {
        if (tempFolder == null) {
            tempFolder = new File(
                    Core.getWorkspace().getTempDir("transient-templates")); //$NON-NLS-1$
            tempFolder.mkdirs();
        }

        final File tempFile = new File(tempFolder,
                name + MindMapUI.FILE_EXT_TEMPLATE);

        if (!tempFile.exists())
            try {
                tempFile.createNewFile();
            } catch (IOException e) {
            }

        final IWorkbookRef tempWorkbookRef = MindMapUIPlugin.getDefault()
                .getWorkbookRefFactory()
                .createWorkbookRef(tempFile.toURI(), null);
        try {
            if (tempWorkbookRef == null)
                return;

            IWorkbenchSiteProgressService context = editor.getSite()
                    .getService(IWorkbenchSiteProgressService.class);
            Assert.isTrue(context != null);

            ITemplate template;
            try {
                context.run(true, true, new IRunnableWithProgress() {
                    @Override
                    public void run(IProgressMonitor monitor)
                            throws InvocationTargetException,
                            InterruptedException {
                        IWorkbookRef sourceWorkbookRef = PreLoadedWorkbookRef
                                .createFromLoadedWorkbook(workbook, null);
                        SubMonitor subMonitor = SubMonitor.convert(monitor,
                                100);
                        sourceWorkbookRef.open(subMonitor.newChild(30));

                        try {
                            tempWorkbookRef.importFrom(subMonitor.newChild(60),
                                    sourceWorkbookRef);

                            /// Fix save as template, the thumbnail markers error
                            tempWorkbookRef.open(monitor);
                            tempWorkbookRef.save(monitor);
                        } finally {
                            sourceWorkbookRef.close(subMonitor.newChild(10));
                        }
                    }
                });

                template = MindMapUI.getResourceManager()
                        .addUserTemplateFromWorkbookURI(
                                tempWorkbookRef.getURI());

            } catch (InterruptedException e) {
                // canceled
                return;
            } catch (InvocationTargetException e) {
                Throwable cause = e.getTargetException();
                if (cause == null)
                    cause = e;
                throw new ExecutionException(cause.getMessage(), cause);
            }

            try {
                editor.getSite().getPage().openEditor(
                        MindMapUI.getEditorInputFactory().createEditorInput(
                                template.getSourceWorkbookURI()),
                        MindMapUI.MINDMAP_EDITOR_ID);
            } catch (PartInitException e) {
                throw new ExecutionException(e.getMessage(), e);
            }

        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
            if (tempWorkbookRef != null) {
                try {
                    tempWorkbookRef.close(new NullProgressMonitor());
                } catch (InvocationTargetException e) {
                    Throwable cause = e.getTargetException();
                    if (cause == null)
                        cause = e;
                    throw new ExecutionException(cause.getMessage(), cause);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
