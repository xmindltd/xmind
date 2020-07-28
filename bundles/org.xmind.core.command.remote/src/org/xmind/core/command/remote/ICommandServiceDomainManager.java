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
 * A command service domain manager provides access to command service domains.
 * 
 * <p>
 * This class can be retrieved as an OSGi service.
 * </p>
 * 
 * @author Frank Shaka
 */
public interface ICommandServiceDomainManager {

    /**
     * Returns all available command service domains.
     * 
     * @return an array of command service domains, never <code>null</code>
     */
    ICommandServiceDomain[] getCommandServiceDomains();

    /**
     * Finds a command service domain by the specific domain id.
     * 
     * @param domainId
     *            the identifier of a specified domain
     * @return the command service corresponding to the given domain id, or
     *         <code>null</code> if the domain can not be found
     */
    ICommandServiceDomain getCommandServiceDomain(String domainId);

}
