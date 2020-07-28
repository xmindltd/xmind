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
package org.xmind.gef.policy;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.geometry.ReferencedFigureBoundsProvider;
import org.xmind.gef.part.IGraphicalPart;

public class GraphicalPartBoundsProvider extends ReferencedFigureBoundsProvider {

    private static GraphicalPartBoundsProvider defaultInstance = null;

    public Rectangle getPrefBounds(Object host, Point reference) {
        if (host instanceof IGraphicalPart) {
            host = ((IGraphicalPart) host).getFigure();
        }
        return super.getPrefBounds(host, reference);
    }

    public static GraphicalPartBoundsProvider getDefault() {
        if (defaultInstance == null)
            defaultInstance = new GraphicalPartBoundsProvider();
        return defaultInstance;
    }

}