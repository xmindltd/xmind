package org.xmind.ui.views;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.ui.services.IEvaluationService;
import org.xmind.ui.internal.ToolkitPlugin;
import org.xmind.ui.viewers.Messages;

public class ContributedContentsView extends PageBookView
        implements IContributedContentsView {

    private static final String EXT_INSPECTORS = "org.xmind.ui.toolkit.contributedContentsViews"; //$NON-NLS-1$

    private static final Map<String, Map<String, IContributedContentPageFactory>> pageFactoryCache = new HashMap<String, Map<String, IContributedContentPageFactory>>();

    private String pageType = null;

    @Override
    protected IPage createDefaultPage(PageBook book) {
        DefaultContributedContentPage page = new DefaultContributedContentPage(
                true);
        initPage(page);
        page.createControl(book);
        page.setMessage(Messages.ContentsView_NoContent);
//        page.setMessage(
//                "No content available.\nTry opening an editor that provides proper content.");
        return page;
    }

    @Override
    protected PageRec doCreatePage(IWorkbenchPart part) {
        IContributedContentPageFactory pageFactory = getPageFactory(part);
        if (pageFactory != null) {
            IPage page = pageFactory.createInspectorPage(this.pageType, part);
            if (page != null) {
                if (page instanceof IPageBookViewPage) {
                    initPage((IPageBookViewPage) page);
                }
                page.createControl(getPageBook());
                return new PageRec(part, page);
            }
        }
        return null;
    }

    @Override
    protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
        IPage page = pageRecord.page;
        if (page != null)
            page.dispose();
        pageRecord.dispose();
    }

    @Override
    protected IWorkbenchPart getBootstrapPart() {
        IWorkbenchPage wbPage = getSite().getPage();
        if (wbPage != null)
            return wbPage.getActiveEditor();
        return null;
    }

    @Override
    protected boolean isImportant(IWorkbenchPart part) {
        return part != null && (part instanceof IEditorPart);
    }

    public IWorkbenchPart getContributingPart() {
        return getCurrentContributingPart();
    }

    @Override
    public <T> T getAdapter(Class<T> key) {
        if (key == IContributedContentsView.class) {
            return key.cast(this);
        }
        return super.getAdapter(key);
    }

    @Override
    public void setInitializationData(IConfigurationElement cfig,
            String propertyName, Object data) {
        super.setInitializationData(cfig, propertyName, data);

        if (data != null && (data instanceof String)) {
            this.pageType = (String) data;
        }
    }

    protected IContributedContentPageFactory getPageFactory(
            IWorkbenchPart part) {
        if (this.pageType == null)
            return null;

        return getOrCreatePageFactory(part, this.pageType);
    }

    private static synchronized IContributedContentPageFactory getOrCreatePageFactory(
            IWorkbenchPart part, String targetPageType) {
        IContributedContentPageFactory pageFactory = null;
        String targetEditorId = part.getSite().getId();
        Map<String, IContributedContentPageFactory> pageFactoriesByType = pageFactoryCache
                .get(targetEditorId);
        if (pageFactoriesByType != null) {
            pageFactory = pageFactoriesByType.get(targetPageType);
            if (pageFactory != null)
                return pageFactory;
        }

        IExtensionPoint extPoint = Platform.getExtensionRegistry()
                .getExtensionPoint(EXT_INSPECTORS);
        if (extPoint != null) {
            IExtension[] exts = extPoint.getExtensions();
            for (IExtension ext : exts) {
                IConfigurationElement[] extElements = ext
                        .getConfigurationElements();
                for (IConfigurationElement extElement : extElements) {
                    if ("pageFactory".equals(extElement.getName())) { //$NON-NLS-1$
                        String editorId = extElement.getAttribute("editorId"); //$NON-NLS-1$
                        String pageTypes = extElement.getAttribute("types"); //$NON-NLS-1$
                        List<String> pageTypeList = Arrays
                                .asList(pageTypes.split(",")); //$NON-NLS-1$
                        if (targetEditorId.equals(editorId) && pageTypes != null
                                && pageTypeList.contains(targetPageType)) {
                            try {
                                pageFactory = (IContributedContentPageFactory) extElement
                                        .createExecutableExtension("class"); //$NON-NLS-1$
                            } catch (CoreException e) {
                                ToolkitPlugin.getDefault().getLog()
                                        .log(new Status(IStatus.ERROR,
                                                ext.getNamespaceIdentifier(),
                                                "Failed to create class " //$NON-NLS-1$
                                                        + extElement
                                                                .getAttribute(
                                                                        "class"), //$NON-NLS-1$
                                                e));
                            }
                            if (pageFactory != null) {
                                if (pageFactoriesByType == null) {
                                    pageFactoriesByType = new HashMap<String, IContributedContentPageFactory>();
                                    pageFactoryCache.put(targetEditorId,
                                            pageFactoriesByType);
                                }
                                for (String pageType : pageTypeList) {
                                    pageFactoriesByType.put(pageType,
                                            pageFactory);
                                }
                                return pageFactory;
                            }
                        }
                    }
                }
            }
        }

        return getAdapter(part, IContributedContentPageFactory.class);
    }

    private static <T> T getAdapter(Object obj, Class<T> adapter) {
        if (adapter.isInstance(obj))
            return adapter.cast(obj);

        if (obj instanceof IAdaptable) {
            T result = ((IAdaptable) obj).getAdapter(adapter);
            if (result != null)
                return result;
        }

        if (!(obj instanceof PlatformObject)) {
            T result = Platform.getAdapterManager().getAdapter(obj, adapter);
            if (result != null)
                return result;
        }

        return null;
    }

    @Override
    public void partActivated(IWorkbenchPart part) {
        IWorkbenchPart oldContributingPart = getCurrentContributingPart();
        super.partActivated(part);
        IWorkbenchPart newContributingPart = getCurrentContributingPart();
        if (newContributingPart != oldContributingPart) {
            IEvaluationService evaluationService = getSite()
                    .getService(IEvaluationService.class);
            if (evaluationService != null) {
                evaluationService.requestEvaluation(
                        "org.xmind.ui.contributedContentsView.contributingPartId"); //$NON-NLS-1$
            }
        }
    }

}
