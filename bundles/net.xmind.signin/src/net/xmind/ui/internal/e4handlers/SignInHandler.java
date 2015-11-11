package net.xmind.ui.internal.e4handlers;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;

import net.xmind.signin.IAccountInfo;
import net.xmind.signin.IAuthenticationListener;
import net.xmind.signin.XMindNet;
import net.xmind.signin.internal.Activator;

public class SignInHandler {

    public void execute(IWorkbenchWindow window) {
        final Display display = window.getShell().getDisplay();
        XMindNet.signIn(new IAuthenticationListener() {

            public void postSignOut(IAccountInfo oldAccountInfo) {
            }

            public void postSignIn(final IAccountInfo accountInfo) {
                display.asyncExec(new Runnable() {
                    public void run() {
                        showAccount(accountInfo);
                    }
                });
            }
        }, false);
    }

    private void showAccount(IAccountInfo accountInfo) {
        String userID = accountInfo.getUser();
        String token = accountInfo.getAuthToken();
        XMindNet.gotoURL(Activator.URL_ACCOUNT, userID, token);
    }

}
