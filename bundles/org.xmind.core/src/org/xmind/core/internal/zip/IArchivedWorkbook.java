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
package org.xmind.core.internal.zip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.xmind.core.CoreException;

/**
 * 
 * @author frankshaka
 * @deprecated
 */
public interface IArchivedWorkbook {

    /**
     * @return the file
     */
    String getFile();

    /**
     * 
     * @throws IOException
     * @throws CoreException
     */
    void save() throws IOException, CoreException;

    /**
     * 
     * @param source
     * @throws IOException
     * @throws CoreException
     */
    void save(IArchivedWorkbook source) throws IOException, CoreException;

    /**
     * 
     * @param entryPath
     * @return
     */
    OutputStream getEntryOutputStream(String entryPath);

    /**
     * 
     * @param entryPath
     * @return
     */
    InputStream getEntryInputStream(String entryPath);

    /**
     * 
     * @param entryPath
     * @return
     */
    long getTime(String entryPath);

    /**
     * 
     * @param entryPath
     * @param time
     */
    void setTime(String entryPath, long time);

    long getSize(String entryPath);

}