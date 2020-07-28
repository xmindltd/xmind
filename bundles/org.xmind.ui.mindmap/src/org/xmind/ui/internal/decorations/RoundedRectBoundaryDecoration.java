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

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.decoration.ICorneredDecoration;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.AbstractBoundaryDecoration;

public class RoundedRectBoundaryDecoration extends AbstractBoundaryDecoration
        implements ICorneredDecoration {

    private int cornerSize = 0;

    public RoundedRectBoundaryDecoration() {
        super();
    }

    public RoundedRectBoundaryDecoration(String id) {
        super(id);
    }

    protected void sketch(IFigure figure, Path shape, Rectangle box,
            int purpose) {
        int c = getAppliedCornerSize();
        if (c == 0) {
            shape.addRectangle(box);
        } else {
            shape.addRoundedRectangle(box, c);
        }
    }

    public int getCornerSize() {
        return cornerSize;
    }

    protected int getAppliedCornerSize() {
        return getCornerSize();
    }

    public void setCornerSize(IFigure figure, int cornerSize) {
        if (cornerSize == this.cornerSize)
            return;

        this.cornerSize = cornerSize;
        invalidate();
        if (figure != null) {
            figure.revalidate();
            figure.repaint();
        }
    }

}