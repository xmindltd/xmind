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
package org.xmind.core.marker;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Frank Shaka
 * @since 3.6.50
 */
public interface IMarkerResourceAllocator {

    /**
     * Allocates a new marker resource path storing the given data.
     * 
     * @param source
     * @param suggestedPath
     * @return
     * @throws IOException
     * @throws UnsupportedOperationException
     */
    String allocateMarkerResource(InputStream source, String suggestedPath)
            throws IOException;

}
