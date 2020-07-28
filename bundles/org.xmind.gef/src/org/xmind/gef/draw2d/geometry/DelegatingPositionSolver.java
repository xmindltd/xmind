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

public abstract class DelegatingPositionSolver implements IPositionSolver {

    private IPositionSolver delegate = null;

    protected IPositionSolver getDelegate() {
        if (delegate == null)
            delegate = createDelegate();
        return delegate;
    }

    protected void setDelegate(IPositionSolver delegate) {
        this.delegate = delegate;
    }

    protected abstract IPositionSolver createDelegate();

    public void solve() {
        getDelegate().solve();
    }

    public void clear() {
        getDelegate().clear();
    }

    public IBoundsProvider getBoundsProvider(Object key) {
        return getDelegate().getBoundsProvider(key);
    }

    public String getCategory(Object key) {
        return getDelegate().getCategory(key);
    }

    public IBoundsProvider getDefaultBoundsProvider() {
        return getDelegate().getDefaultBoundsProvider();
    }

    public IBoundsProvider getGeneralBoundsProvider(String category) {
        return getDelegate().getGeneralBoundsProvider(category);
    }

    public Point getInitPosition(Object key) {
        return getDelegate().getInitPosition(key);
    }

    public Collection<Object> getKeys(String category) {
        return getDelegate().getKeys(category);
    }

    public Collection<String> getCategories() {
        return getDelegate().getCategories();
    }

    public Collection<Object> getKeys() {
        return getDelegate().getKeys();
    }

    public Point getOrigin() {
        return getDelegate().getOrigin();
    }

    public Rectangle getSolvedBounds(Object key) {
        return getDelegate().getSolvedBounds(key);
    }

    public Point getSolvedPosition(Object key) {
        return getDelegate().getSolvedPosition(key);
    }

    public void recordInitPosition(Object key, Point position, String category, boolean output) {
        getDelegate().recordInitPosition(key, position, category, false);
    }

    public void setBoundsProvider(Object key, IBoundsProvider boundsProvider) {
        getDelegate().setBoundsProvider(key, boundsProvider);
    }

    public void setDefaultBoundsProvider(IBoundsProvider boundsProvider) {
        getDelegate().setDefaultBoundsProvider(boundsProvider);
    }

    public void setGeneralBoundsProvider(String category,
            IBoundsProvider boundsProvider) {
        getDelegate().setGeneralBoundsProvider(category, boundsProvider);
    }

    public void setOrigin(Point origin) {
        getDelegate().setOrigin(origin);
    }

    public void setOrigin(int x, int y) {
        getDelegate().setOrigin(x, y);
    }

}