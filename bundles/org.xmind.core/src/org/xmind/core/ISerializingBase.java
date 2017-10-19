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

/**
 * The base interface for {@link ISerializer} and {@link IDeserializer} that
 * provides some common facilities useful both for serialization and
 * deserialization.
 * 
 * @author Frank Shaka
 * @since 3.6.50
 */
public interface ISerializingBase {

    /**
     * Sets the name of the client program whom the end user asks to perform the
     * serializing/deserializing task.
     *
     * @param name
     *            the name to set
     */
    void setCreatorName(String name);

    /**
     * Returns the name of the client program whom the end user asks to perform
     * the serializing/deserializing task.
     *
     * @return the creator name
     */
    String getCreatorName();

    /**
     * Sets the version of the Creator representing the client program whom the
     * end user asks to perform the serializing/deserializing task.
     *
     * @param version
     *            the version to set
     */
    void setCreatorVersion(String version);

    /**
     * Returns the version of the client program whom the end user asks to
     * perform the serializing/deserializing task.
     *
     * @return the creator version
     */
    String getCreatorVersion();

    /**
     * Sets the file entry stream normalizer to perform encoding/encryption or
     * decoding/decryption operations during the serializing/deserializing
     * process.
     * 
     * <p>
     * Note that the given normalizer should honor the <code>equals()</code>
     * method to determine whether two normalizers provides the same abilities.
     * A workbook tends to keep and hold the new normalizer that is different
     * than its old one.
     * </p>
     *
     * @param normalizer
     *            the {@link IEntryStreamNormalizer} to use, or
     *            <code>null</code> to indicate that (for serialization) the one
     *            already held by the workbook should be used or (for
     *            deserialization) no decoding/decryption should be performed
     */
    void setEntryStreamNormalizer(IEntryStreamNormalizer normalizer);

    /**
     * Returns the entry stream normalizer to perform encryption/decryption
     * operations.
     *
     * @return the {@link IEntryStreamNormalizer} to use, or <code>null</code>
     *         if not set
     */
    IEntryStreamNormalizer getEntryStreamNormalizer();

}
