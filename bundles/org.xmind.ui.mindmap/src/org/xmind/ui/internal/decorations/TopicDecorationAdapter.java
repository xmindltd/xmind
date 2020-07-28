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
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.xmind.gef.draw2d.decoration.ICorneredDecoration;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.draw2d.decoration.ILineDecoration;
import org.xmind.gef.draw2d.decoration.IShadowedDecoration;
import org.xmind.gef.draw2d.decoration.IShapeDecoration;
import org.xmind.gef.draw2d.decoration.IShapeDecorationEx;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.AbstractTopicDecoration;

public class TopicDecorationAdapter extends AbstractTopicDecoration implements
        ICorneredDecoration {

    private IDecoration decoration;

    public TopicDecorationAdapter(IDecoration decoration) {
        this.decoration = decoration;
    }

    protected void sketch(IFigure figure, Path shape, Rectangle box, int purpose) {
    }

    public PrecisionPoint getAnchorLocation(IFigure figure, double refX,
            double refY, double expansion) {
        if (decoration instanceof IShapeDecorationEx)
            return ((IShapeDecorationEx) decoration).getAnchorLocation(figure,
                    refX, refY, expansion);
        return super.getAnchorLocation(figure, refX, refY, expansion);
    }

    public Insets getPreferredInsets(IFigure figure, int width, int height) {
        if (decoration instanceof IShapeDecorationEx)
            return ((IShapeDecorationEx) decoration).getPreferredInsets(figure,
                    width, height);
        return super.getPreferredInsets(figure, width, height);
    }

    public boolean containsPoint(IFigure figure, int x, int y) {
        if (decoration instanceof IShapeDecorationEx)
            return ((IShapeDecorationEx) decoration)
                    .containsPoint(figure, x, y);
        return super.containsPoint(figure, x, y);
    }

    public int getAlpha() {
        return decoration.getAlpha();
    }

    public int getFillAlpha() {
        if (decoration instanceof IShapeDecoration)
            return ((IShapeDecoration) decoration).getFillAlpha();
        return super.getFillAlpha();
    }

    public Color getFillColor() {
        if (decoration instanceof IShapeDecoration)
            return ((IShapeDecoration) decoration).getFillColor();
        return super.getFillColor();
    }

    public String getId() {
        return decoration.getId();
    }

    public int getLineAlpha() {
        if (decoration instanceof IShapeDecoration)
            return ((IShapeDecoration) decoration).getLineAlpha();
        return super.getLineAlpha();
    }

    public Color getLineColor() {
        if (decoration instanceof IShapeDecoration)
            return ((IShapeDecoration) decoration).getLineColor();
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

    public void invalidate() {
        decoration.invalidate();
    }

    public boolean isGradient() {
        if (decoration instanceof IShapeDecoration)
            return ((IShapeDecoration) decoration).isGradient();
        return super.isGradient();
    }

    public boolean isVisible() {
        return decoration.isVisible();
    }

    public void paint(IFigure figure, Graphics graphics) {
        decoration.paint(figure, graphics);
    }

    public void setAlpha(IFigure figure, int alpha) {
        decoration.setAlpha(figure, alpha);
    }

    public void setFillAlpha(IFigure figure, int alpha) {
        if (decoration instanceof IShapeDecoration) {
            ((IShapeDecoration) decoration).setFillAlpha(figure, alpha);
        } else {
            super.setFillAlpha(figure, alpha);
        }
    }

    public void setFillColor(IFigure figure, Color c) {
        if (decoration instanceof IShapeDecoration) {
            ((IShapeDecoration) decoration).setFillColor(figure, c);
        } else {
            super.setFillColor(figure, c);
        }
    }

    public void setGradient(IFigure figure, boolean gradient) {
        if (decoration instanceof IShapeDecoration) {
            ((IShapeDecoration) decoration).setGradient(figure, gradient);
        } else {
            super.setGradient(figure, gradient);
        }
    }

    public void setId(String id) {
        decoration.setId(id);
    }

    public void setLineAlpha(IFigure figure, int alpha) {
        if (decoration instanceof IShapeDecoration) {
            ((IShapeDecoration) decoration).setLineAlpha(figure, alpha);
        } else {
            super.setLineAlpha(figure, alpha);
        }
    }

    public void setLineColor(IFigure figure, Color c) {
        if (decoration instanceof ILineDecoration) {
            ((ILineDecoration) decoration).setLineColor(figure, c);
        } else {
            super.setLineColor(figure, c);
        }
    }

    public void setLineStyle(IFigure figure, int lineStyle) {
        if (decoration instanceof ILineDecoration) {
            ((ILineDecoration) decoration).setLineStyle(figure, lineStyle);
        } else {
            super.setLineStyle(figure, lineStyle);
        }
    }

    public void setLineWidth(IFigure figure, int lineWidth) {
        if (decoration instanceof ILineDecoration) {
            ((ILineDecoration) decoration).setLineWidth(figure, lineWidth);
        } else {
            super.setLineWidth(figure, lineWidth);
        }
    }

    public void setVisible(IFigure figure, boolean visible) {
        decoration.setVisible(figure, visible);
    }

    public void validate(IFigure figure) {
        decoration.validate(figure);
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
        if (obj instanceof TopicDecorationAdapter) {
            obj = ((TopicDecorationAdapter) obj).decoration;
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