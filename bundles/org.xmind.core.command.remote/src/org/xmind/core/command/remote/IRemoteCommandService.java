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
import org.xmind.core.command.ICommand;
import org.xmind.core.command.IReturnValueConsumer;

/**
 * A command handling service delegating the command execution by a remote
 * command service provider.
 * 
 * @author Frank Shaka
 */
public interface IRemoteCommandService {

    /**
     * Executes a command synchronously. This method may take a long time to
     * finish and will block the current thread, so it is recommended to run it
     * in a separate thread or an {@link org.eclipse.core.runtime.jobs.Job}.
     * 
     * <p>
     * The return value may contain cached resources which is cleaned right
     * before this method returns, so the client may want to pass in an
     * <code>IReturnValueConsumer</code> to consume the cached resources before
     * they are cleaned.
     * </p>
     * 
     * @param monitor
     *            the progress monitor, or <code>null</code> if progress
     *            monitoring is not required
     * @param command
     *            the command to handle, should never be <code>null</code>
     * @param returnValueConsumer
     *            the return value consumer, or <code>null</code> if return
     *            value consumption is not needed
     * @param options
     *            options to control the execution process, or <code>null</code>
     *            to use default options
     * @return the return value, never <code>null</code>
     * @throws org.eclipse.core.runtime.AssertionFailedException
     *             if any argument is invalid, e.g. the command is
     *             <code>null</code>
     */
    IStatus execute(IProgressMonitor monitor, ICommand command,
            IReturnValueConsumer returnValueConsumer, Options options);

    /**
     * Returns the information and metadata of this remote command service.
     * 
     * @return the information and metadata of this remote command service
     */
    ICommandServiceInfo getInfo();

}
