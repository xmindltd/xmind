/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.core.internal;

import java.io.File;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmind.core.Core;
import org.xmind.core.IWorkspace;
import org.xmind.core.util.FileUtils;

/**
 * 
 * @author Frank Shaka
 */
public class Workspace implements IWorkspace {

    private static final String P_WORKSPACE = "org.xmind.core.workspace"; //$NON-NLS-1$

    private static final String P_APPLIED_WORKSPACE = "org.xmind.core.workspace.applied"; //$NON-NLS-1$

    private static final Pattern EXPANSION_PATTERN = Pattern
            .compile("\\$\\{([^\\}]+)\\}"); //$NON-NLS-1$

    private String workingDirectory = null;

    private String defaultWorkingDirectory = null;

    public String getWorkingDirectory() {
        if (workingDirectory != null)
            return workingDirectory;

        String wd = System.getProperty(P_WORKSPACE);
        if (wd != null && !"".equals(wd)) { //$NON-NLS-1$
            workingDirectory = expandProperties(wd, System.getProperties());
            System.setProperty(P_APPLIED_WORKSPACE, workingDirectory);
            return workingDirectory;
        }

        return getDefaultWorkingDirectory();
    }

    public void setDefaultWorkingDirectory(String path) {
        this.defaultWorkingDirectory = path;
        if (workingDirectory == null && System.getProperty(P_WORKSPACE) == null) {
            System.setProperty(P_APPLIED_WORKSPACE, path);
        }
    }

    private String getDefaultWorkingDirectory() {
        if (defaultWorkingDirectory == null) {
            defaultWorkingDirectory = calculateDefaultWorkingDirectory();
            System.setProperty(P_APPLIED_WORKSPACE, defaultWorkingDirectory);
        }
        return defaultWorkingDirectory;
    }

    public void setWorkingDirectory(String path) {
        workingDirectory = path;
        System.setProperty(P_APPLIED_WORKSPACE, path);
    }

    public String getAbsolutePath(String subPath) {
        if (subPath == null)
            return null;
        String wd = getWorkingDirectory();
        File f = new File(wd, subPath);
        return f.getAbsolutePath();
    }

    public String getTempDir() {
        return getAbsolutePath(DIR_TEMP);
    }

    public String getTempDir(String subPath) {
        return FileUtils.ensureDirectory(new File(getTempDir(), subPath))
                .getAbsolutePath();
    }

    public String getTempFile(String fileName) {
        return FileUtils.ensureFileParent(new File(getTempDir(), fileName))
                .getAbsolutePath();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.IWorkspace#createTempFile(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    public synchronized File createTempFile(String subPath, String prefix,
            String suffix) {
        String subDir = getTempDir(subPath);
        File file;
        while (true) {
            file = new File(subDir, prefix + Core.getIdFactory().createId()
                    + suffix);
            if (!file.exists())
                break;
        }
        return file;
    }

    private static String calculateDefaultWorkingDirectory() {
        String userHome = System.getProperty("user.home"); //$NON-NLS-1$
        if (userHome == null) {
            return ".xmind"; //$NON-NLS-1$
        }
        return new File(userHome, ".xmind").getAbsolutePath(); //$NON-NLS-1$
    }

    private static String expandProperties(String input, Properties props) {
        StringBuffer buffer = new StringBuffer(input.length());
        Matcher m = EXPANSION_PATTERN.matcher(input);
        while (m.find()) {
            String name = m.group(1);
            String value = props.getProperty(name);
            if (value == null) {
                value = m.group();
            }
            // Replace '\' and '$' to literal '\\' and '\$'.
            value = value.replaceAll("([\\\\\\$])", "\\\\$1"); //$NON-NLS-1$ //$NON-NLS-2$
            m.appendReplacement(buffer, value);
        }
        m.appendTail(buffer);
        return buffer.toString();
    }

}