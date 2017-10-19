package org.xmind.cathy.internal.css;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.CompositeElement;
import org.eclipse.e4.ui.workbench.renderers.swt.SashLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

@SuppressWarnings("restriction")
public class SashWidthHandler implements ICSSPropertyHandler {

    private static final String FIELD_SASH_WIDTH = "sashWidth"; //$NON-NLS-1$

    private ReflectionSupport<SashLayout> sashLayout = new ReflectionSupport<SashLayout>(
            SashLayout.class);

    public boolean applyCSSProperty(Object element, String property,
            CSSValue value, String pseudo, CSSEngine engine) throws Exception {
        if (!(element instanceof CompositeElement)) {
            return false;
        }
        CompositeElement compositeElement = (CompositeElement) element;
        Object nativeWidget = compositeElement.getNativeWidget();
        if (!(nativeWidget instanceof Composite)) {
            return false;
        }

        Composite composite = (Composite) nativeWidget;
        Layout layout = composite.getLayout();
        if (!(layout instanceof SashLayout)) {
            return false;
        }

        if (!(value instanceof CSSPrimitiveValue)) {
            return false;
        }

        int newWidth = (int) ((CSSPrimitiveValue) value)
                .getFloatValue(CSSPrimitiveValue.CSS_PX);
        sashLayout.set(layout, FIELD_SASH_WIDTH, newWidth);

        composite.layout(true, true);
        return true;
    }

    public String retrieveCSSProperty(Object element, String property,
            String pseudo, CSSEngine engine) throws Exception {
        if (!(element instanceof CompositeElement)) {
            return null;
        }
        CompositeElement compositeElement = (CompositeElement) element;
        Object nativeWidget = compositeElement.getNativeWidget();
        if (!(nativeWidget instanceof Composite)) {
            return null;
        }

        Composite composite = (Composite) nativeWidget;
        Layout layout = composite.getLayout();
        if (!(layout instanceof SashLayout)) {
            return null;
        }

        Integer width = (Integer) sashLayout.getFieldValue(FIELD_SASH_WIDTH,
                (SashLayout) layout);
        return Integer.toString(width);
    }

}
