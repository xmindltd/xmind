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
package org.xmind.core.licensing;

/**
 * @author Frank Shaka
 * @since 3.6.51
 */
public interface ILicenseAgent {

    int NOT_LICENSED = 1 << 0;

    int PRO_SUBSCRIPTION = 1 << 1;
    int PRO_LICENSE_KEY = 1 << 2;
    int PLUS_LICENSE_KEY = 1 << 3;

    /**
     * Returns the type code of the license verification result. This code may
     * consists of multiple bit flags defined as static members of this
     * interface.
     * 
     * @return the type code
     */
    int getLicenseType();

    /**
     * Returns the representative name of the entity to whom the current XMind
     * Pro/Plus is licensed.
     * 
     * @return the name of the licensee entity, or <code>null</code> if not
     *         licensed
     */
    String getLicenseeName();

    /**
     * Returns a 12-character string representing the verified license key.
     * 
     * @return the license key header, or <code>null</code> if no license key is
     *         verified
     */
    ILicenseKeyHeader getLicenseKeyHeader();

    /**
     * Adds a listener for license changed events.
     * 
     * @param listener
     *            the listener to add
     */
    void addLicenseChangedListener(ILicenseChangedListener listener);

    /**
     * Removes a listener for license changed events.
     * 
     * @param listener
     *            the listener to remove
     */
    void removeLicenseChangedListener(ILicenseChangedListener listener);

}
