package org.xmind.ui.internal.e4models;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public abstract class ModelPageContainer extends ModelPage
        implements IModelPageContainer {

    private Composite container;
    private ArrayList<ModelPage> previousPages = new ArrayList<>();
    private ArrayList<ModelPage> nextPages = new ArrayList<>();
    private Control mainPage;

    protected void addPreviousPage(ModelPage page) {
        previousPages.add(page);
        page.setPageContainer(this);
    }

    protected void addNextPage(ModelPage page) {
        nextPages.add(page);
        page.setPageContainer(this);
    }

    @Override
    protected IModelPageContainer getPageContainer() {
        return this;
    }

    @Override
    public void showModelPage() {
        List<ModelPage> pages = new ArrayList<>();
        pages.addAll(previousPages);
        pages.add(this);
        pages.addAll(nextPages);

        ModelPage page = null;
        for (ModelPage mp : pages) {
            if (!mp.isPageComplete()) {
                page = mp;
                break;
            }
        }
        if (container == null || container.isDisposed()) {
            return;
        }
        StackLayout stackLayout = (StackLayout) container.getLayout();
        Control topControl = null;
        if (this == page) {
            if (mainPage == null || mainPage.isDisposed()) {
                mainPage = createMainPage(container);
            }
            topControl = mainPage;
        } else {
            Control control = page.getControl();
            if (control == null || control.isDisposed()) {
                page.createControl(container);
            }
            topControl = page.getControl();
        }
        stackLayout.topControl = topControl;
        container.layout(true);
    }

    @Override
    protected Control doCreateControl(Composite parent) {
        container = new Composite(parent, SWT.NONE);
        StackLayout layout = new StackLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        container.setLayout(layout);
        showModelPage();
        return container;
    }

    protected abstract Control createMainPage(Composite container);

}
