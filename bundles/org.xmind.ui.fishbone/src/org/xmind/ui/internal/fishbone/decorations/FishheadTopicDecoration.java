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
package org.xmind.ui.internal.fishbone.decorations;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.geometry.IPrecisionTransformer;
import org.xmind.gef.draw2d.geometry.PrecisionHorizontalFlipper;
import org.xmind.gef.draw2d.geometry.PrecisionLine;
import org.xmind.gef.draw2d.geometry.PrecisionLine.LineType;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.AbstractTopicDecoration;

public class FishheadTopicDecoration extends AbstractTopicDecoration {

    private static final PrecisionPoint p1 = new PrecisionPoint();

    private static final PrecisionPoint p2 = new PrecisionPoint();

    private IPrecisionTransformer f = new PrecisionHorizontalFlipper();

    private static final float headGapScale = 0.8f;

    protected static final float headConScale = 0.3f;

    private static final float headHorScale = 0.3f;

    private static final float headVerScale = 0.5f;

    public FishheadTopicDecoration(boolean rightHeaded) {
        f.setEnabled(rightHeaded);
    }

    protected void sketch(IFigure figure, Path shape, Rectangle box,
            int purpose) {
        Insets ins = figure.getInsets();
        Rectangle clientArea = box.getShrinked(ins);
        f.setOrigin(box.x + box.width * 0.5, box.y + box.height * 0.5);
        int right = box.right();
        int bottom = box.bottom();
        float x = box.x
                + clientArea.width * FishheadTopicDecoration.headConScale;
        float y = box.y + box.height * 0.5f;
        f.tp(right, box.y, p1);
        shape.moveTo((float) p1.x, (float) p1.y);
        f.tp(x, box.y, p1);
        f.tp(box.x, y, p2);
        shape.quadTo((float) p1.x, (float) p1.y, (float) p2.x, (float) p2.y);
        f.tp(x, bottom, p1);
        f.tp(right, bottom, p2);
        shape.quadTo((float) p1.x, (float) p1.y, (float) p2.x, (float) p2.y);
        shape.close();
    }

    public PrecisionPoint getAnchorLocation(IFigure figure, double refX,
            double refY, double expansion) {
        Insets ins = figure.getInsets();
        Rectangle box = figure.getBounds();
        f.setOrigin(box.x + box.width * 0.5, box.y + box.height * 0.5);
        Rectangle clientArea = box.getShrinked(ins);
        int right = box.right();
        int bottom = box.bottom();
        float cx = box.x
                + clientArea.width * FishheadTopicDecoration.headConScale;
        float ey = box.y + box.height * 0.5f;
        PrecisionPoint center = getReferencePoint(figure, new PrecisionPoint());
        PrecisionLine line = new PrecisionLine(f.tp(center.x, center.y, p1),
                f.tp(refX, refY, p2), LineType.Ray);
        PrecisionLine rightBorder = new PrecisionLine(right, box.y, right,
                bottom, LineType.LineSegment);
        PrecisionPoint p = line.intersect(rightBorder);
        if (p != null)
            return f.r(p);
        PrecisionPoint[] ps = Geometry.intersectQuadBezier(line, right, box.y,
                cx, box.y, box.x, ey);
        if (ps.length > 0)
            return f.r(ps[0]);
        ps = Geometry.intersectQuadBezier(line, box.x, ey, cx, bottom, right,
                bottom);
        if (ps.length > 0)
            return f.r(ps[0]);
        return f.rp(box.x, box.y);
    }

    public Insets getPreferredInsets(IFigure figure, int width, int height) {
        int v = (int) (height * FishheadTopicDecoration.headVerScale);
        int r = f.isEnabled() ? hor(getRightMargin(), width)
                : gap(getRightMargin());
        int l = f.isEnabled() ? gap(getLeftMargin())
                : hor(getLeftMargin(), width);
        return new Insets(v + getTopMargin() + getLineWidth(),
                l + getLineWidth(), v + getBottomMargin() + getLineWidth(),
                r + getLineWidth());
    }

    private int hor(int margin, int wHint) {
        return (int) (wHint * FishheadTopicDecoration.headHorScale) + margin;
    }

    private int gap(int margin) {
        return (int) (margin * FishheadTopicDecoration.headGapScale);
    }
}
