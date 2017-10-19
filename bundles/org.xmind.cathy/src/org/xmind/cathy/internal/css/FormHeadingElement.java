package org.xmind.cathy.internal.css;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.CompositeElement;
import org.eclipse.ui.internal.forms.widgets.FormHeading;

@SuppressWarnings("restriction")
public class FormHeadingElement extends CompositeElement {

    public FormHeadingElement(FormHeading formHeading, CSSEngine engine) {
        super(formHeading, engine);
    }

}
