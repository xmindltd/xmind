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

import java.util.List;

import org.eclipse.swt.accessibility.ACC;
import org.xmind.gef.GEF;
import org.xmind.gef.part.IPart;

public abstract class AbstractAccessible extends AccessibleBase {

    private IPart host;

    public AbstractAccessible(IPart host) {
        this.host = host;
    }

    public IPart getHost() {
        return host;
    }

    public Object[] getChildren() {
        List<? extends IPart> list = getChildrenParts();
        Object[] children = new Object[list.size()];
        for (int i = 0; i < list.size(); i++) {
            IPart child = list.get(i);
            IAccessible acc = (IAccessible) child.getAdapter(IAccessible.class);
            if (acc == null)
                return null;
            children[i] = Integer.valueOf(acc.getAccessibleId());
        }
        return children;
    }

    protected List<? extends IPart> getChildrenParts() {
        return host.getChildren();
    }

    public int getChildrenCount() {
        return getChildrenParts().size();
    }

    public int getState() {
        int state = -1;
        state = mergeState(state, ACC.STATE_FOCUSABLE);
        if (host.getStatus().isFocused()) {
            state = mergeState(state, ACC.STATE_FOCUSED);
        }

        if (host.hasRole(GEF.ROLE_SELECTABLE)) {
            state = mergeState(state, ACC.STATE_SELECTABLE);
            if (host.getStatus().isSelected()) {
                state = mergeState(state, ACC.STATE_SELECTED);
            }
        }
        return state;
    }

    protected static int mergeState(int currentState, int newState) {
        if (currentState < 0)
            return newState;
        return currentState | newState;
    }

    public String getDefaultAction() {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public String getHelp() {
        return null;
    }

    public String getKeyboardShortcut() {
        return null;
    }

    public int getRole() {
        return -1;
    }

    public String getValue() {
        return null;
    }

}