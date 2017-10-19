package org.xmind.cathy.internal.css;

import org.eclipse.e4.ui.css.core.dom.IElementProvider;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.w3c.dom.Element;
import org.xmind.ui.tabfolder.MTabBar;
import org.xmind.ui.tabfolder.MTabFolder;

@SuppressWarnings("restriction")
public class XSWTElementProvider implements IElementProvider {

    public static final IElementProvider INSTANCE = new XSWTElementProvider();

    public Element getElement(Object element, CSSEngine engine) {
        if (element instanceof FormText) {
            return new FormTextElement((FormText) element, engine);
        } else if (element instanceof ScrolledForm) {
            return new ScrolledFormElement((ScrolledForm) element, engine);
        } else if (element instanceof MTabBar) {
            return new MTabBarElement((MTabBar) element, engine);
        } else if (element instanceof MTabFolder) {
            return new MTabFolderElement((MTabFolder) element, engine);
        }
        return null;
    }

}
