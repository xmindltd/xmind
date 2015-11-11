package net.xmind.workbench.internal;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import net.xmind.workbench.internal.notification.SiteEventNotificationService;

/**
 * The activator class controls the plug-in life cycle
 */
public class XMindNetWorkbench extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "net.xmind.workbench"; //$NON-NLS-1$

    // XMind.net URLs
    /**
     * Feedback page.
     */
    public static final String URL_FEEDBACK = "http://www.xmind.net/xmind/feedback/"; //$NON-NLS-1$

    /**
     * Online help page.
     */
    public static final String URL_HELP = "http://www.xmind.net/xmind/help/"; //$NON-NLS-1$

    /**
     * Invite others to try XMind.
     */
    public static final String URL_INVITE = "http://www.xmind.net/xmind/invite/"; //$NON-NLS-1$

    /**
     * Subscribe to XMind newsletter.
     */
    public static final String URL_SUBSCRIBE_NEWSLETTER = "https://www.xmind.net/xmind/newsletter/subscribe/"; //$NON-NLS-1$

    /**
     * Sign out XMind.net account.
     */
    public static final String URL_SIGNOUT = "https://www.xmind.net/xmind/signout2/"; //$NON-NLS-1$

    /**
     * Purchase XMind Pro/Plus/Subscription.
     */
    public static final String URL_PURCHASE = "https://www.xmind.net/xmind/buy/"; //$NON-NLS-1$

    /**
     * Common welcome page for anonymous users.
     */
    public static final String URL_WELCOME = "http://www.xmind.net/xmind/welcome/"; //$NON-NLS-1$

    /**
     * Welcome page for authenticated users.
     * 
     * <pre>
     * http://www.xmind.net/xmind/welcome/USER_NAME/TOKEN/
     * </pre>
     */
    public static final String URL_WELCOME_USER = "http://www.xmind.net/xmind/welcome/%s/%s"; //$NON-NLS-1$

    /**
     * The home page for an authenticated user.
     * 
     * <pre>
     * http://www.xmind.net/xmind/account/USER_NAME/TOKEN/
     * </pre>
     */
    public static final String URL_ACCOUNT = "https://www.xmind.net/xmind/account/%s/%s/"; //$NON-NLS-1$

    /**
     * System properties to control UI contributions.
     */
    public static final String PROP_AUTHENTICATED = "net.xmind.signin.account.authenticated"; //$NON-NLS-1$
    public static final String VALUE_AUTHENTICATED = "true"; //$NON-NLS-1$
    public static final String VALUE_UNAUTHENTICATED = "false"; //$NON-NLS-1$

    // The shared instance
    private static XMindNetWorkbench plugin;

    private ServiceTracker<DebugOptions, DebugOptions> debugTracker = null;

    /**
     * The constructor
     */
    public XMindNetWorkbench() {
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

        SiteEventNotificationService.migrateLocalStoreFile();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
     * BundleContext )
     */
    public void stop(BundleContext context) throws Exception {
        if (debugTracker != null) {
            debugTracker.close();
            debugTracker = null;
        }

        plugin = null;
        super.stop(context);
    }

    private DebugOptions getDebugOptions() {
        if (debugTracker == null) {
            debugTracker = new ServiceTracker<DebugOptions, DebugOptions>(
                    getBundle().getBundleContext(), DebugOptions.class, null);
            debugTracker.open();
        }
        return debugTracker.getService();
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static XMindNetWorkbench getDefault() {
        return plugin;
    }

    public static void log(Throwable e, String message) {
        getDefault().getLog()
                .log(new Status(IStatus.ERROR, PLUGIN_ID, message, e));
    }

    /**
     * 
     * @param option
     *            <code>"/debug/some/option"</code>
     * @return
     */
    public static boolean isDebugging(String option) {
        DebugOptions options = getDefault().getDebugOptions();
        if (options == null)
            return false;
        return options.getBooleanOption(PLUGIN_ID + option, false);
    }

    public static String getStatePath(String subPath) {
        IPath base = getDefault().getStateLocation();
        return base.append(subPath).makeAbsolute().toOSString();
    }

}
