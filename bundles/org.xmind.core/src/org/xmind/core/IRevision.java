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

/**
 * A revision of the resource.
 * 
 * <p>
 * Currently only sheet resource with content type <code>IRevision.SHEET</code>
 * is supported.
 * </p>
 * 
 * @author Frank Shaka &lt;frank@xmind.net&gt;
 */
public interface IRevision extends IAdaptable, IWorkbookComponent,
        Comparable<IRevision> {

    /**
     * Resource type for a sheet resource. Value is "sheet".
     * <p>
     * Content with this type is safe to be casted into {@link ISheet} objects.
     * </p>
     */
    String SHEET = "application/vnd.xmind.sheet"; //$NON-NLS-1$

    /**
     * Gets the type of the content.
     * 
     * @return the revision content type
     * @see IRevision#SHEET
     */
    String getContentType();

    /**
     * Get the ID of the corresponding resource.
     * 
     * @return
     */
    String getResourceId();

    /**
     * Gets the number of this revision.
     * 
     * @return the revision number
     */
    int getRevisionNumber();

    /**
     * Gets the timestamp of this revision. The timestamp is represented in UNIX
     * epoch milliseconds, which marks the time when this revision is added to
     * the workbook.
     * 
     * @return the revision timestamp
     */
    long getTimestamp();

    /**
     * Gets the content of this revision. If content is not available, returns
     * <code>null</code>.
     * 
     * <p>
     * Note that the object returned by this method is a snapshot of the
     * original resource.
     * </p>
     * 
     * <p>
     * The returned object's class is determined by the resource's content type.
     * For example, an object with {@link IRevision#SHEET} as its content type
     * is safe to be casted into an {@link ISheet} object.
     * </p>
     * 
     * @return the revision content, or <code>null</code> if no content is
     *         available
     * @see IRevision#getContentType()
     */
    IAdaptable getContent();

    /**
     * Gets the manager that manages this revision.
     * 
     * @return the manager of this revision
     */
    IRevisionManager getOwnedManager();

}
