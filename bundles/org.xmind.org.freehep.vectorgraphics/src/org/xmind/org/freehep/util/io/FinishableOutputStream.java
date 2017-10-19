// Copyright 2001, FreeHEP.
package org.xmind.org.freehep.util.io;

import java.io.IOException;

/**
 * The FinishableOutputStream allows a generic way of calling finish on an
 * output stream without closing it.
 *
 * @author Mark Donszelmann
 * @version $Id: src/main/java/org/freehep/util/io/FinishableOutputStream.java
 *          96b41b903496 2005/11/21 19:50:18 duns $
 */
public interface FinishableOutputStream {

    /**
     * Finishes the current outputstream (compresses, flushes, caluclates CRC)
     * and writes whatever is left in the buffers, but does not close the
     * stream.
     *
     * @throws IOException
     *             if write fails
     */
    public void finish() throws IOException;
}
