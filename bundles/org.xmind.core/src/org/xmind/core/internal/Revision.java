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

package org.xmind.core.internal;

import org.xmind.core.IRevision;

/**
 * @author Frank Shaka
 * 
 */
public abstract class Revision extends AbstractWorkbookComponent
        implements IRevision {

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(IRevision r) {
        int c = this.getRevisionNumber() - r.getRevisionNumber();
        if (c == 0) {
            c = (int) (this.getTimestamp() - r.getTimestamp());
        }
        return c;
    }

}
