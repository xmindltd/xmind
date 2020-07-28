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
import org.eclipse.swt.graphics.Image;
import org.xmind.gef.draw2d.SizeableImageFigure;
import org.xmind.gef.part.Decorator;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.internal.svgsupport.SVGImageData;
import org.xmind.ui.internal.svgsupport.SVGImageFigure;
import org.xmind.ui.mindmap.IMarkerPart;

public class MarkerDecorator extends Decorator {
    private static final MarkerDecorator instance = new MarkerDecorator();

    @Override
    public void decorate(IGraphicalPart part, IFigure figure) {
        super.decorate(part, figure);
        if (figure instanceof SizeableImageFigure) {
            SizeableImageFigure imgFigure = (SizeableImageFigure) figure;
            Image image = null;
            if (part instanceof IMarkerPart) {
                image = ((IMarkerPart) part).getImage();
            } else {
                image = (Image) part.getAdapter(Image.class);
            }
            imgFigure.setImage(image);
            Dimension imageSize = imgFigure.getImageSize();
            Dimension preferredSize = ((IMarkerPart) part).getPreferredSize();
            if (preferredSize == null) {
                preferredSize = imageSize;
            } else {
                if (preferredSize.width > imageSize.width)
                    preferredSize.setWidth(imageSize.width);
                if (preferredSize.height > imageSize.height)
                    preferredSize.setHeight(imageSize.height);
            }
            imgFigure.setPreferredSize(preferredSize);
        } else if (figure instanceof SVGImageFigure) {
            SVGImageFigure svgImageFigure = (SVGImageFigure) figure;
            SVGImageData svgData = null;

            svgData = ((IMarkerPart) part).getSVGData();
            svgImageFigure.setSVGData(svgData);

            svgImageFigure
                    .setPreferredSize(((IMarkerPart) part).getPreferredSize());
        }
    }

    @Override
    public void deactivate(IGraphicalPart part, IFigure figure) {
        super.deactivate(part, figure);
        if (figure instanceof SizeableImageFigure) {
            SizeableImageFigure imgFigure = (SizeableImageFigure) figure;
            imgFigure.setImage(null);
        } else if (figure instanceof SVGImageFigure) {
            SVGImageFigure svgImageFigure = (SVGImageFigure) figure;
            svgImageFigure.setSVGData(null);
        }
    }

    public static MarkerDecorator getInstance() {
        return instance;
    }

}