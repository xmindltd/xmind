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
 * A base implementation of command server advertiser.
 * 
 * @author Frank Shaka
 */
public abstract class AbstractCommandServerAdvertiser implements
        ICommandServerAdvertiser {

    private ICommandServiceDomain domain;

    private ICommandServiceInfo registeringInfo = null;

    private ICommandServiceInfo registeredInfo = null;

    /**
     * 
     */
    public AbstractCommandServerAdvertiser() {
    }

    public void init(ICommandServiceDomain domain) {
        this.domain = domain;
    }

    /**
     * @return the domain
     */
    public ICommandServiceDomain getDomain() {
        return domain;
    }

    public ICommandServiceInfo getRegisteredInfo() {
        return registeredInfo;
    }

    public void setRegisteringInfo(ICommandServiceInfo info) {
        this.registeringInfo = info;
    }

    protected void setRegisteredInfo(ICommandServiceInfo info) {
        this.registeredInfo = info;
    }

    protected ICommandServiceInfo getRegisteringInfo() {
        return this.registeringInfo;
    }

}
