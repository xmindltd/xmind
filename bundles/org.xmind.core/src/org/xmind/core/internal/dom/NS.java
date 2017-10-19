/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.core.internal.dom;

import org.w3c.dom.Element;

public enum NS {

    /* Internal Namespaces */
    XMAP("xmap", "urn:xmind:xmap:xmlns:content:2.0"), //$NON-NLS-1$ //$NON-NLS-2$
    Style("style", "urn:xmind:xmap:xmlns:style:2.0"), //$NON-NLS-1$ //$NON-NLS-2$
    Marker("marker", "urn:xmind:xmap:xmlns:marker:2.0"), //$NON-NLS-1$ //$NON-NLS-2$
    Meta("meta", "urn:xmind:xmap:xmlns:meta:2.0"), //$NON-NLS-1$ //$NON-NLS-2$
    Comments("comments", "urn:xmind:xmap:xmlns:comments:2.0"), //$NON-NLS-1$ //$NON-NLS-2$
    Manifest("manifest", "urn:xmind:xmap:xmlns:manifest:1.0"), //$NON-NLS-1$ //$NON-NLS-2$
    Revision("revision", "urn:xmind:xmap:xmlns:revision:1.0"), //$NON-NLS-1$ //$NON-NLS-2$
    Assignee("assignee", "urn:xmind:xmap:xmlns:assignee:1.0"), //$NON-NLS-1$//$NON-NLS-2$
    Stories("stories", "urn:xmind:xmap:xmlns:stories:1.0"), //$NON-NLS-1$//$NON-NLS-2$

    /* External Namespaces */
    Xhtml("xhtml", "http://www.w3.org/1999/xhtml"), //$NON-NLS-1$ //$NON-NLS-2$
    Xlink("xlink", "http://www.w3.org/1999/xlink"), //$NON-NLS-1$ //$NON-NLS-2$
    Fo("fo", "http://www.w3.org/1999/XSL/Format"), //$NON-NLS-1$ //$NON-NLS-2$
    SVG("svg", "http://www.w3.org/2000/svg"); //$NON-NLS-1$ //$NON-NLS-2$

    private String prefix;

    private String uri;

    private NS(String prefix, String uri) {
        this.prefix = prefix;
        this.uri = uri;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getURI() {
        return uri;
    }

    public String getQualifiedName(String localName) {
        return prefix + ":" + localName; //$NON-NLS-1$
    }

    public static String getPrefix(String qualifiedName) {
        int index = qualifiedName.indexOf(':');
        if (index >= 0) {
            return qualifiedName.substring(0, index);
        }
        return null;
    }

    public static String getLocalName(String qualifiedName) {
        int index = qualifiedName.indexOf(':');
        if (index >= 0)
            return qualifiedName.substring(index + 1);
        return qualifiedName;
    }

    public static void setNS(NS defaultNS, Element element, NS... nss) {
        if (defaultNS != null)
            element.setAttribute("xmlns", defaultNS.getURI()); //$NON-NLS-1$
        for (NS ns : nss) {
            element.setAttribute("xmlns:" + ns.getPrefix(), ns.getURI()); //$NON-NLS-1$
        }
    }

}