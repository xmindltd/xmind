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
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.draw2d.graphics.Path;

/**
 * @author Frank Shaka
 */
public class PointFigure extends ReferencedFigure implements
        ITransparentableFigure {

    public static final int SHAPE_SQUARE = 1;
    public static final int SHAPE_DOT = 2;
    public static final int SHAPE_DIAMOND = 3;

    private int borderAlpha = 0xFF;

    private int fillAlpha = 0xFF;

    private int shapeType;

    /**
     * 
     */
    public PointFigure(int shapeType) {
        this.shapeType = shapeType;
    }

    public int getShapeType() {
        return shapeType;
    }

    /**
     * @see org.xmind.gef.draw2d.IUseTransparency#getMainAlpha()
     */
    public int getMainAlpha() {
        return borderAlpha;
    }

    /**
     * @see org.xmind.gef.draw2d.IUseTransparency#setMainAlpha(int)
     */
    public void setMainAlpha(int alpha) {
        if (alpha == this.borderAlpha)
            return;
        this.borderAlpha = alpha;
        repaint();
    }

    /**
     * @see org.xmind.gef.draw2d.IUseTransparency#getSubAlpha()
     */
    public int getSubAlpha() {
        return fillAlpha;
    }

    /**
     * @see org.xmind.gef.draw2d.IUseTransparency#setSubAlpha(int)
     */
    public void setSubAlpha(int alpha) {
        if (alpha == this.fillAlpha)
            return;
        this.fillAlpha = alpha;
        repaint();
    }

    /**
     * @see org.eclipse.draw2d.Figure#paintFigure(org.eclipse.draw2d.Graphics)
     */
    @Override
    protected void paintFigure(Graphics graphics) {
        graphics.setAntialias(SWT.ON);
        graphics.setLineWidth(1);
        graphics.setLineStyle(SWT.LINE_SOLID);
        graphics.setLineJoin(SWT.JOIN_BEVEL);
        Path shape = createShape(shapeType);
        if (shape != null) {
            Color fillColor = getLocalBackgroundColor();
            if (fillColor != null) {
                graphics.setAlpha(getSubAlpha());
                graphics.setBackgroundColor(fillColor);
                graphics.fillPath(shape);
            }
            Color borderColor = getLocalForegroundColor();
            if (borderColor != null) {
                graphics.setAlpha(getMainAlpha());
                graphics.setForegroundColor(borderColor);
                graphics.drawPath(shape);
            }
            shape.dispose();
        }
    }

    private Path createShape(int shapeType) {
        if (shapeType != SHAPE_SQUARE && shapeType != SHAPE_DOT
                && shapeType != SHAPE_DIAMOND)
            return null;

        Point ref = getReference();
        int x = ref.x;
        int y = ref.y;
        Dimension size = getSize();
        int w = size.width;
        int h = size.height;
        Path shape = new Path(Display.getCurrent());
        switch (shapeType) {
        case SHAPE_SQUARE:
            shape.addRectangle(x - w / 2, y - h / 2, w - 1, h - 1);
            return shape;
        case SHAPE_DOT:
            shape.addArc(x - w / 2, y - h / 2, w - 1, h - 1, 0, 360);
            break;
        case SHAPE_DIAMOND:
            shape.moveTo(x - w / 2, y);
            shape.lineTo(x, y - h / 2);
            shape.lineTo(x + w / 2, y);
            shape.lineTo(x, y + h / 2);
            shape.close();
            break;
        }
        return shape;
    }

}