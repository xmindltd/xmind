package org.xmind.core.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.xmind.core.IChecksumStream;

/**
 * 
 * @author Frank Shaka
 * @since 3.6.50
 */
public class ChecksumVerifiedInputStream extends FilterInputStream
        implements IChecksumStream {

    private String expectedChecksum;

    public ChecksumVerifiedInputStream(InputStream in,
            String expectedChecksum) {
        super(in);
        this.expectedChecksum = expectedChecksum;
    }

    @Override
    public int read() throws IOException {
        int n = super.read();
        if (n < 0) {
            verifyChecksum();
        }
        return n;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int n = super.read(b);
        if (n < 0) {
            verifyChecksum();
        }
        return n;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int n = super.read(b, off, len);
        if (n < 0) {
            verifyChecksum();
        }
        return n;
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            verifyChecksum();
        }
    }

    private void verifyChecksum() throws InvalidChecksumException {
        if (!(in instanceof IChecksumStream))
            return;

        String checksum = ((IChecksumStream) in).getChecksum();
        if (checksum == expectedChecksum
                || (checksum != null && checksum.equals(expectedChecksum)))
            return;

        throw new InvalidChecksumException(expectedChecksum, checksum);
    }

    public String getChecksum() {
        if (in instanceof IChecksumStream)
            return ((IChecksumStream) in).getChecksum();
        return null;
    }

}
