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
package org.xmind.core.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xmind.core.IFileEntry;
import org.xmind.core.IManifest;

public abstract class Manifest extends AbstractWorkbookComponent
        implements IManifest {

    public List<IFileEntry> getFileEntries() {
        List<IFileEntry> list = new ArrayList<IFileEntry>();
        Iterator<IFileEntry> it = iterFileEntries();
        while (it.hasNext()) {
            list.add(it.next());
        }
        return list;
    }

    public boolean deleteFileEntry(String path) {
        IFileEntry entry = getFileEntry(path);
        if (entry == null)
            return false;
        entry.increaseReference();
        while (entry.getReferenceCount() > 0) {
            entry.decreaseReference();
        }
        return true;
    }
}