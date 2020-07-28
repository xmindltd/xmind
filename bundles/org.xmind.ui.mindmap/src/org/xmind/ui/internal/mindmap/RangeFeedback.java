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
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.draw2d.SimpleRectangleFigure;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.gef.service.AbstractBendPointsFeedback;
import org.xmind.gef.service.IRectangleProvider;
import org.xmind.ui.branch.IBranchStructureExtension;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IBranchRangePart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;

public abstract class RangeFeedback extends AbstractBendPointsFeedback {

    public static final int[] HORIZONTAL = new int[] { PositionConstants.EAST,
            PositionConstants.WEST };

    public static final int[] VERTICAL = new int[] { PositionConstants.NORTH,
            PositionConstants.SOUTH };

    public static final int[] SIDES = new int[] { PositionConstants.EAST,
            PositionConstants.WEST, PositionConstants.NORTH,
            PositionConstants.SOUTH };

    private IBranchRangePart host;

    private int alpha = 0xff;

    protected SimpleRectangleFigure border = new SimpleRectangleFigure();

    private IRectangleProvider originalBoundsProvider;

    public RangeFeedback(IBranchRangePart host) {
        this.host = host;
        setHidePointLength(MindMapUI.HIDE_BEND_POINT_LENGTH);
        originalBoundsProvider = new IRectangleProvider() {
            public Rectangle getRectangle() {
                return getRangeBounds().getExpanded(3, 3);
            }
        };
        setBoundsProvider(originalBoundsProvider);
        border.setForegroundColor(ColorUtils.getColor("#b0b0b0")); //$NON-NLS-1$
        border.setLineWidth(1);
    }

    protected abstract Rectangle getRangeBounds();

    public void addToLayer(IFigure layer) {
        layer.add(border);
        super.addToLayer(layer);
    }

    public void removeFromLayer(IFigure layer) {
        super.removeFromLayer(layer);
        layer.remove(border);
    }

    public void update() {
        if (host.getStatus().isSelected()
                && (host.getStatus().isPreSelected() || getBoundsProvider() != originalBoundsProvider)) {
            setAlpha(0xff);
            border.setMainAlpha(0xd8);
            border.setVisible(true);
        } else if (host.getStatus().isSelected()
                || host.getStatus().isPreSelected()) {
            setAlpha(0x50);
            border.setMainAlpha(0x30);
            border.setVisible(true);
        } else {
            border.setVisible(false);
        }
        setOrientations(getBendPointOrientations());
        super.update();
    }

    protected void updateWithBounds(Rectangle clientBounds) {
        super.updateWithBounds(clientBounds);
        border.setBounds(clientBounds);
    }

    protected int[] getBendPointOrientations() {
        IBranchPart branch = host.getOwnedBranch();
        if (branch != null) {
            IStructure sa = branch.getBranchPolicy().getStructure(branch);
            if (sa instanceof IBranchStructureExtension) {
                int direction = ((IBranchStructureExtension) sa)
                        .getRangeGrowthDirection(branch, host);
                switch (direction) {
                case PositionConstants.EAST:
                case PositionConstants.WEST:
                    return HORIZONTAL;
                case PositionConstants.SOUTH:
                case PositionConstants.NORTH:
                    return VERTICAL;
                }
            }
        }
        return SIDES;
    }

    public IBranchRangePart getHost() {
        return host;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    protected IReferencedFigure createPointFigure(int orientation) {
        return new RangeMoveHandleFigure(orientation);
    }

    protected void preUpdatePointFigure(IReferencedFigure figure,
            int orientation, Rectangle bounds, Point preferredPosition) {
        super.preUpdatePointFigure(figure, orientation, bounds,
                preferredPosition);
        ((RangeMoveHandleFigure) figure).setClientSize(bounds.getSize());
    }

    protected void updatePointFigure(IReferencedFigure figure, int orientation) {
        figure.repaint();
        ((RangeMoveHandleFigure) figure).setAlpha((int) (getAlpha() * 0.85));
    }

}