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

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.graphics.Path;

public class ArrowedCurveBranchConnection extends CurveBranchConnection {

    private static final double CPRatio = 0.1;

    private static final double ARROW_WING_WIDTH = 2;

    private static final double ARROW_LENGTH = 5;

    private PrecisionPoint realTarget = new PrecisionPoint();

    public ArrowedCurveBranchConnection() {
    }

    public ArrowedCurveBranchConnection(String id) {
        super(id);
    }

    protected double getControlPointRatio() {
        return CPRatio;
    }

    protected double getArrowWidth() {
        return ARROW_WING_WIDTH + getArrowSizeAdjustment();
    }

    protected double getArrowLength() {
        return ARROW_LENGTH + getArrowSizeAdjustment();
    }

    private int getArrowSizeAdjustment() {
        return getLineWidth() - 1;
    }

    protected void calculateTerminalPoints(IFigure figure,
            PrecisionPoint sourcePos, PrecisionPoint targetPos) {
        super.calculateTerminalPoints(figure, sourcePos, targetPos);
        realTarget.setLocation(targetPos);
        if (isTargetHorizontal()) {
            if (targetPos.x > sourcePos.x) {
                targetPos.x -= getArrowLength();
            } else {
                targetPos.x += getArrowLength();
            }
        } else {
            if (targetPos.y > sourcePos.y) {
                targetPos.y -= getArrowLength();
            } else {
                targetPos.y += getArrowLength();
            }
        }
    }

    protected void drawLine(IFigure figure, Graphics g) {
        super.drawLine(figure, g);

        Path arrow = new Path(Display.getCurrent());
        shapeArrow(figure, arrow);
        Color bg = g.getBackgroundColor();
        g.setBackgroundColor(g.getForegroundColor());
        g.fillPath(arrow);
        g.setBackgroundColor(bg);
        arrow.dispose();
    }

    private void shapeArrow(IFigure figure, Path shape) {
        PrecisionPoint sp = getSourcePosition(figure);
        PrecisionPoint tp = realTarget;
        shape.moveTo(tp);
        double w = getArrowWidth();
        double l = getArrowLength();
        if (isTargetHorizontal()) {
            if (tp.x > sp.x) {
                shape.lineTo((float) (tp.x - l), (float) (tp.y - w));
                shape.lineTo((float) (tp.x - l), (float) (tp.y + w));
            } else {
                shape.lineTo((float) (tp.x + l), (float) (tp.y - w));
                shape.lineTo((float) (tp.x + l), (float) (tp.y + w));
            }
        } else {
            if (tp.y > sp.y) {
                shape.lineTo((float) (tp.x - w), (float) (tp.y - l));
                shape.lineTo((float) (tp.x + w), (float) (tp.y - l));
            } else {
                shape.lineTo((float) (tp.x - w), (float) (tp.y + l));
                shape.lineTo((float) (tp.x + w), (float) (tp.y + l));
            }
        }
    }
}