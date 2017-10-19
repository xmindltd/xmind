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
public class CSSPropertyCTabFolderRenderNoneHandler
        extends AbstractCSSPropertySWTHandler {

    public static final ICSSPropertyHandler INSTANCE = new CSSPropertyCTabFolderRenderNoneHandler();

    @Override
    protected void applyCSSProperty(Control control, String property,
            CSSValue value, String pseudo, CSSEngine engine) throws Exception {
        if (!(control instanceof CTabFolder)) {
            return;
        }
        CTabFolder folder = (CTabFolder) control;
        CTabFolderRenderer renderer = folder.getRenderer();
        if (!(renderer instanceof ICTabFolderRendering))
            return;

        boolean isNoneRender = (Boolean) engine.convert(value, Boolean.class,
                null);
        if (ICathyConstants.PROPERTY_CTABFOLDER_RENDER_NONE.equals(property)) {
            ((ICTabFolderRendering) renderer).setNoneRender(isNoneRender);
        }
    }

    @Override
    protected String retrieveCSSProperty(Control control, String property,
            String pseudo, CSSEngine engine) throws Exception {
        return null;
    }

}
