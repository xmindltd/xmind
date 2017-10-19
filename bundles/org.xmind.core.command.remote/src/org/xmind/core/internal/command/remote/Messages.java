package org.xmind.core.internal.command.remote;

import org.eclipse.osgi.util.NLS;

/**
 * @auther Jason Wong
 */

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.xmind.core.internal.command.remote.messages"; //$NON-NLS-1$
    public static String DefaultCommandServiceDomainDirector_ConnectionRemoteCommand;
    public static String DefaultCommandServiceDomainDirector_DisconnectionRemoteCommand;
    public static String IncomingCommandHandler_ConnectionClose;
    public static String IncomingCommandHandler_ExcuteCommand;
    public static String IncomingCommandHandler_ReadCommand;
    public static String IncomingCommandHandler_WriteReponseBack;
    public static String IncomingSocketCommandHandler_ConnectionFailed_Message;
    public static String OutgoingCommandHandler_ConnectionClose;
    public static String OutgoingCommandHandler_ConsumeReturnValue;
    public static String OutgoingCommandHandler_ExcuteCommand;
    public static String OutgoingCommandHandler_InvalidReturnValueCode;
    public static String OutgoingCommandHandler_InvalidReturnValueStatus;
    public static String OutgoingCommandHandler_ReceiveReturnValue;
    public static String OutgoingCommandHandler_SendCommand;
    public static String OutgoingSocketCommandHandler_ConnectionFailed_Message;
    public static String OutgoingSocketCommandHandler_ConnectionRemoteCommand_Message;
    public static String OutgoingSocketCommandHandler_ConnectionTimeOut;
    public static String OutgoingSocketCommandHandler_FailedOpenInputStream;
    public static String OutgoingSocketCommandHandler_FailedOpenOutputStream;
    public static String RemoteCommandJob_CommandSendError_Message;
    public static String SocketCommandServer_CloseCommandServerSocket;
    public static String SocketCommandServer_OpenCommandServerSocket;
    public static String SocketCommandServer_OperationLock;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
