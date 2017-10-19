// Copyright 2001, FreeHEP.
package org.xmind.org.freehep.util.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * The CountedByteOutputStream counts the number of bytes written.
 *
 * @author Mark Donszelmann
 * @version $Id: src/main/java/org/freehep/util/io/CountedByteOutputStream.java
 *          96b41b903496 2005/11/21 19:50:18 duns $
 */
public class CountedByteOutputStream extends FilterOutputStream {

    private int count;

    /**
     * Creates a Counted Bytes output stream from the given stream.
     *
     * @param out
     *            stream to write to
     */
    public CountedByteOutputStream(OutputStream out) {
        super(out);
        count = 0;
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        count++;
    }

    @Override
    public void write(byte[] b) throws IOException {
        out.write(b);
        count += b.length;
    }

    @Override
    public void write(byte[] b, int offset, int len) throws IOException {
        out.write(b, offset, len);
        count += len;
    }

    /**
     * @return number of bytes written.
     */
    public int getCount() {
        return count;
    }
}
