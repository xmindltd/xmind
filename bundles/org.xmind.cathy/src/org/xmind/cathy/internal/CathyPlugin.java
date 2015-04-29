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
package org.xmind.cathy.internal;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.xmind.core.Core;
import org.xmind.core.command.ICommandService;
import org.xmind.core.internal.runtime.WorkspaceConfigurer;
import org.xmind.core.internal.runtime.WorkspaceSession;

/**
 * The main plugin class to be used in the desktop.
 */
public class CathyPlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.xmind.cathy"; //$NON-NLS-1$

    /**
     * Boolean value:<br>
     * <ul>
     * <li><code>true</code> to enable auto saving service when there's opened
     * workbooks</li>
     * <li><code>false</code> to disable this service</li>
     * </ul>
     */
    public static final String AUTO_SAVE_ENABLED = "autoSaveEnabled"; //$NON-NLS-1$

    /**
     * Integer value:<br>
     * the intervals (in minutes) between auto saving actions
     */
    public static final String AUTO_SAVE_INTERVALS = "autoSaveIntervals"; //$NON-NLS-1$

    /**
     * (Deprecated, use {@link #STARTUP_ACTION} instead) Boolean value:<br>
     * <ul>
     * <li><code>true</code> to remember unclosed workbooks when XMind quits and
     * open them next time XMind starts</li>
     * <li><code>false</code> to always open a bootstrap workbook when XMind
     * opens</li>
     * </ul>
     */
    public static final String RESTORE_LAST_SESSION = "restoreLastSession"; //$NON-NLS-1$

    /**
     * Boolean value:<br>
     * <ul>
     * <li><code>true</code> to check updates when XMind starts</li>
     * <li><code>false</code> to skip update checking when XMind starts</li>
     * </ul>
     */
    public static final String CHECK_UPDATES_ON_STARTUP = "checkUpdatesOnStartup"; //$NON-NLS-1$

    /**
     * Integer value (enumerated):<br>
     * <ul>
     * <li><code>0</code>({@link #STARTUP_ACTION_WIZARD}): opens a 'New
     * Workbook' wizard dialog on startup</li>
     * <li><code>1</code>({@link #STARTUP_ACTION_BLANK}): opens a blank map on
     * startup</li>
     * <li><code>2</code>({@link #STARTUP_ACTION_HOME}): opens the home map on
     * startup</li>
     * <li><code>3</code>({@link #STARTUP_ACTION_LAST}): opens last session on
     * startup</li>
     * </ul>
     */
    public static final String STARTUP_ACTION = "startupAction2"; //$NON-NLS-1$

    /**
     * Integer preference store value for opening a 'New Workbook' wizard dialog
     * on startup. (value=0)
     * 
     * @see #STARTUP_ACTION
     */
    public static final int STARTUP_ACTION_WIZARD = 0;

    /**
     * Integer preference store value for opening a blank map on startup.
     * (value=1)
     * 
     * @see #STARTUP_ACTION
     */
    public static final int STARTUP_ACTION_BLANK = 1;

    /**
     * Integer preference store value for opening the home map on startup.
     * (value=2)
     * 
     * @see #STARTUP_ACTION
     */
    public static final int STARTUP_ACTION_HOME = 2;

    /**
     * Integer preference store value for opening last session on startup.
     * (value=3)
     * 
     * @see #STARTUP_ACTION
     */
    public static final int STARTUP_ACTION_LAST = 3;

    /**
     * Boolean value:<br>
     * <ul>
     * <li><code>true</code> to hide system notifications (usually pushed to the
     * user by pop-up windows)</li>
     * <li><code>false</code> to show system notifications</li>
     * </ul>
     */
    //public static final String HIDE_NOTIFICATIONS = "hideNotifications"; //$NON-NLS-1$

    /**
     * String constants identifying the extension part of a XMind command file
     * name.
     */
    public static final String COMMAND_FILE_EXT = ".xmind-command"; //$NON-NLS-1$

    // The shared instance.
    private static CathyPlugin plugin;

    private ServiceTracker<ICommandService, ICommandService> commandServiceTracker = null;

    private ServiceTracker<DebugOptions, DebugOptions> debugTracker = null;

    private WorkspaceSession xmindWorkspaceSession = null;

    /**
     * The constructor.
     */
    public CathyPlugin() {
        plugin = this;
    }

    /**
     * This method is called upon plug-in activation
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);

        activateNetworkSettings();

        activateXMindCore();
    }

    /**
     * This method is called when the plug-in is stopped
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        if (commandServiceTracker != null) {
            commandServiceTracker.close();
            commandServiceTracker = null;
        }

        if (xmindWorkspaceSession != null) {
            xmindWorkspaceSession.close();
            xmindWorkspaceSession = null;
        }

        super.stop(context);
        plugin = null;
    }

    private void activateNetworkSettings() {
        Bundle networkPlugin = Platform.getBundle("org.eclipse.core.net"); //$NON-NLS-1$
        if (networkPlugin != null) {
            try {
                networkPlugin
                        .loadClass("org.eclipse.core.internal.net.Activator"); //$NON-NLS-1$
            } catch (ClassNotFoundException e) {
                getLog().log(
                        new Status(
                                IStatus.WARNING,
                                PLUGIN_ID,
                                "Failed to activate plugin 'org.eclipse.core.net'.", //$NON-NLS-1$
                                e));
            }
        } else {
            getLog().log(
                    new Status(IStatus.WARNING, PLUGIN_ID,
                            "Plugin 'org.eclipse.core.net' not found. Network proxies may not be correct.")); //$NON-NLS-1$
        }
    }

    private void activateXMindCore() throws CoreException {
        WorkspaceConfigurer
                .setDefaultWorkspaceLocation(WorkspaceConfigurer.INSTANCE_LOCATION);

        xmindWorkspaceSession = WorkspaceSession.openSessionIn(new File(Core
                .getWorkspace().getTempDir()));
    }

    /**
     * Returns the distribution identifier of this XMind product.
     * 
     * @return the distribution identifier of this XMind product
     * @deprecated Use system property
     *             <code>"org.xmind.product.distribution.id"</code>
     */
    public static String getDistributionId() {
        String distribId = System
                .getProperty("org.xmind.product.distribution.id"); //$NON-NLS-1$
        if (distribId == null || "".equals(distribId)) { //$NON-NLS-1$
            distribId = "cathy_portable"; //$NON-NLS-1$
        }
        return distribId;
    }

    public synchronized ICommandService getCommandService() {
        if (commandServiceTracker == null) {
            commandServiceTracker = new ServiceTracker<ICommandService, ICommandService>(
                    getBundle().getBundleContext(),
                    ICommandService.class.getName(), null);
            commandServiceTracker.open();
        }
        return commandServiceTracker.getService();
    }

    /**
     * Returns the shared instance.
     * 
     * @return the shared instance.
     */
    public static CathyPlugin getDefault() {
        return plugin;
    }

    public static void log(Throwable e, String message) {
        if (message == null)
            message = ""; //$NON-NLS-1$
        getDefault().getLog().log(
                new Status(IStatus.ERROR, PLUGIN_ID, message, e));
    }

    public static void log(String message) {
        if (message == null)
            message = ""; //$NON-NLS-1$
        getDefault().getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message));
    }

    public DebugOptions getDebugOptions() {
        if (debugTracker == null) {
            debugTracker = new ServiceTracker<DebugOptions, DebugOptions>(
                    getBundle().getBundleContext(),
                    DebugOptions.class.getName(), null);
            debugTracker.open();
        }
        return debugTracker.getService();
    }

    public boolean isDebugging(String option) {
        DebugOptions debugOptions = getDebugOptions();
        return debugOptions != null && debugOptions.isDebugEnabled()
                && debugOptions.getBooleanOption(PLUGIN_ID + option, false);
    }

}