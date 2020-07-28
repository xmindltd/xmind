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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.draw2d.decoration.IShadowedDecoration;
import org.xmind.gef.draw2d.decoration.PathShapeDecoration;
import org.xmind.gef.draw2d.graphics.Path;

public abstract class AbstractTopicDecoration extends PathShapeDecoration
        implements ITopicDecoration, IShadowedDecoration {

    private int left = 0;

    private int top = 0;

    private int right = 0;

    private int bottom = 0;

    protected AbstractTopicDecoration() {
        super();
    }

    protected AbstractTopicDecoration(String id) {
        super(id);
    }

    protected int getCheckingLineWidth() {
        return super.getCheckingLineWidth() * 2 + 4;
    }

    public void paintShadow(IFigure figure, Graphics graphics) {
        if (!isVisible() || !isFillVisible(figure))
            return;
        checkValidation(figure);
        graphics.setAlpha(getAlpha());
        graphics.setBackgroundColor(ColorConstants.black);
        graphics.setForegroundColor(ColorConstants.black);
        paintFill(figure, graphics);
    }

    public Insets getPreferredInsets(IFigure figure, int width, int height) {
        return new Insets(getTopMargin() + getLineWidth(),
                getLeftMargin() + getLineWidth(),
                getBottomMargin() + getLineWidth(),
                getRightMargin() + getLineWidth());
    }

    public int getLeftMargin() {
        return left;
    }

    public int getTopMargin() {
        return top;
    }

    public int getRightMargin() {
        return right;
    }

    public int getBottomMargin() {
        return bottom;
    }

    public void setLeftMargin(IFigure figure, int value) {
        if (left == value)
            return;

        this.left = value;
        invalidate();
        if (figure != null) {
            figure.revalidate();
            repaint(figure);
        }
    }

    public void setTopMargin(IFigure figure, int value) {
        if (top == value)
            return;

        this.top = value;
        invalidate();
        if (figure != null) {
            figure.revalidate();
            repaint(figure);
        }
    }

    public void setRightMargin(IFigure figure, int value) {
        if (right == value)
            return;
        this.right = value;
        invalidate();
        if (figure != null) {
            figure.revalidate();
            repaint(figure);
        }
    }

    public void setBottomMargin(IFigure figure, int value) {
        if (bottom == value)
            return;
        this.bottom = value;
        invalidate();
        if (figure != null) {
            figure.revalidate();
            repaint(figure);
        }
    }

    public Path createClippingPath(IFigure figure) {
        Path shape = new Path(Display.getCurrent());
        Rectangle box = getOutlineBox(figure);
        sketch(figure, shape, box, FILL);
        shape.close();

        return shape;
    }

}