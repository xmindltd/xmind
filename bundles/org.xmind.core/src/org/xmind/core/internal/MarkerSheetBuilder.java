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
package org.xmind.core.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.xmind.core.CoreException;
import org.xmind.core.marker.IMarkerResourceProvider;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.core.marker.IMarkerSheetBuilder;

public abstract class MarkerSheetBuilder implements IMarkerSheetBuilder {

    public IMarkerSheet loadFromPath(String path,
            IMarkerResourceProvider resourceProvider) throws IOException,
            CoreException {
        return loadFromFile(new File(path), resourceProvider);
    }

    public IMarkerSheet loadFromFile(File file,
            IMarkerResourceProvider resourceProvider) throws IOException,
            CoreException {
        if (!file.exists() || file.isDirectory())
            throw new FileNotFoundException();
        FileInputStream stream = new FileInputStream(file);
        try {
            return loadFromStream(stream, resourceProvider);
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
            }
        }
    }

    public IMarkerSheet loadFromURL(URL url,
            IMarkerResourceProvider resourceProvider) throws IOException,
            CoreException {
        InputStream stream = url.openStream();
        try {
            return loadFromStream(stream, resourceProvider);
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
            }
        }
    }

}