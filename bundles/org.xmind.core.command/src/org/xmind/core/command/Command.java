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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;

import org.xmind.core.command.arguments.Attributes;
import org.xmind.core.command.binary.IBinaryEntry;
import org.xmind.core.command.binary.IBinaryStore;
import org.xmind.core.internal.command.Logger;
import org.xmind.core.internal.command.XMindCommandPlugin;

public class Command implements ICommand {

    private static boolean DEBUGGING = XMindCommandPlugin.isDebugging("/debug"); //$NON-NLS-1$

    public static final String XMIND_SCHEME = "xmind"; //$NON-NLS-1$

    public static final String XMIND_PROTOCOL = XMIND_SCHEME + ":"; //$NON-NLS-1$

    private String source;

    private String commandName;

    private Attributes arguments;

    private String target;

    private IBinaryStore files;

    public Command(String source, String commandName) {
        this(source, commandName, null, null, null);
    }

    public Command(String source, String commandName, Attributes arguments,
            String target, IBinaryStore files) {
        this.source = source;
        this.commandName = commandName;
        if (arguments == null) {
            this.arguments = new Attributes();
        } else {
            this.arguments = arguments;
        }
        this.target = target;
        this.files = files;
    }

    public String getCommandName() {
        return commandName;
    }

    public Attributes getArguments() {
        return arguments;
    }

    public String getArgument(String key) {
        return arguments.get(key);
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public IBinaryStore getBinaryStore() {
        return files;
    }

    public IBinaryEntry getBinaryEntry(String entryName) {
        return files == null ? null : files.getEntry(entryName);
    }

    public static Command parseURI(String uriString, IBinaryStore files) {
        URI uri;
        try {
            uri = new URI(uriString);
        } catch (URISyntaxException e) {
            Logger.log("Malformed URI: " + uriString, null); //$NON-NLS-1$
            return null;
        }

        if (!XMIND_SCHEME.equals(uri.getScheme())) {
            if (DEBUGGING)
                System.out
                        .println("Not a valid XMind command URI: " + uriString); //$NON-NLS-1$
            return null;
        }

        return new Command(uri.getHost(), trimPath(uri.getPath()),
                parseArguments(uri.getRawQuery()), uri.getFragment(), files);
    }

    public static Command parseURI(String uriString) {
        return parseURI(uriString, null);
    }

    private static String trimPath(String path) {
        if (path == null)
            return path;
        if (path.startsWith("/")) //$NON-NLS-1$
            path = path.substring(1);
        if (path.endsWith("/")) //$NON-NLS-1$
            path = path.substring(0, path.length() - 1);
        return path;
    }

    private static Attributes parseArguments(String query) {
        if (query == null || "".equals(query)) //$NON-NLS-1$
            return new Attributes();
        Attributes args = new Attributes();
        for (String kv : query.split("&")) { //$NON-NLS-1$
            int sep = kv.indexOf('=');
            if (sep < 0) {
                args.with(urlDecode(kv), ""); //$NON-NLS-1$
            } else {
                args.with(urlDecode(kv.substring(0, sep)),
                        urlDecode(kv.substring(sep + 1)));
            }
        }
        return args;
    }

    private static String urlDecode(String text) {
        try {
            return URLDecoder.decode(text, "UTF-8"); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            if (DEBUGGING) {
                System.err.println("Failed to decode XMind command argument: " //$NON-NLS-1$
                        + text);
                e.printStackTrace();
            }
            return text;
        }
    }

    private static String urlEncode(String text) {
        try {
            return URLEncoder.encode(text, "UTF-8"); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            if (DEBUGGING) {
                System.err.println("Failed to encode XMind command argument: " //$NON-NLS-1$
                        + text);
                e.printStackTrace();
            }
            return text;
        }
    }

    public String toURI() {
        return toURI(this);
    }

    @Override
    public String toString() {
        return toURI();
    }

    public static String toURI(ICommand command) {
        StringBuffer buf = new StringBuffer(32);
        buf.append(XMIND_PROTOCOL);
        buf.append('/');
        buf.append('/');
        buf.append(command.getSource());

        String commandName = command.getCommandName();
        if (commandName != null) {
            if (!commandName.startsWith("/")) { //$NON-NLS-1$
                buf.append('/');
            }
            buf.append(commandName);
        } else {
            buf.append('/');
        }

        Attributes arguments = command.getArguments();
        if (arguments != null && !arguments.isEmpty()) {
            buf.append('?');
            Iterator<String> keys = arguments.keys();
            boolean firstArgument = true;
            while (keys.hasNext()) {
                String key = keys.next();
                String value = arguments.get(key);
                if (value != null) {
                    if (!firstArgument) {
                        buf.append('&');
                    } else {
                        firstArgument = false;
                    }
                    buf.append(urlEncode(key));
                    buf.append('=');
                    buf.append(urlEncode(value));
                }
            }
        }

        String target = command.getTarget();
        if (target != null) {
            buf.append('#');
            buf.append(target);
        }
        return buf.toString();
    }

}
