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
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.AbstractArrowDecoration;

public class NormalArrowDecoration extends AbstractArrowDecoration {

    private PrecisionPoint p1 = new PrecisionPoint();
    private PrecisionPoint p2 = new PrecisionPoint();

    public NormalArrowDecoration() {
        super();
    }

    public NormalArrowDecoration(String id) {
        super(id);
    }

    protected void sketch(IFigure figure, Path shape) {
        shape.moveTo(p1);
        shape.lineTo(getPosition());
        shape.lineTo(p2);
    }

    public void reshape(IFigure figure) {
        int side = getWidth() * 2 + 4;
        p1.setLocation(getPosition()).move(getAngle() - Math.PI / 6, side);
        p2.setLocation(getPosition()).move(getAngle() + Math.PI / 6, side);
    }

}