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
package org.xmind.ui.internal.tools;

import org.eclipse.core.runtime.Assert;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.graphics.Cursor;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.draw2d.SelectionFigure;
import org.xmind.gef.draw2d.SizeableImageFigure;
import org.xmind.gef.event.MouseDragEvent;
import org.xmind.gef.part.IGraphicalEditPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.service.IFeedbackService;
import org.xmind.ui.internal.svgsupport.SVGImageFigure;
import org.xmind.ui.mindmap.IMarkerPart;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.tools.DummyMoveTool;
import org.xmind.ui.util.MindMapUtils;

public class MarkerMoveTool extends DummyMoveTool {

    private ITopicPart targetParent;

    private IFeedbackService feedbackService;

    public void setSource(IGraphicalEditPart source) {
        Assert.isTrue(source instanceof IMarkerPart);
        super.setSource(source);
    }

    protected IFigure createDummy() {
        Layer layer = getTargetViewer().getLayer(GEF.LAYER_PRESENTATION);
        if (layer != null) {
            IMarkerPart part = (IMarkerPart) getSource();
            IFigure dummy;
            if (part.getSVGData() == null) {
                dummy = new SizeableImageFigure(part.getImage());
                ((SizeableImageFigure) dummy).setStretched(true);
                ((SizeableImageFigure) dummy).setConstrained(true);
            } else {
                dummy = new SVGImageFigure(part.getSVGData());
                ((SVGImageFigure) dummy).setManager(part.getResourceManager());
                ((SVGImageFigure) dummy).setStretched(true);
                ((SVGImageFigure) dummy).setConstrained(true);
            }

            dummy.setBounds(getSource().getFigure().getBounds());
            layer.add(dummy);
            return dummy;
        }
        return null;
    }

    protected void start() {
        super.start();
        feedbackService = (IFeedbackService) getTargetViewer()
                .getService(IFeedbackService.class);
    }

    protected void onMoving(Point currentPos, MouseDragEvent me) {
        super.onMoving(currentPos, me);
        ITopicPart newParent = findTopicPart(me.target);
        if (newParent != targetParent) {
            if (feedbackService != null) {
                if (targetParent != null) {
                    feedbackService.removeSelection(targetParent.getFigure());
                }
                if (newParent != null) {
                    SelectionFigure selection = feedbackService
                            .addSelection(newParent.getFigure());
                    selection.setPreselectionColor(
                            ColorUtils.getColor(MindMapUI.COLOR_WARNING));
                    selection.setPreselectionFillAlpha(0);
                    selection.setPreselectionFillColor(null);
                    selection.setPreselected(true);
                }
            }
        }
        targetParent = newParent;
    }

    protected void end() {
        if (feedbackService != null) {
            if (targetParent != null) {
                feedbackService.removeSelection(targetParent.getFigure());
            }
            feedbackService = null;
        }
        targetParent = null;
        super.end();
    }

    private ITopicPart findTopicPart(IPart p) {
        return MindMapUtils.findTopicPart(p);
    }

    private boolean isCopyMove() {
        if (Util.isMac())
            return getStatus().isStatus(GEF.ST_ALT_PRESSED);
        return getStatus().isStatus(GEF.ST_CONTROL_PRESSED);
    }

    protected Request createRequest() {
        boolean copy = isCopyMove();
        Request request = new Request(copy ? GEF.REQ_COPYTO : GEF.REQ_MOVETO);
        request.setDomain(getDomain());
        request.setViewer(getTargetViewer());
        request.setPrimaryTarget(getSource());
        request.setParameter(GEF.PARAM_PARENT, targetParent);
        return request;
    }

    public Cursor getCurrentCursor(Point pos, IPart host) {
        if (isCopyMove())
            return MindMapUI.getImages().getCursor(IMindMapImages.CURSOR_ADD);
        return super.getCurrentCursor(pos, host);
    }

}