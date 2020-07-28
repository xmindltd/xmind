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
package org.xmind.ui.texteditor;

import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Font;
import org.xmind.gef.draw2d.IRotatableFigure;
import org.xmind.gef.draw2d.geometry.PrecisionDimension;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.geometry.PrecisionRectangle;
import org.xmind.gef.draw2d.geometry.PrecisionRotator;
import org.xmind.ui.resources.FontUtils;

public class FloatingTextEditorHelper extends FloatingTextEditorHelperBase
        implements FigureListener {

    private IFigure figure = null;

    private PrecisionRotator rotator = null;

    public FloatingTextEditorHelper() {
        super();
    }

    public FloatingTextEditorHelper(boolean extendsBidirectionalHorizontal) {
        super(extendsBidirectionalHorizontal);
    }

    public IFigure getFigure() {
        return figure;
    }

    public void setFigure(IFigure figure) {
        this.figure = figure;
    }

    public PrecisionRotator getRotator() {
        if (rotator == null)
            rotator = new PrecisionRotator();
        return rotator;
    }

    public void figureMoved(IFigure source) {
        refreshEditor(false);
    }

    public void activate() {
        super.activate();
        if (figure != null) {
            figure.addFigureListener(this);
        }

        if (getEditor() != null && getViewer() != null && getFigure() != null) {
            Rectangle b = figure.getBounds();
            Point loc = getViewer().computeToControl(b.getLocation(), true);
            getEditor().setInitialLocation(
                    new org.eclipse.swt.graphics.Point(loc.x, loc.y));
            getEditor().setInitialSize(
                    new org.eclipse.swt.graphics.Point(b.width, b.height));
        }
    }

    public void deactivate() {
        if (figure != null) {
            figure.removeFigureListener(this);
            figure = null;
        }
        super.deactivate();
    }

    protected Font getPreferredFont() {
        if (getFigure() == null)
            return null;
        return getPreferredFont(getFigure());
    }

    protected Font getPreferredFont(IFigure figure) {
        Font font = figure.getFont();
        if (font == null)
            return null;
        int height = font.getFontData()[0].getHeight();
        height *= getScale();
        return FontUtils.getNewHeight(font, height);
    }

    protected Rectangle getPreferredBounds() {
        if (figure == null)
            return null;
        return calcPreferredBounds(figure, figure.getBounds());
    }

    protected Rectangle calcPreferredBounds(IFigure figure, Rectangle bounds) {
        if (figure instanceof IRotatableFigure
                && ((IRotatableFigure) figure).getRotationDegrees() != 0) {
            IRotatableFigure rf = (IRotatableFigure) figure;
            PrecisionRectangle r = new PrecisionRectangle(bounds);
            PrecisionPoint center = r.getCenter();
            getRotator().setOrigin(center);
            getRotator().setAngle(rf.getRotationDegrees());
            PrecisionDimension d = rf.getNormalPreferredSize(-1, -1);
            PrecisionRectangle r2 = getRotator().r(r, -1, d.height);
            return r2.toDraw2DRectangle();
        }
        return bounds.getCopy();
    }

}
