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
package org.xmind.gef.acc;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.IViewer;
import org.xmind.gef.part.IGraphicalPart;

public abstract class AbstractGraphicalAccessible extends AbstractAccessible {

    public AbstractGraphicalAccessible(IGraphicalPart host) {
        super(host);
    }

    public IGraphicalPart getHost() {
        return (IGraphicalPart) super.getHost();
    }

    public Rectangle getLocation() {
        IFigure figure = getHost().getFigure();
        if (figure == null)
            return null;
        IViewer viewer = getHost().getSite().getViewer();
        if (viewer == null)
            return null;

        Rectangle bounds = figure.getBounds().getCopy();
        figure.translateToAbsolute(bounds);
        if (viewer instanceof IGraphicalViewer) {
            Point p = ((IGraphicalViewer) viewer).computeToDisplay(new Point(
                    bounds.x, bounds.y), true);
            bounds.x = p.x;
            bounds.y = p.y;
        }
        return bounds;
    }

}