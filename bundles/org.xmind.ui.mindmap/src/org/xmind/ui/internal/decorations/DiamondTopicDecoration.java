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
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.AbstractTopicDecoration;

public class DiamondTopicDecoration extends AbstractTopicDecoration {

    public DiamondTopicDecoration() {
    }

    public DiamondTopicDecoration(String id) {
        super(id);
    }

    protected void sketch(IFigure figure, Path shape, Rectangle box,
            int purpose) {
        float cx = box.x + box.width * 0.5f;
        float cy = box.y + box.height * 0.5f;
        shape.moveTo(cx, box.y);
        shape.lineTo(box.x, cy);
        shape.lineTo(cx, box.bottom());
        shape.lineTo(box.right(), cy);
        shape.close();
    }

    public PrecisionPoint getAnchorLocation(IFigure figure, double refX,
            double refY, double expansion) {
        Rectangle r = getOutlineBox(figure);
        double w = r.width * 0.5;
        double h = r.height * 0.5;
        double cx = r.x + w;
        double cy = r.y + h;
        double px = refX - cx;
        double py = refY - cy;

        if (px == 0)
            return new PrecisionPoint(refX,
                    (py > 0) ? cy + h + expansion : cy - h - expansion);
        if (py == 0)
            return new PrecisionPoint(
                    (px > 0) ? cx + w + expansion : cx - w - expansion, refY);

        double x = 0;
        double y = 0;
        Insets ins = figure.getInsets();

        if (Math.abs(h * px) == Math.abs(w * py)) {
            x = (px * (w - ins.left)) / Math.abs(px);
            y = (py * (h - ins.bottom)) / Math.abs(py);
        } else if (px > 0 && py < 0) {
            x = (h * w * px) / (h * px - w * py);
            y = (h * w * py) / (h * px - w * py);
        } else if (px > 0 && py > 0) {
            x = (h * w * px) / (h * px + w * py);
            y = (h * w * py) / (h * px + w * py);
        } else if (px < 0 && py > 0) {
            x = (h * w * px) / (w * py - h * px);
            y = (h * w * py) / (w * py - h * px);
        } else if (px < 0 && py < 0) {
            x = -(h * w * px) / (h * px + w * py);
            y = -(h * w * py) / (h * px + w * py);
        }
        double scale = 0.5
                / Math.max(Math.abs(px) / r.width, Math.abs(py) / r.height);
        px *= scale;
        py *= scale;
        double d = Math.hypot(px, py);
        if (d != 0) {
            double s = expansion / d;
            px = px * s;
            py = py * s;
        }

        return new PrecisionPoint(x + cx + px, y + cy + py);
    }

    public Insets getPreferredInsets(IFigure figure, int width, int height) {
        double w = width * 0.5;
        double h = height * 0.5;
        double d = Math.sqrt(h * w);
        int m = (int) Math.round(d) + 1;
        return new Insets(m + getLineWidth());
    }

}