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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.AssertionFailedException;

/**
 * A simple implementation of {@link ISocketAddress}.
 * 
 * @author Frank Shaka
 * 
 */
public class SocketAddress implements ISocketAddress {

    /**
     * The host name.
     */
    private String hostName;

    /**
     * The port number.
     */
    private int port;

    /**
     * Constructs a new socket address object.
     * 
     * @param hostName
     *            the host name
     * @param port
     *            the port number
     * @throws AssertionFailedException
     *             if hostName is <code>null</code>
     */
    public SocketAddress(String hostName, int port) {
        Assert.isNotNull(hostName);
        this.hostName = hostName;
        this.port = port;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return String.format("%s:%s", hostName, port); //$NON-NLS-1$
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof SocketAddress))
            return false;
        SocketAddress that = (SocketAddress) obj;
        return this.hostName.equals(that.hostName) && this.port == that.port;
    }

    @Override
    public int hashCode() {
        return hostName.hashCode() ^ port;
    }

}
