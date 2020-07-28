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
package org.xmind.ui.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class ToolkitImages {

    public static final String PATH_ICONS = "icons/"; //$NON-NLS-1$

    public static final String PATH_ENABLED = PATH_ICONS + "e/"; //$NON-NLS-1$

    public static final String PATH_DISABLED = PATH_ICONS + "d/"; //$NON-NLS-1$

    public static final String ALIGN_CENTER = "align_center.png"; //$NON-NLS-1$

    public static final String ALIGN_LEFT = "align_left.png"; //$NON-NLS-1$

    public static final String ALIGN_RIGHT = "align_right.png"; //$NON-NLS-1$

    public static final String BACKGROUND = "background.png"; //$NON-NLS-1$

    public static final String BOLD = "bold.png"; //$NON-NLS-1$

    public static final String FONT = "font.gif"; //$NON-NLS-1$

    public static final String FOREGROUND = "foreground.png"; //$NON-NLS-1$

    public static final String INDENT = "indent.png"; //$NON-NLS-1$

    public static final String ITALIC = "italic.png"; //$NON-NLS-1$

    public static final String OUTDENT = "outdent.png"; //$NON-NLS-1$

    public static final String STRIKEOUT = "strikeout.png"; //$NON-NLS-1$

    public static final String UNDERLINE = "underline.png"; //$NON-NLS-1$

    public static final String SLIDER_HANDLE = "slider_handle.png"; //$NON-NLS-1$

    public static final String ZOOM_IN = "zoomin.gif"; //$NON-NLS-1$

    public static final String ZOOM_OUT = "zoomout.gif"; //$NON-NLS-1$

    public static final String BULLET = "bullet.gif"; //$NON-NLS-1$

    public static final String NUMBER = "number.gif"; //$NON-NLS-1$

    private static Map<String, ImageDescriptor> cache = new HashMap<String, ImageDescriptor>();

    public static ImageDescriptor getImageDescriptor(String path) {
        ImageDescriptor img = cache.get(path);
        if (img == null) {
            img = ToolkitPlugin
                    .imageDescriptorFromPlugin(ToolkitPlugin.PLUGIN_ID, path);
            if (img != null)
                cache.put(path, img);
        }
        return img;
    }

    public static ImageDescriptor get(String iconName) {
        return getImageDescriptor(PATH_ICONS + iconName);
    }

    public static ImageDescriptor get(String iconName, boolean enabled) {
        return getImageDescriptor(
                (enabled ? PATH_ENABLED : PATH_DISABLED) + iconName);
    }

//    public static ImageDescriptor getImageDescriptor(String fileName,
//            boolean enabled) {
//        String path = (enabled ? PATH_ENABLED : PATH_DISABLED) + fileName;
//        return getImageDescriptor(path);
//    }

    public static Image getImage(String path) {
        ImageRegistry reg = ToolkitPlugin.getDefault().getImageRegistry();
        Image image = reg.get(path);
        if (image == null) {
            reg.put(path, get(path));
            image = reg.get(path);
        }
        return image;
    }

//    public static Image getImage(String fileName, boolean enabled) {
//        String path = (enabled ? PATH_ENABLED : PATH_DISABLED) + fileName;
//        return getImage(path);
//    }

}
