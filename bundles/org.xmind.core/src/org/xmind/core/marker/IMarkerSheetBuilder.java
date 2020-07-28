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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.xmind.core.CoreException;
import org.xmind.core.util.IPropertiesProvider;

public interface IMarkerSheetBuilder {

    /**
     * 
     * @param resourceProvider
     * @return
     */
    IMarkerSheet createMarkerSheet(IMarkerResourceProvider resourceProvider);

    /**
     * NOTE: The input stream will NOT be closed after loading.
     * 
     * @param stream
     * @param resourceProvider
     * @return
     * @throws IOException
     * @throws CoreException
     */
    IMarkerSheet loadFromStream(InputStream stream,
            IMarkerResourceProvider resourceProvider) throws IOException,
            CoreException;

    IMarkerSheet loadFromFile(File file,
            IMarkerResourceProvider resourceProvider) throws IOException,
            CoreException;

    IMarkerSheet loadFromPath(String path,
            IMarkerResourceProvider resourceProvider) throws IOException,
            CoreException;

    IMarkerSheet loadFromURL(URL url, IMarkerResourceProvider resourceProvider)
            throws IOException, CoreException;

    /**
     * NOTE: The input stream will NOT be closed after loading.
     * 
     * @param stream
     * @param sheet
     * @throws IOException
     * @throws CoreException
     * @deprecated Use {@link IPropertiesProvider} adapted from this marker
     *             sheet
     */
    void loadProperties(InputStream stream, IMarkerSheet sheet)
            throws IOException, CoreException;

}