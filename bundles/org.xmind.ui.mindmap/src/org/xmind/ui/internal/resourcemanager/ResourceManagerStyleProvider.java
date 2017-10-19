package org.xmind.ui.internal.resourcemanager;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ColorDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.tabfolder.MTabFolder;
import org.xmind.ui.tabfolder.MTabItem;
import org.xmind.ui.util.IStyleProvider;

public class ResourceManagerStyleProvider implements IStyleProvider {

    private ResourceManager resourceManager;

    private Map<String, RGB> keyToRGB = new HashMap<String, RGB>();

    public ResourceManagerStyleProvider(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public Color getColor(Object widget, String key) {
        String defaultValue = null;
        if (widget instanceof MTabItem) {
            MTabItem item = (MTabItem) widget;
            if (FILL.equals(key)
                    && (item.isSelected() || item.isPreselected())) {
                defaultValue = "#008EFC"; //$NON-NLS-1$
            } else if (TEXT.equals(key)) {
                if (item.isSelected()) {
                    defaultValue = "#FFFFFF"; //$NON-NLS-1$
                } else {
                    defaultValue = "#000000"; //$NON-NLS-1$
                }
            }
        } else if (widget instanceof MTabFolder) {
            if (MTabFolder.TAB_BAR.equals(key)) {
                defaultValue = "#FFFFFF"; //$NON-NLS-1$
            }
            if (MTabFolder.BODY.equals(key)) {
                defaultValue = "#F2F2F2"; //$NON-NLS-1$
            }
        }
        if (defaultValue != null) {
            RGB rgb = ColorUtils.toRGB(defaultValue);
            return (Color) resourceManager.get(ColorDescriptor.createFrom(rgb));
        }
        return null;
    }

    public int getAlpha(Object widget, String key, int defaultValue) {
        if (widget instanceof MTabItem) {
            MTabItem item = (MTabItem) widget;
            if (FILL.equals(key)) {
                if (item.isSelected())
                    return 0xFF;
                if (item.isPreselected())
                    return 0x13;
            }
        }
        return defaultValue;
    }

    public Font getFont(Object widget, String key) {
        int fontHeight;
        if (Util.isMac()) {
            fontHeight = 12;//font size
        } else {
            fontHeight = 8;//font pixels
        }
        if (widget instanceof MTabItem) {
            if (TEXT.equals(key))
                return (Font) resourceManager.get(JFaceResources
                        .getDefaultFontDescriptor().setHeight(fontHeight));
        }
        return null;
    }

    public int getWidth(Object widget, String key, int defaultValue) {
        if (widget instanceof MTabItem) {
            if (IMAGE.equals(key))
                return 26;
            if (MARGIN.equals(key))
                return 17;
            if (SEPARATOR.equals(key))
                return 2;//TODO:system-based
            if (key == null)
                return 131;
        }
        return defaultValue;
    }

    public int getHeight(Object widget, String key, int defaultValue) {
        if (widget instanceof MTabItem) {
            if (IMAGE.equals(key))
                return 26;
            if (MARGIN.equals(key))
                return 10;
            if (SEPARATOR.equals(key))
                return 2;
            if (key == null)
                return 46;
        }
        return defaultValue;
    }

    public int getPosition(Object widget, String key, int defaultValue) {
        if (widget instanceof MTabItem && TEXT.equals(key)) {
            return SWT.RIGHT;
        } else if (widget instanceof MTabFolder
                && MTabFolder.TAB_BAR.equals(key)) {
            return SWT.LEFT;
        }
        return defaultValue;
    }

    @Override
    public int getTextAlign(Object widget, String key, int defaultValue) {
        if (widget instanceof MTabItem && TEXT_ALIGN.equals(key)) {
            return SWT.LEFT;
        }

        return defaultValue;
    }

    public void setColor(String key, RGB value) {
        keyToRGB.put(key, value);
    }

    public boolean getVisibility(Object widget, String key,
            boolean defaultValue) {
        return defaultValue;
    }

}
