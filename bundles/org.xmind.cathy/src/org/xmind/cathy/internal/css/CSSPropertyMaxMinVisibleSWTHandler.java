package org.xmind.cathy.internal.css;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;
import org.xmind.cathy.internal.ICathyConstants;

@SuppressWarnings("restriction")
public class CSSPropertyMaxMinVisibleSWTHandler
        extends AbstractCSSPropertySWTHandler {

    public static final ICSSPropertyHandler INSTANCE = new CSSPropertyMaxMinVisibleSWTHandler();

    private static final String CSS_CTABFOLDER_MAXMIN_VISIBLE_LISTENER_KEY = "CSS_CTABFOLDER_MAXMIN_VISIBLE_LISTENER_KEY"; //$NON-NLS-1$

    @Override
    public void applyCSSProperty(Control control, String property,
            CSSValue value, String pseudo, CSSEngine engine) throws Exception {
        if (!(control instanceof CTabFolder))
            return;

        CTabFolder folder = (CTabFolder) control;
        boolean visible = (Boolean) engine.convert(value, Boolean.class, null);

        ShowMaxMinVisibleListener listener = (ShowMaxMinVisibleListener) folder
                .getData(CSS_CTABFOLDER_MAXMIN_VISIBLE_LISTENER_KEY);
        if (listener == null) {
            listener = new ShowMaxMinVisibleListener(engine);
            folder.addListener(SWT.Paint, listener);
            folder.setData(CSS_CTABFOLDER_MAXMIN_VISIBLE_LISTENER_KEY,
                    listener);
        } else {
            listener.setEngine(engine);
        }

        if (ICathyConstants.PROPERTY_MAXIMIZE_VISIBLE.equals(property)) {
            folder.setMaximizeVisible(visible);
        } else if (ICathyConstants.PROPERTY_MINIMIZE_VISIBLE.equals(property)) {
            folder.setMinimizeVisible(visible);
        }
    }

    @Override
    public String retrieveCSSProperty(Control control, String property,
            String pseudo, CSSEngine engine) throws Exception {
        if (control instanceof CTabFolder) {
            CTabFolder folder = (CTabFolder) control;
            if (ICathyConstants.PROPERTY_MAXIMIZE_VISIBLE.equals(property)) {
                return Boolean.toString(folder.getMaximizeVisible());
            } else if (ICathyConstants.PROPERTY_MINIMIZE_VISIBLE
                    .equals(property)) {
                return Boolean.toString(folder.getMinimizeVisible());
            }
        }
        return null;
    }

    private class ShowMaxMinVisibleListener implements Listener {

        private CSSEngine engine;

        public ShowMaxMinVisibleListener(CSSEngine engine) {
            this.engine = engine;
        }

        public void setEngine(CSSEngine engine) {
            this.engine = engine;
        }

        public void handleEvent(Event e) {

            CTabFolder folder = (CTabFolder) e.widget;
            if (folder == null || folder.isDisposed()) {
                return;
            }

            Element element = engine.getElement(folder);
            CSSStyleDeclaration style = engine.getViewCSS()
                    .getComputedStyle(element, null);
            if (style != null) {
                CSSValue maximizeVisible = style.getPropertyCSSValue(
                        ICathyConstants.PROPERTY_MAXIMIZE_VISIBLE);
                CSSValue minimizeVisible = style.getPropertyCSSValue(
                        ICathyConstants.PROPERTY_MINIMIZE_VISIBLE);
                if (maximizeVisible != null) {
                    boolean maxVisible = Boolean
                            .parseBoolean(maximizeVisible.getCssText());
                    if (maxVisible != folder.getMaximizeVisible())
                        folder.setMaximizeVisible(maxVisible);
                }
                if (minimizeVisible != null) {
                    boolean miniVisible = Boolean
                            .parseBoolean(minimizeVisible.getCssText());
                    if (miniVisible != folder.getMinimizeVisible())
                        folder.setMinimizeVisible(miniVisible);
                }
            }
        }
    }
}
