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
package org.xmind.ui.internal.decorations;

import org.eclipse.draw2d.IFigure;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.AbstractBranchConnection;

public class StraightBranchConnection extends AbstractBranchConnection {

    private PrecisionPoint s1 = new PrecisionPoint();

    private PrecisionPoint s2 = new PrecisionPoint();

    private PrecisionPoint t1 = new PrecisionPoint();

    private PrecisionPoint t2 = new PrecisionPoint();

    private PrecisionPoint c1 = new PrecisionPoint();

    private PrecisionPoint c2 = new PrecisionPoint();

    private boolean cachedTapered;

    public StraightBranchConnection() {
        super();
    }

    public StraightBranchConnection(String id) {
        super(id);
    }

    protected void route(IFigure figure, Path shape) {
        PrecisionPoint p1 = getSourcePosition(figure);
        PrecisionPoint p2 = getTargetPosition(figure);
        if (isTapered()) {
            shape.moveTo(s1);
            shape.lineTo(c1);
            shape.lineTo(c2);
            shape.lineTo(s2);
            shape.lineTo(t2);
            shape.lineTo(t1);
            shape.close();
        } else {
            shape.moveTo(p1);
            shape.lineTo(p2);
        }
    }

    public void invalidate() {
        super.invalidate();
        cachedTapered = false;
    }

    protected boolean isPositionValid() {
        return super.isPositionValid() && cachedTapered == isTapered();
    }

    protected void calculateControlPoints(IFigure figure,
            PrecisionPoint sourcePos, PrecisionPoint targetPos) {
        super.calculateControlPoints(figure, sourcePos, targetPos);
        cachedTapered = isTapered();
        if (isTapered()) {
            calcTaperedPositions(sourcePos, targetPos, 0, s1, s2);
            calcTaperedPositions(sourcePos, targetPos, 1, t1, t2);

            calculateSourceControlPoints(t1, s1, s2, c1);
            calculateSourceControlPoints(t2, s2, s1, c2);
        }
    }

}