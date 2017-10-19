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
import java.util.List;

import org.xmind.core.CoreException;
import org.xmind.core.IAdaptable;
import org.xmind.core.io.IInputSource;

public interface IMarkerSheet extends IAdaptable {

    List<IMarkerGroup> getMarkerGroups();

    IMarkerGroup createMarkerGroup(boolean singleton);

    IMarker createMarker(String resourcePath);

    IMarkerGroup createMarkerGroupById(String groupId);

    IMarker createMarkerById(String markerId, String resourcePath);

    void addMarkerGroup(IMarkerGroup group);

    void removeMarkerGroup(IMarkerGroup group);

    void setParentSheet(IMarkerSheet parent);

    IMarkerSheet getParentSheet();

    /**
     * Gets the marker group with the given group ID within this marker sheet.
     * 
     * @param groupId
     *            the group ID
     * @return the marker group with the given group ID, or <code>null</code> if
     *         no such marker group is found in this marker sheet
     */
    IMarkerGroup getMarkerGroup(String groupId);

    /**
     * Gets the marker with the given marker ID within this marker sheet.
     * 
     * @param markerId
     *            the marker ID
     * @return the marker with the given marker ID, or <code>null</code> if no
     *         such marker is found in this marker sheet
     */
    IMarker getMarker(String markerId);

    /**
     * Finds the marker group with the given group ID in this marker sheet and
     * all its parents.
     * 
     * @param groupId
     *            the group ID
     * @return the marker group with the given group ID, or <code>null</code> if
     *         no such marker group is found in this marker sheet and all its
     *         parents.
     */
    IMarkerGroup findMarkerGroup(String groupId);

    /**
     * Finds the marker with the given marker ID in this marker sheet and all
     * its parents.
     * 
     * @param markerId
     *            the marker ID
     * @return the marker with the given marker ID, or <code>null</code> if no
     *         such marker is found in this marker sheet and all its parents.
     */
    IMarker findMarker(String markerId);

    /**
     * Returns whether this marker sheet contains at least one marker group.
     * 
     * @return <code>true</code> if no marker groups exist, or
     *         <code>false</code> otherwise
     */
    boolean isEmpty();

    /**
     * Returns whether this marker sheet's content is permanent that will not
     * change over time. A permanent sheet's markers can always be referenced
     * and need not to be copied to another marker sheet.
     * 
     * @return <code>true</code> if this marker sheet is permanent, or
     *         <code>false</code> otherwise
     */
    boolean isPermanent();

    /**
     * <p>
     * <b>WARNING:</b> This method is for internal use only! Clients should use
     * serialization methods to save a marker sheet.
     * </p>
     * 
     * @param out
     * @throws IOException
     * @throws CoreException
     */
    @Deprecated
    void save(OutputStream out) throws IOException, CoreException;

    /**
     * <p>
     * <b>WARNING:</b> This method is for internal use only! Don't call it!
     * </p>
     * 
     * @param source
     * @throws IOException
     * @throws CoreException
     */
    @Deprecated
    void importFrom(IInputSource source) throws IOException, CoreException;

    /**
     * <p>
     * <b>WARNING:</b> This method is for internal use only! Don't call it!
     * </p>
     * 
     * @param source
     * @param groupName
     * @throws IOException
     * @throws CoreException
     */
    @Deprecated
    void importFrom(IInputSource source, String groupName)
            throws IOException, CoreException;

    /**
     * <p>
     * <b>WARNING:</b> This method is for internal use only! Don't call it!
     * </p>
     * 
     * @param sourcePath
     * @throws IOException
     * @throws CoreException
     */
    @Deprecated
    void importFrom(String sourcePath) throws IOException, CoreException;

    /**
     * <p>
     * <b>WARNING:</b> This method is for internal use only! Don't call it!
     * </p>
     * 
     * @param sheet
     */
    @Deprecated
    void importFrom(IMarkerSheet sheet);

    /**
     * <p>
     * <b>WARNING:</b> This method is for internal use only! Don't call it!
     * </p>
     * 
     * @param group
     * @return
     */
    @Deprecated
    IMarkerGroup importGroup(IMarkerGroup group);

    /**
     * Allocates a new marker resource path storing the given data.
     * 
     * @param source
     * @param suggestedPath
     * @return
     * @throws IOException
     * @throws UnsupportedOperationException
     */
    String allocateMarkerResource(InputStream source, String suggestedPath)
            throws IOException;

}
