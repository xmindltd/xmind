/* ******************************************************************************
 * Copyright (c) 2006-2013 XMind Ltd. and others.
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
package org.freehep.graphicsio;

import org.eclipse.osgi.util.NLS;

/**
 * @author Jason Wong
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.freehep.graphicsio.messages"; //$NON-NLS-1$

    public static String PDFMapExportWizard_Orientation_Landscape;
    public static String PDFMapExportWizard_Orientation_Portrait;

    public static String PDFMapExportWizard_PageSize_Letter;
    public static String PDFMapExportWizard_PageSize_Note;
    public static String PDFMapExportWizard_PageSize_Legal;
    public static String PDFMapExportWizard_PageSize_Tabloid;
    public static String PDFMapExportWizard_PageSize_Executive;
    public static String PDFMapExportWizard_PageSize_Postcard;

    public static String PDFMapExportWizard_PageMargin_Small;
    public static String PDFMapExportWizard_PageMargin_Medium;
    public static String PDFMapExportWizard_PageMargin_Large;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }

}
