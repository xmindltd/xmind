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

package org.xmind.ui.mindmap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.xmind.gef.Disposable;
import org.xmind.ui.viewers.ICompositeProvider;

public class GhostShellProvider extends Disposable
        implements ICompositeProvider {

    private Display display;

    private Shell parentShell = null;

    private Shell shell = null;

    /**
     * 
     */
    public GhostShellProvider(Display display) {
        this.display = display;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.window.IShellProvider#getShell()
     */
    public Composite getParent() {
        if (isDisposed())
            return null;

        /*
         * Creating a parent shell without calling 'setVisible()' or 'open()'
         * will hide the working shell's taskbar icon.
         */
        if (parentShell == null) {
            parentShell = new Shell(display, SWT.NO_TRIM);
            parentShell.setBounds(-300, -300, 1, 1);
        }
        if (shell == null) {
            shell = new Shell(parentShell, SWT.NO_TRIM);
            shell.setBounds(-300, -300, 180, 180);
//            if (!"cocoa".equals(SWT.getPlatform())) { //$NON-NLS-1$
//                shell.setVisible(true);
//                display.asyncExec(new Runnable() {
//                    public void run() {
//                        if (shell != null && !shell.isDisposed()) {
//                            shell.setVisible(false);
//                        }
//                    }
//                });
//            }
        }
        return shell;
    }

    public void dispose() {
        final Shell oldParentShell = this.parentShell;
        final Shell oldShell = this.shell;
        this.shell = null;
        if (display != null && !display.isDisposed()) {
            display.syncExec(new Runnable() {
                public void run() {
                    if (oldParentShell != null) {
                        oldParentShell.dispose();
                    }
                    if (oldShell != null) {
                        oldShell.dispose();
                    }
                }
            });
        }
        super.dispose();
    }

}