/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
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
package org.xmind.ui.internal.editor;

import static org.eclipse.ui.IWorkbenchActionConstants.MB_ADDITIONS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.xmind.core.INamed;
import org.xmind.core.ISheet;
import org.xmind.core.ITitled;
import org.xmind.core.IWorkbook;
import org.xmind.core.marker.IMarkerRef;
import org.xmind.gef.ui.editor.GraphicalEditorActionBarContributor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.actions.DelegatingAction;
import org.xmind.ui.actions.MindMapActionFactory;
import org.xmind.ui.internal.IActionBuilder;
import org.xmind.ui.internal.ImageActionExtensionManager;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.actions.ActionConstants;
import org.xmind.ui.internal.actions.AddMarkerHandler;
import org.xmind.ui.internal.actions.AlignmentAction;
import org.xmind.ui.internal.actions.AllMarkersMenu;
import org.xmind.ui.internal.actions.DropDownInsertImageAction;
import org.xmind.ui.internal.actions.FindReplaceAction;
import org.xmind.ui.internal.actions.GroupMarkers;
import org.xmind.ui.internal.actions.RenameSheetAction;
import org.xmind.ui.internal.actions.SaveSheetAsAction;
import org.xmind.ui.internal.actions.SortAction;
import org.xmind.ui.internal.actions.StructureMenu;
import org.xmind.ui.mindmap.ICategoryAnalyzation;
import org.xmind.ui.mindmap.ICategoryManager;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class MindMapContributor extends GraphicalEditorActionBarContributor
        implements ISelectionListener {

    protected static class Contributor {

        private String id;

        private String name;

        private ImageDescriptor icon;

        private List<Object> items;

        public Contributor(String id) {
            this(id, null, null);
        }

        /**
         *
         */
        public Contributor(String id, String name) {
            this(id, name, null);
        }

        public Contributor(String id, String name, ImageDescriptor icon) {
            this.id = id;
            this.name = name;
            this.icon = icon;
        }

        /**
         * @return the icon
         */
        public ImageDescriptor getIcon() {
            return icon;
        }

        /**
         * @param icon
         *            the icon to set
         */
        public void setIcon(ImageDescriptor icon) {
            this.icon = icon;
        }

        /**
         * @return the id
         */
        public String getId() {
            return id;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @param name
         *            the name to set
         */
        public void setName(String name) {
            this.name = name;
        }

        public void add(IAction action) {
            addItem(action);
        }

        public void add(IContributionItem item) {
            addItem(item);
        }

        public void add(Contributor subContributor) {
            addItem(subContributor);
        }

        public void clear() {
            items = null;
        }

        protected void addItem(Object item) {
            if (items == null)
                items = new ArrayList<Object>();
            items.add(item);
        }

        public void applyTo(IMenuManager manager) {
            if (items == null)
                return;

            for (Object item : items) {
                if (item instanceof IAction) {
                    manager.add((IAction) item);
                } else if (item instanceof IContributionItem) {
                    manager.add((IContributionItem) item);
                } else if (item instanceof Contributor) {
                    Contributor subContributor = (Contributor) item;
                    MenuManager subManager = new MenuManager(
                            subContributor.getName(), subContributor.getIcon(),
                            subContributor.getId());
                    subContributor.applyTo(subManager);
                    manager.add(subManager);
                }
            }
        }

    }

    private class ContentPopupContributor extends Contributor {

        /**
         *
         */
        public ContentPopupContributor() {
            super(null);
        }

        void build(ICategoryManager categoryManager,
                ICategoryAnalyzation categories) {
            clear();

            if (categories == null)
                return;

            String category = categories.getMainCategory();
            if (MindMapUI.CATEGORY_TOPIC.equals(category)) {
                if (categories.size() > 1) {
                    buildMultipleTopicsPopupActions();
                } else {
                    buildSingleTopicPopupActions();
                }
            } else if (MindMapUI.CATEGORY_SHEET.equals(category)) {
                buildSheetPopupActions();
            } else if (MindMapUI.CATEGORY_BOUNDARY.equals(category)) {
                buildBoundaryPopupActions();
            } else if (MindMapUI.CATEGORY_RELATIONSHIP.equals(category)) {
                buildRelationshipPopupActions();
            } else if (MindMapUI.CATEGORY_MARKER.equals(category)) {
                buildMarkerPopupActions(categories);
            } else if (MindMapUI.CATEGORY_IMAGE.equals(category)) {
                buildImagePopupActions();
            }
        }

        private void buildSingleTopicPopupActions() {
            add(drillDownAction);
            add(new Separator());

            add(createPopupInsertMenu());
            add(getPopupStructureMenu());
//            add(getPopupAllMarkersMenu());
            add(new Separator());

            add(cutAction);
            add(copyAction);
            add(pasteAction);
            add(duplicateAction);
            add(deleteAction);
            add(new Separator());

            add(extendAllAction);
            add(collapseAllAction);
            add(new Separator());

//            add(drillDownAction);
            add(new Separator());

            add(resetPositionAction);
            add(getPopupSortGroup());
            add(new Separator());

            add(editNotesAction);
            add(modifyHyperlinkAction);
            //task info
//            add(showTaskInfoAction);
            add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
            add(new Separator());

            add(propertiesAction);

//            add(popupEditAction);
//            add(editLabelAction);
//
//            add(cancelHyperlinkAction);
//
//            add(openHyperlinkAction);
//            add(new GroupMarker("org.xmind.ui.popup.hyperlinkGroup")); //$NON-NLS-1$
//            add(saveAttachmentAsAction);
//            add(new GroupMarker("org.xmind.ui.popup.attachmentGroup")); //$NON-NLS-1$
//            add(new Separator());
//
//            add(new Separator());
//
//            add(new Separator(ActionConstants.CM_ADDITIONS));
//            add(new Separator());
        }

        private void buildMultipleTopicsPopupActions() {
            add(createBoundaryAction);
            add(createSummaryAction);
            add(new Separator());

//            add(structureMenu);

//            add(popupAllMarkersMenu);
//            add(getPopupAllMarkersMenu());
            add(new Separator());

            add(cutAction);
            add(copyAction);
            add(pasteAction);
            add(deleteAction);
            add(new Separator());

            add(extendAllAction);
            add(collapseAllAction);
            add(new Separator());

            add(resetPositionAction);
            add(new Separator());

            add(getPopupAlignmentGroup());
            add(new Separator());

            add(new Separator(ActionConstants.CM_ADDITIONS));
            add(new Separator());
            add(propertiesAction);
        }

        private void buildSheetPopupActions() {
            add(insertFloatingTopicAction);
            add(insertFloatingCentralTopicAction);
            add(createRelationshipAction);
            add(createBoundaryAction);
            add(createSummaryAction);
            add(new Separator());

            add(pasteAction);
            add(new Separator());

            add(extendAllAction);
            add(collapseAllAction);
            add(new Separator());

            add(drillUpAction);
            add(new Separator());

            add(allowOverlapsAction);
            add(tileAction);
            add(resetPositionAction);
            add(new Separator());

            add(new Separator(ActionConstants.CM_ADDITIONS));
            add(new Separator());
            add(propertiesAction);
        }

        private void buildBoundaryPopupActions() {
            add(popupEditAction);
            add(new Separator());

            add(cutAction);
            add(copyAction);
            add(deleteAction);
            add(new Separator());

            add(createRelationshipAction);
            add(new Separator());

            add(new Separator(ActionConstants.CM_ADDITIONS));
            add(new Separator());
            add(propertiesAction);
        }

        private void buildRelationshipPopupActions() {
            add(popupEditAction);
            add(new Separator());

            add(deleteAction);
            add(new Separator());

            add(insertTopicAction);
            add(new Separator());

            add(resetPositionAction);
            add(new Separator());

            add(new Separator(ActionConstants.CM_ADDITIONS));
            add(new Separator());
            add(propertiesAction);
        }

        private void buildMarkerPopupActions(ICategoryAnalyzation categories) {
            add(cutAction);
            add(copyAction);
            add(deleteAction);
            add(new Separator());

            groupMarkers.setSourceMarkerRef((IMarkerRef) categories
                    .getElements()[0]);
            add(groupMarkers);
            add(new Separator());

            add(new Separator(ActionConstants.CM_ADDITIONS));
            add(new Separator());
//            add(propertiesAction);
        }

        private void buildImagePopupActions() {
            add(cutAction);
            add(copyAction);
            add(deleteAction);
            add(new Separator());

            add(new Separator(ActionConstants.CM_ADDITIONS));
            add(new Separator());
            add(propertiesAction);
        }

    }

    private ISelectionService selectionService;

    private IWorkbenchAction selectBrothersAction;
    private IWorkbenchAction selectChildrenAction;
    private IWorkbenchAction goHomeAction;

    private IWorkbenchAction zoomInAction;
    private IWorkbenchAction zoomOutAction;
    private IWorkbenchAction actualSizeAction;
    private IWorkbenchAction fitMapAction;
    private IWorkbenchAction fitSelectionAction;
    private IWorkbenchAction drillDownAction;
    private IWorkbenchAction drillUpAction;

    private IWorkbenchAction insertTopicAction;
    private IWorkbenchAction insertSubtopicAction;
    private IWorkbenchAction insertTopicBeforeAction;
    private IWorkbenchAction insertParentTopicAction;
    private IWorkbenchAction insertFloatingTopicAction;
    private IWorkbenchAction insertFloatingCentralTopicAction;

    private IWorkbenchAction insertSheetAction;

    private IWorkbenchAction extendAction;
    private IWorkbenchAction collapseAction;
    private IWorkbenchAction extendAllAction;
    private IWorkbenchAction collapseAllAction;

    private IWorkbenchAction modifyHyperlinkAction;
    private IWorkbenchAction openHyperlinkAction;
    private IWorkbenchAction cancelHyperlinkAction;
    private IWorkbenchAction saveAttachmentAsAction;

    private IWorkbenchAction insertAttachmentAction;
    private IWorkbenchAction insertImageAction;

    private MenuManager alignmentGroup;
    private Contributor popupAlignGroup;
    private Map<Integer, AlignmentAction> alignmentActions;

    private MenuManager sortGroup;
    private Contributor popupSortGroup;
    private Map<String, SortAction> sortActions;

    private IWorkbenchAction newSheetAction;
    private IWorkbenchAction deleteSheetAction;
    private IWorkbenchAction deleteOtherSheetAction;

    private IWorkbenchAction allowOverlapsAction;
    private IWorkbenchAction tileAction;
    private IWorkbenchAction resetPositionAction;

    private IWorkbenchAction createSummaryAction;
    private IWorkbenchAction createRelationshipAction;
    private IWorkbenchAction createBoundaryAction;

    private IWorkbenchAction editTitleAction;
    private IWorkbenchAction popupEditAction;
    private IWorkbenchAction editLabelAction;
    private IWorkbenchAction editNotesAction;

    private IWorkbenchAction traverseAction;
    private IWorkbenchAction finishAction;

    private IWorkbenchAction findReplaceAction;

    private AllMarkersMenu allMarkersMenu;
    private AllMarkersMenu popupAllMarkersMenu;
    private StructureMenu structureMenu;
    private GroupMarkers groupMarkers;

    private SaveSheetAsAction saveSheetAsAction;
    private RenameSheetAction renameSheetAction;

    private IWorkbenchAction saveAsTemplateAction;

    // Global actions:
    private IWorkbenchAction deleteAction;
    private IWorkbenchAction copyAction;
    private IWorkbenchAction cutAction;
    private IWorkbenchAction pasteAction;
    private IWorkbenchAction propertiesAction;

    private IWorkbenchAction duplicateAction;

//    private IWorkbenchAction showTaskInfoAction;
    private IWorkbenchAction newSheetFromTemplateAction;

    private DropDownInsertImageAction dropDownInsertImageAction;

    private IWorkbenchAction removeAllStylesAction;

    private IHandlerService handlerService;

    private Map<IAction, IHandlerActivation> actionHandlerActivations;

    private AddMarkerHandler addMarkerHandler;

    private ContentPopupContributor contentPopupContributor;

    private IGraphicalEditorPage page;

    public void init(IActionBars bars, IWorkbenchPage page) {
        this.handlerService = (IHandlerService) page.getWorkbenchWindow()
                .getService(IHandlerService.class);
        if (this.handlerService != null) {
            this.actionHandlerActivations = new HashMap<IAction, IHandlerActivation>(
                    33);
        } else {
            this.actionHandlerActivations = null;
        }

        if (selectionService != null)
            selectionService.removeSelectionListener(this);
        selectionService = page.getWorkbenchWindow().getSelectionService();
        selectionService.addSelectionListener(this);

        super.init(bars, page);

    }

    protected void declareGlobalActionIds() {
        addGlobalActionId(ActionFactory.UNDO.getId());
        addGlobalActionId(ActionFactory.REDO.getId());
        addGlobalActionId(ActionFactory.SELECT_ALL.getId());
        addGlobalActionId(ActionFactory.PRINT.getId());
    }

    protected void makeActions() {
        IWorkbenchWindow window = getPage().getWorkbenchWindow();

        selectBrothersAction = MindMapActionFactory.SELECT_BROTHERS
                .create(window);
        addRetargetAction((RetargetAction) selectBrothersAction);
        selectChildrenAction = MindMapActionFactory.SELECT_CHILDREN
                .create(window);
        addRetargetAction((RetargetAction) selectChildrenAction);
        goHomeAction = MindMapActionFactory.GO_HOME.create(window);
        addRetargetAction((RetargetAction) goHomeAction);

        zoomInAction = MindMapActionFactory.ZOOM_IN.create(window);
        addRetargetAction((RetargetAction) zoomInAction);
        zoomOutAction = MindMapActionFactory.ZOOM_OUT.create(window);
        addRetargetAction((RetargetAction) zoomOutAction);
        actualSizeAction = MindMapActionFactory.ACTUAL_SIZE.create(window);
        addRetargetAction((RetargetAction) actualSizeAction);
        fitMapAction = MindMapActionFactory.FIT_MAP.create(window);
        addRetargetAction((RetargetAction) fitMapAction);
        fitSelectionAction = MindMapActionFactory.FIT_SELECTION.create(window);
        addRetargetAction((RetargetAction) fitSelectionAction);
        drillDownAction = MindMapActionFactory.DRILL_DOWN.create(window);
        addRetargetAction((RetargetAction) drillDownAction);
        drillUpAction = MindMapActionFactory.DRILL_UP.create(window);
        addRetargetAction((RetargetAction) drillUpAction);

        insertSheetAction = MindMapActionFactory.INSERT_SHEET_FROM
                .create(window);
        addRetargetAction((RetargetAction) insertSheetAction);

        insertTopicAction = MindMapActionFactory.INSERT_TOPIC.create(window);
        addRetargetAction((RetargetAction) insertTopicAction);

        insertSubtopicAction = MindMapActionFactory.INSERT_SUBTOPIC
                .create(window);
        addRetargetAction((RetargetAction) insertSubtopicAction);

        insertTopicBeforeAction = MindMapActionFactory.INSERT_TOPIC_BEFORE
                .create(window);
        addRetargetAction((RetargetAction) insertTopicBeforeAction);
        insertParentTopicAction = MindMapActionFactory.INSERT_PARENT_TOPIC
                .create(window);
        addRetargetAction((RetargetAction) insertParentTopicAction);
        insertFloatingTopicAction = MindMapActionFactory.INSERT_FLOATING_TOPIC
                .create(window);
        addRetargetAction((RetargetAction) insertFloatingTopicAction);
        insertFloatingCentralTopicAction = MindMapActionFactory.INSERT_FLOATING_CENTRAL_TOPIC
                .create(window);
        addRetargetAction((RetargetAction) insertFloatingCentralTopicAction);

        extendAction = MindMapActionFactory.EXTEND.create(window);
        addRetargetAction((RetargetAction) extendAction);
        collapseAction = MindMapActionFactory.COLLAPSE.create(window);
        addRetargetAction((RetargetAction) collapseAction);
        extendAllAction = MindMapActionFactory.EXTEND_ALL.create(window);
        addRetargetAction((RetargetAction) extendAllAction);
        collapseAllAction = MindMapActionFactory.COLLAPSE_ALL.create(window);
        addRetargetAction((RetargetAction) collapseAllAction);

        modifyHyperlinkAction = MindMapActionFactory.MODIFY_HYPERLINK
                .create(window);
        addRetargetAction((RetargetAction) modifyHyperlinkAction);
        openHyperlinkAction = MindMapActionFactory.OPEN_HYPERLINK
                .create(window);
        addRetargetAction((RetargetAction) openHyperlinkAction);

        cancelHyperlinkAction = MindMapActionFactory.CANCEL_HYPERLINK
                .create(window);
        addRetargetAction((RetargetAction) cancelHyperlinkAction);

        saveAttachmentAsAction = MindMapActionFactory.SAVE_ATTACHMENT_AS
                .create(window);
        addRetargetAction((RetargetAction) saveAttachmentAsAction);

        insertAttachmentAction = MindMapActionFactory.INSERT_ATTACHMENT
                .create(window);
        addRetargetAction((RetargetAction) insertAttachmentAction);
        insertImageAction = MindMapActionFactory.INSERT_IMAGE.create(window);
        addRetargetAction((RetargetAction) insertImageAction);
        newSheetAction = MindMapActionFactory.NEW_SHEET.create(window);
        addRetargetAction((RetargetAction) newSheetAction);
        deleteSheetAction = MindMapActionFactory.DELETE_SHEET.create(window);
        addRetargetAction((RetargetAction) deleteSheetAction);
        deleteOtherSheetAction = MindMapActionFactory.DELETE_OTHER_SHEET
                .create(window);
        addRetargetAction((RetargetAction) deleteOtherSheetAction);

        createRelationshipAction = MindMapActionFactory.CREATE_RELATIONSHIP
                .create(window);
        addRetargetAction((RetargetAction) createRelationshipAction);
        createBoundaryAction = MindMapActionFactory.CREATE_BOUNDARY
                .create(window);
        addRetargetAction((RetargetAction) createBoundaryAction);
        createSummaryAction = MindMapActionFactory.CREATE_SUMMARY
                .create(window);
        addRetargetAction((RetargetAction) createSummaryAction);

        editTitleAction = MindMapActionFactory.EDIT_TITLE.create(window);
        addRetargetAction((RetargetAction) editTitleAction);

        popupEditAction = new DelegatingAction(editTitleAction,
                IAction.ENABLED, IAction.CHECKED, IAction.IMAGE);
        popupEditAction.setId("org.xmind.ui.edit.popup"); //$NON-NLS-1$
        popupEditAction.setText(MindMapMessages.Edit_text);
        popupEditAction.setToolTipText(MindMapMessages.Edit_toolTip);

        editLabelAction = MindMapActionFactory.EDIT_LABEL.create(window);
        addRetargetAction((RetargetAction) editLabelAction);
        editNotesAction = MindMapActionFactory.EDIT_NOTES.create(window);
        addRetargetAction((RetargetAction) editNotesAction);

        traverseAction = MindMapActionFactory.TRAVERSE.create(window);
        addRetargetAction((RetargetAction) traverseAction);
        finishAction = MindMapActionFactory.FINISH.create(window);
        addRetargetAction((RetargetAction) finishAction);

        removeAllStylesAction = MindMapActionFactory.REMOVE_ALL_STYLES
                .create(window);
        addRetargetAction((RetargetAction) removeAllStylesAction);

//        IPreferenceStore prefStore = MindMapUIPlugin.getDefault()
//                .getPreferenceStore();
//        allowOverlapsAction = new AllowOverlapsAction(prefStore);
//        addAction(allowOverlapsAction);

        tileAction = MindMapActionFactory.TILE.create(window);
        addRetargetAction((RetargetAction) tileAction);

        resetPositionAction = MindMapActionFactory.RESET_POSITION
                .create(window);
        addRetargetAction((RetargetAction) resetPositionAction);

        newSheetFromTemplateAction = MindMapActionFactory.NEW_SHEET_FROM_TEMPLATE
                .create(window);
        addRetargetAction((RetargetAction) newSheetFromTemplateAction);

        if (handlerService != null) {
            addMarkerHandler = new AddMarkerHandler();
            addMarkerHandler.activate(handlerService);
        }

        findReplaceAction = new FindReplaceAction(window);
        addAction(findReplaceAction);

        saveSheetAsAction = new SaveSheetAsAction();

        renameSheetAction = new RenameSheetAction();
        groupMarkers = new GroupMarkers();

        saveAsTemplateAction = MindMapActionFactory.SAVE_TEMPLATE
                .create(window);
        addAction(saveAsTemplateAction);

        deleteAction = ActionFactory.DELETE.create(window);
        addRetargetAction((RetargetAction) deleteAction);
        copyAction = ActionFactory.COPY.create(window);
        addRetargetAction((RetargetAction) copyAction);
        cutAction = ActionFactory.CUT.create(window);
        addRetargetAction((RetargetAction) cutAction);
        pasteAction = ActionFactory.PASTE.create(window);
        addRetargetAction((RetargetAction) pasteAction);
        propertiesAction = ActionFactory.PROPERTIES.create(window);
        addRetargetAction((RetargetAction) propertiesAction);

        duplicateAction = MindMapActionFactory.DUPLICATE.create(window);
        addRetargetAction((RetargetAction) duplicateAction);

        addRetargetAction((RetargetAction) MindMapActionFactory.MOVE_UP
                .create(window));
        addRetargetAction((RetargetAction) MindMapActionFactory.MOVE_DOWN
                .create(window));
        addRetargetAction((RetargetAction) MindMapActionFactory.MOVE_LEFT
                .create(window));
        addRetargetAction((RetargetAction) MindMapActionFactory.MOVE_RIGHT
                .create(window));

//        contentPopupContributor = new ContentPopupContributor();

        List<IActionBuilder> imageActionBuilders = ImageActionExtensionManager
                .getInstance().getActionBuilders();
        List<IWorkbenchAction> imageActionExtensions = new ArrayList<IWorkbenchAction>(
                imageActionBuilders.size());
        for (IActionBuilder builder : imageActionBuilders) {
            IWorkbenchAction imageActionExtension = builder
                    .createAction(getPage());
            imageActionExtensions.add(imageActionExtension);
            addAction(imageActionExtension);
        }

        if (imageActionExtensions.size() > 0) {
            imageActionExtensions.add(0, insertImageAction);
            dropDownInsertImageAction = new DropDownInsertImageAction(
                    insertImageAction, imageActionExtensions);
            dropDownInsertImageAction.setText(insertImageAction.getText());
            dropDownInsertImageAction.setToolTipText(insertImageAction
                    .getToolTipText());
            dropDownInsertImageAction.setImageDescriptor(insertImageAction
                    .getImageDescriptor());
            dropDownInsertImageAction
                    .setDisabledImageDescriptor(insertImageAction
                            .getDisabledImageDescriptor());
            insertImageAction.setText(MindMapMessages.InsertImageFromFile_text);
            insertImageAction
                    .setToolTipText(MindMapMessages.InsertImageFromFile_toolTip);
            insertImageAction.setImageDescriptor(null);
            insertImageAction.setDisabledImageDescriptor(null);
        }
    }

    private IAction getInsertImageAction() {
        return dropDownInsertImageAction != null ? dropDownInsertImageAction
                : insertImageAction;
    }

    public void init(IActionBars bars) {
        super.init(bars);
        bars.setGlobalActionHandler(ActionFactory.FIND.getId(),
                findReplaceAction);
    }

    protected void addAction(IAction action) {
        super.addAction(action);
        activateHandler(action);
    }

//    public void contributeToMenu(IMenuManager menuManager) {
//        super.contributeToMenu(menuManager);
//
//        IMenuManager editMenu = menuManager.findMenuUsingPath(M_EDIT);
//        if (editMenu != null) {
//            IContributionItem selectAllItem = editMenu
//                    .find(ActionFactory.SELECT_ALL.getId());
//            if (selectAllItem != null) {
//                editMenu.insertBefore(selectAllItem.getId(), goHomeAction);
//                editMenu.insertAfter(selectAllItem.getId(),
//                        selectChildrenAction);
//                editMenu.insertAfter(selectAllItem.getId(),
//                        selectBrothersAction);
//            }
//        }
//
//        boolean useMindMapOrView = hasExistingShowViewMenu(menuManager);
//
//        IMenuManager viewMenu = createMenu(ActionConstants.M_VIEW,
//                useMindMapOrView ? MindMapMessages.MindMapMenu
//                        : MindMapMessages.ViewMenu);
//        addViewActions(viewMenu, useMindMapOrView);
//
//        IMenuManager insertMenu = createMenu(ActionConstants.M_INSERT,
//                MindMapMessages.InsertMenu);
//        addInsertActions(insertMenu);
//
//        IMenuManager modifyMenu = createMenu(ActionConstants.M_MODIFY,
//                MindMapMessages.ModifyMenu);
//        addModifyActions(modifyMenu);
//
//        IMenuManager toolsMenu = createMenu(ActionConstants.M_TOOLS,
//                MindMapMessages.ToolsMenu);
//        addToolsActions(toolsMenu);
//
//        IMenuManager fileMenu = menuManager.findMenuUsingPath(M_FILE);
//
//        if (fileMenu != null) {
//            insertSaveContributions(fileMenu);
//        }
//
//        if (menuManager.find(MB_ADDITIONS) != null) {
//            menuManager.prependToGroup(MB_ADDITIONS, toolsMenu);
//            menuManager.prependToGroup(MB_ADDITIONS, modifyMenu);
//            menuManager.prependToGroup(MB_ADDITIONS, insertMenu);
//            menuManager.prependToGroup(MB_ADDITIONS, viewMenu);
//        } else {
//            menuManager.add(viewMenu);
//            menuManager.add(insertMenu);
//            menuManager.add(modifyMenu);
//            menuManager.add(toolsMenu);
//        }
//    }
//
//    private void insertSaveContributions(IMenuManager menu) {
//        IContributionItem saveItem = menu.find(ActionFactory.SAVE.getId());
//        if (saveItem != null) {
//            IContributionItem lastItem = null;
//            for (IContributionItem item : menu.getItems()) {
//                if (lastItem != null) {
//                    if (item.isSeparator() || item.getId() == null) {
//                        String id = lastItem.getId();
//                        menu.insertAfter(id, saveAsTemplateAction);
//                        menu.insertAfter(id, saveSheetAsAction);
//                        return;
//                    } else {
//                        lastItem = item;
//                    }
//                } else if (item == saveItem) {
//                    lastItem = item;
//                }
//            }
//            if (lastItem != null) {
//                String id = lastItem.getId();
//                menu.insertAfter(id, saveAsTemplateAction);
//                menu.insertAfter(id, saveSheetAsAction);
//            }
//        }
//        if (menu.find(SAVE_EXT) != null) {
//            menu.prependToGroup(SAVE_EXT, saveAsTemplateAction);
//            menu.prependToGroup(SAVE_EXT, saveSheetAsAction);
//        } else {
//            menu.prependToGroup(MB_ADDITIONS, saveAsTemplateAction);
//            menu.prependToGroup(MB_ADDITIONS, saveSheetAsAction);
//        }
//    }
//
//    private boolean hasExistingShowViewMenu(IMenuManager menuManager) {
//        return menuManager.findUsingPath("window/showView") != null; //$NON-NLS-1$
//    }
//
//    private IMenuManager createMenu(String menuId, String menuName) {
//        return new MenuManager(menuName, menuId);
//    }
//
//    private void addViewActions(IMenuManager menu, boolean useMindMapOrView) {
//        menu.add(new GroupMarker(ActionConstants.VIEW_START));
//
//        menu.add(new GroupMarker(ActionConstants.GROUP_PRESENTATION));
//        menu.add(new Separator());
//
//        menu.add(actualSizeAction);
//        menu.add(zoomOutAction);
//        menu.add(zoomInAction);
//        menu.add(new Separator());
//
//        menu.add(fitMapAction);
//        menu.add(fitSelectionAction);
//        menu.add(new Separator());
//
//        menu.add(drillUpAction);
//        menu.add(drillDownAction);
//        menu.add(new Separator());
//
//        menu.add(new GroupMarker(ActionConstants.GROUP_FILTER));
//        menu.add(new Separator());
//
//        menu.add(new Separator(MB_ADDITIONS));
//        menu.add(new Separator());
//
//        menu.add(createViewList(useMindMapOrView));
//        menu.add(new GroupMarker(ActionConstants.VIEW_END));
//    }
//
//    private IContributionItem createViewList(boolean mindMapOrSystem) {
//        if (mindMapOrSystem)
//            return new MindMapViewsMenu(getPage().getWorkbenchWindow());
//        IContributionItem contributeItem = ContributionItemFactory.VIEWS_SHORTLIST
//                .create(getPage().getWorkbenchWindow());
//        return contributeItem;
//    }
//
//    private void addInsertActions(IMenuManager menu) {
//        menu.add(new GroupMarker(ActionConstants.INSERT_START));
//
//        menu.add(new GroupMarker(ActionConstants.GROUP_INSERT_TOPIC));
//
//        menu.add(insertTopicAction);
//        menu.add(insertSubtopicAction);
//        menu.add(insertTopicBeforeAction);
//        menu.add(insertParentTopicAction);
//
//        menu.add(insertFloatingTopicAction);
//        menu.add(insertFloatingCentralTopicAction);
//        menu.add(new GroupMarker(ActionConstants.INSERT_TOPIC_EXT));
//        menu.add(new Separator());
//
//        menu.add(new GroupMarker(ActionConstants.GROUP_INSERT));
//        menu.add(getAllMarkersMenu());
//        menu.add(getInsertImageAction());
//        menu.add(createRelationshipAction);
//        menu.add(createBoundaryAction);
//        menu.add(createSummaryAction);
//        menu.add(insertAttachmentAction);
//        menu.add(new GroupMarker(ActionConstants.INSERT_EXT));
//        menu.add(new Separator());
//
//        menu.add(new GroupMarker(ActionConstants.GROUP_SHEET));
//        menu.add(newSheetAction);
//        menu.add(insertSheetAction);
//        menu.add(deleteSheetAction);
//        menu.add(new GroupMarker(ActionConstants.SHEET_EXT));
//        menu.add(new Separator());
//
//        menu.add(new Separator(MB_ADDITIONS));
//
//        menu.add(new GroupMarker(ActionConstants.INSERT_END));
//    }
//
//    private void addModifyActions(IMenuManager menu) {
//        menu.add(new GroupMarker(ActionConstants.MODIFY_START));
//
//        menu.add(new GroupMarker(ActionConstants.GROUP_TOPIC_EDIT));
//        menu.add(editTitleAction);
//        menu.add(editLabelAction);
//        menu.add(editNotesAction);
//        menu.add(new GroupMarker(ActionConstants.TOPIC_EDIT_EXT));
//        menu.add(new Separator());
//
//        menu.add(new GroupMarker(ActionConstants.GROUP_HYPERLINK));
//        menu.add(modifyHyperlinkAction);
//        menu.add(cancelHyperlinkAction);
//
//        menu.add(openHyperlinkAction);
//        menu.add(saveAttachmentAsAction);
//        menu.add(new GroupMarker(ActionConstants.HYPERLINK_EXT));
//        menu.add(new Separator());
//
//        menu.add(new GroupMarker(ActionConstants.GROUP_EXTEND));
//        menu.add(extendAction);
//        menu.add(collapseAction);
//        menu.add(extendAllAction);
//        menu.add(collapseAllAction);
//        menu.add(new GroupMarker(ActionConstants.EXTEND_EXT));
//        menu.add(new Separator());
//
//        menu.add(new GroupMarker(ActionConstants.GROUP_POSITION));
//        menu.add(allowOverlapsAction);
//        menu.add(tileAction);
//        menu.add(resetPositionAction);
//        menu.add(new Separator());
//        menu.add(getAlignmentGroup());
//        menu.add(getSortGroup());
//        menu.add(new Separator());
//        menu.add(new GroupMarker(ActionConstants.POSITION_EXT));
//        menu.add(new Separator());
//
//        menu.add(new Separator(MB_ADDITIONS));
//
//        menu.add(new GroupMarker(ActionConstants.MODIFY_END));
//    }
//
//    private void addToolsActions(IMenuManager menu) {
//        menu.add(new GroupMarker(ActionConstants.TOOLS_START));
//
//        menu.add(new GroupMarker("additions1")); //$NON-NLS-1$
//        menu.add(new Separator());
//        menu.add(new GroupMarker("additions2")); //$NON-NLS-1$
//        menu.add(new Separator());
//        menu.add(new GroupMarker("additions3")); //$NON-NLS-1$
//        menu.add(new Separator());
//        menu.add(new GroupMarker("additions4")); //$NON-NLS-1$
//        menu.add(new ImportFromWorkbookAction());
//        menu.add(new Separator());
//        menu.add(new GroupMarker("additions5")); //$NON-NLS-1$
//        menu.add(new Separator());
//        menu.add(new GroupMarker("additions6")); //$NON-NLS-1$
//        menu.add(new Separator());
//        menu.add(new GroupMarker("additions7")); //$NON-NLS-1$
//        menu.add(new Separator());
//        menu.add(new GroupMarker("additions8")); //$NON-NLS-1$
//        menu.add(new Separator());
//        menu.add(new GroupMarker("additions9")); //$NON-NLS-1$
//        menu.add(new Separator());
//        menu.add(new GroupMarker("additions10")); //$NON-NLS-1$
//        menu.add(new Separator());
//        menu.add(new Separator(MB_ADDITIONS));
//        menu.add(new Separator());
//
//        menu.add(new GroupMarker(ActionConstants.TOOLS_END));
//    }
//
//    public void contributeToToolBar(IToolBarManager toolBarManager) {
//        super.contributeToToolBar(toolBarManager);
//        toolBarManager.add(new GroupMarker(ActionConstants.GROUP_PRESENTATION));
//        toolBarManager.add(new Separator());
//
//        toolBarManager.add(new GroupMarker(ActionConstants.GROUP_SHEET));
//        toolBarManager.add(newSheetAction);
//        toolBarManager.add(new GroupMarker(ActionConstants.SHEET_EXT));
//        toolBarManager.add(new Separator());
//
//        toolBarManager.add(new GroupMarker(ActionConstants.GROUP_INSERT_TOPIC));
//        toolBarManager.add(insertTopicAction);
//        toolBarManager.add(insertSubtopicAction);
//        toolBarManager.add(insertTopicBeforeAction);
//        toolBarManager.add(insertParentTopicAction);
//        toolBarManager.add(new GroupMarker(ActionConstants.INSERT_TOPIC_EXT));
//        toolBarManager.add(new Separator());
//
//        toolBarManager.add(new GroupMarker(ActionConstants.GROUP_INSERT));
//        toolBarManager.add(insertAttachmentAction);
//        toolBarManager.add(getInsertImageAction());
//        toolBarManager.add(editLabelAction);
//        toolBarManager.add(editNotesAction);
//        toolBarManager.add(modifyHyperlinkAction);
//        toolBarManager.add(createBoundaryAction);
//        toolBarManager.add(createSummaryAction);
//        toolBarManager.add(createRelationshipAction);
//        toolBarManager.add(new GroupMarker(ActionConstants.INSERT_EXT));
//        toolBarManager.add(new Separator());
//
//        toolBarManager.add(new GroupMarker(ActionConstants.GROUP_DRILL_DOWN));
//        toolBarManager.add(drillDownAction);
//        toolBarManager.add(drillUpAction);
//        toolBarManager.add(new GroupMarker(ActionConstants.DRILL_DOWN_EXT));
//    }
//
//    private MenuManager getAllMarkersMenu() {
//        if (allMarkersMenu == null)
//            allMarkersMenu = new AllMarkersMenu();
//        return allMarkersMenu;
//    }

//    private MenuManager getPopupAllMarkersMenu() {
//        if (popupAllMarkersMenu != null) {
//            popupAllMarkersMenu.dispose();
//            popupAllMarkersMenu = null;
//        }
//        popupAllMarkersMenu = new AllMarkersMenu();
//        popupAllMarkersMenu.setActivePage(page);
//        return popupAllMarkersMenu;
//    }

//    private MenuManager getAlignmentGroup() {
//        if (alignmentGroup == null)
//            alignmentGroup = makeAlignmentGroup();
//        return alignmentGroup;
//    }
//
//    private MenuManager getSortGroup() {
//        if (sortGroup == null)
//            sortGroup = makeSortGroup();
//        return sortGroup;
//    }

    private Contributor getPopupAlignmentGroup() {
        if (popupAlignGroup == null)
            popupAlignGroup = makeAlignmentGroup2();
        return popupAlignGroup;
    }

    private Contributor getPopupSortGroup() {
        if (popupSortGroup == null)
            popupSortGroup = makeSortGroup2();
        return popupSortGroup;
    }

//    private MenuManager makeAlignmentGroup() {
//        MenuManager menu = new MenuManager(MindMapMessages.AlignmentMenu,
//                ActionConstants.ALIGNMENT_GROUP_ID);
//        buildAlignmentGroup(menu);
//        return menu;
//    }
//
//    private MenuManager makeSortGroup() {
//        MenuManager menu = new MenuManager(MindMapMessages.SortMenu,
//                ActionConstants.SORT_GROUP_ID);
//        buildSortGroup(menu);
//        return menu;
//    }

    private Contributor makeAlignmentGroup2() {
        Contributor group = new Contributor(ActionConstants.ALIGNMENT_GROUP_ID,
                MindMapMessages.AlignmentMenu);
        buildAlignmentGroup(group);
        return group;
    }

    private Contributor makeSortGroup2() {
        Contributor group = new Contributor(ActionConstants.SORT_GROUP_ID,
                MindMapMessages.SortMenu);
        buildSortGroup(group);
        return group;
    }

    private void buildAlignmentGroup(Object manager) {
        addContribution(manager, new GroupMarker(
                ActionConstants.ALIGNMENT_START));
        addAlignmentAction(manager, PositionConstants.LEFT);
        addAlignmentAction(manager, PositionConstants.CENTER);
        addAlignmentAction(manager, PositionConstants.RIGHT);
        addContribution(manager, new Separator());
        addAlignmentAction(manager, PositionConstants.TOP);
        addAlignmentAction(manager, PositionConstants.MIDDLE);
        addAlignmentAction(manager, PositionConstants.BOTTOM);
        addContribution(manager, new Separator(MB_ADDITIONS));
        addContribution(manager, new GroupMarker(ActionConstants.ALIGNMENT_END));
    }

    private void buildSortGroup(Object manager) {
        addSortAction(manager, ActionConstants.SORT_TITLE_ID);
        addSortAction(manager, ActionConstants.SORT_PRIORITY_ID);
        addSortAction(manager, ActionConstants.SORT_MODIFIED_ID);
        addContribution(manager, new Separator(MB_ADDITIONS));
        addContribution(manager, new GroupMarker(ActionConstants.SORT_EXT));
    }

    private StructureMenu getPopupStructureMenu() {
        if (structureMenu != null) {
            structureMenu.dispose();
            structureMenu = null;
        }
        structureMenu = new StructureMenu();
        structureMenu.setActivePage(page);
        return structureMenu;
    }

    private Contributor createPopupInsertMenu() {
        Contributor popupInsertMenu = new Contributor(ActionConstants.M_INSERT,
                MindMapMessages.InsertMenu);
        fillPopupInsertMenu(popupInsertMenu);
        return popupInsertMenu;
    }

    private void fillPopupInsertMenu(Contributor menu) {
        menu.add(new GroupMarker(ActionConstants.INSERT_START));
        menu.add(new GroupMarker(ActionConstants.GROUP_INSERT_TOPIC));

        menu.add(insertTopicAction);
        menu.add(new Separator());
        menu.add(insertSubtopicAction);
        menu.add(insertTopicBeforeAction);
        menu.add(insertParentTopicAction);
        menu.add(new Separator());

        menu.add(new GroupMarker(ActionConstants.INSERT_TOPIC_EXT));
        menu.add(new Separator());
        menu.add(new GroupMarker(ActionConstants.GROUP_INSERT));

        menu.add(getInsertImageAction());
        menu.add(createRelationshipAction);
        menu.add(createBoundaryAction);
        menu.add(createSummaryAction);
        menu.add(editLabelAction);
        menu.add(insertAttachmentAction);
//        menu.add(new GroupMarker(ActionConstants.INSERT_EXT));
//        menu.add(new Separator());
//        menu.add(new GroupMarker(MB_ADDITIONS));
//        menu.add(new Separator());
//        menu.add(new GroupMarker(ActionConstants.INSERT_END));
        menu.add(new Separator());
        menu.add(insertSheetAction);
        menu.add(newSheetFromTemplateAction);
    }

    private void addContribution(Object manager, Object item) {
        if (manager instanceof IContributionManager) {
            if (item instanceof IAction)
                ((IContributionManager) manager).add((IAction) item);
            else if (item instanceof IContributionItem)
                ((IContributionManager) manager).add((IContributionItem) item);
        } else if (manager instanceof Contributor) {
            ((Contributor) manager).addItem(item);
        }
    }

    private void addAlignmentAction(Object menu, int alignment) {
        AlignmentAction action = createAlignmentAction(alignment);
        if (menu instanceof IMenuManager) {
            ((IMenuManager) menu).add(action);
        } else if (menu instanceof Contributor) {
            ((Contributor) menu).add(action);
        }
        addRetargetAction(action);
    }

    private void addSortAction(Object menu, String sortId) {
        SortAction action = createSortAction(sortId);
        if (menu instanceof IMenuManager) {
            ((IMenuManager) menu).add(action);
        } else if (menu instanceof Contributor) {
            ((Contributor) menu).add(action);
        }
        addRetargetAction(action);
    }

    /**
     * @param alignment
     * @return
     */
    private AlignmentAction createAlignmentAction(int alignment) {
        AlignmentAction action;
        if (alignmentActions != null) {
            action = alignmentActions.get(alignment);
            if (action != null)
                return action;
        }
        action = new AlignmentAction(alignment);
        if (alignmentActions == null) {
            alignmentActions = new HashMap<Integer, AlignmentAction>();
        }
        alignmentActions.put(alignment, action);
        return action;
    }

    private SortAction createSortAction(String sortId) {
        SortAction action;
        if (sortActions != null) {
            action = sortActions.get(sortId);
            if (action != null)
                return action;
        }
        action = new SortAction(sortId);
        if (sortActions == null) {
            sortActions = new HashMap<String, SortAction>();
        }
        sortActions.put(sortId, action);
        return action;
    }

    @Override
    public void setActiveEditor(IEditorPart targetEditor) {
        if (groupMarkers != null) {
            if (targetEditor == null) {
                groupMarkers.setSelectionProvider(null);
            } else {
                groupMarkers.setSelectionProvider(targetEditor.getSite()
                        .getSelectionProvider());
            }
        }
        super.setActiveEditor(targetEditor);
    }

    protected void activePageChanged(IGraphicalEditorPage page) {
        this.page = page;

        if (addMarkerHandler != null) {
            addMarkerHandler.setActivatePage(page);
        }
//        if (getAllMarkersMenu() != null) {
//            allMarkersMenu.setActivePage(page);
//        }
//        if (popupAllMarkersMenu != null) {
//        if (getPopupAllMarkersMenu() != null) {
//            popupAllMarkersMenu.setActivePage(page);
//        }
//        if (groupMarkers != null) {
//            groupMarkers.setSelectionProvider( getSelectionProvider());
//        }

//        if (structureMenu != null) {
//            structureMenu.setActivePage(page);
//        }
//        if (getPopupStructureMenu() != null)
//            getPopupStructureMenu().setActivePage(page);

        if (saveSheetAsAction != null) {
            saveSheetAsAction.setActivePage(page);
        }
        if (renameSheetAction != null) {
            renameSheetAction.setActivePage(page);
        }
    }

    protected void activateHandler(IAction action) {
        if (handlerService != null && actionHandlerActivations != null) {
            String commandId = action.getActionDefinitionId();
            if (commandId != null) {
                IHandlerActivation handlerActivation = handlerService
                        .activateHandler(commandId, new ActionHandler(action));
                actionHandlerActivations.put(action, handlerActivation);
            }
        }
    }

    protected void deactivateHandler(IAction action) {
        if (handlerService != null && actionHandlerActivations != null) {
            IHandlerActivation activation = actionHandlerActivations
                    .remove(action);
            if (activation != null) {
                handlerService.deactivateHandler(activation);
            }
        }
    }

    public void contributeToContentPopupMenu(IMenuManager menu) {
        if (contentPopupContributor != null) {
            contentPopupContributor.applyTo(menu);
        }
    }

    public void contributeToPagePopupMenu(IMenuManager menu) {
        menu.add(renameSheetAction);
        menu.add(saveSheetAsAction);
        menu.add(deleteSheetAction);
        menu.add(deleteOtherSheetAction);
        super.contributeToPagePopupMenu(menu);
        menu.add(new Separator());
        menu.add(propertiesAction);

        int sheetNum = getCurrentWorkbookSheetsNum();
        deleteSheetAction.setEnabled(sheetNum > 1);
        deleteOtherSheetAction.setEnabled(sheetNum > 1);
    }

    private int getCurrentWorkbookSheetsNum() {
        IWorkbook workbook = ((ISheet) page.getAdapter(ISheet.class))
                .getOwnedWorkbook();
        return workbook.getSheets().size();
    }

    public void dispose() {
        if (popupEditAction != null) {
            popupEditAction.dispose();
            popupEditAction = null;
        }
        if (alignmentGroup != null) {
            alignmentGroup.dispose();
            alignmentGroup = null;
        }
        if (sortGroup != null) {
            sortGroup.dispose();
            sortGroup = null;
        }
        popupAlignGroup = null;
        popupSortGroup = null;
        alignmentActions = null;
        sortActions = null;

        if (allMarkersMenu != null) {
            allMarkersMenu.dispose();
            allMarkersMenu = null;
        }
        if (popupAllMarkersMenu != null) {
            popupAllMarkersMenu.dispose();
            popupAllMarkersMenu = null;
        }
        if (structureMenu != null) {
            structureMenu.dispose();
            structureMenu = null;
        }
        if (groupMarkers != null) {
            groupMarkers.dispose();
            groupMarkers = null;
        }

        if (handlerService != null) {
            if (addMarkerHandler != null) {
                addMarkerHandler.deactivate(handlerService);
                addMarkerHandler.dispose();
                addMarkerHandler = null;
            }
            if (getActionRegistry() != null) {
                for (IAction action : getActionRegistry().getActions()) {
                    deactivateHandler(action);
                }
            }
            handlerService = null;
            actionHandlerActivations = null;
        }
        if (selectionService != null) {
            selectionService.removeSelectionListener(this);
            selectionService = null;
        }
        super.dispose();
    }

    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        ICategoryManager manager = MindMapUI.getCategoryManager();
        Object[] elements = (selection instanceof IStructuredSelection) ? ((IStructuredSelection) selection)
                .toArray() : null;
        ICategoryAnalyzation categories = elements == null ? null : manager
                .analyze(elements);
        updateStatusLine(manager, categories);
        updateContentPopupItems(manager, categories);
    }

    private void updateStatusLine(ICategoryManager categoryManager,
            ICategoryAnalyzation categories) {
        IStatusLineManager sl = getActionBars().getStatusLineManager();
        if (sl != null) {
            sl.setMessage(getStatusMessage(categoryManager, categories));
        }
    }

    private static String getStatusMessage(ICategoryManager categoryManager,
            ICategoryAnalyzation categories) {
        if (categories == null)
            return null;
        if (categories.isEmpty())
            return null;
        int size = categories.getElements().length;
        String m;
        if (categories.isMultiple()) {
            m = MindMapMessages.StatusLine_MultipleItems;
        } else {
            String type = categories.getMainCategory();
            if (ICategoryManager.UNKNOWN_CATEGORY.equals(type)) {
                m = ""; //$NON-NLS-1$
            } else {
                String name = categoryManager.getCategoryName(type);
                if (size == 1) {
                    Object ele = categories.getElements()[0];
                    String title = MindMapUtils.trimSingleLine(getTitle(ele));
                    if (title != null) {
                        m = NLS.bind(MindMapMessages.StatusLine_OneItemPattern,
                                name, title);
                    } else {
                        m = NLS.bind(
                                MindMapMessages.StatusLine_OneItemNoTitlePattern,
                                name);
                    }
                } else {
                    m = NLS.bind(
                            MindMapMessages.StatusLine_MultipleItemPattern,
                            size, name);
                }
            }
        }
        return m;
    }

    private static String getTitle(Object ele) {
        if (ele instanceof ITitled)
            return ((ITitled) ele).getTitleText();
        if (ele instanceof INamed)
            return ((INamed) ele).getName();
        if (ele instanceof IMarkerRef)
            return ((IMarkerRef) ele).getDescription();
        return ""; //$NON-NLS-1$
    }

    private void updateContentPopupItems(ICategoryManager categoryManager,
            ICategoryAnalyzation categories) {
        if (contentPopupContributor != null) {
            contentPopupContributor.build(categoryManager, categories);
        }
    }

}
