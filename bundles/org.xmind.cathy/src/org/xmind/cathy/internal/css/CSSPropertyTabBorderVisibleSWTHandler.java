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
public class CSSPropertyTabBorderVisibleSWTHandler
        extends AbstractCSSPropertySWTHandler {

    public static final ICSSPropertyHandler INSTANCE = new CSSPropertyTabBorderVisibleSWTHandler();

    @Override
    protected void applyCSSProperty(Control control, String property,
            CSSValue value, String pseudo, CSSEngine engine) throws Exception {
        if (!(control instanceof CTabFolder)) {
            return;
        }
        boolean borderVisible = (Boolean) engine.convert(value, Boolean.class,
                null);
        CTabFolderRenderer renderer = ((CTabFolder) control).getRenderer();
        if (renderer instanceof ICTabFolderRendering) {
            if (ICathyConstants.PROPERTY_OUTER_BORDER_VISIBLE
                    .equals(property)) {
                ((ICTabFolderRendering) renderer)
                        .setOuterBorderVisible(borderVisible);
            } else if (ICathyConstants.PROPERTY_INNER_BORDER_VISIBLE
                    .equals(property)) {
                ((ICTabFolderRendering) renderer)
                        .setInnerBorderVisible(borderVisible);
            }
        }

    }

    @Override
    protected String retrieveCSSProperty(Control control, String property,
            String pseudo, CSSEngine engine) throws Exception {
        return null;
    }

}
