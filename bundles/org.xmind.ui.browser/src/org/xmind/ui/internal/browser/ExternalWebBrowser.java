/*
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and above are dual-licensed
 * under the Eclipse Public License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors: XMind Ltd. - initial API and implementation
 */
package org.xmind.ui.internal.browser;

import java.net.URL;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.browser.AbstractWebBrowser;

public class ExternalWebBrowser extends AbstractWebBrowser {

    public ExternalWebBrowser(String id) {
        super(id);
    }

    public void openURL(URL url) throws PartInitException {
        String urlText = null;

        if (url != null)
            urlText = url.toExternalForm();

        // change spaces to "%20"
        if (urlText != null && !BrowserUtil.isWindows()) {
            int index = urlText.indexOf(" "); //$NON-NLS-1$
            while (index >= 0) {
                urlText = urlText.substring(0, index) + "%20" //$NON-NLS-1$
                        + urlText.substring(index + 1);
                index = urlText.indexOf(" "); //$NON-NLS-1$
            }
        }
        Program program = Program.findProgram("html"); //$NON-NLS-1$
        if (program != null) {
            if (program.execute(urlText))
                return;
        }
        if (!Program.launch(urlText))
            throw new PartInitException(
                    NLS
                            .bind(
                                    BrowserMessages.ExternalWebBrowser_ErrorCouldNotLaunchWebBrowser_message,
                                    url.toExternalForm()));
    }

}