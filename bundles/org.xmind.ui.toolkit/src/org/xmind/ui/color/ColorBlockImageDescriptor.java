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
package org.xmind.ui.color;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * @author Frank Shaka
 */
public class ColorBlockImageDescriptor extends ImageDescriptor {

    public static final int DEFAULT_WIDTH = 16;
    public static final int DEFAULT_HEIGHT = 16;

    private RGB rgb;

    private Point size;

    private ColorBlockImageDescriptor(RGB color, Point size) {
        this.rgb = color;
        if (size == null) {
            this.size = new Point(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        } else {
            int width = size.x < 0 ? DEFAULT_WIDTH : size.x;
            int height = size.y < 0 ? DEFAULT_HEIGHT : size.y;
            this.size = new Point(width, height);
        }
    }

    public ImageData getImageData() {
        Display display = Display.getCurrent();
        int width = size.x;
        int height = size.y;
        Image image = new Image(display, width, height);
        GC gc = new GC(image);
        gc.setLineWidth(1);
        gc.setLineStyle(SWT.LINE_SOLID);
        gc.setAntialias(SWT.ON);
        if (rgb != null) {
            Color color = new Color(display, rgb);
            gc.setBackground(color);
            gc.fillRectangle(0, 0, width, height);
            color.dispose();
        } else {
            gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
            gc.fillRectangle(0, 0, width, height);
            gc.setForeground(display.getSystemColor(SWT.COLOR_RED));
            gc.drawLine(0, height - 1, width - 1, 0);
        }
        Color color = new Color(display, 88, 88, 88);//#585858
        gc.setForeground(color);
        color.dispose();
        gc.drawRectangle(0, 0, width - 1, height - 1);
        gc.dispose();
        ImageData imageData = image.getImageData();
        image.dispose();
        return imageData;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof ColorBlockImageDescriptor))
            return false;
        ColorBlockImageDescriptor that = (ColorBlockImageDescriptor) obj;
        return that.size.equals(this.size)
                && (that.rgb == this.rgb || (that.rgb != null && that.rgb
                        .equals(this.rgb)));
    }

    public String toString() {
        return "{ColorBlockImageDescriptor: color=" + rgb + ", size=" + size + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public static ColorBlockImageDescriptor createFromColor(Color color,
            Point size) {
        return new ColorBlockImageDescriptor(color.getRGB(), size);
    }

    public static ColorBlockImageDescriptor createFromColor(Color color) {
        return new ColorBlockImageDescriptor(color.getRGB(), null);
    }

    public static ColorBlockImageDescriptor createFromRGB(RGB rgb, Point size) {
        return new ColorBlockImageDescriptor(rgb, size);
    }

    public static ColorBlockImageDescriptor createFromRGB(RGB rgb) {
        return new ColorBlockImageDescriptor(rgb, null);
    }

}