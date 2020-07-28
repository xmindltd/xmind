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
 * A command server advertiser.
 * 
 * @author Frank Shaka
 */
public interface ICommandServerAdvertiser extends IDomainService {

    /**
     * Sets the information of a local command server that will be registered.
     * This method will be called by the command service domain after the
     * command server has been deployed and before the registration is started.
     * 
     * @param info
     *            the information to be registered, never <code>null</code>
     */
    void setRegisteringInfo(ICommandServiceInfo info);

    /**
     * Gets the information of the local command server that has been
     * registered. This method will be called by the command service domain.
     * 
     * <p>
     * Note that all information returned by this method represents only the
     * current state of the advertised command server. It may change due to
     * various events. Client code should <em>never</em> cache this value.
     * Instead, call this method each time you need info about the advertised
     * command server.
     * </p>
     * 
     * @return the registration information of the local command server, or
     *         <code>null</code> if registration is not finished
     */
    ICommandServiceInfo getRegisteredInfo();

    /**
     * Registers the local command server. The
     * {@link #setRegisteringInfo(Object)} method should have been called in
     * prior, otherwise an error status will be returned.
     * 
     * @param monitor
     *            the progress monitor
     * @return the status of the registration
     */
    IStatus register(IProgressMonitor monitor);

    /**
     * Unregisters the local command server. This method intends to undo any
     * operations that {@link #register(IProgressMonitor)} has performed.
     * 
     * @param monitor
     *            the progress monitor
     * @return the status of the unregistration
     */
    IStatus unregister(IProgressMonitor monitor);

}
