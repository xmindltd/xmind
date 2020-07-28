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
package org.xmind.ui.decorations;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.ITextFigure;
import org.xmind.gef.draw2d.ITitledFigure;
import org.xmind.gef.draw2d.decoration.IShadowedDecoration;
import org.xmind.gef.draw2d.decoration.PathShapeDecoration;
import org.xmind.gef.draw2d.graphics.Path;

public abstract class AbstractBoundaryDecoration extends PathShapeDecoration
        implements IBoundaryDecoration, IShadowedDecoration {

    private static final Rectangle CLIP = new Rectangle();

    protected static final int MARGIN_WIDTH = 10;

    protected static final int MARGIN_HEIGHT = 10;

    protected static final int TITLE_MARGIN = 5;

    protected AbstractBoundaryDecoration() {
        super();
    }

    protected AbstractBoundaryDecoration(String id) {
        super(id);
    }

    public boolean containsPoint(IFigure figure, int x, int y) {
        return containsPoint(figure, x, y, true);
    }

    protected int getCheckingLineWidth() {
        return super.getCheckingLineWidth() * 2 + 4;
    }

    protected void paintPath(IFigure figure, Graphics graphics, Path path,
            boolean fill) {
        ITextFigure titleFigure = getTitleFigure(figure);
        if (titleFigure != null && titleFigure.isShowing()) {
            Rectangle bounds = figure.getBounds();
            Rectangle titleArea = titleFigure.getBounds();
            if (titleArea.intersects(bounds)) {
                graphics.pushState();
                try {
                    paintPathAroundTitle(figure, graphics, path, fill, bounds,
                            titleArea);
                } finally {
                    graphics.popState();
                }
                return;
            }
        }
        super.paintPath(figure, graphics, path, fill);
    }

    protected void paintPathAroundTitle(IFigure figure, Graphics graphics,
            Path path, boolean fill, Rectangle bounds, Rectangle titleArea) {
        int w;
        int h;
        // clip the top-right part
        w = bounds.x + bounds.width - titleArea.x - titleArea.width;
        h = titleArea.y + titleArea.height - bounds.y;
        if (w > 0 && h > 0) {
            CLIP.setSize(w, h);
            CLIP.setLocation(titleArea.x + titleArea.width, bounds.y);
            paintPathWithClip(figure, graphics, path, fill, CLIP);
        }
        // clip the bottom part
        w = bounds.width;
        h = bounds.y + bounds.height - titleArea.y - titleArea.height;
        if (w > 0 && h > 0) {
            CLIP.setSize(w, h);
            CLIP.setLocation(bounds.x, titleArea.y + titleArea.height);
            paintPathWithClip(figure, graphics, path, fill, CLIP);
        }
    }

    protected void paintPathWithClip(IFigure figure, Graphics graphics,
            Path path, boolean fill, Rectangle clip) {
        graphics.clipRect(clip);
        super.paintPath(figure, graphics, path, fill);
        graphics.restoreState();
    }

    protected ITextFigure getTitleFigure(IFigure figure) {
        if (figure instanceof ITitledFigure) {
            return ((ITitledFigure) figure).getTitle();
        }
        return null;
    }

    protected Rectangle getOutlineBox(IFigure figure) {
        Rectangle r = super.getOutlineBox(figure);
        ITextFigure titleFigure = getTitleFigure(figure);
        if (titleFigure != null && titleFigure.isShowing()) {
            r.x += 5;
            r.width -= 5;
            int height = titleFigure.getPreferredSize().height;
            height = Math.min(5, height / 2);
            r.y += height;
            r.height -= height;
        }
        return r;
    }

    public void paintShadow(IFigure figure, Graphics graphics) {
        if (!isVisible())
            return;
        checkValidation(figure);
        graphics.setAlpha(getAlpha());
        graphics.setBackgroundColor(ColorConstants.black);
        graphics.setForegroundColor(ColorConstants.black);
        paintFill(figure, graphics);
    }

    public Insets getPreferredInsets(IFigure figure, int width, int height) {
        return new Insets(MARGIN_HEIGHT, MARGIN_WIDTH, MARGIN_HEIGHT,
                MARGIN_WIDTH);
    }

}