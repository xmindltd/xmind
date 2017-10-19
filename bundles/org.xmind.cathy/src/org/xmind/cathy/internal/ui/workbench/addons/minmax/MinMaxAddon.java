package org.xmind.cathy.internal.ui.workbench.addons.minmax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MGenericStack;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.osgi.service.event.Event;
import org.xmind.cathy.internal.ICathyConstants;
import org.xmind.ui.internal.e4models.IModelConstants;

/**
 * Workbench addon that provides methods to hide, show parts in the window
 */
public class MinMaxAddon {

    static final String ID_SUFFIX = "(minimized)"; //$NON-NLS-1$
    static final String STACK_VISIBLE = "StackVisible"; //$NON-NLS-1$
    static final String STACK_HIDDEN = "StackHidden"; //$NON-NLS-1$
    /*
     * TrimStack exist for PartStack
     */
    static final String TRIM_STACK_EXIST = "TrimStackExist"; //$NON-NLS-1$

    @Inject
    IEventBroker eventBroker;

    @Inject
    EModelService modelService;

    @Inject
    EPartService partService;

    @Inject
    private IEclipseContext context;

    @Inject
    MAddon minMaxAddon;

    @Inject
    MApplication application;

    private SelectionListener toolItemSelectionListener = new SelectionListener() {

        public void widgetSelected(SelectionEvent e) {
            widgetDefaultSelected(e);
        }

        public void widgetDefaultSelected(SelectionEvent e) {
            ToolItem selectedToolItem = (ToolItem) e.widget;
            MUIElement uiElement = (MUIElement) selectedToolItem
                    .getData(AbstractPartRenderer.OWNING_ME);

            if (uiElement.getTags() != null && uiElement.getTags()
                    .contains(IModelConstants.DIRECT_COMMAD_TAG))
                return;

            MUIElement rightTrimBar = modelService
                    .find(ICathyConstants.ID_TRIMBAR_RIGHT, application);
            if (rightTrimBar instanceof MTrimBar) {
                List<MTrimElement> children = ((MTrimBar) rightTrimBar)
                        .getChildren();
                for (MTrimElement trimElement : children) {
                    if (trimElement instanceof MToolBar) {
                        List<MToolBarElement> toolItems = ((MToolBar) trimElement)
                                .getChildren();
                        for (MToolBarElement toolBarElement : toolItems) {
                            if (toolBarElement instanceof MHandledToolItem
                                    && toolBarElement != uiElement) {
                                MHandledToolItem handledToolItem = (MHandledToolItem) toolBarElement;
                                handledToolItem.setSelected(false);

                                Map parameters = handledToolItem.getWbCommand()
                                        .getParameterMap();
                                String partId = (String) parameters.get(
                                        IModelConstants.KEY_MODEL_PART_COMMAND_PARAMETER_PART_ID);
                                if (partId != null) {
                                    MPart p = partService.findPart(partId);
                                    if (p != null) {
                                        p.setVisible(false);
                                        partService.hidePart(p);
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    };

    private CTabFolder2Adapter CTFButtonListener = new CTabFolder2Adapter() {
        private MUIElement getElementToChange(CTabFolderEvent event) {
            CTabFolder ctf = (CTabFolder) event.widget;
            MUIElement element = (MUIElement) ctf
                    .getData(AbstractPartRenderer.OWNING_ME);
            if (element instanceof MArea)
                return element.getCurSharedRef();

            MUIElement parentElement = element.getParent();
            while (parentElement != null && !(parentElement instanceof MArea))
                parentElement = parentElement.getParent();

            return parentElement != null ? parentElement.getCurSharedRef()
                    : element;
        }

        @Override
        public void maximize(CTabFolderEvent event) {
            setState(getElementToChange(event), STACK_VISIBLE);
        }

        @Override
        public void minimize(CTabFolderEvent event) {
            setState(getElementToChange(event), TRIM_STACK_EXIST);
            setState(getElementToChange(event), STACK_HIDDEN);
        }

        @Override
        public void restore(CTabFolderEvent event) {
            setState(getElementToChange(event), STACK_VISIBLE);
        }
    };

    private MouseListener doubleClickListener = new MouseAdapter() {

        private MUIElement getElementToChange(MouseEvent event) {
            Widget widget = (Widget) event.widget;
            MUIElement element = (MUIElement) widget
                    .getData(AbstractPartRenderer.OWNING_ME);
            if (element instanceof MArea) {
                // set the state on the placeholder
                return element.getCurSharedRef();
            }

            MUIElement parentElement = element.getParent();
            while (parentElement != null && !(parentElement instanceof MArea))
                parentElement = parentElement.getParent();

            return parentElement != null ? parentElement.getCurSharedRef()
                    : element;
        }

        public void mouseDoubleClick(MouseEvent e) {
            if (e.button == 1) {
                MUIElement elementToChange = getElementToChange(e);
                if (!elementToChange.getTags().contains(STACK_VISIBLE)) {
                    setState(elementToChange, STACK_VISIBLE);
                }
            }
        }
    };

    private void setState(MUIElement element, String state) {
        if (STACK_VISIBLE.equals(state)) {
            element.getTags().remove(STACK_HIDDEN);
            element.getTags().remove(STACK_VISIBLE);
            element.getTags().add(STACK_VISIBLE);
        } else if (STACK_HIDDEN.equals(state)) {
            element.getTags().remove(STACK_HIDDEN);
            element.getTags().remove(STACK_VISIBLE);
            element.getTags().add(STACK_HIDDEN);
        } else if (TRIM_STACK_EXIST.equals(state)) {
            element.getTags().remove(TRIM_STACK_EXIST);
            element.getTags().add(TRIM_STACK_EXIST);
        }
    }

    @Inject
    @Optional
    private void subscribeAppStartUpCompleted(
            @UIEventTopic(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE) Event event,
            @Optional MApplication application) {
        fixToolItemOfRightTrimBarStatus(application);
    }

    private void fixToolItemOfRightTrimBarStatus(MApplication application) {
        if (application == null)
            return;

        MTrimmedWindow tw = null;
        List<MWindow> windows = application.getChildren();
        for (MWindow win : windows) {
            if (win instanceof MTrimmedWindow) {
                tw = (MTrimmedWindow) win;
                break;
            }
        }
        if (tw == null)
            return;

        MUIElement rightPartStack = modelService
                .find(ICathyConstants.ID_PARTSTACK_RIGHT, tw);
        MStackElement selectedPartStackElement = null;
        if (rightPartStack instanceof MPartStack) {
            selectedPartStackElement = ((MPartStack) rightPartStack)
                    .getSelectedElement();
        }

        MTrimBar rightTrimBar = modelService.getTrim((MTrimmedWindow) tw,
                SideValue.RIGHT);
        if (rightTrimBar != null) {
            List<MTrimElement> children = ((MTrimBar) rightTrimBar)
                    .getChildren();
            for (MTrimElement trimElement : children) {
                if (trimElement instanceof MToolBar) {
                    List<MToolBarElement> toolBarElements = ((MToolBar) trimElement)
                            .getChildren();
                    for (MToolBarElement te : toolBarElements) {
                        if (te instanceof MHandledToolItem) {
                            MHandledToolItem handledToolItem = (MHandledToolItem) te;

                            Object widget = te.getWidget();
                            if (widget instanceof ToolItem) {
                                ((ToolItem) widget).removeSelectionListener(
                                        toolItemSelectionListener);
                                ((ToolItem) widget).addSelectionListener(
                                        toolItemSelectionListener);
                            }

                            ParameterizedCommand pc = handledToolItem
                                    .getWbCommand();
                            if (pc != null) {
                                Map parameterMap = pc.getParameterMap();
                                String partId = (String) parameterMap.get(
                                        IModelConstants.KEY_MODEL_PART_COMMAND_PARAMETER_PART_ID);
                                boolean selected = selectedPartStackElement != null
                                        && selectedPartStackElement
                                                .getElementId().equals(partId);
                                handledToolItem.setSelected(selected);
                                Map<String, String> persistedState = handledToolItem
                                        .getPersistedState();
                                String iconURI = persistedState.get(selected
                                        ? IModelConstants.PERSISTED_STATE_KEY_SELECTED_ICONURI
                                        : IModelConstants.PERSISTED_STATE_KEY_UNSELECTED_ICONURI);
                                if (iconURI != null)
                                    handledToolItem.setIconURI(iconURI);
                            }
                        }
                    }
                }
            }
        }
    }

    @Inject
    @Optional
    private void subscribeTopicWidget(
            @UIEventTopic(UIEvents.UIElement.TOPIC_WIDGET) Event event) {
        final MUIElement changedElement = (MUIElement) event
                .getProperty(EventTags.ELEMENT);
        if (!(changedElement instanceof MPartStack)
                && !(changedElement instanceof MArea))
            return;

        Control control = (Control) changedElement.getWidget();
        if (control == null || control.isDisposed())
            return;

        control.removeMouseListener(doubleClickListener);// Prevent multiple instances
        control.addMouseListener(doubleClickListener);

        if (control instanceof CTabFolder) {
            ((CTabFolder) control).removeCTabFolder2Listener(CTFButtonListener); // Prevent multiple instances
            ((CTabFolder) control).addCTabFolder2Listener(CTFButtonListener);
        }
    }

    /**
     * Handles removals from the perspective
     *
     * @param event
     */

    @Inject
    @Optional
    private void subscribeTopicChildren(
            @UIEventTopic(UIEvents.ElementContainer.TOPIC_CHILDREN) Event event) {
        final MUIElement changedElement = (MUIElement) event
                .getProperty(EventTags.ELEMENT);
        MWindow window = modelService.getTopLevelWindowFor(changedElement);

        // this method is intended to update the minimized stacks in a trim
        // if the removed element is no perspective and the top level window
        // is not a trimmed window, we don't need to do anything here
        if (!(changedElement instanceof MPerspectiveStack) || window == null
                || !(window instanceof MTrimmedWindow)) {
            return;
        }

        if (UIEvents.isREMOVE(event)) {
            for (Object removedElement : UIEvents.asIterable(event,
                    UIEvents.EventTags.OLD_VALUE)) {
                MUIElement removed = (MUIElement) removedElement;
                String perspectiveId = removed.getElementId();

                MTrimBar bar = modelService.getTrim((MTrimmedWindow) window,
                        SideValue.TOP);

                // gather up any minimized stacks for this perspective...
                List<MToolControl> toRemove = new ArrayList<MToolControl>();
                for (MUIElement child : bar.getChildren()) {
                    String trimElementId = child.getElementId();
                    if (child instanceof MToolControl
                            && trimElementId.contains(perspectiveId)) {
                        toRemove.add((MToolControl) child);
                    }
                }

                // ...and remove them
                for (MToolControl minStack : toRemove) {
                    minStack.setToBeRendered(false);
                    bar.getChildren().remove(minStack);
                }
            }
        }
    }

    /**
     * Handles changes of the perspective
     *
     * @param event
     */

    @Inject
    @Optional
    private void subscribeTopicSelectedElement(
            @UIEventTopic(UIEvents.ElementContainer.TOPIC_SELECTEDELEMENT) Event event,
            @Optional MApplication application) {
        final MUIElement changedElement = (MUIElement) event
                .getProperty(EventTags.ELEMENT);

        if (changedElement instanceof MPartStack
                && ICathyConstants.ID_PARTSTACK_RIGHT
                        .equals(changedElement.getElementId())) {
            fixToolItemOfRightTrimBarStatus(application);
            return;
        }

        if (!(changedElement instanceof MPerspectiveStack))
            return;

        MPerspectiveStack ps = (MPerspectiveStack) changedElement;
        MWindow window = modelService.getTopLevelWindowFor(ps);

        List<MToolControl> tcList = modelService.findElements(window, null,
                MToolControl.class, null);

        final MPerspective curPersp = ps.getSelectedElement();
        if (curPersp != null) {
            List<String> tags = new ArrayList<String>();
            tags.add(TRIM_STACK_EXIST);
            List<MUIElement> minimizedElements = modelService
                    .findElements(curPersp, null, MUIElement.class, tags);
            // Show any minimized stack from the current perspective
            String perspId = '(' + curPersp.getElementId() + ')';
            for (MUIElement ele : minimizedElements) {
                String fullId = ele.getElementId() + perspId;

                for (MToolControl tc : tcList) {
                    if (fullId.equals(tc.getElementId())) {
                        tc.setToBeRendered(true);
                    }
                }
            }
        }

        // Hide any minimized stacks from the old perspective
        if (event.getProperty(EventTags.OLD_VALUE) instanceof MPerspective) {
            MPerspective oldPersp = (MPerspective) event
                    .getProperty(EventTags.OLD_VALUE);
            String perspId = '(' + oldPersp.getElementId() + ')';
            for (MToolControl tc : tcList) {
                if (tc.getObject() instanceof XTrimStack
                        && tc.getElementId().contains(perspId)) {
                    XTrimStack ts = (XTrimStack) tc.getObject();
                    ts.setStateForShowStack(false);
                    tc.setToBeRendered(false);
                }
            }
        }

        final Shell winShell = (Shell) window.getWidget();
        winShell.getDisplay().asyncExec(new Runnable() {
            public void run() {
                if (!winShell.isDisposed()) {
                    winShell.layout(true, true);
                }
            }
        });
    }

    /**
     * Handles changes in tags
     *
     * @param event
     */

    @Inject
    @Optional
    private void subscribeTopicTagsChanged(
            @UIEventTopic(UIEvents.ApplicationElement.TOPIC_TAGS) Event event) {
        Object changedObj = event.getProperty(EventTags.ELEMENT);

        if (!(changedObj instanceof MUIElement))
            return;

        final MUIElement changedElement = (MUIElement) changedObj;

        if (UIEvents.isADD(event)) {
            if (UIEvents.contains(event, UIEvents.EventTags.NEW_VALUE,
                    STACK_HIDDEN)) {
                hideStack(changedElement);
            } else if (UIEvents.contains(event, UIEvents.EventTags.NEW_VALUE,
                    STACK_VISIBLE)) {
                showStack(changedElement);
            } else if (UIEvents.contains(event, UIEvents.EventTags.NEW_VALUE,
                    TRIM_STACK_EXIST)) {
                createTrim(changedElement);
                changedElement.setVisible(false);
            }
        }
    }

    /**
     * Handles changes in the id of the element If a perspective ID changes fix
     * any TrimStacks that reference the old id to point at the new id. This
     * keeps trim stacks attached to the correct perspective when a perspective
     * is saved with a new name.
     *
     * @param event
     */

    @Inject
    @Optional
    private void subscribeTopicElementId(
            @UIEventTopic(UIEvents.ApplicationElement.TOPIC_ELEMENTID) Event event) {
        Object changedObject = event.getProperty(EventTags.ELEMENT);

        // Only care about MPerspective id changes
        if (!(changedObject instanceof MPerspective))
            return;

        MPerspective perspective = (MPerspective) changedObject;

        String newID = (String) event.getProperty(UIEvents.EventTags.NEW_VALUE);
        String oldID = (String) event.getProperty(UIEvents.EventTags.OLD_VALUE);

        // pattern is trimStackID(perspectiveID)
        newID = '(' + newID + ')';
        oldID = '(' + oldID + ')';

        // Search the trim for the window containing the perspective
        MWindow perspWin = modelService.getTopLevelWindowFor(perspective);
        if (perspWin == null)
            return;

        List<MToolControl> trimStacks = modelService.findElements(perspWin,
                null, MToolControl.class, null);
        for (MToolControl trimStack : trimStacks) {
            // Only care about MToolControls that are TrimStacks
            if (XTrimStack.CONTRIBUTION_URI_XTRIMSTACK
                    .equals(trimStack.getContributionURI()))
                trimStack.setElementId(
                        trimStack.getElementId().replace(oldID, newID));
        }
    }

    /**
     * Handles the event that the perspective is saved
     *
     * @param event
     */

    @Inject
    @Optional
    private void subscribeTopicPerspSaved(
            @UIEventTopic(UIEvents.UILifeCycle.PERSPECTIVE_SAVED) Event event) {
        final MPerspective savedPersp = (MPerspective) event
                .getProperty(EventTags.ELEMENT);
        String cache = getTrimCache(savedPersp);
        minMaxAddon.getPersistedState().put(savedPersp.getElementId(), cache);
    }

    private String getTrimCache(MPerspective savedPersp) {
        MWindow topWin = modelService.getTopLevelWindowFor(savedPersp);
        String perspIdStr = '(' + savedPersp.getElementId() + ')';

        String cache = getWinCache(topWin, perspIdStr);
        for (MWindow dw : savedPersp.getWindows()) {
            cache += getWinCache(dw, perspIdStr);
        }

        return cache;
    }

    private String getWinCache(MWindow win, String perspIdStr) {
        String winStr = ""; //$NON-NLS-1$

        List<MPartStack> stackList = modelService.findElements(win, null,
                MPartStack.class, null);
        for (MPartStack stack : stackList) {
            winStr += getStackTrimLoc(stack, perspIdStr);
        }
        return winStr;
    }

    private String getStackTrimLoc(MPartStack stack, String perspIdStr) {
        MWindow stackWin = modelService.getTopLevelWindowFor(stack);// getContainingWindow(stack);
        MUIElement tcElement = modelService
                .find(stack.getElementId() + perspIdStr, stackWin);
        if (tcElement == null)
            return ""; //$NON-NLS-1$

        MTrimBar bar = (MTrimBar) ((MUIElement) tcElement.getParent());
        int sideVal = bar.getSide().getValue();
        int index = bar.getChildren().indexOf(tcElement);
        return stack.getElementId() + ' ' + sideVal + ' ' + index + "#"; //$NON-NLS-1$
    }

    /**
     * Handles the event that the perspective is reset
     *
     * @param event
     */
    @Inject
    @Optional
    private void subscribeTopicPerspReset(
            @UIEventTopic(UIEvents.UILifeCycle.PERSPECTIVE_RESET) Event event,
            @Optional MApplication application) {
        final MPerspective resetPersp = (MPerspective) event
                .getProperty(EventTags.ELEMENT);

        //Any stack which has TrimStackExist tag should have a created trim
        List<MUIElement> minimizedElements = modelService.findElements(
                resetPersp, null, MUIElement.class,
                Arrays.asList(TRIM_STACK_EXIST));
        for (MUIElement element : minimizedElements) {
            createTrim(element);
        }

        fixToolItemOfRightTrimBarStatus(application);

    }

    /**
     * Handles the event that the perspective is opened
     *
     * @param event
     */
    @Inject
    @Optional
    private void subscribeTopicPerspOpened(
            @UIEventTopic(UIEvents.UILifeCycle.PERSPECTIVE_OPENED) Event event) {
        final MPerspective openedPersp = (MPerspective) event
                .getProperty(EventTags.ELEMENT);

        //Any stack which has TrimStackExist tag should have a created trim
        List<MGenericStack> allStacks = modelService.findElements(openedPersp,
                null, MGenericStack.class, Arrays.asList(TRIM_STACK_EXIST));
        for (MGenericStack stack : allStacks) {
            createTrim(stack);
        }
    }

    boolean isEmptyPerspectiveStack(MUIElement element) {
        if (!(element instanceof MPerspectiveStack))
            return false;
        MPerspectiveStack ps = (MPerspectiveStack) element;
        return ps.getChildren().size() == 0;
    }

    void hideStack(MUIElement element) {
        // Can't create trim for a non-rendered element
        if (!element.isToBeRendered())
            return;

        if (isEmptyPerspectiveStack(element)) {
            element.setVisible(false);
            return;
        }

        createTrim(element);
        element.setVisible(false);
    }

    void showStack(final MUIElement element) {
        if (!element.isToBeRendered())
            return;

        List<MUIElement> elementsToHide = getElementsToHide(element);
        for (MUIElement toMinimize : elementsToHide) {
            setState(toMinimize, STACK_HIDDEN);
        }
        element.setVisible(true);
    }

    private List<MUIElement> getElementsToHide(MUIElement element) {
        MWindow win = getWindowFor(element);
        MPerspective persp = modelService.getActivePerspective(win);

        List<MUIElement> elementsToHide = new ArrayList<MUIElement>();
        int loc = modelService.getElementLocation(element);
        if ((loc & EModelService.OUTSIDE_PERSPECTIVE) != 0) {
            // Hide all other global stacks
            List<MPartStack> globalStacks = modelService.findElements(win, null,
                    MPartStack.class, null, EModelService.OUTSIDE_PERSPECTIVE);
            for (MPartStack gStack : globalStacks) {
                if (gStack == element || !gStack.isToBeRendered())
                    continue;

                if (gStack.getWidget() != null
                        && !gStack.getTags().contains(STACK_HIDDEN)) {
                    elementsToHide.add(gStack);
                }
            }

            // Hide the Perspective Stack
            MUIElement perspStack = null;
            if (persp == null) {
                // special case for windows with no perspectives (eg bug 372614:
                // intro part with no perspectives). We know we're outside
                // of the perspective stack, so find it top-down
                List<MPerspectiveStack> pStacks = modelService.findElements(win,
                        null, MPerspectiveStack.class, null);
                perspStack = (pStacks.size() > 0) ? pStacks.get(0) : null;
            } else {
                perspStack = persp.getParent();
            }
            if (perspStack != null) {
                if (perspStack.getElementId() == null
                        || perspStack.getElementId().length() == 0)
                    perspStack.setElementId("PerspectiveStack"); //$NON-NLS-1$

                elementsToHide.add(perspStack);
            }
        } else {
            List<MPartStack> stacks = modelService.findElements(
                    persp == null ? win : persp, null, MPartStack.class, null,
                    EModelService.PRESENTATION);
            for (MPartStack theStack : stacks) {
                if (theStack == element || !theStack.isToBeRendered())
                    continue;

                // Exclude stacks in DW's
                if (getWindowFor(theStack) != win)
                    continue;

                loc = modelService.getElementLocation(theStack);
                if (loc != EModelService.IN_SHARED_AREA
                        && theStack.getWidget() != null && theStack.isVisible()
                        && !theStack.getTags().contains(STACK_HIDDEN)) {
                    elementsToHide.add(theStack);
                }
            }

            // Find any 'standalone' views *not* in a stack
            List<String> standaloneTag = new ArrayList<String>();
            standaloneTag.add(IPresentationEngine.STANDALONE);
            List<MPlaceholder> standaloneViews = modelService.findElements(
                    persp == null ? win : persp, null, MPlaceholder.class,
                    standaloneTag, EModelService.PRESENTATION);
            for (MPlaceholder part : standaloneViews) {
                if (!part.isToBeRendered())
                    continue;
                elementsToHide.add(part);
            }
        }

        return elementsToHide;
    }

    /**
     * Return the MWindow containing this element (if any). This may either be a
     * 'top level' window -or- a detached window. This allows the min.max code
     * to only affect elements in the window containing the element.
     *
     * @param element
     *            The element to check
     * @return the window containing the element.
     */
    private MWindow getWindowFor(MUIElement element) {
        MUIElement parent = element.getParent();

        // We rely here on the fact that a DW's 'getParent' will return
        // null since it's not in the 'children' hierarchy
        while (parent != null && !(parent instanceof MWindow))
            parent = parent.getParent();

        // A detached window will end up with getParent() == null
        return (MWindow) parent;
    }

    private void createTrim(MUIElement element) {
        MWindow win = getWindowFor(element);
        if (!(win instanceof MTrimmedWindow)) {
            return;
        }

        MTrimmedWindow window = (MTrimmedWindow) win;
        Shell winShell = (Shell) window.getWidget();

        // Is there already a TrimControl there ?
        String trimId = element.getElementId()
                + getMinimizedElementSuffix(element);
        MToolControl trimStack = (MToolControl) modelService.find(trimId,
                window);

        if (trimStack == null) {
            trimStack = modelService.createModelElement(MToolControl.class);
            trimStack.setElementId(trimId);
            trimStack
                    .setContributionURI(XTrimStack.CONTRIBUTION_URI_XTRIMSTACK);
            trimStack.getTags().add("XTrimStack"); //$NON-NLS-1$
            trimStack.getTags().add(ICathyConstants.TAG_TRIMBAR_LAYOUT_CENTER);

            MTrimBar bar = modelService.getTrim(window, SideValue.RIGHT);
            bar.getChildren().add(trimStack);
            bar.setVisible(true);

            // get the parent trim bar, see bug 320756
            if (bar.getWidget() == null) {
                // ask it to be rendered
                bar.setToBeRendered(true);

                // create the widget
                context.get(IPresentationEngine.class).createGui(bar, winShell,
                        window.getContext());
            }
        } else {
            // get the parent trim bar, see bug 320756
            MUIElement parent = trimStack.getParent();
            parent.setVisible(true);
            if (parent.getWidget() == null) {
                // ask it to be rendered
                parent.setToBeRendered(true);
                // create the widget
                context.get(IPresentationEngine.class).createGui(parent,
                        winShell, window.getContext());
            }
        }
        trimStack.setToBeRendered(true);
    }

    private String getMinimizedElementSuffix(MUIElement element) {
        String id = ID_SUFFIX;
        MPerspective persp = modelService.getPerspectiveFor(element);
        if (persp != null) {
            id = '(' + persp.getElementId() + ')';
        }
        return id;
    }

}
