/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
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
 * This class is used to create new serializer instances.
 * 
 * @author Frank Shaka
 * @since 3.6.2
 */
public interface ISerializerFactory {

    /**
     * Creates a new serializer instance.
     * 
     * @return a new serializer instance (never <code>null</code>)
     */
    ISerializer newSerializer();

}
