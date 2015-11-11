/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package net.xmind.signin.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.widgets.Display;
import org.xmind.core.net.IDataStore;

import net.xmind.signin.IAccountInfo;
import net.xmind.signin.IAuthenticationListener;
import net.xmind.signin.ISignInDialogExtension;
import net.xmind.signin.IXMindNetCommand;
import net.xmind.signin.IXMindNetCommandHandler;
import net.xmind.signin.XMindNet;

public class XMindNetAuthenticator implements IXMindNetCommandHandler {

    public static final int TOKEN_LIFE_TIME = 3600 * 24 * 7; //two weeks

    private List<IAuthenticationListener> callbacks = null;

    private SignInJob job = null;

    public XMindNetAuthenticator() {
        XMindNet.addXMindNetCommandHandler("signout", this); //$NON-NLS-1$
        XMindNet.addXMindNetCommandHandler("200", this); //$NON-NLS-1$
    }

    public IAccountInfo signIn(String message,
            ISignInDialogExtension extension) {
        signIn(null, true, message, extension);
        return XMindNet.getAccountInfo();
    }

    public void signIn(IAuthenticationListener callback, boolean block,
            String message, ISignInDialogExtension extension) {
        if (XMindNet.getAccountInfo() != null) {
            if (callback != null) {
                callback.postSignIn(XMindNet.getAccountInfo());
            }
            return;
        }

        if (callback != null) {
            if (callbacks == null)
                callbacks = new ArrayList<IAuthenticationListener>();
            callbacks.add(callback);
        }

        Display display = block ? Display.getCurrent() : null;

        if (job == null) {
            job = new SignInJob(message, extension);
            job.addJobChangeListener(new JobChangeAdapter() {
                public void done(IJobChangeEvent event) {
                    if (job == event.getJob()) {
                        authenticationChanged(job.getData());
                        job = null;
                    }
                }
            });
            job.schedule();
        }

        if (block)
            block(display);
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
        authenticationChanged(IDataStore.EMPTY);
    }

    private void authenticationChanged(IDataStore data) {
        String user = data.getString(XMindNetAccount.USER);
        String authToken = data.getString(XMindNetAccount.TOKEN);
        if (user != null && !"".equals(user) //$NON-NLS-1$
                && authToken != null && !"".equals(authToken)) { //$NON-NLS-1$
            long expireDate = System.currentTimeMillis() + TOKEN_LIFE_TIME;
            notifyCallbacks(new AccountInfo(user, authToken, expireDate));
            InternalXMindNet.getInstance().getAccount().signedIn(user,
                    authToken, expireDate, true);
//                    data.getBoolean(SignInJob.REMEMBER));
        } else {
            notifyCallbacks(null);
            InternalXMindNet.getInstance().getAccount().signedOut();
        }
    }

    private void notifyCallbacks(final IAccountInfo accountInfo) {
        if (callbacks == null || callbacks.isEmpty())
            return;
        Object[] array = callbacks.toArray();
        callbacks = null;
        for (final Object callback : array) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    if (accountInfo != null) {
                        ((IAuthenticationListener) callback)
                                .postSignIn(accountInfo);
                    } else {
                        ((IAuthenticationListener) callback).postSignOut(null);
                    }
                }
            });
        }
    }

    public boolean handleXMindNetCommand(IXMindNetCommand command) {
        if ("signout".equals(command.getCommandName())) { //$NON-NLS-1$
            signOut();
            return true;
        } else if ("200".equals(command.getCode())) { //$NON-NLS-1$
            authenticationChanged(command.getContent());
            return true;
        }
        return false;
    }

}
