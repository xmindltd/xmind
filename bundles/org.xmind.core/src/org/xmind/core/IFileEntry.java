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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

/**
 * A file entry represents a file-like data block included in a workbook. A file
 * entry is identified by a <em>relative</em> path string, i.e. not starting
 * with a slash('/'). A file entry should be <em>referenced</em> to keep its
 * existence in the workbook, and non-referenced file entries will be excluded
 * when the workbook is serialized. However, clients typically need <em>not</em>
 * to explicitly call <code>increaseReference()</code> if they use
 * <code>xap:</code> links as topic hyperlinks, notes hyperinks, etc.
 * 
 * @author Frank Shaka
 */
public interface IFileEntry extends IAdaptable, IWorkbookComponent {

    /**
     * Returns the path of this file entry. This path can be used to find this
     * file entry in the owner workbook's {@link IManifest#getFileEntry(String)
     * manifest}. File entries that have the same paths should be regarded as
     * equal instances.
     * 
     * @return the path
     */
    String getPath();

    /**
     * Returns the media type of this file entry. This attribute helps identify
     * what type this file entry's content should be. For entries that is a
     * directory or does not have a obvious media type, an empty string is
     * returned.
     * 
     * @return the media type, or an empty string, never <code>null</code>
     */
    String getMediaType();

    /**
     * TODO java doc
     * 
     * @return
     */
    boolean hasBeenReferred();

    /**
     * TODO java doc
     * 
     * @return
     */
    int getReferenceCount();

    /**
     * TODO java doc
     * 
     */
    void increaseReference();

    /**
     * TODO java doc
     * 
     */
    void decreaseReference();

    /**
     * Checks whether this file entry is available for reading. Calls to
     * {@link #openInputStream()} will fail if this file entry is not available
     * for reading.
     * 
     * @return <code>true</code> if this file entry is available for reading, or
     *         <code>false</code> otherwise
     */
    boolean canRead();

    /**
     * Checks whether this file entry is available for writing. Calls to
     * {@link #openOutputStream()} will fail if this file entry is not available
     * for writing.
     * 
     * @return <code>true</code> if this file entry is available for writing, or
     *         <code>false</code> otherwise
     */
    boolean canWrite();

    /**
     * Opens a new input stream for reading data from this file entry.
     * 
     * @return an input stream for this file entry
     * @throws IOException
     *             if I/O error occurs, this entry is not found in storage, or
     *             this entry is a directory
     */
    InputStream openInputStream() throws IOException;

    /**
     * Opens a new output stream for writing data to this file entry.
     * 
     * @return an output stream for this file entry
     * @throws IOException
     *             if I/O error occurs, or this entry is not available for
     *             writing
     */
    OutputStream openOutputStream() throws IOException;

    /**
     * Checks whether this file entry represents a directory. An entry is a
     * directory if its path has a trailing slash('/'). A directory entry is
     * neither readable nor writable, but has <em>sub-entries</em>.
     * 
     * @return <code>true</code> if this entry is a directory entry, or
     *         <code>false</code> otherwise
     * @see #getSubEntries()
     * @see #iterSubEntries()
     */
    boolean isDirectory();

    /**
     * TODO java doc
     * 
     * @return
     */
    List<IFileEntry> getSubEntries();

    /**
     * TODO java doc
     * 
     * @return
     */
    Iterator<IFileEntry> iterSubEntries();

    /**
     * Returns the time (in milliseconds since midnight, January 1, 1970 UTC)
     * this entry last modified.
     * 
     * @return the time this entry last modified, or <code>-1</code> if this
     *         entry is not accessible or some error occurred while getting the
     *         time
     */
    long getTime();

    /**
     * TODO java doc
     * 
     * @param time
     */
    void setTime(long time);

    /**
     * Returns the size (in bytes) of this entry's content.
     * 
     * @return the size of this entry's content, or <code>-1</code> if this
     *         entry is not accessible or some error occurred while getting the
     *         size
     */
    long getSize();

    /**
     * TODO java doc
     * 
     * @return
     */
    IEncryptionData getEncryptionData();

    /**
     * TODO java doc
     * 
     * @return
     */
    IEncryptionData createEncryptionData();

    /**
     * TODO java doc
     * 
     */
    void deleteEncryptionData();

    /**
     * Opens a new input stream for reading data from this file entry.
     * 
     * @deprecated <strong>For diagnostic purpose, this method is not
     *             recommended any more. Use {@link #canRead()} to test
     *             readability and {@link #openInputStream()} to create a byte
     *             stream for reading.</strong>
     * 
     * @return an input stream for this file entry, or <code>null</code> if the
     *         input stream is not available
     */
    @Deprecated
    InputStream getInputStream();

    /**
     * Opens a new output stream for writing data to this file entry.
     * 
     * @deprecated <strong>For diagnostic purpose, this method is not
     *             recommended any more. Use {@link #canWrite()} to test
     *             writability and {@link #openOutputStream()} to create a byte
     *             stream for writing.</strong>
     * 
     * @return an output stream for this file entry, or <code>null</code> if the
     *         output stream is not available
     */
    @Deprecated
    OutputStream getOutputStream();

}