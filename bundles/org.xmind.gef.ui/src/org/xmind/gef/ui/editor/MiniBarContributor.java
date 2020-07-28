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

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IEditorPart;

public class MiniBarContributor implements IMiniBarContributor {

    private IMiniBar bar;

    private IGraphicalEditor editor;

    /**
     * @deprecated Use
     *             {@link IGraphicalEditor#addPageChangedListener(org.eclipse.jface.dialogs.IPageChangedListener)}
     *             instead.
     */
    private IGraphicalEditorPage activePage;

    public void init(IMiniBar bar, IGraphicalEditor editor) {
        this.editor = editor;
        init(bar);
    }

    protected void init(IMiniBar bar) {
        this.bar = bar;
        contributeToToolBar(bar.getToolBarManager());
    }

    public void dispose() {
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter == IMiniBar.class)
            return bar;
        if (adapter == IGraphicalEditor.class || adapter == IEditorPart.class)
            return editor;
        return null;
    }

    public IMiniBar getBar() {
        return bar;
    }

    public IGraphicalEditor getEditor() {
        return editor;
    }

    public void contributeToToolBar(IToolBarManager toolBar) {
    }

    /**
     * 
     * @deprecated Use
     *             {@link IGraphicalEditor#addPageChangedListener(org.eclipse.jface.dialogs.IPageChangedListener)}
     *             instead.
     */
    public void setActivePage(IGraphicalEditorPage page) {
        if (page == this.activePage)
            return;

        if (this.activePage != null) {
            unhookPage(this.activePage);
        }
        this.activePage = page;
        if (page != null) {
            hookPage(page);
        }
        pageChanged(page);
    }

    /**
     * 
     * @return
     * @deprecated Use
     *             {@link IGraphicalEditor#addPageChangedListener(org.eclipse.jface.dialogs.IPageChangedListener)}
     *             to listen to active page change events.
     */
    public IGraphicalEditorPage getActivePage() {
        return activePage;
    }

    /**
     * 
     * @param page
     * @deprecated Use
     *             {@link IGraphicalEditor#addPageChangedListener(org.eclipse.jface.dialogs.IPageChangedListener)}
     *             to listen to active page change events.
     */
    protected void pageChanged(IGraphicalEditorPage page) {

    }

    /**
     * 
     * @param page
     * @deprecated Use
     *             {@link IGraphicalEditor#addPageChangedListener(org.eclipse.jface.dialogs.IPageChangedListener)}
     *             to listen to active page change events.
     */
    protected void hookPage(IGraphicalEditorPage page) {

    }

    /**
     * 
     * @param page
     * @deprecated Use
     *             {@link IGraphicalEditor#addPageChangedListener(org.eclipse.jface.dialogs.IPageChangedListener)}
     *             to listen to active page change events.
     */
    protected void unhookPage(IGraphicalEditorPage page) {

    }

}