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
import org.xmind.ui.browser.IBrowser;

public class InternalWorkbenchBrowser implements IBrowser {

    private IWebBrowser impl;

    public InternalWorkbenchBrowser(String browserClientId, String name,
            String tooltip) throws PartInitException {
        this.impl = PlatformUI.getWorkbench().getBrowserSupport().createBrowser(
                IWorkbenchBrowserSupport.AS_EDITOR, browserClientId, name,
                tooltip);
    }

    public void close() {
        impl.close();
    }

    public String getClientId() {
        return impl.getId();
    }

    public void openURL(String url) throws PartInitException {
        ICommand command = Command.parseURI(url);
        if (command != null) {
            new CommandJob(command, null).schedule();
            return;
        }

        url = BrowserUtil.normalizeURL(url);

        URL theURL;
        try {
            theURL = new URL(url);
        } catch (MalformedURLException e) {
            throw new PartInitException(
                    BrowserMessages.InternalWebBrowser_ErrorCouldNotLaunchWebBrowser_message);
        }
        impl.openURL(theURL);
    }

    public void setText(String text) throws PartInitException {
        // do nothing
    }

}