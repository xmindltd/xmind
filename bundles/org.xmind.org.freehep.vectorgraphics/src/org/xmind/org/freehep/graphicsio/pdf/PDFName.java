package org.xmind.org.freehep.graphicsio.pdf;

/**
 * Specifies a PDFName object.
 * <p>
 *
 * @author Mark Donszelmann
 * @author Jason Wong
 */
public class PDFName implements PDFConstants {

    private String name;

    PDFName(String name) {
        this.name = name;
    }

    public String toString() {
        return "/" + name; //$NON-NLS-1$
    }
}