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
package org.xmind.gef.image;

import java.io.OutputStream;

import org.eclipse.draw2d.FreeformFigure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.internal.image.ConstrainedExportAreaProvider;
import org.xmind.gef.internal.image.FittedExportAreaProvider;
import org.xmind.gef.internal.image.MaxPixelsExportAreaProvider;
import org.xmind.gef.internal.image.SWTImageWriter;
import org.xmind.gef.internal.image.StretchedExportAreaProvider;

public class ImageExportUtils {

    public static IExportAreaProvider createExportAreaProvider(
            Rectangle sourceArea) {
        return createExportAreaProvider(sourceArea, ResizeConstants.RESIZE_NONE,
                -1, -1, null);
    }

    public static IExportAreaProvider createExportAreaProvider(
            Rectangle sourceArea, int resizeStrategy) {
        return createExportAreaProvider(sourceArea, resizeStrategy, -1, -1,
                null);
    }

    public static IExportAreaProvider createExportAreaProvider(
            Rectangle sourceArea, int resizeStrategy, int wHint, int hHint) {
        return createExportAreaProvider(sourceArea, resizeStrategy, wHint,
                hHint, null);
    }

    public static IExportAreaProvider createExportAreaProvider(
            Rectangle sourceArea, int resizeStrategy, Insets margins) {
        return createExportAreaProvider(sourceArea, resizeStrategy, -1, -1,
                margins);
    }

    public static IExportAreaProvider createExportAreaProvider(
            Rectangle sourceArea, Insets margins) {
        return createExportAreaProvider(sourceArea, ResizeConstants.RESIZE_NONE,
                -1, -1, margins);
    }

    public static IExportAreaProvider createExportAreaProvider(
            Rectangle sourceArea, int resizeStrategy, int wHint, int hHint,
            Insets margins) {
        switch (resizeStrategy) {
        case ResizeConstants.RESIZE_STRETCH:
            return new StretchedExportAreaProvider(sourceArea, wHint, hHint,
                    margins);
        case ResizeConstants.RESIZE_FIT:
            return new FittedExportAreaProvider(sourceArea, wHint, hHint,
                    margins);
        case ResizeConstants.RESIZE_CONSTRAIN:
            return new ConstrainedExportAreaProvider(sourceArea, wHint, hHint,
                    margins);
        case ResizeConstants.RESIZE_MAXPIXELS:
            return new MaxPixelsExportAreaProvider(sourceArea, wHint, hHint,
                    margins);
        }
        return new ExportAreaProvider(sourceArea, wHint, hHint, margins);
    }

    public static ImageWriter createImageWriter(Image image, int format,
            OutputStream output) {
        return new SWTImageWriter(image, format, output);
    }

    public static ImageWriter createImageWriter(ImageData[] imageData,
            int format, OutputStream output) {
        return new SWTImageWriter(imageData, format, output);
    }

    public static Rectangle calcBoundsUnion(IFigure[] figures) {
        Rectangle r = null;
        for (IFigure figure : figures) {
            r = Geometry.union(r, getBounds(figure));
        }
        return r;
    }

    public static Rectangle calcBoundsIntersection(IFigure[] figures) {
        Rectangle r = null;
        for (IFigure figure : figures) {
            r = Geometry.intersect(r, getBounds(figure));
        }
        return r;
    }

    public static Rectangle getBounds(IFigure figure) {
        if (figure instanceof FreeformFigure) {
            return ((FreeformFigure) figure).getFreeformExtent();
        }
        return figure.getBounds();
    }

    public static Image createImage(Device device, IExportSourceProvider source,
            int resizeStrategy, int wHint, int hHint) {
        IExportAreaProvider area = createExportAreaProvider(
                source.getSourceArea(), resizeStrategy, wHint, hHint,
                source.getMargins());
        FigureRenderer renderer = new FigureRenderer();
        renderer.init(source, area);
        return createImage(device, renderer);
//        ImageDescriptor imageDescriptor = createImageDescriptor(source,
//                resizeStrategy, wHint, hHint);
//        return imageDescriptor.createImage(false, device);
    }

    public static Image createImage(Device device, FigureRenderer renderer) {
        Rectangle bounds = renderer.getBounds();
        Image image = new Image(device, bounds.width, bounds.height);
        GC gc = new GC(image);
        try {
            renderer.render(gc);
        } finally {
            gc.dispose();
        }
        return image;
    }

    /**
     * 
     * @param image
     * @param stream
     * @param format
     */
    public static void saveImage(Image image, OutputStream stream, int format) {
        ImageData data = image.getImageData();
        ImageLoader saver = new ImageLoader();
        saver.data = new ImageData[] { data };
        saver.save(stream, format);
    }

}