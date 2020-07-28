/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.core.internal.security;

import java.io.InputStream;
import java.io.OutputStream;

import org.xmind.core.CoreException;
import org.xmind.core.IEncryptionData;

/**
 * @author Frank Shaka
 * 
 */
@Deprecated
public class NullSecurityProvider implements ISecurityProvider {

    private static final NullSecurityProvider instance = new NullSecurityProvider();

    private NullSecurityProvider() {
    }

    public InputStream createPasswordProtectedInputStream(InputStream input,
            boolean encrypt, IEncryptionData encData, String password)
                    throws CoreException {
        return input;
    }

    public OutputStream createPasswordProtectedOutputStream(OutputStream output,
            boolean encrypt, IEncryptionData encData, String password)
                    throws CoreException {
        return output;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.internal.security.ISecurityProvider#initEncryptionData
     * (org.xmind.core.IEncryptionData)
     */
    public void initializeEncryptionData(IEncryptionData encryptionData) {
        // do nothing
    }

    /**
     * @return the instance
     */
    public static NullSecurityProvider getInstance() {
        return instance;
    }

}
