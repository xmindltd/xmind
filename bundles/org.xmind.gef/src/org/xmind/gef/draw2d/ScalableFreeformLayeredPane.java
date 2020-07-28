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

import org.eclipse.draw2d.Graphics;
import org.xmind.gef.draw2d.graphics.ScaledGraphics;

public class ScalableFreeformLayeredPane
        extends org.eclipse.draw2d.ScalableFreeformLayeredPane {

    protected void paintClientArea(Graphics graphics) {
        if (getChildren().isEmpty())
            return;
        if (getScale() == 1.0) {
            super.paintClientArea(graphics);
        } else {
            ScaledGraphics sg = ScaledGraphics.SCALED_GRAPHICS_ENABLED
                    ? createScaledGraphics(graphics) : null;
            Graphics g = sg == null ? graphics : sg;
            boolean optimizeClip = getBorder() == null
                    || getBorder().isOpaque();
            if (!optimizeClip)
                g.clipRect(getBounds().getShrinked(getInsets()));
            g.scale(getScale());
            g.pushState();
            paintChildren(g);
            g.popState();
            if (sg != null) {
                sg.dispose();
            }
            graphics.restoreState();
        }
    }

    protected ScaledGraphics createScaledGraphics(Graphics graphics) {
        return new ScaledGraphics(graphics);
    }

}