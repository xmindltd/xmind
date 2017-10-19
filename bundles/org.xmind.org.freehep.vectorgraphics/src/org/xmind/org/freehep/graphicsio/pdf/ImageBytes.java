// Copyright 2000-2007 FreeHEP
package org.xmind.org.freehep.graphicsio.pdf;

import java.awt.Color;
import java.awt.image.RenderedImage;
import java.io.IOException;

import org.xmind.org.freehep.graphicsio.ImageConstants;
import org.xmind.org.freehep.graphicsio.ImageGraphics2D;

/**
 * PDF writes images as ZLIB or JPEG. This class converts an image to a byte[].
 * If neither {@link ImageConstants#ZLIB} nor {@link ImageConstants#JPEG} is
 * passed the smallest size is stored for {@link #getBytes()}. The method
 * {@link #getFormat()} returns the used format.
 *
 * @author Steffen Greiffenberg
 * @version $Revision$
 */
class ImageBytes {

    /**
     * Used format during creation of bytes
     */
    private String format;

    /**
     * Bytes in given format
     */
    private byte[] bytes;

    /**
     * Encodes the passed image
     *
     * @param image
     *            image to convert
     * @param bkg
     *            background color
     * @param format
     *            format could be {@link ImageConstants#ZLIB} or
     *            {@link ImageConstants#JPEG}
     * @param colorModel
     *            e.g.
     *            {@link org.xmind.org.freehep.graphicsio.ImageConstants#COLOR_MODEL_RGB}
     * @throws IOException
     *             thrown by
     *             {@link org.xmind.org.freehep.graphicsio.ImageGraphics2D#toByteArray(java.awt.image.RenderedImage, String, String, java.util.Properties)}
     */
    public ImageBytes(RenderedImage image, Color bkg, String format,
            String colorModel) throws IOException {

        // ZLIB encoding, transparent images allways require ZLIB
        if (ImageConstants.ZLIB.equals(format)
                || (image.getColorModel().hasAlpha() && (bkg == null))) {
            bytes = toZLIB(image, bkg, colorModel);
            this.format = ImageConstants.ZLIB;
        }

        // JPG encoding
        else if (ImageConstants.JPEG.equals(format)) {
            bytes = toJPG(image);
            this.format = ImageConstants.JPG;
        } else {
            // calculate both byte arrays
            byte[] jpgBytes = toJPG(image);
            byte[] zlibBytes = toZLIB(image, bkg, colorModel);

            // compare sizes to determine smalles format
            if (jpgBytes.length < 0.5 * zlibBytes.length) {
                bytes = jpgBytes;
                this.format = ImageConstants.JPG;
            } else {
                bytes = zlibBytes;
                this.format = ImageConstants.ZLIB;
            }
        }
    }

    /**
     * Creates the ZLIB Bytes for PDF images
     *
     * @param image
     *            image to convert
     * @param bkg
     *            background color
     * @param colorModel
     *            e.g.
     *            {@link org.xmind.org.freehep.graphicsio.ImageConstants#COLOR_MODEL_RGB}
     * @return bytes
     * @throws IOException
     *             thrown by
     *             {@link org.xmind.org.freehep.graphicsio.ImageGraphics2D#toByteArray(java.awt.image.RenderedImage, String, String, java.util.Properties)}
     */
    private byte[] toZLIB(RenderedImage image, Color bkg, String colorModel)
            throws IOException {
        return ImageGraphics2D.toByteArray(image, ImageConstants.RAW,
                ImageConstants.ENCODING_FLATE_ASCII85,
                ImageGraphics2D.getRAWProperties(bkg, colorModel));
    }

    /**
     * Creates the JPG bytes for PDF images
     *
     * @param image
     *            image to convert
     * @return bytes
     * @throws IOException
     *             thrown by
     *             {@link org.xmind.org.freehep.graphicsio.ImageGraphics2D#toByteArray(java.awt.image.RenderedImage, String, String, java.util.Properties)}
     */
    private byte[] toJPG(RenderedImage image) throws IOException {
        return ImageGraphics2D.toByteArray(image, ImageConstants.JPG,
                ImageConstants.ENCODING_ASCII85, null);
    }

    /**
     * @return bytes
     */
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * @return used encoding format
     */
    public String getFormat() {
        return format;
    }
}
/**
 * $Log$
 */
