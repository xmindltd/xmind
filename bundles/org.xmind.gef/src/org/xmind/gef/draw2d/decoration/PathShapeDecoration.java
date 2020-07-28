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
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.graphics.GraphicsUtils;
import org.xmind.gef.draw2d.graphics.Path;

public abstract class PathShapeDecoration extends AbstractShapeDecoration
        implements IShapeDecorationEx {

    private static final PrecisionPoint REF = new PrecisionPoint();

    protected static final int FILL = 1;

    protected static final int OUTLINE = 2;

    protected static final int CHECK = 3;

    protected PathShapeDecoration() {
        super();
    }

    protected PathShapeDecoration(String id) {
        super(id);
    }

    protected void paintFill(IFigure figure, Graphics graphics) {
        Path shape = new Path(Display.getCurrent());
        sketch(figure, shape, getOutlineBox(figure), FILL);
        paintPath(figure, graphics, shape, true);
        shape.dispose();
    }

    protected void paintOutline(IFigure figure, Graphics graphics) {
        Path shape = new Path(Display.getCurrent());
        sketch(figure, shape, getOutlineBox(figure), OUTLINE);
        paintPath(figure, graphics, shape, false);
        shape.dispose();
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

    public boolean containsPoint(IFigure figure, int x, int y) {
        return containsPoint(figure, x, y, false);
    }

    protected boolean containsPoint(IFigure figure, int x, int y,
            boolean outline) {
        checkValidation(figure);
        GC gc = GraphicsUtils.getAdvanced().getGC();
        gc.setLineWidth(getCheckingLineWidth());
        Path shape = new Path(Display.getCurrent());
        sketch(figure, shape, getOutlineBox(figure), CHECK);
        boolean ret = shape.contains(x, y, gc, outline);
        shape.dispose();
        return ret;
    }

    protected int getCheckingLineWidth() {
        return getLineWidth();
    }

    /**
     * @param figure
     * @param shape
     * @param box
     * @param purpose
     *            {@link #FILL}, {@link #OUTLINE}, {@link #CHECK}
     */
    protected abstract void sketch(IFigure figure, Path shape, Rectangle box,
            int purpose);

    public PrecisionPoint getAnchorLocation(IFigure figure, int orientation,
            double expansion) {
        checkValidation(figure);
        switch (orientation) {
        case PositionConstants.WEST:
            return getWest(figure, expansion);
        case PositionConstants.SOUTH:
            return getSouth(figure, expansion);
        case PositionConstants.NORTH:
            return getNorth(figure, expansion);
        case PositionConstants.EAST:
            return getEast(figure, expansion);
        case PositionConstants.CENTER:
            return REF.setLocation(figure.getBounds().getCenter());
        }
        return null;
    }

    public PrecisionPoint getAnchorLocation(IFigure figure, double refX,
            double refY, double expansion) {
        return Geometry.getChopBoxLocation(refX, refY, getOutlineBox(figure),
                expansion);
    }

    protected PrecisionPoint getEast(IFigure figure, double expansion) {
        PrecisionPoint ref = getReferencePoint(figure, REF);
        return getAnchorLocation(figure, ref.x + 100, ref.y, expansion);
    }

    protected PrecisionPoint getNorth(IFigure figure, double expansion) {
        PrecisionPoint ref = getReferencePoint(figure, REF);
        return getAnchorLocation(figure, ref.x, ref.y - 100, expansion);
    }

    protected PrecisionPoint getSouth(IFigure figure, double expansion) {
        PrecisionPoint ref = getReferencePoint(figure, REF);
        return getAnchorLocation(figure, ref.x, ref.y + 100, expansion);
    }

    protected PrecisionPoint getWest(IFigure figure, double expansion) {
        PrecisionPoint ref = getReferencePoint(figure, REF);
        return getAnchorLocation(figure, ref.x - 100, ref.y, expansion);
    }

    protected PrecisionPoint getReferencePoint(IFigure figure,
            PrecisionPoint result) {
        if (figure instanceof IReferencedFigure)
            return result.setLocation(((IReferencedFigure) figure)
                    .getReference());
        return result.setLocation(figure.getBounds().getCenter());
    }
}