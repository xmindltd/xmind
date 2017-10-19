package org.xmind.ui.util;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;

public interface IStyleProvider {

    /**
     * The key for the textual content of a widget (value='text').
     */
    public static final String TEXT = "text"; //$NON-NLS-1$

    public static final String TEXT_ALIGN = "textAlign"; //$NON-NLS-1$

    /**
     * The key for the image content of a widget (value='image').
     */
    public static final String IMAGE = "image"; //$NON-NLS-1$

    /**
     * The key for the filler of a widget, which covers the content area and the
     * inner margin (value='fill').
     */
    public static final String FILL = "fill"; //$NON-NLS-1$

    /**
     * The key for the outer margin of a widget, which expands outside the
     * border (value='margin').
     */
    public static final String MARGIN = "margin"; //$NON-NLS-1$

    /**
     * The key for the inner margin of a widget, which expands inside the border
     * (value='padding').
     */
    public static final String PADDING = "padding"; //$NON-NLS-1$

    /**
     * The key for the border line of a widget, which is drawn between the
     * margin and the padding (value='border').
     */
    public static final String BORDER = "border"; //$NON-NLS-1$

    /**
     * The key for the separators of a widget, which lie between each pair of
     * sibling items (value='separator').
     */
    public static final String SEPARATOR = "separator"; //$NON-NLS-1$

    /**
     * The key for the border corner of a widget (value='corner').
     */
    public static final String CORNER = "corner"; //$NON-NLS-1$

    public abstract void setColor(String key, RGB value);

    public abstract Color getColor(Object widget, String key);

    public abstract Font getFont(Object widget, String key);

    public abstract int getAlpha(Object widget, String key, int defaultValue);

    public abstract int getWidth(Object widget, String key, int defaultValue);

    public abstract int getHeight(Object widget, String key, int defaultValue);

    public abstract int getPosition(Object widget, String key,
            int defaultValue);

    public abstract int getTextAlign(Object widget, String key,
            int defaultValue);

    public abstract boolean getVisibility(Object widget, String key,
            boolean defaultValue);

}
