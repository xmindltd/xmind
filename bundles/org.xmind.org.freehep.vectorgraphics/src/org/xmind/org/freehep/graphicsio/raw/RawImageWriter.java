// Copyright 2003, FreeHEP
package org.xmind.org.freehep.graphicsio.raw;

import java.awt.image.RenderedImage;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;

import org.xmind.org.freehep.util.images.ImageUtilities;

/**
 *
 * @author Jason Wong
 */
public class RawImageWriter extends ImageWriter {

    public RawImageWriter(RawImageWriterSpi originatingProvider) {
        super(originatingProvider);
    }

    public void write(IIOMetadata streamMetadata, IIOImage image,
            ImageWriteParam param) throws IOException {
        if (image == null)
            throw new IllegalArgumentException("image == null"); //$NON-NLS-1$

        if (image.hasRaster())
            throw new UnsupportedOperationException("Cannot write rasters"); //$NON-NLS-1$

        Object output = getOutput();
        if (output == null)
            throw new IllegalStateException("output was not set"); //$NON-NLS-1$

        if (param == null)
            param = getDefaultWriteParam();

        ImageOutputStream ios = (ImageOutputStream) output;
        RenderedImage ri = image.getRenderedImage();

        RawImageWriteParam rawParam = (RawImageWriteParam) param;
        byte[] bytes = ImageUtilities.getBytes(ri, rawParam.getBackground(),
                rawParam.getCode(), rawParam.getPad());
        ios.write(bytes);
        ios.close();
    }

    public IIOMetadata convertStreamMetadata(IIOMetadata inData,
            ImageWriteParam param) {
        return null;
    }

    public IIOMetadata convertImageMetadata(IIOMetadata inData,
            ImageTypeSpecifier imageType, ImageWriteParam param) {
        return null;
    }

    public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType,
            ImageWriteParam param) {
        return null;
    }

    public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
        return null;
    }

    public ImageWriteParam getDefaultWriteParam() {
        return new RawImageWriteParam(getLocale());
    }
}
