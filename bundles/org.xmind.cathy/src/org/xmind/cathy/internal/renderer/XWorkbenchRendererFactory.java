package org.xmind.cathy.internal.renderer;

import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MDialog;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.workbench.renderers.swt.LazyStackRenderer;
import org.eclipse.e4.ui.workbench.renderers.swt.WorkbenchRendererFactory;
import org.xmind.ui.internal.e4models.IModelConstants;

public class XWorkbenchRendererFactory extends WorkbenchRendererFactory {

    private XWBWRenderer xwbwRenderer;
    private XToolBarManagerRenderer xtoolbarRenderer;
    private XDialogRenderer xdialogRenderer;
    private XStackRenderer xviewStackRenderer;
    private XSashRenderer xsashRenderer;
    private XMenuManagerRenderer xMenuManagerRenderer;
    private LazyStackRenderer stackRenderer;

    public AbstractPartRenderer getRenderer(MUIElement uiElement,
            Object parent) {
        boolean viewPartStack = (uiElement instanceof MPartStack)
                && (uiElement.getTags().contains(IModelConstants.TAG_X_STACK))
                && (uiElement.getElementId() != null);
        boolean editorPartStack = (uiElement instanceof MPartStack)
                && (((uiElement.getTags().contains("EditorStack")) //$NON-NLS-1$
                        && (uiElement.getElementId() != null))
                        || (uiElement.getParent() != null
                                && "org.eclipse.ui.editorss".equals( //$NON-NLS-1$
                                        uiElement.getParent().getElementId())));

        if (viewPartStack) {
            if (xviewStackRenderer == null) {
                xviewStackRenderer = new XStackRenderer();
                initRenderer(xviewStackRenderer);
            }
            return xviewStackRenderer;
        } else if (uiElement instanceof MPartSashContainer) {
            if (xsashRenderer == null) {
                xsashRenderer = new XSashRenderer();
                initRenderer(xsashRenderer);
            }
            return xsashRenderer;
        } else if (uiElement instanceof MToolBar) {
            if (xtoolbarRenderer == null) {
                xtoolbarRenderer = new XToolBarManagerRenderer();
                initRenderer(xtoolbarRenderer);
            }
            return xtoolbarRenderer;
        } else if (uiElement instanceof MMenu) {
            if (xMenuManagerRenderer == null) {
                xMenuManagerRenderer = new XMenuManagerRenderer();
                initRenderer(xMenuManagerRenderer);
            }
            return xMenuManagerRenderer;

        } else if (uiElement instanceof MTrimmedWindow) {
            if (xwbwRenderer == null) {
                xwbwRenderer = new XWBWRenderer();
                initRenderer(xwbwRenderer);
            }
            return xwbwRenderer;
        } else if (uiElement instanceof MDialog) {
            if (xdialogRenderer == null)
                xdialogRenderer = new XDialogRenderer();
            initRenderer(xdialogRenderer);
            return xdialogRenderer;
        } else if (uiElement instanceof MPartStack && editorPartStack) {
            if (stackRenderer == null) {
                stackRenderer = new XEditorStackRenderer();
                initRenderer(stackRenderer);
            }
            return stackRenderer;
        }
        return super.getRenderer(uiElement, parent);
    }
}
