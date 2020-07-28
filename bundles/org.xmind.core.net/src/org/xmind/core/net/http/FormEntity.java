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

import static org.xmind.core.net.internal.EncodingUtils.toAsciiBytes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

import org.xmind.core.net.Field;
import org.xmind.core.net.FieldSet;
import org.xmind.core.net.internal.EncodingUtils;

/**
 * 
 * @author Frank Shaka
 * @since 3.6.50
 */
public class FormEntity extends HttpEntity {

    private static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded; charset=utf-8"; //$NON-NLS-1$

    private FieldSet parameters;

    /**
     * 
     */
    public FormEntity(FieldSet parameters) {
        this.parameters = parameters;
    }

    /**
     * @return the fields
     */
    public FieldSet getParameters() {
        return parameters;
    }

    private byte[] formData = null;

    private byte[] getFormData() {
        if (formData != null)
            return formData;
        formData = toAsciiBytes(toQueryString(parameters.toList()));
        return formData;
    }

    public String getContentType() {
        return FORM_CONTENT_TYPE;
    }

    public long getContentLength() {
        return getFormData().length;
    }

    public void writeTo(OutputStream stream) throws IOException {
        stream.write(getFormData());
    }

    private static String toQueryString(Collection<Field> parameters) {
        StringBuffer buffer = new StringBuffer(parameters.size() * 15);
        for (Field param : parameters) {
            if (buffer.length() > 0) {
                buffer.append('&');
            }
            buffer.append(param.getEncodedName());
            buffer.append('=');
            buffer.append(param.getEncodedValue());
        }
        return buffer.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            writeTo(bytes);
        } catch (IOException e) {
            throw new AssertionError(
                    "Failed to dump form data using byte array stream", e); //$NON-NLS-1$
        } finally {
            try {
                bytes.close();
            } catch (IOException e) {
            }
        }
        try {
            return bytes.toString(EncodingUtils.DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw EncodingUtils.wrapEncodingException(e,
                    EncodingUtils.DEFAULT_ENCODING);
        }
    }

}