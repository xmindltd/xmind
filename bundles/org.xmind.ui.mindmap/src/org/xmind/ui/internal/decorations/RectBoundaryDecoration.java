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
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.AbstractBoundaryDecoration;

public class RectBoundaryDecoration extends AbstractBoundaryDecoration {

    public RectBoundaryDecoration() {
        super();
    }

    public RectBoundaryDecoration(String id) {
        super(id);
    }

    protected void sketch(IFigure figure, Path shape, Rectangle box, int purpose) {
        shape.addRectangle(box.x, box.y, box.width, box.height);
    }

}