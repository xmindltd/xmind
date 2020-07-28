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
 * A command service domain represents a specific range of access to command
 * services, and provides a local command server and a remote command service
 * discoverer for building/registering a command server and searching for remote
 * command servers within this range. For example, a LAN command service domain
 * may provide a command server that binds itself on a local server socket and
 * use Zeroconf's DNS Service Discovery as the implementor of the remote command
 * service discoverer for searching for remote command services within the local
 * area network.
 * 
 * <p>
 * Connections to this domain should be made before the local command server and
 * remote command service discoverer can return accurate results.
 * </p>
 * 
 * @author Frank Shaka
 */
public interface ICommandServiceDomain {

    /**
     * Returns the unique identifier of this domain.
     * 
     * @return the unqieu identifier of this domain
     */
    String getId();

    /**
     * Returns the display name of this domain.
     * 
     * @return the display name of this domain
     */
    String getName();

    /**
     * Returns the director of this command service domain. The director is
     * responsible for controlling the lifecycle of this domain.
     * 
     * @return the director of this command service domain, never
     *         <code>null</code>
     */
    ICommandServiceDomainDirector getDirector();

    /**
     * Returns the command server that will be deployed on the local machine.
     * The command server is responsible for handling command requests sent from
     * remote clients. The information of this command server will be advertised
     * across this domain by an advertiser.
     * 
     * @return the command server, never <code>null</code>
     */
    ICommandServer getCommandServer();

    /**
     * Returns the advertiser for this domain's local command server. The
     * advertiser is responsible for broadcasting the information of the local
     * command server across the domain and registering it as a remote command
     * service within this domain.
     * 
     * @return the command server advertiser, never <code>null</code>
     */
    ICommandServerAdvertiser getCommandServerAdvertiser();

    /**
     * Returns the remote command service discoverer for this domain. The
     * discoverer is responsible for searching for available remote command
     * services and provide clients methods to access them.
     * 
     * @return the remote command service discoverer, never <code>null</code>
     */
    IRemoteCommandServiceDiscoverer getRemoteCommandServiceDiscoverer();

}
