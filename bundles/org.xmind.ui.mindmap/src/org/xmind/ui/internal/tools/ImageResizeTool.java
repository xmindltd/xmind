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
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.draw2d.SizeableImageFigure;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.part.IGraphicalEditPart;
import org.xmind.gef.service.IBendPointsFeedback;
import org.xmind.gef.service.IFeedback;
import org.xmind.gef.status.IStatusListener;
import org.xmind.gef.status.StatusEvent;
import org.xmind.gef.tool.ISourceTool;
import org.xmind.ui.mindmap.IImagePart;

public class ImageResizeTool extends FeedbackResizeTool
        implements ISourceTool, IStatusListener {

    private static final int MIN_IMAGE_WIDTH = 2;

    private SizeableImageFigure feedbackImageFigure;

    private IImagePart source;

    public ImageResizeTool() {
        setKeepRatio(true);
        getStatus().addStatusListener(this);
    }

    public IGraphicalEditPart getSource() {
        return (IGraphicalEditPart) source;
    }

    public void setSource(IGraphicalEditPart source) {
        Assert.isTrue(source instanceof IImagePart);
        this.source = (IImagePart) source;
    }

    protected Rectangle getSourceArea() {
        return getSource().getFigure().getBounds();
    }

    protected IFeedback getSourceFeedback() {
        return (IFeedback) getSource().getAdapter(IFeedback.class);
    }

    protected void initFeedback(IBendPointsFeedback feedback) {
        super.initFeedback(feedback);
        Layer layer = getTargetViewer().getLayer(GEF.LAYER_PRESENTATION);
        if (layer != null) {
            Image sourceImage = ((IImagePart) getSource()).getImage();
            Rectangle sourceBounds = getSource().getFigure().getBounds();
            Image image = new Image(sourceImage.getDevice(), sourceBounds.width,
                    sourceBounds.height);
            GC gc = new GC(image);
            try {
                gc.setAntialias(SWT.ON);
                org.eclipse.swt.graphics.Rectangle sourceImageSize = sourceImage
                        .getBounds();
                org.eclipse.swt.graphics.Rectangle imageSize = image
                        .getBounds();
                gc.drawImage(sourceImage, 0, 0, sourceImageSize.width,
                        sourceImageSize.height, 0, 0, imageSize.width,
                        imageSize.height);
            } finally {
                gc.dispose();
            }
            feedbackImageFigure = new SizeableImageFigure(image);
            feedbackImageFigure.setStretched(true);
            feedbackImageFigure.setConstrained(false);
            feedbackImageFigure.setBounds(sourceBounds);
            layer.add(feedbackImageFigure);
        }
    }

    protected void updateAreaBounds(Rectangle area, Point cursorPosition) {
        super.updateAreaBounds(area, cursorPosition);
        if (feedbackImageFigure != null) {
            feedbackImageFigure.setBounds(area);
        }
    }

    protected void removeFeedback(IBendPointsFeedback feedback) {
        if (feedbackImageFigure != null) {
            Image image = feedbackImageFigure.getImage();
            feedbackImageFigure.setImage(null);
            image.dispose();
            if (feedbackImageFigure.getParent() != null)
                feedbackImageFigure.getParent().remove(feedbackImageFigure);
            feedbackImageFigure = null;
        }
        super.removeFeedback(feedback);
    }

    public void finish() {
        super.finish();
        Request request = new Request(GEF.REQ_RESIZE);
        request.setDomain(getDomain());
        request.setViewer(getTargetViewer());
        request.setPrimaryTarget(getSource());
        request.setParameter(GEF.PARAM_SIZE, getResultArea().getSize());
        getDomain().handleRequest(request);
    }

    protected int constrainWidth(int w) {
        return Math.max(w, MIN_IMAGE_WIDTH);
    }

    protected int constrainHeight(int h) {
        return Math.max(h, MIN_IMAGE_WIDTH);
    }

    public void statusChanged(StatusEvent event) {
        if (event.key == GEF.ST_SHIFT_PRESSED) {
            setKeepRatio(!event.newValue);
            updateArea(getResultArea(), getCursorPosition(), null);
        }
    }

    protected void keepRatio(Dimension result, int w, int h, int initW,
            int initH) {
        result.setSize(Geometry.getScaledConstrainedSize2(w, h, initW, initH,
                MIN_IMAGE_WIDTH, MIN_IMAGE_WIDTH, Integer.MAX_VALUE,
                Integer.MAX_VALUE));
    }

}
