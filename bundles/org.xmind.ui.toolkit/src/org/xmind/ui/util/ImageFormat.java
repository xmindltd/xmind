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
package org.xmind.ui.util;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;

public enum ImageFormat {
    BMP("BMP", "image/bmp", //$NON-NLS-1$ //$NON-NLS-2$ 
            Messages.BMPFile, SWT.IMAGE_BMP, ".bmp"), //$NON-NLS-1$
    JPEG("JPEG", "image/jpeg", //$NON-NLS-1$ //$NON-NLS-2$
            Messages.JPEGFile, SWT.IMAGE_JPEG, ".jpg", ".jpeg"), //$NON-NLS-1$ //$NON-NLS-2$
    GIF("GIF", "image/gif", //$NON-NLS-1$ //$NON-NLS-2$ 
            Messages.GIFFile, SWT.IMAGE_GIF, ".gif"), //$NON-NLS-1$
    PNG("PNG", "image/png", //$NON-NLS-1$ //$NON-NLS-2$ 
            Messages.PNGFile, SWT.IMAGE_PNG, ".png"); //$NON-NLS-1$

    private String name;

    private String mediaType;

    private String description;

    private int swtFormat;

    private List<String> fileExtensions;

    private ImageFormat(String name, String mediaType, String description,
            int swtType, String... fileExtensions) {
        this.name = name;
        this.mediaType = mediaType;
        this.description = description;
        this.swtFormat = swtType;
        this.fileExtensions = Arrays.asList(fileExtensions);
    }

    public String getName() {
        return name;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getDescription() {
        return description;
    }

    public int getSWTFormat() {
        return swtFormat;
    }

    public List<String> getExtensions() {
        return fileExtensions;
    }

    public static ImageFormat findByExtension(String ext,
            ImageFormat defaultFormat) {
        for (ImageFormat format : values()) {
            if (format.fileExtensions.contains(ext.toLowerCase()))
                return format;
        }
        return defaultFormat;
    }

    public static ImageFormat findByMediaType(String mediaType,
            ImageFormat defaultFormat) {
        for (ImageFormat format : values()) {
            if (format.mediaType.equals(mediaType))
                return format;
        }
        return defaultFormat;
    }

    public static ImageFormat findBySWTFormat(int swtFormat,
            ImageFormat defaultFormat) {
        for (ImageFormat format : values()) {
            if (format.swtFormat == swtFormat)
                return format;
        }
        return defaultFormat;
    }
}