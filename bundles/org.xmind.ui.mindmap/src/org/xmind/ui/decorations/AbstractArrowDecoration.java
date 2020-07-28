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
package org.xmind.ui.decorations;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.internal.DPIUtil;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.draw2d.decoration.AbstractDecoration;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.geometry.PrecisionRectangle;
import org.xmind.gef.draw2d.graphics.Path;

@SuppressWarnings("restriction")
public abstract class AbstractArrowDecoration extends AbstractDecoration
        implements IArrowDecoration {

    private static final float[] RECT = new float[4];

    private Color color = null;

    private double angle = 0;

    private int width = 1;

    private PrecisionPoint position = new PrecisionPoint();

    protected AbstractArrowDecoration() {
    }

    protected AbstractArrowDecoration(String id) {
        super(id);
    }

    protected abstract void sketch(IFigure figure, Path shape);

    protected boolean usesFill() {
        return false;
    }

    protected boolean usesOutline() {
        return true;
    }

    protected void performPaint(IFigure figure, Graphics graphics) {
        if (getColor() == null)
            return;

        Path shape = new Path(Display.getCurrent());
        sketch(figure, shape);
        graphics.setAlpha(getAlpha());
        if (usesFill()) {
            Color oldBackground = graphics.getBackgroundColor();
            graphics.setBackgroundColor(getColor());
            graphics.fillPath(shape);
            graphics.setBackgroundColor(oldBackground);
        }
        if (usesOutline()) {
            graphics.setForegroundColor(getColor());
            graphics.setLineWidth(getWidth());
            graphics.setLineStyle(getLineStyle());
            graphics.drawPath(shape);
        }
        shape.dispose();
    }

    protected int getLineStyle() {
        return SWT.LINE_SOLID;
    }

    public double getAngle() {
        return angle;
    }

    public Color getColor() {
        return color;
    }

    public PrecisionPoint getPosition() {
        return position;
    }

    public Rectangle getPreferredBounds(IFigure figure) {
        if (usesFill() || usesOutline()) {
            checkValidation(figure);
            Path shape = new Path(Display.getCurrent());
            sketch(figure, shape);
            shape.getBounds(RECT);
            if (Util.isWindows()) {
                float[] autoScaleDown = DPIUtil.autoScaleDown(RECT);
                RECT[0] = autoScaleDown[0];
                RECT[1] = autoScaleDown[1];
                RECT[2] = autoScaleDown[2];
                RECT[3] = autoScaleDown[3];
            }

            shape.dispose();
            return PrecisionRectangle
                    .toDraw2DRectangle(RECT[0], RECT[1], RECT[2], RECT[3])
                    .expand(getWidth(), getWidth());
        }
        return new PrecisionRectangle(getPosition(), getPosition())
                .toDraw2DRectangle();
    }

    public int getWidth() {
        return width;
    }

    public void setAngle(IFigure figure, double angle) {
        if (angle == this.angle)
            return;

        this.angle = angle;
        if (figure != null) {
            figure.revalidate();
            repaint(figure);
        }
    }

    public void setColor(IFigure figure, Color color) {
        if (color == this.color || (color != null && color.equals(this.color)))
            return;

        this.color = color;
        if (figure != null) {
            repaint(figure);
        }
    }

    public void setPosition(IFigure figure, PrecisionPoint position) {
        if (position == null || this.position.equals(position))
            return;

        this.position.setLocation(position);
        if (figure != null) {
            figure.revalidate();
            repaint(figure);
        }
    }

    public void setWidth(IFigure figure, int width) {
        if (width == this.width)
            return;

        this.width = width;
        if (figure != null) {
            figure.revalidate();
            repaint(figure);
        }
    }

}
