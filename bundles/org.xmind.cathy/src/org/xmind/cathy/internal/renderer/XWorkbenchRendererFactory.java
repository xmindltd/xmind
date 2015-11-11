package org.xmind.cathy.internal.renderer;

import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.workbench.renderers.swt.ToolBarManagerRenderer;
import org.eclipse.e4.ui.workbench.renderers.swt.WorkbenchRendererFactory;

public class XWorkbenchRendererFactory extends WorkbenchRendererFactory {

    private XWBWRenderer xwbwRenderer;
    private ToolBarManagerRenderer xtoolbarRenderer;

    public AbstractPartRenderer getRenderer(MUIElement uiElement,
            Object parent) {
        if (uiElement instanceof MToolBar) {
            if (xtoolbarRenderer == null) {
                xtoolbarRenderer = new XToolBarManagerRenderer();
                initRenderer(xtoolbarRenderer);
            }
            return xtoolbarRenderer;
        } else if (uiElement instanceof MTrimmedWindow) {
            if (xwbwRenderer == null) {
                xwbwRenderer = new XWBWRenderer();
                initRenderer(xwbwRenderer);
            }
            return xwbwRenderer;
        }
        return super.getRenderer(uiElement, parent);
    }
}
