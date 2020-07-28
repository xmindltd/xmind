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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.xmind.core.CoreException;
import org.xmind.core.IChecksumStream;

/**
 * @author Frank Shaka
 * 
 */
public class ChecksumInputStream extends FilterInputStream
        implements IChecksumStream {

    private Digest digest;

    private String checksum = null;

    public ChecksumInputStream(InputStream in) throws CoreException {
        super(in);
        this.digest = new MD5Digest();
    }

    public int read() throws IOException {
        int b = super.read();
        digest.update((byte) b);
        return b;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int num = super.read(b, off, len);
        digest.update(b, off, num);
        return num;
    }

    public String getChecksum() {
        if (this.checksum == null) {
            // Generate checksum:
            byte[] checksumBytes = new byte[digest.getDigestSize()];
            digest.doFinal(checksumBytes, 0);
            this.checksum = Base64.byteArrayToBase64(checksumBytes);
        }
        return this.checksum;
    }
}
