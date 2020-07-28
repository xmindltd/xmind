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
package org.xmind.ui.internal.spreadsheet.decorations;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.decoration.ICorneredDecoration;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.AbstractTopicDecoration;

public class HeadTopicDecoration extends AbstractTopicDecoration
        implements ICorneredDecoration {

    private int corner = 2;

    protected void performPaint(IFigure figure, Graphics g) {
        // do nothing
    }

    public void paintShadow(IFigure figure, Graphics graphics) {
        // do nothing
    }

    protected void sketch(IFigure figure, Path shape, Rectangle box,
            int purpose) {
        shape.addRectangle(box);
    }

    public PrecisionPoint getAnchorLocation(IFigure figure, double refX,
            double refY, double expansion) {
        return Geometry.getChopBoxLocation(refX, refY, figure.getBounds(),
                expansion);
    }

    public int getCornerSize() {
        return corner;
    }

    public void setCornerSize(IFigure figure, int cornerSize) {
//        if (cornerSize == this.corner)
//            return;
//
//        this.corner = cornerSize;
//        invalidate();
//        if (figure != null) {
//            figure.revalidate();
//            repaint(figure);
//        }
    }

    protected void repaint(IFigure figure) {
        super.repaint(figure);
        if (figure.getParent() != null) {
            figure.getParent().repaint();
        }
    }

}