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
package org.xmind.ui.internal.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerSheet;

public class MarkerGroupContentProvider implements ITreeContentProvider {

    public Object[] getChildren(Object parentElement) {
        return new Object[0];
    }

    public Object getParent(Object element) {
        if (element instanceof IMarkerGroup)
            return ((IMarkerGroup) element).getOwnedSheet();
        if (element instanceof IMarker)
            return ((IMarker) element).getParent();
        return null;
    }

    public boolean hasChildren(Object element) {
        return element instanceof IMarkerSheet;
    }

    public Object[] getElements(Object inputElement) {
        List<Object> list = new ArrayList<Object>();
        if (inputElement instanceof IMarkerSheet) {
            for (IMarkerGroup group : ((IMarkerSheet) inputElement)
                    .getMarkerGroups()) {
                list.add(group);
            }
        } else if (inputElement instanceof IMarkerGroup) {
            for (IMarker marker : ((IMarkerGroup) inputElement).getMarkers()) {
                list.add(marker);
            }
        }
        return list.toArray();
    }

    public void dispose() {
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

}