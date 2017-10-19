package org.xmind.cathy.internal.renderer;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.renderers.swt.SWTPartRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.osgi.service.event.Event;

public class XSashRenderer extends SWTPartRenderer {

    private static final int UNDEFINED_WEIGHT = -1;
    private static final int DEFAULT_WEIGHT = 5000;

    private int processedContent = 0;

    @SuppressWarnings("unchecked")
    @Inject
    @Optional
    private void subscribeTopicOrientationChanged(
            @UIEventTopic(UIEvents.GenericTile.TOPIC_HORIZONTAL) Event event) {
        // Ensure that this event is for a MPartSashContainer
        MUIElement element = (MUIElement) event
                .getProperty(UIEvents.EventTags.ELEMENT);
        if (element.getRenderer() != XSashRenderer.this) {
            return;
        }
        forceLayout((MElementContainer<MUIElement>) element);
    }

    @SuppressWarnings("unchecked")
    @Inject
    @Optional
    private void subscribeTopicSashWeightChanged(
            @UIEventTopic(UIEvents.UIElement.TOPIC_CONTAINERDATA) Event event) {
        // Ensure that this event is for a MPartSashContainer
        MUIElement element = (MUIElement) event
                .getProperty(UIEvents.EventTags.ELEMENT);
        if (element.getRenderer() != XSashRenderer.this) {
            return;
        }
        forceLayout((MElementContainer<MUIElement>) element);
    }

    /**
     * @param pscModel
     */
    protected void forceLayout(MElementContainer<MUIElement> pscModel) {
        if (processedContent != 0) {
            return;
        }
        // layout the containing Composite
        while (!(pscModel.getWidget() instanceof Composite))
            pscModel = pscModel.getParent();

        Composite s = (Composite) pscModel.getWidget();
        s.layout(true, true);
    }

    @Override
    public Object createWidget(final MUIElement element, Object parent) {
        MUIElement elementParent = element.getParent();
        if (elementParent == null && element.getCurSharedRef() != null)
            elementParent = element.getCurSharedRef();

        if (elementParent != null && elementParent.getRenderer() == this) {
            Rectangle newRect = new Rectangle(0, 0, 0, 0);

            // If my layout's container gets disposed 'unbind' the sash elements
            if (parent instanceof Composite) {
                ((Composite) parent).addDisposeListener(new DisposeListener() {
                    public void widgetDisposed(DisposeEvent e) {
                        element.setWidget(null);
                        element.setRenderer(null);
                    }
                });
            }
            return newRect;
        }

        // Check to see if this is a new PSC added 'above' the previous 'root'
        // sash
        Composite sashComposite = null;
        MPartSashContainer psc = (MPartSashContainer) element;
        for (MPartSashContainerElement psce : psc.getChildren()) {
            if (psce instanceof MPartSashContainer
                    && psce.getWidget() instanceof Composite) {
                // 'Adopt' the previous root's layout / composite
                sashComposite = (Composite) psce.getWidget();
                bindWidget(psce, new Rectangle(0, 0, 0, 0));
            }
        }
        // This is a 'root' sash container, create a composite
        if (sashComposite == null)
            sashComposite = new Composite((Composite) parent, SWT.NONE);
        sashComposite.setLayout(new XSashLayout(element));

        return sashComposite;
    }

    @Override
    public void childRendered(MElementContainer<MUIElement> parentElement,
            MUIElement element) {
        super.childRendered(parentElement, element);

        // Ensure that the element's 'containerInfo' is initialized
        int weight = getWeight(element);
        if (weight == UNDEFINED_WEIGHT) {
            element.setContainerData(Integer.toString(DEFAULT_WEIGHT));
        }
        forceLayout(parentElement);
    }

    @Override
    public void processContents(MElementContainer<MUIElement> container) {
        try {
            processedContent++;
            super.processContents(container);
        } finally {
            processedContent--;
            if (processedContent == 0) {
                forceLayout(container);
            }
        }
    }

    @Override
    public void hideChild(MElementContainer<MUIElement> parentElement,
            MUIElement child) {
        super.hideChild(parentElement, child);

        forceLayout(parentElement);
    }

    @Override
    public Object getUIContainer(MUIElement element) {
        // OK, find the 'root' of the sash container
        MUIElement parentElement = element.getParent();
        while (parentElement.getRenderer() == this
                && !(parentElement.getWidget() instanceof Composite))
            parentElement = parentElement.getParent();

        if (parentElement.getWidget() instanceof Composite)
            return parentElement.getWidget();

        return null;
    }

    private static int getWeight(MUIElement element) {
        String info = element.getContainerData();
        if (info == null || info.length() == 0) {
            element.setContainerData(Integer.toString(10000));
            info = element.getContainerData();
        }

        try {
            int value = Integer.parseInt(info);
            return value;
        } catch (NumberFormatException e) {
            return UNDEFINED_WEIGHT;
        }
    }
}
