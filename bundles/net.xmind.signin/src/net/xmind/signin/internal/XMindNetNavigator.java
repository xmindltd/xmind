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
package net.xmind.signin.internal;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.xmind.core.command.Command;
import org.xmind.core.command.CommandJob;
import org.xmind.core.command.ICommand;
import org.xmind.core.net.internal.EncodingUtils;
import org.xmind.ui.browser.BrowserSupport;
import org.xmind.ui.browser.IBrowser;
import org.xmind.ui.browser.IBrowserSupport;
import org.xmind.ui.internal.browser.BrowserUtil;

public class XMindNetNavigator {

    private static final String BROWSER_ID = "net.xmind.browser.commmon"; //$NON-NLS-1$

    private static final String EXTERNAL_BROWSER_ID = "net.xmind.browser.common.external"; //$NON-NLS-1$

    public void gotoURL(String url, Object... values) {
        gotoURL(false, url, values);
    }

    public void gotoURL(boolean external, String urlPattern, Object... values) {
        final String url = EncodingUtils.format(urlPattern, values);
        ICommand command = Command.parseURI(url);
        if (command != null) {
            new CommandJob(command, null).schedule();
        } else {
            final IBrowser browser;
            if (external) {
                browser = BrowserSupport.getInstance().createBrowser(
                        IBrowserSupport.AS_EXTERNAL, EXTERNAL_BROWSER_ID);
            } else {
                browser = BrowserSupport.getInstance().createBrowser(
                        IBrowserSupport.AS_EDITOR
                                | IBrowserSupport.NO_LOCATION_BAR
                                | IBrowserSupport.NO_EXTRA_CONTRIBUTIONS,
                        BROWSER_ID);
            }
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    browser.openURL(BrowserUtil.makeRedirectURL(url));
                }
            });
        }
    }

}
