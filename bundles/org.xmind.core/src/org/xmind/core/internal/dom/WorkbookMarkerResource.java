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

import org.xmind.core.IFileEntry;
import org.xmind.core.internal.zip.ArchiveConstants;
import org.xmind.core.marker.AbstractMarkerResource;
import org.xmind.core.marker.IMarker;

public class WorkbookMarkerResource extends AbstractMarkerResource {

    private WorkbookImpl workbook;

    public WorkbookMarkerResource(WorkbookImpl workbook, IMarker marker) {
        super(marker, ArchiveConstants.PATH_MARKERS);
        this.workbook = workbook;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public InputStream getInputStream() {
        IFileEntry entry = workbook.getManifest().getFileEntry(getFullPath());
        return entry == null ? null : entry.getInputStream();
    }

    /**
     * @deprecated
     */
    @Deprecated
    public OutputStream getOutputStream() {
        IFileEntry entry = workbook.getManifest()
                .createFileEntry(getFullPath());
        return entry.getOutputStream();
    }

    public InputStream openInputStream() throws IOException {
        IFileEntry entry = workbook.getManifest().getFileEntry(getFullPath());
        if (entry == null)
            throw new FileNotFoundException();
        return entry.openInputStream();
    }

    public OutputStream openOutputStream() throws IOException {
        IFileEntry entry = workbook.getManifest()
                .createFileEntry(getFullPath());
        return entry.openOutputStream();
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof WorkbookMarkerResource))
            return false;
        WorkbookMarkerResource that = (WorkbookMarkerResource) obj;
        return this.workbook.equals(that.workbook) && super.equals(obj);
    }

}