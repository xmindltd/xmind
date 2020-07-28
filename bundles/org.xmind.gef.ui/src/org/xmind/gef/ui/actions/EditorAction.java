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
import org.xmind.gef.EditDomain;
import org.xmind.gef.IDisposable2;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.Request;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.tool.ITool;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;

public abstract class EditorAction extends Action implements IDisposable2 {

    private IGraphicalEditor editor;

    protected EditorAction(IGraphicalEditor editor) {
        this.editor = editor;
    }

    protected EditorAction(String id, IGraphicalEditor editor) {
        this(editor);
        setId(id);
    }

    protected IGraphicalEditor getEditor() {
        return editor;
    }

    protected ICommandStack getCommandStack() {
        return editor == null ? null : editor.getCommandStack();
    }

    protected void saveAndRun(Command command) {
        ICommandStack cs = getCommandStack();
        if (cs != null) {
            cs.execute(command);
        }
    }

    protected IGraphicalEditorPage getActivePage() {
        return editor == null ? null : editor.getActivePageInstance();
    }

    protected IGraphicalViewer getActiveViewer() {
        IGraphicalEditorPage page = getActivePage();
        return page == null ? null : page.getViewer();
    }

    protected EditDomain getActiveEditDomain() {
        IGraphicalViewer viewer = getActiveViewer();
        return viewer == null ? null : viewer.getEditDomain();
    }

    protected ITool getActiveTool() {
        EditDomain domain = getActiveEditDomain();
        return domain == null ? null : domain.getActiveTool();
    }

    protected void performRequest(String request) {
        EditDomain domain = getActiveEditDomain();
        if (domain != null) {
            domain.handleRequest(request, getActiveViewer());
        }
    }

    protected void performRequest(Request request) {
        EditDomain domain = getActiveEditDomain();
        if (domain != null) {
            domain.handleRequest(request);
        }
    }

    public boolean isDisposed() {
        return editor == null;
    }

    public void dispose() {
        editor = null;
    }

}