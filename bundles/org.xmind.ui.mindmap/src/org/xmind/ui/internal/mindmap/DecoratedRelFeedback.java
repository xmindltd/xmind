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
package org.xmind.ui.internal.mindmap;

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.SWT;
import org.xmind.core.IControlPoint;
import org.xmind.core.IRelationship;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.decorations.IRelationshipDecoration;
import org.xmind.ui.mindmap.IRelationshipPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;
import org.xmind.ui.util.MindMapUtils;

public class DecoratedRelFeedback extends DecoratedLineFeedback {

    public DecoratedRelFeedback(IGraphicalPart part) {
        super(part);
        setLineColor(ColorUtils.getColor(MindMapUI.LINE_COLOR_SELECTION));
        setLineStyle(SWT.LINE_SOLID);
        setLineWidthExpansion(MindMapUI.SELECTION_LINE_WIDTH / 2);
    }

    protected IDecoration createNewDecoration(IFigure figure,
            String decorationId) {
        return StyleUtils.createRelationshipDecoration(getHost(), decorationId);
    }

    protected String getNewDecorationId() {
        return StyleUtils.getString(getHost(),
                StyleUtils.getStyleSelector(getHost()), Styles.ShapeClass,
                Styles.REL_SHAPE_STRAIGHT);
    }

    protected void updateDecoration(IFigure figure, IDecoration decoration,
            String decorationId, IStyleSelector ss) {
        super.updateDecoration(figure, decoration, decorationId, ss);
        if (decoration instanceof IRelationshipDecoration) {
            if (getHost() instanceof IRelationshipPart) {
                IRelationship r = ((IRelationshipPart) getHost())
                        .getRelationship();
                decorateControlPoints(r, figure,
                        (IRelationshipDecoration) decoration);
            }
        }
    }

    private void decorateControlPoints(IRelationship r, IFigure figure,
            IRelationshipDecoration decoration) {
        if (r != null) {
            IControlPoint cp0 = r.getControlPoint(0);
            decoration.setRelativeSourceControlPoint(figure,
                    MindMapUtils.toGraphicalPosition(cp0.getPosition()));
            Double angle = cp0 == null ? null : cp0.getPolarAngle();
            Double amount = cp0 == null ? null : cp0.getPolarAmount();
            decoration.setSourceControlPointHint(figure, angle, amount);

            IControlPoint cp1 = r.getControlPoint(1);
            decoration.setRelativeTargetControlPoint(figure,
                    MindMapUtils.toGraphicalPosition(cp1.getPosition()));
            angle = cp1 == null ? null : cp1.getPolarAngle();
            amount = cp1 == null ? null : cp1.getPolarAmount();
            decoration.setTargetControlPointHint(figure, angle, amount);
        }
    }

}