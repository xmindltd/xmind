package org.xmind.cathy.internal.css;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.SWTElementHelpers;
import org.eclipse.e4.ui.css.swt.properties.custom.CTabETabHelper;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;
import org.xmind.cathy.internal.ICathyConstants;

@SuppressWarnings("restriction")
public class CSSPropertyXShowCloseHandler extends CTabETabHelper
        implements ICSSPropertyHandler {

    public static final ICSSPropertyHandler INSTANCE = new CSSPropertyXShowCloseHandler();

    private static final String CSS_CTABITEM_SELECTED_SHOW_CLOSE_LISTENER_KEY = "CSS_CTABFOLDER_SELECTED_SHOW_CLOSE_LISTENER_KEY"; //$NON-NLS-1$

    private static final String SUPPORTED_PSEUDO = "selected"; //$NON-NLS-1$ 

    private static final String TAG_NOCLOSE = "NoClose"; //$NON-NLS-1$

    public boolean applyCSSProperty(Object element, String property,
            CSSValue value, String pseudo, CSSEngine engine) throws Exception {
        Widget widget = SWTElementHelpers.getWidget(element);
        if (widget instanceof CTabItem) {
            Item item = (Item) widget;
            boolean showClose = ((Boolean) engine.convert(value, Boolean.class,
                    null)).booleanValue();

            if (SUPPORTED_PSEUDO.equals(pseudo)) {
                Control parent = getParent(widget);

                ShowCloseSelectionListener listener = (ShowCloseSelectionListener) parent
                        .getData(CSS_CTABITEM_SELECTED_SHOW_CLOSE_LISTENER_KEY);
                if (listener == null) {
                    listener = new ShowCloseSelectionListener(engine);
                    parent.addListener(SWT.Paint, listener);
                    parent.setData(
                            CSS_CTABITEM_SELECTED_SHOW_CLOSE_LISTENER_KEY,
                            listener);
                } else {
                    listener.setEngine(engine);
                }
                item = getSelection(getParent(widget));

                if (item != null) {
                    internalSetShowClose(item, showClose);
                }
            } else {
                internalSetShowClose(item, showClose);
            }
            return true;
        }
        return false;
    }

    public String retrieveCSSProperty(Object element, String property,
            String pseudo, CSSEngine engine) throws Exception {
        Widget widget = SWTElementHelpers.getWidget(element);
        if (widget instanceof CTabItem) {
            CTabItem item = (CTabItem) widget;
            return Boolean.toString(item.getShowClose());
        }
        return null;
    }

    private class ShowCloseSelectionListener implements Listener {

        private CSSEngine engine;

        public ShowCloseSelectionListener(CSSEngine engine) {
            this.engine = engine;
        }

        public void setEngine(CSSEngine engine) {
            this.engine = engine;
        }

        public void handleEvent(Event e) {

            Item selection = getSelection(e.widget);

            if (selection == null || selection.isDisposed()) {
                return;
            }

            Item[] items = getItems(e.widget);
            int selectionIndex = getSelectionIndex(e.widget);

            boolean selectionSet = false;

            CSSStyleDeclaration selectedStyle = engine.getViewCSS()
                    .getComputedStyle(engine.getElement(selection),
                            SUPPORTED_PSEUDO);
            if (selectedStyle != null) {
                CSSValue value = selectedStyle.getPropertyCSSValue(
                        ICathyConstants.PROPERTY_SHOW_CLOSE);
                if (value != null) {
                    internalSetShowClose(selection,
                            Boolean.parseBoolean(value.getCssText()));
                    selectionSet = true;
                }
            }

            CSSStyleDeclaration unselectedStyle = engine.getViewCSS()
                    .getComputedStyle(engine.getElement(selection), null);
            if (unselectedStyle == null) {
                for (int i = 0; i < items.length; i++) {
                    if (selectionSet && i != selectionIndex) {
                        internalSetShowClose(items[i], false);
                    }
                }
            } else {
                CSSValue value = unselectedStyle.getPropertyCSSValue(
                        ICathyConstants.PROPERTY_SHOW_CLOSE);
                boolean unselectedShowClose = value == null ? false
                        : Boolean.parseBoolean(value.getCssText());
                for (int i = 0; i < items.length; i++) {
                    if (selectionSet && i != selectionIndex) {
                        internalSetShowClose(items[i], unselectedShowClose);
                    }
                }
            }

        }
    }

    private void internalSetShowClose(Item item, boolean showClose) {
        Object data = item.getData(AbstractPartRenderer.OWNING_ME);
        if (data instanceof MUIElement) {
            boolean noClose = ((MUIElement) data).getTags()
                    .contains(TAG_NOCLOSE);
            if (noClose)
                showClose = !noClose;
        }
        if (item instanceof CTabItem)
            ((CTabItem) item).setShowClose(showClose);
    }
}
