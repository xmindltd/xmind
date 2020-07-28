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

import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerSheet;

public abstract class MarkerSheet implements IMarkerSheet {

    private IMarkerSheet parent = null;

    public <T> T getAdapter(Class<T> adapter) {
        return null;
    }

    public boolean isEmpty() {
        return getMarkerGroups().isEmpty();
    }

    public void setParentSheet(IMarkerSheet parent) {
        this.parent = parent;
    }

    public IMarkerSheet getParentSheet() {
        return parent;
    }

    public IMarker findMarker(String markerId) {
        IMarker marker = getLocalMarker(markerId);
        if (marker != null)
            return marker;
        if (parent != null)
            return parent.findMarker(markerId);
        return null;
    }

    public IMarker getMarker(String markerId) {
        return getLocalMarker(markerId);
    }

    protected abstract IMarker getLocalMarker(String markerId);

    public IMarkerGroup findMarkerGroup(String groupId) {
        IMarkerGroup group = getLocalMarkerGroup(groupId);
        if (group != null)
            return group;
        if (parent != null)
            return parent.findMarkerGroup(groupId);
        return null;
    }

    public IMarkerGroup getMarkerGroup(String groupId) {
        return getLocalMarkerGroup(groupId);
    }

    protected abstract IMarkerGroup getLocalMarkerGroup(String groupId);

}