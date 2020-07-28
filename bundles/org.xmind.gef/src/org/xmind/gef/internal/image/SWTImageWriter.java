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
package org.xmind.gef.internal.image;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.xmind.gef.image.ImageWriter;

public class SWTImageWriter extends ImageWriter {

    private int format;

    public SWTImageWriter(Image image, int format, OutputStream output) {
        super(image, output);
        this.format = format;
    }

    public SWTImageWriter(ImageData[] imageData, int format, OutputStream output) {
        super(imageData, output);
        this.format = format;
    }

    public int getFormat() {
        return format;
    }

    public void write(IProgressMonitor monitor) throws IOException {
        ImageData[] data = getCompatibleImageData();
        ImageLoader loader = new ImageLoader();
        loader.data = data;
        try {
            loader.save(getOutput(), getFormat());
        } catch (Throwable e) {
            throw new IOException(e.getMessage());
        }
    }

    protected ImageData[] getCompatibleImageData() {
        ImageData[] oldData = getImageData();
        return needsDepthConvertion(getFormat()) ? convertTo256Colors(oldData)
                : oldData;
    }

    protected ImageData[] convertTo256Colors(ImageData[] imageData) {
        ImageData[] newImageData = new ImageData[imageData.length];
        for (int i = 0; i < imageData.length; i++) {
            newImageData[i] = ImageConverter.converTo256Colors(imageData[i]);
        }
        return newImageData;
    }

    private static boolean needsDepthConvertion(int format) {
        return format == SWT.IMAGE_GIF
                || (format == SWT.IMAGE_BMP && Util.isMac());
    }

}