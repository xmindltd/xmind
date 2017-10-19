package org.xmind.cathy.internal.css;

import org.eclipse.e4.ui.css.core.dom.properties.Gradient;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelper;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.internal.forms.widgets.FormHeading;
import org.w3c.dom.css.CSSValue;
import org.xmind.cathy.internal.ICathyConstants;

@SuppressWarnings("restriction")
public class CSSPropertyFormHeadingHandler
        extends AbstractCSSPropertySWTHandler {

    public static final CSSPropertyFormHeadingHandler INSTANCE = new CSSPropertyFormHeadingHandler();

    @Override
    protected void applyCSSProperty(Control control, String property,
            CSSValue value, String pseudo, CSSEngine engine) throws Exception {
        if (!(control instanceof FormHeading))
            return;

        FormHeading formHeading = (FormHeading) control;
        if (ICathyConstants.PROPERTY_XTEXT_BACKGROUND.equals(property)) {
            applyCSSPropertyTextBackgroud(formHeading, value, engine);
        } else if (ICathyConstants.PROPERTY_XBOTTOM_KEYLINE_1_COLOR
                .equals(property)) {
            Color keyline1Color = (Color) engine.convert(value, Color.class,
                    formHeading.getDisplay());
            formHeading.putColor(IFormColors.H_BOTTOM_KEYLINE1, keyline1Color);

        } else if (ICathyConstants.PROPERTY_XBOTTOM_KEYLINE_2_COLOR
                .equals(property)) {
            Color keyline2Color = (Color) engine.convert(value, Color.class,
                    formHeading.getDisplay());
            formHeading.putColor(IFormColors.H_BOTTOM_KEYLINE2, keyline2Color);
        }

    }

    private void applyCSSPropertyTextBackgroud(FormHeading formHeading,
            CSSValue value, CSSEngine engine) throws Exception {
        if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
            Gradient grad = (Gradient) engine.convert(value, Gradient.class,
                    formHeading.getDisplay());
            if (grad == null) {
                return;
            }
            Color[] colors = null;
            int[] percents = null;
            if (!grad.getValues().isEmpty()) {
                colors = CSSSWTColorHelper.getSWTColors(grad,
                        formHeading.getDisplay(), engine);
                percents = CSSSWTColorHelper.getPercents(grad);
            }
            formHeading.setTextBackground(colors, percents, true);
        } else if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
            Color color = (Color) engine.convert(value, Color.class,
                    formHeading.getDisplay());
            formHeading.setTextBackground(new Color[] { color, color },
                    new int[] { 100 }, true);
        }
    }

    @Override
    protected String retrieveCSSProperty(Control control, String property,
            String pseudo, CSSEngine engine) throws Exception {
        if (control instanceof FormHeading) {
            if (ICathyConstants.PROPERTY_XBOTTOM_KEYLINE_1_COLOR
                    .equals(property)) {
                Color keyline1Color = ((FormHeading) control)
                        .getColor(IFormColors.H_BOTTOM_KEYLINE1);
                return engine.convert(keyline1Color, Color.class, null);
            } else if (ICathyConstants.PROPERTY_XBOTTOM_KEYLINE_2_COLOR
                    .equals(property)) {
                Color keyline2Color = ((FormHeading) control)
                        .getColor(IFormColors.H_BOTTOM_KEYLINE2);
                return engine.convert(keyline2Color, Color.class, null);
            }
        }
        return null;
    }
}
