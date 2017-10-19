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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.draw2d.IAnchor;
import org.xmind.gef.draw2d.decoration.AbstractDecoration;
import org.xmind.gef.draw2d.decoration.IShadowedDecoration;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.IBranchConnections;
import org.xmind.ui.decorations.IBranchDecoration;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.style.Styles;

public class DefaultBranchDecoration extends AbstractDecoration implements
        IBranchDecoration, IShadowedDecoration {

    private IBranchPart branch;

    public DefaultBranchDecoration(IBranchPart branch) {
        this(branch, Styles.DEF_BRANCH_DECORATION);
    }

    public DefaultBranchDecoration(IBranchPart branch, String id) {
        super(id);
        this.branch = branch;
    }

    protected void performPaint(IFigure figure, Graphics graphics) {
        paintExpandedLine(graphics, false);
    }

    private void paintExpandedLine(Graphics graphics, boolean paintingShadow) {
        IBranchConnections connections = branch.getConnections();
        if (connections == null || connections.isEmpty()
                || !connections.isVisible())
            return;

        int orientation = connections.getSourceOrientation();
        if (orientation == PositionConstants.NONE)
            return;

        IAnchor anc = connections.getSourceAnchor();
        if (anc == null)
            return;

        PrecisionPoint p1 = anc.getLocation(orientation, 0);
        PrecisionPoint p2 = anc.getLocation(orientation, connections
                .getSourceExpansion());
        graphics.setLineStyle(connections.getLineStyle());
        graphics.setLineWidth(connections.getLineWidth());
        if (!paintingShadow)
            graphics.setForegroundColor(connections.getLineColor());
        if (graphics.getForegroundColor() == null)
            graphics.setForegroundColor(ColorConstants.black);
        graphics.setAlpha(connections.getAlpha());
        Path p = new Path(Display.getCurrent());
        p.moveTo(p1);
        p.lineTo(p2);
        graphics.drawPath(p);
        p.dispose();
    }

    public void paintAboveChildren(IFigure figure, Graphics graphics) {
    }

    public void paintShadow(IFigure figure, Graphics graphics) {
        graphics.setForegroundColor(ColorConstants.black);
        paintExpandedLine(graphics, true);
    }

}