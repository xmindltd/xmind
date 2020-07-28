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
package org.xmind.core.command.remote;

/**
 * A listener notified when remote command service is discovered or dropped.
 * Clients may implement this interface.
 * 
 * @author Frank Shaka
 */
public interface IRemoteCommandServiceListener {

    /**
     * Called when a remote command service has been discovered. Note that this
     * method may not be called in the UI thread.
     * 
     * @param service
     *            the discovered remote command service
     */
    void remoteCommandServiceDiscovered(IRemoteCommandService service);

    /**
     * Called when a remote command service has been dropped. Note that this
     * method may not be called in the UI thread.
     * 
     * @param service
     *            the dropped remote command service
     */
    void remoteCommandServiceDropped(IRemoteCommandService service);

}
