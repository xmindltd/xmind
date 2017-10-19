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
package org.xmind.core;

import java.io.File;

/**
 * A workspace is a location in local file system where XMind Core stores
 * temporary and preference data.
 * 
 * <p>
 * <b>NOTE:</b> <em>This interface is not intended to be implemented by client.
 * Use the facade method <code>Core.getWorkspace()</code> to get a singleton
 * instance.</em>
 * </p>
 * 
 * @author Frank Shaka (frank@xmind.net)
 * @since 1.0
 */
public interface IWorkspace {

    /**
     * A relative path representing the temporary sub-directory inside a
     * workspace.
     * 
     * @see #getTempDir()
     */
    String DIR_TEMP = "temp"; //$NON-NLS-1$

    /**
     * Returns the root directory of this workspace. XMind Core must be given
     * read-and-write access to this directory and all contents inside it.
     * 
     * <p>
     * The result will be searched for in the following order:
     * 
     * <ol>
     * <li>the path set by invoking <code>setWorkingDirectory(String)</code>;
     * </li>
     * <li><code>"${org.xmind.core.workspace}"</code> if
     * <code>org.xmind.core.workspace</code> system property exists;</li>
     * <li><code>".xmind"</code> under the program's current working directory.
     * </li>
     * </ol>
     * </p>
     * 
     * @return the root directory of this workspace (never <code>null</code>)
     */
    String getWorkingDirectory();

    /**
     * Sets the root directory of this workspace. The current program must be
     * given read-and-write access to this directory and all contents inside it.
     * If the directory does not exist, it will be automatically created on
     * demand.
     * 
     * @param path
     *            an absolute path to set, or <code>null</code> to indicate
     *            system defaults should be used
     */
    void setWorkingDirectory(String path);

    /**
     * Calculates and returns an absolute file path representing a sub-directory
     * inside this workspace specified by the relative <code>subPath</code>
     * based on the root of this workspace.
     * 
     * @param subPath
     *            a relative path inside this workspace
     * @return an absolute path representing the desired file inside this
     *         workspace
     */
    String getAbsolutePath(String subPath);

    /**
     * Returns an absolute path representing the sub-directory inside this
     * workspace for storing temporary data.
     * 
     * <p>
     * This method behaves equivalent to <code>getAbsolutePath(DIR_TEMP)</code>.
     * </p>
     * 
     * @return an absolute path of the temporary data sub-directory
     */
    String getTempDir();

    /**
     * Calculates and returns an absolute path representing a sub-directory
     * inside this workspace specified by the relative <code>subDir</code> based
     * on the temporary data sub-directory in this workspace. If the
     * sub-directory does not exist, it will be created before returned.
     * 
     * @param subDir
     *            a relative path based on the temporary data sub-directory
     * @return an absolute path representing the desired directory inside this
     *         workspace
     */
    String getTempDir(String subDir);

    /**
     * Calculates and returns an absolute path representing a file inside this
     * workspace specified by the relative <code>fileName</code> based on the
     * temporary data sub-directory in this workspace.
     * 
     * @param fileName
     *            a relative path based on the temporary data sub-directory
     * @return an absolute path representing the desired file inside this
     *         workspace
     */
    String getTempFile(String fileName);

    /**
     * Creates a new empty file in the directory specified by the relative
     * <code>subDir</code> based on the temporary data sub-directory inside this
     * workspace, using the given <code>prefix</code> and <code>suffix</code> to
     * generate its name. It is guaranteed that the file denoted by the returned
     * abstract pathname did not exist before this method was invoked.
     * 
     * <p>
     * This method simply ensures that the <code>subDir</code> sub-directory
     * exists and calls <code>File.createTempFile(String, String, File)</code>
     * to create the empty file.
     * </p>
     * 
     * @param subDir
     *            a relative path based on the temporary data sub-directory
     * @param prefix
     *            The prefix string to be used in generating the file's name;
     *            must be at least three characters long
     * @param suffix
     *            The suffix string to be used in generating the file's name;
     *            may be null, in which case the suffix ".tmp" will be used
     * @return a new empty file
     */
    File createTempFile(String subDir, String prefix, String suffix);

}
