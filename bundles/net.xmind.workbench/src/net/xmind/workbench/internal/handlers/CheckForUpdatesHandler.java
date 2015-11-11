package net.xmind.workbench.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import net.xmind.workbench.internal.CheckForUpdatesJob;

public class CheckForUpdatesHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        if (PlatformUI.isWorkbenchRunning()) {
            IWorkbench workbench = PlatformUI.getWorkbench();
            new CheckForUpdatesJob(workbench).schedule();
        }
        return null;
    }

}
