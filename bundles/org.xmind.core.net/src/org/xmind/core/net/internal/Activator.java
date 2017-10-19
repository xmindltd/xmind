package org.xmind.core.net.internal;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator implements BundleActivator {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.xmind.core.net"; //$NON-NLS-1$

    public static final String OPTION_HTTP_ASSC = "/debug/http/assc"; //$NON-NLS-1$

    public static final String OPTION_HTTP_REQEUSTS = "/debug/http/requests"; //$NON-NLS-1$

    public static final String CONFIG_DEBUG_HTTP_REQUESTS = "org.xmind.debug.httprequests"; //$NON-NLS-1$

    // The shared instance
    private static Activator plugin;

    private BundleContext bundleContext;

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
     * BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        plugin = this;
        this.bundleContext = context;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
     * BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        ServiceTracker<DebugOptions, DebugOptions> theDebugTracker = this.debugTracker;
        if (theDebugTracker != null) {
            theDebugTracker.close();
        }
        this.debugTracker = null;

        this.bundleContext = null;
        plugin = null;
    }

    public Bundle getBundle() {
        return bundleContext.getBundle();
    }

    public ILog getLog() {
        return Platform.getLog(getBundle());
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

    public static boolean isDebugging(String option) {
        Activator defaultInstance = getDefault();
        if (defaultInstance == null)
            return false;
        DebugOptions options = defaultInstance.getDebugOptions();
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
        Activator p = getDefault();
        if (p == null) {
            System.out.println(message);
        } else {
            p.getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message));
        }
    }

    public static void log(Throwable e) {
        log(e, null);
    }

    public static void log(Throwable e, String message) {
        Activator p = getDefault();
        if (p == null) {
            if (message != null) {
                System.err.println(message);
            }
            e.printStackTrace();
        } else {
            p.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, e));
        }
    }

}
