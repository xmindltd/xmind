package org.xmind.ui.internal.e4models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.osgi.framework.Bundle;

@SuppressWarnings("restriction")
public class MultiPageModelPart extends ViewModelPart {

    public static final String PERSISTED_STATE_PAGES_CONTRIBUTIONURI = "modelPart.pages.contributionUri"; //$NON-NLS-1$
    public static final String KEY_MODEL_PART_CURRENT_PAGE_ID = "org.xmind.ui.modelPart.currentPageId"; //$NON-NLS-1$

    protected static final String OWING_ME = "modelPage"; //$NON-NLS-1$

    private List<ModelPage> registeredModelPageItems;

    private CTabFolder ctf;

    @Override
    protected void createContent(Composite parent) {
        ctf = new CTabFolder(parent, SWT.BORDER);
        ctf.setRenderer(new ViewModelFolderRenderer(ctf));
        ctf.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        ctf.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                showModelPage((CTabItem) e.item);
            }

            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });

        String pageId = getAdapter(MPart.class).getPersistedState()
                .get(KEY_MODEL_PART_CURRENT_PAGE_ID);
        CTabItem currentSelectedItem = null;

        for (final ModelPage modelPage : registeredModelPageItems) {
            CTabItem cti = new CTabItem(ctf, SWT.NONE);
            cti.setData(OWING_ME, modelPage);
            cti.setText(modelPage.getModelPageTitle());

            if (modelPage.getModelPageId().equals(pageId)) {
                currentSelectedItem = cti;
            }
        }

        if (currentSelectedItem == null) {
            currentSelectedItem = ctf.getItem(0);
        }

        ctf.setSelection(currentSelectedItem);
        showModelPage(currentSelectedItem);

        addTopRight(ctf, false);
        adjustViewMenuBar(false);
    }

    @Override
    protected void handleBringToTop() {
        super.handleBringToTop();
        if (ctf != null && !ctf.isDisposed()) {
            String pageId = getAdapter(MPart.class).getPersistedState()
                    .get(KEY_MODEL_PART_CURRENT_PAGE_ID);
            CTabItem itemToShow = null;
            CTabItem[] items = ctf.getItems();
            for (CTabItem item : items) {
                Object modelPage = item.getData(OWING_ME);
                if (modelPage instanceof ModelPage) {
                    if (((ModelPage) modelPage).getModelPageId()
                            .equals(pageId)) {
                        itemToShow = item;
                        break;
                    }
                }
            }
            if (itemToShow == null && ctf.getItemCount() > 0) {
                itemToShow = ctf.getItem(0);
            }
            ctf.setSelection(itemToShow);
            showModelPage(itemToShow);
        }
    }

    private void showModelPage(CTabItem cti) {
        Object modelPage = cti.getData(OWING_ME);
        if (modelPage instanceof ModelPage) {
            ModelPage modelPageItem = (ModelPage) modelPage;
            Control control = modelPageItem.getControl();
            if (control == null || control.isDisposed()) {
                modelPageItem.createControl(ctf);
                cti.setControl(modelPageItem.getControl());
            }
            if (modelPageItem.getControl().getParent() != ctf) {
                control.setParent(ctf);
            }
            setSelectionProvider(
                    modelPageItem.getAdapter(ISelectionProvider.class));

            String pageId = modelPageItem.getModelPageId();
            getAdapter(MPart.class).getPersistedState()
                    .put(KEY_MODEL_PART_CURRENT_PAGE_ID, pageId);
            adjustViewMenuBar(false);
        }
    }

    @Override
    protected MMenu getViewMenu(MPart part) {
        if (ctf == null) {
            return null;
        }
        if (part == null || part.getMenus() == null) {
            return null;
        }
        for (MMenu menu : part.getMenus()) {
            boolean viewMenu = menu.getTags().contains(TAG_VIEW_MENU);
            String pageId = getCurrentModelPageItem().getModelPageId();
            boolean ofThePage = menu.getTags().contains(pageId);
            if (viewMenu && ofThePage) {
                return menu;
            }
        }
        return null;
    }

    private ModelPage getCurrentModelPageItem() {
        return (ModelPage) ctf.getSelection().getData(OWING_ME);
    }

    @Override
    protected void init() {
        super.init();
        registeredModelPageItems = new ArrayList<ModelPage>();

        Map<String, String> persistedState = getAdapter(MPart.class)
                .getPersistedState();
        String originPages = persistedState
                .get(PERSISTED_STATE_PAGES_CONTRIBUTIONURI);
        if (originPages != null) {
            String[] pages = originPages.split(","); //$NON-NLS-1$
            for (String page : pages) {
                ModelPage modelPage = (ModelPage) readPage(page);
                registeredModelPageItems.add(modelPage);
            }
        }
    }

    private Object readPage(String pageUri) {
        String contributionURI = pageUri;
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
        return contribution;

    }

    @Override
    protected void setFocus() {
        super.setFocus();

        if (ctf == null) {
            return;
        }
        ModelPage pageItem = getCurrentModelPageItem();
        pageItem.setFocus();
    }

}
