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
package org.xmind.core.command;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The command handling service. The service will look up registered command
 * handlers and delegate the execution of the command to the best matched
 * command handler.
 * 
 * <p>
 * Note that <code>ICommandHandler</code> and <code>IReturnValueConsumer</code>
 * must take the responsibility to stop execution when the cancellation of the
 * given progress monitor is detected.
 * </p>
 * 
 * <p>
 * To register a command handler, extend the
 * <code>org.xmind.core.command.handlers</code> extension point.
 * </p>
 * 
 * <p>
 * This service is registered as an OSGi service, so it can be retrieved via
 * {@link ServiceTracker}. Example:
 * 
 * <pre>
 * ServiceTracker commandServiceTracker;
 * 
 * public void start(BundleContext bundleContext) throws Exception {
 *     commandServiceTracker = new ServiceTracker(bundleContext,
 *             ICommandService.class.getName(), null);
 *     commandServiceTracker.open();
 * }
 * 
 * public void stop(BundleContext bundleContext) throws Exception {
 *     if (commandServiceTracker != null) {
 *         commandServiceTracker.close();
 *         commandServiceTracker = null;
 *     }
 * }
 * 
 * public ICommandService getCommandService() {
 *     if (commandServiceTracker != null) {
 *         return (ICommandService) commandServiceTracker.getService();
 *     }
 *     return null;
 * }
 * </pre>
 * 
 * </p>
 * 
 * @author Frank Shaka
 * @since 3.4.0
 * @see ICommandHandler
 */
public interface ICommandService {

    /**
     * Integer constant indicating that no handlers are available for executing
     * a command. Value is <code>10001</code>. Used as the 'code' of a return
     * value with {@link IStatus#WARNING} severity.
     */
    int CODE_NO_HANDLERS = 10001;

    /**
     * Integer constant indicating that a command is not handled by any of the
     * handlers. Value is <code>10002</code>. Used as the 'code' of a return
     * value with {@link IStatus#WARNING} severity.
     */
    int CODE_NOT_HANDLED = 10002;

    /**
     * Executes a command synchronously. This method may take a long time to
     * finish and will block the current thread, so it is recommended to run it
     * in a separate thread of an {@link org.eclipse.core.runtime.jobs.Job}.
     * 
     * <p>
     * The return value may contain cached resources which is cleaned right
     * before the method <code>execute()</code> returns, so the client may want
     * to pass in an <code>IReturnValueConsumer</code> to consume the cached
     * resources before they are cleaned.
     * </p>
     * 
     * @param monitor
     *            the progress monitor, or <code>null</code> if the progress
     *            monitoring is not required
     * @param command
     *            the command to handle
     * @param returnValueConsumer
     *            the return value consumer
     * @return the return value, never <code>null</code>
     */
    IStatus execute(IProgressMonitor monitor, ICommand command,
            IReturnValueConsumer returnValueConsumer);

}
