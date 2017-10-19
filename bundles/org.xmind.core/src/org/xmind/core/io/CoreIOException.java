package org.xmind.core.io;

import java.io.IOException;

import org.xmind.core.CoreException;

/**
 * This class simply wraps a {@link CoreException} into an {@link IOException}
 * so that, when performing I/O operations, core exceptions can be successfully
 * thrown and caught.
 * 
 * @author Frank Shaka
 * @since 3.6.2
 */
public class CoreIOException extends IOException {

    /**
     * 
     */
    private static final long serialVersionUID = -5617725226498169561L;

    private CoreException coreException;

    public CoreIOException(CoreException coreException) {
        super(coreException.getMessage(), coreException);
        this.coreException = coreException;
    }

    public CoreException getCoreException() {
        return this.coreException;
    }

    @Override
    public synchronized Throwable getCause() {
        return this.coreException;
    }

}
