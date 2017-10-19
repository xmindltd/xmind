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
package org.xmind.ui.internal.mindmap;

import java.util.List;

import org.xmind.core.ILegend;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerSheet;

public class LegendItem extends ViewerModel implements Comparable<LegendItem> {

    private String markerId;

    private IMarker marker;

    public LegendItem(ILegend legend, String markerId) {
        super(LegendItemPart.class, legend);
        this.markerId = markerId;
    }

    public ILegend getLegend() {
        return (ILegend) getRealModel();
    }

    public String getMarkerId() {
        return markerId;
    }

    public IMarker getMarker() {
        if (marker == null) {
            marker = getLegend().getOwnedWorkbook().getMarkerSheet()
                    .findMarker(markerId);
        }
        return marker;
    }

    public String getDescription() {
        return getLegend().getMarkerDescription(markerId);
    }

    public int compareTo(LegendItem that) {
        if (!this.getLegend().equals(that.getLegend()))
            return 1000;

        IMarker m1 = this.getMarker();
        IMarker m2 = that.getMarker();
        if (m2 == null)
            return -1;
        if (m1 == null)
            return 1;

        IMarkerSheet s1 = m1.getOwnedSheet();
        IMarkerSheet s2 = m2.getOwnedSheet();
        if (s2 == null)
            return -100;
        if (s1 == null)
            return 100;
        if (!s1.equals(s2)) {
            if (isAncestorMarkerSheet(s1, s2))
                return -100;
            return 100;
        }
        IMarkerGroup g1 = m1.getParent();
        IMarkerGroup g2 = m2.getParent();
        if (g2 == null)
            return -10;
        if (g1 == null)
            return 10;
        if (!g1.equals(g2)) {
            List<IMarkerGroup> groups = s1.getMarkerGroups();
            int index1 = groups.indexOf(g1);
            int index2 = groups.indexOf(g2);
            if (index2 == -1)
                return -50;
            if (index1 == -1)
                return 50;
            return index1 - index2;
        }
        List<IMarker> markers = g1.getMarkers();
        int index1 = markers.indexOf(m1);
        int index2 = markers.indexOf(m2);
        if (index2 == -1)
            return -10;
        if (index1 == -1)
            return 10;
        return index1 - index2;
    }

    private boolean isAncestorMarkerSheet(IMarkerSheet s1, IMarkerSheet s2) {
        IMarkerSheet p = s2.getParentSheet();
        while (p != null) {
            if (s1.equals(p))
                return true;
            p = p.getParentSheet();
        }
        return false;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof LegendItem))
            return false;
        LegendItem that = (LegendItem) obj;
        return super.equals(obj) && this.markerId.equals(that.markerId);
    }

}