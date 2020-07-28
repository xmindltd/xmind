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

import java.util.List;

/**
 * @author briansun
 * 
 */
public interface ITopicPath {

    /**
     * Returns the elements along the path from the owned workbook to the topic.
     * The order is:
     * <p>
     * <blockquote><code>[workbook, sheet, root-topic, ..., topic]</code></blockquote>
     * </p>
     * 
     * @return Elements along this path.
     */
    List<Object> toList();

    /**
     * Returns all topics along the path from the root-topic to the topic. The
     * order is:
     * <p>
     * <blockquote><code>[root-topic, ..., parent, this topic]</code></blockquote>
     * </p>
     * 
     * @return All topics along this path.
     */
    List<ITopic> toTopicList();

    /**
     * Returns the owned workbook of all topics along this path.
     * 
     * @return The owned workbook of all topics along this path.
     */
    IWorkbook getWorkbook();

    /**
     * Returns the owned sheet of all topics along this path.
     * 
     * @return The owned sheet of all topics along this path.
     */
    ISheet getSheet();

    /**
     * Returns the root topic in this path.
     * 
     * @return The root topic in this path.
     */
    ITopic getRootTopic();

    /**
     * Determines whether the specified topic is along this path (including this
     * topic itself).
     * 
     * @param topic
     *            The topic to determine.
     * @return <code>true</code> if the specified topic is along this path;
     *         <code>false</code> otherwise.
     */
    boolean contains(ITopic topic);

    /**
     * Determines whether the specified topic is one of the ancestors of this
     * topic (NOT including this topic itself).
     * 
     * @param ancestor
     *            The topic to determine.
     * @return <code>true</code> if the specified topic is one of the
     *         ancestors of this topic; <code>false</code> otherwise.
     */
    boolean isDescendentOf(ITopic ancestor);

}