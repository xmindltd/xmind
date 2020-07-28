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

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.xmind.ui.internal.spelling.messages"; //$NON-NLS-1$

    public static String addToDictionary;
    public static String noSpellSuggestion;
    public static String enableSpellCheck;
    public static String options;
    public static String SpellingPrefPage_title;
    public static String dictionaries;
    public static String dictionaries_add;
    public static String dictionaries_remove;
    public static String dictionaries_remove_confirm_title;
    public static String dictionaries_remove_confirm_message;
    public static String addingDictionary;
    public static String removingDictionary;
    public static String defaultDictionary;
    public static String detailsLink_text;

    public static String ignoreAllCapital;
    public static String ignoreMultiCapital;
    public static String ignoreWebAddress;
    public static String ignoreNumberousAppendix;
    public static String ignoreFirstLowercaseSentences;

    public static String loadingSpellChecker;
    public static String creatingSpellCheckerInstance;
    public static String addingSystemDictionary;
    public static String addingUserDictionary;
    public static String initializingSpellingSettings;
    public static String notifyingSpellingVisitors;

    public static String spellCheckProgress_Text;

    public static String importDict_Text;
    public static String importDictLabel_Text;

    public static String SpellingCheckView_button_ScanAllWorkbooks;
    public static String SpellingCheckView_button_ScanWorkbook;
    public static String SpellingCheckView_column_Word;
    public static String SpellingCheckView_column_Suggestions;
    public static String SpellingCheckView_Scanning;
    public static String SpellingCheckView_dialogTitle;
    public static String SpellingCheckView_NoEditors_message;
    public static String SpellingCheckView_NoProviders_message;
    public static String SpellingCheckView_ScanCompleted_message;
    public static String SpellingCheckView_ScanCompleted_link;
    public static String SpellingCheckView_NoSpellingError_text;

    public static String CheckSpellingJob_name;
    public static String CheckSpellingJob_task_Scanning;

    public static String SpellingCheckDialog_title;
    public static String SpellingCheckDialog_description;
    public static String SpellingCheckDialog_Result_NoDictionary_label;
    public static String SpellingCheckDialog_Result_Ignore_button;
    public static String SpellingCheckDialog_Result_IgnoreAll_button;
    public static String SpellingCheckDialog_Result_AddToDictionary_button;
    public static String SpellingCheckDialog_Result_Suggestions_label;
    public static String SpellingCheckDialog_Result_Change_button;
    public static String SpellingCheckDialog_Result_ChangeAll_button;
    public static String SpellingCheckDialog_ButtonBar_Options_button;
    public static String SpellingCheckDialog_Result_NoError_label;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }

}
