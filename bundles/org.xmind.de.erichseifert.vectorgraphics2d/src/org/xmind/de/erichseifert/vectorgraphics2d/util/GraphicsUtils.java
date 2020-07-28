/* ******************************************************************************
 * Copyright (c) 2006-2013 XMind Ltd. and others.
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
package org.xmind.de.erichseifert.vectorgraphics2d.util;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.PixelGrabber;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

import javax.swing.ImageIcon;

/**
 * Abstract class that contains utility functions for working with graphics. For
 * example, this includes font handling.
 *
 * @author Jason Wong
 */
public abstract class GraphicsUtils {
    /**
     * Default constructor that prevents creation of class.
     */
    protected GraphicsUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * This method returns {@code true} if the specified image has the
     * possibility to store transparent pixels. Inspired by
     * http://www.exampledepot.com/egs/java.awt.image/HasAlpha.html
     *
     * @param image
     *            Image that should be checked for alpha channel.
     * @return {@code true} if the specified image can have transparent pixels,
     *         {@code false} otherwise
     */
    public static boolean hasAlpha(Image image) {
        ColorModel cm;
        // If buffered image, the color model is readily available
        if (image instanceof BufferedImage) {
            BufferedImage bimage = (BufferedImage) image;
            cm = bimage.getColorModel();
        } else {
            // Use a pixel grabber to retrieve the image's color model;
            // grabbing a single pixel is usually sufficient
            PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
            try {
                pg.grabPixels();
            } catch (InterruptedException e) {
                return false;
            }
            // Get the image's color model
            cm = pg.getColorModel();
        }
        return cm.hasAlpha();
    }

    /**
     * This method returns {@code true} if the specified image has at least one
     * pixel that is not fully opaque.
     *
     * @param image
     *            Image that should be checked for non-opaque pixels.
     * @return {@code true} if the specified image has transparent pixels,
     *         {@code false} otherwise
     */
    public static boolean usesAlpha(Image image) {
        if (image == null) {
            return false;
        }
        BufferedImage bimage = toBufferedImage(image);
        Raster alphaRaster = bimage.getAlphaRaster();
        if (alphaRaster == null) {
            return false;
        }
        DataBuffer dataBuffer = alphaRaster.getDataBuffer();
        for (int i = 0; i < dataBuffer.getSize(); i++) {
            int alpha = dataBuffer.getElem(i);
            if (alpha < 255) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts an arbitrary image to a {@code BufferedImage}.
     *
     * @param image
     *            Image that should be converted.
     * @return a buffered image containing the image pixels, or the original
     *         instance if the image already was of type {@code BufferedImage}.
     */
    public static BufferedImage toBufferedImage(RenderedImage image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }

        ColorModel cm = image.getColorModel();
        WritableRaster raster = cm.createCompatibleWritableRaster(
                image.getWidth(), image.getHeight());
        boolean isRasterPremultiplied = cm.isAlphaPremultiplied();
        Hashtable<String, Object> properties = null;
        if (image.getPropertyNames() != null) {
            properties = new Hashtable<String, Object>();
            for (String key : image.getPropertyNames()) {
                properties.put(key, image.getProperty(key));
            }
        }

        BufferedImage bimage = new BufferedImage(cm, raster,
                isRasterPremultiplied, properties);
        image.copyData(raster);
        return bimage;
    }

    /**
     * This method returns a buffered image with the contents of an image. Taken
     * from http://www.exampledepot.com/egs/java.awt.image/Image2Buf.html
     *
     * @param image
     *            Image to be converted
     * @return a buffered image with the contents of the specified image
     */
    public static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }
        // This code ensures that all the pixels in the image are loaded
        image = new ImageIcon(image).getImage();
        // Determine if the image has transparent pixels
        boolean hasAlpha = hasAlpha(image);

        // Create a buffered image with a format that's compatible with the
        // screen
        BufferedImage bimage = null;
        GraphicsEnvironment ge = GraphicsEnvironment
                .getLocalGraphicsEnvironment();
        try {
            // Determine the type of transparency of the new buffered image
            int transparency = Transparency.OPAQUE;
            if (hasAlpha) {
                transparency = Transparency.TRANSLUCENT;
            }
            // Create the buffered image
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(image.getWidth(null),
                    image.getHeight(null), transparency);
        } catch (HeadlessException e) {
            // The system does not have a screen
            bimage = null;
        }
        if (bimage == null) {
            // Create a buffered image using the default color model
            int type = BufferedImage.TYPE_INT_RGB;
            if (hasAlpha) {
                type = BufferedImage.TYPE_INT_ARGB;
            }
            bimage = new BufferedImage(image.getWidth(null),
                    image.getHeight(null), type);
        }
        // Copy image to buffered image
        Graphics g = bimage.createGraphics();
        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return bimage;
    }
}
