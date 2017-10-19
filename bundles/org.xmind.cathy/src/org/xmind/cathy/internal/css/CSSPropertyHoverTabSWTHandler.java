package org.xmind.cathy.internal.css;

import org.eclipse.e4.ui.css.core.dom.properties.Gradient;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelper;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolderRenderer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSValue;

@SuppressWarnings("restriction")
public class CSSPropertyHoverTabSWTHandler extends
        AbstractCSSPropertySWTHandler {

    public static final CSSPropertyHoverTabSWTHandler INSTANCE = new CSSPropertyHoverTabSWTHandler();

    @Override
    protected void applyCSSProperty(Control control, String property,
            CSSValue value, String pseudo, CSSEngine engine) throws Exception {
        if (!(control instanceof CTabFolder)) {
            return;
        }

        CTabFolderRenderer renderer = ((CTabFolder) control).getRenderer();
        if (!(renderer instanceof ICTabFolderRendering))
            return;

        ICTabFolderRendering tabFolderRendering = (ICTabFolderRendering) renderer;

        if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
            Color color = (Color) engine.convert(value, Color.class,
                    control.getDisplay());
            tabFolderRendering.setHoverTabColor(color);
            return;
        }

        if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
            Gradient grad = (Gradient) engine.convert(value, Gradient.class,
                    control.getDisplay());
            if (grad == null) {
                return;
            }
            Color[] colors = null;
            int[] percents = null;
            if (!grad.getValues().isEmpty()) {
                colors = CSSSWTColorHelper.getSWTColors(grad,
                        control.getDisplay(), engine);
                percents = CSSSWTColorHelper.getPercents(grad);
            }
            tabFolderRendering.setHoverTabColor(colors, percents);
        }

    }

    @Override
    protected String retrieveCSSProperty(Control control, String property,
            String pseudo, CSSEngine engine) throws Exception {
        return null;
    }

}
