package org.xmind.org.freehep.graphicsio.pdf;

import java.io.IOException;
import java.util.Calendar;

/**
 * Implements the Document Information Dictionary (see Table 8.2).
 * <p>
 *
 * @author Mark Donszelmann
 * @author Jason Wong
 */
@SuppressWarnings("nls")
public class PDFDocInfo extends PDFDictionary {

    PDFDocInfo(PDF pdf, PDFByteWriter writer, PDFObject parent)
            throws IOException {
        super(pdf, writer, parent);
    }

    public void setTitle(String title) throws IOException {
        entry("Title", title);
    }

    public void setAuthor(String author) throws IOException {
        entry("Author", author);
    }

    public void setSubject(String subject) throws IOException {
        entry("Subject", subject);
    }

    public void setKeywords(String keywords) throws IOException {
        entry("Keywords", keywords);
    }

    public void setCreator(String creator) throws IOException {
        entry("Creator", creator);
    }

    public void setProducer(String producer) throws IOException {
        entry("Producer", producer);
    }

    public void setCreationDate(Calendar date) throws IOException {
        entry("CreationDate", date);
    }

    public void setModificationDate(Calendar date) throws IOException {
        entry("ModDate", date);
    }

    public void setTrapped(String name) throws IOException {
        entry("Trapped", pdf.name(name));
    }
}
