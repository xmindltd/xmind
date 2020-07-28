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
package org.xmind.ui.internal.prefs;

/**
 * @deprecated See {@link org.xmind.ui.prefs.PrefConstants}
 * @author Brian Sun
 */
public class PreferenceConstants {

    public static final String WELCOME_ON_STARTUP = "WELCOME_ON_STARTUP"; // [New] //$NON-NLS-1$

    public static final String RECENT_OPENED_FILES = "RECENT_OPENED_FILES"; //$NON-NLS-1$
    public static final String SHOW_RECENT_OPENED_FILES = "SHOW_RECENT_OPENED_FILES"; //$NON-NLS-1$
    public static final String NUM_RECENT_OPENED_FILES = "NUM_RECENT_OPENED_FILES"; // BrainyWorkspace.recentCount //$NON-NLS-1$

    public static final String RECENTLY_USED_TEMPLATES = "RECENTLY_USED_TEMPLATES"; //$NON-NLS-1$
    public static final String SHOW_RECENTLY_USED_TEMPLATES = "SHOW_RECENTLY_USED_TEMPLATES"; //$NON-NLS-1$
    public static final String NUM_RECENTLY_USED_TEMPLATES = "NUM_RECENETLY_USED_TEMPLATES"; //$NON-NLS-1$

    //    public static final String AUTO_UPDATE = "AUTO_UPDATE";

    public static final String SHOW_ANIMATION = "SHOW_ANIMATION"; // BrainyUI.isAnimated() //$NON-NLS-1$
    public static final String SHOW_INSERT_PROMPT = "SHOW_INSERT_PROMPT"; // [New] //$NON-NLS-1$

    public static final String UNDO_LIMIT = "UNDO_LIMIT"; // CommandStackBase.DEFAULT_UNDO_LIMIT //$NON-NLS-1$

    public static final String COPY_IMAGE = "COPY_IMAGE"; // BrainyUI.IS_COPY_IMAGE //$NON-NLS-1$

    public static final String COPY_NOTES = "COPY_NOTES"; // [New] //$NON-NLS-1$

    public static final String COPY_2D_CHART = "COPY_2D_CHART"; //$NON-NLS-1$

    public static final String AUTO_NUMBERING = "AUTO_NUMBERING"; // [New][Style] //$NON-NLS-1$

//    public static final String NOTES_FONT         = "NOTES_FONT";        // [New] //$NON-NLS-1$

    public static final String AUTO_SAVE = "AUTO_SAVE"; // BrainyWorkspace.autoSaveMin //$NON-NLS-1$

    public static final String AUTO_SAVE_INTERVAL = "AUTO_SAVE_INTERVAL"; // BrainyWorkspace.autoSaveMin //$NON-NLS-1$

    public static final String ENABLE_OVERLAPS = "ENABLE_OVERLAPS"; //$NON-NLS-1$

    public static final String ENABLE_FREE_POSITION = "ENABLE_FREE_POSITION"; //$NON-NLS-1$

    public static final String MAP_SHOT_DESTINATION = "MAP_SHOT_DESTINATION"; //$NON-NLS-1$

    public static final String RESTORE_SESSION = "REMEMBER_CURRENT_SESSION"; //$NON-NLS-1$

    public static final String PRESENTATION_BUTTONS_ORIENTATION = "PRESENTATION_BUTTONS_ORIENTATION"; //$NON-NLS-1$

    public static final String PRESENTATION_SHOW_TIPS = "PRESENTATION_SHOW_TIPS"; //$NON-NLS-1$

    public static final String MAP_SHOT_CLIPBOARD = "org.xmind.ui.mapshot.dest.clipboard"; //$NON-NLS-1$
    public static final String MAP_SHOT_DESKTOP = "org.xmind.ui.mapshot.dest.desktop"; //$NON-NLS-1$
    public static final String MAP_SHOT_ALL = "org.xmin.ui.mapshot.dest.all"; //$NON-NLS-1$

//    public static final String PAGE_MARGIN_TOP = "PAGE_MARGIN_TOP"; //$NON-NLS-1$
//    public static final String PAGE_MARGIN_LEFT = "PAGE_MARGIN_LEFT"; //$NON-NLS-1$
//    public static final String PAGE_MARGIN_RIGHT = "PAGE_MARGIN_RIGHT"; //$NON-NLS-1$
//    public static final String PAGE_MARGIN_BOTTOM = "PAGE_MARGIN_BOTTOM"; //$NON-NLS-1$

    /*
     * ==== Keys For Page Settings ====
     */
    public static final String PAGE_MARGINS = "PAGE_MARGINS"; //$NON-NLS-1$
    public static final String PAGE_UNIT = "PAGE_UNIT"; //$NON-NLS-1$ // Geometry.UNIT_INCH (default), Geometry.UNIT_MM

    public static final String PAGE_HEADER_TEXT = "PAGE_HEADER_TEXT"; //$NON-NLS-1$
    public static final String PAGE_HEADER_FONT = "PAGE_HEADER_FONT"; //$NON-NLS-1$
    public static final String PAGE_HEADER_ALIGNMENT = "PAGE_HEADER_ALIGNMENT"; //$NON-NLS-1$

    public static final String PAGE_FOOTER_TEXT = "PAGE_FOOTER_TEXT"; //$NON-NLS-1$
    public static final String PAGE_FOOTER_FONT = "PAGE_FOOTER_FONT"; //$NON-NLS-1$
    public static final String PAGE_FOOTER_ALIGNMENT = "PAGE_FOOTER_ALIGNMENT"; //$NON-NLS-1$

    public static final String PAGE_PORTRAIT = "PAGE_PORTRAIT"; // true: portrait; false: landscape //$NON-NLS-1$

    public static final String DEF_PAGE_HEADER = ""; //$NON-NLS-1$
    //public static final String DEF_PAGE_FOOTER = Messages.PreferenceInitializer_DefaultFooterText;

//    public static final String INCLUDE_BORDER = "INCLUDE_BORDER"; //$NON-NLS-1$
//    public static final String INCLUDE_BACKGROUND = "INCLUDE_BACKGROUND"; //$NON-NLS-1$

    /*
     * ==== ====
     */
    public static final String IMPORT_TO = "IMPORT_TO"; //$NON-NLS-1$
    public static final String KEEP_SCALE = "KEEP_SCALE"; //$NON-NLS-1$

    public static final String SAVE_WITH_LATEST_FILE_FORMAT = "SAVE_WITH_LATEST_FILE_FORMAT"; //$NON-NLS-1$

//    public static final String USER_TEMPLATE_PATH = "USER_TEMPLATE_PATH";

    public static final String SPELL_ENABLE = "SPELL_ENABLE"; //$NON-NLS-1$
    //added by baipeng. 07.7.19
    public static final String ENABLED_SEARCH_ENGINES = "ENABLED_SEARCH_ENGINES"; //key  //$NON-NLS-1$

    public static final String IMAGE_SEARCH_ENGINE = "IMAGE_SEARCH_ENGINE"; //key //$NON-NLS-1$
    public static final String IMAGE_SEARCH_GOOGLE = "Google"; //value //$NON-NLS-1$
    public static final String IMAGE_SEARCH_YAHOO = "Yahoo"; //value //$NON-NLS-1$

//    public static final String DEFAULT_SEARCH_ENGINE = "$org.xmind.searchPlugin.baiDu$"  ; //value  

}