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
 * The header info of a license key.
 * 
 * @author Frank Shaka
 * @since 3.6.51
 */
public interface ILicenseKeyHeader {

    /**
     * The identifying marker of a XMind product license key (value is
     * <code>"X"</code> ).
     */
    String IDENTIFYING_MARKER = "X"; //$NON-NLS-1$

    /**
     * The type of a XMind Pro license key (value is <code>"A"</code>).
     * 
     * @see #getLicenseType()
     */
    String TYPE_PRO = "A"; //$NON-NLS-1$

    /**
     * The type of a XMind Plus license key (value is <code>"B"</code>).
     * 
     * @see #getLicenseType()
     */
    String TYPE_PLUS = "B"; //$NON-NLS-1$

    /**
     * The type of a XMind Pro VLE license key (value is <code>"C"</code>).
     * 
     * @see #getLicenseType()
     */
    String TYPE_VINDY = "C"; //$NON-NLS-1$

    /**
     * The type of individual customers (value is <code>"I"</code>).
     * 
     * @see #getLicenseeType()
     */
    String LICENSEE_INDIVIDUAL = "I"; //$NON-NLS-1$

    /**
     * The type of educational/non-profit organizations (value is
     * <code>"E"</code>).
     * 
     * @see #getLicenseeType()
     */
    String LICENSEE_EDU = "E"; //$NON-NLS-1$

    /**
     * The type of govenment organizations (value is <code>"G"</code>).
     * 
     * @see #getLicenseeType()
     */
    String LICENSEE_GOV = "G"; //$NON-NLS-1$

    /**
     * The type of families (value is <code>"F"</code>).
     * 
     * @see #getLicenseeType()
     */
    String LICENSEE_FAMILY = "F"; //$NON-NLS-1$

    /**
     * The type of small teams with 5 members (value is <code>"T"</code>).
     * 
     * @see #getLicenseeType()
     */
    String LICENSEE_TEAM_5U = "T"; //$NON-NLS-1$

    /**
     * The type of small teams with 10 members (value is <code>"1"</code>).
     * 
     * @see #getLicenseeType()
     */
    String LICENSEE_TEAM_10U = "1"; //$NON-NLS-1$

    /**
     * The type of small teams with 20 members (value is <code>"2"</code>).
     * 
     * @see #getLicenseeType()
     */
    String LICENSEE_TEAM_20U = "2"; //$NON-NLS-1$

    /**
     * The type of VLE customers (value is <code>"V"</code>).
     * 
     * @see #getLicenseeType()
     */
    String LICENSEE_VLE = "V"; //$NON-NLS-1$

    /**
     * Returns the type of this license key.
     * 
     * @return the identifier of license key type
     * @see #TYPE_PRO
     * @see #TYPE_PLUS
     * @see #TYPE_VINDY
     */
    String getLicenseType();

    /**
     * Returns the code name of the XMind product vendor that this license key
     * was generated for.
     * 
     * @return the code name of XMind product vendor
     */
    String getVendorName();

    /**
     * Returns the major part of the XMind product version number bound to this
     * license key.
     * 
     * @return the major version number
     */
    int getMajorVersionNumber();

    /**
     * Returns the minor part of the XMind product version number bound to this
     * license key.
     * 
     * @return the minor version number
     */
    int getMinorVersionNumber();

    /**
     * Returns the type of the entity that is licensed to.
     * 
     * @return the type of the licensee
     * 
     * @see #LICENSEE_INDIVIDUAL
     * @see #LICENSEE_EDU
     * @see #LICENSEE_GOV
     * @see #LICENSEE_FAMILY
     * @see #LICENSEE_TEAM_5U
     * @see #LICENSEE_TEAM_10U
     * @see #LICENSEE_TEAM_20U
     * @see #LICENSEE_VLE
     */
    String getLicenseeType();

    /**
     * Returns the number of years within which this license key can be upgraded
     * to the latest version.
     * 
     * @return a positive number of years of free version upgrade, or
     *         <code>0</code> to indicate no free version upgrade is available
     *         for this license key
     */
    int getYearsOfUpgrade();

    /**
     * Returns the 12-character string representing this license key header. See
     * {@link ILicenseKeyHeader} for detailed description of how all information
     * is encoded.
     * 
     * <p>
     * A license key header contains 12 characters which have the following
     * meanings:
     * </p>
     * 
     * <ol>
     * <li>Characters 1 is an <em>identifying marker</em>, <code>"X"</code>,
     * indicating that following characters describe a license key used to
     * activate an XMind product</li>
     * <li>Character 2 is the <em>type</em> of the license key</li>
     * <li>Characters 3-6 specifies the XMind product this license key is
     * generated against:
     * <ul>
     * <li>Characters 3 &amp; 4 = the code name of the XMind product vendor</li>
     * <li>Characters 5 = the <em>major version number</em> of XMind product
     * (Base36 encoded)</li>
     * <li>Characters 6 = the <em>minor version number</em> of XMind product
     * (Base36 encoded)</li>
     * </ul>
     * </li>
     * <li>Character 7 is the <em>type of the licensee</em></li>
     * <li>Character 8 &amp; 9 = the <em>years of free version upgrade</em>
     * applied to this license key (Base36 encoded, left padded with 0)</li>
     * <li>Character 10 is the <em>free months number of the license</em></li>
     * <li>Character 11 &amp; 12 = (reserved for future use, currently randomly
     * generated and Base36 encoded)</li>
     * </ol>
     * 
     * <p>
     * Each <em>type</em> of a license key is bound to a unique method (i.e.
     * secure key pair) to verify the license key body.
     * </p>
     * 
     * <p>
     * Product info may not be checked restrictly but may be prompted if
     * verification fails.
     * </p>
     * 
     * <p>
     * It's gauranteed that same license key headers are encoded as the same
     * string and different headers as different strings. That is to say, if two
     * license key headers are same (or different) as determined by
     * {@link #equals(Object)}, they are encoded as the same (or different)
     * strings, and vice versa.
     * </p>
     * 
     * @return the encoded 12-character string of the license key
     */
    String toEncoded();

}
