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

public interface IChained<T extends IChained> {

    /**
     * Gets the previous element in the chain.
     * 
     * @return
     */
    T getPrevious();

    /**
     * Gets the next element in the chain.
     * 
     * @return
     */
    T getNext();

    /**
     * Sets the previous element.
     * 
     * @param element
     */
    void setPrevious(T element);

    /**
     * Sets the next element.
     * 
     * @param element
     */
    void setNext(T element);

}
