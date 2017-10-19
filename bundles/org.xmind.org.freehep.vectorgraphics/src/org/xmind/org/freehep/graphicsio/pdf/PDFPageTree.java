package org.xmind.org.freehep.graphicsio.pdf;

import java.io.IOException;
import java.util.Vector;

/**
 * Implements the Page Tree Node (see Table 3.16).
 * <p>
 *
 * @author Mark Donszelmann
 * @version $Id: PDFPageTree.java 8584 2006-08-10 23:06:37Z duns $
 */

public class PDFPageTree extends PDFPageBase {

    Vector<PDFRef> pages = new Vector<PDFRef>();

    PDFPageTree(PDF pdf, PDFByteWriter writer, PDFObject object, PDFRef parent)
            throws IOException {
        super(pdf, writer, object, parent);
        entry("Type", pdf.name("Pages")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void addPage(String name) {
        pages.add(pdf.ref(name));
    }

    void close() throws IOException {
        Object[] kids = new Object[pages.size()];
        pages.copyInto(kids);
        entry("Kids", kids); //$NON-NLS-1$
        entry("Count", kids.length); //$NON-NLS-1$
        super.close();
    }
}
