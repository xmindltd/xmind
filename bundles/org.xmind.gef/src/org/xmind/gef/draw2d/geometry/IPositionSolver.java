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
package org.xmind.gef.draw2d.geometry;

import java.util.Collection;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.IOriginBased2;

/**
 * @author Frank Shaka
 */
public interface IPositionSolver extends IOriginBased2 {

    void clear();

    /**
     * 
     * @param key
     * @param position
     * @param category
     * @param output
     *            <code>true</code> to use the given point object as output,
     *            <code>false</code> to create a new point object as output.
     */
    void recordInitPosition(Object key, Point position, String category,
            boolean output);

    void solve();

    Collection<Object> getKeys(String category);

    Collection<String> getCategories();

    Collection<Object> getKeys();

    Point getInitPosition(Object key);

    Point getSolvedPosition(Object key);

    String getCategory(Object key);

    Rectangle getSolvedBounds(Object key);

    void setBoundsProvider(Object key, IBoundsProvider boundsProvider);

    IBoundsProvider getBoundsProvider(Object key);

    void setGeneralBoundsProvider(String category,
            IBoundsProvider boundsProvider);

    IBoundsProvider getGeneralBoundsProvider(String category);

    void setDefaultBoundsProvider(IBoundsProvider boundsProvider);

    IBoundsProvider getDefaultBoundsProvider();

}