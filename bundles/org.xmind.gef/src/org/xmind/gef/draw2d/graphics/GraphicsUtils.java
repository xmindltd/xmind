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
package org.xmind.gef.draw2d.graphics;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Brian Sun
 */
public class GraphicsUtils {

    private static Boolean carbon = null;

    private static boolean isCarbon() {
        if (carbon == null) {
            carbon = Boolean.valueOf(Util.isCarbon());
        }
        return carbon.booleanValue();
    }

    private static Boolean carbonSnowLeopard = null;

    public static boolean isCarbonSnowLeopard() {
        if (carbonSnowLeopard == null) {
            if (isCarbon()) {
                String osVersion = System.getProperty("os.version"); //$NON-NLS-1$
                if (osVersion != null) {
                    String[] parts = osVersion.split("\\."); //$NON-NLS-1$
                    if (isGreater(parts[0], 10)) {
                        if (parts.length > 1) {
                            carbonSnowLeopard = Boolean.valueOf(isGreater(
                                    parts[1], 6));
                        }
                    }
                }
            }
            if (carbonSnowLeopard == null)
                carbonSnowLeopard = Boolean.FALSE;
        }
        return carbonSnowLeopard.booleanValue();
    }

    private static boolean isGreater(String str, int value) {
        try {
            return Integer.parseInt(str) >= value;
        } catch (NumberFormatException e) {
        }
        return false;
    }

    public static void fixGradientBugForCarbon(Graphics graphics, IFigure figure) {
        fixGradientBugForCarbon(graphics, figure.getBounds());
    }

    public static void fixGradientBugForCarbon(Graphics graphics,
            Rectangle bounds) {
        if (isCarbon()) {
            graphics.pushState();
            graphics.setAlpha(0);
            graphics.setBackgroundColor(ColorConstants.white);
            graphics.fillRectangle(bounds);
            graphics.restoreState();
            graphics.popState();
        }
    }

    private static final GraphicsUtils normal = new GraphicsUtils(false);
    private static final GraphicsUtils advanced = new GraphicsUtils(true);

    public static GraphicsUtils getNormal() {
        return normal;
    }

    public static GraphicsUtils getAdvanced() {
        return advanced;
    }

    private GC gc = null;
    private Font appliedFont = null;
    private FontMetrics metrics = null;

    protected GraphicsUtils(boolean advanced) {
        getGC().setAdvanced(advanced);
    }

    public GC getGC() {
        if (gc == null) {
            gc = new GC(new Shell());
        }
        return gc;
    }

    protected void setFont(Font f) {
        if (appliedFont == f || f.equals(appliedFont))
            return;
        getGC().setFont(f);
        appliedFont = f;
        metrics = null;
    }

    /**
     * @return the appliedFont
     */
    public Font getAppliedFont() {
        return appliedFont;
    }

    public Dimension getTextSize(String text, Font font) {
        setFont(font);
        return getTextSize(text);
    }

    public Dimension getStringSize(String string, Font font) {
        setFont(font);
        return getStringSize(string);
    }

    /**
     * @param text
     * @return
     */
    public Dimension getTextSize(String text) {
        return new Dimension(getGC().textExtent(text));
    }

    /**
     * @param string
     * @return
     */
    public Dimension getStringSize(String string) {
        return new Dimension(getGC().stringExtent(string));
    }

    /**
     * Returns the FontMetrics associated with the passed Font.
     * 
     * @param f
     *            the font
     * @return the FontMetrics for the given font
     * @see GC#getFontMetrics()
     */
    public FontMetrics getFontMetrics(Font f) {
        setFont(f);
        if (metrics == null)
            metrics = getGC().getFontMetrics();
        return metrics;
    }

    public static final int CENTER = 0;

    public static final int TRAIL = 1;

    public static final int LEAD = 2;

    public static final int PATH = 3;

    public String constrain(String path, int maxWidth, Font font,
            int startPositionHint) {
        Dimension size = getTextSize(path, font);
        if (size.width > maxWidth) {
            StringBuffer sb = new StringBuffer(path);
            int right;
            int left;
            int start;

            switch (startPositionHint) {
            case PATH:
                left = 1;
                right = sb.lastIndexOf(".") - 2; //$NON-NLS-1$
                start = right * 5 / 6;
                break;
            case LEAD:
                left = 0;
                right = sb.length() - 2;
                start = 0;
                break;
            case TRAIL:
                left = 1;
                right = sb.length() - 1;
                start = Math.max(right, left);
                break;
            default:
                left = 1;
                right = sb.length() - 2;
                start = right / 2;
            }
            sb.replace(start, start + 1, "..."); //$NON-NLS-1$
            size = getTextSize(sb.toString());
            boolean down = true;
            while (size.width > maxWidth && start <= right && start > left
                    && right > left) {
                if ((down || (right >= 0 && start + 4 > right)) && start > left) {
                    start--;
                }
                down = !down;
                sb.replace(start, start + 4, "..."); //$NON-NLS-1$
                if (right >= 0)
                    right--;
                size = getTextSize(sb.toString());
            }
            path = sb.toString();
        }
        return path;
    }

}