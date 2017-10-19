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

package org.xmind.ui.gallery;

import java.util.List;

import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.part.IPart;
import org.xmind.gef.policy.IEditPolicy;

/**
 * @author Frank Shaka
 * 
 */
public class NavigationItemNavigablePolicy implements IEditPolicy {

    public static final NavigationItemNavigablePolicy DEFAULT = new NavigationItemNavigablePolicy();

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.policy.IEditPolicy#understands(java.lang.String)
     */
    public boolean understands(String requestType) {
        return GEF.REQ_NAV_LEFT.equals(requestType)
                || GEF.REQ_NAV_RIGHT.equals(requestType)
                || GEF.REQ_NAV_PREV.equals(requestType)
                || GEF.REQ_NAV_NEXT.equals(requestType)
                || GEF.REQ_NAV_BEGINNING.equals(requestType)
                || GEF.REQ_NAV_END.equals(requestType);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.policy.IEditPolicy#handle(org.xmind.gef.Request)
     */
    public void handle(Request request) {
        NavigationItemPart item = findItem(request);
        if (item == null)
            return;

        IPart parent = item.getParent();
        if (parent == null)
            return;

        List<IPart> items = parent.getChildren();
        int index = items.indexOf(item);
        if (index < 0)
            return;

        String requestType = request.getType();
        if (GEF.REQ_NAV_BEGINNING.equals(requestType)) {
            handleNavFirst(request, items);
        } else if (GEF.REQ_NAV_END.equals(requestType)) {
            handleNavLast(request, items);
        } else if (GEF.REQ_NAV_LEFT.equals(requestType)
                || GEF.REQ_NAV_PREV.equals(requestType)) {
            handleNavPrev(request, items, index);
        } else if (GEF.REQ_NAV_RIGHT.equals(requestType)
                || GEF.REQ_NAV_NEXT.equals(requestType)) {
            handleNavNext(request, items, index);
        }
    }

    /**
     * @param request
     */
    private void handleNavFirst(Request request, List<IPart> items) {
        if (items.size() > 0) {
            setResult(request, items.get(0));
        }
    }

    /**
     * @param request
     */
    private void handleNavLast(Request request, List<IPart> items) {
        if (items.size() > 0) {
            setResult(request, items.get(items.size() - 1));
        }
    }

    /**
     * @param request
     */
    private void handleNavPrev(Request request, List<IPart> items, int index) {
        index = Math.max(0, Math.min(items.size() - 1, index - 1));
        setResult(request, items.get(index));
    }

    /**
     * @param request
     */
    private void handleNavNext(Request request, List<IPart> items, int index) {
        index = Math.max(0, Math.min(items.size() - 1, index + 1));
        setResult(request, items.get(index));
    }

    private void setResult(Request request, IPart part) {
        request.setResult(GEF.RESULT_NAVIGATION, new IPart[] { part });
    }

    private NavigationItemPart findItem(Request request) {
        for (IPart part : request.getTargets()) {
            if (part instanceof NavigationItemPart)
                return (NavigationItemPart) part;
        }
        return null;
    }

}
