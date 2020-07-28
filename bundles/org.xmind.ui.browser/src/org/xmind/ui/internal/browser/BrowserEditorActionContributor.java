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
package org.xmind.ui.internal.browser;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.xmind.ui.browser.BrowserSupport;
import org.xmind.ui.browser.IBrowser;
import org.xmind.ui.browser.IBrowserSupport;

public class BrowserEditorActionContributor extends EditorActionBarContributor {

    private static class OpenInExternalAction extends Action {

        private InternalBrowserEditor editor;

        /**
         * 
         */
        public OpenInExternalAction() {
            super(BrowserMessages.BrowserView_OpenInExternalBrowser_text,
                    BrowserImages.getImageDescriptor(BrowserImages.BROWSER));
            setToolTipText(
                    BrowserMessages.BrowserView_OpenInExternalBrowser_toolTip);
            setEnabled(false);
        }

        /**
         * @param editor
         *            the editor to set
         */
        public void setEditor(InternalBrowserEditor editor) {
            this.editor = editor;
            setEnabled(editor != null);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.action.Action#run()
         */
        @Override
        public void run() {
            if (editor == null)
                return;

            BrowserViewer viewer = editor.getViewer();
            if (viewer == null || viewer.getControl() == null
                    || viewer.getControl().isDisposed())
                return;

            IBrowser browser = BrowserSupport.getInstance()
                    .createBrowser(IBrowserSupport.AS_EXTERNAL);
            try {
                browser.openURL(viewer.getURL());
            } catch (PartInitException e) {
                BrowserPlugin.log(e);
            }
        }
    }

    private OpenInExternalAction openInExternalAction;

    /*
     * (non-Javadoc)
     * 
     * @seeorg.eclipse.ui.part.EditorActionBarContributor#init(org.eclipse.ui.
     * IActionBars)
     */
    @Override
    public void init(IActionBars bars) {
        openInExternalAction = new OpenInExternalAction();
        super.init(bars);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.EditorActionBarContributor#contributeToMenu(org.
     * eclipse .jface.action.IMenuManager)
     */
    @Override
    public void contributeToMenu(IMenuManager menuManager) {
        IMenuManager fileMenu = menuManager
                .findMenuUsingPath(IWorkbenchActionConstants.M_FILE);
        if (fileMenu != null) {
            if (fileMenu.find(IWorkbenchActionConstants.OPEN_EXT) != null) {
                fileMenu.prependToGroup(IWorkbenchActionConstants.OPEN_EXT,
                        openInExternalAction);
            }
        }
    }

    @Override
    public void contributeToToolBar(IToolBarManager toolBarManager) {
//        super.contributeToToolBar(toolBarManager);
//        toolBarManager.add(openInExternalAction);
    }

    public void setActiveEditor(IEditorPart targetEditor) {
        if (targetEditor instanceof InternalBrowserEditor) {
            InternalBrowserEditor editor = (InternalBrowserEditor) targetEditor;
            openInExternalAction.setEditor(editor);

            IActionBars bars = getActionBars();
            setHandler(bars, ActionFactory.COPY.getId(), editor);
            setHandler(bars, ActionFactory.CUT.getId(), editor);
            setHandler(bars, ActionFactory.PASTE.getId(), editor);
            setHandler(bars, ActionFactory.DELETE.getId(), editor);
        }
    }

    private void setHandler(IActionBars bars, String actionId,
            InternalBrowserEditor editor) {
        IAction handler = editor.getAction(actionId);
        if (handler != null) {
            bars.setGlobalActionHandler(actionId, handler);
        }
    }

}