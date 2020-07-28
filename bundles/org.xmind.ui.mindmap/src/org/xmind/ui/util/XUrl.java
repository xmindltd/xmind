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
package org.xmind.ui.util;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author briansun
 */
public class XUrl {

    private URL url = null;
    private Map<String, String> params = Collections.emptyMap();

    /**
     * 
     */
    private XUrl(URL url) {
        this.url = url;

        if (url.getQuery() != null) {
            params = new HashMap<String, String>();
            for (String s : url.getQuery().split("\\&")) { //$NON-NLS-1$
                int index = s.indexOf('=');
                if (index > 1) {
                    params.put(decode(s.substring(0, index)), decode(s
                            .substring(index + 1, s.length())));
                }
            }
        }
    }

    /**
     * @param urlString
     * @return
     */
    public static XUrl createUrl(String urlString) {
        URL u = null;
        try {
            u = new URL(urlString);
        } catch (MalformedURLException e) {
            return null;
        }
        return u == null ? null : new XUrl(u);
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, "utf-8"); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @return the url
     */
    public URL toUrl() {
        return url;
    }

    /**
     * @return the params
     */
    public Map<String, String> getParams() {
        return params;
    }

    /**
     * @param key
     * @return
     */
    public String getParamValue(String key) {
        return params.get(key);
    }

    /**
     * @return
     * @see java.net.URL#getAuthority()
     */
    public String getAuthority() {
        return url.getAuthority();
    }

    /**
     * @return
     * @see java.net.URL#getFile()
     */
    public String getFile() {
        return url.getFile();
    }

    /**
     * @return
     * @see java.net.URL#getHost()
     */
    public String getHost() {
        return url.getHost();
    }

    /**
     * @return
     * @see java.net.URL#getPath()
     */
    public String getPath() {
        return url.getPath();
    }

    /**
     * @return
     * @see java.net.URL#getPort()
     */
    public int getPort() {
        return url.getPort();
    }

    /**
     * @return
     * @see java.net.URL#getProtocol()
     */
    public String getProtocol() {
        return url.getProtocol();
    }

    /**
     * @return
     * @see java.net.URL#getQuery()
     */
    public String getQuery() {
        return url.getQuery();
    }

    /**
     * @return
     * @see java.net.URL#getRef()
     */
    public String getRef() {
        return url.getRef();
    }

    /**
     * @return
     * @see java.net.URL#getUserInfo()
     */
    public String getUserInfo() {
        return url.getUserInfo();
    }

    /**
     * @return
     * @see java.net.URL#toExternalForm()
     */
    public String toExternalForm() {
        return url.toExternalForm();
    }

    /**
     * @return
     * @see java.net.URL#toString()
     */
    @Override
    public String toString() {
        return url.toString();
    }

    /**
     * @return
     * @throws URISyntaxException
     * @see java.net.URL#toURI()
     */
    public URI toURI() throws URISyntaxException {
        return url.toURI();
    }

}