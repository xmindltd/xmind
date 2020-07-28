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
 * @deprecated Use {@link IMeta#getValue(String)} and
 *             {@link IMeta#setValue(String, String)} instead
 * 
 * @author Frank Shaka
 */
public interface IMetaData extends IAdaptable {

    /**
     * 
     * @return
     */
    String getKey();

    /**
     * 
     * @return
     */
    String getValue();

    /**
     * 
     * @param value
     */
    void setValue(String value);

    /**
     * 
     * @param key
     * @return
     */
    String getAttribute(String key);

    /**
     * 
     * @param key
     * @param value
     */
    void setAttribute(String key, String value);

    /**
     * 
     * @param key
     * @return
     */
    IMetaData[] getMetaData(String key);

    /**
     * 
     * @param data
     */
    void addMetaData(IMetaData data);

    /**
     * 
     * @param data
     */
    void removeMetaData(IMetaData data);

    /**
     * 
     * @return
     */
    IMeta getOwnedMeta();

}