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
package org.xmind.ui.internal.statushandlers;

/**
 * @author Frank Shaka
 *
 */
public interface IErrorReporter {

    /**
     * Reports the specified error to the product vendor. This method may block
     * the current thread.
     * 
     * @param error
     *            the error to report
     * @return <code>true</code> if reported successfully, or <code>false</code>
     *         otherwise
     */
    boolean report(StatusDetails error) throws InterruptedException;

}
