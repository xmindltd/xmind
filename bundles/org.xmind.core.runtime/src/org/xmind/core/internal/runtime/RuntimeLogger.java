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

package org.xmind.core.internal.runtime;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.xmind.core.internal.XmindCore;
import org.xmind.core.util.ILogger;

/**
 * @author Frank Shaka
 * 
 */
public class RuntimeLogger implements ILogger {

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.util.ILogger#log(java.lang.String)
     */
    public void log(String message) {
        XmindCore.getDefault().getLog().log(
                new Status(IStatus.ERROR, XmindCore.PLUGIN_ID, message));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.util.ILogger#log(java.lang.Throwable,
     * java.lang.String)
     */
    public void log(Throwable e, String message) {
        XmindCore.getDefault().getLog().log(
                new Status(IStatus.ERROR, XmindCore.PLUGIN_ID, message, e));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.util.ILogger#log(java.lang.Throwable)
     */
    public void log(Throwable e) {
        XmindCore.getDefault().getLog().log(
                new Status(IStatus.ERROR, XmindCore.PLUGIN_ID,
                        "(Untitled error)", e)); //$NON-NLS-1$
    }

}
