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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.xmind.core.command.Command;
import org.xmind.core.command.CommandJob;
import org.xmind.core.command.ICommand;
import org.xmind.ui.browser.IBrowser;

public class InternalBrowser implements IBrowser {

    private BrowserSupportImpl support;

    private String clientId;

    private Object windowKey = null;

    private boolean asEditor;

    private int browserStyle;

    private String name;

    private String tooltip;

    private IWorkbenchPart part;

    private IPartListener listener;

    private IWebBrowser workbenchBrowser;

    public InternalBrowser(BrowserSupportImpl support, String clientId,
            boolean asEditor, int style) {
        this.support = support;
        this.clientId = clientId;
        this.asEditor = asEditor;
        this.browserStyle = style;
    }

    public IWorkbenchPart getPart() {
        return part;
    }

    public String getClientId() {
        return clientId;
    }

    public void openURL(String url) throws PartInitException {
        ICommand command = Command.parseURI(url);
        if (command != null) {
            new CommandJob(command, null).schedule();
            return;
        }

        url = BrowserUtil.normalizeURL(url);

        try {
            doOpenURL(url);
        } catch (PartInitException e) {
            doOpenURLByWorkbenchBrowser(url);
        }
    }

    protected void doOpenURL(String url) throws PartInitException {
        IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        IWorkbenchPage page = window == null ? null : window.getActivePage();

        if (page == null)
            throw new PartInitException(
                    BrowserMessages.InternalWebBrowser_ErrorCouldNotLaunchWebBrowser_message);

        if (part instanceof IEditorPart) {
            ((IEditorPart) part).init(((IEditorPart) part).getEditorSite(),
                    createEditorInput(url));
            page.activate(part);
        } else {
            if (asEditor) {
                part = page.openEditor(createEditorInput(url),
                        InternalBrowserEditor.BROWSER_EDITOR_ID);
            } else {
                part = page.showView(InternalBrowserView.BROWSER_VIEW_ID,
                        clientId, IWorkbenchPage.VIEW_ACTIVATE);
                if (part instanceof InternalBrowserView) {
                    ((InternalBrowserView) part).changeStyle(browserStyle);
                    ((InternalBrowserView) part).openURL(url);
                }
            }
            hookPart(page, part);
        }
    }

    /**
     * @param url
     * @return
     */
    private BrowserEditorInput createEditorInput(String url) {
        BrowserEditorInput input = new BrowserEditorInput(url, clientId,
                browserStyle);
        input.setName(this.name);
        input.setToolTipText(this.tooltip);
        return input;
    }

    private void hookPart(final IWorkbenchPage page,
            IWorkbenchPart editorPart) {
        listener = new IPartListener() {
            public void partActivated(IWorkbenchPart part) {
            }

            public void partBroughtToTop(IWorkbenchPart part) {
            }

            public void partClosed(IWorkbenchPart part) {
                if (part.equals(InternalBrowser.this.part)) {
                    InternalBrowser.this.part = null;
                    page.removePartListener(listener);
                    support.removeInternalBrowser(InternalBrowser.this);
                }
            }

            public void partDeactivated(IWorkbenchPart part) {
            }

            public void partOpened(IWorkbenchPart part) {
            }
        };
        page.addPartListener(listener);
    }

    protected void doOpenURLByWorkbenchBrowser(String url)
            throws PartInitException {
        try {
            URL theURL = new URL(url);
            getWorkbenchBrowser().openURL(theURL);
        } catch (MalformedURLException e) {
            throw new PartInitException(
                    BrowserMessages.InternalWebBrowser_ErrorCouldNotLaunchWebBrowser_message);
        }
    }

    private IWebBrowser getWorkbenchBrowser() throws PartInitException {
        if (workbenchBrowser == null) {
            workbenchBrowser = createWorkbenchBrowser();
        }
        return workbenchBrowser;
    }

    protected IWebBrowser createWorkbenchBrowser() throws PartInitException {
        return PlatformUI.getWorkbench().getBrowserSupport().createBrowser(
                IWorkbenchBrowserSupport.AS_EDITOR
                        | IWorkbenchBrowserSupport.LOCATION_BAR
                        | IWorkbenchBrowserSupport.NAVIGATION_BAR,
                getClientId(), name, tooltip);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public String getName() {
        return name;
    }

    public String getTooltip() {
        return tooltip;
    }

    public Object getWindowKey() {
        if (windowKey == null) {
            windowKey = createWindowKey();
        }
        return windowKey;
    }

    private Object createWindowKey() {
        IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        if (window == null)
            return this;
        return BrowserUtil.getWindowKey(window);
    }

    public void close() {
        if (part != null) {
            if (part instanceof IEditorPart) {
                part.getSite().getPage().closeEditor((IEditorPart) part, false);
            } else {
                part.getSite().getPage().hideView((IViewPart) part);
            }
        }
        if (workbenchBrowser != null) {
            workbenchBrowser.close();
        }
    }

    public void setText(String text) throws PartInitException {
        doOpenURL(null);
        if (part != null && part instanceof InternalBrowserEditor) {
            ((InternalBrowserEditor) part).setText(text);
        }
    }

}