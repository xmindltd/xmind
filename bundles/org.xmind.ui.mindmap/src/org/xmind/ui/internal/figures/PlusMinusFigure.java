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

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.resources.ColorUtils;

public class PlusMinusFigure extends Figure {

    private static final float MARGIN = 2.5f;

    private static final int FLAG_PLUS_MINUS;
    private static final int FLAG_PRESELECTED;

    static {
        FLAG_PLUS_MINUS = MAX_FLAG << 1;
        FLAG_PRESELECTED = MAX_FLAG << 2;
        MAX_FLAG = FLAG_PRESELECTED;
    }

//    private static final Color FillPlus = ColorUtils.getColor(200, 228, 248);
//    private static final Color FillPlus2 = ColorUtils.getColor(160, 196, 234);
//    private static final Color FillMinus = ColorUtils.getColor(210, 230, 255);
//    private static final Color FillMinus2 = ColorUtils.getColor(180, 210, 240);
    private static final Color Fill1 = ColorUtils.getColor(160, 196, 234);
    private static final Color Fill2 = ColorUtils.getColor(250, 250, 250);
    private static final Color BorderPlus = ColorUtils.getColor(120, 136, 162);
    private static final Color BorderMinus = ColorUtils.getColor(180, 200, 240);
    private static final Color ContentPlus = ColorUtils.getColor(48, 64, 96);
    private static final Color ContentMinus = ColorUtils
            .getColor(150, 160, 200);

    private Color borderValue;

    public PlusMinusFigure() {
    }

    public PlusMinusFigure(boolean plusOrMinus) {
        setFlag(FLAG_PLUS_MINUS, plusOrMinus);
    }

    public void setBorderValue(Color borderValue) {
        this.borderValue = borderValue;
    }

    public Color getBorderValue() {
        return borderValue;
    }

    /**
     * Returns whether the current value is '+' or '-'.
     * 
     * @return <code>true</code> for '+' or <code>false</code> for '-'
     */
    public boolean getValue() {
        return getFlag(FLAG_PLUS_MINUS);
    }

    /**
     * Sets the current value to '+' or '-'.
     * 
     * @param plusOrMinus
     *            <code>true</code> for '+' or <code>false</code> for '-'
     */
    public void setValue(boolean plusOrMinus) {
        boolean currentState = getValue();
        if (plusOrMinus == currentState)
            return;

        setFlag(FLAG_PLUS_MINUS, plusOrMinus);
        repaint();
    }

    public boolean isPreselected() {
        return getFlag(FLAG_PRESELECTED);
    }

    public void setPreselected(boolean preselected) {
        boolean currentPreselection = isPreselected();
        if (preselected == currentPreselection)
            return;

        setFlag(FLAG_PRESELECTED, preselected);
        repaint();
    }

    protected Color getFillColor() {
        return isPreselected() ? Fill1 : Fill2;
    }

    protected Color getBorderColor() {
        return getBorderValue() != null ? getBorderValue()
                : (getValue() ? BorderPlus : BorderMinus);
    }

    protected Color getContentColor() {
        return getValue() ? ContentPlus : ContentMinus;
    }

    protected void paintFigure(Graphics g) {
        g.setAntialias(SWT.ON);
        super.paintFigure(g);
        g.setLineWidth(1);
        g.setLineStyle(SWT.LINE_SOLID);

        Rectangle rect = getBounds();
        float l = rect.x + 0.5f;
        float t = rect.y + 0.5f;
        float r = rect.right() - 1.5f;
        float b = rect.bottom() - 1.5f;

        Path p = new Path(Display.getCurrent());
        p.addArc(l, t, r - l, b - t, 0, 360);

        g.setBackgroundColor(getFillColor());
        g.fillPath(p);

        g.setForegroundColor(getBorderColor());
        g.drawPath(p);

        p.dispose();

        g.setForegroundColor(getContentColor());

        p = new Path(Display.getCurrent());
        float centerY = (t + b) / 2;
        p.moveTo(l + MARGIN, centerY);
        p.lineTo(r - MARGIN, centerY);
        if (getValue()) {
            float centerX = (l + r) / 2;
            p.moveTo(centerX, t + MARGIN);
            p.lineTo(centerX, b - MARGIN);
        }
        g.drawPath(p);
        p.dispose();
    }

}