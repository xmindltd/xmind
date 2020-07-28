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
package org.xmind.gef.draw2d;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.geometry.PrecisionInsets;
import org.xmind.gef.draw2d.geometry.PrecisionRectangle;
import org.xmind.gef.draw2d.geometry.PrecisionRotator;
import org.xmind.gef.draw2d.graphics.Path;

public class RotatableLineBorder extends LineBorder implements IRotatable {

    private static PrecisionRectangle rect = new PrecisionRectangle();

    public RotatableLineBorder() {
        super();
    }

    public RotatableLineBorder(Color color, int width) {
        super(color, width);
    }

    public RotatableLineBorder(Color color) {
        super(color);
    }

    public RotatableLineBorder(int width) {
        super(width);
    }

    private PrecisionRotator rotator = new PrecisionRotator();

    public double getRotationDegrees() {
        return rotator.getAngle();
    }

    public void setRotationDegrees(double degrees) {
        rotator.setAngle(degrees);
    }

    public void paint(IFigure figure, Graphics graphics, Insets insets) {
        graphics.setAntialias(SWT.ON);
        graphics.setLineStyle(SWT.LINE_SOLID);
        if (!Geometry.isSameAngleDegree(getRotationDegrees(), 0, 0.00001)) {
            paintRotatedBorder(figure, graphics, insets);
            return;
        }
        super.paint(figure, graphics, insets);
    }

    private void paintRotatedBorder(IFigure figure, Graphics graphics,
            Insets insets) {
        tempRect.setBounds(getPaintRectangle(figure, insets));
        tempRect.shrink(rotator.t(new PrecisionInsets(super.getInsets(figure)))
                .toDraw2DInsets());
        rect.setBounds(tempRect);
        rotator.setOrigin(rect.x + rect.width / 2, rect.y + rect.height / 2);
        rect = rotator.r(rect);
        graphics.setLineWidth(getWidth());
        if (getColor() != null)
            graphics.setForegroundColor(getColor());
        Path p = new Path(Display.getCurrent());
        p.moveTo(rotator.t(rect.getTopLeft()));
        p.lineTo(rotator.t(rect.getTopRight()));
        p.lineTo(rotator.t(rect.getBottomRight()));
        p.lineTo(rotator.t(rect.getBottomLeft()));
        p.close();
        graphics.drawPath(p);
        p.dispose();
    }

}