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

package org.xmind.ui.internal.tools;

import java.io.IOException;

import org.eclipse.core.runtime.Platform;

/**
 * @author Frank Shaka
 */
public class QuickOpenHelper {

    private static QuickOpenHelper instance = null;

    private Process process;

    public boolean canShow() {
        return "macosx".equals(Platform.getOS()); //$NON-NLS-1$
    }

    public void show(String... paths) {
        hide();
        String[] commands = new String[paths.length + 2];
        commands[0] = "qlmanage"; //$NON-NLS-1$
        commands[1] = "-p"; //$NON-NLS-1$
        System.arraycopy(paths, 0, commands, 2, paths.length);
        Process proc;
        try {
            proc = Runtime.getRuntime().exec(commands);
        } catch (IOException e) {
            return;
        }
        this.process = proc;
    }

    public boolean isOpen() {
        if (process == null)
            return false;
        try {
            process.exitValue();
            return false;
        } catch (IllegalThreadStateException e) {
            return true;
        }
    }

    public void hide() {
        if (process != null) {
            process.destroy();
            process = null;
        }
    }

    /**
     * @return the instance
     */
    public static QuickOpenHelper getInstance() {
        if (instance == null) {
            instance = new QuickOpenHelper();
        }
        return instance;
    }

}
