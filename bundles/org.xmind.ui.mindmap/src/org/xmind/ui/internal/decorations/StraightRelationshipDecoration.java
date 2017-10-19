/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.decorations;

import org.eclipse.draw2d.IFigure;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.AbstractRelationshipDecoration;

public class StraightRelationshipDecoration extends
        AbstractRelationshipDecoration {

    public StraightRelationshipDecoration() {
        super();
    }

    public StraightRelationshipDecoration(String id) {
        super(id);
    }

    protected void route(IFigure figure, Path shape) {
        PrecisionPoint p1 = getSourcePosition(figure);
        PrecisionPoint p2 = getTargetPosition(figure);
        shape.moveTo(p1);
        shape.lineTo(p2);
    }

    protected double getSourceAnchorAngle(IFigure figure) {
        PrecisionPoint p1 = getSourcePosition(figure);
        PrecisionPoint p2 = getTargetPosition(figure);
        return Geometry.getAngle(p2, p1);
    }

    protected double getTargetAnchorAngle(IFigure figure) {
        PrecisionPoint p1 = getTargetPosition(figure);
        PrecisionPoint p2 = getSourcePosition(figure);
        return Geometry.getAngle(p2, p1);
    }

}