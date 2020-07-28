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
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.xmind.gef.draw2d.IAnchor;
import org.xmind.gef.draw2d.decoration.CompoundDecoration;
import org.xmind.gef.draw2d.decoration.IConnectionDecoration;
import org.xmind.gef.draw2d.decoration.ICorneredDecoration;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.draw2d.decoration.ILineDecoration;
import org.xmind.gef.draw2d.decoration.IShadowedDecoration;
import org.xmind.ui.decorations.IBranchConnectionDecoration;
import org.xmind.ui.decorations.IBranchConnections;

public class BranchConnections extends CompoundDecoration implements
        IBranchConnections, IShadowedDecoration {

    private IAnchor sourceAnchor;

    private Color lineColor = null;

    private int lineStyle = SWT.LINE_SOLID;

    private int lineWidth = 1;

    private int sourceOrientation = PositionConstants.NONE;

    private int sourceExpansion = 0;

    private boolean tapered = false;

    private int cornerSize = 0;

    public void paintShadow(IFigure figure, Graphics graphics) {
        if (!isVisible())
            return;
        checkValidation(figure);
        for (IDecoration decoration : getDecorations()) {
            if (decoration instanceof IShadowedDecoration) {
                ((IShadowedDecoration) decoration)
                        .paintShadow(figure, graphics);
            }
        }
    }

    public void setSourceAnchor(IFigure figure, IAnchor anchor) {
        if (anchor == this.sourceAnchor)
            return;
        this.sourceAnchor = anchor;
        updateAnchors(figure);
    }

    private void updateAnchors(IFigure figure) {
        for (IDecoration decoration : getDecorations()) {
            updateAnchor(figure, decoration);
        }
    }

    private void updateAnchor(IFigure figure, IDecoration decoration) {
        if (decoration instanceof IConnectionDecoration) {
            ((IConnectionDecoration) decoration).setSourceAnchor(figure,
                    getSourceAnchor());
        }
    }

    public void rerouteAll(IFigure figure) {
        int size = size();
        for (int i = 0; i < size; i++) {
            IDecoration decoration = getDecoration(i);
            if (decoration instanceof IConnectionDecoration) {
                ((IConnectionDecoration) decoration).reroute(figure);
            }
        }
        if (figure != null) {
            repaint(figure);
        }
    }

    @Override
    protected void update(IFigure figure, IDecoration decoration) {
        super.update(figure, decoration);
        updateAnchor(figure, decoration);
        updateConnection(figure, decoration);
    }

    public IAnchor getSourceAnchor() {
        return sourceAnchor;
    }

    protected void update(IFigure figure) {
        for (IDecoration decoration : getDecorations()) {
            if (decoration != null)
                update(figure, decoration);
        }
    }

    protected void updateConnection(IFigure figure, IDecoration decoration) {
        if (decoration instanceof ICorneredDecoration) {
            ((ICorneredDecoration) decoration)
                    .setCornerSize(figure, cornerSize);
        }
        if (decoration instanceof ILineDecoration) {
            ILineDecoration line = (ILineDecoration) decoration;

            // don't update connection color for connections may have different colors
            //line.setLineColor(figure, getLineColor());

            line.setLineStyle(figure, getLineStyle());
            line.setLineWidth(figure, getLineWidth());
        }
        if (decoration instanceof IBranchConnectionDecoration) {
            IBranchConnectionDecoration conn = (IBranchConnectionDecoration) decoration;
            conn.setSourceOrientation(figure, sourceOrientation);
            conn.setSourceExpansion(figure, sourceExpansion);
            conn.setTapered(figure, tapered);
        }

    }

    public void setLineColor(IFigure figure, Color color) {
        if (color == this.lineColor
                || (color != null && color.equals(this.lineColor)))
            return;
        this.lineColor = color;
        if (figure != null) {
            repaint(figure);
        }
        // don't update connection color for connections may have different colors
    }

    public void setLineStyle(IFigure figure, int style) {
        if (style == this.lineStyle)
            return;
        this.lineStyle = style;
        if (figure != null) {
            repaint(figure);
        }
        update(figure);
    }

    /**
     * @see org.xmind.ui.layers.decorations.IBranchConnectionDecoration#setLineWidth(int)
     */
    public void setLineWidth(IFigure figure, int width) {
        if (width == this.lineWidth)
            return;
        this.lineWidth = width;
        if (figure != null) {
            figure.revalidate();
            repaint(figure);
        }
        invalidate();
        update(figure);
    }

    public void setSourceOrientation(IFigure figure, int orientation) {
        if (orientation == this.sourceOrientation)
            return;
        this.sourceOrientation = orientation;
        if (figure != null) {
            figure.revalidate();
            repaint(figure);
        }
        invalidate();
        update(figure);
    }

    public void setSourceExpansion(IFigure figure, int expansion) {
        if (expansion == this.sourceExpansion)
            return;
        this.sourceExpansion = expansion;
        if (figure != null) {
            figure.revalidate();
            repaint(figure);
        }
        invalidate();
        update(figure);
    }

    public void setTapered(IFigure figure, boolean tapered) {
        if (tapered == this.tapered)
            return;
        this.tapered = tapered;
        if (figure != null) {
            figure.revalidate();
            repaint(figure);
        }
        invalidate();
        update(figure);
    }

    public void setCornerSize(IFigure figure, int cornerSize) {
        if (cornerSize == this.cornerSize)
            return;
        this.cornerSize = cornerSize;
        if (figure != null) {
            figure.revalidate();
            repaint(figure);
        }
        invalidate();
        update(figure);
    }

    public Color getLineColor() {
        return lineColor;
    }

    public int getLineStyle() {
        return lineStyle;
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public int getCornerSize() {
        return cornerSize;
    }

    public int getSourceExpansion() {
        return sourceExpansion;
    }

    public int getSourceOrientation() {
        return sourceOrientation;
    }

    public boolean isTapered() {
        return tapered;
    }

//    public int getMinimumMajorSpacing(IFigure figure) {
//        int spacing = 0;
//        for (IDecoration decoration : getDecorations()) {
//            if (decoration instanceof IBranchConnectionDecoration) {
//                int s = ((IBranchConnectionDecoration) decoration)
//                        .getMinimumMajorSpacing(figure);
//                spacing = Math.max(spacing, s);
//            }
//        }
//        return spacing;
//    }

}