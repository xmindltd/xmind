/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
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
/**
 *
 */
package org.xmind.core.net.internal;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.xmind.core.net.http.HttpException;
import org.xmind.core.net.http.HttpRequest;

/**
 * @author Frank Shaka
 *
 */
public class FixedLengthInputStream extends FilterInputStream {

    private final HttpRequest request;

    private final long expectedCount;

    private long actualCount

    ;

    /**
     * @param in
     */
    public FixedLengthInputStream(InputStream in, HttpRequest request,
            long expectedCount) {
        super(in);
        this.request = request;
        this.expectedCount = expectedCount;
        this.actualCount = 0;
    }

    @Override
    public int read() throws IOException {
        if (actualCount >= expectedCount)
            return -1;

        int b = in.read();
        if (b < 0) {
            if (actualCount < expectedCount) {
                throw new HttpException(request, HttpRequest.HTTP_RECEIVING,
                        "Insufficient data received", null); //$NON-NLS-1$
            }
            return b;
        }
        actualCount += 1;
        return b;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.FilterInputStream#read(byte[])
     */
    @Override
    public int read(byte[] b) throws IOException {
        if (actualCount >= expectedCount)
            return -1;

        int num = in.read(b);
        if (num < 0) {
            if (actualCount < expectedCount) {
                throw new HttpException(request, HttpRequest.HTTP_RECEIVING,
                        "Insufficient data received", null); //$NON-NLS-1$
            }
            return num;
        }

        long oldCount = actualCount;
        actualCount = Math.min(actualCount + num, expectedCount);
        return (int) (actualCount - oldCount);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.FilterInputStream#read(byte[], int, int)
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (actualCount >= expectedCount)
            return -1;

        int num = in.read(b, off, len);
        if (num < 0) {
            if (actualCount < expectedCount) {
                throw new HttpException(request, HttpRequest.HTTP_RECEIVING,
                        "Insufficient data received", null); //$NON-NLS-1$
            }
            return num;
        }

        long oldCount = actualCount;
        actualCount = Math.min(actualCount + num, expectedCount);
        return (int) (actualCount - oldCount);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.FilterInputStream#available()
     */
    @Override
    public int available() throws IOException {
        int num = super.available();
        if (num < 0) {
            return num;
        }
        return Math.min(num, (int) (expectedCount - actualCount));
    }

}
