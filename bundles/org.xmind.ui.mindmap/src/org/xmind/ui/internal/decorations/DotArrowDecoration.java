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
import org.xmind.gef.draw2d.geometry.PrecisionRectangle;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.AbstractArrowDecoration;

public class DotArrowDecoration extends AbstractArrowDecoration {

    private PrecisionRectangle circle = new PrecisionRectangle();

    public DotArrowDecoration() {
        super();
    }

    public DotArrowDecoration(String id) {
        super(id);
    }

    protected boolean usesFill() {
        return true;
    }

    protected void sketch(IFigure figure, Path shape) {
        shape.addArc(circle, 0, 360);
    }

    public void reshape(IFigure figure) {
        int r = getWidth();
        circle.setLocation(getPosition()).setSize(0, 0).expand(r, r);
    }

}