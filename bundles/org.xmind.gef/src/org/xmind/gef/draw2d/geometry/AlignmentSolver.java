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

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

public class AlignmentSolver extends AbstractPositionSolver implements
        Comparator<Object> {

    private int alignmentHint;

    private ITransformer t;

    /**
     * @see PositionConstants#LEFT
     * @see PositionConstants#CENTER
     * @see PositionConstants#RIGHT
     * @see PositionConstants#TOP
     * @see PositionConstants#MIDDLE
     * @see PositionConstants#BOTTOM
     * @param alignmentHint
     */
    public AlignmentSolver(int alignmentHint) {
        this.alignmentHint = alignmentHint;
        this.t = new Transposer();
        this.t.setEnabled(!isHorizontal());
    }

    public int getAlignmentHint() {
        return alignmentHint;
    }

    public void setAlignmentHint(int alignmentHint) {
        this.alignmentHint = alignmentHint;
        boolean horizontal = isHorizontal();
        this.t.setEnabled(!horizontal);
    }

    protected boolean isHorizontal() {
        return (getAlignmentHint() & PositionConstants.LEFT_CENTER_RIGHT) != 0;
    }

    public void setOrigin(int x, int y) {
        t.setOrigin(x, y);
        super.setOrigin(x, y);
    }

    public void setOrigin(Point origin) {
        t.setOrigin(origin);
        super.setOrigin(origin);
    }

    public void solve() {
        Rectangle refBounds = getReferenceBounds();
        if (refBounds == null)
            return;

        setOrigin(refBounds.x + refBounds.width / 2, refBounds.y
                + refBounds.height / 2);

        t.t(refBounds);
        int refLine = getReferenceLine(refBounds);
        Object[] keys = sort(getFreeKeys().toArray());
        for (Object key : keys) {
            Point pos = getSolvedPosition(key);
            Rectangle bounds = getSolvedBounds(key);
            t.t(pos);
            pos.x += refLine - getReferenceLine(t.tr(bounds));
            t.r(pos);
        }
    }

    protected Collection<Object> getFreeKeys() {
        return getKeys(IIntersectionSolver.CATEGORY_FREE);
    }

    private Object[] sort(Object[] keys) {
        Arrays.sort(keys, this);
        return keys;
    }

    protected Collection<Object> getReferenceKeys() {
        return getKeys();
    }

    protected Rectangle getReferenceBounds() {
        Rectangle r = null;
        for (Object key : getReferenceKeys()) {
            r = Geometry.union(r, getSolvedBounds(key));
        }
        return r;
    }

    private int getReferenceLine(Rectangle bounds) {
        if (isLead())
            return bounds.x;
        if (isTrail())
            return bounds.x + bounds.width;
        return bounds.x + bounds.width / 2;
    }

    private boolean isLead() {
        return (getAlignmentHint() & PositionConstants.LEFT) != 0
                || (getAlignmentHint() & PositionConstants.TOP) != 0;
    }

    private boolean isTrail() {
        return (getAlignmentHint() & PositionConstants.RIGHT) != 0
                || (getAlignmentHint() & PositionConstants.BOTTOM) != 0;
    }

    public int compare(Object o1, Object o2) {
        Rectangle r1 = t.tr(getSolvedBounds(o1));
        Rectangle r2 = t.tr(getSolvedBounds(o2));
        int c1 = r1.y + r1.height / 2;
        int c2 = r2.y + r2.height / 2;
        if (c1 == c2)
            return 1;
        return c1 - c2;
    }

}