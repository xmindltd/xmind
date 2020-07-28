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

public class ScallopsBoundaryDecoration extends SegmentedBoundaryDecoration {

    private static final float c1 = 0.25f;

    private static final float c2 = 0.75f;

    public ScallopsBoundaryDecoration() {
        super();
    }

    public ScallopsBoundaryDecoration(String id) {
        super(id);
    }

    protected float getMarginAmount() {
        return 0.6f;
    }

    protected float getPreferredVerticalStep() {
        return super.getPreferredVerticalStep() * 0.75f;
    }

    protected void sketchBottomSegment(IFigure figure, Path shape, float x,
            float y, float bottom, int index) {
        shape.cubicTo(x - hstep * c1, bottom, x - hstep * c2, bottom,
                x - hstep, y);
    }

    protected void sketchLeftSegment(IFigure figure, Path shape, float x,
            float y, float left, int index) {
        shape.cubicTo(left, y - vstep * c1, left, y - vstep * c2, x, y - vstep);
    }

    protected void sketchRightSegment(IFigure figure, Path shape, float x,
            float y, float right, int index) {
        shape.cubicTo(right, y + vstep * c1, right, y + vstep * c2, x, y
                + vstep);
    }

    protected void sketchTopSegment(IFigure figure, Path shape, float x,
            float y, float top, int index) {
        shape.cubicTo(x + hstep * c1, top, x + hstep * c2, top, x + hstep, y);
    }

    public Insets getPreferredInsets(IFigure figure, int width, int height) {
        return Geometry
                .add(super.getPreferredInsets(figure, width, height), 10);
    }

}