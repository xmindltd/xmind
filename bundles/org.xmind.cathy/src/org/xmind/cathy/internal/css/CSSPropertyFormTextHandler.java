package org.xmind.cathy.internal.css;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.ui.forms.widgets.FormText;
import org.w3c.dom.css.CSSValue;
import org.xmind.cathy.internal.ICathyConstants;

@SuppressWarnings("restriction")
public class CSSPropertyFormTextHandler extends AbstractCSSPropertySWTHandler {

    public static final CSSPropertyFormTextHandler INSTANCE = new CSSPropertyFormTextHandler();

    @Override
    protected void applyCSSProperty(Control control, String property,
            CSSValue value, String pseudo, CSSEngine engine) throws Exception {
        if (!(control instanceof FormText))
            return;

        FormText formText = (FormText) control;
        if (ICathyConstants.PROPERTY_HYPERLINK_COLOR.equals(property)) {
            if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
                Color hyperlinkColor = (Color) engine.convert(value,
                        Color.class, formText.getDisplay());
                HyperlinkSettings hyperlinkSettings = formText
                        .getHyperlinkSettings();
                hyperlinkSettings.setForeground(hyperlinkColor);
            }
        } else if (ICathyConstants.PROPERTY_ACTIVE_HYPERLINK_COLOR
                .equals(property)) {
            Color activeHyperlinkColor = (Color) engine.convert(value,
                    Color.class, formText.getDisplay());
            formText.getHyperlinkSettings()
                    .setActiveForeground(activeHyperlinkColor);
        }

    }

    @Override
    protected String retrieveCSSProperty(Control control, String property,
            String pseudo, CSSEngine engine) throws Exception {
        return null;
    }

}
