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
package org.xmind.core.command.remote.socket;

import org.xmind.core.command.remote.CommandServiceInfo;
import org.xmind.core.command.remote.ICommandServiceInfo;

/**
 * Command service info used by a socket command server.
 * 
 * @author Frank Shaka
 */
public class SocketCommandServiceInfo extends CommandServiceInfo {

    private ISocketAddress address;

    /**
     * 
     */
    public SocketCommandServiceInfo() {
        super();
    }

    /**
     * @param source
     */
    public SocketCommandServiceInfo(ICommandServiceInfo source) {
        super(source);
        if (source instanceof SocketCommandServiceInfo) {
            this.address = ((SocketCommandServiceInfo) source).address;
        }
    }

    @SuppressWarnings("rawtypes")
    public Object getAdapter(Class adapter) {
        if (adapter == ISocketAddress.class)
            return address;
        return super.getAdapter(adapter);
    }

    public ISocketAddress getAddress() {
        return address;
    }

    public void setAddress(ISocketAddress address) {
        this.address = address;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SocketCommandServiceInfo{id="); //$NON-NLS-1$
        sb.append(getId());
        sb.append(",name="); //$NON-NLS-1$
        sb.append(getName());
        sb.append(",address="); //$NON-NLS-1$
        sb.append(address);
        sb.append(",metadata="); //$NON-NLS-1$
        sb.append(getMetadataImpl());
        sb.append('}');
        return sb.toString();
    }
}
