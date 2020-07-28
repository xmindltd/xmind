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

/**
 * This interface represents an object that can accept a string as its title
 * text.
 * 
 * @author Brian Sun
 * @author Frank Shaka
 */
public interface ITitled {

    /**
     * Sets the title text of this object to given one. If the given string is
     * <code>null</code>, the object's title text will be cleared.
     * 
     * @param titleText
     *            the new title text to set
     */
    void setTitleText(String titleText);

    /**
     * Returns the title text of this object. If this object does not have a
     * title text, an empty string is returned.
     * 
     * @return the title text, never <code>null</code>
     */
    String getTitleText();

    /**
     * Returns whether this object has a valid title text.
     * 
     * @return <code>true</code> if this object has a valid title text, or
     *         <code>false</code> otherwise
     */
    boolean hasTitle();

}