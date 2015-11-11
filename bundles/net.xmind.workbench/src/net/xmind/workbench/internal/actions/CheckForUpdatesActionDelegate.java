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
package net.xmind.workbench.internal.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import net.xmind.workbench.internal.CheckForUpdatesJob;

@Deprecated
public class CheckForUpdatesActionDelegate
        implements IWorkbenchWindowActionDelegate {

    private IWorkbenchWindow window = null;

    public void run(IAction action) {
        if (this.window == null)
            return;

        IWorkbench workbench = this.window.getWorkbench();
        if (workbench == null || workbench.isClosing())
            return;

        new CheckForUpdatesJob(workbench).schedule();
    }

    public void selectionChanged(IAction action, ISelection selection) {
    }

    public void dispose() {
        this.window = null;
    }

    public void init(IWorkbenchWindow window) {
        this.window = window;
    }

}
