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
package org.xmind.gef.acc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AccessibleRegistry {

    private List<Integer> ids = new ArrayList<Integer>();

    private List<Integer> removedIds = new ArrayList<Integer>();

    private List<IAccessible> accessibles = new ArrayList<IAccessible>();

    private int lastId = 0;

    public void register(IAccessible acc) {
        if (acc == null)
            return;
        Integer id;
        if (acc instanceof AccessibleBase) {
//            id = createId(0);
            lastId = createId(lastId);
            id = lastId++;
            ((AccessibleBase) acc).setAccessibleId(id);
        } else {
            id = acc.getAccessibleId();
        }
        ids.add(id);
        accessibles.add(acc);
    }

    private Integer createId(Integer newId) {
        if (!ids.contains(newId))
            return newId;
        return createId(newId + 1);
    }

    public void unregister(IAccessible acc) {
        if (acc == null)
            return;
        Integer id = acc.getAccessibleId();
        int index = ids.indexOf(id);
        if (index >= 0) {
            if (index < ids.size() - 1) {
                removedIds.add(id);
            }
            ids.remove(index);
            accessibles.remove(index);
        }
    }

    public IAccessible getAccessible(int id) {
        int index = ids.indexOf(id);
        if (index >= 0) {
            return accessibles.get(index);
        }
        return null;
    }

    public Object[] getAllAccessibleIDs() {
        Object[] array = ids.toArray();
        Arrays.sort(array);
        return array;
    }

    public int getNumAccessibles() {
        return ids.size();
    }

}