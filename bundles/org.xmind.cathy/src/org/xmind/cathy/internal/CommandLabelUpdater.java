
package org.xmind.cathy.internal;

import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MItem;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.SubActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.osgi.service.event.Event;

public class CommandLabelUpdater implements IPropertyChangeListener {

    private static final int TEXT = 1 << 1;
    private static final int TOOLTIP = 1 << 2;

    private static final String DATA_ORIGINAL_TEXT = "CommandLabelUpdater:OriginalText"; //$NON-NLS-1$
    private static final String DATA_ORIGINAL_TOOLTIP = "CommandLabelUpdater:OriginalTooltip"; //$NON-NLS-1$
    private static final String WEAK_VALUE_PLACEHOLDER = "WeakValuePlaceHolder"; //$NON-NLS-1$

    @Inject
    private EModelService modelService;

    private MWindow activeWindow = null;
    private WeakHashMap<IActionBars, String> activeActionBarsWeakRef = new WeakHashMap<IActionBars, String>();
    private Set<IAction> trackedHandlers = null;

    @Inject
    @Optional
    public void applicationStarted(
            @EventTopic(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE) Event event) {
    }

    @Inject
    @Optional
    public void activePartChanged(
            @EventTopic(UIEvents.UILifeCycle.ACTIVATE) Event event) {
        Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
        if (!(element instanceof MPart))
            return;

        MPart part = (MPart) element;
        MWindow window = findWindowFor(part);
        if (window == null)
            return;

        partActivated(part, window);
    }

    private void partActivated(MPart part, MWindow window) {
        IWorkbenchWindow ww = window.getContext().get(IWorkbenchWindow.class);
        IActionBars actionBars = findActionBars(ww);

        this.activeWindow = window;
        Object[] bars = activeActionBarsWeakRef.keySet().toArray();
        IActionBars activeActionBars = bars.length > 0 ? (IActionBars) bars[0]
                : null;

        if (actionBars != activeActionBars) {
            if (activeActionBars instanceof SubActionBars) {
                ((SubActionBars) activeActionBars)
                        .removePropertyChangeListener(this);
            }
            activeActionBarsWeakRef.put(actionBars, WEAK_VALUE_PLACEHOLDER);
            if (actionBars instanceof SubActionBars) {
                ((SubActionBars) actionBars).addPropertyChangeListener(this);
            }

            updateAllItemLabels();
        }
    }

    private void updateAllItemLabels() {
        if (activeWindow == null)
            return;

        Set<IAction> oldTrackedHandlers = this.trackedHandlers;
        Set<IAction> newTrackedHandlers = new HashSet<IAction>();

        Object[] bars = activeActionBarsWeakRef.keySet().toArray();
        IActionBars activeActionBars = bars.length > 0 ? (IActionBars) bars[0]
                : null;

        updateItemLabel(activeActionBars, activeWindow.getMainMenu(),
                ICathyConstants.ID_MENU_ITEM_UNDO, ActionFactory.UNDO.getId(),
                ActionFactory.UNDO.getCommandId(), TEXT, newTrackedHandlers);
        updateItemLabel(activeActionBars, activeWindow.getMainMenu(),
                ICathyConstants.ID_MENU_ITEM_REDO, ActionFactory.REDO.getId(),
                ActionFactory.REDO.getCommandId(), TEXT, newTrackedHandlers);

        updateItemLabel(activeActionBars, activeWindow,
                ICathyConstants.ID_TOOL_ITEM_UNDO, ActionFactory.UNDO.getId(),
                ActionFactory.UNDO.getCommandId(), TOOLTIP, newTrackedHandlers);
        updateItemLabel(activeActionBars, activeWindow,
                ICathyConstants.ID_TOOL_ITEM_REDO, ActionFactory.REDO.getId(),
                ActionFactory.REDO.getCommandId(), TOOLTIP, newTrackedHandlers);

        this.trackedHandlers = newTrackedHandlers;

        if (oldTrackedHandlers != null) {
            for (IAction handler : oldTrackedHandlers) {
                if (!newTrackedHandlers.contains(handler)) {
                    handler.removePropertyChangeListener(this);
                }
            }
        }
        for (IAction handler : newTrackedHandlers) {
            if (oldTrackedHandlers == null
                    || !oldTrackedHandlers.contains(handler)) {
                handler.addPropertyChangeListener(this);
            }
        }
    }

    private void updateItemLabel(IActionBars actionBars, MUIElement topElement,
            String itemId, String actionId, String commandId, int attributes,
            Set<IAction> handlers) {
        if (modelService == null || topElement == null)
            return;

        MUIElement element = modelService.find(itemId, topElement);
        if (!(element instanceof MItem))
            return;

        MItem item = (MItem) element;

        IAction handler = actionBars == null ? null
                : actionBars.getGlobalActionHandler(actionId);

        if ((attributes & TEXT) != 0) {
            String text = handler == null ? null : handler.getText();
            if (text != null) {
                if (!item.getTransientData().containsKey(DATA_ORIGINAL_TEXT)) {
                    item.getTransientData().put(DATA_ORIGINAL_TEXT,
                            item.getLabel());
                }
                item.setLabel(text);
            } else {
                Object originalText = item.getTransientData()
                        .get(DATA_ORIGINAL_TEXT);
                if (originalText != null && originalText instanceof String) {
                    item.setLabel((String) originalText);
                }
            }
        }
        if ((attributes & TOOLTIP) != 0) {
            String tooltip = handler == null ? null : handler.getToolTipText();
            if (tooltip != null) {
                if (!item.getTransientData()
                        .containsKey(DATA_ORIGINAL_TOOLTIP)) {
                    item.getTransientData().put(DATA_ORIGINAL_TOOLTIP,
                            item.getTooltip());
                }
                item.setTooltip(tooltip);
            } else {
                Object originalTooltip = item.getTransientData()
                        .get(DATA_ORIGINAL_TOOLTIP);
                if (originalTooltip != null
                        && originalTooltip instanceof String) {
                    item.setTooltip((String) originalTooltip);
                }
            }
        }

    }

    private MWindow findWindowFor(MUIElement element) {
        if (element == null)
            return null;
        if (element instanceof MWindow)
            return (MWindow) element;
        MPlaceholder placeholder = element.getCurSharedRef();
        if (placeholder != null)
            return findWindowFor(placeholder);
        MUIElement parent = element.getParent();
        if (parent != null)
            return findWindowFor(parent);
        return null;
    }

    private IActionBars findActionBars(IWorkbenchWindow window) {
        if (window == null)
            return null;

        IWorkbenchPage page = window.getActivePage();
        if (page == null)
            return null;

        IWorkbenchPart activePart = page.getActivePart();
        if (activePart == null)
            return null;

        IWorkbenchPartSite site = activePart.getSite();
        if (site instanceof IEditorSite)
            return ((IEditorSite) site).getActionBars();
        if (site instanceof IViewSite)
            return ((IViewSite) site).getActionBars();
        return null;
    }

    public void propertyChange(PropertyChangeEvent event) {
        updateAllItemLabels();
    }

}
