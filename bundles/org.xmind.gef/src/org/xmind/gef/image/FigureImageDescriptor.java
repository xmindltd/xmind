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

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.xmind.gef.draw2d.graphics.ScaledGraphics;

public class FigureImageDescriptor extends ImageDescriptor {

    public static ImageDescriptor createFromFigure(IFigure figure) {
        return new FigureImageDescriptor(new IFigure[] { figure },
                ImageExportUtils.createExportAreaProvider(
                        ImageExportUtils.getBounds(figure)));
    }

    public static ImageDescriptor createFromFigure(IFigure figure,
            IExportAreaProvider exportAreaProvider) {
        return new FigureImageDescriptor(new IFigure[] { figure },
                exportAreaProvider);
    }

    public static ImageDescriptor createFromFigures(IFigure[] figures,
            IExportAreaProvider exportAreaProvider) {
        return new FigureImageDescriptor(figures, exportAreaProvider);
    }

    private final IFigure[] figures;

    private IExportAreaProvider exportAreaProvider;

    private ImageData data = null;

    protected FigureImageDescriptor(IFigure[] figures,
            IExportAreaProvider exportAreaProvider) {
        this.figures = figures;
        this.exportAreaProvider = exportAreaProvider;
    }

    @Override
    public Image createImage(boolean returnMissingImageOnError, Device device) {
        Rectangle exportArea = exportAreaProvider.getExportArea();
        Image image = new Image(device, exportArea.width, exportArea.height);
        double scale = exportAreaProvider.getScale();
        if (scale == 0)
            return image;
        return render(image, returnMissingImageOnError, exportArea, scale);
    }

    private Image render(Image image, boolean returnMissingImageOnError,
            Rectangle exportArea, double scale) {
        GC gc = new GC(image);
        SWTGraphics baseGraphcis = new SWTGraphics(gc);
        baseGraphcis.translate(-exportArea.x, -exportArea.y);

        Graphics graphics = baseGraphcis;
        ScaledGraphics scaledGraphics = null;
        if (scale > 0 && scale != 1) {
            if (ScaledGraphics.SCALED_GRAPHICS_ENABLED) {
                scaledGraphics = new ScaledGraphics(graphics);
                scaledGraphics.scale(scale);
                graphics = scaledGraphics;
            } else {
                graphics.scale(scale);
            }
        }
        try {
            graphics.pushState();
            try {
                for (int i = 0; i < figures.length; i++) {
                    IFigure figure = figures[i];
                    figure.paint(graphics);
                    graphics.restoreState();
                }
            } finally {
                graphics.popState();
            }
        } catch (Throwable t) {
            if (!returnMissingImageOnError) {
                image.dispose();
                image = null;
            }
        } finally {
            if (scaledGraphics != null) {
                scaledGraphics.dispose();
            }
            baseGraphcis.dispose();
            gc.dispose();
        }
        return image;
    }

    @Override
    public ImageData getImageData() {
        if (data == null) {
            Image tempImage = createImage(false);
            if (tempImage != null) {
                data = tempImage.getImageData();
                tempImage.dispose();
            }
        }
        return data;
    }

}