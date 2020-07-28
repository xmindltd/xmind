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
import org.xmind.core.IFileEntry;
import org.xmind.core.internal.zip.ArchiveConstants;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerResource;
import org.xmind.core.marker.IMarkerResourceAllocator;
import org.xmind.core.marker.IMarkerResourceProvider;
import org.xmind.core.util.FileUtils;

public class WorkbookMarkerResourceProvider
        implements IMarkerResourceProvider, IMarkerResourceAllocator {

    private WorkbookImpl workbook;

    public WorkbookMarkerResourceProvider(WorkbookImpl workbook) {
        this.workbook = workbook;
    }

    public IMarkerResource getMarkerResource(IMarker marker) {
        if ("".equals(marker.getResourcePath())) //$NON-NLS-1$
            return null;
        return new WorkbookMarkerResource(workbook, marker);
    }

    public boolean isPermanent() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.marker.IMarkerResourceAllocator#allocateMarkerResourcePath
     * (java.io.InputStream, java.lang.String)
     */
    public String allocateMarkerResource(InputStream source,
            String suggestedPath) throws IOException {
        String ext = suggestedPath == null ? ".png" //$NON-NLS-1$
                : FileUtils.getExtension(suggestedPath);
        String path = Core.getIdFactory().createId() + ext;
        IFileEntry entry = workbook.getManifest()
                .createFileEntry(ArchiveConstants.PATH_MARKERS + path);
        OutputStream target = entry.openOutputStream();
        try {
            FileUtils.transfer(source, target, false);
        } finally {
            target.close();
        }
        return path;
    }

}