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

/**
 * This interface represents a piece of text a user writes to comment on an
 * object. The target object MUST be available via
 * <code>getOwnedWorkbook().getElementById(getObjectId())</code>, otherwise the
 * comment will not be included in the workbook. The user who wrote a comment is
 * called the <em>author</em>, whose name will be associated with this comment.
 * 
 * @author Frank Shaka
 * @since 3.6.50
 */
public interface IComment
        extends IAdaptable, IWorkbookComponent, Comparable<IComment> {

    /**
     * Returns the id of the object to which this comment is associated. This
     * attribute can not be modified once this comment is created.
     * 
     * @return the object id, never <code>null</code>
     */
    String getObjectId();

    /**
     * Returns the name of the author of this comment.
     * 
     * @return the author name, never <code>null</code>
     */
    String getAuthor();

    /**
     * Returns the time (in milliseconds since Jan 1, 1970) when this comment is
     * created.
     * 
     * @return the creation time
     */
    long getTime();

    /**
     * Returns the text content of this comment. This method may return
     * <code>null</code> if the content is not set yet or be cleared out.
     * 
     * @return the text content, or <code>null</code>
     */
    String getContent();

    /**
     * Sets the text content of this comment to the specified string. If the
     * string is <code>null</code>, the original content will be cleared out.
     * 
     * @param content
     *            a {@link String} of the new content
     */
    void setContent(String content);

}
