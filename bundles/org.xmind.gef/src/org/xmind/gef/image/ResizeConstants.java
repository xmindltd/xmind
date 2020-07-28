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

public class ResizeConstants {

    /**
     * Resize Strategy: <b>Original Size</b><br>
     * No resizing should be performed and the image remains its original size.
     */
    public static final int RESIZE_NONE = 0;

    /**
     * Resize Strategy: <b>Stretch</b><br>
     * The image is to be stretched horizontally <b>and</b> vertically to cover
     * the whole area.
     */
    public static final int RESIZE_STRETCH = 1;

    /**
     * Resize Strategy: <b>Fit</b><br>
     * The image is to be scaled to fit in the area, which means to get its
     * width or height equal to or shorter than the area's.
     */
    public static final int RESIZE_FIT = 2;

    /**
     * Resize Strategy: <b>Constrain</b><br>
     * If the width or height of the image is longer than the area's, the image
     * is to be fitted in that area, otherwise, it will remain its orginal size;
     */
    public static final int RESIZE_CONSTRAIN = 3;

    /**
     * Resize Strategy: <b>Max Pixels</b><br>
     * If the total pixels of the image is more than the area's, the image is to
     * be fitted into the area's total pixels, keeping the image's width/height
     * ratio but disregarding the area's.
     */
    public static final int RESIZE_MAXPIXELS = 4;

}