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
package org.xmind.ui.gallery;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.xmind.gef.IViewer;
import org.xmind.gef.draw2d.SizeableImageFigure;
import org.xmind.gef.part.Decorator;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.util.Properties;

public class SimpleImageDecorator extends Decorator {

    public static final SimpleImageDecorator DEFAULT = new SimpleImageDecorator();

    public void decorate(IGraphicalPart part, IFigure figure) {
        super.decorate(part, figure);
        if (figure instanceof SizeableImageFigure) {
            IViewer viewer = part.getSite().getViewer();
            if (viewer instanceof GalleryViewer) {
                IBaseLabelProvider labelProvider = ((GalleryViewer) viewer)
                        .getLabelProvider();
                if (labelProvider != null) {
                    Object model = part.getModel();
                    Properties properties = viewer.getProperties();
                    Image image = getImage(model, labelProvider);
                    decorateImage((SizeableImageFigure) figure, image,
                            properties);
                }
            }
        }
    }

    private Image getImage(Object element, IBaseLabelProvider labelProvider) {
        if (labelProvider instanceof ILabelProvider)
            return ((ILabelProvider) labelProvider).getImage(element);
        return null;
    }

    protected void decorateImage(SizeableImageFigure imageFigure, Image image,
            Properties properties) {
        imageFigure.setImage(image);
        boolean stretched = properties.getBoolean(GalleryViewer.ImageStretched,
                false);
        boolean constained = properties
                .getBoolean(GalleryViewer.ImageConstrained, false);
        imageFigure.setConstrained(constained);
        imageFigure.setStretched(stretched);
        Insets margins = (Insets) properties
                .get(GalleryViewer.ContentPaneMargins);
        imageFigure.setMargins(margins);
//        Dimension size = (Dimension) properties
//                .get(GalleryViewer.FrameContentSize);
//        if (size == null) {
//            imageFigure.setPreferredSize(imageFigure.getImageSize());
//        } else {
//            imageFigure.setPreferredSize(size);
//        }
    }

}