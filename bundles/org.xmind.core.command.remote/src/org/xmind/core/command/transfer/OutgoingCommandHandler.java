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
package org.xmind.core.command.transfer;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.xmind.core.command.Command;
import org.xmind.core.command.ICommand;
import org.xmind.core.command.IReturnValueConsumer;
import org.xmind.core.command.ReturnValue;
import org.xmind.core.command.arguments.Attributes;
import org.xmind.core.command.binary.IBinaryStore;
import org.xmind.core.internal.command.remote.Messages;
import org.xmind.core.internal.command.remote.RemoteCommandPlugin;

/**
 * A basic handler that sends a command to a remote command service via an
 * output stream and wait for responded return value from an input stream.
 * 
 * @author Frank Shaka
 */
public class OutgoingCommandHandler {

    private static final String DEBUG_OPTION = "/debug/outgoingCommandHandler"; //$NON-NLS-1$

    private static boolean DEBUGGING = RemoteCommandPlugin.getDefault()
            .isDebugging(DEBUG_OPTION);

    private Object remoteLocation = "(unknown location)"; //$NON-NLS-1$

    private String pluginId = RemoteCommandPlugin.PLUGIN_ID;

    /**
     * 
     */
    public OutgoingCommandHandler() {
    }

    /**
     * For debugging purpose only.
     * 
     * @param location
     *            the remote location representation
     */
    public void setRemoteLocation(Object location) {
        if (location == null)
            location = "(unknown location)"; //$NON-NLS-1$
        this.remoteLocation = location;
    }

    public Object getRemoteLocation() {
        return remoteLocation;
    }

    public void setPluginId(String pluginId) {
        if (pluginId == null)
            pluginId = RemoteCommandPlugin.PLUGIN_ID;
        this.pluginId = pluginId;
    }

    public String getPluginId() {
        return pluginId;
    }

    public IStatus handleOutgoingCommand(IProgressMonitor monitor,
            ICommand command, IReturnValueConsumer returnValueConsumer,
            InputStream input, OutputStream output) {
        ChunkReader reader = new ChunkReader(input);
        try {
            ChunkWriter writer = new ChunkWriter(output);
            try {
                return handleOutgoingCommand(monitor, command,
                        returnValueConsumer, reader, writer);
            } finally {
                try {
                    writer.close();
                } catch (IOException e) {
                    RemoteCommandPlugin.log(null, e);
                }
            }
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                RemoteCommandPlugin.log(null, e);
            }
        }
    }

    public IStatus handleOutgoingCommand(IProgressMonitor monitor,
            ICommand command, IReturnValueConsumer returnValueConsumer,
            ChunkReader reader, ChunkWriter writer) {
        monitor.beginTask(null, 100);

        monitor.subTask(Messages.OutgoingCommandHandler_ExcuteCommand);
        IProgressMonitor executeMonitor = new SubProgressMonitor(monitor, 90);
        IStatus returnValue = executeCommandRemotely(executeMonitor, command,
                reader, writer);
        if (!executeMonitor.isCanceled())
            executeMonitor.done();

        try {
            monitor.subTask(Messages.OutgoingCommandHandler_ConsumeReturnValue);
            IProgressMonitor consumeMonitor = new SubProgressMonitor(monitor,
                    10);
            consumeCommand(consumeMonitor, command, returnValue,
                    returnValueConsumer);
            if (!consumeMonitor.isCanceled())
                consumeMonitor.done();

            if (!monitor.isCanceled())
                monitor.done();
            return returnValue;
        } finally {
            if (returnValue != null && returnValue instanceof ReturnValue) {
                Object value = ((ReturnValue) returnValue).getValue();
                if (value instanceof IBinaryStore) {
                    ((IBinaryStore) value).clear();
                }
            }
        }
    }

    private IStatus executeCommandRemotely(IProgressMonitor monitor,
            ICommand command, ChunkReader reader, ChunkWriter writer) {
        monitor.beginTask(null, 100);

        monitor.subTask(Messages.OutgoingCommandHandler_SendCommand);
        IProgressMonitor sendMonitor = new SubProgressMonitor(monitor, 30);
        try {
            sendCommand(sendMonitor, command, writer);
        } catch (Throwable e) {
            return createSendingErrorStatus(e);
        }
        if (sendMonitor.isCanceled())
            return Status.CANCEL_STATUS;
        sendMonitor.done();

        monitor.subTask(Messages.OutgoingCommandHandler_ReceiveReturnValue);
        IProgressMonitor receiveMonitor = new SubProgressMonitor(monitor, 70);
        IStatus returnValue;
        try {
            returnValue = receive(receiveMonitor, reader);
        } catch (Throwable e) {
            returnValue = createReceivingErrorStatus(e);
        }
        if (!receiveMonitor.isCanceled())
            receiveMonitor.done();
        if (!monitor.isCanceled())
            monitor.done();
        return returnValue;
    }

    private void sendCommand(IProgressMonitor monitor, ICommand command,
            ChunkWriter writer) throws IOException {
        monitor.beginTask(null, 100);

        if (DEBUGGING)
            System.out.println("Sending command to " //$NON-NLS-1$
                    + remoteLocation + ": " + command); //$NON-NLS-1$
        long start = System.currentTimeMillis();
        String uri = Command.toURI(command);
        writer.writeText(uri);
        if (monitor.isCanceled())
            return;
        monitor.worked(40);

        IBinaryStore files = command.getBinaryStore();
        if (files != null && !files.isEmpty()) {
            writer.writeText(CommandTransferUtil.MARKER_FILES);
            if (monitor.isCanceled())
                return;
            monitor.worked(10);

            IProgressMonitor filesMonitor = new SubProgressMonitor(monitor, 40);
            CommandTransferUtil.writeFiles(filesMonitor, files, writer);
            if (monitor.isCanceled())
                return;
            filesMonitor.done();
        } else {
            monitor.worked(50);
        }
        writer.writeText(""); //$NON-NLS-1$
        if (monitor.isCanceled())
            return;
        monitor.worked(10);

        writer.flush();
        if (monitor.isCanceled())
            return;
        monitor.done();

        long end = System.currentTimeMillis();
        if (DEBUGGING)
            System.out.println("Command sent to " //$NON-NLS-1$
                    + remoteLocation + " (" //$NON-NLS-1$
                    + (end - start) + " ms): " + command); //$NON-NLS-1$
    }

    private IStatus receive(IProgressMonitor monitor, ChunkReader reader)
            throws IOException {
        monitor.beginTask(null, 100);

        if (DEBUGGING)
            System.out.println("Receiving return value from " //$NON-NLS-1$
                    + remoteLocation);
        long start = System.currentTimeMillis();

        String status = reader.readText();
        if (status == null)
            return null;

        int severity;
        try {
            severity = Integer.parseInt(status, 10);
        } catch (NumberFormatException e) {
            return new Status(IStatus.ERROR, getPluginId(), NLS.bind(
                    Messages.OutgoingCommandHandler_InvalidReturnValueStatus,
                    status));
        }
        if (monitor.isCanceled())
            return Status.CANCEL_STATUS;
        monitor.worked(15);

        String pluginId = reader.readText();
        if (monitor.isCanceled())
            return Status.CANCEL_STATUS;
        monitor.worked(15);

        String codeStr = reader.readText();
        int code;
        if (codeStr != null && !"".equals(codeStr)) { //$NON-NLS-1$
            try {
                code = Integer.parseInt(codeStr, 10);
            } catch (NumberFormatException e) {
                return new Status(IStatus.ERROR, getPluginId(), NLS.bind(
                        Messages.OutgoingCommandHandler_InvalidReturnValueCode,
                        codeStr));
            }
        } else {
            code = 0;
        }
        if (monitor.isCanceled())
            return Status.CANCEL_STATUS;
        monitor.worked(15);

        String message = reader.readText();
        if (monitor.isCanceled())
            return Status.CANCEL_STATUS;
        monitor.worked(15);

        String responseType = reader.readText();
        if (monitor.isCanceled())
            return Status.CANCEL_STATUS;
        monitor.worked(15);

        IStatus returnValue;
        if (CommandTransferUtil.MARKER_PROPERTIES.equals(responseType)) {
            Attributes attrs = new Attributes();
            String name;
            String value;
            while ((name = reader.readText()) != null
                    && (value = reader.readText()) != null) {
                if (monitor.isCanceled())
                    return Status.CANCEL_STATUS;

                if ("".equals(name) || "".equals(value)) //$NON-NLS-1$ //$NON-NLS-2$
                    break;

                attrs.with(CommandTransferUtil.decode(name),
                        CommandTransferUtil.decode(value));
            }
            returnValue = new ReturnValue(severity, pluginId, code, message,
                    attrs);
        } else if (CommandTransferUtil.MARKER_VALUES.equals(responseType)) {
            List<String> values = new ArrayList<String>();
            String value;
            while ((value = reader.readText()) != null) {
                if (monitor.isCanceled())
                    return Status.CANCEL_STATUS;
                if ("".equals(value)) //$NON-NLS-1$
                    break;
                values.add(CommandTransferUtil.decode(value));
            }
            returnValue = new ReturnValue(severity, pluginId, code, message,
                    values.toArray(new String[values.size()]));
        } else if (CommandTransferUtil.MARKER_FILES.equals(responseType)) {
            IProgressMonitor filesMonitor = new SubProgressMonitor(monitor, 20);
            IBinaryStore files = CommandTransferUtil.readFiles(filesMonitor,
                    reader);
            if (filesMonitor.isCanceled()) {
                if (files != null)
                    files.clear();
                return Status.CANCEL_STATUS;
            }
            filesMonitor.done();
            returnValue = new ReturnValue(severity, pluginId, code, message,
                    files);
        } else {
            returnValue = new Status(severity, pluginId, code, message, null);
        }
        if (monitor.isCanceled())
            return Status.CANCEL_STATUS;
        monitor.done();

        long end = System.currentTimeMillis();
        if (DEBUGGING) {
            System.out.println("Return value received from " //$NON-NLS-1$
                    + remoteLocation + " (" //$NON-NLS-1$
                    + (end - start) + " ms): " + returnValue); //$NON-NLS-1$
        }
        return returnValue;
    }

    private void consumeCommand(IProgressMonitor monitor, ICommand command,
            IStatus returnValue, IReturnValueConsumer returnValueConsumer) {
        if (returnValueConsumer != null) {
            try {
                IStatus consumed = returnValueConsumer.consumeReturnValue(
                        monitor, returnValue);
                if (!monitor.isCanceled() && consumed != null
                        && !consumed.isOK()
                        && consumed.getSeverity() != IStatus.CANCEL) {
                    RemoteCommandPlugin.log(consumed);
                }
            } catch (Throwable e) {
                RemoteCommandPlugin.log(
                        "Error occurred while consuming return value.", //$NON-NLS-1$
                        e);
            }
        }
    }

    protected IStatus createSendingErrorStatus(Throwable e) {
        return new Status(IStatus.ERROR, getPluginId(), null, e);
    }

    protected IStatus createReceivingErrorStatus(Throwable e) {
        if (e instanceof EOFException) {
            return new Status(IStatus.WARNING, getPluginId(),
                    Messages.OutgoingCommandHandler_ConnectionClose, e);
        }
        return new Status(IStatus.ERROR, getPluginId(), null, e);
    }

}
