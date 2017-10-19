package org.xmind.cathy.internal.css;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.CompositeElement;
import org.xmind.ui.tabfolder.MTabFolder;

@SuppressWarnings("restriction")
public class MTabFolderElement extends CompositeElement {

    public MTabFolderElement(MTabFolder tabFolder, CSSEngine engine) {
        super(tabFolder, engine);
    }

}
