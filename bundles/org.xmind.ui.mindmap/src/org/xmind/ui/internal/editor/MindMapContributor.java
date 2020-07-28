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
package org.xmind.ui.internal.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IActionBars;
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
import org.xmind.ui.actions.MindMapActionFactory;
import org.xmind.ui.internal.IActionBuilder;
import org.xmind.ui.internal.ImageActionExtensionManager;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.actions.CopiedSheetStorageSupport;
import org.xmind.ui.internal.actions.DropDownInsertImageAction;
import org.xmind.ui.internal.actions.FindReplaceAction;
import org.xmind.ui.internal.actions.RenameSheetAction;
import org.xmind.ui.internal.actions.SaveSheetAsAction;
import org.xmind.ui.mindmap.ICategoryAnalyzation;
import org.xmind.ui.mindmap.ICategoryManager;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class MindMapContributor extends GraphicalEditorActionBarContributor
        implements ISelectionListener {

    private IWorkbenchAction selectBrothersAction;
    private IWorkbenchAction selectChildrenAction;
    private IWorkbenchAction goHomeAction;

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

    private IWorkbenchAction insertAttachmentAction;
    private IWorkbenchAction insertImageAction;

    private IWorkbenchAction newSheetAction;
    private IWorkbenchAction deleteSheetAction;
    private IWorkbenchAction deleteOtherSheetAction;

    private IWorkbenchAction duplicateSheetAction;
    private IWorkbenchAction copySheetAction;
    private IWorkbenchAction pasteSheetAction;

    private SaveSheetAsAction saveSheetAsAction;

    private IWorkbenchAction tileAction;
    private IWorkbenchAction resetPositionAction;

    private IWorkbenchAction createRelationshipAction;

    private IWorkbenchAction editTitleAction;
    private IWorkbenchAction editLabelAction;
    private IWorkbenchAction editNotesAction;

    private IWorkbenchAction traverseAction;
    private IWorkbenchAction finishAction;

    private IWorkbenchAction findReplaceAction;

    private RenameSheetAction renameSheetAction;

    // Global actions:
    private IWorkbenchAction deleteAction;
    private IWorkbenchAction copyAction;
    private IWorkbenchAction cutAction;
    private IWorkbenchAction pasteAction;
    private IWorkbenchAction propertiesAction;

    private IWorkbenchAction duplicateAction;

    private IWorkbenchAction dropDownInsertImageAction;

    private IWorkbenchAction removeAllStylesAction;

    private IHandlerService handlerService;

    private Map<IAction, IHandlerActivation> actionHandlerActivations;

    private IGraphicalEditorPage page;

    private ISelectionService selectionService;

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

        duplicateSheetAction = MindMapActionFactory.DUPLICATE_SHEET
                .create(window);
        addRetargetAction((RetargetAction) duplicateSheetAction);
        copySheetAction = MindMapActionFactory.COPY_SHEET.create(window);
        addRetargetAction((RetargetAction) copySheetAction);
        pasteSheetAction = MindMapActionFactory.PASTE_SHEET.create(window);
        addRetargetAction((RetargetAction) pasteSheetAction);

        saveSheetAsAction = new SaveSheetAsAction();
        addAction(saveSheetAsAction);

        createRelationshipAction = MindMapActionFactory.CREATE_RELATIONSHIP
                .create(window);
        addRetargetAction((RetargetAction) createRelationshipAction);

        editTitleAction = MindMapActionFactory.EDIT_TITLE.create(window);
        addRetargetAction((RetargetAction) editTitleAction);

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

        tileAction = MindMapActionFactory.TILE.create(window);
        addRetargetAction((RetargetAction) tileAction);

        resetPositionAction = MindMapActionFactory.RESET_POSITION
                .create(window);
        addRetargetAction((RetargetAction) resetPositionAction);

        findReplaceAction = new FindReplaceAction(window);
        addAction(findReplaceAction);

        renameSheetAction = new RenameSheetAction();
        addAction(renameSheetAction);

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

        addRetargetAction(
                (RetargetAction) MindMapActionFactory.MOVE_UP.create(window));
        addRetargetAction(
                (RetargetAction) MindMapActionFactory.MOVE_DOWN.create(window));
        addRetargetAction(
                (RetargetAction) MindMapActionFactory.MOVE_LEFT.create(window));
        addRetargetAction((RetargetAction) MindMapActionFactory.MOVE_RIGHT
                .create(window));

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
            addAction(dropDownInsertImageAction);
            dropDownInsertImageAction.setText(insertImageAction.getText());
            dropDownInsertImageAction
                    .setToolTipText(insertImageAction.getToolTipText());
            dropDownInsertImageAction
                    .setImageDescriptor(insertImageAction.getImageDescriptor());
            dropDownInsertImageAction.setDisabledImageDescriptor(
                    insertImageAction.getDisabledImageDescriptor());
            insertImageAction.setText(MindMapMessages.InsertImageFromFile_text);
            insertImageAction.setToolTipText(
                    MindMapMessages.InsertImageFromFile_toolTip);
            insertImageAction.setImageDescriptor(null);
            insertImageAction.setDisabledImageDescriptor(null);
        }
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

    protected void activePageChanged(IGraphicalEditorPage page) {
        this.page = page;
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

    public void contributeToPagePopupMenu(IMenuManager menu) {
        menu.add(renameSheetAction);
        menu.add(new Separator());

        menu.add(copySheetAction);
        if (isCopiedSheetAvailable()) {
            menu.add(pasteSheetAction);
        }
        menu.add(duplicateSheetAction);
        menu.add(deleteSheetAction);
        menu.add(deleteOtherSheetAction);
        menu.add(new Separator());

        menu.add(saveSheetAsAction);
        menu.add(new Separator());

        IAction createSheetAction = getActionRegistry()
                .getAction(MindMapActionFactory.NEW_SHEET.getId());
        menu.add(createSheetAction);
        menu.add(new Separator(IWorkbenchActionConstants.NEW_EXT));

        super.contributeToPagePopupMenu(menu);

        //set delete actions state
        if (getSheetsCountOfCurrentWorkbook() < 2) {
            deleteSheetAction.setEnabled(false);
            deleteOtherSheetAction.setEnabled(false);
        } else {
            deleteSheetAction.setEnabled(true);
            deleteOtherSheetAction.setEnabled(true);
        }
    }

    private int getSheetsCountOfCurrentWorkbook() {
        if (page == null || page.isDisposed()) {
            return -1;
        }
        IWorkbook workbook = ((ISheet) page.getAdapter(ISheet.class))
                .getOwnedWorkbook();
        return workbook.getSheets().size();
    }

    public void contributeToSheetCompositePopupMenu(MenuManager menuManager) {
        super.contributeToSheetCompositePopupMenu(menuManager);
    }

    private boolean isCopiedSheetAvailable() {
        return CopiedSheetStorageSupport.getInstance().isCopiedSheetExist();
    }

    public void dispose() {
        if (handlerService != null) {
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
        page = null;
        super.dispose();
    }

    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        ICategoryManager manager = MindMapUI.getCategoryManager();
        Object[] elements = (selection instanceof IStructuredSelection)
                ? ((IStructuredSelection) selection).toArray() : null;
        ICategoryAnalyzation categories = elements == null ? null
                : manager.analyze(elements);
        updateStatusLine(manager, categories);
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
                    m = NLS.bind(MindMapMessages.StatusLine_MultipleItemPattern,
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

}
