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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.IDocument;

/**
 * @author Frank Shaka
 */
public class RichDocumentUndoManagerRegistry {

    private static final class Record {
        public Record(IRichDocument document) {
            count = 0;
            undoManager = new RichDocumentUndoManager(document);
        }

        private int count;
        private IRichDocumentUndoManager undoManager;
    }

    private static Map<IRichDocument, Record> fgFactory = new HashMap<IRichDocument, Record>();

    private RichDocumentUndoManagerRegistry() {
        //  Do not instantiate  
    }

    /**
     * Connects the file at the given location to this manager. After that call
     * successfully completed it is guaranteed that each call to
     * <code>getFileBuffer</code> returns the same file buffer until
     * <code>disconnect</code> is called.
     * <p>
     * <em>The recoding of changes starts with the first {@link #connect(IDocument)}.</em>
     * </p>
     * 
     * @param document
     *            the document to be connected
     */
    public static synchronized void connect(IRichDocument document) {
        Assert.isNotNull(document);
        Record record = (Record) fgFactory.get(document);
        if (record == null) {
            record = new Record(document);
            fgFactory.put(document, record);
        }
        record.count++;
    }

    /**
     * Disconnects the given document from this registry.
     * 
     * @param document
     *            the document to be disconnected
     */
    public static synchronized void disconnect(IRichDocument document) {
        Assert.isNotNull(document);
        Record record = (Record) fgFactory.get(document);
        if (record != null) {
            record.count--;
            if (record.count == 0)
                fgFactory.remove(document);
        }
    }

    /**
     * Returns the file buffer managed for the given location or
     * <code>null</code> if there is no such file buffer.
     * <p>
     * The provided location is either a full path of a workspace resource or an
     * absolute path in the local file system. The file buffer manager does not
     * resolve the location of workspace resources in the case of linked
     * resources.
     * </p>
     * 
     * @param document
     *            the document for which to get its undo manager
     * @return the document undo manager or <code>null</code>
     */
    public static synchronized IRichDocumentUndoManager getDocumentUndoManager(
            IRichDocument document) {
        Assert.isNotNull(document);
        Record record = (Record) fgFactory.get(document);
        if (record == null)
            return null;
        return record.undoManager;
    }

}