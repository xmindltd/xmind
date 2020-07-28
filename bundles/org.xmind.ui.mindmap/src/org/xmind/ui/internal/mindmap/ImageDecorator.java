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
import org.eclipse.draw2d.geometry.Dimension;
import org.xmind.core.IImage;
import org.xmind.gef.draw2d.SizeableImageFigure;
import org.xmind.gef.part.Decorator;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.mindmap.IImagePart;
import org.xmind.ui.mindmap.MindMapUI;

public class ImageDecorator extends Decorator {

    private static final ImageDecorator instance = new ImageDecorator();

    public void decorate(IGraphicalPart part, IFigure figure) {
        super.decorate(part, figure);
        if (part instanceof IImagePart) {
            IImagePart imagePart = (IImagePart) part;
            if (figure instanceof SizeableImageFigure) {
                SizeableImageFigure imageFigure = (SizeableImageFigure) figure;
                decorateImageFigure(imagePart, imageFigure);
            }
        }
    }

    private void decorateImageFigure(IImagePart imagePart,
            SizeableImageFigure imageFigure) {
        imageFigure.setStretched(true);
        imageFigure.setConstrained(false);
        imageFigure.setImage(imagePart.getImage());
        IImage imageModel = imagePart.getImageModel();
        int width = imageModel.getWidth();
        int height = imageModel.getHeight();
        if (width >= 0 && height >= 0) {
            imageFigure.setPreferredSize(width, height);
        } else {
            Dimension originalSize = imageFigure.getImageSize();
            if (width >= 0) {
                imageFigure.setPreferredSize(width, originalSize.width == 0 ? 0
                        : width * originalSize.height / originalSize.width);
            } else if (height >= 0) {
                imageFigure.setPreferredSize(originalSize.height == 0 ? 0
                        : height * originalSize.width / originalSize.height,
                        height);
            } else {
                width = Math
                        .min(MindMapUI.IMAGE_INIT_WIDTH, originalSize.width);
                height = Math.min(MindMapUI.IMAGE_INIT_HEIGHT,
                        originalSize.height);
                double scale1 = width * 1.0 / originalSize.width;
                double scale2 = height * 1.0 / originalSize.height;
                if (scale1 < scale2) {
                    height = (int) (originalSize.height * scale1);
                } else {
                    width = (int) (originalSize.width * scale2);
                }
                imageFigure.setPreferredSize(width, height);
            }
        }
    }

    public static ImageDecorator getInstance() {
        return instance;
    }

}