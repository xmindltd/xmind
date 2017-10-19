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
package org.xmind.ui.internal.views;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.Page;
import org.xmind.gef.ui.actions.IActionRegistry;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.tabfolder.PageBookPage;

public class WorkbookOverviewPage extends PageBookPage {

    /**
     * 
     */
    public WorkbookOverviewPage(IGraphicalEditor editor) {
        super(editor);
    }

    public IGraphicalEditor getEditor() {
        return (IGraphicalEditor) super.getSourcePageProvider();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.tabfolder.PageBookPage#createDefaultPage(org.eclipse.swt
     * .widgets.Composite)
     */
    @Override
    protected Control createDefaultPage(Composite parent) {
        return new Composite(parent, SWT.NONE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.tabfolder.PageBookPage#doCreateNestedPage(java.lang.Object)
     */
    @Override
    protected Page doCreateNestedPage(Object sourcePage) {
        return new SheetOverviewPage((IGraphicalEditorPage) sourcePage);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.tabfolder.PageBookPage#refreshGlobalActionHandlers()
     */
    @Override
    protected void refreshGlobalActionHandlers() {
        super.refreshGlobalActionHandlers();

        // Set new actions from editor.
        IActionRegistry registry = (IActionRegistry) getEditor().getAdapter(
                IActionRegistry.class);
        if (registry != null) {
            initGlobalActionHandlers(getSite().getActionBars(), registry);
        }
    }

    protected void initGlobalActionHandlers(IActionBars bars,
            IActionRegistry registry) {
        setGlobalActionHandler(bars, registry, ActionFactory.UNDO.getId());
        setGlobalActionHandler(bars, registry, ActionFactory.REDO.getId());
    }

    protected void setGlobalActionHandler(IActionBars bars,
            IActionRegistry registry, String actionId) {
        IAction action = registry.getAction(actionId);
        if (action != null) {
            getSite().getActionBars().setGlobalActionHandler(actionId, action);
        }
    }

}
