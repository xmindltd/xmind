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

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;
import org.xmind.gef.draw2d.IDecoratedFigure;
import org.xmind.gef.draw2d.IDecoratedFigureListener;
import org.xmind.gef.draw2d.decoration.IConnectionDecorationEx;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.draw2d.decoration.IShapeDecorationEx;
import org.xmind.gef.draw2d.graphics.GraphicsUtils;
import org.xmind.gef.service.AbstractFeedback;

public abstract class DecoratedFeedback extends AbstractFeedback {

    protected class DecoratedFigure extends Figure implements IDecoratedFigure {

        public IDecoration getDecoration() {
            return decoration;
        }

        public void paint(Graphics graphics) {
            GraphicsUtils.fixGradientBugForCarbon(graphics, this);
            super.paint(graphics);
        }

        protected void paintFigure(Graphics graphics) {
            super.paintFigure(graphics);
            if (decoration != null) {
                graphics.setAntialias(SWT.ON);
                paintDecoration(graphics, this, decoration);
            }
        }

        public void invalidate() {
            super.invalidate();
            if (decoration != null) {
                decoration.invalidate();
            }
        }

        public void addDecoratedFigureListener(IDecoratedFigureListener listener) {
        }

        public void removeDecoratedFigureListener(
                IDecoratedFigureListener listener) {
        }

    }

    private IDecoration decoration;

    private DecoratedFigure figure = null;

    protected void updateDecoration() {
        if (figure == null)
            return;

        String newDecorationId = getNewDecorationId();
        String oldDecorationId = this.decoration == null ? null
                : this.decoration.getId();
        if (!equals(oldDecorationId, newDecorationId)) {
            if (this.decoration != null) {
                disposeOldDecoration(figure, decoration);
            }
            this.decoration = createNewDecoration(figure, newDecorationId);
        }
        if (decoration != null)
            updateDecoration(figure, decoration);
    }

    protected abstract String getNewDecorationId();

    protected abstract IDecoration createNewDecoration(IFigure figure,
            String decorationId);

    protected abstract void updateDecoration(IFigure figure,
            IDecoration decoration);

    protected abstract void disposeOldDecoration(IFigure figure,
            IDecoration decoration);

    public DecoratedFigure getFigure() {
        return figure;
    }

    public IDecoration getDecoration() {
        return decoration;
    }

    protected DecoratedFigure createDecoratedFigure() {
        return new DecoratedFigure();
    }

    public void addToLayer(IFigure layer) {
        if (figure == null) {
            figure = createDecoratedFigure();
            updateDecoration();
        }
        layer.add(figure);
    }

    public boolean containsPoint(Point point) {
        if (figure != null && decoration != null) {
            if (decoration instanceof IShapeDecorationEx) {
                return ((IShapeDecorationEx) decoration).containsPoint(figure,
                        point.x, point.y);
            } else if (decoration instanceof IConnectionDecorationEx) {
                return ((IConnectionDecorationEx) decoration).containsPoint(
                        figure, point.x, point.y);
            }
        }
        return false;
    }

    public void removeFromLayer(IFigure layer) {
        if (figure != null && figure.getParent() == layer) {
            if (decoration != null) {
                disposeOldDecoration(figure, decoration);
            }
            layer.remove(figure);
        }
        decoration = null;
        figure = null;
    }

    public void update() {
        updateDecoration();
        if (figure != null) {
            updateBounds(figure);
        }
    }

    protected void paintDecoration(Graphics graphics, IFigure figure,
            IDecoration decoration) {
        decoration.paint(figure, graphics);
    }

    protected abstract void updateBounds(IFigure figure);

    private static boolean equals(String s1, String s2) {
        return s1 == s2 || (s1 != null && s1.equals(s2));
    }

}