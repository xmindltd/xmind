/* ******************************************************************************
 * Copyright (c) 2006-2013 XMind Ltd. and others.
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
package org.xmind.ui.internal.exports.vector.svg;

import org.eclipse.osgi.util.NLS;

/**
 * @author Jason Wong
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.xmind.ui.internal.exports.vector.svg.messages"; //$NON-NLS-1$

    public static String SVGWizard_WindowTitle;
    public static String SVGWizard_FormatName;
    public static String SVGPage_Title;
    public static String SVGPage_Description;
    public static String SVGPage_FilterName;
    public static String SVGExportJob_Name;

    public static String SVGExportWizard_showMinusCheck_text;

    public static String SVGExportWizard_showPlusCheck_text;

    public static String ExportPage_Launching;

    public static String ExportWizard_Collapse_Expand_text;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
