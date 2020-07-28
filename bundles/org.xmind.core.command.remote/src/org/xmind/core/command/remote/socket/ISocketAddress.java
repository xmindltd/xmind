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
package org.xmind.core.command.remote.socket;

/**
 * A lightweight representation of a socket address.
 * 
 * @author Frank Shaka
 * 
 */
public interface ISocketAddress {

    /**
     * Returns the host name of this socket address.
     * 
     * @return the host name of this socket address
     */
    String getHostName();

    /**
     * Returns the port number of this socket address.
     * 
     * @return the port number of this socket address
     */
    int getPort();

}
