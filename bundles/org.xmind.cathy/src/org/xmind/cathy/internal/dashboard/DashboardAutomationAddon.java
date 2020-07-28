
package org.xmind.cathy.internal.dashboard;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.workbench.Selector;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.osgi.service.event.Event;
import org.xmind.cathy.internal.HandledItemMatcher;
import org.xmind.cathy.internal.ICathyConstants;
import org.xmind.cathy.internal.WorkbenchMessages;

public class DashboardAutomationAddon {

    @Inject
    private EModelService modelService;

    @Inject
    private MApplication application;

    private Selector itemMatcher = new HandledItemMatcher(
            ICathyConstants.COMMAND_TOGGLE_DASHBOARD);

    /**
     * @param modelService
     *            the modelService to set
     */
    public void setModelService(EModelService modelService) {
        this.modelService = modelService;
    }

    /**
     * @param application
     *            the application to set
     */
    public void setApplication(MApplication application) {
        this.application = application;
    }

    public void showDashboard(MWindow window) {
        if (doShowDashboard(window)) {
            updateDashboardToolItems(window);
        }

        //hide right parts.
        hideVisiblePart(window, "org.xmind.ui.stack.right"); //$NON-NLS-1$
    }

    @Inject
    @Optional
    public void updateDashboardVisibilityWhenWindowTagsChanged(
            @EventTopic(UIEvents.ApplicationElement.TOPIC_TAGS) Event event) {
        Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
        if (!(element instanceof MWindow) || !ICathyConstants.ID_MAIN_WINDOW
                .equals(((MWindow) element).getElementId()))
            return;

        MWindow window = (MWindow) element;
        if (UIEvents.EventTypes.ADD
                .equals(event.getProperty(UIEvents.EventTags.TYPE))
                && ICathyConstants.TAG_SHOW_DASHBOARD.equals(
                        event.getProperty(UIEvents.EventTags.NEW_VALUE))) {
            if (!doShowDashboard(window)) {
                window.getTags().remove(ICathyConstants.TAG_SHOW_DASHBOARD);
                return;
            }
        } else if (UIEvents.EventTypes.REMOVE
                .equals(event.getProperty(UIEvents.EventTags.TYPE))
                && ICathyConstants.TAG_SHOW_DASHBOARD.equals(
                        event.getProperty(UIEvents.EventTags.OLD_VALUE))) {
            if (!doHideDashboard(window)) {
                window.getTags().add(ICathyConstants.TAG_SHOW_DASHBOARD);
                return;
            }
        }

        updateDashboardToolItems(window);
    }

    private boolean doShowDashboard(MWindow window) {
        MPart dashboardPart = findReferencedDashboardPartIn(window, true);
        if (dashboardPart == null)
            return false;

        EPartService partService = window.getContext().get(EPartService.class);
        if (partService == null)
            return false;

        partService.activate(dashboardPart, true);
        return partService.getActivePart() == dashboardPart;
    }

    private boolean doHideDashboard(MWindow window) {
        MPart dashboardPart = findReferencedDashboardPartIn(window, false);
        if (dashboardPart == null)
            return true;

        EPartService partService = window.getContext().get(EPartService.class);
        if (partService == null)
            return false;

        partService.hidePart(dashboardPart);
        return partService.getActivePart() != dashboardPart;
    }

    @Inject
    @Optional
    public void updateWindowTagsWhenDashboardVisibilityChanged(
            @EventTopic(UIEvents.ElementContainer.TOPIC_SELECTEDELEMENT) Event event) {
        Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
        if (!(element instanceof MElementContainer))
            return;

        MElementContainer container = (MElementContainer) element;
        final MWindow window = modelService.getTopLevelWindowFor(container);
        if (window == null || !ICathyConstants.ID_MAIN_WINDOW
                .equals(window.getElementId()))
            return;

        MPart activePart = findSelectedElementIn(window, MPart.class);
        if (activePart != null && ICathyConstants.ID_DASHBOARD_PART
                .equals(activePart.getElementId())) {
            /*
             * The Dashboard is shown, we must ensure that the
             * CathyShowDashboard tag is added to window.
             */
            if (!window.getTags()
                    .contains(ICathyConstants.TAG_SHOW_DASHBOARD)) {
                window.getTags().add(ICathyConstants.TAG_SHOW_DASHBOARD);
            }
        } else {
            /*
             * The Dashboard is about to be hidden, we must ensure that the
             * CathyShowDashboard tag is removed from the window.
             */
            if (window.getTags().contains(ICathyConstants.TAG_SHOW_DASHBOARD)) {
                window.getTags().remove(ICathyConstants.TAG_SHOW_DASHBOARD);
            }
        }
    }

    @Inject
    @Optional
    public void showDashboardWhenAllEditorsAreRemoved(
            @EventTopic(UIEvents.ElementContainer.TOPIC_CHILDREN) Event event) {
        if (!UIEvents.isREMOVE(event))
            return;

        Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
        if (!(element instanceof MElementContainer))
            return;

        boolean partRemoved = false;
        for (Object removed : UIEvents.asIterable(event,
                UIEvents.EventTags.OLD_VALUE)) {
            if (removed instanceof MPart) {
                partRemoved = true;
                break;
            }
        }

        if (!partRemoved)
            return;

        MWindow window = modelService
                .getTopLevelWindowFor((MUIElement) element);
        if (window == null || !ICathyConstants.ID_MAIN_WINDOW
                .equals(window.getElementId()))
            return;

        List<MPart> editors = modelService.findElements(window, null,
                MPart.class, Arrays.asList(ICathyConstants.TAG_EDITOR));
        if (!editors.isEmpty())
            return;

        //hide right parts.
        hideVisiblePart(window, "org.xmind.ui.stack.right"); //$NON-NLS-1$

//        if (!window.getTags().contains(ICathyConstants.TAG_SHOW_DASHBOARD)) {
//            window.getTags().add(ICathyConstants.TAG_SHOW_DASHBOARD);
//        }
    }

    private MPart findReferencedDashboardPartIn(MWindow window,
            boolean createIfMissing) {
        MPart dashboardPart = null;

        /*
         * Find Dashboard instance in window model tree.
         */
        List<MPart> dashboardParts = modelService.findElements(window,
                ICathyConstants.ID_DASHBOARD_PART, MPart.class, null);
        if (!dashboardParts.isEmpty()) {
            dashboardPart = dashboardParts.get(0);
        } else {
            /*
             * Find Dashboard instance in shared elements.
             */
            for (MUIElement p : window.getChildren()) {
                if (p instanceof MPart && ICathyConstants.ID_DASHBOARD_PART
                        .equals(p.getElementId())) {
                    dashboardPart = (MPart) p;
                    break;
                }
            }
        }

        if (dashboardPart == null && createIfMissing) {
            /*
             * Create Dashboard part from snippet.
             */
            MUIElement part = modelService.cloneSnippet(application,
                    ICathyConstants.ID_DASHBOARD_PART, window);
            if (part != null && part instanceof MPart
                    && ICathyConstants.ID_DASHBOARD_PART
                            .equals(part.getElementId())) {
                dashboardPart = (MPart) part;
                window.getChildren().add(dashboardPart);
            }
        }

        return dashboardPart;
    }

    private <T extends MUIElement> T findSelectedElementIn(MUIElement root,
            Class<T> type) {
        if (type.isInstance(root))
            return type.cast(root);

        if (root instanceof MPlaceholder)
            return findSelectedElementIn(((MPlaceholder) root).getRef(), type);

        if (root instanceof MElementContainer)
            return findSelectedElementIn(
                    ((MElementContainer) root).getSelectedElement(), type);

        return null;
    }

    private void updateDashboardToolItems(MWindow window) {
        String tooltip;
        boolean selected;
        if (window.getTags().contains(ICathyConstants.TAG_SHOW_DASHBOARD)) {
            tooltip = WorkbenchMessages.DashboardHideHome_tooltip;
            selected = true;
        } else {
            tooltip = WorkbenchMessages.DashboardShowHome_tooltip;
            selected = false;
        }

        List<MHandledItem> items = modelService.findElements(window,
                MHandledItem.class, EModelService.ANYWHERE, itemMatcher);
        for (MHandledItem item : items) {
            item.setTooltip(tooltip);
            item.setSelected(selected);
        }
    }

    public static final String hideVisiblePart(MWindow window,
            String partStackId) {
        if (window == null || window.getContext() == null
                || partStackId == null) {
            return null;
        }

        EModelService modelService = window.getContext()
                .get(EModelService.class);
        EPartService partService = window.getContext().get(EPartService.class);

        List<MPartStack> partStacks = modelService.findElements(window,
                partStackId, MPartStack.class, null);
        if (partStacks.isEmpty()) {
            return null;
        }
        MPartStack partStack = partStacks.get(0);

        MStackElement selectedElement = partStack.getSelectedElement();
        String hidePartId = hidePart(partService, selectedElement);
        if (hidePartId != null) {
            return hidePartId;
        }

        //fix: part may not be hiden
        List<MStackElement> children = partStack.getChildren();
        for (MStackElement child : children) {
            hidePart(partService, child);
        }

        return null;
    }

    private static String hidePart(EPartService partService,
            MStackElement element) {
        MPart visiblePart = null;
        if (element instanceof MPlaceholder) {
            MPlaceholder placeholder = (MPlaceholder) element;
            visiblePart = partService.findPart(placeholder.getElementId());
        } else if (element instanceof MPart) {
            visiblePart = (MPart) element;
        }

        if (visiblePart != null) {
            visiblePart.setVisible(false);
            partService.hidePart(visiblePart);
            return visiblePart.getElementId();
        }

        return null;
    }

}
