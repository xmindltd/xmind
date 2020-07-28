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

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.xmind.core.net.util.LinkUtils;

public class BetaVerifier {

    /**
     * The timestamp when the beta will expire. Beta releases will change this
     * value to a valid timestamp.
     */
    // Note: Not defined as 'final' to prevent compiling warning
    // at expression 'BETA_EXPIRE_TIME > 0'.
    private static long BETA_EXPIRY_TIME = 0L;

    private static long LAUNCH_TIME = System.currentTimeMillis();

    private final Display display;

    private final String brandingVersion;

    private final String buildId;

    public BetaVerifier(Display display) {
        this.display = display;
        this.brandingVersion = System
                .getProperty(CathyApplication.SYS_BRANDING_VERSION, ""); //$NON-NLS-1$
        this.buildId = System.getProperty(CathyApplication.SYS_BUILDID,
                "X.x.x"); //$NON-NLS-1$
    }

    public boolean shouldExitAfterBetaExpired() {
        if (isBeta() && isBetaExpired()) {
            promptBetaExpiry();
            return true;
        } else if (isBeta()) {
            String licenseRestrictions = System
                    .getProperty("org.xmind.product.license.restrictions"); //$NON-NLS-1$
            if (licenseRestrictions == null || "".equals(licenseRestrictions)) { //$NON-NLS-1$
                licenseRestrictions = NLS.bind(
                        WorkbenchMessages.About_BetaExpiryMessage_withExpiryTime,
                        new SimpleDateFormat("MMM d, yyyy") //$NON-NLS-1$
                                .format(new Date(BETA_EXPIRY_TIME)));
                System.setProperty("org.xmind.product.license.restrictions", //$NON-NLS-1$
                        licenseRestrictions);
            }
        }
        return false;
    }

    private int openMessageDialog(String message, int dialogType,
            String[] buttonLabels) {
        URL titleIconURL = Platform.getBundle(CathyPlugin.PLUGIN_ID)
                .getResource("icons/xmind.16.png"); //$NON-NLS-1$
        Image titleIcon = null;
        try {
            titleIcon = new Image(display, titleIconURL.openStream());
        } catch (IOException e) {
        }

        try {
            MessageDialog dialog = new MessageDialog(null,
                    WorkbenchMessages.BetaVerifier_BetaExpiredPromptDialog_windowTitle,
                    titleIcon, message, dialogType, buttonLabels, 0);
            return dialog.open();
        } finally {
            if (titleIcon != null)
                titleIcon.dispose();
        }

    }

    private void promptBetaExpiry() {
        int selection = openMessageDialog(
                NLS.bind(
                        WorkbenchMessages.BetaVerifier_BetaExpiredPromptDialog_message_withBrandingVersion_andBuildId,
                        brandingVersion, buildId),
                MessageDialog.INFORMATION,
                new String[] {
                        WorkbenchMessages.BetaVerifier_BetaExpiredPromptDialog_CheckAndInstallButton_text,
                        WorkbenchMessages.BetaVerifier_BetaExpiredPromptDialog_ExitButton_text });
        if (selection == 0) {
            openDownloadSite();
        }
    }

    private void openDownloadSite() {
        Program.launch(
                LinkUtils.getLinkByLanguage(true, true, "/xmind/beta-expired/") //$NON-NLS-1$
                        + buildId.replace("qualifier", "000000000000")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static boolean isBeta() {
        return BETA_EXPIRY_TIME > 0;
    }

    public static boolean isBetaExpired() {
        return LAUNCH_TIME > BETA_EXPIRY_TIME;
    }

}
