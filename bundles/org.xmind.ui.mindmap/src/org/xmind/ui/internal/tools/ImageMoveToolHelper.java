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
package org.xmind.ui.internal.tools;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.core.Core;
import org.xmind.core.IImage;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.draw2d.SelectionFigure;
import org.xmind.gef.service.IFeedbackService;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.tools.ToolHelperBase;

public class ImageMoveToolHelper extends ToolHelperBase {

    private static class AlphaRectangle extends RectangleFigure {

//        private int alpha;
//
//        public void setAlpha(int alpha) {
//            if (alpha == this.alpha)
//                return;
//            this.alpha = alpha;
//            repaint();
//        }
//        
//        public Integer getAlpha() {
//            return alpha;
//        }
//
//        public void paintFigure(Graphics graphics) {
//            graphics.setAlpha(getAlpha());
//            super.paintFigure(graphics);
//        }
    }

    private static final int LINE_WIDTH = 2;

    private static final int NORMAL_ALPHA = 0x80;

    private static final int SELECTION_ALPHA = 0xff;

    private static final String BORDER_COLOR = "#00a000"; //$NON-NLS-1$

    private static final String FILL_COLOR = "#50d050"; //$NON-NLS-1$

    private static final String SELECTION_COLOR = "#b0d040"; //$NON-NLS-1$

    private IFigure layer;

    private IFeedbackService feedbackService;

    private ITopicPart currentParent;

    private IFigure area;

    private AlphaRectangle top;

    private AlphaRectangle bottom;

    private AlphaRectangle left;

    private AlphaRectangle right;

    public void activate(EditDomain domain, IViewer viewer) {
        super.activate(domain, viewer);
        layer = ((IGraphicalViewer) viewer).getLayer(GEF.LAYER_PRESENTATION);
        feedbackService = (IFeedbackService) viewer
                .getService(IFeedbackService.class);
        removeArea();
    }

    public void deactivate(EditDomain domain, IViewer viewer) {
        removeArea();
        layer = null;
        if (feedbackService != null) {
            if (currentParent != null) {
                feedbackService.removeSelection(currentParent.getFigure());
            }
            feedbackService = null;
        }
        currentParent = null;
        super.deactivate(domain, viewer);
    }

    public void update(ITopicPart parent, IFigure feedback, Point cursorPos) {
        ITopicPart oldParent = currentParent;
        currentParent = parent;
        update(oldParent, parent, feedback, cursorPos);
    }

    private void update(ITopicPart oldParent, ITopicPart newParent,
            IFigure feedback, Point cursorPos) {
        if (oldParent != newParent) {
            if (feedbackService != null) {
                if (oldParent != null) {
                    feedbackService.removeSelection(oldParent.getFigure());
                }
                if (newParent != null) {
                    SelectionFigure selection = feedbackService
                            .addSelection(newParent.getFigure());
                    selection.setPreselectionColor(ColorUtils
                            .getColor(MindMapUI.COLOR_WARNING));
                    selection.setPreselectionFillAlpha(0);
                    selection.setPreselectionFillColor(null);
                    selection.setPreselected(true);
                }
            }
            removeArea();
            createArea(newParent, feedback);
        }
        updateArea(cursorPos);
    }

    private void updateArea(Point cursorPos) {
        IFigure targetDistrict = getTargetDistrict(cursorPos);
        updateColor(top, targetDistrict);
        updateColor(bottom, targetDistrict);
        updateColor(left, targetDistrict);
        updateColor(right, targetDistrict);
    }

    private IFigure getTargetDistrict(Point cursorPos) {
        if (top != null && top.containsPoint(cursorPos))
            return top;
        if (bottom != null && bottom.containsPoint(cursorPos))
            return bottom;
        if (left != null && left.containsPoint(cursorPos))
            return left;
        if (right != null && right.containsPoint(cursorPos))
            return right;
        return null;
    }

    private void updateColor(AlphaRectangle fig, IFigure district) {
        if (fig == null)
            return;
        if (fig == district) {
            fig.setBackgroundColor(ColorUtils.getColor(SELECTION_COLOR));
            fig.setAlpha(SELECTION_ALPHA);
        } else {
            fig.setBackgroundColor(ColorUtils.getColor(FILL_COLOR));
            fig.setAlpha(NORMAL_ALPHA);
        }
    }

    private void createArea(ITopicPart parent, IFigure feedback) {
        if (layer == null || parent == null)
            return;

        area = new Layer();
        int feedbackIndex = layer.getChildren().indexOf(feedback);
        layer.add(area, feedbackIndex);

        top = createDistrict();
        bottom = createDistrict();
        left = createDistrict();
        right = createDistrict();

        area.add(top);
        area.add(bottom);
        area.add(left);
        area.add(right);

        IFigure parentFigure = parent.getFigure();
        Rectangle bounds = parentFigure.getBounds();
        area.setBounds(bounds);
        layoutDistricts(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    private void layoutDistricts(int x, int y, int width, int height) {
        int halfLine1 = LINE_WIDTH / 2;
        int halfLine2 = LINE_WIDTH - halfLine1;

        int w = width / 4;

        left.setBounds(new Rectangle(x, y, w + halfLine2 + 1, height));
        right.setBounds(new Rectangle(x + width - w - halfLine1 - 1, y, w
                + halfLine1 + 1, height));

        width -= w * 2 + LINE_WIDTH;
        x += w + halfLine2;
        int h = height / 2;

        top.setBounds(new Rectangle(x, y, width, h + halfLine2));
        bottom.setBounds(new Rectangle(x, y + h - halfLine1, width, h
                + halfLine1));
    }

    private AlphaRectangle createDistrict() {
        AlphaRectangle fig = new AlphaRectangle();
        fig.setFill(true);
        fig.setOutline(true);
        fig.setBackgroundColor(ColorUtils.getColor(FILL_COLOR));
        fig.setForegroundColor(ColorUtils.getColor(BORDER_COLOR));
        fig.setLineWidth(LINE_WIDTH);
        return fig;
    }

    private void removeArea() {
        if (area != null) {
            if (area.getParent() != null)
                area.getParent().remove(area);
            area = null;
        }
        top = null;
        bottom = null;
        left = null;
        right = null;
    }

    public void decorateMoveRequest(Request request, Point cursorPos) {
        String alignment = getAlignment(cursorPos);
        if (alignment != null) {
            request.setParameter(MindMapUI.PARAM_PROPERTY_PREFIX
                    + Core.ImageAlignment, alignment);
        }
    }

    private String getAlignment(Point cursorPos) {
        if (top != null && top.containsPoint(cursorPos))
            return IImage.TOP;
        if (bottom != null && bottom.containsPoint(cursorPos))
            return IImage.BOTTOM;
        if (left != null && left.containsPoint(cursorPos))
            return IImage.LEFT;
        if (right != null && right.containsPoint(cursorPos))
            return IImage.RIGHT;
        return null;
    }

}