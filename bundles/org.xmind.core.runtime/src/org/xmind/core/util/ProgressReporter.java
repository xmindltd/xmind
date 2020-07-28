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
package org.xmind.core.util;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.xmind.core.util.IProgressReporter;

/**
 * @author Frank Shaka
 *
 */
public class ProgressReporter implements IProgressReporter {

    private static final int DEFAULT_TOTAL = 10000;

    private SubMonitor monitor;

    private int worked;

    /**
     * 
     */
    public ProgressReporter(IProgressMonitor monitor) {
        this.monitor = SubMonitor.convert(monitor, DEFAULT_TOTAL);
        this.worked = 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.util.IProgressReporter#progressChanged(int, int)
     */
    public void progressChanged(int current, int total) {
        int target = current * DEFAULT_TOTAL / total;
        monitor.worked(target - worked);
        this.worked = target;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.util.IProgressReporter#isCanceled()
     */
    public boolean isCanceled() {
        return monitor.isCanceled();
    }

}
