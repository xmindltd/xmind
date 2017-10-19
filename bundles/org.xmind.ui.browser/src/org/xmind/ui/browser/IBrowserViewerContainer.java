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
package org.xmind.ui.browser;

import org.eclipse.swt.browser.Browser;
import org.eclipse.ui.IActionBars;

public interface IBrowserViewerContainer {

    String getClientId();

    /**
     * Closes the container.
     * 
     * @return
     */
    boolean close();

    /**
     * Returns the action bars of the container.
     * 
     * @return action bars of the container or <code>null</code> if not
     *         available.
     */
    IActionBars getActionBars();

    /**
     * Opens the url in the external browser if internal browser failed to
     * create.
     * 
     * @param url
     */
    void openInExternalBrowser(String url);

    /**
     * Creates a new browser instance for the viewer to open a new browser
     * window on.
     * 
     * @return a <code>Browser</code> instance or <code>null</code> if not
     *         available.
     */
    Browser openNewBrowser();

}