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
package org.xmind.ui.resources;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.resource.ColorDescriptor;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 * 
 * @author Frank Shaka
 */
public class ColorUtils {

    private ColorUtils() {
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Color getColor(String key, RGB rgb) {
        if (key == null) {
            key = toString(rgb);
            if (key == null)
                return null;
        }
        ColorRegistry reg = JFaceResources.getColorRegistry();
        if (!reg.hasValueFor(key)) {
            if (rgb == null) {
                rgb = toRGB(key);
                if (rgb == null)
                    return null;
            }
            reg.put(key, rgb);
        }
        return reg.get(key);
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Color getColor(RGB rgb) {
        return getColor(null, rgb);
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Color getColor(String s) {
        return getColor(s, null);
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Color getColor(int r, int g, int b) {
        return getColor(new RGB(r, g, b));
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Color getRelative(RGB source, int dr, int dg, int db) {
        return getColor(
                new RGB(source.red + dr, source.green + dg, source.blue + db));
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Color getRelative(Color c, int dr, int dg, int db) {
        return getRelative(c.getRGB(), dr, dg, db);
    }

    public static ColorDescriptor toDescriptor(String s) {
        return ColorDescriptor.createFrom(toRGB(s));
    }

    private static final Pattern PATTERN_RGBA = Pattern.compile(
            "rgb\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*(,\\s*(\\d+)\\s*)?\\)"); //$NON-NLS-1$

    public static RGB toRGB(String s) {
        if (s == null)
            return null;

        if (s.startsWith("#")) { //$NON-NLS-1$
            int n;
            try {
                n = Integer.parseInt(s.substring(1), 16);
            } catch (NumberFormatException e) {
                return null;
            }

            if (s.length() == 4) {
                return splitShortRGB(n);
            } else if (s.length() == 7) {
                return splitRGB(n);
            }
            return null;
        }

        Matcher m = PATTERN_RGBA.matcher(s);
        if (m.matches()) {
            int r = Integer.parseInt(m.group(1), 10);
            int g = Integer.parseInt(m.group(2), 10);
            int b = Integer.parseInt(m.group(3), 10);
            // currently alpha value is not honored by RGB
            return new RGB(r, g, b);
        }

        return null;
    }

    public static String toString(RGB rgb) {
        if (rgb == null)
            return null;

        return String.format("#%06X", merge(rgb)); //$NON-NLS-1$
    }

    public static String toString(Color color) {
        return color == null ? null : toString(color.getRGB());
    }

    public static String toString(int r, int g, int b) {
        return String.format("#%06X", merge(r, g, b)); //$NON-NLS-1$
    }

    public static int merge(int r, int g, int b) {
        return ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
    }

    public static int merge(RGB rgb) {
        return merge(rgb.red, rgb.green, rgb.blue);
    }

    public static int merge(Color color) {
        return merge(color.getRGB());
    }

    public static RGB split(int rgb) {
        return splitRGB(rgb);
    }

    private static RGB splitRGB(int rgb) {
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;
        return new RGB(r, g, b);
    }

    private static RGB splitShortRGB(int rgb) {
        int r = ((rgb >> 8) & 0xf) * 17;
        int g = ((rgb >> 4) & 0xf) * 17;
        int b = (rgb & 0xf) * 17;
        return new RGB(r, g, b);
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Color darker(Color c) {
        return getColor(darker(c.getRGB()));
    }

    public static RGB darker(RGB rgb) {
        return new RGB(rgb.red * 2 / 3, rgb.green * 2 / 3, rgb.blue * 2 / 3);
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Color lighter(Color c) {
        return getColor(lighter(c.getRGB()));
    }

    public static RGB lighter(RGB rgb) {
        return new RGB(rgb.red + (255 - rgb.red) / 2,
                rgb.green + (255 - rgb.green) / 2,
                rgb.blue + (255 - rgb.blue) / 2);
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Color gradientLighter(Color c) {
        return getColor(gradientLighter(c.getRGB()));
    }

    public static RGB gradientLighter(RGB rgb) {
        return new RGB(rgb.red + (255 - rgb.red) * 5 / 7,
                rgb.green + (255 - rgb.green) * 5 / 7,
                rgb.blue + (255 - rgb.blue) * 5 / 7);
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Color gray(Color c) {
        return getColor(gray(c.getRGB()));
    }

    public static RGB gray(RGB rgb) {
        int l = lightness(rgb);
        return new RGB(l, l, l);
    }

//    public static Color foreground(Color back) {
//        return (back.getRed() < 128 && back.getGreen() < 128 && back.getBlue() < 128) ? ColorConstants.white
//                : ColorConstants.black;
//    }

    private static final int min = 0x60;
    private static final int max = 0xac;
    private static final int t = max - min;

    private static final RGB[] rainbowColors = new RGB[] {
            new RGB(0xac, 0x60, 0x60), new RGB(0xac, 0xac, 0x60),
            new RGB(0x60, 0xac, 0x60), new RGB(0x60, 0xac, 0xac),
            new RGB(0x60, 0x60, 0xac), new RGB(0xac, 0x60, 0xac) };

    public static RGB getRainbow(int index, int total) {
        return rainbowColors[index % 6];
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Color getRainbowColor(int index, int total) {
        return getColor(getRainbow(index, total));
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Color getRainbowColor2(int index, int total) {
        total = Math.abs(total);
        if (index < 0)
            index = index % total + total;
        else if (index >= total)
            index = index % total;
        double step = t * 6.0 / total;
        int f = (int) (step * index);
        if (f >= 0 && f < t)
            return getColor(max, min + f, min);
        if (f >= t && f < t * 2)
            return getColor(max + t - f, max, min);
        if (f >= t * 2 && f < t * 3)
            return getColor(min, max, min + f - t * 2);
        if (f >= t * 3 && f < t * 4)
            return getColor(min, max + t * 3 - f, max);
        if (f >= t * 4 && f < t * 5)
            return getColor(min + f - t * 4, min, max);
        return getColor(max, min, max + t * 5 - f);
    }

    public static int lightness(Color c) {
        return lightness(c.getRed(), c.getGreen(), c.getBlue());
    }

    public static int lightness(RGB rgb) {
        return lightness(rgb.red, rgb.green, rgb.blue);
    }

    /**
     * Lightness = 0.3R + 0.59G + 0.11B
     * 
     * @param r
     * @param g
     * @param b
     * @return
     */
    public static int lightness(int r, int g, int b) {
        return (int) (r * 0.3 + g * 0.59 + b * 0.11);
    }

}