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
package org.xmind.core;

import java.io.IOException;
import java.io.OutputStream;

import org.xmind.core.io.IOutputTarget;
import org.xmind.core.util.IProgressReporter;

/**
 * A serializer is responsible for storing the content of a workbook as binary
 * data to external resources. Workbooks saved by this serializer can be later
 * loaded via {@link IDeserializer} objects of the same format version.
 *
 * <p>
 * Instances of this interface can be retrieved via
 * <code>Core.getSerializationProvider().newSerializer()</code>. <b>NOTE</b>
 * that clients are allowed to use one serializer instance to save <em>one</em>
 * workbook to <em>one</em> location for only <em>once</em>, and repeated usage
 * of a single serializer instance may result in unexpected behaviors. Also note
 * that all methods of this interface may NOT be thread-safe.
 * </p>
 *
 * @author Frank Shaka
 * @since 3.6.50
 */
public interface ISerializer extends ISerializingBase {

    /**
     * Serializes the workbook (set via {@link #setWorkbook(IWorkbook)}),
     * storing the serialized binary data into the output target (set via
     * {@link #setOutputTarget(IOutputTarget)},
     * {@link #setOutputStream(OutputStream)} or
     * {@link #setWorkbookStorageAsOutputTarget()}). The progress during the
     * serialization is reported via the given progress reporter.
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
     *             if either workbook or output target is not set properly
     */
    void serialize(IProgressReporter reporter)
            throws IOException, CoreException, IllegalStateException;

    /**
     * Sets the workbook to be serialized.
     * 
     * @param workbook
     *            the {@link IWorkbook} to be serialized
     * @throws IllegalArgumentException
     *             if the workbook is <code>null</code> or can not be serialized
     *             by this serializer
     */
    void setWorkbook(IWorkbook workbook);

    /**
     * Returns the workbook set via {@link #setWorkbook(IWorkbook)}.
     * 
     * @return the {@link IWorkbook} to be serialized, or <code>null</code> if
     *         not set yet
     */
    IWorkbook getWorkbook();

    /**
     * Sets the output target to which the content of the serialized workbook
     * will be stored.
     * 
     * @param target
     *            the {@link IOutputTarget}
     * @throws IllegalArgumentException
     *             if the target is <code>null</code>
     */
    void setOutputTarget(IOutputTarget target);

    /**
     * Sets the output stream as the output target to which the content of the
     * serialized workbook will be stored.
     * 
     * <p>
     * NOTE: The serializer may not close the given stream after usage. The
     * client should close it on its own.
     * </p>
     * 
     * @param stream
     *            the target {@link OutputStream}
     * @throws IllegalArgumentException
     *             if the stream is <code>null</code>
     */
    void setOutputStream(OutputStream stream);

    /**
     * Sets the output target to be the current file entry storage of the
     * workbook to be serialized. Calling this method will remove any output
     * target set previously.
     * 
     * <p>
     * This special setting indicates some special behaviors, e.g. skipping some
     * kinds of file entries to save time if the normalizer is not going to
     * change, or, if it <em>will</em> change, re-encoding/re-encrypting all
     * existing file entries using the new normalizer, who will then be kept and
     * held by the serialized workbook to perform normalizing tasks afterwards.
     * </p>
     */
    void setWorkbookStorageAsOutputTarget();

    /**
     * Tests whether an output target is properly set via
     * {@link #setOutputTarget(IOutputTarget)},
     * {@link #setOutputStream(OutputStream)} or
     * {@link #setWorkbookStorageAsOutputTarget()}.
     * 
     * @return <code>true</code> if the output target is set, or
     *         <code>false</code> otherwise
     */
    boolean hasOutputTarget();

    /**
     * Sets the set of entry paths whose encryption should be ignored during the
     * serializing process.
     *
     * @param entryPaths
     *            a set of entry paths, or <code>null</code> to use default
     *            settings
     */
    void setEncryptionIgnoredEntries(String[] entryPaths);

    /**
     * Returns a set of entry paths whose encryption will be ignored during the
     * serializing process.
     *
     * @return a set of entry paths, or <code>null</code> if not set or set to
     *         <code>null</code>
     */
    String[] getEncryptionIgnoredEntries();

}
