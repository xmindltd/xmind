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

import java.util.HashMap;
import java.util.Map;

import org.xmind.core.IFileEntry;
import org.xmind.core.internal.AbstractRefCounter;
import org.xmind.core.internal.zip.ArchiveConstants;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.util.IMarkerRefCounter;

public class WorkbookMarkerRefCounter extends AbstractRefCounter
        implements IMarkerRefCounter {

    private WorkbookImpl workbook;

    private Map<IMarker, IMarkerGroup> groupCache;

    WorkbookMarkerRefCounter(WorkbookImpl workbook) {
        this.workbook = workbook;
        this.groupCache = new HashMap<IMarker, IMarkerGroup>();
    }

    protected Object findResource(String resourceId) {
        return workbook.getMarkerSheet().findMarker(resourceId);
    }

    protected void postIncreaseRef(String resourceId, Object resource) {
        IMarker marker = (IMarker) resource;

        MarkerSheetImpl markerSheet = workbook.getMarkerSheet();
        if (markerSheet.equals(marker.getOwnedSheet())) {
            String resourcePath = marker.getResourcePath();
            if (resourcePath != null) {
                IFileEntry entry = workbook.getManifest().getFileEntry(
                        ArchiveConstants.PATH_MARKERS + resourcePath);
                if (entry != null) {
                    entry.increaseReference();
                }
            }

            IMarkerGroup group = marker.getParent();
            if (group == null) {
                group = groupCache.get(marker);
                if (group != null)
                    group.addMarker(marker);
            }
            if (group != null) {
                groupCache.put(marker, group);
                if (group.getParent() == null) {
                    markerSheet.addMarkerGroup(group);
                }
            }
        }
    }

    protected void postDecreaseRef(String resourceId, Object resource) {
        IMarker marker = (IMarker) resource;
        MarkerSheetImpl markerSheet = workbook.getMarkerSheet();
        if (markerSheet.equals(marker.getOwnedSheet())) {
            String resourcePath = marker.getResourcePath();
            if (resourcePath != null) {
                IFileEntry entry = workbook.getManifest().getFileEntry(
                        ArchiveConstants.PATH_MARKERS + resourcePath);
                if (entry != null) {
                    entry.decreaseReference();
                }
            }

            IMarkerGroup group = marker.getParent();
            if (group != null) {
                groupCache.put(marker, group);
                if (getRefCount(resourceId) <= 0) {
                    group.removeMarker(marker);
                }
                if (group.isEmpty() && markerSheet.equals(group.getParent())) {
                    markerSheet.removeMarkerGroup(group);
                }
            }
        }
    }

}