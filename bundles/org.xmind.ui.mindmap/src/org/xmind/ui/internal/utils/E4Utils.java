package org.xmind.ui.internal.utils;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;
import org.xmind.ui.internal.e4models.IContextRunnable;
import org.xmind.ui.internal.e4models.IModelConstants;

public class E4Utils {

    public static final void showPart(String commandId, IWorkbenchWindow window,
            final String partId, final String pageId,
            final String partStackId) {

        final IHandlerService hs = window.getService(IHandlerService.class);
        final ICommandService cs = window.getService(ICommandService.class);
        final Command command = cs.getCommand(commandId);
        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                IParameter partIdParam = command.getParameter(
                        IModelConstants.KEY_MODEL_PART_COMMAND_PARAMETER_PART_ID);
                IParameter pageIdParam = command.getParameter(
                        IModelConstants.KEY_MODEL_PART_COMMAND_PARAMETER_PAGE_ID);
                IParameter stackIdParam = command.getParameter(
                        IModelConstants.KEY_MODEL_PART_COMMAND_PARAMETER_PARTSTACK_ID);

                if (partIdParam == null || pageIdParam == null
                        || stackIdParam == null)
                    return;

                Parameterization[] parameters = new Parameterization[] {
                        new Parameterization(partIdParam, partId),
                        new Parameterization(pageIdParam, pageId),
                        new Parameterization(stackIdParam, partStackId) };

                ParameterizedCommand pc = new ParameterizedCommand(command,
                        parameters);
                hs.executeCommand(pc, null);
            }
        });

    }

    public static final MPart findPart(IWorkbenchWindow window, String partId) {
        EPartService partService = window.getService(EPartService.class);
        return partService.findPart(partId);
    }

    public static final IEclipseContext getEclipseContext(
            ExecutionEvent event) {
        Object eclipseContext = HandlerUtil.getVariable(event,
                IEclipseContext.class.getName());
        return (IEclipseContext) eclipseContext;
    }

    public static final IContextRunnable getContextRunnable(
            IEclipseContext context, String key) {
        String pageId = (String) context
                .get(IModelConstants.KEY_MODEL_PART_CURRENT_PAGE_ID);
        String contextRunnableKey = pageId == null ? key : pageId + "/" + key; //$NON-NLS-1$
        Object contextRunnable = context.get(contextRunnableKey);
        if (contextRunnable == null) {
            contextRunnable = context.get(key);
        }
        return (contextRunnable instanceof IContextRunnable)
                ? (IContextRunnable) contextRunnable : null;
    }

}
