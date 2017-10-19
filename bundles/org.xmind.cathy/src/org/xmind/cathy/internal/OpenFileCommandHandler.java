package org.xmind.cathy.internal;

import java.util.Collections;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.xmind.cathy.internal.jobs.DownloadAndOpenFileJob;
import org.xmind.cathy.internal.jobs.OpenFilesJob;
import org.xmind.core.command.ICommand;
import org.xmind.core.command.ICommandHandler;

public class OpenFileCommandHandler implements ICommandHandler {

    public OpenFileCommandHandler() {
    }

    public IStatus execute(IProgressMonitor monitor, ICommand command,
            String[] matches) {
        IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null)
            return null;

        String file = command.getArgument("file"); //$NON-NLS-1$
        if (file != null)
            return openFile(workbench, file);

        String url = command.getArgument("url"); //$NON-NLS-1$
        if (url != null)
            return openURL(workbench, url, command.getArgument("name")); //$NON-NLS-1$

        return null;
    }

    private IStatus openFile(IWorkbench workbench, String filePath) {
        CathyPlugin.log("Opening file through 'xmind:' protocol: " + filePath); //$NON-NLS-1$
        OpenFilesJob openFilesJob = new OpenFilesJob(workbench,
                WorkbenchMessages.CheckOpenFilesJob_CheckFiles_name,
                Collections.singletonList(filePath));
        openFilesJob.setRule(Log.get(Log.OPENING));
        openFilesJob.schedule();
        return Status.OK_STATUS;
    }

    private IStatus openURL(IWorkbench workbench, String url, String name) {
        CathyPlugin.log("Opening URL through 'xmind:' protocol: " + url); //$NON-NLS-1$
        new DownloadAndOpenFileJob(workbench, url, name).schedule();
        return Status.OK_STATUS;
    }

}
