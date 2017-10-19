package org.xmind.cathy.internal.css;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.CompositeElement;
import org.eclipse.ui.forms.widgets.Section;

@SuppressWarnings("restriction")
public class SectionElement extends CompositeElement {

    public SectionElement(Section section, CSSEngine engine) {
        super(section, engine);
    }

}
