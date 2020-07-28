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

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.draw2d.geometry.IPrecisionTransformer;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.geometry.PrecisionTransposer;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.AbstractSummaryDecoration;

public class CurlySummaryDecoration extends AbstractSummaryDecoration {

    private static final double H = 0.3;

    private static PrecisionPoint p1 = new PrecisionPoint();
    private static PrecisionPoint p2 = new PrecisionPoint();
    private static IPrecisionTransformer t = new PrecisionTransposer();

    public CurlySummaryDecoration() {
    }

    public CurlySummaryDecoration(String id) {
        super(id);
    }

    protected boolean usesFill() {
        return true;
    }

    protected void drawLine(IFigure figure, Graphics graphics) {
        graphics.setFillRule(SWT.FILL_WINDING);
        Color bg = graphics.getBackgroundColor();
        graphics.setBackgroundColor(graphics.getForegroundColor());

        PrecisionPoint sp = getSourcePosition(figure);
        PrecisionPoint tp = getTargetPosition(figure);
        PrecisionPoint cp = getConclusionPoint(figure);

        t.setEnabled(!isHorizontal());
        t.t(sp);
        t.t(tp);
        t.t(cp);

        double lw = getLineWidth() * 2;
        double x = (Math.abs(sp.x - cp.x) < Math.abs(tp.x - cp.x)) ? sp.x
                : tp.x;
        lw = Math.min(lw, Math.abs(cp.x - x));

        Path shape = new Path(Display.getCurrent());
        routeCurlyLine(shape, sp, cp, lw);
        paintPath(figure, graphics, shape, true);
        shape.dispose();

        shape = new Path(Display.getCurrent());
        routeCurlyLine(shape, cp, tp, lw);
        paintPath(figure, graphics, shape, true);
        shape.dispose();

        t.r(sp);
        t.r(tp);
        t.r(cp);

        graphics.setBackgroundColor(bg);
    }

    protected void paintPath(IFigure figure, Graphics graphics, Path path,
            boolean fill) {
        super.paintPath(figure, graphics, path, fill);
    }

    protected void route(IFigure figure, Path shape) {
        PrecisionPoint sp = getSourcePosition(figure);
        PrecisionPoint tp = getTargetPosition(figure);
        PrecisionPoint cp = getConclusionPoint(figure);

        t.setEnabled(!isHorizontal());
        t.t(sp);
        t.t(tp);
        t.t(cp);

        double lw = getLineWidth() * 2;
        double x = (Math.abs(sp.x - cp.x) < Math.abs(tp.x - cp.x)) ? sp.x
                : tp.x;
        lw = Math.min(lw, Math.abs(cp.x - x));
        routeCurlyLine(shape, sp, cp, lw);
        routeCurlyLine(shape, cp, tp, lw);

        t.r(sp);
        t.r(tp);
        t.r(cp);
    }

    private void routeCurlyLine(Path shape, PrecisionPoint sp,
            PrecisionPoint tp, double w) {
        double sy1 = (sp.y < tp.y) ? sp.y - H : sp.y + H;
        double sy2 = (sp.y < tp.y) ? sp.y + H : sp.y - H;
        double ty1 = (tp.y < sp.y) ? tp.y + H : tp.y - H;
        double ty2 = (tp.y < sp.y) ? tp.y - H : tp.y + H;
        double cx = (sp.x + tp.x) / 2;
        double cy = (sp.y + tp.y) / 2;
        double cx1 = (cx < tp.x) ? cx + w / 2 : cx - w / 2;
        double cx2 = (cx < tp.x) ? cx - w / 2 : cx + w / 2;
        double scx1 = tp.x;
        double scx2 = (scx1 < sp.x) ? scx1 + w : scx1 - w;
        double tcx2 = sp.x;
        double tcx1 = (tcx2 < tp.x) ? tcx2 + w : tcx2 - w;
        shape.moveTo(t.r(p1.setLocation(sp.x, sy1)));
        shape.quadTo(t.r(p1.setLocation(scx1, sy1)), t.r(p2
                .setLocation(cx1, cy)));
        shape.quadTo(t.r(p1.setLocation(tcx1, ty1)), t.r(p2.setLocation(tp.x,
                ty1)));
        shape.lineTo(t.r(p1.setLocation(tp.x, ty2)));
        shape.quadTo(t.r(p1.setLocation(tcx2, ty2)), t.r(p2
                .setLocation(cx2, cy)));
        shape.quadTo(t.r(p1.setLocation(scx2, sy2)), t.r(p2.setLocation(sp.x,
                sy2)));
        shape.lineTo(t.r(p1.setLocation(sp.x, sy1)));
    }

}