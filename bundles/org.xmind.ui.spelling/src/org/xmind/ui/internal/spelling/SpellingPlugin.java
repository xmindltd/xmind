/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.spelling;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.xmind.core.usagedata.IUsageDataSampler;

/**
 * The activator class controls the plug-in life cycle
 */
public class SpellingPlugin extends AbstractUIPlugin {

    public static final String SPELLING_CHECK_ENABLED = "SPELLING_CHECK_ENABLED"; //$NON-NLS-1$

    public static final String DEFAULT_SPELLING_CHECKER_INVISIBLE = "DEFAULT_SPELLING_CHECKER_INVISIBLE"; //$NON-NLS-1$

    public static final String DEFAULT_SPELLING_CHECKER_DISABLED = "DEFAULT_SPELLING_CHECKER_DISABLED"; //$NON-NLS-1$

    public static final String HIDE_SPELLING_CHECK_PROGRESS = "HIDE_SPELLING_CHECK_PROGRESS"; //$NON-NLS-1$

    public static final String SPELLING_CHECK_VIEW_ID = "org.xmind.ui.SpellingCheckView"; //$NON-NLS-1$

    // The plug-in ID
    public static final String PLUGIN_ID = "org.xmind.ui.spelling"; //$NON-NLS-1$

    // The shared instance
    private static SpellingPlugin plugin;

    private ServiceTracker<IUsageDataSampler, IUsageDataSampler> usageDataTracker;

    /**
     * The constructor
     */
    public SpellingPlugin() {
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

        usageDataTracker = new ServiceTracker<IUsageDataSampler, IUsageDataSampler>(
                context, IUsageDataSampler.class, null);
        usageDataTracker.open();

        // Migrate old settings
        SpellCheckerAgent.migrateUserDictFile();
        SpellCheckerRegistry.migrateUserDictDir();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
     * BundleContext )
     */
    public void stop(BundleContext context) throws Exception {
        usageDataTracker.close();
        usageDataTracker = null;

        plugin = null;
        super.stop(context);
    }

    public IUsageDataSampler getUsageDataCollector() {
        IUsageDataSampler service = usageDataTracker == null ? null
                : usageDataTracker.getService();
        return service == null ? IUsageDataSampler.NULL : service;
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static SpellingPlugin getDefault() {
        return plugin;
    }

    public static void log(Throwable e) {
        log(e, ""); //$NON-NLS-1$
    }

    public static void log(Throwable e, String message) {
        getDefault().getLog()
                .log(new Status(IStatus.ERROR, PLUGIN_ID, message, e));
    }

    public static boolean isSpellingCheckEnabled() {
        return getDefault().getPreferenceStore()
                .getBoolean(SPELLING_CHECK_ENABLED);
    }

    public static String getBundleDataPath(String subPath) {
        IPath stateLocation = getDefault().getStateLocation();
        return stateLocation.append(subPath).makeAbsolute().toOSString();
    }

}