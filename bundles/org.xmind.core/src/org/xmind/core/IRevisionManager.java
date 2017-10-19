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

package org.xmind.core;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * The manager of all revisions related to a resource.
 * 
 * @author Frank Shaka &lt;frank@xmind.net&gt;
 * 
 */
public interface IRevisionManager extends IWorkbookComponent, IAdaptable {

    /**
     * Gets corresponding resource ID.
     * 
     * @return the ID of the corresponding resource
     */
    String getResourceId();

    /**
     * Gets the content type of the corresponding resource. See
     * {@link IRevision} for all available content types.
     * 
     * @return the content type of the corresponding resource
     * @see IRevision
     */
    String getContentType();

    /**
     * Gets a list of all revisions. The list is sorted by revision's number so
     * that the revision with the smallest number is in the front and the one
     * with the largest number in the end.
     * 
     * @return a list of revisions
     */
    List<IRevision> getRevisions();

    /**
     * Gets a list of all revisions. The list is sorted by revision's number in
     * reversed order so that the revision with the largest number is in the
     * front and the one with the smallest number in the end.
     * 
     * @return a list of revision in reversed order
     */
    List<IRevision> getRevisionsReversed();

    /**
     * Gets an iterator of all revisions. The revisions in the iterator is
     * returned in the order of revision numbers, so that the revision with the
     * smallest number is returned first and the one with the largest number is
     * returned last.
     * 
     * @return an iterator of revisions
     */
    Iterator<IRevision> iterRevisions();

    /**
     * Gets an iterator of all revisions. The revisions in the iterator is
     * returned in the reversed order of revision numbers, so that the revision
     * with the largest number is returned first and the one with the smallest
     * number is returned last.
     * 
     * @return an iterator of revisions in reversed order
     */
    Iterator<IRevision> iterRevisionsReversed();

    /**
     * Gets the revision by the specified revision number.
     * 
     * @param number
     *            the number of the returned revision
     * @return the revision with the specified revision number, or
     *         <code>null</code> if not found
     */
    IRevision getRevision(int number);

    /**
     * Gets the revision with the largest revision number.
     * 
     * @return the latest revision
     */
    IRevision getLatestRevision();

    /**
     * Gets the next revision number to be assigned.
     * 
     * <p>
     * Note that this number may be different from the size of the list returned
     * by {@link IRevisionManager#getRevisions()}. Removed revisions will not be
     * present in the list, but its revision number is taken and will not be
     * assigned to another revision. For example, assume that there have been 6
     * revisions in this manager, the next revision number will be 7 even if the
     * 6th revision is removed.
     * </p>
     * 
     * @return
     */
    int getNextRevisionNumber();

    /**
     * Creates a snapshot derived from the specified content and add it as a new
     * revision into this manager. If the content is regarded the same as the
     * content of the latest revision, it will not create any new revision and
     * simply return <code>null</code>.
     * 
     * @param content
     *            the content to make snapshot
     * @return the newly added revision, or <code>null</code> if the content
     *         equals the latest revision
     */
    IRevision addRevision(IAdaptable content) throws IOException, CoreException;

    /**
     * Removes the specified revision from this manager.
     * 
     * @param revision
     *            the revision to remove
     * @return an object used to restore this revision into this workbook, or
     *         <code>null</code> if the revision has already been deleted or not
     *         found.
     * @throws IllegalArgumentException
     *             if the revision is <code>null</code> or not owned by the same
     *             workbook
     */
    Object removeRevision(IRevision revision);

    /**
     * Restores a previously removed revision into this manager.
     * 
     * @param revision
     *            the revision to restore
     * @param removal
     *            an object returned by <code>remove()</code> method
     * @throws IllegalArgumentException
     *             if the revision is <code>null</code>, or the <em>removal</em>
     *             object is invalid
     */
    void restoreRevision(IRevision revision, Object removal);

    /**
     * Determines if this manager has any revisions.
     * 
     * @return <code>true</code> if the this manager has at least one revision,
     *         <code>false</code> otherwise
     */
    boolean hasRevisions();

}
