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
package org.xmind.gef.draw2d.decoration;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.internal.DPIUtil;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.draw2d.geometry.PrecisionRectangle;
import org.xmind.gef.draw2d.graphics.GraphicsUtils;
import org.xmind.gef.draw2d.graphics.Path;

@SuppressWarnings("restriction")
public abstract class PathConnectionDecoration extends
        AbstractConnectionDecoration implements IConnectionDecorationEx {

    protected PathConnectionDecoration() {
    }

    protected PathConnectionDecoration(String id) {
        super(id);
    }

    protected boolean usesFill() {
        return false;
    }

    protected void drawLine(IFigure figure, Graphics graphics) {
        if (usesFill()) {
            Color bg = graphics.getBackgroundColor();
            graphics.setBackgroundColor(graphics.getForegroundColor());
            Path shape = new Path(Display.getCurrent());
            route(figure, shape);
            paintPath(figure, graphics, shape, true);
            shape.dispose();
            graphics.setBackgroundColor(bg);
        } else {
            Path shape = new Path(Display.getCurrent());
            route(figure, shape);
            paintPath(figure, graphics, shape, false);
            shape.dispose();
        }
    }

    protected void paintPath(IFigure figure, Graphics graphics, Path path,
            boolean fill) {
        if (fill) {
            graphics.fillPath(path);
        } else {
            if (getLineWidth() > 0)
                graphics.drawPath(path);
        }
    }

    protected abstract void route(IFigure figure, Path shape);

    public boolean containsPoint(IFigure figure, int x, int y) {
        checkValidation(figure);
        GC gc = GraphicsUtils.getAdvanced().getGC();
        gc.setLineWidth(getLineWidthForChecking());
        Path shape = new Path(Display.getCurrent());
        route(figure, shape);
        boolean usesFill = usesFill();
        boolean ret = shape.contains(x, y, gc, !usesFill);
        if (!ret && usesFill && checkOutline(figure)) {
            ret = shape.contains(x, y, gc, true);
        }
        shape.dispose();
        return ret;
    }

    protected boolean checkOutline(IFigure figure) {
        return true;
    }

    protected int getLineWidthForChecking() {
        return getLineWidth();
    }

    public Rectangle getPreferredBounds(IFigure figure) {
        checkValidation(figure);
        Path shape = new Path(Display.getCurrent());
        route(figure, shape);
        float[] bounds = new float[4];
        shape.getBounds(bounds);
        if (Util.isWindows()) {
            float[] autoScaleDown = DPIUtil.autoScaleDown(bounds);
            bounds[0] = autoScaleDown[0];
            bounds[1] = autoScaleDown[1];
            bounds[2] = autoScaleDown[2];
            bounds[3] = autoScaleDown[3];
        }
        shape.dispose();
        return PrecisionRectangle
                .toDraw2DRectangle(bounds[0], bounds[1], bounds[2], bounds[3])
                .expand(getLineWidth(), getLineWidth());
    }

}
