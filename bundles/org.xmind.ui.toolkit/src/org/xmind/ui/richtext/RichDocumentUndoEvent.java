/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.richtext;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.IDocument;

/**
 * @author Frank Shaka
 */
public class RichDocumentUndoEvent {

    /**
     * Indicates that the described document event is about to be undone.
     */
    public static final int ABOUT_TO_UNDO = 1 << 0;

    /**
     * Indicates that the described document event is about to be redone.
     */
    public static final int ABOUT_TO_REDO = 1 << 1;

    /**
     * Indicates that the described document event has been undone.
     */
    public static final int UNDONE = 1 << 2;

    /**
     * Indicates that the described document event has been redone.
     */
    public static final int REDONE = 1 << 3;

    /**
     * Indicates that the described document event is a compound undo or redo
     * event.
     */
    public static final int COMPOUND = 1 << 4;

    /** The changed document. */
    private IDocument fDocument;

    /** The document offset where the change begins. */
    private int fOffset;

    /** Text inserted into the document. */
    private String fText;

    /** Text replaced in the document. */
    private String fPreservedText;

    /** Bit mask of event types describing the event */
    private int fEventType;

    /** The source that triggered this event or <code>null</code> if unknown. */
    private Object fSource;

    /**
     * Creates a new document event.
     * 
     * @param doc
     *            the changed document
     * @param offset
     *            the offset of the replaced text
     * @param text
     *            the substitution text
     * @param preservedText
     *            the replaced text
     * @param eventType
     *            a bit mask describing the type(s) of event
     * @param source
     *            the source that triggered this event or <code>null</code> if
     *            unknown
     */
    public RichDocumentUndoEvent(IDocument doc, int offset, String text,
            String preservedText, int eventType, Object source) {

        Assert.isNotNull(doc);
        Assert.isTrue(offset >= 0);

        fDocument = doc;
        fOffset = offset;
        fText = text;
        fPreservedText = preservedText;
        fEventType = eventType;
        fSource = source;
    }

    /**
     * Returns the changed document.
     * 
     * @return the changed document
     */
    public IDocument getDocument() {
        return fDocument;
    }

    /**
     * Returns the offset of the change.
     * 
     * @return the offset of the change
     */
    public int getOffset() {
        return fOffset;
    }

    /**
     * Returns the text that has been inserted.
     * 
     * @return the text that has been inserted
     */
    public String getText() {
        return fText;
    }

    /**
     * Returns the text that has been replaced.
     * 
     * @return the text that has been replaced
     */
    public String getPreservedText() {
        return fPreservedText;
    }

    /**
     * Returns the type of event that is occurring.
     * 
     * @return the bit mask that indicates the type (or types) of the event
     */
    public int getEventType() {
        return fEventType;
    }

    /**
     * Returns the source that triggered this event.
     * 
     * @return the source that triggered this event.
     */
    public Object getSource() {
        return fSource;
    }

    /**
     * Returns whether the change was a compound change or not.
     * 
     * @return <code>true</code> if the undo or redo change is a compound
     *         change, <code>false</code> if it is not
     */
    public boolean isCompound() {
        return (fEventType & COMPOUND) != 0;
    }

}