package org.xmind.cathy.internal.dashboard;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ColorDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.tabfolder.MTabFolder;
import org.xmind.ui.tabfolder.MTabItem;
import org.xmind.ui.util.StyleProvider;

public class DashboardStyleProvider extends StyleProvider {

    private ResourceManager resourceManager;

    private Map<String, RGB> keyToRGB = new HashMap<String, RGB>();

    public DashboardStyleProvider(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    @Override
    public Color getColor(Object widget, String key) {
        String defaultValue = null;
        if (widget instanceof MTabItem) {
            MTabItem item = (MTabItem) widget;
            if (FILL.equals(key)
                    && (item.isSelected() || item.isPreselected())) {
                defaultValue = "#FFFFFF"; //$NON-NLS-1$
            } else if (TEXT.equals(key)) {
                defaultValue = "#FFFFFF"; //$NON-NLS-1$
            }
        } else if (widget instanceof MTabFolder) {
            if (MTabFolder.TAB_BAR.equals(key)) {
                defaultValue = "#535f5e"; //$NON-NLS-1$
            }
            if (MTabFolder.BODY.equals(key)) {
                defaultValue = "#F2F2F2"; //$NON-NLS-1$
            }
        }
        if (defaultValue != null) {
            RGB rgb = ColorUtils.toRGB(defaultValue);
            if (keyToRGB.containsKey(key))
                rgb = keyToRGB.get(key);
            return (Color) resourceManager.get(ColorDescriptor.createFrom(rgb));
        }
        return super.getColor(widget, key);
    }

    @Override
    public int getAlpha(Object widget, String key, int defaultValue) {
        if (widget instanceof MTabItem) {
            MTabItem item = (MTabItem) widget;
            if (FILL.equals(key)) {
                if (item.isSelected())
                    return 0x26;
                if (item.isPreselected())
                    return 0x13;
            }
        }
        return super.getAlpha(widget, key, defaultValue);
    }

    @Override
    public Font getFont(Object widget, String key) {
        if (widget instanceof MTabItem) {
            if (TEXT.equals(key))
                return (Font) resourceManager.get(JFaceResources
                        .getDefaultFontDescriptor().setHeight(12));
        }
        return super.getFont(widget, key);
    }

    @Override
    public int getWidth(Object widget, String key, int defaultValue) {
        if (widget instanceof MTabItem) {
            if (IMAGE.equals(key))
                return 56;
            if (MARGIN.equals(key))
                return 3;
            if (SEPARATOR.equals(key))
                return 2;
            if (key == null)
                return 72;
        }
        return super.getWidth(widget, key, defaultValue);
    }

    @Override
    public int getHeight(Object widget, String key, int defaultValue) {
        if (widget instanceof MTabItem) {
            if (IMAGE.equals(key))
                return 45;
            if (MARGIN.equals(key))
                return 6;
            if (SEPARATOR.equals(key))
                return 2;
            if (key == null)
                return 74;
        }
        return super.getHeight(widget, key, defaultValue);
    }

    @Override
    public int getPosition(Object widget, String key, int defaultValue) {
        if (widget instanceof MTabItem) {
            if (TEXT.equals(key))
                return SWT.BOTTOM;
        } else if (widget instanceof MTabFolder) {
            if (MTabFolder.TAB_BAR.equals(key))
                return SWT.LEFT;
        }
        return super.getPosition(widget, key, defaultValue);
    }

    public void setColor(String key, RGB value) {
        keyToRGB.put(key, value);
    }
}