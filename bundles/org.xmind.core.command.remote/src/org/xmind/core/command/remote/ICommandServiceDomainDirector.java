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
/**
 * 
 */
package org.xmind.core.command.remote;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * A command service domain director.
 * 
 * @author Frank Shaka
 */
public interface ICommandServiceDomainDirector extends IDomainService {

    int INACTIVE = 1;

    int ACTIVATING = 2;

    int ACTIVE = 3;

    int DEACTIVATING = 4;

    /**
     * Opens a connection to the command service domain this director is
     * responsible for. All services provided by this domain, e.g. the remote
     * command service discoverer, will be activated on the first connection
     * being opened. Subsequent connections will wait for the activation or, if
     * the activation has finished, return immediately.
     * 
     * <p>
     * This method may take a long time and block the current thread.
     * </p>
     * 
     * @param monitor
     *            the progress monitor
     * @return the connection token
     */
    IStatus connect(IProgressMonitor monitor);

    /**
     * Closes a connection to the command service domain this director is
     * responsible for. All services provided by this domain, e.g. the remote
     * command service discoverer, will be deactivated on the last connection
     * being closed. Disconnections prior to the last one will return
     * immediately.
     * 
     * <p>
     * This method may take a long time and block the current thread.
     * </p>
     * 
     * @param monitor
     *            the progress monitor
     * @return the status of the disconnection
     */
    IStatus disconnect(IProgressMonitor monitor);

    /**
     * Returns the status code of this director.
     * 
     * @return the status code of this director
     */
    int getStatus();

}
