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
package org.xmind.ui.commands;

import org.eclipse.osgi.util.NLS;

public class CommandMessages extends NLS {

    private static final String BUNDLE_NAME = "org.xmind.ui.commands.messages"; //$NON-NLS-1$

    public static String Command_AddMarker;
    public static String Command_AddResources;
    public static String Command_Align;
    public static String Command_AlignLeft;
    public static String Command_AlignCenter;
    public static String Command_AlignRight;
    public static String Command_AlignTop;
    public static String Command_AlignMiddle;
    public static String Command_AlignBottom;
    public static String Command_CreateBoundary;
    public static String Command_CreateFloatingTopic;
    public static String Command_CreateRelationship;
    public static String Command_CreateSheet;
    public static String Command_CreateSheetFromTopic;
    public static String Command_CreateSummary;
    public static String Command_CreateTopic;
    public static String Command_Collapse;
    public static String Command_CollapseAll;
    public static String Command_CopyImage;
    public static String Command_CopyMarker;
    public static String Command_CopyTopic;
    public static String Command_Cut;
    public static String Command_CutBoundary;
    public static String Command_CutRelationship;
    public static String Command_CutSheet;
    public static String Command_CutTopic;
    public static String Command_Delete;
    public static String Command_DeleteBoundary;
    public static String Command_DeleteMarker;
    public static String Command_DeleteRelationship;
    public static String Command_DeleteSheet;
    public static String Command_DeleteTopic;
    public static String Command_Extend;
    public static String Command_ExtendAll;
    public static String Command_HideLegend;
    public static String Command_InsertAttachment;
    public static String Command_InsertImage;
    public static String Command_ModifyBorderColor;

    public static String Command_ModifyBorderShape;

    public static String Command_ModifyBoundaryRange;
    public static String Command_ModifyBoundaryTitle;
    public static String Command_ModifyLabels;
    public static String Command_ModifyLegendMarkerDescription;
    public static String Command_ModifyNotes;
    public static String Command_ModifyNumbering;
    public static String Command_ModifyPosition;
    public static String Command_ModifyRange;
    public static String Command_ModifyRelationshipTitle;
    public static String Command_ModifySheetTitle;
    public static String Command_ModifyStyle;
    public static String Command_RemoveStyle;
    public static String Command_ModifySummaryRange;
    public static String Command_ModifyTitle;
    public static String Command_ModifyTitleWidth;
    public static String Command_ModifyTopicHyperlink;
    public static String Command_ModifyTopicStructure;
    public static String Command_ModifyTopicTitle;
    public static String Command_MoveImage;
    public static String Command_MoveLegend;
    public static String Command_MoveMarker;
    public static String Command_MoveSheet;
    public static String Command_MoveRelationshipControlPoint;
    public static String Command_MoveTopic;
    public static String Command_ResizeImage;
    public static String Command_Paste;
    public static String Command_PasteSheet;
    public static String Command_PasteTopic;
    public static String Command_ReplaceMarker;
    public static String Command_ResetPosition;
    public static String Command_RetargetRelationship;
    public static String Command_ShowLegend;
    public static String Command_Tile;
    public static String Command_Typing;
    public static String Command_ModifyTheme;
    public static String Command_ModifyLegendBackgroundColor;

    public static String Command_ShowOrHideInfoItem;

    // Labels of Modify Style Commands:
    public static String Command_ModifyBoundaryOpacity;
    public static String Command_ModifyBoundaryShape;
    public static String Command_ModifyFillColor;
    public static String Command_ModifyFont;
    public static String Command_ModifyLineColor;
    public static String Command_ModifyLineShape;
    public static String Command_RemoveWallpaper;
    public static String Command_ModifySheetBackgroundColor;
    public static String Command_ModifyTextColor;
    public static String Command_ModifyTopicShape;
    public static String Command_ModifyRelationshipShape;
    public static String Command_ModifyBeginArrowShape;
    public static String Command_ModifyEndArrowShape;
    public static String Command_ModifyWallpaper;
    public static String Command_ModifyWallpaperOpacity;
    public static String Command_ToggleMultiLineColors;
    public static String Command_ToggleTaperedLines;
    public static String Command_ToggleGradientColor;
    public static String Command_ShowLabels;
    public static String Command_ShowNotes;
    public static String Command_ShowHyperlink;
    public static String Command_ShowTaskInfo;
    public static String Command_ModifyYellowBoxBackgroundColor;

    public static String Command_TextAlignLeft;
    public static String Command_TextAlignCenter;
    public static String Command_TextAlignRight;

    public static String Command_TextManual;
    public static String Command_TextUppercase;
    public static String Command_TextLowercase;
    public static String Command_TextCapitalize;

    public static String Command_SortByTitle;
    public static String Command_SortByPriority;
    public static String Command_SortByModifiedTime;
    public static String Command_Sort;
    public static String Command_ModifyWidth;

    public static String Command_TurnOnAutoRevisionSaving;
    public static String Command_TurnOffAutoRevisionSaving;
    public static String Command_CopySheet;
    public static String Command_DuplicateSheet;

    public static String Command_ClearMarkers;
    public static String Command_RemoveMarkerFromAllTopics;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, CommandMessages.class);
    }

    private CommandMessages() {
    }
}