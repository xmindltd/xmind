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
package org.xmind.gef.part;

import java.util.HashMap;
import java.util.Map;

import org.xmind.gef.GEF;

/**
 * @author Brian Sun
 */
public class PartRoles {

    private Map<String, String> reqToRole = new HashMap<String, String>();

    public PartRoles() {
        setRole(GEF.REQ_SELECT, GEF.ROLE_SELECTABLE);
        setRole(GEF.REQ_SELECT_ALL, GEF.ROLE_SELECTABLE);
        setRole(GEF.REQ_SELECT_MULTI, GEF.ROLE_SELECTABLE);
        setRole(GEF.REQ_SELECT_NONE, GEF.ROLE_SELECTABLE);
        setRole(GEF.REQ_SELECT_SINGLE, GEF.ROLE_SELECTABLE);

        setRole(GEF.REQ_CREATE, GEF.ROLE_CREATABLE);
        setRole(GEF.REQ_DELETE, GEF.ROLE_DELETABLE);

        setRole(GEF.REQ_EXTEND, GEF.ROLE_EXTENDABLE);
        setRole(GEF.REQ_COLLAPSE, GEF.ROLE_EXTENDABLE);
        setRole(GEF.REQ_EXTEND_ALL, GEF.ROLE_EXTENDABLE);
        setRole(GEF.REQ_COLLAPSE_ALL, GEF.ROLE_EXTENDABLE);

        setRole(GEF.REQ_MOVETO, GEF.ROLE_MOVABLE);
        setRole(GEF.REQ_COPYTO, GEF.ROLE_MOVABLE);
        setRole(GEF.REQ_RESIZE, GEF.ROLE_MOVABLE);
        setRole(GEF.REQ_ALIGN, GEF.ROLE_MOVABLE);

        setRole(GEF.REQ_SORT, GEF.ROLE_SORTABLE);

        setRole(GEF.REQ_MOVE_NEXT, GEF.ROLE_MOVABLE);
        setRole(GEF.REQ_MOVE_PREV, GEF.ROLE_MOVABLE);
        setRole(GEF.REQ_MOVE_UP, GEF.ROLE_MOVABLE);
        setRole(GEF.REQ_MOVE_DOWN, GEF.ROLE_MOVABLE);
        setRole(GEF.REQ_MOVE_LEFT, GEF.ROLE_MOVABLE);
        setRole(GEF.REQ_MOVE_RIGHT, GEF.ROLE_MOVABLE);

        setRole(GEF.REQ_MODIFY, GEF.ROLE_MODIFIABLE);

        setRole(GEF.REQ_ZOOM, GEF.ROLE_SCALABLE);
        setRole(GEF.REQ_ZOOMIN, GEF.ROLE_SCALABLE);
        setRole(GEF.REQ_ZOOMOUT, GEF.ROLE_SCALABLE);
        setRole(GEF.REQ_ACTUALSIZE, GEF.ROLE_SCALABLE);
        setRole(GEF.REQ_FITSIZE, GEF.ROLE_SCALABLE);
        setRole(GEF.REQ_FITSELECTION, GEF.ROLE_SCALABLE);

        setRole(GEF.REQ_SHOW_ALL, GEF.ROLE_FILTERABLE);
        setRole(GEF.REQ_SHOW_OTHER, GEF.ROLE_FILTERABLE);
        setRole(GEF.REQ_HIDE_ALL, GEF.ROLE_FILTERABLE);
        setRole(GEF.REQ_SHOW, GEF.ROLE_FILTERABLE);
        setRole(GEF.REQ_HIDE, GEF.ROLE_FILTERABLE);
        setRole(GEF.REQ_SHOW_ONLY, GEF.ROLE_FILTERABLE);

        setRole(GEF.REQ_COPY, GEF.ROLE_EDITABLE);
        setRole(GEF.REQ_CUT, GEF.ROLE_EDITABLE);
        setRole(GEF.REQ_PASTE, GEF.ROLE_EDITABLE);

        setRole(GEF.REQ_NAV_UP, GEF.ROLE_NAVIGABLE);
        setRole(GEF.REQ_NAV_LEFT, GEF.ROLE_NAVIGABLE);
        setRole(GEF.REQ_NAV_END, GEF.ROLE_NAVIGABLE);
        setRole(GEF.REQ_NAV_RIGHT, GEF.ROLE_NAVIGABLE);
        setRole(GEF.REQ_NAV_BEGINNING, GEF.ROLE_NAVIGABLE);
        setRole(GEF.REQ_NAV_DOWN, GEF.ROLE_NAVIGABLE);
        setRole(GEF.REQ_NAV_NEXT, GEF.ROLE_NAVIGABLE);
        setRole(GEF.REQ_NAV_PREV, GEF.ROLE_NAVIGABLE);

        setRole(GEF.REQ_TRAVERSE, GEF.ROLE_TRAVERSABLE);
        setRole(GEF.REQ_GET_TRAVERSABLES, GEF.ROLE_TRAVERSABLE);

        setRole(GEF.REQ_DROP, GEF.ROLE_DROP_TARGET);
    }

    public boolean hasRole(String role) {
        return reqToRole.containsValue(role);
    }

    public boolean hasRequest(String requestType) {
        return reqToRole.containsKey(requestType);
    }

    public String getRole(String requestType) {
        return reqToRole.get(requestType);
    }

    public void setRole(String requestType, String role) {
        reqToRole.put(requestType, role);
    }

}