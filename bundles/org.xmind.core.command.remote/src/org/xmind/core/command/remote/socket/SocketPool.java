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

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * A socket pool maintains a list of socket objects and provides a method to
 * clear/close all maintained sockets.
 * 
 * @author Frank Shaka
 */
public class SocketPool {

    private List<Socket> sockets = new ArrayList<Socket>();

    public SocketPool() {
    }

    /**
     * Registers a socket.
     * 
     * @param socket
     */
    public synchronized void addSocket(Socket socket) {
        sockets.add(socket);
    }

    /**
     * Unregisters a socket.
     * 
     * @param socket
     */
    public synchronized void removeSocket(Socket socket) {
        sockets.remove(socket);
    }

    /**
     * Clears and closes all registered sockets.
     */
    public synchronized void clear() {
        Object[] theSockets = sockets.toArray();
        sockets.clear();
        for (int i = 0; i < theSockets.length; i++) {
            Socket socket = ((Socket) theSockets[i]);
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // ignore close exceptions
                }
            }
        }
    }

}
