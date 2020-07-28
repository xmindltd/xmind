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
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.draw2d.SizeableImageFigure;
import org.xmind.gef.event.MouseDragEvent;
import org.xmind.gef.part.IGraphicalEditPart;
import org.xmind.gef.part.IPart;
import org.xmind.ui.mindmap.IImagePart;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.tools.DummyMoveTool;
import org.xmind.ui.util.MindMapUtils;

public class ImageMoveTool extends DummyMoveTool {

    private ITopicPart targetParent = null;

    private ImageMoveToolHelper helper = null;

    public void setSource(IGraphicalEditPart source) {
        Assert.isTrue(source instanceof IImagePart);
        super.setSource(source);
    }

    protected IFigure createDummy() {
        Layer layer = getTargetViewer().getLayer(GEF.LAYER_PRESENTATION);
        if (layer != null) {
            Image sourceImage = ((IImagePart) getSource()).getImage();
            Rectangle sourceBounds = getSource().getFigure().getBounds();
            Image image = new Image(sourceImage.getDevice(),
                    sourceBounds.width, sourceBounds.height);
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
            SizeableImageFigure dummy = new SizeableImageFigure(image);
            dummy.setStretched(true);
            dummy.setConstrained(false);
            dummy.setBounds(getSource().getFigure().getBounds());
            dummy.setAlpha(0xc0);
            layer.add(dummy);
            return dummy;
        }
        return null;
    }

    @Override
    protected void destroyDummy(IFigure dummy) {
        if (dummy instanceof SizeableImageFigure) {
            Image image = ((SizeableImageFigure) dummy).getImage();
            ((SizeableImageFigure) dummy).setImage(null);
            image.dispose();
        }
        super.destroyDummy(dummy);
    }

    protected void onMoving(Point currentPos, MouseDragEvent me) {
        super.onMoving(currentPos, me);
        targetParent = findTopicPart(me.target);
        if (helper == null) {
            helper = new ImageMoveToolHelper();
            helper.activate(getDomain(), getTargetViewer());
        }
        helper.update(targetParent, getDummy(), currentPos);
    }

    private ITopicPart findTopicPart(IPart p) {
        return MindMapUtils.findTopicPart(p);
    }

    protected void end() {
        if (helper != null) {
            helper.deactivate(getDomain(), getTargetViewer());
            helper = null;
        }
        super.end();
    }

    private boolean isCopyMove() {
        if (Util.isMac())
            return getStatus().isStatus(GEF.ST_ALT_PRESSED);
        return getStatus().isStatus(GEF.ST_CONTROL_PRESSED);
    }

    protected Request createRequest() {
        boolean copy = isCopyMove();
        Request request = new Request(copy ? GEF.REQ_COPYTO : GEF.REQ_MOVETO);
        request.setPrimaryTarget(getSource());
        request.setParameter(GEF.PARAM_PARENT, targetParent);
        if (helper != null) {
            helper.decorateMoveRequest(request, getCursorPosition());
        }
        return request;
    }

    public Cursor getCurrentCursor(Point pos, IPart host) {
        if (isCopyMove())
            return MindMapUI.getImages().getCursor(IMindMapImages.CURSOR_ADD);
        return super.getCurrentCursor(pos, host);
    }

}