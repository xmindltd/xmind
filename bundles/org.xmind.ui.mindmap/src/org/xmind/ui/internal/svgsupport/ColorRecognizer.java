package org.xmind.ui.internal.svgsupport;

import java.util.Map;

/**
 * 
 * @author Enki Xiong
 *
 */
public class ColorRecognizer {

    public static final String PROPERTIES_RESOURCE_URL = "platform:/plugin/org.xmind.ui.mindmap/org/xmind/ui/internal/svgsupport/ColorName2HexRGBValue.properties"; //$NON-NLS-1$

//    private static final Properties COLOR_NAME2RGB = new Properties();

    public static final int INVALID_RGB_VALUE = -1;

//    static {
//        URL url;
//        try {
//            url = new URL(PROPERTIES_RESOURCE_URL);
//            COLOR_NAME2RGB.load(url.openStream());
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }

    static SVGColor recognizeColor(String colorStr,
            final Map<String, SVGDefinition> id2svgDefinitions) {
        if (colorStr == null || "".equals(colorStr)) //$NON-NLS-1$
            return null;
        colorStr = colorStr.trim();
        SVGColor color = null;
        if (colorStr.startsWith(SVGDefinitionConstants.COLOR_HEX_BEGIN)) {
            color = getColorWithHexValue(colorStr);
        } else if (colorStr.startsWith(SVGDefinitionConstants.URL)) {
            color = getLinearGradient(colorStr, id2svgDefinitions);
        } else
            if (colorStr.startsWith(SVGDefinitionConstants.COLOR_RGB_BEGIN)) {
            color = getColorWithRGBStrring(colorStr);
        } else {
            color = getColorWithName(colorStr);
        }
        return color;
    }

    private static SVGColor getColorWithHexValue(String colorStr) {
        String hexRGBStr = colorStr
                .split(SVGDefinitionConstants.COLOR_HEX_BEGIN)[1].trim();
        int rgb = Integer.parseInt(hexRGBStr, 16);
        return new SVGColor(rgb);
    }

    private static SVGColor getLinearGradient(String colorStr,
            final Map<String, SVGDefinition> id2svgDefinitions) {
        if (id2svgDefinitions != null) {
            String key = colorStr.split(SVGDefinitionConstants.REF_BEGIN)[1]
                    .split(SVGDefinitionConstants.RIGHT_BRACKET_REGEX)[0];
            SVGDefinition def = id2svgDefinitions.get(key.trim());
            if (def instanceof LinearGradient) {
                return new SVGColor((LinearGradient) def);
            }
        }
        return null;
    }

    private static SVGColor getColorWithRGBStrring(String colorStr) {
        String[] strs = colorStr
                .split(SVGDefinitionConstants.COLOR_RGB_BEGIN_REGEX)[1]
                        .split(SVGDefinitionConstants.RIGHT_BRACKET_REGEX)[0]
                                .split(","); //$NON-NLS-1$
        int r = Integer.parseInt(strs[0].trim());
        r = r << 16 & 0xff0000;
        int g = Integer.parseInt(strs[1].trim());
        g = g << 8 & 0xff00;
        int b = Integer.parseInt(strs[2].trim());
        b = b & 0xff;
        return new SVGColor(r | g | b);
    }

    private static SVGColor getColorWithName(String name) {
        String str = ColorName2HexRGBValue.getHexRGBString(name);
        if (str != null) {
            int hex = Integer.parseInt(str.split("#")[1].trim(), 16); //$NON-NLS-1$
            return new SVGColor(hex);
        } else
            return null;
    }
}
