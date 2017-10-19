package org.xmind.cathy.internal.css;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.CompositeElement;
import org.eclipse.ui.forms.widgets.ScrolledForm;

@SuppressWarnings("restriction")
public class ScrolledFormElement extends CompositeElement {

    public ScrolledFormElement(ScrolledForm scrolledForm, CSSEngine engine) {
        super(scrolledForm, engine);
    }

}