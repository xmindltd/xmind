package org.xmind.cathy.internal.handlers;

import java.net.URI;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.cathy.internal.WorkbenchMessages;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.MindMapUI;

public class WelcomeToXMindHandler extends AbstractHandler {

    private static final String URL_WELCOME_FILE = "platform:/plugin/org.xmind.cathy/$nl$/resource/Welcome%20to%20XMind.xmind"; //$NON-NLS-1$

    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
        if (window == null)
            return null;

        final IWorkbenchPage activePage = window.getActivePage();
        if (activePage == null)
            return null;

        MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                UserDataConstants.CATEGORY_OTHERS,
                UserDataConstants.WELCOME_TO_XMIND);
        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                IEditorInput input = MindMapUI.getEditorInputFactory()
                        .createEditorInput(new URI(URL_WELCOME_FILE),
                                WorkbenchMessages.WelcomeToXMindHandler_welcomeToXMind_templatedName);
                activePage.openEditor(input, MindMapUI.MINDMAP_EDITOR_ID);
            }
        });

        return null;
    }

}
