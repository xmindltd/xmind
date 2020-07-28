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

public abstract class AbstractIntersectionSolver extends
        AbstractPositionSolver implements IIntersectionSolver {

    private int spacing = 5;

    /**
     * @see cn.brainy.gef.draw2d.figure.IPositionSolver#solve()
     */
    public void solve() {
        while (calcIntersections()) {
            solveIntersections();
        }
    }

    public int getSpacing() {
        return spacing;
    }

    public void setSpacing(int spacing) {
        this.spacing = Math.max(1, spacing);
    }

    protected Collection<Object> getSteadyKeys() {
        return getKeys(CATEGORY_STEADY);
    }

    protected Collection<Object> getFreeKeys() {
        return getKeys(CATEGORY_FREE);
    }

    protected abstract boolean calcIntersections();

    protected abstract void solveIntersections();

}