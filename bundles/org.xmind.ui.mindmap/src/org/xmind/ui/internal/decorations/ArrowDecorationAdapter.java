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
import org.xmind.gef.draw2d.decoration.ICorneredDecoration;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.AbstractArrowDecoration;

public class ArrowDecorationAdapter extends AbstractArrowDecoration implements
        ICorneredDecoration {

    private IDecoration decoration;

    public ArrowDecorationAdapter(IDecoration decoration) {
        this.decoration = decoration;
    }

    protected void sketch(IFigure figure, Path shape) {
    }

    public void reshape(IFigure figure) {
    }

    public int getAlpha() {
        return decoration.getAlpha();
    }

    public String getId() {
        return decoration.getId();
    }

    public void invalidate() {
        decoration.invalidate();
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

    public void setId(String id) {
        decoration.setId(id);
    }

    public void setVisible(IFigure figure, boolean visible) {
        decoration.setVisible(figure, visible);
    }

    public void validate(IFigure figure) {
        decoration.validate(figure);
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
        if (obj instanceof ArrowDecorationAdapter) {
            obj = ((ArrowDecorationAdapter) obj).decoration;
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