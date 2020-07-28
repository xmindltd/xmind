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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.xmind.core.util.DOMUtils;

public class ResourceGroup {

    private Element implementation;

    private ResourceMappingManager manager;

    private List<ResourceMapping> items;

    private Map<String, String> srcToDest;

    private Map<String, String> destToSrc;

    protected ResourceGroup(Element implementation,
            ResourceMappingManager manager) {
        this.implementation = implementation;
        this.manager = manager;
    }

    public Element getImplementation() {
        return implementation;
    }

    public ResourceMappingManager getManager() {
        return manager;
    }

    public String getApplicationId() {
        return manager.getApplicationId();
    }

    public String getDestination(String source) {
        ensureLoaded();
        return srcToDest.get(source);
    }

    public String getSource(String destination) {
        ensureLoaded();
        return destToSrc.get(destination);
    }

    public String getType() {
        return DOMUtils.getAttribute(implementation,
                ResourceMappingConstants.ATT_TYPE);
    }

    public List<ResourceMapping> getItems() {
        ensureLoaded();
        return items;
    }

    private void ensureLoaded() {
        if (items != null && srcToDest != null && destToSrc != null)
            return;
        lazyLoad();
        if (items == null)
            items = Collections.emptyList();
        if (srcToDest == null)
            srcToDest = Collections.emptyMap();
        if (destToSrc == null)
            destToSrc = Collections.emptyMap();
    }

    private void lazyLoad() {
        Iterator<Element> it = DOMUtils.childElementIterByTag(implementation,
                ResourceMappingConstants.TAG_TRANSFER_MAPPING);
        while (it.hasNext()) {
            createTransferItem(it.next());
        }
    }

    private void createTransferItem(Element element) {
        ResourceMapping item = new ResourceMapping(element, this);
        if (items == null)
            items = new ArrayList<ResourceMapping>();
        items.add(item);
        String src = item.getSource();
        String dest = item.getDestination();
        if (src != null) {
            if (srcToDest == null)
                srcToDest = new HashMap<String, String>();
            srcToDest.put(src, dest);
        }
        if (dest != null) {
            if (destToSrc == null)
                destToSrc = new HashMap<String, String>();
            destToSrc.put(dest, src);
        }
    }

}