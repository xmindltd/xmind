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

import java.util.List;

import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.ITopicPath;
import org.xmind.core.IWorkbook;

/**
 * @author briansun
 * 
 */
public abstract class TopicPath implements ITopicPath {

    private List<Object> pathEntries;

    /**
     * @see org.xmind.core.ITopicPath#toList()
     */
    public List<Object> toList() {
        if (pathEntries == null) {
            pathEntries = createPathEntries();
        }
        return pathEntries;
    }

    protected abstract List<Object> createPathEntries();

    /**
     * @see org.xmind.core.ITopicPath#getRootTopic()
     */
    public ITopic getRootTopic() {
        for (Object o : toList()) {
            if (o instanceof ITopic) {
                ITopic t = (ITopic) o;
                if (t.isRoot())
                    return t;
                break;
            }
        }
        return null;
    }

    /**
     * @see org.xmind.core.ITopicPath#getSheet()
     */
    public ISheet getSheet() {
        List<Object> list = toList();
        if (list.size() > 0) {
            Object o = list.get(0);
            if (o instanceof ISheet)
                return (ISheet) o;
            if (list.size() > 1) {
                o = list.get(1);
                if (o instanceof ISheet)
                    return (ISheet) o;
            }
        }
        return null;
    }

    /**
     * @see org.xmind.core.ITopicPath#getWorkbook()
     */
    public IWorkbook getWorkbook() {
        List<Object> list = toList();
        if (list.size() > 0) {
            Object o = list.get(0);
            if (o instanceof IWorkbook)
                return (IWorkbook) o;
        }
        return null;
    }

    /**
     * @see org.xmind.core.ITopicPath#contains(org.xmind.core.ITopic)
     */
    public boolean contains(ITopic topic) {
        return toList().contains(topic);
    }

    public boolean isDescendentOf(ITopic ancestor) {
        List<Object> list = toList();
        if (list.size() > 0) {
            Object t = list.get(list.size() - 1);
            if (t.equals(ancestor))
                return false;
            return list.contains(ancestor);
        }
        return false;
    }

    /**
     * @see org.xmind.core.ITopicPath#toTopicList()
     */
    @SuppressWarnings("unchecked")
    public List<ITopic> toTopicList() {
        int begin = 0;
        if (getWorkbook() != null)
            begin++;
        if (getSheet() != null)
            begin++;
        if (begin == 0)
            return (List) toList();
        return (List) toList().subList(begin, toList().size());
    }

}