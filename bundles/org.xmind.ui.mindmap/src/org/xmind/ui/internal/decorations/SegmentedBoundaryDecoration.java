/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.decorations;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.AbstractBoundaryDecoration;

public abstract class SegmentedBoundaryDecoration extends
        AbstractBoundaryDecoration {

    private static final float DEFAULT_STEP = 46;

    protected float startX = 0;

    protected float startY = 0;

    protected int numHorizontal = 0;

    protected int numVertical = 0;

    protected float hstep = 0;

    protected float vstep = 0;

    public SegmentedBoundaryDecoration() {
        super();
    }

    public SegmentedBoundaryDecoration(String id) {
        super(id);
    }

    public void validate(IFigure figure) {
        super.validate(figure);
        Insets ins = figure.getInsets();
        float marginAmount = getMarginAmount();
        float top = ins.top * marginAmount;
        float left = ins.left * marginAmount;
        float bottom = ins.bottom * marginAmount;
        float right = ins.right * marginAmount;

        Rectangle box = getOutlineBox(figure);
        float width = box.width - left - right;
        float height = box.height - top - bottom;
        numHorizontal = Math.max(1,
                (int) (width / getPreferredHorizontalStep()));
        numVertical = Math.max(1, (int) (height / getPreferredVerticalStep()));
        hstep = width / numHorizontal;
        vstep = height / numVertical;
        startX = left;
        startY = top;
    }

    protected abstract float getMarginAmount();

    protected float getPreferredHorizontalStep() {
        return DEFAULT_STEP;
    }

    protected float getPreferredVerticalStep() {
        return DEFAULT_STEP;
    }

    protected void sketch(IFigure figure, Path shape, Rectangle box, int purpose) {
        float x = startX + box.x;
        float y = startY + box.y;
        float top = box.y;
        float left = box.x;
        float bottom = box.y + box.height;
        float right = box.x + box.width;

        shape.moveTo(x, y);
        for (int i = 0; i < numHorizontal; i++) {
            sketchTopSegment(figure, shape, x, y, top, i);
            x += hstep;
        }
        for (int i = 0; i < numVertical; i++) {
            sketchRightSegment(figure, shape, x, y, right, i);
            y += vstep;
        }
        for (int i = 0; i < numHorizontal; i++) {
            sketchBottomSegment(figure, shape, x, y, bottom, i);
            x -= hstep;
        }
        for (int i = 0; i < numVertical; i++) {
            sketchLeftSegment(figure, shape, x, y, left, i);
            y -= vstep;
        }
        shape.close();
    }

    protected abstract void sketchTopSegment(IFigure figure, Path shape,
            float x, float y, float top, int index);

    protected abstract void sketchRightSegment(IFigure figure, Path shape,
            float x, float y, float right, int index);

    protected abstract void sketchBottomSegment(IFigure figure, Path shape,
            float x, float y, float bottom, int index);

    protected abstract void sketchLeftSegment(IFigure figure, Path shape,
            float x, float y, float left, int index);

}