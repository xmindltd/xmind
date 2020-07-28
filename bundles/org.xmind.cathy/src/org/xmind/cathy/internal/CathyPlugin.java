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
package org.xmind.cathy.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.xmind.core.Core;
import org.xmind.core.internal.runtime.WorkspaceConfigurer;
import org.xmind.core.internal.runtime.WorkspaceSession;
import org.xmind.core.licensing.ILicenseAgent;
import org.xmind.core.net.util.LinkUtils;
import org.xmind.core.usagedata.IUsageDataSampler;
import org.xmind.ui.internal.app.ApplicationConstants;
import org.xmind.ui.internal.statushandlers.DefaultErrorReporter;
import org.xmind.ui.internal.statushandlers.IErrorReporter;

/*---- BAD BOY HANDSOME DEBUT, DO NOT TOUCH ME ----*/
/*---- BEGIN IMPORT ----*/
/*---- END IMPORT ----*/
/*---- BAD BOY PERFECT CURTAIN CALL, DO NOT TOUCH ME ----*/

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

    /**
     * Online help page.
     */
    public static final String ONLINE_HELP_URL = LinkUtils
            .getLinkByLanguage(true, true, "/xmind/help"); //$NON-NLS-1$

    /**
     * Boolean value:<br>
     * <ul>
     * <li><code>true</code> to upload usage data to XMind server</li>
     * <li><code>false</code> to skip uploading usage data</li>
     * </ul>
     */
    public static final String USAGE_DATA_UPLOADING_ENABLED = "usageDataUploadingEnabled"; //$NON-NLS-1$

    /**
     * One minute delay between two auto save operations.
     */
    public static final int AUTO_SAVE_EDITOR_STATE_INTERVALS = 60000;
    public static final String OPTION_AUTO_SAVE_EDITOR_STATE_INTERVALS = "/debug/autoSaveEditorStateIntervals"; //$NON-NLS-1$

    public static final String KEY_NOT_SHOW_UPLOAD_DATA_CHECK = "org.xmind.cathy.notShowUploadDataCheck"; //$NON-NLS-1$

    // The shared instance.
    private static CathyPlugin plugin;

    private ServiceTracker<DebugOptions, DebugOptions> debugTracker;

    private WorkspaceSession xmindWorkspaceSession;

    private IUsageDataSampler usageDataSampler;

    private IErrorReporter errorReporter;

    private LicenseAgentProxy licenseAgent;

    private ILogger logger;

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

        /*---- BAD BOY HANDSOME DEBUT, DO NOT TOUCH ME ----*/
        /*---- BEGIN INSERT ----*/
        /*---- END INSERT ----*/
        /*---- BAD BOY PERFECT CURTAIN CALL, DO NOT TOUCH ME ----*/

        usageDataSampler = IUsageDataSampler.NULL;
        errorReporter = DefaultErrorReporter.getInstance();
        licenseAgent = new LicenseAgentProxy();

        logger = new ILogger() {

            public void logWarning(String message, Throwable error) {
                getLog().log(toStatus(IStatus.ERROR, message, error));
            }

            public void logInfo(String message, Throwable error) {
                getLog().log(toStatus(IStatus.WARNING, message, error));
            }

            public void logError(String message, Throwable error) {
                getLog().log(toStatus(IStatus.INFO, message, error));
            }

            private IStatus toStatus(int severity, String message,
                    Throwable error) {
                if (message == null) {
                    message = error.getMessage();
                }
                return new Status(severity, PLUGIN_ID, message, error);
            }
        };

//        activateNetworkSettings();

        activateXMindCore();

        /*
         * We have to manually activate 'org.xmind.ui' plugin so that command
         * handler classes contributed by that plugin can be loaded upon
         * startup. This ensures that IElementUpdater implementations and
         * enablement updates to behave correctly. For example, the class
         * org.xmind.ui.internal.handlers.ClearWorkbookHistoryHandler should be
         * loaded on startup so that its enablement state can be correctly
         * updated, otherwise the command in menu will be shown as enabled even
         * when it should not be.
         */
        activateMindMapUIContributions();

        hackConstants();

        upgradeWorkspace();

    }

//    usageDataCollector.setUploadEnabled(getPreferenceStore()
//            .getBoolean(USAGE_DATA_UPLOADING_ENABLED));

    private void hackConstants() {
        ConstantsHacker.hack();
    }

    /**
     * This method is called when the plug-in is stopped
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        if (xmindWorkspaceSession != null) {
            xmindWorkspaceSession.close();
            xmindWorkspaceSession = null;
        }

        licenseAgent = null;

        super.stop(context);
        plugin = null;
    }

    public void activateNetworkSettings() {
        Bundle networkPlugin = Platform.getBundle("org.eclipse.core.net"); //$NON-NLS-1$
        if (networkPlugin != null) {
            try {
                networkPlugin
                        .loadClass("org.eclipse.core.internal.net.Activator"); //$NON-NLS-1$
                /// cancel show AuthenticationDialog when return 401
                Authenticator.setDefault(null);
            } catch (ClassNotFoundException e) {
                getLog().log(new Status(IStatus.WARNING, PLUGIN_ID,
                        "Failed to activate plugin 'org.eclipse.core.net'.", //$NON-NLS-1$
                        e));
            }
        } else {
            getLog().log(new Status(IStatus.WARNING, PLUGIN_ID,
                    "Plugin 'org.eclipse.core.net' not found. Network proxies may not be correct.")); //$NON-NLS-1$
        }
    }

    private void activateXMindCore() throws CoreException {
        WorkspaceConfigurer.setDefaultWorkspaceLocation(
                WorkspaceConfigurer.INSTANCE_LOCATION);

        xmindWorkspaceSession = WorkspaceSession
                .openSessionIn(new File(Core.getWorkspace().getTempDir()));
    }

    private void activateMindMapUIContributions() {
        Bundle uiPlugin = Platform.getBundle("org.xmind.ui"); //$NON-NLS-1$
        if (uiPlugin != null) {
            try {
                uiPlugin.loadClass("org.xmind.ui.internal.XmindUIPlugin"); //$NON-NLS-1$
            } catch (ClassNotFoundException e) {
                getLog().log(new Status(IStatus.WARNING, PLUGIN_ID,
                        "Failed to activate plugin 'org.xmind.ui'.", //$NON-NLS-1$
                        e));
            }
        } else {
            getLog().log(new Status(IStatus.WARNING, PLUGIN_ID,
                    "Plugin 'org.xmind.ui' is not found.")); //$NON-NLS-1$
        }
    }

    /**
     * @return the logger
     */
    public ILogger getLogger() {
        return logger;
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
                .getProperty(ApplicationConstants.PRODUCT_DISTRIBITION_ID);
        if (distribId == null || "".equals(distribId)) { //$NON-NLS-1$
            distribId = "cathy_portable"; //$NON-NLS-1$
        }
        return distribId;
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

    public String getDebugValue(String option) {
        DebugOptions debugOptions = getDebugOptions();
        return debugOptions == null ? null
                : debugOptions.getOption(PLUGIN_ID + option, null);
    }

    public int getDebugValue(String option, int defaultValue) {
        DebugOptions debugOptions = getDebugOptions();
        return debugOptions == null ? defaultValue
                : debugOptions.getIntegerOption(PLUGIN_ID + option,
                        defaultValue);
    }

    public Properties loadNLSProperties(String pathBase) {
        return doLoadNLSProperties(getBundle(), pathBase);
    }

    public IUsageDataSampler getUsageDataCollector() {
        return usageDataSampler;
    }

    void setUsageDataCollector(IUsageDataSampler sampler) {
        this.usageDataSampler = sampler == null ? IUsageDataSampler.NULL
                : sampler;
    }

    /**
     * @return
     */
    public IErrorReporter getErrorReporter() {
        return errorReporter;
    }

    void setErrorReporter(IErrorReporter reporter) {
        this.errorReporter = reporter == null
                ? DefaultErrorReporter.getInstance() : reporter;
    }

    public ILicenseAgent getLicenseAgent() {
        return licenseAgent;
    }

    void setLicenseAgent(ILicenseAgent agent) {
        this.licenseAgent.setDelegate(agent);
    }

    private void upgradeWorkspace() {
        IPath stateLocation;
        try {
            stateLocation = Platform.getStateLocation(getBundle());
        } catch (Exception e) {
            return;
        }
        String versionFileName = String.format("workspace-upgraded-v%s", //$NON-NLS-1$
                getBundle().getVersion().toString());
        File versionFile = stateLocation.append(versionFileName).toFile();
        if (versionFile.exists())
            return;

        /// delete old workbench state on first launch
        File workbenchFile = stateLocation.removeLastSegments(1)
                .append("org.eclipse.e4.workbench") //$NON-NLS-1$
                .append("workbench.xmi") //$NON-NLS-1$
                .toFile();
        if (workbenchFile.exists()) {
            workbenchFile.delete();
        }

        try {
            File parentDir = versionFile.getParentFile();
            if (parentDir != null)
                parentDir.mkdirs();
            OutputStream output = new FileOutputStream(versionFile);
            try {
                output.write("done".getBytes()); //$NON-NLS-1$
            } finally {
                output.close();
            }
        } catch (IOException e) {
        }
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
        getDefault().getLog()
                .log(new Status(IStatus.ERROR, PLUGIN_ID, message, e));
    }

    public static void log(String message) {
        if (message == null)
            message = ""; //$NON-NLS-1$
        getDefault().getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message));
    }

    private static Properties doLoadNLSProperties(Bundle bundle,
            String pathBase) {
        Properties properties = new Properties();

        String nl = Platform.getNL();
        String[] nlSegments = nl.split("_"); //$NON-NLS-1$
        URL entry;
        for (int i = 0; i <= nlSegments.length; i++) {
            StringBuilder nlsName = new StringBuilder(pathBase);
            for (int j = 0; j < i; j++) {
                nlsName.append('_');
                nlsName.append(nlSegments[j]);
            }
            nlsName.append(".properties"); //$NON-NLS-1$
            entry = findSingleEntry(bundle, nlsName.toString());
            if (entry != null) {
                try {
                    InputStream stream = entry.openStream();
                    try {
                        properties.load(stream);
                    } finally {
                        stream.close();
                    }
                } catch (IOException e) {
                    // ignore
                }
            }
        }

        return properties;
    }

    private static URL findSingleEntry(Bundle bundle, String path) {
        int sepIndex = path.lastIndexOf('/');
        String dir, name;
        if (sepIndex < 0) {
            dir = "/"; //$NON-NLS-1$
            name = path;
        } else {
            dir = path.substring(0, sepIndex);
            name = path.substring(sepIndex + 1);
        }
        Enumeration<URL> entries = bundle.findEntries(dir, name, true);
        return entries != null && entries.hasMoreElements()
                ? entries.nextElement() : null;
    }

    public static <T> T getAdapter(Object obj, Class<T> adapter) {
        if (adapter.isInstance(obj))
            return adapter.cast(obj);

        if (obj instanceof IAdaptable) {
            T result = ((IAdaptable) obj).getAdapter(adapter);
            if (result != null)
                return result;
        }

        if (!(obj instanceof PlatformObject))
            return Platform.getAdapterManager().getAdapter(obj, adapter);

        return null;
    }

}
