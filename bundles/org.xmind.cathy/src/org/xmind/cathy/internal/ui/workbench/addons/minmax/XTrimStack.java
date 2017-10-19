package org.xmind.cathy.internal.ui.workbench.addons.minmax;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.internal.workbench.swt.CSSRenderingUtils;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MGenericStack;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.renderers.swt.TrimmedPartLayout;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * Class for representing window trim containing minimized views and shared
 * areas
 */
public class XTrimStack {

    /**
     * Contribution URI for this class
     */
    public static String CONTRIBUTION_URI_XTRIMSTACK = "bundleclass://org.xmind.cathy/org.xmind.cathy.internal.ui.workbench.addons.minmax.XTrimStack"; //$NON-NLS-1$

    private ToolBar trimStackTB;

    private MUIElement mGenericStack;

    /**
     * A map of created images from a part's icon URI path.
     */
    private Map<String, Image> imageMap = new HashMap<String, Image>();

    // Listens to ESC and closes the active fast view
    private Listener escapeListener = new Listener() {
        public void handleEvent(Event event) {
            if (event.character == SWT.ESC) {
                setStateForShowStack(false);
                partService.requestActivation();
            }
        }
    };

    @Inject
    EModelService modelService;

    @Inject
    EPartService partService;

    @Inject
    MWindow window;

    @Inject
    MToolControl toolControl;

    @Inject
    protected IEventBroker eventBroker;

    @SuppressWarnings("unchecked")
    @Inject
    @Optional
    private void handleTransientDataEvents(
            @UIEventTopic(UIEvents.ApplicationElement.TOPIC_TRANSIENTDATA) org.osgi.service.event.Event event) {
        // Prevent exceptions on shutdown
        if (trimStackTB == null || trimStackTB.isDisposed())
            return;

        Object changedElement = event.getProperty(UIEvents.EventTags.ELEMENT);
        if (!(changedElement instanceof MUIElement)) {
            return;
        }

        String key;
        if (UIEvents.isREMOVE(event)) {
            key = ((Entry<String, Object>) event
                    .getProperty(UIEvents.EventTags.OLD_VALUE)).getKey();
        } else {
            key = ((Entry<String, Object>) event
                    .getProperty(UIEvents.EventTags.NEW_VALUE)).getKey();
        }

        if (key.equals(IPresentationEngine.OVERRIDE_ICON_IMAGE_KEY)) {
            ToolItem toolItem = getChangedToolItem((MUIElement) changedElement);
            if (toolItem != null)
                toolItem.setImage(getImage((MUILabel) toolItem.getData()));
        } else if (key
                .equals(IPresentationEngine.OVERRIDE_TITLE_TOOL_TIP_KEY)) {
            ToolItem toolItem = getChangedToolItem((MUIElement) changedElement);
            if (toolItem != null)
                toolItem.setToolTipText(
                        getLabelText((MUILabel) toolItem.getData()));
        }
    }

    private ToolItem getChangedToolItem(MUIElement changedElement) {
        ToolItem[] toolItems = trimStackTB.getItems();
        for (ToolItem toolItem : toolItems) {
            if (changedElement.equals(toolItem.getData())) {
                return toolItem;
            }
        }
        return null;
    }

    // Listener attached to every ToolItem in a TrimStack. Responsible for activating the
    // appropriate part.
    private SelectionListener toolItemSelectionListener = new SelectionListener() {
        public void widgetSelected(SelectionEvent e) {
            ToolItem toolItem = (ToolItem) e.widget;
            MUIElement uiElement = (MUIElement) toolItem.getData();

            // Clicking on the already showing item ? NOTE: the selection will already have been
            // turned off by the time the event arrives
            if (!toolItem.getSelection()) {
                partService.requestActivation();
                setStateForShowStack(false);
                return;
            }

            if (uiElement instanceof MPart) {
                partService.activate((MPart) uiElement);
            } else if (uiElement instanceof MPerspective) {
                uiElement.getParent().setSelectedElement(uiElement);
            }
            setStateForShowStack(true);
        }

        public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
        }
    };

    /**
     * Add or remove someone of MGenericStack's children
     * 
     * @param event
     */
    @Inject
    @Optional
    private void handleChildrenEvents(
            @UIEventTopic(UIEvents.ElementContainer.TOPIC_CHILDREN) org.osgi.service.event.Event event) {

        if (mGenericStack == null || trimStackTB == null)
            return;

        Object changedObj = event.getProperty(UIEvents.EventTags.ELEMENT);

        if (changedObj == mGenericStack) {
            trimStackTB.getDisplay().asyncExec(new Runnable() {
                public void run() {
                    updateTrimStackItems();
                }
            });
        }
    }

    /**
     * Someone of MGenericStack's children changes state
     * 
     * @param event
     */
    @Inject
    @Optional
    private void handleToBeRenderedEvents(
            @UIEventTopic(UIEvents.UIElement.TOPIC_TOBERENDERED) org.osgi.service.event.Event event) {

        if (mGenericStack == null || trimStackTB == null)
            return;

        MUIElement changedElement = (MUIElement) event
                .getProperty(UIEvents.EventTags.ELEMENT);

        MUIElement parentElement = changedElement.getParent();
        if (parentElement == mGenericStack) {
            trimStackTB.getDisplay().asyncExec(new Runnable() {
                public void run() {
                    updateTrimStackItems();
                }
            });
        }
    }

    /**
     * Handle changes in tags of stack
     * 
     * @param event
     */
    @Inject
    @Optional
    private void subscribeTopicTagsChanged(
            @UIEventTopic(UIEvents.ApplicationElement.TOPIC_TAGS) org.osgi.service.event.Event event) {
        Object changedObj = event.getProperty(EventTags.ELEMENT);
        if (changedObj != mGenericStack)
            return;

        if (UIEvents.isADD(event)) {
            if (UIEvents.contains(event, UIEvents.EventTags.NEW_VALUE,
                    MinMaxAddon.STACK_HIDDEN)) {
                fixToolItemSelection();
            } else if (UIEvents.contains(event, UIEvents.EventTags.NEW_VALUE,
                    MinMaxAddon.STACK_VISIBLE)) {
                fixToolItemSelection();
            }
        }
    }

    @Inject
    @Optional
    private void handleWidgeEvents(
            @UIEventTopic(UIEvents.UIElement.TOPIC_WIDGET) org.osgi.service.event.Event event) {
        Object changedObj = event.getProperty(UIEvents.EventTags.ELEMENT);
        if (changedObj != mGenericStack)
            return;

        if (mGenericStack.getWidget() != null) {
            trimStackTB.getDisplay().asyncExec(new Runnable() {
                public void run() {
                    updateTrimStackItems();
                }
            });
        }
    }

    /**
     * Someone of MGenericStack's children was selected
     * 
     * @param event
     */
    @Inject
    @Optional
    private void handleBringToTopEvents(
            @UIEventTopic(UIEvents.UILifeCycle.BRINGTOTOP) org.osgi.service.event.Event event) {
        MUIElement changedElement = (MUIElement) event
                .getProperty(UIEvents.EventTags.ELEMENT);

        // Open if shared area
        if (getLeafPart(mGenericStack) == changedElement
                && !(mGenericStack instanceof MPerspectiveStack)) {
            setStateForShowStack(true);
            return;
        }

        MUIElement selectedElement = null;

        if (mGenericStack instanceof MPlaceholder) {
            selectedElement = ((MPlaceholder) mGenericStack).getRef();
        } else if (mGenericStack instanceof MPartStack) {
            selectedElement = ((MPartStack) mGenericStack).getSelectedElement();
        }

        if (selectedElement == null)
            return;

        if (selectedElement instanceof MPlaceholder)
            selectedElement = ((MPlaceholder) selectedElement).getRef();

        if (changedElement != selectedElement)
            return;

        setStateForShowStack(true);
    }

    @Inject
    @Optional
    private void handleAppShutDownAndStartedEvents(
            @UIEventTopic(UIEvents.UILifeCycle.APP_SHUTDOWN_STARTED) org.osgi.service.event.Event event) {
        setStateForShowStack(false);
    }

    @PostConstruct
    void createWidget(Composite parent, MToolControl me,
            CSSRenderingUtils cssUtils) {
        if (mGenericStack == null) {
            mGenericStack = findElement();
        }

        MUIElement meParent = me.getParent();
        int orientation = SWT.HORIZONTAL;
        if (meParent instanceof MTrimBar) {
            MTrimBar bar = (MTrimBar) meParent;
            if (bar.getSide() == SideValue.RIGHT
                    || bar.getSide() == SideValue.LEFT)
                orientation = SWT.VERTICAL;
        }
        trimStackTB = new ToolBar(parent, orientation | SWT.FLAT | SWT.WRAP);
        trimStackTB.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                setStateForShowStack(false);

                trimStackTB = null;
            }
        });

        updateTrimStackItems();
    }

    @PreDestroy
    void destroy() {
        for (Image image : imageMap.values()) {
            image.dispose();
        }
    }

    public MUIElement getMinimizedElement() {
        return mGenericStack;
    }

    //Find stack
    private MUIElement findElement() {
        MUIElement result;
        List<MPerspectiveStack> ps = modelService.findElements(window, null,
                MPerspectiveStack.class, null);
        if (ps.size() == 0) {
            String toolControlId = toolControl.getElementId();
            int index = toolControlId.indexOf('(');
            String stackId = toolControlId.substring(0, index);
            result = modelService.find(stackId, window);
        } else {
            String toolControlId = toolControl.getElementId();
            int index = toolControlId.indexOf('(');
            String stackId = toolControlId.substring(0, index);
            String perspId = toolControlId.substring(index + 1,
                    toolControlId.length() - 1);

            MPerspective persp = null;
            List<MPerspective> perspectives = modelService
                    .findElements(ps.get(0), perspId, MPerspective.class, null);
            if (perspectives != null && !perspectives.isEmpty()) {
                persp = perspectives.get(0);
            }

            if (persp != null) {
                result = modelService.find(stackId, persp);
            } else {
                result = modelService.find(stackId, window);
            }
        }

        return result;
    }

    private String getLabelText(MUILabel label) {
        // Use override text if available
        if (label instanceof MUIElement) {
            String text = getOverrideTitleToolTip((MUIElement) label);
            if (text != null && text.length() > 0)
                return text;
        }

        String string = label.getLocalizedLabel();
        return string == null ? "" : string; //$NON-NLS-1$
    }

    private Image getImage(MUILabel element) {
        // Use override image if available
        if (element instanceof MUIElement) {
            Image image = getOverrideImage((MUIElement) element);
            if (image != null)
                return image;
        }

        String iconURI = element.getIconURI();
        if (iconURI != null && iconURI.length() > 0) {
            Image image = imageMap.get(iconURI);
            if (image == null) {
                image = imageDescriptorFromURI(iconURI).createImage();
                imageMap.put(iconURI, image);
            }
            return image;
        }

        return null;
    }

    public ImageDescriptor imageDescriptorFromURI(String iconPath) {
        try {
            URI uri = new URI(iconPath);
            return ImageDescriptor.createFromURL(new URL(uri.toString()));
        } catch (MalformedURLException e) {
        } catch (URISyntaxException e) {
        }
        return null;
    }

    private MUILabel getLabelElement(MUIElement element) {
        if (element instanceof MPlaceholder)
            element = ((MPlaceholder) element).getRef();

        return (MUILabel) (element instanceof MUILabel ? element : null);
    }

    private void updateTrimStackItems() {
        // Prevent exceptions on shutdown
        if (trimStackTB == null || trimStackTB.isDisposed())
            return;

        while (trimStackTB.getItemCount() > 0) {
            trimStackTB.getItem(trimStackTB.getItemCount() - 1).dispose();
        }

        if (mGenericStack instanceof MPlaceholder) {
            MPlaceholder ph = (MPlaceholder) mGenericStack;
            if (ph.getRef() instanceof MPart) {
                MPart part = (MPart) ph.getRef();
                ToolItem ti = new ToolItem(trimStackTB, SWT.CHECK);
                ti.setData(part);
                ti.setImage(getImage(part));
                ti.setToolTipText(getLabelText(part));
                ti.addSelectionListener(toolItemSelectionListener);
            }
        } else if (mGenericStack instanceof MGenericStack<?>) {
            // Handle *both* PartStacks and PerspectiveStacks here...
            MGenericStack<?> theStack = (MGenericStack<?>) mGenericStack;

            for (MUIElement stackElement : theStack.getChildren()) {
                boolean trimStackExists = stackElement.getTags()
                        .contains(MinMaxAddon.TRIM_STACK_EXIST);
                if (stackElement instanceof MPlaceholder) {
                    MUIElement part = ((MPlaceholder) stackElement).getRef();
                    trimStackExists = part.getTags()
                            .contains(MinMaxAddon.TRIM_STACK_EXIST);
                }
                if (/* stackElement.isToBeRendered() || FIXME */ trimStackExists) {
                    MUILabel labelElement = getLabelElement(stackElement);
                    ToolItem newItem = new ToolItem(trimStackTB, SWT.CHECK);
                    newItem.setData(labelElement);
                    newItem.setImage(getImage(labelElement));
                    newItem.setToolTipText(getLabelText(labelElement));
                    newItem.addSelectionListener(toolItemSelectionListener);
                }
            }
        }

        trimStackTB.pack();
        trimStackTB.getShell().layout(new Control[] { trimStackTB }, SWT.DEFER);
        fixToolItemSelection();
    }

    /**
     * Sets whether this stack should be visible or hidden
     *
     * @param show
     *            whether the stack should be visible
     */
    public void setStateForShowStack(boolean show) {
        Control ctrl = (Control) mGenericStack.getWidget();

        Composite clientAreaComposite = getCAComposite();
        if (clientAreaComposite == null || clientAreaComposite.isDisposed())
            return;

        if (show) {
            mGenericStack.getTags().remove(MinMaxAddon.STACK_HIDDEN);
            mGenericStack.getTags().remove(MinMaxAddon.STACK_VISIBLE);
            mGenericStack.getTags().add(MinMaxAddon.STACK_VISIBLE);

            ctrl.removeListener(SWT.Traverse, escapeListener);
            ctrl.addListener(SWT.Traverse, escapeListener);
        } else {
            if (ctrl != null && !ctrl.isDisposed())
                ctrl.removeListener(SWT.Traverse, escapeListener);
            mGenericStack.getTags().remove(MinMaxAddon.STACK_HIDDEN);
            mGenericStack.getTags().remove(MinMaxAddon.STACK_VISIBLE);
            mGenericStack.getTags().add(MinMaxAddon.STACK_HIDDEN);
        }
    }

    private void fixToolItemSelection() {
        if (trimStackTB == null || trimStackTB.isDisposed())
            return;

        boolean toHide = mGenericStack.getTags()
                .contains(MinMaxAddon.STACK_HIDDEN);
        boolean toShow = mGenericStack.getTags()
                .contains(MinMaxAddon.STACK_VISIBLE);
        if (toHide) {
            // Not open...no selection
            for (ToolItem item : trimStackTB.getItems()) {
                item.setSelection(false);
            }
        } else if (toShow) {
            if (mGenericStack instanceof MPlaceholder) {
                trimStackTB.getItem(1).setSelection(true);
            } else if (isPerspectiveStack()) {
                MPerspectiveStack pStack = (MPerspectiveStack) mGenericStack;
                MUIElement selElement = pStack.getSelectedElement();
                for (ToolItem item : trimStackTB.getItems()) {
                    item.setSelection(item.getData() == selElement);
                }
            } else {
                MPartStack partStack = (MPartStack) mGenericStack;
                MUIElement selElement = partStack.getSelectedElement();
                if (selElement instanceof MPlaceholder)
                    selElement = ((MPlaceholder) selElement).getRef();

                for (ToolItem item : trimStackTB.getItems()) {
                    boolean isSel = item.getData() == selElement;
                    item.setSelection(isSel);
                }
            }
        }
    }

    private boolean isPerspectiveStack() {
        return mGenericStack instanceof MPerspectiveStack;
    }

    private MPart getLeafPart(MUIElement element) {
        if (element instanceof MPlaceholder)
            return getLeafPart(((MPlaceholder) element).getRef());

        if (element instanceof MElementContainer<?>)
            return getLeafPart(
                    ((MElementContainer<?>) element).getSelectedElement());

        if (element instanceof MPart)
            return (MPart) element;

        return null;
    }

    private Composite getCAComposite() {
        if (trimStackTB == null)
            return null;

        // Get the shell's client area composite
        Shell theShell = trimStackTB.getShell();
        if (theShell.getLayout() instanceof TrimmedPartLayout) {
            TrimmedPartLayout tpl = (TrimmedPartLayout) theShell.getLayout();
            if (!tpl.clientArea.isDisposed())
                return tpl.clientArea;
        }
        return null;
    }

    private Image getOverrideImage(MUIElement element) {
        Image result = null;

        Object imageObject = element.getTransientData()
                .get(IPresentationEngine.OVERRIDE_ICON_IMAGE_KEY);
        if (imageObject != null && imageObject instanceof Image
                && !((Image) imageObject).isDisposed())
            result = (Image) imageObject;
        return result;
    }

    private String getOverrideTitleToolTip(MUIElement element) {
        String result = null;

        Object stringObject = element.getTransientData()
                .get(IPresentationEngine.OVERRIDE_TITLE_TOOL_TIP_KEY);
        if (stringObject != null && stringObject instanceof String)
            result = (String) stringObject;

        if (result == null || result.length() == 0)
            return null;

        if (element instanceof MUILabel) {
            String label = ((MUILabel) element).getLocalizedLabel();
            if (label != null && label.length() > 0) {
                result = label + ' ' + '(' + result + ')';
            }
        }

        return result;
    }

}
