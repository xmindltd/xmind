package org.xmind.cathy.internal.renderer;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MDialog;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindowElement;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.ElementContainer;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.IWindowCloseHandler;
import org.eclipse.e4.ui.workbench.renderers.swt.SWTPartRenderer;
import org.eclipse.jface.util.Geometry;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.osgi.service.event.Event;
import org.xmind.ui.internal.e4models.IModelConstants;

public class XDialogRenderer extends SWTPartRenderer {

    private class WindowSizeUpdateJob implements Runnable {
        public List<MWindow> windowsToUpdate = new ArrayList<MWindow>();

        public void run() {
            boundsJob = null;
            while (!windowsToUpdate.isEmpty()) {
                MWindow window = windowsToUpdate.remove(0);
                Shell shell = (Shell) window.getWidget();
                if (shell == null || shell.isDisposed())
                    continue;
                shell.setBounds(window.getX(), window.getY(), window.getWidth(),
                        window.getHeight());
            }
        }
    }

    WindowSizeUpdateJob boundsJob;

    @Inject
    private IEclipseContext context;

    @Inject
    private Display display;

    @Inject
    private MApplication application;

    @Inject
    @Optional
    @Named("localActiveShell")
    private Shell parentShell;

    @SuppressWarnings("unchecked")
    @Inject
    @Optional
    private void subscribeTopicChildAdded(
            @UIEventTopic(ElementContainer.TOPIC_CHILDREN) Event event) {
        Object changedObject = event.getProperty(UIEvents.EventTags.ELEMENT);
        if (!(changedObject instanceof MDialog)) {
            return;
        }
        if (UIEvents.isADD(event)) {
            processContents((MElementContainer<MUIElement>) changedObject);
            postProcess((MDialog) changedObject);
        }
    }

    @Inject
    @Optional
    private void subscribeTopicWindowChanged(
            @UIEventTopic(UIEvents.Window.TOPIC_ALL) Event event) {
        Object objElement = event.getProperty(UIEvents.EventTags.ELEMENT);
        if (!(objElement instanceof MDialog)) {
            return;
        }

        // Is this listener interested ?
        MDialog dialogModel = (MDialog) objElement;
        if (dialogModel.getRenderer() != XDialogRenderer.this) {
            return;
        }

        // No widget == nothing to update
        Shell theShell = (Shell) dialogModel.getWidget();
        if (theShell == null) {
            return;
        }

        String attName = (String) event.getProperty(UIEvents.EventTags.ATTNAME);

        if (UIEvents.Window.X.equals(attName)
                || UIEvents.Window.Y.equals(attName)
                || UIEvents.Window.WIDTH.equals(attName)
                || UIEvents.Window.HEIGHT.equals(attName)) {
            if (boundsJob == null) {
                boundsJob = new WindowSizeUpdateJob();
                boundsJob.windowsToUpdate.add(dialogModel);
                theShell.getDisplay().asyncExec(boundsJob);
            } else {
                if (!boundsJob.windowsToUpdate.contains(dialogModel))
                    boundsJob.windowsToUpdate.add(dialogModel);
            }
        }
    }

    @Override
    public Object createWidget(MUIElement element, Object parent) {
        final Widget newWidget;

        if (!(element instanceof MDialog)
                || (parent != null && !(parent instanceof Control)))
            return null;

        MDialog dialogModel = (MDialog) element;

        MApplication appModel = dialogModel.getContext()
                .get(MApplication.class);
        Boolean rtlMode = (Boolean) appModel.getTransientData()
                .get(E4Workbench.RTL_MODE);
        int rtlStyle = (rtlMode != null && rtlMode.booleanValue())
                ? SWT.RIGHT_TO_LEFT : 0;

        Shell parentShell = parent == null ? null
                : ((Control) parent).getShell();

        final Shell wbwShell;

        int styleOverride = getStyleOverride(dialogModel) | rtlStyle;
        if (parentShell == null) {
            int style = styleOverride == -1 ? SWT.SHELL_TRIM | rtlStyle
                    : styleOverride;
            wbwShell = new Shell(display, style);
            dialogModel.getTags().add("topLevel"); //$NON-NLS-1$
        } else {
            int style = SWT.TITLE | SWT.RESIZE | SWT.MAX | SWT.CLOSE | rtlStyle;
            style = styleOverride == -1 ? style : styleOverride;
            if (dialogModel.getTags()
                    .contains(IPresentationEngine.WINDOW_TOP_LEVEL))
                wbwShell = new Shell(display, style);
            else
                wbwShell = new Shell(parentShell, style);
        }

        wbwShell.setBackgroundMode(SWT.INHERIT_DEFAULT);

        Rectangle modelBounds = wbwShell.getBounds();
        modelBounds.x = dialogModel.getX();
        modelBounds.y = dialogModel.getY();
        modelBounds.height = dialogModel.getHeight();
        modelBounds.width = dialogModel.getWidth();

        // Force the shell onto the display if it would be invisible otherwise
        Rectangle displayBounds = Display.getCurrent().getPrimaryMonitor()
                .getBounds();
        if (!modelBounds.intersects(displayBounds)) {
            Rectangle clientArea = Display.getCurrent().getPrimaryMonitor()
                    .getClientArea();
            modelBounds.x = clientArea.x;
            modelBounds.y = clientArea.y;
        }
        wbwShell.setBounds(modelBounds);

        setCSSInfo(dialogModel, wbwShell);

        wbwShell.setLayout(new FillLayout(SWT.VERTICAL));
        newWidget = wbwShell;
        bindWidget(element, newWidget);

        // set up context
        IEclipseContext localContext = getContext(dialogModel);
        localContext.set(Shell.class, wbwShell);
        localContext.set(E4Workbench.LOCAL_ACTIVE_SHELL, wbwShell);
        localContext.set(IShellProvider.class, new IShellProvider() {
            public Shell getShell() {
                return wbwShell;
            }
        });
        localContext.set(IWindowCloseHandler.class, new IWindowCloseHandler() {
            public boolean close(MWindow window) {
                return closeDetachedWindow(window);
            }
        });

        if (dialogModel.getLabel() != null)
            wbwShell.setText(dialogModel.getLocalizedLabel());

        if (dialogModel.getIconURI() != null
                && dialogModel.getIconURI().length() > 0) {
            wbwShell.setImage(getImage(dialogModel));
        } else {
            wbwShell.setImages(Window.getDefaultImages());
        }

        return newWidget;
    }

    private boolean closeDetachedWindow(MWindow window) {
        EPartService partService = window.getContext().get(EPartService.class);
        List<MPart> parts = modelService.findElements(window, null, MPart.class,
                null);
        // this saves one part at a time, not ideal but better than not saving
        // at all
        for (MPart part : parts) {
            if (!partService.savePart(part, true)) {
                // user cancelled the operation, return false
                return false;
            }
        }

        // hide every part individually, following 3.x behaviour
        for (MPart part : parts) {
            partService.hidePart(part);
        }
        return true;
    }

    @Override
    public void hookControllerLogic(final MUIElement me) {
        super.hookControllerLogic(me);
        Widget widget = (Widget) me.getWidget();

        if (widget instanceof Shell && me instanceof MWindow) {
            final Shell shell = (Shell) widget;
            shell.addDisposeListener(new DisposeListener() {

                public void widgetDisposed(DisposeEvent e) {
                    //Save user dialog bounds
                    String splitSymbol = ","; //$NON-NLS-1$
                    Rectangle bounds = shell.getBounds();
                    String location = bounds.x + splitSymbol + bounds.y
                            + splitSymbol + bounds.width + splitSymbol
                            + bounds.height;
                    me.getPersistedState().put(
                            IModelConstants.KEY_DIALOG_PART_CUSTOM_LOCATION,
                            location);

                    MWindow window = (MWindow) me;
                    IWindowCloseHandler closeHandler = window.getContext()
                            .get(IWindowCloseHandler.class);
                    // if there's no handler or the handler permits the close
                    // request, clean-up as necessary
                    if (closeHandler == null || closeHandler.close(window)) {
                        Object parentModel = shell.getParent()
                                .getData(OWNING_ME);
                        if (parentModel instanceof MWindow) {
                            List<MWindowElement> children = ((MWindow) parentModel)
                                    .getChildren();
                            if (children.contains(window)) {
                                children.remove(window);
                            }
                        } else {
                            MWindow trimmedWindow = application.getChildren()
                                    .get(0);
                            List<MWindow> windows = trimmedWindow.getWindows();
                            if (windows.contains(window)) {
                                windows.remove(window);
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public void processContents(MElementContainer<MUIElement> me) {
        if (!(((MUIElement) me) instanceof MDialog))
            return;
        MDialog wbwModel = (MDialog) ((MUIElement) me);
        super.processContents(me);

        // Populate the main menu
        IPresentationEngine renderer = context.get(IPresentationEngine.class);
        if (wbwModel.getMainMenu() != null) {
            renderer.createGui(wbwModel.getMainMenu(), me.getWidget(), null);
            Shell shell = (Shell) me.getWidget();
            shell.setMenuBar((Menu) wbwModel.getMainMenu().getWidget());
        }

        // create Detached Windows
        for (MWindow dw : wbwModel.getWindows()) {
            renderer.createGui(dw, me.getWidget(), wbwModel.getContext());
        }
    }

    @Override
    public void postProcess(MUIElement shellME) {
        if (!(shellME instanceof MDialog))
            return;
        MDialog dialogModel = (MDialog) shellME;
        super.postProcess(shellME);

        Shell shell = (Shell) shellME.getWidget();
        String location = shellME.getPersistedState()
                .get(IModelConstants.KEY_DIALOG_PART_CUSTOM_LOCATION);
        location = location == null ? "" : location; //$NON-NLS-1$
        String[] locations = location.split(","); //$NON-NLS-1$

        if (locations.length < 4) {
            String[] tempLocations = new String[4];
            for (int i = 0; i < locations.length; i++)
                tempLocations[i] = locations[i];
            locations = tempLocations;
        }

        Point size = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        if (isNone(locations[2])) {
            locations[2] = String.valueOf(size.x);
        }
        if (isNone(locations[3])) {
            locations[3] = String.valueOf(size.y);
        }
        size = new Point(Integer.valueOf(locations[2]),
                Integer.valueOf(locations[3]));
        Point initLocation = getInitialLocation(shell, size);
        Rectangle bounds = getConstrainedShellBounds(shell,
                new Rectangle(initLocation.x, initLocation.y, size.x, size.y));
        if (isNone(locations[0]) && isNone(locations[1])) {
            locations[0] = String.valueOf(bounds.x);
            locations[1] = String.valueOf(bounds.y);
        }

        dialogModel.setX(Integer.valueOf(locations[0]));
        dialogModel.setY(Integer.valueOf(locations[1]));
        dialogModel.setWidth(Integer.valueOf(locations[2]));
        dialogModel.setHeight(Integer.valueOf(locations[3]));

        StringBuffer sb = new StringBuffer();
        sb.append(locations[0]);
        sb.append(","); //$NON-NLS-1$
        sb.append(locations[1]);
        sb.append(","); //$NON-NLS-1$
        sb.append(locations[2]);
        sb.append(","); //$NON-NLS-1$
        sb.append(locations[3]);

        dialogModel.getPersistedState().put(
                IModelConstants.KEY_DIALOG_PART_CUSTOM_LOCATION, sb.toString());

        shell.layout(true);
        forceLayout(shell);
        if (shellME.isVisible()) {
            shell.open();
        } else {
            shell.setVisible(false);
        }
    }

    private boolean isNone(String value) {
        return value == null || "".equals(value); //$NON-NLS-1$
    }

    private Point getInitialLocation(Shell shell, Point initialSize) {
        Composite parent = shell.getParent();

        Monitor monitor = shell.getDisplay().getPrimaryMonitor();
        if (parent != null) {
            monitor = parent.getMonitor();
        }

        Rectangle monitorBounds = monitor.getClientArea();
        Point centerPoint;
        if (parent != null) {
            centerPoint = Geometry.centerPoint(parent.getBounds());
        } else {
            centerPoint = Geometry.centerPoint(monitorBounds);
        }

        return new Point(centerPoint.x - (initialSize.x / 2),
                centerPoint.y - (initialSize.y / 2));
    }

    protected Rectangle getConstrainedShellBounds(Shell shell,
            Rectangle preferredSize) {
        Rectangle result = new Rectangle(preferredSize.x, preferredSize.y,
                preferredSize.width, preferredSize.height);

        Monitor mon = getClosestMonitor(shell.getDisplay(),
                Geometry.centerPoint(result));

        Rectangle bounds = mon.getClientArea();

        if (result.height > bounds.height) {
            result.height = bounds.height;
        }

        if (result.width > bounds.width) {
            result.width = bounds.width;
        }

        result.x = Math.max(bounds.x,
                Math.min(result.x, bounds.x + bounds.width - result.width));
        result.y = Math.max(bounds.y,
                Math.min(result.y, bounds.y + bounds.height - result.height));

        return result;
    }

    private static Monitor getClosestMonitor(Display toSearch, Point toFind) {
        int closest = Integer.MAX_VALUE;

        Monitor[] monitors = toSearch.getMonitors();
        Monitor result = monitors[0];

        for (int idx = 0; idx < monitors.length; idx++) {
            Monitor current = monitors[idx];

            Rectangle clientArea = current.getClientArea();

            if (clientArea.contains(toFind)) {
                return current;
            }

            int distance = Geometry
                    .distanceSquared(Geometry.centerPoint(clientArea), toFind);
            if (distance < closest) {
                closest = distance;
                result = current;
            }
        }

        return result;
    }

    private void forceLayout(Shell shell) {
        int i = 0;
        while (shell.isLayoutDeferred()) {
            shell.setLayoutDeferred(false);
            i++;
        }
        while (i > 0) {
            shell.setLayoutDeferred(true);
            i--;
        }
    }

}
