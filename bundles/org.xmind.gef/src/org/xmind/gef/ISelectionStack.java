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
package org.xmind.gef;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.xmind.gef.command.ICommandStack;

/**
 * Record selection status before each command operation and restore it when
 * reversing those operations.
 * 
 * @author Frank Shaka
 */
public interface ISelectionStack {

    /**
     * Sets the selection provider for this stack to cache and restore
     * selections.
     * <p>
     * If the selection provider is changed, all selections previously cached in
     * this stack will be cleared.
     * </p>
     * 
     * @param selectionProvider
     *            The selection provider to set; may be <code>null</code> to
     *            tell this stack to stop any further operations on the current
     *            selection provider.
     */
    void setSelectionProvider(ISelectionProvider selectionProvider);

    /**
     * Sets the command stack to listen command event to.
     * 
     * @param commandStack
     *            The command stack to set; may be <code>null</code> to tell
     *            this stack to stop listen to any command events fired by the
     *            current command stack.
     */
    void setCommandStack(ICommandStack commandStack);

}