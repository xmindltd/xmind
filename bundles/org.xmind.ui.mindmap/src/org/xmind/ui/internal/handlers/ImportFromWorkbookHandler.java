package org.xmind.ui.internal.handlers;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.core.ISheet;
import org.xmind.core.IWorkbook;
import org.xmind.core.util.CloneHandler;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.command.ICommandStack;
import org.xmind.ui.commands.AddSheetCommand;
import org.xmind.ui.dialogs.IDialogConstants;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.wizards.WizardMessages;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.MindMapUI;

public class ImportFromWorkbookHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        final IEditorPart editor = HandlerUtil.getActiveEditorChecked(event);
        final IWorkbook targetWorkbook = editor.getAdapter(IWorkbook.class);
        if (targetWorkbook == null)
            throw new ExecutionException("No workbook available in editor."); //$NON-NLS-1$

        final ICommandStack commandStack = editor
                .getAdapter(ICommandStack.class);

        FileDialog dialog = new FileDialog(editor.getSite().getShell(),
                SWT.OPEN);
        dialog.setText(WizardMessages.ImportPage_FileDialog_text);
        dialog.setFilterExtensions(new String[] { "*" //$NON-NLS-1$
                + MindMapUI.FILE_EXT_XMIND });
        dialog.setFilterNames(new String[] { NLS.bind("{0} (*{1})", //$NON-NLS-1$
                IDialogConstants.FILE_DIALOG_FILTER_WORKBOOK,
                MindMapUI.FILE_EXT_XMIND) });
        final String sourcePath = dialog.open();
        if (sourcePath == null)
            /// canceled
            return null;

        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                URI uri = new File(sourcePath).toURI();
                IWorkbookRef sourceWorkbookRef = MindMapUIPlugin.getDefault()
                        .getWorkbookRefFactory().createWorkbookRef(uri, null);
                if (sourceWorkbookRef == null)
                    return;

                sourceWorkbookRef.open(null);
                try {
                    IWorkbook sourceWorkbook = sourceWorkbookRef.getWorkbook();
                    if (sourceWorkbook == null)
                        return;

                    List<Command> commands = new ArrayList<Command>();

                    CloneHandler cloneHandler = new CloneHandler()
                            .withWorkbooks(sourceWorkbook, targetWorkbook);
                    for (ISheet sourceSheet : sourceWorkbook.getSheets()) {
                        ISheet targetSheet = (ISheet) cloneHandler
                                .cloneObject(sourceSheet);
                        if (targetSheet != null) {
                            commands.add(new AddSheetCommand(targetSheet,
                                    targetWorkbook));
                        }
                    }

                    Command command = new CompoundCommand(commands);

                    if (commandStack != null) {
                        commandStack.execute(command);
                    } else {
                        command.execute();
                    }

                } finally {
                    sourceWorkbookRef.close(null);
                }

            }
        });

        return null;
    }

}
