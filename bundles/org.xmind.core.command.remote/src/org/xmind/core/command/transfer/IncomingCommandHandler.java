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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.xmind.core.command.Command;
import org.xmind.core.command.ICommand;
import org.xmind.core.command.ICommandService;
import org.xmind.core.command.IReturnValueConsumer;
import org.xmind.core.command.ReturnValue;
import org.xmind.core.command.arguments.Attributes;
import org.xmind.core.command.binary.IBinaryStore;
import org.xmind.core.internal.command.remote.Messages;
import org.xmind.core.internal.command.remote.RemoteCommandPlugin;

/**
 * A basic handler that handles incoming command requests from an input stream
 * and writes the return value to an output stream.
 * 
 * @author Frank Shaka
 */
public class IncomingCommandHandler {

    private static final String DEBUG_OPTION = "/debug/incomingCommandHandler"; //$NON-NLS-1$

    private static boolean DEBUGGING = RemoteCommandPlugin.getDefault()
            .isDebugging(DEBUG_OPTION);

    private Object remoteLocation = "(unknown location)"; //$NON-NLS-1$

    private String pluginId = RemoteCommandPlugin.PLUGIN_ID;

    /**
     * 
     */
    public IncomingCommandHandler() {
    }

    /**
     * For debugging purpose only.
     * 
     * @param location
     *            the remote location representation
     */
    public void setRemoteLocation(Object location) {
        if (location == null) {
            location = "(unknown location)"; //$NON-NLS-1$
        }
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

    public IStatus handleIncomingCommand(IProgressMonitor monitor,
            InputStream input, OutputStream output) {
        ChunkReader reader = new ChunkReader(input);
        try {
            ChunkWriter writer = new ChunkWriter(output);
            try {
                return handleIncomingCommand(monitor, reader, writer);
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

    public IStatus handleIncomingCommand(IProgressMonitor monitor,
            ChunkReader reader, ChunkWriter writer) {
        monitor.beginTask(null, 100);

        // Read command:
        monitor.subTask(Messages.IncomingCommandHandler_ReadCommand);
        IProgressMonitor readMonitor = new SubProgressMonitor(monitor, 10);
        ICommand command;
        try {
            command = readCommand(readMonitor, reader);
        } catch (Throwable e) {
            return createReadingErrorStatus(e);
        }
        if (command == null || monitor.isCanceled())
            return Status.CANCEL_STATUS;
        readMonitor.done();

        try {
            // Execute command:
            monitor.subTask(NLS.bind(
                    Messages.IncomingCommandHandler_ExcuteCommand, command));
            IProgressMonitor executeMonitor = new SubProgressMonitor(monitor,
                    90);
            IStatus returnValue = executeCommand(executeMonitor, command,
                    writer);
            if (returnValue != null && !returnValue.isOK())
                return returnValue;
            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;
            executeMonitor.done();

            monitor.done();
            return returnValue;
        } finally {
            IBinaryStore binaryStore = command.getBinaryStore();
            if (binaryStore != null)
                binaryStore.clear();
        }
    }

    private ICommand readCommand(IProgressMonitor monitor, ChunkReader reader)
            throws IOException {
        monitor.beginTask(null, 100);

        if (DEBUGGING)
            System.out.println("Start reading command from " //$NON-NLS-1$
                    + remoteLocation);
        // Read command:
        String uri = reader.readText();
        if (uri == null)
            // No command available
            return null;

        if (DEBUGGING)
            System.out.println("Request received: " + uri); //$NON-NLS-1$
        if (monitor.isCanceled())
            return null;
        monitor.worked(50);

        // Read file arguments:
        String argumentType = reader.readText();
        if (monitor.isCanceled())
            return null;
        IBinaryStore files = null;
        IProgressMonitor filesMonitor = new SubProgressMonitor(monitor, 40);
        if (CommandTransferUtil.MARKER_FILES.equals(argumentType)) {
            files = CommandTransferUtil.readFiles(filesMonitor, reader);
            if (DEBUGGING)
                System.out.println("Files received: " + files); //$NON-NLS-1$
        }
        if (monitor.isCanceled())
            return null;
        filesMonitor.done();

        // Build command:
        ICommand command = Command.parseURI(uri, files);
        if (monitor.isCanceled())
            return null;
        monitor.done();
        return command;
    }

    private IStatus executeCommand(IProgressMonitor monitor, ICommand command,
            final ChunkWriter writer) {
        // Retrieve command handling service:
        ICommandService commandService = RemoteCommandPlugin.getDefault()
                .getCommandService();
        if (commandService == null) {
            try {
                writeReturnValue(monitor,
                        new Status(IStatus.ERROR, getPluginId(),
                                "No command handling service available."), //$NON-NLS-1$
                        writer);
            } catch (IOException e) {
                RemoteCommandPlugin.log(null, e);
            }
            return new Status(IStatus.WARNING, getPluginId(),
                    "No command handling service available."); //$NON-NLS-1$
        }
        if (monitor.isCanceled()) {
            try {
                writeReturnValue(monitor, Status.CANCEL_STATUS, writer);
            } catch (IOException e) {
                RemoteCommandPlugin.log(null, e);
            }
            return Status.CANCEL_STATUS;
        }

        // Execute command locally:
        IStatus returnValue = commandService.execute(monitor, command,
                new IReturnValueConsumer() {
                    public IStatus consumeReturnValue(IProgressMonitor monitor,
                            IStatus returnValue) {
                        try {
                            writeReturnValue(monitor, returnValue, writer);
                            if (monitor.isCanceled())
                                return Status.CANCEL_STATUS;
                            return Status.OK_STATUS;
                        } catch (Throwable e) {
                            return createWritingErrorStatus(e);
                        }
                    }
                });
        if (monitor.isCanceled())
            return Status.CANCEL_STATUS;
        monitor.done();
        return returnValue;
    }

    /**
     * @param monitor
     * @param returnValue
     * @param writer
     * @return
     */
    private void writeReturnValue(IProgressMonitor monitor,
            IStatus returnValue, ChunkWriter writer) throws IOException {
        if (DEBUGGING)
            System.out.println("Command executed: " + returnValue); //$NON-NLS-1$

        monitor.beginTask(null, 100);

        monitor.subTask(Messages.IncomingCommandHandler_WriteReponseBack);

        // Write status:
        writer.writeText(String.valueOf(returnValue.getSeverity()));
        monitor.worked(10);

        // Write plugin ID:
        writer.writeText(returnValue.getPlugin() == null ? "" : returnValue.getPlugin()); //$NON-NLS-1$
        monitor.worked(10);

        // Write plugin-specific code:
        writer.writeText(String.valueOf(returnValue.getCode()));
        monitor.worked(10);

        // Write message:
        writer.writeText(returnValue.getMessage());
        monitor.worked(10);

        // Write return value:
        SubProgressMonitor writeValueMonitor = new SubProgressMonitor(monitor,
                40);
        writeValue(writeValueMonitor, writer, returnValue);
        if (writeValueMonitor.isCanceled())
            return;
        writeValueMonitor.done();

        writer.writeText(""); //$NON-NLS-1$
        monitor.worked(10);

        writer.flush();
        monitor.worked(10);

        monitor.done();
    }

    private void writeValue(IProgressMonitor monitor, ChunkWriter writer,
            IStatus returnValue) throws IOException {
        if (returnValue.isOK()) {
            if (returnValue instanceof ReturnValue) {
                Object value = ((ReturnValue) returnValue).getValue();
                if (value instanceof Attributes) {
                    Attributes attrs = (Attributes) value;
                    monitor.beginTask(null, attrs.size());
                    writer.writeText(CommandTransferUtil.MARKER_PROPERTIES);
                    Iterator<String> names = attrs.keys();
                    while (names.hasNext()) {
                        String name = names.next();
                        writer.writeText(CommandTransferUtil.encode(name));
                        writer.writeText(CommandTransferUtil.encode(attrs
                                .getString(name, ""))); //$NON-NLS-1$
                        monitor.worked(1);
                    }
                } else if (value instanceof String[]) {
                    String[] strings = (String[]) value;
                    monitor.beginTask(null, strings.length);
                    writer.writeText(CommandTransferUtil.MARKER_VALUES);
                    for (int i = 0; i < strings.length; i++) {
                        writer.writeText(CommandTransferUtil.encode(strings[i]));
                        monitor.worked(1);
                    }
                } else if (value instanceof IBinaryStore) {
                    IBinaryStore files = (IBinaryStore) value;
                    writer.writeText(CommandTransferUtil.MARKER_FILES);
                    CommandTransferUtil.writeFiles(monitor, files, writer);
                }
            }
        }
    }

    protected IStatus createReadingErrorStatus(Throwable e) {
//        if (e instanceof EOFException) {
//            return new Status(
//                    IStatus.WARNING,
//                    getPluginId(),
//                    Messages.IncomingCommandHandler_ConnectionClose,
//                    e);
//        }
        return new Status(IStatus.ERROR, getPluginId(), null, e);
    }

    protected IStatus createWritingErrorStatus(Throwable e) {
        return new Status(IStatus.ERROR, getPluginId(), null, e);
    }

}
