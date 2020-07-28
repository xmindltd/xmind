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
package org.xmind.ui.internal.views;

import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.xmind.gef.ui.editor.IGraphicalEditor;

/**
 * 
 * @author Frank Shaka
 * @deprecated Replaced by {@link org.xmind.ui.views.ContributedContentsView}
 */
@Deprecated
public class OverviewView extends PageBookView
        implements IContributedContentsView {

    @Override
    protected IPage createDefaultPage(PageBook book) {
        DefaultOverviewPage page = new DefaultOverviewPage();
        initPage(page);
        page.createControl(book);
        return page;
    }

    @Override
    protected PageRec doCreatePage(IWorkbenchPart part) {
        WorkbookOverviewPage page = new WorkbookOverviewPage(
                (IGraphicalEditor) part);
        initPage(page);
        page.createControl(getPageBook());
        return new PageRec(part, page);
    }

    @Override
    protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
        IPage page = pageRecord.page;
        page.dispose();
        pageRecord.dispose();
    }

    @Override
    protected IWorkbenchPart getBootstrapPart() {
        return getSite().getPage().getActiveEditor();
    }

    @Override
    protected boolean isImportant(IWorkbenchPart part) {
        return part instanceof IGraphicalEditor;
    }

    public IWorkbenchPart getContributingPart() {
        return getSite().getPage().getActiveEditor();
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter == IContributedContentsView.class) {
            return this;
        }
        return super.getAdapter(adapter);
    }

}
