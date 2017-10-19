package org.xmind.core.internal.command;

import org.eclipse.osgi.util.NLS;

/**
 * @auther Jason Wong
 */

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.xmind.core.internal.command.messages"; //$NON-NLS-1$

    public static String CommandJob_Name;

    public static String XMindCommandService_CommandHandledError_Message;
    public static String XMindCommandService_ConsumingValue;
    public static String XMindCommandService_ExcutingCommand;
    public static String XMindCommandService_HandingCommand_Message;
    public static String XMindCommandService_InvokingCommandError_Message;
    public static String XMindCommandService_SearchHandler_Message;
    public static String XMindCommandService_SearchHandlersError_Message;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
