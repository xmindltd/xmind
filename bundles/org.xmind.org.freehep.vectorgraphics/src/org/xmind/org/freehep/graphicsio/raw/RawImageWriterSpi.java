// Copyright 2003-2006, FreeHEP
package org.xmind.org.freehep.graphicsio.raw;

import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

/**
 *
 * @author Jason Wong
 */
public class RawImageWriterSpi extends ImageWriterSpi {

    @SuppressWarnings("nls")
    public RawImageWriterSpi() {
        super("FreeHEP Java Libraries, http://java.freehep.org/", "1.0",
                new String[] { "raw" }, new String[] { "raw" },
                new String[] { "image/x-raw" },
                "org.xmind.org.freehep.graphicsio.raw.RawImageWriter",
                new Class[] { ImageOutputStream.class }, null, false, null, null, null, null,
                false, null, null, null, null);
    }

    public String getDescription(Locale locale) {
        return "FreeHEP RAW Image Format"; //$NON-NLS-1$
    }

    public ImageWriter createWriterInstance(Object extension)
            throws IOException {
        return new RawImageWriter(this);
    }

    public boolean canEncodeImage(ImageTypeSpecifier type) {
        // FIXME
        return true;
    }
}
