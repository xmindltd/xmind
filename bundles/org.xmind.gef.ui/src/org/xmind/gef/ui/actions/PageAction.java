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

public abstract class PageAction extends Action implements IDisposable2 {

    private IGraphicalEditorPage page;

    protected PageAction(IGraphicalEditorPage page) {
        this.page = page;
    }

    protected PageAction(String id, IGraphicalEditorPage page) {
        this(page);
        setId(id);
    }

    protected IGraphicalEditorPage getPage() {
        return page;
    }

    protected IGraphicalViewer getViewer() {
        return page == null ? null : page.getViewer();
    }

    protected EditDomain getEditDomain() {
        return page == null ? null : page.getEditDomain();
    }

    protected ITool getActiveTool() {
        EditDomain domain = getEditDomain();
        return domain == null ? null : domain.getActiveTool();
    }

    protected void sendRequest(String request) {
        EditDomain domain = getEditDomain();
        if (domain != null) {
            domain.handleRequest(request, getViewer());
        }
    }

    protected void sendRequest(Request request) {
        request.setViewer(getViewer());
        EditDomain domain = getEditDomain();
        if (domain != null) {
            domain.handleRequest(request);
        }
    }

    protected IGraphicalEditor getEditor() {
        return page == null ? null : page.getParentEditor();
    }

    protected ICommandStack getCommandStack() {
        IGraphicalEditor editor = getEditor();
        return editor == null ? null : editor.getCommandStack();
    }

    protected void saveAndRun(Command command) {
        ICommandStack cs = getCommandStack();
        if (cs != null) {
            cs.execute(command);
        }
    }

    public void dispose() {
        page = null;
    }

    public boolean isDisposed() {
        return page == null || page.isDisposed();
    }

}