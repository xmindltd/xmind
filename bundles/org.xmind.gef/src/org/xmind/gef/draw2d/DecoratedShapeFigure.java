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
package org.xmind.gef.draw2d;

import java.util.Iterator;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.draw2d.decoration.IShapeDecorationEx;
import org.xmind.gef.draw2d.graphics.GraphicsUtils;

public class DecoratedShapeFigure extends ReferencedFigure implements
        IDecoratedFigure {

    private IShapeDecorationEx shape = null;

    private Insets prefInsets = null;

    private Insets insets = null;

    public IShapeDecorationEx getDecoration() {
        return shape;
    }

    public void setDecoration(IShapeDecorationEx shape) {
        IShapeDecorationEx oldShape = this.shape;
        if (shape == oldShape)
            return;

        if (oldShape != null) {
            oldShape.invalidate();
        }
        this.shape = shape;
        revalidate();
        fireDecorationChanged(oldShape, shape);
        repaint();
    }

    public void addDecoratedFigureListener(IDecoratedFigureListener listener) {
        addListener(IDecoratedFigureListener.class, listener);
    }

    public void removeDecoratedFigureListener(IDecoratedFigureListener listener) {
        removeListener(IDecoratedFigureListener.class, listener);
    }

    protected void fireDecorationChanged(IDecoration oldDecoration,
            IDecoration newDecoration) {
        Iterator listeners = getListeners(IDecoratedFigureListener.class);
        while (listeners.hasNext()) {
            ((IDecoratedFigureListener) listeners.next()).decorationChanged(
                    this, oldDecoration, newDecoration);
        }
    }

    public boolean containsPoint(int x, int y) {
        if (shape != null)
            return shape.containsPoint(this, x, y);
        return super.containsPoint(x, y);
    }

    public Insets getInsets() {
        if (insets != null)
            return insets;
        return getPreferredInsets();
    }

    public void setInsets(Insets ins) {
        if (ins == this.insets || (ins != null && ins.equals(this.insets)))
            return;
        if (ins == null) {
            this.insets = null;
        } else {
            if (this.insets != null) {
                this.insets.top = ins.top;
                this.insets.bottom = ins.bottom;
                this.insets.left = ins.left;
                this.insets.right = ins.right;
            } else {
                this.insets = new Insets(ins);
            }
        }
        revalidate();
    }

    public Insets getPreferredInsets() {
        if (prefInsets == null)
            prefInsets = calculatePreferredInsets();
        return prefInsets;
    }

    protected Insets calculatePreferredInsets() {
        if (shape != null) {
            int wHint, hHint;
            if (getLayoutManager() instanceof IReferencedLayout) {
                Rectangle area = ((IReferencedLayout) getLayoutManager())
                        .getPreferredClientArea(this);
                wHint = area.width;
                hHint = area.height;
            } else {
                wHint = 0;
                hHint = 0;
            }
            return shape.getPreferredInsets(this, wHint, hHint);
        }
        return NO_INSETS;
    }

    public void invalidate() {
        if (shape != null)
            shape.invalidate();
        prefInsets = null;
        super.invalidate();
    }

    public void paint(Graphics graphics) {
        GraphicsUtils.fixGradientBugForCarbon(graphics, this);
        super.paint(graphics);
    }

    protected void paintFigure(Graphics graphics) {
        super.paintFigure(graphics);
        graphics.setAntialias(SWT.ON);
        if (shape != null)
            shape.paint(this, graphics);
    }
}