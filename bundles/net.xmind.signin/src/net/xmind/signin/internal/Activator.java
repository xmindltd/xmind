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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    /**
     * The home page for an authenticated user.
     * 
     * <pre>
     * http://www.xmind.net/xmind/account/USER_NAME/TOKEN/
     * </pre>
     */
    public static final String URL_ACCOUNT = "https://www.xmind.net/xmind/account/%s/%s/"; //$NON-NLS-1$

    /**
     * Sign out XMind.net account.
     */
    public static final String URL_SIGNOUT = "https://www.xmind.net/xmind/signout2/"; //$NON-NLS-1$

    /**
     * Sign up a new XMind.net account.
     */
    static final String URL_SIGN_UP = "https://www.xmind.net/xmind/signup/"; //$NON-NLS-1$

    /**
     * Handle with forgotten password.
     */
    public static final String URL_FORGOT_PASSWORD = "https://www.xmind.net/xmind/forgotpassword/"; //$NON-NLS-1$

    /**
     * System properties that contains account info of the current user.
     */
    public static final String PROP_USER = "net.xmind.signin.account.user"; //$NON-NLS-1$
    public static final String PROP_TOKEN = "net.xmind.signin.account.token"; //$NON-NLS-1$
    public static final String PROP_EXPIRE_DATE = "net.xmind.signin.account.expireDate"; //$NON-NLS-1$

    // The plug-in ID
    public static final String PLUGIN_ID = "net.xmind.signin"; //$NON-NLS-1$

    // The shared instance
    private static Activator plugin;

    private ServiceTracker<DebugOptions, DebugOptions> debugTracker = null;

    /**
     * The constructor
     */
    public Activator() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
     * BundleContext )
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
     * BundleContext )
     */
    public void stop(BundleContext context) throws Exception {
        ServiceTracker<DebugOptions, DebugOptions> theDebugTracker = this.debugTracker;
        if (theDebugTracker != null) {
            theDebugTracker.close();
        }
        this.debugTracker = null;

        plugin = null;
        super.stop(context);
    }

    private synchronized DebugOptions getDebugOptions() {
        if (debugTracker == null) {
            debugTracker = new ServiceTracker<DebugOptions, DebugOptions>(
                    getDefault().getBundle().getBundleContext(),
                    DebugOptions.class, null);
            debugTracker.open();
        }
        return debugTracker.getService();
    }

    public boolean isDebugging(String option) {
        DebugOptions options = getDebugOptions();
        return options != null
                && options.getBooleanOption(PLUGIN_ID + option, false);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    public static void log(String message) {
        getDefault().getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message));
    }

    public static void log(Throwable e) {
        log(e, ""); //$NON-NLS-1$
    }

    public static void log(Throwable e, String message) {
        getDefault().getLog()
                .log(new Status(IStatus.ERROR, PLUGIN_ID, message, e));
    }

}