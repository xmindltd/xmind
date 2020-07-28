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
package org.xmind.ui.gallery;

import org.eclipse.draw2d.geometry.Insets;

/**
 * @author Frank Shaka
 */
public class GalleryLayout {

    public static final int ALIGN_CENTER = 0;
    public static final int ALIGN_TOPLEFT = 1;
    public static final int ALIGN_BOTTOMRIGHT = 2;
    public static final int ALIGN_FILL = 3;

    public int majorAlignment;

    public int minorAlignment;

    public int majorSpacing;

    public int minorSpacing;

    public int marginTop;

    public int marginLeft;

    public int marginRight;

    public int marginBottom;

    public GalleryLayout() {
        this(ALIGN_CENTER, ALIGN_CENTER, 10, 5, 10, 10, 10, 10);
    }

    public GalleryLayout(int majorAlign, int minorAlign, int majorSpacing,
            int minorSpacing, Insets margins) {
        this(majorAlign, minorAlign, majorSpacing, minorSpacing, //
                margins == null ? 0 : margins.top, //
                margins == null ? 0 : margins.left, //
                margins == null ? 0 : margins.right, //
                margins == null ? 0 : margins.bottom);
    }

    public GalleryLayout(int majorAlign, int minorAlign, int majorSpacing,
            int minorSpacing, int marginTop, int marginLeft, int marginRight,
            int marginBottom) {
        this.majorAlignment = majorAlign;
        this.minorAlignment = minorAlign;
        this.majorSpacing = majorSpacing;
        this.minorSpacing = minorSpacing;
        this.marginTop = marginTop;
        this.marginLeft = marginLeft;
        this.marginRight = marginRight;
        this.marginBottom = marginBottom;
    }

    public GalleryLayout align(int major, int minor) {
        this.majorAlignment = major;
        this.minorAlignment = minor;
        return this;
    }

    public GalleryLayout spacing(int major, int minor) {
        this.majorSpacing = major;
        this.minorSpacing = minor;
        return this;
    }

    public GalleryLayout margins(Insets margins) {
        if (margins == null) {
            this.marginTop = 0;
            this.marginLeft = 0;
            this.marginRight = 0;
            this.marginBottom = 0;
        } else {
            this.marginTop = margins.top;
            this.marginLeft = margins.left;
            this.marginRight = margins.right;
            this.marginBottom = margins.bottom;
        }
        return this;
    }

    public GalleryLayout margins(int all) {
        return margins(all, all, all, all);
    }

    public GalleryLayout margins(int t, int l, int b, int r) {
        return margins(new Insets(t, l, b, r));
    }

    public GalleryLayout copy() {
        return new GalleryLayout(majorAlignment, minorAlignment, majorSpacing,
                minorSpacing, marginTop, marginLeft, marginRight, marginBottom);
    }

    public Insets getMargins() {
        return new Insets(marginTop, marginLeft, marginBottom, marginRight);
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof GalleryLayout))
            return false;
        GalleryLayout that = (GalleryLayout) obj;
        return that.majorAlignment == this.majorAlignment
                && that.minorAlignment == this.minorAlignment
                && that.majorSpacing == this.majorSpacing
                && that.minorSpacing == this.minorSpacing
                && that.marginTop == this.marginTop
                && that.marginLeft == this.marginLeft
                && that.marginRight == this.marginRight
                && that.marginBottom == this.marginBottom;

    }
}