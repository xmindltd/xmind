package net.xmind.signin.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class SignOutHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        new net.xmind.ui.internal.e4handlers.SignOutHandler().execute();
        return null;
    }

}
