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
package org.xmind.ui.internal.fishbone.decorations;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.draw2d.IAnchor;
import org.xmind.gef.draw2d.IDecoratedFigure;
import org.xmind.gef.draw2d.decoration.AbstractDecoration;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.geometry.PrecisionLine;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.geometry.PrecisionPolygon;
import org.xmind.gef.draw2d.geometry.PrecisionRectangle;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.IBranchConnectionDecoration;
import org.xmind.ui.decorations.ITopicDecoration;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.INodePart;
import org.xmind.ui.mindmap.ITopicPart;

public class SubFishboneBranchDecoration extends AbstractDecoration {

    private IBranchPart branch;

    private PrecisionPoint start = null;

    private PrecisionPoint end = null;

    public SubFishboneBranchDecoration(IBranchPart branch, String id) {
        super(id);
        this.branch = branch;
    }

    public void validate(IFigure figure) {
        super.validate(figure);
        ITopicPart topicPart = branch.getTopicPart();
        if (topicPart == null) {
            setVisible(figure, false);
            return;
        }

        IAnchor anchor = ((INodePart) topicPart).getSourceAnchor(branch);
        if (anchor == null) {
            setVisible(figure, false);
            return;
        }

        IBranchPart parent = branch.getParentBranch();
        if (parent == null) {
            setVisible(figure, false);
            return;
        }

        IBranchConnectionDecoration conn = (IBranchConnectionDecoration) parent
                .getConnections().getDecoration(branch.getBranchIndex());
        if (conn == null) {
            setVisible(figure, false);
            return;
        }

        int orientation = conn.getTargetOrientation();
        PrecisionPoint p1 = anchor.getLocation(orientation, conn
                .getTargetExpansion());
        PrecisionPoint p2 = anchor.getLocation(Geometry
                .getOppositePosition(orientation), 0);
        if (p1.equals(p2)) {
            setVisible(figure, false);
            return;
        }

        PrecisionRectangle r = new PrecisionRectangle(topicPart.getFigure()
                .getBounds());
        for (IBranchPart subBranch : branch.getSubBranches()) {
            ITopicPart subTopicPart = subBranch.getTopicPart();
            if (subTopicPart != null) {
                r = Geometry.union(r, new PrecisionRectangle(subTopicPart
                        .getFigure().getBounds()));
            } else {
                r = Geometry.union(r, new PrecisionRectangle(subBranch
                        .getFigure().getBounds()));
            }
        }
        PrecisionPolygon polygon = PrecisionPolygon.createFromRect(r);
        PrecisionPoint p = polygon.intersectFarthest(new PrecisionLine(p1, p2,
                PrecisionLine.LineType.Ray));
        if (p != null) {
            p2 = p;
        }

        setVisible(figure, true);

        this.start = p1;
        this.end = p2;
    }

    public void invalidate() {
        start = null;
        end = null;
        super.invalidate();
    }

    protected void performPaint(IFigure figure, Graphics graphics) {
        ITopicDecoration topicDecoration = getTopicDecoration();
        if (topicDecoration == null)
            return;

        Color lineColor = topicDecoration.getLineColor();
        if (lineColor == null)
            return;

        int lineWidth = topicDecoration.getLineWidth();
        int lineStyle = topicDecoration.getLineStyle();
        Path shape = new Path(Display.getCurrent());
        shape.moveTo(start);
        shape.lineTo(end);
        graphics.setForegroundColor(lineColor);
        graphics.setLineWidth(lineWidth);
        graphics.setLineStyle(lineStyle);
        graphics.drawPath(shape);
        shape.dispose();
    }

    private ITopicDecoration getTopicDecoration() {
        ITopicPart topicPart = branch.getTopicPart();
        if (topicPart != null) {
            IFigure topicFigure = topicPart.getFigure();
            if (topicFigure instanceof IDecoratedFigure) {
                IDecoration decoration = ((IDecoratedFigure) topicFigure)
                        .getDecoration();
                if (decoration instanceof ITopicDecoration)
                    return (ITopicDecoration) decoration;
            }
        }
        return null;
    }

}