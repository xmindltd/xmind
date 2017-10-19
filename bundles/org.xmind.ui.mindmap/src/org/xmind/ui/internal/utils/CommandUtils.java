package org.xmind.ui.internal.utils;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

public class CommandUtils {

    public static void executeCommand(String commandId,
            IWorkbenchWindow window) {
        if (window == null || commandId == null || commandId.equals("")) { //$NON-NLS-1$
            return;
        }

        final IHandlerService hs = window.getService(IHandlerService.class);
        final ICommandService cs = window.getService(ICommandService.class);
        final Command command = cs.getCommand(commandId);

        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {

                ParameterizedCommand pc = new ParameterizedCommand(command,
                        null);
                hs.executeCommand(pc, null);
            }
        });

    }

}
