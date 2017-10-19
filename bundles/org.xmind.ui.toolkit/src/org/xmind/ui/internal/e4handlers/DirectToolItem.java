package org.xmind.ui.internal.e4handlers;

import javax.inject.Inject;

import org.eclipse.core.runtime.IAdaptable;
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

public class DirectToolItem implements IAdaptable {

    @Inject
    private EModelService modelService;
    private MMenu mmenu;
    private ToolItem toolItem;

    @Execute
    public void execute(@Optional MToolItem model) {
        if (modelService == null || model == null)
            return;

        mmenu = model.getMenu();
        Object widget = model.getWidget();
        if (!(widget instanceof ToolItem))
            return;
        toolItem = (ToolItem) widget;
        Rectangle itemBounds = toolItem.getBounds();
        Point displayAt = toolItem.getParent().toDisplay(itemBounds.x,
                itemBounds.y);
        showPullDownMenuAt(new Rectangle(displayAt.x, displayAt.y,
                itemBounds.width, itemBounds.height));
        showExtensionControl(new Rectangle(displayAt.x, displayAt.y,
                itemBounds.width, itemBounds.height));
    }

    protected void showExtensionControl(Rectangle itemBoundsToDisplay) {
    }

    protected void showPullDownMenuAt(Rectangle itemBoundsToDisplay) {
    }

    protected Menu getMenu() {
        if (mmenu == null || toolItem == null)
            return null;
        return getMenu(mmenu, toolItem);
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

    public <T> T getAdapter(Class<T> adapter) {
        if (EModelService.class.equals(adapter)) {
            return adapter.cast(modelService);
        } else if (MMenu.class.equals(adapter)) {
            return adapter.cast(mmenu);
        } else if (ToolItem.class.equals(adapter)) {
            return adapter.cast(toolItem);
        }
        return null;
    }

}
