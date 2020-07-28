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
package org.xmind.ui.internal.views;

import java.util.HashMap;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.xmind.core.style.IStyle;
import org.xmind.gef.draw2d.graphics.GraphicsUtils;

public class StyleFigure extends Figure {

    private static final Rectangle RECT = new Rectangle();

    private IStyle style;

    private HashMap<String, String> existedStyle;

    public StyleFigure() {
    }

    public IStyle getStyle() {
        return style;
    }

    public void setStyle(IStyle style) {
        if (style == this.style)
            return;

        this.style = style;
        repaint();
    }

    public void setExistedStyle(HashMap<String, String> existedStyle) {
        this.existedStyle = existedStyle;
        repaint();
    }

    public void paint(Graphics graphics) {
        GraphicsUtils.fixGradientBugForCarbon(graphics, this);
        super.paint(graphics);
    }

    protected void paintFigure(Graphics graphics) {
        super.paintFigure(graphics);
        drawStyle(graphics);
    }

    protected void drawStyle(Graphics graphics) {
        if (style == null)
            return;

        graphics.setAntialias(SWT.ON);
        graphics.setTextAntialias(SWT.ON);

        Rectangle r = getClientArea(RECT);
        drawStyle(graphics, style, r);
    }

    private void drawStyle(Graphics graphics, IStyle style, Rectangle r) {
        String type = style.getType();
        if (IStyle.TOPIC.equalsIgnoreCase(type)) {
            StyleFigureUtils.drawTopic(graphics, r, existedStyle, style, null,
                    false, false);
            StyleFigureUtils.drawStyleText(graphics, "Topic", r, //$NON-NLS-1$
                    existedStyle, style, StyleFigureUtils.defaultMainStyle);
        } else if (IStyle.BOUNDARY.equalsIgnoreCase(type)) {
            StyleFigureUtils.drawBoundary(graphics, r, existedStyle, style,
                    null);
        } else if (IStyle.RELATIONSHIP.equalsIgnoreCase(type)) {
            StyleFigureUtils.drawRelationship(graphics, r, existedStyle, style,
                    null);
        } else if (IStyle.MAP.equalsIgnoreCase(type)) {
            StyleFigureUtils.drawSheetBackground(graphics, r, existedStyle,
                    style, null, true);
        }
    }

}