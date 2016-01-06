package org.xmind.core.net.internal;

public class UnsupportedEncodingError extends Error {

    /**
     * 
     */
    private static final long serialVersionUID = 4244631665346326519L;

    public UnsupportedEncodingError(String encoding, Throwable cause) {
        super(encoding, cause);
    }

    public UnsupportedEncodingError(String encoding) {
        super(encoding);
    }

}
