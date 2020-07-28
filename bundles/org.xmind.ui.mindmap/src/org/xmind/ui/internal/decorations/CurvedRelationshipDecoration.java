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
import org.xmind.gef.draw2d.IAnchor;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.AbstractRelationshipDecoration;
import org.xmind.ui.style.Styles;

public class CurvedRelationshipDecoration
        extends AbstractRelationshipDecoration {

    private static final double f1 = 0.125;

    private static final double f2 = 3 * 0.125;

    public CurvedRelationshipDecoration() {
    }

    public CurvedRelationshipDecoration(String id) {
        super(id);
    }

    protected void route(IFigure figure, Path shape) {
        PrecisionPoint sp = getSourcePosition(figure);
        PrecisionPoint tp = getTargetPosition(figure);
        PrecisionPoint cp1 = getSourceControlPoint(figure);
        PrecisionPoint cp2 = getTargetControlPoint(figure);
        shape.moveTo(sp);
        shape.cubicTo(cp1, cp2, tp);
    }

    protected void calcTitlePosition(IFigure figure, PrecisionPoint titlePos,
            PrecisionPoint sourcePos, PrecisionPoint targetPos,
            PrecisionPoint sourceCP, PrecisionPoint targetCP) {
        double x = f1 * sourcePos.x + f2 * sourceCP.x + f2 * targetCP.x
                + f1 * targetPos.x;
        double y = f1 * sourcePos.y + f2 * sourceCP.y + f2 * targetCP.y
                + f1 * targetPos.y;
        titlePos.setLocation(x, y);
    }

    @Override
    protected void reroute(IFigure figure, PrecisionPoint sourcePos,
            PrecisionPoint targetPos, PrecisionPoint sourceCP,
            PrecisionPoint targetCP) {
        IAnchor sa = getSourceAnchor();
        IAnchor ta = getTargetAnchor();

        PrecisionPoint a1 = null;
        PrecisionPoint a2 = null;
        if (sa != null) {
            if (relativeSourceCP != null) {
                sourceCP.setLocation(sa.getReferencePoint())
                        .translate(relativeSourceCP.x, relativeSourceCP.y);
                sourcePos.setLocation(sa.getLocation(sourceCP, 0));
            } else if (ta != null) {
                if (a1 == null)
                    a1 = sa.getLocation(ta.getReferencePoint(), 0);
                sourcePos.setLocation(a1);
                if (a2 == null)
                    a2 = ta.getLocation(sa.getReferencePoint(), 0);
                sourceCP.setLocation(a1).move(a2,
                        Styles.DEF_CONTROL_POINT_AMOUNT);

                if (sourceCPAngle != null && sourceCPAngle != 0) {
                    if (!sa.getOwner().containsPoint(sourceCP.toDraw2DPoint())
                            && !sa.getOwner()
                                    .intersects(ta.getOwner().getBounds())) {
                        updatePosAndCp(sa, sourcePos, sourceCP, sourceCPAngle);
                    }
                }
            }

            if (ta != null) {
                if (relativeTargetCP != null) {
                    targetCP.setLocation(ta.getReferencePoint())
                            .translate(relativeTargetCP.x, relativeTargetCP.y);
                    targetPos.setLocation(ta.getLocation(targetCP, 0));
                } else if (sa != null) {
                    if (a2 == null)
                        a2 = ta.getLocation(sa.getReferencePoint(), 0);
                    targetPos.setLocation(a2);
                    if (a1 == null)
                        a1 = sa.getLocation(ta.getReferencePoint(), 0);
                    targetCP.setLocation(a2).move(a1,
                            Styles.DEF_CONTROL_POINT_AMOUNT);

                    if (targetCPAngle != null && targetCPAngle != 0) {
                        if (!sa.getOwner()
                                .containsPoint(sourceCP.toDraw2DPoint())
                                && !sa.getOwner().intersects(
                                        ta.getOwner().getBounds())) {
                            updatePosAndCp(ta, targetPos, targetCP,
                                    targetCPAngle);
                        }
                    }
                }
            }
        }
    }

}