/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
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
/**
 * 
 */
package org.xmind.core.net.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xmind.core.net.internal.EncodingUtils;

/**
 * @author Frank Shaka
 * @since 3.6.50
 */
public class HttpResponse implements IResponseHandler {

    private byte[] bytes = null;

    public void handleResponseEntity(IProgressMonitor monitor,
            HttpRequest request, HttpEntity entity)
                    throws InterruptedException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            entity.writeTo(output);
        } finally {
            try {
                output.close();
            } catch (IOException e) {
            }
        }
        bytes = output.toByteArray();
    }

    /**
     * 
     * @return a string as the response, or <code>null</code> if no response is
     *         received
     */
    public String asString() {
        if (bytes == null)
            return null;
        return EncodingUtils.toDefaultString(bytes);
    }

    /**
     * 
     * @return a JSON object as the response, or <code>null</code> if no valid
     *         JSON object is available
     */
    public JSONObject asJSONObject() {
        if (bytes == null)
            return null;
        ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        try {
            return new JSONObject(new JSONTokener(input));
        } catch (JSONException e) {
            // not a valid JSON object
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException e) {
            }
        }
    }

}
