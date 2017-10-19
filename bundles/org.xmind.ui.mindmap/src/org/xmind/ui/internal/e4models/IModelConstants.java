package org.xmind.ui.internal.e4models;

public interface IModelConstants {

    /*
     * Keys
     */
    public static final String KEY_MODEL_PART_COMMAND_PARAMETER_PARTSTACK_ID = "org.xmind.ui.commandParameter.modelPart.partStackId"; //$NON-NLS-1$
    public static final String KEY_MODEL_PART_COMMAND_PARAMETER_PART_ID = "org.xmind.ui.commandParameter.modelPart.partId"; //$NON-NLS-1$
    public static final String KEY_MODEL_PART_COMMAND_PARAMETER_PAGE_ID = "org.xmind.ui.commandParameter.modelPart.pageId"; //$NON-NLS-1$
    public static final String KEY_MODEL_PART_CURRENT_PAGE_ID = "org.xmind.ui.modelPart.currentPageId"; //$NON-NLS-1$
    public static final String KEY_MODEL_PART_REFRESH_PAGE = "org.xmind.ui.modelPart.refreshPage"; //$NON-NLS-1$
    public static final String KEY_MODEL_PART_SET_DEFAULT = "org.xmind.ui.modelPart.setDefault"; //$NON-NLS-1$
    public static final String KEY_MODEL_PART_DUPLICATE = "org.xmind.ui.modelPart.duplicate"; //$NON-NLS-1$
    public static final String KEY_MODEL_PART_RENAME = "org.xmind.ui.modelPart.rename"; //$NON-NLS-1$
    public static final String KEY_MODEL_PART_DELETE = "org.xmind.ui.modelPart.delete"; //$NON-NLS-1$
    public static final String KEY_MODEL_PART_EDIT = "org.xmind.ui.modelPart.edit"; //$NON-NLS-1$
    public static final String KEY_LAST_OPENED_MODEL_PART_ID = "org.xmind.ui.modelPart.lastOpened"; //$NON-NLS-1$

    public static final String KEY_DIALOG_PART_CUSTOM_LOCATION = "org.xmind.ui.dialogPart.customLocation"; //$NON-NLS-1$

    /*
     * Commands
     */
    public static final String COMMAND_SHOW_DIALOG_PART = "org.xmind.ui.command.showDialogPart"; //$NON-NLS-1$
    public static final String COMMAND_SHOW_MODEL_PART = "org.xmind.ui.command.showModelPart"; //$NON-NLS-1$
    public static final String COMMAND_SHOW_POPOVER = "org.xmind.ui.command.showPopover"; //$NON-NLS-1$

    /*
     * PartStackIds
     */
    public static final String PART_STACK_ID_RIGHT = "org.xmind.ui.stack.right"; //$NON-NLS-1$

    /*
     * PartIds
     */
    public static final String PART_ID_IMAGE = "org.xmind.ui.modelPart.image"; //$NON-NLS-1$
    public static final String PART_ID_NOTES = "org.xmind.ui.modelPart.notes"; //$NON-NLS-1$
    public static final String PART_ID_THEMES = "org.xmind.ui.modelPart.themes"; //$NON-NLS-1$
    public static final String PART_ID_OUTLINE = "org.xmind.ui.modelPart.outline"; //$NON-NLS-1$
    public static final String PART_ID_MARKERS = "org.xmind.ui.modelPart.markers"; //$NON-NLS-1$
    public static final String PART_ID_COMMENTS = "org.xmind.ui.modelPart.comments"; //$NON-NLS-1$
    public static final String PART_ID_PROPERTIES = "org.xmind.ui.modelPart.properties"; //$NON-NLS-1$
    public static final String PART_ID_TASKINFO = "org.xmind.ui.modelPart.taskinfo"; //$NON-NLS-1$
    public static final String PART_ID_RESOURCE_MANAGER = "org.xmind.ui.dialogPart.resourceManager"; //$NON-NLS-1$
    public static final String PART_ID_COMPATIBILITY_EDITOR = "org.eclipse.e4.ui.compatibility.editor"; //$NON-NLS-1$

    /*
     * PageIds
     */
    public static final String PAGE_ID_RESOURCE_MANAGER_TEMPLATE = "org.xmind.ui.dialogPart.resourceManager.template"; //$NON-NLS-1$
    public static final String PAGE_ID_RESOURCE_MANAGER_CLIPART = "org.xmind.ui.dialogPart.resourceManager.clipArt"; //$NON-NLS-1$
    public static final String PAGE_ID_RESOURCE_MANAGER_MARKER = "org.xmind.ui.dialogPart.resourceManager.marker"; //$NON-NLS-1$
    public static final String PAGE_ID_RESOURCE_MANAGER_THEME = "org.xmind.ui.dialogPart.resourceManager.theme"; //$NON-NLS-1$
    public static final String PAGE_ID_RESOURCE_MANAGER_STYLE = "org.xmind.ui.dialogPart.resourceManager.style"; //$NON-NLS-1$

    /*
     * ViewMenuIds
     */
    public static final String VIEWMENU_ID_THEME = "org.xmind.ui.modelPart.theme.viewMenu"; //$NON-NLS-1$

    /*
     * PopupMenuIds
     */
    public static final String POPUPMENU_ID_RESOURCEMANAGER_TEMPLATE = "org.xmind.ui.dialogPart.resourceManager.template.popupMenu"; //$NON-NLS-1$
    public static final String POPUPMENU_ID_RESOURCEMANAGER_CLIPART = "org.xmind.ui.dialogPart.resourceManager.clipArt.popupMenu"; //$NON-NLS-1$
    public static final String POPUPMENU_ID_RESOURCEMANAGER_MARKER = "org.xmind.ui.dialogPart.resourceManager.marker.popupMenu"; //$NON-NLS-1$
    public static final String POPUPMENU_ID_RESOURCEMANAGER_THEME = "org.xmind.ui.dialogPart.resourceManager.theme.popupMenu"; //$NON-NLS-1$
    public static final String POPUPMENU_ID_RESOURCEMANAGER_STYLE = "org.xmind.ui.dialogPart.resourceManager.style.popupMenu"; //$NON-NLS-1$

    /*
     * PersistedState Keys
     */
    public static final String PERSISTED_STATE_KEY_UNSELECTED_ICONURI = "unselectedIconURI"; //$NON-NLS-1$
    public static final String PERSISTED_STATE_KEY_SELECTED_ICONURI = "selectedIconURI"; //$NON-NLS-1$

    /*
     * tags
     */
    public static final String DIRECT_COMMAD_TAG = "DirectCommand"; //$NON-NLS-1$
    public static final String TAG_ACTIVE = "active"; //$NON-NLS-1$
    public static final String TAG_EDITOR = "Editor"; //$NON-NLS-1$
    public static final String TAG_X_STACK = "XStack"; //$NON-NLS-1$

    /*
     * Popover toolItem Ids
     */
    public static final String TOOLITEM_ID_MARKER_POPOVER = "org.xmind.ui.toolbar.mindmap.marker"; //$NON-NLS-1$
}
