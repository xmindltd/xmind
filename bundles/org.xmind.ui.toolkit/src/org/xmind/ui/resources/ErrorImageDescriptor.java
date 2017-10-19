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
package org.xmind.ui.resources;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ErrorImageDescriptor extends ImageDescriptor {

    private int width;

    private int height;

    protected ErrorImageDescriptor(int width, int height) {
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
    }

    public ImageData getImageData() {
        Device device = Display.getCurrent();
        if (device == null)
            return null;

        // declare resources
        Image img = new Image(device, width, height);
        GC gc = new GC(img);
        Color white = new Color(device, 0xff, 0xff, 0xff);
        Color red = new Color(device, 0xff, 0, 0);

        ImageData data = null;
        try {
            // draw contents
            gc.setBackground(white);
            gc.fillRectangle(0, 0, width, height);
            int padding = Math.min(width, height);
            padding = (padding < 8) ? 0
                    : ((padding < 16) ? padding / 2 - 4 : 4);
            gc.setForeground(red);
            int w = width - 1;
            int h = height - 1;
            gc.drawRectangle(0, 0, w, h);
            w -= padding;
            h -= padding;
            gc.drawLine(padding, padding, w, h);
            gc.drawLine(padding, h, w, padding);
            data = img.getImageData();
            data.transparentPixel = data.palette.getPixel(new RGB(0xff, 0xff,
                    0xff));
        } finally {
            // dispose resources
            gc.dispose();
            img.dispose();
            white.dispose();
            red.dispose();
        }

        return data;
    }

    public String toString() {
        return "ErrorImageDescriptor(" + width + "," + height + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof ErrorImageDescriptor))
            return false;
        ErrorImageDescriptor that = (ErrorImageDescriptor) obj;
        return this.width == that.width && this.height == that.height;
    }

    public int hashCode() {
        return height ^ width;
    }

}