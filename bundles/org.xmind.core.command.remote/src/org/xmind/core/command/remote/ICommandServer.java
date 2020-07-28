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
 * A local command server listens for incoming command-handling requests,
 * executes commands using {@link org.xmind.core.command.ICommandService} and
 * sends return value back to the client.
 * 
 * @author Frank Shaka
 */
public interface ICommandServer extends IDomainService {

    /**
     * Deploys this command server.
     * 
     * <p>
     * This method is not intended to be called by clients directly because it
     * will be called automatically by the {@link ICommandServiceDomain} on
     * activation.
     * </p>
     * 
     * <p>
     * This method may take a long time and block the current thread.
     * </p>
     * 
     * @param monitor
     *            the progress monitor
     * @return the status of the deployment
     */
    IStatus deploy(IProgressMonitor monitor);

    /**
     * Undeploys this command server.
     * 
     * <p>
     * This method is not intended to be called by clients directly because it
     * will be called automatically by the {@link ICommandServiceDomain} on
     * deactivation.
     * </p>
     * 
     * <p>
     * This method may take a long time and block the current thread.
     * </p>
     * 
     * @param monitor
     *            the progress monitor
     * @return the status of the undeployment
     */
    IStatus undeploy(IProgressMonitor monitor);

    /**
     * Returns the information that will be used when registering this command
     * server in the command service domain.
     * 
     * @return the information used for registration, or <code>null</code> if
     *         the server is not deployed yet or has been undeployed
     */
    ICommandServiceInfo getRegisteringInfo();

}
