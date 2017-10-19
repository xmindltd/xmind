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

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.xmind.core.marker.IMarkerRef;
import org.xmind.core.style.IStyled;

/**
 * @author briansun
 * 
 */
public interface ITopic extends ITitled, IStyled, IRelationshipEnd, IAdaptable,
        ILabeled, ITopicComponent, IPositioned, IModifiable {

    /**
     * Type for the root topic (value='root').
     * 
     * @see org.xmind.core.ITopic#getType()
     */
    public static final String ROOT = "root"; //$NON-NLS-1$

    /**
     * Type for topics directly attached to their parent topics
     * (value='attached').
     * 
     * @see org.xmind.core.ITopic#getType()
     */
    public static final String ATTACHED = "attached"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String CALLOUT = "callout"; //$NON-NLS-1$

    /**
     * Type for topics detached from their parent topics (value='detached').
     * 
     * @see org.xmind.core.ITopic#getType()
     */
    public static final String DETACHED = "detached"; //$NON-NLS-1$

    /**
     * Type for topics attached on their parent topics' summaries
     * (value='summary').
     * 
     * @see org.xmind.core.ITopic#getType()
     */
    public static final String SUMMARY = "summary"; //$NON-NLS-1$

    public static final int UNSPECIFIED = -1;

    /**
     * @return
     */
    boolean isFolded();

    /**
     * @param folded
     */
    void setFolded(boolean folded);

    /**
     * Gets the type of this topic.
     * <p>
     * A topic's <i>Type</i> is a {@link String} used to define how this topic
     * is connected to its parent topic, such as '<code>attached</code>'
     * (directly connected), '<code>detached</code>' (totally separated), or '
     * <code>summary</code>' (attached on a summary), etc. This attribute
     * matters when it is required how a topic and its parent topic are painted
     * on graphics.
     * </p>
     * <p>
     * A root topic always returns a special type 'root' since it has NO parent
     * topic.
     * </p>
     * <p>
     * If this topic has not been added to any parent topic, or its type is not
     * specified, <code>null</code> is returned.
     * </p>
     * 
     * @return The type of this topic, or <code>null</code> indicating that the
     *         type is unspecified
     * 
     * @see org.xmind.core.ITopic#ATTACHED
     * @see org.xmind.core.ITopic#DETACHED
     * @see org.xmind.core.ITopic#SUMMARY
     * @see org.xmind.core.ITopic#ROOT
     */
    String getType();

    /**
     * @return Whether this topic is directly connected to its parent topic
     * 
     * @see #getType();
     */
    boolean isAttached();

    /**
     * Gets all children topics of this topic.
     * <p>
     * <b>NOTE</b>: This list is not supposed to be modifiable. Use
     * {@link #add(ITopic)}, {@link #add(ITopic, String)},
     * {@link #add(ITopic, int, String)} or {@link #remove(ITopic)} to
     * add/remove child topic to this topic.
     * </p>
     * 
     * @return A list containing all children topics of this topic
     */
    List<ITopic> getAllChildren();

    /**
     * Returns an iterator over all children topics of this topic.
     * <p>
     * <b>NOTE</b>: This iterator is not supposed to be modifiable and calling
     * {@link Iterator#remove()} method on the returned iterator will cause an
     * {@link java.lang.UnsupportedOperationException} to be thrown. Use
     * {@link #remove(ITopic)} to remove child topic.
     * </p>
     * 
     * @return An iterator over all children topics of this topic.
     */
    Iterator<ITopic> getAllChildrenIterator();

    /**
     * Gets this topic's children topics that are of the specified type.
     * <p>
     * If this topic has NO children topics that are of this type, or the type
     * is <code>null</code>, an empty list will be returned.
     * </p>
     * <p>
     * <b>NOTE</b>: This list is not supposed to be modifiable. Use
     * {@link #add(ITopic)}, {@link #add(ITopic, String)},
     * {@link #add(ITopic, int, String)} or {@link #remove(ITopic)} to
     * add/remove child topic to this topic.
     * </p>
     * 
     * @param type
     *            The children's type
     * @return A list containing this topic's children topics that are of the
     *         specified type
     * 
     * @see #getType()
     */
    List<ITopic> getChildren(String type);

    /**
     * Returns an iterator over this topic's children topics that are of the
     * specified type.
     * 
     * <p>
     * <b>NOTE</b>: This iterator is not supposed to be modifiable and calling
     * {@link Iterator#remove()} method on the returned iterator will cause an
     * {@link java.lang.UnsupportedOperationException} to be thrown. Use
     * {@link #remove(ITopic)} to remove child topic.
     * </p>
     * 
     * @param type
     *            the children topics' type
     * @return An iterator over this topic's children topics that are of the
     *         specified type (never be <code>null</code>)
     */
    Iterator<ITopic> getChildrenIterator(String type);

    /**
     * 
     * @param type
     * @return
     */
    boolean hasChildren(String type);

    /**
     * 
     * @return
     */
    Set<String> getChildrenTypes();

    /**
     * @param child
     */
    void add(ITopic child);

    /**
     * @param child
     * @param type
     *            The child's type.
     */
    void add(ITopic child, String type);

    /**
     * @param child
     * @param index
     * @param type
     *            The child's type.
     */
    void add(ITopic child, int index, String type);

    /**
     * @param child
     */
    void remove(ITopic child);

    /**
     * @return
     */
    ITopicPath getPath();

    /**
     * @return
     */
    int getIndex();

    /**
     * @param string
     */
    void setHyperlink(String hyperlink);

    /**
     * @return
     */
    String getHyperlink();

    /**
     * @return
     */
    INotes getNotes();

    /**
     * 
     * @return
     */
    INumbering getNumbering();

    /**
     * @return
     */
    boolean isRoot();

    void addMarker(String markerId);

    void removeMarker(String markerId);

    boolean hasMarker(String markerId);

    Set<IMarkerRef> getMarkerRefs();

    void addBoundary(IBoundary boundary);

    void removeBoundary(IBoundary boundary);

    Set<IBoundary> getBoundaries();

    void addSummary(ISummary summary);

    void removeSummary(ISummary summary);

    Set<ISummary> getSummaries();

    String getStructureClass();

    void setStructureClass(String structureClass);

    IImage getImage();

    int getTitleWidth();

    void setTitleWidth(int width);

    List<ITopicExtension> getExtensions();

    ITopicExtension getExtension(String providerName);

    ITopicExtension createExtension(String providerName);

    void deleteExtension(String providerName);

    long getModifiedTime();

}