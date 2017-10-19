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
import java.io.OutputStream;

/**
 * An output target provides abilities to write multi-entry data into specific
 * resources.
 * 
 * @author Frank Shaka
 */
public interface IOutputTarget {

    /**
     * Determines whether an entry with the specified name is available or not.
     * 
     * @param entryName
     *            the name of the desired entry
     * @return <code>true</code> if the entry is available, or
     *         <code>false</code> otherwise
     */
    boolean isEntryAvaialble(String entryName);

    /**
     * Opens a new output stream to receive data for the specified entry. The
     * stream should be closed by clients when data writing finishes.
     * 
     * @deprecated <strong>For diagnostic purpose, this method is not
     *             recommended any more. Use {@link #openEntryStream(String)}
     *             instead to let potential I/O errors be thrown.</strong>
     * 
     * @param entryName
     *            the name of the entry
     * @return an output stream for the specified entry, or <code>null</code> if
     *         the specified entry is not available
     */
    @Deprecated
    OutputStream getEntryStream(String entryName);

    /**
     * Opens a new output stream to receive data for the specified entry. The
     * stream should be closed by clients when data writing finishes.
     * 
     * @param entryName
     *            the name of the entry
     * @return an output stream for the specified entry (never <code>null</code>
     *         )
     * @throws IOException
     *             if I/O error occurs
     */
    OutputStream openEntryStream(String entryName) throws IOException;

    /**
     * Sets the modification time of the specific entry.
     * 
     * <p>
     * Note that this method should be called before
     * <code>getEntryStream()</code>, otherwise it may have no effect.
     * </p>
     * 
     * @param time
     *            the new modification time to set
     */
    void setEntryTime(String entryName, long time);

}