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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * @author Frank Shaka
 */
public abstract class AbstractPositionSolver implements IPositionSolver {

    private static final IBoundsProvider DEFAULT_BOUNDS_PROVIDER = new ReferencedFigureBoundsProvider();

    private static final Collection<Object> EMPTY_KEYS = Collections.emptySet();

    private Point origin = new Point();

    private Map<String, Set<Object>> keys = new HashMap<String, Set<Object>>();

    private Map<Object, String> categories = new HashMap<Object, String>();

    private Map<Object, Point> initPositions = new HashMap<Object, Point>();

    private Map<Object, Point> solvedPositions = new HashMap<Object, Point>();

    private Map<String, IBoundsProvider> generalBoundsProviders = new HashMap<String, IBoundsProvider>();

    private Map<Object, IBoundsProvider> boundsProvideres = new HashMap<Object, IBoundsProvider>();

    private IBoundsProvider defaultBoundsProvider = DEFAULT_BOUNDS_PROVIDER;

    /**
     * @see cn.brainy.gef.draw2d.figure.IPositionSolver#clear()
     */
    public void clear() {
        keys.clear();
        categories.clear();
        initPositions.clear();
        solvedPositions.clear();
        boundsProvideres.clear();
    }

    public IBoundsProvider getBoundsProvider(Object key) {
        IBoundsProvider boundsProvider = boundsProvideres.get(key);
        if (boundsProvider == null)
            return getGeneralBoundsProvider(getCategory(key));
        return boundsProvider;
    }

    public IBoundsProvider getGeneralBoundsProvider(String category) {
        IBoundsProvider boundsProvider = generalBoundsProviders.get(category);
        if (boundsProvider == null)
            return getDefaultBoundsProvider();
        return boundsProvider;
    }

    public IBoundsProvider getDefaultBoundsProvider() {
        return defaultBoundsProvider;
    }

    public String getCategory(Object key) {
        return categories.get(key);
    }

    public Point getInitPosition(Object key) {
        return initPositions.get(key);
    }

    public Point getSolvedPosition(Object key) {
        return solvedPositions.get(key);
    }

    public Collection<String> getCategories() {
        return keys.keySet();
    }

    public Collection<Object> getKeys() {
        return categories.keySet();
    }

    public Collection<Object> getKeys(String category) {
        Set<Object> set = keys.get(category);
        if (set == null)
            return EMPTY_KEYS;
        return set;
    }

    public void recordInitPosition(Object key, Point position, String category,
            boolean output) {
        if (output) {
            initPositions.put(key, position.getCopy());
            solvedPositions.put(key, position);
        } else {
            initPositions.put(key, position);
            solvedPositions.put(key, position.getCopy());
        }
        categories.put(key, category);
        Set<Object> set = keys.get(category);
        if (set == null) {
            set = new HashSet<Object>();
            keys.put(category, set);
        }
        set.add(key);
    }

    public void setGeneralBoundsProvider(String category,
            IBoundsProvider boundsProvider) {
        generalBoundsProviders.put(category, boundsProvider);
    }

    public void setBoundsProvider(Object key, IBoundsProvider boundsProvider) {
        boundsProvideres.put(key, boundsProvider);
    }

    public void setDefaultBoundsProvider(IBoundsProvider boundsProvider) {
        this.defaultBoundsProvider = boundsProvider == null ? DEFAULT_BOUNDS_PROVIDER
                : boundsProvider;
    }

    public Rectangle getSolvedBounds(Object key) {
        return getPrefBounds(key, getSolvedPosition(key));
    }

    protected Rectangle getPrefBounds(Object key, Point position) {
        return getBoundsProvider(key).getPrefBounds(key, position);
    }

    /**
     * @see cn.brainy.gef.draw2d.figure.IPositionSolver#getOrigin()
     */
    public Point getOrigin() {
        return origin;
    }

    /**
     * @see cn.brainy.gef.draw2d.figure.IPositionSolver#setOrigin(org.eclipse.draw2d.geometry.Point)
     */
    public void setOrigin(Point origin) {
        this.origin.setLocation(origin);
    }

    public void setOrigin(int x, int y) {
        this.origin.setLocation(x, y);
    }
}