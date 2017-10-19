package org.xmind.org.freehep.graphicsio.pdf;

/**
 * This class implements a numbered reference to a PDFObject. Internally the
 * class keeps track of the numbers. The user only sees its logical name. Only
 * generation 0 is used in this PDFWriter, since we do not allow for updates of
 * the PDF file.
 * <p>
 *
 * @author Mark Donszelmann
 * @author Jason Wong
 */
public class PDFRef implements PDFConstants {

    private String name;

    private int objectNumber;

    private int generationNumber;

    PDFRef(String name, int objectNumber, int generationNumber) {
        this.name = name;
        this.objectNumber = objectNumber;
        this.generationNumber = generationNumber;
    }

    public String getName() {
        return name;
    }

    public int getObjectNumber() {
        return objectNumber;
    }

    public int getGenerationNumber() {
        return generationNumber;
    }

    public String toString() {
        return objectNumber + " " + generationNumber + " R"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}