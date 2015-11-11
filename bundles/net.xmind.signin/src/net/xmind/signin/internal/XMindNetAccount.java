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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.SafeRunnable;

import net.xmind.signin.IAccountInfo;
import net.xmind.signin.IAuthenticationListener;
import net.xmind.signin.IAuthorizationListener;
import net.xmind.signin.IPreauthorizationListener;

@SuppressWarnings("deprecation")
public class XMindNetAccount {

    public static final String USER = "user"; //$NON-NLS-1$

    public static final String TOKEN = "token"; //$NON-NLS-1$

    public static final String SUBSCRIPTION_EXPIRE_DATE = "subscriptionExpireDate"; //$NON-NLS-1$

    public static final String EXPIRE_DATE = "expireDate"; //$NON-NLS-1$

    private List<IAuthenticationListener> authenticationListeners = new ArrayList<IAuthenticationListener>();

    @Deprecated
    private List<IAuthorizationListener> authorizationListeners = new ArrayList<IAuthorizationListener>();

    @Deprecated
    private List<IPreauthorizationListener> preauthorizationListeners = new ArrayList<IPreauthorizationListener>();

    private IPreferenceStore localStore;

    private AccountInfo accountInfo = null;

    private boolean preauthorized = false;

    public XMindNetAccount(IPreferenceStore prefStore) {
        this.localStore = prefStore;

        // Clear previously stored info
        prefStore.setValue("USER_ID", ""); //$NON-NLS-1$ //$NON-NLS-2$
        prefStore.setValue("TOKEN", ""); //$NON-NLS-1$ //$NON-NLS-2$

        readFromStore();
    }

    public void addAuthenticationListener(IAuthenticationListener listener) {
        authenticationListeners.add(listener);
    }

    public void removeAuthenticationListener(IAuthenticationListener listener) {
        authenticationListeners.remove(listener);
    }

    @Deprecated
    public void addAuthorizationListener(IAuthorizationListener listener) {
        authorizationListeners.add(listener);
    }

    @Deprecated
    public void removeAuthorizationListener(IAuthorizationListener listener) {
        authorizationListeners.remove(listener);
    }

    @Deprecated
    public void addPreauthorizationListener(
            IPreauthorizationListener listener) {
        if (preauthorized)
            listener.preauthorized();
        else if (preauthorizationListeners != null)
            preauthorizationListeners.add(listener);
    }

    /**
     * @param listener
     */
    @Deprecated
    public void removePreauthorizationListener(
            IPreauthorizationListener listener) {
        if (preauthorizationListeners != null) {
            preauthorizationListeners.remove(listener);
        }
    }

    public IAccountInfo getAccountInfo() {
        return accountInfo;
    }

    public void signedIn(String user, String authToken, long expireDate,
            boolean remember) {
        IAccountInfo oldAccountInfo = this.accountInfo;
        this.accountInfo = new AccountInfo(user, authToken, expireDate);
//        if (remember) {
        saveToStore(user, authToken, expireDate);
//        } else {
//            clearStore();
//        }
        saveToSystemProperties(user, authToken, expireDate);
        if (oldAccountInfo != null)
            firePostSignOut(oldAccountInfo);
        firePostSignIn(this.accountInfo);
    }

    public void signedOut() {
        IAccountInfo oldAccountInfo = this.accountInfo;
        this.accountInfo = null;
        clearSystemProperties();
        clearStore();
        firePostSignOut(oldAccountInfo);
    }

    private void saveToSystemProperties(String user, String authToken,
            long expireDate) {
        System.setProperty(Activator.PROP_USER, user);
        System.setProperty(Activator.PROP_TOKEN, authToken);
        System.setProperty(Activator.PROP_EXPIRE_DATE, String.valueOf(expireDate));
    }

    private void clearSystemProperties() {
        System.setProperty(Activator.PROP_USER, ""); //$NON-NLS-1$
        System.setProperty(Activator.PROP_TOKEN, ""); //$NON-NLS-1$
        System.setProperty(Activator.PROP_EXPIRE_DATE, ""); //$NON-NLS-1$
    }

    @Deprecated
    public void authorized(int subscriptionStatus) {
        if (this.accountInfo == null)
            return;

        this.accountInfo.setSubscriptionStatus(subscriptionStatus);
        fireAuthorized(this.accountInfo);
    }

    @Deprecated
    public void unauthorized(IStatus result) {
        if (this.accountInfo != null)
            this.accountInfo.setSubscriptionStatus(IAccountInfo.UNKNOWN);
        fireUnauthorized(result, this.accountInfo);
    }

    @Deprecated
    public void preauthorized() {
        this.preauthorized = true;
        firePreauthorized();
    }

    private void firePostSignIn(final IAccountInfo accountInfo) {
        for (final IAuthenticationListener listener : authenticationListeners
                .toArray(new IAuthenticationListener[authenticationListeners
                        .size()])) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    listener.postSignIn(accountInfo);
                }

                @Override
                public void handleException(Throwable e) {
                    Activator.log(e, "Error while firing 'postSignIn'"); //$NON-NLS-1$
                    super.handleException(e);
                }
            });
        }
    }

    private void firePostSignOut(final IAccountInfo oldAccountInfo) {
        for (final IAuthenticationListener listener : authenticationListeners
                .toArray(new IAuthenticationListener[authenticationListeners
                        .size()])) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    listener.postSignOut(oldAccountInfo);
                }

                @Override
                public void handleException(Throwable e) {
                    Activator.log(e, "Error while firing 'postSignOut'"); //$NON-NLS-1$
                    super.handleException(e);
                }
            });
        }
    }

    @Deprecated
    private void fireAuthorized(final IAccountInfo accountInfo) {
        for (final IAuthorizationListener listener : authorizationListeners
                .toArray(new IAuthorizationListener[authorizationListeners
                        .size()])) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    listener.authorized(accountInfo);
                }

                @Override
                public void handleException(Throwable e) {
                    Activator.log(e, "Error while firing 'authorized'"); //$NON-NLS-1$
                    super.handleException(e);
                }
            });
        }
    }

    @Deprecated
    private void fireUnauthorized(final IStatus result,
            final IAccountInfo accountInfo) {
        for (final IAuthorizationListener listener : authorizationListeners
                .toArray(new IAuthorizationListener[authorizationListeners
                        .size()])) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    listener.unauthorized(result, accountInfo);
                }

                @Override
                public void handleException(Throwable e) {
                    Activator.log(e, "Error while firing 'unauthorized'"); //$NON-NLS-1$
                    super.handleException(e);
                }
            });
        }
    }

    @Deprecated
    private void firePreauthorized() {
        IPreauthorizationListener[] listeners = preauthorizationListeners
                .toArray(new IPreauthorizationListener[preauthorizationListeners
                        .size()]);
        preauthorizationListeners = null;
        for (final IPreauthorizationListener listener : listeners) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    listener.preauthorized();
                }

                @Override
                public void handleException(Throwable e) {
                    Activator.log(e, "Error while firing 'preauthorized'"); //$NON-NLS-1$
                    super.handleException(e);
                }
            });
        }
    }

    private void readFromStore() {
        String user = localStore.getString(USER);
        String authToken = localStore.getString(TOKEN);
        long expireDate = localStore.getLong(EXPIRE_DATE);
        if (user != null && !"".equals(user) //$NON-NLS-1$
                && authToken != null && !"".equals(authToken) //$NON-NLS-1$
                && expireDate > 0) {
            this.accountInfo = new AccountInfo(user, authToken, expireDate);
            saveToSystemProperties(user, authToken, expireDate);
        }
    }

    private void saveToStore(String user, String authToken, long expireDate) {
        localStore.setValue(USER, user);
        localStore.setValue(TOKEN, authToken);
        localStore.setValue(EXPIRE_DATE, expireDate);
        flushStore();
    }

    private void clearStore() {
        localStore.setValue(USER, ""); //$NON-NLS-1$
        localStore.setValue(TOKEN, ""); //$NON-NLS-1$
        localStore.setValue(EXPIRE_DATE, 0);
        flushStore();
    }

    private void flushStore() {
        IEclipsePreferences node = InstanceScope.INSTANCE
                .getNode(Activator.PLUGIN_ID);
        if (node != null) {
            try {
                node.flush();
            } catch (Throwable e) {
            }
        }
    }

}
