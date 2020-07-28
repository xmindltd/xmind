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
/**
 * 
 */
package org.xmind.core.command.remote;

/**
 * This class identifies a command service.
 * 
 * @author Frank Shaka
 */
public interface IIdentifier {

    /**
     * Returns the domain id of this identifier.
     * 
     * @return the domain id of this identifier
     */
    String getDomain();

    /**
     * Returns the unique name of this identifier. This name should be unique
     * within the specific domain returned by {@link #getDomain()}.
     * 
     * @return the unique name of this identifier, never <code>null</code>
     */
    String getName();

    /**
     * Returns a string representing this identifier. Different identifiers
     * should return different strings.
     * 
     * @return a string representing this identifier
     */
    String toString();

    /**
     * Tests whether this identifier equals another object.
     * 
     * @param obj
     *            the object to test against
     * @return <code>true</code> is the specified object is equal to this
     *         identifier, or <code>false</code> otherwise
     */
    boolean equals(Object obj);

}
