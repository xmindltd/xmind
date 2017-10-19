package org.xmind.core.io;

import java.io.IOException;

/**
 * @deprecated Use {@link java.io.Closeable} instead.
 * 
 * @author Frank Shaka
 */
@Deprecated
public interface ICloseableOutputTarget extends IOutputTarget {

    /**
     * Close this output target and flush all contents to the target.
     * 
     * @deprecated
     */
    @Deprecated
    void close() throws IOException;

}
