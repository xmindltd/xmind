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
package org.xmind.ui.richtext;

import org.eclipse.swt.graphics.Image;

/**
 * @author Frank Shaka
 */
public class ImagePlaceHolder implements Cloneable {

    public static final String PLACE_HOLDER = "\uFFFC"; //$NON-NLS-1$

    public int offset;

    public Image image;

    public ImagePlaceHolder(int offset, Image image) {
        this.offset = offset;
        this.image = image;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof ImagePlaceHolder))
            return false;
        ImagePlaceHolder that = (ImagePlaceHolder) obj;
        return this.offset == that.offset && this.image == that.image;
    }

    public String toString() {
        return "Image{offset=" + offset + ",image=" + image + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public Object clone() {
        ImagePlaceHolder img = new ImagePlaceHolder(offset, image);
        return img;
    }

}