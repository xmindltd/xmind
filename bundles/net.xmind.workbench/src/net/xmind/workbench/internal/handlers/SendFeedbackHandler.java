package net.xmind.workbench.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import net.xmind.signin.XMindNet;
import net.xmind.workbench.internal.XMindNetWorkbench;

public class SendFeedbackHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {

        XMindNet.gotoURL(XMindNetWorkbench.URL_FEEDBACK);

        return null;
    }

}
