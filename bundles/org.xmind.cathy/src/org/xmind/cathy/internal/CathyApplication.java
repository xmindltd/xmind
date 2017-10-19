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

import java.awt.Toolkit;
import java.io.File;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.xmind.core.Core;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.core.internal.dom.DOMConstants;
import org.xmind.core.usagedata.IUsageDataSampler;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.app.IApplicationValidator;
import org.xmind.ui.prefs.PrefConstants;

/**
 * This class controls all aspects of the application's execution
 */
public class CathyApplication implements IApplication {

    public static final String SYS_VERSION = "org.xmind.product.version"; //$NON-NLS-1$

    public static final String SYS_BUILDID = "org.xmind.product.buildid"; //$NON-NLS-1$

    public static final String SYS_BRANDING_VERSION = "org.xmind.product.brandingVersion"; //$NON-NLS-1$

    public static final String SYS_APP_STATUS = "org.xmind.cathy.app.status"; //$NON-NLS-1$

    /**
     * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
     */
    public Object start(IApplicationContext context) throws Exception {
        // Check product information.
        // Product build id and version may have been set in config.ini.
        // If not set, calculate them using runtime properties.
        String buildId = System.getProperty(SYS_BUILDID);
        if (buildId == null || "".equals(buildId)) { //$NON-NLS-1$
            buildId = calculateBuildId(context);
            System.setProperty(SYS_BUILDID, buildId);
        }
        String appVersion = System.getProperty(SYS_VERSION);
        if (appVersion == null || "".equals(appVersion)) { //$NON-NLS-1$
            appVersion = extractVersionNumber(buildId);
            System.setProperty(SYS_VERSION, appVersion);
        }

        System.setProperty("org.xmind.product.about.copyright", //$NON-NLS-1$
                WorkbenchMessages.About_Copyright);
        System.setProperty("org.xmind.product.about.homepage", //$NON-NLS-1$
                WorkbenchMessages.About_Homepage);

        IPreferenceStore pref = MindMapUIPlugin.getDefault()
                .getPreferenceStore();

        String name = pref.getString(PrefConstants.AUTHOR_INFO_NAME);

        if (name == null || "".equals(name)) //$NON-NLS-1$
            name = System.getProperty("user.name"); //$NON-NLS-1$
        if (name != null)
            System.setProperty(DOMConstants.AUTHOR_NAME, name);

        if (pref.getString(PrefConstants.AUTHOR_INFO_EMAIL) != null)
            System.setProperty(DOMConstants.AUTHOR_EMAIL,
                    pref.getString(PrefConstants.AUTHOR_INFO_EMAIL));

        if (pref.getString(PrefConstants.AUTHOR_INFO_ORG) != null)
            System.setProperty(DOMConstants.AUTHOR_ORG,
                    pref.getString(PrefConstants.AUTHOR_INFO_ORG));

        // Set Cathy product as the workbook creator.
        Core.getWorkbookBuilder().setCreator("XMind", buildId); //$NON-NLS-1$

        // Create the default display instance.
        Display display = PlatformUI.createDisplay();

        try {
            // Install global OpenDocument listener:
            OpenDocumentQueue.getInstance().hook(display);

            /// Activate network proxy settings. On Linux, we need a 
            /// UI environment to show a dialog for retrieving the master
            /// password of the secure storage containing proxy server
            /// credentials.
            CathyPlugin.getDefault().activateNetworkSettings();

            // Check if we are in beta and should quit due to beta expiry.
            if (new BetaVerifier(display).shouldExitAfterBetaExpired())
                return EXIT_OK;

            // Check if this app session should exit early:
            if (shouldExitEarly(context)) {
                // Log all application arguments to local disk to exchange
                // between running XMind instances:
                logApplicationArgs();
                return EXIT_OK;
            }

            // Log all application arguments to local disk to exchange
            // between running XMind instances:
            logApplicationArgs();

            // Mark application status to 'starting':
            System.setProperty(SYS_APP_STATUS, "starting"); //$NON-NLS-1$

            // Set cookies to let web pages loaded within internal web browser
            // to recognize the environment:
            initializeInternalBrowserCookies();

            //Close model's auto-save to avoid repeated model elements 
            //which application exits non-normally:
            WorkbenchPlugin.getDefault().getPreferenceStore()
                    .setValue(IPreferenceConstants.WORKBENCH_SAVE_INTERVAL, 0);

            try {
                captureAppSessionInfo(
                        CathyPlugin.getDefault().getUsageDataCollector(),
                        context, buildId);

                // Launch workbench and get return code:
                int returnCode = PlatformUI.createAndRunWorkbench(display,
                        new CathyWorkbenchAdvisor());

                if (returnCode == PlatformUI.RETURN_RESTART) {
                    // Restart:
                    return EXIT_RESTART;
                }

                // Quit:
                return EXIT_OK;
            } finally {
                CathyPlugin.getDefault().getUsageDataCollector().put(
                        UserDataConstants.SHUT_DOWN_TIME,
                        System.currentTimeMillis());
            }
        } finally {
            display.dispose();
        }
    }

    /**
     * @param sampler
     * @param context
     * @param buildId
     */
    private void captureAppSessionInfo(IUsageDataSampler sampler,
            IApplicationContext context, String buildId) {
        sampler.put(UserDataConstants.START_UP_TIME,
                System.currentTimeMillis());
        sampler.put(UserDataConstants.APP_ID, context.getBrandingApplication());
        sampler.put(UserDataConstants.BUILD_ID, buildId);
        sampler.put(UserDataConstants.DISTRIBUTION_ID,
                System.getProperty("org.xmind.product.distribution.id", //$NON-NLS-1$
                        null));
        sampler.put(UserDataConstants.NL, Platform.getNL());
        sampler.put(UserDataConstants.OS, Platform.getOS());
        sampler.put(UserDataConstants.ARCH, Platform.getOSArch());
        sampler.put(UserDataConstants.OS_NAME,
                System.getProperty("os.name", null));  //$NON-NLS-1$
        sampler.put(UserDataConstants.OS_VERSION,
                System.getProperty("os.version", null));   //$NON-NLS-1$
        sampler.put(UserDataConstants.COUNTRY,
                System.getProperty("user.country", null));   //$NON-NLS-1$
        sampler.put(UserDataConstants.JAVA_VERSION,
                System.getProperty("java.version", null));  //$NON-NLS-1$
        sampler.put(UserDataConstants.JAVA_VENDOR,
                System.getProperty("java.vendor", null));  //$NON-NLS-1$
        sampler.put(UserDataConstants.SCREEN_WIDTH,
                Toolkit.getDefaultToolkit().getScreenSize().width);
        sampler.put(UserDataConstants.SCREEN_HEIGHT,
                Toolkit.getDefaultToolkit().getScreenSize().height);
        sampler.put(UserDataConstants.SCREEN_RESOLUTION,
                Toolkit.getDefaultToolkit().getScreenResolution());

    }

    private static String calculateBuildId(IApplicationContext context) {
        String buildId = System.getProperty("eclipse.buildId"); //$NON-NLS-1$
        if (buildId != null && !"".equals(buildId)) //$NON-NLS-1$
            return buildId;
        return context.getBrandingBundle().getVersion().toString();
    }

    private static String extractVersionNumber(String buildId) {
        String[] numbers = buildId.split("\\."); //$NON-NLS-1$
        StringBuilder buffer = new StringBuilder(10);
        for (int i = 0; i < 3; i++) {
            if (i >= numbers.length)
                break;
            if (buffer.length() > 0) {
                buffer.append('.');
            }
            buffer.append(numbers[i]);
        }
        return buffer.toString();
    }

    private void initializeInternalBrowserCookies() {
        String appVersion = System.getProperty(SYS_VERSION);
        Browser.setCookie(
                "_env=xmind_" + appVersion //$NON-NLS-1$
                        + "; path=/; domain=.xmind.net", //$NON-NLS-1$
                "http://www.xmind.net/"); //$NON-NLS-1$
    }

    private void logApplicationArgs() {
        final String[] args = Platform.getApplicationArgs();
        if (args == null || args.length == 0)
            return;

        Log openingLog = Log.get(Log.OPENING);
        for (String arg : args) {
            if ("-p".equals(arg)) {//$NON-NLS-1$
                // The "-p" argument is used to start Presentation Mode
                // immediately on startup:
                System.setProperty("org.xmind.cathy.startup.presentation", //$NON-NLS-1$
                        "true"); //$NON-NLS-1$
            } else if (arg.startsWith("xmind:") || new File(arg).exists()) { //$NON-NLS-1$
                // Add xmind command or existing file path to '.opening' log:
                openingLog.append(arg);
            } else if (!arg.startsWith("-psn_0_")) { //$NON-NLS-1$
                // The "-psn_0_<ProcessSerialNumber>" argument is passed in by
                // Mac OS X for each GUI application. No need to log that.
                // Log any other unknown command line argument for debugging:
                CathyPlugin.log("Skip unrecognized command line argument: '" //$NON-NLS-1$
                        + arg + "'"); //$NON-NLS-1$
            }
        }
    }

    private boolean shouldExitEarly(IApplicationContext appContext)
            throws Exception {
        IExtensionPoint extPoint = Platform.getExtensionRegistry()
                .getExtensionPoint(
                        "org.xmind.ui.toolkit.applicationValidators"); //$NON-NLS-1$
        if (extPoint != null) {
            for (IExtension ext : extPoint.getExtensions()) {
                for (IConfigurationElement validatorElement : ext
                        .getConfigurationElements()) {
                    if ("applicationValidator" //$NON-NLS-1$
                            .equals(validatorElement.getName())) {
                        Object validator = validatorElement
                                .createExecutableExtension(
                                        IWorkbenchRegistryConstants.ATT_CLASS);
                        if (validator instanceof IApplicationValidator
                                && ((IApplicationValidator) validator)
                                        .shouldApplicationExitEarly(
                                                appContext)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * @see org.eclipse.equinox.app.IApplication#stop()
     */
    public void stop() {
        if (!PlatformUI.isWorkbenchRunning())
            return;

        final IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null)
            return;

        Display display = workbench.getDisplay();
        if (display == null || display.isDisposed())
            return;

        display.syncExec(new Runnable() {
            public void run() {
                workbench.close();
            }
        });
    }

}
