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
 * A remote command service discoverer.
 * 
 * @author Frank Shaka
 */
public interface IRemoteCommandServiceDiscoverer extends IDomainService {

    /**
     * Activates this remote command service discoverer.
     * 
     * @param monitor
     *            the progress monitor
     * @return the status of the activation
     */
    IStatus activate(IProgressMonitor monitor);

    /**
     * Deactivates this remote command service discoverer. This method intends
     * to undo any operation that {@link #activate(IProgressMonitor)} has
     * performed.
     * 
     * @param monitor
     *            the progress monitor
     * @return the status of the deactivation
     */
    IStatus deactivate(IProgressMonitor monitor);

    /**
     * Returns discovered remote command services. May not return an accurate
     * result if {@link #activate(IProgressMonitor, ICommandServiceInfo)} has
     * not been called and finished.
     * 
     * @return an array of discovered remote command services, possibly an empty
     *         array, but never <code>null</code>
     */
    IRemoteCommandService[] getRemoteCommandServices();

    /**
     * Finds the remote command service identified by the specified service
     * name. May not return an accurate result if
     * {@link #activate(IProgressMonitor, ICommandServiceInfo)} has not been
     * called and finished.
     * 
     * @param serviceName
     *            the unique name of the remote command service to find
     * @return a remote command service identified by the speicified service
     *         name, or <code>null</code> if not found
     * @see IIdentifier#getName()
     */
    IRemoteCommandService findRemoteCommandService(String serviceName);

    /**
     * Installs a remote command service listener to this discoverer.
     * 
     * @param listener
     *            the listener to add
     */
    void addRemoteCommandServiceListener(IRemoteCommandServiceListener listener);

    /**
     * Uninstalls a remote command service listener from this discoverer.
     * 
     * @param listener
     *            the listener to remove
     */
    void removeRemoteCommandServiceListener(
            IRemoteCommandServiceListener listener);

    /**
     * Drops all previously discovered remote command services and search again
     * for all existing remote command services. Dropped and discovered services
     * will all be notified to installed remote command service listeners.
     * 
     * <p>
     * This method may take a long time and block the current thread.
     * </p>
     * 
     * @param monitor
     *            the progress monitor
     * @return the status of the refresh operation
     */
    IStatus refresh(IProgressMonitor monitor);

}
