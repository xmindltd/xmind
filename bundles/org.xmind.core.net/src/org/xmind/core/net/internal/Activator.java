package org.xmind.core.net.internal;

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

    // The plug-in ID
    public static final String PLUGIN_ID = "org.xmind.core.net"; //$NON-NLS-1$

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
     * BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
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

        plugin = null;
        super.stop(context);
    }

    private synchronized DebugOptions getDebugOptions() {
        if (debugTracker == null) {
            debugTracker = new ServiceTracker<DebugOptions, DebugOptions>(getDefault().getBundle().getBundleContext(),
                    DebugOptions.class, null);
            debugTracker.open();
        }
        return debugTracker.getService();
    }

    public boolean isDebugging(String option) {
        DebugOptions options = getDebugOptions();
        return options != null && options.getBooleanOption(PLUGIN_ID + option, false);
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
        getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, e));
    }

}
