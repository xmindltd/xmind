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
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.xmind.gef.draw2d.IAnchor;
import org.xmind.gef.draw2d.decoration.IConnectionDecoration;
import org.xmind.gef.draw2d.decoration.IConnectionDecorationEx;
import org.xmind.gef.draw2d.decoration.ICorneredDecoration;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.draw2d.decoration.ILineDecoration;
import org.xmind.gef.draw2d.decoration.IShadowedDecoration;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.AbstractBranchConnection;

public class BranchConnectionDecorationAdapter extends AbstractBranchConnection
        implements ICorneredDecoration {

    private IDecoration decoration;

    public BranchConnectionDecorationAdapter(IDecoration decoration) {
        this.decoration = decoration;
    }

    protected void route(IFigure figure, Path shape) {
    }

    public int getAlpha() {
        return decoration.getAlpha();
    }

    public String getId() {
        return decoration.getId();
    }

    public Color getLineColor() {
        if (decoration instanceof ILineDecoration)
            return ((ILineDecoration) decoration).getLineColor();
        return super.getLineColor();
    }

    public int getLineStyle() {
        if (decoration instanceof ILineDecoration)
            return ((ILineDecoration) decoration).getLineStyle();
        return super.getLineStyle();
    }

    public int getLineWidth() {
        if (decoration instanceof ILineDecoration)
            return ((ILineDecoration) decoration).getLineWidth();
        return super.getLineWidth();
    }

    public IAnchor getSourceAnchor() {
        if (decoration instanceof IConnectionDecoration)
            return ((IConnectionDecoration) decoration).getSourceAnchor();
        return super.getSourceAnchor();
    }

    public PrecisionPoint getSourcePosition(IFigure figure) {
        if (decoration instanceof IConnectionDecoration)
            return ((IConnectionDecoration) decoration)
                    .getSourcePosition(figure);
        return super.getSourcePosition(figure);
    }

    public IAnchor getTargetAnchor() {
        if (decoration instanceof IConnectionDecoration)
            return ((IConnectionDecoration) decoration).getTargetAnchor();
        return super.getTargetAnchor();
    }

    public PrecisionPoint getTargetPosition(IFigure figure) {
        if (decoration instanceof IConnectionDecoration)
            return ((IConnectionDecoration) decoration)
                    .getTargetPosition(figure);
        return super.getTargetPosition(figure);
    }

    public void invalidate() {
        decoration.invalidate();
    }

    public boolean isVisible() {
        return decoration.isVisible();
    }

    public void paint(IFigure figure, Graphics graphics) {
        decoration.paint(figure, graphics);
    }

    public void reroute(IFigure figure) {
        if (decoration instanceof IConnectionDecoration) {
            ((IConnectionDecoration) decoration).reroute(figure);
        } else {
            super.reroute(figure);
        }
    }

    public void setAlpha(IFigure figure, int alpha) {
        decoration.setAlpha(figure, alpha);
    }

    public void setId(String id) {
        decoration.setId(id);
    }

    public void setLineColor(IFigure figure, Color color) {
        if (decoration instanceof ILineDecoration) {
            ((ILineDecoration) decoration).setLineColor(figure, color);
        } else {
            super.setLineColor(figure, color);
        }
    }

    public void setLineStyle(IFigure figure, int style) {
        if (decoration instanceof ILineDecoration) {
            ((ILineDecoration) decoration).setLineStyle(figure, style);
        } else {
            super.setLineStyle(figure, style);
        }
    }

    public void setLineWidth(IFigure figure, int width) {
        if (decoration instanceof ILineDecoration) {
            ((ILineDecoration) decoration).setLineWidth(figure, width);
        } else {
            super.setLineWidth(figure, width);
        }
    }

    public void setSourceAnchor(IFigure figure, IAnchor anchor) {
        if (decoration instanceof IConnectionDecoration) {
            ((IConnectionDecoration) decoration)
                    .setSourceAnchor(figure, anchor);
        } else {
            super.setSourceAnchor(figure, anchor);
        }
    }

    public void setTargetAnchor(IFigure figure, IAnchor anchor) {
        if (decoration instanceof IConnectionDecoration) {
            ((IConnectionDecoration) decoration)
                    .setTargetAnchor(figure, anchor);
        } else {
            super.setTargetAnchor(figure, anchor);
        }
    }

    public void setVisible(IFigure figure, boolean visible) {
        decoration.setVisible(figure, visible);
    }

    public void validate(IFigure figure) {
        decoration.validate(figure);
    }

    public boolean containsPoint(IFigure figure, int x, int y) {
        if (decoration instanceof IConnectionDecorationEx)
            return ((IConnectionDecorationEx) decoration).containsPoint(figure,
                    x, y);
        return super.containsPoint(figure, x, y);
    }

    public Rectangle getPreferredBounds(IFigure figure) {
        if (decoration instanceof IConnectionDecorationEx)
            return ((IConnectionDecorationEx) decoration)
                    .getPreferredBounds(figure);
        return super.getPreferredBounds(figure);
    }

    public void paintShadow(IFigure figure, Graphics g) {
        if (decoration instanceof IShadowedDecoration) {
            ((IShadowedDecoration) decoration).paintShadow(figure, g);
        } else {
            super.paintShadow(figure, g);
        }
    }

    public int hashCode() {
        return decoration.hashCode();
    }

    public String toString() {
        return decoration.toString();
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj instanceof BranchConnectionDecorationAdapter) {
            obj = ((BranchConnectionDecorationAdapter) obj).decoration;
        }
        return decoration.equals(obj);
    }

    public int getCornerSize() {
        if (decoration instanceof ICorneredDecoration)
            return ((ICorneredDecoration) decoration).getCornerSize();
        return 0;
    }

    public void setCornerSize(IFigure figure, int cornerSize) {
        if (decoration instanceof ICorneredDecoration) {
            ((ICorneredDecoration) decoration)
                    .setCornerSize(figure, cornerSize);
        }
    }

}