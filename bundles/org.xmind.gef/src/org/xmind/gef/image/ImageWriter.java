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

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

public abstract class ImageWriter {

    private Image image;

    private ImageData[] imageData;

    private OutputStream output;

    protected ImageWriter(Image image, OutputStream output) {
        this.image = image;
        this.imageData = new ImageData[] { image.getImageData() };
        this.output = output;
    }

    protected ImageWriter(ImageData[] imageData, OutputStream output) {
        this.image = null;
        this.imageData = imageData;
        this.output = output;
    }

    public Image getImage() {
        return image;
    }

    public ImageData[] getImageData() {
        return imageData;
    }

    public OutputStream getOutput() {
        return output;
    }

    public void write() throws IOException {
        write(null);
    }

    public abstract void write(IProgressMonitor monitor) throws IOException;

}