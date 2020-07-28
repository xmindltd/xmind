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
package org.xmind.gef.draw2d;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;

import org.xmind.gef.draw2d.graphics.AlphaGraphics;

public class TransparentableLayer extends Figure implements
        ITransparentableFigure {
    
    private int mainAlpha = 0xff;
    
    private int subAlpha = 0xff;

    public int getMainAlpha() {
        return mainAlpha;
    }

    public int getSubAlpha() {
        return subAlpha;
    }

    public void setMainAlpha(int alpha) {
        if (alpha == mainAlpha)
            return;
        this.mainAlpha = alpha;
        repaint();
    }

    public void setSubAlpha(int alpha) {
        if (alpha == subAlpha)
            return;
        this.subAlpha = alpha;
        repaint();
    }
    
    /**
     * @see org.eclipse.draw2d.Figure#paint(org.eclipse.draw2d.Graphics)
     */
    @Override
    public void paint( Graphics graphics ) {
        Graphics g = createAlphaGraphics( graphics, getMainAlpha() );
        super.paint( g );
        g.dispose();
    }
    
    protected void paintClientArea( Graphics graphics ) {
        Graphics g = createAlphaGraphics( graphics, getSubAlpha() );
        super.paintClientArea( g );
        g.dispose();
    }
    
    protected Graphics createAlphaGraphics( Graphics graphics, int alpha ) {
        AlphaGraphics ag = new AlphaGraphics( graphics );
        ag.setMainAlpha( alpha );
        return ag;
    }

}