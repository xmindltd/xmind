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

/**
 * @author frankshaka
 * 
 */
public interface IEncryptionData extends IAdaptable {

    /**
     * Use SEP to separate multiple keys.
     * <p>
     * Example:
     * 
     * <pre>
     * encryption data: checksum=&quot;acbdefghij==&quot; checksum-type=&quot;MD5&quot;
     *     +-- algorithm: algorithm-name=&quot;PBEWithMD5AndDES/CBC/Padded&quot;  
     *     +-- key-derivation: key-derivation-name=&quot;PKCS12&quot; salt=&quot;12345678==&quot; iteration-count=&quot;1024&quot;
     * </pre>
     * 
     * encryptionData.getAttribute("checksum") = "acbdefghij=="<br>
     * encryptionData.getAttribute("checksum-type") = "MD5"<br>
     * encryptionData.getAttribute("algorithm", "algorithm-name") =
     * "PBEWithMD5AndDES/CBC/Padded"<br>
     * encryptionData.getAttribute("key-derivation", "key-derivation-name") =
     * "PKCS12"<br>
     * encryptionData.getAttribute("key-derivation", "salt") = "12345678=="<br>
     * encryptionData.getAttribute("key-derivation", "iteration-count") = "1024"
     * </p>
     * 
     * @param keyPath
     * @return
     */
    String getAttribute(String... keyPath);

    /**
     * 
     * @param defaultValue
     * @param keyPath
     * @return
     */
    int getIntAttribute(int defaultValue, String... keyPath);

    /**
     * 
     * @param value
     * @param keyPath
     */
    void setAttribute(String value, String... keyPath);

    /**
     * 
     * @return
     */
    String getChecksum();

    /**
     * 
     * @param checksum
     */
    void setChecksum(String checksum);

    /**
     * 
     * @return
     */
    String getChecksumType();

    /**
     * 
     * @param checksumType
     */
    void setChecksumType(String checksumType);

    /**
     * 
     * @return
     */
    IFileEntry getFileEntry();

}
