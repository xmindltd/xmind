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
package org.xmind.ui.internal.editor;

/**
 * This interface represents an object that accepts a password to encrypt its
 * content.
 * 
 * @author Frank Shaka
 * @since 3.6.50
 */
public interface IEncryptable {

    String getPasswordHint();

    String getPassword();

    /**
     * Sets the password to be the given one.
     * 
     * @param newPassword
     *            a {@link String} of the new password, or <code>null</code> to
     *            indicate that no encryption should be used
     */
    void setPassword(String newPassword);

    /**
     * Sets the password to be the given one with hint message.
     *
     * @param hintMessage
     *            a {@link String} of the password hint message
     */
    void setPasswordHint(String hintMessage);

    /**
     * Tests whether the given password equals the one this object has.
     * 
     * @param passwordToTest
     *            a {@link String} of the password to test, or <code>null</code>
     *            to test if this object has no password
     * @return <code>true</code> if the given password equals the one this
     *         object has, or <code>false</code> otherwise
     */
    boolean testsPassword(String passwordToTest);

    /**
     * Tests whether this object has a non-<code>null</code> password.
     * 
     * @return <code>true</code> if this object has a password, or
     *         <code>false</code> otherwise
     */
    boolean hasPassword();

}
