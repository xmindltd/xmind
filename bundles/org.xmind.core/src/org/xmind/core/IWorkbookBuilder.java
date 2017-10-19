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
import java.io.IOException;
import java.io.InputStream;

import org.xmind.core.io.IInputSource;
import org.xmind.core.io.IStorage;

/**
 * A workbook builder is responsible for creating/loading/saving workbook
 * instances.
 * 
 * <p>
 * Every workbook instance created by this workbook builder holds an
 * {@link IStorage} object to store temporary data during creation/loading and
 * afterwards. This storage object can be retrieved using
 * <code>workbook.getAdapter(IStorage.class)</code>.
 * </p>
 * 
 * @author Frank Shaka
 * @since 3.0
 */
public interface IWorkbookBuilder {

    /**
     * Creates a new <em>empty</em> workbook instance with a new in-memory
     * storage. Equivalent to
     * <code>createWorkbook(new ByteArrayStorage())</code>.
     * 
     * @return a new empty workbook instance
     */
    IWorkbook createWorkbook();

    /**
     * Creates a new <em>empty</em> workbook instance with the specified
     * storage. The storage will be cleared.
     * 
     * <p>
     * The storage can be retrieved using
     * <code>workbook.getAdapter(IStorage.class)</code>.
     * </p>
     * 
     * @param storage
     *            used by the created workbook to store temporary data after
     *            creation
     * @return a new empty workbook instance
     */
    IWorkbook createWorkbook(IStorage storage);

    /**
     * Creates a new instance of {@link ISerializer} used for saving a workbook
     * to a location.
     *
     * @return a new serializer instance (never <code>null</code>)
     */
    ISerializer newSerializer();

    /**
     * Creates a new instance of {@link IDeserializer} used for loading a
     * workbook from a location.
     * 
     * @return a new deserializer instance (never <code>null</code>)
     */
    IDeserializer newDeserializer();

    /**
     * Sets the default name and version of all workbooks loaded/saved by this
     * workbook builder, to be stored in their meta info.
     * 
     * @param name
     *            the name of this builder
     * @param version
     *            the version of this builder
     */
    void setCreator(String name, String version);

    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////
    ////
    //// DEPRECATED METHODS
    ////
    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////

    /**
     * Creates a new workbook instance and loads its content from the specified
     * local file path, into a new in-memory storage. The default encryption
     * handler of this workbook builder is used to decrypt any
     * password-protected content during the process.
     * 
     * @param path
     *            the absolute path of the local file from which the created
     *            workbook's content is loaded
     * @return a new workbook instance with content
     * @throws IOException
     *             if any I/O error occurs
     * @throws CoreException
     *             if any syntax error occurs, or the operation is canceled
     * @deprecated See {@link org.xmind.core.IDeserializer}
     */
    @Deprecated
    IWorkbook loadFromPath(String path) throws IOException, CoreException;

    /**
     * Creates a workbook instance and loads its content from the specified
     * local file path, into a new in-memory storage. The specified encryption
     * handler is used to decrypt any password-protected content during the
     * process.
     * 
     * @param file
     *            the absolute path of the local file from which the created
     *            workbook's content is loaded
     * @param encryptionHandler
     *            providing decryption information
     * @return a new workbook instance with content
     * @throws IOException
     *             if any I/O error occurs
     * @throws CoreException
     *             if any syntax error occurs, or the operation is canceled
     * @deprecated See {@link org.xmind.core.IDeserializer}
     */
    @Deprecated
    IWorkbook loadFromPath(String path, IEncryptionHandler encryptionHandler)
            throws IOException, CoreException;

    /**
     * Creates a workbook instance and loads its content from the specified
     * local file path, into the specified storage. The specified encryption
     * handler is used to decrypt any password-protected content during the
     * process. The storage will be cleared.
     * 
     * <p>
     * The storage can be retrieved using
     * <code>workbook.getAdapter(IStorage.class)</code>.
     * </p>
     * 
     * @param file
     *            the absolute path of the local file from which the created
     *            workbok's content is loaded
     * @param storage
     *            used by the created workbook to store temporary data after
     *            loading
     * @param encryptionHandler
     *            providing decryption information
     * @return a new workbook instance with content
     * @throws IOException
     *             if any I/O error occurs
     * @throws CoreException
     *             if any syntax error occurs, or the operation is canceled
     * @deprecated See {@link org.xmind.core.IDeserializer}
     */
    @Deprecated
    IWorkbook loadFromPath(String path, IStorage storage,
            IEncryptionHandler encryptionHandler)
            throws IOException, CoreException;

    /**
     * Creates a workbook instance and loads its content from the specified
     * local file, into a new in-memory storage. The default encryption handler
     * of this workbook builder is used to decrypt any password-protected
     * content during the process.
     * 
     * @param file
     *            the local file from which the created workbook's content is
     *            loaded
     * @return a new workbook instance with content
     * @throws IOException
     *             if any I/O error occurs
     * @throws CoreException
     *             if any syntax error occurs, or the operation is canceled
     * @deprecated See {@link org.xmind.core.IDeserializer}
     */
    @Deprecated
    IWorkbook loadFromFile(File file) throws IOException, CoreException;

    /**
     * Creates a workbook instance and loads its content from the specified
     * local file, into a new in-memory storage. The specified encryption
     * handler is used to decrypt any password-protected content during the
     * process.
     * 
     * @param file
     *            the local file from which the created workbook's content is
     *            loaded
     * @param encryptionHandler
     *            providing decryption information
     * @return a new workbook instance with content
     * @throws IOException
     *             if any I/O error occurs
     * @throws CoreException
     *             if any syntax error occurs, or the operation is canceled
     * @deprecated See {@link org.xmind.core.IDeserializer}
     */
    @Deprecated
    IWorkbook loadFromFile(File file, IEncryptionHandler encryptionHandler)
            throws IOException, CoreException;

    /**
     * Creates a workbook instance and loads its content from the specified
     * local file, into the specified storage. The specified encryption handler
     * is used to decrypt any password-protected content during the process. The
     * storage will be cleared.
     * 
     * <p>
     * The storage can be retrieved using
     * <code>workbook.getAdapter(IStorage.class)</code>.
     * </p>
     * 
     * @param file
     *            the local file from which the created workbok's content is
     *            loaded
     * @param storage
     *            used by the created workbook to store temporary data after
     *            loading
     * @param encryptionHandler
     *            providing decryption information
     * @return a new workbook instance with content
     * @throws IOException
     *             if any I/O error occurs
     * @throws CoreException
     *             if any syntax error occurs, or the operation is canceled
     * @deprecated See {@link org.xmind.core.IDeserializer}
     */
    @Deprecated
    IWorkbook loadFromFile(File file, IStorage storage,
            IEncryptionHandler encryptionHandler)
            throws IOException, CoreException;

    /**
     * Creates a workbook instance and loads its content from the specified
     * input stream, into a new in-memory storage. The default encryption
     * handler of this workbook builder is used to decrypt any
     * password-protected content during the process.
     * 
     * <p>
     * <b>NOTE</b> that the specified input stream will be closed after this
     * method returns.
     * </p>
     * 
     * @param in
     *            the input stream from which the created workbok's content is
     *            loaded
     * @return a new workbook instance with content
     * @throws IOException
     *             if any I/O error occurs
     * @throws CoreException
     *             if any syntax error occurs, or the operation is canceled
     * @deprecated See {@link org.xmind.core.IDeserializer}
     */
    @Deprecated
    IWorkbook loadFromStream(InputStream in) throws IOException, CoreException;

    /**
     * Creates a workbook instance and loads its content from the specified
     * input stream, into the specified storage. The default encryption handler
     * of this workbook builder is used to decrypt any password-protected
     * content during the process. The storage will be cleared.
     * 
     * <p>
     * The storage can be retrieved using
     * <code>workbook.getAdapter(IStorage.class)</code>.
     * </p>
     * 
     * <p>
     * <b>NOTE</b> that the specified input stream will be closed after this
     * method returns.
     * </p>
     * 
     * @param in
     *            the input stream from which the created workbok's content is
     *            loaded
     * @param storage
     *            used by the created workbook to store temporary data after
     *            loading
     * @return a new workbook instance with content
     * @throws IOException
     *             if any I/O error occurs
     * @throws CoreException
     *             if any syntax error occurs, or the operation is canceled
     * @deprecated See {@link org.xmind.core.IDeserializer}
     */
    @Deprecated
    IWorkbook loadFromStream(InputStream in, IStorage storage)
            throws IOException, CoreException;

    /**
     * Creates a workbook instance and loads its content from the specified
     * input stream, into the specified storage. The specified encryption
     * handler is used to decrypt any password-protected content during the
     * process. The storage will be cleared.
     * 
     * <p>
     * The storage can be retrieved using
     * <code>workbook.getAdapter(IStorage.class)</code>.
     * </p>
     * 
     * <p>
     * <b>NOTE</b> that the specified input stream will be closed after this
     * method returns.
     * </p>
     * 
     * @param in
     *            the input stream from which the created workbok's content is
     *            loaded
     * @param storage
     *            used by the created workbook to store temporary data after
     *            loading
     * @param encryptionHandler
     *            providing decryption information
     * @return a new workbook instance with content
     * @throws IOException
     *             if any I/O error occurs
     * @throws CoreException
     *             if any syntax error occurs, or the operation is canceled
     * @deprecated See {@link org.xmind.core.IDeserializer}
     */
    @Deprecated
    IWorkbook loadFromStream(InputStream in, IStorage storage,
            IEncryptionHandler encryptionHandler)
            throws IOException, CoreException;

    /**
     * Creates a workbook instance and loads its content from the specified
     * input source, into a new in-memory storage. The default encryption
     * handler of this workbook builder is used to decrypt any
     * password-protected content during the process.
     * 
     * @param source
     *            the input source from which the created workbok's content is
     *            loaded
     * @return a new workbook instance with content
     * @throws IOException
     *             if any I/O error occurs
     * @throws CoreException
     *             if any syntax error occurs, or the operation is canceled
     * @deprecated See {@link org.xmind.core.IDeserializer}
     */
    @Deprecated
    IWorkbook loadFromInputSource(IInputSource source)
            throws IOException, CoreException;

    /**
     * Creates a workbook instance and loads its content from the specified
     * input source, into a new in-memory storage. The specified encryption
     * handler is used to decrypt any password-protected content during the
     * process.
     * 
     * @param source
     *            the input source from which the created workbok's content is
     *            loaded
     * @param encryptionHandler
     *            providing decryption information
     * @return a new workbook instance with content
     * @throws IOException
     *             if any I/O error occurs
     * @throws CoreException
     *             if any syntax error occurs, or the operation is canceled
     * @deprecated See {@link org.xmind.core.IDeserializer}
     */
    @Deprecated
    IWorkbook loadFromInputSource(IInputSource source,
            IEncryptionHandler encryptionHandler)
            throws IOException, CoreException;

    /**
     * Creates a workbook instance and loads its content from the specified
     * input source, into the specified storage. The specified encryption
     * handler is used to decrypt any password-protected content during the
     * process. The storage will be cleared.
     * 
     * <p>
     * The storage can be retrieved using
     * <code>workbook.getAdapter(IStorage.class)</code>.
     * </p>
     * 
     * @param source
     *            the input source from which the created workbok's content is
     *            loaded
     * @param storage
     *            used by the created workbook to store temporary data after
     *            loading
     * @param encryptionHandler
     *            providing decryption information
     * @return a new workbook instance with content
     * @throws IOException
     *             if any I/O error occurs
     * @throws CoreException
     *             if any syntax error occurs, or the operation is canceled
     * @deprecated See {@link org.xmind.core.IDeserializer}
     */
    @Deprecated
    IWorkbook loadFromInputSource(IInputSource source, IStorage storage,
            IEncryptionHandler encryptionHandler)
            throws IOException, CoreException;

    /**
     * Creates a new workbook instance and loads its content <em>directly</em>
     * from the specified storage. The default encryption handler of this
     * workbook builder is used to decrypt any password-protected content during
     * the process.
     * 
     * <p>
     * The storage will <b>NOT</b> be cleared so that all existing data in it
     * will be preserved. If the storage is empty or corrupted, loading errors
     * may occur.
     * </p>
     * 
     * <p>
     * The storage can be retrieved using
     * <code>workbook.getAdapter(IStorage.class)</code>.
     * </p>
     * 
     * @param storage
     *            used by the worbook to load initial content and store
     *            temporary data after loading
     * @return a new workbook instance with content
     * @throws IOException
     *             if any I/O error occurs
     * @throws CoreException
     *             if any syntax error occurs
     * @deprecated See {@link org.xmind.core.IDeserializer}
     */
    @Deprecated
    IWorkbook loadFromStorage(IStorage storage)
            throws IOException, CoreException;

    /**
     * Creates a new workbook instance and loads its content <em>directly</em>
     * from the specified storage. The specified encryption handler is used to
     * decrypt any password-protected content during the process.
     * 
     * <p>
     * The storage will <b>NOT</b> be cleared so that all existing data in it
     * will be preserved. If the storage is empty or corrupted, loading errors
     * may occur.
     * </p>
     * 
     * <p>
     * The storage can be retrieved using
     * <code>workbook.getAdapter(IStorage.class)</code>.
     * </p>
     * 
     * @param storage
     *            used by the worbook to load initial content and store
     *            temporary data after loading
     * @param encryptionHandler
     *            providing decryption information
     * @return a new workbook instance with content
     * @throws IOException
     *             if any I/O error occurs
     * @throws CoreException
     *             if any syntax error occurs
     * @deprecated See {@link org.xmind.core.IDeserializer}
     */
    @Deprecated
    IWorkbook loadFromStorage(IStorage storage,
            IEncryptionHandler encryptionHandler)
            throws IOException, CoreException;

    /**
     * Sets the default encryption handler to use for loadFromXXX methods.
     * 
     * @param encryptionHandler
     *            the new encryption handler to use
     * @deprecated See {@link org.xmind.core.IDeserializer}
     */
    @Deprecated
    void setDefaultEncryptionHandler(IEncryptionHandler encryptionHandler);

    /**
     * @deprecated Do NOT let workbook know about its file path.
     */
    @Deprecated
    IWorkbook createWorkbook(String targetPath);

    /**
     * @deprecated See {@link org.xmind.core.IDeserializer}
     */
    @Deprecated
    IWorkbook createWorkbookOnTemp(String tempLocation);

    /**
     * @deprecated See {@link org.xmind.core.IDeserializer}
     */
    @Deprecated
    IWorkbook loadFromStream(InputStream in, String tempLocation)
            throws IOException, CoreException;

    /**
     * @deprecated See {@link org.xmind.core.IDeserializer}
     */
    @Deprecated
    IWorkbook loadFromStream(InputStream in, String tempLocation,
            IEncryptionHandler encryptionHandler)
            throws IOException, CoreException;

    /**
     * @deprecated See {@link org.xmind.core.IDeserializer}
     */
    @Deprecated
    IWorkbook loadFromTempLocation(String tempLocation)
            throws IOException, CoreException;

}