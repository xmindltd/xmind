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
package org.xmind.ui.io;

import java.io.IOException;

/**
 * 
 * @author frankshaka
 * @deprecated Use {@link java.io.InterruptedIOException}
 */
public class StreamInterruptedException extends IOException {

    /**
     * 
     */
    private static final long serialVersionUID = 6119053622620085971L;

    private Throwable cause;

    public StreamInterruptedException() {
        this(null, null);
    }

    public StreamInterruptedException(String message, Throwable cause) {
        super(message);
        this.cause = cause;
    }

    public StreamInterruptedException(String message) {
        this(message, null);
    }

    public StreamInterruptedException(Throwable cause) {
        this(null, cause);
    }

    public Throwable getCause() {
        return cause;
    }

}