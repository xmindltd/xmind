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

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

public class SplitIntersectionSolver extends AbstractIntersectionSolver {

    private ITransformer t;

    private ITransformer f;

    private Object currentKey = null;

    private Rectangle intersection = null;

    public SplitIntersectionSolver() {
        this(true);
    }

    public SplitIntersectionSolver(boolean horizontal) {
        this.t = new Transposer();
        this.t.setEnabled(!horizontal);
        this.f = new HorizontalFlipper();
    }

    public void setHorizontal(boolean horizontal) {
        t.setEnabled(!horizontal);
    }

    /**
     * @see cn.brainy.gef.draw2d.figure.AbstractPositionSolver#calcIntersections()
     */
    @Override
    protected boolean calcIntersections() {
        for (Object key : getFreeKeys()) {
            Rectangle freeBounds = getSolvedBounds(key);
            if (freeBounds != null) {
                for (Object steadyKey : getSteadyKeys()) {
                    Rectangle steadyBounds = getSolvedBounds(steadyKey);
                    if (steadyBounds != null) {
                        intersection = freeBounds.getIntersection(steadyBounds);
                        if (!intersection.isEmpty()) {
                            currentKey = key;
                            return true;
                        }
                    }
                }
                for (Object key2 : getFreeKeys()) {
                    if (key2 == key)
                        continue;
                    Rectangle freeBounds2 = getSolvedBounds(key2);
                    if (freeBounds2 != null) {
                        intersection = freeBounds.getIntersection(freeBounds2);
                        if (!intersection.isEmpty()) {
                            currentKey = key;
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public void setOrigin(int x, int y) {
        t.setOrigin(x, y);
        f.setOrigin(x, y);
        super.setOrigin(x, y);
    }

    public void setOrigin(Point origin) {
        t.setOrigin(origin);
        f.setOrigin(origin);
        super.setOrigin(origin);
    }

    /**
     * @see cn.brainy.gef.draw2d.figure.AbstractPositionSolver#solveIntersections()
     */
    @Override
    protected void solveIntersections() {
        t.t(intersection);
        Point pos = getSolvedPosition(currentKey);
        t.t(pos);
        f.setEnabled(pos.x < getOrigin().x);
        f.t(pos);
        pos.x += (intersection.width + Math.max(1, getSpacing()));
        t.r(f.r(pos));
        t.r(intersection);
    }

}