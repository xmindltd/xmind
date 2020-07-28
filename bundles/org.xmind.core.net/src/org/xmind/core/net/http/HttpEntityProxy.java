/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
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
package org.xmind.core.net.http;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Frank Shaka
 * @since 3.6.50
 */
public class HttpEntityProxy extends HttpEntity {

    private HttpEntity entity;

    private String customContentType;

    /**
     * 
     */
    public HttpEntityProxy(HttpEntity entity, String customContentType) {
        this.entity = entity;
        this.customContentType = customContentType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.net.http.HttpEntity#getContentLength()
     */
    @Override
    public long getContentLength() {
        return entity.getContentLength();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.net.http.HttpEntity#writeTo(java.io.OutputStream)
     */
    @Override
    public void writeTo(OutputStream output) throws IOException {
        entity.writeTo(output);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.net.http.HttpEntity#getContentType()
     */
    @Override
    public String getContentType() {
        if (customContentType != null)
            return customContentType;
        return entity.getContentType();
    }

}
