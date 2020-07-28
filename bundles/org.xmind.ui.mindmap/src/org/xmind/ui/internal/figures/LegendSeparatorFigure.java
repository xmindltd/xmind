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
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.style.Styles;

public class LegendSeparatorFigure extends Figure {

    public LegendSeparatorFigure() {
        setForegroundColor(ColorUtils.getColor(Styles.LEGEND_LINE_COLOR));
    }

    public Dimension getPreferredSize(int wHint, int hHint) {
        if (wHint < 0)
            wHint = 20;
        return new Dimension(wHint, 3);
    }

    protected void paintFigure(Graphics graphics) {
        super.paintFigure(graphics);
        graphics.setAntialias(SWT.ON);
        Rectangle r = getBounds();
        int y = r.y + r.height / 2;
        graphics.setAlpha(0xc0);
        graphics.drawLine(r.x, y, r.x + r.width, y);
    }

}