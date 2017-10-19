package org.xmind.cathy.internal.css;

import java.net.URL;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.xmind.cathy.internal.ICathyConstants;

@SuppressWarnings("restriction")
public class CSSPropertyToolBarSWTHandler
        extends AbstractCSSPropertySWTHandler {

    public static final CSSPropertyToolBarSWTHandler INSTANCE = new CSSPropertyToolBarSWTHandler();

    private static final String TOOLBAR_TAG_VIEW_MENU = "ViewMenu"; //$NON-NLS-1$

    @Override
    protected void applyCSSProperty(Control control, String property,
            CSSValue value, String pseudo, CSSEngine engine) throws Exception {
        if (!(control instanceof ToolBar))
            return;

        ToolBar toolBar = (ToolBar) control;
        if (ICathyConstants.PROPERTY_TOOL_ITEM_COLOR.equals(property)) {
            Color color = (Color) engine.convert(value, Color.class,
                    toolBar.getDisplay());
            toolBar.setForeground(color);

            ToolItem[] items = toolBar.getItems();
            for (ToolItem each : items) {
                String text = each.getText();
                each.setText(text);
            }
        } else if (ICathyConstants.PROPERTY_VIEW_MENU.equals(property)) {
            if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
                if (((CSSPrimitiveValue) value)
                        .getPrimitiveType() == CSSPrimitiveValue.CSS_URI) {
                    String imageUrl = ((CSSPrimitiveValue) value)
                            .getStringValue();
                    ImageDescriptor imageDescriptor = ImageDescriptor
                            .createFromURL(new URL(imageUrl.toString()));
                    Image image = JFaceResources.getResources()
                            .createImage(imageDescriptor);
                    if (TOOLBAR_TAG_VIEW_MENU.equals(toolBar.getData())) {
                        toolBar.getItem(0).setImage(image);
                    }

                }
            }
        }
//        else if ("xswt-view-properties-pin".equals(property)) {
//            ToolItem[] items = toolBar.getItems();
//            for (ToolItem each : items) {
//                Object data = each.getData();
//                if (data instanceof ActionContributionItem) {
//                    String id = ((ActionContributionItem) data).getId();
//                    if (id.contains("org.eclipse.ui.views.properties.PinPropertySheetAction")) {
//
//                    }
//                }
//            }
//        }
    }

    @Override
    protected String retrieveCSSProperty(Control control, String property,
            String pseudo, CSSEngine engine) throws Exception {
        if (control instanceof ToolBar) {
            if (ICathyConstants.PROPERTY_TOOL_ITEM_COLOR.equals(property)) {
                Color fgColor = ((ToolBar) control).getForeground();
                return engine.convert(fgColor, Color.class, null);
            }

        }
        return null;
    }

}
