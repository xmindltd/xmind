package net.xmind.workbench.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import net.xmind.workbench.internal.NewsletterSubscriptionReminder;
import net.xmind.workbench.internal.XMindNetWorkbenchServiceCenter;

public class SubscribeNewsletterHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        NewsletterSubscriptionReminder reminder = XMindNetWorkbenchServiceCenter
                .getNewsletterSubscriptionReminder();
        if (reminder != null) {
            reminder.show();
        }
        return null;
    }

}
