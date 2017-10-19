package org.xmind.ui.util;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;

public class StyleProvider implements IStyleProvider {

    public Color getColor(Object widget, String key) {
        return null;
    }

    public Font getFont(Object widget, String key) {
        return null;
    }

    public int getAlpha(Object widget, String key, int defaultValue) {
        return defaultValue;
    }

    public int getWidth(Object widget, String key, int defaultValue) {
        return defaultValue;
    }

    public int getHeight(Object widget, String key, int defaultValue) {
        return defaultValue;
    }

    public int getPosition(Object widget, String key, int defaultValue) {
        return defaultValue;
    }

    public boolean getVisibility(Object widget, String key,
            boolean defaultValue) {
        return defaultValue;
    }

    public void setColor(String key, RGB value) {
    }

    public int getTextAlign(Object widget, String key, int defaultValue) {
        return defaultValue;
    }

}
