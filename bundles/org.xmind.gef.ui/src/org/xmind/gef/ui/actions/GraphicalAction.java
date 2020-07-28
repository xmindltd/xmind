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
package org.xmind.gef.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.tool.ITool;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;

public abstract class GraphicalAction extends Action implements
        IWorkbenchAction {

    private IWorkbenchWindow window;

    private IGraphicalEditor editor;

    private Object data = null;

    public GraphicalAction(IWorkbenchWindow window) {
        if (window == null)
            throw new IllegalArgumentException(
                    "Can't initialize a graphical action without a workbench window or an editor."); //$NON-NLS-1$
        this.window = window;
    }

    public GraphicalAction(IGraphicalEditor editor) {
        if (editor == null)
            throw new IllegalArgumentException(
                    "Can't initialize a graphical action without a workbench window or an editor."); //$NON-NLS-1$
        this.editor = editor;
    }

    public GraphicalAction() {
        this(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
    }

    protected IWorkbenchWindow getWindow() {
        if (window != null)
            return window;
        if (editor != null)
            return editor.getSite().getWorkbenchWindow();
        return null;
    }

    protected Shell getShell() {
        IWorkbenchWindow window = getWindow();
        return window == null ? null : window.getShell();
    }

    /**
     * @return the editor
     */
    protected IGraphicalEditor getEditor() {
        if (editor != null)
            return editor;
        IWorkbenchWindow window = getWindow();
        if (window == null)
            return null;
        IEditorPart editor = window.getActivePage().getActiveEditor();
        if (editor instanceof IGraphicalEditor)
            return (IGraphicalEditor) editor;
        return null;
    }

    protected IGraphicalEditorPage getActivePage() {
        IGraphicalEditor editor = getEditor();
        return editor == null ? null : editor.getActivePageInstance();
    }

    protected IGraphicalViewer getActiveViewer() {
        IGraphicalEditorPage page = getActivePage();
        return page == null ? null : page.getViewer();
    }

    protected ITool getActiveTool() {
        IGraphicalViewer viewer = getActiveViewer();
        return viewer == null ? null : viewer.getEditDomain().getActiveTool();
    }

    protected ISelection getSelection() {
        IGraphicalEditor editor = getEditor();
        return editor == null ? StructuredSelection.EMPTY : editor.getSite()
                .getSelectionProvider().getSelection();
    }

    protected ICommandStack getCommandStack() {
        IGraphicalEditor editor = getEditor();
        return editor == null ? null : editor.getCommandStack();
    }

    protected void saveAndRun(Command command) {
        ICommandStack cs = getCommandStack();
        if (cs != null)
            cs.execute(command);
    }

    public void dispose() {
        window = null;
        editor = null;
    }

    protected boolean isDisposed() {
        return window == null && editor == null;
    }

    /**
     * @return the data
     */
    public Object getData() {
        return data;
    }

    /**
     * @param data
     *            the data to set
     */
    public void setData(Object data) {
        this.data = data;
    }

}