package org.xmind.ui.internal.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.core.IRevision;
import org.xmind.core.IRevisionManager;
import org.xmind.core.ISheet;
import org.xmind.core.IWorkbook;
import org.xmind.ui.internal.dialogs.RevisionPreviewDialog;

@Deprecated
public class PreviewRevisionHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        previewRevision(HandlerUtil.getCurrentSelection(event),
                HandlerUtil.getActiveShell(event));
        return null;
    }

    private static void previewRevision(ISelection selection, Shell shell) {
        if (selection == null || selection.isEmpty()
                || !(selection instanceof IStructuredSelection)
                || shell == null)
            return;

        Object obj = ((IStructuredSelection) selection).getFirstElement();
        if (!(obj instanceof IRevision))
            return;

        IRevision revision = (IRevision) obj;
        if (!IRevision.SHEET.equals(revision.getContentType()))
            return;

        IWorkbook workbook = revision.getOwnedWorkbook();
        ISheet sheet = (ISheet) workbook.findElement(revision.getResourceId(),
                null);
        if (sheet == null)
            return;

        IRevisionManager revisionManager = sheet.getOwnedWorkbook()
                .getRevisionRepository()
                .getRevisionManager(sheet.getId(), IRevision.SHEET);
        List<IRevision> revisions = revisionManager.getRevisions();
        int index = revisions.indexOf(revision);
        RevisionPreviewDialog dialog = new RevisionPreviewDialog(shell, sheet,
                revisions, index);
        dialog.open();
    }

}
