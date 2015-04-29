package org.xmind.ui.internal.evernote.signin;

import java.util.Properties;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.widgets.Display;
import org.xmind.ui.evernote.EvernoteStore;
import org.xmind.ui.evernote.signin.Evernote;
import org.xmind.ui.evernote.signin.IEvernoteAccount;

import com.evernote.clients.UserStoreClient;

/**
 * @author Jason Wong
 */
public class EvernoteAuthenticator {

    private SignInJob job = null;

    public EvernoteAuthenticator() {
    }

    public IEvernoteAccount signIn() {
        IEvernoteAccount account = Evernote.getAccountInfo();
        if (account != null)
            return account;

        Display display = Display.getCurrent();
        job = new SignInJob();
        job.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(IJobChangeEvent event) {
                if (job == event.getJob()) {
                    authenticationChanged(job.getData());
                    job = null;
                }
            }
        });
        job.setSystem(true);
        job.schedule();
        block(display);
        return Evernote.getAccountInfo();
    }

    private void authenticationChanged(Properties data) {
        if (data == null) {
            InternalEvernote.getInstance().getAccountStore().signedOut();
        } else {
            authenticationChanged(data.getProperty(EvernoteAccountStore.TOKEN),
                    data.getProperty(EvernoteAccountStore.USERNAME),
                    data.getProperty(EvernoteAccountStore.SERVICE_TYPE));
        }
    }

    private void authenticationChanged(String token, String username,
            String acceptLanguage) {
        if (token != null && !"".equals(token)//$NON-NLS-1$ 
                && username != null && !"".equals(username)//$NON-NLS-1$
                && acceptLanguage != null && !"".equals(acceptLanguage)) {//$NON-NLS-1$ 
            InternalEvernote.getInstance().getAccountStore()
                    .signedIn(token, username, acceptLanguage);
        } else if (token == null && username == null) {
            InternalEvernote.getInstance().getAccountStore().signedOut();
        }
    }

    private void block(Display display) {
        while (job != null) {
            if (display == null) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    break;
                }
            } else {
                if (!display.readAndDispatch())
                    display.sleep();
            }
        }
    }

    public void signOut() {
        IEvernoteAccount account = Evernote.getAccountInfo();
        if (account != null) {
            EvernoteStore evernoteStore = new EvernoteStore(account);
            try {
                UserStoreClient userStore = evernoteStore.getUserStore();
                userStore.revokeLongSession();
            } catch (Exception e) {
            }
        }
        authenticationChanged(null, null, null);
    }

}
