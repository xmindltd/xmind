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

public class HerringBoneArrowDecoration extends AbstractArrowDecoration {

    private static final double deltaAngle = Math.PI * 2 / 3;

    private PrecisionPoint p1 = new PrecisionPoint();
    private PrecisionPoint p2 = new PrecisionPoint();
    private PrecisionPoint p0left = new PrecisionPoint();
    private PrecisionPoint p0right = new PrecisionPoint();
    private PrecisionPoint p1left = new PrecisionPoint();
    private PrecisionPoint p1right = new PrecisionPoint();
    private PrecisionPoint p2left = new PrecisionPoint();
    private PrecisionPoint p2right = new PrecisionPoint();

    public HerringBoneArrowDecoration() {
    }

    public HerringBoneArrowDecoration(String id) {
        super(id);
    }

    protected void sketch(IFigure figure, Path shape) {
        shape.moveTo(p0left);
        shape.lineTo(getPosition());
        shape.lineTo(p0right);
        shape.moveTo(p1left);
        shape.lineTo(p1);
        shape.lineTo(p1right);
        shape.moveTo(p2left);
        shape.lineTo(p2);
        shape.lineTo(p2right);
        shape.moveTo(getPosition());
        shape.lineTo(p2);
    }

    public void reshape(IFigure figure) {
        double length = getWidth() * 2 + 4;
        double width = getWidth() * 2 + 2;
        p0left.setLocation(getPosition()).move(getAngle() - deltaAngle, width);
        p0right.setLocation(getPosition()).move(getAngle() + deltaAngle, width);

        p1.setLocation(getPosition()).move(getAngle(), length / 2);
        p1left.setLocation(p1).move(getAngle() - deltaAngle, width);
        p1right.setLocation(p1).move(getAngle() + deltaAngle, width);

        p2.setLocation(getPosition()).move(getAngle(), length);
        p2left.setLocation(p2).move(getAngle() - deltaAngle, width);
        p2right.setLocation(p2).move(getAngle() + deltaAngle, width);
    }

}