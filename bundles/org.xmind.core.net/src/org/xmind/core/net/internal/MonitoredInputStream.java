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
package org.xmind.core.net.internal;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * 
 * @author Frank Shaka
 * @since 3.6.50
 */
public class MonitoredInputStream extends InputStream {

    private InputStream realStream;

    private IProgressMonitor monitor;

    public MonitoredInputStream(InputStream realStream,
            IProgressMonitor monitor) {
        this.realStream = realStream;
        this.monitor = monitor;
    }

    private void closeRealStream() {
        try {
            realStream.close();
        } catch (IOException ignore) {
        }
    }

    public int read() throws IOException {
        if (monitor.isCanceled()) {
            closeRealStream();
            throw new OperationCanceledException();
        }
        return realStream.read();
    }

    public int available() throws IOException {
        return realStream.available();
    }

    public void close() throws IOException {
        realStream.close();
    }

    public void mark(int readlimit) {
        realStream.mark(readlimit);
    }

    public boolean markSupported() {
        return realStream.markSupported();
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (monitor.isCanceled()) {
            closeRealStream();
            throw new OperationCanceledException();
        }
        return realStream.read(b, off, len);
    }

    public int read(byte[] b) throws IOException {
        if (monitor.isCanceled()) {
            closeRealStream();
            throw new OperationCanceledException();
        }
        return realStream.read(b);
    }

    public void reset() throws IOException {
        if (monitor.isCanceled()) {
            closeRealStream();
            throw new OperationCanceledException();
        }
        realStream.reset();
    }

    public long skip(long n) throws IOException {
        if (monitor.isCanceled()) {
            closeRealStream();
            throw new OperationCanceledException();
        }
        return realStream.skip(n);
    }

}