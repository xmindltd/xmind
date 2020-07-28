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
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.decoration.ICorneredDecoration;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.AbstractTopicDecoration;

public class RoundedRectTopicDecoration extends AbstractTopicDecoration
        implements ICorneredDecoration {

    private static final double M = (1 - Math.sqrt(2) / 2) * 0.8;

    private int cornerSize = 0;

    public RoundedRectTopicDecoration() {
        super();
    }

    public RoundedRectTopicDecoration(String id) {
        super(id);
    }

    protected void sketch(IFigure figure, Path shape, Rectangle box,
            int purpose) {
        float c = getAppliedCornerSize();
        if (c == 0) {
            shape.addRectangle(box.x, box.y, box.width, box.height);
        } else {
            if (purpose == CHECK) {
                shape.addRoundedRectangle(
                        box.getExpanded(getLineWidth() / 2, getLineWidth() / 2),
                        c);
            } else {
                shape.addRoundedRectangle(box, c);
            }
        }
    }

    public Insets getPreferredInsets(IFigure figure, int width, int height) {
        int c = (int) (M * getAppliedCornerSize()) + getLineWidth();
        return Geometry.union(super.getPreferredInsets(figure, width, height),
                c, c, c, c);
    }

    public int getCornerSize() {
        return cornerSize;
    }

    protected int getAppliedCornerSize() {
        return getCornerSize();// * getLineWidth();
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