/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.core.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.xmind.core.IFileEntry;

public abstract class FileEntry extends AbstractWorkbookComponent
        implements IFileEntry {

    protected static final List<IFileEntry> NO_SUB_FILE_ENTRIES = Collections
            .emptyList();

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.IFileEntry#getSubEntries()
     */
    public List<IFileEntry> getSubEntries() {
        if (!isDirectory())
            return NO_SUB_FILE_ENTRIES;
        List<IFileEntry> list = new ArrayList<IFileEntry>();
        Iterator<IFileEntry> it = iterSubEntries();
        while (it.hasNext()) {
            list.add(it.next());
        }
        return list;
    }

}