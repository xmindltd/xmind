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

public class TensionBoundaryDecoration extends SegmentedBoundaryDecoration {

    private static final float f = 0.5f;

    private static final float c1 = 0.25f;

    private static final float c2 = 0.75f;

    private float topInset;

    private float leftInset;

    private float bottomInset;

    private float rightInset;

    public TensionBoundaryDecoration() {
        super();
    }

    public TensionBoundaryDecoration(String id) {
        super(id);
    }

    protected float getMarginAmount() {
        return 0.2f;
    }

    public void validate(IFigure figure) {
        super.validate(figure);
        Insets ins = figure.getInsets();
        topInset = ins.top * f;
        leftInset = ins.left * f;
        bottomInset = ins.bottom * f;
        rightInset = ins.right * f;
    }

    protected void sketchBottomSegment(IFigure figure, Path shape, float x,
            float y, float bottom, int index) {
        shape.cubicTo(x - hstep * c1, y - bottomInset, x - hstep * c2, y
                - bottomInset, x - hstep, y);
    }

    protected void sketchLeftSegment(IFigure figure, Path shape, float x,
            float y, float left, int index) {
        shape.cubicTo(x + leftInset, y - vstep * c1, x + leftInset, y - vstep
                * c2, x, y - vstep);
    }

    protected void sketchRightSegment(IFigure figure, Path shape, float x,
            float y, float right, int index) {
        shape.cubicTo(x - rightInset, y + vstep * c1, x - rightInset, y + vstep
                * c2, x, y + vstep);
    }

    protected void sketchTopSegment(IFigure figure, Path shape, float x,
            float y, float top, int index) {
        shape.cubicTo(x + hstep * c1, y + topInset, x + hstep * c2, y
                + topInset, x + hstep, y);
    }

    public Insets getPreferredInsets(IFigure figure, int width, int height) {
        return Geometry
                .add(super.getPreferredInsets(figure, width, height), 10);
    }

}