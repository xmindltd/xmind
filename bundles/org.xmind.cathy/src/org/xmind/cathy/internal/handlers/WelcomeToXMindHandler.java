package org.xmind.cathy.internal.handlers;

import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.cathy.internal.CathyPlugin;
import org.xmind.cathy.internal.WorkbenchMessages;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.mindmap.MindMapUI;

public class WelcomeToXMindHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
        if (window == null)
            return null;

        IWorkbenchPage activePage = window.getActivePage();
        if (activePage == null)
            return null;

        URL url = FileLocator.find(Platform.getBundle(CathyPlugin.PLUGIN_ID),
                new Path("$nl$/resource/Welcome to XMind.xmind"), null); //$NON-NLS-1$
        if (url == null)
            return null;

        try {
            InputStream inputStream = url.openStream();
            IEditorInput input = MME.createTemplatedEditorInput(
                    WorkbenchMessages.WelcomeToXMindHandler_welcomeToXMind_templatedName, inputStream);
            activePage.openEditor(input, MindMapUI.MINDMAP_EDITOR_ID);
        } catch (Exception e) {
            CathyPlugin.log(e, null);
        }
        return null;
    }

}
