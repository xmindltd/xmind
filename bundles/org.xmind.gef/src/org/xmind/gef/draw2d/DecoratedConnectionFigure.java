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
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.xmind.gef.draw2d.decoration.IConnectionDecorationEx;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.draw2d.graphics.GraphicsUtils;

public class DecoratedConnectionFigure extends ConnectionFigure implements
        IDecoratedFigure {

    private IConnectionDecorationEx decoration;

    public IConnectionDecorationEx getDecoration() {
        return decoration;
    }

    public void setDecoration(IConnectionDecorationEx decoration) {
        IConnectionDecorationEx oldDecoration = this.decoration;
        if (decoration == oldDecoration)
            return;

        if (oldDecoration != null) {
            connectionRemoved(oldDecoration);
        }
        this.decoration = decoration;
        if (decoration != null) {
            connectionAdded(decoration);
        }
        revalidate();
        fireDecorationChanged(oldDecoration, decoration);
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

    protected void connectionRemoved(IConnectionDecorationEx connection) {
        connection.setSourceAnchor(this, null);
        connection.setTargetAnchor(this, null);
    }

    protected void connectionAdded(IConnectionDecorationEx connection) {
        connection.setSourceAnchor(this, getSourceAnchor());
        connection.setTargetAnchor(this, getTargetAnchor());
    }

    @Override
    public void setSourceAnchor(IAnchor anchor) {
        super.setSourceAnchor(anchor);
        if (decoration != null) {
            decoration.setSourceAnchor(this, anchor);
        }
    }

    @Override
    public void setTargetAnchor(IAnchor anchor) {
        super.setTargetAnchor(anchor);
        if (decoration != null) {
            decoration.setTargetAnchor(this, anchor);
        }
    }

    public void setBounds(Rectangle rect) {
        super.setBounds(rect);
        repaint();
    }

    public Rectangle getPreferredBounds() {
        if (decoration != null) {
            Rectangle r = decoration.getPreferredBounds(this);
            if (r != null)
                return r;
        }
        return getBounds();
    }

    public boolean containsPoint(int x, int y) {
        if (decoration != null) {
            return decoration.containsPoint(this, x, y);
        }
        return false;
    }

    public void invalidate() {
        if (decoration != null) {
            decoration.invalidate();
        }
        super.invalidate();
    }

    protected void layout() {
        super.layout();
        setBounds(getPreferredBounds());
    }

    public void anchorMoved(IAnchor anchor) {
        super.anchorMoved(anchor);
        if (anchor.getOwner() != this) {
            if (decoration != null) {
                decoration.reroute(this);
            }
            setBounds(getPreferredBounds());
        }
    }

    public void paint(Graphics graphics) {
        GraphicsUtils.fixGradientBugForCarbon(graphics, this);
        super.paint(graphics);
    }

    protected void paintFigure(Graphics graphics) {
        super.paintFigure(graphics);
        graphics.setAntialias(SWT.ON);
        if (decoration != null)
            decoration.paint(this, graphics);
    }

}