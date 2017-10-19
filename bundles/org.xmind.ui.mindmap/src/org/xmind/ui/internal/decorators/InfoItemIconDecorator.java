package org.xmind.ui.internal.decorators;

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.Image;
import org.xmind.gef.draw2d.SizeableImageFigure;
import org.xmind.gef.part.Decorator;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.internal.svgsupport.SVGImageData;
import org.xmind.ui.internal.svgsupport.SVGImageFigure;
import org.xmind.ui.mindmap.IInfoItemPart;

public class InfoItemIconDecorator extends Decorator {

    private static final InfoItemIconDecorator instance = new InfoItemIconDecorator();

    @Override
    public void decorate(IGraphicalPart part, IFigure figure) {
        super.decorate(part, figure);
        if (figure instanceof SizeableImageFigure) {
            SizeableImageFigure imgFigure = (SizeableImageFigure) figure;
            Image image = null;
            if (part instanceof IInfoItemPart) {
                image = ((IInfoItemPart) part).getImage();
            } else {
                image = (Image) part.getAdapter(Image.class);
            }
            imgFigure.setImage(image);
            imgFigure.setPreferredSize(imgFigure.getImageSize());
        } else if (figure instanceof SVGImageFigure) {
            SVGImageFigure svgImageFigure = (SVGImageFigure) figure;
            SVGImageData svgData = null;

            svgData = ((IInfoItemPart) part).getSVGData();
            svgImageFigure.setSVGData(svgData);
        }
    }

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

    public static InfoItemIconDecorator getInstance() {
        return instance;
    }

}
