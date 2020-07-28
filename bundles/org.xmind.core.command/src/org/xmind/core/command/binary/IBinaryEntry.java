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

import java.io.IOException;
import java.io.InputStream;

/**
 * A binary entry holds a sort of binary contents and provides methods to access
 * these contents and flush caches.
 * 
 * @author Frank Shaka
 */
public interface IBinaryEntry {

    /**
     * A binary entry providing no contents. Clients may use
     * <code>obj == IBinaryEntry.NULL</code> to determine whether the given
     * object equals this binary entry.
     */
    IBinaryEntry NULL = new IBinaryEntry() {

        /**
         * This method is not intended to be called as no contents are available
         * for this binary entry.
         * 
         * @return never returns
         * @throws IOException
         *             whenever this method is invoked
         */
        public InputStream openInputStream() throws IOException {
            throw new IOException(
                    "No input stream available for NULL binary entry."); //$NON-NLS-1$
        }

        /**
         * Do nothing as there is nothing to dispose for this binary entry.
         */
        public void dispose() {
        }

        /**
         * Returns <code>"NULL"</code> for this binary entry.
         */
        public String toString() {
            return "NULL"; //$NON-NLS-1$
        }
    };

    /**
     * Opens an input stream for underlying contents. The returned input stream
     * should be consumed before this binary entry is disposed.
     * 
     * @return an input stream, never <code>null</code>
     * @throws IOException
     *             if this binary entry has been disposed, the underlying
     *             contents can not be found, or any other IO error occurs
     */
    InputStream openInputStream() throws IOException;

    /**
     * Disposes this binary entry. This may flush all caches in this binary
     * entry.
     */
    void dispose();

}
