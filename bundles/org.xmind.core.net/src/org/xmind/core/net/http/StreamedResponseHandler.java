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

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Frank Shaka
 * @since 3.6.50
 */
public abstract class StreamedResponseHandler implements IResponseHandler {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.net.http.IResponseHandler#handleResponseEntity(org.eclipse
     * .core.runtime.IProgressMonitor, org.xmind.core.net.http.HttpRequest,
     * org.xmind.core.net.http.HttpEntity)
     */
    public void handleResponseEntity(IProgressMonitor monitor,
            HttpRequest request, HttpEntity entity)
                    throws InterruptedException, IOException {
        OutputStream output = openOutputStream(monitor, request);
        try {
            entity.writeTo(output);
        } finally {
            output.close();
        }
    }

    protected abstract OutputStream openOutputStream(IProgressMonitor monitor,
            HttpRequest request) throws IOException;

}
