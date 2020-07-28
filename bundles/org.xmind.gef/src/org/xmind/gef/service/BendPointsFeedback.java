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
package org.xmind.gef.service;

import org.eclipse.swt.graphics.Color;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.draw2d.PointFigure;

public class BendPointsFeedback extends AbstractBendPointsFeedback {

    private int pointHeight;

    private Color borderColor;

    private Color fillColor;

    private int alpha;

    protected IReferencedFigure createPointFigure(int orientation) {
        PointFigure f = new PointFigure(PointFigure.SHAPE_SQUARE);
        f.setSize(pointHeight, pointHeight);
        f.setForegroundColor(borderColor);
        f.setBackgroundColor(fillColor);
        f.setMainAlpha(alpha);
        f.setSubAlpha(alpha);
        return f;
    }

    protected void updatePointFigure(IReferencedFigure figure, int orientation) {
        PointFigure f = (PointFigure) figure;
        f.setForegroundColor(borderColor);
        f.setBackgroundColor(fillColor);
        f.setMainAlpha(alpha);
        f.setSubAlpha(alpha);
    }

    public void setPointHeight(int pointHeight) {
        this.pointHeight = pointHeight;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

}