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

import org.eclipse.jface.action.IAction;
import org.xmind.gef.IDisposable;

public interface IActionRegistry extends IDisposable {

    IAction getAction(String actionId);

    Collection<IAction> getActions();

    void addAction(IAction action);

    void removeAction(IAction action);

    void removeAction(String actionId);

    IActionRegistry getParent();

    void setParent(IActionRegistry parent);

}