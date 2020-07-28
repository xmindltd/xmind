/*
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and above are dual-licensed
 * under the Eclipse Public License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors: XMind Ltd. - initial API and implementation
 */
package org.xmind.ui.internal.browser;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.CloseWindowListener;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.StatusTextEvent;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.browser.VisibilityWindowListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PartInitException;
import org.xmind.core.net.util.LinkUtils;
import org.xmind.ui.animation.AnimationViewer;
import org.xmind.ui.animation.IAnimationContentProvider;
import org.xmind.ui.browser.BrowserSupport;
import org.xmind.ui.browser.IBrowser;
import org.xmind.ui.browser.IBrowserSupport;
import org.xmind.ui.browser.IBrowserViewer;
import org.xmind.ui.browser.IBrowserViewerContainer;
import org.xmind.ui.browser.IBrowserViewerContribution;
import org.xmind.ui.browser.IBrowserViewerContribution2;
import org.xmind.ui.browser.IPropertyChangingListener;
import org.xmind.ui.browser.PropertyChangingEvent;
import org.xmind.ui.viewers.ImageCachedLabelProvider;

/**
 * A viewer implementing a web browser. It provides an embeded SWT Browser
 * widget with an optional toolbar consisting of a URL combo box, back & forward
 * buttons, a refresh button and a home logo.
 */
public class BrowserViewer implements IBrowserViewer {

    private static Object DEFAULT_BUSY_PICTURES = null;

    private static class BusyIndicatorContentProvider
            extends ArrayContentProvider implements IAnimationContentProvider {
        public long getDuration(Object element) {
            return SWT.DEFAULT; // use default duration
        }

        public Object getStaticElement(Object inputElement, Object[] elements) {
            return elements[0];
        }
    }

    private static class BusyIndicatorLabelProvider
            extends ImageCachedLabelProvider {
        protected Image createImage(Object element) {
            if (element instanceof ImageDescriptor) {
                ImageDescriptor desc = (ImageDescriptor) element;
                return desc.createImage(false);
            }
            return null;
        }
    }

    private class OpenInExternalAction extends Action {

        public OpenInExternalAction() {
            super(BrowserMessages.BrowserView_OpenInExternalBrowser_text,
                    BrowserImages.getImageDescriptor(BrowserImages.BROWSER));
            setToolTipText(
                    BrowserMessages.BrowserView_OpenInExternalBrowser_toolTip);
        }

        public void run() {
            IBrowser browser = BrowserSupport.getInstance()
                    .createBrowser(IBrowserSupport.AS_EXTERNAL);
            try {
                browser.openURL(getURL());
            } catch (PartInitException e) {
                BrowserPlugin.log(e);
            }
        }
    }

    private class BackAction extends Action {
        public BackAction() {
            super(BrowserMessages.BrowserViewer_PrevPage_toolTip, BrowserImages
                    .getImageDescriptor(BrowserImages.BACKWARD, true));
            setToolTipText(BrowserMessages.BrowserViewer_PrevPage_toolTip);
            setDisabledImageDescriptor(BrowserImages
                    .getImageDescriptor(BrowserImages.BACKWARD, false));
        }

        public void run() {
            back();
        }
    }

    private class ForwardAction extends Action {
        public ForwardAction() {
            super(BrowserMessages.BrowserViewer_NextPage_toolTip, BrowserImages
                    .getImageDescriptor(BrowserImages.FORWARD, true));
            setToolTipText(BrowserMessages.BrowserViewer_NextPage_toolTip);
            setDisabledImageDescriptor(BrowserImages
                    .getImageDescriptor(BrowserImages.FORWARD, false));
        }

        public void run() {
            forward();
        }
    }

    private class StopRefreshAction extends Action {
        private boolean stop = false;

        public StopRefreshAction() {
            setRefresh();
        }

        public void setStop() {
            stop = true;
            setText(BrowserMessages.BrowserViewer_Stop_toolTip);
            setToolTipText(BrowserMessages.BrowserViewer_Stop_toolTip);
            setImageDescriptor(
                    BrowserImages.getImageDescriptor(BrowserImages.STOP, true));
            setDisabledImageDescriptor(BrowserImages
                    .getImageDescriptor(BrowserImages.STOP, false));
        }

        public void setRefresh() {
            stop = false;
            setText(BrowserMessages.BrowserViewer_Refresh_toolTip);
            setToolTipText(BrowserMessages.BrowserViewer_Refresh_toolTip);
            setImageDescriptor(BrowserImages
                    .getImageDescriptor(BrowserImages.REFRESH, true));
            setDisabledImageDescriptor(BrowserImages
                    .getImageDescriptor(BrowserImages.REFRESH, false));
        }

        public void run() {
            if (stop) {
                stop();
            } else {
                refresh();
            }
        }
    }

    protected class BrowserListener implements LocationListener,
            OpenWindowListener, VisibilityWindowListener, CloseWindowListener,
            ProgressListener, TitleListener, StatusTextListener {

        private String locationText;

        private String titleText;

        private String statusText;

        public void hook(Browser browser) {
            browser.addLocationListener(this);
            browser.addOpenWindowListener(this);
            browser.addVisibilityWindowListener(this);
            browser.addCloseWindowListener(this);
            browser.addProgressListener(this);
            browser.addTitleListener(this);
            browser.addStatusTextListener(this);
        }

        public void unhook(Browser browser) {
            browser.removeLocationListener(this);
            browser.removeOpenWindowListener(this);
            browser.removeVisibilityWindowListener(this);
            browser.removeCloseWindowListener(this);
            browser.removeProgressListener(this);
            browser.removeTitleListener(this);
            browser.removeStatusTextListener(this);
        }

        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.swt.browser.LocationListener#changed(org.eclipse.swt.
         * browser.LocationEvent)
         */
        public void changed(LocationEvent event) {
            if (getControl() == null || getControl().isDisposed())
                return;
            if (!event.top)
                return;
            if (!"about:blank".equals(event.location)) { //$NON-NLS-1$
                String oldLocation = locationText;
                locationText = event.location;
                if (location != null) {
                    location.setText(event.location);
                }
                addToHistory(event.location);
                updateHistory();
                firePropertyChangeEvent(PROPERTY_LOCATION, oldLocation,
                        event.location);
            }
        }

        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.swt.browser.LocationListener#changing(org.eclipse.swt
         * .browser.LocationEvent)
         */
        public void changing(LocationEvent event) {
            if (getControl() == null || getControl().isDisposed())
                return;
            if (!"about:blank".equals(event.location)) { //$NON-NLS-1$
                event.doit = firePropertyChangingEvent(PROPERTY_LOCATION,
                        locationText, event.location, event.doit);
                if (event.doit && redirect) {
                    event.location = makeRedirectUrl(event.location);
                }
            }
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.swt.browser.OpenWindowListener#open(org.eclipse.swt.
         * browser .WindowEvent)
         */
        public void open(WindowEvent event) {
            if (getControl() == null || getControl().isDisposed())
                return;
            if (container != null) {
                event.browser = container.openNewBrowser();
            }
        }

        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.swt.browser.VisibilityWindowListener#hide(org.eclipse
         * .swt.browser.WindowEvent)
         */
        public void hide(WindowEvent event) {
            // do nothing
        }

        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.swt.browser.VisibilityWindowListener#show(org.eclipse
         * .swt.browser.WindowEvent)
         */
        public void show(WindowEvent event) {
//            if (event.widget == browser) {
//                if (composite.getParent() instanceof Shell) {
//                    Shell shell = (Shell) composite.getParent();
//                    if (event.location != null)
//                        shell.setLocation(event.location);
//                    if (event.size != null)
//                        shell.setSize(shell.computeSize(event.size.x,
//                                event.size.y));
//                    shell.open();
//                }
//            }
        }

        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.swt.browser.CloseWindowListener#close(org.eclipse.swt
         * .browser.WindowEvent)
         */
        public void close(WindowEvent event) {
            if (getControl() == null || getControl().isDisposed())
                return;
            if (container != null) {
                container.close();
            }
        }

        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.swt.browser.ProgressListener#changed(org.eclipse.swt.
         * browser.ProgressEvent)
         */
        public void changed(ProgressEvent event) {
            if (getControl() == null || getControl().isDisposed())
                return;
            if (event.total == 0)
                return;

            boolean done = (event.current == event.total);

            int percentProgress = event.current * 100 / event.total;
            if (container != null) {
                IProgressMonitor monitor = container.getActionBars()
                        .getStatusLineManager().getProgressMonitor();
                if (done) {
                    monitor.done();
                    progressWorked = 0;
                } else if (progressWorked == 0) {
                    monitor.beginTask("", event.total); //$NON-NLS-1$
                    progressWorked = percentProgress;
                } else {
                    monitor.worked(event.current - progressWorked);
                    progressWorked = event.current;
                }
            }

            if (homeBusy != null) {
                if (!homeBusy.isAnimating() && !done)
                    setLoading(true);
                else if (homeBusy.isAnimating() && done) // once the progress hits
                    // 100 percent, done, set
                    // busy to false
                    setLoading(false);
            }

            updateBackNextBusy();
            updateHistory();
        }

        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.swt.browser.ProgressListener#completed(org.eclipse.swt
         * .browser.ProgressEvent)
         */
        public void completed(ProgressEvent event) {
            if (getControl() == null || getControl().isDisposed())
                return;
            if (container != null) {
                IProgressMonitor monitor = container.getActionBars()
                        .getStatusLineManager().getProgressMonitor();
                monitor.done();
            }
            setLoading(false);
            updateBackNextBusy();
            updateHistory();
        }

        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.swt.browser.TitleListener#changed(org.eclipse.swt.browser
         * .TitleEvent)
         */
        public void changed(TitleEvent event) {
            if (getControl() == null || getControl().isDisposed())
                return;
            String oldTitle = titleText;
            titleText = event.title;
            firePropertyChangeEvent(PROPERTY_TITLE, oldTitle, titleText);
        }

        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.swt.browser.StatusTextListener#changed(org.eclipse.swt
         * .browser.StatusTextEvent)
         */
        public void changed(StatusTextEvent event) {
            if (getControl() == null || getControl().isDisposed())
                return;
            boolean doit = true;
            String oldStatus = statusText;
            if (!firePropertyChangingEvent(PROPERTY_STATUS, oldStatus,
                    event.text, doit)) {
                event.text = ""; //$NON-NLS-1$
            }
            if (container != null) {
                IStatusLineManager status = container.getActionBars()
                        .getStatusLineManager();
                status.setMessage(event.text);
            }
            statusText = event.text;
            firePropertyChangeEvent(PROPERTY_STATUS, oldStatus, statusText);
        }

    }

    private static final int MAX_HISTORY = 50;

    private static final String URL_HOME = LinkUtils.getHostByLanguage(true,
            true);

    private static List<String> URL_HISTORY;

    private Composite composite;

    private boolean mozilla;

    private Clipboard clipboard;

    private Composite toolBar;

    private ToolBarManager actionBar;

    private Combo location;

    private AnimationViewer homeBusy;

    private boolean loading;

    private Composite browserContainer;

    private Browser browser;

//    private Browser alternateBrowser;

    private BrowserListener browserListener;

    private BrowserErrorText errorText;

//    private boolean newWindow;

    private IBrowserViewerContainer container;

    private int progressWorked = 0;

    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
            this);

    private BackAction backAction;

    private ForwardAction forwardAction;

    private StopRefreshAction stopRefreshAction;

    private boolean redirect = true;

    private int style = 0;

    private Label description;

    public BrowserViewer(Composite parent, int style) {
        this(parent, style, null);
    }

    public BrowserViewer(Composite parent, int style,
            IBrowserViewerContainer container) {
        this.style = style;
        this.container = container;
        this.composite = new Composite(parent, style);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        layout.numColumns = 1;
        this.composite.setLayout(layout);
        this.composite
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        createContents(this.composite);
        hookControl(this.composite);
    }

    public int getStyle() {
        return style;
    }

    Label getDescriptionLabel() {
        return description;
    }

    protected void hookControl(Control control) {
        control.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                handleDispose();
            }
        });
        for (IBrowserViewerContribution contribution : BrowserContributionManager
                .getInstance().getContributions()) {
            if (contribution instanceof IBrowserViewerContribution2) {
                ((IBrowserViewerContribution2) contribution)
                        .installBrowserListeners(this);
            }
        }
    }

    protected void createContents(Composite parent) {
        clipboard = new Clipboard(parent.getDisplay());
        createToolBar(parent);
        createBrowser(parent);
        updateWithStyle();
        updateHistory();
        updateBackNextBusy();
//        if (browserContainer != null) {
//        } else {
//            errorText.getControl().setLayoutData(
//                    new GridData(SWT.FILL, SWT.FILL, true, true));
//        }
//        addBrowserListeners(browser);
    }

    protected void createToolBar(Composite parent) {
        toolBar = new Composite(parent, SWT.NONE);
        GridLayout toolbarLayout = new GridLayout();
        toolbarLayout.marginHeight = 2;
        toolbarLayout.marginWidth = 2;
        toolbarLayout.horizontalSpacing = 5;
        toolbarLayout.verticalSpacing = 0;
        toolBar.setLayout(toolbarLayout);
        toolBar.setLayoutData(
                new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        createActionBar(toolBar);
        createLocationBar(toolBar);
        //forbid extra contributions, such as SearchBoxContribution
        //if accept in the future, we can delete browserViewerContributions extension about search box.
//        createExtraContributions(toolBar);
        createHomeBusyIndicator(toolBar);
        toolbarLayout.numColumns = toolBar.getChildren().length;
    }

//    private void createExtraContributions(final Composite toolbarContainer) {
//        ContributionManager manager = new ContributionManager() {
//            public void update(boolean force) {
//                if ((style & IBrowserSupport.NO_EXTRA_CONTRIBUTIONS) == 0) {
//                    for (IContributionItem item : getItems()) {
//                        item.fill(toolbarContainer);
//                    }
//                }
//            }
//        };
//        for (IBrowserViewerContribution contribution : BrowserContributionManager
//                .getInstance().getContributions()) {
//            contribution.fillToolBar(this, manager);
//        }
//        manager.update(true);
//    }

    private void createActionBar(Composite parent) {
        actionBar = new ToolBarManager(SWT.FLAT);
        actionBar.add(new OpenInExternalAction());
        actionBar.add(backAction = new BackAction());
        actionBar.add(forwardAction = new ForwardAction());
        actionBar.add(stopRefreshAction = new StopRefreshAction());
        actionBar.createControl(parent);
        actionBar.getControl().setLayoutData(
                new GridData(SWT.FILL, SWT.CENTER, false, false));
    }

    private void createLocationBar(Composite parent) {
        location = new Combo(parent, SWT.BORDER | SWT.DROP_DOWN);
        location.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        description = new Label(parent, SWT.NONE);
        description.setText(""); //$NON-NLS-1$
        description
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        updateHistory();

        /*
         * We ignore selection event to avoid unneccessary selection (e.g. mouse
         * scrolling in the combo, arrow up/down, or even showing drop-down on
         * some platforms). User must press Enter whenver they have selected an
         * item in order to go to that URL.
         */
//        location.addSelectionListener(new SelectionAdapter() {
//            @Override
//            public void widgetSelected(SelectionEvent we) {
//                try {
//                    if (location.getSelectionIndex() != -1)
//                        setURL(location.getItem(location.getSelectionIndex()));
//                } catch (Exception ignore) {
//                }
//            }
//        });
        location.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event event) {
                int end = location.getText().length();
                location.setSelection(new Point(0, end));
            }
        });
        location.addListener(SWT.DefaultSelection, new Listener() {
            public void handleEvent(Event e) {
                setURL(location.getText());
            }
        });
    }

    protected void createHomeBusyIndicator(Composite parenet) {
        homeBusy = new AnimationViewer(parenet, SWT.NONE);
        homeBusy.setContentProvider(new BusyIndicatorContentProvider());
        homeBusy.setLabelProvider(new BusyIndicatorLabelProvider());
        homeBusy.setInput(getDefaultBusyPictures());
        homeBusy.getControl()
                .setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        homeBusy.getControl().addListener(SWT.MouseDown, new Listener() {
            public void handleEvent(Event event) {
                setURL(URL_HOME);
            }
        });
    }

    public AnimationViewer getBusyIndicator() {
        return homeBusy;
    }

    protected void createBrowser(Composite parent) {
        browserContainer = new Composite(parent, SWT.NONE);
        browserContainer.setLayout(new StackLayout());
        browserContainer
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Browser b = null;
//        try {
//            // try Mozilla
//            b = createBrowser(browserContainer, SWT.MOZILLA | SWT.BORDER);
//            mozilla = true;
//        } catch (SWTError e) {
//        mozilla = false;
        try {
            // try default browser
            b = createBrowser(browserContainer, SWT.NONE);
        } catch (SWTError e2) {
            if (e2.code != SWT.ERROR_NO_HANDLES) {
                return;
            }
            // show error text
            errorText = new BrowserErrorText(browserContainer, this, e2);
        }
//        }
        if (b != null) {
            this.browser = b;
        }

        mozilla = b != null && "mozilla".equals(b.getBrowserType()); //$NON-NLS-1$
        if (mozilla)
            new MozillaPref(this);

        browserListener = new BrowserListener();

        if (browser != null) {
            browserListener.hook(browser);
            show(browser);
        } else {
            show(errorText.getControl());
        }
    }

    private void show(Control control) {
        ((StackLayout) browserContainer.getLayout()).topControl = control;
        for (Control c : browserContainer.getChildren()) {
            c.setVisible(c == control);
        }
        browserContainer.layout();
    }

//    private void changeBrowser() {
//        if (errorText != null)
//            return;
//
//        ensureAlternateBrowser();
//        if (alternateBrowser == null || alternateBrowser.isDisposed())
//            return;
//
//        browserListener.unhook(browser);
//
//        Browser temp = alternateBrowser;
//        alternateBrowser = browser;
//        browser = temp;
//
//        browserListener.hook(browser);
//        show(browser);
//    }
//
//    private void ensureAlternateBrowser() {
//        if (alternateBrowser != null && !alternateBrowser.isDisposed())
//            return;
//
//        if (errorText != null)
//            return;
//
//        try {
//            if (mozilla) {
//                alternateBrowser = new Browser(browserContainer, SWT.MOZILLA
//                        | SWT.BORDER);
//            } else {
//                alternateBrowser = new Browser(browserContainer, SWT.NONE);
//            }
//
//        } catch (SWTError e) {
//            if (e.code != SWT.ERROR_NO_HANDLES) {
//                return;
//            }
//            errorText = new BrowserErrorText(browserContainer, this, e);
//        }
//
//        if (alternateBrowser != null) {
//            alternateBrowser.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
//                    true, true));
//        } else {
//            errorText.getControl().setLayoutData(
//                    new GridData(SWT.FILL, SWT.FILL, true, true));
//        }
//    }

    private Browser createBrowser(Composite parent, int style) {
        return new Browser(parent, style);
    }

    public void changeStyle(int newStyle) {
        this.style = newStyle;
        if (composite != null && !composite.isDisposed()) {
            updateWithStyle();
        }
    }

    protected void updateWithStyle() {
        boolean locationVisible = (style
                & IBrowserSupport.NO_LOCATION_BAR) == 0;
        location.setVisible(locationVisible);
        ((GridData) location.getLayoutData()).exclude = !locationVisible;

        boolean descriptionVisible = !locationVisible;
        description.setVisible(descriptionVisible);
        ((GridData) description.getLayoutData()).exclude = !descriptionVisible;

        toolBar.layout(true);

        boolean hasToolBar = (style & IBrowserSupport.NO_TOOLBAR) == 0;
        toolBar.setVisible(hasToolBar);
        ((GridData) toolBar.getLayoutData()).exclude = !hasToolBar;

        composite.layout(true);
    }

    public Browser getBrowser() {
        return browser;
    }

    public Control getControl() {
        return composite;
    }

    private void home() {
        if (browser == null || browser.isDisposed())
            return;
        browser.setText(""); //$NON-NLS-1$
    }

    /**
     * Loads a URL.
     * 
     * @param url
     *            the URL to be loaded
     * @return true if the operation was successful and false otherwise.
     * @exception IllegalArgumentException
     *                <ul>
     *                <li>ERROR_NULL_ARGUMENT - if the url is null</li>
     *                </ul>
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong
     *                thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     * @see #getURL()
     */
    public boolean setURL(String url) {
        return setURL(url, true);
    }

    private boolean setURL(String url, boolean browse) {
        if (url == null) {
            home();
            return true;
        }

        if ("xmind".equalsIgnoreCase(url)) //$NON-NLS-1$
            url = URL_HOME;

        if (redirect) {
            url = makeRedirectUrl(url);
        }

        if (browse)
            navigate(url);

        addToHistory(url);
        updateHistory();
        return true;
    }

    protected void updateBackNextBusy() {
        if (backAction != null)
            backAction.setEnabled(isBackEnabled());
        if (forwardAction != null)
            forwardAction.setEnabled(isForwardEnabled());
        if (homeBusy != null && homeBusy.getControl() != null
                && !homeBusy.getControl().isDisposed()) {
            if (loading) {
                homeBusy.start();
            } else {
                homeBusy.stop();
            }
        }
    }

//    /**
//     *
//     */
//    private void addBrowserListeners(Browser browser) {
//        if (browser == null)
//            return;
//
//        browser.addStatusTextListener(new StatusTextListener() {
//            public void changed(StatusTextEvent event) {
//                if (container != null) {
//                    IStatusLineManager status = container.getActionBars()
//                            .getStatusLineManager();
//                    status.setMessage(event.text);
//                }
//            }
//        });
//
//        /*
//         * Add listener for new window creation. Use just the same browser for
//         * the new window.
//         */
//        browser.addOpenWindowListener(new OpenWindowListener() {
//            public void open(WindowEvent event) {
//                event.browser = BrowserViewer.this.browser;
//
//                // Forget about the following way....
//
////                Display display = Display.getCurrent();
////                final Shell tempShell = new Shell(display);
////                tempShell.setBounds(-100, -100, 50, 50);
////                tempShell.setLayout(new FillLayout());
////                BrowserViewer tempBrowserViewer = new BrowserViewer(tempShell,
////                        0, null);
////                tempBrowserViewer.newWindow = true;
////                Browser tempBrowser = tempBrowserViewer.browser;
////                tempBrowser.addLocationListener(new LocationListener() {
////
////                    public void changing(LocationEvent event) {
////                    }
////
////                    public void changed(LocationEvent event) {
////                        String url = event.location;
////                        if (container != null && url != null && !"".equals(url)) { //$NON-NLS-1$
////                            container.openInExternalBrowser(url);
////                        }
////                        tempShell.close();
////                        tempShell.dispose();
////                    }
////
////                });
////                event.browser = tempBrowser;
//            }
//        });
//
//        browser.addVisibilityWindowListener(new VisibilityWindowListener() {
//            public void hide(WindowEvent e) {
//            }
//
//            public void show(WindowEvent e) {
//                Browser browser2 = (Browser) e.widget;
//                if (browser2.getParent().getParent() instanceof Shell) {
//                    Shell shell = (Shell) browser2.getParent().getParent();
//                    if (e.location != null)
//                        shell.setLocation(e.location);
//                    if (e.size != null)
//                        shell.setSize(shell.computeSize(e.size.x, e.size.y));
//                    shell.open();
//                }
//            }
//        });
//
//        browser.addCloseWindowListener(new CloseWindowListener() {
//            public void close(WindowEvent event) {
//                // if shell is not null, it must be a secondary popup window,
//                // else its an editor window
////                if (newWindow) {
////                    getControl().getShell().dispose();
////                } else {
////                    container.close();
////                }
//            }
//        });
//
//        browser.addProgressListener(new ProgressListener() {
//            public void changed(ProgressEvent event) {
//                if (event.total == 0)
//                    return;
//
//                boolean done = (event.current == event.total);
//
//                int percentProgress = event.current * 100 / event.total;
//                if (container != null) {
//                    IProgressMonitor monitor = container.getActionBars()
//                            .getStatusLineManager().getProgressMonitor();
//                    if (done) {
//                        monitor.done();
//                        progressWorked = 0;
//                    } else if (progressWorked == 0) {
//                        monitor.beginTask("", event.total); //$NON-NLS-1$
//                        progressWorked = percentProgress;
//                    } else {
//                        monitor.worked(event.current - progressWorked);
//                        progressWorked = event.current;
//                    }
//                }
//
//                if (!homeBusy.isAnimating() && !done)
//                    setLoading(true);
//                else if (homeBusy.isAnimating() && done) // once the progress hits
//                    // 100 percent, done, set
//                    // busy to false
//                    setLoading(false);
//
//                updateBackNextBusy();
//                updateHistory();
//            }
//
//            public void completed(ProgressEvent event) {
//                if (container != null) {
//                    IProgressMonitor monitor = container.getActionBars()
//                            .getStatusLineManager().getProgressMonitor();
//                    monitor.done();
//                }
//                setLoading(false);
//                updateBackNextBusy();
//                updateHistory();
//            }
//        });
//
//        browser.addLocationListener(new LocationListener() {
//            public void changed(LocationEvent event) {
//                if (!event.top)
//                    return;
//                if (location != null) {
//                    if (!"about:blank".equals(event.location)) { //$NON-NLS-1$
//                        String oldLocation = location.getText();
//                        location.setText(event.location);
//                        addToHistory(event.location);
//                        updateHistory();
//                        firePropertyChangeEvent(PROPERTY_LOCATION,
//                                event.location, oldLocation);
//                    }
//                }
//            }
//
//            public void changing(LocationEvent event) {
//                // do nothing
//            }
//        });
//
//        browser.addTitleListener(new TitleListener() {
//            public void changed(TitleEvent event) {
//                String oldTitle = title;
//                title = event.title;
//                firePropertyChangeEvent(PROPERTY_TITLE, oldTitle, title);
//            }
//        });
//
//    }

    public Combo getLocationBar() {
        return location;
    }

    public Clipboard getClipboard() {
        return clipboard;
    }

    public void copy() {
        if (location == null || location.isDisposed()
                || !location.isFocusControl())
            return;

        location.copy();
    }

    public void cut() {
        if (location == null || location.isDisposed()
                || !location.isFocusControl())
            return;

        location.cut();
    }

    public void delete() {
        if (location == null || location.isDisposed()
                || !location.isFocusControl())
            return;

        String text = location.getText();
        Point selection = location.getSelection();
        if (selection.y > selection.x) {
            text = text.substring(0, selection.x) + text.substring(selection.y);
            location.setText(text);
            location.setSelection(new Point(selection.x, selection.x));
        }
    }

    public void paste() {
        if (location == null || location.isDisposed()
                || !location.isFocusControl())
            return;

        location.paste();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName,
                listener);
    }

    protected void firePropertyChangeEvent(String propertyName, Object oldValue,
            Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue,
                newValue);
    }

    protected boolean firePropertyChangingEvent(String propertyName,
            Object oldValue, Object newValue, boolean doit) {
        PropertyChangingEvent event = new PropertyChangingEvent(this,
                propertyName, oldValue, newValue, doit);
        PropertyChangeListener[] listeners = propertyChangeSupport
                .getPropertyChangeListeners();
        if (firePropertyChangingEvent(listeners, event)) {
            listeners = propertyChangeSupport
                    .getPropertyChangeListeners(propertyName);
            return firePropertyChangingEvent(listeners, event);
        }
        return event.doit;
    }

    private boolean firePropertyChangingEvent(
            PropertyChangeListener[] listeners, PropertyChangingEvent event) {
        for (Object pcl : listeners) {
            if (pcl instanceof IPropertyChangingListener) {
                try {
                    ((IPropertyChangingListener) pcl).propertyChanging(event);
                } catch (Throwable ignore) {
                }
            }
        }
        return event.doit;
    }

    /**
     * Navigate to the next session history item. Convenience method that calls
     * the underlying SWT browser.
     * 
     * @return <code>true</code> if the operation was successful and
     *         <code>false</code> otherwise
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong
     *                thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     */
    public boolean forward() {
        if (browser == null || browser.isDisposed())
            return false;
        boolean forward = browser.forward();
        if (!forward) {

        }
        return forward;
    }

    /**
     * Navigate to the previous session history item. Convenience method that
     * calls the underlying SWT browser.
     * 
     * @return <code>true</code> if the operation was successful and
     *         <code>false</code> otherwise
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong
     *                thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     */
    public boolean back() {
        if (browser == null || browser.isDisposed())
            return false;
        return browser.back();
    }

    /**
     * Returns <code>true</code> if the receiver can navigate to the previous
     * session history item, and <code>false</code> otherwise. Convenience
     * method that calls the underlying SWT browser.
     * 
     * @return the receiver's back command enabled state
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
     *                thread that created the receiver</li>
     *                </ul>
     */
    public boolean isBackEnabled() {
        if (browser == null || browser.isDisposed())
            return false;
        return browser.isBackEnabled();
    }

    /**
     * Returns <code>true</code> if the receiver can navigate to the next
     * session history item, and <code>false</code> otherwise. Convenience
     * method that calls the underlying SWT browser.
     * 
     * @return the receiver's forward command enabled state
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
     *                thread that created the receiver</li>
     *                </ul>
     */
    public boolean isForwardEnabled() {
        if (browser == null || browser.isDisposed())
            return false;
        return browser.isForwardEnabled();
    }

    /**
     * Stop any loading and rendering activity. Convenience method that calls
     * the underlying SWT browser.
     * 
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong
     *                thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     */
    public void stop() {
        if (browser != null && !browser.isDisposed())
            browser.stop();
        setLoading(false);
    }

    public boolean setText(String html) {
        return false;
    }

    private boolean navigate(String url) {
        if (url != null && url.equals(getURL())) {
            refresh();
            return true;
        }
        if (browser != null && !browser.isDisposed())
            return browser.setUrl(url);
        if (errorText != null && errorText.getControl() != null
                && !errorText.getControl().isDisposed())
            return errorText.setUrl(url);
        return false;
    }

    /**
     * Refresh the current page. Convenience method that calls the underlying
     * SWT browser.
     * 
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong
     *                thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     */
    public void refresh() {
        if (browser != null) {
            if (!browser.isDisposed())
                browser.refresh();
        } else {
            if (errorText != null && errorText.getControl() != null
                    && !errorText.getControl().isDisposed())
                errorText.refresh();
        }
        try {
            Thread.sleep(50);
        } catch (Exception ignore) {
        }
    }

    protected void addToHistory(String url) {
        if (URL_HISTORY == null)
            URL_HISTORY = BrowserPref.getInternalWebBrowserHistory();
        int found = -1;
        int size = URL_HISTORY.size();
        for (int i = 0; i < size; i++) {
            String s = URL_HISTORY.get(i);
            if (s.equals(url)) {
                found = i;
                break;
            }
        }

        if (found == -1) {
            if (size >= MAX_HISTORY)
                URL_HISTORY.remove(size - 1);
            URL_HISTORY.add(0, url);
            BrowserPref.setInternalWebBrowserHistory(URL_HISTORY);
        } else if (found != 0) {
            URL_HISTORY.remove(found);
            URL_HISTORY.add(0, url);
            BrowserPref.setInternalWebBrowserHistory(URL_HISTORY);
        }
    }

    protected void handleDispose() {
        for (IBrowserViewerContribution contribution : BrowserContributionManager
                .getInstance().getContributions()) {
            if (contribution instanceof IBrowserViewerContribution2) {
                ((IBrowserViewerContribution2) contribution)
                        .uninstallBrowserListeners(this);
            }
        }
        if (this.container != null) {
            IStatusLineManager manager = this.container.getActionBars()
                    .getStatusLineManager();
            if (manager != null)
                manager.getProgressMonitor().done();
        }
        homeBusy = null;
        browser = null;
        errorText = null;
        if (clipboard != null)
            clipboard.dispose();
        clipboard = null;
    }

    private void setLoading(boolean loading) {
        this.loading = loading;
        if (loading) {
            if (stopRefreshAction != null) {
                stopRefreshAction.setStop();
            }
        } else {
            if (stopRefreshAction != null) {
                stopRefreshAction.setRefresh();
            }
            if (homeBusy != null && homeBusy.getControl() != null
                    && !homeBusy.getControl().isDisposed())
                homeBusy.stop();
            if (container != null) {
                IProgressMonitor monitor = container.getActionBars()
                        .getStatusLineManager().getProgressMonitor();
                monitor.done();
            }
        }
    }

    /**
     * Returns the current URL. Convenience method that calls the underlying SWT
     * browser.
     * 
     * @return the current URL or an empty <code>String</code> if there is no
     *         current URL
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong
     *                thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     * @see #setURL(String)
     */
    public String getURL() {
        if (browser != null)
            return browser.getUrl();
        return errorText.getUrl();
    }

    protected void updateHistory() {
        if (location == null || location.isDisposed())
            return;

        String temp = location.getText();
        if (URL_HISTORY == null)
            URL_HISTORY = BrowserPref.getInternalWebBrowserHistory();

        String[] historyList = new String[URL_HISTORY.size()];
        URL_HISTORY.toArray(historyList);
        location.setItems(historyList);

        location.setText(temp);
    }

    public IBrowserViewerContainer getContainer() {
        return container;
    }

    public void setFocus() {
        if (browser != null && !browser.isDisposed()) {
            if (browser.setFocus()) {
                updateHistory();
            } else if (location != null) {
                location.setFocus();
            }
        }
    }

    /**
     * @return the mozilla
     */
    public boolean isMozilla() {
        return mozilla;
    }

    public BackAction getBackAction() {
        return backAction;
    }

    public ForwardAction getForwardAction() {
        return forwardAction;
    }

    public StopRefreshAction getStopRefreshAction() {
        return stopRefreshAction;
    }

    private static Object getDefaultBusyPictures() {
        if (DEFAULT_BUSY_PICTURES == null) {
            ArrayList<ImageDescriptor> list = new ArrayList<ImageDescriptor>(
                    13);
            list.add(BrowserImages.getImageDescriptor(BrowserImages.XMIND));
            list.addAll(Arrays.asList(BrowserImages.getBusyImages()));
            DEFAULT_BUSY_PICTURES = list.toArray();
        }
        return DEFAULT_BUSY_PICTURES;
    }

    private String makeRedirectUrl(String source) {
        if (!source.startsWith("file:")) { //$NON-NLS-1$
            try {
                return BrowserUtil.makeRedirectURL(source);
            } catch (Throwable e) {
                //ignore
            } finally {
                redirect = false;
            }
        }
        return source;
    }

}
