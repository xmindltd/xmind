package org.xmind.cathy.internal.css;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSValue;
import org.xmind.cathy.internal.ICathyConstants;
import org.xmind.ui.tabfolder.MTabBar;
import org.xmind.ui.tabfolder.MTabFolder;

@SuppressWarnings("restriction")
public class CSSPropertyMTabBarHandler extends AbstractCSSPropertySWTHandler {

    public static final CSSPropertyMTabBarHandler INSTANCE = new CSSPropertyMTabBarHandler();

    @Override
    protected void applyCSSProperty(Control control, String property,
            CSSValue value, String pseudo, CSSEngine engine) throws Exception {
        if (!(control instanceof MTabBar))
            return;

        MTabBar tabBar = (MTabBar) control;
        if (ICathyConstants.PROPERTY_TABFOLDER_BACKGROUND.equals(property)) {
            if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
                Color color = (Color) engine.convert(value, Color.class,
                        tabBar.getDisplay());
                tabBar.getStyleProvider().setColor(MTabFolder.TAB_BAR,
                        color.getRGB());
            }
        }
    }

    @Override
    protected String retrieveCSSProperty(Control control, String property,
            String pseudo, CSSEngine engine) throws Exception {
        return null;
    }

}
