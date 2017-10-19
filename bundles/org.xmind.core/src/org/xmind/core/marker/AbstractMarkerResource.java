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
package org.xmind.core.marker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMarkerResource implements IMarkerResource {

    private String mainPath;

    private IMarker marker;

    private List<IMarkerVariation> variations = null;

    public AbstractMarkerResource(IMarker marker) {
        this(marker, null);
    }

    public AbstractMarkerResource(IMarker marker, String mainPath) {
        if (marker == null)
            throw new IllegalArgumentException();
        this.marker = marker;
        this.mainPath = mainPath == null ? "" : mainPath; //$NON-NLS-1$
    }

    protected IMarker getMarker() {
        return marker;
    }

    public String getPath() {
        return marker.getResourcePath();
    }

    public String getFullPath() {
        String path = getPath();
        if (!path.startsWith("/")) { //$NON-NLS-1$
            path = getMainPath() + path;
        }
        return path;
    }

    protected String getMainPath() {
        return mainPath;
    }

    public synchronized List<IMarkerVariation> getVariations() {
        if (variations == null) {
            variations = new ArrayList<IMarkerVariation>();
            loadVariations(variations);
        }
        return variations;
    }

    protected void loadVariations(List<IMarkerVariation> variations) {
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.marker.IMarkerResource#getInputStream()
     */
    public InputStream getInputStream() {
        try {
            return openInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.marker.IMarkerResource#getOutputStream()
     */
    @Deprecated
    public OutputStream getOutputStream() {
        try {
            return openOutputStream();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * @deprecated
     */
    @Deprecated
    public InputStream getInputStream(IMarkerVariation variation) {
        return getInputStream();
    }

    /**
     * @deprecated
     */
    @Deprecated
    public OutputStream getOutputStream(IMarkerVariation variation) {
        return getOutputStream();
    }

    public InputStream openInputStream(IMarkerVariation variation)
            throws IOException {
        return openInputStream();
    }

    public InputStream openInputStream(int zoom) throws IOException {
        return openInputStream();
    }

    public InputStream openInputStream(IMarkerVariation variation, int zoom)
            throws IOException {
        return openInputStream();
    }

    @Deprecated
    public OutputStream openOutputStream(IMarkerVariation variation)
            throws IOException {
        return openOutputStream();
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof AbstractMarkerResource))
            return false;
        AbstractMarkerResource that = (AbstractMarkerResource) obj;
        return this.marker.equals(that.marker);
    }
}
