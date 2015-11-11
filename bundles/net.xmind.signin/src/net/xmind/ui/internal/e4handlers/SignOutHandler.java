package net.xmind.ui.internal.e4handlers;

import net.xmind.signin.XMindNet;
import net.xmind.signin.internal.Activator;

public class SignOutHandler {

    public void execute() {
        boolean signedIn = XMindNet.getAccountInfo() != null;
        if (!signedIn)
            return;

        XMindNet.signOut();
        XMindNet.gotoURL(Activator.URL_SIGNOUT);
    }

}
