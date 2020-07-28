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
package org.xmind.ui.util;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.xmind.ui.internal.MindMapUIPlugin;

public class Logger {

    public static void log(Throwable e) {
        log(e, null);
    }

    public static void log(Throwable e, String message) {
        if (message == null)
            message = ""; //$NON-NLS-1$
        MindMapUIPlugin
                .getDefault()
                .getLog()
                .log(new Status(IStatus.ERROR, MindMapUIPlugin.PLUGIN_ID,
                        IStatus.ERROR, message, e));
    }

    public static void log(String message) {
        if (message == null)
            message = ""; //$NON-NLS-1$
        MindMapUIPlugin
                .getDefault()
                .getLog()
                .log(new Status(IStatus.INFO, MindMapUIPlugin.PLUGIN_ID,
                        message));
    }

    public static void debug(Class<?> clazz, String message) {
        System.out.println(clazz.getSimpleName() + ": " + message); //$NON-NLS-1$
    }

}