package org.xmind.cathy.internal.css;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.CompositeElement;
import org.eclipse.ui.forms.widgets.FormText;

@SuppressWarnings("restriction")
public class FormTextElement extends CompositeElement {

    public FormTextElement(FormText formText, CSSEngine engine) {
        super(formText, engine);
    }

}
