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
package org.xmind.ui.internal.statushandlers;

import org.eclipse.osgi.util.NLS;

public class StatusHandlerMessages extends NLS {
    private static final String BUNDLE_NAME = "org.xmind.ui.internal.statushandlers.messages"; //$NON-NLS-1$
    public static String RuntimeErrorDialog_CloseButton_Text;
    public static String RuntimeErrorDialog_ReportHyperlink_Text;
    public static String RuntimeErrorDialog_windowTitle;
    public static String StatusDetails_ErrorMessage_with_RootCauseClassName_and_RootCauseMessage;
    public static String StatusDetails_SimpleErrorMessage;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, StatusHandlerMessages.class);
    }

    private StatusHandlerMessages() {
    }
}
