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

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.xmind.gef.ui.actions.ActionRegistry;
import org.xmind.gef.ui.actions.IActionRegistry;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.actions.ActionConstants;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;

/**
 * @author Frank Shaka
 * @deprecated Replaced by {@link org.xmind.ui.views.ContributedContentsView}
 */
@Deprecated
public class RevisionsView extends PageBookView
        implements IContributedContentsView {

    private static class DefaultRevisionsPage extends Page {
        private Control control;

        @Override
        public void createControl(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setLayout(new GridLayout());

            Label label = new Label(composite, SWT.WRAP);
            label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            label.setText(MindMapMessages.DefaultRevisionsPage_message);

            control = composite;
        }

        @Override
        public Control getControl() {
            return control;
        }

        @Override
        public void setFocus() {
            control.setFocus();
        }

    }

    private IActionRegistry actions = new ActionRegistry();

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);

        RetargetAction removeAction = new RetargetAction(
                ActionConstants.REMOVE_REVISION_ID,
                MindMapMessages.RevisionsView_DeleteRevisionsAction_text);
        removeAction.setToolTipText(
                MindMapMessages.RevisionsView_DeleteRevisionsAction_toolTip);
        removeAction.setImageDescriptor(
                MindMapUI.getImages().get(IMindMapImages.DELETE, true));
        removeAction.setDisabledImageDescriptor(
                MindMapUI.getImages().get(IMindMapImages.DELETE, false));
        registerAction(removeAction);

        RetargetAction revertAction = new RetargetAction(
                ActionConstants.REVERT_TO_REVISION_ID,
                MindMapMessages.RevisionsView_RevertToRevisionAction_text);
        revertAction.setToolTipText(
                MindMapMessages.RevisionsView_RevertToRevisionAction_toolTip);
        revertAction.setImageDescriptor(
                MindMapUI.getImages().get("revisions_revert.gif", true)); //$NON-NLS-1$
        registerAction(revertAction);

        RetargetAction previewAction = new RetargetAction(
                ActionConstants.PREVIEW_REVISIONS,
                MindMapMessages.RevisionsView_PreviewAction_text);
        previewAction.setToolTipText(
                MindMapMessages.RevisionsView_PreviewAction_toolTip);
        previewAction.setImageDescriptor(
                MindMapUI.getImages().get(IMindMapImages.ZOOMIN, true));
        previewAction.setDisabledImageDescriptor(
                MindMapUI.getImages().get(IMindMapImages.ZOOMIN, false));
        registerAction(previewAction);

        IToolBarManager toolbar = getViewSite().getActionBars()
                .getToolBarManager();
        toolbar.add(previewAction);
        toolbar.add(revertAction);
        toolbar.add(removeAction);
        toolbar.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        IMenuManager menu = getViewSite().getActionBars().getMenuManager();
        menu.add(previewAction);
        menu.add(revertAction);
        menu.add(removeAction);
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        getViewSite().getActionBars().updateActionBars();
    }

    private void registerAction(final RetargetAction action) {
        action.partActivated(this);
//        getSite().getPage().addPartListener(action);
        actions.addAction(action);
    }

    @Override
    public void dispose() {
        actions.dispose();
        super.dispose();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.part.PageBookView#createDefaultPage(org.eclipse.ui.part
     * .PageBook)
     */
    @Override
    protected IPage createDefaultPage(PageBook book) {
        IPageBookViewPage page = new DefaultRevisionsPage();
        initPage(page);
        page.createControl(book);
        return page;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.PageBookView#doCreatePage(org.eclipse.ui.
     * IWorkbenchPart )
     */
    @Override
    protected PageRec doCreatePage(IWorkbenchPart part) {
        IPageBookViewPage page = new WorkbookRevisionsPage(
                (IGraphicalEditor) part);
        initPage(page);
        page.createControl(getPageBook());
        return new PageRec(part, page);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.PageBookView#doDestroyPage(org.eclipse.ui.
     * IWorkbenchPart , org.eclipse.ui.part.PageBookView.PageRec)
     */
    @Override
    protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
        IPage page = pageRecord.page;
        page.dispose();
        pageRecord.dispose();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.PageBookView#getBootstrapPart()
     */
    @Override
    protected IWorkbenchPart getBootstrapPart() {
        return getSite().getPage().getActiveEditor();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.PageBookView#isImportant(org.eclipse.ui.
     * IWorkbenchPart )
     */
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
