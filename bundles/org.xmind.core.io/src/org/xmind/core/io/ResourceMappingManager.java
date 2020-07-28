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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmind.core.util.DOMUtils;
import org.xml.sax.SAXException;

public class ResourceMappingManager {

    private String applicationId;

    private Document implementation;

    private List<ResourceGroup> groups;

    private Map<String, ResourceGroup> map;

    private ResourceMappingManager(Document implementation) {
        this.implementation = implementation;
        this.applicationId = DOMUtils.getAttribute(
                implementation.getDocumentElement(),
                ResourceMappingConstants.ATT_APPLICATION_ID);
    }

    private ResourceMappingManager(String applicationId) {
        this.implementation = null;
        this.applicationId = applicationId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public List<ResourceGroup> getGroups() {
        ensureLoaded();
        return groups;
    }

    public ResourceGroup getGroup(String groupName) {
        ensureLoaded();
        return map.get(groupName);
    }

    public String getDestination(String groupName, String source) {
        ResourceGroup list = getGroup(groupName);
        if (list != null)
            return list.getDestination(source);
        return null;
    }

    public String getSource(String groupName, String destination) {
        ResourceGroup list = getGroup(groupName);
        if (list != null)
            return list.getSource(destination);
        return null;
    }

    private void ensureLoaded() {
        if (groups != null && map != null)
            return;
        lazyLoad();
        if (groups == null)
            groups = Collections.emptyList();
        if (map == null)
            map = Collections.emptyMap();
    }

    private void lazyLoad() {
        if (implementation == null)
            return;

        Iterator<Element> it = DOMUtils.childElementIterByTag(
                implementation.getDocumentElement(),
                ResourceMappingConstants.TAG_RESOURCE_GROUP);
        while (it.hasNext()) {
            createResourceGorup(it.next());
        }
    }

    private void createResourceGorup(Element element) {
        ResourceGroup list = new ResourceGroup(element, this);
        if (groups == null)
            groups = new ArrayList<ResourceGroup>();
        groups.add(list);
        String type = list.getType();
        if (type != null) {
            if (map == null)
                map = new HashMap<String, ResourceGroup>();
            map.put(type, list);
        }
    }

    public static ResourceMappingManager createEmptyInstance(
            String applicationId) {
        return new ResourceMappingManager(applicationId);
    }

    public static ResourceMappingManager createInstance(InputStream stream)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder builder = DOMUtils.getDefaultDocumentBuilder();
        try {
            Document document = builder.parse(stream);
            return new ResourceMappingManager(document);
        } finally {
            try {
                stream.close();
            } catch (IOException ignore) {
            }
        }
    }

    public static ResourceMappingManager createInstance(URL url)
            throws ParserConfigurationException, SAXException, IOException {
        InputStream stream = url.openStream();
        return createInstance(stream);
    }

    public static ResourceMappingManager createInstance(Class<?> clazz,
            String name) throws ParserConfigurationException, SAXException,
            IOException {
        InputStream stream = clazz.getResourceAsStream(name);
        if (stream == null)
            throw new FileNotFoundException();
        return createInstance(stream);
    }
}