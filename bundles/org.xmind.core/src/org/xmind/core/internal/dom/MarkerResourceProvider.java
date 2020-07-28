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
package org.xmind.core.internal.dom;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.xmind.core.Core;
import org.xmind.core.io.IInputSource;
import org.xmind.core.io.IOutputTarget;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerResource;
import org.xmind.core.marker.IMarkerResourceAllocator;
import org.xmind.core.marker.IMarkerResourceProvider;
import org.xmind.core.util.FileUtils;

public class MarkerResourceProvider
        implements IMarkerResourceProvider, IMarkerResourceAllocator {

    private IInputSource source;

    private IOutputTarget target;

    private boolean permanent;

    public MarkerResourceProvider(IInputSource source, IOutputTarget target) {
        this(source, target, false);
    }

    public MarkerResourceProvider(IInputSource source, IOutputTarget target,
            boolean permanent) {
        this.source = source;
        this.target = target;
        this.permanent = permanent;
    }

    public IMarkerResource getMarkerResource(IMarker marker) {
        if ("".equals(marker.getResourcePath())) //$NON-NLS-1$
            return null;
        return new MarkerResource(marker, source, target);
    }

    public boolean isPermanent() {
        return permanent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.marker.IMarkerResourceAllocator#allocateMarkerResourcePath
     * (java.io.InputStream, java.lang.String)
     */
    public String allocateMarkerResource(InputStream input,
            String suggestedPath) throws IOException {
        if (target == null)
            return null;

        String ext = suggestedPath == null ? ".png" //$NON-NLS-1$
                : FileUtils.getExtension(suggestedPath);
        String path = Core.getIdFactory().createId() + ext;
        if (!target.isEntryAvaialble(path))
            return null;

        OutputStream output = target.openEntryStream(path);
        try {
            FileUtils.transfer(input, output, false);
        } finally {
            output.close();
        }
        return path;
    }

}