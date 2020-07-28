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
package org.xmind.core.marker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface IMarkerResource {

    String getPath();

    InputStream openInputStream() throws IOException;

    List<IMarkerVariation> getVariations();

    InputStream openInputStream(IMarkerVariation variation) throws IOException;

    InputStream openInputStream(int zoom) throws IOException;

    InputStream openInputStream(IMarkerVariation variation, int zoom)
            throws IOException;

    /**
     * @deprecated Use
     *             {@link IMarkerSheet#allocateMarkerResource(InputStream, String)}
     * @return
     * @throws IOException
     */
    @Deprecated
    OutputStream openOutputStream() throws IOException;

    /**
     * @deprecated Use
     *             {@link IMarkerSheet#allocateMarkerResource(InputStream, String)}
     * @param variation
     * @return
     * @throws IOException
     */
    @Deprecated
    OutputStream openOutputStream(IMarkerVariation variation)
            throws IOException;

    /**
     * @deprecated Use {@link #openInputStream()}
     */
    @Deprecated
    InputStream getInputStream();

    /**
     * @deprecated Use {@link #openOutputStream()}
     */
    @Deprecated
    OutputStream getOutputStream();

    /**
     * @deprecated Use {@link #openInputStream(IMarkerVariation)}
     */
    @Deprecated
    InputStream getInputStream(IMarkerVariation variation);

    /**
     * @deprecated Use {@link #openOutputStream(IMarkerVariation)}
     */
    @Deprecated
    OutputStream getOutputStream(IMarkerVariation variation);

}
