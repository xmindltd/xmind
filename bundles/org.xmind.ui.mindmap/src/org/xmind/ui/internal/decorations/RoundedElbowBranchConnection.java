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
import org.xmind.gef.draw2d.decoration.ICorneredDecoration;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.graphics.Path;

public class RoundedElbowBranchConnection extends ElbowBranchConnection
        implements ICorneredDecoration {

    private static final float CORNER_CONTROL_RATIO = 0.448f;

    private static final PrecisionPoint sc = new PrecisionPoint();
    private static final PrecisionPoint tc = new PrecisionPoint();
    private static final PrecisionPoint scc = new PrecisionPoint();
    private static final PrecisionPoint tcc = new PrecisionPoint();

    private int cornerSize = 0;

    public RoundedElbowBranchConnection() {
        super();
    }

    public RoundedElbowBranchConnection(String id) {
        super(id);
    }

    protected void route(IFigure figure, Path shape) {
        PrecisionPoint sp = getSourcePosition(figure);
        PrecisionPoint tp = getTargetPosition(figure);
        if (isTapered()) {
            if (horizontal || vertical) {
                if (horizontal && vertical) {
                    shape.moveTo(sp);
                    shape.lineTo((float) sp.x, (float) tp.y);
                    shape.lineTo(tp);
                    shape.lineTo((float) tp.x, (float) sp.y);
                    shape.close();
                } else {
                    shape.moveTo(s1);
                    shape.lineTo(t1);
                    shape.lineTo(t2);
                    shape.lineTo(s2);
                    shape.close();
                }
            } else {
                shape.moveTo(s1);
                sc.setLocation(s1);
                tc.setLocation(t1);
                calculateCorners(s1, t1, sc, tc, getCornerSize());
                if (!sc.equals(s1))
                    shape.lineTo(sc);
                shape.cubicTo(scc.setLocation(e1)
                        .move(sc, CORNER_CONTROL_RATIO), tcc.setLocation(e1)
                        .move(tc, CORNER_CONTROL_RATIO), tc);
                if (!tc.equals(t1))
                    shape.lineTo(t1);

                shape.lineTo(t2);
                sc.setLocation(s2);
                tc.setLocation(t2);
                calculateCorners(s2, t2, sc, tc, getCornerSize());
                if (!tc.equals(t2))
                    shape.lineTo(tc);
                shape.cubicTo(tcc.setLocation(e2)
                        .move(tc, CORNER_CONTROL_RATIO), scc.setLocation(e2)
                        .move(sc, CORNER_CONTROL_RATIO), sc);
                if (!sc.equals(s2))
                    shape.lineTo(s2);
                shape.close();
            }
        } else {
            shape.moveTo(sp);
            sc.setLocation(sp);
            tc.setLocation(tp);
            calculateCorners(sp, tp, sc, tc, getCornerSize());
            if (!sc.equals(sp))
                shape.lineTo(sc);
            shape.cubicTo(
                    scc.setLocation(elbow).move(sc, CORNER_CONTROL_RATIO), //
                    tcc.setLocation(elbow).move(tc, CORNER_CONTROL_RATIO), //
                    tc);
            if (!tc.equals(tp)) {
                shape.lineTo(tp);
            }
        }
    }

    private void calculateCorners(PrecisionPoint sp, PrecisionPoint tp,
            PrecisionPoint sourceCorner, PrecisionPoint targetCorner,
            double corner) {
        double width = Math.abs(sp.x - tp.x);
        double height = Math.abs(tp.y - sp.y);
        if (isTargetHorizontal()) {
            if (corner >= 0 && corner <= height) {
                double y = tp.y > sp.y ? tp.y - corner : tp.y + corner;
                sourceCorner.setLocation(sp.x, y);
            }
            if (corner >= 0 && corner <= width) {
                double x = sp.x > tp.x ? sp.x - corner : sp.x + corner;
                targetCorner.setLocation(x, tp.y);
            }
        } else {
            if (corner >= 0 && corner <= width) {
                double x = tp.x > sp.x ? tp.x - corner : tp.x + corner;
                sourceCorner.setLocation(x, sp.y);
            }
            if (corner >= 0 && corner <= height) {
                double y = sp.y > tp.y ? sp.y - corner : sp.y + corner;
                targetCorner.setLocation(tp.x, y);
            }
        }
    }

    public int getCornerSize() {
        return cornerSize;
    }

    public void setCornerSize(IFigure figure, int cornerSize) {
        if (cornerSize == this.cornerSize)
            return;

        this.cornerSize = cornerSize;
        if (figure != null) {
            figure.revalidate();
            figure.repaint();
        }
        invalidate();
    }

}