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
package org.xmind.gef.tool;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.xmind.gef.part.IPart;

public class PartTextSelection extends TextSelection {

    private IPart part;

    private IDocument cachedDocument;

    public PartTextSelection(int offset, int length) {
        this(null, null, offset, length);
        this.cachedDocument = null;
    }

    public PartTextSelection(IPart part, int offset, int length) {
        this(part, null, offset, length);
        this.cachedDocument = null;
    }

    public PartTextSelection(IDocument document, int offset, int length) {
        this(null, document, offset, length);
        this.cachedDocument = document;
    }

    public PartTextSelection(IPart part, IDocument document, int offset,
            int length) {
        super(document, offset, length);
        this.part = part;
        this.cachedDocument = document;
    }

    public IPart getPart() {
        return part;
    }

    public IDocument getDocument() {
        return cachedDocument;
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof PartTextSelection))
            return false;
        PartTextSelection that = (PartTextSelection) obj;
        if (this.part != that.part)
            return false;
        boolean sameRange = this.getOffset() == that.getOffset()
                && this.getLength() == that.getLength();
        if (sameRange) {
            if (that.cachedDocument == null && this.cachedDocument == null)
                return true;
            if (that.cachedDocument == null || this.cachedDocument == null)
                return false;

            try {
                String sContent = that.cachedDocument.get(that.getOffset(),
                        that.getLength());
                String content = this.cachedDocument.get(this.getOffset(), this
                        .getLength());
                return sContent.equals(content);
            } catch (BadLocationException x) {
            }
        }
        return false;
    }

    public int hashCode() {
        int low = cachedDocument != null ? cachedDocument.hashCode() : 0;
        low ^= part != null ? part.hashCode() : 0;
        return (getOffset() << 24) | (getLength() << 16) | low;
    }

    public String toString() {
        return "[part=" + part + ", <" + getOffset() + "," + getLength() + ">=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                + getText() + "]"; //$NON-NLS-1$
    }
}