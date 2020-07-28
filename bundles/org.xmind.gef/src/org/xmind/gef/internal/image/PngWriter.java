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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.xmind.gef.image.ImageWriter;

public class PngWriter extends ImageWriter {

    public PngWriter(Image image, OutputStream output) {
        super(image, output);
    }

    public void write(IProgressMonitor monitor) throws IOException {
        if (Util.isMac()) {
            writeOnCarbon(monitor);
        } else {
            ImageIO.write(ImageConverter.convert(getImage()),
                    "png", getOutput()); //$NON-NLS-1$
        }
    }

    private void writeOnCarbon(IProgressMonitor monitor) throws IOException {
        File temp;
        try {
            temp = File.createTempFile("EXPORT_", ".png"); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (IOException e) {
            throw e;
        }
        OutputStream tempOutput = new FileOutputStream(temp);
        SWTImageWriter tempWriter = new SWTImageWriter(getImage(),
                SWT.IMAGE_BMP, tempOutput);
        tempWriter.write(monitor);

        try {
            ImageIO.write(ImageIO.read(temp), "png", getOutput()); //$NON-NLS-1$
        } finally {
            temp.delete();
        }
    }

}