package org.xmind.cathy.internal.renderer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.renderers.swt.LazyStackRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class XStackRenderer extends LazyStackRenderer {

    @Inject
    private IPresentationEngine renderer;

    @Inject
    private IEventBroker eventBroker;

    @Override
    public Object createWidget(MUIElement element, Object parent) {
        if (!(element instanceof MPartStack) || !(parent instanceof Composite))
            return null;

        MPartStack viewStack = (MPartStack) element;

        Composite parentComposite = (Composite) parent;
        Composite viewContainer = null;

        // Ensure that all rendered PartStacks have an Id
        if (element.getElementId() == null
                || element.getElementId().length() == 0) {
            String generatedId = "RightStack@" //$NON-NLS-1$
                    + Integer.toHexString(element.hashCode());
            element.setElementId(generatedId);
        }

        int styleOverride = getStyleOverride(viewStack);
        int style = styleOverride == -1 ? SWT.NONE : styleOverride;
        //TODO Should use custom composite?
        viewContainer = new Composite(parentComposite, style);
        viewContainer.setLayout(new StackLayout());

        bindWidget(element, viewContainer);

        return viewContainer;
    }

    @Override
    protected void showTab(MUIElement element) {
        super.showTab(element);

        if (element instanceof MPartStack
                && element.getRenderer() == XStackRenderer.this) {
            MPartStack stackModel = (MPartStack) element;
            MUIElement curSel = stackModel.getSelectedElement();
            MPart part = (MPart) ((curSel instanceof MPlaceholder)
                    ? ((MPlaceholder) curSel).getRef() : curSel);
            if (curSel instanceof MPlaceholder) {
                part.setCurSharedRef((MPlaceholder) curSel);
            }
        }

        // an invisible element won't have the correct widget hierarchy
        if (!element.isVisible()) {
            return;
        }

        final Composite viewContainer = (Composite) getParentWidget(element);
        Control ctrl = (Control) element.getWidget();
        if (ctrl != null && ctrl.getParent() != viewContainer) {
            ctrl.setParent(viewContainer);
        } else if (ctrl == null) {
            renderer.createGui(element);
        }

        ctrl = (Control) element.getWidget();

        if (ctrl instanceof Composite) {
            ((Composite) ctrl).layout(false, true);
        }
        ((StackLayout) viewContainer.getLayout()).topControl = ctrl;
        viewContainer.layout(true, true);

    }

    @Override
    public void childRendered(final MElementContainer<MUIElement> parentElement,
            MUIElement element) {
        super.childRendered(parentElement, element);

        if (!(((MUIElement) parentElement) instanceof MPartStack)
                || !(element instanceof MStackElement))
            return;
        showTab(element);
    }

    @PostConstruct
    public void init() {
        super.init(eventBroker);
    }

    @PreDestroy
    public void contextDisposed() {
        super.contextDisposed(eventBroker);
    }
}
