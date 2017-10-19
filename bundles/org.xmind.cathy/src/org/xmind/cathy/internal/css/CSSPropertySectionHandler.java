package org.xmind.cathy.internal.css;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.Section;
import org.w3c.dom.css.CSSValue;
import org.xmind.cathy.internal.ICathyConstants;

@SuppressWarnings("restriction")
public class CSSPropertySectionHandler extends AbstractCSSPropertySWTHandler {

    public static final CSSPropertySectionHandler INSTANCE = new CSSPropertySectionHandler();

    @Override
    protected void applyCSSProperty(Control control, String property,
            CSSValue value, String pseudo, CSSEngine engine) throws Exception {
        if (!(control instanceof Section))
            return;

        Section section = (Section) control;
        if (ICathyConstants.PROPERTY_TITLE_BAR_TEXT_COLOR.equals(property)) {
            if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
                Color textColor = (Color) engine.convert(value, Color.class,
                        section.getDisplay());
                section.setTitleBarForeground(textColor);
                section.setToggleColor(textColor);
            }
        } else if (ICathyConstants.PROPERTY_TITLE_BAR_ACTIVE_TEXT_COLOR
                .equals(property)) {
            if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
                Color activeTextColor = (Color) engine.convert(value,
                        Color.class, section.getDisplay());
                section.setActiveToggleColor(activeTextColor);
            }
        }

    }

    @Override
    protected String retrieveCSSProperty(Control control, String property,
            String pseudo, CSSEngine engine) throws Exception {
        if (control instanceof Section) {
            Section section = (Section) control;
            if (ICathyConstants.PROPERTY_TITLE_BAR_TEXT_COLOR
                    .equals(property)) {
                Color textColor = section.getTitleBarForeground();
                return engine.convert(textColor, Color.class, null);
            }
        }
        return null;
    }
}
