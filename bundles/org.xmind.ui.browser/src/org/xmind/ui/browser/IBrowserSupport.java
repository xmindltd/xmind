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
package org.xmind.ui.browser;

/**
 * <p>
 * <b>IMPORTANT:</b> Since 3.6.51, only <em>external</em> browser are allowed,
 * and all other styles will be ignored.
 * </p>
 * 
 * @author Frank Shaka
 */
public interface IBrowserSupport {

    int AS_DEFAULT = 0;

    int AS_EXTERNAL = 1;

    @Deprecated
    int AS_VIEW = 1 << 1;

    @Deprecated
    int AS_EDITOR = 1 << 2;

    @Deprecated
    int NO_LOCATION_BAR = 1 << 10;

    @Deprecated
    int NO_EXTRA_CONTRIBUTIONS = 1 << 11;

    @Deprecated
    int NO_TOOLBAR = 1 << 12;

    @Deprecated
    int AS_INTERNAL = AS_VIEW | AS_EDITOR;

    int IMPL_TYPES = AS_EXTERNAL | AS_INTERNAL;

    int INTERNAL_STYLES = NO_LOCATION_BAR | NO_EXTRA_CONTRIBUTIONS | NO_TOOLBAR;

    IBrowser createBrowser(int style, String browserClientId, String name,
            String tooltip);

    IBrowser createBrowser(int style, String browserClientId);

    IBrowser createBrowser(String browserClientId);

    IBrowser createBrowser(int style);

    IBrowser createBrowser();

}