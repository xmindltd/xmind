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
package org.xmind.core;

public interface IRange {

    int UNSPECIFIED = -1;

    /**
     * Gets the index of the starting topic of this boundary.
     * 
     * @return The index of the starting topic of this boundary, or
     *         <code>UNSPECIFIED</code> if the index is unspecified
     */
    int getStartIndex();

    /**
     * Gets the index of the ending topic of this boundary.
     * 
     * @return The index of the ending topic of this boundary, or
     *         <code>UNSPECIFIED</code> if the index is unspecified
     */
    int getEndIndex();

    /**
     * 
     * @param index
     */
    void setStartIndex(int index);

    /**
     * 
     * @param length
     */
    void setEndIndex(int index);

}