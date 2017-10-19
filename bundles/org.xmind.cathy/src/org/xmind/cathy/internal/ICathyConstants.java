package org.xmind.cathy.internal;

import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;

public interface ICathyConstants {

    /*
     * Element Tag Names
     */
    public static final String TAG_SHOW_IMAGE = "CathyShowImage"; //$NON-NLS-1$
    public static final String TAG_SHOW_DASHBOARD = "CathyShowDashboard"; //$NON-NLS-1$
    public static final String TAG_EDITOR = "Editor"; //$NON-NLS-1$
    public static final String TAG_FORCE_TEXT = "FORCE_TEXT"; //$NON-NLS-1$

    public static final String TAG_TRIMBAR_LAYOUT_BEGINING = "TrimBarLayout:begining"; //$NON-NLS-1$
    public static final String TAG_TRIMBAR_LAYOUT_CENTER = "TrimBarLayout:center"; //$NON-NLS-1$
    public static final String TAG_TRIMBAR_LAYOUT_END = "TrimBarLayout:end"; //$NON-NLS-1$

    /*
     * Element Ids
     */
    public static final String ID_APPLICATION = "org.xmind.cathy.application"; //$NON-NLS-1$
    public static final String ID_MAIN_WINDOW = "org.xmind.cathy.window.main"; //$NON-NLS-1$
    public static final String ID_PERSPECTIVE_STACK = "org.eclipse.ui.ide.perspectivestack"; //$NON-NLS-1$
    public static final String ID_EDITOR_AREA = "org.eclipse.ui.editorss"; //$NON-NLS-1$
    public static final String ID_PRIMARY_EDITOR_STACK = "org.eclipse.e4.primaryDataStack"; //$NON-NLS-1$ 
    public static final String ID_MAIN_TOOLBAR = "org.eclipse.ui.main.toolbar"; //$NON-NLS-1$
    public static final String ID_STATUS_BAR = "org.eclipse.ui.trim.status"; //$NON-NLS-1$
    public static final String ID_TRIMBAR_RIGHT = "org.xmind.ui.trimbar.right"; //$NON-NLS-1$
    public static final String ID_PARTSTACK_RIGHT = "org.xmind.ui.stack.right"; //$NON-NLS-1$
    public static final String ID_DASHBOARD_PART = "org.xmind.cathy.part.dashboard"; //$NON-NLS-1$
    public static final String ID_TOOL_ITEM_TOGGLE_DASHBOARD = "org.xmind.ui.toolbar.dashboard.toggle"; //$NON-NLS-1$

    public static final String ID_MENU_ITEM_UNDO = "undo"; //$NON-NLS-1$
    public static final String ID_MENU_ITEM_REDO = "redo"; //$NON-NLS-1$
    public static final String ID_MENU_ITEM_SAVE = "save"; //$NON-NLS-1$
    public static final String ID_TOOL_ITEM_UNDO = "org.xmind.ui.toolbar.edit.undo"; //$NON-NLS-1$
    public static final String ID_TOOL_ITEM_REDO = "org.xmind.ui.toolbar.edit.redo"; //$NON-NLS-1$
    public static final String ID_TOOL_ITEM_SAVE = "org.xmind.ui.toolbar.edit.save"; //$NON-NLS-1$

    public static final String MB_XADDITIONS = "xadditions"; //$NON-NLS-1$

    /*
     * CSS Property Names
     */
    public static final String PROPERTY_UNSELECTED_TABS_BG_VISIBLE = "xmind-swt-unselected-tabs-bg-visible"; //$NON-NLS-1$
    public static final String PROPERTY_MAXIMIZE_IMAGE = "xmind-swt-maximize-image"; //$NON-NLS-1$
    public static final String PROPERTY_MINIMIZE_IMAGE = "xmind-swt-minimize-image"; //$NON-NLS-1$
    public static final String PROPERTY_CHEVRON_VISIBLE = "xmind-swt-chevron-visible"; //$NON-NLS-1$
    public static final String PROPERTY_CLOSE_IMAGE = "xmind-swt-close-image"; //$NON-NLS-1$
    public static final String PROPERTY_CLOSE_HOVER_IMAGE = "xmind-swt-close-hover-image"; //$NON-NLS-1$
    public static final String PROPERTY_XTEXT_BACKGROUND = "xmind-text-background"; //$NON-NLS-1$
    public static final String PROPERTY_XBOTTOM_KEYLINE_1_COLOR = "xmind-bottom-keyline-1-color"; //$NON-NLS-1$
    public static final String PROPERTY_XBOTTOM_KEYLINE_2_COLOR = "xmind-bottom-keyline-2-color"; //$NON-NLS-1$
    public static final String PROPERTY_HYPERLINK_COLOR = "xmind-hyperlink-color"; //$NON-NLS-1$
    public static final String PROPERTY_ACTIVE_HYPERLINK_COLOR = "xmind-active-hyperlink-color"; //$NON-NLS-1$
    public static final String PROPERTY_MARGIN = "xmind-margin"; //$NON-NLS-1$
    public static final String PROPERTY_MARGIN_TOP = "xmind-margin-top"; //$NON-NLS-1$
    public static final String PROPERTY_MARGIN_BOTTOM = "xmind-margin-bottom"; //$NON-NLS-1$
    public static final String PROPERTY_MARGIN_LEFT = "xmind-margin-left"; //$NON-NLS-1$
    public static final String PROPERTY_MARGIN_RIGHT = "xmind-margin-right"; //$NON-NLS-1$
    public static final String PROPERTY_MINIMIZE_VISIBLE = "xmind-swt-minimize-visible"; //$NON-NLS-1$
    public static final String PROPERTY_MAXIMIZE_VISIBLE = "xmind-swt-maximize-visible"; //$NON-NLS-1$
    public static final String PROPERTY_CTABFOLDER_RENDER_NONE = "xmind-ctabfolder-render-none"; //$NON-NLS-1$
    public static final String PROPERTY_BG_COLOR = "xmind-background-color"; //$NON-NLS-1$
    public static final String PROPERTY_FG_COLOR = "xmind-foreground-color"; //$NON-NLS-1$
    public static final String PROPERTY_TITLE_BAR_TEXT_COLOR = "xmind-title-bar-text-color"; //$NON-NLS-1$
    public static final String PROPERTY_TITLE_BAR_ACTIVE_TEXT_COLOR = "xmind-title-bar-active-text-color"; //$NON-NLS-1$
    public static final String PROPERTY_OUTER_BORDER_VISIBLE = "xmind-swt-outer-border-visible"; //$NON-NLS-1$
    public static final String PROPERTY_INNER_BORDER_VISIBLE = "xmind-swt-inner-border-visible"; //$NON-NLS-1$
    public static final String PROPERTY_IMAGE_VISIBLE = "xmind-swt-image-visible"; //$NON-NLS-1$
    public static final String PROPERTY_TEXT_VISIBLE = "xmind-swt-text-visible"; //$NON-NLS-1$
    public static final String PROPERTY_TOOL_ITEM_COLOR = "xmind-swt-tool-item-color"; //$NON-NLS-1$
    public static final String PROPERTY_VIEW_MENU = "xmind-swt-view-menu-image"; //$NON-NLS-1$
    public static final String PROPERTY_UNSELECTED_TABS_COLOR = "xmind-swt-unselected-tabs-color"; //$NON-NLS-1$
    public static final String PROPERTY_SHOW_CLOSE = "xmind-swt-show-close"; //$NON-NLS-1$
    public static final String PROPERTY_TABFOLDER_BACKGROUND = "xmind-tab-folder-bg"; //$NON-NLS-1$
    public static final String PROPERTY_TABBAR_BACKGROUND = "xmind-tab-bar-bg"; //$NON-NLS-1$

    /*
     * Commmand Ids
     */
    public static final String COMMAND_TOGGLE_DASHBOARD = "org.xmind.ui.command.toggleDashboard"; //$NON-NLS-1$
    public static final String COMMAND_SHOW_DASHBOARD = "org.xmind.ui.command.showDashboard"; //$NON-NLS-1$
    public static final String COMMAND_RECENTFILE_PIN = "org.xmind.ui.command.pinRecentFile"; //$NON-NLS-1$
    public static final String COMMAND_RECENTFILE_UNPIN = "org.xmind.ui.command.unpinRecentFile"; //$NON-NLS-1$
    public static final String COMMAND_RECENTFILE_CLEAR = "org.xmind.ui.command.clearRecentFile"; //$NON-NLS-1$

    public static final String COMMAND_TEMPLATE_DUPLICATE = "org.xmind.ui.command.template.duplicate"; //$NON-NLS-1$
    public static final String COMMAND_TEMPLATE_RENAME = "org.xmind.ui.command.template.rename"; //$NON-NLS-1$
    public static final String COMMAND_TEMPLATE_DELETE = "org.xmind.ui.command.template.delete"; //$NON-NLS-1$

    /*
     * Command Parameter Ids
     */
    public static final String PARAMETER_DASHBOARD_PAGE_ID = "org.xmind.ui.command.showDashboard.pageId"; //$NON-NLS-1$

    /*
     * Data Keys
     */
    public static final String DATA_PART_OF_WIDGET = AbstractPartRenderer.OWNING_ME;
    public static final String DATA_DASHBOARD_SELECTED_PAGE_ID = "org.xmind.ui.part.dashboard.selectedPageId"; //$NON-NLS-1$

    /*
     * Dashboard Pages
     */
    public static final String DASHBOARD_PAGE_NEW = "org.xmind.ui.part.dashboard.new"; //$NON-NLS-1$
    public static final String DASHBOARD_PAGE_RECENT = "org.xmind.ui.part.dashboard.recent"; //$NON-NLS-1$

    /*
     * Popup Menu Ids
     */
    public static final String POPUP_RECENTFILE = "org.xmind.ui.popup.recentFile"; //$NON-NLS-1$

    /*
     * Popup Menu Ids
     */
    public static final String POPUP_TEMPLATE = "org.xmind.ui.popup.template"; //$NON-NLS-1$

    /*
     * Helper Ids
     */
    public static final String HELPER_RECENTFILE_PIN = "org.xmind.ui.helper.recentFile.pin"; //$NON-NLS-1$
    public static final String HELPER_RECENTFILE_DELETE = "org.xmind.ui.helper.recentFile.delete"; //$NON-NLS-1$
    public static final String HELPER_RECENTFILE_CLEAR = "org.xmind.ui.helper.recentFile.clear"; //$NON-NLS-1$

    public static final String HELPER_TEMPLATE_RENAME = "org.xmind.ui.helper.template.rename"; //$NON-NLS-1$
}
