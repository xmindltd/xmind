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
package org.xmind.core.internal.runtime;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.datalocation.Location;
import org.xmind.core.Core;
import org.xmind.core.internal.Workspace;

public class WorkspaceConfigurer {

    public static final String INSTANCE_LOCATION = "${osgi.instance.area}"; //$NON-NLS-1$

    public static final String USER_HOME = "${user.home}"; //$NON-NLS-1$

    private static final Pattern EXPANSION = Pattern
            .compile("\\$\\{([^\\}]+)\\}"); //$NON-NLS-1$

    private WorkspaceConfigurer() {
        throw new AssertionError();
    }

    public static void setDefaultWorkspaceLocation(String path) {
        String location = expandWorkspaceLocation(path);
        File dir = new File(location);
        if (!dir.isDirectory()) {
            dir.mkdirs();
        }
        ((Workspace) Core.getWorkspace()).setDefaultWorkingDirectory(location);
    }

    private static String expandWorkspaceLocation(String path) {
        if (INSTANCE_LOCATION.equals(path)) {
            String oldWorkspaceLocation = expandWorkspaceLocation(path
                    + "/.xmind"); //$NON-NLS-1$
            if (new File(oldWorkspaceLocation).isDirectory())
                return oldWorkspaceLocation;
        }

        File instanceLocation = calculateInstanceDir();

        Properties p = new Properties();
        p.putAll(System.getProperties());
        p.put(INSTANCE_LOCATION.substring(2, INSTANCE_LOCATION.length() - 1),
                instanceLocation.getAbsolutePath());

        StringBuffer buffer = new StringBuffer(path.length() * 2);
        Matcher m = EXPANSION.matcher(path);
        while (m.find()) {
            String value = p.getProperty(m.group(1));
            if (value == null)
                value = m.group();
            // Replace '\' and '$' to literal '\\' and '\$'.
            value = value.replaceAll("([\\\\\\$])", "\\\\$1"); //$NON-NLS-1$ //$NON-NLS-2$
            m.appendReplacement(buffer, value);
        }
        m.appendTail(buffer);
        return buffer.toString();
    }

    private static File calculateInstanceDir() {
        Location loc = Platform.getInstanceLocation();
        if (loc == null) {
            // Instance location not specified.
            return calculateDefaultInstanceDir();
        }
        URL url = loc.getURL();
        try {
            url = FileLocator.toFileURL(url);
        } catch (IOException e) {
            // Invalid URL.
            return calculateDefaultInstanceDir();
        }

        String file = url.getFile();
        if (file == null || "".equals(file)) {//$NON-NLS-1$
            // Invalid URL path.
            return calculateDefaultInstanceDir();
        }

        return new File(file);
    }

    private static File calculateDefaultInstanceDir() {
        String homeDir = System.getProperty("user.home"); //$NON-NLS-1$
        String os = Platform.getOS();
        if (Platform.OS_WIN32.equals(os)) {
            return new File(new File(new File(homeDir, "Application Data"), //$NON-NLS-1$
                    "XMind"), //$NON-NLS-1$
                    "workspace-cathy"); //$NON-NLS-1$
        } else if (Platform.OS_MACOSX.equals(os)) {
            return new File(new File(new File(homeDir, "Library"), //$NON-NLS-1$
                    "XMind"), //$NON-NLS-1$
                    "workspace-cathy"); //$NON-NLS-1$
        } else {
            return new File(new File(homeDir, ".xmind"), //$NON-NLS-1$
                    "workspace-cathy"); //$NON-NLS-1$
        }
    }

}
