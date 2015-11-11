package org.xmind.ui.internal.e4handlers;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.internal.workbench.RenderedElementUtil;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolItem;

public class ShowPullDownMenuToolItem {

    @Inject
    private EModelService modelService;

    @Execute
    public void execute(@Optional MToolItem model) {
        if (modelService == null || model == null)
            return;

        Object widget = model.getWidget();

        if (!(widget instanceof ToolItem))
            return;

        MMenu mmenu = model.getMenu();
        if (mmenu == null)
            return;

        ToolItem toolItem = (ToolItem) widget;
        Menu menu = getMenu(mmenu, toolItem);
        if (menu == null || menu.isDisposed())
            return;

        Rectangle itemBounds = toolItem.getBounds();
        Point displayAt = toolItem.getParent().toDisplay(itemBounds.x,
                itemBounds.y + itemBounds.height);
        menu.setLocation(displayAt);
        menu.setVisible(true);
    }

    private Menu getMenu(final MMenu mmenu, ToolItem toolItem) {
        Object obj = mmenu.getWidget();
        if (obj instanceof Menu) {
            return (Menu) obj;
        }
        // this is a temporary passthrough of the IMenuCreator
        if (RenderedElementUtil.isRenderedMenu(mmenu)) {
            obj = RenderedElementUtil.getContributionManager(mmenu);
            if (obj instanceof IContextFunction) {
                final IEclipseContext lclContext = getContext(mmenu);
                obj = ((IContextFunction) obj).compute(lclContext, null);
                RenderedElementUtil.setContributionManager(mmenu, obj);
            }
            if (obj instanceof IMenuCreator) {
                final IMenuCreator creator = (IMenuCreator) obj;
                final Menu menu = creator
                        .getMenu(toolItem.getParent().getShell());
                if (menu != null) {
                    toolItem.addDisposeListener(new DisposeListener() {
                        public void widgetDisposed(DisposeEvent e) {
                            if (menu != null && !menu.isDisposed()) {
                                creator.dispose();
                                mmenu.setWidget(null);
                            }
                        }
                    });
                    mmenu.setWidget(menu);
                    menu.setData(AbstractPartRenderer.OWNING_ME, menu);
                    return menu;
                }
            }
        } else {
            final IEclipseContext lclContext = getContext(mmenu);
            IPresentationEngine engine = lclContext
                    .get(IPresentationEngine.class);
            obj = engine.createGui(mmenu, toolItem.getParent(), lclContext);
            if (obj instanceof Menu) {
                return (Menu) obj;
            }
        }
        return null;
    }

    private IEclipseContext getContext(MUIElement part) {
        if (part instanceof MContext) {
            return ((MContext) part).getContext();
        }
        return getContextForParent(part);
    }

    private IEclipseContext getContextForParent(MUIElement element) {
        return modelService.getContainingContext(element);
    }

}
