package org.xmind.cathy.internal.css;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.w3c.dom.css.CSSValue;
import org.xmind.cathy.internal.ICathyConstants;

@SuppressWarnings("restriction")
public class CSSPropertyScrolledFormHandler
        extends AbstractCSSPropertySWTHandler {

    public static final CSSPropertyScrolledFormHandler INSTANCE = new CSSPropertyScrolledFormHandler();

    @Override
    protected void applyCSSProperty(Control control, String property,
            CSSValue value, String pseudo, CSSEngine engine) throws Exception {
        if (!(control instanceof ScrolledForm))
            return;

        ScrolledForm form = (ScrolledForm) control;
        if (ICathyConstants.PROPERTY_FG_COLOR.equals(property)) {
            if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
                Color fgColor = (Color) engine.convert(value, Color.class,
                        form.getDisplay());
                form.setForeground(fgColor);
            }
        } else if (ICathyConstants.PROPERTY_BG_COLOR.equals(property)) {
            Color bgColor = (Color) engine.convert(value, Color.class,
                    form.getDisplay());
            form.setBackground(bgColor);
        }

    }

    @Override
    protected String retrieveCSSProperty(Control control, String property,
            String pseudo, CSSEngine engine) throws Exception {
        return null;
    }

}
