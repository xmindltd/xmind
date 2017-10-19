/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
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
package org.xmind.core.internal.dom;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.xmind.core.io.IInputSource;
import org.xmind.core.io.IOutputTarget;
import org.xmind.core.marker.AbstractMarkerResource;
import org.xmind.core.marker.IMarker;

public class MarkerResource extends AbstractMarkerResource {

    private IInputSource source;

    private IOutputTarget target;

    public MarkerResource(IMarker marker, IInputSource source,
            IOutputTarget target) {
        super(marker);
        this.source = source;
        this.target = target;
    }

    public InputStream openInputStream() throws IOException {
        if (source == null)
            throw new FileNotFoundException(getFullPath());
        return source.openEntryStream(getFullPath());
    }

    public OutputStream openOutputStream() throws IOException {
        if (target == null)
            throw new FileNotFoundException(getFullPath());
        return target.openEntryStream(getFullPath());
    }

}