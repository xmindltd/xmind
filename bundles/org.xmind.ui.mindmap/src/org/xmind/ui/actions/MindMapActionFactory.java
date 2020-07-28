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
package org.xmind.ui.actions;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.LabelRetargetAction;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.actions.SaveAsTemplateAction;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;

public class MindMapActionFactory {

    @Deprecated
    public static final ActionFactory NEW_WORKBOOK = new ActionFactory(
            "org.xmind.ui.newWorkbook") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null)
                throw new IllegalArgumentException();
            org.xmind.ui.internal.actions.NewWorkbookAction action = new org.xmind.ui.internal.actions.NewWorkbookAction(
                    window);
            action.setId(getId());
            return action;
        }
    };

    @Deprecated
    public static final ActionFactory OPEN = new ActionFactory(
            "org.xmind.ui.open") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null)
                throw new IllegalArgumentException();
            org.xmind.ui.internal.actions.OpenWorkbookAction action = new org.xmind.ui.internal.actions.OpenWorkbookAction(
                    window);
            action.setId(getId());
            return action;
        }
    };

    @Deprecated
    public static final ActionFactory SAVE_TEMPLATE = new ActionFactory(
            "org.xmind.ui.saveTemplate") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null)
                throw new IllegalArgumentException();
            return new SaveAsTemplateAction(getId(), window);
        }
    };

    public static final ActionFactory SELECT_BROTHERS = new ActionFactory(
            "org.xmind.ui.selectBrothers") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.SelectBrothers_text);
            action.setToolTipText(MindMapMessages.SelectBrothers_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.selectBrothers"); //$NON-NLS-1$
            return action;
        }
    };

    public static final ActionFactory SELECT_CHILDREN = new ActionFactory(
            "org.xmind.ui.selectChildren") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.SelectChildren_text);
            action.setToolTipText(MindMapMessages.SelectChildren_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.selectChildren"); //$NON-NLS-1$
            return action;
        }
    };

    public static final ActionFactory GO_HOME = new ActionFactory(
            "org.xmind.ui.selectHome") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.GoToCentral_text);
            action.setToolTipText(MindMapMessages.GoToCentral_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.goHome"); //$NON-NLS-1$
            return action;
        }
    };

    public static final ActionFactory ZOOM_IN = new ActionFactory(
            "org.xmind.ui.zoomIn") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null)
                throw new IllegalArgumentException();
            LabelRetargetAction action = new LabelRetargetAction(getId(),
                    MindMapMessages.ZoomIn_text);
            action.setToolTipText(MindMapMessages.ZoomIn_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.zoomIn"); //$NON-NLS-1$
            action.setImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.ZOOMIN, true));
            action.setDisabledImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.ZOOMIN, false));
            return action;
        }
    };

    public static final ActionFactory ZOOM_OUT = new ActionFactory(
            "org.xmind.ui.zoomOut") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null)
                throw new IllegalArgumentException();
            LabelRetargetAction action = new LabelRetargetAction(getId(),
                    MindMapMessages.ZoomOut_text);
            action.setToolTipText(MindMapMessages.ZoomOut_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.zoomOut"); //$NON-NLS-1$
            action.setImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.ZOOMOUT, true));
            action.setDisabledImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.ZOOMOUT, false));
            return action;
        }
    };

    public static final ActionFactory ACTUAL_SIZE = new ActionFactory(
            "org.xmind.ui.actualSize") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null)
                throw new IllegalArgumentException();
            LabelRetargetAction action = new LabelRetargetAction(getId(),
                    MindMapMessages.ActualSize_text);
            action.setToolTipText(MindMapMessages.ActualSize_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.actualSize"); //$NON-NLS-1$
            action.setImageDescriptor(MindMapUI.getImages()
                    .get(IMindMapImages.ACTUAL_SIZE, true));
            action.setDisabledImageDescriptor(MindMapUI.getImages()
                    .get(IMindMapImages.ACTUAL_SIZE, false));
            return action;
        }
    };

    public static final ActionFactory FIT_MAP = new ActionFactory(
            "org.xmind.ui.fitMap") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null)
                throw new IllegalArgumentException();
            LabelRetargetAction action = new LabelRetargetAction(getId(),
                    MindMapMessages.FitMap_text);
            action.setToolTipText(MindMapMessages.FitMap_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.fitMap"); //$NON-NLS-1$
            action.setImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.FIT_SIZE, true));
            action.setDisabledImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.FIT_SIZE, false));
            return action;
        }
    };

    public static final ActionFactory FIT_SELECTION = new ActionFactory(
            "org.xmind.ui.fitSelection") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null)
                throw new IllegalArgumentException();
            LabelRetargetAction action = new LabelRetargetAction(getId(),
                    MindMapMessages.FitSelection_text);
            action.setToolTipText(MindMapMessages.FitSelection_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.fitSelection"); //$NON-NLS-1$
            action.setImageDescriptor(MindMapUI.getImages()
                    .get(IMindMapImages.FIT_SELECTION, true));
            action.setDisabledImageDescriptor(MindMapUI.getImages()
                    .get(IMindMapImages.FIT_SELECTION, false));
            return action;
        }
    };

    public static final ActionFactory INSERT_TOPIC = new ActionFactory(
            "org.xmind.ui.insertTopic") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null)
                throw new IllegalArgumentException();
            LabelRetargetAction action = new LabelRetargetAction(getId(),
                    MindMapMessages.InsertTopic_text);
            action.setToolTipText(MindMapMessages.InsertTopic_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.insertTopic"); //$NON-NLS-1$
            action.setImageDescriptor(MindMapUI.getImages()
                    .get(IMindMapImages.INSERT_AFTER, true));
            action.setDisabledImageDescriptor(MindMapUI.getImages()
                    .get(IMindMapImages.INSERT_AFTER, false));
            return action;
        }
    };

    public static final ActionFactory INSERT_SUBTOPIC = new ActionFactory(
            "org.xmind.ui.insertSubtopic") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null)
                throw new IllegalArgumentException();
            LabelRetargetAction action = new LabelRetargetAction(getId(),
                    MindMapMessages.InsertSubtopic_text);
            action.setToolTipText(MindMapMessages.InsertSubtopic_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.insertSubtopic"); //$NON-NLS-1$
            action.setImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.INSERT_SUB, true));
            action.setDisabledImageDescriptor(MindMapUI.getImages()
                    .get(IMindMapImages.INSERT_SUB, false));
            return action;
        }
    };

    public static final ActionFactory INSERT_TOPIC_BEFORE = new ActionFactory(
            "org.xmind.ui.insertTopicBefore") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null)
                throw new IllegalArgumentException();
            LabelRetargetAction action = new LabelRetargetAction(getId(),
                    MindMapMessages.InsertTopicBefore_text);
            action.setToolTipText(MindMapMessages.InsertTopicBefore_toolTip);
            action.setActionDefinitionId(
                    "org.xmind.ui.command.insertTopicBefore"); //$NON-NLS-1$
            action.setImageDescriptor(MindMapUI.getImages()
                    .get(IMindMapImages.INSERT_BEFORE, true));
            action.setDisabledImageDescriptor(MindMapUI.getImages()
                    .get(IMindMapImages.INSERT_BEFORE, false));
            return action;
        }
    };

    public static final ActionFactory INSERT_PARENT_TOPIC = new ActionFactory(
            "org.xmind.ui.insertParentTopic") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            LabelRetargetAction action = new LabelRetargetAction(getId(),
                    MindMapMessages.InsertParentTopic_text);
            action.setToolTipText(MindMapMessages.InsertParentTopic_toolTip);
            action.setActionDefinitionId(
                    "org.xmind.ui.command.insertParentTopic"); //$NON-NLS-1$
            action.setImageDescriptor(MindMapUI.getImages()
                    .get(IMindMapImages.INSERT_PARENT, true));
            action.setDisabledImageDescriptor(MindMapUI.getImages()
                    .get(IMindMapImages.INSERT_PARENT, false));
            return action;
        }
    };

//    public static final ActionFactory INSERT_SHEET = new ActionFactory(
//            "org.xmind.ui.insertSheet") { //$NON-NLS-1$
//        public IWorkbenchAction create(IWorkbenchWindow window) {
//            if (window == null)
//                throw new IllegalArgumentException();
//            LabelRetargetAction action = new LabelRetargetAction(getId(),
//                    "new Sheet");
//            action.setToolTipText("create a new Sheet");
//            action.setActionDefinitionId("org.xmind.ui.command.insertSheet");
//            action.setImageDescriptor(MindMapUI.getImages().get(
//                    IMindMapImages.SHEET, true));
//            action.setDisabledImageDescriptor(MindMapUI.getImages().get(
//                    IMindMapImages.SHEET, false));
//            return action;
//        }
//    };

    public static final ActionFactory INSERT_FLOATING_TOPIC = new ActionFactory(
            "org.xmind.ui.insertFloatingTopic") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.InsertFloatingTopic_text);
            action.setToolTipText(MindMapMessages.InsertFloatingTopic_toolTip);
            action.setActionDefinitionId(
                    "org.xmind.ui.command.insertFloatingTopic"); //$NON-NLS-1$
            action.setImageDescriptor(MindMapUI.getImages()
                    .get(IMindMapImages.INSERT_FLOATING_MAIN, true));
            action.setDisabledImageDescriptor(MindMapUI.getImages()
                    .get(IMindMapImages.INSERT_FLOATING_MAIN, false));
            return action;
        }
    };

    public static final ActionFactory INSERT_FLOATING_CENTRAL_TOPIC = new ActionFactory(
            "org.xmind.ui.insertFloatingCentralTopic") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.InsertFloatingCentralTopic_text);
            action.setToolTipText(
                    MindMapMessages.InsertFloatingCentralTopic_toolTip);
            action.setActionDefinitionId(
                    "org.xmind.ui.command.insertFloatingCentralTopic"); //$NON-NLS-1$
            action.setImageDescriptor(MindMapUI.getImages()
                    .get(IMindMapImages.INSERT_FLOATING_CENTRAL, true));
            action.setDisabledImageDescriptor(MindMapUI.getImages()
                    .get(IMindMapImages.INSERT_FLOATING_CENTRAL, false));
            return action;
        }
    };

    public static final ActionFactory EXTEND = new ActionFactory(
            "org.xmind.ui.extend") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.Extend_text);
            action.setToolTipText(MindMapMessages.Extend_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.extend"); //$NON-NLS-1$
            return action;
        }
    };

    public static final ActionFactory EXTEND_ALL = new ActionFactory(
            "org.xmind.ui.extendAll") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.ExtendAll_text);
            action.setToolTipText(MindMapMessages.ExtendAll_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.extendAll"); //$NON-NLS-1$
            return action;
        }
    };

    public static final ActionFactory COLLAPSE = new ActionFactory(
            "org.xmind.ui.collapse") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.Collapse_text);
            action.setToolTipText(MindMapMessages.Collapse_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.collapse"); //$NON-NLS-1$
            return action;
        }
    };

    public static final ActionFactory COLLAPSE_ALL = new ActionFactory(
            "org.xmind.ui.collapseAll") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.CollapseAll_text);
            action.setToolTipText(MindMapMessages.CollapseAll_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.collapseAll"); //$NON-NLS-1$
            return action;
        }
    };

    public static final ActionFactory NEW_SHEET = new ActionFactory(
            "org.xmind.ui.newSheet") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.NewSheet_text);
            action.setToolTipText(MindMapMessages.NewSheet_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.newSheet"); //$NON-NLS-1$
            action.setImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.NEWMAP, true));
            action.setDisabledImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.NEWMAP, false));
            return action;
        }
    };

    public static final ActionFactory INSERT_SHEET_FROM = new ActionFactory(
            "org.xmind.ui.topic.newSheet") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.NEWSheet_from_text);
            action.setToolTipText(MindMapMessages.NEWSheet_from_toolTip);
            action.setActionDefinitionId(
                    "org.xmind.ui.command.insertSheetFrom"); //$NON-NLS-1$
            action.setImageDescriptor(MindMapUI.getImages()
                    .get(IMindMapImages.NEW_SHEET_AS, true));
            action.setDisabledImageDescriptor(MindMapUI.getImages()
                    .get(IMindMapImages.NEW_SHEET_AS, false));

            return action;
        }

    };

    public static final ActionFactory DELETE_SHEET = new ActionFactory(
            "org.xmind.ui.deleteSheet") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.DeleteSheet_text);
            action.setToolTipText(MindMapMessages.DeleteSheet_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.deleteSheet"); //$NON-NLS-1$
            return action;
        }
    };

    public static final ActionFactory DELETE_OTHER_SHEET = new ActionFactory(
            "org.xmind.ui.deleteOtherSheet") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.Delete_OtherSheets_text);
            action.setToolTipText(MindMapMessages.Delete_OtherSheets_toolTip);
//            action
//                    .setActionDefinitionId("org.xmind.ui.command.deleteOtherSheet"); //$NON-NLS-1$
            return action;
        }

    };

    public static final ActionFactory MODIFY_HYPERLINK = new ActionFactory(
            "org.xmind.ui.modifyHyperlink") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.ModifyHyperlink_text);
            action.setToolTipText(MindMapMessages.ModifyHyperlink_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.hyperlink"); //$NON-NLS-1$
            action.setImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.HYPERLINK, true));
            action.setDisabledImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.HYPERLINK, false));
            return action;
        }
    };

    public static final ActionFactory CANCEL_HYPERLINK = new ActionFactory(
            "org.xmind.ui.cancelHyperlinnk") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.CancelHyperlink_text);
            action.setToolTipText(MindMapMessages.CancelHyperlink_toolTip);
            action.setActionDefinitionId(
                    "org.xmind.ui.command.cancelHyperlink"); //$NON-NLS-1$
            return action;
        }
    };
//
//    public static final ActionFactory SAVE_ATTACHMENT_AS = new ActionFactory(
//            "org.xmind.ui.saveAttachmentAs") { //$NON-NLS-1$
//        public IWorkbenchAction create(IWorkbenchWindow window) {
//            RetargetAction action = new RetargetAction(getId(),
//                    MindMapMessages.SaveAttachment_text);
//            action.setToolTipText(MindMapMessages.SaveAttachment_toolTip);
//            action.setActionDefinitionId("org.xmind.ui.command.saveAttachmentAs"); //$NON-NLS-1$
//            return action;
//        }
//    };
//
    public static final ActionFactory OPEN_HYPERLINK = new ActionFactory(
            "org.xmind.ui.openHyperlink") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            LabelRetargetAction action = new LabelRetargetAction(getId(),
                    MindMapMessages.OpenHyperlink_text);
            action.setToolTipText(MindMapMessages.OpenHyperlink_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.openHyperlink"); //$NON-NLS-1$
            return action;
        }
    };

    public static final ActionFactory INSERT_ATTACHMENT = new ActionFactory(
            "org.xmind.ui.insertAttachment") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.InsertAttachment_text);
            action.setToolTipText(MindMapMessages.InsertAttachment_toolTip);
            action.setImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.ATTACHMENT, true));
            action.setDisabledImageDescriptor(MindMapUI.getImages()
                    .get(IMindMapImages.ATTACHMENT, false));
            action.setActionDefinitionId(
                    "org.xmind.ui.command.insertAttachment"); //$NON-NLS-1$
            return action;
        }
    };

    public static final ActionFactory CREATE_RELATIONSHIP = new ActionFactory(
            "org.xmind.ui.createRelationship") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.InsertRelationship_text);
            action.setToolTipText(MindMapMessages.InsertRelationship_toolTip);
            action.setActionDefinitionId(
                    "org.xmind.ui.command.createRelationship"); //$NON-NLS-1$
            action.setImageDescriptor(MindMapUI.getImages()
                    .get(IMindMapImages.RELATIONSHIP, true));
            action.setDisabledImageDescriptor(MindMapUI.getImages()
                    .get(IMindMapImages.RELATIONSHIP, false));
            return action;
        }
    };

    public static final ActionFactory CREATE_BOUNDARY = new ActionFactory(
            "org.xmind.ui.createBoundary") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.InsertBoundary_text);
            action.setToolTipText(MindMapMessages.InsertBoundary_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.createBoundary"); //$NON-NLS-1$
            action.setImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.BOUNDARY, true));
            action.setDisabledImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.BOUNDARY, false));
            return action;
        }
    };

    public static final ActionFactory CREATE_SUMMARY = new ActionFactory(
            "org.xmind.ui.createSummary") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.InsertSummary_text);
            action.setToolTipText(MindMapMessages.InsertSummary_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.createSummary"); //$NON-NLS-1$
            action.setImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.SUMMARY, true));
            action.setDisabledImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.SUMMARY, false));
            return action;
        }
    };

    public static final ActionFactory DRILL_DOWN = new ActionFactory(
            "org.xmind.ui.drillDown") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.DrillDown_text);
            action.setToolTipText(MindMapMessages.DrillDown_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.drillDown"); //$NON-NLS-1$
            action.setImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.DRILL_DOWN, true));
            action.setDisabledImageDescriptor(MindMapUI.getImages()
                    .get(IMindMapImages.DRILL_DOWN, false));
            return action;
        }
    };

    public static final ActionFactory DRILL_UP = new ActionFactory(
            "org.xmind.ui.drillUp") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.DrillUp_text);
            action.setToolTipText(MindMapMessages.DrillUp_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.drillUp"); //$NON-NLS-1$
            action.setImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.DRILL_UP, true));
            action.setDisabledImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.DRILL_UP, false));
            return action;
        }
    };

//    public static final ActionFactory SORT_ASALPHA = new ActionFactory(
//            "org.xmind.ui.sortAsAlpha") { //$NON-NLS-1$
//        public IWorkbenchAction create(IWorkbenchWindow window) {
//            RetargetAction action = new RetargetAction(getId(),
//                    MindMapMessages.SortAsAlpha_text);
//            action.setToolTipText(MindMapMessages.SortAsAlpha_toolTip);
////            action.setActionDefinitionId("org.xmind.ui.command.sortAsAlpha"); //$NON-NLS-1$
//            action.setImageDescriptor(MindMapUI.getImages().get(
//                    IMindMapImages.ALAPHA, true));
//            action.setDisabledImageDescriptor(MindMapUI.getImages().get(
//                    IMindMapImages.ALAPHA, false));
//            return action;
//        }
//    };
//
//    public static final ActionFactory SORT_ASPRIORITY = new ActionFactory(
//            "org.xmind.ui.sortAsPriority") { //$NON-NLS-1$
//        public IWorkbenchAction create(IWorkbenchWindow window) {
//            RetargetAction action = new RetargetAction(getId(),
//                    MindMapMessages.SortAsPriority_text);
//            action.setToolTipText(MindMapMessages.SortAsPriority_toolTip);
////            action.setActionDefinitionId("org.xmind.ui.command.sortAsPriority"); //$NON-NLS-1$
//            return action;
//        }
//    };
//
//    public static final ActionFactory SORT_ASDATE = new ActionFactory(
//            "org.xmind.ui.sortAsModifyDate") { //$NON-NLS-1$
//        public IWorkbenchAction create(IWorkbenchWindow window) {
//            RetargetAction action = new RetargetAction(getId(),
//                    MindMapMessages.SortAsModifyDate_text);
//            action.setToolTipText(MindMapMessages.SortAsModifyDate_toolTip);
//            return action;
//        }
//    };

    public static final ActionFactory EDIT_TITLE = new ActionFactory(
            "org.xmind.ui.editTitle") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.EditTitle_text);
            action.setToolTipText(MindMapMessages.EditTitle_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.editTitle"); //$NON-NLS-1$
            return action;
        }
    };

    public static final ActionFactory EDIT_LABEL = new ActionFactory(
            "org.xmind.ui.editLabel") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.EditLabel_text);
            action.setToolTipText(MindMapMessages.EditLabel_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.editLabel"); //$NON-NLS-1$
            action.setImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.LABEL, true));
            action.setDisabledImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.LABEL, false));
            return action;
        }
    };

    public static final ActionFactory EDIT_NOTES = new ActionFactory(
            "org.xmind.ui.editNotes") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.EditNotes_text);
            action.setToolTipText(MindMapMessages.EditNotes_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.editNotes"); //$NON-NLS-1$
            action.setImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.NOTES, true));
            action.setDisabledImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.NOTES, false));
            return action;
        }
    };

    public static final ActionFactory INSERT_IMAGE = new ActionFactory(
            "org.xmind.ui.insertImage") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.InsertImage_text);
            action.setToolTipText(MindMapMessages.InsertImage_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.insertImage"); //$NON-NLS-1$
            action.setImageDescriptor(MindMapUI.getImages()
                    .get(IMindMapImages.INSERT_IMAGE, true));
            action.setDisabledImageDescriptor(MindMapUI.getImages()
                    .get(IMindMapImages.INSERT_IMAGE, false));
            return action;
        }
    };

    public static final ActionFactory TRAVERSE = new ActionFactory(
            "org.xmind.ui.traverse") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.Traverse_text);
            action.setToolTipText(MindMapMessages.Traverse_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.traverse"); //$NON-NLS-1$
            return action;
        }
    };

    public static final ActionFactory FINISH = new ActionFactory(
            "org.xmind.ui.finish") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.Finish_text);
            action.setToolTipText(MindMapMessages.Finish_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.finish"); //$NON-NLS-1$
            return action;
        }
    };

    public static final ActionFactory TILE = new ActionFactory(
            "org.xmind.ui.tile") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.Tile_text);
            action.setToolTipText(MindMapMessages.Tile_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.tile"); //$NON-NLS-1$
            return action;
        }
    };

    public static final ActionFactory RESET_POSITION = new ActionFactory(
            "org.xmind.ui.resetPosition") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.ResetPosition_text);
            action.setToolTipText(MindMapMessages.ResetPosition_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.resetPosition"); //$NON-NLS-1$
            return action;
        }
    };

    public static final ActionFactory MOVE_UP = new ActionFactory(
            "org.xmind.ui.moveUp") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.MoveUp_text);
            action.setToolTipText(MindMapMessages.MoveUp_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.moveUp"); //$NON-NLS-1$
            return action;
        }
    };

    public static final ActionFactory MOVE_DOWN = new ActionFactory(
            "org.xmind.ui.moveDown") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.MoveDown_text);
            action.setToolTipText(MindMapMessages.MoveDown_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.moveDown"); //$NON-NLS-1$
            return action;
        }
    };

    public static final ActionFactory MOVE_LEFT = new ActionFactory(
            "org.xmind.ui.moveLeft") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.MoveLeft_text);
            action.setToolTipText(MindMapMessages.MoveLeft_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.moveLeft"); //$NON-NLS-1$
            return action;
        }
    };

    public static final ActionFactory MOVE_RIGHT = new ActionFactory(
            "org.xmind.ui.moveRight") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.MoveRight_text);
            action.setToolTipText(MindMapMessages.MoveRight_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.moveRight"); //$NON-NLS-1$
            return action;
        }
    };

    public static final ActionFactory DUPLICATE = new ActionFactory(
            "org.xmind.ui.duplicate", "org.xmind.ui.command.duplicate") { //$NON-NLS-1$ //$NON-NLS-2$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null)
                throw new IllegalArgumentException();
            LabelRetargetAction action = new LabelRetargetAction(getId(),
                    MindMapMessages.MindMapActionFactory_Duplicate_text);
            action.setToolTipText(
                    MindMapMessages.MindMapActionFactory_Duplicate_tooltip);
            action.setActionDefinitionId(getCommandId());
            action.setImageDescriptor(MindMapUI.getImages()
                    .get(IMindMapImages.INSERT_BEFORE, true));
            action.setDisabledImageDescriptor(MindMapUI.getImages()
                    .get(IMindMapImages.INSERT_BEFORE, false));
            return action;
        }
    };

    public static final ActionFactory NEW_SHEET_FROM_TEMPLATE = new ActionFactory(
            "org.xmind.ui.newSheetFromTemplate") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.MindMapActionFactory_NewSheetFromTemplte_text);
            action.setToolTipText(
                    MindMapMessages.MindMapActionFactory_NewSheetFromTemplate_tooltip);
            action.setActionDefinitionId(
                    "org.xmind.ui.command.newSheetFromTemplate"); //$NON-NLS-1$
            action.setImageDescriptor(MindMapUI.getImages()
                    .get(IMindMapImages.NEW_SHEET_FROM_TEMPLATE, true));
            action.setDisabledImageDescriptor(MindMapUI.getImages()
                    .get(IMindMapImages.NEW_SHEET_FROM_TEMPLATE, false));
            return action;
        }
    };

    public static final ActionFactory DUPLICATE_SHEET = new ActionFactory(
            "org.xmind.ui.duplicateSheet") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.DuplicateSheet_text);
            action.setToolTipText(MindMapMessages.DuplicateSheet_toolTip);
            action.setActionDefinitionId("org.xmind.ui.command.duplicateSheet"); //$NON-NLS-1$
            return action;
        }
    };

    public static final ActionFactory COPY_SHEET = new ActionFactory(
            "org.xmind.ui.copySheet") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.CopySheet_text);
            action.setToolTipText(WorkbenchMessages.Workbench_copyToolTip);
            action.setActionDefinitionId("org.xmind.ui.command.copySheet"); //$NON-NLS-1$
            return action;
        }
    };

    public static final ActionFactory PASTE_SHEET = new ActionFactory(
            "org.xmind.ui.pasteSheet") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.PasteSheet_text);
            action.setToolTipText(WorkbenchMessages.Workbench_pasteToolTip);
            action.setActionDefinitionId("org.xmind.ui.command.pasteSheet"); //$NON-NLS-1$
            return action;
        }
    };

    public static final ActionFactory REMOVE_ALL_STYLES = new ActionFactory(
            "org.xmindl.ui.removeAllStyles") { //$NON-NLS-1$
        public IWorkbenchAction create(IWorkbenchWindow window) {
            RetargetAction action = new RetargetAction(getId(),
                    MindMapMessages.RemoveAllStyles_text);
            action.setToolTipText(MindMapMessages.RemoveAllStyles_tooltip);
            action.setActionDefinitionId(
                    "org.xmind.ui.command.removeAllStyles"); //$NON-NLS-1$
            return action;
        }
    };
}
