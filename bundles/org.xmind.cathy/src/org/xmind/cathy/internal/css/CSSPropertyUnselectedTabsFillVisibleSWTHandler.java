package org.xmind.cathy.internal.css;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolderRenderer;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSValue;
import org.xmind.cathy.internal.ICathyConstants;

@SuppressWarnings("restriction")
public class CSSPropertyUnselectedTabsFillVisibleSWTHandler
        extends AbstractCSSPropertySWTHandler {

    public static final ICSSPropertyHandler INSTANCE = new CSSPropertyUnselectedTabsFillVisibleSWTHandler();

    @Override
    protected void applyCSSProperty(Control control, String property,
            CSSValue value, String pseudo, CSSEngine engine) throws Exception {
        if (!(control instanceof CTabFolder)) {
            return;
        }
        boolean unselectedTabsBackgroundVisible = (Boolean) engine
                .convert(value, Boolean.class, null);
        CTabFolderRenderer renderer = ((CTabFolder) control).getRenderer();
        if (renderer instanceof ICTabFolderRendering) {
            if (ICathyConstants.PROPERTY_UNSELECTED_TABS_BG_VISIBLE
                    .equals(property)) {
                ((ICTabFolderRendering) renderer)
                        .setUnselectedTabsBackgroundVisible(
                                unselectedTabsBackgroundVisible);
            }
        }
    }

    @Override
    protected String retrieveCSSProperty(Control control, String property,
            String pseudo, CSSEngine engine) throws Exception {
        return null;
    }

}
