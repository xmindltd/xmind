package org.xmind.cathy.internal.css;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.e4.ui.workbench.renderers.swt.TrimBarLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;
import org.xmind.cathy.internal.ICathyConstants;

@SuppressWarnings("restriction")
public class CSSPropertyMarginXHandler extends AbstractCSSPropertySWTHandler {

    public static final CSSPropertyMarginXHandler INSTANCE = new CSSPropertyMarginXHandler();

    private final static int TOP = 0;
    private final static int RIGHT = 1;
    private final static int BOTTOM = 2;
    private final static int LEFT = 3;

    @Override
    protected void applyCSSProperty(Control control, String property,
            CSSValue value, String pseudo, CSSEngine engine) throws Exception {
        if (!(control instanceof Composite))
            return;

        Composite composite = (Composite) control;

        if (ICathyConstants.PROPERTY_MARGIN.equals(property))
            applyCSSPropertyMargin(composite, value, pseudo, engine);
        else if (ICathyConstants.PROPERTY_MARGIN_TOP.equals(property))
            applyCSSPropertyMarginTop(composite, value, pseudo, engine);
        else if (ICathyConstants.PROPERTY_MARGIN_RIGHT.equals(property))
            applyCSSPropertyMarginRight(composite, value, pseudo, engine);
        else if (ICathyConstants.PROPERTY_MARGIN_BOTTOM.equals(property))
            applyCSSPropertyMarginBottom(composite, value, pseudo, engine);
        else if (ICathyConstants.PROPERTY_MARGIN_LEFT.equals(property))
            applyCSSPropertyMarginLeft(composite, value, pseudo, engine);

    }

    public void applyCSSPropertyMargin(Composite element, CSSValue value,
            String pseudo, CSSEngine engine) throws Exception {

        // If single value then assigned to all four margins
        if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
            setMargin(element, TOP, value, pseudo);
            setMargin(element, RIGHT, value, pseudo);
            setMargin(element, BOTTOM, value, pseudo);
            setMargin(element, LEFT, value, pseudo);
            return;
        }

        if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
            CSSValueList valueList = (CSSValueList) value;
            int length = valueList.getLength();

            if (length < 2 || length > 4)
                return;

            switch (length) {
            case 4:
                // If four values then assigned top/right/bottom/left
                setMargin(element, TOP, valueList.item(0), pseudo);
                setMargin(element, RIGHT, valueList.item(1), pseudo);
                setMargin(element, BOTTOM, valueList.item(2), pseudo);
                setMargin(element, LEFT, valueList.item(3), pseudo);
                break;
            case 3:
                // If three values then assigned top=v1, left=v2, right=v2, bottom=v3
                setMargin(element, TOP, valueList.item(0), pseudo);
                setMargin(element, RIGHT, valueList.item(1), pseudo);
                setMargin(element, BOTTOM, valueList.item(2), pseudo);
                setMargin(element, LEFT, valueList.item(1), pseudo);
            case 2:
                // If two values then assigned top/bottom=v1, left/right=v2
                setMargin(element, TOP, valueList.item(0), pseudo);
                setMargin(element, RIGHT, valueList.item(1), pseudo);
                setMargin(element, BOTTOM, valueList.item(0), pseudo);
                setMargin(element, LEFT, valueList.item(1), pseudo);
            }
        }
    }

    private void applyCSSPropertyMarginTop(Composite element, CSSValue value,
            String pseudo, CSSEngine engine) throws Exception {
        setMargin(element, TOP, value, pseudo);
    }

    private void applyCSSPropertyMarginRight(Composite element, CSSValue value,
            String pseudo, CSSEngine engine) throws Exception {
        setMargin(element, RIGHT, value, pseudo);
    }

    private void applyCSSPropertyMarginBottom(Composite element, CSSValue value,
            String pseudo, CSSEngine engine) throws Exception {
        setMargin(element, BOTTOM, value, pseudo);
    }

    private void applyCSSPropertyMarginLeft(Composite element, CSSValue value,
            String pseudo, CSSEngine engine) throws Exception {
        setMargin(element, LEFT, value, pseudo);
    }

    private void setMargin(Composite composite, int side, CSSValue value,
            String pseudo) {
        if (value.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE)
            return;
        int pixelValue = (int) ((CSSPrimitiveValue) value)
                .getFloatValue(CSSPrimitiveValue.CSS_PX);

        Layout layout = composite.getLayout();
        if (layout == null || !(layout instanceof TrimBarLayout))
            return;

        TrimBarLayout trimBarLayout = (TrimBarLayout) layout;
        switch (side) {
        case TOP:
            trimBarLayout.marginTop = pixelValue;
            break;
        case RIGHT:
            trimBarLayout.marginRight = pixelValue;
            break;
        case BOTTOM:
            trimBarLayout.marginBottom = pixelValue;
            break;
        case LEFT:
            trimBarLayout.marginLeft = pixelValue;
            break;
        }
    }

    @Override
    protected String retrieveCSSProperty(Control control, String property,
            String pseudo, CSSEngine engine) throws Exception {
        return null;
    }

}
