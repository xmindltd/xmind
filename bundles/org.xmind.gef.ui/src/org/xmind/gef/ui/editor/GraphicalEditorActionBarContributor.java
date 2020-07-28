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
package org.xmind.gef.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.xmind.gef.ui.actions.ActionRegistry;
import org.xmind.gef.ui.actions.IActionRegistry;

public abstract class GraphicalEditorActionBarContributor extends
        EditorActionBarContributor implements IGlobalActionHandlerUpdater {

    private IActionRegistry actionRegistry = new ActionRegistry();

    private List<RetargetAction> retargetActions = new ArrayList<RetargetAction>();

    private List<String> globalActionIds = new ArrayList<String>();

    private IEditorPart activeEditor = null;

    public void init(IActionBars bars) {
        makeActions();
        declareGlobalActionIds();
        super.init(bars);
    }

    protected abstract void makeActions();

    protected abstract void declareGlobalActionIds();

    protected void addAction(IAction action) {
        actionRegistry.addAction(action);
    }

    protected void addRetargetAction(RetargetAction action) {
        String actionId = action.getId();
        Assert.isNotNull(actionId,
                "Retarget action must have an ID before added: "//$NON-NLS-1$
                        + action.toString());
        addAction(action);
        retargetActions.add(action);
        getPage().addPartListener(action);
        addGlobalActionId(actionId);
    }

    protected void addGlobalActionId(String actionId) {
        globalActionIds.add(actionId);
    }

    public void setActiveEditor(IEditorPart targetEditor) {
        this.activeEditor = targetEditor;
        IGraphicalEditorPage activePage = null;
        if (targetEditor instanceof IGraphicalEditor) {
            activePage = ((IGraphicalEditor) targetEditor)
                    .getActivePageInstance();
        }
        activePageChanged(activePage);
        updateGlobalActions(getActionBars(), targetEditor, activePage);
    }

    private void updateGlobalActions(IActionBars actionBars, IEditorPart editor,
            IGraphicalEditorPage page) {
        IActionRegistry editorActions = getActionRegistry(editor);
        IActionRegistry pageActions = getActionRegistry(page);

        for (String actionId : globalActionIds) {
            IAction handler = findHandler(actionId, pageActions, editorActions);
            actionBars.setGlobalActionHandler(actionId, handler);
        }
        actionBars.updateActionBars();
    }

    private static IActionRegistry getActionRegistry(IAdaptable adaptable) {
        if (adaptable != null) {
            return (IActionRegistry) adaptable
                    .getAdapter(IActionRegistry.class);
        }
        return null;
    }

    private IAction findHandler(String actionId, IActionRegistry pageActions,
            IActionRegistry editorActions) {
        IAction action;
        if (pageActions != null) {
            action = pageActions.getAction(actionId);
            if (action != null)
                return action;
        }
        if (editorActions != null) {
            action = editorActions.getAction(actionId);
            if (action != null)
                return action;
        }
        return null;
    }

    public void setActivePage(IGraphicalEditorPage page) {
        activePageChanged(page);
        IEditorPart editor = page == null ? null : page.getParentEditor();
        updateGlobalActions(getActionBars(), editor, page);
    }

    protected abstract void activePageChanged(IGraphicalEditorPage page);

    protected void setGlobalHandlers(IActionRegistry actionRegistry) {
        IActionBars bars = getActionBars();
        for (String id : globalActionIds) {
            IAction action = actionRegistry.getAction(id);
            if (action != null) {
                bars.setGlobalActionHandler(id, action);
            }
        }
    }

    protected IActionRegistry getActionRegistry() {
        return actionRegistry;
    }

    public IAction getAction(String actionId) {
        return getActionRegistry().getAction(actionId);
    }

    public void contributeToContentPopupMenu(IMenuManager menu) {
        menu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    public void contributeToPagePopupMenu(IMenuManager menu) {
        menu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    public void contributeToSheetCompositePopupMenu(MenuManager menu) {
        menu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    public void dispose() {
        if (retargetActions != null) {
            for (RetargetAction action : retargetActions) {
                getPage().removePartListener(action);
                action.dispose();
            }
            retargetActions = null;
        }
        if (actionRegistry != null) {
            actionRegistry.dispose();
            actionRegistry = null;
        }
        activeEditor = null;
        super.dispose();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.gef.ui.editor.IActionBarsUpdater#updateActionBars(org.eclipse
     * .ui.IActionBars)
     */
    public void updateGlobalActionHandlers(IActionBars actionBars) {
        if (activeEditor != null && activeEditor instanceof IGraphicalEditor) {
            IGraphicalEditorPage page = ((IGraphicalEditor) activeEditor)
                    .getActivePageInstance();
            updateGlobalActions(actionBars, activeEditor, page);
        }
    }
}
