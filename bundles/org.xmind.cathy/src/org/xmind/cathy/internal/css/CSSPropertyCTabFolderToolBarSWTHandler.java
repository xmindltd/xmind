package org.xmind.cathy.internal.css;

import java.lang.reflect.Method;
import java.net.URL;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolderRenderer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.xmind.cathy.internal.ICathyConstants;

@SuppressWarnings("restriction")
public class CSSPropertyCTabFolderToolBarSWTHandler
        extends AbstractCSSPropertySWTHandler {

    public static final ICSSPropertyHandler INSTANCE = new CSSPropertyCTabFolderToolBarSWTHandler();

    private static final String METHOD_SET_CHEVRON_VISIBLE = "setChevronVisible"; //$NON-NLS-1$

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

        ICTabFolderRendering tabFolderRendering = (ICTabFolderRendering) renderer;

        Image image = null;
        if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
            if (((CSSPrimitiveValue) value)
                    .getPrimitiveType() == CSSPrimitiveValue.CSS_URI) {
                String imageUrl = ((CSSPrimitiveValue) value).getStringValue();
                ImageDescriptor imageDescriptor = ImageDescriptor
                        .createFromURL(new URL(imageUrl.toString()));
                image = JFaceResources.getResources()
                        .createImage(imageDescriptor);
            }
        }

        if (ICathyConstants.PROPERTY_MAXIMIZE_IMAGE.equals(property)) {
            tabFolderRendering.setMaximizeImage(image);
        } else if (ICathyConstants.PROPERTY_MINIMIZE_IMAGE.equals(property)) {
            tabFolderRendering.setMinimizeImage(image);
        } else if (ICathyConstants.PROPERTY_CLOSE_IMAGE.equals(property)) {
            tabFolderRendering.setCloseImage(image);
        } else
            if (ICathyConstants.PROPERTY_CLOSE_HOVER_IMAGE.equals(property)) {
            tabFolderRendering.setClsoeHoverImage(image);
        } else if (ICathyConstants.PROPERTY_CHEVRON_VISIBLE.equals(property)) {
            ReflectionSupport<CTabFolder> reflect = new ReflectionSupport<CTabFolder>(
                    CTabFolder.class);
            boolean chevronVisible = (Boolean) engine.convert(value,
                    Boolean.class, null);
            Method setChevronVisible = reflect.getMethod(
                    METHOD_SET_CHEVRON_VISIBLE,
                    new Class<?>[] { boolean.class });
            reflect.executeMethod(setChevronVisible, folder,
                    new Object[] { chevronVisible });
        }

    }

    @Override
    protected String retrieveCSSProperty(Control control, String property,
            String pseudo, CSSEngine engine) throws Exception {
        return null;
    }

}
