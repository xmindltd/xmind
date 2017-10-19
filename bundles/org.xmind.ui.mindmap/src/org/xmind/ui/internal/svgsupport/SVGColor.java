package org.xmind.ui.internal.svgsupport;

import org.eclipse.swt.graphics.RGB;

/**
 * 
 * @author Enki Xiong
 *
 */
class SVGColor {
    private LinearGradient linearGradient;
    private RGB rgb;

    public SVGColor(int rgb) {
        setRGB(rgb);
    }

    public SVGColor(LinearGradient linearGradient) {
        setLinearGradient(linearGradient);
    }

    private void setRGB(int rgb) {
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        this.rgb = new RGB(red, green, blue);
    }

    RGB getRGB() {
        return rgb;
    }

    LinearGradient getLinearGradient() {
        return linearGradient;
    }

    private void setLinearGradient(LinearGradient linearGradient) {
        this.linearGradient = linearGradient;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;

        if (obj instanceof SVGColor) {
            SVGColor color = (SVGColor) obj;
            if (rgb != null)
                return rgb.equals(color.rgb);
            else
                return linearGradient.equals(color.linearGradient);
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        if (rgb != null)
            result = result * 31 + rgb.hashCode();
        else if (linearGradient != null)
            result = result * 31 + linearGradient.hashCode();

        return result;
    }

    @Override
    public String toString() {
        if (rgb != null)
            return rgb.toString();
        else if (linearGradient != null)
            return linearGradient.toString();
        return SVGDefinitionConstants.NONE;
    }

}
