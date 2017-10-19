// Copyright 2001-2005, FreeHEP
package org.xmind.org.freehep.graphicsio.pdf;

import java.awt.Color;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Delay <tt>Image</tt> objects for writing XObjects to the pdf file when the
 * pageStream is complete. Caches identical images to only write them once.
 *
 * @author Simon Fischer
 * @author Mark Donszelmann
 * @author Jason Wong
 */
@SuppressWarnings("nls")
public class PDFImageDelayQueue {

    private int currentNumber = 0;

    private class Entry {
        private RenderedImage image;

        private String name, maskName;

        private Color bkg;

        private String writeAs;

        private boolean written;

        private Entry(RenderedImage image, Color bkg, String writeAs) {
            this.image = image;
            this.bkg = bkg;
            this.writeAs = writeAs;
            this.name = "Img" + (currentNumber++);
            if (image.getColorModel().hasAlpha() && (bkg == null)) {
                maskName = name + "Mask";
            } else {
                maskName = null;
            }
            this.written = false;
        }
    }

    private Map<RenderedImage, Entry> imageMap;

    private List<Entry> imageList;

    private PDFWriter pdf;

    public PDFImageDelayQueue(PDFWriter pdf) {
        this.pdf = pdf;
        this.imageMap = new HashMap<RenderedImage, Entry>();
        this.imageList = new LinkedList<Entry>();
    }

    public PDFName delayImage(RenderedImage image, Color bkg, String writeAs) {
        Entry entry = (Entry) imageMap.get(image);
        if (entry == null) {
            entry = new Entry(image, bkg, writeAs);
            imageMap.put(image, entry);
            imageList.add(entry);
        }

        return pdf.name(entry.name);
    }

    /** Creates a stream for every delayed image that is not written yet. */
    public void processAll() throws IOException {
        for (Iterator<Entry> i = imageList.iterator(); i.hasNext();) {
            Entry entry = (Entry) i.next();

            if (!entry.written) {
                entry.written = true;

                PDFStream img = pdf.openStream(entry.name);
                img.entry("Subtype", pdf.name("Image"));
                if (entry.maskName != null)
                    img.entry("SMask", pdf.ref(entry.maskName));
                img.image(entry.image, entry.bkg, entry.writeAs);
                pdf.close(img);

                if (entry.maskName != null) {
                    PDFStream mask = pdf.openStream(entry.maskName);
                    mask.entry("Subtype", pdf.name("Image"));
                    mask.imageMask(entry.image, entry.writeAs);
                    pdf.close(mask);
                }
            }
        }
    }

    /**
     * Adds all names to the dictionary which should be the value of the
     * resources dicionrary's /XObject entry.
     */
    public int addXObjects() throws IOException {
        if (imageList.size() > 0) {
            PDFDictionary xobj = pdf.openDictionary("XObjects");
            for (Iterator<Entry> i = imageList.iterator(); i.hasNext();) {
                Entry entry = (Entry) i.next();
                xobj.entry(entry.name, pdf.ref(entry.name));
                if (entry.maskName != null)
                    xobj.entry(entry.maskName, pdf.ref(entry.maskName));
            }
            pdf.close(xobj);
        }
        return imageList.size();
    }
}
