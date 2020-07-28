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
package org.xmind.gef.ui.actions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.xmind.gef.IDisposable;

public class ActionRegistry implements IActionRegistry {

    private Map<Object, IAction> actions = new HashMap<Object, IAction>();

    private IActionRegistry parent;

    public ActionRegistry() {
    }

    public ActionRegistry(IActionRegistry parent) {
        setParent(parent);
    }

    public void dispose() {
        for (IAction action : getActions()) {
            if (action instanceof IDisposable) {
                ((IDisposable) action).dispose();
            } else if (action instanceof IWorkbenchAction) {
                ((IWorkbenchAction) action).dispose();
            }
        }
        if (actions != null) {
            actions.clear();
            actions = null;
        }
        parent = null;
    }

    public IAction getAction(String id) {
        IAction action = null;
        if (actions != null) {
            action = actions.get(id);
        }
        if (action == null && parent != null) {
            action = parent.getAction(id);
        }
        return action;
    }

    public Collection<IAction> getActions() {
        return actions.values();
    }

    public void addAction(IAction action) {
        String id = action.getId();
        Assert.isNotNull(id, "Action must have an ID before registering: " //$NON-NLS-1$
                + action.toString());
        setAction(id, action);
    }

    public void setAction(String actionId, IAction action) {
        if (action == null) {
            actions.remove(actionId);
        } else {
            actions.put(actionId, action);
        }
    }

    public void removeAction(IAction action) {
        removeAction(action.getId());
    }

    public void removeAction(String actionId) {
        actions.remove(actionId);
    }

    public IActionRegistry getParent() {
        return parent;
    }

    public void setParent(IActionRegistry parent) {
        this.parent = parent;
    }

}
