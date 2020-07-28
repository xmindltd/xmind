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

import org.xmind.core.IAdaptable;
import org.xmind.core.IIdentifiable;
import org.xmind.core.INamed;

/**
 * A marker represents meaningful information by a specific visual object, e.g.
 * an image.
 * 
 * @author Frank Shaka
 *
 */
public interface IMarker extends IAdaptable, IIdentifiable, INamed {

    /**
     * Returns the path to this marker's resource. This path is used by the
     * marker and its owner marker sheet to provide a readable (or possibly
     * writable) {@link IMarkerResource} object.
     * 
     * @return a {@link String} representing the resource path
     * @see #getResource()
     */
    String getResourcePath();

    /**
     * Returns a handle object to read or write resource content of this marker.
     * The returned resource may be targeted at the path as returned by
     * {@link #getResourcePath()}.
     * 
     * @return an object to read/write resource content, or <code>null</code> if
     *         there's no resource available (e.g. no
     *         {@link IMarkerResourceProvider} available in the marker sheet)
     * @see #getResourcePath()
     */
    IMarkerResource getResource();

    /**
     * Returns the SVG path of this marker.
     * 
     * @return an SVG path, or <code>null</code> if none
     */
    public String getSVGPath();

    /**
     * Returns the parent group of this marker.
     * 
     * @return the parent group, or <code>null</code> if the marker is not added
     *         to any group yet or has been removed from a group
     */
    IMarkerGroup getParent();

    /**
     * Returns the marker sheet who creates and owns this marker.
     * 
     * @return the owner marker sheet
     */
    IMarkerSheet getOwnedSheet();

    /**
     * Tests whether this marker should be hidden from user interface.
     * 
     * @return <code>true</code> to indicate that this marker should be hidden,
     *         or <code>false</code> otherwise
     */
    boolean isHidden();

    /**
     * Sets whether this marker should be hidden from user interface.
     * 
     * @param hidden
     *            <code>true</code> to indicate that this marker should be
     *            hidden, or <code>false</code> otherwise
     */
    void setHidden(boolean hidden);

}
