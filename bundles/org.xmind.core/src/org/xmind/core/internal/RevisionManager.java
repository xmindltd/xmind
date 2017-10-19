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
import java.util.Iterator;
import java.util.List;

import org.xmind.core.IRevision;
import org.xmind.core.IRevisionManager;

/**
 * @author Frank Shaka
 * 
 */
public abstract class RevisionManager extends AbstractWorkbookComponent
        implements IRevisionManager {

    public List<IRevision> getRevisions() {
        ArrayList<IRevision> list = new ArrayList<IRevision>();
        Iterator<IRevision> it = iterRevisions();
        while (it.hasNext()) {
            list.add(it.next());
        }
        return list;
    }

    public List<IRevision> getRevisionsReversed() {
        ArrayList<IRevision> list = new ArrayList<IRevision>();
        Iterator<IRevision> it = iterRevisionsReversed();
        while (it.hasNext()) {
            list.add(it.next());
        }
        return list;
    }

    public boolean hasRevisions() {
        return iterRevisions().hasNext();
    }

    public IRevision getLatestRevision() {
        Iterator<IRevision> it = iterRevisionsReversed();
        return it.hasNext() ? it.next() : null;
    }

    public IRevision getRevision(int number) {
        Iterator<IRevision> it = iterRevisionsReversed();
        while (it.hasNext()) {
            IRevision revision = it.next();
            if (revision.getRevisionNumber() == number)
                return revision;
        }
        return null;
    }

}
