package org.xmind.core.io;

import java.io.IOException;

/**
 * 
 * @author Frank Shaka
 * @since 3.6.50
 */
public class InvalidChecksumException extends IOException {

    /**
     * 
     */
    private static final long serialVersionUID = -7976264532100430240L;

    private String expectedValue;

    private String actualValue;

    public InvalidChecksumException(String expectedValue, String actualValue) {
        super("Invalid checksum, expected value is '" + expectedValue //$NON-NLS-1$
                + "', actual value is '" + actualValue + "'"); //$NON-NLS-1$ //$NON-NLS-2$
        this.expectedValue = expectedValue;
        this.actualValue = actualValue;
    }

    public String getExpectedValue() {
        return expectedValue;
    }

    public String getActualValue() {
        return actualValue;
    }

}
