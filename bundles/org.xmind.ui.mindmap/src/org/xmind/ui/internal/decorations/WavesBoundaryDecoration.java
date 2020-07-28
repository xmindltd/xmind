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
import org.eclipse.draw2d.geometry.Insets;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.graphics.Path;

public class WavesBoundaryDecoration extends SegmentedBoundaryDecoration {

    private static final float c = 0.5f;
    private static final float c1 = 0.125f;
    private static final float c2 = 0.375f;
    private static final float c3 = 0.625f;
    private static final float c4 = 0.875f;

    public WavesBoundaryDecoration() {
    }

    public WavesBoundaryDecoration(String id) {
        super(id);
    }

    protected float getMarginAmount() {
        return 0.25f;
    }

    protected float getPreferredVerticalStep() {
        return super.getPreferredVerticalStep() * 0.9f;
    }

    protected void sketchBottomSegment(IFigure figure, Path shape, float x,
            float y, float bottom, int index) {
        shape.cubicTo(x - hstep * c1, bottom, x - hstep * c2, bottom, x - hstep
                * c, y);
        shape.cubicTo(x - hstep * c3, y - (bottom - y), x - hstep * c4, y
                - (bottom - y), x - hstep, y);
    }

    protected void sketchLeftSegment(IFigure figure, Path shape, float x,
            float y, float left, int index) {
        shape.cubicTo(left, y - vstep * c1, left, y - vstep * c2, x, y - vstep
                * c);
        shape.cubicTo(x + (x - left), y - vstep * c3, x + (x - left), y - vstep
                * c4, x, y - vstep);
    }

    protected void sketchRightSegment(IFigure figure, Path shape, float x,
            float y, float right, int index) {
        shape.cubicTo(right, y + vstep * c1, right, y + vstep * c2, x, y
                + vstep * c);
        shape.cubicTo(x - (right - x), y + vstep * c3, x - (right - x), y
                + vstep * c4, x, y + vstep);
    }

    protected void sketchTopSegment(IFigure figure, Path shape, float x,
            float y, float top, int index) {
        shape.cubicTo(x + hstep * c1, top, x + hstep * c2, top, x + hstep * c,
                y);
        shape.cubicTo(x + hstep * c3, y + (y - top), x + hstep * c4, y
                + (y - top), x + hstep, y);
    }

    public Insets getPreferredInsets(IFigure figure, int width, int height) {
        return Geometry
                .add(super.getPreferredInsets(figure, width, height), 10);
    }

}