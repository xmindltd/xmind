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
package org.xmind.ui.internal.figures;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.swt.SWT;
import org.xmind.gef.draw2d.DecoratedShapeFigure;
import org.xmind.gef.draw2d.IMinimizable;
import org.xmind.gef.draw2d.IShadowedFigure;
import org.xmind.gef.draw2d.ITextFigure;
import org.xmind.gef.draw2d.ITitledFigure;
import org.xmind.gef.draw2d.decoration.IShadowedDecoration;
import org.xmind.ui.decorations.IBoundaryDecoration;

public class BoundaryFigure extends DecoratedShapeFigure
        implements ITitledFigure, IMinimizable, IShadowedFigure {

    protected static final int FLAG_MINIMIZED = MAX_FLAG << 1;

    static {
        MAX_FLAG = FLAG_MINIMIZED;
    }

    private ITextFigure title = null;

    private boolean isTitleVisible = false;

    public ITextFigure getTitle() {
        return title;
    }

    public boolean isShadowShowing() {
        return isShowing();
    }

    public void setTitle(ITextFigure title) {
        if (title == this.title)
            return;

        this.title = title;
        revalidate();
        repaint();
    }

    public boolean isTitleVisible() {
        return isTitleVisible;
    }

    public void setTitleVisible(boolean isTitleVisible) {
        if (isTitleVisible == this.isTitleVisible)
            return;

        this.isTitleVisible = isTitleVisible;
        revalidate();
        repaint();
    }

    public IBoundaryDecoration getDecoration() {
        return (IBoundaryDecoration) super.getDecoration();
    }

    protected Insets calculatePreferredInsets() {
        if (isMinimized())
            return NO_INSETS;
        Insets ins = super.calculatePreferredInsets();
//        if (isTitleVisible() && title != null) {
//            Dimension s = title.getPreferredSize();
//            ins = new Insets(ins);
//            ins.top = Math.max(s.height, ins.top);
//            ins.left += 5;
//        }
        return ins;
    }

    protected void layout() {
        super.layout();
        if (title != null && title.getParent() == this) {
            Dimension size = title.getPreferredSize(getBounds().width,
                    SWT.DEFAULT);
            if (size.width > getBounds().width) {
                size = new Dimension(getBounds().width, size.height);
            }
            title.setSize(size);
            title.setLocation(getBounds().getLocation());
        }
    }

    public boolean isMinimized() {
        return getFlag(FLAG_MINIMIZED);
    }

    public void setMinimized(boolean minimized) {
        if (minimized == isMinimized())
            return;

        setFlag(FLAG_MINIMIZED, minimized);
        revalidate();
        repaint();
    }

    public void paintShadow(Graphics graphics) {
        if (getDecoration() != null
                && getDecoration() instanceof IShadowedDecoration) {
            ((IShadowedDecoration) getDecoration()).paintShadow(this, graphics);
        }
    }

    public String toString() {
        if (title != null)
            return "BoundaryFigure(" + title.getText() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        return super.toString();
    }

}