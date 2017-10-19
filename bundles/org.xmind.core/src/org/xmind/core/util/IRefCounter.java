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
package org.xmind.core.util;

import java.util.Collection;

public interface IRefCounter {

    /**
     * 
     * @param resourceId
     */
    void increaseRef(String resourceId);

    /**
     * 
     * @param resourceId
     */
    void decreaseRef(String resourceId);

    /**
     * 
     * @param resourceId
     * @return
     */
    int getRefCount(String resourceId);

    /**
     * Returns all references ever increased by this counter (some of which may
     * possibly have no counts).
     * 
     * @return
     */
    Collection<String> getRefs();

    /**
     * Returns all references each of which has at least one count.
     * 
     * @return
     */
    Collection<String> getCountedRefs();

}