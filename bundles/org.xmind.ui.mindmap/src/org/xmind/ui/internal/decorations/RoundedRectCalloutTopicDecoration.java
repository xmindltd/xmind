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
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.decoration.ICorneredDecoration;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.AbstractTopicDecoration;

public class RoundedRectCalloutTopicDecoration extends AbstractTopicDecoration
        implements ICorneredDecoration {

    private static final double M = 1 - Math.sqrt(2) / 2;

    private static final float X = 0.2f;

    private int cornerSize = 0;

    public RoundedRectCalloutTopicDecoration() {
    }

    public RoundedRectCalloutTopicDecoration(String id) {
        super(id);
    }

    protected void sketch(IFigure figure, Path shape, Rectangle box,
            int purpose) {
        float x = box.x;
        float y = box.y;
        float width = box.width;
        float height = box.height;

        float tailHeight = getTailHeight(
                figure.getBounds().height - figure.getInsets().getHeight());
        height -= tailHeight;

        float r = x + width;
        float b = y + height;
        float x0 = x + width / 2;
        float y0 = y + height / 2;
        float corner = getAppliedCornerSize();

        float x1 = Math.min(x + corner, x0);
        shape.moveTo(x1, y);

        float y1 = Math.min(y + corner, y0);
        float cx1 = x + (x1 - x) / 4;
        float cy1 = y + (y1 - y) / 4;
        shape.cubicTo(cx1, y, x, cy1, x, y1);

        float y2 = Math.max(b - corner, y0);
        shape.lineTo(x, y2);

        float cy2 = b - (b - y2) / 4;
        shape.cubicTo(x, cy2, cx1, b, x1, b);

        shape.lineTo(x, b + tailHeight);
        shape.lineTo(x + width * X, b);

        float x2 = Math.max(r - corner, x0);
        shape.lineTo(x2, b);

        float cx2 = r - (r - x2) / 4;
        shape.cubicTo(cx2, b, r, cy2, r, y2);

        shape.lineTo(r, y1);

        shape.cubicTo(r, cy1, cx2, y, x2, y);

//        float y1 = Math.min(y + corner, y0);
//        shape.moveTo(x, y1);
//
//        float x1 = Math.min(x + corner, x0);
//        float cx1 = x + (x1 - x) / 4;
//        float cy1 = y + (y1 - y) / 4;
//        shape.cubicTo(x, cy1, cx1, y, x1, y);
//
//        float x2 = Math.max(r - corner, x0);
//        shape.lineTo(x2, y);
//
//        float cx2 = r - (r - x2) / 4;
//        shape.cubicTo(cx2, y, r, cy1, r, y1);
//
//        float y2 = Math.max(b - corner, y0);
//        shape.lineTo(r, y2);
//
//        float cy2 = b - (b - y2) / 4;
//        shape.cubicTo(r, cy2, cx2, b, x2, b);
//
//        shape.lineTo(x + width * X, b);
//        shape.lineTo(x, b + tailHeight);
//        shape.lineTo(x1, b);
//
//        shape.cubicTo(cx1, b, x, cy2, x, y2);

        shape.close();
    }

    public Insets getPreferredInsets(IFigure figure, int width, int height) {
        int c = (int) (M * getAppliedCornerSize()) + getLineWidth();
        Insets ins = super.getPreferredInsets(figure, width, height);
        ins = Geometry.union(ins, c, c, c, c);
        ins.bottom += getTailHeight(height);
        return ins;
    }

    private int getTailHeight(int clientHeight) {
        return clientHeight / 3;
    }

    public int getCornerSize() {
        return cornerSize;
    }

    protected int getAppliedCornerSize() {
        return getCornerSize();// * getLineWidth();
    }

    public void setCornerSize(IFigure figure, int cornerSize) {
        if (cornerSize == this.cornerSize)
            return;

        this.cornerSize = cornerSize;
        invalidate();
        if (figure != null) {
            figure.revalidate();
            figure.repaint();
        }
    }

}