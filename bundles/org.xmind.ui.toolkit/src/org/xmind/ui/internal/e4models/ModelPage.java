package org.xmind.ui.internal.e4models;

import org.xmind.ui.views.Page;

public abstract class ModelPage extends Page {

    private IModelPageContainer pageContainer;
    private boolean pageComplete;

    public boolean isPageComplete() {
        return pageComplete;
    }

    protected void setPageComplete(boolean pageComplete) {
        this.pageComplete = pageComplete;
        IModelPageContainer pageContainer = getPageContainer();
        if (pageContainer != null) {
            pageContainer.showModelPage();
        }
    }

    protected IModelPageContainer getPageContainer() {
        return pageContainer;
    }

    public void setPageContainer(IModelPageContainer pageContainer) {
        this.pageContainer = pageContainer;
    }

    public abstract String getModelPageId();

    public abstract String getModelPageTitle();

}
