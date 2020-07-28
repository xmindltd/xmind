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

public class DiamondArrowDecoration extends AbstractArrowDecoration {

    private static final double deltaAngle1 = Math.PI / 2;

    private PrecisionPoint p1 = new PrecisionPoint();
    private PrecisionPoint p2 = new PrecisionPoint();
    private PrecisionPoint p3 = new PrecisionPoint();
    private PrecisionPoint p4 = new PrecisionPoint();

    public DiamondArrowDecoration() {
    }

    public DiamondArrowDecoration(String id) {
        super(id);
    }

    protected boolean usesFill() {
        return true;
    }

    protected void sketch(IFigure figure, Path shape) {
        shape.moveTo(p1);
        shape.lineTo(p2);
        shape.lineTo(p3);
        shape.lineTo(p4);
        shape.close();
    }

    public void reshape(IFigure figure) {
        int w1 = getWidth() + 3;
        int w2 = getWidth() + 2;
        p1.setLocation(getPosition()).move(getAngle(), w1);
        p2.setLocation(getPosition()).move(getAngle() - deltaAngle1, w2);
        p3.setLocation(getPosition()).move(getAngle() + Math.PI, w1);
        p4.setLocation(getPosition()).move(getAngle() + deltaAngle1, w2);
    }

}