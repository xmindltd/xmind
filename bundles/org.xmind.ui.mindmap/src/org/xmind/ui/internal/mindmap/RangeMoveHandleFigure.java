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
package org.xmind.ui.internal.mindmap;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.draw2d.ReferencedFigure;
import org.xmind.gef.draw2d.geometry.IPrecisionTransformer;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.geometry.PrecisionTransposer;
import org.xmind.gef.draw2d.geometry.PrecisionVerticalFlipper;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;

class RangeMoveHandleFigure extends ReferencedFigure {

    private static final int PREF_MIN_WIDTH = 16;

    private static final double RATIO = 0.7;

    private static final int HEIGHT = (MindMapUI.SELECTION_LINE_WIDTH + (int) Math
            .ceil(PREF_MIN_WIDTH * RATIO / 4)) * 2;

    private static final int LINE_WIDTH = 2;

    private static final double CORNER_WIDTH = LINE_WIDTH * RATIO;

    private static PrecisionPoint p1 = new PrecisionPoint();
    private static PrecisionPoint p2 = new PrecisionPoint();
    private static PrecisionPoint p3 = new PrecisionPoint();
    private static PrecisionPoint p4 = new PrecisionPoint();
    private static PrecisionPoint p5 = new PrecisionPoint();
    private static PrecisionPoint p6 = new PrecisionPoint();
    private static PrecisionPoint p7 = new PrecisionPoint();

    private static IPrecisionTransformer t = new PrecisionTransposer();
    private static IPrecisionTransformer v = new PrecisionVerticalFlipper();

    private int orientation;

    private int alpha = 0xff;

    public RangeMoveHandleFigure(int orientation) {
        this.orientation = orientation;
//        int width = LINE_WIDTH;//vertical ? 17 : 7;
//        int height = LINE_WIDTH; //vertical ? 7 : 17;
//        setSize(width, height);
//        setPreferredSize(width, height);
        setForegroundColor(ColorUtils.getColor("#0033cc")); //$NON-NLS-1$
        setBackgroundColor(ColorUtils.getColor("#90c8f0")); //$NON-NLS-1$
    }

    public void setClientSize(Dimension client) {
        if (isVertical()) {
            int w = Math.min(client.width, Math.max(client.width / 2,
                    PREF_MIN_WIDTH));
            setPreferredSize(w, HEIGHT);
            setSize(w, HEIGHT);
        } else {
            int h = Math.min(client.height, Math.max(client.height / 2,
                    PREF_MIN_WIDTH));
            setPreferredSize(HEIGHT, h);
            setSize(HEIGHT, h);
        }
    }

    private boolean isVertical() {
        return (orientation & PositionConstants.NORTH_SOUTH) != 0;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
        repaint();
    }

    protected void paintFigure(Graphics graphics) {
        super.paintFigure(graphics);
        graphics.setAntialias(SWT.ON);
        graphics.setAlpha(getAlpha());
        graphics.setLineWidth(1);
        graphics.setLineStyle(SWT.LINE_SOLID);
        Path shape = new Path(Display.getCurrent());
        createShape2(shape);
        graphics.fillPath(shape);
        graphics.drawPath(shape);
        shape.dispose();
    }

    protected void createShape2(Path shape) {
        Rectangle r = getBounds();
        p1.setLocation(r.x, r.y);
        p6.setLocation(r.right(), r.bottom());

        t.setEnabled((orientation & PositionConstants.EAST_WEST) != 0);
        v.setEnabled((orientation & PositionConstants.SOUTH_EAST) != 0);
        t.setOrigin((p1.x + p6.x) / 2, (p1.y + p6.y) / 2);
        v.setOrigin(t.getOrigin());
        v.t(t.t(p1));
        v.t(t.t(p6));
        if (p6.x < p1.x) {
            double temp = p1.x;
            p1.x = p6.x;
            p6.x = temp;
        }
        if (p6.y < p1.y) {
            double temp = p1.y;
            p1.y = p6.y;
            p6.y = temp;
        }
        p6.y = (p6.y + p1.y) / 2;
        p1.y = p6.y - LINE_WIDTH;
        double width = p6.x - p1.x;
        double a;
        if (width < PREF_MIN_WIDTH) {
            a = width / 4;
        } else {
            a = PREF_MIN_WIDTH / 4;
        }
        double b = a * RATIO;
        double c = p1.x + width / 2;
        p2.setLocation(c - a, p1.y);
        p3.setLocation(c, p1.y - b);
        p4.setLocation(c + a, p1.y);
        p5.setLocation(p6.x, p1.y);
        p7.setLocation(p1.x, p6.y);
        double d = Math.min(CORNER_WIDTH, (width - a * 2) / 2);
        p1.x += d;
        p5.x -= d;
        t.r(v.r(p1));
        t.r(v.r(p2));
        t.r(v.r(p3));
        t.r(v.r(p4));
        t.r(v.r(p5));
        t.r(v.r(p6));
        t.r(v.r(p7));
        shape.moveTo(p1);
        shape.lineTo(p2);
        shape.lineTo(p3);
        shape.lineTo(p4);
        shape.lineTo(p5);
        shape.lineTo(p6);
        shape.lineTo(p7);
        shape.close();
    }

    protected void createShape1(Path shape) {
        Rectangle r = getBounds().getResized(-1, -1);
        switch (orientation) {
        case PositionConstants.NORTH:
            shape.moveTo(r.x, r.bottom());
            shape.lineTo(r.x + r.width * 0.5f, r.y);
            shape.lineTo(r.right(), r.bottom());
            shape.close();
            break;
        case PositionConstants.SOUTH:
            shape.moveTo(r.x, r.y);
            shape.lineTo(r.right(), r.y);
            shape.lineTo(r.x + r.width * 0.5f, r.bottom());
            shape.close();
            break;
        case PositionConstants.EAST:
            shape.moveTo(r.x, r.y);
            shape.lineTo(r.right(), r.y + r.height * 0.5f);
            shape.lineTo(r.x, r.bottom());
            shape.close();
            break;
        case PositionConstants.WEST:
            shape.moveTo(r.right(), r.y);
            shape.lineTo(r.right(), r.bottom());
            shape.lineTo(r.x, r.y + r.height * 0.5f);
            shape.close();
            break;
        }
    }
}