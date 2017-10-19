/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
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
/**
 * 
 */
package org.xmind.core;

import java.util.Set;

/**
 * This interface provides abilities to manage comments within a workbook.
 * Instances of this interface should also provide a functionality that
 * automatically adds (or removes) comments whose associated object is added to
 * (or removed from) the owner workbook, so that no redundant comments exist.
 * 
 * @author Frank Shaka
 * @since 3.6.50
 */
public interface ICommentManager extends IAdaptable, IWorkbookComponent {

    /**
     * Creates a new comment with basic attributes. The created comment instance
     * is stored in memory only and has not been added to workbook yet (i.e.
     * <code>comment.isOrphan()</code> returns <code>true</code>). This method
     * associates the created comment with the specified object id regardless of
     * its existence or orphanhood.
     * 
     * @param author
     *            the author name
     * @param time
     *            the creation time (may be
     *            <code>System.currentTimeMillis()</code>)
     * @param objectId
     *            the id of the object to be associated with
     * @return a new comment
     * @throws IllegalArgumentException
     *             if the author is <code>null</code>, time is less than or
     *             equals to 0, or the object id is <code>null</code>
     */
    IComment createComment(String author, long time, String objectId);

    /**
     * Adds a comment into the owner workbook. Has no effect if the comment is
     * already added.
     * 
     * <p>
     * Note that, if the comment's associated object is an orphan or does not
     * exist, this operation will be remembered and suppressed, and the comment
     * will not be included in the results of {@link #getComments(String)} or
     * {@link #getAllComments()}, until the associated object becomes part of
     * the workbook. Pending comments will not be saved with workbook.
     * </p>
     * 
     * @param comment
     *            the comment to be added
     * @throws IllegalArgumentException
     *             if the comment is not owned by the same workbook
     */
    void addComment(IComment comment);

    /**
     * Removes a comment from the owner workbook. Has no effect if the comment
     * is already removed.
     * 
     * <p>
     * Note that, if the comment's associated object is an orphan or does not
     * exist, this operation is still remembered and the comment will not be
     * included in results of {@link #getComments(String)} or
     * {@link #getAllComments()} even though the associated object becomes part
     * of the workbook again.
     * </p>
     * 
     * @param comment
     *            the comment to be removed
     * @throws IllegalArgumentException
     *             if the comment is not owned by the same workbook
     */
    void removeComment(IComment comment);

    /**
     * Returns a set of comments that are associated with the same object id. An
     * empty set will be returned if the associated object is an orphan or does
     * not exist.
     * 
     * @param objectId
     *            the id of the associated object
     * @return a set of comments (never <code>null</code>)
     * @throws IllegalArgumentException
     *             if the object id is <code>null</code>
     */
    Set<IComment> getComments(String objectId);

    /**
     * Tests whether this workbook contains any comment that is associated with
     * a specific object. The result should be the same with
     * <code>!getComments(objectId).isEmpty()</code>.
     * 
     * @param objectId
     * @return <code>true</code> if such comments exist, or <code>false</code>
     *         otherwise
     * @see #getComments(String)
     * @throws IllegalArgumentException
     *             if the object id is <code>null</code>
     */
    boolean hasComments(String objectId);

    /**
     * Returns a set of all comments in this workbook. Note that comments whose
     * associated objects are orphans will be excluded.
     * 
     * @return a set of all comments (never <code>null</code>)
     */
    Set<IComment> getAllComments();

    /**
     * Tests whether this workbook contains no comment. The result should be
     * same with <code>getAllComments().isEmpty()</code>.
     * 
     * @return <code>true</code> if no comments exist, or <code>false</code>
     *         otherwise
     * @see #getAllComments()
     */
    boolean isEmpty();

}
