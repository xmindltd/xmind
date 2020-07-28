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
package org.xmind.core.command.binary;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ByteArrayEntry implements IBinaryEntry {

    private byte[] bytes;

    public ByteArrayEntry(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public InputStream openInputStream() throws IOException {
        if (this.bytes == null)
            throw new IOException("Byte array entry already disposed."); //$NON-NLS-1$
        return new ByteArrayInputStream(bytes);
    }

    /**
     * Release byte array.
     */
    public void dispose() {
        this.bytes = null;
    }

    @Override
    public String toString() {
        return "[" + bytes.length + " bytes]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
