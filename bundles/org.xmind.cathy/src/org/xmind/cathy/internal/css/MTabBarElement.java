package org.xmind.cathy.internal.css;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.CompositeElement;
import org.xmind.ui.tabfolder.MTabBar;

@SuppressWarnings("restriction")
public class MTabBarElement extends CompositeElement {

    public MTabBarElement(MTabBar tabBar, CSSEngine engine) {
        super(tabBar, engine);
    }

}
