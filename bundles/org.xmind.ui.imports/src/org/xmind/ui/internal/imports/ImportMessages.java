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
package org.xmind.ui.internal.imports;

import org.eclipse.osgi.util.NLS;

public class ImportMessages extends NLS {

    private static final String BUNDLE_NAME = "org.xmind.ui.internal.imports.messages"; //$NON-NLS-1$

    public static String Importer_CentralTopic;
    public static String Importer_MainTopic;
    public static String Importer_FloatingTopic;
    public static String Importer_Subtopic;
    public static String Importer_Summary;

    public static String MindManagerImportWizard_windowTitle;

    public static String MindManagerImportPage_title;
    public static String MindManagerImportPage_description;
    public static String MindManagerImportPage_FilterName;

    public static String MindManagerImporter_ReadingContent;
    public static String MindManagerImporter_ReadingElements;
    public static String MindManagerImporter_ArrangingStyles;
    public static String MindManagerImporter_GeneratingTheme;
    public static String MindManagerImporter_ResourceLabel;
    public static String MindManagerImporter_DurationLabel;
    public static String MindManagerImporter_Months;
    public static String MindManagerImporter_Weeks;
    public static String MindManagerImporter_Days;
    public static String MindManagerImporter_Hours;

    public static String FreeMindImportWizard_windowTitle;
    public static String FreeMindImportPage_title;
    public static String FreeMindImportPage_description;
    public static String FreeMindImportPage_FilterName;

    public static String OpmlImportWizard_windowTitle;
    public static String OpmlImportPage_title;
    public static String OpmlImportPage_description;
    public static String OpmlImportPage_FilterName;

    public static String WorkbookImportPage_FilterName;

    public static String WorkbookImportPage_title;

    public static String WorkbookImportWizard_windowTitle;
    public static String WorkbookImportPage_description;
    public static String WorkbookImportPage_NoTargetWorkbookWarning;

    public static String LightenImportWizard_windowTitle;
    public static String LightenImportPage_title;
    public static String LightenImportPage_description;
    public static String LightenImportPage_FilterName;

    public static String NovaMindImporter_Duration_Years_label;
    public static String NovaMindImporter_Duration_Months_label;
    public static String NovaMindImporter_Duration_Weeks_label;
    public static String NovaMindImporter_Duration_Hours_label;
    public static String NovaMindImporter_Duration_Minutes_label;
    public static String NovaMindImporter_Duration_Days_label;
    public static String NovaMindImporter_Duration_label;
    public static String NovaMindImporter_Resource_label;

    public static String NovaMindImportPage_title;
    public static String NovaMindImportPage_FileDialog_Filter_name;
    public static String NovaMindImportWizard_windowTitle;

    static {
        NLS.initializeMessages(BUNDLE_NAME, ImportMessages.class);
    }

    private ImportMessages() {
    }

}
