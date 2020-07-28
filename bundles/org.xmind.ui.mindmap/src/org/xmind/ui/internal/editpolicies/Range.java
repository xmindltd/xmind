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
package org.xmind.ui.internal.editpolicies;

import org.xmind.core.ITopic;

public class Range {

    public int start;

    public int end;

    public ITopic overTopic;

    public Range(int initIndex) {
        this.start = initIndex;
        this.end = initIndex;
        this.overTopic = null;
    }

    public Range(int start, int end) {
        this.start = start;
        this.end = end;
        this.overTopic = null;
    }

    public Range(ITopic overTopic) {
        this.start = -1;
        this.end = -1;
        this.overTopic = overTopic;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof Range))
            return false;
        Range that = (Range) obj;
        return this.start == that.start
                && this.end == that.end
                && (this.overTopic == that.overTopic || (this.overTopic != null && this.overTopic
                        .equals(that.overTopic)));
    }

    public int hashCode() {
        return start ^ end;
    }

}