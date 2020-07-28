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

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.xmind.ui.browser.BrowserSupport;
import org.xmind.ui.browser.IBrowser;
import org.xmind.ui.browser.IBrowserSupport;
import org.xmind.ui.browser.IBrowserViewer;
import org.xmind.ui.browser.IBrowserViewerContainer;
import org.xmind.ui.internal.browser.actions.CopyAction;
import org.xmind.ui.internal.browser.actions.CutAction;
import org.xmind.ui.internal.browser.actions.DeleteAction;
import org.xmind.ui.internal.browser.actions.PasteAction;

public class InternalBrowserView extends ViewPart
        implements IBrowserViewerContainer {

    private class OpenInExternalAction extends Action {
        /**
         * 
         */
        public OpenInExternalAction() {
            super(BrowserMessages.BrowserView_OpenInExternalBrowser_text,
                    BrowserImages.getImageDescriptor(BrowserImages.BROWSER));
            setToolTipText(
                    BrowserMessages.BrowserView_OpenInExternalBrowser_toolTip);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.action.Action#run()
         */
        @Override
        public void run() {
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

    public static final String BROWSER_VIEW_ID = "org.xmind.ui.BrowserView"; //$NON-NLS-1$

    private static final String GROUP_CONTROLS = "org.xmind.ui.browser.controls"; //$NON-NLS-1$

    private static final String KEY_STYLE = "style"; //$NON-NLS-1$

    private BrowserViewer viewer;

    private String clientId;

    private int style;

    private ActionContributionItem backActionItem = null;

    private ActionContributionItem forwardActionItem = null;

    private ActionContributionItem stopRefreshActionItem = null;

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void changeStyle(int newStyle) {
        if ((newStyle & IBrowserSupport.NO_LOCATION_BAR) != 0
                && (newStyle & IBrowserSupport.NO_EXTRA_CONTRIBUTIONS) != 0) {
            newStyle |= IBrowserSupport.NO_TOOLBAR;
        }
        this.style = newStyle;
        if (viewer != null && viewer.getControl() != null
                && !viewer.getControl().isDisposed()) {
            int oldStyle = viewer.getStyle();
            viewer.changeStyle(newStyle);
            boolean hadNoToolBar = (oldStyle & IBrowserSupport.NO_TOOLBAR) != 0;
            boolean hasNoToolBar = (newStyle & IBrowserSupport.NO_TOOLBAR) != 0;
            if (hasNoToolBar && !hadNoToolBar) {
                IToolBarManager toolBar = getViewSite().getActionBars()
                        .getToolBarManager();
                removeControls(toolBar);
                toolBar.update(true);
            } else if (hadNoToolBar && !hasNoToolBar) {
                IToolBarManager toolBar = getViewSite().getActionBars()
                        .getToolBarManager();
                addControls(toolBar);
                toolBar.update(true);
            }
        }
    }

    private void removeControls(IToolBarManager toolBar) {
        if (backActionItem != null) {
            toolBar.remove(backActionItem);
            backActionItem.dispose();
            backActionItem = null;
        }
        if (forwardActionItem != null) {
            toolBar.remove(forwardActionItem);
            forwardActionItem.dispose();
            forwardActionItem = null;
        }
        if (stopRefreshActionItem != null) {
            toolBar.remove(stopRefreshActionItem);
            stopRefreshActionItem.dispose();
            stopRefreshActionItem = null;
        }
    }

    private void addControls(IToolBarManager toolBar) {
        stopRefreshActionItem = new ActionContributionItem(
                viewer.getStopRefreshAction());
        toolBar.prependToGroup(GROUP_CONTROLS, stopRefreshActionItem);
        forwardActionItem = new ActionContributionItem(
                viewer.getForwardAction());
        toolBar.prependToGroup(GROUP_CONTROLS, forwardActionItem);
        backActionItem = new ActionContributionItem(viewer.getBackAction());
        toolBar.prependToGroup(GROUP_CONTROLS, backActionItem);
    }

    @Override
    public void saveState(IMemento memento) {
        memento.putInteger(KEY_STYLE, style);
        super.saveState(memento);
    }

    @Override
    public void init(IViewSite site, IMemento memento)
            throws PartInitException {
        this.clientId = site.getSecondaryId();
        Integer styleValue = memento == null ? null
                : memento.getInteger(KEY_STYLE);
        this.style = styleValue == null ? SWT.NONE : styleValue.intValue();
        super.init(site, memento);
    }

    public void createPartControl(final Composite parent) {
        viewer = new BrowserViewer(parent, style, this);
        initActions();
        final Image defaultImage = getTitleImage();
        viewer.getBusyIndicator()
                .addSelectionChangedListener(new ISelectionChangedListener() {
                    public void selectionChanged(SelectionChangedEvent event) {
                        if (!parent.isDisposed()) {
                            parent.getDisplay().asyncExec(new Runnable() {
                                public void run() {
                                    if (!parent.isDisposed()
                                            && defaultImage != null
                                            && !defaultImage.isDisposed()) {
                                        Image currentImage = viewer
                                                .getBusyIndicator()
                                                .getCurrentImage();
                                        if (currentImage == null
                                                || !viewer.getBusyIndicator()
                                                        .isAnimating()) {
                                            setTitleImage(defaultImage);
                                        } else {
                                            setTitleImage(currentImage);
                                        }
                                    }
                                }
                            });
                        }
                    }
                });
    }

    /**
     * 
     */
    private void initActions() {
        IActionBars actionBars = getViewSite().getActionBars();

        OpenInExternalAction openInExternalAction = new OpenInExternalAction();

        IMenuManager menu = actionBars.getMenuManager();
        menu.add(new GroupMarker(GROUP_CONTROLS));
        menu.add(openInExternalAction);
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

//        IToolBarManager toolBar = actionBars.getToolBarManager();
//        toolBar.add(new GroupMarker(GROUP_CONTROLS));
//        toolBar.add(openInExternalAction);
//        toolBar.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        registerAction(actionBars, new CopyAction(viewer));
        registerAction(actionBars, new CutAction(viewer));
        registerAction(actionBars, new PasteAction(viewer));
        registerAction(actionBars, new DeleteAction(viewer));

//        if ((style & IBrowserSupport.NO_TOOLBAR) != 0) {
//            addControls(toolBar);
//        }
    }

    private void registerAction(IActionBars actionBars, IAction action) {
        actionBars.setGlobalActionHandler(action.getId(), action);
    }

    public void setFocus() {
        viewer.setFocus();
    }

    public void openURL(String url) {
        if (viewer != null && !viewer.getControl().isDisposed()) {
            viewer.setURL(url);
        }
    }

    public BrowserViewer getViewer() {
        return viewer;
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter == IBrowserViewer.class)
            return viewer;
        if (adapter == IBrowserViewerContainer.class)
            return this;
        return super.getAdapter(adapter);
    }

    public boolean close() {
        try {
            getSite().getPage().hideView(this);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public IActionBars getActionBars() {
        return getViewSite().getActionBars();
    }

    public String getClientId() {
        return clientId;
    }

    public void openInExternalBrowser(String url) {
        BrowserUtil.gotoUrl(url);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.browser.IBrowserViewerContainer#openNewBrowser()
     */
    public Browser openNewBrowser() {
        final Browser[] ret = new Browser[1];
        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                IBrowser browser = BrowserSupport.getInstance().createBrowser(
                        IBrowserSupport.AS_EDITOR, getClientId());
                browser.openURL(""); //$NON-NLS-1$
                if (browser instanceof InternalBrowser) {
                    IWorkbenchPart part = ((InternalBrowser) browser).getPart();
                    if (part instanceof InternalBrowserEditor) {
                        ret[0] = ((InternalBrowserEditor) part).getViewer()
                                .getBrowser();
                    }
                }
            }
        });
        return ret[0];
    }

}