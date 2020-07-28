/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
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
/**
 * 
 */
package org.xmind.ui.internal.app;

import org.eclipse.equinox.app.IApplicationContext;

/**
 * A service that determines whether the current application should exit in an
 * early state.
 * 
 * @author Frank Shaka
 * @since 3.6.51
 */
public interface IApplicationValidator {

    /**
     * Checks application status and returns <code>true</code> to prevent this
     * application from starting up. This method is called within the main
     * thread and blocks it. Blocking dialogs are allowed in this method to
     * assist user make choices.
     * 
     * @param appContext
     * @return <code>true</code> if this application should stop, or
     *         <code>false</code> otherwise
     */
    boolean shouldApplicationExitEarly(IApplicationContext appContext);

}
