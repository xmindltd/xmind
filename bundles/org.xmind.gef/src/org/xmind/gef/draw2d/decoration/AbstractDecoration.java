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
package org.xmind.gef.draw2d.decoration;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;

public abstract class AbstractDecoration implements IDecoration {

    private String id;

    private int alpha = 0xFF;

    private boolean visible = true;

    private boolean valid = false;

    protected AbstractDecoration() {
    }

    protected AbstractDecoration(String id) {
        this.id = id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    /**
     * @see org.xmind.gef.draw2d.decoration.IDecoration#getAlpha()
     */
    public int getAlpha() {
        return alpha;
    }

    public void invalidate() {
        setValid(false);
    }

    protected boolean isValid() {
        return valid;
    }

    /**
     * @see org.xmind.gef.draw2d.decoration.IDecoration#isVisible()
     */
    public boolean isVisible() {
        return visible;
    }

    public void paint(IFigure figure, Graphics graphics) {
        if (!isVisible())
            return;
        checkValidation(figure);
        if (!isVisible())
            return;
        graphics.pushState();
        try {
            performPaint(figure, graphics);
        } finally {
            graphics.popState();
        }
    }

    /**
     * @param figure
     */
    protected void checkValidation(IFigure figure) {
        if (!isValid()) {
            validate(figure);
        }
    }

    public void validate(IFigure figure) {
        setValid(true);
    }

    protected abstract void performPaint(IFigure figure, Graphics graphics);

    /**
     * @see org.xmind.gef.draw2d.decoration.IDecoration#setAlpha(int)
     */
    public void setAlpha(IFigure figure, int alpha) {
        if (alpha == this.alpha)
            return;
        this.alpha = alpha;
        if (figure != null) {
            repaint(figure);
        }
    }

    protected void repaint(IFigure figure) {
        figure.repaint();
    }

    protected void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * @see org.xmind.gef.draw2d.decoration.IDecoration#setVisible(boolean)
     */
    public void setVisible(IFigure figure, boolean visible) {
        if (visible == this.visible)
            return;
        this.visible = visible;
        if (figure != null) {
            repaint(figure);
        }
    }

}