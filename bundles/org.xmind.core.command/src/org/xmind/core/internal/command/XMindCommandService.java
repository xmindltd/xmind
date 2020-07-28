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
package org.xmind.core.internal.command;

import java.util.List;
import java.util.regex.Matcher;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.xmind.core.command.ICommand;
import org.xmind.core.command.ICommandHandler;
import org.xmind.core.command.ICommandService;
import org.xmind.core.command.IReturnValueConsumer;
import org.xmind.core.command.ReturnValue;
import org.xmind.core.command.binary.IBinaryStore;
import org.xmind.core.internal.command.XMindCommandHandlerRegistry.CommandHandlerDescriptor;

/**
 * This implementation of command service delegates command execution to
 * specific command handlers.
 * 
 * @author Frank Shaka
 */
public class XMindCommandService implements ICommandService {

    private static final String[] NO_MATCH_GROUPS = new String[0];

    XMindCommandService() {
    }

    public IStatus execute(IProgressMonitor monitor, ICommand command,
            IReturnValueConsumer returnValueConsumer) {
        if (monitor == null)
            monitor = new NullProgressMonitor();
        monitor.beginTask(null, 100);

        monitor.subTask(NLS.bind(Messages.XMindCommandService_ExcutingCommand,
                command));
        IProgressMonitor executeMonitor = new SubProgressMonitor(monitor, 90);
        IStatus returnValue = executeCommand(executeMonitor, command);
        if (!executeMonitor.isCanceled())
            executeMonitor.done();

        try {
            monitor.subTask(Messages.XMindCommandService_ConsumingValue);
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

    private IStatus executeCommand(IProgressMonitor monitor, ICommand command) {
        monitor.beginTask(null, 100);

        monitor.subTask(Messages.XMindCommandService_SearchHandler_Message);

        List<CommandHandlerDescriptor> handlerDescriptors = XMindCommandHandlerRegistry
                .getInstance().findMatchedHandlerDescriptors(command);
        if (handlerDescriptors.isEmpty()) {
            monitor.done();
            return new Status(
                    IStatus.WARNING,
                    XMindCommandPlugin.PLUGIN_ID,
                    CODE_NO_HANDLERS,
                    NLS.bind(
                            Messages.XMindCommandService_SearchHandlersError_Message,
                            command), null);
        }
        if (monitor.isCanceled())
            return Status.CANCEL_STATUS;
        monitor.worked(10);

        monitor.subTask(NLS.bind(
                Messages.XMindCommandService_HandingCommand_Message, command));
        SubProgressMonitor handlersMonitor = new SubProgressMonitor(monitor, 80);
        IStatus returnValue = handleCommand(handlersMonitor, command,
                handlerDescriptors);
        if (returnValue != null && !returnValue.isOK()
                && returnValue.getSeverity() != IStatus.CANCEL) {
            Logger.log(returnValue);
        } else if (returnValue == null) {
            Logger.log("Command not handled: " + command, null); //$NON-NLS-1$
            returnValue = new Status(
                    IStatus.WARNING,
                    XMindCommandPlugin.PLUGIN_ID,
                    CODE_NOT_HANDLED,
                    NLS.bind(
                            Messages.XMindCommandService_CommandHandledError_Message,
                            command), null);
        }
        if (monitor.isCanceled())
            return Status.CANCEL_STATUS;
        handlersMonitor.done();
        monitor.done();
        return returnValue;
    }

    protected IStatus handleCommand(SubProgressMonitor monitor,
            ICommand command, List<CommandHandlerDescriptor> handlerDescriptors) {
        IStatus returnValue = null;
        try {
            monitor.beginTask(null, handlerDescriptors.size());
            for (CommandHandlerDescriptor handlerDescriptor : handlerDescriptors) {
                ICommandHandler handler = handlerDescriptor.getHandler();
                if (handler != null) {
                    SubProgressMonitor handlerMonitor = new SubProgressMonitor(
                            monitor, 1);
                    returnValue = handler.execute(handlerMonitor, command,
                            getMatchGroups(handlerDescriptor, command));
                    if (monitor.isCanceled())
                        return Status.CANCEL_STATUS;
                    handlerMonitor.done();
                    if (returnValue != null)
                        break;
                }
            }
        } catch (Throwable e) {
            return new Status(
                    IStatus.ERROR,
                    XMindCommandPlugin.PLUGIN_ID,
                    NLS.bind(
                            Messages.XMindCommandService_InvokingCommandError_Message,
                            e.toString()), e);
        }
        if (monitor.isCanceled())
            return Status.CANCEL_STATUS;
        monitor.done();
        return returnValue;
    }

    private String[] getMatchGroups(CommandHandlerDescriptor handlerDescriptor,
            ICommand command) {
        Matcher matcher = handlerDescriptor.match(command.getCommandName());
        if (!matcher.find())
            return NO_MATCH_GROUPS;
        int total = matcher.groupCount();
        if (total <= 0)
            return NO_MATCH_GROUPS;
        String[] groups = new String[total];
        for (int i = 0; i < total; i++) {
            groups[i] = matcher.group(i + 1);
        }
        return groups;
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
                    Logger.log(consumed);
                }
            } catch (Throwable e) {
                Logger.log("Error occurred while consuming return value.", //$NON-NLS-1$
                        e);
            }
        }
    }
}
