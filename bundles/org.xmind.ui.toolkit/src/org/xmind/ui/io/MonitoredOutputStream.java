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
import java.io.InterruptedIOException;
import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;

public class MonitoredOutputStream extends OutputStream {

    private OutputStream realStream;

    private IProgressMonitor monitor;

    public MonitoredOutputStream(OutputStream realStream,
            IProgressMonitor monitor) {
        super();
        this.realStream = realStream;
        this.monitor = monitor;
    }

    private void closeRealStream() {
        try {
            realStream.close();
        } catch (IOException ignore) {
        }
    }

    public void close() throws IOException {
        realStream.close();
    }

    public void flush() throws IOException {
        if (monitor.isCanceled()) {
            closeRealStream();
            throw new InterruptedIOException();
        }
        realStream.flush();
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (monitor.isCanceled()) {
            closeRealStream();
            throw new InterruptedIOException();
        }
        realStream.write(b, off, len);
    }

    public void write(byte[] b) throws IOException {
        if (monitor.isCanceled()) {
            closeRealStream();
            throw new InterruptedIOException();
        }
        realStream.write(b);
    }

    public void write(int b) throws IOException {
        if (monitor.isCanceled()) {
            closeRealStream();
            throw new InterruptedIOException();
        }
        realStream.write(b);
    }

}