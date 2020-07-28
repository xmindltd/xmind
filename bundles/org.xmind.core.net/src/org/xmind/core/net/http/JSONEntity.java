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

import org.json.JSONObject;
import org.xmind.core.net.internal.EncodingUtils;

/**
 * @author Frank Shaka
 * @since 3.6.50
 */
public class JSONEntity extends HttpEntity {

    public static final String CONTENT_TYPE_JSON = "application/json; charset=utf-8"; //$NON-NLS-1$

    private JSONObject object;

    private boolean pretty;

    private String json;

    /**
     * 
     */
    public JSONEntity(JSONObject object) {
        this(object, false);
    }

    /**
     * 
     */
    public JSONEntity(JSONObject object, boolean pretty) {
        this.object = object;
        this.pretty = pretty;
        this.json = null;
    }

    /**
     * @return the json
     */
    public String getJSON() {
        if (json == null) {
            if (pretty) {
                json = object.toString(4);
            } else {
                json = object.toString();
            }
        }
        return json;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.net.http.HttpEntity#getContentType()
     */
    @Override
    public String getContentType() {
        return CONTENT_TYPE_JSON;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.net.http.HttpEntity#getContentLength()
     */
    @Override
    public long getContentLength() {
        return EncodingUtils.toDefaultBytes(getJSON()).length;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.net.http.HttpEntity#writeTo(java.io.OutputStream)
     */
    @Override
    public void writeTo(OutputStream output) throws IOException {
        output.write(EncodingUtils.toDefaultBytes(getJSON()));
    }

}
