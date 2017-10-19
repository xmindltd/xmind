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
package org.xmind.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public interface IInputSource {

    /**
     * 
     * @param entryName
     * @return
     */
    boolean hasEntry(String entryName);

    /**
     * 
     * @return
     */
    Iterator<String> getEntries();

    /**
     * Determines whether a specified entry is a available.
     * 
     * @param entryName
     *            the name of the entry
     * @return <code>true</code> if the specified entry is available, or
     *         <code>false</code> otherwise
     */
    boolean isEntryAvailable(String entryName);

    /**
     * Opens a new input stream to read data from for specified entry.
     * 
     * @deprecated <strong>For diagnostic purpose, this method is not
     *             recommended any more. Use {@link #openEntryStream(String)}
     *             instead to let potential I/O errors be thrown.</strong>
     * 
     * @param entryName
     *            the name of the entry
     * @return an input stream for the specified entry, or <code>null</code> if
     *         the specified entry is not available
     */
    InputStream getEntryStream(String entryName);

    /**
     * Opens a new input stream to read data from for the specified entry.
     * 
     * @param entryName
     * @return an output stream for the specified entry (never <code>null</code>
     *         )
     * @throws IOException
     *             if I/O error occurs or entry is not found
     */
    InputStream openEntryStream(String entryName) throws IOException;

    /**
     * Returns the file size of the specific entry.
     * 
     * @param entryName
     *            The name of the entry
     * @return The file size of the specific entry; or <code>-1</code> if the
     *         entry does not exist
     */
    long getEntrySize(String entryName);

    /**
     * Returns the last modification time of the specific entry.
     * 
     * @param entryName
     *            The name of the entry
     * @return The last modification time of the specific entry; or
     *         <code>-1</code> if the entry does not exist
     */
    long getEntryTime(String entryName);

}