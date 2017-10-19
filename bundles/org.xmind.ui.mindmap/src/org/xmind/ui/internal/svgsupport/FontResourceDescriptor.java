package org.xmind.ui.internal.svgsupport;

import org.eclipse.jface.resource.DeviceResourceDescriptor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;

/**
 * 
 * @author Enki Xiong
 *
 */
public class FontResourceDescriptor extends DeviceResourceDescriptor {
    private String fontFamily;
    private int fontSize = 0;
    private int style = 0;

    public FontResourceDescriptor(String fontFamily, int fontSize, int style) {
        this.fontFamily = fontFamily;
        this.fontSize = fontSize;
        this.style = style;
    }

    @Override
    public Font createResource(Device device) {
        return new Font(device, fontFamily, fontSize, style);
    }

    @Override
    public void destroyResource(Object obj) {
        if (obj instanceof Font) {
            Font font = (Font) obj;
            if (!font.isDisposed())
                font.dispose();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FontResourceDescriptor) {
            FontResourceDescriptor desc = (FontResourceDescriptor) obj;
            if (this == desc)
                return true;
            if ((fontFamily == desc.fontFamily
                    || fontFamily.equals(desc.fontFamily))
                    && fontSize == desc.fontSize && style == desc.style)
                return true;

        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + fontSize;
        result = 31 * result + style;
        result = 31 * result + fontFamily.hashCode();
        return result;
    }

    @Override
    public String toString() {
        String str = "Font( "; //$NON-NLS-1$
        str += "font family:" + fontFamily; //$NON-NLS-1$
        str += ", font size:" + fontSize + ")"; //$NON-NLS-1$ //$NON-NLS-2$

        return str;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public int getFontSize() {
        return fontSize;
    }

    public int getStyle() {
        return style;
    }

}
