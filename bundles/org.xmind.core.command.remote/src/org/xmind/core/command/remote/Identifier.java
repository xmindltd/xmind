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

import org.eclipse.core.runtime.Assert;

/**
 * An identifying object implementing {@link IIdentifier} interface.
 * 
 * @author Frank Shaka
 */
public class Identifier implements IIdentifier {

    private final String domain;

    private final String name;

    private final String fullName;

    /**
     * 
     */
    public Identifier(String domain, String name) {
        Assert.isNotNull(domain);
        Assert.isNotNull(name);
        this.domain = domain;
        this.name = name;
        this.fullName = domain + "/" + name; //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.command.remote.IIdentifier#getDomain()
     */
    public String getDomain() {
        return domain;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.command.remote.IIdentifier#getName()
     */
    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof Identifier))
            return false;
        Identifier that = (Identifier) obj;
        return this.domain.equals(that.domain) && this.name.equals(that.name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return fullName;
    }

}
