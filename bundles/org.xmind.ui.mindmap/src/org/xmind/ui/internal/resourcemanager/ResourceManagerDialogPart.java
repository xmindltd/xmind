package org.xmind.ui.internal.resourcemanager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.osgi.framework.Bundle;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.e4models.IContextRunnable;
import org.xmind.ui.internal.e4models.IModelConstants;
import org.xmind.ui.internal.e4models.IModelPartContext;
import org.xmind.ui.internal.e4models.ModelPart;
import org.xmind.ui.tabfolder.MTabFolder;
import org.xmind.ui.tabfolder.MTabItem;

public class ResourceManagerDialogPart extends ModelPart
        implements IModelPartContext {

    private static final int TITLE_AREA_LABEL_MARGIN = 10;
    private static final String RESOURCE_MANAGER_EXTENSION_ID = "org.xmind.ui.resourceManager"; //$NON-NLS-1$
    private static final String DATA_ID = "org.xmind.ui.resourceManager.itemId"; //$NON-NLS-1$
    private static final String ATTR_LABEL = "label"; //$NON-NLS-1$
    private static final String ATTR_ICON_URI = "iconURI"; //$NON-NLS-1$
    private static final String ATTR_CONTRIBUTION_URI = "contributionURI"; //$NON-NLS-1$
    private static final String ATTR_ID = "id"; //$NON-NLS-1$
    private static final String TITLE_IMAGE_PATH = "platform:/plugin/org.xmind.ui.mindmap/icons/title.png"; //$NON-NLS-1$

    private MTabFolder tabFolder;
    private ResourceManager resourceManagerForPart;

    private ArrayList<IResourceManagerDialogPage> registedPages;

    @Override
    protected void createContent(Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().margins(0, 0).spacing(0, 0)
                .applyTo(composite);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        composite.setBackground(parent.getBackground());
        resourceManagerForPart = new LocalResourceManager(
                JFaceResources.getResources(), parent);

        createTitleComposite(composite);
        createContentComposite(composite);

        parent.layout(true, true);
    }

    private void createTitleComposite(Composite parent) {
        Composite title = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false)
                .margins(0, 0).spacing(0, 0).extendedMargins(0, 0, 0, 0)
                .applyTo(title);
        title.setBackground(
                parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        GridDataFactory.fillDefaults().grab(true, false).applyTo(title);
        ((GridData) title.getLayoutData()).heightHint = 70;

        Composite labelsComposite = new Composite(title, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(true)
                .margins(0, 0).spacing(0, 0)
                .extendedMargins(TITLE_AREA_LABEL_MARGIN, 0, 0, 0)
                .applyTo(labelsComposite);
        GridDataFactory.fillDefaults().grab(true, true)
                .applyTo(labelsComposite);
        Label text = new Label(labelsComposite, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, true)
                .align(SWT.FILL, SWT.BOTTOM).applyTo(text);
        text.setFont(JFaceResources.getBannerFont());
        text.setText(MindMapMessages.ResourceManagerPart_title);
        Label message = new Label(labelsComposite, SWT.WRAP);
        GridDataFactory.fillDefaults().grab(true, true)
                .align(SWT.FILL, SWT.CENTER).applyTo(message);
        message.setFont(JFaceResources.getDialogFont());
        message.setText(MindMapMessages.ResourceManagerPart_message);

        Composite imageComposite = new Composite(title, SWT.NONE);
        imageComposite.setLayout(new GridLayout(1, true));

        Label titleImageLabel = new Label(imageComposite, SWT.CENTER);
        GridDataFactory.fillDefaults().grab(true, true)
                .applyTo(titleImageLabel);

        ImageDescriptor imageDescriptor = null;
        try {
            imageDescriptor = ImageDescriptor
                    .createFromURL(new URL(TITLE_IMAGE_PATH));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (imageDescriptor != null) {
            Image img = resourceManagerForPart.createImage(imageDescriptor);
            titleImageLabel.setImage(img);
        } else {
            titleImageLabel.setImage(JFaceResources
                    .getImage(TitleAreaDialog.DLG_IMG_TITLE_BANNER));
        }

    }

    private void createContentComposite(Composite composite) {
        Composite content = new Composite(composite, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false)
                .margins(0, 0).spacing(0, 0).applyTo(content);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(content);

        tabFolder = new MTabFolder(content, SWT.BORDER);
        tabFolder.setStyleProvider(
                new ResourceManagerStyleProvider(resourceManagerForPart));
        tabFolder.setBackground(
                composite.getDisplay().getSystemColor(SWT.COLOR_TRANSPARENT));
        tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        tabFolder.addListener(SWT.Selection, new Listener() {
            public void handleEvent(org.eclipse.swt.widgets.Event event) {
                showPage((MTabItem) event.item);
            }
        });

        final Map<String, String> persistedState = getAdapter(MPart.class)
                .getPersistedState();

        tabFolder.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                MTabItem item = tabFolder.getSelection();
                String pageId = (String) item.getData(DATA_ID);
                if (pageId != null) {
                    persistedState.put(
                            IModelConstants.KEY_MODEL_PART_CURRENT_PAGE_ID,
                            pageId);
                }
            }
        });

        String persistedSelectedPageId = persistedState
                .get(IModelConstants.KEY_MODEL_PART_CURRENT_PAGE_ID);
        MTabItem selectedItem = null;
        for (final IResourceManagerDialogPage page : registedPages) {
            MTabItem item = new MTabItem(tabFolder, SWT.RADIO);
            item.setText(page.getTitle());
            item.setImage(page.getImage());

            item.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e) {
                    Control pageControl = page.getControl();
                    if (pageControl != null) {
                        pageControl.dispose();
                    }
                    page.dispose();
                }
            });

            item.setData(page);
            item.setData(DATA_ID, page.getId());
            if (page.getId().equals(persistedSelectedPageId)) {
                selectedItem = item;
            }
        }

        if (selectedItem == null && tabFolder.getItemCount() > 0) {
            selectedItem = tabFolder.getItem(0);
        }

        tabFolder.setSelection(selectedItem);
        showPage(selectedItem);

    }

    @Override
    protected void init() {
        super.init();
        registedPages = new ArrayList<IResourceManagerDialogPage>();

        IExtensionPoint extPoint = Platform.getExtensionRegistry()
                .getExtensionPoint(RESOURCE_MANAGER_EXTENSION_ID);
        IConfigurationElement[] elements = extPoint.getConfigurationElements();
        for (IConfigurationElement ele : elements) {
            try {
                IResourceManagerDialogPage page = readPage(ele);
                if (page != null) {
                    registedPages.add(page);
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        getAdapter(MPart.class).getContext().set(
                IModelConstants.KEY_MODEL_PART_REFRESH_PAGE,
                new IContextRunnable() {
                    @Override
                    public void run() {
                        if (tabFolder != null && !tabFolder.isDisposed()) {
                            Object page = tabFolder.getSelection().getData();
                            if (page instanceof IResourceManagerDialogPage) {
                                ((IResourceManagerDialogPage) page).refresh();
                            }
                        }
                    }
                });

    }

    private IResourceManagerDialogPage readPage(IConfigurationElement element)
            throws InstantiationException, IllegalAccessException,
            MalformedURLException {
        String id = element.getAttribute(ATTR_ID);
        if (id == null || "".equals(id)) //$NON-NLS-1$
            throw new IllegalArgumentException("No id for page."); //$NON-NLS-1$

        String contributionURI = element.getAttribute(ATTR_CONTRIBUTION_URI);

        if (contributionURI == null
                || !contributionURI.startsWith("bundleclass://")) //$NON-NLS-1$
            throw new IllegalArgumentException(
                    "Invalid contributionURI: " + contributionURI); //$NON-NLS-1$
        String[] contributionPaths = contributionURI.substring(14).split("/"); //$NON-NLS-1$
        if (contributionPaths.length != 2)
            throw new IllegalArgumentException(
                    "Invalid contributionURI: " + contributionURI); //$NON-NLS-1$
        String bundleId = contributionPaths[0];
        String className = contributionPaths[1];
        Class<?> cls;
        try {
            Bundle bundle = Platform.getBundle(bundleId);
            if (bundle == null)
                throw new ClassNotFoundException();
            cls = bundle.loadClass(className);
        } catch (ClassNotFoundException e) {
            // ignore errors caused contribution not found
            return null;
        }
        Object contribution = ContextInjectionFactory.make(cls,
                getAdapter(MPart.class).getContext());
        if (!(contribution instanceof IResourceManagerDialogPage))
            return null;

        final IResourceManagerDialogPage page = (IResourceManagerDialogPage) contribution;
        page.setId(id);

        String label = element.getAttribute(ATTR_LABEL);
        page.setTitle(label);

        ImageDescriptor icon = readIcon(element);
        page.setImageDescriptor(icon);

        return page;
    }

    private ImageDescriptor readIcon(IConfigurationElement element)
            throws MalformedURLException {
        String iconURI = element.getAttribute(ATTR_ICON_URI);
        ImageDescriptor icon = (iconURI == null || "".equals(iconURI)) //$NON-NLS-1$
                ? null : ImageDescriptor.createFromURL(new URL(iconURI));
        return icon;
    }

    @Override
    protected void handleBringToTop() {
        super.handleBringToTop();
        if (tabFolder != null && !tabFolder.isDisposed()) {
            String pageId = getAdapter(MPart.class).getPersistedState()
                    .get(IModelConstants.KEY_MODEL_PART_CURRENT_PAGE_ID);
            MTabItem itemToShow = null;
            MTabItem[] items = tabFolder.getItems();
            for (MTabItem item : items) {
                if (item.getData(DATA_ID).equals(pageId)) {
                    itemToShow = item;
                    break;
                }
            }
            if (itemToShow == null && tabFolder.getItemCount() > 0) {
                itemToShow = tabFolder.getItem(0);
            }
            tabFolder.setSelection(itemToShow);
            showPage(itemToShow);
        }
    }

    private void showPage(MTabItem item) {
        Object resourcePage = item.getData();
        if (resourcePage instanceof IResourceManagerDialogPage) {
            IResourceManagerDialogPage page = (IResourceManagerDialogPage) resourcePage;

            Control control = page.getControl();
            if (control == null || control.isDisposed()) {
                page.createControl(this.tabFolder.getBody());
                item.setControl(page.getControl());
            }
            setSelectionProvider(page.getAdapter(ISelectionProvider.class));
            MPart part = getAdapter(MPart.class);
            part.getPersistedState().put(
                    IModelConstants.KEY_MODEL_PART_CURRENT_PAGE_ID,
                    page.getId());
            part.getContext().set(
                    IModelConstants.KEY_MODEL_PART_CURRENT_PAGE_ID,
                    page.getId());
        }
    }

}
