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
package org.xmind.core;

/**
 * An interface for an adaptable object.
 * <p>
 * Adaptable objects can be dynamically extended to provide different interfaces
 * (or "adapters"). Workbooks and workbook components implement this interface
 * to provide additional functionalities specific to their implementations.
 * </p>
 * For example,
 * 
 * <pre>
 *     IAdaptable a = [some adaptable];
 *     IFoo x = a.getAdapter(IFoo.class);
 *     if (x != null)
 *         [do IFoo things with x]
 * </pre>
 */
public interface IAdaptable {

    /**
     * Returns an object which is an instance of the given class associated with
     * this object. Returns <code>null</code> if no such object can be found.
     *
     * @param adapter
     *            the adapter class to look up
     * @return a object of the given class, or <code>null</code> if this object
     *         does not have an adapter for the given class
     */
    <T> T getAdapter(Class<T> adapter);

}