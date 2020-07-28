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

/**
 * @author Frank Shaka
 * @since 3.6.50
 */
public class HttpException extends IOException {

    /**
     * 
     */
    private static final long serialVersionUID = -7867134378055860110L;

    private int code;

    private String status;

    private HttpRequest request;

    /**
     * 
     */
    public HttpException(HttpRequest request, Throwable cause) {
        this(request, request.getStatusCode(), request.getStatusMessage(),
                cause);
    }

    /**
     * 
     */
    public HttpException(HttpRequest request, int code, String status,
            Throwable cause) {
        super(String.format("%s %s", code, status), cause); //$NON-NLS-1$
        this.request = request;
        this.code = code;
        this.status = status;
    }

    /**
     * @return the code
     */
    public int getCode() {
        return code;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @return the request
     */
    public HttpRequest getRequest() {
        return request;
    }

}
