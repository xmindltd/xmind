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
import org.xmind.core.IEntryStreamNormalizer;

/**
 * @author Frank Shaka
 * @deprecated Use {@link IEntryStreamNormalizer}
 */
public class Crypto {

    private static ISecurityProvider provider = null;

    public static void setProvider(ISecurityProvider provider) {
        if (Crypto.provider != null)
            return;
        Crypto.provider = provider;
    }

    public static ISecurityProvider getProvider() {
        if (provider == null)
            provider = NullSecurityProvider.getInstance();
        return provider;
    }

    public static OutputStream creatOutputStream(OutputStream output,
            boolean encrypt, IEncryptionData encData, String password)
                    throws CoreException {
        return getProvider().createPasswordProtectedOutputStream(output,
                encrypt, encData, password);
    }

    public static InputStream createInputStream(InputStream input,
            boolean encrypt, IEncryptionData encData, String password)
                    throws CoreException {
        return getProvider().createPasswordProtectedInputStream(input, encrypt,
                encData, password);
    }

    public static void initEncryptionData(IEncryptionData encData) {
        getProvider().initializeEncryptionData(encData);
    }
}
