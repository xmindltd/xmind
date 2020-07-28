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

import java.net.URL;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.xmind.core.command.Command;
import org.xmind.core.command.CommandJob;
import org.xmind.core.command.ICommand;
import org.xmind.ui.browser.IBrowser;

public class ExternalBrowser implements IBrowser {

    private String clientId;

    private IWebBrowser workbenchBrowser;

    public ExternalBrowser(String clientId) {
        this.clientId = clientId;
    }

    public void close() {
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
            URL theURL = new URL(url);
            getWorkbenchBrowser().openURL(theURL);
        } catch (Exception e) {
            BrowserUtil.gotoUrl(url);
        }
    }

    private IWebBrowser getWorkbenchBrowser() throws PartInitException {
        if (workbenchBrowser == null)
            workbenchBrowser = createWorkbenchBrowser();
        return workbenchBrowser;
    }

    private IWebBrowser createWorkbenchBrowser() throws PartInitException {
        return PlatformUI.getWorkbench().getBrowserSupport().createBrowser(
                IWorkbenchBrowserSupport.AS_EXTERNAL, clientId, null, null);
    }

    public void setText(String text) throws PartInitException {
        //TODO set html to external browser
    }

}