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
package org.xmind.gef.draw2d.geometry;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

public class FigureBoundsProvider implements IBoundsProvider {

    public Rectangle getPrefBounds(Object host, Point reference) {
        if (host instanceof IFigure) {
            IFigure figure = (IFigure) host;
            Dimension size = figure.getPreferredSize();
            return new Rectangle(reference.x - size.width / 2, reference.y
                    - size.height / 2, size.width, size.height);
        }
        return new Rectangle(reference.x, reference.y, 0, 0);
    }

}