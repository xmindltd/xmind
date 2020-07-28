package org.xmind.ui.internal.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressService;
import org.xmind.core.ISheet;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.command.ICommandStack;
import org.xmind.ui.commands.AddSheetCommand;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.dialogs.NewSheetFromTemplateDialog;
import org.xmind.ui.mindmap.ITemplate;
import org.xmind.ui.mindmap.IWorkbookRef;

/**
 * @author Frank Shaka
 * @since 3.6.50
 */
public class CreateSheetFromTemplateHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        createSheetFromTemplate(HandlerUtil.getActiveEditorChecked(event));
        return null;
    }

    private void createSheetFromTemplate(IEditorPart targetEditor)
            throws ExecutionException {
        final IWorkbookRef targetWorkbookRef = targetEditor
                .getAdapter(IWorkbookRef.class);
        final IWorkbook targetWorkbook = targetWorkbookRef == null ? null
                : targetWorkbookRef.getWorkbook();
        if (targetWorkbook == null)
            throw new ExecutionException(
                    "No workbook available in active editor"); //$NON-NLS-1$

        NewSheetFromTemplateDialog dialog = new NewSheetFromTemplateDialog(
                targetEditor.getSite().getShell());
        if (dialog.open() != NewSheetFromTemplateDialog.OK)
            return;

        MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                UserDataConstants.CATEGORY_SHEET,
                UserDataConstants.CREATE_SHEET);
        MindMapUIPlugin.getDefault().getUsageDataCollector()
                .trackView(UserDataConstants.VIEW_TEMPLATES);

        final ITemplate template = dialog.getTemplate();

        MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                UserDataConstants.CATEGORY_TEMPLATE,
                String.format(UserDataConstants.USE_TEMPLATE_S,
                        template.getName().replaceAll(" ", "_"))); //$NON-NLS-1$ //$NON-NLS-2$
        Assert.isTrue(template != null);
        final IWorkbookRef tempWorkbookRef = template.createWorkbookRef();
        if (tempWorkbookRef == null)
            throw new ExecutionException(
                    "Failed to create workbook ref from template: " //$NON-NLS-1$
                            + template.toString());

        final List<Command> commands = new ArrayList<Command>();
        IProgressService context = targetEditor.getSite()
                .getService(IProgressService.class);
        Assert.isTrue(context != null);
        try {
            context.run(true, true, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException {
                    createCommands(monitor, commands, tempWorkbookRef,
                            targetWorkbook);
                }
            });
        } catch (InterruptedException e) {
            // canceled
            return;
        } catch (InvocationTargetException e) {
            Throwable cause = e.getTargetException();
            if (cause instanceof ExecutionException)
                throw (ExecutionException) cause;
            if (cause != null)
                throw new ExecutionException(cause.getMessage(), cause);
            throw new ExecutionException(e.getMessage(), e);
        }

        if (commands.isEmpty())
            return;

        final Command command = new CompoundCommand(commands);
        command.setLabel(
                MindMapMessages.NewSheetFromTemplateDialog_NewSheetFromTemplteCommand_label);
        ICommandStack commandStack = targetWorkbookRef.getCommandStack();
        if (commandStack != null) {
            commandStack.execute(command);
        } else {
            command.execute();
        }

    }

    private void createCommands(IProgressMonitor monitor,
            List<Command> commands, IWorkbookRef tempWorkbookRef,
            IWorkbook targetWorkbook)
            throws InterruptedException, InvocationTargetException {
        SubMonitor subMonitor = SubMonitor.convert(monitor, 100);

        tempWorkbookRef.open(subMonitor.newChild(50));
        try {
            if (monitor.isCanceled())
                throw new InterruptedException();

            IWorkbook tempWorkbook = tempWorkbookRef.getWorkbook();
            Assert.isTrue(tempWorkbook != null);
            addCommands(subMonitor.newChild(40), commands, tempWorkbook,
                    targetWorkbook);

        } finally {
            tempWorkbookRef.close(subMonitor.newChild(10));
        }
    }

    private void addCommands(IProgressMonitor monitor, List<Command> commands,
            IWorkbook tempWorkbook, IWorkbook targetWorkbook) {
        int sheetCount = targetWorkbook.getSheets().size();
        for (ISheet tempSheet : tempWorkbook.getSheets()) {
            ISheet sheet = (ISheet) targetWorkbook
                    .clone(Arrays.asList(tempSheet)).get(tempSheet);
            if (sheet == null)
                continue;
            sheetCount += 1;
            sheet.setTitleText(
                    NLS.bind(MindMapMessages.TitleText_Sheet, sheetCount));
            commands.add(new AddSheetCommand(sheet, targetWorkbook));
        }

    }
}
