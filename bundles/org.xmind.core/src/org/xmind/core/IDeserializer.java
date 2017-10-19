/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
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
/**
 * 
 */
package org.xmind.core;

import java.io.IOException;
import java.io.InputStream;

import org.xmind.core.io.ByteArrayStorage;
import org.xmind.core.io.IInputSource;
import org.xmind.core.io.IStorage;
import org.xmind.core.util.IProgressReporter;

/**
 * A deserializer is responsible for parsing binary data into a workbook.
 * Workbooks loaded by this deserializer can be later saved via
 * {@link ISerializer} objects of the same format version.
 * 
 * <p>
 * Instances of this interface can be retrieved via
 * <code>Core.getSerializationProvider().newDeserializer()</code>. <b>NOTE</b>
 * that clients are allowed to use one deserializer instance to load
 * <em>one</em> workbook from <em>one</em> location for only <em>once</em>, and
 * repeated usage of a single deserializer instance may result in unexpected
 * behaviors. Also note that all methods of this interface may NOT be
 * thread-safe.
 * </p>
 * 
 * @author Frank Shaka
 * @since 3.6.50
 */
public interface IDeserializer extends ISerializingBase {

    IManifest getManifest();

    void deserializeManifest(IProgressReporter reporter)
            throws IOException, CoreException, IllegalStateException;

    /**
     * Returns the deserialized workbook.
     * 
     * @return the deserialized workbook, or <code>null</code> if no workbook is
     *         successfully deserialized
     */
    IWorkbook getWorkbook();

    /**
     * Deserializes binary data from the given input source (set via
     * {@link #setInputSource(IInputSource)},
     * {@link #setInputStream(InputStream)}, or
     * {@link #setWorkbookStorageAsInputSource()}) into a workbook, storing its
     * file entries in the specified storage (set via
     * {@link #setWorkbookStorage(IStorage)} or, if not set, a new
     * {@link ByteArrayStorage} created automatically). The deserialized
     * workbook can be retrieved via {@link #getWorkbook()} after the
     * deserialization is done. The progress during the deserialization is
     * reported via the given progress reporter.
     * 
     * @param reporter
     *            an {@link IProgressReporter} object to receive progress
     *            information and indicate user cancellation requests, or
     *            <code>null</code> if the progress is not significant and
     *            cancellation is not required
     * @throws IOException
     *             if any I/O error occurs
     * @throws CoreException
     *             <ul>
     *             <li><code>Core.ERROR_CANCELLATION</code> - if the operation
     *             is canceled</li>
     *             <li><code>Core.ERROR_WRONG_PASSWORD</code> - if the workbook
     *             is encrypted and the decryption operation failed</li>
     *             <li>(TODO add more error codes)</li>
     *             </ul>
     * @throws IllegalStateException
     *             if either input source or workbook storage is not set
     *             properly
     */
    void deserialize(IProgressReporter reporter)
            throws IOException, CoreException, IllegalStateException;

    /**
     * Sets the storage with read-write random-access abilities to store
     * contents of file entries for the deserialized workbook. This storage will
     * be kept and held by the deserialized workbook after deserialization is
     * over. If no storage is set, a new empty in-memory
     * {@link ByteArrayStorage} will be automatically created and used.
     * 
     * @param storage
     *            the {@link IStorage} to use
     * @throws IllegalArgumentException
     *             if the storage is <code>null</code>
     */
    void setWorkbookStorage(IStorage storage);

    /**
     * Returns the storage set via {@link #setWorkbookStorage(IStorage)}.
     * 
     * @return the storage set previously, or <code>null</code> if not set yet
     */
    IStorage getWorkbookStorage();

    /**
     * Sets the input source from which the content of the deserialized workbook
     * will be loaded.
     * 
     * @param source
     *            the {@link IInputSource}
     * @throws IllegalArgumentException
     *             if the source is <code>null</code>
     */
    void setInputSource(IInputSource source);

    /**
     * Sets the input stream as the input source from which the content of the
     * deserialized workbook will be loaded.
     * 
     * <p>
     * NOTE: The deserializer may not close the given stream after usage. The
     * client should close it on its own.
     * </p>
     * 
     * @param stream
     *            the source {@link InputStream}
     * @throws IllegalArgumentException
     *             if the stream is <code>null</code>
     */
    void setInputStream(InputStream stream);

    /**
     * Sets the output target to be the input source of the storage set via
     * {@link #setWorkbookStorage(IStorage)}. Calling this method will remove
     * any input source set previously.
     * 
     * <p>
     * This special setting indicates some special behaviors, e.g. no file
     * entries will be copied to save time.
     * </p>
     */
    void setWorkbookStorageAsInputSource();

    /**
     * Tests whether an input source is properly set via
     * {@link #setInputSource(IInputSource)},
     * {@link #setInputStream(InputStream)} or
     * {@link #setWorkbookStorageAsInputSource()}.
     * 
     * @return <code>true</code> if the input source is set, or
     *         <code>false</code> otherwise
     */
    boolean hasInputSource();

}
