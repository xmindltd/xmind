// Copyright 2001 freehep
package org.xmind.org.freehep.graphicsio.pdf;

import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

/**
 * This class keeps track of all kinds of objects written to a pdf file and
 * avoids to write them several times instead of referencing the same object
 * several times. Right now only encoding tables are supported.
 *
 * An implementation for images and paint would be possible.
 *
 * @author Simon Fischer
 * @author Jason Wong
 */
public class PDFRedundanceTracker {

    /**
     * To be implemented by Writers which write objects that may already have
     * been written.
     */
    public interface Writer {
        public void writeObject(Object o, PDFRef reference, PDFWriter pdf)
                throws IOException;
    }

    private class Entry {
        private static final String REF_PREFIX = "PDF_RTObj"; //$NON-NLS-1$

        private Object object;

        private Writer writer;

        private boolean written;

        private PDFRef reference;

        private Object groupID;

        private Entry(Object o, Object groupID, Writer w) {
            this.object = o;
            this.groupID = groupID;
            this.writer = w;
            this.written = false;
            this.reference = pdf.ref(REF_PREFIX + (refCount++));
        }
    }

    private static int refCount = 1;

    private PDFWriter pdf;

    private Map<Object, Entry> objects;

    private Vector<Entry> orderedObjects; // to keep order

    public PDFRedundanceTracker(PDFWriter pdf) {
        this.pdf = pdf;
        objects = new Hashtable<Object, Entry>();
        orderedObjects = new Vector<Entry>();
    }

    /**
     * Returns a reference that points to <tt>object</tt>. When this method is
     * called several times for the same object (according to its hash code) the
     * same reference is returned. When <tt>writeAll()</tt> is called the
     * writer's <tt>writeObject()</tt> method will be called once with
     * <tt>object</tt> as argument.<br>
     * The groupID is only used for <tt>getGroup()</tt>
     */
    public PDFRef getReference(Object object, Object groupID, Writer writer) {
        Object o = objects.get(object);
        if (o != null) {
            return ((Entry) o).reference;
        } else {
            Entry entry = new Entry(object, groupID, writer);
            objects.put(object, entry);
            orderedObjects.add(entry);
            return entry.reference;
        }
    }

    public PDFRef getReference(Object object, Writer writer) {
        return getReference(object, null, writer);
    }

    /**
     * Writes all objects that are not yet written. If the method is called
     * several times then each times only the new objects are written.
     */
    public void writeAll() {
        Iterator<Entry> i = orderedObjects.iterator();
        while (i.hasNext()) {
            Entry entry = (Entry) i.next();
            if (!entry.written) {
                try {
                    // System.out.println("PDFRT: Writing: " + entry.object);
                    entry.writer
                            .writeObject(entry.object, entry.reference, pdf);
                    entry.written = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /** Returns all objects belonging to a particular group. */
    public Collection<Object> getGroup(Object groupID) {
        Collection<Object> result = new LinkedList<Object>();
        Iterator<Entry> i = orderedObjects.iterator();
        while (i.hasNext()) {
            Entry entry = (Entry) i.next();
            if (groupID.equals(entry.groupID)) {
                result.add(entry.object);
            }
        }
        return result;
    }
}
