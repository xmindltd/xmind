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
package org.xmind.core.marker;

import java.util.List;

import org.xmind.core.IAdaptable;
import org.xmind.core.IIdentifiable;
import org.xmind.core.INamed;

public interface IMarkerGroup extends IAdaptable, IIdentifiable, INamed {

    List<IMarker> getMarkers();

    boolean isEmpty();

    IMarker getMarker(String markerId);

    boolean isSingleton();

    void setSingleton(boolean singleton);

    boolean isHidden();

    void setHidden(boolean hidden);

    /**
     * Returns the marker sheet containing this group.
     * <p>
     * If this is <code>null</code>, it means that this group has not been added
     * to the owner sheet.
     * </p>
     * 
     * @return The marker sheet containing this group
     */
    IMarkerSheet getParent();

    /**
     * Returns the marker sheet owning this group.
     * <p>
     * This is never <code>null</code>.
     * </p>
     * 
     * @return The marker sheet owning this group
     */
    IMarkerSheet getOwnedSheet();

    void addMarker(IMarker marker);

    void removeMarker(IMarker marker);

}