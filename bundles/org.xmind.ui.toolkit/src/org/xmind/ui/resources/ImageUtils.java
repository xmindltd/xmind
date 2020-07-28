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
package org.xmind.ui.resources;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 * @author Frank Shaka
 */
public class ImageUtils {

    private static Display display = null;

    private static Display getDisplay() {
        if (display == null) {
            display = Display.getCurrent();
            if (display == null)
                display = Display.getDefault();
        }
        return display;
    }

    private static ImageRegistry imageRegistry = null;

    private static ImageRegistry getImageRegistry() {
        if (imageRegistry == null)
            imageRegistry = JFaceResources.getImageRegistry();
        return imageRegistry;
    }

    private ImageUtils() {
    }

    /**
     * @deprecated Use local {@link ImageRegistry} instances
     */
    public static Image getImage(String key) {
        return getImageRegistry().get(key);
    }

    /**
     * @deprecated Use local {@link ImageRegistry} instances
     */
    public static ImageDescriptor getDescriptor(String key) {
        return getImageRegistry().getDescriptor(key);
    }

    /**
     * @deprecated Use local {@link ImageRegistry} instances
     */
    public static void putImage(String key, Image image) {
        getImageRegistry().put(key, image);
    }

    /**
     * @deprecated Use local {@link ImageRegistry} instances
     */
    public static void putImageDescriptor(String key,
            ImageDescriptor descriptor) {
        getImageRegistry().put(key, descriptor);
    }

    /**
     * @deprecated Use local {@link ImageRegistry} instances
     */
    public static boolean disposeImage(String key) {
        Image img = getImage(key);
        if (img == null)
            return false;
        getImageRegistry().remove(key);
        if (!img.isDisposed())
            img.dispose();
        return true;
    }

    /**
     * @deprecated Use local {@link ImageRegistry} instances
     */
    public static Image getImage(String key, ImageData defaultImageData) {
        Image image = getImage(key);
        if (image == null) {
            getImageRegistry().put(key,
                    ImageDescriptor.createFromImageData(defaultImageData));
            image = getImage(key);
        }
        return image;
    }

    /**
     * @deprecated Use local {@link ImageRegistry} instances
     */
    public static Image getImage(String key,
            ImageDescriptor defaultImageDescriptor) {
        ImageDescriptor descriptor = getImageRegistry().getDescriptor(key);
        if (descriptor == null && defaultImageDescriptor != null) {
            getImageRegistry().put(key, defaultImageDescriptor);
        }
        return getImageRegistry().get(key);
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Image getImage(ImageDescriptor imgDesc) {
        return imgDesc == null ? null : getImage(imgDesc.toString(), imgDesc);
    }

    public static Image getSmallIconByRGB(String key, RGB rgb) {
        return getSmallIconByColor(key, ColorUtils.getColor(rgb));
    }

    public static Image getSmallIconByColor(String key, Color c) {
        Image image = getImage(key);
        if (image == null) {
            image = new Image(getDisplay(), 16, 16);
            GC gc = new GC(image);
            gc.setBackground(c);
            gc.fillRectangle(image.getBounds());
            gc.dispose();
            getImageRegistry().put(key, image);
        }
        return image;
    }

    public static Image getFilledImage(String key, Image src, int width,
            int height) {
        Image image = getImage(key);
        if (image == null) {
            image = createFilledImage(src, width, height);
            getImageRegistry().put(key, image);
        }
        return image;
    }

    public static Image createFilledImage(Image src, int width, int height) {
        Rectangle srcBounds = src.getBounds();
        Image dest = new Image(getDisplay(), width, height);
        GC gc = new GC(dest);
        gc.drawImage(src, 0, 0, srcBounds.width, srcBounds.height, 0, 0, width,
                height);
        gc.dispose();
        return dest;
    }

    public static Image createFilledImage2(Image src, int width, int height) {
        ImageData srcData = src.getImageData();
        return new Image(getDisplay(), srcData.scaledTo(width, height));
    }

    public static Image getScaledConstrainedImage(String key, Image src,
            int width, int height) {
        Image image = getImage(key);
        if (image == null) {
            image = createScaledConstrainedImage(src, width, height);
            getImageRegistry().put(key, image);
        }
        return image;
    }

    public static Image createScaledConstrainedImage(Image src, int width,
            int height) {
        ImageData srcData = src.getImageData();
        ImageData destData = new ImageData(width, height, srcData.depth,
                srcData.palette);
        destData.type = srcData.type;
        destData.transparentPixel = srcData.transparentPixel;
        destData.alpha = -1;

        Point destSize = getScaledConstrainedSize(srcData.width, srcData.height,
                width, height);
        int startX = (width - destSize.x) / 2;
        int startY = (height - destSize.y) / 2;

        srcData = srcData.scaledTo(destSize.x, destSize.y);

        if (srcData.transparentPixel != -1) {
            for (int x = 0; x < width; x++)
                for (int y = 0; y < height; y++)
                    destData.setPixel(x, y, srcData.transparentPixel);
        } else {
            for (int x = 0; x < width; x++)
                for (int y = 0; y < height; y++)
                    destData.setAlpha(x, y, 0);
        }

        int length = destSize.x;
        int[] pixels = new int[length];
        byte[] alphas = null;
        for (int y = 0; y < destSize.y; y++) {
            srcData.getPixels(0, y, length, pixels, 0);
            destData.setPixels(startX, startY + y, length, pixels, 0);
            if (srcData.alpha == -1 && srcData.alphaData != null) {
                if (alphas == null)
                    alphas = new byte[length];
                srcData.getAlphas(0, y, length, alphas, 0);
            } else if (srcData.alpha != -1 && alphas == null) {
                alphas = new byte[length];
                for (int i = 0; i < alphas.length; i++)
                    alphas[i] = (byte) srcData.alpha;
            } else if (alphas == null) {
                alphas = new byte[length];
                for (int i = 0; i < alphas.length; i++)
                    alphas[i] = (byte) 0xff;
            }
            destData.setAlphas(startX, startY + y, length, alphas, 0);
        }

        Image image = new Image(getDisplay(), destData);
        return image;
    }

    private static Point getScaledConstrainedSize(int w, int h, int maxWidth,
            int maxHeight) {
        if (w == 0 || h == 0)
            return new Point(0, 0);
        if (maxWidth < 0 && maxHeight < 0)
            return new Point(w, h);
        if (w <= maxWidth && h <= maxHeight)
            return new Point(w, h);
        int nw = w * maxHeight / h;
        int nh = h * maxWidth / w;
        if (maxWidth < 0)
            return new Point(nw, maxHeight);
        if (maxHeight < 0)
            return new Point(maxWidth, nh);
        if (nw < maxWidth)
            maxWidth = nw;
        if (nh < maxHeight)
            maxHeight = nh;
        return new Point(Math.max(1, maxWidth), Math.max(1, maxHeight));
    }

    public static Image createScaledImage(Image src, int width, int height) {
        Rectangle srcBounds = src.getBounds();
        Image image = new Image(getDisplay(), width, height);
        GC gc = new GC(image);
        gc.drawImage(src, 0, 0, srcBounds.width, srcBounds.height, 0, 0, width,
                height);
        gc.dispose();
        return image;
    }

    public static ImageDescriptor createErrorImage(int width, int height) {
        return new ErrorImageDescriptor(width, height);
    }

    public static Image getErrorImage(int width, int height) {
        String key = "image.common.error#" + width + "," + height; //$NON-NLS-1$ //$NON-NLS-2$
        Image img = getImage(key);
        if (img == null) {
            img = createErrorImage(width, height).createImage(getDisplay());
            if (img != null) {
                getImageRegistry().put(key, img);
            }
        }
        return img;
    }

    public static ImageDescriptor scaleImage(Display display,
            ImageDescriptor imageDesc, int width, int height) {
        if (imageDesc == null)
            return null;

        ImageData imageData = imageDesc.getImageData();
        if (imageData == null)    // Can by JPEG using CMYK colour space etc.
            return imageDesc;

        int newHeight = height;
        int newWidth = (imageData.width * newHeight) / imageData.height;
        if (newWidth > width) {
            newWidth = width;
            newHeight = (imageData.height * newWidth) / imageData.width;
        }

        // Use GC.drawImage to scale which gives better result on Mac

        Image newImage = new Image(display, newWidth, newHeight);

        GC gc = new GC(newImage);

        Image oldImage = imageDesc.createImage();

        gc.drawImage(oldImage, 0, 0, imageData.width, imageData.height, 0, 0,
                newWidth, newHeight);

        ImageDescriptor result = ImageDescriptor.createFromImage(newImage);

        oldImage.dispose();
        gc.dispose();

        return result;
    }

}
