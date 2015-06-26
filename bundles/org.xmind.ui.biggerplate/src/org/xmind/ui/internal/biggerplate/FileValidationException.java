package org.xmind.ui.internal.biggerplate;

public class FileValidationException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -8068548909942785547L;

    /**
     * 
     */
    public FileValidationException() {
        super();
    }

    /**
     * @param message
     */
    public FileValidationException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public FileValidationException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public FileValidationException(String message, Throwable cause) {
        super(message, cause);
    }

}
