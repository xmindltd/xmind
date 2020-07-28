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

package org.xmind.core;

import java.util.Set;

/**
 * The repository of resource revisions.
 * 
 * @author Frank Shaka &lt;frank@xmind.net&gt;
 */
public interface IRevisionRepository extends IAdaptable, IWorkbookComponent {

    /**
     * Gets the revision manager with corresponding resource ID. If no revision
     * manager is related to the specified resource ID, a new one will be
     * created with the specified content type.
     * 
     * @param resourceId
     *            the ID of the resource
     * @param contentType
     *            the content type of the resource, see {@link IRevision} for
     *            all available content types
     * @return the corresponding revision manager with the specified resource ID
     * @throws IllegalArgumentException
     *             if <code>resourceId</code> is null
     * @see IRevision
     * @see IRevisionManager
     */
    IRevisionManager getRevisionManager(String resourceId, String contentType);

    /**
     * Gets the revision manager with corresponding resource ID. If no revision
     * manager is related to the specified resource ID, <code>null</code> will
     * be returned.
     * 
     * @param resourceId
     *            the ID of the resource
     * @return the registered revision manager with the specified resource ID,
     *         or <code>null</code> if not found
     * @throws IllegalArgumentException
     *             if <code>resourceId</code> is null
     * @see IRevision
     * @see IRevisionManager
     */
    IRevisionManager getRegisteredRevisionManager(String resourceId);

    /**
     * Replaces the current revision manager of the specified resource with the
     * given one. If <code>null</code> is used as the new revision manager,
     * there will be no revision manager registered related to the specified
     * resource, and a new revision manager will be created upon next call to
     * {@link #getRevisionManager(String, String)}. Removed revision manager
     * will not be saved.
     * 
     * @param resourceId
     *            the ID of a resource
     * @param manager
     *            the new revision manager to use for the specified resource, or
     *            <code>null</code> to remove the current one
     * @throws IllegalArgumentException
     *             if <code>resourceId</code> is null, or <code>manager</code>
     *             belongs to another workbook or can not be recognized
     * @see IRevision
     * @see IRevisionManager
     */
    void setRevisionManager(String resourceId, IRevisionManager manager);

    /**
     * Returns a set of resource IDs that has corresponding revision managers
     * registered. If no revision manager is found, an empty set will be
     * returned. If a resource's revision manager has been set to
     * <code>null</code> using
     * {@link #setRevisionManager(String, IRevisionManager)}, that resource's ID
     * will not be included.
     * 
     * @return a set of resource IDs
     * @see IRevisionManager
     */
    Set<String> getRegisteredResourceIds();

}
