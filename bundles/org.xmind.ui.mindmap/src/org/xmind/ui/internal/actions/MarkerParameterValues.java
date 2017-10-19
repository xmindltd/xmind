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
package org.xmind.ui.internal.actions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.IParameterValues;
import org.eclipse.osgi.util.NLS;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.mindmap.MindMapUI;

public final class MarkerParameterValues implements IParameterValues {

    public Map getParameterValues() {
        Map<String, String> map = new HashMap<String, String>();
        IMarkerSheet ms = MindMapUI.getResourceManager().getSystemMarkerSheet();
        for (IMarkerGroup mg : ms.getMarkerGroups()) {
            for (IMarker m : mg.getMarkers()) {
                map.put(makeName(mg, m), m.getId());
            }
        }
        return map;
    }

    private String makeName(IMarkerGroup group, IMarker marker) {
        String markerName = marker.getName();
        String groupName = group.getName();
        return NLS.bind(MindMapMessages.MarkerParameterNamePattern, groupName,
                markerName);
    }

}