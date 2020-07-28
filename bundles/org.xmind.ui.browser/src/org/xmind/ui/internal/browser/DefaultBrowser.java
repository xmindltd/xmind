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

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.xmind.core.command.Command;
import org.xmind.core.command.CommandJob;
import org.xmind.core.command.ICommand;

public class DefaultBrowser extends InternalBrowser {

    private IWebBrowser externalWorkbenchBrowser = null;

    public DefaultBrowser(BrowserSupportImpl support, String clientId) {
        super(support, clientId, false, 0);
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
            try {
                doOpenURLByWorkbenchBrowser(url);
            } catch (PartInitException e1) {
                openExternal(url);
            }
        }
    }

    private void openExternal(String url) {
        try {
            doOpenURLByExternalWorkbenchBrowser(url);
        } catch (PartInitException e2) {
            doOpenURLByDefault(url);
        }
    }

    private void doOpenURLByExternalWorkbenchBrowser(String url)
            throws PartInitException {
        try {
            URL theURL = new URL(url);
            getExternalWorkbenchBrowser().openURL(theURL);
        } catch (MalformedURLException e) {
            throw new PartInitException(
                    BrowserMessages.InternalWebBrowser_ErrorCouldNotLaunchWebBrowser_message);
        }
    }

    private IWebBrowser getExternalWorkbenchBrowser() throws PartInitException {
        if (externalWorkbenchBrowser == null) {
            externalWorkbenchBrowser = createExternalWorkbenchBrowser();
        }
        return externalWorkbenchBrowser;
    }

    protected IWebBrowser createExternalWorkbenchBrowser()
            throws PartInitException {
        return PlatformUI.getWorkbench().getBrowserSupport().createBrowser(
                IWorkbenchBrowserSupport.AS_EXTERNAL
                        | IWorkbenchBrowserSupport.LOCATION_BAR
                        | IWorkbenchBrowserSupport.NAVIGATION_BAR,
                getClientId(), getName(), getTooltip());
    }

    protected void doOpenURLByDefault(String url) {
        BrowserUtil.gotoUrl(url);
    }

}