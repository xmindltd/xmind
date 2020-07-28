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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.xmind.core.command.ICommand;
import org.xmind.core.command.IReturnValueConsumer;
import org.xmind.core.internal.command.remote.Messages;

/**
 * A job handling simple remote command execution.
 * 
 * @author Frank Shaka
 */
public abstract class RemoteCommandJob extends Job implements
        IReturnValueConsumer {

    private String pluginId;

    private IRemoteCommandService remoteCommandService;

    /**
     * 
     */
    public RemoteCommandJob(String jobName, String pluginId,
            IRemoteCommandService remoteCommandService) {
        super(jobName);
        this.pluginId = pluginId;
        this.remoteCommandService = remoteCommandService;
    }

    /**
     * @return the pluginId
     */
    public String getPluginId() {
        return pluginId;
    }

    /**
     * @return the remoteCommandService
     */
    public IRemoteCommandService getRemoteCommandService() {
        return remoteCommandService;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getAdapter(Class adapter) {
        if (adapter == IRemoteCommandService.class)
            return getRemoteCommandService();
        return super.getAdapter(adapter);
    }

    protected IStatus run(IProgressMonitor monitor) {
        monitor.beginTask(null, 100);

        /*
         * Create command.
         */
        ICommand command;
        SubProgressMonitor createCommandMonitor = new SubProgressMonitor(
                monitor, 10);
        try {
            command = createCommand(createCommandMonitor);
        } catch (CoreException e) {
            return e.getStatus();
        }
        if (monitor.isCanceled())
            return Status.CANCEL_STATUS;
        if (command == null)
            return new Status(IStatus.CANCEL, pluginId,
                    Messages.RemoteCommandJob_CommandSendError_Message);
        createCommandMonitor.done();

        /*
         * Send command.
         */
        SubProgressMonitor sendCommandMonitor = new SubProgressMonitor(monitor,
                90);
        IStatus executed = executeCommand(sendCommandMonitor, command);
        if (monitor.isCanceled())
            return Status.CANCEL_STATUS;
        sendCommandMonitor.done();
        monitor.done();
        return executed;
    }

    protected IStatus executeCommand(IProgressMonitor sendCommandMonitor,
            ICommand command) {
        return getRemoteCommandService().execute(sendCommandMonitor, command,
                this, getOptions());
    }

    protected abstract ICommand createCommand(IProgressMonitor monitor)
            throws CoreException;

    protected Options getOptions() {
        return Options.DEFAULT;
    }

}
