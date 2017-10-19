package org.xmind.cathy.internal.renderer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindowElement;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.renderers.swt.CSSEngineHelper;
import org.eclipse.e4.ui.workbench.renderers.swt.TrimmedPartLayout;
import org.eclipse.e4.ui.workbench.renderers.swt.WBWRenderer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.event.Event;

public class XWBWRenderer extends WBWRenderer {

    @Inject
    private Display display;

    @Inject
    private MApplication application;

    @Inject
    private EModelService modelService;

    /**
     * Hides all contents on startup, and will be set to true once the
     * application fully starts up.
     */
    private boolean showingContents = false;

    @Override
    public Object createWidget(MUIElement element, Object parent) {
        Object widget = super.createWidget(element, parent);
        if (widget != null && widget instanceof Shell) {
            recreateLayout(element, (Shell) widget);
        }

        // modify dnd manager's dropAgents & dragAgents for widget
        // (drop: remove SplitDropAgent2 & DetachedDropAgent & TrimDropAgent;  drag: remove IBFDragAgent)
        if (widget instanceof Shell && !((Shell) widget).isDisposed()) {
            Shell shell = (Shell) widget;
            Object theManager = shell.getData("DnDManager"); //$NON-NLS-1$
            if (theManager == null) {
                theManager = createDnDManager((MWindow) element);
            }

            if (theManager != null) {
                trimDndManager(theManager);
                shell.setData("DnDManager", theManager); //$NON-NLS-1$
            }
        }

        return widget;
    }

    private Object createDnDManager(MWindow window) {
        try {
            Class managerClass = Class.forName(
                    "org.eclipse.e4.ui.workbench.addons.dndaddon.DnDManager"); //$NON-NLS-1$
            Constructor constructor = (managerClass
                    .getDeclaredConstructors())[0];
            constructor.setAccessible(true);
            Object dndManager = constructor.newInstance(window);

            return dndManager;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void trimDndManager(Object theManager) {
        try {
            Class managerClass = theManager.getClass();

            //trim dropAgents
            Field dropField = managerClass.getDeclaredField("dropAgents"); //$NON-NLS-1$

            dropField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Object> dropAgents = (List<Object>) dropField.get(theManager);
            // remove SplitDropAgent2 & DetachedDropAgent & TrimDropAgent
            if (dropAgents.size() == 4) {
                dropAgents.remove(1);
                dropAgents.remove(1);
                dropAgents.remove(1);
            }

            dropField.set(theManager, dropAgents);

            //trim dragAgents
            Field dragField = managerClass.getDeclaredField("dragAgents"); //$NON-NLS-1$

            dragField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Object> dragAgents = (List<Object>) dragField.get(theManager);
            // remove IBFDragAgent
            if (dragAgents.size() == 2) {
                dragAgents.remove(1);
            }

            dragField.set(theManager, dragAgents);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void recreateLayout(MUIElement element, Shell shell) {
        Layout oldLayout = shell.getLayout();
        if (oldLayout != null && oldLayout instanceof TrimmedPartLayout) {
            TrimmedPartLayout trimmedLayout = (TrimmedPartLayout) oldLayout;
            if (trimmedLayout.clientArea != null)
                trimmedLayout.clientArea.dispose();
            if (trimmedLayout.top != null)
                trimmedLayout.top.dispose();
            if (trimmedLayout.bottom != null)
                trimmedLayout.bottom.dispose();
            if (trimmedLayout.left != null)
                trimmedLayout.left.dispose();
            if (trimmedLayout.right != null)
                trimmedLayout.right.dispose();
        }

        XTrimmedPartLayout layout = new XTrimmedPartLayout(shell);

        IEclipseContext localContext = getContext(element);
        // We need to retrieve specific CSS properties for our layout.
        CSSEngineHelper helper = new CSSEngineHelper(localContext, shell);
        layout.gutterTop = helper.getMarginTop(0);
        layout.gutterBottom = helper.getMarginBottom(0);
        layout.gutterLeft = helper.getMarginLeft(0);
        layout.gutterRight = helper.getMarginRight(0);

        shell.setLayout(layout);
    }

    @Override
    public Object getUIContainer(MUIElement element) {
        if (element instanceof MPart) {
            MUIElement parent = element.getParent();
            if (parent != null) {
                Object parentWidget = parent.getWidget();
                if (parentWidget instanceof Composite) {
                    Layout layout = ((Composite) parentWidget).getLayout();
                    if (layout instanceof XTrimmedPartLayout) {
                        return ((XTrimmedPartLayout) layout).getContainer(
                                (Composite) parentWidget, element);
                    }
                }
            }
        }
        return super.getUIContainer(element);
    }

    @Override
    public void postProcess(MUIElement shellME) {
        super.postProcess(shellME);

        MWindow window = (MWindow) shellME;
        MWindowElement selectedElement = window.getSelectedElement();
        if (selectedElement != null) {
            showChild(window, selectedElement);
        } else if (!window.getChildren().isEmpty()) {
            for (MWindowElement child : window.getChildren()) {
                if (child.isToBeRendered() && child.isVisible()) {
                    window.setSelectedElement(child);
                    break;
                }
            }
        }
    }

    private void showChild(MWindow window, MWindowElement selectedChild) {
        Shell shell = (Shell) window.getWidget();
        if (shell == null || shell.isDisposed())
            return;

        if (window.getChildren().isEmpty())
            return;

        for (MWindowElement child : window.getChildren()) {
            Object container = getUIContainer(child);
            if (container instanceof Control) {
                ((Control) container)
                        .setVisible(showingContents && child == selectedChild);
            }
        }

        Layout layout = shell.getLayout();
        if (layout instanceof TrimmedPartLayout) {
            TrimmedPartLayout tpl = (TrimmedPartLayout) layout;
            boolean primaryClientAreaVisible = tpl.clientArea != null
                    && tpl.clientArea.isVisible();
            if (tpl.top != null)
                tpl.top.setVisible(primaryClientAreaVisible);
            if (tpl.bottom != null)
                tpl.bottom.setVisible(primaryClientAreaVisible);
            if (tpl.left != null)
                tpl.left.setVisible(primaryClientAreaVisible);
            if (tpl.right != null)
                tpl.right.setVisible(primaryClientAreaVisible);
        }

        shell.layout(true);
    }

    @Inject
    @Optional
    public void subscribeTopicPartActivate(
            @EventTopic(UIEvents.ElementContainer.TOPIC_SELECTEDELEMENT) Event event) {
        Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
        if (element == null || !(element instanceof MWindow))
            return;

        MWindow window = (MWindow) element;
        if (window.getRenderer() != XWBWRenderer.this)
            return;

        showChild(window, window.getSelectedElement());
    }

    @Inject
    @Optional
    public void subscribeTopicChildrenRemoved(
            @EventTopic(UIEvents.ElementContainer.TOPIC_CHILDREN) Event event) {
        if (!UIEvents.isREMOVE(event))
            return;

        Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
        if (element == null || !(element instanceof MWindow))
            return;

        MWindow window = (MWindow) element;
        if (window.getRenderer() != XWBWRenderer.this
                || !(window.getWidget() instanceof Shell)
                || !(((Shell) window.getWidget())
                        .getLayout() instanceof XTrimmedPartLayout))
            return;

        XTrimmedPartLayout layout = (XTrimmedPartLayout) ((Shell) window
                .getWidget()).getLayout();
        for (Object removed : UIEvents.asIterable(event,
                UIEvents.EventTags.OLD_VALUE)) {
            layout.removeContainer(removed);
        }
    }

    /**
     * Once the application fully starts up, set the 'showingContents' flag to
     * true and show all window contents.
     * 
     * @param event
     *            the UI event of the topic
     *            {@link UIEvents.UILifeCycle#APP_STARTUP_COMPLETE}
     */
    @Inject
    @Optional
    public void applicationStarted(
            @EventTopic(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE) Event event) {
        showingContents = true;

        if (display == null)
            return;

        display.syncExec(new Runnable() {
            public void run() {
                List<MWindow> windows = modelService.findElements(application,
                        null, MWindow.class, null);
                for (MWindow window : windows) {
                    if (window.getRenderer() == XWBWRenderer.this) {
                        showChild(window, window.getSelectedElement());
                    }
                }
            }
        });
    }
}
