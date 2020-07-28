/*
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and above are dual-licensed
 * under the Eclipse Public License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors: XMind Ltd. - initial API and implementation
 */
package org.xmind.ui.internal.browser;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class BrowserImages {

    public static final String PATH_ICONS = "icons/"; //$NON-NLS-1$

    public static final String PATH_ENABLED = PATH_ICONS + "e/"; //$NON-NLS-1$

    public static final String PATH_DISABLED = PATH_ICONS + "d/"; //$NON-NLS-1$

    public static final String XMIND = PATH_ICONS + "xmind.16.png"; //$NON-NLS-1$

    public static final String BROWSER = PATH_ICONS + "browser.gif"; //$NON-NLS-1$

    public static final String BACKWARD = "backward_nav.gif"; //$NON-NLS-1$

    public static final String FORWARD = "forward_nav.gif"; //$NON-NLS-1$

    public static final String REFRESH = "refresh_nav.gif"; //$NON-NLS-1$

    public static final String STOP = "nav_stop.gif"; //$NON-NLS-1$

    public static final String GO = "nav_go.gif"; //$NON-NLS-1$

    private static Map<String, ImageDescriptor> cache = new HashMap<String, ImageDescriptor>();

    private static ImageDescriptor[] busyImages = null;

    public static ImageDescriptor getImageDescriptor(String path) {
        ImageDescriptor img = cache.get(path);
        if (img == null) {
            img = BrowserPlugin
                    .imageDescriptorFromPlugin(BrowserPlugin.PLUGIN_ID, path);
            if (img != null)
                cache.put(path, img);
        }
        return img;
    }

    public static ImageDescriptor getImageDescriptor(String fileName,
            boolean enabled) {
        String path = (enabled ? PATH_ENABLED : PATH_DISABLED) + fileName;
        return getImageDescriptor(path);
    }

    public static Image getImage(String path) {
        ImageRegistry reg = BrowserPlugin.getDefault().getImageRegistry();
        Image image = reg.get(path);
        if (image == null) {
            reg.put(path, getImageDescriptor(path));
            image = reg.get(path);
        }
        return image;
    }

    public static Image getImage(String fileName, boolean enabled) {
        String path = (enabled ? PATH_ENABLED : PATH_DISABLED) + fileName;
        return getImage(path);
    }

    public static ImageDescriptor[] getBusyImages() {
        if (busyImages == null) {
            busyImages = new ImageDescriptor[12];
            for (int i = 0; i < 12; i++) {
                String path = String.format(PATH_ICONS + "busy/busy_f%02d.gif", //$NON-NLS-1$
                        (i + 1));
                busyImages[i] = getImageDescriptor(path);
            }
        }
        return busyImages;
    }

}
