/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
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
/**
 * 
 */
package org.xmind.ui.internal.actions;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.xmind.gef.EditDomain;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.ICommandStack;

/**
 * @author Frank Shaka
 *
 */
public abstract class ViewerAction extends Action {

    private final IGraphicalViewer viewer;

    /**
     * 
     */
    public ViewerAction(IGraphicalViewer viewer) {
        super();
        Assert.isNotNull(viewer);
        this.viewer = viewer;
    }

    /**
     * @return the viewer
     */
    protected IGraphicalViewer getViewer() {
        return viewer;
    }

    protected ICommandStack getCommandStack() {
        EditDomain editDomain = viewer.getEditDomain();
        return editDomain == null ? null : editDomain.getCommandStack();
    }

    protected void executeCommand(Command command) {
        ICommandStack commandStack = getCommandStack();
        if (commandStack != null) {
            commandStack.execute(command);
        } else {
            command.execute();
        }
    }

}