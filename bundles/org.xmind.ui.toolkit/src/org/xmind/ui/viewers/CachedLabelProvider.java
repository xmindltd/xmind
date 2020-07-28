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
/**
 * 
 */
package org.xmind.ui.viewers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ColorDescriptor;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.widgets.Display;

/**
 * @author Frank Shaka
 */
public class CachedLabelProvider extends LabelProvider implements
        IColorProvider, IFontProvider {

    private ILabelDescriptor labelDescriptor;

    private Map<Object, Image> images = new HashMap<Object, Image>();

    private Map<Object, Color> foregroundColors = new HashMap<Object, Color>();

    private Map<Object, Color> backgroundColors = new HashMap<Object, Color>();

    private Map<Object, Font> fonts = new HashMap<Object, Font>();

    /**
     * 
     */
    public CachedLabelProvider(ILabelDescriptor labelDescriptor) {
        this.labelDescriptor = labelDescriptor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
     */
    @Override
    public String getText(Object element) {
        if (labelDescriptor == null)
            return getDefaultText(element);
        return labelDescriptor.getText(element);
    }

    protected String getDefaultText(Object element) {
        return super.getText(element);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
     */
    @Override
    public Image getImage(Object element) {
        if (labelDescriptor == null)
            return getDefaultImage(element);
        Image image = images.get(element);
        if (image == null || image.isDisposed()) {
            ImageDescriptor imageDescriptor = labelDescriptor.getImage(element);
            image = imageDescriptor == null ? null : imageDescriptor
                    .createImage(false, Display.getCurrent());
            images.put(element, image);
        }
        return image;
    }

    protected Image getDefaultImage(Object element) {
        return super.getImage(element);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
     */
    public Font getFont(Object element) {
        if (labelDescriptor == null)
            return getDefaultFont(element);
        Font font = fonts.get(element);
        if (font == null || font.isDisposed()) {
            FontDescriptor fontDescriptor = labelDescriptor.getFont(element);
            font = fontDescriptor == null ? null : fontDescriptor
                    .createFont(Display.getCurrent());
            fonts.put(element, font);
        }
        return font;
    }

    protected Font getDefaultFont(Object element) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
     */
    public Color getForeground(Object element) {
        if (labelDescriptor == null)
            return getDefaultForeground(element);
        Color color = foregroundColors.get(element);
        if (color == null || color.isDisposed()) {
            ColorDescriptor colorDescriptor = labelDescriptor
                    .getForeground(element);
            color = colorDescriptor == null ? null : colorDescriptor
                    .createColor(Display.getCurrent());
            foregroundColors.put(element, color);
        }
        return color;
    }

    protected Color getDefaultForeground(Object element) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
     */
    public Color getBackground(Object element) {
        if (labelDescriptor == null)
            return getDefaultBackground(element);
        Color color = backgroundColors.get(element);
        if (color == null || color.isDisposed()) {
            ColorDescriptor colorDescriptor = labelDescriptor
                    .getBackground(element);
            color = colorDescriptor == null ? null : colorDescriptor
                    .createColor(Display.getCurrent());
            backgroundColors.put(element, color);
        }
        return color;
    }

    protected Color getDefaultBackground(Object element) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
     */
    @Override
    public void dispose() {
        Object[] cachedImages = images.values().toArray();
        Object[] cachedForegroundColors = foregroundColors.values().toArray();
        Object[] cachedBackgroundColors = backgroundColors.values().toArray();
        Object[] cachedFonts = fonts.values().toArray();
        images.clear();
        foregroundColors.clear();
        backgroundColors.clear();
        fonts.clear();
        disposeResources(cachedImages);
        disposeResources(cachedForegroundColors);
        disposeResources(cachedBackgroundColors);
        disposeResources(cachedFonts);
        super.dispose();
    }

    private static void disposeResources(Object[] resources) {
        for (int i = 0; i < resources.length; i++) {
            if (resources[i] != null)
                ((Resource) resources[i]).dispose();
        }
    }

}
