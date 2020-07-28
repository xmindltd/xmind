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

import org.xmind.core.net.Entity;

/**
 * @author Frank Shaka
 * @since 3.6.50
 */
public abstract class HttpEntity implements Entity {

    private String fileName = null;

    protected HttpEntity() {
        super();
    }

    /**
     * Returns the content type string.
     * 
     * @return the content type string
     */
    public String getContentType() {
        return "application/octet-stream"; //$NON-NLS-1$
    }

    /**
     * Returns the suggested name for this entity as a file part when used in a
     * {@link MultipartEntity}.
     * 
     * @return the suggested file name for this entity
     */
    public String getFileName() {
        return this.fileName;
    }

    /**
     * Sets the suggested file name for this entity.
     * 
     * @param fileName
     *            the name to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Returns the total length (in bytes) of this entity.
     * 
     * @return the total length of this entity
     */
    public abstract long getContentLength();

    /**
     * Writes the content of this entity into the output stream.
     */
    public abstract void writeTo(OutputStream output) throws IOException;

}
