package org.xmind.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.ui.internal.wizards.MarkerExportWizard;

public class ExportMarkerHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
        if (window == null)
            return null;

        Shell shell = window.getShell();
        if (shell == null || shell.isDisposed())
            return null;

        MarkerExportWizard wizard = new MarkerExportWizard();
        wizard.init(PlatformUI.getWorkbench(),
                HandlerUtil.getCurrentStructuredSelection(event));
        WizardDialog dialog = new WizardDialog(shell, wizard);
        dialog.create();
        dialog.open();
        return null;
    }
}
