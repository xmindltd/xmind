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
package org.xmind.ui.forms;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

public class BellowsData {

    public boolean expandable = false;

    public boolean shrinkable = false;

    public int majorAlignment = SWT.FILL;

    public int minorAlignment = SWT.FILL;

    public int expandPriority = 0;

    public int shrinkPriority = 0;

    public int softMaximum = 0;

    public int softMinimum = 0;

    public int hardMaximum = 0;

    public int hardMinimum = 0;

    public boolean exclude = false;

    private int cachedWHint = SWT.DEFAULT;
    private int cachedHHint = SWT.DEFAULT;
    private Point cachedSize = null;

    public BellowsData() {
    }

    public BellowsData(int majorAlignment, int minorAlignment) {
        this.majorAlignment = majorAlignment;
        this.minorAlignment = minorAlignment;
    }

    public BellowsData(int majorAlignment, int minorAlignment,
            boolean expandable, boolean shrinkable) {
        this.majorAlignment = majorAlignment;
        this.minorAlignment = minorAlignment;
        this.expandable = expandable;
        this.shrinkable = shrinkable;
    }

    public BellowsData withExpansion(int priority, int softMaximum,
            int hardMaximum) {
        this.softMaximum = softMaximum;
        this.hardMaximum = hardMaximum;
        return this;
    }

    public BellowsData withShrinkage(int priority, int softMinimum,
            int hardMinimum) {
        this.shrinkPriority = priority;
        this.softMinimum = softMinimum;
        this.hardMinimum = hardMinimum;
        return this;
    }

    Point computeSize(Control control, int wHint, int hHint, boolean flushCache) {
        if (cachedSize != null && wHint == cachedWHint && hHint == cachedHHint
                && !flushCache)
            return cachedSize;
        cachedSize = control.computeSize(wHint, hHint, true);
        cachedWHint = wHint;
        cachedHHint = hHint;
        return cachedSize;
    }

    void flushCache() {
        cachedSize = null;
    }
}
