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
package org.xmind.core.io;

import org.w3c.dom.Element;
import org.xmind.core.util.DOMUtils;

public class ResourceMapping {

    private Element implementation;

    private ResourceGroup list;

    protected ResourceMapping(Element implementation, ResourceGroup list) {
        this.implementation = implementation;
        this.list = list;
    }

    public Element getImplementation() {
        return implementation;
    }

    public ResourceGroup getList() {
        return list;
    }

    public ResourceMappingManager getManager() {
        return list.getManager();
    }

    public String getType() {
        return list.getType();
    }

    public String getApplicationId() {
        return getManager().getApplicationId();
    }

    public String getSource() {
        return DOMUtils.getAttribute(implementation,
                ResourceMappingConstants.ATT_SRC);
    }

    public String getDestination() {
        return DOMUtils.getAttribute(implementation,
                ResourceMappingConstants.ATT_DEST);
    }

}