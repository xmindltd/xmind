/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.figures;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.DecoratedConnectionFigure;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.draw2d.IShadowedFigure;
import org.xmind.gef.draw2d.ITextFigure;
import org.xmind.gef.draw2d.ITitledFigure;
import org.xmind.gef.draw2d.decoration.IShadowedDecoration;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.ui.decorations.IRelationshipDecoration;

public class RelationshipFigure extends DecoratedConnectionFigure implements
        ITitledFigure, IShadowedFigure {

    private ITextFigure title = null;

    private boolean titleVisible = false;

    public ITextFigure getTitle() {
        return title;
    }

    public void setTitle(ITextFigure title) {
        if (title == this.title)
            return;

        this.title = title;
        revalidate();
        repaint();
    }

    public boolean isTitleVisible() {
        return titleVisible;
    }

    public void setTitleVisible(boolean titleVisible) {
        if (titleVisible == this.titleVisible)
            return;

        this.titleVisible = titleVisible;
        revalidate();
        repaint();
    }

    protected void layout() {
        super.layout();
        if (title != null) {
            PrecisionPoint pos = getDecoration().getTitlePosition(this);
            Rectangle r2;
            if (title instanceof IReferencedFigure) {
                r2 = ((IReferencedFigure) title).getPreferredBounds(pos
                        .toDraw2DPoint());
            } else {
                Dimension size = title.getPreferredSize();
                r2 = Rectangle.SINGLETON;
                r2.setSize(size);
                r2.setLocation((int) (pos.x - size.width / 2.0),
                        (int) (pos.y - size.height / 2.0));
            }
            title.setBounds(r2);
        }
    }

    public Rectangle getPreferredBounds() {
        Rectangle r = super.getPreferredBounds();
        if (title != null) {
            PrecisionPoint pos = getDecoration().getTitlePosition(this);
            Rectangle r2;
            if (title instanceof IReferencedFigure) {
                r2 = ((IReferencedFigure) title).getPreferredBounds(pos
                        .toDraw2DPoint());
            } else {
                Dimension size = title.getPreferredSize();
                r2 = Rectangle.SINGLETON;
                r2.setSize(size);
                r2.setLocation((int) (pos.x - size.width / 2.0),
                        (int) (pos.y - size.height / 2.0));
            }
            r.union(r2);
        }
        return r;
    }

    public IRelationshipDecoration getDecoration() {
        return (IRelationshipDecoration) super.getDecoration();
    }

    public boolean isShadowShowing() {
        return isShowing();
    }

    public void paintShadow(Graphics graphics) {
        if (getDecoration() != null
                && getDecoration() instanceof IShadowedDecoration) {
            ((IShadowedDecoration) getDecoration()).paintShadow(this, graphics);
        }
    }

    public String toString() {
        if (title != null)
            return title.getText();
        return super.toString();
    }

}