/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.gef.internal.image;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.xmind.neuquant.NeuQuant;

/**
 * @author Frank Shaka
 */
public class ImageConverter {

    private static final int DEPTH_256 = 8;

    private static final int OFFSET = 3;

    private static final int MASK = (0xFF >> OFFSET) << OFFSET;

    private static final int INIT_COLOR_DIFFERENCE_THRESHOLD = 255 * 3;

    /**
     * @param srcImage
     * @return
     */
    public static ImageData converTo256Colors(ImageData srcImage) {
        return convertDepth_4(srcImage, DEPTH_256);
    }

    protected static ImageData convertDepth(ImageData srcImage, int depth) {
        int numMaxColors = 1 << depth;
        final Map<RGB, Integer> colorOccurrences = new HashMap<RGB, Integer>();

        /* Calculate occurrences of each color in source image */
        PaletteData srcPalette = srcImage.palette;
        for (int i = 0; i < srcImage.width; i++) {
            for (int j = 0; j < srcImage.height; j++) {
                RGB rgb = srcPalette.getRGB(srcImage.getPixel(i, j));

                /*
                 * Cut off lower bits of each color value in order to reduce the
                 * differences among colors and raise the frequencies of colors
                 * with lower occurrences. The disadvantage is that it makes the
                 * image a little distorted since most of the colors will be
                 * changed.
                 */
                rgb = getShortenedRGB(rgb);

                int occur = !colorOccurrences.containsKey(rgb) ? 1
                        : colorOccurrences.get(rgb) + 1;
                colorOccurrences.put(rgb, occur);
            }
        }

        /* Sort colors by occurrences */
        Set<RGB> sortedColors = new TreeSet<RGB>(new Comparator<RGB>() {
            public int compare(RGB o1, RGB o2) {
                int x = colorOccurrences.get(o2) - colorOccurrences.get(o1);
                return x == 0 ? 1 : x;
            }
        });
        sortedColors.addAll(colorOccurrences.keySet());

        /* Filter colors to fit within the max size */
        List<RGB> newPaletteColors = new ArrayList<RGB>(sortedColors);
        if (newPaletteColors.size() > numMaxColors)
            /* Cut off colors with lower occurrrences */
            newPaletteColors = newPaletteColors.subList(0, numMaxColors);

        /* Generate new palette */
        RGB[] colors = newPaletteColors
                .toArray(new RGB[newPaletteColors.size()]);
        PaletteData newPalette = new PaletteData(colors);

        Map<RGB, Integer> pixelValues = new HashMap<RGB, Integer>();
        for (int i = 0; i < colors.length; i++) {
            pixelValues.put(colors[i], i);
        }

        Map<RGB, RGB> oldToNew = new HashMap<RGB, RGB>();

        /* Generate new image from source image with new palette */
        ImageData result = new ImageData(srcImage.width, srcImage.height,
                depth, newPalette);
        for (int i = 0; i < srcImage.width; i++) {
            for (int j = 0; j < srcImage.height; j++) {
                RGB oldColor = srcPalette.getRGB(srcImage.getPixel(i, j));

                /* Convert colors from source image to colors in new palette */
                RGB newColor = oldToNew.get(oldColor);
                if (newColor == null) {
                    newColor = findSimilarColor(oldColor, colors);
                    oldToNew.put(oldColor, newColor);
                }

                result.setPixel(i, j, pixelValues.get(newColor));
            }
        }
        return result;
    }

    /**
     * @param rgb
     * @return
     */
    private static RGB getShortenedRGB(RGB rgb) {
        rgb.red = rgb.red & MASK;
        rgb.blue = rgb.blue & MASK;
        rgb.green = rgb.green & MASK;
        return rgb;
    }

    private static RGB findSimilarColor(RGB src, RGB[] colors) {
        RGB result = null;
        int droppingThreshold = INIT_COLOR_DIFFERENCE_THRESHOLD;
        for (int i = 0; i < colors.length; i++) {
            RGB toTest = colors[i];
            int diff = getColorDifference(src, toTest);
            if (diff < droppingThreshold) {
                droppingThreshold = diff;
                result = toTest;
            }
        }
        return result;
    }

    static int getColorDifference(RGB c1, RGB c2) {
        return Math.abs(c1.red - c2.red) + Math.abs(c1.green - c2.green)
                + Math.abs(c1.blue - c2.blue);
    }

    protected static ImageData convertDepth_2(ImageData srcImage, int depth) {
        int numMaxColors = 1 << depth;
        final Map<RGB, Integer> colorOccurrences = new HashMap<RGB, Integer>();

        /* Calculate occurrences of each color in source image */
        PaletteData srcPalette = srcImage.palette;
        for (int i = 0; i < srcImage.width; i++) {
            for (int j = 0; j < srcImage.height; j++) {
                RGB rgb = srcPalette.getRGB(srcImage.getPixel(i, j));
                int occur = !colorOccurrences.containsKey(rgb) ? 1
                        : colorOccurrences.get(rgb) + 1;
                colorOccurrences.put(rgb, occur);
            }
        }

        IColorReplacingPolicy policy;
        if (colorOccurrences.size() <= numMaxColors) {
            policy = new SameColorReplacingPolicy();
        } else {
            policy = new MinRiskColorReplacingPolicy();
        }

        PaletteData newPalette = new PaletteData(policy.getReplacingColors(
                numMaxColors, colorOccurrences));
        Map<RGB, Integer> pixelValues = new HashMap<RGB, Integer>();
        RGB[] rgbs = newPalette.getRGBs();
        for (int i = 0; i < rgbs.length; i++) {
            pixelValues.put(rgbs[i], i);
        }

        /* Generate new image from source image with new palette */
        ImageData result = new ImageData(srcImage.width, srcImage.height,
                depth, newPalette);
        for (int i = 0; i < srcImage.width; i++) {
            for (int j = 0; j < srcImage.height; j++) {
                RGB rgb = srcPalette.getRGB(srcImage.getPixel(i, j));
                RGB newRGB = policy.getReplacedColor(rgb);
                result.setPixel(i, j, pixelValues.get(newRGB));
            }
        }
        return result;
    }

//    protected static ImageData convertDepth_3(ImageData srcImage, int depth) {
//        EightTreeQuantizer quantizer = new EightTreeQuantizer(depth);
//        final Map<RGB, Integer> colorOccurrences = new HashMap<RGB, Integer>();
//
//        /* Calculate occurrences of each color in source image */
//        PaletteData srcPalette = srcImage.palette;
//        for (int i = 0; i < srcImage.width; i++) {
//            for (int j = 0; j < srcImage.height; j++) {
//                RGB rgb = srcPalette.getRGB(srcImage.getPixel(i, j));
//                int occur = !colorOccurrences.containsKey(rgb) ? 1
//                        : colorOccurrences.get(rgb) + 1;
//                colorOccurrences.put(rgb, occur);
//            }
//        }
//        quantizer.setColorOccurrences(colorOccurrences);
//        return quantizer.processImage(srcImage);
//    }

    protected static ImageData convertDepth_4(ImageData srcImage, int depth) {
        if (depth != DEPTH_256)
            throw new IllegalArgumentException();
        NeuQuant nq = new NeuQuant();
        nq.init(srcImage, 1);
        PaletteData oldPalette = srcImage.palette;
        PaletteData newPalette = new PaletteData(nq.getColourMap());
        int width = srcImage.width;
        int height = srcImage.height;
        ImageData result = new ImageData(width, height, depth, newPalette);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                RGB rgb = oldPalette.getRGB(srcImage.getPixel(i, j));
//                rgb = nq.convert( rgb );
//                result.setPixel( i, j, newPalette.getPixel( rgb ) );
//                result.setPixel( i, j, newPalette.getPixel( rgb ) );
                result.setPixel(i, j, nq.lookup(rgb));
            }
        }
        return result;
    }

    private static final PaletteData PALETTE_DATA = new PaletteData(0xFF0000,
            0xFF00, 0xFF);

    /**
     * Converts an AWT based buffered image into an SWT <code>Image</code>. This
     * will always return an <code>Image</code> that has 24 bit depth regardless
     * of the type of AWT buffered image that is passed into the method.
     * 
     * @param srcImage
     *            the {@link java.awt.image.BufferedImage} to be converted to an
     *            <code>Image</code>
     * @return an <code>Image</code> that represents the same image data as the
     *         AWT <code>BufferedImage</code> type.
     */
    public static Image convert(Device device, BufferedImage srcImage) {
        // We can force bitdepth to be 24 bit because BufferedImage getRGB allows us to always
        // retrieve 24 bit data regardless of source color depth.
        ImageData swtImageData = new ImageData(srcImage.getWidth(), srcImage
                .getHeight(), 24, PALETTE_DATA);

        // ensure scansize is aligned on 32 bit.
        int scansize = (((srcImage.getWidth() * 3) + 3) * 4) / 4;

        WritableRaster alphaRaster = srcImage.getAlphaRaster();
        byte[] alphaBytes = new byte[srcImage.getWidth()];

        for (int y = 0; y < srcImage.getHeight(); y++) {
            int[] buff = srcImage.getRGB(0, y, srcImage.getWidth(), 1, null, 0,
                    scansize);
            swtImageData.setPixels(0, y, srcImage.getWidth(), buff, 0);

            // check for alpha channel
            if (alphaRaster != null) {
                int[] alpha = alphaRaster.getPixels(0, y, srcImage.getWidth(),
                        1, (int[]) null);
                for (int i = 0; i < srcImage.getWidth(); i++)
                    alphaBytes[i] = (byte) alpha[i];
                swtImageData
                        .setAlphas(0, y, srcImage.getWidth(), alphaBytes, 0);
            }
        }

        return new Image(device, swtImageData);
    }

    /**
     * Converts an swt based image into an AWT <code>BufferedImage</code>. This
     * will always return a <code>BufferedImage</code> that is of type
     * <code>BufferedImage.TYPE_INT_ARGB</code> regardless of the type of swt
     * image that is passed into the method.
     * 
     * @param srcImage
     *            the {@link org.eclipse.swt.graphics.Image} to be converted to
     *            a <code>BufferedImage</code>
     * @return a <code>BufferedImage</code> that represents the same image data
     *         as the swt <code>Image</code>
     */
    public static BufferedImage convert(Image srcImage) {

        ImageData imageData = srcImage.getImageData();
        int width = imageData.width;
        int height = imageData.height;
        ImageData maskData = null;
        int alpha[] = new int[1];

        if (imageData.alphaData == null)
            maskData = imageData.getTransparencyMask();

        // now we should have the image data for the bitmap, decompressed in imageData[0].data.
        // Convert that to a Buffered Image.
        BufferedImage image = new BufferedImage(imageData.width,
                imageData.height, BufferedImage.TYPE_INT_ARGB);

        WritableRaster alphaRaster = image.getAlphaRaster();

        // loop over the imagedata and set each pixel in the BufferedImage to the appropriate color.
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = imageData.getPixel(x, y);

                color = translateColor(imageData, color);
                image.setRGB(x, y, color);

                // check for alpha channel
                if (alphaRaster != null) {
                    if (imageData.alphaData != null) {
                        alpha[0] = imageData.getAlpha(x, y);
                        alphaRaster.setPixel(x, y, alpha);
                    } else {
                        // check for transparency mask
                        if (maskData != null) {
                            alpha[0] = maskData.getPixel(x, y) == 0 ? 0 : 255;
                            alphaRaster.setPixel(x, y, alpha);
                        }
                    }
                }
            }
        }

        return image;
    }

    private static int translateColor(ImageData imageData, int color) {

        int bitCount = imageData.depth;
        RGB[] rgb = imageData.getRGBs();

        if (bitCount == 1 || bitCount == 4 || bitCount == 8) {
            // Look up actual rgb value in the rgb array.
            if (rgb != null) {
                java.awt.Color foo = new java.awt.Color(rgb[color].red,
                        rgb[color].green, rgb[color].blue);
                color = foo.getRGB();
            } else {
                color = 0;
            }
        } else if (bitCount == 16) {
            int BLUE_MASK = 0x1f;
            int GREEN_MASK = 0x3e0;
            int RED_MASK = 0x7C00;

            // Each word in the bitmap array represents a single pixels, 5 bits for each
            // red, green and blue.
            color = applyRGBMask(color, RED_MASK, GREEN_MASK, BLUE_MASK);
        } else if (bitCount == 24) {
            // 3 8 bit color values.
            int blue = (color & 0x00ff0000) >> 16;
            int green = (color & 0x0000ff00) >> 8;
            int red = (color & 0x000000ff);

            java.awt.Color foo = new java.awt.Color(red, green, blue);
            color = foo.getRGB();
        } else if (bitCount == 32) {
            int blue = (color & 0xff000000) >>> 24;
            int green = (color & 0x00ff0000) >> 16;
            int red = (color & 0x0000ff00) >> 8;

            java.awt.Color foo = new java.awt.Color(red, green, blue);
            color = foo.getRGB();
        }

        return color;
    }

    private static int applyRGBMask(int color, int redMask, int greenMask,
            int blueMask) {
        int shiftCount;
        int maskSize;
        int red;
        int green;
        int blue;

        shiftCount = getShiftCount(redMask);
        maskSize = countBits(redMask);
        red = (color & redMask) >>> shiftCount;
        // Scale the color value to something between 0 and 255.
        red = red * 255 / ((int) Math.pow(2, maskSize) - 1);

        shiftCount = getShiftCount(greenMask);
        maskSize = countBits(greenMask);
        green = (color & greenMask) >>> shiftCount;
        // Scale the color value to something between 0 and 255.
        green = green * 255 / ((int) Math.pow(2, maskSize) - 1);

        shiftCount = getShiftCount(blueMask);
        maskSize = countBits(blueMask);
        blue = (color & blueMask) >>> shiftCount;
        // Scale the color value to something between 0 and 255.
        blue = blue * 255 / ((int) Math.pow(2, maskSize) - 1);

        java.awt.Color foo = new java.awt.Color(red, green, blue);
        color = foo.getRGB();

        return color;
    }

    private static int getShiftCount(int mask) {
        int count = 0;

        while (mask != 0 && ((mask & 0x1) == 0)) {
            mask = mask >>> 1;
            count++;
        }

        return count;
    }

    private static int countBits(int mask) {
        int count = 0;
        for (int index = 0; index < 32; index++) {
            if ((mask & 0x1) != 0) {
                count++;
            }
            mask = mask >>> 1;
        }

        return count;
    }

}