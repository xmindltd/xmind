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
import java.util.Iterator;
import java.util.List;

/**
 * A manifest manages file-like data blocks, identified as <em>file entries</em>
 * , included in the owner workbook. It provides methods to import external
 * files/streams into the workbook storage, and manages a list of file entries
 * that have at least one <em>reference count</em>, in a way that, when a file
 * entry's reference count is reduced to zero, it is automatically removed from
 * this list, and, when its reference count is increased from zero, it is
 * automatically added to this list.
 * 
 * @author Frank Shaka
 *
 */
public interface IManifest extends IWorkbookComponent, IAdaptable {

    String getPasswordHint();

    void setPasswordHint(String hint);

    /**
     * Lists all existing file entries that has at least one reference.
     * 
     * @return referenced file entries
     */
    List<IFileEntry> getFileEntries();

    /**
     * Returns an iterator to iterate over all existing file entries that has at
     * least one reference.
     * 
     * @return an iterator for referenced file entries
     */
    Iterator<IFileEntry> iterFileEntries();

    /**
     * Returns an iterator to iterate over all existing file entries that has at
     * least one reference <em>and</em> passes the given filter.
     * 
     * @param filter
     *            an object to determine whether a file entry should be included
     *            in the results, or <code>null</code> to accept all
     * @return an iterator for filtered and referenced file entries
     */
    Iterator<IFileEntry> iterFileEntries(IFileEntryFilter filter);

    /**
     * Finds and returns a file entry that has the desired path, no matter it's
     * referenced or not.
     * 
     * @param path
     *            the path of the desired file entry
     * @return a file entry that has the desired path
     */
    IFileEntry getFileEntry(String path);

    /**
     * Creates a new file entry and copies the content of the given stream to
     * that entry. If the source file name is not <code>null</code>, its
     * extension part will be appended to the created entry's file name and used
     * to guess the media type of that entry. The entry's time will be set the
     * time when it's created.
     * 
     * @param stream
     *            the source stream containing entry data
     * @param sourceName
     *            a suggested name, typically the source file's name or path, or
     *            <code>null</code> to indicate no extension part
     * @return a new file entry
     * @throws IOException
     *             if any I/O error occurs
     * @throws IllegalArgumentException
     *             if the source stream is <code>null</code>
     */
    IFileEntry createAttachmentFromStream(InputStream stream, String sourceName)
            throws IOException;

    /**
     * Creates a new file entry and copies the content of the given stream to
     * that entry. If the source file name is not <code>null</code>, its
     * extension part will be appended to the created entry's file name. The
     * entry's media type will be set the given one, and it time will be set the
     * time when it's created.
     * 
     * @param stream
     *            the source stream containing entry data
     * @param sourceName
     *            a suggested name, typically the source file's name or path, or
     *            <code>null</code> to indicate no extension part
     * @param mediaType
     *            a suggested media type of the entry, or <code>null</code> to
     *            let the manifest guess a media type
     * @return a new file entry
     * @throws IOException
     *             if any I/O error occurs
     * @throws IllegalArgumentException
     *             if the source stream is <code>null</code>
     */
    IFileEntry createAttachmentFromStream(InputStream stream, String sourceName,
            String mediaType) throws IOException;

    /**
     * Creates a new file entry and copies the content of the given file to that
     * entry. The source file's extension name will be appended to the created
     * entry's file name and used to guess the media type of that entry. The
     * entry's time will be set to the time when it's created.
     * 
     * <p>
     * This method is simply a convenient way of 1) opening a file input stream
     * using the given file path, 2) calling
     * {@link #createAttachmentFromStream(InputStream, String)} and 3) closing
     * the file input stream.
     * </p>
     * 
     * @param sourcePath
     *            the path of a local file
     * @return a new file entry
     * @throws IOException
     *             if any I/O error occurs
     * @throws IllegalArgumentException
     *             if the source path is <code>null</code>
     */
    IFileEntry createAttachmentFromFilePath(String sourcePath)
            throws IOException;

    /**
     * Creates a new file entry and copies the content of the given file to that
     * entry. The source file's extension name will be appended to the created
     * entry's file name. The entry's media type will be set the given one, and
     * it time will be set to the time when it's created.
     * 
     * <p>
     * This method is simply a convenient way of 1) opening a file input stream
     * using the given file path, 2) calling
     * {@link #createAttachmentFromStream(InputStream, String, String)} and 3)
     * closing the file input stream.
     * </p>
     * 
     * @param sourcePath
     *            the path of a local file
     * @param mediaType
     *            a suggested media type of the entry, or <code>null</code> to
     *            let the manifest guess a media type
     * @return a new file entry
     * @throws IOException
     *             if any I/O error occurs
     * @throws IllegalArgumentException
     *             if the source path is <code>null</code>
     */
    IFileEntry createAttachmentFromFilePath(String sourcePath, String mediaType)
            throws IOException;

    /**
     * Creates a new file entry using the given path as its path.
     * 
     * <p>
     * NOTE: Use this method with CAUTION, as the way of storing internal
     * resources by a workbook may change over time or across implementations.
     * </p>
     * 
     * @param path
     *            an internal path to the desired file entry
     * @return a new file entry, or an existing one if the path already exists
     * @throws IllegalArgumentException
     *             if the path is <code>null</code>
     */
    IFileEntry createFileEntry(String path);

    /**
     * Creates a new file entry using the given path as its path and the given
     * media type as its media type.
     * 
     * <p>
     * NOTE: Use this method with CAUTION, as the way of storing internal
     * resources by a workbook may change over time or across implementations.
     * </p>
     * 
     * @param path
     *            an internal path to the desired file entry
     * @param mediaType
     *            the media type of the new file entry, or <code>null</code> to
     *            indicate no media type
     * @return a new file entry, or an existing one if the path already exists
     * @throws IllegalArgumentException
     *             if the path is <code>null</code>
     */
    IFileEntry createFileEntry(String path, String mediaType);

    /**
     * TODO add java docs
     * 
     * @param sourceEntry
     * @param targetPath
     * @return
     * @throws IOException
     */
    IFileEntry cloneEntry(IFileEntry sourceEntry, String targetPath)
            throws IOException;

    /**
     * TODO add java docs
     * 
     * @param sourceEntry
     * @return
     * @throws IOException
     */
    IFileEntry cloneEntryAsAttachment(IFileEntry sourceEntry)
            throws IOException;

    /**
     * 
     * @param entryPath
     * @return
     */
    IEncryptionData getEncryptionData(String entryPath);

    /**
     * @deprecated No need to delete file entries.
     * @param path
     * @return
     */
    @Deprecated
    boolean deleteFileEntry(String path);

    /**
     * @deprecated Use one of <code>createAttachmentFromXXXX</code> methods.
     * @param sourcePath
     * @return
     */
    @Deprecated
    String makeAttachmentPath(String sourcePath);

    /**
     * @deprecated Use one of <code>createAttachmentFromXXX</code> methods.
     * @param source
     * @param directory
     * @return
     */
    @Deprecated
    String makeAttachmentPath(String source, boolean directory);

}